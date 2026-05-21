import { useState } from "react";
import { createFileRoute, Link, redirect } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";

import { DarkMap } from "@/components/site/DarkMap";
import { useCreateContact } from "@/queries";
import { MapPin, Phone, Mail, Clock, Send, Calendar, Loader2, Check, X } from "lucide-react";

export const Route = createFileRoute("/contact")({
  beforeLoad: () => {
    throw redirect({ to: "/", hash: "contact" });
  },
  component: ContactPage,
});

const contactInfo = [
  { icon: MapPin, label: "Morada", value: ["Rua de Teste, 123", "Cidade de Teste, 1234-567"] },
  { icon: Phone, label: "Telefone", value: ["+351 912 345 678"] },
  {
    icon: Mail,
    label: "Email",
    value: ["4fourfight@gmail.com"],
    href: "mailto:4fourfight@gmail.com",
  },
  { icon: Clock, label: "Horário", value: ["Seg–Sex: 8h–22h", "Sáb: 8h–20h | Dom: 8h–12h"] },
];

function ContactPage() {
  const createContact = useCreateContact();
  const [success, setSuccess] = useState(false);
  const [form, setForm] = useState({
    name: "",
    email: "",
    phone: "",
    subject: "",
    message: "",
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (createContact.isPending) return;
    try {
      await createContact.mutateAsync(form);
      setSuccess(true);
      setForm({ name: "", email: "", phone: "", subject: "", message: "" });
      setTimeout(() => setSuccess(false), 5000);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Erro ao enviar. Tente novamente.";
      if (message.toLowerCase().includes("email")) {
        alert("E-mail inválido. Verifique o formato.");
      } else {
        alert(message);
      }
    }
  };

  return (
    <main className="bg-background text-foreground min-h-screen">
      <div className="pt-32 pb-20 px-4">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h1 className="font-display text-3xl sm:text-4xl md:text-5xl lg:text-6xl tracking-wider">FALA COMIGO</h1>
            <p className="mt-4 text-text-secondary text-lg">
              Tens dúvidas? Queres experimentar? Fala connosco!
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-8">
            <Card className="bg-surface border-border-subtle">
              <CardHeader>
                <CardTitle className="text-lg tracking-wider">ENVIA UMA MENSAGEM</CardTitle>
              </CardHeader>
              <CardContent>
                {success ? (
                  <div className="text-center py-8">
                    <Check
                      size={48}
                      strokeWidth={1.5}
                      style={{ color: "#C1121F", margin: "0 auto 16px" }}
                    />
                    <p className="text-lg text-foreground">Mensagem enviada!</p>
                    <p className="text-sm text-text-secondary mt-2">Vamos contactar-te em breve.</p>
                  </div>
                ) : (
                  <form onSubmit={handleSubmit} className="space-y-4">
                    {createContact.error && (
                      <div className="p-3 rounded bg-destructive/10 text-destructive text-sm flex items-center gap-2">
                        <X size={14} />
                        {createContact.error instanceof Error
                          ? createContact.error.message
                          : "Erro ao enviar"}
                      </div>
                    )}
                    <div className="grid sm:grid-cols-2 gap-4">
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
                          Telefone
                        </label>
                        <Input
                          placeholder="+351"
                          value={form.phone}
                          onChange={(e) => setForm({ ...form, phone: e.target.value })}
                          className="bg-surface-2 border-border-subtle"
                        />
                      </div>
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
                        Assunto *
                      </label>
                      <Input
                        placeholder="Assunto da mensagem"
                        value={form.subject}
                        onChange={(e) => setForm({ ...form, subject: e.target.value })}
                        required
                        className="bg-surface-2 border-border-subtle"
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-xs tracking-wider uppercase text-text-secondary">
                        Mensagem *
                      </label>
                      <Textarea
                        placeholder="O que queres saber?"
                        value={form.message}
                        onChange={(e) => setForm({ ...form, message: e.target.value })}
                        required
                        className="bg-surface-2 border-border-subtle min-h-[120px]"
                      />
                    </div>
                    <Button
                      type="submit"
                      disabled={createContact.isPending}
                      className="w-full btn-red tracking-[0.2em] uppercase"
                    >
                      {createContact.isPending ? (
                        <>
                          <Loader2 size={16} className="mr-2 animate-spin" />A enviar...
                        </>
                      ) : (
                        <>
                          <Send size={16} className="mr-2" />
                          Enviar Mensagem
                        </>
                      )}
                    </Button>
                  </form>
                )}
              </CardContent>
            </Card>

            <div className="space-y-6">
              <Card className="bg-surface border-border-subtle">
                <CardHeader>
                  <CardTitle className="text-lg tracking-wider">INFORMAÇÕES DE CONTACTO</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  {contactInfo.map(({ icon: Icon, label, value, href }) => (
                    <div key={label} className="flex items-center gap-4">
                      <Icon size={20} style={{ color: "#C1121F" }} />
                      <div>
                        <p className="text-xs tracking-wider uppercase text-text-secondary">
                          {label}
                        </p>
                        {href ? (
                          <a
                            href={href}
                            className="text-foreground transition-colors hover:text-primary"
                          >
                            {value[0]}
                          </a>
                        ) : (
                          value.map((line) => (
                            <p key={line} className="text-foreground">
                              {line}
                            </p>
                          ))
                        )}
                      </div>
                    </div>
                  ))}
                </CardContent>
              </Card>
            </div>
          </div>

          {/* ENCONTRE-NOS Section */}
          <div className="mt-16 max-w-6xl mx-auto">
            <div className="text-center mb-8">
              <h2 className="font-display text-3xl sm:text-4xl md:text-5xl tracking-wider text-foreground">
                ENCONTRE-NOS
              </h2>
              <p className="mt-4 text-text-secondary text-lg">
                Visite-nos na nossa academia e descubra o seu potencial
              </p>
            </div>

            <DarkMap height="clamp(250px, 50vw, 400px)" className="rounded-2xl" />

            <div className="mt-6 text-center">
              <Button
                asChild
                className="btn-red px-10 py-4 text-[12px] tracking-[0.2em] uppercase font-semibold"
              >
                <a
                  href="https://www.google.com/maps/search/?api=1&query=4Four+Fight+Academy+Gondomar+Portugal"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <MapPin size={16} className="mr-2" />
                  Abrir no Google Maps
                </a>
              </Button>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
