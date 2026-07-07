import { Link, createFileRoute, useSearch, useNavigate, redirect } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";

import { CheckCircle2, ArrowRight, User } from "lucide-react";
import { useEffect } from "react";
import { isAuthenticated } from "@/lib/api";

interface MembershipSuccessSearch {
  userId?: string;
}

export const Route = createFileRoute("/membership/success")({
  validateSearch: (search: Record<string, unknown>): MembershipSuccessSearch => ({
    userId: typeof search.userId === "string" ? search.userId : undefined,
  }),
  beforeLoad: () => {
    if (!isAuthenticated()) {
      throw redirect({ to: "/login" });
    }
  },
  component: MembershipSuccessPage,
});

function MembershipSuccessPage() {
  const { userId } = useSearch({ from: "/membership/success" });
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated()) {
      const timer = setTimeout(() => navigate({ to: "/student-area" }), 5000);
      return () => clearTimeout(timer);
    }
  }, [navigate]);

  return (
    <div className="min-h-screen bg-background">
      <header className="bg-surface border-b border-border-subtle">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <Link to="/" className="flex items-center">
            <img
              src="/assets/logo.png"
              alt="4Four Fight Academy"
              style={{
                height: "44px",
                width: "auto",
                mixBlendMode: "screen",
                filter: "brightness(1.1)",
              }}
            />
          </Link>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-16">
        <div className="text-center space-y-6">
          <div className="mx-auto w-20 h-20 flex items-center justify-center rounded-full bg-green-500/10">
            <CheckCircle2 className="h-10 w-10 text-green-500" />
          </div>

          <div>
            <h1 className="font-display text-3xl sm:text-4xl md:text-5xl lg:text-6xl tracking-wider text-green-500">
              Pagamento Efetuado!
            </h1>
            <p className="mt-4 text-text-secondary text-lg">
              Bem-vindo a 4Four Fight Academy! A sua adesao esta agora ativa.
            </p>
          </div>

          <Card className="bg-surface border-green-500/20 text-left">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="h-5 w-5 text-green-500" />
                Proximos Passos
              </CardTitle>
              <CardDescription>O que pode fazer agora:</CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="space-y-3">
                <li className="flex items-start gap-2">
                  <span className="text-green-500 mt-1">*</span>
                  <span className="text-text-secondary">
                    Aceda a sua <strong className="text-foreground">Area do Aluno</strong> para ver
                    os detalhes da adesao
                  </span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-green-500 mt-1">+</span>
                  <span className="text-text-secondary">
                    Marque uma <strong className="text-foreground">Aula Experimental</strong> para
                    comecar
                  </span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-green-500 mt-1">+</span>
                  <span className="text-text-secondary">
                    Faca download da nossa{" "}
                    <strong className="text-foreground">App da Comunidade</strong> para conteudo
                    exclusivo
                  </span>
                </li>
              </ul>
            </CardContent>
          </Card>

          <div className="flex flex-col sm:flex-row gap-4 justify-center pt-6">
            <Button asChild className="btn-red px-8 py-3 tracking-[0.2em] uppercase">
              <Link to="/student-area">
                Ir para Area do Aluno
                <ArrowRight className="h-4 w-4" />
              </Link>
            </Button>
            <Button
              asChild
              variant="outline"
              className="px-8 py-3 tracking-[0.2em] uppercase border-border-subtle"
            >
              <Link to="/programs">Explorar Programas</Link>
            </Button>
          </div>

          <p className="text-xs text-text-secondary pt-4">
            Sera redirecionado automaticamente para a area do aluno em alguns segundos...
          </p>
        </div>
      </main>
    </div>
  );
}
