import { createFileRoute, Link } from "@tanstack/react-router";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

import {
  Music,
  Check,
  Clock,
  Zap,
  Shield,
  Target,
  Trophy,
  Users,
  Heart,
  Activity,
} from "lucide-react";
import capoeiraHero from "@/assets/gymlutas/jiu-jitsu-2.jpg";
import capoeiraGallery1 from "@/assets/gymlutas/jiu-jitsu-1.jpg";
import capoeiraGallery2 from "@/assets/gymlutas/jiu-jitsu-3.jpg";
import capoeiraVideo from "@/assets/gymlutas/jiu-jitsu-video.mp4";

export const Route = createFileRoute("/programas/capoeira")({
  component: CapoeiraPage,
});

const trainingContent = [
  {
    icon: Music,
    title: "Ginga e Movimentação",
    items: [
      "Ginga base e variações",
      "Deslocamentos laterais e angulares",
      "Transições fluidas",
      "Controlo de distância",
    ],
  },
  {
    icon: Activity,
    title: "Ritmo e Musicalidade",
    items: [
      "Sincronização com o ritmo",
      "Leitura do berimbau",
      "Expressão corporal",
      "Adaptação ao jogo da roda",
    ],
  },
  {
    icon: Shield,
    title: "Defesa e Ataque",
    items: [
      "Esquivas e desvios",
      "Cabeçadas e penteados",
      "Chutes básicos e avançados",
      "Takedowns e quedas",
    ],
  },
  {
    icon: Zap,
    title: "Flexibilidade e Mobilidade",
    items: [
      "Alongamentos dinâmicos",
      "Mobilidade articular",
      "Equilíbrio e controlo",
      "Movimentos acrobáticos base",
    ],
  },
];

const graduations = [
  { name: "Crua", order: 1 },
  { name: "Crua-Amarela", order: 2 },
  { name: "Amarela", order: 3 },
  { name: "Amarela-Laranja", order: 4 },
  { name: "Laranja", order: 5 },
  { name: "Laranja-Azul", order: 6 },
  { name: "Azul", order: 7 },
  { name: "Azul-Verde", order: 8 },
  { name: "Verde", order: 9 },
  { name: "Verde-Roxa", order: 10 },
  { name: "Roxa", order: 11 },
  { name: "Roxa-Marrom", order: 12 },
  { name: "Marrom", order: 13 },
  { name: "Marrom-Vermelha", order: 14 },
  { name: "Vermelha", order: 15 },
];

const benefits = [
  {
    icon: Heart,
    title: "Expressão Corporal",
    desc: "Desenvolve liberdade de movimento, criatividade e consciência corporal.",
  },
  {
    icon: Zap,
    title: "Coordenação",
    desc: "Melhora coordenação motora, ritmo e sincronização através do jogo.",
  },
  {
    icon: Target,
    title: "Flexibilidade",
    desc: "Aumenta amplitude de movimento e previne lesões com mobilidade ativa.",
  },
  {
    icon: Shield,
    title: "Defesa Pessoal",
    desc: "Aprende esquivas, desvios e técnicas aplicáveis em situações reais.",
  },
  {
    icon: Users,
    title: "Comunidade",
    desc: "Vive a cultura da roda, partilha e crescimento coletivo.",
  },
  {
    icon: Trophy,
    title: "Disciplina",
    desc: "Cultiva foco, respeito e preservação da tradição da Capoeira.",
  },
];

function CapoeiraPage() {
  return (
    <main className="bg-background text-foreground min-h-screen">
      <section className="relative min-h-[80vh] flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0">
          <img
            src={capoeiraHero}
            alt="Treino de Capoeira na 4Four Fight Academy"
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
            CAPOEIRA
          </h1>

          <p
            className="mt-6 mx-auto hero-word"
            style={{
              color: "#888",
              fontSize: "18px",
              maxWidth: "620px",
            }}
          >
            Arte marcial brasileira que combina ritmo, mobilidade e técnica.
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
              O programa de Capoeira da 4Four Fight Academy une tradição e modernidade, ensinando a
              arte marcial brasileira com respeito à sua cultura e eficácia.
            </p>
            <p>
              As aulas combinam ginga, musicalidade, expressão corporal e técnica de ataque e
              defesa. O aluno desenvolve mobilidade, ritmo e consciência corporal enquanto aprende
              uma das artes marciais mais completas do mundo.
            </p>
            <p>
              Na roda, aprendes que a Capoeira é diálogo constante — onde movimento, música e
              estratégia se encontram para criar um jogo único e pessoal.
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
              O QUE VAIS APRENDER
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
              Uma arte marcial completa para corpo, mente e espírito.
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
              GRADUAÇÃO
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-2xl mx-auto">
              Progressão através das cordas da Capoeira.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          <div className="grid grid-cols-3 md:grid-cols-5 gap-4 max-w-4xl mx-auto">
            {graduations.map((grad) => (
              <div
                key={grad.name}
                className="text-center py-4 px-2"
                style={{
                  background: "#111111",
                  border: "1px solid #1E1E1E",
                  borderRadius: "4px",
                }}
              >
                <div
                  className="font-display text-sm mb-2"
                  style={{ color: "#F5F5F5", letterSpacing: "0.05em" }}
                >
                  {grad.name}
                </div>
                <div
                  style={{
                    borderTop: "1px solid #1E1E1E",
                    paddingTop: "8px",
                  }}
                >
                  <span className="text-xs tracking-widest uppercase" style={{ color: "#555" }}>
                    {grad.order}º
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
                src={capoeiraGallery1}
                alt="Treino de Capoeira na 4Four Fight Academy"
                loading="lazy"
                className="w-full h-full min-h-[280px] object-cover transition-transform duration-500 group-hover:scale-[1.01]"
              />
            </div>
            <div className="group rounded-lg overflow-hidden border border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_24px_rgba(193,18,31,0.22)]">
              <img
                src={capoeiraGallery2}
                alt="Ambiente de treino da 4Four Fight Academy"
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
              <source src={capoeiraVideo} type="video/mp4" />
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
                PRONTO PARA COMEÇAR?
              </h2>
              <p
                className="text-lg mb-8 mx-auto max-w-md"
                style={{ color: "#666", lineHeight: 1.8 }}
              >
                Escolhe um plano e começa a treinar Capoeira na melhor academia da região.
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
