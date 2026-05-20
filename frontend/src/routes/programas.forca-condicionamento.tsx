import { createFileRoute, Link } from "@tanstack/react-router";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

import {
  Activity,
  BatteryCharging,
  Check,
  Clock,
  Dumbbell,
  HeartPulse,
  ShieldCheck,
  Target,
  Trophy,
  Users,
  Zap,
} from "lucide-react";
import forcaHero from "@/assets/gymlutas/jiu-jitsu-2.jpg";
import forcaGallery1 from "@/assets/gymlutas/jiu-jitsu-4.jpg";
import forcaGallery2 from "@/assets/gymlutas/jiu-jitsu-1.jpg";
import forcaTrainingVideo from "@/assets/gymlutas/jiu-jitsu-video.mp4";

export const Route = createFileRoute("/programas/forca-condicionamento")({
  component: ForcaCondicionamentoPage,
});

const trainingContent = [
  {
    icon: Dumbbell,
    title: "Força Funcional",
    items: [
      "Padrões de empurrar, puxar, agachar e levantar",
      "Força de core e estabilidade",
      "Treino unilateral e controlo corporal",
      "Progressões adequadas ao nível do aluno",
    ],
  },
  {
    icon: Zap,
    title: "Potência",
    items: [
      "Explosão para quedas, golpes e deslocamentos",
      "Saltos, lançamentos e movimentos balísticos",
      "Aceleração e mudança de direção",
      "Transferência de força para modalidades de combate",
    ],
  },
  {
    icon: HeartPulse,
    title: "Resistência",
    items: [
      "Condicionamento cardiovascular específico",
      "Circuitos por rounds",
      "Capacidade de recuperação entre esforços",
      "Trabalho aeróbico e anaeróbico",
    ],
  },
  {
    icon: ShieldCheck,
    title: "Prevenção de Lesões",
    items: [
      "Mobilidade ativa",
      "Estabilidade articular",
      "Fortalecimento de zonas vulneráveis",
      "Técnica de execução e gestão de carga",
    ],
  },
];

const levels = [
  {
    title: "Base",
    desc: "Para quem quer construir técnica, mobilidade e força geral antes de aumentar intensidade.",
    duration: "0-3 meses",
  },
  {
    title: "Performance",
    desc: "Para alunos que já treinam e querem melhorar potência, resistência e consistência.",
    duration: "3-12 meses",
  },
  {
    title: "Competição",
    desc: "Preparação física orientada a rounds, recuperação e exigências reais de combate.",
    duration: "12+ meses",
  },
];

const progressionLevels = [
  { name: "Base", order: 1 },
  { name: "Intermédio", order: 2 },
  { name: "Avançado", order: 3 },
  { name: "Performance", order: 4 },
];

const benefits = [
  {
    icon: Zap,
    title: "Mais Potência",
    desc: "Desenvolve explosão para movimentos rápidos, fortes e eficientes.",
  },
  {
    icon: BatteryCharging,
    title: "Mais Energia",
    desc: "Aumenta resistência e capacidade de manter intensidade durante o treino.",
  },
  {
    icon: ShieldCheck,
    title: "Menos Lesões",
    desc: "Fortalece articulações, melhora mobilidade e cria melhores padrões de movimento.",
  },
  {
    icon: Target,
    title: "Objetivo Claro",
    desc: "Treino estruturado para evoluir força, cardio e performance de forma mensurável.",
  },
  {
    icon: Users,
    title: "Treino Guiado",
    desc: "Sessões acompanhadas, com correção técnica e adaptação ao teu nível.",
  },
  {
    icon: Trophy,
    title: "Alto Desempenho",
    desc: "Preparação física pensada para complementar artes marciais e rotina atlética.",
  },
];

