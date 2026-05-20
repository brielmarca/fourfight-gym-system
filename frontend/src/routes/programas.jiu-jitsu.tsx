import { createFileRoute, Link } from "@tanstack/react-router";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { useBookTrial } from "@/queries";
import { useState } from "react";
import {
  Swords,
  Hand,
  Shield,
  Trophy,
  Target,
  Check,
  Loader2,
  Clock,
  Users,
  Heart,
  Zap,
} from "lucide-react";
import jiuJitsu1 from "@/assets/gymlutas/jiu-jitsu-1.jpg";
import jiuJitsu3 from "@/assets/gymlutas/jiu-jitsu-3.jpg";
import jiuJitsuVideo from "@/assets/gymlutas/jiu-jitsu-video.mp4";

export const Route = createFileRoute("/programas/jiu-jitsu")({
  component: JiuJitsuPage,
});

const trainingContent = [
  {
    icon: Swords,
    title: "Técnicas de Finalização",
    items: [
      "Mata Leão e variações",
      "Triângulos e chaves de Tornozelo",
      "Armbars e Omoplatas",
      "Chokes e Estrangulamentos",
      "Kimura e Americana",
      "Rear Naked Choke",
    ],
  },
  {
    icon: Hand,
    title: "Posições de Controlo",
    items: [
      "Side Control e variações",
      "Mount e Back Mount",
      "Half Guard e Deep Half",
      "Spider Guard",
      "De La Riva Guard",
      "Torrei e X Guard",
    ],
  },
  {
    icon: Shield,
    title: "Defesa Pessoal",
    items: [
      "Quedas e Rolamentos",
      "Posições de Segurança",
      "Defesa contra Grabbing",
      "Defesa contra Takedowns",
      "Escapes de Posições",
      "Controlo de Distância",
    ],
  },
  {
    icon: Trophy,
    title: "Preparação para Competição",
    items: [
      "Estratégia de Competição",
      "Treino Específico de Ronda",
      "Preparação Física",
      "Gestão de Energia",
      "Análise de Oponente",
      "Rotina de Competição",
    ],
  },
];

const levels = [
  {
    title: "Iniciante",
    desc: "Sem experiência prévia. Aprende os fundamentos e desenvolve coordenação.",
    duration: "3-6 meses",
  },
  {
    title: "Intermédio",
    desc: "Já domina os fundamentos. Desenvolve técnicas mais complexas e aplica em sparring.",
    duration: "6-18 meses",
  },
  {
    title: "Avançado",
    desc: "Pratica há mais de 1 ano. Foco em 세부alhes, competição e liderança.",
    duration: "18+ meses",
  },
];

const graduations = [
  { name: "Branca", order: 1 },
  { name: "Cinzenta", order: 2 },
  { name: "Amarela", order: 3 },
  { name: "Laranja", order: 4 },
  { name: "Verde", order: 5 },
  { name: "Azul", order: 6 },
  { name: "Roxa", order: 7 },
  { name: "Castanha", order: 8 },
  { name: "Preta", order: 9 },
];

