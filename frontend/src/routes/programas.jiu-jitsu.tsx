import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useMemo } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useSchedule } from "@/queries";
import { useAuth } from "@/contexts/auth-context";
import { Shield, Target, Users, Heart } from "lucide-react";
import jiuJitsuHero from "@/assets/gymlutas/jiu-jitsu-1.webp";
import jiuJitsuAbout from "@/assets/gymlutas/jiu-jitsu-3.webp";

export const Route = createFileRoute("/programas/jiu-jitsu")({
  component: JiuJitsuPage,
});

const benefits = [
  {
    icon: Target,
    title: "Técnica e controlo",
    desc: "Aprende a usar alavancas, posicionamento e estratégia para controlar situações com eficiência.",
  },
  {
    icon: Shield,
    title: "Defesa pessoal",
    desc: "Desenvolve confiança e capacidade de reação através de técnicas aplicáveis em cenários reais.",
  },
  {
    icon: Heart,
    title: "Condicionamento físico",
    desc: "Melhora força, mobilidade, resistência e coordenação com treinos progressivos e intensos.",
  },
  {
    icon: Users,
    title: "Disciplina e confiança",
    desc: "Constrói foco, consistência e autocontrolo dentro e fora do tatame.",
  },
];

const belts = [
  { name: "Branca", order: "1", stripeClass: "bg-zinc-100 border-zinc-300", detailClass: "text-zinc-300" },
  { name: "Azul", order: "2", stripeClass: "bg-blue-500 border-blue-400", detailClass: "text-blue-300" },
  { name: "Roxa", order: "3", stripeClass: "bg-violet-500 border-violet-400", detailClass: "text-violet-300" },
  { name: "Castanha", order: "4", stripeClass: "bg-amber-700 border-amber-600", detailClass: "text-amber-400" },
  { name: "Preta", order: "5", stripeClass: "bg-zinc-900 border-red-700", detailClass: "text-red-400" },
];

const dayLabels: Record<string, string> = {
  MONDAY: "Segunda",
  TUESDAY: "Terça",
  WEDNESDAY: "Quarta",
  THURSDAY: "Quinta",
  FRIDAY: "Sexta",
  SATURDAY: "Sábado",
  SUNDAY: "Domingo",
};

function formatScheduleLevel(level: string) {
  const knownLevels: Record<string, string> = {
    ALL_LEVELS: "Todos os níveis",
    BEGINNER: "Iniciação",
    INTERMEDIATE: "Intermédio",
    ADVANCED: "Avançado",
    COMPETITION: "Competição",
  };

  if (knownLevels[level]) {
    return knownLevels[level];
  }

  return level
    .toLowerCase()
    .split("_")
    .map((word) => `${word.charAt(0).toUpperCase()}${word.slice(1)}`)
    .join(" ");
}

function JiuJitsuPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { data: schedule = [], isLoading: isScheduleLoading } = useSchedule();

  const jiuJitsuSchedule = useMemo(
    () =>
      schedule
        .filter((entry) => entry.modality === "JIU_JITSU")
        .sort((a, b) => a.dayOfWeek.localeCompare(b.dayOfWeek) || a.startTime.localeCompare(b.startTime))
        .slice(0, 6),
    [schedule],
  );

  const handleTrialCtaClick = () => {
    if (isAuthenticated) {
      void navigate({ to: "/plans" });
      return;
    }

    void navigate({ to: "/login" });
  };

  return (
    <main className="bg-background text-foreground min-h-screen">
      <nav className="sticky top-0 z-40 border-b border-border-subtle bg-background/95 backdrop-blur">
        <div className="mx-auto max-w-6xl px-4 py-3">
          <div className="flex flex-wrap items-center justify-center gap-2 sm:gap-3">
            <Button asChild variant="outline" size="sm" className="border-border-subtle">
              <Link to="/">Início</Link>
            </Button>
            <Button asChild variant="outline" size="sm" className="border-border-subtle">
              <Link to="/programs">Programas</Link>
            </Button>
            <Button asChild variant="outline" size="sm" className="border-border-subtle">
              <Link to="/schedule">Horários</Link>
            </Button>
            <Button asChild variant="outline" size="sm" className="border-border-subtle">
              <Link to="/plans">Planos</Link>
            </Button>
            <Button asChild className="btn-red" size="sm">
              <Link to="/login">Login</Link>
            </Button>
          </div>
        </div>
      </nav>

      <section className="relative min-h-[80vh] flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0">
          <img
            src={jiuJitsuHero}
            alt="Jiu-Jitsu treino na 4Four Fight Academy"
            loading="eager"
            className="absolute inset-0 w-full h-full object-cover"
          />
          <div
            className="absolute inset-0"
            style={{
              background: "linear-gradient(to bottom, rgba(0,0,0,0.55), rgba(11,11,11,0.78) 58%, #0B0B0B 100%)",
            }}
          />
        </div>

        <div className="relative px-4 text-center max-w-[980px] mx-auto pt-24" style={{ zIndex: 10 }}>
          <div className="flex items-center justify-center gap-4 mb-8">
            <span className="block w-12 h-px" style={{ background: "#C1121F" }} />
            <p className="text-[10px] tracking-[0.4em] uppercase" style={{ color: "#888" }}>
              4FOUR FIGHT ACADEMY
            </p>
            <span className="block w-12 h-px" style={{ background: "#C1121F" }} />
          </div>

          <h1
            className="font-display hero-word"
            style={{
              lineHeight: 0.9,
              fontSize: "clamp(56px, 12vw, 140px)",
              letterSpacing: "0.02em",
              color: "#F5F5F5",
            }}
          >
            A ARTE SUAVE
          </h1>

          <p className="mt-6 mx-auto hero-word" style={{ color: "#888", fontSize: "18px", maxWidth: "620px" }}>
            Domina a técnica, o controlo e a disciplina do Jiu-Jitsu Brasileiro num ambiente
            estruturado, seguro e focado na evolução real.
          </p>

          <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4 hero-word">
            <Button
              onClick={handleTrialCtaClick}
              className="btn-red w-full sm:w-auto px-10 py-5 text-[12px] tracking-[0.25em] uppercase font-semibold rounded-[2px]"
            >
              Agendar aula experimental
            </Button>
            <Button
              asChild
              variant="outline"
              className="w-full sm:w-auto px-10 py-5 text-[12px] tracking-[0.25em] uppercase rounded-[2px] border-border-subtle hover:border-red-500"
            >
              <Link to="/schedule">Ver horários</Link>
            </Button>
          </div>
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-12">
            <h2 className="font-display" style={{ fontSize: "clamp(36px, 6vw, 56px)", color: "#F5F5F5" }}>
              JIU-JITSU NA 4FOUR FIGHT ACADEMY
            </h2>
            <div className="mx-auto mt-6" style={{ width: "48px", height: "2px", background: "#C1121F" }} />
          </div>
          <div className="grid lg:grid-cols-2 gap-8 lg:gap-10 items-center">
            <div className="space-y-6 text-text-secondary leading-relaxed">
              <p className="text-lg">
                O Jiu-Jitsu é uma arte marcial baseada em técnica, alavancas, controlo e estratégia.
                Na 4Four Fight Academy, o treino é pensado para desenvolver confiança, disciplina,
                defesa pessoal e capacidade física, respeitando o nível de cada aluno.
              </p>
              <p>
                Cada aula combina fundamentos, progressão técnica e aplicação prática em treino
                estruturado. O foco está na evolução consistente, com segurança e acompanhamento.
              </p>
            </div>
            <div className="group rounded-lg overflow-hidden border border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_24px_rgba(193,18,31,0.2)]">
              <img
                src={jiuJitsuAbout}
                alt="Prática técnica de Jiu-Jitsu no tatame"
                className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-[1.01]"
                loading="lazy"
              />
            </div>
          </div>
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="font-display" style={{ fontSize: "clamp(36px, 6vw, 56px)", color: "#F5F5F5" }}>
              BENEFÍCIOS DO JIU-JITSU
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-3xl mx-auto">
              Mais do que uma arte marcial, o Jiu-Jitsu desenvolve técnica, confiança, disciplina e
              preparação física.
            </p>
            <div className="mx-auto mt-6" style={{ width: "48px", height: "2px", background: "#C1121F" }} />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6">
            {benefits.map((benefit, idx) => (
              <Card
                key={benefit.title}
                className="bg-surface border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_20px_rgba(193,18,31,0.18)]"
                style={{ transitionDelay: `${idx * 100}ms` }}
              >
                <CardContent className="pt-8">
                  <benefit.icon size={32} strokeWidth={1.5} style={{ color: "#C1121F", marginBottom: "20px" }} />
                  <h3 className="font-display text-2xl mb-4" style={{ color: "#F5F5F5", letterSpacing: "0.05em" }}>
                    {benefit.title}
                  </h3>
                  <p className="text-sm" style={{ color: "#888", lineHeight: 1.8 }}>
                    {benefit.desc}
                  </p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section className="px-4 py-20" style={{ background: "#0B0B0B", borderTop: "1px solid #1E1E1E" }}>
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="font-display" style={{ fontSize: "clamp(36px, 6vw, 56px)", color: "#F5F5F5" }}>
              PROGRESSÃO DE FAIXAS
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-2xl mx-auto">
              Evolução técnica por etapas, com critérios claros e treino consistente.
            </p>
            <div className="mx-auto mt-6" style={{ width: "48px", height: "2px", background: "#C1121F" }} />
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-4 max-w-4xl mx-auto">
            {belts.map((belt) => (
              <div key={belt.name} className="text-center py-4 px-2 rounded border border-border-subtle bg-surface">
                <div className="mb-3 h-2 w-full rounded-sm border border-border-subtle overflow-hidden" aria-hidden>
                  <span className={`block h-full w-full ${belt.stripeClass}`} />
                </div>
                <div className="font-display text-sm mb-2" style={{ color: "#F5F5F5", letterSpacing: "0.05em" }}>
                  Faixa {belt.name}
                </div>
                <div className="pt-2 border-t border-border-subtle">
                  <span className={`text-xs tracking-widest uppercase ${belt.detailClass}`}>
                    Etapa {belt.order} - progressão técnica
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-12">
            <h2 className="font-display" style={{ fontSize: "clamp(36px, 6vw, 56px)", color: "#F5F5F5" }}>
              HORÁRIOS
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-2xl mx-auto">
              Pré-visualização das próximas aulas de Jiu-Jitsu.
            </p>
            <div className="mx-auto mt-6" style={{ width: "48px", height: "2px", background: "#C1121F" }} />
          </div>

          <Card className="bg-surface border-border-subtle">
            <CardContent className="pt-6">
              {isScheduleLoading ? (
                <p className="text-sm text-text-secondary">A carregar horários...</p>
              ) : jiuJitsuSchedule.length === 0 ? (
                <p className="text-sm text-text-secondary">
                  Ainda não há aulas de Jiu-Jitsu publicadas. Consulta os horários completos para
                  atualizações.
                </p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border-subtle">
                        <th className="text-left py-3 px-4 text-text-secondary font-medium">Dia</th>
                        <th className="text-left py-3 px-4 text-text-secondary font-medium">Hora</th>
                        <th className="text-left py-3 px-4 text-text-secondary font-medium">Nível</th>
                      </tr>
                    </thead>
                    <tbody className="text-text-secondary">
                      {jiuJitsuSchedule.map((entry) => (
                        <tr
                          key={entry.id}
                          className="border-b border-border-subtle hover:bg-surface-2 transition-colors"
                        >
                          <td className="py-3 px-4">{dayLabels[entry.dayOfWeek] ?? entry.dayOfWeek}</td>
                          <td className="py-3 px-4">
                            {entry.startTime.slice(0, 5)} - {entry.endTime.slice(0, 5)}
                          </td>
                          <td className="py-3 px-4">{formatScheduleLevel(entry.level)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
              <Button asChild variant="outline" className="mt-5 w-full sm:w-auto">
                <Link to="/schedule">Ver horários completos</Link>
              </Button>
            </CardContent>
          </Card>
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="max-w-4xl mx-auto text-center">
          <Card className="bg-surface border-border-subtle" style={{ padding: "60px 40px" }}>
            <CardContent>
              <Target size={40} strokeWidth={1.5} style={{ color: "#C1121F", marginBottom: "24px" }} />
              <h2 className="font-display text-4xl mb-4" style={{ color: "#F5F5F5" }}>
                COMEÇA A TUA EVOLUÇÃO NO TATAME
              </h2>
              <p className="text-lg mb-8 mx-auto max-w-md" style={{ color: "#666", lineHeight: 1.8 }}>
                Agenda uma aula experimental e descobre como o Jiu-Jitsu pode melhorar a tua técnica,
                confiança e disciplina.
              </p>
              <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                <Button
                  onClick={handleTrialCtaClick}
                  className="btn-red w-full sm:w-auto px-10 py-4 text-[12px] tracking-[0.25em] uppercase font-semibold rounded-[2px]"
                >
                  Reservar aula experimental
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>
    </main>
  );
}
