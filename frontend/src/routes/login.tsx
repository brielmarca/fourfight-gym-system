import { useState } from "react";
import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useAuth } from "@/contexts/auth-context";
import { getUser, isAuthenticated } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

import { Loader2, Eye, EyeOff } from "lucide-react";

export const Route = createFileRoute("/login")({
  component: LoginPage,
});

function getSafeRedirect(search: unknown): string | null {
  const redirect = (search as { redirect?: unknown }).redirect;
  return typeof redirect === "string" && redirect.startsWith("/") && !redirect.startsWith("//")
    ? redirect
    : null;
}

function LoginPage() {
  const navigate = useNavigate();
  const search = Route.useSearch();
  const redirect = getSafeRedirect(search);
  const { login, isAuthenticated: isAuth } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  if (isAuth) {
    const user = getUser();
    if (redirect) {
      navigate({ to: redirect, replace: true });
      return null;
    }

    if (user?.role === "ADMIN" || user?.role === "MANAGER") {
      navigate({ to: "/admin", replace: true });
    } else {
      navigate({ to: "/student-area", replace: true });
    }
    return null;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      await login(email, password);

      const user = getUser();
      if (redirect) {
        navigate({ to: redirect, replace: true });
      } else if (user?.role === "ADMIN" || user?.role === "MANAGER") {
        navigate({ to: "/admin", replace: true });
      } else {
        navigate({ to: "/student-area", replace: true });
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : "Falha no login.";
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-6 sm:mb-8">
          <Link to="/" className="inline-flex items-center justify-center">
            <img
              src="/assets/logo.png"
              alt="4Four Fight Academy"
              style={{
                height: "60px",
                width: "auto",
                mixBlendMode: "screen",
                filter: "brightness(1.1)",
              }}
            />
          </Link>
        </div>

        <Card
          className="bg-surface border-border-subtle"
          style={{ borderTop: "2px solid #C1121F" }}
        >
          <CardHeader className="text-center pb-2">
            <CardTitle className="font-display text-2xl tracking-wider">
              BEM-VINDO DE VOLTA
            </CardTitle>
            <CardDescription className="text-text-secondary">
              Introduz as tuas credenciais para aceder à tua conta
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <div className="bg-destructive/10 border border-destructive/30 text-destructive px-4 py-3 text-sm">
                  {error}
                </div>
              )}

              <div className="space-y-2">
                <label className="text-xs tracking-[0.15em] uppercase text-text-secondary">
                  E-mail
                </label>
                <Input
                  type="email"
                  placeholder="your@email.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="bg-surface-2 border-border-subtle h-12"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs tracking-[0.15em] uppercase text-text-secondary">
                  Palavra-passe
                </label>
                <div className="relative">
                  <Input
                    type={showPassword ? "text" : "password"}
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    className="bg-surface-2 border-border-subtle h-12 pr-10"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    aria-label={showPassword ? "Hide password" : "Show password"}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-text-secondary hover:text-foreground focus:outline-none"
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
              </div>

              <Button
                type="submit"
                disabled={loading}
                className="btn-red w-full h-12 mt-6 tracking-[0.2em] uppercase text-xs font-semibold"
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Entrando...
                  </span>
                ) : (
                  "ENTRAR"
                )}
              </Button>
            </form>

            <div className="mt-6 space-y-3 text-center">
              <p className="text-xs text-text-secondary">
                Ainda não tem conta?{" "}
                <Link
                  to="/register"
                  search={redirect ? { redirect } : undefined}
                  className="text-primary hover:underline font-semibold"
                >
                  Criar Conta
                </Link>
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
