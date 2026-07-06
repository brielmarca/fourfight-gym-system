import { createFileRoute, Link, redirect } from "@tanstack/react-router";
import { LogOut, ShieldCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/contexts/auth-context";
import { isAuthenticated } from "@/lib/api";

export const Route = createFileRoute("/trainer")({
  beforeLoad: () => {
    if (!isAuthenticated()) {
      throw redirect({ to: "/login", search: { redirect: "/trainer" } });
    }
  },
  component: TrainerPage,
});

function TrainerPage() {
  const navigate = Route.useNavigate();
  const { user, hasRole, logout, isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return null;
  }

  if (!isAuthenticated || !user) {
    void navigate({ to: "/login", search: { redirect: "/trainer" }, replace: true });
    return null;
  }

  if (hasRole(["ADMIN", "MANAGER"])) {
    void navigate({ to: "/admin", replace: true });
    return null;
  }

  if (hasRole(["PROFESSOR"])) {
    void navigate({ to: "/professor", replace: true });
    return null;
  }

  if (hasRole(["CLIENT"])) {
    void navigate({ to: "/student-area", replace: true });
    return null;
  }

  if (!hasRole(["TRAINER"])) {
    void navigate({ to: "/", replace: true });
    return null;
  }

  const handleLogout = () => {
    logout();
    void navigate({ to: "/", replace: true });
  };

  return (
    <div className="min-h-screen bg-background">
      <header className="bg-surface border-b border-border-subtle">
        <div className="max-w-4xl mx-auto px-4 py-3 flex items-center justify-between">
          <Link to="/" className="flex flex-col">
            <span className="font-display text-xl sm:text-2xl tracking-wider">4FOUR</span>
            <span className="font-sans text-[7px] sm:text-[8px] tracking-[0.3em] text-text-muted">
              FIGHT ACADEMY TRAINER
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

      <main className="max-w-4xl mx-auto px-4 py-8">
        <Card
          className="bg-surface border-border-subtle"
          style={{ borderTop: "2px solid #C1121F" }}
        >
          <CardHeader>
            <CardTitle className="font-display text-2xl sm:text-4xl tracking-wider">
              Painel do Treinador
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-text-secondary">
            <div className="flex items-start gap-3">
              <ShieldCheck className="mt-1 shrink-0" size={22} style={{ color: "#C1121F" }} />
              <div className="space-y-2">
                <p>
                  A tua conta de treinador esta ativa. As permissoes operacionais existem no backend
                  para presencas, perfis de alunos e pedidos de horario.
                </p>
                <p>
                  O painel operacional de treinador ainda nao tem uma interface dedicada nesta
                  versao. Contacta a equipa de gestao para executar acoes operacionais.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
