import { FormEvent, useEffect, useState } from "react";
import { createFileRoute, Link } from "@tanstack/react-router";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { api } from "@/lib/api";

export const Route = createFileRoute("/redefinir-senha")({
  component: ResetPasswordPage,
  validateSearch: (search: Record<string, unknown>) => ({ token: typeof search.token === "string" ? search.token : "" }),
});

function ResetPasswordPage() {
  const { token } = Route.useSearch();
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    setPassword("");
    setConfirmPassword("");
  }, []);

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError("");
    setMessage("");

    if (!token) {
      setError("Token em falta.");
      return;
    }
    if (password.length < 8) {
      setError("A palavra-passe deve ter pelo menos 8 caracteres.");
      return;
    }
    if (password !== confirmPassword) {
      setError("As palavras-passe nao coincidem.");
      return;
    }

    setLoading(true);
    try {
      await api.auth.resetPassword(token, password);
      setMessage("Palavra-passe redefinida com sucesso.");
      setPassword("");
      setConfirmPassword("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel redefinir a palavra-passe.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md bg-surface border-border-subtle">
        <CardHeader>
          <CardTitle>Redefinir Palavra-passe</CardTitle>
          <CardDescription>Define uma nova palavra-passe para a tua conta.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="space-y-4" autoComplete="off">
            <Input
              id="reset-password-new"
              name="reset-password-new"
              type="password"
              placeholder="Nova palavra-passe"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <Input
              id="reset-password-confirm"
              name="reset-password-confirm"
              type="password"
              placeholder="Confirmar palavra-passe"
              autoComplete="new-password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
            {message && <p className="text-sm text-green-600">{message}</p>}
            {error && <p className="text-sm text-destructive">{error}</p>}
            <Button type="submit" disabled={loading} className="w-full">
              {loading ? "A guardar..." : "Redefinir"}
            </Button>
          </form>
          <p className="mt-4 text-sm text-center">
            <Link to="/login" className="text-primary hover:underline">
              Ir para login
            </Link>
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
