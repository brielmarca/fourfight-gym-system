const DEFAULT_API_BASE = "https://fourfight-gym-system.onrender.com/api";
const RAW_API_URL = import.meta.env.VITE_API_URL?.trim();

let resolvedBase = RAW_API_URL || DEFAULT_API_BASE;

if (resolvedBase.startsWith("http") && !resolvedBase.endsWith("/api")) {
  resolvedBase = resolvedBase.replace(/\/+$/, "") + "/api";
}

const API_BASE = resolvedBase.replace(/\/+$/, "");

console.log("[API] VITE_API_URL:", RAW_API_URL);
console.log("[API] DEFAULT_API_BASE:", DEFAULT_API_BASE);
console.log("[API] RESOLVED BASE:", resolvedBase);
console.log("[API] FINAL API_BASE:", API_BASE);

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
let restorePromise: Promise<boolean> | null = null;

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

  const isFormData = typeof FormData !== "undefined" && options.body instanceof FormData;

  try {
    const finalUrl = `${API_BASE}${endpoint}`;
    console.log("API REQUEST", { endpoint, API_BASE, finalUrl, method: options.method || "GET" });
    response = await fetch(finalUrl, {
      ...options,
      credentials: "include",
      signal: options.signal ?? AbortSignal.timeout(30000),
      headers: {
        ...(isFormData ? {} : { "Content-Type": "application/json" }),
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
          signal: AbortSignal.timeout(10000),
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
    void refreshToken;
  }
}

function clearTokens() {
  if (typeof window !== "undefined") {
    memoryAccessToken = null;
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

async function restoreAuthSession(): Promise<boolean> {
  if (memoryAccessToken && isAuthenticated()) {
    return true;
  }

  if (!restorePromise) {
    restorePromise = (async () => {
      try {
        const response = await fetch(`${API_BASE}/auth/refresh`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          signal: AbortSignal.timeout(10000),
        });

        if (!response.ok) {
          memoryAccessToken = null;
          return false;
        }

        const data = (await response.json()) as TokenResponse;
        if (!data?.accessToken) {
          memoryAccessToken = null;
          return false;
        }

        setTokens(data.accessToken);
        return true;
      } catch {
        memoryAccessToken = null;
        return false;
      } finally {
        restorePromise = null;
      }
    })();
  }

  return restorePromise;
}

export const api = {
  auth: {
    login: (email: string, password: string) => {
      console.log("[AUTH] login called, endpoint: /auth/login");
      return request<TokenResponse>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
    },
    register: (data: RegisterRequest) => {
      console.log("[AUTH] register called, endpoint: /auth/register");
      return request<UserResponse>("/auth/register", {
        method: "POST",
        body: JSON.stringify(data),
      });
    },
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

  schedule: {
    getPublic: () => request<PublicScheduleEntry[]>("/schedule"),
    getAdmin: () => request<AdminScheduleEntry[]>("/admin/schedule"),
    create: (data: CreateScheduleEntryRequest) =>
      request<AdminScheduleEntry>("/admin/schedule", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    update: (id: string, data: UpdateScheduleEntryRequest) =>
      request<AdminScheduleEntry>(`/admin/schedule/${id}`, {
        method: "PUT",
        body: JSON.stringify(data),
      }),
    deactivate: (id: string) =>
      request<AdminScheduleEntry>(`/admin/schedule/${id}/deactivate`, {
        method: "PATCH",
      }),
  },

  admin: {
    listPreRegistrations: (page = 0, size = 50) =>
      request<PageResponse<AdminPreRegistrationListItem>>(
        `/admin/pre-registrations?page=${page}&size=${size}`,
      ),
    getPreRegistrationById: (id: string) =>
      request<AdminPreRegistrationDetail>(`/admin/pre-registrations/${id}`),
    importPreRegistrationsCsv: (file: File) => {
      const formData = new FormData();
      formData.append("file", file);
      return request<{
        totalRows: number;
        importedRows: number;
        duplicateRows: number;
        invalidRows: number;
        issues: string[];
      }>("/admin/pre-registrations/import", {
        method: "POST",
        body: formData,
      });
    },
    listGraduations: () => request<AdminGraduation[]>("/admin/graduations"),
    updateGraduation: (data: UpdateAdminGraduationRequest) =>
      request<AdminGraduation>("/admin/graduations", {
        method: "PUT",
        body: JSON.stringify(data),
      }),
    getProfessors: () => request<ProfessorSummary[]>("/admin/professors"),
    promoteProfessor: (payload: PromoteProfessorRequest) =>
      request<ProfessorSummary>("/admin/professors", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    updateProfessorModalities: (professorId: string, payload: UpdateProfessorModalitiesRequest) =>
      request<ProfessorSummary>(`/admin/professors/${professorId}/modalities`, {
        method: "PUT",
        body: JSON.stringify(payload),
      }),
    getProfessorAssignments: () => request<ProfessorAssignment[]>("/admin/professor-assignments"),
    createProfessorAssignment: (payload: CreateProfessorAssignmentRequest) =>
      request<ProfessorAssignment>("/admin/professor-assignments", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    deactivateProfessorAssignment: (assignmentId: string) =>
      request<void>(`/admin/professor-assignments/${assignmentId}/deactivate`, {
        method: "PATCH",
      }),
  },

  professor: {
    getMyProfessorStudents: () => request<AssignedProfessorStudent[]>("/professor/students"),
  },

  videoLessons: {
    getManage: () => request<VideoLesson[]>("/video-lessons/manage"),
    create: (payload: UpsertVideoLessonRequest) =>
      request<VideoLesson>("/video-lessons", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    update: (id: string, payload: UpsertVideoLessonRequest) =>
      request<VideoLesson>(`/video-lessons/${id}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      }),
    deactivate: (id: string) =>
      request<VideoLesson>(`/video-lessons/${id}/deactivate`, {
        method: "PATCH",
      }),
    getMy: () => request<VideoLesson[]>("/video-lessons/my"),
    getMyById: (id: string) => request<VideoLesson>(`/video-lessons/my/${id}`),
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
    createReceptionRequest: (planId: string) =>
      request<{
        membershipId: string;
        status: string;
        message: string;
      }>("/stripe/reception-request", {
        method: "POST",
        body: JSON.stringify({ planId }),
      }),
    listPendingReceptionRequests: () =>
      request<Array<{
        membershipId: string;
        userName: string;
        userEmail: string;
        planName: string;
        planPrice: number;
        status: string;
        requestedAt: string;
      }>>("/stripe/reception-requests/pending"),
    approveReceptionRequest: (membershipId: string) =>
      request<void>(`/stripe/reception-requests/${membershipId}/approve`, {
        method: "POST",
      }),
    rejectReceptionRequest: (membershipId: string) =>
      request<void>(`/stripe/reception-requests/${membershipId}/reject`, {
        method: "POST",
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

export { setTokens, clearTokens, getAccessToken, getUser, isAuthenticated, hasRole, restoreAuthSession };

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
  PublicScheduleEntry,
  AdminScheduleEntry,
  CreateScheduleEntryRequest,
  UpdateScheduleEntryRequest,
  AdminPreRegistrationListItem,
  AdminPreRegistrationDetail,
  UserRole,
  Modality,
  ProfessorSummary,
  ProfessorAssignment,
  AssignedProfessorStudent,
  PromoteProfessorRequest,
  UpdateProfessorModalitiesRequest,
  CreateProfessorAssignmentRequest,
  VideoLesson,
  UpsertVideoLessonRequest,
} from "@/types/api";
