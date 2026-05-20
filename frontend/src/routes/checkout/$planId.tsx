import { useState } from "react";
import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { api, getUser, isAuthenticated } from "@/lib/api";
import { useCreateReceptionRequest, usePlan, useStripeCheckout } from "@/queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Label } from "@/components/ui/label";

import { CreditCard, Loader2, Building, ExternalLink } from "lucide-react";

export const Route = createFileRoute("/checkout/$planId")({
  beforeLoad: ({ params }) => {
    if (!isAuthenticated()) {
      throw redirect({ to: "/login", search: { redirect: `/checkout/${params.planId}` } });
    }
  },
  component: CheckoutPage,
});

function CheckoutPage() {
  const { planId } = Route.useParams();
  const navigate = useNavigate();
  const { data: plan, isLoading } = usePlan(planId);
  const stripeCheckout = useStripeCheckout();
  const receptionRequest = useCreateReceptionRequest();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<"STRIPE" | "RECECAO">("STRIPE");

  const displayPlan = plan || {
    id: planId,
    name: planId.includes("basic") ? "Basic" : planId.includes("premium") ? "Premium" : "Standard",
    price: planId.includes("basic") ? 29.99 : planId.includes("premium") ? 79.99 : 49.99,
    durationDays: 30,
    maxClasses: planId.includes("basic") ? 3 : -1,
    isActive: true,
    description: "Plano demo",
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccessMessage(null);

    const user = getUser();
    if (!user) {
      navigate({ to: "/login", search: { redirect: `/checkout/${planId}` }, replace: true });
      return;
    }

    if (paymentMethod === "STRIPE") {
      setSubmitting(true);
      try {
        const response = await stripeCheckout.mutateAsync(planId);
        if (response.checkoutUrl) {
          window.location.href = response.checkoutUrl;
        } else {
          setError("Nao foi possivel criar a sessao de pagamento.");
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : "Nao foi possivel ativar o plano. Tente novamente.");
      } finally {
        setSubmitting(false);
      }
      return;
    }

    if (paymentMethod === "RECECAO") {
      setSubmitting(true);
      try {
        const response = await receptionRequest.mutateAsync(planId);
        setSuccessMessage(response.message || "Pedido enviado. A sua adesao ficara pendente ate aprovacao na rececao.");
      } catch (err) {
        setError(err instanceof Error ? err.message : "Nao foi possivel enviar o pedido para aprovacao.");
      } finally {
        setSubmitting(false);
      }
      return;
    }
  };

  if (isLoading) {
     return (
       <div className="min-h-screen bg-background flex items-center justify-center">
         <div className="text-center space-y-4">
           <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin" />
           <p className="text-sm text-text-secondary">A carregar detalhes do plano...</p>
         </div>
       </div>
     );
   }

   if (error && !plan) {
     return (
       <div className="min-h-screen bg-background flex items-center justify-center px-4">
         <div className="text-center space-y-4">
           <p className="text-text-secondary">{error}</p>
         </div>
       </div>
     );
   }

   return (
     <div className="min-h-screen bg-background">
       <header className="bg-surface border-b border-border-subtle">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <a href="/" className="flex items-center">
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
          </a>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-16">
        <div className="text-center mb-12">
          <h1 className="font-display text-3xl sm:text-4xl md:text-5xl tracking-wider">Checkout</h1>
          <p className="mt-4 text-text-secondary">
            Complete a sua adesao para <span className="text-foreground font-semibold">{displayPlan.name}</span>
          </p>
        </div>

        <div className="grid md:grid-cols-5 gap-8">
          <div className="md:col-span-2">
            <Card className="bg-surface border-border-subtle md:sticky md:top-8">
              <CardHeader>
                <CardTitle className="text-lg">Resumo do Pedido</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex justify-between">
                    <span className="text-text-secondary">Plano</span>
                    <span className="font-semibold">{displayPlan.name}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-text-secondary">Duracao</span>
                    <span>{displayPlan.durationDays} dias</span>
                  </div>
                  <div className="border-t border-border-subtle pt-4 flex justify-between">
                    <span className="text-lg font-semibold">Total</span>
                    <span className="text-2xl font-display text-primary">€{displayPlan.price}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="md:col-span-3">
            <Card className="bg-surface border-border-subtle">
              <CardHeader>
                  <CardTitle>Pagamento</CardTitle>
                 <CardDescription>
                   Escolha o método de pagamento para concluir a adesão
                 </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-6">
                  {error && (
                    <div className="p-3 rounded-md bg-destructive/10 text-destructive text-sm">
                      {error}
                    </div>
                  )}

                  {successMessage && (
                    <div className="p-3 rounded-md bg-primary/10 text-primary text-sm border border-primary/20">
                      {successMessage}
                    </div>
                  )}

                  <RadioGroup
                     value={paymentMethod}
                     onValueChange={(value) => {
                       setError(null);
                        setSuccessMessage(null);
                        setPaymentMethod(value as "STRIPE" | "RECECAO");
                      }}
                      className="grid gap-4"
                    >
                     <Label
                       htmlFor="stripe"
                       className={`${
                         paymentMethod === "STRIPE"
                           ? "border-primary bg-primary/5"
                           : "border-border-subtle hover:border-primary/50"
                       } border-2 rounded-lg p-5 cursor-pointer transition-all`}
                     >
                       <div className="flex items-center gap-4">
                         <RadioGroupItem value="STRIPE" id="stripe" />
                         <CreditCard className="h-6 w-6 text-primary" />
                         <div>
                         <CardTitle className="text-lg">Cartão / MB WAY / SEPA</CardTitle>
                            <CardDescription>
                              Pagamento seguro via Stripe
                            </CardDescription>
                         </div>
                       </div>
                     </Label>

                     <Label
                        htmlFor="rececao"
                       className={`${
                         paymentMethod === "RECECAO"
                           ? "border-primary bg-primary/5"
                           : "border-border-subtle hover:border-primary/50"
                       } border-2 rounded-lg p-5 cursor-pointer transition-all`}
                     >
                       <div className="flex items-center gap-4">
                         <RadioGroupItem value="RECECAO" id="rececao" />
                         <Building className="h-6 w-6 text-primary" />
                         <div>
                         <CardTitle className="text-lg">Receção</CardTitle>
                            <CardDescription>
                              Presencialmente na academia
                            </CardDescription>
                         </div>
                       </div>
                     </Label>
                   </RadioGroup>

                  {paymentMethod === "STRIPE" && (
                    <div className="p-4 rounded-md bg-primary/5 border border-primary/20 flex gap-3 text-sm text-text-secondary">
                      <CreditCard className="h-5 w-5 flex-shrink-0 text-primary mt-0.5" />
                      <p>
                        Será redirecionado para o Stripe para concluir o pagamento de forma segura. Aceitamos cartões, MB WAY, SEPA, Apple Pay e Google Pay.
                      </p>
                    </div>
                  )}

                  {paymentMethod === "RECECAO" && (
                    <div className="p-4 rounded-md bg-primary/5 border border-primary/20 flex gap-3 text-sm text-text-secondary">
                      <Building className="h-5 w-5 flex-shrink-0 text-primary mt-0.5" />
                      <p>
                        Dirija-se à receção da 4Four Fight Academy para efetuar o pagamento presencialmente.
                      </p>
                    </div>
                  )}

                  <Button
                    type="submit"
                    disabled={submitting || stripeCheckout.isPending || receptionRequest.isPending}
                    className="w-full btn-red h-auto min-h-14 px-4 py-4 text-sm sm:text-base tracking-[0.08em] sm:tracking-[0.2em] uppercase font-semibold whitespace-normal leading-relaxed text-center"
                  >
                    {submitting || stripeCheckout.isPending || receptionRequest.isPending ? (
                      <span className="flex flex-wrap items-center justify-center gap-2 text-center">
                        <Loader2 className="h-4 w-4 animate-spin" />
                        A processar...
                      </span>
                    ) : paymentMethod === "STRIPE" ? (
                      <span className="flex items-center justify-center gap-2">
                        Pagar com Stripe <ExternalLink className="h-4 w-4" />
                      </span>
                    ) : (
                      "Enviar pedido para aprovacao"
                    )}
                  </Button>

                  <p className="text-xs text-text-secondary text-center">
                    Ao prosseguir, concorda com os nossos Termos de Servico e Politica de Privacidade.
                  </p>
                </form>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  );
}
