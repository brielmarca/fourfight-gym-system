import { useState } from "react";
import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { isAuthenticated } from "@/lib/api";
import { usePlans } from "@/queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

import { Loader2 } from "lucide-react";

export const Route = createFileRoute("/plans")({
  component: PlansPage,
  head: () => ({
    meta: [
      { title: "Planos e Preços — 4Four Fight Academy" },
      {
        name: "description",
        content:
          "Escolha seu plano de treino na 4Four Fight Academy. Jiu-Jitsu, Boxing, Kickboxing e Musculação.",
      },
    ],
  }),
});

function PlansPage() {
  const { data: plans, isLoading, error } = usePlans();
  const [selectedPlan, setSelectedPlan] = useState<string | null>(null);
  const navigate = useNavigate();

  const displayPlans =
    plans && plans.length > 0
      ? plans
      : [
          {
            id: "demo-basico",
            name: "Basico",
            price: 29.99,
            durationDays: 30,
            maxClasses: 3,
            isActive: true,
            description: "Plano basica",
          },
          {
            id: "demo-padrao",
            name: "Padrao",
            price: 49.99,
            durationDays: 30,
            maxClasses: -1,
            isActive: true,
            description: "Plano padrao",
          },
          {
            id: "demo-premium",
            name: "Premium",
            price: 79.99,
            durationDays: 30,
            maxClasses: -1,
            isActive: true,
            description: "Plano premium",
          },
        ];

  if (!selectedPlan && displayPlans.length > 1) {
    setSelectedPlan(displayPlans[1].id);
  } else if (!selectedPlan && displayPlans.length > 0) {
    setSelectedPlan(displayPlans[0].id);
  }

  const handleSelectPlan = (planId: string) => {
    setSelectedPlan(planId);
    const redirect = `/checkout/${planId}`;

    if (!isAuthenticated()) {
      navigate({ to: "/login", search: { redirect } });
      return;
    }

    navigate({ to: "/checkout/$planId", params: { planId } });
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-background">
        <div className="flex flex-col items-center justify-center py-20 space-y-4">
          <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          <p className="text-sm text-text-secondary">Carregando planos...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <header className="bg-surface border-b border-border-subtle">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="h-11 w-11" aria-hidden="true" />
          <nav className="hidden md:flex items-center gap-6">
            <Link
              href="/"
              className="text-xs tracking-wider uppercase text-text-secondary hover:text-foreground"
            >
              Início
            </Link>
            <Link
              href="/#contact"
              className="text-xs tracking-wider uppercase text-text-secondary hover:text-foreground"
            >
              Contacto
            </Link>
            <Link
              href="/login"
              className="btn-red bg-primary text-primary-foreground px-4 py-2 text-xs tracking-wider uppercase font-semibold rounded-[2px]"
            >
              Login
            </Link>
          </nav>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-16">
        <div className="text-center mb-16">
          <h1 className="font-display text-5xl md:text-6xl tracking-wider">Escolha Seu Plano</h1>
          <p className="mt-4 text-text-secondary text-lg max-w-2xl mx-auto">
            Começe hoje mesmo. Todos os planos incluem acesso à academia, vestiários completos e app
            da comunidade.
          </p>
          <p className="mt-2 text-sm text-primary font-semibold">
            Plano Popular: Padrao — melhor custo-beneficio
          </p>
        </div>

        {error && !plans ? (
          <div className="text-center py-20 space-y-4">
            <p className="text-text-secondary">
              Erro ao carregar planos. A mostrar planos de demonstração.
            </p>
          </div>
        ) : null}

        {displayPlans.length > 0 ? (
          <div className="grid md:grid-cols-3 gap-6">
            {displayPlans.map((plan, i) => (
              <Card
                key={plan.id}
                className={`relative flex flex-col bg-surface border-border-subtle transition-all hover:scale-105 hover:border-red-500 hover:shadow-lg ${
                  i === 1 ? "border-primary" : ""
                }`}
                style={{
                  boxShadow: i === 1 ? "0 0 40px rgba(193,18,31,0.15)" : "none",
                }}
              >
                {i === 1 && (
                  <Badge className="absolute -top-3 left-1/2 -translate-x-1/2 bg-primary text-white text-[10px] tracking-[0.2em] uppercase">
                    Mais Popular
                  </Badge>
                )}
                <CardHeader className="text-center pb-2">
                  <CardTitle className="font-display text-2xl tracking-wider">
                    {plan.name}
                  </CardTitle>
                  <CardDescription className="text-text-secondary">
                    {plan.durationDays} dias
                  </CardDescription>
                </CardHeader>
                <CardContent className="flex-1 flex flex-col">
                  <div className="text-center mb-6">
                    <span
                      className="font-display text-5xl"
                      style={{ color: i === 1 ? "#C1121F" : "#F5F5F5" }}
                    >
                      €{plan.price}
                    </span>
                    <span className="text-text-secondary text-sm">
                      /{plan.durationDays === 30 ? "mês" : "meses"}
                    </span>
                  </div>

                  <ul className="flex-1 mb-6 space-y-3">
                    {plan.maxClasses && (
                      <li className="flex items-center gap-2 text-sm text-text-secondary">
                        <span className="text-primary">*</span>
                        {plan.maxClasses === -1
                          ? "Aulas ilimitadas"
                          : `${plan.maxClasses} aulas por semana`}
                      </li>
                    )}
                    <li className="flex items-center gap-2 text-sm text-text-secondary">
                      <span className="text-primary">+</span>
                      Acesso ao vestiário
                    </li>
                    <li className="flex items-center gap-2 text-sm text-text-secondary">
                      <span className="text-primary">+</span>
                      App da comunidade
                    </li>
                  </ul>

                  <Button
                    onClick={() => handleSelectPlan(plan.id)}
                    className={`w-full tracking-[0.2em] uppercase text-xs font-semibold ${
                      i === 1 ? "btn-red" : "btn-ghost border-border-subtle"
                    }`}
                  >
                    {selectedPlan === plan.id ? (
                      <span className="flex items-center justify-center gap-2">+ Selecionado</span>
                    ) : (
                      "Selecionar"
                    )}
                  </Button>
                  {i === 1 && (
                    <p className="mt-3 text-xs text-text-secondary text-center">
                      Inclui tudo do Basico + aulas coletivas e avaliacao mensal
                    </p>
                  )}
                  {i === 0 && (
                    <p className="mt-3 text-xs text-text-secondary text-center">
                      Ideal para iniciantes
                    </p>
                  )}
                  {i === 2 && (
                    <p className="mt-3 text-xs text-text-secondary text-center">
                      Acesso 24h + nutricionista incluso
                    </p>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          <div className="text-center py-20 space-y-4">
            <p className="text-text-secondary">Nenhum plano disponível no momento.</p>
            <Button asChild variant="outline" className="mt-4">
              <a href="/contact">Contacte-nos para mais informações</a>
            </Button>
          </div>
        )}

        <div id="faq" className="mt-20 max-w-2xl mx-auto">
          <h2 className="font-display text-3xl tracking-wider text-center mb-10">
            PERGUNTAS FREQUENTES
          </h2>
          <div className="space-y-6">
            <div className="bg-surface p-6 rounded-md border border-border-subtle">
              <h3 className="font-semibold mb-2">Posso experimentar uma aula grátis?</h3>
              <p className="text-text-secondary text-sm">
                Sim! Oferecemos uma aula experimental gratuita para novos alunos. Contacta-nos para
                agendares.
              </p>
            </div>
            <div className="bg-surface p-6 rounded-md border border-border-subtle">
              <h3 className="font-semibold mb-2">Como funciona o cancelamento?</h3>
              <p className="text-text-secondary text-sm">
                Podes cancelar a qualquer momento. Sem contratos, sem burocracia. O cancelamento
                entra em vigor no fim do período pago.
              </p>
            </div>
            <div className="bg-surface p-6 rounded-md border border-border-subtle">
              <h3 className="font-semibold mb-2">O que está incluído no plano?</h3>
              <p className="text-text-secondary text-sm">
                Todos os planos incluem acesso às aulas selecionadas, vestiários e app da
                comunidade. Planos superiores incluem benefícios adicionais.
              </p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
