import { useEffect, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { X, Clock, Award, Calendar, Zap } from "lucide-react";
import type { LucideIcon } from "lucide-react";

interface ProgramData {
  icon: LucideIcon;
  title: string;
  desc: string;
  details: string[];
  level: string;
  duration: string;
  schedule?: string;
}

interface ProgramModalProps {
  program: ProgramData | null;
  isOpen: boolean;
  onClose: () => void;
}

export function ProgramModal({ program, isOpen, onClose }: ProgramModalProps) {
  const handleEscape = useCallback(
    (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    },
    [onClose],
  );

  useEffect(() => {
    if (isOpen) {
      document.addEventListener("keydown", handleEscape);
      document.body.style.overflow = "hidden";
    }
    return () => {
      document.removeEventListener("keydown", handleEscape);
      document.body.style.overflow = "unset";
    };
  }, [isOpen, handleEscape]);

  if (!program) return null;

  const Icon = program.icon;

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.25, ease: "easeOut" }}
            className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm"
            onClick={onClose}
          />

          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              transition={{ duration: 0.25, ease: "easeOut" }}
              className="relative w-full max-w-2xl max-h-[85vh] overflow-y-auto bg-[#0f0f0f] border border-[#2A2A2A] rounded-2xl shadow-[0_20px_60px_rgba(0,0,0,0.5)]"
              onClick={(e) => e.stopPropagation()}
            >
              <button
                onClick={onClose}
                className="absolute top-4 right-4 p-2 rounded-full bg-[#1E1E1E] hover:bg-[#2A2A2A] transition-colors z-10 group"
                aria-label="Close modal"
              >
                <X
                  size={18}
                  className="text-text-secondary group-hover:text-foreground transition-colors"
                />
              </button>

              <div className="p-8 md:p-10">
                <div className="flex items-center gap-4 mb-6">
                  <div className="p-3 rounded-xl bg-[#C1121F]/10">
                    <Icon size={32} strokeWidth={1.5} className="text-[#C1121F]" />
                  </div>
                  <div>
                    <h2 className="font-display text-3xl md:text-4xl tracking-wider text-foreground">
                      {program.title}
                    </h2>
                    <p className="text-text-secondary text-sm mt-1">{program.desc}</p>
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-4 mb-8">
                  <div className="bg-[#111111] rounded-xl p-4 border border-[#1E1E1E]">
                    <div className="flex items-center gap-2 text-text-secondary text-xs uppercase tracking-wider mb-2">
                      <Award size={14} />
                      Nível
                    </div>
                    <p className="text-foreground text-sm font-semibold">{program.level}</p>
                  </div>
                  <div className="bg-[#111111] rounded-xl p-4 border border-[#1E1E1E]">
                    <div className="flex items-center gap-2 text-text-secondary text-xs uppercase tracking-wider mb-2">
                      <Clock size={14} />
                      Duração
                    </div>
                    <p className="text-foreground text-sm font-semibold">{program.duration}</p>
                  </div>
                  <div className="bg-[#111111] rounded-xl p-4 border border-[#1E1E1E]">
                    <div className="flex items-center gap-2 text-text-secondary text-xs uppercase tracking-wider mb-2">
                      <Zap size={14} />
                      Modalidade
                    </div>
                    <p className="text-foreground text-sm font-semibold">Elite</p>
                  </div>
                </div>

                <div className="mb-8">
                  <h3 className="font-display text-lg tracking-wider text-foreground mb-4">
                    BENEFÍCIOS DO PROGRAMA
                  </h3>
                  <ul className="space-y-3">
                    {program.details.map((detail, idx) => (
                      <li key={idx} className="flex items-center gap-3 text-text-secondary">
                        <span className="text-[#C1121F] text-lg">✓</span>
                        <span className="text-sm">{detail}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                {program.schedule && (
                  <div className="mb-8 bg-[#111111] rounded-xl p-6 border border-[#1E1E1E]">
                    <div className="flex items-center gap-2 mb-3">
                      <Calendar size={16} className="text-[#C1121F]" />
                      <h4 className="font-display text-sm tracking-wider uppercase text-foreground">
                        Horários
                      </h4>
                    </div>
                    <p className="text-text-secondary text-sm">{program.schedule}</p>
                  </div>
                )}

                <div className="flex flex-col sm:flex-row gap-4 pt-4 border-t border-[#1E1E1E]">
                  <button
                    className="flex-1 bg-transparent border border-[#2A2A2A] hover:border-[#C1121F] text-foreground font-semibold text-xs tracking-[0.2em] uppercase py-3 px-6 rounded-lg transition-all duration-200 hover:scale-[1.02]"
                    onClick={onClose}
                  >
                    Escolher Plano
                  </button>
                </div>
              </div>
            </motion.div>
          </div>
        </>
      )}
    </AnimatePresence>
  );
}
