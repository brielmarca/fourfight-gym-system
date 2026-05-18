const plans = [
  {
    name: "FIGHTER BASIC",
    price: "€49",
    originalPrice: "€60",
    features: ["3 aulas por semana", "Acesso ao vestiário", "App da comunidade"],
    cta: "QUERO COMEÇAR",
    href: "/#contact",
    featured: false,
  },
  {
    name: "WARRIOR PRO",
    price: "€79",
    originalPrice: "€95",
    features: [
      "Aulas ilimitadas",
      "Todas as modalidades",
      " booking prioritário",
      "Acompanhamento de progresso",
    ],
    cta: "QUERO ESTE",
    href: "/#contact",
    featured: true,
  },
  {
    name: "CHAMPION ELITE",
    price: "€99",
    originalPrice: "€120",
    features: ["Tudo do Pro", "1-on-1 com instrutor", "Plano nutricional", "Apoio a competições"],
    cta: "QUERO ELITE",
    href: "/#contact",
    featured: false,
  },
];

export function Pricing() {
  return (
    <section id="pricing" className="section-pad px-4" style={{ background: "#0B0B0B" }}>
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-16 reveal">
          <h2
            className="font-display"
            style={{
              fontSize: "clamp(48px, 8vw, 72px)",
              color: "#F5F5F5",
            }}
          >
            ESCOLHA O TEU CAMINHO
          </h2>
          <p
            className="mt-4"
            style={{
              fontSize: "14px",
              color: "#666",
              fontStyle: "italic",
            }}
          >
            Junte-se a 500+ alunos que já transformaram o seu treino.
          </p>
          <p className="mt-2 text-sm" style={{ color: "#C1121F", fontWeight: 600 }}>
            ⭐ Garantia de 7 dias ou seu dinheiro de volta
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-6 max-w-5xl mx-auto">
          {plans.map((p, i) => (
            <div
              key={p.name}
              className="reveal relative flex flex-col"
              style={{
                background: "#111111",
                border: p.featured ? "1px solid #C1121F" : "1px solid #1E1E1E",
                borderRadius: "4px",
                padding: "40px 32px",
                transitionDelay: `${i * 100}ms`,
                boxShadow: p.featured ? "0 0 60px rgba(193,18,31,0.08)" : "none",
              }}
            >
              {p.featured && (
                <div
                  className="absolute left-1/2 -translate-x-1/2"
                  style={{
                    top: "-14px",
                    background: "#C1121F",
                    color: "#FFFFFF",
                    fontSize: "10px",
                    letterSpacing: "0.2em",
                    textTransform: "uppercase",
                    padding: "6px 16px",
                    borderRadius: "2px",
                    fontWeight: 600,
                  }}
                >
                  MAIS POPULAR
                </div>
              )}

              <div
                style={{
                  fontSize: "11px",
                  color: "#555",
                  letterSpacing: "0.25em",
                  textTransform: "uppercase",
                  marginBottom: "8px",
                }}
              >
                {p.name}
              </div>

              <div className="flex items-baseline gap-2">
                <span
                  className="font-display"
                  style={{
                    fontSize: "56px",
                    color: p.featured ? "#C1121F" : "#F5F5F5",
                    lineHeight: 1,
                  }}
                >
                  {p.price}
                </span>
                {p.originalPrice && (
                  <span style={{ fontSize: "18px", color: "#555", textDecoration: "line-through" }}>
                    {p.originalPrice}
                  </span>
                )}
                <span style={{ fontSize: "14px", color: "#555" }}>/mês</span>
              </div>

              <div style={{ height: "1px", background: "#1E1E1E", margin: "24px 0" }} />

              <ul className="flex-1 mb-8">
                {p.features.map((f) => (
                  <li
                    key={f}
                    className="flex items-center gap-3"
                    style={{
                      padding: "10px 0",
                      borderBottom: "1px solid #161616",
                      fontSize: "14px",
                      color: "#888",
                    }}
                  >
                    <span
                      style={{
                        color: p.featured ? "#C1121F" : "#555",
                        fontSize: "12px",
                      }}
                    >
                      ✓
                    </span>
                    <span>{f}</span>
                  </li>
                ))}
              </ul>

              {p.featured ? (
                <a
                  href={p.href}
                  className="btn-red w-full block text-center"
                  style={{
                    background: "#C1121F",
                    color: "#FFFFFF",
                    fontSize: "11px",
                    letterSpacing: "0.25em",
                    textTransform: "uppercase",
                    padding: "14px 0",
                    borderRadius: "2px",
                    fontWeight: 600,
                    cursor: "pointer",
                    textDecoration: "none",
                  }}
                >
                  {p.cta}
                </a>
              ) : (
                <a
                  href={p.href}
                  className="w-full block text-center"
                  style={{
                    background: "transparent",
                    border: "1px solid #2A2A2A",
                    color: "#888",
                    fontSize: "11px",
                    letterSpacing: "0.25em",
                    textTransform: "uppercase",
                    padding: "14px 0",
                    borderRadius: "2px",
                    fontWeight: 600,
                    transition: "all 0.25s ease",
                    cursor: "pointer",
                    textDecoration: "none",
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.borderColor = "#F5F5F5";
                    e.currentTarget.style.color = "#F5F5F5";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = "#2A2A2A";
                    e.currentTarget.style.color = "#888";
                  }}
                >
                  {p.cta}
                </a>
              )}
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
