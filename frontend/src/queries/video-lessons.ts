import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { UpsertVideoLessonRequest } from "@/types";
import queryKeys from "./query-keys";

export function useManageVideoLessons(enabled = true) {
  return useQuery({
    queryKey: queryKeys.videoLessons.manage(),
    queryFn: () => api.videoLessons.getManage(),
    enabled,
  });
}

export function useMyVideoLessons(enabled = true) {
  return useQuery({
    queryKey: queryKeys.videoLessons.my(),
    queryFn: () => api.videoLessons.getMy(),
    enabled,
  });
}

export function useMyVideoLesson(id: string, enabled = true) {
  return useQuery({
    queryKey: queryKeys.videoLessons.detail(id),
    queryFn: () => api.videoLessons.getMyById(id),
    enabled: enabled && !!id,
  });
}

export function useCreateVideoLesson() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ file, payload }: { file: File; payload: UpsertVideoLessonRequest }) =>
      api.videoLessons.upload(file, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.videoLessons.manage() });
      queryClient.invalidateQueries({ queryKey: queryKeys.videoLessons.my() });
    },
  });
}

export function useUpdateVideoLesson() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpsertVideoLessonRequest }) =>
      api.videoLessons.update(id, payload),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.videoLessons.manage() });
      queryClient.invalidateQueries({ queryKey: queryKeys.videoLessons.my() });
      queryClient.invalidateQueries({ queryKey: queryKeys.videoLessons.detail(variables.id) });
    },
  });
}

export function useDeactivateVideoLesson() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.videoLessons.deactivate(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.videoLessons.manage() });
      queryClient.invalidateQueries({ queryKey: queryKeys.videoLessons.my() });
      queryClient.invalidateQueries({ queryKey: queryKeys.videoLessons.detail(id) });
    },
  });
}
