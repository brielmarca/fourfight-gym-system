import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/contexts/use-auth";
import { Circle, Drum, Shield, Sparkles, Users, Waves } from "lucide-react";

const capoeiraHero = "/assets/optimized/capoeira/capoeira-training.webp";

export const Route = createFileRoute("/programas/capoeira")({
  component: CapoeiraPage,
});

const trainingContent = [
  {
    icon: Drum,
    title: "Ginga e movimentação",
    desc: "Base da capoeira: ritmo, esquiva, equilíbrio, coordenação e controle corporal.",
  },
  {
    icon: Shield,
    title: "Esquivas e defesa",
    desc: "Aprenda a sair da linha de ataque com fluidez, leitura de jogo e proteção.",
  },
  {
    icon: Sparkles,
    title: "Chutes e ataques",
    desc: "Técnicas como meia-lua, armada, martelo e benção, com segurança e progressão.",
  },
  {
    icon: Users,
    title: "Ritmo, musicalidade e roda",
    desc: "Entenda a energia da roda, chamada, resposta, timing e expressão corporal.",
  },
  {
    icon: Waves,
    title: "Condicionamento e mobilidade",
    desc: "Força, flexibilidade, resistência, mobilidade e consciência corporal.",
  },
];

const cordas = [
  { name: "Crua", level: "Nível 1", stripe: "bg-stone-300" },
  { name: "Amarela", level: "Nível 2", stripe: "bg-yellow-400" },
  { name: "Laranja", level: "Nível 3", stripe: "bg-orange-500" },
  { name: "Azul", level: "Nível 4", stripe: "bg-blue-500" },
  { name: "Verde", level: "Nível 5", stripe: "bg-green-500" },
  { name: "Rubi", level: "Nível 6", stripe: "bg-red-700" },
];

const benefits = [
  {
    icon: Sparkles,
    title: "Expressão corporal",
    desc: "Desenvolve liberdade de movimento, criatividade e consciência corporal.",
  },
  {
    icon: Drum,
    title: "Ritmo e coordenação",
    desc: "Melhora coordenação motora, musicalidade e sincronização através do jogo.",
  },
  {
    icon: Waves,
    title: "Mobilidade e flexibilidade",
    desc: "Aumenta amplitude de movimento, equilíbrio e controlo corporal com progressão.",
  },
  {
    icon: Users,
    title: "Comunidade e tradição",
    desc: "Vive a cultura da roda, o respeito e o crescimento coletivo.",
  },
];

