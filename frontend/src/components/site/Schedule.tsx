const schedule = [
  {
    name: "JIU-JITSU",
    times: [
      ["Seg", "18:00"],
      ["Qua", "19:00"],
      ["Sex", "18:00"],
      ["Sáb", "10:00"],
    ],
  },
  {
    name: "BOXE",
    times: [
      ["Ter", "19:00"],
      ["Qui", "18:00"],
      ["Sáb", "11:00"],
    ],
  },
  {
    name: "KICKBOXING",
    times: [
      ["Seg", "20:00"],
      ["Qua", "18:00"],
      ["Sex", "19:00"],
    ],
  },
  {
    name: "FORÇA",
    times: [
      ["Ter", "18:00"],
      ["Qui", "19:00"],
      ["Sáb", "09:00"],
    ],
  },
];

export function Schedule() {
  return (
    <section id="schedule" className="section-pad px-4">
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-16 reveal">
          <h2
            className="font-display"
            style={{ fontSize: "clamp(48px, 8vw, 72px)", color: "#F5F5F5" }}
          >
            HORÁRIO DAS AULAS
          </h2>
          <p className="mt-4" style={{ fontSize: "14px", color: "#666" }}>
            Treina quando te convém. Aparece. Repete.
          </p>
          <div
            className="mx-auto mt-6"
            style={{ width: "48px", height: "2px", background: "#C1121F" }}
          />
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {schedule.map((s, i) => (
            <div
              key={s.name}
              className="reveal flex flex-col"
              style={{
                background: "#111111",
                border: "1px solid #1E1E1E",
                borderTop: "2px solid #C1121F",
                borderRadius: "4px",
                padding: "32px 28px",
                transitionDelay: `${i * 80}ms`,
                minHeight: "420px",
              }}
            >
              <h3
                className="font-display"
                style={{
                  fontSize: "28px",
                  color: "#F5F5F5",
                  paddingBottom: "16px",
                  borderBottom: "1px solid #1E1E1E",
                }}
              >
                {s.name}
              </h3>
              <ul className="flex-1 mt-2">
                {s.times.map(([day, time]) => (
                  <li
                    key={day + time}
                    className="flex justify-between items-center"
                    style={{
                      padding: "12px 0",
                      borderBottom: "1px solid #161616",
                    }}
                  >
                    <span
                      style={{
                        fontSize: "11px",
                        letterSpacing: "0.18em",
                        textTransform: "uppercase",
                        color: "#555",
                      }}
                    >
                      {day}
                    </span>
                    <span style={{ fontSize: "16px", fontWeight: 500, color: "#F5F5F5" }}>
                      {time}
                    </span>
                  </li>
                ))}
              </ul>
              <button
                className="mt-6 w-full"
                style={{
                  background: "transparent",
                  border: "1px solid #C1121F",
                  color: "#C1121F",
                  fontSize: "11px",
                  letterSpacing: "0.25em",
                  textTransform: "uppercase",
                  padding: "14px 0",
                  borderRadius: "2px",
                  fontWeight: 600,
                  transition: "all 0.25s ease",
                  cursor: "pointer",
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = "#C1121F";
                  e.currentTarget.style.color = "#FFFFFF";
                  e.currentTarget.style.boxShadow = "0 0 24px rgba(193,18,31,0.25)";
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = "transparent";
                  e.currentTarget.style.color = "#C1121F";
                  e.currentTarget.style.boxShadow = "none";
                }}
              >
                MARCAR AULA
              </button>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
