import { Swords, Hand, Dumbbell, type LucideIcon } from "lucide-react";
import { useState } from "react";
import { ProgramModal } from "./ProgramModal";

interface ProgramData {
  icon: LucideIcon;
  title: string;
  desc: string;
  details: string[];
  level: string;
  duration: string;
  schedule?: string;
}

const programs: ProgramData[] = [
  {
    icon: Swords,
    title: "JIU-JITSU",
    desc: "Domine o jogo no chão. Finalizações, controlo e disciplina através da metodologia BJJ comprovada.",
    details: [
      "Técnicas de finalização",
      "Posições de controlo",
      "Defesa pessoal",
      "Preparação para competição",
      "Gi e No-Gi",
    ],
    level: "Iniciante a Avançado",
    duration: "60-90 min",
    schedule: "Seg/Qua/Sex 19h-21h, Sáb 10h-12h",
  },
  {
    icon: Hand,
    title: "BOXE / KICKBOXING",
    desc: "Precisão nos golpes e potência explosiva. Dos fundamentos ao desempenho pronto para o ringue.",
    details: [
      "Fundamentos de Boxe",
      "Combinções de golpes",
      "Movimentação e esquiva",
      "Condicionamento específico",
      "Sparring técnico",
    ],
    level: "Iniciante a Avançado",
    duration: "60 min",
    schedule: "Seg/Qua 18h-19h, Ter/Qui 19h-20h",
  },
  {
    icon: Dumbbell,
    title: "FORÇA & CONDICIONAMENTO",
    desc: "Condicionamento atlético construído para lutadores. Potência, resistência e prevenção de lesões.",
    details: [
      "Treino funcional",
      "Hipertrofia e força",
      "Cardio de alta intensidade",
      "Mobilidade e flexibilidade",
      "Prevenção de lesões",
    ],
    level: "Todos os níveis",
    duration: "45-60 min",
    schedule: "Seg a Sex 7h-22h (Livre)",
  },
];

function ProgramCard({
  icon: Icon,
  title,
  desc,
  delay,
  onClick,
}: {
  icon: LucideIcon;
  title: string;
  desc: string;
  delay: number;
  onClick: () => void;
}) {
  const [hover, setHover] = useState(false);
  return (
    <div
      className="reveal relative flex flex-col cursor-pointer"
      style={{
        transitionDelay: `${delay}ms`,
        background: hover ? "#131313" : "#111111",
        border: "1px solid",
        borderColor: hover ? "#2A2A2A" : "#1E1E1E",
        borderLeft: hover ? "3px solid #C1121F" : "1px solid #1E1E1E",
        borderRadius: "4px",
        padding: "40px 32px",
        transform: hover ? "translateY(-6px)" : "translateY(0)",
        transition: "all 0.3s ease",
      }}
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => setHover(false)}
      onClick={onClick}
    >
      <Icon size={32} strokeWidth={1.5} style={{ color: "#C1121F", marginBottom: "28px" }} />
      <h3
        className="font-display"
        style={{ fontSize: "32px", color: "#F5F5F5", marginBottom: "12px" }}
      >
        {title}
      </h3>
      <p style={{ fontSize: "14px", color: "#666", lineHeight: 1.8, marginBottom: "32px" }}>
        {desc}
      </p>
      <div style={{ borderTop: "1px solid #1E1E1E", paddingTop: "20px", marginTop: "auto" }}>
        <span
          style={{
            fontSize: "11px",
            letterSpacing: "0.2em",
            textTransform: "uppercase",
            color: hover ? "#C1121F" : "#444",
            transition: "color 0.3s ease",
          }}
        >
          EXPLORAR PROGRAMA →
        </span>
      </div>
    </div>
  );
}

export function Programs() {
  const [selectedProgram, setSelectedProgram] = useState<ProgramData | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const openModal = (program: ProgramData) => {
    setSelectedProgram(program);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setTimeout(() => setSelectedProgram(null), 250);
  };

  return (
    <>
      <section id="programs" className="section-pad px-4">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16 reveal">
            <h2
              className="font-display"
              style={{ fontSize: "clamp(48px, 8vw, 72px)", color: "#F5F5F5" }}
            >
              FORJA A TUA DISCIPLINA
            </h2>
            <p className="mt-4" style={{ fontSize: "14px", color: "#666" }}>
              Três modalidades de elite. Uma academia.
            </p>
            <div
              className="mx-auto mt-6"
              style={{ width: "48px", height: "2px", background: "#C1121F" }}
            />
          </div>

          <div className="grid md:grid-cols-3 gap-6">
            {programs.map((p, i) => (
              <ProgramCard key={p.title} {...p} delay={i * 100} onClick={() => openModal(p)} />
            ))}
          </div>
        </div>
      </section>

      <ProgramModal program={selectedProgram} isOpen={isModalOpen} onClose={closeModal} />
    </>
  );
}
