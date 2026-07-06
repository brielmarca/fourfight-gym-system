import { createFileRoute, Link, redirect } from "@tanstack/react-router";
import { Loader2, LogOut } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import { isAuthenticated } from "@/lib/api";
import {
  useMyProfessorStudents,
  useManageVideoLessons,
  useCreateVideoLesson,
  useDeactivateVideoLesson,
} from "@/queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useState } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

const modalityLabels = {
  JIU_JITSU: "Jiu-Jitsu",
  BOXE_KICKBOXING: "Boxe/Kickboxing",
  CAPOEIRA: "Capoeira",
  MMA: "MMA",
} as const;

export const Route = createFileRoute("/professor")({
  beforeLoad: () => {
    if (!isAuthenticated()) {
      throw redirect({ to: "/login", search: { redirect: "/professor" } });
    }
  },
  component: ProfessorPage,
});

function ProfessorPage() {
  const navigate = Route.useNavigate();
  const { user, hasRole, logout } = useAuth();
  const { data: students = [], isLoading } = useMyProfessorStudents(hasRole(["PROFESSOR"]));
  const { data: lessons = [], isLoading: lessonsLoading } = useManageVideoLessons(
    hasRole(["PROFESSOR"]),
  );
  const createVideoLesson = useCreateVideoLesson();
  const deactivateVideoLesson = useDeactivateVideoLesson();
  const [feedback, setFeedback] = useState<string | null>(null);
  const [videoFile, setVideoFile] = useState<File | null>(null);
  const [form, setForm] = useState({
    title: "",
    description: "",
    modality: "JIU_JITSU" as const,
    minimumPlanRank: 1 as 1 | 2 | 3,
  });

  if (user && hasRole(["ADMIN", "MANAGER"])) {
    void navigate({ to: "/admin", replace: true });
    return null;
  }

  if (user && hasRole(["TRAINER"])) {
    void navigate({ to: "/trainer", replace: true });
    return null;
  }

  if (user && !hasRole(["PROFESSOR"])) {
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

  return (
    <div className="min-h-screen bg-background">
      <header className="bg-surface border-b border-border-subtle">
        <div className="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
          <Link to="/" className="flex flex-col">
            <span className="font-display text-xl sm:text-2xl tracking-wider">4FOUR</span>
            <span className="font-sans text-[7px] sm:text-[8px] tracking-[0.3em] text-text-muted">
              FIGHT ACADEMY PROFESSOR
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
          <h1 className="font-display text-2xl sm:text-4xl tracking-wider">Painel do Professor</h1>
          <p className="text-text-secondary mt-1 text-sm sm:text-base">Alunos atribuídos</p>
        </div>

        <Card
          className="bg-surface border-border-subtle"
          style={{ borderTop: "2px solid #C1121F" }}
        >
          <CardHeader>
            <CardTitle className="text-xs tracking-[0.2em] uppercase">Alunos atribuídos</CardTitle>
          </CardHeader>
          <CardContent className="overflow-x-auto">
            {isLoading ? (
              <div className="py-10 text-center">
                <Loader2
                  size={24}
                  className="animate-spin mx-auto mb-3"
                  style={{ color: "#C1121F" }}
                />
                <p className="text-text-secondary">A carregar alunos atribuídos...</p>
              </div>
            ) : students.length === 0 ? (
              <p className="py-10 text-center text-text-secondary">
                Ainda não existem alunos atribuídos.
              </p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Aluno</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Modalidade</TableHead>
                    <TableHead>Plano/estado</TableHead>
                    <TableHead>Notas</TableHead>
                    <TableHead>Atribuído em</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {students.map((student) => (
                    <TableRow key={`${student.studentId}-${student.assignedAt}`}>
                      <TableCell>{student.studentName}</TableCell>
                      <TableCell>{student.studentEmail}</TableCell>
                      <TableCell>{modalityLabels[student.modality] ?? student.modality}</TableCell>
                      <TableCell>
                        {[student.planName, student.membershipStatus].filter(Boolean).join(" - ") ||
                          "-"}
                      </TableCell>
                      <TableCell>{student.notes || "-"}</TableCell>
                      <TableCell>
                        {new Date(student.assignedAt).toLocaleDateString("pt-PT")}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>

        <div className="mt-6 grid gap-4">
          <Card
            className="bg-surface border-border-subtle"
            style={{ borderTop: "2px solid #C1121F" }}
          >
            <CardHeader>
              <CardTitle className="text-xs tracking-[0.2em] uppercase">Videoaulas</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="grid md:grid-cols-2 gap-3">
                <Input
                  placeholder="Titulo"
                  value={form.title}
                  onChange={(event) => setForm((prev) => ({ ...prev, title: event.target.value }))}
                />
                <Select
                  value={form.modality}
                  onValueChange={(value) =>
                    setForm((prev) => ({ ...prev, modality: value as typeof form.modality }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="JIU_JITSU">Jiu-Jitsu</SelectItem>
                    <SelectItem value="BOXE_KICKBOXING">Boxe/Kickboxing</SelectItem>
                    <SelectItem value="CAPOEIRA">Capoeira</SelectItem>
                    <SelectItem value="MMA">MMA</SelectItem>
                  </SelectContent>
                </Select>
                <Input
                  type="file"
                  accept="video/mp4,.mp4"
                  onChange={(event) => setVideoFile(event.target.files?.[0] ?? null)}
                  className="md:col-span-2"
                />
                <Select
                  value={String(form.minimumPlanRank)}
                  onValueChange={(value) =>
                    setForm((prev) => ({ ...prev, minimumPlanRank: Number(value) as 1 | 2 | 3 }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="1">Basic</SelectItem>
                    <SelectItem value="2">Standard</SelectItem>
                    <SelectItem value="3">Premium</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <Textarea
                placeholder="Descricao"
                value={form.description}
                onChange={(event) =>
                  setForm((prev) => ({ ...prev, description: event.target.value }))
                }
              />
              <p className="text-xs text-text-secondary">
                Apenas MP4. O ficheiro sera convertido para WebM e o MP4 original sera removido apos
                o processamento.
              </p>
              <Button
                onClick={async () => {
                  setFeedback(null);
                  try {
                    if (!videoFile) {
                      setFeedback("Selecione um ficheiro MP4 para upload.");
                      return;
                    }
                    await createVideoLesson.mutateAsync({ file: videoFile, payload: form });
                    setVideoFile(null);
                    setForm({
                      title: "",
                      description: "",
                      modality: "JIU_JITSU",
                      minimumPlanRank: 1,
                    });
                    setFeedback("Videoaula guardada com sucesso.");
                  } catch (error) {
                    setFeedback(
                      error instanceof Error ? error.message : "Erro ao guardar videoaula.",
                    );
                  }
                }}
                disabled={createVideoLesson.isPending}
              >
                {createVideoLesson.isPending ? "A guardar..." : "Guardar"}
              </Button>
              {feedback && <p className="text-sm text-text-secondary">{feedback}</p>}
            </CardContent>
          </Card>

          <Card
            className="bg-surface border-border-subtle"
            style={{ borderTop: "2px solid #C1121F" }}
          >
            <CardHeader>
              <CardTitle className="text-xs tracking-[0.2em] uppercase">
                Minhas videoaulas
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {lessonsLoading ? (
                <p className="text-text-secondary">A carregar videoaulas...</p>
              ) : lessons.length === 0 ? (
                <p className="text-text-secondary">Ainda nao tens videoaulas.</p>
              ) : (
                lessons.map((lesson) => (
                  <div
                    key={lesson.id}
                    className="flex items-center justify-between gap-3 border border-border-subtle rounded-md p-3"
                  >
                    <div>
                      <p className="font-semibold">{lesson.title}</p>
                      <p className="text-xs text-text-secondary">
                        {modalityLabels[lesson.modality]} - Plano minimo {lesson.minimumPlanRank} -
                        Estado {lesson.status ?? "READY"}
                      </p>
                    </div>
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
                  </div>
                ))
              )}
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  );
}