function ForcaCondicionamentoPage() {
  return (
    <main className="bg-background text-foreground min-h-screen">
      <section className="relative min-h-[80vh] flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0">
          <img
            src={forcaHero}
            alt="Ambiente de treino e preparação física na 4Four Fight Academy"
            loading="eager"
            className="absolute inset-0 w-full h-full object-cover"
          />
        </div>
        <div
          className="absolute inset-0"
          style={{
            background:
              "linear-gradient(to bottom, rgba(0,0,0,0.58), rgba(11,11,11,0.8) 58%, #0B0B0B 100%)",
          }}
        />
        <div
          className="absolute inset-x-0 top-1/2 h-px"
          style={{ background: "rgba(193,18,31,0.35)" }}
        />

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
              fontSize: "clamp(48px, 10vw, 118px)",
              letterSpacing: "0.02em",
              color: "#F5F5F5",
            }}
          >
            FORÇA & CONDICIONAMENTO
          </h1>

          <p
            className="mt-6 mx-auto hero-word"
            style={{
              color: "#888",
              fontSize: "18px",
              maxWidth: "640px",
            }}
          >
            Potência, resistência e preparação física para alto desempenho.
          </p>

          <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4 hero-word">
            <Button
              asChild
              className="btn-red w-full sm:w-auto px-10 py-5 text-[12px] tracking-[0.25em] uppercase font-semibold rounded-[2px]"
            >
              <Link to="/plans">Ver Planos</Link>
            </Button>
          </div>
        </div>

        <div
          className="absolute bottom-0 left-0 right-0"
          style={{
            background: "linear-gradient(to top, #0B0B0B, transparent)",
            height: "120px",
          }}
        />
      </section>

      <section className="section-pad px-4">
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-12">
            <h2
              className="font-display"
              style={{ fontSize: "clamp(36px, 6vw, 56px)", color: "#F5F5F5" }}
            >
              SOBRE O PROGRAMA
            </h2>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>
          <div className="space-y-6 text-text-secondary leading-relaxed">
            <p className="text-lg">
              O programa de Força & Condicionamento da 4Four Fight Academy prepara o corpo para
              treinar melhor, recuperar melhor e sustentar maior intensidade com segurança.
            </p>
            <p>
              As sessões combinam força funcional, potência, mobilidade e condicionamento por
              rounds. A estrutura é pensada para atletas de combate, mas adapta-se a todos os níveis
              de experiência.
            </p>
            <p>
              O objetivo é simples: construir uma base física forte, resistente e eficiente para
              melhorar desempenho dentro e fora do tatame ou ringue.
            </p>
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
              O QUE VAIS TREINAR
            </h2>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          <div className="grid md:grid-cols-2 gap-6">
            {trainingContent.map((section, idx) => (
              <Card
                key={section.title}
                className="reveal bg-surface border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_20px_rgba(193,18,31,0.2)]"
                style={{ transitionDelay: `${idx * 100}ms` }}
              >
                <CardContent className="pt-8">
                  <section.icon
                    size={32}
                    strokeWidth={1.5}
                    style={{ color: "#C1121F", marginBottom: "20px" }}
                  />
                  <h3
                    className="font-display text-2xl mb-6"
                    style={{ color: "#F5F5F5", letterSpacing: "0.05em" }}
                  >
                    {section.title}
                  </h3>
                  <ul className="space-y-3">
                    {section.items.map((item) => (
                      <li key={item} className="flex items-start gap-3">
                        <Check
                          size={16}
                          strokeWidth={2}
                          style={{ color: "#C1121F", marginTop: "4px" }}
                        />
                        <span className="text-sm" style={{ color: "#888", lineHeight: 1.6 }}>
                          {item}
                        </span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            ))}
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
              BENEFÍCIOS
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-2xl mx-auto">
              Preparação física aplicada, eficiente e sustentável.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          <div className="grid md:grid-cols-3 gap-6">
            {benefits.map((benefit, idx) => (
              <div
                key={benefit.title}
                className="reveal text-center py-8 px-6 transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_20px_rgba(193,18,31,0.18)]"
                style={{
                  background: "#111111",
                  border: "1px solid #1E1E1E",
                  borderRadius: "4px",
                  transitionDelay: `${idx * 100}ms`,
                }}
              >
                <benefit.icon
                  size={32}
                  strokeWidth={1.5}
                  style={{ color: "#C1121F", marginBottom: "16px" }}
                />
                <h3
                  className="font-display text-xl mb-3"
                  style={{ color: "#F5F5F5", letterSpacing: "0.05em" }}
                >
                  {benefit.title}
                </h3>
                <p className="text-sm" style={{ color: "#666", lineHeight: 1.8 }}>
                  {benefit.desc}
                </p>
              </div>
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
              NÍVEIS DE PROGRESSÃO
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-2xl mx-auto">
              Evolução contínua através de níveis estruturados.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 max-w-3xl mx-auto">
            {progressionLevels.map((level) => (
              <div
                key={level.name}
                className="text-center py-6 px-3"
                style={{
                  background: "#111111",
                  border: "1px solid #1E1E1E",
                  borderRadius: "4px",
                }}
              >
                <div className="font-display text-3xl mb-2" style={{ color: "#F5F5F5" }}>
                  {level.order}
                </div>
                <h3
                  className="font-display text-sm mb-2"
                  style={{ color: "#F5F5F5", letterSpacing: "0.1em" }}
                >
                  {level.name.toUpperCase()}
                </h3>
                <div
                  style={{
                    borderTop: "1px solid #1E1E1E",
                    paddingTop: "8px",
                  }}
                >
                  <span className="text-xs tracking-widest uppercase" style={{ color: "#555" }}>
                    Nível {level.order}
                  </span>
                </div>
              </div>
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
              PARA QUEM É
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-2xl mx-auto">
              Para alunos que querem treinar com mais força, mais resistência e menos risco.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          <div className="grid md:grid-cols-3 gap-6">
            {levels.map((level, idx) => (
              <div
                key={level.title}
                className="reveal text-center py-10 px-6"
                style={{
                  background: "#111111",
                  border: "1px solid",
                  borderColor: idx === 1 ? "#C1121F" : "#1E1E1E",
                  borderRadius: "4px",
                  transitionDelay: `${idx * 100}ms`,
                }}
              >
                <div
                  className="font-display text-5xl mb-4"
                  style={{ color: idx === 1 ? "#C1121F" : "#F5F5F5" }}
                >
                  {String(idx + 1)}
                </div>
                <h3
                  className="font-display text-2xl mb-3"
                  style={{ color: "#F5F5F5", letterSpacing: "0.1em" }}
                >
                  {level.title.toUpperCase()}
                </h3>
                <p className="text-sm mb-6" style={{ color: "#666", lineHeight: 1.8 }}>
                  {level.desc}
                </p>
                <div
                  style={{
                    borderTop: "1px solid #1E1E1E",
                    paddingTop: "20px",
                  }}
                >
                  <span className="text-xs tracking-widest uppercase" style={{ color: "#555" }}>
                    {level.duration}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="px-4 py-12">
        <div className="max-w-6xl mx-auto">
          <div className="grid md:grid-cols-3 gap-6">
            <div className="group rounded-lg overflow-hidden border border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_24px_rgba(193,18,31,0.22)]">
              <img
                src={forcaGallery1}
                alt="Atleta em preparação para treino na 4Four Fight Academy"
                loading="lazy"
                className="w-full h-full min-h-[280px] object-cover transition-transform duration-500 group-hover:scale-[1.01]"
              />
            </div>
            <div className="group md:col-span-2 rounded-lg overflow-hidden border border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_24px_rgba(193,18,31,0.22)]">
              <img
                src={forcaGallery2}
                alt="Treino físico e técnico na 4Four Fight Academy"
                loading="lazy"
                className="w-full h-full min-h-[280px] object-cover transition-transform duration-500 group-hover:scale-[1.01]"
              />
            </div>
          </div>
          <div className="group mt-6 rounded-lg overflow-hidden border border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_24px_rgba(193,18,31,0.22)]">
            <video
              autoPlay
              loop
              muted
              playsInline
              className="w-full max-h-[620px] object-cover transition-transform duration-500 group-hover:scale-[1.01]"
            >
              <source src={forcaTrainingVideo} type="video/mp4" />
            </video>
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
              Sessões disponíveis de Segunda a Sexta.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

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
                      <th className="text-left py-3 px-4 text-text-secondary font-medium">Foco</th>
                    </tr>
                  </thead>
                  <tbody className="text-text-secondary">
                    <tr className="border-b border-border-subtle hover:bg-surface-2 transition-colors">
                      <td className="py-3 px-4">Segunda</td>
                      <td className="py-3 px-4">07:00 - 08:00</td>
                      <td className="py-3 px-4">Força</td>
                    </tr>
                    <tr className="border-b border-border-subtle hover:bg-surface-2 transition-colors">
                      <td className="py-3 px-4">Terça</td>
                      <td className="py-3 px-4">18:00 - 19:00</td>
                      <td className="py-3 px-4">Condicionamento</td>
                    </tr>
                    <tr className="border-b border-border-subtle hover:bg-surface-2 transition-colors">
                      <td className="py-3 px-4">Quarta</td>
                      <td className="py-3 px-4">07:00 - 08:00</td>
                      <td className="py-3 px-4">Potência</td>
                    </tr>
                    <tr className="border-b border-border-subtle hover:bg-surface-2 transition-colors">
                      <td className="py-3 px-4">Quinta</td>
                      <td className="py-3 px-4">18:00 - 19:00</td>
                      <td className="py-3 px-4">Rounds</td>
                    </tr>
                    <tr className="hover:bg-surface-2 transition-colors">
                      <td className="py-3 px-4">Sexta</td>
                      <td className="py-3 px-4">07:00 - 08:00</td>
                      <td className="py-3 px-4">Mobilidade</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <p className="text-xs text-text-secondary mt-4 text-center">
                * Horários sujeitos a alterações. Consulta a página de horários para mais detalhes.
              </p>
            </CardContent>
          </Card>
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
                PRONTO PARA EVOLUIR?
              </h2>
              <p
                className="text-lg mb-8 mx-auto max-w-md"
                style={{ color: "#666", lineHeight: 1.8 }}
              >
                Escolhe um plano e começa a construir potência, resistência e preparação física para
                alto desempenho.
              </p>
              <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                <Button
                  asChild
                  className="btn-red w-full sm:w-auto px-10 py-4 text-[12px] tracking-[0.25em] uppercase font-semibold rounded-[2px]"
                >
                  <Link to="/plans">Ver Planos</Link>
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>
    </main>
  );
}
