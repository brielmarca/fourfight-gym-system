import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";
import type { UpdateAdminGraduationRequest } from "@/types";

export function useAdminGraduations(enabled = true) {
  return useQuery({
    queryKey: queryKeys.graduations.list(),
    queryFn: () => api.admin.listGraduations(),
    enabled,
  });
}

export function useUpdateAdminGraduation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: UpdateAdminGraduationRequest) => api.admin.updateGraduation(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.graduations.list() });
    },
  });
}
