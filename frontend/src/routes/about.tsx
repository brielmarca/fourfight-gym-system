import { createFileRoute } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

import { Shield, Trophy, Users, Star, MapPin, Phone, Mail, Clock } from "lucide-react";

export const Route = createFileRoute("/about")({
  component: AboutPage,
});

const features = [
  {
    icon: Shield,
    title: "Tatames Profissionais",
    desc: "Piso de competição e superfícies de treino de alto nível",
  },
  {
    icon: Star,
    title: "Equipamento Premium",
    desc: "Área de força e condicionamento físico completa",
  },
  {
    icon: Users,
    title: "Instrutores de Elite",
    desc: "Treinadores com experiência profissional em competição",
  },
  {
    icon: Trophy,
    title: "Preparação para Competição",
    desc: "Treino estruturado para atletas que competem",
  },
];

const facilities = [
  { src: "/DSC06344.webp", label: "ÁREA PRINCIPAL DE TREINO" },
  { src: "/DSC06357.webp", label: "GINÁSIO & CONDICIONAMENTO" },
  { src: "/DSC06369(1).webp", label: "BALNEÁRIO & RECUPERAÇÃO" },
];

const stats = [
  { number: "500+", label: "Atletas Treinados" },
  { number: "12+", label: "Anos de Excelência" },
  { number: "3", label: "Modalidades" },
  { number: "10+", label: "Campeões" },
];

function AboutPage() {
  return (
    <main className="bg-background text-foreground min-h-screen">
      <div className="pt-32 pb-20 px-4">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h1 className="font-display text-3xl sm:text-4xl md:text-5xl lg:text-6xl tracking-wider">SOBRE NÓS</h1>
            <p className="mt-4 text-text-secondary text-lg">
              Mais do que uma academia. Uma família de lutadores.
            </p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-16">
            {stats.map((s) => (
              <div
                key={s.label}
                className="text-center py-6 border border-border-subtle bg-surface"
              >
                <p className="font-display text-4xl tracking-wider" style={{ color: "#C1121F" }}>
                  {s.number}
                </p>
                <p className="text-xs tracking-wider uppercase text-text-secondary mt-2">
                  {s.label}
                </p>
              </div>
            ))}
          </div>

          <div className="mb-16">
            <h2 className="font-display text-3xl tracking-wider mb-8">INSTALAÇÕES</h2>
            <div className="grid md:grid-cols-3 gap-4">
              {facilities.map((f, i) => (
                <div
                  key={f.label}
                  className="relative overflow-hidden"
                  style={{ height: "clamp(200px, 40vw, 320px)", borderRadius: "4px" }}
                >
                  <img
                    src={f.src}
                    alt={f.label}
                    className="absolute inset-0 w-full h-full object-cover"
                    style={{ filter: "brightness(0.6)" }}
                  />
                  <div className="absolute inset-x-0 bottom-6 text-center">
                    <span className="text-xs tracking-[0.3em] uppercase">{f.label}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="mb-16">
            <h2 className="font-display text-3xl tracking-wider mb-8">PORQUÊ ESCOLHER-NOS</h2>
            <div className="grid sm:grid-cols-2 gap-6">
              {features.map(({ icon: Icon, title, desc }) => (
                <Card key={title} className="bg-surface border-border-subtle">
                  <CardContent className="flex items-start gap-4 pt-6">
                    <Icon size={24} strokeWidth={1.5} style={{ color: "#C1121F" }} />
                    <div>
                      <h3 className="font-semibold text-lg">{title}</h3>
                      <p className="text-sm text-text-secondary">{desc}</p>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>

          <Card className="bg-surface border-border-subtle">
            <CardHeader>
              <CardTitle className="text-lg tracking-wider">LOCALIZAÇÃO</CardTitle>
            </CardHeader>
            <CardContent className="grid md:grid-cols-3 gap-6">
              <div className="flex items-start gap-3">
                <MapPin size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="font-semibold">Morada</p>
                  <p className="text-sm text-text-secondary">Gondomar, Portugal</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <Clock size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="font-semibold">Horário</p>
                  <p className="text-sm text-text-secondary">Seg-Sáb: 9h-22h</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <Phone size={20} style={{ color: "#C1121F" }} />
                <div>
                  <p className="font-semibold">Contacto</p>
                  <p className="text-sm text-text-secondary">+351 9xx xxx xxx</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </main>
  );
}
