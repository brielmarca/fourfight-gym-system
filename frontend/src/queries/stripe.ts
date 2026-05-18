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
