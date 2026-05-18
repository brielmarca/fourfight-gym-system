import {
  createFileRoute,
  Outlet,
  useNavigate,
  useRouterState,
  redirect,
} from "@tanstack/react-router";
import { isAuthenticated } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Label } from "@/components/ui/label";

import { useState } from "react";
import { Loader2, Smartphone, CreditCard } from "lucide-react";

export const Route = createFileRoute("/membership/$membershipId")({
  beforeLoad: ({ params }) => {
    if (!isAuthenticated()) {
      throw redirect({ to: "/login", search: { redirect: `/membership/${params.membershipId}` } });
    }
  },
  component: MembershipMethodPage,
  head: () => ({
    meta: [
      { title: "Metodo de Pagamento — 4Four Fight Academy" },
      { name: "description", content: "Selecione o metodo de pagamento" },
    ],
  }),
});

function MembershipMethodPage() {
  const { membershipId } = Route.useParams();
  const navigate = useNavigate();
  const isPaymentFormRoute = useRouterState({
    select: (state) => state.location.pathname.endsWith(`/membership/${membershipId}/form`),
  });
  const [paymentMethod, setPaymentMethod] = useState("MBWAY");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await navigate({
        to: "/membership/$membershipId/form",
        params: { membershipId },
        search: { method: paymentMethod },
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Falha ao prosseguir");
    } finally {
      setLoading(false);
    }
  };

  if (isPaymentFormRoute) {
    return <Outlet />;
  }

  return (
    <div className="min-h-screen bg-background">
      <header className="bg-surface border-b border-border-subtle">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <a href="/" className="flex items-center">
            <img
              src="/logo.png"
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
          <h1 className="font-display text-4xl md:text-5xl tracking-wider">Metodo de Pagamento</h1>
          <p className="mt-4 text-text-secondary">Escolha o método de pagamento</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {error && (
            <div className="p-3 rounded-md bg-destructive/10 text-destructive text-sm">{error}</div>
          )}

          <RadioGroup value={paymentMethod} onValueChange={setPaymentMethod}>
            <div className="grid gap-4">
              <Label
                htmlFor="mbway"
                className={`${
                  paymentMethod === "MBWAY"
                    ? "border-primary bg-primary/5"
                    : "border-border-subtle hover:border-primary/50"
                } border-2 rounded-lg p-6 cursor-pointer transition-all`}
              >
                <div className="flex items-center gap-4">
                  <RadioGroupItem value="MBWAY" id="mbway" />
                  <div className="flex-1">
                    <div className="flex items-center gap-3">
                      <Smartphone className="h-6 w-6 text-primary" />
                      <div>
                        <CardTitle className="text-lg">MB WAY</CardTitle>
                        <CardDescription>
                          Disponível — pague instantaneamente com o telemóvel
                        </CardDescription>
                      </div>
                    </div>
                  </div>
                </div>
              </Label>

              <Label
                htmlFor="card"
                className={`${
                  paymentMethod === "CARD"
                    ? "border-primary bg-primary/5"
                    : "border-border-subtle hover:border-primary/50"
                } border-2 rounded-lg p-6 cursor-pointer transition-all`}
              >
                <div className="flex items-center gap-4">
                  <RadioGroupItem value="CARD" id="card" />
                  <div className="flex-1">
                    <div className="flex items-center gap-3">
                      <CreditCard className="h-6 w-6 text-primary" />
                      <div>
                        <CardTitle className="text-lg">Cartão de Débito</CardTitle>
                        <CardDescription>Indisponível de momento</CardDescription>
                      </div>
                    </div>
                  </div>
                </div>
              </Label>
            </div>
          </RadioGroup>

          <Button
            type="submit"
            disabled={loading}
            className="w-full btn-red py-6 text-base tracking-[0.2em] uppercase font-bold"
          >
            {loading ? (
              <span className="flex items-center justify-center gap-2">
                <Loader2 className="h-4 w-4 animate-spin" />A processar...
              </span>
            ) : (
              "Continuar para Pagamento →"
            )}
          </Button>
        </form>
      </main>
    </div>
  );
}
