import { useState } from "react";
import { createFileRoute, useNavigate, Link } from "@tanstack/react-router";
import { useAuth } from "@/contexts/auth-context";
import { api, setTokens } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardTitle } from "@/components/ui/card";

import { Loader2, Eye, EyeOff } from "lucide-react";

export const Route = createFileRoute("/register")({
  component: RegisterPage,
});

function getSafeRedirect(search: unknown): string | null {
  const redirect = (search as { redirect?: unknown }).redirect;
  return typeof redirect === "string" && redirect.startsWith("/") && !redirect.startsWith("//")
    ? redirect
    : null;
}

function RegisterPage() {
  const navigate = useNavigate();
  const search = Route.useSearch();
  const redirect = getSafeRedirect(search);
  const { register: authRegister } = useAuth();
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phone: "",
    dateOfBirth: "",
    password: "",
    confirmPassword: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    if (formData.password !== formData.confirmPassword) {
      setError("As palavras-passe não coincidem");
      return;
    }

    if (formData.password.length < 6) {
      setError("A palavra-passe deve ter pelo menos 6 caracteres");
      return;
    }

    setLoading(true);

    try {
      await authRegister({
        name: formData.name,
        email: formData.email,
        password: formData.password,
        phone: formData.phone,
      });

      const response = await api.auth.login(formData.email, formData.password);
      if (!response.refreshToken) {
        throw new Error("A conta foi criada, mas o login requer validação adicional.");
      }

      setTokens(response.accessToken, response.refreshToken);

      if (redirect) {
        navigate({ to: redirect, replace: true });
      } else {
        navigate({ to: "/plans", replace: true });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel criar a conta");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <div className="w-full max-w-2xl">
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

        <Card className="bg-surface border-border-subtle">
          <div className="text-center pb-2 pt-6">
            <CardTitle className="font-display text-2xl tracking-wider">CRIAR CONTA</CardTitle>
            <CardDescription className="text-text-secondary mt-2">
              Preencha os dados abaixo para se registrar
            </CardDescription>
          </div>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <div className="bg-destructive/10 border border-destructive/30 text-destructive px-4 py-2 text-sm rounded-md whitespace-pre-line">
                  {error}
                </div>
              )}

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-xs tracking-[0.15em] uppercase text-text-secondary">
                    Nome Completo
                  </label>
                  <Input
                    name="name"
                    type="text"
                    placeholder="Seu nome completo"
                    value={formData.name}
                    onChange={handleChange}
                    required
                    className="bg-surface-2 border-border-subtle h-12"
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-xs tracking-[0.15em] uppercase text-text-secondary">
                    E-mail
                  </label>
                  <Input
                    name="email"
                    type="email"
                    placeholder="your@email.com"
                    value={formData.email}
                    onChange={handleChange}
                    required
                    className="bg-surface-2 border-border-subtle h-12"
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-xs tracking-[0.15em] uppercase text-text-secondary">
                    Telefone
                  </label>
                  <Input
                    name="phone"
                    type="tel"
                    placeholder="+351 900 000 000"
                    value={formData.phone}
                    onChange={handleChange}
                    required
                    className="bg-surface-2 border-border-subtle h-12"
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-xs tracking-[0.15em] uppercase text-text-secondary">
                    Data de Nascimento
                  </label>
                  <Input
                    name="dateOfBirth"
                    type="date"
                    value={formData.dateOfBirth}
                    onChange={handleChange}
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
                      name="password"
                      type={showPassword ? "text" : "password"}
                      placeholder="••••••••"
                      value={formData.password}
                      onChange={handleChange}
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

                <div className="space-y-2">
                  <label className="text-xs tracking-[0.15em] uppercase text-text-secondary">
                    Confirmar Palavra-passe
                  </label>
                  <div className="relative">
                    <Input
                      name="confirmPassword"
                      type={showConfirmPassword ? "text" : "password"}
                      placeholder="••••••••"
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      required
                      className="bg-surface-2 border-border-subtle h-12 pr-10"
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      aria-label={showConfirmPassword ? "Hide password" : "Show password"}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-text-secondary hover:text-foreground focus:outline-none"
                    >
                      {showConfirmPassword ? (
                        <EyeOff className="w-5 h-5" />
                      ) : (
                        <Eye className="w-5 h-5" />
                      )}
                    </button>
                  </div>
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
                    Processando...
                  </span>
                ) : (
                  "CRIAR CONTA"
                )}
              </Button>
            </form>

            <div className="mt-6 space-y-3 text-center">
              <p className="text-xs text-text-secondary">
                Já tem conta?{" "}
                <Link
                  to="/login"
                  search={redirect ? { redirect } : undefined}
                  className="text-primary hover:underline font-semibold"
                >
                  Fazer Login
                </Link>
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
