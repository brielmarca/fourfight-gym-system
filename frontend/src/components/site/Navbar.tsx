import { useEffect, useState } from "react";
import { Menu, X, User, LogOut } from "lucide-react";
import { Link, useNavigate } from "@tanstack/react-router";
import { useAuth } from "@/contexts/auth-context";

const links = [
  { label: "ACADEMIA", href: "/about" },
  { label: "PROGRAMAS", href: "/programs" },
  { label: "HORÁRIOS", href: "/schedule" },
  { label: "PLANOS", href: "/plans" },
  { label: "CONTACTO", href: "/contact" },
];

function Navbar() {
  const navigate = useNavigate();
  const { role, isAuthenticated, isLoading, logout } = useAuth();
  const [scrolled, setScrolled] = useState(false);
  const [open, setOpen] = useState(false);

  const getDashboardAction = () => {
    if (!isAuthenticated || !role) {
      return { to: "/login", label: "Login" };
    }

    if (role === "ADMIN" || role === "MANAGER") {
      return { to: "/admin", label: "Painel Admin" };
    }

    if (role === "PROFESSOR") {
      return { to: "/professor", label: "Área Trainer" };
    }

    return { to: "/student-area", label: "Área do Aluno" };
  };

  const dashboardAction = getDashboardAction();

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 80);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });

    return () => {
      window.removeEventListener("scroll", onScroll);
    };
  }, []);

  const handleLogout = () => {
    logout();
    void navigate({ to: "/", replace: true });
  };

  const brandLogoClass = "h-12 w-auto object-contain md:h-14";

  return (
    <header
      className={`fixed top-0 left-0 right-0 transition-all duration-300 ${
        scrolled ? "bg-[#0D0D0D]/95 backdrop-blur-xl border-b border-[#1A1A1A]" : "bg-transparent"
      }`}
      style={{ zIndex: 999 }}
    >
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 h-16 md:h-20 flex items-center justify-between">
        <Link to="/" className="flex items-center shrink-0 py-1">
          <img
            src="/assets/logo.png"
            alt="4Four Fight Academy"
            className={brandLogoClass}
          />
        </Link>

        <nav className="hidden md:flex items-center gap-9">
          {links.map((l) => (
            <Link
              key={l.href}
              to={l.href}
              className="text-[11px] tracking-[0.15em] uppercase transition-colors duration-200"
              style={{ color: "#888" }}
              onMouseEnter={(e) => (e.currentTarget.style.color = "#F5F5F5")}
              onMouseLeave={(e) => (e.currentTarget.style.color = "#888")}
            >
              {l.label}
            </Link>
          ))}
        </nav>

        <div className="hidden md:flex items-center gap-4">
          {isLoading ? (
            <span className="inline-flex items-center justify-center border border-border-subtle bg-surface-2 text-text-secondary px-5 py-2.5 text-[11px] tracking-[0.2em] uppercase font-semibold rounded-[2px] opacity-70">
              A carregar...
            </span>
          ) : isAuthenticated ? (
            <>
              <Link
                to={dashboardAction.to}
                className="flex items-center gap-2 text-[11px] tracking-[0.15em] uppercase text-primary hover:text-primary/80 transition-colors"
              >
                <User size={16} />
                {dashboardAction.label}
              </Link>
              <button
                onClick={handleLogout}
                className="text-[11px] tracking-[0.15em] uppercase text-text-secondary hover:text-destructive transition-colors"
              >
                <LogOut size={16} />
              </button>
            </>
          ) : (
            <Link
              to={dashboardAction.to}
              className="btn-red inline-flex items-center justify-center bg-primary text-primary-foreground px-5 py-2.5 text-[11px] tracking-[0.2em] uppercase font-semibold rounded-[2px]"
            >
              {dashboardAction.label}
            </Link>
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
          <Link to="/" className="flex items-center shrink-0 py-1">
            <img
              src="/assets/logo.png"
              alt="4Four Fight Academy"
              className="h-11 w-auto object-contain"
            />
          </Link>
          <button aria-label="Close menu" className="p-2 -mr-2" onClick={() => setOpen(false)}>
            <X size={26} />
          </button>
        </div>
        <nav className="flex-1 flex flex-col items-center justify-center gap-5 overflow-y-auto py-8 px-4">
          {links.map((l) => (
            <Link
              key={l.href}
              to={l.href}
              onClick={() => setOpen(false)}
              className="font-display text-[28px] sm:text-[40px] leading-none tracking-wider text-center"
            >
              {l.label}
            </Link>
          ))}
          {isLoading ? (
            <span className="mt-4 border border-border-subtle bg-surface-2 text-text-secondary px-8 py-3 text-sm tracking-[0.2em] font-bold rounded-[2px] text-center opacity-70">
              A CARREGAR...
            </span>
          ) : isAuthenticated ? (
            <>
              <Link
                to={dashboardAction.to}
                onClick={() => setOpen(false)}
                className="font-display text-[28px] sm:text-[40px] leading-none tracking-wider text-primary text-center"
              >
                {dashboardAction.label.toUpperCase()}
              </Link>
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
            <Link
              to={dashboardAction.to}
              onClick={() => setOpen(false)}
              className="btn-red mt-4 bg-primary text-primary-foreground px-8 py-3 text-sm tracking-[0.2em] font-bold rounded-[2px] text-center"
            >
              {dashboardAction.label.toUpperCase()}
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
}

export { Navbar };
