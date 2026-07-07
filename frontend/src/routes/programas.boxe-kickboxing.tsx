import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useMemo } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useSchedule } from "@/queries";
import { useAuth } from "@/contexts/auth-context";
import { Dumbbell, Flame, Shield, Target } from "lucide-react";

const boxeHero = "/assets/optimized/boxe/espaco-pv-2-boxe-kickboxing-hero.webp";
const boxeAbout = "/assets/optimized/boxe/espaco-pv-4-striking-area.webp";

export const Route = createFileRoute("/programas/boxe-kickboxing")({
  component: BoxeKickboxingPage,
});

const benefits = [
  {
    icon: Target,
    title: "Potência e precisão",
    desc: "Aprende a gerar força com técnica, melhorar o timing e executar golpes com mais controlo.",
  },
  {
    icon: Dumbbell,
    title: "Condicionamento físico",
    desc: "Desenvolve resistência, coordenação, mobilidade e capacidade cardiovascular com treinos progressivos.",
  },
  {
    icon: Shield,
    title: "Defesa e reação",
    desc: "Melhora reflexos, guarda, deslocamento e leitura de distância em situações de combate.",
  },
  {
    icon: Flame,
    title: "Disciplina e confiança",
    desc: "Constrói foco, consistência e autocontrolo dentro e fora do treino.",
  },
];

