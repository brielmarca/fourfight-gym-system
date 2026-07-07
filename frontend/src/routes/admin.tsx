import { useState } from "react";
import { Link, createFileRoute, redirect, useNavigate } from "@tanstack/react-router";
import { useAuth } from "@/contexts/use-auth";
import { isAuthenticated, getUser } from "@/lib/api";
import {
  useApproveReceptionRequest,
  useAdminGraduations,
  useAdminStudents,
  usePendingReceptionRequests,
  usePlans,
  useRejectReceptionRequest,
  useAdminSchedule,
  useCreateScheduleEntry,
  useDeactivateScheduleEntry,
  useUpdateScheduleEntry,
  usePreRegistrations,
  usePreRegistrationDetail,
  useImportPreRegistrationsCsv,
  useAcceptPreRegistration,
  useArchivePreRegistration,
  useUpdateAdminGraduation,
  useProfessors,
  usePromoteProfessor,
  useUpdateProfessorModalities,
  useProfessorAssignments,
  useCreateProfessorAssignment,
  useDeactivateProfessorAssignment,
  useManageVideoLessons,
  useCreateVideoLesson,
  useUpdateVideoLesson,
  useDeactivateVideoLesson,
  useDeactivateAdminStudent,
  useAdminGraduationOptions,
  useUpdateAdminStudentGraduation,
} from "@/queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Loader2, LogOut, Users, Shield, CreditCard, Clock, MoreHorizontal } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import type { CreateScheduleEntryRequest, GraduationModality, Modality } from "@/types";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export const Route = createFileRoute("/admin")({
  beforeLoad: () => {
    if (!isAuthenticated()) {
      throw redirect({ to: "/login", search: { redirect: "/admin" } });
    }
  },
  component: AdminPage,
});

type MembershipItem = {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  planName?: string | null;
  startDate?: string | null;
  endDate?: string | null;
  status: string;
};

