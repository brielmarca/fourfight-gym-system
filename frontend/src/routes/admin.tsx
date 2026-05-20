import { createFileRoute, redirect } from "@tanstack/react-router";
import { useAuth } from "@/contexts/auth-context";
import { isAuthenticated, getUser, clearTokens } from "@/lib/api";
import {
  useApproveReceptionRequest,
  useBelts,
  useMemberships,
  usePendingReceptionRequests,
  usePlans,
  useRejectReceptionRequest,
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
import { Loader2, LogOut } from "lucide-react";

export const Route = createFileRoute("/admin")({
  beforeLoad: () => {
    if (!isAuthenticated()) {
      throw redirect({ to: "/login", search: { redirect: "/admin" } });
    }
  },
  component: AdminPage,
});

function AdminPage() {
  const { user, hasRole, logout } = useAuth();
  const { data: belts = [], isLoading: beltsLoading } = useBelts();
  const { data: membershipsData, isLoading: membershipsLoading } = useMemberships(0, 50);
  const { data: plans = [], isLoading: plansLoading } = usePlans();
  const canManageReception = hasRole(["ADMIN"]);
  const { data: pendingReception = [], isLoading: pendingReceptionLoading } = usePendingReceptionRequests(canManageReception);
  const approveReception = useApproveReceptionRequest();
  const rejectReception = useRejectReceptionRequest();

  const loading = beltsLoading || membershipsLoading || plansLoading || pendingReceptionLoading;

  if (user && !hasRole(["ADMIN", "MANAGER"])) {
    window.location.href = "/student-area";
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
          <p className="text-text-secondary text-sm tracking-wider">Loading...</p>
        </div>
      </div>
    );
  }

  const memberships = membershipsData as {
    content?: Array<{
      id: string;
      userName: string;
      planName: string;
      startDate: string;
      endDate: string;
      status: string;
    }>;
    totalElements?: number;
  } | null;
  const activeCount = memberships?.content?.filter((m) => m.status === "ACTIVE").length || 0;
  const expiredCount = memberships?.content?.filter((m) => m.status === "EXPIRED").length || 0;

  return (
    <div className="min-h-screen bg-background">
      <header className="bg-surface border-b border-border-subtle">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <a href="/" className="flex flex-col">
            <span className="font-display text-xl sm:text-2xl tracking-wider">4FOUR</span>
            <span className="font-sans text-[7px] sm:text-[8px] tracking-[0.3em] text-muted-foreground">
              FIGHT ACADEMY ADMIN
            </span>
          </a>
          <div className="flex items-center gap-2 md:gap-4">
            <a href="/" className="text-xs text-text-secondary hover:text-foreground">
              Ver Site
            </a>
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
          <p className="text-text-secondary mt-1 text-sm sm:text-base">Gerencie sua academia</p>
        </div>

        <Tabs defaultValue="dashboard" className="space-y-6">
          <div className="overflow-x-auto">
            <TabsList className="bg-surface border border-border-subtle">
            <TabsTrigger
              value="dashboard"
              className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white"
            >
              Dashboard
            </TabsTrigger>
            <TabsTrigger
              value="students"
              className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white"
            >
              Alunos
            </TabsTrigger>
            <TabsTrigger
              value="belts"
              className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white"
            >
              Faixas
            </TabsTrigger>
            <TabsTrigger
              value="plans"
              className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white"
            >
              Planos
            </TabsTrigger>
            {canManageReception && (
              <TabsTrigger
                value="reception"
                className="tracking-[0.15em] uppercase text-xs data-[state=active]:bg-primary data-[state=active]:text-white"
              >
                Receção
              </TabsTrigger>
            )}
          </TabsList>
          </div>

          <TabsContent value="dashboard">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
              <Card className="bg-surface border-border-subtle">
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary">
                    Total de Alunos
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider">
                    {memberships?.totalElements || 0}
                  </p>
                </CardContent>
              </Card>

              <Card className="bg-surface border-border-subtle">
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary">
                    Matrículas Ativas
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider text-green-500">
                    {activeCount}
                  </p>
                </CardContent>
              </Card>

              <Card className="bg-surface border-border-subtle">
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary">
                    Matrículas Expiradas
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider text-destructive">
                    {expiredCount}
                  </p>
                </CardContent>
              </Card>

              <Card className="bg-surface border-border-subtle">
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary">
                    Faixas Cadastradas
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider">{belts.length}</p>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="students">
            <Card className="bg-surface border-border-subtle">
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Todos os Alunos
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow className="border-border-subtle">
                      <TableHead className="text-text-secondary">Aluno</TableHead>
                      <TableHead className="text-text-secondary">Plano</TableHead>
                      <TableHead className="text-text-secondary">Início</TableHead>
                      <TableHead className="text-text-secondary">Validade</TableHead>
                      <TableHead className="text-text-secondary">Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {memberships?.content?.length > 0 ? (
                      memberships.content.map((m) => (
                        <TableRow key={m.id} className="border-border-subtle">
                          <TableCell className="font-medium">{m.userName}</TableCell>
                          <TableCell>{m.planName}</TableCell>
                          <TableCell>{new Date(m.startDate).toLocaleDateString("pt-PT")}</TableCell>
                          <TableCell>{new Date(m.endDate).toLocaleDateString("pt-PT")}</TableCell>
                          <TableCell>
                            <Badge
                              variant={
                                m.status === "ACTIVE"
                                  ? "default"
                                  : m.status === "EXPIRED"
                                    ? "destructive"
                                    : "secondary"
                              }
                            >
                              {m.status}
                            </Badge>
                          </TableCell>
                        </TableRow>
                      ))
                    ) : (
                      <TableRow>
                        <TableCell colSpan={5} className="text-center py-8 text-text-secondary">
                          Nenhum aluno encontrado
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="belts">
            <Card className="bg-surface border-border-subtle">
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Gerenciar Faixas
                </CardTitle>
              </CardHeader>
              <CardContent>
                {belts.length > 0 ? (
                  <div className="space-y-2">
                    {belts.map((belt) => (
                      <div
                        key={belt.id}
                        className="flex items-center justify-between p-3 bg-surface-2 rounded-md"
                      >
                        <div className="flex items-center gap-3">
                          <div
                            className="w-6 h-6 rounded-full border-2"
                            style={{ borderColor: belt.colorHex }}
                          />
                          <span className="font-medium">{belt.name}</span>
                        </div>
                        <span className="text-xs text-text-secondary">Ordem: {belt.rankOrder}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-text-secondary">
                    Nenhuma faixa cadastrada. Cadastre as faixas da academia.
                  </p>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="plans">
            <Card className="bg-surface border-border-subtle">
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
                        className="flex items-center justify-between p-3 bg-surface-2 rounded-md"
                      >
                        <div>
                          <p className="font-medium">{plan.name}</p>
                          <p className="text-xs text-text-secondary">
                            {plan.durationDays} dias • {plan.maxClasses || "Ilimitadas"} aulas
                          </p>
                        </div>
                        <p className="font-display text-xl">€{plan.price}</p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-text-secondary">Nenhum plano cadastrado.</p>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {canManageReception && <TabsContent value="reception">
            <Card className="bg-surface border-border-subtle">
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Pedidos pendentes na receção
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow className="border-border-subtle">
                        <TableHead className="text-text-secondary">Cliente</TableHead>
                        <TableHead className="text-text-secondary">Plano</TableHead>
                        <TableHead className="text-text-secondary">Preco</TableHead>
                        <TableHead className="text-text-secondary">Pedido</TableHead>
                        <TableHead className="text-text-secondary">Status</TableHead>
                        <TableHead className="text-text-secondary text-right">Acoes</TableHead>
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
                                <Badge variant="secondary">{request.status}</Badge>
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
                        <TableRow>
                          <TableCell colSpan={6} className="text-center py-8 text-text-secondary">
                            Sem pedidos pendentes na receção.
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
            </Card>
          </TabsContent>}
        </Tabs>
      </main>
    </div>
  );
}
