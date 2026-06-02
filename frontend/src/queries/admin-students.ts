import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";
import type { AdminStudent, PageResponse } from "@/types";

export function useAdminStudents(page = 0, size = 50) {
  return useQuery<PageResponse<AdminStudent>>({
    queryKey: queryKeys.adminStudents.list(page, size),
    queryFn: () => api.admin.listStudents(page, size),
  });
}
