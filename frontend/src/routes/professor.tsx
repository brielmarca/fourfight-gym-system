import { createFileRoute, Link, redirect } from "@tanstack/react-router";
import { Loader2, LogOut } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import { isAuthenticated } from "@/lib/api";
import { useMyProfessorStudents } from "@/queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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

  if (user && hasRole(["ADMIN", "MANAGER"])) {
    void navigate({ to: "/admin", replace: true });
    return null;
  }

  if (user && !hasRole(["PROFESSOR"])) {
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

        <Card className="bg-surface border-border-subtle" style={{ borderTop: "2px solid #C1121F" }}>
          <CardHeader>
            <CardTitle className="text-xs tracking-[0.2em] uppercase">Alunos atribuídos</CardTitle>
          </CardHeader>
          <CardContent className="overflow-x-auto">
            {isLoading ? (
              <div className="py-10 text-center">
                <Loader2 size={24} className="animate-spin mx-auto mb-3" style={{ color: "#C1121F" }} />
                <p className="text-text-secondary">A carregar alunos atribuídos...</p>
              </div>
            ) : students.length === 0 ? (
              <p className="py-10 text-center text-text-secondary">Ainda não existem alunos atribuídos.</p>
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
                      <TableCell>{[student.planName, student.membershipStatus].filter(Boolean).join(" - ") || "-"}</TableCell>
                      <TableCell>{student.notes || "-"}</TableCell>
                      <TableCell>{new Date(student.assignedAt).toLocaleDateString("pt-PT")}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
