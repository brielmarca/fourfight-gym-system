import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";
import type { AttendanceRecord, CreateAttendanceRequest } from "@/types";

export function useMyMonthlyAttendance() {
  return useQuery<number>({
    queryKey: queryKeys.attendance.myMonthly(),
    queryFn: () => api.attendance.getMyMonthly(),
    staleTime: 1000 * 60 * 5,
  });
}

export function useMyAttendance(startDate: string, endDate: string) {
  return useQuery<AttendanceRecord[]>({
    queryKey: queryKeys.attendance.my(startDate, endDate),
    queryFn: () => api.attendance.getMy(startDate, endDate),
    enabled: !!startDate && !!endDate,
  });
}

export function useAttendanceByDate(date: string) {
  return useQuery<AttendanceRecord[]>({
    queryKey: [...queryKeys.attendance.all, "byDate", date],
    queryFn: () => api.attendance.getByDate(date),
    enabled: !!date,
  });
}

export function useCreateAttendance() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateAttendanceRequest) => api.attendance.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.attendance.myMonthly() });
    },
  });
}

export function useUpdateAttendance() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: CreateAttendanceRequest }) =>
      api.attendance.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.attendance.myMonthly() });
    },
  });
}
