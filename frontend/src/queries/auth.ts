import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, setTokens, clearTokens, getUser } from "@/lib/api";
import queryKeys from "./query-keys";
import type { RegisterRequest, User } from "@/types";

export function useCurrentUser() {
  return useQuery({
    queryKey: queryKeys.auth.me(),
    queryFn: () => api.auth.me(),
    enabled: !!getUser(),
    staleTime: 1000 * 60 * 30,
  });
}

export function useLoginMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ email, password }: { email: string; password: string }) =>
      api.auth.login(email, password),
    onSuccess: (data) => {
      if (data.accessToken) {
        setTokens(data.accessToken);
      }
      queryClient.invalidateQueries({ queryKey: queryKeys.auth.me() });
    },
  });
}

export function useRegisterMutation() {
  return useMutation({
    mutationFn: (data: RegisterRequest) => api.auth.register(data),
  });
}

export function useLogoutMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => api.auth.logout(),
    onSuccess: () => {
      clearTokens();
      queryClient.clear();
    },
  });
}

export function getCurrentUser(): User | null {
  return getUser();
}

export function checkIsAuthenticated(): boolean {
  const token = getUser();
  return !!token;
}
