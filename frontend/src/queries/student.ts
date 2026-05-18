import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";
import type {
  StudentProfile,
  UpdateStudentProfileRequest,
  CreateStudentProfileRequest,
} from "@/types";

export function useMyStudentProfile() {
  return useQuery<StudentProfile>({
    queryKey: queryKeys.studentProfile.me(),
    queryFn: () => api.studentProfile.getMe(),
    retry: false,
  });
}

export function useStudentProfileByUserId(userId: string) {
  return useQuery<StudentProfile>({
    queryKey: [...queryKeys.studentProfile.all, "byUser", userId],
    queryFn: () => api.studentProfile.getByUserId(userId),
    enabled: !!userId,
  });
}

export function useUpdateStudentProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: UpdateStudentProfileRequest) => api.studentProfile.updateMe(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.studentProfile.me() });
    },
  });
}

export function useCreateStudentProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateStudentProfileRequest) => api.studentProfile.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.studentProfile.me() });
    },
  });
}
