const DEFAULT_API_BASE = "/api";
const API_BASE = (import.meta.env.VITE_API_URL?.trim() || DEFAULT_API_BASE).replace(/\/+$/, "");
const NETWORK_ERROR_MESSAGE =
  "Não foi possível conectar ao servidor. Verifique se o backend está ativo.";

interface ApiError {
  message: string;
  field?: string;
}

interface TokenPayload {
  sub: string;
  email: string;
  role: string;
  exp: number;
}

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value: string) => void;
  reject: (error: Error) => void;
}> = [];

const publicAuthEndpoints = new Set([
  "/auth/login",
  "/auth/register",
  "/auth/refresh",
  "/auth/mfa/validate",
]);

function unwrapContent<T>(data: T[] | PageResponse<T>): T[] {
  return Array.isArray(data) ? data : (data.content ?? []);
}

function normalizeGymClass(raw: GymClass): GymClass {
  const scheduleDate = raw.schedule ? new Date(raw.schedule) : null;
  const durationMin = raw.durationMin ?? 60;
  const endDate = scheduleDate ? new Date(scheduleDate.getTime() + durationMin * 60_000) : null;
  const timeFromDate = (date: Date | null) =>
    date
      ? date.toLocaleTimeString("en-GB", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        })
      : "";

  return {
    ...raw,
    dayOfWeek: raw.dayOfWeek ?? scheduleDate?.getDay() ?? 0,
    startTime: raw.startTime ?? timeFromDate(scheduleDate),
    endTime: raw.endTime ?? timeFromDate(endDate),
    maxStudents: raw.maxStudents ?? raw.capacity ?? 20,
    isActive: raw.isActive ?? raw.status === "SCHEDULED",
  };
}

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token!);
    }
  });
  failedQueue = [];
};

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const isPublicAuthEndpoint = publicAuthEndpoints.has(endpoint);
  const token = isPublicAuthEndpoint ? null : getAccessToken();

  let response: Response;

  try {
    response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    });
  } catch {
    throw new Error(NETWORK_ERROR_MESSAGE);
  }

  if (response.status === 401 && !isPublicAuthEndpoint && typeof window !== "undefined") {
    if (!isRefreshing) {
      isRefreshing = true;

      try {
        const res = await fetch(`${API_BASE}/auth/refresh`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
        });

        if (!res.ok) throw new Error("Refresh failed");

        const data = await res.json();
        setTokens(data.accessToken);
        processQueue(null, data.accessToken);

        return request<T>(endpoint, options);
      } catch (err) {
        processQueue(err as Error, null);
        clearTokens();
        window.location.href = "/login";
        throw err;
      } finally {
        isRefreshing = false;
      }
    }

    clearTokens();
    window.location.href = "/login";
    throw new Error("Session expired");
  }

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null);

    let errorMessage: string;

    if (errorBody) {
      if (errorBody.fieldErrors && typeof errorBody.fieldErrors === "object") {
        const messages = Object.values(errorBody.fieldErrors).filter(
          (msg): msg is string => typeof msg === "string",
        );
        errorMessage = messages.join("\n");
      } else if (errorBody.detail) {
        errorMessage = errorBody.detail;
      } else if (errorBody.title) {
        errorMessage = errorBody.title;
      } else if (errorBody.message) {
        errorMessage = errorBody.message;
      } else {
        errorMessage = `HTTP ${response.status}`;
      }
    } else {
      errorMessage =
        response.status >= 500
          ? NETWORK_ERROR_MESSAGE
          : "Ocorreu um erro inesperado. Tente novamente.";
    }

    throw new Error(errorMessage);
  }

  if (response.status === 204) return null as T;
  return response.json();
}

// Access token stored in memory (XSS protection)
let memoryAccessToken: string | null = null;

function setTokens(accessToken: string, refreshToken?: string | null) {
  if (typeof window !== "undefined") {
    memoryAccessToken = accessToken;
    // Refresh token should be set as HttpOnly cookie by backend
    // If provided, store only if backend doesn't use HttpOnly cookies
    if (refreshToken) {
      localStorage.setItem("refreshTokenFallback", refreshToken);
    } else {
      localStorage.removeItem("refreshTokenFallback");
    }
  }
}

