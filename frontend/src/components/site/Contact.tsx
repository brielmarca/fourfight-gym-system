import { MapPin, Phone, Mail, Clock, Shield, Calendar } from "lucide-react";
import { DarkMap } from "./DarkMap";
import { whatsappNumberDisplay } from "@/lib/contact";

const items = [
  { icon: MapPin, label: "Endereço", value: "Rua de Teste, 123, Gondomar, Portugal" },
  { icon: Phone, label: "WhatsApp", value: whatsappNumberDisplay },
  { icon: Mail, label: "Email", value: "4fourfight@gmail.com" },
  { icon: Clock, label: "Horário", value: "Seg–Sex 8h–22h · Sáb 8h–20h · Dom 8h–12h" },
];

export function Contact() {
  return (
    <section id="contact" className="section-pad px-4">
      <div className="max-w-7xl mx-auto">
        {/* Left aligned heading */}
        <div className="reveal mb-14 flex items-center gap-5">
          <span style={{ width: "4px", height: "56px", background: "#C1121F" }} />
          <h2
            className="font-display"
            style={{ fontSize: "clamp(40px, 7vw, 64px)", color: "#F5F5F5" }}
          >
            VISITA A ACADEMIA
          </h2>
        </div>

        <div className="grid md:grid-cols-5 gap-10 items-start">
          {/* LEFT 60% */}
          <div className="reveal md:col-span-3">
            {items.map(({ icon: Icon, label, value }) => (
              <div
                key={label}
                className="flex gap-4 items-start"
                style={{
                  padding: "20px 0",
                  borderBottom: "1px solid #161616",
                }}
              >
                <Icon
                  size={20}
                  strokeWidth={1.6}
                  style={{ color: "#C1121F", marginTop: "4px", flexShrink: 0 }}
                />
                <div>
                  <div
                    style={{
                      fontSize: "10px",
                      letterSpacing: "0.25em",
                      textTransform: "uppercase",
                      color: "#444",
                    }}
                  >
                    {label}
                  </div>
                  <div style={{ fontSize: "15px", color: "#F5F5F5", marginTop: "4px" }}>
                    {value}
                  </div>
                </div>
              </div>
            ))}

            {/* Trust indicators */}
            <div className="grid grid-cols-2 gap-4 mt-8">
              <div
                className="flex items-center gap-3"
                style={{
                  background: "#111111",
                  border: "1px solid #1E1E1E",
                  padding: "16px 20px",
                  borderRadius: "4px",
                }}
              >
                <Shield size={18} style={{ color: "#C1121F" }} />
                <span style={{ fontSize: "13px", color: "#888" }}>500+ Atletas treinados</span>
              </div>
              <div
                className="flex items-center gap-3"
                style={{
                  background: "#111111",
                  border: "1px solid #1E1E1E",
                  padding: "16px 20px",
                  borderRadius: "4px",
                }}
              >
                <Calendar size={18} style={{ color: "#C1121F" }} />
                <span style={{ fontSize: "13px", color: "#888" }}>Desde 2012</span>
              </div>
            </div>
          </div>

          {/* RIGHT 40% map */}
          <div className="reveal md:col-span-2">
            <DarkMap height="380px" />
          </div>
        </div>
      </div>
    </section>
  );
}
