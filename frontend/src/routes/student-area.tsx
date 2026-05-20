import { createFileRoute, Link, redirect } from "@tanstack/react-router";
import { useAuth } from "@/contexts/auth-context";
import { isAuthenticated, getUser, clearTokens } from "@/lib/api";
import {
  useMyStudentProfile,
  useMyMembership,
  useMyMonthlyAttendance,
  useMyEnrollments,
  useStripeSubscription,
  useCancelStripeSubscription,
} from "@/queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Loader2,
  LogOut,
  Award,
  Calendar,
  CreditCard,
  Target,
  Check,
  Clock,
  MapPin,
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
  const { user, hasRole, logout } = useAuth();
  const { data: profile, isLoading: profileLoading } = useMyStudentProfile();
  const { data: membership, isLoading: membershipLoading } = useMyMembership();
  const { data: monthlyAttendance = 0, isLoading: attendanceLoading } = useMyMonthlyAttendance();
  const { data: enrollments = [], isLoading: enrollmentsLoading } = useMyEnrollments();

  const loading = profileLoading || membershipLoading || attendanceLoading || enrollmentsLoading;

  if (user && hasRole(["ADMIN", "MANAGER"])) {
    window.location.href = "/admin";
    return null;
  }

  const handleLogout = () => {
    logout();
    window.location.href = "/";
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

  return (
    <div className="min-h-screen bg-background">
      <header className="bg-surface border-b border-border-subtle">
        <div className="max-w-6xl mx-auto px-4 py-4 flex items-center justify-between">
          <Link to="/" className="flex flex-col hover:opacity-80 transition-opacity">
            <span className="font-display text-2xl tracking-wider">4FOUR</span>
            <span className="font-sans text-[8px] tracking-[0.3em] text-text-muted">
              FIGHT ACADEMY
            </span>
          </Link>
          <Button
            onClick={handleLogout}
            variant="ghost"
            className="text-xs tracking-wider uppercase text-text-secondary hover:text-foreground"
          >
            <LogOut size={14} className="mr-1" />
            Sair
          </Button>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="font-display text-3xl sm:text-4xl tracking-wider">ÁREA DO ALUNO</h1>
          <p className="text-text-secondary mt-1">
            Bem-vindo de volta, {profile?.userName || "Atleta"}! 👋
          </p>
          {!membership && (
            <p className="mt-3 text-sm text-primary font-semibold">
              ⚡ Ainda não tens plano ativo —{" "}
              <a href="/plans" className="underline">
                escolhe um abaixo
              </a>
            </p>
          )}
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <Card className="bg-surface border-border-subtle">
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

          <Card className="bg-surface border-border-subtle">
            <CardContent className="pt-6">
              <div className="flex items-center gap-3">
                <Calendar size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="text-xs tracking-wider uppercase text-text-secondary">
                    Presença este Mês
                  </p>
                  <p className="font-display text-xl tracking-wider">{monthlyAttendance}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-surface border-border-subtle">
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

          <Card className="bg-surface border-border-subtle">
            <CardContent className="pt-6">
              <div className="flex items-center gap-3">
                <Target size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="text-xs tracking-wider uppercase text-text-secondary">Status</p>
                  <Badge
                    variant={membership?.status === "ACTIVE" ? "default" : "destructive"}
                    className="tracking-wider mt-1"
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
          <TabsList className="bg-surface border border-border-subtle">
            <TabsTrigger value="overview" className="tracking-[0.15em] uppercase text-xs">
              Visão Geral
            </TabsTrigger>
            <TabsTrigger value="progress" className="tracking-[0.15em] uppercase text-xs">
              Progresso
            </TabsTrigger>
            <TabsTrigger value="membership" className="tracking-[0.15em] uppercase text-xs">
              Minha Mensalidade
            </TabsTrigger>
          </TabsList>
          </div>

          <TabsContent value="overview">
            <Card className="bg-surface border-border-subtle">
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
                        <Badge variant="outline">Agendada</Badge>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-text-secondary">Nenhuma aula agendada.</p>
                )}
                <div className="mt-4 pt-4 border-t border-border-subtle">
                  {membership ? (
                    <Link href="/schedule">
                      <Button className="btn-red w-full tracking-[0.2em] uppercase text-xs">
                        📅 VER HORÁRIOS E AGENDAR
                      </Button>
                    </Link>
                  ) : (
                    <Link href="/plans">
                      <Button className="btn-red w-full tracking-[0.2em] uppercase text-xs">
                        🎯 ESCOLHER PLANO AGORA
                      </Button>
                    </Link>
                  )}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="progress">
            <Card className="bg-surface border-border-subtle">
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
                  <p className="text-sm text-text-secondary">
                    {monthlyAttendance >= 8
                      ? "🔥 Incrível! Estás no topo do teu objetivo este mês!"
                      : monthlyAttendance >= 4
                        ? "💪 Boa continuação! Mantém o ritmo de treino."
                        : "🎯 Bora treinar mais este mês!"}
                  </p>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-surface border-border-subtle mt-4">
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
                    Define teus objetivos no teu perfil para acompanhar o progresso.
                  </p>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="membership">
            <Card className="bg-surface border-border-subtle">
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Dados da Mensalidade
                </CardTitle>
              </CardHeader>
              <CardContent>
                {membership ? (
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
                      <Badge variant={membership.status === "ACTIVE" ? "default" : "destructive"}>
                        {membership.status}
                      </Badge>
                    </div>
                    {membership.stripeSubscriptionId && (
                      <>
                        <div className="flex justify-between py-2 border-b border-border-subtle">
                          <span className="text-text-secondary">Pagamento</span>
                          <Badge className="bg-primary/20 text-primary border-primary/30">
                            Stripe
                          </Badge>
                        </div>
                        {membership.currentPeriodEnd && (
                          <div className="flex justify-between py-2 border-b border-border-subtle">
                            <span className="text-text-secondary">Proxima renovacao</span>
                            <span>{new Date(membership.currentPeriodEnd).toLocaleDateString("pt-PT")}</span>
                          </div>
                        )}
                        {membership.cancelAtPeriodEnd && (
                          <div className="p-3 rounded-md bg-yellow-500/10 border border-yellow-500/30 text-sm text-yellow-500">
                            A tua subscricao sera cancelada no fim do periodo atual.
                          </div>
                        )}
                      </>
                    )}
                  </div>
                ) : (
                  <div className="text-center py-6 space-y-4">
                    <p className="text-text-secondary">Ainda não tens uma mensalidade ativa.</p>
                    <Link href="/plans">
                      <Button className="btn-red tracking-[0.2em] uppercase">Ver Planos</Button>
                    </Link>
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
