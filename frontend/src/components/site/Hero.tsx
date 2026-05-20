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
        className="relative w-full max-w-[1040px] mx-auto px-4 sm:px-6 text-center pt-20 pb-36 sm:pt-36 sm:pb-44"
        style={{ zIndex: 10 }}
      >
        {/* Top label with side lines */}
        <div className="flex items-center justify-center gap-2 sm:gap-5 mb-5 sm:mb-8">
          <span
            className="block w-5 sm:w-12 h-px shrink-0"
            style={{ background: "rgba(193,18,31,0.8)" }}
          ></span>
          <p
            className="text-[7px] sm:text-[9px] tracking-[0.3em] sm:tracking-[0.48em] uppercase"
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
          className="font-display leading-[0.85] sm:leading-[0.82] w-full break-words"
          style={{
            letterSpacing: "0.02em",
            fontSize: "clamp(32px, 9vw, 190px)",
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
          className="mt-5 sm:mt-10 mx-auto hero-word px-2"
          style={{
            color: "rgba(255,255,255,0.9)",
            fontSize: "clamp(13px, 3.5vw, 19px)",
            lineHeight: 1.7,
            maxWidth: "560px",
            animationDelay: "0.85s",
            opacity: 1,
            textShadow: "0 2px 16px rgba(0,0,0,0.8)",
          }}
        >
          Treino de elite com instrutores campees.{" "}
          <strong style={{ color: "#FFFFFF" }}>Comece hoje, cancela quando quiser.</strong>
        </p>

        <div
          className="mt-7 sm:mt-12 flex flex-col sm:flex-row items-stretch sm:items-center justify-center gap-3 sm:gap-x-8 sm:gap-y-4 hero-word w-full max-w-md mx-auto px-2"
          style={{ animationDelay: "1.05s" }}
        >
          <a
            href="/plans"
            className="w-full text-center whitespace-normal leading-tight bg-gradient-to-r from-red-600 to-red-500 text-white px-6 sm:px-8 py-3.5 sm:py-4 text-[11px] sm:text-[12px] tracking-[0.2em] sm:tracking-[0.25em] uppercase font-semibold rounded-2xl border border-red-400/40 transition-all duration-300 hover:scale-105 hover:shadow-[0_0_40px_rgba(255,0,0,0.4)] shadow-[0_0_25px_rgba(255,0,0,0.25)]"
          >
            COMEAR AGORA  7 DIAS GRTIS
          </a>
          <a
            href="/plans"
            className="w-full text-center whitespace-normal leading-tight px-6 sm:px-8 py-3.5 sm:py-4 text-[11px] sm:text-[12px] tracking-[0.2em] sm:tracking-[0.25em] uppercase font-semibold rounded-2xl border border-white/40 text-white bg-black/40 transition-all duration-300 hover:scale-105 hover:border-red-400/60 hover:bg-red-600/20 hover:shadow-[0_0_25px_rgba(255,0,0,0.25)]"
          >
            VER PLANOS
          </a>
        </div>
      </div>

      {/* Stats bar */}
      <div
        className="absolute bottom-0 left-0 right-0 w-full"
        style={{
          background: "rgba(10,10,10,0.85)",
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
            ["12+", "Anos de Excelncia"],
            ["3", "Modalidades"],
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