const progressionLevels = [
  { name: "Fundamentos", detail: "Base, guarda e deslocamento" },
  { name: "Técnica", detail: "Golpes com precisão e controlo" },
  { name: "Combinações", detail: "Sequências, timing e ritmo" },
  { name: "Sparring Controlado", detail: "Aplicação técnica com segurança" },
  { name: "Performance", detail: "Consistência e evolução contínua" },
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

function BoxeKickboxingPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { data: schedule = [], isLoading: isScheduleLoading } = useSchedule();

  const strikingSchedule = useMemo(
    () =>
      schedule
        .filter((entry) => entry.modality === "BOXE_KICKBOXING")
        .sort(
          (a, b) =>
            a.dayOfWeek.localeCompare(b.dayOfWeek) || a.startTime.localeCompare(b.startTime),
        )
        .slice(0, 6),
    [schedule],
  );

  const handleCtaClick = () => {
    if (isAuthenticated) {
      void navigate({ to: "/plans" });
      return;
    }

    void navigate({ to: "/login" });
  };

  return (
    <main className="bg-background text-foreground min-h-screen">
      <section className="relative min-h-[80vh] flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0">
          <img
            src={boxeHero}
            alt="Treino de Boxe e Kickboxing na 4Four Fight Academy"
            loading="eager"
            className="absolute inset-0 w-full h-full object-cover"
          />
          <div
            className="absolute inset-0"
            style={{
              background:
                "linear-gradient(to bottom, rgba(0,0,0,0.55), rgba(11,11,11,0.78) 58%, #0B0B0B 100%)",
            }}
          />
        </div>

        <div
          className="relative px-4 text-center max-w-[980px] mx-auto pt-24"
          style={{ zIndex: 10 }}
        >
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
              fontSize: "clamp(52px, 11vw, 128px)",
              letterSpacing: "0.02em",
              color: "#F5F5F5",
            }}
          >
            BOXE / KICKBOXING
          </h1>

          <p
            className="mt-6 mx-auto hero-word"
            style={{ color: "#888", fontSize: "18px", maxWidth: "620px" }}
          >
            Desenvolve potência, precisão, resistência e confiança através de um treino estruturado
            de striking.
          </p>

          <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4 hero-word">
            <Button
              onClick={handleCtaClick}
              className="btn-red w-full sm:w-auto px-10 py-5 text-[12px] tracking-[0.25em] uppercase font-semibold rounded-[2px]"
            >
              Começar treino
            </Button>
          </div>
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-12">
            <h2
              className="font-display"
              style={{ fontSize: "clamp(36px, 6vw, 56px)", color: "#F5F5F5" }}
            >
              BOXE E KICKBOXING NA 4FOUR FIGHT ACADEMY
            </h2>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>
          <div className="grid lg:grid-cols-2 gap-8 lg:gap-10 items-center">
            <p className="text-lg text-text-secondary leading-relaxed">
              O treino de Boxe e Kickboxing combina técnica de golpes, deslocamento, defesa, timing
              e condicionamento físico. Na 4Four Fight Academy, cada aula é pensada para desenvolver
              coordenação, potência, resistência e confiança, respeitando o nível de cada aluno.
            </p>
            <div className="group rounded-lg overflow-hidden border border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_24px_rgba(193,18,31,0.2)]">
              <img
                src={boxeAbout}
                alt="Treino técnico de Boxe e Kickboxing"
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
            <h2
              className="font-display"
              style={{ fontSize: "clamp(36px, 6vw, 56px)", color: "#F5F5F5" }}
            >
              BENEFÍCIOS DO BOXE E KICKBOXING
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-3xl mx-auto">
              Mais do que golpes, o treino desenvolve foco, preparação física, controlo emocional e
              confiança.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6">
            {benefits.map((benefit, idx) => (
              <Card
                key={benefit.title}
                className="bg-surface border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_20px_rgba(193,18,31,0.18)]"
                style={{ transitionDelay: `${idx * 100}ms` }}
              >
                <CardContent className="pt-8">
                  <benefit.icon
                    size={32}
                    strokeWidth={1.5}
                    style={{ color: "#C1121F", marginBottom: "20px" }}
                  />
                  <h3
                    className="font-display text-2xl mb-4"
                    style={{ color: "#F5F5F5", letterSpacing: "0.05em" }}
                  >
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

      <section
        className="px-4 py-20"
        style={{ background: "#0B0B0B", borderTop: "1px solid #1E1E1E" }}
      >
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h2
              className="font-display"
              style={{ fontSize: "clamp(36px, 6vw, 56px)", color: "#F5F5F5" }}
            >
              PROGRESSÃO NO STRIKING
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-2xl mx-auto">
              Evolução técnica por etapas, com critérios claros e treino consistente.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4 max-w-6xl mx-auto">
            {progressionLevels.map((level, idx) => (
              <Card key={level.name} className="bg-surface border-border-subtle">
                <CardContent className="pt-6 text-center">
                  <p className="text-xs tracking-[0.2em] uppercase text-text-secondary">
                    Etapa {idx + 1}
                  </p>
                  <h3
                    className="font-display text-lg mt-3"
                    style={{ color: "#F5F5F5", letterSpacing: "0.05em" }}
                  >
                    {level.name}
                  </h3>
                  <p className="text-sm mt-3" style={{ color: "#888", lineHeight: 1.7 }}>
                    {level.detail}
                  </p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-12">
            <h2
              className="font-display"
              style={{ fontSize: "clamp(36px, 6vw, 56px)", color: "#F5F5F5" }}
            >
              HORÁRIOS
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-2xl mx-auto">
              Pré-visualização das aulas de Boxe / Kickboxing disponíveis.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          {isScheduleLoading ? (
            <Card className="bg-surface border-border-subtle">
              <CardContent className="pt-6">
                <p className="text-center text-text-secondary">A carregar horários...</p>
              </CardContent>
            </Card>
          ) : strikingSchedule.length > 0 ? (
            <Card className="bg-surface border-border-subtle">
              <CardContent className="pt-6">
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border-subtle">
                        <th className="text-left py-3 px-4 text-text-secondary font-medium">Dia</th>
                        <th className="text-left py-3 px-4 text-text-secondary font-medium">
                          Horário
                        </th>
                        <th className="text-left py-3 px-4 text-text-secondary font-medium">
                          Nível
                        </th>
                      </tr>
                    </thead>
                    <tbody className="text-text-secondary">
                      {strikingSchedule.map((entry) => (
                        <tr
                          key={entry.id}
                          className="border-b border-border-subtle last:border-0 hover:bg-surface-2 transition-colors"
                        >
                          <td className="py-3 px-4">
                            {dayLabels[entry.dayOfWeek] ?? entry.dayOfWeek}
                          </td>
                          <td className="py-3 px-4">
                            {entry.startTime} - {entry.endTime}
                          </td>
                          <td className="py-3 px-4">{formatScheduleLevel(entry.level)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <p className="text-xs text-text-secondary mt-4 text-center">
                  Agenda sujeita a alterações. Consulta a página de agenda semanal para mais
                  detalhes.
                </p>
              </CardContent>
            </Card>
          ) : (
            <Card className="bg-surface border-border-subtle">
              <CardContent className="pt-6 text-center">
                <p className="text-text-secondary">
                  Consulta a agenda atualizada na página de agenda semanal.
                </p>
                <Button
                  asChild
                  variant="outline"
                  className="mt-4 border-border-subtle hover:border-red-500"
                >
                  <Link to="/schedule">Ver horários</Link>
                </Button>
              </CardContent>
            </Card>
          )}
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="max-w-4xl mx-auto text-center">
          <Card className="bg-surface border-border-subtle" style={{ padding: "60px 40px" }}>
            <CardContent>
              <Target
                size={40}
                strokeWidth={1.5}
                style={{ color: "#C1121F", marginBottom: "24px" }}
              />
              <h2 className="font-display text-4xl mb-4" style={{ color: "#F5F5F5" }}>
                Começa o teu treino de striking
              </h2>
              <p
                className="text-lg mb-8 mx-auto max-w-md"
                style={{ color: "#666", lineHeight: 1.8 }}
              >
                Escolhe um plano e começa a treinar Boxe/Kickboxing com acompanhamento estruturado.
              </p>
              <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                <Button
                  onClick={handleCtaClick}
                  className="btn-red w-full sm:w-auto px-10 py-4 text-[12px] tracking-[0.25em] uppercase font-semibold rounded-[2px]"
                >
                  Ver planos
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>
    </main>
  );
}
