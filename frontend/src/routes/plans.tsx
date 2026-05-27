import { useEffect, useState } from "react";
import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { motion } from "framer-motion";
import { isAuthenticated } from "@/lib/api";
import { useAuth } from "@/contexts/auth-context";
import { usePlans } from "@/queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Feedback, EmptyState } from "@/components/ui/feedback";
import { Skeleton } from "@/components/ui/skeleton";
import { whatsappAriaLabel, whatsappUrl } from "@/lib/contact";

import { AlertTriangle } from "lucide-react";

export const Route = createFileRoute("/plans")({
  component: PlansPage,
});

function PlansPage() {
  const { data: plans, isLoading, error, refetch } = usePlans();
  const { isAuthenticated: isUserAuthenticated, isLoading: isAuthLoading } = useAuth();
  const [selectedPlan, setSelectedPlan] = useState<string | null>(null);
  const [hoveredPlanId, setHoveredPlanId] = useState<string | null>(null);
  const navigate = useNavigate();

  const displayPlans = plans && plans.length > 0 ? plans : [];
  const popularPlanId =
    displayPlans.find((plan) => plan.popular)?.id ??
    displayPlans.find((plan) => plan.id === "standard")?.id ??
    displayPlans[1]?.id ??
    displayPlans[0]?.id ??
    "standard";

  useEffect(() => {
    if (selectedPlan || displayPlans.length === 0) return;
    if (displayPlans.length > 1) {
      setSelectedPlan(displayPlans[1].id);
      return;
    }
    setSelectedPlan(displayPlans[0].id);
  }, [displayPlans, selectedPlan]);

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
        <div className="max-w-6xl mx-auto px-4 py-10 sm:py-16">
          <div className="text-center mb-10 sm:mb-16">
            <Skeleton className="h-10 w-64 sm:w-80 mx-auto bg-surface" />
            <Skeleton className="h-4 w-72 sm:w-[32rem] mx-auto mt-4 bg-surface" />
          </div>
          <div className="grid md:grid-cols-3 gap-6">
            {Array.from({ length: 3 }).map((_, index) => (
              <Card key={index} className="bg-surface border-border-subtle">
                <CardHeader className="space-y-3">
                  <Skeleton className="h-8 w-32 mx-auto bg-surface-2" />
                  <Skeleton className="h-4 w-24 mx-auto bg-surface-2" />
                </CardHeader>
                <CardContent className="space-y-4">
                  <Skeleton className="h-10 w-24 mx-auto bg-surface-2" />
                  <Skeleton className="h-4 w-full bg-surface-2" />
                  <Skeleton className="h-4 w-5/6 bg-surface-2" />
                  <Skeleton className="h-4 w-2/3 bg-surface-2" />
                  <Skeleton className="h-10 w-full bg-surface-2" />
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <header className="bg-surface border-b border-border-subtle">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="h-10 w-10" aria-hidden="true" />
          <div>
            {isAuthLoading ? (
              <div className="h-9 w-28" aria-hidden="true" />
            ) : isUserAuthenticated ? (
              <Link
                to="/student-area"
                className="btn-red border border-primary bg-transparent text-primary px-4 py-2 text-xs tracking-wider uppercase font-semibold hover:text-primary-foreground"
              >
                Área do Aluno
              </Link>
            ) : (
              <Link
                to="/login"
                className="btn-red bg-primary text-primary-foreground px-4 py-2 text-xs tracking-wider uppercase font-semibold"
              >
                Login
              </Link>
            )}
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-10 sm:py-16">
        <motion.div
          className="text-center mb-10 sm:mb-16"
          initial={{ opacity: 0, y: 18 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, amount: 0.5 }}
          transition={{ duration: 0.45, ease: "easeOut" }}
        >
          <motion.h1
            className="font-display text-2xl sm:text-4xl md:text-5xl lg:text-6xl tracking-wider"
            initial={{ opacity: 0, y: 14 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, amount: 0.6 }}
            transition={{ duration: 0.45, ease: "easeOut" }}
          >
            Escolha Seu Plano
          </motion.h1>
          <motion.p
            className="mt-3 sm:mt-4 text-text-secondary text-base sm:text-lg max-w-2xl mx-auto px-2"
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true, amount: 0.6 }}
            transition={{ duration: 0.45, delay: 0.08, ease: "easeOut" }}
          >
            Comece hoje mesmo. Todos os planos incluem acesso academia, vestirios completos e app da
            comunidade.
          </motion.p>
          <motion.p
            className="mt-2 text-sm text-primary font-semibold px-2"
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true, amount: 0.6 }}
            transition={{ duration: 0.45, delay: 0.14, ease: "easeOut" }}
          >
            Plano Popular: Padrao melhor custo-beneficio
          </motion.p>
        </motion.div>

        {error && !plans ? (
          <div className="max-w-md mx-auto mb-8">
            <Feedback
              type="error"
              message="Erro ao carregar planos. Tente novamente em instantes."
              action={{ label: "Tentar novamente", onClick: () => refetch() }}
            />
          </div>
        ) : null}

        {displayPlans.length > 0 ? (
          <motion.div
            className="grid md:grid-cols-3 gap-6"
            onMouseLeave={() => setHoveredPlanId(null)}
            initial="hidden"
            whileInView="show"
            viewport={{ once: true, amount: 0.2 }}
            variants={{
              hidden: {},
              show: {
                transition: {
                  staggerChildren: 0.08,
                },
              },
            }}
          >
            {displayPlans.map((plan, i) => {
              const isActiveHighlight = (hoveredPlanId ?? popularPlanId) === plan.id;
              const isPopular = plan.id === popularPlanId;

              return (
                <motion.div
                  key={plan.id}
                  variants={{
                    hidden: { opacity: 0, y: 20 },
                   show: {
                     opacity: 1,
                     y: 0,
                    transition: { duration: 0.42, ease: "easeOut" },
                  },
                }}
                animate={i === 1 ? { boxShadow: ["0 0 30px rgba(193,18,31,0.1)", "0 0 42px rgba(193,18,31,0.16)", "0 0 30px rgba(193,18,31,0.1)"] } : undefined}
                transition={i === 1 ? { duration: 3.2, repeat: Infinity, ease: "easeInOut" } : undefined}
              >
                <Card
                  onMouseEnter={() => setHoveredPlanId(plan.id)}
                  className={`relative flex h-full flex-col bg-surface transition-all duration-300 ${
                    isActiveHighlight ? "border-primary" : "border-border-subtle"
                  }`}
                  style={{
                    borderTop: isActiveHighlight ? "2px solid #C1121F" : "2px solid #1E1E1E",
                  }}
                >
                  {isPopular && (
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
                  <p className="mt-2 text-[11px] text-text-muted text-center">
                    Seras redirecionado para login se ainda nao tiveres sessao iniciada.
                  </p>
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
              </motion.div>
              );
            })}
          </motion.div>
        ) : (
          <EmptyState
            icon={AlertTriangle}
            title="Nenhum plano disponível"
            description="Nenhum plano disponível no momento."
            action={
              <Button asChild variant="outline" className="mt-4">
                <a
                  href={whatsappUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  aria-label={whatsappAriaLabel}
                >
                  Contacte-nos para mais informações
                </a>
              </Button>
            }
          />
        )}

        <div id="faq" className="mt-14 sm:mt-20 max-w-2xl mx-auto px-2">
          <h2 className="font-display text-2xl sm:text-3xl tracking-wider text-center mb-8 sm:mb-10">
            PERGUNTAS FREQUENTES
          </h2>
          <div className="space-y-4 sm:space-y-6">
            <div
              className="bg-surface p-4 sm:p-6 border border-border-subtle"
              style={{ borderLeft: "3px solid #C1121F" }}
            >
              <h3 className="font-semibold mb-2 text-sm sm:text-base">
                Posso experimentar uma aula grátis?
              </h3>
               <p className="text-text-secondary text-sm">
                 Sim! Oferecemos uma aula experimental gratuita para novos alunos. Contacta-nos para
                 {" "}
                 <a
                   href={whatsappUrl}
                   target="_blank"
                   rel="noopener noreferrer"
                   aria-label={whatsappAriaLabel}
                   className="text-primary hover:text-primary/80"
                 >
                   agendares
                 </a>
                 .
               </p>
            </div>
            <div
              className="bg-surface p-4 sm:p-6 border border-border-subtle"
              style={{ borderLeft: "3px solid #C1121F" }}
            >
              <h3 className="font-semibold mb-2 text-sm sm:text-base">
                Como funciona o cancelamento?
              </h3>
              <p className="text-text-secondary text-sm">
                Podes cancelar a qualquer momento. Sem contratos, sem burocracia. O cancelamento
                entra em vigor no fim do período pago.
              </p>
            </div>
            <div
              className="bg-surface p-4 sm:p-6 border border-border-subtle"
              style={{ borderLeft: "3px solid #C1121F" }}
            >
              <h3 className="font-semibold mb-2 text-sm sm:text-base">
                O que está incluído no plano?
              </h3>
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
