import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import queryKeys from "./query-keys";

interface StripeCheckoutResponse {
  sessionId: string;
  checkoutUrl: string;
}

interface StripeSubscriptionResponse {
  id: string;
  planName: string;
  planPrice: number;
  status: string;
  currentPeriodStart: string;
  currentPeriodEnd: string;
  cancelAtPeriodEnd: boolean;
  stripeSubscriptionId: string;
}

interface ReceptionRequestItem {
  membershipId: string;
  userName: string;
  userEmail: string;
  planName: string;
  planPrice: number;
  status: string;
  requestedAt: string;
}

export function useStripeCheckout() {
  return useMutation({
    mutationFn: (planId: string) =>
      api.stripe.createCheckoutSession(planId),
  });
}

export function useStripeSubscription() {
  return useQuery<StripeSubscriptionResponse>({
    queryKey: queryKeys.stripe.subscription(),
    queryFn: () => api.stripe.getSubscription(),
    retry: false,
  });
}

export function useCancelStripeSubscription() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => api.stripe.cancelSubscription(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.stripe.subscription() });
      queryClient.invalidateQueries({ queryKey: queryKeys.memberships.my() });
    },
  });
}

export function useCreateReceptionRequest() {
  return useMutation({
    mutationFn: (planId: string) => api.stripe.createReceptionRequest(planId),
  });
}

export function usePendingReceptionRequests(enabled = true) {
  return useQuery<ReceptionRequestItem[]>({
    queryKey: queryKeys.stripe.receptionPending(),
    queryFn: () => api.stripe.listPendingReceptionRequests(),
    enabled,
  });
}

export function useApproveReceptionRequest() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (membershipId: string) => api.stripe.approveReceptionRequest(membershipId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.stripe.receptionPending() });
      queryClient.invalidateQueries({ queryKey: queryKeys.memberships.list(0, 50) });
    },
  });
}

export function useRejectReceptionRequest() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (membershipId: string) => api.stripe.rejectReceptionRequest(membershipId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.stripe.receptionPending() });
      queryClient.invalidateQueries({ queryKey: queryKeys.memberships.list(0, 50) });
    },
  });
}
