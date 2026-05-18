import { useMutation } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { CreateContactRequest, BookTrialRequest } from "@/types";

export function useCreateContact() {
  return useMutation({
    mutationFn: (data: CreateContactRequest) => api.contacts.create(data),
  });
}

export function useBookTrial() {
  return useMutation({
    mutationFn: ({ data, program }: { data: BookTrialRequest; program?: string }) =>
      api.trialBooking.book(data, program),
  });
}
