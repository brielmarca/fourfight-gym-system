import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";
import type { GymClass, ClassEnrollment, PageResponse } from "@/types";

export function useClasses(page = 0, size = 100) {
  return useQuery<GymClass[]>({
    queryKey: queryKeys.classes.list(page, size),
    queryFn: () => api.classes.getAll(page, size),
    staleTime: 1000 * 60 * 5,
  });
}

export function useClass(id: string) {
  return useQuery<GymClass>({
    queryKey: queryKeys.classes.detail(id),
    queryFn: () => api.classes.getById(id),
    enabled: !!id,
  });
}

export function useClassRoster(classId: string, page = 0, size = 50) {
  return useQuery<PageResponse<ClassEnrollment> | ClassEnrollment[]>({
    queryKey: queryKeys.classes.roster(classId, page, size),
    queryFn: () => api.classes.getRoster(classId, page, size),
    enabled: !!classId,
  });
}

export function useMyEnrollments() {
  return useQuery<ClassEnrollment[]>({
    queryKey: queryKeys.enrollments.my(),
    queryFn: () => api.classEnrollments.getAll(),
  });
}

export function useEnrollClass() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (classId: string) => api.classes.enroll(classId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.enrollments.my() });
      queryClient.invalidateQueries({ queryKey: queryKeys.classes.list(0, 100) });
    },
  });
}

export function useUnenrollClass() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (classId: string) => api.classes.unenroll(classId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.enrollments.my() });
      queryClient.invalidateQueries({ queryKey: queryKeys.classes.list(0, 100) });
    },
  });
}
