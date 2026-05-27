import { FormEvent, useState } from "react";
import { createFileRoute, Link } from "@tanstack/react-router";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { api } from "@/lib/api";

export const Route = createFileRoute("/esqueci-senha")({
  component: ForgotPasswordPage,
});

function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setMessage("");
    try {
      await api.auth.forgotPassword(email.trim().toLowerCase());
      setMessage("Se o email existir, enviamos instrucoes para redefinir a palavra-passe.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel enviar o pedido.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md bg-surface border-border-subtle">
        <CardHeader>
          <CardTitle>Recuperar Palavra-passe</CardTitle>
          <CardDescription>Introduz o teu email para receber o link de recuperacao.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="space-y-4">
            <Input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            {message && <p className="text-sm text-green-600">{message}</p>}
            {error && <p className="text-sm text-destructive">{error}</p>}
            <Button type="submit" disabled={loading} className="w-full">
              {loading ? "A enviar..." : "Enviar link"}
            </Button>
          </form>
          <p className="mt-4 text-sm text-center">
            <Link to="/login" className="text-primary hover:underline">
              Voltar ao login
            </Link>
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
