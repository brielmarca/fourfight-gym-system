import { createFileRoute, Link } from "@tanstack/react-router";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

import {
  Target,
  Check,
  Clock,
  Zap,
  Shield,
  Trophy,
  Users,
  Dumbbell,
  Activity,
  Swords,
} from "lucide-react";
import mmaHero from "@/assets/gymlutas/jiu-jitsu-3.jpg";
import mmaGallery1 from "@/assets/gymlutas/jiu-jitsu-1.jpg";
import mmaGallery2 from "@/assets/gymlutas/jiu-jitsu-2.jpg";
import mmaVideo from "@/assets/gymlutas/jiu-jitsu-video.mp4";

export const Route = createFileRoute("/programas/mma")({
  component: MMAPage,
});

const trainingContent = [
  {
    icon: Target,
    title: "Striking",
    items: [
      "Boxe e Kickboxing base",
      "Cotoveladas e joelhadas",
      "Combinações e distância",
      "Defesa e contra-ataque",
    ],
  },
  {
    icon: Swords,
    title: "Wrestling",
    items: [
      "Quedas (takedowns)",
      "Defesa de quedas",
      "Controlo de clinch",
      "Trabalho de parede (wall work)",
    ],
  },
  {
    icon: Activity,
    title: "Grappling",
    items: [
      "Jiu-Jitsu no MMA",
      "Posições dominantes",
      "Defesa de submissão",
      "Transições e escapes",
    ],
  },
  {
    icon: Dumbbell,
    title: "Condicionamento de Luta",
    items: [
      "Preparação física específica",
      "Rounds e recuperação",
      "Força e explosão",
      "Mentalidade competitiva",
    ],
  },
];

const progressionLevels = [
  { name: "Fundamentos", order: 1 },
  { name: "Intermédio", order: 2 },
  { name: "Avançado", order: 3 },
  { name: "Sparring", order: 4 },
  { name: "Competição", order: 5 },
];

const benefits = [
  {
    icon: Zap,
    title: "Técnica Completa",
    desc: "Domina striking, grappling e transições entre todas as fases do combate.",
  },
  {
    icon: Shield,
    title: "Defesa Real",
    desc: "Prepara para situações reais com ferramentas de todas as artes marciais.",
  },
  {
    icon: Activity,
    title: "Condicionamento",
    desc: "Desenvolve fôlego, resistência e explosão para sustentar intensidade total.",
  },
  {
    icon: Target,
    title: "Estratégia",
    desc: "Aprende a ler o adversário e aplicar a técnica certa no momento certo.",
  },
  {
    icon: Users,
    title: "Ambiente de Elite",
    desc: "Treina com atletas focados e preparados para alto rendimento.",
  },
  {
    icon: Trophy,
    title: "Competição",
    desc: "Preparação técnica e física orientada para o combate competitivo.",
  },
];

function MMAPage() {
  return (
    <main className="bg-background text-foreground min-h-screen">
      <section className="relative min-h-[80vh] flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0">
          <img
            src={mmaHero}
            alt="Treino de MMA na 4Four Fight Academy"
            loading="eager"
            className="absolute inset-0 w-full h-full object-cover"
          />
        </div>
        <div
          className="absolute inset-0"
          style={{
            background:
              "linear-gradient(to bottom, rgba(0,0,0,0.55), rgba(11,11,11,0.8) 58%, #0B0B0B 100%)",
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
              fontSize: "clamp(52px, 11vw, 128px)",
              letterSpacing: "0.02em",
              color: "#F5F5F5",
            }}
          >
            MMA
          </h1>

          <p
            className="mt-6 mx-auto hero-word"
            style={{
              color: "#888",
              fontSize: "18px",
              maxWidth: "620px",
            }}
          >
            Combinação completa de striking, wrestling, grappling e condicionamento.
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
              O programa de MMA da 4Four Fight Academy oferece treino completo para combate,
              integrando striking, wrestling, grappling e condicionamento numa única metodologia.
            </p>
            <p>
              As aulas são estruturadas para desenvolver todas as fases do combate: stand-up,
              clinch, takedowns e ground game. Cada sessão trabalha técnica específica com
              progressão adequada ao nível do atleta.
            </p>
            <p>
              No MMA, a versatilidade é a chave. Vais aprender a transitar entre todas as distâncias
              de combate com confiança, técnica e preparação física de elite.
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
              Treino completo para alto desempenho em combate.
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

          <div className="grid grid-cols-2 md:grid-cols-5 gap-4 max-w-4xl mx-auto">
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

      <section className="px-4 py-12">
        <div className="max-w-6xl mx-auto">
          <div className="grid md:grid-cols-3 gap-6">
            <div className="group md:col-span-2 rounded-lg overflow-hidden border border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_24px_rgba(193,18,31,0.22)]">
              <img
                src={mmaGallery1}
                alt="Treino de MMA na 4Four Fight Academy"
                loading="lazy"
                className="w-full h-full min-h-[280px] object-cover transition-transform duration-500 group-hover:scale-[1.01]"
              />
            </div>
            <div className="group rounded-lg overflow-hidden border border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_24px_rgba(193,18,31,0.22)]">
              <img
                src={mmaGallery2}
                alt="Ambiente de treino MMA na 4Four Fight Academy"
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
              <source src={mmaVideo} type="video/mp4" />
            </video>
          </div>
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
                PRONTO PARA COMBATER?
              </h2>
              <p
                className="text-lg mb-8 mx-auto max-w-md"
                style={{ color: "#666", lineHeight: 1.8 }}
              >
                Escolhe um plano e começa a tua jornada no MMA com preparação de elite.
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
