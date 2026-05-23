import { useState } from "react";
import { createFileRoute, useNavigate, Link } from "@tanstack/react-router";
import { useAuth } from "@/contexts/auth-context";
import { api, setTokens } from "@/lib/api";
import type { PreferredContactMethod, PreferredModality, PreferredTrainingDay, PreferredTrainingTime } from "@/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardTitle } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";

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
    age: "",
    parishOrArea: "",
    password: "",
    confirmPassword: "",
    hasMartialArtsExperience: "",
    martialArtsExperienceDetails: "",
    trainingGoal: "",
    preferredModality: "" as "" | PreferredModality,
    preferredModalityOther: "",
    preferredTrainingTime: "" as "" | PreferredTrainingTime,
    preferredTrainingTimeOther: "",
    preferredTrainingDays: [] as PreferredTrainingDay[],
    valuesMartialArtsPhilosophy: "",
    preferredContactMethod: "" as "" | PreferredContactMethod,
    preferredContactMethodOther: "",
  });
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleToggleDay = (day: PreferredTrainingDay) => {
    setFormData((prev) => ({
      ...prev,
      preferredTrainingDays: prev.preferredTrainingDays.includes(day)
        ? prev.preferredTrainingDays.filter((d) => d !== day)
        : [...prev.preferredTrainingDays, day],
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (formData.password !== formData.confirmPassword) {
      setError("As palavras-passe não coincidem");
      return;
    }

    if (formData.password.length < 8) {
      setError("A palavra-passe deve ter pelo menos 8 caracteres");
      return;
    }

    if (formData.preferredTrainingDays.length === 0) {
      setError("Seleciona pelo menos um dia da semana");
      return;
    }

    setLoading(true);

    try {
      await authRegister({
        name: formData.name,
        email: formData.email,
        password: formData.password,
        phone: formData.phone,
        age: Number(formData.age),
        parishOrArea: formData.parishOrArea,
        hasMartialArtsExperience: formData.hasMartialArtsExperience === "SIM",
        martialArtsExperienceDetails: formData.martialArtsExperienceDetails || undefined,
        trainingGoal: formData.trainingGoal,
        preferredModality: formData.preferredModality,
        preferredModalityOther: formData.preferredModalityOther || undefined,
        preferredTrainingTime: formData.preferredTrainingTime,
        preferredTrainingTimeOther: formData.preferredTrainingTimeOther || undefined,
        preferredTrainingDays: formData.preferredTrainingDays,
        valuesMartialArtsPhilosophy: formData.valuesMartialArtsPhilosophy === "SIM",
        preferredContactMethod: formData.preferredContactMethod,
        preferredContactMethodOther: formData.preferredContactMethodOther || undefined,
      });

      const response = await api.auth.login(formData.email, formData.password);
      if (!response.accessToken) {
        throw new Error("A conta foi criada, mas o login requer validacao adicional.");
      }

      setTokens(response.accessToken);

      setSuccess("Conta criada com sucesso. A redirecionar...");
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

        <Card
          className="bg-surface border-border-subtle"
          style={{ borderTop: "2px solid #C1121F" }}
        >
          <div className="text-center pb-2 pt-6">
            <CardTitle className="font-display text-2xl tracking-wider">CRIAR CONTA</CardTitle>
            <CardDescription className="text-text-secondary mt-2">Pré-inscrição e criação de conta</CardDescription>
          </div>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <div className="bg-destructive/10 border border-destructive/30 text-destructive px-4 py-3 text-sm">
                  {error}
                </div>
              )}
              {success && <div className="bg-green-500/10 border border-green-500/30 text-green-400 px-4 py-3 text-sm">{success}</div>}

              <div className="text-xs text-text-secondary tracking-[0.15em] uppercase">Passo {currentStep} de 3</div>

              {currentStep === 1 && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2"><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Nome</label><Input name="name" type="text" value={formData.name} onChange={handleChange} required className="bg-surface-2 border-border-subtle h-12" /></div>
                  <div className="space-y-2"><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">E-mail</label><Input name="email" type="email" value={formData.email} onChange={handleChange} required className="bg-surface-2 border-border-subtle h-12" /></div>
                  <div className="space-y-2"><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Telemóvel</label><Input name="phone" type="tel" value={formData.phone} onChange={handleChange} required className="bg-surface-2 border-border-subtle h-12" /></div>
                  <div className="space-y-2"><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Idade</label><Input name="age" type="number" min={4} max={100} value={formData.age} onChange={handleChange} required className="bg-surface-2 border-border-subtle h-12" /></div>
                  <div className="space-y-2 md:col-span-2"><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Morada / freguesia</label><Input name="parishOrArea" type="text" value={formData.parishOrArea} onChange={handleChange} required className="bg-surface-2 border-border-subtle h-12" /></div>
                  <div className="space-y-2">
                    <label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Palavra-passe</label>
                    <div className="relative"><Input name="password" type={showPassword ? "text" : "password"} value={formData.password} onChange={handleChange} required className="bg-surface-2 border-border-subtle h-12 pr-10" />
                    <button type="button" onClick={() => setShowPassword(!showPassword)} aria-label={showPassword ? "Hide password" : "Show password"} className="absolute right-3 top-1/2 -translate-y-1/2 text-text-secondary hover:text-foreground focus:outline-none">{showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}</button></div>
                  </div>
                  <div className="space-y-2">
                    <label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Confirmar Palavra-passe</label>
                    <div className="relative"><Input name="confirmPassword" type={showConfirmPassword ? "text" : "password"} value={formData.confirmPassword} onChange={handleChange} required className="bg-surface-2 border-border-subtle h-12 pr-10" />
                    <button type="button" onClick={() => setShowConfirmPassword(!showConfirmPassword)} aria-label={showConfirmPassword ? "Hide password" : "Show password"} className="absolute right-3 top-1/2 -translate-y-1/2 text-text-secondary hover:text-foreground focus:outline-none">{showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}</button></div>
                  </div>
                </div>
              )}

              {currentStep === 2 && (
                <div className="space-y-4">
                  <div><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Pratica ou já praticou alguma arte marcial?</label><select name="hasMartialArtsExperience" value={formData.hasMartialArtsExperience} onChange={handleChange} required className="mt-2 w-full h-12 bg-surface-2 border border-border-subtle px-3"><option value="">Selecionar</option><option value="SIM">Sim</option><option value="NAO">Não</option></select></div>
                  <div><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Se já praticou, descreva qual modalidade e quanto tempo</label><Textarea name="martialArtsExperienceDetails" value={formData.martialArtsExperienceDetails} onChange={handleChange} className="bg-surface-2 border-border-subtle mt-2" /></div>
                  <div><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Objetivo com a arte marcial</label><Textarea required name="trainingGoal" value={formData.trainingGoal} onChange={handleChange} className="bg-surface-2 border-border-subtle mt-2" /></div>
                  <div><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">A filosofia da arte marcial é importante?</label><select name="valuesMartialArtsPhilosophy" value={formData.valuesMartialArtsPhilosophy} onChange={handleChange} required className="mt-2 w-full h-12 bg-surface-2 border border-border-subtle px-3"><option value="">Selecionar</option><option value="SIM">Sim</option><option value="NAO">Não</option></select></div>
                </div>
              )}

              {currentStep === 3 && (
                <div className="space-y-4">
                  <div><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Modalidade de maior interesse</label><select name="preferredModality" value={formData.preferredModality} onChange={handleChange} required className="mt-2 w-full h-12 bg-surface-2 border border-border-subtle px-3"><option value="">Selecionar</option><option value="KICKBOXING">Kickboxing</option><option value="JIU_JITSU">Jiu Jitsu</option><option value="CAPOEIRA">Capoeira</option><option value="BOXE">Boxe</option><option value="MMA">M.M.A</option><option value="JIU_JITSU_KIDS">Jiu Jitsu Kids</option><option value="CAPOEIRA_KIDS">Capoeira Kids</option><option value="KICKBOXING_KIDS">Kickboxing Kids</option><option value="OTHER">Outro</option></select></div>
                  {formData.preferredModality === "OTHER" && <Input name="preferredModalityOther" value={formData.preferredModalityOther} onChange={handleChange} placeholder="Qual modalidade?" className="bg-surface-2 border-border-subtle h-12" />}
                  <div><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Horário pretendido</label><select name="preferredTrainingTime" value={formData.preferredTrainingTime} onChange={handleChange} required className="mt-2 w-full h-12 bg-surface-2 border border-border-subtle px-3"><option value="">Selecionar</option><option value="MORNING_BEFORE_0830">Manhã antes das 8h30</option><option value="LUNCH_1230">Almoço 12h30</option><option value="AFTERNOON_14_17">Tarde 14h a 17h</option><option value="NIGHT_AFTER_18">Noite depois das 18h</option><option value="OTHER">Outro</option></select></div>
                  {formData.preferredTrainingTime === "OTHER" && <Input name="preferredTrainingTimeOther" value={formData.preferredTrainingTimeOther} onChange={handleChange} placeholder="Qual horário?" className="bg-surface-2 border-border-subtle h-12" />}
                  <div><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Dias da semana pretendidos</label><div className="grid grid-cols-2 gap-2 mt-2 text-sm">{[["MONDAY","Segunda-feira"],["TUESDAY","Terça-feira"],["WEDNESDAY","Quarta-feira"],["THURSDAY","Quinta-feira"],["FRIDAY","Sexta-feira"]].map(([value,label]) => <label key={value} className="flex items-center gap-2"><input type="checkbox" checked={formData.preferredTrainingDays.includes(value as PreferredTrainingDay)} onChange={() => handleToggleDay(value as PreferredTrainingDay)} />{label}</label>)}</div></div>
                  <div><label className="text-xs tracking-[0.15em] uppercase text-text-secondary">Como prefere ser contactado?</label><select name="preferredContactMethod" value={formData.preferredContactMethod} onChange={handleChange} required className="mt-2 w-full h-12 bg-surface-2 border border-border-subtle px-3"><option value="">Selecionar</option><option value="CALL">Chamada</option><option value="MESSAGE">Mensagem</option><option value="OTHER">Outro</option></select></div>
                  {formData.preferredContactMethod === "OTHER" && <Input name="preferredContactMethodOther" value={formData.preferredContactMethodOther} onChange={handleChange} placeholder="Preferência de contacto" className="bg-surface-2 border-border-subtle h-12" />}
                </div>
              )}
              <div className="flex gap-2">
                <Button type="button" variant="outline" className="h-12 flex-1" disabled={currentStep === 1 || loading} onClick={() => setCurrentStep((s) => Math.max(1, s - 1))}>Anterior</Button>
                {currentStep < 3 ? (
                  <Button type="button" className="btn-red h-12 flex-1 tracking-[0.2em] uppercase text-xs font-semibold" onClick={() => setCurrentStep((s) => Math.min(3, s + 1))}>Seguinte</Button>
                ) : (
                  <Button type="submit" disabled={loading} className="btn-red h-12 flex-1 tracking-[0.2em] uppercase text-xs font-semibold">
                    {loading ? <span className="flex items-center justify-center gap-2"><Loader2 className="w-4 h-4 animate-spin" />Processando...</span> : "CRIAR CONTA"}
                  </Button>
                )}
              </div>
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