function JiuJitsuPage() {
  const [isOpen, setIsOpen] = useState(false);
  const [success, setSuccess] = useState(false);
  const [form, setForm] = useState({ name: "", email: "", phone: "" });
  const bookTrial = useBookTrial();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await bookTrial.mutateAsync({ data: form, program: "JIU-JITSU" });
      setSuccess(true);
      setForm({ name: "", email: "", phone: "" });
      setTimeout(() => {
        setIsOpen(false);
        setSuccess(false);
      }, 2000);
    } catch (err) {
      alert(err instanceof Error ? err.message : "Erro ao agendar. Tenta novamente.");
    }
  };

  return (
    <main className="bg-background text-foreground min-h-screen">
      <section className="relative min-h-[80vh] flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0">
          <img
            src={jiuJitsu1}
            alt="Jiu-Jitsu treino na 4Four Fight Academy"
            loading="eager"
            className="absolute inset-0 w-full h-full object-cover"
          />
          <div
            className="absolute inset-0"
            style={{
              background: "linear-gradient(to bottom, transparent 40%, #0B0B0B 100%)",
            }}
          />
        </div>
        <div className="absolute inset-0" style={{ background: "rgba(0,0,0,0.5)" }} />

        <div
          className="relative px-4 text-center max-w-[900px] mx-auto pt-24"
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
              fontSize: "clamp(56px, 12vw, 140px)",
              letterSpacing: "0.02em",
              color: "#F5F5F5",
            }}
          >
            JIU-JITSU
          </h1>

          <p
            className="mt-6 mx-auto hero-word"
            style={{
              color: "#888",
              fontSize: "18px",
              maxWidth: "600px",
            }}
          >
            A arte suave que transforma força em técnica. Domine o chão e tome controlo de qualquer
            situação.
          </p>

          <div className="mt-10 hero-word">
            <Dialog open={isOpen} onOpenChange={setIsOpen}>
              <DialogTrigger asChild>
                <Button className="btn-red px-10 py-5 text-[12px] tracking-[0.25em] uppercase font-semibold rounded-[2px]">
                  Agendar Aula
                </Button>
              </DialogTrigger>
              <DialogContent className="bg-surface border-border-subtle max-w-md">
                <DialogHeader>
                  <DialogTitle className="font-display text-2xl tracking-wider text-center">
                    AULA EXPERIMENTAL
                  </DialogTitle>
                </DialogHeader>
                {success ? (
                  <div className="text-center py-8">
                    <Check
                      size={48}
                      strokeWidth={1.5}
                      style={{ color: "#C1121F", margin: "0 auto 16px" }}
                    />
                    <p className="text-lg text-foreground">Aula agendada!</p>
                    <p className="text-sm text-text-secondary mt-2">Vamos contactar-te em breve.</p>
                  </div>
                ) : (
                  <form onSubmit={handleSubmit} className="space-y-4 mt-4">
                    {bookTrial.error && (
                      <p className="text-sm text-destructive bg-destructive/10 p-3 rounded">
                        {bookTrial.error instanceof Error
                          ? bookTrial.error.message
                          : "Erro ao agendar"}
                      </p>
                    )}
                    <div className="space-y-2">
                      <label className="text-xs tracking-wider uppercase text-text-secondary">
                        Nome *
                      </label>
                      <Input
                        placeholder="O teu nome"
                        value={form.name}
                        onChange={(e) => setForm({ ...form, name: e.target.value })}
                        required
                        className="bg-surface-2 border-border-subtle"
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-xs tracking-wider uppercase text-text-secondary">
                        Email *
                      </label>
                      <Input
                        type="email"
                        placeholder="teu@email.com"
                        value={form.email}
                        onChange={(e) => setForm({ ...form, email: e.target.value })}
                        required
                        className="bg-surface-2 border-border-subtle"
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-xs tracking-wider uppercase text-text-secondary">
                        Telefone
                      </label>
                      <Input
                        placeholder="+351"
                        value={form.phone}
                        onChange={(e) => setForm({ ...form, phone: e.target.value })}
                        className="bg-surface-2 border-border-subtle"
                      />
                    </div>
                    <Button
                      type="submit"
                      disabled={bookTrial.isPending}
                      className="w-full btn-red tracking-[0.2em] uppercase mt-4"
                    >
                      {bookTrial.isPending ? (
                        <>
                          <Loader2 size={16} className="mr-2 animate-spin" />A enviar...
                        </>
                      ) : (
                        "Agendar Aula"
                      )}
                    </Button>
                  </form>
                )}
              </DialogContent>
            </Dialog>
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
              O Jiu-Jitsu na 4Four Fight Academy é muito mais que uma arte marcial — é um estilo de
              vida que promove disciplina, resiliência e autoconfiança através do controlo técnico.
            </p>
            <p>
              Sob a orientação de instrutores experientes, os nossos treinos combinam fundamentos
              sólidos com aplicações práticas de defesa pessoal e preparação para competição. Cada
              aula é estruturada para desafiar o teu corpo e mente, independentemente do teu nível.
            </p>
            <p>
              No tatame, aprenderás que a técnica vence a força bruta. O Jiu-Jitsu ensina-te a
              manter a calma sob pressão, a resolver problemas em tempo real e a adaptar-te a
              qualquer situação — lições que se estendem muito além do treino.
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
              O QUE VAI APRENDER
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
                className="reveal bg-surface border-border-subtle"
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

      <section className="px-4 py-12">
        <div className="max-w-4xl mx-auto">
          <div className="group rounded-lg overflow-hidden border border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_24px_rgba(193,18,31,0.22)]">
            <video
              autoPlay
              loop
              muted
              playsInline
              className="w-full h-auto transition-transform duration-500 group-hover:scale-[1.01]"
            >
              <source src={jiuJitsuVideo} type="video/mp4" />
            </video>
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
              Transforma o teu corpo e mente através da prática regular.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          <div className="grid md:grid-cols-3 gap-6">
            {[
              {
                icon: Heart,
                title: "Saúde & Bem-estar",
                desc: "Melhora o condicionamento cardiovascular, flexibilidade e força funcional.",
              },
              {
                icon: Shield,
                title: "Defesa Pessoal",
                desc: "Aprende técnicas eficazes para proteger-te em situações reais.",
              },
              {
                icon: Zap,
                title: "Disciplina Mental",
                desc: "Desenvolve foco, paciência e resiliência que se aplicam no dia-a-dia.",
              },
              {
                icon: Users,
                title: "Comunidade",
                desc: "Faz parte de uma equipa unida onde todos crescem juntos.",
              },
              {
                icon: Trophy,
                title: "Competição",
                desc: "Prepara-te para competir com confiança em qualquer nível.",
              },
              {
                icon: Target,
                title: "Autoconfiança",
                desc: "Ganha confiança ao dominar novas técnicas e superar desafios.",
              },
            ].map((benefit, idx) => (
              <div
                key={benefit.title}
                className="reveal text-center py-8 px-6"
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

      <section className="px-4 py-12">
        <div className="max-w-4xl mx-auto">
          <div className="group rounded-lg overflow-hidden border border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_24px_rgba(193,18,31,0.22)]">
            <img
              src={jiuJitsu3}
              alt="Jiu-Jitsu treino na 4Four Fight Academy"
              className="w-full h-auto object-cover transition-transform duration-500 group-hover:scale-[1.01]"
              loading="lazy"
            />
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
              Sistema de faixas do Jiu-Jitsu.
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
              O Jiu-Jitsu é para todos. Não requer força, flexibilidade ou experiência prévia.
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
              Treinos disponíveis de Segunda a Sábado.
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
                      <th className="text-left py-3 px-4 text-text-secondary font-medium">Nível</th>
                    </tr>
                  </thead>
                  <tbody className="text-text-secondary">
                    <tr className="border-b border-border-subtle hover:bg-surface-2 transition-colors">
                      <td className="py-3 px-4">Segunda</td>
                      <td className="py-3 px-4">19:00 - 20:30</td>
                      <td className="py-3 px-4">Todos</td>
                    </tr>
                    <tr className="border-b border-border-subtle hover:bg-surface-2 transition-colors">
                      <td className="py-3 px-4">Terça</td>
                      <td className="py-3 px-4">07:00 - 08:30</td>
                      <td className="py-3 px-4">Todos</td>
                    </tr>
                    <tr className="border-b border-border-subtle hover:bg-surface-2 transition-colors">
                      <td className="py-3 px-4">Quarta</td>
                      <td className="py-3 px-4">19:00 - 20:30</td>
                      <td className="py-3 px-4">Intermédio/Avançado</td>
                    </tr>
                    <tr className="border-b border-border-subtle hover:bg-surface-2 transition-colors">
                      <td className="py-3 px-4">Quinta</td>
                      <td className="py-3 px-4">07:00 - 08:30</td>
                      <td className="py-3 px-4">Todos</td>
                    </tr>
                    <tr className="border-b border-border-subtle hover:bg-surface-2 transition-colors">
                      <td className="py-3 px-4">Sexta</td>
                      <td className="py-3 px-4">19:00 - 20:30</td>
                      <td className="py-3 px-4">Todos</td>
                    </tr>
                    <tr className="hover:bg-surface-2 transition-colors">
                      <td className="py-3 px-4">Sábado</td>
                      <td className="py-3 px-4">10:00 - 11:30</td>
                      <td className="py-3 px-4">Open Mat</td>
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
                PRONTO PARA COMEÇAR?
              </h2>
              <p
                className="text-lg mb-8 mx-auto max-w-md"
                style={{ color: "#666", lineHeight: 1.8 }}
              >
                Descubra como o Jiu-Jitsu pode transformar a sua vida.
              </p>
              <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                <Button
                  asChild
                  variant="outline"
                  className="w-full sm:w-auto px-10 py-4 text-[12px] tracking-[0.25em] uppercase font-semibold rounded-[2px] border-border-subtle hover:border-red-500 transition-all duration-300"
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
