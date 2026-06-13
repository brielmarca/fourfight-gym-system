const queryKeys = {
  auth: {
    all: ["auth"] as const,
    me: () => [...queryKeys.auth.all, "me"] as const,
  },
  plans: {
    all: ["plans"] as const,
    list: () => [...queryKeys.plans.all, "list"] as const,
    detail: (id: string) => [...queryKeys.plans.all, "detail", id] as const,
  },
  memberships: {
    all: ["memberships"] as const,
    my: () => [...queryKeys.memberships.all, "my"] as const,
    list: (page: number, size: number) =>
      [...queryKeys.memberships.all, "list", page, size] as const,
    status: (id: string) => [...queryKeys.memberships.all, "status", id] as const,
  },
  classes: {
    all: ["classes"] as const,
    list: (page: number, size: number) => [...queryKeys.classes.all, "list", page, size] as const,
    detail: (id: string) => [...queryKeys.classes.all, "detail", id] as const,
    roster: (id: string, page: number, size: number) =>
      [...queryKeys.classes.all, "roster", id, page, size] as const,
  },
  enrollments: {
    all: ["enrollments"] as const,
    my: () => [...queryKeys.enrollments.all, "my"] as const,
  },
  studentProfile: {
    all: ["studentProfile"] as const,
    me: () => [...queryKeys.studentProfile.all, "me"] as const,
  },
  attendance: {
    all: ["attendance"] as const,
    myMonthly: () => [...queryKeys.attendance.all, "myMonthly"] as const,
    my: (startDate: string, endDate: string) =>
      [...queryKeys.attendance.all, "my", startDate, endDate] as const,
  },
  belts: {
    all: ["belts"] as const,
    list: () => [...queryKeys.belts.all, "list"] as const,
    active: () => [...queryKeys.belts.all, "active"] as const,
    detail: (id: string) => [...queryKeys.belts.all, "detail", id] as const,
  },
  stripe: {
    all: ["stripe"] as const,
    subscription: () => [...queryKeys.stripe.all, "subscription"] as const,
    receptionPending: () => [...queryKeys.stripe.all, "reception-pending"] as const,
  },
  schedule: {
    all: ["schedule"] as const,
    public: () => [...queryKeys.schedule.all, "public"] as const,
    admin: () => [...queryKeys.schedule.all, "admin"] as const,
  },
  preRegistrations: {
    all: ["preRegistrations"] as const,
    list: (page: number, size: number) => [...queryKeys.preRegistrations.all, "list", page, size] as const,
    detail: (id: string) => [...queryKeys.preRegistrations.all, "detail", id] as const,
  },
  graduations: {
    all: ["graduations"] as const,
    list: () => [...queryKeys.graduations.all, "list"] as const,
  },
  graduationOptions: {
    all: ["graduationOptions"] as const,
    list: () => [...queryKeys.graduationOptions.all, "list"] as const,
  },
  adminStudents: {
    all: ["adminStudents"] as const,
    list: (page: number, size: number) => [...queryKeys.adminStudents.all, "list", page, size] as const,
  },
  professors: {
    all: ["professors"] as const,
    list: () => [...queryKeys.professors.all, "list"] as const,
    assignments: () => [...queryKeys.professors.all, "assignments"] as const,
    myStudents: () => [...queryKeys.professors.all, "my-students"] as const,
  },
  videoLessons: {
    all: ["videoLessons"] as const,
    manage: () => [...queryKeys.videoLessons.all, "manage"] as const,
    my: () => [...queryKeys.videoLessons.all, "my"] as const,
    detail: (id: string) => [...queryKeys.videoLessons.all, "detail", id] as const,
  },
};

export default queryKeys;
