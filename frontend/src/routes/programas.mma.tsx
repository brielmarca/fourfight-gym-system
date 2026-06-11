import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useAuth } from "@/contexts/auth-context";
import { Activity, ArrowRight, Clock3, Dumbbell, Shield, Swords, Target, Zap } from "lucide-react";

const mmaHero = "/assets/optimized/jiu-jitsu/dsc06312-jiu-jitsu-hero.webp";

export const Route = createFileRoute("/programas/mma")({
  component: MMAPage,
});

const trainingCards = [
  {
    icon: Target,
    title: "Striking",
    items: [
      "Boxe e Kickboxing como base",
      "Combinações, ângulos e distância",
      "Defesa, contra-ataque e timing",
      "Entradas para clinch a partir do combate em pé",
    ],
  },
  {
    icon: Swords,
    title: "Wrestling e Clinch",
    items: [
      "Quedas e defesa de quedas",
      "Controlo de clinch e pressão",
      "Trabalho de parede",
      "Transições para posições dominantes",
    ],
  },
  {
    icon: Activity,
    title: "Grappling para MMA",
    items: [
      "Jiu-Jitsu adaptado ao MMA",
      "Controlo no solo e ground game",
      "Escapes, reversões e submissões",
      "Transições entre striking e chão",
    ],
  },
  {
    icon: Dumbbell,
    title: "Condicionamento",
    items: [
      "Força, explosão e resistência",
      "Rounds técnicos e recuperação",
      "Preparação física específica",
      "Disciplina e consistência de treino",
    ],
  },
];

const benefits = [
  {
    icon: Shield,
    title: "Versatilidade completa",
    desc: "Desenvolve ferramentas para responder em pé, no clinch e no solo com mais confiança.",
  },
  {
    icon: Zap,
    title: "Potência e resistência",
    desc: "Treina força, explosão, coordenação e capacidade cardiovascular com progressão segura.",
  },
  {
    icon: Target,
    title: "Estratégia e timing",
    desc: "Aprende a ler distâncias, escolher entradas e tomar decisões com mais controlo.",
  },
  {
    icon: Activity,
    title: "Disciplina competitiva",
    desc: "Constrói foco, consistência e mentalidade para evoluir dentro e fora do treino.",
  },
];

const progressionLevels = [
  { name: "Fundamentos", desc: "Base técnica, postura, deslocamento e segurança." },
  { name: "Técnica", desc: "Combinações, quedas, defesa e controlo." },
  { name: "Transições", desc: "Ligação entre striking, clinch, wrestling e chão." },
  { name: "Sparring Controlado", desc: "Aplicação progressiva com foco em leitura e proteção." },
  { name: "Performance", desc: "Estratégia, intensidade e preparação completa." },
];

function MMAPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const handleMainCta = () => {
    if (isAuthenticated) {
      void navigate({ to: "/plans" });
      return;
    }

    void navigate({ to: "/login" });
  };

  return (
    <main className="min-h-screen bg-background text-foreground">
      <section className="relative flex min-h-[78vh] items-center overflow-hidden border-b border-border-subtle">
        <img
          src={mmaHero}
          alt="Treino de MMA na 4Four Fight Academy"
          loading="eager"
          className="absolute inset-0 h-full w-full object-cover"
        />
        <div
          className="absolute inset-0"
          style={{
            background:
              "radial-gradient(circle at 18% 20%, rgba(193,18,31,0.28), transparent 42%), linear-gradient(to bottom, rgba(7,7,7,0.45), rgba(7,7,7,0.82) 52%, #0B0B0B 100%)",
          }}
        />

        <div className="relative mx-auto w-full max-w-6xl px-4 py-24 text-center">
          <h1
            className="font-display"
            style={{ fontSize: "clamp(58px, 12vw, 136px)", lineHeight: 0.9, letterSpacing: "0.04em", color: "#F5F5F5" }}
          >
            MMA
          </h1>
          <p className="mx-auto mt-6 max-w-3xl text-lg text-text-secondary">
            Combina striking, wrestling, grappling e condicionamento num treino completo, estruturado e focado na evolução real.
          </p>
          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <Button
              onClick={handleMainCta}
              className="btn-red w-full rounded-[2px] px-10 py-5 text-[12px] font-semibold uppercase tracking-[0.2em] sm:w-auto"
            >
              Começar treino
            </Button>
            <Button
              asChild
              variant="outline"
              className="w-full border-red-600/60 bg-black/40 px-8 py-5 text-[12px] font-semibold uppercase tracking-[0.2em] text-foreground hover:bg-red-950/30 sm:w-auto"
            >
              <Link to="/schedule">Ver horários</Link>
            </Button>
          </div>
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="mx-auto grid max-w-6xl items-center gap-8 rounded-lg border border-border-subtle bg-surface/80 p-8 md:grid-cols-[1.2fr_0.8fr] md:p-10">
          <div>
            <h2 className="font-display text-4xl" style={{ color: "#F5F5F5" }}>
              A ciência da eficácia
            </h2>
            <p className="mt-5 text-lg leading-relaxed text-text-secondary">
              O MMA na 4Four Fight Academy integra combate em pé, wrestling, grappling e preparação física específica num sistema progressivo e seguro. Cada aula desenvolve distância, controlo, timing e transições para que o aluno evolua em todas as fases do combate.
            </p>
          </div>
          <div className="rounded-md border border-red-700/40 bg-black/40 p-6">
            <p className="text-[11px] uppercase tracking-[0.24em] text-red-400">Metodologia</p>
            <p className="mt-4 text-sm leading-relaxed text-text-secondary">
              Progressão técnica com intensidade controlada, foco em segurança e evolução prática em todas as distâncias de combate.
            </p>
          </div>
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="mx-auto max-w-6xl">
          <h2 className="text-center font-display text-5xl" style={{ color: "#F5F5F5" }}>
            O que vais treinar
          </h2>
          <div className="mx-auto mt-5 h-[2px] w-14" style={{ background: "#C1121F" }} />
          <div className="mt-12 grid gap-6 md:grid-cols-2">
            {trainingCards.map((card) => (
              <Card key={card.title} className="border-border-subtle bg-surface transition-all duration-300 hover:border-red-600/70 hover:shadow-[0_0_24px_rgba(193,18,31,0.15)]">
                <CardContent className="pt-8">
                  <card.icon size={30} strokeWidth={1.7} style={{ color: "#C1121F" }} />
                  <h3 className="mt-4 font-display text-2xl" style={{ color: "#F5F5F5" }}>
                    {card.title}
                  </h3>
                  <ul className="mt-5 space-y-3">
                    {card.items.map((item) => (
                      <li key={item} className="flex gap-3 text-sm text-text-secondary">
                        <ArrowRight size={16} className="mt-1 shrink-0 text-red-500" />
                        <span>{item}</span>
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
        <div className="mx-auto max-w-6xl">
          <h2 className="text-center font-display text-5xl" style={{ color: "#F5F5F5" }}>
            BENEFÍCIOS DO MMA
          </h2>
          <p className="mx-auto mt-4 max-w-3xl text-center text-lg text-text-secondary">
            Mais do que combate, o treino desenvolve versatilidade, disciplina e capacidade física.
          </p>
          <div className="mt-12 grid gap-6 md:grid-cols-2 xl:grid-cols-4">
            {benefits.map((benefit) => (
              <Card key={benefit.title} className="h-full border-border-subtle bg-[#111111]">
                <CardContent className="pt-8">
                  <benefit.icon size={28} strokeWidth={1.6} className="text-red-500" />
                  <h3 className="mt-4 font-display text-2xl" style={{ color: "#F5F5F5" }}>
                    {benefit.title}
                  </h3>
                  <p className="mt-3 text-sm leading-relaxed text-text-secondary">{benefit.desc}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section className="border-y border-border-subtle bg-[#0B0B0B] px-4 py-20">
        <div className="mx-auto max-w-6xl">
          <h2 className="text-center font-display text-5xl" style={{ color: "#F5F5F5" }}>
            Progressão MMA
          </h2>
          <p className="mx-auto mt-4 max-w-3xl text-center text-lg text-text-secondary">
            Evolução técnica por etapas, do primeiro contacto à performance avançada.
          </p>
          <div className="mt-12 grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
            {progressionLevels.map((level, index) => (
              <Card key={level.name} className="border-border-subtle bg-[#111111]">
                <CardContent className="pt-7 text-center">
                  <p className="font-display text-4xl text-red-500">{index + 1}</p>
                  <h3 className="mt-3 font-display text-lg" style={{ color: "#F5F5F5" }}>
                    {level.name}
                  </h3>
                  <p className="mt-3 text-sm leading-relaxed text-text-secondary">{level.desc}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="mx-auto max-w-4xl text-center">
          <Card className="border-border-subtle bg-surface">
            <CardContent className="px-6 py-12 sm:px-10">
              <Clock3 size={34} strokeWidth={1.7} className="mx-auto text-red-500" />
              <h2 className="mt-4 font-display text-4xl" style={{ color: "#F5F5F5" }}>
                Horários MMA
              </h2>
              <p className="mx-auto mt-4 max-w-2xl text-lg text-text-secondary">
                Consulta os horários atualizados e escolhe a sessão mais adequada ao teu nível.
              </p>
              <Button
                asChild
                className="btn-red mt-8 rounded-[2px] px-10 py-4 text-[12px] font-semibold uppercase tracking-[0.2em]"
              >
                <Link to="/schedule">Ver horários</Link>
              </Button>
            </CardContent>
          </Card>
        </div>
      </section>

      <section className="px-4 pb-20">
        <div className="mx-auto max-w-4xl text-center">
          <Card className="border-red-700/40 bg-gradient-to-b from-[#14070a] to-[#0b0b0b]">
            <CardContent className="px-6 py-14 sm:px-10">
              <h2 className="font-display text-4xl" style={{ color: "#F5F5F5" }}>
                Começa o teu treino de MMA
              </h2>
              <p className="mx-auto mt-4 max-w-2xl text-lg text-text-secondary">
                Escolhe um plano e começa a treinar com acompanhamento estruturado.
              </p>
              <Button
                onClick={handleMainCta}
                className="btn-red mt-8 rounded-[2px] px-10 py-4 text-[12px] font-semibold uppercase tracking-[0.2em]"
              >
                Ver planos
              </Button>
            </CardContent>
          </Card>
        </div>
      </section>
    </main>
  );
}
