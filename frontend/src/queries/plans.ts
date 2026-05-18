import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";
import type { Plan, CreatePlanRequest, UpdatePlanRequest } from "@/types";

export function usePlans() {
  return useQuery<Plan[]>({
    queryKey: queryKeys.plans.list(),
    queryFn: () => api.plans.getAll(),
    staleTime: 1000 * 60 * 10,
  });
}

export function usePlan(id: string) {
  return useQuery<Plan>({
    queryKey: queryKeys.plans.detail(id),
    queryFn: () => api.plans.getById(id),
    enabled: !!id,
  });
}

export function useCreatePlan() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreatePlanRequest) => api.plans.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.plans.list() });
    },
  });
}

export function useUpdatePlan() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdatePlanRequest }) =>
      api.plans.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.plans.list() });
    },
  });
}

export function useDeletePlan() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => api.plans.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.plans.list() });
    },
  });
}
