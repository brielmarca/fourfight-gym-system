import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";
import type { AdminGraduationOption, UpdateStudentGraduationRequest } from "@/types";

export function useAdminGraduationOptions(enabled = true) {
  return useQuery<AdminGraduationOption[]>({
    queryKey: queryKeys.graduationOptions.list(),
    queryFn: () => api.admin.listGraduationOptions(),
    enabled,
  });
}

export function useUpdateAdminStudentGraduation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      userId,
      payload,
    }: {
      userId: string;
      payload: UpdateStudentGraduationRequest;
    }) => api.admin.updateStudentGraduation(userId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminStudents.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.graduationOptions.all });
    },
  });
}
