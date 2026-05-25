import { Link } from "@tanstack/react-router";

import { openCookiePreferences } from "@/components/privacy/CookieConsentBanner";

const navigationLinks = [
  { label: "Academia", to: "/about" },
  { label: "Programas", to: "/programs" },
  { label: "Horários", to: "/schedule" },
  { label: "Planos", to: "/plans" },
  { label: "Contacto", to: "/contact" },
  { label: "Política de Cookies", to: "/politica-cookies" },
];

const contactInfo = {
  address: ["Rua de Teste, 123", "Cidade de Teste, 1234-567"],
  phone: "+351 912 345 678",
  email: "4fourfight@gmail.com",
  schedule: ["Seg–Sex: 8h–22h", "Sáb: 8h–20h | Dom: 8h–12h"],
};

export function Footer() {
  return (
    <footer
      className="px-4"
      style={{
        background: "#080808",
        borderTop: "1px solid #161616",
        paddingTop: "48px",
        paddingBottom: "32px",
      }}
    >
      <div className="max-w-7xl mx-auto">
        <div className="grid grid-cols-1 md:grid-cols-[1.25fr_0.8fr_1fr] gap-12 md:gap-10">
          {/* Logo and description */}
          <div className="text-center md:text-left">
            <Link
              to="/"
              className="inline-block mb-5 transition-opacity duration-300 hover:opacity-85"
            >
              <img
                src="/assets/logo.png"
                alt="4Four Fight Academy"
                style={{
                  height: "52px",
                  width: "auto",
                  mixBlendMode: "screen",
                  filter: "brightness(1.1)",
                }}
              />
            </Link>
            <p className="mx-auto max-w-sm text-sm leading-7 md:mx-0" style={{ color: "#8A8A8A" }}>
              Transforme a sua mente, corpo e espírito através das disciplinas de Jiu-Jitsu
              Brasileiro, Boxe e Kickboxing.
            </p>
          </div>

          {/* Navigation links */}
          <div className="text-center">
            <h3
              className="text-[11px] uppercase tracking-[0.22em] font-semibold mb-6"
              style={{ color: "#A0A0A0" }}
            >
              Navegação
            </h3>
            <nav className="mx-auto flex max-w-[280px] flex-wrap items-center justify-center gap-x-5 gap-y-4">
              {navigationLinks.map((l) => (
                <Link
                  key={l.to}
                  to={l.to}
                  className="group relative inline-flex justify-center pb-2 text-[11px] font-medium uppercase tracking-[0.28em] text-zinc-500 transition-colors duration-300 hover:text-red-500 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-red-600/70"
                  activeProps={{ className: "text-[#C1121F]" }}
                >
                  {l.label}
                  <span className="absolute bottom-0 left-1/2 h-px w-0 -translate-x-1/2 bg-red-500 transition-all duration-300 group-hover:w-6" />
                </Link>
              ))}
              <button
                type="button"
                onClick={openCookiePreferences}
                className="group relative inline-flex justify-center pb-2 text-[11px] font-medium uppercase tracking-[0.28em] text-zinc-500 transition-colors duration-300 hover:text-red-500 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-red-600/70"
              >
                Preferências de cookies
                <span className="absolute bottom-0 left-1/2 h-px w-0 -translate-x-1/2 bg-red-500 transition-all duration-300 group-hover:w-6" />
              </button>
            </nav>
          </div>

          {/* Contact information */}
          <div className="text-center md:text-right">
            <h3
              className="text-[11px] uppercase tracking-[0.22em] font-semibold mb-6"
              style={{ color: "#A0A0A0" }}
            >
              Contacto
            </h3>
            <div className="mx-auto max-w-sm space-y-4 md:mx-0 md:ml-auto">
              <div className="space-y-1 border-y border-white/[0.06] py-4 md:border-l md:border-y-0 md:py-0 md:pl-5">
                {contactInfo.address.map((line) => (
                  <p key={line} className="text-sm leading-6" style={{ color: "#777" }}>
                    {line}
                  </p>
                ))}
              </div>
              <p className="text-sm" style={{ color: "#777" }}>
                {contactInfo.phone}
              </p>
              <a
                href={`mailto:${contactInfo.email}`}
                className="group relative inline-flex pb-1 text-sm text-[#888] transition-colors duration-300 hover:text-[#C1121F] focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-red-600/70"
              >
                {contactInfo.email}
                <span className="absolute bottom-0 left-0 h-px w-0 bg-[#C1121F] transition-all duration-300 group-hover:w-full" />
              </a>
              <div className="space-y-1 pt-1">
                {contactInfo.schedule.map((line) => (
                  <p
                    key={line}
                    className="text-[12px] uppercase tracking-[0.16em]"
                    style={{ color: "#626262" }}
                  >
                    {line}
                  </p>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Copyright */}
        <div
          className="text-center"
          style={{
            borderTop: "1px solid #161616",
            paddingTop: "24px",
            marginTop: "40px",
            fontSize: "12px",
            color: "#333",
          }}
        >
          © 2025 4Four Fight Academy. Todos os direitos reservados.
        </div>
      </div>
    </footer>
  );
}
