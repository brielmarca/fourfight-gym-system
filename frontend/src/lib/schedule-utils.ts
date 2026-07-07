import type {
  ScheduleClass,
  UserPlan,
  PlanType,
  ScheduleModalityLabel,
  DayOfWeek,
} from "@/types/schedule";

const planHierarchy: Record<PlanType, number> = {
  Basic: 1,
  Standard: 2,
  Premium: 3,
};

/**
 * Check if user's plan can access a given class
 */
export function canAccessClass(userPlan: UserPlan | null, requiredPlan: PlanType): boolean {
  if (!userPlan || !userPlan.active) return false;
  return planHierarchy[userPlan.type] >= planHierarchy[requiredPlan];
}

/**
 * Get the display name for the required plan
 */
export function getPlanDisplayName(plan: PlanType): string {
  const names: Record<PlanType, string> = {
    Basic: "Basic",
    Standard: "Standard",
    Premium: "Premium",
  };
  return names[plan];
}

/**
 * Get the locked message for a class requiring a higher plan
 */
export function getLockedMessage(requiredPlan: PlanType): string {
  return `Disponível no plano ${getPlanDisplayName(requiredPlan)}`;
}

/**
 * Count user's enrolled classes for the current week
 */
export function countWeeklyEnrollments(enrollments: string[]): number {
  return enrollments.length;
}

/**
 * Check if user has reached their weekly class limit
 */
export function hasReachedWeeklyLimit(userPlan: UserPlan | null, currentCount: number): boolean {
  if (!userPlan || !userPlan.active) return true;
  const limits: Record<PlanType, number> = {
    Basic: 8,
    Standard: 20,
    Premium: 100,
  };
  return currentCount >= limits[userPlan.type];
}

/**
 * Filter classes by modality
 */
export function filterByModality(
  classes: ScheduleClass[],
  modality: ScheduleModalityLabel | "Todos",
): ScheduleClass[] {
  if (modality === "Todos") return classes;
  return classes.filter((c) => c.modality === modality);
}

/**
 * Filter classes by day
 */
export function filterByDay(classes: ScheduleClass[], day: DayOfWeek | "Todos"): ScheduleClass[] {
  if (day === "Todos") return classes;
  return classes.filter((c) => c.dayOfWeek === day);
}

/**
 * Filter classes by time range
 */
export function filterByTime(classes: ScheduleClass[], timeFilter: string): ScheduleClass[] {
  if (timeFilter === "Todos") return classes;
  const [startHour] = timeFilter.split(":").map(Number);
  return classes.filter((c) => {
    const classStart = parseInt(c.startTime.split(":")[0]);
    return classStart === startHour;
  });
}

/**
 * Get unique time slots from classes
 */
export function getTimeSlots(classes: ScheduleClass[]): string[] {
  const times = new Set(classes.map((c) => c.startTime));
  return Array.from(times).sort();
}

/**
 * Create a mock user plan - replace with real API later
 */
export function getMockUserPlan(): UserPlan | null {
  // Check if we have a stored preference (for demo purposes)
  const stored = localStorage.getItem("mockUserPlan");
  if (stored) {
    const planType = stored as PlanType;
    const limits: Record<PlanType, number> = {
      Basic: 8,
      Standard: 20,
      Premium: 100,
    };
    return {
      type: planType,
      maxClassesPerWeek: limits[planType],
      active: true,
    };
  }
  return null;
}

/**
 * Set mock user plan (for demo/testing)
 */
export function setMockUserPlan(plan: PlanType | null): void {
  if (plan) {
    localStorage.setItem("mockUserPlan", plan);
  } else {
    localStorage.removeItem("mockUserPlan");
  }
}

/**
 * Convert real membership to UserPlan
 */
export function membershipToUserPlan(
  membership: { planName?: string; status: string } | null,
): UserPlan | null {
  if (!membership || membership.status !== "ACTIVE") return null;

  const planName = membership.planName?.toLowerCase() || "";
  let type: PlanType = "Basic";

  if (planName.includes("premium")) type = "Premium";
  else if (planName.includes("padr") || planName.includes("standard")) type = "Standard";

  const limits: Record<PlanType, number> = {
    Basic: 8,
    Standard: 20,
    Premium: 100,
  };

  return {
    type,
    maxClassesPerWeek: limits[type],
    active: true,
  };
}