function AdminPage() {
  const navigate = useNavigate();
  const { user, hasRole, logout } = useAuth();
  const studentsPageSize = 50;
  const [studentsPage, setStudentsPage] = useState(0);
  const { data: graduations = [], isLoading: graduationsLoading } = useAdminGraduations(
    hasRole(["ADMIN", "MANAGER"]),
  );
  const updateGraduation = useUpdateAdminGraduation();
  const {
    data: membershipsData,
    isLoading: membershipsLoading,
    error: membershipsError,
  } = useAdminStudents(studentsPage, studentsPageSize);
  const deactivateAdminStudent = useDeactivateAdminStudent();
  const { data: graduationOptions = [], isLoading: graduationOptionsLoading } =
    useAdminGraduationOptions(hasRole(["ADMIN"]));
  const updateStudentGraduation = useUpdateAdminStudentGraduation();
  const { data: plans = [], isLoading: plansLoading } = usePlans();
  const canManageReception = hasRole(["ADMIN"]);
  const canDeactivateStudents = hasRole(["ADMIN"]);
  const canManageProfessors = hasRole(["ADMIN", "MANAGER"]);
  const { data: pendingReception = [], isLoading: pendingReceptionLoading } =
    usePendingReceptionRequests(canManageReception);
  const approveReception = useApproveReceptionRequest();
  const rejectReception = useRejectReceptionRequest();
  const { data: adminSchedule = [], isLoading: adminScheduleLoading } = useAdminSchedule(
    hasRole(["ADMIN"]),
  );
  const { data: professors = [], isLoading: professorsLoading } =
    useProfessors(canManageProfessors);
  const { data: professorAssignments = [], isLoading: assignmentsLoading } =
    useProfessorAssignments(canManageProfessors);
  const promoteProfessor = usePromoteProfessor();
  const updateProfessorModalities = useUpdateProfessorModalities();
  const createProfessorAssignment = useCreateProfessorAssignment();
  const deactivateProfessorAssignment = useDeactivateProfessorAssignment();
  const createSchedule = useCreateScheduleEntry();
  const { data: manageVideoLessons = [], isLoading: videoLessonsLoading } =
    useManageVideoLessons(canManageProfessors);
  const createVideoLesson = useCreateVideoLesson();
  const updateVideoLesson = useUpdateVideoLesson();
  const deactivateVideoLesson = useDeactivateVideoLesson();
  const updateSchedule = useUpdateScheduleEntry();
  const deactivateSchedule = useDeactivateScheduleEntry();
  const [editingId, setEditingId] = useState<string | null>(null);
  const [studentsFilter, setStudentsFilter] = useState<
    "ALL" | "ACTIVE" | "REGISTERED" | "CANCELLED" | "BASIC" | "STANDARD" | "PREMIUM"
  >("ALL");
  const [selectedPreRegistrationId, setSelectedPreRegistrationId] = useState<string>("");
  const [selectedCsvFile, setSelectedCsvFile] = useState<File | null>(null);
  const [graduationFilter, setGraduationFilter] = useState<
    "ALL" | "JIU_JITSU" | "BOXE_KICKBOXING" | "CAPOEIRA" | "MMA"
  >("ALL");
  const [selectedLevels, setSelectedLevels] = useState<Record<string, string>>({});
  const [importFeedback, setImportFeedback] = useState<string>("");
  const [formError, setFormError] = useState<string | null>(null);
  const [professorEmail, setProfessorEmail] = useState("");
  const [selectedProfessorModalities, setSelectedProfessorModalities] = useState<Modality[]>([]);
  const [assignmentProfessorId, setAssignmentProfessorId] = useState<string>("");
  const [assignmentStudentId, setAssignmentStudentId] = useState<string>("");
  const [assignmentModality, setAssignmentModality] = useState<Modality>("JIU_JITSU");
  const [assignmentNotes, setAssignmentNotes] = useState("");
  const [professorFeedback, setProfessorFeedback] = useState<string | null>(null);
  const [assignmentFeedback, setAssignmentFeedback] = useState<string | null>(null);
  const [studentToDeactivate, setStudentToDeactivate] = useState<MembershipItem | null>(null);
  const [deactivationReason, setDeactivationReason] = useState("");
  const [deactivationFeedback, setDeactivationFeedback] = useState<string | null>(null);
  const [studentToEditGraduation, setStudentToEditGraduation] = useState<MembershipItem | null>(
    null,
  );
  const [editGraduationModality, setEditGraduationModality] = useState<string>("");
  const [editGraduationId, setEditGraduationId] = useState<string>("");
  const [editGraduationReason, setEditGraduationReason] = useState("");
  const [editGraduationFeedback, setEditGraduationFeedback] = useState<string | null>(null);
  const [editingVideoLessonId, setEditingVideoLessonId] = useState<string | null>(null);
  const [videoLessonFeedback, setVideoLessonFeedback] = useState<string | null>(null);
  const [videoFile, setVideoFile] = useState<File | null>(null);
  const [videoForm, setVideoForm] = useState({
    title: "",
    description: "",
    modality: "JIU_JITSU" as Modality,
    minimumPlanRank: 1 as 1 | 2 | 3,
    active: true,
  });
  const importPreRegistrations = useImportPreRegistrationsCsv();
  const acceptPreRegistration = useAcceptPreRegistration();
  const archivePreRegistration = useArchivePreRegistration();
  const [form, setForm] = useState<CreateScheduleEntryRequest>({
    title: "",
    modality: "JIU_JITSU",
    dayOfWeek: "MONDAY",
    startTime: "19:00",
    endTime: "20:00",
    instructorName: "",
    level: "ALL_LEVELS",
    location: "",
    capacity: null,
    notes: "",
    active: true,
  });

  const loading =
    graduationsLoading || membershipsLoading || plansLoading || pendingReceptionLoading;
  const { data: preRegistrationsData, isLoading: preRegistrationsLoading } = usePreRegistrations(
    0,
    100,
    canManageReception,
  );
  const { data: preRegistrationDetail } = usePreRegistrationDetail(
    selectedPreRegistrationId,
    canManageReception && !!selectedPreRegistrationId,
  );

  if (user && !hasRole(["ADMIN", "MANAGER"])) {
    if (hasRole(["PROFESSOR"])) {
      void navigate({ to: "/professor", replace: true });
      return null;
    }

    if (hasRole(["TRAINER"])) {
      void navigate({ to: "/trainer", replace: true });
      return null;
    }

    if (!hasRole(["CLIENT"])) {
      void navigate({ to: "/", replace: true });
      return null;
    }

    void navigate({ to: "/student-area", replace: true });
    return null;
  }

  const handleLogout = () => {
    logout();
    void navigate({ to: "/", replace: true });
  };

  const handleAcceptPreRegistration = (id: string) => {
    acceptPreRegistration.mutate(id, {
      onError: () => {
        setImportFeedback("Nao foi possivel aceitar a pre-inscricao. Tente novamente.");
      },
    });
  };

  const handleArchivePreRegistration = (id: string) => {
    const shouldArchive = window.confirm("Remover esta pre-inscricao da lista?");
    if (!shouldArchive) return;
    archivePreRegistration.mutate(id, {
      onSuccess: () => {
        if (selectedPreRegistrationId === id) {
          setSelectedPreRegistrationId("");
        }
      },
      onError: () => {
        setImportFeedback("Nao foi possivel remover a pre-inscricao. Tente novamente.");
      },
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="text-center">
          <Loader2 size={40} className="animate-spin mx-auto mb-4" style={{ color: "#C1121F" }} />
          <p className="text-text-secondary text-sm tracking-wider">A carregar painel...</p>
        </div>
      </div>
    );
  }

  const memberships = membershipsData as {
    content?: MembershipItem[];
    totalElements?: number;
    totalPages?: number;
    number?: number;
  } | null;

  const includedStudentStatuses = ["ACTIVE", "REGISTERED", "CANCELLED", "EXPIRED"] as const;
  const excludedStudentStatuses = ["PENDING_PAYMENT", "REJECTED"] as const;

  const normalizeText = (value?: string) => value?.trim().toLowerCase() ?? "";

  const isDeveloperMembership = (membership: { userEmail: string }) => {
    return normalizeText(membership.userEmail) === "brielmarca@gmail.com";
  };

  const getMembershipPriority = (status: string) => {
    if (status === "ACTIVE") return 3;
    if (status === "EXPIRED") return 2;
    if (status === "CANCELLED") return 1;
    return 0;
  };

  const getBestMembershipPerUser = (items: MembershipItem[]) => {
    const grouped = new Map<string, MembershipItem>();

    for (const membership of items) {
      const keyByEmail = normalizeText(membership.userEmail);
      const keyByName = normalizeText(membership.userName);
      const groupKey = keyByEmail || keyByName;

      if (!groupKey) continue;

      const currentBest = grouped.get(groupKey);
      if (!currentBest) {
        grouped.set(groupKey, membership);
        continue;
      }

      const currentPriority = getMembershipPriority(currentBest.status);
      const nextPriority = getMembershipPriority(membership.status);

      if (nextPriority > currentPriority) {
        grouped.set(groupKey, membership);
        continue;
      }

      if (nextPriority === currentPriority) {
        const currentStart = currentBest.startDate ? Date.parse(currentBest.startDate) : Number.NaN;
        const nextStart = membership.startDate ? Date.parse(membership.startDate) : Number.NaN;

        if (!Number.isNaN(nextStart) && (Number.isNaN(currentStart) || nextStart > currentStart)) {
          grouped.set(groupKey, membership);
        }
      }
    }

    return Array.from(grouped.values());
  };

  const visibleStudents =
    memberships?.content?.filter(
      (membership) =>
        includedStudentStatuses.includes(
          membership.status as (typeof includedStudentStatuses)[number],
        ) &&
        !excludedStudentStatuses.includes(
          membership.status as (typeof excludedStudentStatuses)[number],
        ),
    ) ?? [];

  const dedupedVisibleStudents = getBestMembershipPerUser(visibleStudents);
  const businessStudents = dedupedVisibleStudents.filter(
    (membership) => !isDeveloperMembership(membership),
  );

  const activeVisibleStudents = dedupedVisibleStudents.filter((m) => m.status === "ACTIVE");
  const registeredVisibleStudents = dedupedVisibleStudents.filter((m) => m.status === "REGISTERED");
  const cancelledVisibleStudents = dedupedVisibleStudents.filter((m) => m.status === "CANCELLED");
  const basicStudents = activeVisibleStudents.filter(
    (m) => normalizeText(m.planName ?? undefined) === "basic",
  );
  const standardStudents = activeVisibleStudents.filter(
    (m) => normalizeText(m.planName ?? undefined) === "standard",
  );
  const premiumStudents = activeVisibleStudents.filter(
    (m) => normalizeText(m.planName ?? undefined) === "premium",
  );
  const activeBusinessStudents = businessStudents.filter((m) => m.status === "ACTIVE");
  const cancelledBusinessStudents = businessStudents.filter((m) => m.status === "CANCELLED");
  const preRegistrationsCount =
    preRegistrationsData?.totalElements ?? preRegistrationsData?.content?.length ?? 0;
  const pendingRequestsCount = pendingReception.length;

  const plansByName = new Map(plans.map((plan) => [normalizeText(plan.name), plan]));

  const estimatedMonthlyRevenue = activeBusinessStudents.reduce((total, membership) => {
    const plan = plansByName.get(normalizeText(membership.planName ?? undefined));
    return total + (plan?.price ?? 0);
  }, 0);

  const estimatedMonthlyRevenueLabel = new Intl.NumberFormat("pt-PT", {
    style: "currency",
    currency: "EUR",
  }).format(estimatedMonthlyRevenue);

  const activePlanCounts = activeBusinessStudents.reduce((counts, membership) => {
    const planName = membership.planName?.trim();
    if (!planName) {
      return counts;
    }

    counts.set(planName, (counts.get(planName) ?? 0) + 1);
    return counts;
  }, new Map<string, number>());

  const topPlan =
    activePlanCounts.size > 0
      ? (Array.from(activePlanCounts.entries()).sort((a, b) => b[1] - a[1])[0]?.[0] ?? "Sem dados")
      : "Sem dados";

  const studentsByFilter =
    studentsFilter === "ALL"
      ? dedupedVisibleStudents
      : studentsFilter === "ACTIVE"
        ? activeVisibleStudents
        : studentsFilter === "REGISTERED"
          ? registeredVisibleStudents
          : studentsFilter === "CANCELLED"
            ? cancelledVisibleStudents
            : studentsFilter === "BASIC"
              ? basicStudents
              : studentsFilter === "STANDARD"
                ? standardStudents
                : premiumStudents;

  const studentsTotalPages = Math.max(memberships?.totalPages ?? 1, 1);
  const studentsCurrentPage = memberships?.number ?? studentsPage;

  const graduationLabels: Record<string, string[]> = {
    JIU_JITSU: ["Branca", "Azul", "Roxa", "Castanha", "Preta"],
    BOXE_KICKBOXING: ["Fundamentos", "Tecnica", "Combinacoes", "Sparring", "Performance"],
    CAPOEIRA: ["Crua", "Amarela", "Laranja", "Azul", "Verde", "Rubi"],
    MMA: ["Fundamentos", "Tecnica", "Transicoes", "Sparring Controlado", "Performance"],
  };

  const modalityLabels: Record<string, string> = {
    JIU_JITSU: "Jiu-Jitsu",
    BOXE_KICKBOXING: "Boxe/Kickboxing",
    CAPOEIRA: "Capoeira",
    MMA: "MMA",
  };
  const modalityOptions: Modality[] = ["JIU_JITSU", "BOXE_KICKBOXING", "CAPOEIRA", "MMA"];

  const toggleProfessorModality = (modality: Modality, checked: boolean) => {
    setSelectedProfessorModalities((prev) => {
      if (checked && !prev.includes(modality)) {
        return [...prev, modality];
      }
      return prev.filter((item) => item !== modality);
    });
  };

  const visibleGraduations = graduations.filter((item) =>
    graduationFilter === "ALL" ? true : item.modality === graduationFilter,
  );

  const graduationKey = (studentEmail: string, modality: string) => `${studentEmail}::${modality}`;

  const openDeactivateStudentDialog = (student: MembershipItem) => {
    setStudentToDeactivate(student);
    setDeactivationReason("");
    setDeactivationFeedback(null);
  };

  const handleDeactivateStudent = async () => {
    const reason = deactivationReason.trim();
    if (!studentToDeactivate || !reason || reason.length > 1000) {
      return;
    }

    setDeactivationFeedback(null);
    try {
      await deactivateAdminStudent.mutateAsync({
        userId: studentToDeactivate.userId,
        payload: { reason },
      });
      setDeactivationReason("");
      setDeactivationFeedback("Aluno desativado com sucesso. A lista foi atualizada.");
    } catch (error) {
      setDeactivationFeedback(
        error instanceof Error ? error.message : "Nao foi possivel desativar o aluno.",
      );
    }
  };

  const openEditGraduationDialog = (student: MembershipItem) => {
    setStudentToEditGraduation(student);
    setEditGraduationModality("");
    setEditGraduationId("");
    setEditGraduationReason("");
    setEditGraduationFeedback(null);
  };

  const handleUpdateStudentGraduation = async () => {
    if (
      !studentToEditGraduation ||
      !editGraduationModality ||
      !editGraduationId ||
      !editGraduationReason.trim()
    ) {
      return;
    }

    setEditGraduationFeedback(null);
    try {
      await updateStudentGraduation.mutateAsync({
        userId: studentToEditGraduation.userId,
        payload: {
          modality: editGraduationModality as GraduationModality,
          graduationId: editGraduationId,
          reason: editGraduationReason.trim(),
        },
      });
      setEditGraduationFeedback("Graduação atualizada com sucesso.");
    } catch (error) {
      setEditGraduationFeedback(
        error instanceof Error ? error.message : "Não foi possível atualizar a graduação.",
      );
    }
  };

  const filteredGraduationOptions = editGraduationModality
    ? graduationOptions
        .filter((opt) => opt.modality === editGraduationModality)
        .sort((a, b) => {
          const orderDiff = (a.levelOrder ?? 0) - (b.levelOrder ?? 0);
          if (orderDiff !== 0) return orderDiff;
          return a.name.localeCompare(b.name);
        })
    : [];

  const handleSaveGraduation = async (
    studentEmail: string,
    modality: "JIU_JITSU" | "BOXE_KICKBOXING" | "CAPOEIRA" | "MMA",
    currentFallback: string,
  ) => {
    const key = graduationKey(studentEmail, modality);
    const selected = selectedLevels[key] || currentFallback;
    await updateGraduation.mutateAsync({
      studentEmail,
      modality,
      currentLevel: selected,
    });
  };

  const studentsEmptyMessage =
    studentsFilter === "ALL"
      ? "Nenhum aluno encontrado."
      : studentsFilter === "ACTIVE"
        ? "Nenhum aluno ativo encontrado."
        : studentsFilter === "REGISTERED"
          ? "Nenhum aluno registado sem plano encontrado."
          : studentsFilter === "CANCELLED"
            ? "Nenhum aluno cancelado encontrado."
            : studentsFilter === "BASIC"
              ? "Nenhum aluno ativo no plano Basic."
              : studentsFilter === "STANDARD"
                ? "Nenhum aluno ativo no plano Standard."
                : "Nenhum aluno ativo no plano Premium.";

  const handleImportCsv = async () => {
    if (!selectedCsvFile) {
      setImportFeedback("Selecione um ficheiro CSV para importar.");
      return;
    }
    try {
      const result = await importPreRegistrations.mutateAsync(selectedCsvFile);
      setImportFeedback(
        `Importacao concluida: ${result.importedRows}/${result.totalRows} importadas, ${result.duplicateRows} duplicadas, ${result.invalidRows} invalidas.`,
      );
    } catch (error) {
      const message = error instanceof Error ? error.message : "Falha ao importar CSV.";
      setImportFeedback(message);
    }
  };

  const handlePromoteProfessor = async () => {
    if (!professorEmail.trim() || selectedProfessorModalities.length === 0) {
      return;
    }
    setProfessorFeedback(null);
    try {
      await promoteProfessor.mutateAsync({
        email: professorEmail.trim(),
        modalities: selectedProfessorModalities,
      });
      setProfessorEmail("");
      setSelectedProfessorModalities([]);
      setProfessorFeedback("Professor guardado com sucesso.");
    } catch (error) {
      setProfessorFeedback(error instanceof Error ? error.message : "Erro ao guardar professor.");
    }
  };

  const handleCreateAssignment = async () => {
    if (!assignmentProfessorId || !assignmentStudentId) {
      return;
    }
    setAssignmentFeedback(null);
    try {
      await createProfessorAssignment.mutateAsync({
        professorId: assignmentProfessorId,
        studentId: assignmentStudentId,
        modality: assignmentModality,
        notes: assignmentNotes.trim() || undefined,
      });
      setAssignmentStudentId("");
      setAssignmentNotes("");
      setAssignmentFeedback("Atribuicao criada com sucesso.");
    } catch (error) {
      setAssignmentFeedback(error instanceof Error ? error.message : "Erro ao atribuir aluno.");
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <header className="bg-surface border-b border-border-subtle">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <Link to="/" className="flex flex-col">
            <span className="font-display text-xl sm:text-2xl tracking-wider">4FOUR</span>
            <span className="font-sans text-[7px] sm:text-[8px] tracking-[0.3em] text-muted-foreground">
              FIGHT ACADEMY ADMIN
            </span>
          </Link>
          <div className="flex items-center gap-2 md:gap-4">
            <Link to="/" className="text-xs text-text-secondary hover:text-foreground">
              Ver Site
            </Link>
            <Button
              onClick={handleLogout}
              variant="ghost"
              size="sm"
              className="text-xs tracking-wider uppercase text-text-secondary hover:text-foreground"
            >
              <LogOut size={14} className="sm:mr-1" />
              <span className="hidden sm:inline">Sair</span>
            </Button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-6 sm:py-8">
        <div className="mb-6 sm:mb-8">
          <h1 className="font-display text-2xl sm:text-4xl tracking-wider">PAINEL ADMIN</h1>
          <p className="text-text-secondary mt-1 text-sm sm:text-base">Gerencie a sua academia</p>
        </div>

        <Tabs defaultValue="dashboard" className="space-y-6">
          <div className="overflow-x-auto">
            <TabsList className="bg-surface border border-border-subtle p-1">
              <TabsTrigger
                value="dashboard"
                className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white rounded-sm"
              >
                Dashboard
              </TabsTrigger>
              <TabsTrigger
                value="students"
                className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white rounded-sm"
              >
                Alunos
              </TabsTrigger>
              <TabsTrigger
                value="belts"
                className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white rounded-sm"
              >
                Graduação
              </TabsTrigger>
              {canManageProfessors && (
                <TabsTrigger
                  value="professors"
                  className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white rounded-sm"
                >
                  Professores
                </TabsTrigger>
              )}
              {canManageProfessors && (
                <TabsTrigger
                  value="video-lessons"
                  className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white rounded-sm"
                >
                  Videoaulas
                </TabsTrigger>
              )}
              <TabsTrigger
                value="plans"
                className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white rounded-sm"
              >
                Planos
              </TabsTrigger>
              {canManageReception && (
                <TabsTrigger
                  value="schedule"
                  className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white rounded-sm"
                >
                  Horários
                </TabsTrigger>
              )}
              {canManageReception && (
                <TabsTrigger
                  value="pre-registrations"
                  className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white rounded-sm"
                >
                  Pré-inscrições
                </TabsTrigger>
              )}
              {canManageReception && (
                <TabsTrigger
                  value="reception"
                  className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white rounded-sm"
                >
                  Receção
                </TabsTrigger>
              )}
            </TabsList>
          </div>

          <TabsContent value="dashboard">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
              <Card
                className="bg-surface border-border-subtle"
                style={{ borderTop: "2px solid #22C55E" }}
              >
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary flex items-center gap-2">
                    <Users size={14} />
                    Alunos Ativos
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider" style={{ color: "#22C55E" }}>
                    {activeBusinessStudents.length}
                  </p>
                </CardContent>
              </Card>

              <Card
                className="bg-surface border-border-subtle"
                style={{ borderTop: "2px solid #C1121F" }}
              >
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary flex items-center gap-2">
                    <Shield size={14} />
                    Alunos Cancelados
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider" style={{ color: "#C1121F" }}>
                    {cancelledBusinessStudents.length}
                  </p>
                </CardContent>
              </Card>

              <Card
                className="bg-surface border-border-subtle"
                style={{ borderTop: "2px solid #C1121F" }}
              >
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary flex items-center gap-2">
                    <Clock size={14} />
                    Pré-inscrições
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider" style={{ color: "#C1121F" }}>
                    {preRegistrationsCount}
                  </p>
                </CardContent>
              </Card>

              <Card
                className="bg-surface border-border-subtle"
                style={{ borderTop: "2px solid #C1121F" }}
              >
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary flex items-center gap-2">
                    <CreditCard size={14} />
                    Pedidos Pendentes
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider" style={{ color: "#F5F5F5" }}>
                    {pendingRequestsCount}
                  </p>
                </CardContent>
              </Card>

              <Card
                className="bg-surface border-border-subtle"
                style={{ borderTop: "2px solid #EAB308" }}
              >
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary flex items-center gap-2">
                    <CreditCard size={14} />
                    Receita Mensal Estimada
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-3xl tracking-wider" style={{ color: "#EAB308" }}>
                    {estimatedMonthlyRevenueLabel}
                  </p>
                  <p className="mt-2 text-xs text-text-secondary">
                    Estimativa baseada nas matrículas ativas e nos preços dos planos.
                  </p>
                </CardContent>
              </Card>

              <Card
                className="bg-surface border-border-subtle"
                style={{ borderTop: "2px solid #3B82F6" }}
              >
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary flex items-center gap-2">
                    <Users size={14} />
                    Plano Mais Vendido
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-2xl tracking-wider" style={{ color: "#3B82F6" }}>
                    {topPlan}
                  </p>
                </CardContent>
              </Card>
            </div>
            <p className="mt-4 text-sm text-text-secondary">
              Os valores financeiros são estimativas operacionais, não substituem faturação ou
              contabilidade.
            </p>
            <p className="mt-1 text-sm text-text-secondary">
              Contas internas/developer não entram nas métricas de alunos pagantes nem na receita
              estimada.
            </p>
          </TabsContent>

          <TabsContent value="schedule">
            <Card
              className="bg-surface border-border-subtle"
              style={{ borderTop: "2px solid #C1121F" }}
            >
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Gestão de horários
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid md:grid-cols-2 gap-3">
                  <Input
                    placeholder="Nome da aula"
                    value={form.title}
                    onChange={(e) => setForm({ ...form, title: e.target.value })}
                  />
                  <Input
                    placeholder="Instrutor"
                    value={form.instructorName}
                    onChange={(e) => setForm({ ...form, instructorName: e.target.value })}
                  />
                  <Select
                    value={form.modality}
                    onValueChange={(value) =>
                      setForm({
                        ...form,
                        modality: value as CreateScheduleEntryRequest["modality"],
                      })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="JIU_JITSU">Jiu-Jitsu</SelectItem>
                      <SelectItem value="BOXE_KICKBOXING">Boxe / Kickboxing</SelectItem>
                      <SelectItem value="CAPOEIRA">Capoeira</SelectItem>
                      <SelectItem value="MMA">MMA</SelectItem>
                    </SelectContent>
                  </Select>
                  <Select
                    value={form.dayOfWeek}
                    onValueChange={(value) =>
                      setForm({
                        ...form,
                        dayOfWeek: value as CreateScheduleEntryRequest["dayOfWeek"],
                      })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="MONDAY">Segunda</SelectItem>
                      <SelectItem value="TUESDAY">Terça</SelectItem>
                      <SelectItem value="WEDNESDAY">Quarta</SelectItem>
                      <SelectItem value="THURSDAY">Quinta</SelectItem>
                      <SelectItem value="FRIDAY">Sexta</SelectItem>
                      <SelectItem value="SATURDAY">Sábado</SelectItem>
                      <SelectItem value="SUNDAY">Domingo</SelectItem>
                    </SelectContent>
                  </Select>
                  <Input
                    type="time"
                    value={form.startTime}
                    onChange={(e) => setForm({ ...form, startTime: e.target.value })}
                  />
                  <Input
                    type="time"
                    value={form.endTime}
                    onChange={(e) => setForm({ ...form, endTime: e.target.value })}
                  />
                  <Select
                    value={form.level}
                    onValueChange={(value) =>
                      setForm({ ...form, level: value as CreateScheduleEntryRequest["level"] })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="BEGINNER">Iniciante</SelectItem>
                      <SelectItem value="INTERMEDIATE">Intermédio</SelectItem>
                      <SelectItem value="ADVANCED">Avançado</SelectItem>
                      <SelectItem value="ALL_LEVELS">Todos os níveis</SelectItem>
                    </SelectContent>
                  </Select>
                  <Input
                    placeholder="Sala/Local"
                    value={form.location ?? ""}
                    onChange={(e) => setForm({ ...form, location: e.target.value })}
                  />
                  <Input
                    type="number"
                    placeholder="Capacidade"
                    value={form.capacity ?? ""}
                    onChange={(e) =>
                      setForm({ ...form, capacity: e.target.value ? Number(e.target.value) : null })
                    }
                  />
                  <div className="flex items-center gap-2">
                    <Switch
                      checked={!!form.active}
                      onCheckedChange={(v) => setForm({ ...form, active: v })}
                    />
                    <span className="text-sm">Ativo</span>
                  </div>
                </div>
                <Textarea
                  placeholder="Notas"
                  value={form.notes ?? ""}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
                />
                {formError && <p className="text-destructive text-sm">{formError}</p>}
                <Button
                  disabled={createSchedule.isPending || updateSchedule.isPending}
                  onClick={async () => {
                    try {
                      setFormError(null);
                      if (!form.title.trim() || !form.instructorName.trim()) {
                        setFormError("Nome da aula e instrutor são obrigatórios.");
                        return;
                      }
                      if (editingId) {
                        await updateSchedule.mutateAsync({ id: editingId, payload: form });
                      } else {
                        await createSchedule.mutateAsync(form);
                      }
                      setEditingId(null);
                      setForm({
                        ...form,
                        title: "",
                        instructorName: "",
                        notes: "",
                        location: "",
                        capacity: null,
                      });
                    } catch (error) {
                      setFormError(
                        error instanceof Error ? error.message : "Erro ao guardar horário.",
                      );
                    }
                  }}
                >
                  {createSchedule.isPending || updateSchedule.isPending
                    ? "A guardar..."
                    : editingId
                      ? "Atualizar"
                      : "Adicionar"}
                </Button>

                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Dia</TableHead>
                        <TableHead>Hora</TableHead>
                        <TableHead>Aula</TableHead>
                        <TableHead>Instrutor</TableHead>
                        <TableHead>Ativo</TableHead>
                        <TableHead className="text-right">Ações</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {adminScheduleLoading ? (
                        <TableRow>
                          <TableCell colSpan={6}>A carregar horários...</TableCell>
                        </TableRow>
                      ) : adminSchedule.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={6}>Sem horários cadastrados.</TableCell>
                        </TableRow>
                      ) : (
                        adminSchedule.map((entry) => (
                          <TableRow key={entry.id}>
                            <TableCell>{entry.dayOfWeek}</TableCell>
                            <TableCell>
                              {entry.startTime.slice(0, 5)} - {entry.endTime.slice(0, 5)}
                            </TableCell>
                            <TableCell>{entry.title}</TableCell>
                            <TableCell>{entry.instructorName}</TableCell>
                            <TableCell>{entry.active ? "Sim" : "Não"}</TableCell>
                            <TableCell className="text-right space-x-2">
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => {
                                  setEditingId(entry.id);
                                  setForm({
                                    ...entry,
                                    location: entry.location ?? "",
                                    notes: entry.notes ?? "",
                                  });
                                }}
                              >
                                Editar
                              </Button>
                              <Button
                                size="sm"
                                variant="destructive"
                                disabled={deactivateSchedule.isPending}
                                onClick={() => deactivateSchedule.mutate(entry.id)}
                              >
                                {deactivateSchedule.isPending ? "..." : "Desativar"}
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="students">
            <Card
              className="bg-surface border-border-subtle"
              style={{ borderTop: "2px solid #C1121F" }}
            >
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">Alunos</CardTitle>
                <p className="text-xs text-text-secondary">
                  Esta área mostra alunos reais organizados por estado e plano. Pedidos pendentes
                  ficam em Receção e pré-inscrições ficam na aba Pré-inscrições.
                </p>
              </CardHeader>
              <CardContent>
                <div className="mb-4 flex flex-wrap gap-2">
                  <Button
                    size="sm"
                    variant={studentsFilter === "ALL" ? "default" : "outline"}
                    onClick={() => setStudentsFilter("ALL")}
                  >
                    Todos
                  </Button>
                  <Button
                    size="sm"
                    variant={studentsFilter === "ACTIVE" ? "default" : "outline"}
                    onClick={() => setStudentsFilter("ACTIVE")}
                  >
                    Ativos
                  </Button>
                  <Button
                    size="sm"
                    variant={studentsFilter === "REGISTERED" ? "default" : "outline"}
                    onClick={() => setStudentsFilter("REGISTERED")}
                  >
                    Registados
                  </Button>
                  <Button
                    size="sm"
                    variant={studentsFilter === "CANCELLED" ? "default" : "outline"}
                    onClick={() => setStudentsFilter("CANCELLED")}
                  >
                    Cancelados
                  </Button>
                  <Button
                    size="sm"
                    variant={studentsFilter === "BASIC" ? "default" : "outline"}
                    onClick={() => setStudentsFilter("BASIC")}
                  >
                    Basic
                  </Button>
                  <Button
                    size="sm"
                    variant={studentsFilter === "STANDARD" ? "default" : "outline"}
                    onClick={() => setStudentsFilter("STANDARD")}
                  >
                    Standard
                  </Button>
                  <Button
                    size="sm"
                    variant={studentsFilter === "PREMIUM" ? "default" : "outline"}
                    onClick={() => setStudentsFilter("PREMIUM")}
                  >
                    Premium
                  </Button>
                </div>
                <div className="mb-4 flex flex-wrap items-center justify-between gap-3 text-xs text-text-secondary">
                  <span>
                    Página {studentsCurrentPage + 1} de {studentsTotalPages}
                  </span>
                  <div className="flex gap-2">
                    <Button
                      size="sm"
                      variant="outline"
                      disabled={studentsCurrentPage <= 0 || membershipsLoading}
                      onClick={() => setStudentsPage((page) => Math.max(page - 1, 0))}
                    >
                      Anterior
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      disabled={studentsCurrentPage >= studentsTotalPages - 1 || membershipsLoading}
                      onClick={() => setStudentsPage((page) => page + 1)}
                    >
                      Seguinte
                    </Button>
                  </div>
                </div>
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow className="border-border-subtle hover:bg-transparent">
                        <TableHead className="text-text-secondary">Aluno</TableHead>
                        <TableHead className="text-text-secondary">Plano</TableHead>
                        <TableHead className="text-text-secondary">Início</TableHead>
                        <TableHead className="text-text-secondary">Validade</TableHead>
                        <TableHead className="text-text-secondary">Status</TableHead>
                        {canDeactivateStudents && (
                          <TableHead className="text-right text-text-secondary">Ações</TableHead>
                        )}
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {membershipsError ? (
                        <TableRow className="hover:bg-transparent">
                          <TableCell
                            colSpan={canDeactivateStudents ? 6 : 5}
                            className="text-center py-12 text-destructive"
                          >
                            Falha ao carregar alunos. {(membershipsError as Error).message}
                          </TableCell>
                        </TableRow>
                      ) : studentsByFilter.length > 0 ? (
                        studentsByFilter.map((m) => (
                          <TableRow key={m.id} className="border-border-subtle">
                            <TableCell className="font-medium">
                              <div className="flex items-center gap-2">
                                <span>{m.userName || "-"}</span>
                                {isDeveloperMembership(m) && (
                                  <Badge
                                    variant="outline"
                                    className="border-blue-500/40 bg-blue-500/10 text-blue-300"
                                  >
                                    DEVELOPER
                                  </Badge>
                                )}
                              </div>
                              <div className="text-xs text-text-secondary">
                                {m.userEmail || "-"}
                              </div>
                            </TableCell>
                            <TableCell>{m.planName || "Sem plano"}</TableCell>
                            <TableCell>
                              {m.startDate
                                ? new Date(m.startDate).toLocaleDateString("pt-PT")
                                : "-"}
                            </TableCell>
                            <TableCell>
                              {m.endDate ? new Date(m.endDate).toLocaleDateString("pt-PT") : "-"}
                            </TableCell>
                            <TableCell>
                              <Badge
                                variant="outline"
                                className={
                                  m.status === "ACTIVE"
                                    ? "border-primary/30 bg-primary/10 text-primary"
                                    : m.status === "EXPIRED"
                                      ? "border-destructive/30 bg-destructive/10 text-destructive"
                                      : m.status === "REGISTERED"
                                        ? "border-blue-500/40 bg-blue-500/10 text-blue-300"
                                        : "border-border-subtle bg-surface-2 text-text-secondary"
                                }
                              >
                                {m.status === "ACTIVE"
                                  ? "Ativo"
                                  : m.status === "EXPIRED"
                                    ? "Expirado"
                                    : m.status === "REGISTERED"
                                      ? "Registado"
                                      : "Cancelado"}
                              </Badge>
                            </TableCell>
                            {canDeactivateStudents && (
                              <TableCell className="text-right">
                                <DropdownMenu>
                                  <DropdownMenuTrigger asChild>
                                    <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                                      <span className="sr-only">Abrir ações</span>
                                      <MoreHorizontal size={16} />
                                    </Button>
                                  </DropdownMenuTrigger>
                                  <DropdownMenuContent
                                    align="end"
                                    className="bg-surface border-border-subtle"
                                  >
                                    <DropdownMenuItem onSelect={() => openEditGraduationDialog(m)}>
                                      Editar graduação
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                      className="text-destructive focus:text-destructive"
                                      onSelect={() => openDeactivateStudentDialog(m)}
                                    >
                                      Desativar aluno
                                    </DropdownMenuItem>
                                  </DropdownMenuContent>
                                </DropdownMenu>
                              </TableCell>
                            )}
                          </TableRow>
                        ))
                      ) : (
                        <TableRow className="hover:bg-transparent">
                          <TableCell
                            colSpan={canDeactivateStudents ? 6 : 5}
                            className="text-center py-12 text-text-secondary"
                          >
                            {studentsEmptyMessage}
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
                <Dialog
                  open={!!studentToEditGraduation}
                  onOpenChange={(open) => {
                    if (open || updateStudentGraduation.isPending) return;
                    setStudentToEditGraduation(null);
                    setEditGraduationModality("");
                    setEditGraduationId("");
                    setEditGraduationReason("");
                    setEditGraduationFeedback(null);
                  }}
                >
                  <DialogContent className="border-border-subtle bg-surface text-foreground sm:max-w-xl">
                    <DialogHeader>
                      <DialogTitle className="font-display tracking-wider">
                        Editar graduação
                      </DialogTitle>
                      <DialogDescription>
                        Atualize a graduação de {studentToEditGraduation?.userName || "este aluno"}.
                      </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4">
                      <div className="rounded-md border border-border-subtle bg-background/50 p-3 text-sm">
                        <p className="font-medium">
                          {studentToEditGraduation?.userName || "Aluno"}
                        </p>
                        <p className="text-xs text-text-secondary">
                          {studentToEditGraduation?.userEmail || "Sem email"}
                        </p>
                      </div>
                      <div className="space-y-2">
                        <label className="text-xs uppercase tracking-[0.2em] text-text-secondary">
                          Modalidade
                        </label>
                        <Select
                          value={editGraduationModality}
                          onValueChange={(value) => {
                            setEditGraduationModality(value);
                            setEditGraduationId("");
                          }}
                          disabled={updateStudentGraduation.isPending || !!editGraduationFeedback}
                        >
                          <SelectTrigger>
                            <SelectValue placeholder="Selecionar modalidade" />
                          </SelectTrigger>
                          <SelectContent>
                            {modalityOptions.map((mod) => (
                              <SelectItem key={mod} value={mod}>
                                {modalityLabels[mod]}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <label className="text-xs uppercase tracking-[0.2em] text-text-secondary">
                          Nova graduação
                        </label>
                        <Select
                          value={editGraduationId}
                          onValueChange={setEditGraduationId}
                          disabled={
                            !editGraduationModality ||
                            updateStudentGraduation.isPending ||
                            !!editGraduationFeedback
                          }
                        >
                          <SelectTrigger>
                            <SelectValue
                              placeholder={
                                editGraduationModality
                                  ? "Selecionar graduação"
                                  : "Selecione primeiro a modalidade"
                              }
                            />
                          </SelectTrigger>
                          <SelectContent>
                            {graduationOptionsLoading ? (
                              <div className="px-2 py-4 text-center text-sm text-text-secondary">
                                A carregar graduações...
                              </div>
                            ) : filteredGraduationOptions.length === 0 ? (
                              <div className="px-2 py-4 text-center text-sm text-text-secondary">
                                Nenhuma graduação cadastrada para esta modalidade.
                              </div>
                            ) : (
                              filteredGraduationOptions.map((opt) => (
                                <SelectItem key={opt.id} value={opt.id}>
                                  {opt.name}
                                </SelectItem>
                              ))
                            )}
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <label
                          htmlFor="edit-graduation-reason"
                          className="text-xs uppercase tracking-[0.2em] text-text-secondary"
                        >
                          Motivo da alteração
                        </label>
                        <Textarea
                          id="edit-graduation-reason"
                          maxLength={1000}
                          placeholder="Descreva o motivo da alteração de graduação."
                          value={editGraduationReason}
                          disabled={
                            updateStudentGraduation.isPending ||
                            editGraduationFeedback === "Graduação atualizada com sucesso."
                          }
                          onChange={(event) => setEditGraduationReason(event.target.value)}
                        />
                        <div className="flex justify-between gap-3 text-xs text-text-secondary">
                          <span>Obrigatório. Máximo de 1000 caracteres.</span>
                          <span>{editGraduationReason.length}/1000</span>
                        </div>
                      </div>
                      {editGraduationFeedback && (
                        <p
                          className={
                            editGraduationFeedback === "Graduação atualizada com sucesso."
                              ? "text-sm text-emerald-400"
                              : "text-sm text-destructive"
                          }
                        >
                          {editGraduationFeedback}
                        </p>
                      )}
                    </div>
                    <DialogFooter>
                      <Button
                        variant="outline"
                        disabled={updateStudentGraduation.isPending}
                        onClick={() => {
                          setStudentToEditGraduation(null);
                          setEditGraduationModality("");
                          setEditGraduationId("");
                          setEditGraduationReason("");
                          setEditGraduationFeedback(null);
                        }}
                      >
                        {editGraduationFeedback === "Graduação atualizada com sucesso."
                          ? "Fechar"
                          : "Cancelar"}
                      </Button>
                      {editGraduationFeedback !== "Graduação atualizada com sucesso." && (
                        <Button
                          disabled={
                            updateStudentGraduation.isPending ||
                            !editGraduationModality ||
                            !editGraduationId ||
                            !editGraduationReason.trim() ||
                            editGraduationReason.length > 1000
                          }
                          onClick={handleUpdateStudentGraduation}
                        >
                          {updateStudentGraduation.isPending
                            ? "A atualizar..."
                            : "Atualizar graduação"}
                        </Button>
                      )}
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
                <Dialog
                  open={!!studentToDeactivate}
                  onOpenChange={(open) => {
                    if (open || deactivateAdminStudent.isPending) return;
                    setStudentToDeactivate(null);
                    setDeactivationReason("");
                    setDeactivationFeedback(null);
                  }}
                >
                  <DialogContent className="border-border-subtle bg-surface text-foreground sm:max-w-xl">
                    <DialogHeader>
                      <DialogTitle className="font-display tracking-wider">
                        Desativar aluno
                      </DialogTitle>
                      <DialogDescription>
                        Esta ação desativa o acesso de{" "}
                        {studentToDeactivate?.userName || "este aluno"}. O histórico, pagamentos,
                        graduações e registos existentes serão preservados.
                      </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-3">
                      <div className="rounded-md border border-border-subtle bg-background/50 p-3 text-sm">
                        <p className="font-medium">{studentToDeactivate?.userName || "Aluno"}</p>
                        <p className="text-xs text-text-secondary">
                          {studentToDeactivate?.userEmail || "Sem email"}
                        </p>
                      </div>
                      <div className="space-y-2">
                        <label
                          htmlFor="deactivation-reason"
                          className="text-xs uppercase tracking-[0.2em] text-text-secondary"
                        >
                          Motivo da desativação
                        </label>
                        <Textarea
                          id="deactivation-reason"
                          maxLength={1000}
                          placeholder="Descreva o motivo da desativação."
                          value={deactivationReason}
                          disabled={
                            deactivateAdminStudent.isPending ||
                            deactivationFeedback?.startsWith("Aluno desativado")
                          }
                          onChange={(event) => setDeactivationReason(event.target.value)}
                        />
                        <div className="flex justify-between gap-3 text-xs text-text-secondary">
                          <span>Obrigatório. Máximo de 1000 caracteres.</span>
                          <span>{deactivationReason.length}/1000</span>
                        </div>
                      </div>
                      {deactivationFeedback && (
                        <p
                          className={
                            deactivationFeedback.startsWith("Aluno desativado")
                              ? "text-sm text-emerald-400"
                              : "text-sm text-destructive"
                          }
                        >
                          {deactivationFeedback}
                        </p>
                      )}
                    </div>
                    <DialogFooter>
                      <Button
                        variant="outline"
                        disabled={deactivateAdminStudent.isPending}
                        onClick={() => {
                          setStudentToDeactivate(null);
                          setDeactivationReason("");
                          setDeactivationFeedback(null);
                        }}
                      >
                        {deactivationFeedback?.startsWith("Aluno desativado")
                          ? "Fechar"
                          : "Cancelar"}
                      </Button>
                      {!deactivationFeedback?.startsWith("Aluno desativado") && (
                        <Button
                          variant="destructive"
                          disabled={
                            deactivateAdminStudent.isPending ||
                            !deactivationReason.trim() ||
                            deactivationReason.length > 1000
                          }
                          onClick={handleDeactivateStudent}
                        >
                          {deactivateAdminStudent.isPending ? "A desativar..." : "Desativar"}
                        </Button>
                      )}
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </CardContent>
            </Card>
          </TabsContent>

          {canManageProfessors && (
            <TabsContent value="video-lessons">
              <div className="space-y-4">
                <Card
                  className="bg-surface border-border-subtle"
                  style={{ borderTop: "2px solid #C1121F" }}
                >
                  <CardHeader>
                    <CardTitle className="text-xs tracking-[0.2em] uppercase">
                      Nova videoaula
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div className="grid md:grid-cols-2 gap-3">
                      <Input
                        placeholder="Titulo"
                        value={videoForm.title}
                        onChange={(event) =>
                          setVideoForm((prev) => ({ ...prev, title: event.target.value }))
                        }
                      />
                      <Select
                        value={videoForm.modality}
                        onValueChange={(value) =>
                          setVideoForm((prev) => ({ ...prev, modality: value as Modality }))
                        }
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Modalidade" />
                        </SelectTrigger>
                        <SelectContent>
                          {modalityOptions.map((modality) => (
                            <SelectItem key={modality} value={modality}>
                              {modalityLabels[modality]}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <Input
                        type="file"
                        accept="video/mp4,.mp4"
                        onChange={(event) => setVideoFile(event.target.files?.[0] ?? null)}
                        className="md:col-span-2"
                      />
                      <Select
                        value={String(videoForm.minimumPlanRank)}
                        onValueChange={(value) =>
                          setVideoForm((prev) => ({
                            ...prev,
                            minimumPlanRank: Number(value) as 1 | 2 | 3,
                          }))
                        }
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Plano minimo" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="1">Basic</SelectItem>
                          <SelectItem value="2">Standard</SelectItem>
                          <SelectItem value="3">Premium</SelectItem>
                        </SelectContent>
                      </Select>
                      <div className="flex items-center gap-2">
                        <Switch
                          checked={videoForm.active}
                          onCheckedChange={(value) =>
                            setVideoForm((prev) => ({ ...prev, active: value }))
                          }
                        />
                        <span className="text-sm">Ativo</span>
                      </div>
                    </div>
                    <Textarea
                      placeholder="Descricao"
                      value={videoForm.description}
                      onChange={(event) =>
                        setVideoForm((prev) => ({ ...prev, description: event.target.value }))
                      }
                    />
                    <p className="text-xs text-text-secondary">
                      Apenas MP4. O ficheiro sera convertido para WebM e o MP4 original sera
                      removido apos o processamento.
                    </p>
                    <Button
                      onClick={async () => {
                        setVideoLessonFeedback(null);
                        try {
                          const payload = {
                            title: videoForm.title,
                            description: videoForm.description,
                            modality: videoForm.modality,
                            minimumPlanRank: videoForm.minimumPlanRank,
                            active: videoForm.active,
                          };
                          if (editingVideoLessonId) {
                            await updateVideoLesson.mutateAsync({
                              id: editingVideoLessonId,
                              payload,
                            });
                            setVideoLessonFeedback("Videoaula atualizada com sucesso.");
                          } else {
                            if (!videoFile) {
                              setVideoLessonFeedback("Selecione um ficheiro MP4 para upload.");
                              return;
                            }
                            await createVideoLesson.mutateAsync({ file: videoFile, payload });
                            setVideoLessonFeedback("Videoaula criada com sucesso.");
                          }
                          setEditingVideoLessonId(null);
                          setVideoFile(null);
                          setVideoForm({
                            title: "",
                            description: "",
                            modality: "JIU_JITSU",
                            minimumPlanRank: 1,
                            active: true,
                          });
                        } catch (error) {
                          setVideoLessonFeedback(
                            error instanceof Error ? error.message : "Erro ao guardar videoaula.",
                          );
                        }
                      }}
                      disabled={createVideoLesson.isPending || updateVideoLesson.isPending}
                    >
                      {createVideoLesson.isPending || updateVideoLesson.isPending
                        ? "A guardar..."
                        : "Guardar"}
                    </Button>
                    {videoLessonFeedback && (
                      <p className="text-sm text-text-secondary">{videoLessonFeedback}</p>
                    )}
                  </CardContent>
                </Card>

                <Card
                  className="bg-surface border-border-subtle"
                  style={{ borderTop: "2px solid #C1121F" }}
                >
                  <CardHeader>
                    <CardTitle className="text-xs tracking-[0.2em] uppercase">Videoaulas</CardTitle>
                  </CardHeader>
                  <CardContent className="overflow-x-auto">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Titulo</TableHead>
                          <TableHead>Modalidade</TableHead>
                          <TableHead>Plano minimo</TableHead>
                          <TableHead>Professor</TableHead>
                          <TableHead>Estado</TableHead>
                          <TableHead>Ativo</TableHead>
                          <TableHead>Acoes</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {videoLessonsLoading ? (
                          <TableRow>
                            <TableCell colSpan={7}>A carregar videoaulas...</TableCell>
                          </TableRow>
                        ) : manageVideoLessons.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={7}>Sem videoaulas.</TableCell>
                          </TableRow>
                        ) : (
                          manageVideoLessons.map((lesson) => (
                            <TableRow key={lesson.id}>
                              <TableCell>{lesson.title}</TableCell>
                              <TableCell>
                                {modalityLabels[lesson.modality] ?? lesson.modality}
                              </TableCell>
                              <TableCell>
                                {lesson.minimumPlanRank === 1
                                  ? "Basic"
                                  : lesson.minimumPlanRank === 2
                                    ? "Standard"
                                    : "Premium"}
                              </TableCell>
                              <TableCell>{lesson.professorName || lesson.createdByName}</TableCell>
                              <TableCell>{lesson.status ?? "READY"}</TableCell>
                              <TableCell>{lesson.active ? "Sim" : "Nao"}</TableCell>
                              <TableCell className="space-x-2">
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() => {
                                    setEditingVideoLessonId(lesson.id);
                                    setVideoForm({
                                      title: lesson.title,
                                      description: lesson.description ?? "",
                                      modality: lesson.modality,
                                      minimumPlanRank: lesson.minimumPlanRank,
                                      active: lesson.active,
                                    });
                                  }}
                                >
                                  Editar
                                </Button>
                                {lesson.active && (
                                  <Button
                                    size="sm"
                                    variant="destructive"
                                    onClick={() => {
                                      if (window.confirm("Eliminar esta videoaula?")) {
                                        deactivateVideoLesson.mutate(lesson.id);
                                      }
                                    }}
                                    disabled={deactivateVideoLesson.isPending}
                                  >
                                    Eliminar
                                  </Button>
                                )}
                              </TableCell>
                            </TableRow>
                          ))
                        )}
                      </TableBody>
                    </Table>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>
          )}

          {canManageProfessors && (
            <TabsContent value="professors">
              <div className="space-y-4">
                <Card
                  className="bg-surface border-border-subtle"
                  style={{ borderTop: "2px solid #C1121F" }}
                >
                  <CardHeader>
                    <CardTitle className="text-xs tracking-[0.2em] uppercase">
                      Guardar professor
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <Input
                      placeholder="email@exemplo.com"
                      value={professorEmail}
                      onChange={(event) => setProfessorEmail(event.target.value)}
                    />
                    <div className="grid sm:grid-cols-2 gap-2">
                      {modalityOptions.map((modality) => (
                        <label key={modality} className="flex items-center gap-2 text-sm">
                          <Checkbox
                            checked={selectedProfessorModalities.includes(modality)}
                            onCheckedChange={(checked) =>
                              toggleProfessorModality(modality, checked === true)
                            }
                          />
                          {modalityLabels[modality]}
                        </label>
                      ))}
                    </div>
                    <Button
                      onClick={() => void handlePromoteProfessor()}
                      disabled={
                        promoteProfessor.isPending ||
                        !professorEmail.trim() ||
                        selectedProfessorModalities.length === 0
                      }
                    >
                      {promoteProfessor.isPending ? "A guardar..." : "Guardar professor"}
                    </Button>
                    {professorFeedback && (
                      <p className="text-sm text-text-secondary">{professorFeedback}</p>
                    )}
                  </CardContent>
                </Card>

                <Card
                  className="bg-surface border-border-subtle"
                  style={{ borderTop: "2px solid #C1121F" }}
                >
                  <CardHeader>
                    <CardTitle className="text-xs tracking-[0.2em] uppercase">
                      Professores
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="overflow-x-auto">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Nome</TableHead>
                          <TableHead>Email</TableHead>
                          <TableHead>Modalidades</TableHead>
                          <TableHead>Ações</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {professorsLoading ? (
                          <TableRow>
                            <TableCell colSpan={4}>A carregar professores...</TableCell>
                          </TableRow>
                        ) : professors.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={4}>Sem professores.</TableCell>
                          </TableRow>
                        ) : (
                          professors.map((professor) => (
                            <TableRow key={professor.id}>
                              <TableCell>{professor.name}</TableCell>
                              <TableCell>{professor.email}</TableCell>
                              <TableCell>
                                {professor.modalities
                                  .map((item) => modalityLabels[item] ?? item)
                                  .join(", ")}
                              </TableCell>
                              <TableCell>
                                <Button
                                  size="sm"
                                  variant="outline"
                                  disabled={updateProfessorModalities.isPending}
                                  onClick={() =>
                                    updateProfessorModalities.mutate({
                                      professorId: professor.id,
                                      payload: {
                                        modalities:
                                          professor.modalities.length > 0
                                            ? professor.modalities
                                            : ["JIU_JITSU"],
                                      },
                                    })
                                  }
                                >
                                  Atualizar modalidades
                                </Button>
                              </TableCell>
                            </TableRow>
                          ))
                        )}
                      </TableBody>
                    </Table>
                  </CardContent>
                </Card>

                <Card
                  className="bg-surface border-border-subtle"
                  style={{ borderTop: "2px solid #C1121F" }}
                >
                  <CardHeader>
                    <CardTitle className="text-xs tracking-[0.2em] uppercase">
                      Atribuir aluno
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div className="grid md:grid-cols-2 gap-3">
                      <Select
                        value={assignmentProfessorId}
                        onValueChange={setAssignmentProfessorId}
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Professor" />
                        </SelectTrigger>
                        <SelectContent>
                          {professors.map((professor) => (
                            <SelectItem key={professor.id} value={professor.id}>
                              {professor.name}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <Select value={assignmentStudentId} onValueChange={setAssignmentStudentId}>
                        <SelectTrigger>
                          <SelectValue placeholder="Aluno" />
                        </SelectTrigger>
                        <SelectContent>
                          {activeVisibleStudents.map((student) => (
                            <SelectItem key={student.userId} value={student.userId}>
                              {student.userName} ({student.userEmail})
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <Select
                        value={assignmentModality}
                        onValueChange={(value) => setAssignmentModality(value as Modality)}
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Modalidade" />
                        </SelectTrigger>
                        <SelectContent>
                          {modalityOptions.map((modality) => (
                            <SelectItem key={modality} value={modality}>
                              {modalityLabels[modality]}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <Textarea
                      placeholder="Notas (opcional)"
                      value={assignmentNotes}
                      onChange={(event) => setAssignmentNotes(event.target.value)}
                    />
                    <Button
                      onClick={() => void handleCreateAssignment()}
                      disabled={
                        createProfessorAssignment.isPending ||
                        !assignmentProfessorId ||
                        !assignmentStudentId
                      }
                    >
                      {createProfessorAssignment.isPending ? "A atribuir..." : "Atribuir aluno"}
                    </Button>
                    {assignmentFeedback && (
                      <p className="text-sm text-text-secondary">{assignmentFeedback}</p>
                    )}
                  </CardContent>
                </Card>

                <Card
                  className="bg-surface border-border-subtle"
                  style={{ borderTop: "2px solid #C1121F" }}
                >
                  <CardHeader>
                    <CardTitle className="text-xs tracking-[0.2em] uppercase">
                      Atribuições
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="overflow-x-auto">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Professor</TableHead>
                          <TableHead>Aluno</TableHead>
                          <TableHead>Modalidade</TableHead>
                          <TableHead>Notas</TableHead>
                          <TableHead>Estado</TableHead>
                          <TableHead>Ações</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {assignmentsLoading ? (
                          <TableRow>
                            <TableCell colSpan={6}>A carregar atribuições...</TableCell>
                          </TableRow>
                        ) : professorAssignments.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={6}>Sem atribuições.</TableCell>
                          </TableRow>
                        ) : (
                          professorAssignments.map((assignment) => (
                            <TableRow key={assignment.id}>
                              <TableCell>{assignment.professorName}</TableCell>
                              <TableCell>{assignment.studentName}</TableCell>
                              <TableCell>
                                {modalityLabels[assignment.modality] ?? assignment.modality}
                              </TableCell>
                              <TableCell>{assignment.notes || "-"}</TableCell>
                              <TableCell>{assignment.active ? "Ativa" : "Desativada"}</TableCell>
                              <TableCell>
                                {assignment.active && (
                                  <Button
                                    size="sm"
                                    variant="destructive"
                                    disabled={deactivateProfessorAssignment.isPending}
                                    onClick={() =>
                                      deactivateProfessorAssignment.mutate(assignment.id)
                                    }
                                  >
                                    Desativar atribuição
                                  </Button>
                                )}
                              </TableCell>
                            </TableRow>
                          ))
                        )}
                      </TableBody>
                    </Table>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>
          )}

          <TabsContent value="pre-registrations">
            <Card
              className="bg-surface border-border-subtle"
              style={{ borderTop: "2px solid #C1121F" }}
            >
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Clientes / Pré-inscrições
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex flex-col gap-2 md:flex-row md:items-center">
                  <Input
                    type="file"
                    accept=".csv,text/csv"
                    onChange={(event) => setSelectedCsvFile(event.target.files?.[0] ?? null)}
                    className="max-w-xl"
                  />
                  <Button
                    onClick={handleImportCsv}
                    disabled={!selectedCsvFile || importPreRegistrations.isPending}
                  >
                    {importPreRegistrations.isPending ? "A importar..." : "Importar CSV"}
                  </Button>
                </div>
                {importFeedback && <p className="text-xs text-text-secondary">{importFeedback}</p>}
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow className="border-border-subtle hover:bg-transparent">
                        <TableHead>Nome</TableHead>
                        <TableHead>Telefone</TableHead>
                        <TableHead>Idade</TableHead>
                        <TableHead>Freguesia</TableHead>
                        <TableHead>Modalidades</TableHead>
                        <TableHead>Horario</TableHead>
                        <TableHead>Dias</TableHead>
                        <TableHead>Contacto</TableHead>
                        <TableHead>Data</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead className="text-right">Acoes</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {preRegistrationsLoading ? (
                        <TableRow>
                          <TableCell colSpan={11}>A carregar pré-inscrições...</TableCell>
                        </TableRow>
                      ) : (preRegistrationsData?.content?.length ?? 0) === 0 ? (
                        <TableRow>
                          <TableCell colSpan={11}>Sem pré-inscrições registadas.</TableCell>
                        </TableRow>
                      ) : (
                        preRegistrationsData?.content?.map((item) => (
                          <TableRow
                            key={item.id}
                            className="cursor-pointer"
                            onClick={() => setSelectedPreRegistrationId(item.id)}
                          >
                            <TableCell>{item.fullName}</TableCell>
                            <TableCell>{item.phone}</TableCell>
                            <TableCell>{item.age ?? "-"}</TableCell>
                            <TableCell>{item.parish || "-"}</TableCell>
                            <TableCell>{item.preferredModalities || "-"}</TableCell>
                            <TableCell>{item.preferredTrainingTimes || "-"}</TableCell>
                            <TableCell>{item.preferredTrainingDays || "-"}</TableCell>
                            <TableCell>{item.preferredContactMethod || "-"}</TableCell>
                            <TableCell>
                              {new Date(item.submittedAt).toLocaleDateString("pt-PT")}
                            </TableCell>
                            <TableCell>{item.status}</TableCell>
                            <TableCell className="text-right">
                              <div className="flex justify-end gap-2">
                                <Button
                                  size="sm"
                                  onClick={(event) => {
                                    event.stopPropagation();
                                    handleAcceptPreRegistration(item.id);
                                  }}
                                  disabled={
                                    acceptPreRegistration.isPending ||
                                    archivePreRegistration.isPending ||
                                    item.status === "ACCEPTED"
                                  }
                                >
                                  Aceitar
                                </Button>
                                <Button
                                  size="sm"
                                  variant="destructive"
                                  onClick={(event) => {
                                    event.stopPropagation();
                                    handleArchivePreRegistration(item.id);
                                  }}
                                  disabled={
                                    archivePreRegistration.isPending ||
                                    acceptPreRegistration.isPending
                                  }
                                >
                                  Remover
                                </Button>
                              </div>
                            </TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </div>

                {preRegistrationDetail && (
                  <Card className="bg-surface-2 border-border-subtle">
                    <CardHeader>
                      <CardTitle className="text-sm tracking-[0.1em] uppercase">
                        Detalhes completos do cliente
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                      <div>
                        <strong>Nome:</strong> {preRegistrationDetail.fullName}
                      </div>
                      <div>
                        <strong>Telefone:</strong> {preRegistrationDetail.phone}
                      </div>
                      <div>
                        <strong>Idade:</strong> {preRegistrationDetail.age}
                      </div>
                      <div>
                        <strong>Submetido em:</strong>{" "}
                        {new Date(preRegistrationDetail.submittedAt).toLocaleString("pt-PT")}
                      </div>
                      <div>
                        <strong>Morada/Freguesia:</strong> {preRegistrationDetail.parish || "-"}
                      </div>
                      <div>
                        <strong>Experiência prévia:</strong>{" "}
                        {preRegistrationDetail.hasMartialArtsExperience ? "Sim" : "Não"}
                      </div>
                      <div>
                        <strong>Detalhes experiência:</strong>{" "}
                        {preRegistrationDetail.martialArtsExperienceDetails || "-"}
                      </div>
                      <div className="md:col-span-2">
                        <strong>Objetivo:</strong> {preRegistrationDetail.trainingGoal || "-"}
                      </div>
                      <div>
                        <strong>Modalidade:</strong>{" "}
                        {preRegistrationDetail.preferredModalities || "-"}
                      </div>
                      <div>
                        <strong>Horário:</strong>{" "}
                        {preRegistrationDetail.preferredTrainingTimes || "-"}
                      </div>
                      <div>
                        <strong>Dias:</strong> {preRegistrationDetail.preferredTrainingDays || "-"}
                      </div>
                      <div>
                        <strong>Filosofia importante:</strong>{" "}
                        {preRegistrationDetail.philosophyImportant ? "Sim" : "Não"}
                      </div>
                      <div>
                        <strong>Contacto preferido:</strong>{" "}
                        {preRegistrationDetail.preferredContactMethod}
                      </div>
                      <div>
                        <strong>Origem:</strong> {preRegistrationDetail.source}
                      </div>
                      <div>
                        <strong>Status:</strong> {preRegistrationDetail.status}
                      </div>
                      <div className="md:col-span-2">
                        <strong>Notas:</strong> {preRegistrationDetail.notes || "-"}
                      </div>
                    </CardContent>
                  </Card>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="belts">
            <Card
              className="bg-surface border-border-subtle"
              style={{ borderTop: "2px solid #C1121F" }}
            >
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Gestão de Graduação
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="mb-4 flex flex-wrap gap-2">
                  <Button
                    size="sm"
                    variant={graduationFilter === "ALL" ? "default" : "outline"}
                    onClick={() => setGraduationFilter("ALL")}
                  >
                    Todas
                  </Button>
                  <Button
                    size="sm"
                    variant={graduationFilter === "JIU_JITSU" ? "default" : "outline"}
                    onClick={() => setGraduationFilter("JIU_JITSU")}
                  >
                    Jiu-Jitsu
                  </Button>
                  <Button
                    size="sm"
                    variant={graduationFilter === "BOXE_KICKBOXING" ? "default" : "outline"}
                    onClick={() => setGraduationFilter("BOXE_KICKBOXING")}
                  >
                    Boxe/Kickboxing
                  </Button>
                  <Button
                    size="sm"
                    variant={graduationFilter === "CAPOEIRA" ? "default" : "outline"}
                    onClick={() => setGraduationFilter("CAPOEIRA")}
                  >
                    Capoeira
                  </Button>
                  <Button
                    size="sm"
                    variant={graduationFilter === "MMA" ? "default" : "outline"}
                    onClick={() => setGraduationFilter("MMA")}
                  >
                    MMA
                  </Button>
                </div>

                {visibleGraduations.length > 0 ? (
                  <div className="overflow-x-auto">
                    <Table>
                      <TableHeader>
                        <TableRow className="border-border-subtle hover:bg-transparent">
                          <TableHead>Aluno</TableHead>
                          <TableHead>Email</TableHead>
                          <TableHead>Modalidade</TableHead>
                          <TableHead>Graduação atual</TableHead>
                          <TableHead>Próxima graduação</TableHead>
                          <TableHead>Atualizado em</TableHead>
                          <TableHead className="text-right">Ações</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {visibleGraduations.map((item) => {
                          const key = graduationKey(item.studentEmail, item.modality);
                          const options = graduationLabels[item.modality] ?? [];
                          return (
                            <TableRow key={key} className="border-border-subtle">
                              <TableCell className="font-medium">{item.studentName}</TableCell>
                              <TableCell>{item.studentEmail}</TableCell>
                              <TableCell>
                                {modalityLabels[item.modality] ?? item.modality}
                              </TableCell>
                              <TableCell>
                                <Select
                                  value={selectedLevels[key] ?? item.currentGraduation}
                                  onValueChange={(value) =>
                                    setSelectedLevels((prev) => ({ ...prev, [key]: value }))
                                  }
                                >
                                  <SelectTrigger className="w-[200px]">
                                    <SelectValue placeholder="Selecionar" />
                                  </SelectTrigger>
                                  <SelectContent>
                                    {options.map((option) => (
                                      <SelectItem key={option} value={option}>
                                        {option}
                                      </SelectItem>
                                    ))}
                                  </SelectContent>
                                </Select>
                              </TableCell>
                              <TableCell>{item.nextGraduation || "-"}</TableCell>
                              <TableCell>
                                {item.updatedAt
                                  ? new Date(item.updatedAt).toLocaleDateString("pt-PT")
                                  : "-"}
                              </TableCell>
                              <TableCell className="text-right">
                                <Button
                                  size="sm"
                                  disabled={updateGraduation.isPending}
                                  onClick={() =>
                                    handleSaveGraduation(
                                      item.studentEmail,
                                      item.modality,
                                      item.currentGraduation,
                                    )
                                  }
                                >
                                  Guardar
                                </Button>
                              </TableCell>
                            </TableRow>
                          );
                        })}
                      </TableBody>
                    </Table>
                  </div>
                ) : (
                  <div className="text-center py-12">
                    <Shield size={32} className="mx-auto mb-3 text-text-muted" />
                    <p className="text-text-secondary">Sem alunos encontrados</p>
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="plans">
            <Card
              className="bg-surface border-border-subtle"
              style={{ borderTop: "2px solid #C1121F" }}
            >
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Planos de Mensalidade
                </CardTitle>
              </CardHeader>
              <CardContent>
                {plans.length > 0 ? (
                  <div className="space-y-2">
                    {plans.map((plan) => (
                      <div
                        key={plan.id}
                        className="flex items-center justify-between p-3 bg-surface-2 border border-border-subtle"
                      >
                        <div>
                          <p className="font-medium">{plan.name}</p>
                          <p className="text-xs text-text-secondary">
                            {plan.durationDays} dias · {plan.maxClasses || "Ilimitadas"} aulas
                          </p>
                        </div>
                        <p className="font-display text-xl" style={{ color: "#C1121F" }}>
                          €{plan.price}
                        </p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-12">
                    <CreditCard size={32} className="mx-auto mb-3 text-text-muted" />
                    <p className="text-text-secondary">Nenhum plano cadastrado.</p>
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {canManageReception && (
            <TabsContent value="reception">
              <Card
                className="bg-surface border-border-subtle"
                style={{ borderTop: "2px solid #C1121F" }}
              >
                <CardHeader>
                  <CardTitle className="text-xs tracking-[0.2em] uppercase">
                    Pedidos pendentes na receção
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="overflow-x-auto">
                    <Table>
                      <TableHeader>
                        <TableRow className="border-border-subtle hover:bg-transparent">
                          <TableHead className="text-text-secondary">Cliente</TableHead>
                          <TableHead className="text-text-secondary">Plano</TableHead>
                          <TableHead className="text-text-secondary">Preço</TableHead>
                          <TableHead className="text-text-secondary">Pedido</TableHead>
                          <TableHead className="text-text-secondary">Status</TableHead>
                          <TableHead className="text-text-secondary text-right">Ações</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {pendingReception.length > 0 ? (
                          pendingReception.map((request) => {
                            const handling =
                              approveReception.isPending || rejectReception.isPending;
                            return (
                              <TableRow key={request.membershipId} className="border-border-subtle">
                                <TableCell>
                                  <p className="font-medium">{request.userName}</p>
                                  <p className="text-xs text-text-secondary">{request.userEmail}</p>
                                </TableCell>
                                <TableCell>{request.planName}</TableCell>
                                <TableCell>€{request.planPrice}</TableCell>
                                <TableCell>
                                  {new Date(request.requestedAt).toLocaleDateString("pt-PT")}
                                </TableCell>
                                <TableCell>
                                  <Badge
                                    variant="outline"
                                    className="border-border-subtle bg-surface-2 text-text-secondary"
                                  >
                                    {request.status}
                                  </Badge>
                                </TableCell>
                                <TableCell className="text-right">
                                  <div className="flex items-center justify-end gap-2">
                                    <Button
                                      size="sm"
                                      className="btn-red"
                                      disabled={handling}
                                      onClick={() => approveReception.mutate(request.membershipId)}
                                    >
                                      Aprovar
                                    </Button>
                                    <Button
                                      size="sm"
                                      variant="outline"
                                      disabled={handling}
                                      onClick={() => rejectReception.mutate(request.membershipId)}
                                    >
                                      Rejeitar
                                    </Button>
                                  </div>
                                </TableCell>
                              </TableRow>
                            );
                          })
                        ) : (
                          <TableRow className="hover:bg-transparent">
                            <TableCell
                              colSpan={6}
                              className="text-center py-12 text-text-secondary"
                            >
                              Sem pedidos pendentes na receção.
                            </TableCell>
                          </TableRow>
                        )}
                      </TableBody>
                    </Table>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          )}
        </Tabs>
      </main>
    </div>
  );
}
