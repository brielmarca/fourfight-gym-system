import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { AdminRegistration, AdminPreRegistrationDetail, PageResponse } from "@/types";
import queryKeys from "./query-keys";

export function usePreRegistrations(
  page = 0,
  size = 50,
  source: "ALL" | "SITE" | "CSV" = "ALL",
  enabled = true,
) {
  return useQuery<PageResponse<AdminRegistration>>({
    queryKey: queryKeys.preRegistrations.list(page, size, source),
    queryFn: () => api.admin.listRegistrations(page, size, source),
    enabled,
  });
}

export function usePreRegistrationDetail(id: string, enabled = true) {
  return useQuery<AdminPreRegistrationDetail>({
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

export function useAcceptPreRegistration() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.admin.acceptPreRegistration(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.preRegistrations.all });
    },
  });
}

export function useArchivePreRegistration() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.admin.archivePreRegistration(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.preRegistrations.all });
    },
  });
}
