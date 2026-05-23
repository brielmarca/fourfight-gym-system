import { Link, createFileRoute, useNavigate, useSearch, redirect } from "@tanstack/react-router";
import { api, waitForAuthRestore } from "@/lib/api";
import { useProcessPayment } from "@/queries";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";

import { useState } from "react";
import { Loader2, CheckCircle2, AlertCircle } from "lucide-react";

const PAYMENT_TIMEOUT_MS = 15000;

function withTimeout<T>(promise: Promise<T>, timeoutMs: number, message: string): Promise<T> {
  return new Promise((resolve, reject) => {
    const timeoutId = window.setTimeout(() => reject(new Error(message)), timeoutMs);

    promise
      .then(resolve)
      .catch(reject)
      .finally(() => window.clearTimeout(timeoutId));
  });
}

export const Route = createFileRoute("/membership/$membershipId/form")({
  beforeLoad: async ({ params }) => {
    // Wait for auth restore to complete
    const isAuth = await waitForAuthRestore();
    if (!isAuth) {
      throw redirect({
        to: "/login",
        search: { redirect: `/membership/${params.membershipId}/form` },
      });
    }
  },
  component: MembershipFormPage,
});

function MembershipFormPage() {
  const { membershipId } = Route.useParams();
  const search = useSearch({ from: "/membership/$membershipId/form" });
  const method = (search as any).method || "CARD";
  const navigate = useNavigate();
  const isDemoMode = membershipId.startsWith("demo-");
  const processPayment = useProcessPayment();

  const [phoneNumber, setPhoneNumber] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!isDemoMode) {
      if (method === "MBWAY" && !phoneNumber) {
        setError("Numero de telemovel obrigatorio para MB WAY");
        return;
      }

      if (method === "CARD") {
        setError(
          "Pagamento com cartão ainda indisponível. Por favor, use MB WAY ou dirija-se à receção.",
        );
        return;
      }
    }

    setLoading(true);

    try {
      if (isDemoMode) {
        await new Promise((resolve) => setTimeout(resolve, 1000));
        setSuccess(true);
        setTimeout(() => navigate({ to: "/membership/success" }), 2000);
        return;
      }

      const response = await withTimeout(
        processPayment.mutateAsync({
          membershipId,
          data: {
            ...(method === "MBWAY" ? { phoneNumber } : {}),
            paymentId: membershipId,
          },
        }),
        PAYMENT_TIMEOUT_MS,
        "O processamento do pagamento demorou demasiado. Verifique a configuracao do provedor de pagamentos e tente novamente.",
      );

      if (response.paymentStatus === "COMPLETED") {
        setSuccess(true);
        setTimeout(() => {
          navigate({ to: "/membership/success", search: { userId: response.userId } });
        }, 2000);
      } else {
        setError(response.message || "Pagamento falhou. Tente novamente.");
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Nao foi possivel processar o pagamento. Tente novamente.",
      );
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center px-4">
        <div className="text-center space-y-6">
          <div className="mx-auto w-16 h-16 flex items-center justify-center rounded-full bg-green-500/10">
            <CheckCircle2 className="h-8 w-8 text-green-500" />
          </div>
          <h1 className="font-display text-4xl tracking-tight">Pagamento Efetuado!</h1>
          <p className="text-text-secondary">
            {isDemoMode
              ? "Pagamento de teste concluido com sucesso!"
              : "A sua adesao esta agora ativa. A redirecionar..."}
          </p>
        </div>
      </div>
    );
  }

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
        {isDemoMode && (
          <div className="mb-6 p-4 bg-yellow-500/10 border border-yellow-500/30 rounded-md flex items-center gap-3">
            <AlertCircle className="h-5 w-5 text-yellow-500 flex-shrink-0" />
            <p className="text-sm text-yellow-500">
              <strong>Modo de teste</strong> – nenhum pagamento real sera processado
            </p>
          </div>
        )}

        <div className="text-center mb-12">
          <h1 className="font-display text-3xl sm:text-4xl md:text-5xl tracking-wider">
            Detalhes de Pagamento
          </h1>
          <p className="mt-4 text-text-secondary">
            {method === "MBWAY"
              ? "Insira o número de telemóvel para receber o pedido de pagamento"
              : "Pagamento com cartão indisponível de momento"}
          </p>
        </div>

        <Card className="bg-surface border-border-subtle">
          <CardHeader>
            <CardTitle className="text-2xl">
              {method === "MBWAY" ? "Pagamento MB WAY" : "Pagamento com Debit Card"}
            </CardTitle>
            <CardDescription>
              {method === "MBWAY"
                ? "Enviaremos um pedido de pagamento para o seu telemóvel"
                : "Indisponível de momento — use MB WAY ou dirija-se à receção"}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              {error && (
                <div className="p-3 rounded-md bg-destructive/10 text-destructive text-sm">
                  {error}
                </div>
              )}

              {method === "MBWAY" ? (
                <div className="space-y-2">
                  <Label htmlFor="phone">Número de Telemóvel</Label>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-text-secondary">
                      +351
                    </span>
                    <Input
                      id="phone"
                      type="tel"
                      required
                      value={phoneNumber}
                      onChange={(e) => setPhoneNumber(e.target.value)}
                      className="pl-16 bg-background border-border-subtle"
                      placeholder="912 345 678"
                    />
                  </div>
                  <p className="text-xs text-text-secondary">
                    Receberá uma notificação no telemóvel para confirmar o pagamento
                  </p>
                </div>
              ) : (
                <div className="p-4 rounded-md bg-yellow-500/10 border border-yellow-500/30 flex items-start gap-3">
                  <AlertCircle className="h-5 w-5 text-yellow-500 flex-shrink-0 mt-0.5" />
                  <p className="text-sm text-yellow-500">
                    O pagamento com cartão não está disponível neste momento. Pode pagar com MB WAY
                    ou dirigir-se à receção.
                  </p>
                </div>
              )}

              <Button
                type="submit"
                disabled={loading || processPayment.isPending}
                className="w-full btn-red py-6 text-base tracking-[0.2em] uppercase font-bold"
              >
                {loading || processPayment.isPending ? (
                  <span className="flex items-center justify-center gap-2">
                    <Loader2 className="h-4 w-4 animate-spin" />A processar pagamento...
                  </span>
                ) : (
                  `Pagar ${method === "MBWAY" ? "Agora" : "de forma segura"} →`
                )}
              </Button>

              <p className="text-xs text-text-secondary text-center">
                O seu pagamento e seguro e esta encriptado
              </p>
            </form>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
