import { useState } from "react";
import { Link, createFileRoute, redirect, useNavigate } from "@tanstack/react-router";
import { useAuth } from "@/contexts/auth-context";
import { isAuthenticated, getUser } from "@/lib/api";
import {
  useApproveReceptionRequest,
  useBelts,
  useMemberships,
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
import { Loader2, LogOut, Users, Shield, CreditCard, Clock } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import type { CreateScheduleEntryRequest } from "@/types";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

export const Route = createFileRoute("/admin")({
  beforeLoad: () => {
    if (!isAuthenticated()) {
      throw redirect({ to: "/login", search: { redirect: "/admin" } });
    }
  },
  component: AdminPage,
});

function AdminPage() {
  const navigate = useNavigate();
  const { user, hasRole, logout } = useAuth();
  const { data: belts = [], isLoading: beltsLoading } = useBelts();
  const { data: membershipsData, isLoading: membershipsLoading } = useMemberships(0, 50);
  const { data: plans = [], isLoading: plansLoading } = usePlans();
  const canManageReception = hasRole(["ADMIN"]);
  const { data: pendingReception = [], isLoading: pendingReceptionLoading } =
    usePendingReceptionRequests(canManageReception);
  const approveReception = useApproveReceptionRequest();
  const rejectReception = useRejectReceptionRequest();
  const { data: adminSchedule = [], isLoading: adminScheduleLoading } = useAdminSchedule(hasRole(["ADMIN"]));
  const createSchedule = useCreateScheduleEntry();
  const updateSchedule = useUpdateScheduleEntry();
  const deactivateSchedule = useDeactivateScheduleEntry();
  const [editingId, setEditingId] = useState<string | null>(null);
  const [selectedPreRegistrationId, setSelectedPreRegistrationId] = useState<string>("");
  const [selectedCsvFile, setSelectedCsvFile] = useState<File | null>(null);
  const [importFeedback, setImportFeedback] = useState<string>("");
  const [formError, setFormError] = useState<string | null>(null);
  const importPreRegistrations = useImportPreRegistrationsCsv();
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

  const loading = beltsLoading || membershipsLoading || plansLoading || pendingReceptionLoading;
  const { data: preRegistrationsData, isLoading: preRegistrationsLoading } = usePreRegistrations(0, 100, canManageReception);
  const { data: preRegistrationDetail } = usePreRegistrationDetail(selectedPreRegistrationId, canManageReception && !!selectedPreRegistrationId);

  if (user && !hasRole(["ADMIN", "MANAGER"])) {
    void navigate({ to: "/student-area", replace: true });
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
          <p className="text-text-secondary text-sm tracking-wider">A carregar painel...</p>
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
          <p className="text-text-secondary mt-1 text-sm sm:text-base">Gerencie sua academia</p>
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
                Faixas
              </TabsTrigger>
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
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
              <Card
                className="bg-surface border-border-subtle"
                style={{ borderTop: "2px solid #C1121F" }}
              >
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary flex items-center gap-2">
                    <Users size={14} />
                    Total de Alunos
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider" style={{ color: "#F5F5F5" }}>
                    {memberships?.totalElements || 0}
                  </p>
                </CardContent>
              </Card>

              <Card
                className="bg-surface border-border-subtle"
                style={{ borderTop: "2px solid #22C55E" }}
              >
                <CardHeader className="pb-2">
                  <CardTitle className="text-xs tracking-[0.2em] uppercase text-text-secondary flex items-center gap-2">
                    <Shield size={14} />
                    Matrículas Ativas
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider" style={{ color: "#22C55E" }}>
                    {activeCount}
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
                    Matrículas Expiradas
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider" style={{ color: "#C1121F" }}>
                    {expiredCount}
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
                    Faixas Cadastradas
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="font-display text-4xl tracking-wider" style={{ color: "#F5F5F5" }}>
                    {belts.length}
                  </p>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="schedule">
            <Card className="bg-surface border-border-subtle" style={{ borderTop: "2px solid #C1121F" }}>
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">Gestão de horários</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid md:grid-cols-2 gap-3">
                  <Input placeholder="Nome da aula" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
                  <Input placeholder="Instrutor" value={form.instructorName} onChange={(e) => setForm({ ...form, instructorName: e.target.value })} />
                  <Select value={form.modality} onValueChange={(value) => setForm({ ...form, modality: value as CreateScheduleEntryRequest["modality"] })}><SelectTrigger><SelectValue /></SelectTrigger><SelectContent><SelectItem value="JIU_JITSU">Jiu-Jitsu</SelectItem><SelectItem value="BOXE_KICKBOXING">Boxe / Kickboxing</SelectItem><SelectItem value="CAPOEIRA">Capoeira</SelectItem><SelectItem value="MMA">MMA</SelectItem></SelectContent></Select>
                  <Select value={form.dayOfWeek} onValueChange={(value) => setForm({ ...form, dayOfWeek: value as CreateScheduleEntryRequest["dayOfWeek"] })}><SelectTrigger><SelectValue /></SelectTrigger><SelectContent><SelectItem value="MONDAY">Segunda</SelectItem><SelectItem value="TUESDAY">Terça</SelectItem><SelectItem value="WEDNESDAY">Quarta</SelectItem><SelectItem value="THURSDAY">Quinta</SelectItem><SelectItem value="FRIDAY">Sexta</SelectItem><SelectItem value="SATURDAY">Sábado</SelectItem><SelectItem value="SUNDAY">Domingo</SelectItem></SelectContent></Select>
                  <Input type="time" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} />
                  <Input type="time" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} />
                  <Select value={form.level} onValueChange={(value) => setForm({ ...form, level: value as CreateScheduleEntryRequest["level"] })}><SelectTrigger><SelectValue /></SelectTrigger><SelectContent><SelectItem value="BEGINNER">Iniciante</SelectItem><SelectItem value="INTERMEDIATE">Intermédio</SelectItem><SelectItem value="ADVANCED">Avançado</SelectItem><SelectItem value="ALL_LEVELS">Todos os níveis</SelectItem></SelectContent></Select>
                  <Input placeholder="Sala/Local" value={form.location ?? ""} onChange={(e) => setForm({ ...form, location: e.target.value })} />
                  <Input type="number" placeholder="Capacidade" value={form.capacity ?? ""} onChange={(e) => setForm({ ...form, capacity: e.target.value ? Number(e.target.value) : null })} />
                  <div className="flex items-center gap-2"><Switch checked={!!form.active} onCheckedChange={(v) => setForm({ ...form, active: v })} /><span className="text-sm">Ativo</span></div>
                </div>
                <Textarea placeholder="Notas" value={form.notes ?? ""} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
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
                      setForm({ ...form, title: "", instructorName: "", notes: "", location: "", capacity: null });
                    } catch (error) {
                      setFormError(error instanceof Error ? error.message : "Erro ao guardar horário.");
                    }
                  }}
                >
                  {createSchedule.isPending || updateSchedule.isPending ? "A guardar..." : editingId ? "Atualizar" : "Adicionar"}
                </Button>

                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader><TableRow><TableHead>Dia</TableHead><TableHead>Hora</TableHead><TableHead>Aula</TableHead><TableHead>Instrutor</TableHead><TableHead>Ativo</TableHead><TableHead className="text-right">Ações</TableHead></TableRow></TableHeader>
                    <TableBody>
                      {adminScheduleLoading ? (
                        <TableRow><TableCell colSpan={6}>A carregar horários...</TableCell></TableRow>
                      ) : adminSchedule.length === 0 ? (
                        <TableRow><TableCell colSpan={6}>Sem horários cadastrados.</TableCell></TableRow>
                      ) : (
                        adminSchedule.map((entry) => (
                          <TableRow key={entry.id}>
                            <TableCell>{entry.dayOfWeek}</TableCell>
                            <TableCell>{entry.startTime.slice(0, 5)} - {entry.endTime.slice(0, 5)}</TableCell>
                            <TableCell>{entry.title}</TableCell>
                            <TableCell>{entry.instructorName}</TableCell>
                            <TableCell>{entry.active ? "Sim" : "Não"}</TableCell>
                            <TableCell className="text-right space-x-2">
                              <Button size="sm" variant="outline" onClick={() => { setEditingId(entry.id); setForm({ ...entry, location: entry.location ?? "", notes: entry.notes ?? "" }); }}>Editar</Button>
                              <Button size="sm" variant="destructive" disabled={deactivateSchedule.isPending} onClick={() => deactivateSchedule.mutate(entry.id)}>
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
                <CardTitle className="text-xs tracking-[0.2em] uppercase">
                  Todos os Alunos
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow className="border-border-subtle hover:bg-transparent">
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
                            <TableCell>
                              {new Date(m.startDate).toLocaleDateString("pt-PT")}
                            </TableCell>
                            <TableCell>{new Date(m.endDate).toLocaleDateString("pt-PT")}</TableCell>
                            <TableCell>
                              <Badge
                                variant="outline"
                                className={
                                  m.status === "ACTIVE"
                                    ? "border-primary/30 bg-primary/10 text-primary"
                                    : m.status === "EXPIRED"
                                      ? "border-destructive/30 bg-destructive/10 text-destructive"
                                      : "border-border-subtle bg-surface-2 text-text-secondary"
                                }
                              >
                                {m.status === "ACTIVE"
                                  ? "Ativo"
                                  : m.status === "EXPIRED"
                                    ? "Expirado"
                                    : m.status}
                              </Badge>
                            </TableCell>
                          </TableRow>
                        ))
                      ) : (
                        <TableRow className="hover:bg-transparent">
                          <TableCell colSpan={5} className="text-center py-12 text-text-secondary">
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

          <TabsContent value="pre-registrations">
            <Card className="bg-surface border-border-subtle" style={{ borderTop: "2px solid #C1121F" }}>
              <CardHeader>
                <CardTitle className="text-xs tracking-[0.2em] uppercase">Clientes / Pré-inscrições</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex flex-col gap-2 md:flex-row md:items-center">
                  <Input
                    type="file"
                    accept=".csv,text/csv"
                    onChange={(event) => setSelectedCsvFile(event.target.files?.[0] ?? null)}
                    className="max-w-xl"
                  />
                  <Button onClick={handleImportCsv} disabled={!selectedCsvFile || importPreRegistrations.isPending}>
                    {importPreRegistrations.isPending ? "A importar..." : "Importar CSV"}
                  </Button>
                </div>
                {importFeedback && <p className="text-xs text-text-secondary">{importFeedback}</p>}
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow className="border-border-subtle hover:bg-transparent">
                        <TableHead>Nome</TableHead><TableHead>Telefone</TableHead><TableHead>Idade</TableHead><TableHead>Freguesia</TableHead><TableHead>Modalidades</TableHead><TableHead>Horario</TableHead><TableHead>Dias</TableHead><TableHead>Contacto</TableHead><TableHead>Data</TableHead><TableHead>Status</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {preRegistrationsLoading ? (
                        <TableRow><TableCell colSpan={10}>A carregar pré-inscrições...</TableCell></TableRow>
                      ) : (preRegistrationsData?.content?.length ?? 0) === 0 ? (
                        <TableRow><TableCell colSpan={10}>Sem pré-inscrições registadas.</TableCell></TableRow>
                      ) : (
                        preRegistrationsData?.content?.map((item) => (
                          <TableRow key={item.id} className="cursor-pointer" onClick={() => setSelectedPreRegistrationId(item.id)}>
                            <TableCell>{item.fullName}</TableCell><TableCell>{item.phone}</TableCell><TableCell>{item.age ?? "-"}</TableCell><TableCell>{item.parish || "-"}</TableCell><TableCell>{item.preferredModalities || "-"}</TableCell><TableCell>{item.preferredTrainingTimes || "-"}</TableCell><TableCell>{item.preferredTrainingDays || "-"}</TableCell><TableCell>{item.preferredContactMethod || "-"}</TableCell><TableCell>{new Date(item.submittedAt).toLocaleDateString("pt-PT")}</TableCell><TableCell>{item.status}</TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </div>

                {preRegistrationDetail && (
                  <Card className="bg-surface-2 border-border-subtle">
                    <CardHeader>
                      <CardTitle className="text-sm tracking-[0.1em] uppercase">Detalhes completos do cliente</CardTitle>
                    </CardHeader>
                    <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                      <div><strong>Nome:</strong> {preRegistrationDetail.fullName}</div>
                      <div><strong>Telefone:</strong> {preRegistrationDetail.phone}</div>
                      <div><strong>Idade:</strong> {preRegistrationDetail.age}</div>
                      <div><strong>Submetido em:</strong> {new Date(preRegistrationDetail.submittedAt).toLocaleString("pt-PT")}</div>
                      <div><strong>Morada/Freguesia:</strong> {preRegistrationDetail.parish || "-"}</div>
                      <div><strong>Experiência prévia:</strong> {preRegistrationDetail.hasMartialArtsExperience ? "Sim" : "Não"}</div>
                      <div><strong>Detalhes experiência:</strong> {preRegistrationDetail.martialArtsExperienceDetails || "-"}</div>
                      <div className="md:col-span-2"><strong>Objetivo:</strong> {preRegistrationDetail.trainingGoal || "-"}</div>
                      <div><strong>Modalidade:</strong> {preRegistrationDetail.preferredModalities || "-"}</div>
                      <div><strong>Horário:</strong> {preRegistrationDetail.preferredTrainingTimes || "-"}</div>
                      <div><strong>Dias:</strong> {preRegistrationDetail.preferredTrainingDays || "-"}</div>
                      <div><strong>Filosofia importante:</strong> {preRegistrationDetail.philosophyImportant ? "Sim" : "Não"}</div>
                      <div><strong>Contacto preferido:</strong> {preRegistrationDetail.preferredContactMethod}</div>
                      <div><strong>Origem:</strong> {preRegistrationDetail.source}</div>
                      <div><strong>Status:</strong> {preRegistrationDetail.status}</div>
                      <div className="md:col-span-2"><strong>Notas:</strong> {preRegistrationDetail.notes || "-"}</div>
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
                  Gerenciar Faixas
                </CardTitle>
              </CardHeader>
              <CardContent>
                {belts.length > 0 ? (
                  <div className="space-y-2">
                    {belts.map((belt) => (
                      <div
                        key={belt.id}
                        className="flex items-center justify-between p-3 bg-surface-2 border border-border-subtle"
                      >
                        <div className="flex items-center gap-3">
                          <div
                            className="w-6 h-6 border-2"
                            style={{
                              borderColor: belt.colorHex,
                              backgroundColor: belt.colorHex + "22",
                            }}
                          />
                          <span className="font-medium">{belt.name}</span>
                        </div>
                        <span className="text-xs text-text-secondary">Ordem: {belt.rankOrder}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-12">
                    <Shield size={32} className="mx-auto mb-3 text-text-muted" />
                    <p className="text-text-secondary">
                      Nenhuma faixa cadastrada. Cadastre as faixas da academia.
                    </p>
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