function clearTokens() {
  if (typeof window !== "undefined") {
    memoryAccessToken = null;
    localStorage.removeItem("refreshTokenFallback");
    // Clear HttpOnly cookie via backend endpoint
    fetch(`${API_BASE}/auth/logout`, {
      method: "POST",
      credentials: "include",
    }).catch(() => {});
  }
}

function getAccessToken(): string | null {
  return memoryAccessToken;
}

function getUser(): { id: string; email: string; role: string } | null {
  const token = getAccessToken();
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split(".")[1])) as TokenPayload;
    return { id: payload.sub, email: payload.email, role: payload.role };
  } catch {
    clearTokens();
    return null;
  }
}

function isAuthenticated(): boolean {
  const token = getAccessToken();
  if (!token) return false;
  try {
    const payload = JSON.parse(atob(token.split(".")[1])) as TokenPayload;
    return payload.exp * 1000 > Date.now();
  } catch {
    return false;
  }
}

function hasRole(roles: string[]): boolean {
  const user = getUser();
  return user ? roles.includes(user.role) : false;
}

export const api = {
  auth: {
    login: (email: string, password: string) =>
      request<TokenResponse>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      }),
    register: (data: RegisterRequest) =>
      request<UserResponse>("/auth/register", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    me: () => request<UserResponse>("/auth/me"),
    refresh: () =>
      request<TokenResponse>("/auth/refresh", {
        method: "POST",
      }),
    logout: async () => {
      try {
        await request("/auth/logout", { method: "POST" });
      } finally {
        clearTokens();
      }
    },
  },

  belts: {
    getAll: () => request<Belt[]>("/belts"),
    getActive: () => request<Belt[]>("/belts/active"),
    getById: (id: string) => request<Belt>(`/belts/${id}`),
    create: (data: CreateBeltRequest) =>
      request<Belt>("/belts", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    update: (id: string, data: CreateBeltRequest) =>
      request<Belt>(`/belts/${id}`, {
        method: "PUT",
        body: JSON.stringify(data),
      }),
    delete: (id: string) => request<void>(`/belts/${id}`, { method: "DELETE" }),
  },

  plans: {
    getAll: () => request<Plan[]>("/plans"),
    getById: (id: string) => request<Plan>(`/plans/${id}`),
    create: (data: CreatePlanRequest) =>
      request<Plan>("/plans", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    update: (id: string, data: UpdatePlanRequest) =>
      request<Plan>(`/plans/${id}`, {
        method: "PUT",
        body: JSON.stringify(data),
      }),
    delete: (id: string) => request<void>(`/plans/${id}`, { method: "DELETE" }),
  },

  studentProfile: {
    getMe: () => request<StudentProfile>("/student-profile/me"),
    getByUserId: (userId: string) => request<StudentProfile>(`/student-profile/by-user/${userId}`),
    updateMe: (data: UpdateStudentProfileRequest) =>
      request<StudentProfile>("/student-profile/me", {
        method: "PUT",
        body: JSON.stringify(data),
      }),
    create: (data: CreateStudentProfileRequest) =>
      request<StudentProfile>("/student-profile", {
        method: "POST",
        body: JSON.stringify(data),
      }),
  },

  attendance: {
    getMy: (startDate: string, endDate: string) =>
      request<AttendanceRecord[]>(`/attendance/my?startDate=${startDate}&endDate=${endDate}`),
    getMyMonthly: () => request<number>("/attendance/my/monthly"),
    getByStudent: (studentId: string, startDate: string, endDate: string) =>
      request<AttendanceRecord[]>(
        `/attendance/student/${studentId}?startDate=${startDate}&endDate=${endDate}`,
      ),
    getByDate: (date: string) => request<AttendanceRecord[]>(`/attendance/date?date=${date}`),
    create: (data: CreateAttendanceRequest) =>
      request<AttendanceRecord>("/attendance", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    update: (id: string, data: CreateAttendanceRequest) =>
      request<AttendanceRecord>(`/attendance/${id}`, {
        method: "PUT",
        body: JSON.stringify(data),
      }),
  },

  membership: {
    getMy: () => request<Membership>("/memberships/me"),
    getAll: (page = 0, size = 20) =>
      request<PageResponse<Membership>>(`/memberships?page=${page}&size=${size}`),
    create: (data: CreateMembershipRequest) =>
      request<Membership>("/memberships", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    renew: (id: string) => request<Membership>(`/memberships/${id}/renew`, { method: "POST" }),
    cancel: (id: string) => request<Membership>(`/memberships/${id}/cancel`, { method: "POST" }),
    initiate: (data: CheckoutRequest) =>
      request<CheckoutResponse>("/checkout", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    processPayment: (membershipId: string, data: PaymentFormRequest) =>
      request<CheckoutResponse>(`/checkout/${membershipId}/payment`, {
        method: "POST",
        body: JSON.stringify(data),
      }),
    getStatus: (membershipId: string) =>
      request<CheckoutResponse>(`/checkout/${membershipId}/status`),
  },

  classes: {
    getAll: async (page = 0, size = 100) => {
      const data = await request<PageResponse<GymClass> | GymClass[]>(
        `/classes?page=${page}&size=${size}`,
      );
      return unwrapContent(data).map(normalizeGymClass);
    },
    getById: async (id: string) => normalizeGymClass(await request<GymClass>(`/classes/${id}`)),
    enroll: (id: string, _userId?: string) =>
      request<ClassEnrollment>(`/classes/${id}/enroll`, { method: "POST" }),
    unenroll: (id: string) => request<void>(`/classes/${id}/unenroll`, { method: "POST" }),
    getRoster: async (id: string, page = 0, size = 50) => {
      const data = await request<PageResponse<ClassEnrollment> | ClassEnrollment[]>(
        `/classes/${id}/roster?page=${page}&size=${size}`,
      );
      return unwrapContent(data);
    },
  },

  classEnrollments: {
    getAll: async () => [] as ClassEnrollment[],
  },

  contacts: {
    create: (data: CreateContactRequest) =>
      request<ContactResponse>("/contacts", {
        method: "POST",
        body: JSON.stringify(data),
      }),
  },

  trialBooking: {
    book: (data: BookTrialRequest, program = "JIU-JITSU") =>
      request<TrialBookingResponse>(`/book-trial?program=${encodeURIComponent(program)}`, {
        method: "POST",
        body: JSON.stringify(data),
      }),
  },

  stripe: {
    createCheckoutSession: (planId: string) =>
      request<{ sessionId: string; checkoutUrl: string }>("/stripe/checkout", {
        method: "POST",
        body: JSON.stringify({ planId }),
      }),
    getSubscription: () =>
      request<{
        id: string;
        planName: string;
        planPrice: number;
        status: string;
        currentPeriodStart: string;
        currentPeriodEnd: string;
        cancelAtPeriodEnd: boolean;
        stripeSubscriptionId: string;
      }>("/stripe/subscription"),
    cancelSubscription: () =>
      request<void>("/stripe/subscription/cancel", {
        method: "POST",
      }),
  },

  isAuthenticated,
};

export { setTokens, clearTokens, getAccessToken, getUser, isAuthenticated, hasRole };

export type {
  TokenResponse,
  UserResponse,
  User,
  Belt,
  StudentProfile,
  AttendanceRecord,
  Membership,
  Plan,
  PageResponse,
  RegisterRequest,
  UpdateUserRequest,
  CreateBeltRequest,
  CreatePlanRequest,
  UpdatePlanRequest,
  UpdateStudentProfileRequest,
  CreateStudentProfileRequest,
  CreateAttendanceRequest,
  CreateMembershipRequest,
  BookTrialRequest,
  TrialBookingResponse,
  CreateContactRequest,
  ContactResponse,
  CheckoutRequest,
  PaymentFormRequest,
  CheckoutResponse,
  GymClass,
  ClassEnrollment,
} from "@/types/api";
