import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type {
  AssignedProfessorStudent,
  CreateProfessorAssignmentRequest,
  ProfessorAssignment,
  ProfessorSummary,
  PromoteProfessorRequest,
  UpdateProfessorModalitiesRequest,
} from "@/types";
import queryKeys from "./query-keys";

export function useProfessors(enabled = true) {
  return useQuery<ProfessorSummary[]>({
    queryKey: queryKeys.professors.list(),
    queryFn: () => api.admin.getProfessors(),
    enabled,
  });
}

export function usePromoteProfessor() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: PromoteProfessorRequest) => api.admin.promoteProfessor(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.professors.list() });
    },
  });
}

export function useUpdateProfessorModalities() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      professorId,
      payload,
    }: {
      professorId: string;
      payload: UpdateProfessorModalitiesRequest;
    }) => api.admin.updateProfessorModalities(professorId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.professors.list() });
    },
  });
}

export function useProfessorAssignments(enabled = true) {
  return useQuery<ProfessorAssignment[]>({
    queryKey: queryKeys.professors.assignments(),
    queryFn: () => api.admin.getProfessorAssignments(),
    enabled,
  });
}

export function useCreateProfessorAssignment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateProfessorAssignmentRequest) =>
      api.admin.createProfessorAssignment(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.professors.assignments() });
    },
  });
}

export function useDeactivateProfessorAssignment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (assignmentId: string) => api.admin.deactivateProfessorAssignment(assignmentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.professors.assignments() });
    },
  });
}

export function useMyProfessorStudents(enabled = true) {
  return useQuery<AssignedProfessorStudent[]>({
    queryKey: queryKeys.professors.myStudents(),
    queryFn: () => api.professor.getMyProfessorStudents(),
    enabled,
  });
}
