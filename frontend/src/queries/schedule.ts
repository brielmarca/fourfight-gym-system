import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type {
  AdminScheduleEntry,
  CreateScheduleEntryRequest,
  PublicScheduleEntry,
  UpdateScheduleEntryRequest,
} from "@/types";
import queryKeys from "./query-keys";

export function useSchedule() {
  return useQuery<PublicScheduleEntry[]>({
    queryKey: queryKeys.schedule.public(),
    queryFn: () => api.schedule.getPublic(),
  });
}

export function useAdminSchedule(enabled = true) {
  return useQuery<AdminScheduleEntry[]>({
    queryKey: queryKeys.schedule.admin(),
    queryFn: () => api.schedule.getAdmin(),
    enabled,
  });
}

export function useCreateScheduleEntry() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateScheduleEntryRequest) => api.schedule.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.schedule.admin() });
      queryClient.invalidateQueries({ queryKey: queryKeys.schedule.public() });
    },
  });
}

export function useUpdateScheduleEntry() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateScheduleEntryRequest }) =>
      api.schedule.update(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.schedule.admin() });
      queryClient.invalidateQueries({ queryKey: queryKeys.schedule.public() });
    },
  });
}

export function useDeactivateScheduleEntry() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.schedule.deactivate(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.schedule.admin() });
      queryClient.invalidateQueries({ queryKey: queryKeys.schedule.public() });
    },
  });
}
