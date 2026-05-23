export interface TokenResponse {
  accessToken: string;
  refreshToken?: string | null;
  expiresIn: number;
}

export interface UserResponse {
  id: string;
  name: string;
  email: string;
  role: string;
  phone?: string;
  avatarUrl?: string;
}

export interface User {
  id: string;
  email: string;
  role: string;
}

export interface Belt {
  id: string;
  name: string;
  colorHex: string;
  rankOrder: number;
}

export interface StudentProfile {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  beltId: string | null;
  beltName: string | null;
  beltColorHex: string | null;
  trainingDays: string | null;
  emergencyContact: string | null;
  emergencyPhone: string | null;
  medicalNotes: string | null;
  recoveryNotes: string | null;
  goals: string | null;
  observations: string | null;
  isActive: boolean;
}

export interface AttendanceRecord {
  id: string;
  studentId: string;
  studentName: string;
  date: string;
  present: boolean;
  classId: string | null;
  notes: string | null;
}

export interface Membership {
  id: string;
  userId: string;
  userName: string;
  planId: string;
  planName: string;
  plan?: Plan | null;
  startDate: string;
  endDate: string;
  status: "ACTIVE" | "EXPIRED" | "CANCELLED" | "SUSPENDED";
  autoRenew: boolean;
}

export interface Plan {
  id: string;
  name: string;
  description: string | null;
  price: number;
  currency?: string;
  durationDays: number;
  maxClasses: number | null;
  features?: string[];
  level?: string | null;
  instructor?: string | null;
  schedule?: string[] | null;
  isActive: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface GymClass {
  id: string;
  trainerId?: string;
  trainerName?: string;
  name: string;
  type?: string;
  description?: string | null;
  dayOfWeek?: number;
  startTime?: string;
  endTime?: string;
  maxStudents?: number;
  capacity?: number;
  enrolledCount?: number;
  schedule?: string;
  durationMin?: number;
  status?: string;
  isActive?: boolean;
}

export type ScheduleModality = "JIU_JITSU" | "BOXE_KICKBOXING" | "CAPOEIRA" | "MMA";
export type ScheduleDayOfWeek =
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";
export type ScheduleLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED" | "ALL_LEVELS";

export interface ScheduleEntryBase {
  title: string;
  modality: ScheduleModality;
  dayOfWeek: ScheduleDayOfWeek;
  startTime: string;
  endTime: string;
  instructorName: string;
  level: ScheduleLevel;
  location?: string | null;
  capacity?: number | null;
  active?: boolean;
  notes?: string | null;
}

export interface PublicScheduleEntry extends ScheduleEntryBase {
  id: string;
}

export interface AdminScheduleEntry extends ScheduleEntryBase {
  id: string;
  createdAt: string;
  updatedAt: string;
}

export type CreateScheduleEntryRequest = ScheduleEntryBase;
export type UpdateScheduleEntryRequest = Partial<ScheduleEntryBase>;

export interface ClassEnrollment {
  id: string;
  studentId?: string;
  studentName?: string;
  userId?: string;
  userName?: string;
  classId: string;
  enrolledAt: string;
  attended?: boolean;
  cancelledAt?: string | null;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone: string;
  dateOfBirth?: string;
  age: number;
  parishOrArea: string;
  hasMartialArtsExperience: boolean;
  martialArtsExperienceDetails?: string;
  trainingGoal: string;
  preferredModality: PreferredModality;
  preferredModalityOther?: string;
  preferredTrainingTime: PreferredTrainingTime;
  preferredTrainingTimeOther?: string;
  preferredTrainingDays: PreferredTrainingDay[];
  valuesMartialArtsPhilosophy: boolean;
  preferredContactMethod: PreferredContactMethod;
  preferredContactMethodOther?: string;
}

export type PreferredModality =
  | "KICKBOXING"
  | "JIU_JITSU"
  | "CAPOEIRA"
  | "BOXE"
  | "MMA"
  | "JIU_JITSU_KIDS"
  | "CAPOEIRA_KIDS"
  | "KICKBOXING_KIDS"
  | "OTHER";

export type PreferredTrainingTime =
  | "MORNING_BEFORE_0830"
  | "LUNCH_1230"
  | "AFTERNOON_14_17"
  | "NIGHT_AFTER_18"
  | "OTHER";

export type PreferredTrainingDay = "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY";

export type PreferredContactMethod = "CALL" | "MESSAGE" | "OTHER";

export interface AdminPreRegistrationListItem {
  id: string;
  fullName: string;
  phone: string;
  age: number | null;
  parish: string | null;
  preferredModalities: string | null;
  preferredTrainingTimes: string | null;
  preferredTrainingDays: string | null;
  preferredContactMethod: string | null;
  submittedAt: string;
  status: string;
}

export interface AdminPreRegistrationDetail extends AdminPreRegistrationListItem {
  hasMartialArtsExperience: boolean | null;
  martialArtsExperienceDetails?: string;
  trainingGoal?: string;
  philosophyImportant: boolean | null;
  source: string;
  notes?: string;
  updatedAt: string;
  createdAt: string;
}

export interface UpdateUserRequest {
  name?: string;
  phone?: string;
  avatarUrl?: string;
}

export interface CreateBeltRequest {
  name: string;
  colorHex?: string;
  rankOrder: number;
}

export interface CreatePlanRequest {
  name: string;
  description?: string | null;
  price: number;
  currency?: string;
  durationDays: number;
  maxClasses?: number | null;
  features?: string[];
  level?: string | null;
  instructor?: string | null;
  schedule?: string[] | null;
}

export type UpdatePlanRequest = Partial<CreatePlanRequest> & {
  isActive?: boolean;
};

export interface UpdateStudentProfileRequest {
  beltId?: string;
  trainingDays?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
  medicalNotes?: string;
  recoveryNotes?: string;
  goals?: string;
  observations?: string;
}

export interface CreateStudentProfileRequest {
  userId: string;
  beltId?: string;
  trainingDays?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
  medicalNotes?: string;
  recoveryNotes?: string;
  goals?: string;
  observations?: string;
}

export interface CreateAttendanceRequest {
  studentId: string;
  date: string;
  present: boolean;
  classId?: string;
  notes?: string;
}

export interface CreateMembershipRequest {
  userId: string;
  planId: string;
  startDate: string;
  autoRenew?: boolean;
}

export interface BookTrialRequest {
  name: string;
  email: string;
  phone?: string;
}

export interface TrialBookingResponse {
  id: string;
  name: string;
  email: string;
  phone?: string;
  program: string;
  status: string;
  createdAt: string;
}

export interface CreateContactRequest {
  name: string;
  email: string;
  phone?: string;
  subject: string;
  message: string;
}

export interface ContactResponse {
  id: string;
  name: string;
  email: string;
  phone?: string;
  subject: string;
  message: string;
  status: string;
  createdAt: string;
}

export interface CheckoutRequest {
  name: string;
  email: string;
  password: string;
  planId: string;
  paymentMethod: string;
}

export interface PaymentFormRequest {
  phoneNumber?: string;
  cardHolderName?: string;
  cardNumber?: string;
  expirationDate?: string;
  cvv?: string;
  paymentId: string;
}

export interface CheckoutResponse {
  id: string;
  name: string;
  email: string;
  planName: string;
  planPrice: number;
  paymentMethod: string;
  paymentStatus: string;
  message: string;
  userId?: string;
  accessToken?: string;
  refreshToken?: string;
  createdAt?: string;
}
