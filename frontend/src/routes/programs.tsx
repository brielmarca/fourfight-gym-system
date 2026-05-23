import { createFileRoute, Link } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

import { Swords, Hand, Zap, Target, Clock, Award, Users } from "lucide-react";

export const Route = createFileRoute("/programs")({
  component: ProgramsPage,
});

const programs = [
  {
    icon: Swords,
    title: "JIU-JITSU",
    desc: "Domine o jogo no chão. Finalizações, controllo e disciplina através da metodologia BJJ comprovada.",
    details: [
      "Técnicas de finalização",
      "Posições de controle",
      "Defesa pessoal",
      "Preparação para competição",
    ],
    level: "Todos os níveis",
  },
  {
    icon: Hand,
    title: "BOXE / KICKBOXING",
    desc: "Precisão nos golpes e potência explosiva. Dos fundamentos ao desempenho pronto para o ringue.",
    details: ["Fundamentos de Boxe", "Combinações", "Movimentação", "Condicionamento"],
    level: "Iniciante a Avançado",
  },
  {
    icon: Zap,
    title: "CAPOEIRA",
    desc: "Arte marcial brasileira que combina ritmo, mobilidade, expressão corporal e técnica.",
    details: ["Ginga e movimentação", "Ritmo e musicalidade", "Flexibilidade", "Defesa e ataque"],
    level: "Todos os níveis",
  },
  {
    icon: Target,
    title: "MMA",
    desc: "Combinação completa de striking, wrestling, grappling e condicionamento para combate.",
    details: ["Striking", "Wrestling", "Grappling", "Condicionamento de luta"],
    level: "Iniciante a Avançado",
  },
];

function ProgramsPage() {
  return (
    <main className="bg-background text-foreground min-h-screen">
      <div className="pt-32 pb-20 px-4">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h1 className="font-display text-3xl sm:text-4xl md:text-5xl lg:text-6xl tracking-wider">
              NOSSOS PROGRAMAS
            </h1>
            <p className="mt-4 text-text-secondary text-lg">
              Quatro modalidades de elite. Uma academia.
            </p>
          </div>

          <div className="mx-auto max-w-6xl grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {programs.map((p, i) => (
              <Card
                key={p.title}
                className={`bg-surface border-border-subtle ${p.title === "MMA" ? "lg:col-start-2" : ""}`}
              >
                <CardHeader className="text-center pb-2">
                  <p.icon
                    size={48}
                    strokeWidth={1.5}
                    style={{ color: "#C1121F", margin: "0 auto 16px" }}
                  />
                  <CardTitle className="font-display text-3xl tracking-wider">{p.title}</CardTitle>
                  <p className="text-sm text-text-secondary mt-2">{p.desc}</p>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div>
                    <div className="flex items-center gap-2 text-xs tracking-wider uppercase text-text-secondary mb-3">
                      <Award size={14} />
                      Nível: {p.level}
                    </div>
                    <ul className="space-y-2">
                      {p.details.map((d) => (
                        <li key={d} className="flex items-center gap-2 text-sm">
                          <span style={{ color: "#C1121F" }}>✓</span>
                          {d}
                        </li>
                      ))}
                    </ul>
                  </div>
                  {p.title === "JIU-JITSU" ? (
                    <Button asChild className="w-full btn-red tracking-[0.2em] uppercase text-xs">
                      <Link to="/programas/jiu-jitsu">Ver Mais</Link>
                    </Button>
                  ) : p.title === "BOXE / KICKBOXING" ? (
                    <Button asChild className="w-full btn-red tracking-[0.2em] uppercase text-xs">
                      <Link to="/programas/boxe-kickboxing">Ver Mais</Link>
                    </Button>
                  ) : p.title === "CAPOEIRA" ? (
                    <Button asChild className="w-full btn-red tracking-[0.2em] uppercase text-xs">
                      <Link to="/programas/capoeira">Ver Mais</Link>
                    </Button>
                  ) : p.title === "MMA" ? (
                    <Button asChild className="w-full btn-red tracking-[0.2em] uppercase text-xs">
                      <Link to="/programas/mma">Ver Mais</Link>
                    </Button>
                  ) : (
                    <Button className="w-full btn-red tracking-[0.2em] uppercase text-xs" disabled>
                      Ver Mais
                    </Button>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </div>
    </main>
  );
}
