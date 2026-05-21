import { Link } from "@tanstack/react-router";

export function Hero() {
  return (
    <section
      id="top"
      className="relative w-full overflow-hidden"
    >
      {/* Single BG image */}
      <div className="absolute inset-0">
        <img
          src="/assets/FundoTelaInicial.webp"
          alt=""
          loading="eager"
          className="absolute inset-0 w-full h-full object-cover grayscale scale-[1.08]"
          style={{ objectPosition: "58% 50%" }}
        />
        <div
          className="absolute inset-0"
          style={{
            background:
              "linear-gradient(to bottom, rgba(0,0,0,0.45) 0%, rgba(0,0,0,0.20) 30%, rgba(0,0,0,0.15) 50%, rgba(0,0,0,0.55) 100%)",
          }}
        />
      </div>
      {/* Global dark overlay */}
      <div
        className="absolute inset-0"
        style={{
          background:
            "radial-gradient(circle at 50% 42%, rgba(193,18,31,0.15), transparent 40%), radial-gradient(circle at 50% 48%, rgba(0,0,0,0.05), rgba(0,0,0,0.30) 85%)",
        }}
      />

      <div
        className="relative w-full max-w-[1040px] mx-auto px-4 sm:px-6 text-center pt-32 pb-16 sm:pt-44 sm:pb-20"
        style={{ zIndex: 10 }}
      >
        {/* Top label with side lines */}
        <div className="flex items-center justify-center gap-2 sm:gap-5 mb-4 sm:mb-8">
          <span
            className="block w-5 sm:w-12 h-px shrink-0"
            style={{ background: "rgba(193,18,31,0.8)" }}
          ></span>
          <p
            className="text-[7px] sm:text-[9px] tracking-[0.28em] sm:tracking-[0.48em] uppercase"
            style={{
              color: "#E3E3E3",
              opacity: 0.88,
              textShadow: "0 2px 12px rgba(0,0,0,0.85)",
            }}
          >
            4FOUR FIGHT ACADEMY
          </p>
          <span
            className="block w-5 sm:w-12 h-px shrink-0"
            style={{ background: "rgba(193,18,31,0.8)" }}
          ></span>
        </div>

        <h1
          className="font-display leading-[0.9] sm:leading-[0.82] w-full max-w-[90vw] sm:max-w-none mx-auto break-words"
          style={{
            letterSpacing: "0.02em",
            fontSize: "clamp(38px, 11vw, 190px)",
            opacity: 1,
            textShadow: "0 8px 36px rgba(0,0,0,0.9)",
          }}
        >
          <span className="block hero-word" style={{ animationDelay: "0.1s", color: "#F5F5F5" }}>
            TREINE O
          </span>
          <span className="block hero-word" style={{ animationDelay: "0.35s", color: "#C1121F" }}>
            INSTINTO
          </span>
        </h1>

        <p
          className="mt-4 sm:mt-10 mx-auto hero-word px-2"
          style={{
            color: "rgba(255,255,255,0.9)",
            fontSize: "clamp(13px, 3.5vw, 19px)",
            lineHeight: 1.65,
            maxWidth: "520px",
            animationDelay: "0.85s",
            opacity: 1,
            textShadow: "0 2px 16px rgba(0,0,0,0.8)",
          }}
        >
          Treino de elite com instrutores campeões.{" "}
          <strong style={{ color: "#FFFFFF" }}>Comece hoje, cancela quando quiser.</strong>
        </p>

        <div className="hero-cta-group" style={{ animationDelay: "1.05s", opacity: 1 }}>
          <Link
            to="/plans"
            className="hero-cta-btn hero-cta-btn-primary"
          >
            COMEÇAR AGORA<span className="cta-desktop-suffix">  7 DIAS GRÁTIS</span>
          </Link>
          <Link
            to="/plans"
            className="hero-cta-btn hero-cta-btn-secondary"
          >
            VER PLANOS
          </Link>
        </div>
      </div>

      {/* Stats bar — in normal flow, not absolute */}
      <div
        className="relative w-full"
        style={{
          background: "rgba(10,10,10,0.92)",
          borderTop: "1px solid #1E1E1E",
          zIndex: 10,
        }}
      >
        <div
          className="max-w-5xl mx-auto grid grid-cols-3 text-center gap-1 sm:gap-2 px-2 sm:px-4"
          style={{ padding: "16px 0 20px" }}
        >
          {[
            ["500+", "Atletas Treinados"],
            ["12+", "Anos de Excelência"],
            ["4", "Modalidades"],
          ].map(([n, l], i) => (
            <div
              key={l}
              className="min-w-0"
              style={{
                borderRight: i < 2 ? "1px solid #1E1E1E" : "none",
              }}
            >
              <div
                className="font-display truncate"
                style={{ fontSize: "clamp(18px, 5.5vw, 40px)", color: "#F5F5F5" }}
              >
                {n}
              </div>
              <div
                className="mt-0.5 sm:mt-1 px-1"
                style={{
                  fontSize: "clamp(7px, 2vw, 11px)",
                  letterSpacing: "0.15em",
                  textTransform: "uppercase",
                  color: "#555",
                  lineHeight: 1.3,
                }}
              >
                {l}
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
