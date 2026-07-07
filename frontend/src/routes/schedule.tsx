import { useMemo, useState } from "react";
import { createFileRoute, Link } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Feedback, EmptyState } from "@/components/ui/feedback";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useSchedule } from "@/queries";
import type {
  PublicScheduleEntry,
  ScheduleDayOfWeek,
  ScheduleLevel,
  ScheduleModality,
} from "@/types";
import { CalendarOff } from "lucide-react";

export const Route = createFileRoute("/schedule")({ component: SchedulePage });

const DAY_LABELS: Record<ScheduleDayOfWeek, string> = {
  MONDAY: "Segunda",
  TUESDAY: "Terça",
  WEDNESDAY: "Quarta",
  THURSDAY: "Quinta",
  FRIDAY: "Sexta",
  SATURDAY: "Sábado",
  SUNDAY: "Domingo",
};
const LEVEL_LABELS: Record<ScheduleLevel, string> = {
  BEGINNER: "Iniciante",
  INTERMEDIATE: "Intermédio",
  ADVANCED: "Avançado",
  ALL_LEVELS: "Todos os níveis",
};
const MODALITY_LABELS: Record<ScheduleModality, string> = {
  JIU_JITSU: "Jiu-Jitsu",
  BOXE_KICKBOXING: "Boxe / Kickboxing",
  CAPOEIRA: "Capoeira",
  MMA: "MMA",
};

function SchedulePage() {
  const { data = [], isLoading, error, refetch } = useSchedule();

  const [dayFilter, setDayFilter] = useState<"ALL" | ScheduleDayOfWeek>("ALL");
  const [modalityFilter, setModalityFilter] = useState<"ALL" | ScheduleModality>("ALL");
  const [levelFilter, setLevelFilter] = useState<"ALL" | ScheduleLevel>("ALL");

  const filtered = useMemo(
    () =>
      data
        .filter((item) => dayFilter === "ALL" || item.dayOfWeek === dayFilter)
        .filter((item) => modalityFilter === "ALL" || item.modality === modalityFilter)
        .filter((item) => levelFilter === "ALL" || item.level === levelFilter)
        .sort(
          (a, b) =>
            a.dayOfWeek.localeCompare(b.dayOfWeek) || a.startTime.localeCompare(b.startTime),
        ),
    [data, dayFilter, modalityFilter, levelFilter],
  );

  const grouped = useMemo(() => {
    const map = new Map<ScheduleDayOfWeek, PublicScheduleEntry[]>();
    filtered.forEach((entry) => {
      const list = map.get(entry.dayOfWeek) ?? [];
      list.push(entry);
      map.set(entry.dayOfWeek, list);
    });
    return map;
  }, [filtered]);

  return (
    <main className="bg-background text-foreground min-h-screen">
      <div className="pt-32 pb-20 px-4 max-w-6xl mx-auto">
        <div className="text-center mb-10">
          <h1 className="font-display text-4xl tracking-wider">HORÁRIO DAS AULAS</h1>
          <p className="mt-3 text-text-secondary">Horários atualizados em tempo real para todos.</p>
        </div>

        <div className="grid md:grid-cols-3 gap-3 mb-6">
          <Select
            value={dayFilter}
            onValueChange={(v) => setDayFilter(v as "ALL" | ScheduleDayOfWeek)}
          >
            <SelectTrigger className="bg-surface border-border-subtle">
              <SelectValue placeholder="Dia" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">Todos os dias</SelectItem>
              {Object.entries(DAY_LABELS).map(([k, v]) => (
                <SelectItem key={k} value={k}>
                  {v}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select
            value={modalityFilter}
            onValueChange={(v) => setModalityFilter(v as "ALL" | ScheduleModality)}
          >
            <SelectTrigger className="bg-surface border-border-subtle">
              <SelectValue placeholder="Modalidade" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">Todas as modalidades</SelectItem>
              {Object.entries(MODALITY_LABELS).map(([k, v]) => (
                <SelectItem key={k} value={k}>
                  {v}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select
            value={levelFilter}
            onValueChange={(v) => setLevelFilter(v as "ALL" | ScheduleLevel)}
          >
            <SelectTrigger className="bg-surface border-border-subtle">
              <SelectValue placeholder="Nível" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">Todos os níveis</SelectItem>
              {Object.entries(LEVEL_LABELS).map(([k, v]) => (
                <SelectItem key={k} value={k}>
                  {v}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {error ? (
          <Feedback
            type="error"
            message="Erro ao carregar horários."
            action={{ label: "Tentar novamente", onClick: () => refetch() }}
          />
        ) : isLoading ? (
          <div className="space-y-3">
            {Array.from({ length: 6 }).map((_, i) => (
              <Skeleton key={i} className="h-16 bg-surface-2" />
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={CalendarOff}
            title="Sem horários"
            description="Ainda não há aulas para os filtros selecionados."
          />
        ) : (
          <>
            <div className="hidden md:block">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Dia</TableHead>
                    <TableHead>Hora</TableHead>
                    <TableHead>Aula</TableHead>
                    <TableHead>Instrutor</TableHead>
                    <TableHead>Nível</TableHead>
                    <TableHead>Sala</TableHead>
                    <TableHead>Notas</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filtered.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>{DAY_LABELS[item.dayOfWeek]}</TableCell>
                      <TableCell>
                        {item.startTime.slice(0, 5)} - {item.endTime.slice(0, 5)}
                      </TableCell>
                      <TableCell>{MODALITY_LABELS[item.modality]}</TableCell>
                      <TableCell>{item.instructorName}</TableCell>
                      <TableCell>{LEVEL_LABELS[item.level]}</TableCell>
                      <TableCell>{item.location || "-"}</TableCell>
                      <TableCell>{item.notes || "-"}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
            <div className="md:hidden space-y-3">
              {Array.from(grouped.entries()).map(([day, entries]) => (
                <Card key={day} className="bg-surface border-border-subtle">
                  <CardHeader>
                    <CardTitle className="text-sm uppercase tracking-wider">
                      {DAY_LABELS[day]}
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    {entries.map((item) => (
                      <div
                        key={item.id}
                        className="border border-border-subtle bg-surface-2 p-3 text-sm"
                      >
                        <p className="font-semibold">{MODALITY_LABELS[item.modality]}</p>
                        <p>
                          {item.startTime.slice(0, 5)} - {item.endTime.slice(0, 5)}
                        </p>
                        <p>
                          {item.instructorName} · {LEVEL_LABELS[item.level]}
                        </p>
                        {item.location && <p>{item.location}</p>}
                        {item.notes && <p className="text-text-secondary">{item.notes}</p>}
                      </div>
                    ))}
                  </CardContent>
                </Card>
              ))}
            </div>
          </>
        )}

        <p className="mt-8 text-center text-sm text-text-secondary">
          Queres treinar connosco?{" "}
          <Link to="/plans" className="text-primary hover:underline">
            Escolhe um plano.
          </Link>
        </p>
      </div>
    </main>
  );
}
