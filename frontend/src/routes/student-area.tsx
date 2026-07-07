import { createFileRoute, Link, redirect } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "@/contexts/use-auth";
import { api, isAuthenticated } from "@/lib/api";
import type { Membership } from "@/types";
import {
  useMyStudentProfile,
  useMyMembership,
  useMyMonthlyAttendance,
  useMyEnrollments,
  useMyVideoLessons,
  useCancelMyMembership,
} from "@/queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { EmptyState } from "@/components/ui/feedback";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import {
  Loader2,
  LogOut,
  Award,
  Calendar,
  CreditCard,
  Target,
  Check,
  Clock,
  Flame,
  TrendingUp,
  AlertTriangle,
} from "lucide-react";

export const Route = createFileRoute("/student-area")({
  beforeLoad: () => {
    if (!isAuthenticated()) {
      throw redirect({ to: "/login", search: { redirect: "/student-area" } });
    }
  },
  component: StudentAreaPage,
});

function StudentAreaPage() {
  const navigate = Route.useNavigate();
  const { user, hasRole, logout } = useAuth();
  const { data: profile, isLoading: profileLoading } = useMyStudentProfile();
  const { data: membership, isLoading: membershipLoading } = useMyMembership();
  const { data: monthlyAttendance = 0, isLoading: attendanceLoading } = useMyMonthlyAttendance();
  const { data: enrollments = [], isLoading: enrollmentsLoading } = useMyEnrollments();
  const { data: videoLessons = [], isLoading: videoLessonsLoading } = useMyVideoLessons(true);
  const [videoErrorById, setVideoErrorById] = useState<Record<string, string>>({});
  const [videoBlobUrlById, setVideoBlobUrlById] = useState<Record<string, string>>({});

  const loading = profileLoading || membershipLoading || attendanceLoading || enrollmentsLoading;

  const readyActiveLessons = useMemo(
    () =>
      videoLessons.filter(
        (lesson) =>
          (lesson.status ? lesson.status === "READY" : true) &&
          (typeof lesson.isActive === "boolean" ? lesson.isActive : (lesson.active ?? true)),
      ),
    [videoLessons],
  );

  useEffect(() => {
    return () => {
      Object.values(videoBlobUrlById).forEach((url) => URL.revokeObjectURL(url));
    };
  }, [videoBlobUrlById]);

  if (user && hasRole(["ADMIN", "MANAGER"])) {
    void navigate({ to: "/admin", replace: true });
    return null;
  }

  if (user && hasRole(["PROFESSOR"])) {
    void navigate({ to: "/professor", replace: true });
    return null;
  }

  if (user && hasRole(["TRAINER"])) {
    void navigate({ to: "/trainer", replace: true });
    return null;
  }

  if (user && !hasRole(["CLIENT"])) {
    void navigate({ to: "/", replace: true });
    return null;
  }

  const handleLogout = () => {
    logout();
    void navigate({ to: "/", replace: true });
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="text-center">
          <Loader2 size={40} className="animate-spin mx-auto mb-4" style={{ color: "#C1121F" }} />
          <p className="text-text-secondary text-sm tracking-wider">A carregar...</p>
        </div>
      </div>
    );
  }

  const attendanceProgress = membership?.plan?.maxClasses
    ? Math.min(100, (monthlyAttendance / membership.plan.maxClasses) * 100)
    : Math.min(100, (monthlyAttendance / 12) * 100);

  const loadVideoBlob = async (lessonId: string) => {
    if (videoBlobUrlById[lessonId]) {
      return;
    }

    try {
      setVideoErrorById((prev) => ({ ...prev, [lessonId]: "" }));
      const blob = await api.videoLessons.getStreamBlob(lessonId);
      const objectUrl = URL.createObjectURL(blob);
      setVideoBlobUrlById((prev) => ({ ...prev, [lessonId]: objectUrl }));
    } catch (error) {
      setVideoErrorById((prev) => ({
        ...prev,
        [lessonId]: error instanceof Error ? error.message : "Nao foi possivel carregar o video.",
      }));
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <header className="bg-surface border-b border-border-subtle">
        <div className="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
          <Link to="/" className="flex flex-col hover:opacity-80 transition-opacity">
            <span className="font-display text-xl sm:text-2xl tracking-wider">4FOUR</span>
            <span className="font-sans text-[7px] sm:text-[8px] tracking-[0.3em] text-text-muted">
              FIGHT ACADEMY
            </span>
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
      </header>

      <main className="max-w-6xl mx-auto px-4 py-6 sm:py-8">
        <div className="mb-6 sm:mb-8">
          <h1 className="font-display text-2xl sm:text-4xl tracking-wider">ÁREA DO ALUNO</h1>
          <p className="text-text-secondary mt-1 text-sm sm:text-base">
            Bem-vindo de volta, {profile?.userName || "Atleta"}!
          </p>
          {!membership && (
            <p className="mt-3 text-sm text-primary font-semibold">
              Ainda não tens plano ativo —{" "}
              <Link to="/plans" className="underline">
                escolhe um abaixo
              </Link>
            </p>
          )}
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <Card
            className="bg-surface border-border-subtle"
            style={{ borderTop: "2px solid #C1121F" }}
          >
            <CardContent className="pt-6">
              <div className="flex items-center gap-3">
                <Award size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="text-xs tracking-wider uppercase text-text-secondary">Faixa</p>
                  <p className="font-display text-xl tracking-wider">
                    {profile?.beltName || "Sem faixa"}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card
            className="bg-surface border-border-subtle"
            style={{ borderTop: "2px solid #C1121F" }}
          >
            <CardContent className="pt-6">
              <div className="flex items-center gap-3">
                <Calendar size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="text-xs tracking-wider uppercase text-text-secondary">
                    Presenças este Mês
                  </p>
                  <p className="font-display text-xl tracking-wider">{monthlyAttendance}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card
            className="bg-surface border-border-subtle"
            style={{ borderTop: "2px solid #C1121F" }}
          >
            <CardContent className="pt-6">
              <div className="flex items-center gap-3">
                <CreditCard size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="text-xs tracking-wider uppercase text-text-secondary">Plano</p>
                  <p className="font-display text-xl tracking-wider">
                    {membership?.planName || "Sem plano"}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card
            className="bg-surface border-border-subtle"
            style={{ borderTop: "2px solid #C1121F" }}
          >
            <CardContent className="pt-6">
              <div className="flex items-center gap-3">
                <Target size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="text-xs tracking-wider uppercase text-text-secondary">Status</p>
                  <Badge
                    variant="outline"
                    className={
                      membership?.status === "ACTIVE"
                        ? "border-primary/30 bg-primary/10 text-primary tracking-wider mt-1"
                        : "border-destructive/30 bg-destructive/10 text-destructive tracking-wider mt-1"
                    }
                  >
                    {membership?.status === "ACTIVE" ? (
                      <>
                        <Check size={10} className="mr-1" />
                        ATIVO
                      </>
                    ) : (
                      "INATIVO"
                    )}
                  </Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <Tabs defaultValue="overview" className="space-y-6">
          <div className="overflow-x-auto">
            <TabsList className="bg-surface border border-border-subtle p-1">
              <TabsTrigger
                value="overview"
                className="tracking-[0.15em] uppercase text-xs rounded-sm"
              >
                Visão Geral
              </TabsTrigger>
              <TabsTrigger
                value="progress"
                className="tracking-[0.15em] uppercase text-xs rounded-sm"
              >
                Progresso
              </TabsTrigger>
              <TabsTrigger
                value="membership"
                className="tracking-[0.15em] uppercase text-xs rounded-sm"
              >
                Minha Mensalidade
              </TabsTrigger>
              <TabsTrigger
                value="video-lessons"
                className="tracking-[0.15em] uppercase text-xs rounded-sm"
              >
                Videoaulas
              </TabsTrigger>
            </TabsList>
          </div>

          <TabsContent value="overview">
            <Card
              className="bg-surface border-border-subtle"
              style={{ borderTop: "2px solid #C1121F" }}
            >
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Aulas Agendadas
                </CardTitle>
              </CardHeader>
              <CardContent>
                {enrollments.length > 0 ? (
                  <div className="space-y-3">
                    {enrollments.slice(0, 5).map((enrollment) => (
                      <div
                        key={enrollment.id}
                        className="flex justify-between items-center py-2 border-b border-border-subtle last:border-0"
                      >
                        <div className="flex items-center gap-3">
                          <Clock size={16} style={{ color: "#C1121F" }} />
                          <span>Aula #{enrollment.classId?.slice(0, 8)}</span>
                        </div>
                        <Badge
                          variant="outline"
                          className="border-primary/30 bg-primary/10 text-primary"
                        >
                          Agendada
                        </Badge>
                      </div>
                    ))}
                  </div>
                ) : (
                  <EmptyState
                    icon={Calendar}
                    title="Nenhuma aula agendada"
                    description="Agenda a tua primeira aula para comecar esta semana."
                  />
                )}
                <div className="mt-4 pt-4 border-t border-border-subtle">
                  {membership ? (
                    <Link to="/schedule">
                      <Button className="btn-red w-full tracking-[0.2em] uppercase text-xs">
                        Ver horários e agendar
                      </Button>
                    </Link>
                  ) : (
                    <Link to="/plans">
                      <Button className="btn-red w-full tracking-[0.2em] uppercase text-xs">
                        Escolher plano agora
                      </Button>
                    </Link>
                  )}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="progress">
            <Card
              className="bg-surface border-border-subtle"
              style={{ borderTop: "2px solid #C1121F" }}
            >
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Evolução Mensal
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div>
                    <div className="flex justify-between text-sm mb-2">
                      <span className="text-text-secondary">Presenças este mês</span>
                      <span className="font-semibold">
                        {monthlyAttendance} / {membership?.plan?.maxClasses || 12}
                      </span>
                    </div>
                    <Progress value={attendanceProgress} className="h-2 bg-surface-2" />
                  </div>
                  <div className="flex items-center gap-2 text-sm text-text-secondary">
                    {monthlyAttendance >= 8 ? (
                      <>
                        <Flame size={16} style={{ color: "#C1121F" }} />
                        <span>Incrível! Estás no topo do teu objetivo este mês.</span>
                      </>
                    ) : monthlyAttendance >= 4 ? (
                      <>
                        <TrendingUp size={16} style={{ color: "#C1121F" }} />
                        <span>Boa continuação! Mantém o ritmo de treino.</span>
                      </>
                    ) : (
                      <>
                        <Target size={16} style={{ color: "#C1121F" }} />
                        <span>Bora treinar mais este mês.</span>
                      </>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card
              className="bg-surface border-border-subtle mt-4"
              style={{ borderTop: "2px solid #C1121F" }}
            >
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Objetivos Pessoais
                </CardTitle>
              </CardHeader>
              <CardContent>
                {profile?.goals ? (
                  <p className="text-foreground">{profile.goals}</p>
                ) : (
                  <p className="text-text-secondary text-sm">
                    Define os teus objetivos no teu perfil para acompanhar o progresso.
                  </p>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="membership">
            <MembershipTabContent membership={membership} />
          </TabsContent>

          <TabsContent value="video-lessons">
            <Card
              className="bg-surface border-border-subtle"
              style={{ borderTop: "2px solid #C1121F" }}
            >
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">Videoaulas</CardTitle>
              </CardHeader>
              <CardContent>
                {videoLessonsLoading ? (
                  <p className="text-text-secondary">A carregar videoaulas...</p>
                ) : readyActiveLessons.length === 0 ? (
                  <EmptyState
                    icon={Target}
                    title="Sem videoaulas disponíveis"
                    description="Quando houver conteúdo para o teu plano ele aparece aqui."
                  />
                ) : (
                  <div className="space-y-6">
                    {(["JIU_JITSU", "BOXE_KICKBOXING", "CAPOEIRA", "MMA"] as const).map(
                      (modality) => {
                        const byModality = readyActiveLessons.filter(
                          (lesson) => lesson.modality === modality,
                        );
                        if (byModality.length === 0) return null;
                        const label =
                          modality === "JIU_JITSU"
                            ? "Jiu-Jitsu"
                            : modality === "BOXE_KICKBOXING"
                              ? "Boxe/Kickboxing"
                              : modality === "CAPOEIRA"
                                ? "Capoeira"
                                : "MMA";
                        return (
                          <div key={modality} className="space-y-3">
                            <h3 className="text-sm tracking-[0.15em] uppercase text-text-secondary">
                              {label}
                            </h3>
                            <div className="grid md:grid-cols-2 gap-4">
                              {byModality.map((lesson) => (
                                <Card key={lesson.id} className="bg-surface-2 border-border-subtle">
                                  <CardHeader>
                                    <CardTitle className="text-base">{lesson.title}</CardTitle>
                                    <p className="text-xs text-text-secondary">
                                      Plano mínimo:{" "}
                                      {lesson.minimumPlanRank === 1
                                        ? "Basic"
                                        : lesson.minimumPlanRank === 2
                                          ? "Standard"
                                          : "Premium"}
                                    </p>
                                  </CardHeader>
                                  <CardContent className="space-y-3">
                                    {lesson.description && (
                                      <p className="text-sm text-text-secondary">
                                        {lesson.description}
                                      </p>
                                    )}
                                    {lesson.embedUrl ? (
                                      <div className="aspect-video rounded-md overflow-hidden border border-border-subtle">
                                        <iframe
                                          title={lesson.title}
                                          src={lesson.embedUrl}
                                          className="w-full h-full"
                                          loading="lazy"
                                          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                                          allowFullScreen
                                          referrerPolicy="strict-origin-when-cross-origin"
                                        />
                                      </div>
                                    ) : (
                                      <div className="space-y-2">
                                        {videoBlobUrlById[lesson.id] ? (
                                          <video
                                            controls
                                            preload="metadata"
                                            className="w-full rounded-md border border-border-subtle bg-black"
                                          >
                                            <source
                                              src={videoBlobUrlById[lesson.id]}
                                              type="video/webm"
                                            />
                                          </video>
                                        ) : (
                                          <Button
                                            size="sm"
                                            variant="outline"
                                            onClick={() => void loadVideoBlob(lesson.id)}
                                          >
                                            Carregar video
                                          </Button>
                                        )}
                                        {videoErrorById[lesson.id] && (
                                          <p className="text-xs text-destructive">
                                            {videoErrorById[lesson.id]}
                                          </p>
                                        )}
                                      </div>
                                    )}
                                  </CardContent>
                                </Card>
                              ))}
                            </div>
                          </div>
                        );
                      },
                    )}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </main>
    </div>
  );
}

function MembershipTabContent({ membership }: { membership: Membership | undefined }) {
  const [dialogOpen, setDialogOpen] = useState(false);
  const cancelMutation = useCancelMyMembership();

  const handleCancel = () => {
    cancelMutation.mutate(undefined, {
      onSuccess: () => {
        setDialogOpen(false);
      },
    });
  };

  if (!membership) {
    return (
      <Card className="bg-surface border-border-subtle" style={{ borderTop: "2px solid #C1121F" }}>
        <CardContent>
          <div className="text-center py-6 space-y-4">
            <p className="text-text-secondary">Ainda não tens uma mensalidade ativa.</p>
            <Link to="/plans">
              <Button className="btn-red tracking-[0.2em] uppercase">Ver Planos</Button>
            </Link>
          </div>
        </CardContent>
      </Card>
    );
  }

  const accessUntil = membership.currentPeriodEnd || membership.endDate;
  const formattedAccessUntil = new Date(accessUntil).toLocaleDateString("pt-PT");

  return (
    <Card className="bg-surface border-border-subtle" style={{ borderTop: "2px solid #C1121F" }}>
      <CardHeader>
        <CardTitle className="text-xs tracking-[0.2em] uppercase">Dados da Mensalidade</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="flex justify-between py-2 border-b border-border-subtle">
            <span className="text-text-secondary">Plano</span>
            <span className="font-semibold">{membership.planName}</span>
          </div>
          <div className="flex justify-between py-2 border-b border-border-subtle">
            <span className="text-text-secondary">Início</span>
            <span>{new Date(membership.startDate).toLocaleDateString("pt-PT")}</span>
          </div>
          <div className="flex justify-between py-2 border-b border-border-subtle">
            <span className="text-text-secondary">Validade</span>
            <span>{new Date(membership.endDate).toLocaleDateString("pt-PT")}</span>
          </div>
          <div className="flex justify-between py-2 border-b border-border-subtle">
            <span className="text-text-secondary">Status</span>
            <Badge
              variant="outline"
              className={
                membership.status === "ACTIVE"
                  ? "border-primary/30 bg-primary/10 text-primary"
                  : "border-destructive/30 bg-destructive/10 text-destructive"
              }
            >
              {membership.status === "ACTIVE" ? "Ativo" : "Inativo"}
            </Badge>
          </div>
          {membership.stripePayment && (
            <div className="flex justify-between py-2 border-b border-border-subtle">
              <span className="text-text-secondary">Pagamento</span>
              <Badge className="bg-primary/20 text-primary border-primary/30">Stripe</Badge>
            </div>
          )}
          {accessUntil && (
            <div className="flex justify-between py-2 border-b border-border-subtle">
              <span className="text-text-secondary">
                {membership.cancelAtPeriodEnd
                  ? "Acesso até"
                  : membership.stripePayment
                    ? "Próxima renovação"
                    : "Fim do plano"}
              </span>
              <span>{formattedAccessUntil}</span>
            </div>
          )}

          {membership.cancelAtPeriodEnd ? (
            <div className="p-3 bg-yellow-500/10 border border-yellow-500/30 rounded-sm text-sm text-yellow-500 flex items-start gap-2">
              <AlertTriangle size={16} className="mt-0.5 shrink-0" />
              <div>
                <span className="font-semibold">Cancelamento agendado</span>
                <p className="mt-1">O teu acesso continua ativo até {formattedAccessUntil}.</p>
              </div>
            </div>
          ) : membership.status === "ACTIVE" ? (
            <AlertDialog open={dialogOpen} onOpenChange={setDialogOpen}>
              <AlertDialogTrigger asChild>
                <Button
                  variant="outline"
                  className="w-full border-destructive/50 text-destructive hover:bg-destructive/10 tracking-[0.1em] uppercase text-xs"
                >
                  Cancelar mensalidade
                </Button>
              </AlertDialogTrigger>
              <AlertDialogContent>
                <AlertDialogHeader>
                  <AlertDialogTitle>Cancelar mensalidade?</AlertDialogTitle>
                  <AlertDialogDescription>
                    A tua mensalidade continuará ativa até {formattedAccessUntil}. Depois dessa
                    data, não haverá nova renovação automática.
                  </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                  <AlertDialogCancel disabled={cancelMutation.isPending}>Voltar</AlertDialogCancel>
                  <AlertDialogAction
                    onClick={handleCancel}
                    disabled={cancelMutation.isPending}
                    className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                  >
                    {cancelMutation.isPending ? (
                      <>
                        <Loader2 size={14} className="animate-spin mr-1" />A cancelar...
                      </>
                    ) : (
                      "Confirmar cancelamento"
                    )}
                  </AlertDialogAction>
                </AlertDialogFooter>
              </AlertDialogContent>
            </AlertDialog>
          ) : null}

          {cancelMutation.isError && (
            <div className="p-3 bg-destructive/10 border border-destructive/30 rounded-sm text-sm text-destructive">
              {cancelMutation.error instanceof Error
                ? cancelMutation.error.message
                : "Ocorreu um erro ao cancelar a mensalidade."}
            </div>
          )}

          {cancelMutation.isSuccess && (
            <div className="p-3 bg-yellow-500/10 border border-yellow-500/30 rounded-sm text-sm text-yellow-500 flex items-start gap-2">
              <AlertTriangle size={16} className="mt-0.5 shrink-0" />
              <div>
                <span className="font-semibold">Cancelamento agendado</span>
                <p className="mt-1">
                  {cancelMutation.data?.message ||
                    `O teu acesso continua ativo até ${formattedAccessUntil}.`}
                </p>
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
