import { useEffect, useState } from "react";
import { Menu, X, User, LogOut } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";

const links = [
  { label: "ACADEMIA", href: "/about" },
  { label: "PROGRAMAS", href: "/programs" },
  { label: "HORÁRIOS", href: "/schedule" },
  { label: "PLANOS", href: "/plans" },
  { label: "CONTACTO", href: "/contact" },
];

function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const [scrolled, setScrolled] = useState(false);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 80);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
  }, []);

  const handleLogout = () => {
    logout();
    window.location.href = "/";
  };

  return (
    <header
      className={`fixed top-0 left-0 right-0 transition-all duration-300 ${
        scrolled ? "bg-[#0D0D0D]/95 backdrop-blur-xl border-b border-[#1A1A1A]" : "bg-transparent"
      }`}
      style={{ zIndex: 999 }}
    >
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 h-16 md:h-20 flex items-center justify-between">
        <a href="/" className="flex items-center shrink-0">
          <img
            src="/assets/logo.png"
            alt="4Four Fight Academy"
            style={{
              height: "34px",
              width: "auto",
              mixBlendMode: "screen",
              filter: "brightness(1.1)",
            }}
            className="md:h-[40px]"
          />
        </a>

        <nav className="hidden md:flex items-center gap-9">
          {links.map((l) => (
            <a
              key={l.href}
              href={l.href}
              className="text-[11px] tracking-[0.15em] uppercase transition-colors duration-200"
              style={{ color: "#888" }}
              onMouseEnter={(e) => (e.currentTarget.style.color = "#F5F5F5")}
              onMouseLeave={(e) => (e.currentTarget.style.color = "#888")}
            >
              {l.label}
            </a>
          ))}
        </nav>

        <div className="hidden md:flex items-center gap-4">
          {isAuthenticated && user ? (
            <>
              {user.role === "ADMIN" || user.role === "MANAGER" ? (
                <a
                  href="/admin"
                  className="text-[11px] tracking-[0.15em] uppercase text-text-secondary hover:text-foreground transition-colors"
                >
                  Admin
                </a>
              ) : null}
              <a
                href="/student-area"
                className="flex items-center gap-2 text-[11px] tracking-[0.15em] uppercase text-primary hover:text-primary/80 transition-colors"
              >
                <User size={16} />
                Minha Área
              </a>
              <button
                onClick={handleLogout}
                className="text-[11px] tracking-[0.15em] uppercase text-text-secondary hover:text-destructive transition-colors"
              >
                <LogOut size={16} />
              </button>
            </>
          ) : (
            <a
              href="/login"
              className="btn-red inline-flex items-center justify-center bg-primary text-primary-foreground px-5 py-2.5 text-[11px] tracking-[0.2em] uppercase font-semibold rounded-[2px]"
            >
              Login
            </a>
          )}
        </div>

        <button
          aria-label="Open menu"
          className="md:hidden text-foreground shrink-0 p-2 -mr-2"
          onClick={() => setOpen(true)}
        >
          <Menu size={24} />
        </button>
      </div>

      <div
        className={`fixed inset-0 bg-[#0B0B0B] flex flex-col transition-all duration-300 md:hidden ${
          open ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"
        }`}
        style={{ zIndex: 1000 }}
      >
        <div className="h-16 px-4 sm:px-6 flex items-center justify-between border-b border-[#1A1A1A]">
          <a href="/" className="flex items-center shrink-0">
            <img
              src="/assets/logo.png"
              alt="4Four Fight Academy"
              style={{
                height: "34px",
                width: "auto",
                mixBlendMode: "screen",
                filter: "brightness(1.1)",
              }}
            />
          </a>
          <button aria-label="Close menu" className="p-2 -mr-2" onClick={() => setOpen(false)}>
            <X size={26} />
          </button>
        </div>
        <nav className="flex-1 flex flex-col items-center justify-center gap-5 overflow-y-auto py-8 px-4">
          {links.map((l) => (
            <a
              key={l.href}
              href={l.href}
              onClick={() => setOpen(false)}
              className="font-display text-[28px] sm:text-[40px] leading-none tracking-wider text-center"
            >
              {l.label}
            </a>
          ))}
          {isAuthenticated && user ? (
            <>
              {(user.role === "ADMIN" || user.role === "MANAGER") && (
                <a
                  href="/admin"
                  onClick={() => setOpen(false)}
                  className="font-display text-[28px] sm:text-[40px] leading-none tracking-wider text-primary text-center"
                >
                  ADMIN
                </a>
              )}
              <a
                href="/student-area"
                onClick={() => setOpen(false)}
                className="font-display text-[28px] sm:text-[40px] leading-none tracking-wider text-center"
              >
                MINHA ÁREA
              </a>
              <button
                onClick={() => {
                  handleLogout();
                  setOpen(false);
                }}
                className="font-display text-[28px] sm:text-[40px] leading-none tracking-wider text-destructive text-center"
              >
                SAIR
              </button>
            </>
          ) : (
            <a
              href="/login"
              onClick={() => setOpen(false)}
              className="btn-red mt-4 bg-primary text-primary-foreground px-8 py-3 text-sm tracking-[0.2em] font-bold rounded-[2px] text-center"
            >
              LOGIN
            </a>
          )}
        </nav>
      </div>
    </header>
  );
}

export { Navbar };
