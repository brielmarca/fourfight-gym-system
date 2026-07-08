import { Shield, Trophy, Users, Star, CheckCircle } from "lucide-react";
import { Badge } from "@/components/ui/badge";

const features = [
  {
    icon: Shield,
    title: "Instrutores Certificados",
    desc: "Pso de competição e formação especializada em Jiu-Jitsu e Boxe",
  },
  {
    icon: Star,
    title: "5★ Avaliação Média",
    desc: "Alunos confirmam a qualidade do treino e atenção individual",
  },
  {
    icon: Users,
    title: "Equipa de Elite",
    desc: "Treinadores com mais de 12 anos de experiência em competição",
  },
  {
    icon: Trophy,
    title: "Preparação para Competição",
    desc: "Treino estruturado com atletas que já competiram internacionalmente",
  },
];

const facilities = [
  {
    src: "/assets/espacodetreino.webp",
    label: "Área Principal de Treino",
    badge: "500m²",
    alt: "Área de treino da 4Four Fight Academy",
  },
  { src: "/assets/DSC06357.webp", label: "Ginásio & Condicionamento", badge: "Equipamento Pro" },
  { src: "/assets/DSC06369(1).webp", label: "Balneário & Recuperação", badge: "Sauna Incluída" },
];

export function Academy() {
  return (
    <section id="academy" className="section-pad px-4" style={{ background: "#0B0B0B" }}>
      <div className="max-w-7xl mx-auto">
        {/* Heading with trust badge */}
        <div className="reveal mb-16 flex flex-col sm:flex-row items-start sm:items-center gap-4">
          <div style={{ width: "4px", height: "56px", background: "#C1121F" }} />
          <div>
            <h2
              className="font-display"
              style={{
                fontSize: "clamp(40px, 7vw, 64px)",
                color: "#F5F5F5",
                lineHeight: 1,
              }}
            >
              INFRAESTRUTURA DE ALTO NÍVEL
            </h2>
            <p style={{ fontSize: "14px", color: "#666", fontStyle: "italic", marginTop: "4px" }}>
              Confia na academia escolhida por campeões
            </p>
          </div>
          <Badge className="bg-primary/10 text-primary border-primary/30 ml-auto hidden sm:block">
            ✓ Verificado
          </Badge>
        </div>

        {/* Features 2x2 — open layout */}
        <div className="grid sm:grid-cols-2 gap-12 mb-24">
          {features.map(({ icon: Icon, title, desc }, i) => (
            <div key={title} className="reveal" style={{ transitionDelay: `${i * 100}ms` }}>
              <Icon size={20} style={{ color: "#C1121F" }} strokeWidth={1.6} />
              <h3
                style={{
                  fontSize: "15px",
                  fontWeight: 600,
                  color: "#F5F5F5",
                  marginTop: "16px",
                  fontFamily: "var(--font-sans)",
                }}
              >
                {title}
              </h3>
              <p style={{ fontSize: "13px", color: "#666", lineHeight: 1.7, marginTop: "6px" }}>
                {desc}
              </p>
            </div>
          ))}
        </div>

        {/* Facilities photos with badges */}
        <div className="grid md:grid-cols-3 gap-4">
          {facilities.map((f, i) => (
            <div
              key={f.label}
              className="reveal group relative overflow-hidden cursor-pointer"
              style={{
                height: "320px",
                borderRadius: "4px",
                transitionDelay: `${i * 100}ms`,
              }}
            >
              <img
                src={f.src}
                alt={f.alt ?? f.label}
                loading="lazy"
                className="absolute inset-0 w-full h-full object-cover transition-all duration-700 ease-out group-hover:scale-[1.06]"
                style={{ filter: "brightness(0.7) contrast(1.1)" }}
                onMouseEnter={(e) =>
                  (e.currentTarget.style.filter = "brightness(0.5) contrast(1.1)")
                }
                onMouseLeave={(e) =>
                  (e.currentTarget.style.filter = "brightness(0.7) contrast(1.1)")
                }
              />
              <div
                className="absolute inset-0"
                style={{
                  background: "linear-gradient(to bottom, transparent 30%, rgba(0,0,0,0.5) 100%)",
                }}
              />
              <div className="absolute inset-x-0 bottom-6 text-center">
                <Badge className="bg-primary/90 text-white border-0 mb-2">{f.badge}</Badge>
                <p
                  style={{
                    fontSize: "11px",
                    letterSpacing: "0.3em",
                    textTransform: "uppercase",
                    color: "#F5F5F5",
                    fontWeight: 600,
                  }}
                >
                  {f.label}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
