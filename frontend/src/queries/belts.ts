import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";
import type { Belt, CreateBeltRequest } from "@/types";

export function useBelts() {
  return useQuery<Belt[]>({
    queryKey: queryKeys.belts.list(),
    queryFn: () => api.belts.getAll(),
    staleTime: 1000 * 60 * 10,
  });
}

export function useActiveBelts() {
  return useQuery<Belt[]>({
    queryKey: queryKeys.belts.active(),
    queryFn: () => api.belts.getActive(),
    staleTime: 1000 * 60 * 10,
  });
}

export function useBelt(id: string) {
  return useQuery<Belt>({
    queryKey: queryKeys.belts.detail(id),
    queryFn: () => api.belts.getById(id),
    enabled: !!id,
  });
}

export function useCreateBelt() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateBeltRequest) => api.belts.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.belts.list() });
    },
  });
}

export function useUpdateBelt() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: CreateBeltRequest }) =>
      api.belts.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.belts.list() });
    },
  });
}

export function useDeleteBelt() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => api.belts.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.belts.list() });
    },
  });
}
