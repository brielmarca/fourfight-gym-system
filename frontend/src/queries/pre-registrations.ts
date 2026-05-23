import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";

export function usePreRegistrations(page = 0, size = 50, enabled = true) {
  return useQuery({
    queryKey: queryKeys.preRegistrations.list(page, size),
    queryFn: () => api.admin.listPreRegistrations(page, size),
    enabled,
  });
}

export function usePreRegistrationDetail(id: string, enabled = true) {
  return useQuery({
    queryKey: queryKeys.preRegistrations.detail(id),
    queryFn: () => api.admin.getPreRegistrationById(id),
    enabled: enabled && !!id,
  });
}

export function useImportPreRegistrationsCsv() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (file: File) => api.admin.importPreRegistrationsCsv(file),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.preRegistrations.all });
    },
  });
}