function CapoeiraPage() {
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
          src={capoeiraHero}
          alt="Treino de Capoeira na 4Four Fight Academy"
          loading="eager"
          className="absolute inset-0 h-full w-full object-cover"
        />
        <div
          className="absolute inset-0"
          style={{
            background:
              "radial-gradient(circle at 16% 20%, rgba(193,18,31,0.28), transparent 45%), linear-gradient(to bottom, rgba(7,7,7,0.55), rgba(7,7,7,0.82) 55%, #0B0B0B 100%)",
          }}
        />

        <div className="relative mx-auto w-full max-w-6xl px-4 py-24 text-center">
          <h1
            className="font-display"
            style={{
              fontSize: "clamp(58px, 12vw, 140px)",
              lineHeight: 0.9,
              letterSpacing: "0.04em",
              color: "#F5F5F5",
            }}
          >
            CAPOEIRA
          </h1>
          <p className="mx-auto mt-6 max-w-3xl text-lg text-text-secondary">
            Ritmo, mobilidade, expressão corporal e técnica numa arte marcial brasileira completa e
            cheia de identidade.
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
              A essência do jogo
            </h2>
            <p className="mt-5 text-lg leading-relaxed text-text-secondary">
              A Capoeira na 4Four Fight Academy une tradição, ritmo, mobilidade e técnica para
              desenvolver corpo, mente e expressão. Cada treino trabalha ginga, esquivas,
              coordenação, musicalidade e jogo, respeitando o nível de cada aluno.
            </p>
          </div>
          <div className="rounded-md border border-red-700/40 bg-black/40 p-6">
            <p className="text-[11px] uppercase tracking-[0.24em] text-red-400">Capoeira</p>
            <p className="mt-4 text-sm leading-relaxed text-text-secondary">
              Evolução técnica com identidade brasileira, progressão segura e foco no jogo dentro da
              roda.
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
              O que vais aprender
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
                className="bg-surface border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_20px_rgba(193,18,31,0.2)]"
                style={{ transitionDelay: `${idx * 100}ms` }}
              >
                <CardContent className="pt-8 pb-8">
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
                  <p className="text-sm" style={{ color: "#B8B8B8", lineHeight: 1.8 }}>
                    {section.desc}
                  </p>
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
              BENEFÍCIOS DA CAPOEIRA
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-2xl mx-auto">
              Uma arte marcial completa para corpo, mente, cultura e movimento.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-4">
            {benefits.map((benefit, idx) => (
              <div
                key={benefit.title}
                className="text-center py-8 px-6 transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_20px_rgba(193,18,31,0.18)]"
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
                <p className="text-sm" style={{ color: "#B8B8B8", lineHeight: 1.8 }}>
                  {benefit.desc}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="border-y border-border-subtle bg-[#0B0B0B] px-4 py-20">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h2
              className="font-display"
              style={{ fontSize: "clamp(36px, 6vw, 56px)", color: "#F5F5F5" }}
            >
              Sistema de cordas
            </h2>
            <p className="mt-4 text-text-secondary text-lg max-w-2xl mx-auto">
              Progressão técnica através de etapas de aprendizagem e responsabilidade.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          <div className="mx-auto grid max-w-5xl gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {cordas.map((corda) => (
              <Card key={corda.name} className="border-border-subtle bg-[#111111]">
                <CardContent className="pt-6">
                  <div className="mb-4 flex items-center justify-between">
                    <span className="text-xs uppercase tracking-[0.2em] text-text-secondary">
                      Corda
                    </span>
                    <Circle size={14} className="text-red-500" />
                  </div>
                  <h3 className="font-display text-2xl" style={{ color: "#F5F5F5" }}>
                    {corda.name}
                  </h3>
                  <p className="mt-2 text-sm text-text-secondary">{corda.level}</p>
                  <div className="mt-5 h-1.5 rounded-full bg-neutral-900" aria-hidden="true">
                    <div className={`h-full w-3/4 rounded-full ${corda.stripe}`} />
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="mx-auto max-w-4xl text-center">
          <Card className="border-border-subtle bg-surface" style={{ padding: "60px 40px" }}>
            <CardContent>
              <h2 className="font-display text-4xl" style={{ color: "#F5F5F5" }}>
                Horários Capoeira
              </h2>
              <p className="mx-auto mt-4 max-w-2xl text-lg text-text-secondary">
                Consulta os horários atualizados e encontra a sessão ideal para começar.
              </p>
              <Button
                asChild
                variant="outline"
                className="mt-8 border-red-600/60 bg-black/40 px-8 py-5 text-[12px] font-semibold uppercase tracking-[0.2em] text-foreground hover:bg-red-950/30"
              >
                <Link to="/schedule">Ver horários</Link>
              </Button>
            </CardContent>
          </Card>
        </div>
      </section>

      <section className="section-pad px-4">
        <div className="max-w-4xl mx-auto text-center">
          <Card className="bg-surface border-border-subtle" style={{ padding: "60px 40px" }}>
            <CardContent>
              <h2 className="font-display text-4xl mb-4" style={{ color: "#F5F5F5" }}>
                Começa o teu treino de Capoeira
              </h2>
              <p
                className="text-lg mb-8 mx-auto max-w-md"
                style={{ color: "#B8B8B8", lineHeight: 1.8 }}
              >
                Escolhe um plano e entra na roda com acompanhamento estruturado.
              </p>
              <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                <Button
                  onClick={handleMainCta}
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
