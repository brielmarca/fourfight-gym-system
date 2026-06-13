import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";
import type { AdminStudent, DeactivateStudentRequest, PageResponse } from "@/types";

export function useAdminStudents(page = 0, size = 50) {
  return useQuery<PageResponse<AdminStudent>>({
    queryKey: queryKeys.adminStudents.list(page, size),
    queryFn: () => api.admin.listStudents(page, size),
  });
}

export function useDeactivateAdminStudent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ userId, payload }: { userId: string; payload: DeactivateStudentRequest }) =>
      api.admin.deactivateStudent(userId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminStudents.all });
    },
  });
}
