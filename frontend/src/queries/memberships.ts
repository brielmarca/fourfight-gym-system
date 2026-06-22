import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";
import type {
  Membership,
  PageResponse,
  CancelMyMembershipResponse,
  CreateMembershipRequest,
  CheckoutRequest,
  PaymentFormRequest,
  CheckoutResponse,
} from "@/types";

export function useMyMembership() {
  return useQuery<Membership>({
    queryKey: queryKeys.memberships.my(),
    queryFn: () => api.membership.getMy(),
    retry: false,
  });
}

export function useMemberships(page = 0, size = 20) {
  return useQuery<PageResponse<Membership>>({
    queryKey: queryKeys.memberships.list(page, size),
    queryFn: () => api.membership.getAll(page, size),
  });
}

export function useCheckoutStatus(membershipId: string) {
  return useQuery<CheckoutResponse>({
    queryKey: queryKeys.memberships.status(membershipId),
    queryFn: () => api.membership.getStatus(membershipId),
    enabled: !!membershipId,
  });
}

export function useInitiateCheckout() {
  return useMutation({
    mutationFn: (data: CheckoutRequest) => api.membership.initiate(data),
  });
}

export function useProcessPayment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ membershipId, data }: { membershipId: string; data: PaymentFormRequest }) =>
      api.membership.processPayment(membershipId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.memberships.my() });
    },
  });
}

export function useCreateMembership() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateMembershipRequest) => api.membership.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.memberships.list(0, 20) });
    },
  });
}

export function useRenewMembership() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => api.membership.renew(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.memberships.my() });
    },
  });
}

export function useCancelMembership() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => api.membership.cancel(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.memberships.my() });
    },
  });
}

export function useCancelMyMembership() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (reason?: string) => api.membership.cancelMy(reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.memberships.my() });
    },
  });
}
