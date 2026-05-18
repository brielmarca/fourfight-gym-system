import { Link } from "@tanstack/react-router";
import { Home } from "lucide-react";

export function HomeButton() {
  return (
    <Link
      to="/"
      aria-label="Voltar à página inicial"
      className="fixed left-4 top-4 z-[60] inline-flex h-10 w-10 items-center justify-center rounded-full border border-white/10 bg-black/55 text-white shadow-[0_8px_24px_rgba(0,0,0,0.32)] backdrop-blur-md transition-all duration-300 hover:scale-105 hover:border-red-500/70 hover:shadow-[0_0_22px_rgba(193,18,31,0.32)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500/70 focus-visible:ring-offset-2 focus-visible:ring-offset-background"
    >
      <Home size={18} strokeWidth={1.8} aria-hidden="true" />
    </Link>
  );
}
