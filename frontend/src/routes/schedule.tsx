import { useState, useEffect } from "react";
import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

import { isAuthenticated } from "@/lib/api";
import { useClasses, useMyEnrollments, useMyMembership, useEnrollClass } from "@/queries";
import {
  mockScheduleClasses,
  type ScheduleClass,
  type Modality,
  type DayOfWeek,
} from "@/types/schedule";
import {
  canAccessClass,
  hasReachedWeeklyLimit,
  getLockedMessage,
  filterByModality,
  filterByDay,
  filterByTime,
  getTimeSlots,
  membershipToUserPlan,
} from "@/lib/schedule-utils";
import {
  Clock,
  Calendar,
  MapPin,
  Loader2,
  Check,
  X,
  Users,
  Zap,
  Filter,
  ChevronDown,
} from "lucide-react";

export const Route = createFileRoute("/schedule")({
  component: SchedulePage,
});

const dayNames: Record<number, string> = {
  0: "Domingo",
  1: "Segunda",
  2: "Terça",
  3: "Quarta",
  4: "Quinta",
  5: "Sexta",
  6: "Sábado",
};

function SchedulePage() {
  const navigate = useNavigate();
  const authenticated = isAuthenticated();

  const { data: apiClasses, isLoading: classesLoading } = useClasses();
  const { data: enrollments = [], isLoading: enrollmentsLoading } = useMyEnrollments();
  const { data: membership, isLoading: membershipLoading } = useMyMembership();
  const enrollMutation = useEnrollClass();

  const [classes, setClasses] = useState<ScheduleClass[]>(mockScheduleClasses);
  const [enrolling, setEnrolling] = useState<string | null>(null);
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);
  const [modalityFilter, setModalityFilter] = useState<Modality | "Todos">("Todos");
  const [dayFilter, setDayFilter] = useState<DayOfWeek | "Todos">("Todos");
  const [timeFilter, setTimeFilter] = useState<string>("Todos");
  const [selectedClassId, setSelectedClassId] = useState<string | null>(null);

  useEffect(() => {
    if (apiClasses && apiClasses.length > 0) {
      const converted: ScheduleClass[] = apiClasses.map((c) => ({
        id: c.id,
        modality: (c.type || "Jiu-Jitsu") as Modality,
        dayOfWeek: (c.dayOfWeek ?? 1) as DayOfWeek,
        time: `${c.startTime ?? "09:00"} - ${c.endTime ?? "10:00"}`,
        startTime: c.startTime ?? "09:00",
        endTime: c.endTime ?? "10:00",
        level: "Todos",
        requiredPlan: "Basic",
        capacity: c.maxStudents ?? c.capacity ?? 20,
        enrolledCount: c.enrolledCount ?? 0,
        trainerName: c.trainerName,
        isActive: c.isActive ?? true,
      }));
      setClasses(converted);
    }
  }, [apiClasses]);

  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => setMessage(null), 4000);
      return () => clearTimeout(timer);
    }
  }, [message]);

  const today = new Date().getDay();
  const userPlan = membershipToUserPlan(membership);
  const loading = classesLoading || enrollmentsLoading || membershipLoading;

  const timeSlots = getTimeSlots(classes);
  const modalities: (Modality | "Todos")[] = [
    "Todos",
    "Jiu-Jitsu",
    "Boxe / Kickboxing",
    "Força & Condicionamento",
  ];

  const filteredClasses = classes
    .filter((c) => c.isActive)
    .filter((c) => modalityFilter === "Todos" || c.modality === modalityFilter)
    .filter((c) => dayFilter === "Todos" || c.dayOfWeek === dayFilter)
    .filter((c) => timeFilter === "Todos" || c.startTime === timeFilter)
    .sort((a, b) => {
      if (a.dayOfWeek !== b.dayOfWeek) return a.dayOfWeek - b.dayOfWeek;
      return a.startTime.localeCompare(b.startTime);
    });

  const getAvailableSpots = (cls: ScheduleClass) => {
    return Math.max(0, cls.capacity - cls.enrolledCount);
  };

  const isEnrolled = (classId: string) => enrollments.some((e) => e.classId === classId);

  const handleEnroll = async (classId: string) => {
    if (!authenticated) {
      navigate({ to: "/login", search: { redirect: "/schedule" } });
      return;
    }
    if (!userPlan || !userPlan.active) {
      setMessage({ type: "error", text: "Precisas de um plano ativo para agendar aulas." });
      return;
    }
    setEnrolling(classId);
    setMessage(null);
    try {
      await enrollMutation.mutateAsync(classId);
      setMessage({ type: "success", text: "Aula agendada com sucesso!" });
    } catch (err) {
      setMessage({
        type: "error",
        text: err instanceof Error ? err.message : "Erro ao agendar. Tenta novamente.",
      });
    } finally {
      setEnrolling(null);
    }
  };

  return (
    <main className="bg-background text-foreground min-h-screen">
      <div className="pt-32 pb-20 px-4">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h1 className="font-display text-3xl sm:text-4xl md:text-5xl lg:text-6xl tracking-wider">HORÁRIO DAS AULAS</h1>
            <p className="mt-4 text-text-secondary text-lg">
              Treina quando te convém. Aparece. Repete.
            </p>
          </div>

          {!authenticated ? (
            <div className="text-center py-20 space-y-6">
              <div className="w-20 h-20 mx-auto rounded-full bg-surface flex items-center justify-center">
                <Calendar size={32} style={{ color: "#C1121F" }} />
              </div>
              <p className="text-text-secondary text-lg">
                Inicia sessão para ver horários disponíveis para o teu plano.
              </p>
              <Link href="/login" search={{ redirect: "/schedule" }}>
                <Button className="btn-red tracking-[0.2em] uppercase text-xs px-8 py-4">
                  Fazer Login
                </Button>
              </Link>
            </div>
          ) : !userPlan || !userPlan.active ? (
            <div className="text-center py-20 space-y-6">
              <div className="w-20 h-20 mx-auto rounded-full bg-surface flex items-center justify-center">
                <Zap size={32} style={{ color: "#C1121F" }} />
              </div>
              <p className="text-text-secondary text-lg">Ainda não tens um plano ativo.</p>
              <Link href="/plans">
                <Button className="btn-red tracking-[0.2em] uppercase text-xs px-8 py-4">
                  Ver Planos
                </Button>
              </Link>
            </div>
          ) : (
            <>
              <div className="mb-8 flex flex-wrap items-center justify-center gap-3">
                <Badge className="bg-primary/20 text-primary border border-primary/30 tracking-wider">
                  <Zap size={12} className="mr-1" />
                  Plano {userPlan.type}
                </Badge>
                <span className="text-sm text-text-secondary">
                  {userPlan.maxClassesPerWeek} aulas/semana
                </span>
              </div>

              {message && (
                <div
                  className={`mb-6 p-4 rounded flex items-center gap-3 ${
                    message.type === "success"
                      ? "bg-primary/10 text-primary"
                      : "bg-destructive/10 text-destructive"
                  }`}
                >
                  {message.type === "success" ? <Check size={18} /> : <X size={18} />}
                  <span className="text-sm">{message.text}</span>
                  <button
                    onClick={() => setMessage(null)}
                    className="ml-auto text-xs opacity-70 hover:opacity-100"
                  >
                    ✕
                  </button>
                </div>
              )}

              <div className="mb-8 flex flex-wrap gap-4 items-center justify-center">
                <div className="flex items-center gap-2">
                  <Filter size={16} className="text-text-secondary" />
                  <select
                    value={modalityFilter}
                    onChange={(e) => setModalityFilter(e.target.value as Modality | "Todos")}
                    className="bg-surface border border-border-subtle text-foreground rounded px-3 py-2 text-sm"
                  >
                    {modalities.map((m) => (
                      <option key={m} value={m}>
                        {m}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="flex items-center gap-2">
                  <Calendar size={16} className="text-text-secondary" />
                  <select
                    value={dayFilter}
                    onChange={(e) => setDayFilter(Number(e.target.value) as DayOfWeek | "Todos")}
                    className="bg-surface border border-border-subtle text-foreground rounded px-3 py-2 text-sm"
                  >
                    <option value="Todos">Todos os dias</option>
                    {Object.entries(dayNames).map(([key, name]) => (
                      <option key={key} value={key}>
                        {name}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="flex items-center gap-2">
                  <Clock size={16} className="text-text-secondary" />
                  <select
                    value={timeFilter}
                    onChange={(e) => setTimeFilter(e.target.value)}
                    className="bg-surface border border-border-subtle text-foreground rounded px-3 py-2 text-sm"
                  >
                    <option value="Todos">Todas as horas</option>
                    {timeSlots.map((t) => (
                      <option key={t} value={t}>
                        {t}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {loading ? (
                <div className="flex items-center justify-center py-20">
                  <Loader2 size={40} className="animate-spin" style={{ color: "#C1121F" }} />
                </div>
              ) : filteredClasses.length === 0 ? (
                <div className="text-center py-20">
                  <p className="text-text-secondary mb-4">
                    Nenhuma aula encontrada com os filtros selecionados.
                  </p>
                </div>
              ) : (
                <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                  {Object.entries(
                    filteredClasses.reduce(
                      (acc, cls) => {
                        const key = cls.dayOfWeek;
                        if (!acc[key]) acc[key] = [];
                        acc[key].push(cls);
                        return acc;
                      },
                      {} as Record<number, ScheduleClass[]>,
                    ),
                  ).map(([dayIndex, dayClasses]) => (
                    <Card
                      key={dayIndex}
                      className="bg-surface border-border-subtle"
                      style={{
                        borderTop: `3px solid ${Number(dayIndex) === today ? "#C1121F" : "#1E1E1E"}`,
                      }}
                    >
                      <CardHeader className="pb-2">
                        <CardTitle className="font-display text-xl tracking-wider flex items-center gap-2">
                          {dayNames[Number(dayIndex) as DayOfWeek]}
                          {Number(dayIndex) === today && (
                            <span className="text-[10px] bg-primary text-white px-2 py-0.5 rounded">
                              HOJE
                            </span>
                          )}
                        </CardTitle>
                      </CardHeader>
                      <CardContent>
                        <ul className="space-y-3">
                          {dayClasses.map((cls) => {
                            const enrolled = isEnrolled(cls.id);
                            const accessible = canAccessClass(userPlan, cls.requiredPlan);
                            const spots = getAvailableSpots(cls);
                            const selected = selectedClassId === cls.id;

                            return (
                              <li
                                key={cls.id}
                                onClick={() => {
                                  if (accessible && !enrolled) {
                                    setSelectedClassId(selected ? null : cls.id);
                                  }
                                }}
                                className={`p-3 rounded border transition-all ${
                                  enrolled
                                    ? "border-primary/50 bg-primary/5"
                                    : !accessible
                                      ? "border-border-subtle bg-surface/50 opacity-60 cursor-not-allowed"
                                      : selected
                                        ? "border-primary shadow-[0_0_12px_rgba(193,18,31,0.4)] cursor-pointer"
                                        : "border-border-subtle hover:border-primary/30 cursor-pointer"
                                }`}
                              >
                                <div className="flex justify-between items-start gap-2">
                                  <div className="flex-1">
                                    <div className="flex items-center gap-2 mb-1">
                                      <span
                                        className="inline-block w-2 h-2 rounded-full"
                                        style={{
                                          backgroundColor:
                                            cls.modality === "Jiu-Jitsu"
                                              ? "#3B82F6"
                                              : cls.modality === "Boxe / Kickboxing"
                                                ? "#F59E0B"
                                                : "#10B981",
                                        }}
                                      />
                                      <span className="text-xs tracking-wider uppercase text-text-secondary">
                                        {cls.modality}
                                      </span>
                                    </div>
                                    <span className="block text-base font-semibold">
                                      {cls.time}
                                    </span>
                                    <span className="block text-xs text-text-secondary mt-1">
                                      {cls.level} {cls.trainerName && `· ${cls.trainerName}`}
                                    </span>
                                    {spots <= 5 && spots > 0 && (
                                      <span className="block text-[10px] text-orange-400 mt-1">
                                        {spots} vagas restantes
                                      </span>
                                    )}
                                    {spots === 0 && (
                                      <span className="block text-[10px] text-destructive mt-1">
                                        Lotado
                                      </span>
                                    )}
                                  </div>
                                  <div className="flex flex-col items-end gap-1">
                                    {!accessible && !enrolled && (
                                      <span className="text-[10px] text-text-muted text-right">
                                        {getLockedMessage(cls.requiredPlan)}
                                      </span>
                                    )}
                                    {enrolled ? (
                                      <Badge className="bg-primary/20 text-primary border-primary/30 text-[10px]">
                                        <Check size={10} className="mr-1" />
                                        Inscrito
                                      </Badge>
                                    ) : selected ? (
                                      <Button
                                        size="sm"
                                        onClick={(e) => {
                                          e.stopPropagation();
                                          handleEnroll(cls.id);
                                        }}
                                        disabled={enrolling === cls.id || enrollMutation.isPending}
                                        className="text-xs tracking-wider uppercase min-w-[70px]"
                                      >
                                        {enrolling === cls.id ? (
                                          <Loader2 size={12} className="animate-spin" />
                                        ) : (
                                          "Confirmar"
                                        )}
                                      </Button>
                                    ) : null}
                                  </div>
                                </div>
                              </li>
                            );
                          })}
                        </ul>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              )}
            </>
          )}

          <Card className="mt-12 bg-surface border-border-subtle">
            <CardHeader>
              <CardTitle className="text-lg tracking-wider">INFORMAÇÕES IMPORTANTES</CardTitle>
            </CardHeader>
            <CardContent className="grid md:grid-cols-3 gap-6">
              <div className="flex items-start gap-3">
                <Clock size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="font-semibold">Duração</p>
                  <p className="text-sm text-text-secondary">Aulas de 60-90 minutos</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <Calendar size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="font-semibold">Feriados</p>
                  <p className="text-sm text-text-secondary">A academia fecha em feriados</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <MapPin size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="font-semibold">Local</p>
                  <p className="text-sm text-text-secondary">Gondomar, Portugal</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </main>
  );
}
