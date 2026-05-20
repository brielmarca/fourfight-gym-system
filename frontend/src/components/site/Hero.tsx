export function Hero() {
  return (
    <section
      id="top"
      className="relative min-h-screen w-full overflow-hidden flex items-center justify-center"
    >
      {/* Single BG image */}
      <div className="absolute inset-0">
        <img
          src="/FundoTelaInicial.webp"
          alt=""
          loading="eager"
          className="absolute inset-0 w-full h-full object-cover grayscale scale-[1.08]"
          style={{ objectPosition: "58% 50%" }}
        />
        <div
          className="absolute inset-0"
          style={{
            background:
              "linear-gradient(to bottom, rgba(0,0,0,0.86) 0%, rgba(0,0,0,0.58) 34%, rgba(0,0,0,0.46) 58%, rgba(0,0,0,0.88) 100%)",
          }}
        />
      </div>
      {/* Global dark overlay */}
      <div
        className="absolute inset-0"
        style={{
          background:
            "radial-gradient(circle at 50% 42%, rgba(193,18,31,0.12), transparent 32%), radial-gradient(circle at 50% 48%, rgba(0,0,0,0.1), rgba(0,0,0,0.62) 82%)",
        }}
      />

      <div
        className="relative px-4 text-center max-w-[1040px] mx-auto pt-32 pb-32 sm:pt-36 sm:pb-36"
        style={{ zIndex: 10 }}
      >
        {/* Top label with side lines */}
        <div className="flex items-center justify-center gap-5 mb-7 sm:mb-8">
          <span
            className="block w-8 sm:w-12 h-px"
            style={{ background: "rgba(193,18,31,0.8)" }}
          ></span>
          <p
            className="text-[9px] tracking-[0.48em] uppercase"
            style={{
              color: "#E3E3E3",
              opacity: 0.88,
              textShadow: "0 2px 12px rgba(0,0,0,0.85)",
            }}
          >
            4FOUR FIGHT ACADEMY
          </p>
          <span
            className="block w-8 sm:w-12 h-px"
            style={{ background: "rgba(193,18,31,0.8)" }}
          ></span>
        </div>

        <h1
          className="font-display"
          style={{
            lineHeight: 0.82,
            letterSpacing: "0.02em",
            fontSize: "clamp(70px, 15vw, 190px)",
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
          className="mt-10 mx-auto hero-word"
          style={{
            color: "rgba(255,255,255,0.9)",
            fontSize: "clamp(16px, 1.6vw, 19px)",
            lineHeight: 1.9,
            maxWidth: "560px",
            animationDelay: "0.85s",
            opacity: 1,
            textShadow: "0 2px 16px rgba(0,0,0,0.8)",
          }}
        >
          Treino de elite com instrutores campeões.{" "}
          <strong style={{ color: "#FFFFFF" }}>Começe hoje, cancela quando quiser.</strong>
        </p>

        <div
          className="mt-12 flex flex-col sm:flex-row items-center justify-center gap-x-8 gap-y-4 hero-word"
          style={{ animationDelay: "1.05s" }}
        >
          <a
            href="/plans"
            className="w-full sm:w-auto bg-gradient-to-r from-red-600 to-red-500 text-white px-8 py-4 text-[12px] tracking-[0.25em] uppercase font-semibold rounded-2xl border border-red-400/40 transition-all duration-300 hover:scale-105 hover:shadow-[0_0_40px_rgba(255,0,0,0.4)] shadow-[0_0_25px_rgba(255,0,0,0.25)]"
          >
            COMEÇAR AGORA — 7 DIAS GRÁTIS
          </a>
          <a
            href="/plans"
            className="w-full sm:w-auto px-8 py-4 text-[12px] tracking-[0.25em] uppercase font-semibold rounded-2xl border border-white/40 text-white bg-black/40 transition-all duration-300 hover:scale-105 hover:border-red-400/60 hover:bg-red-600/20 hover:shadow-[0_0_25px_rgba(255,0,0,0.25)]"
          >
            VER PLANOS →
          </a>
        </div>
      </div>

      {/* Stats bar */}
      <div
        className="absolute bottom-0 left-0 right-0"
        style={{
          background: "rgba(10,10,10,0.85)",
          borderTop: "1px solid #1E1E1E",
          zIndex: 10,
        }}
      >
        <div
          className="max-w-5xl mx-auto grid grid-cols-3 text-center gap-2 px-2"
          style={{ padding: "28px 0" }}
        >
          {[
            ["500+", "Atletas Treinados"],
            ["12+", "Anos de Excelência"],
            ["3", "Modalidades"],
          ].map(([n, l], i) => (
            <div
              key={l}
              style={{
                borderRight: i < 2 ? "1px solid #1E1E1E" : "none",
              }}
            >
              <div
                className="font-display"
                style={{ fontSize: "clamp(28px, 5vw, 40px)", color: "#F5F5F5" }}
              >
                {n}
              </div>
              <div
                className="mt-1"
                style={{
                  fontSize: "clamp(9px, 1.4vw, 11px)",
                  letterSpacing: "0.25em",
                  textTransform: "uppercase",
                  color: "#555",
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
