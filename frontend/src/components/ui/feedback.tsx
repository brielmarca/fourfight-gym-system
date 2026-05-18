import { cn } from "@/lib/utils";
import { X, Check, Loader2, AlertTriangle } from "lucide-react";

interface FeedbackProps {
  type: "error" | "success" | "loading" | "info";
  message: string;
  action?: {
    label: string;
    onClick: () => void;
  };
  className?: string;
}

export function Feedback({ type, message, action, className }: FeedbackProps) {
  const styles = {
    error: "bg-destructive/10 border-destructive/30 text-destructive",
    success: "bg-green-500/10 border-green-500/30 text-green-600",
    loading: "bg-primary/10 border-primary/30 text-primary",
    info: "bg-blue-500/10 border-blue-500/30 text-blue-600",
  };

  const icons = {
    error: X,
    success: Check,
    loading: Loader2,
    info: AlertTriangle,
  };

  const Icon = icons[type];

  return (
    <div
      className={cn(
        "flex items-start gap-3 px-4 py-3 text-sm rounded-md border",
        styles[type],
        className,
      )}
    >
      <Icon
        size={16}
        className={type === "loading" ? "animate-spin shrink-0 mt-0.5" : "shrink-0 mt-0.5"}
      />
      <span className="flex-1">{message}</span>
      {action && (
        <button
          onClick={action.onClick}
          className="underline font-semibold shrink-0 hover:no-underline"
        >
          {action.label}
        </button>
      )}
    </div>
  );
}

export function LoadingCard({ message = "Carregando..." }: { message?: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 space-y-4">
      <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      <p className="text-sm text-muted-foreground">{message}</p>
    </div>
  );
}

export function EmptyState({
  icon: Icon = AlertTriangle,
  title = "Nada aqui",
  description,
  action,
}: {
  icon?: React.ComponentType<{ size?: number; className?: string }>;
  title?: string;
  description?: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="text-center py-20 space-y-4">
      <div className="flex justify-center">
        <Icon size={48} className="text-muted-foreground/50" />
      </div>
      <h3 className="font-semibold text-lg">{title}</h3>
      {description && (
        <p className="text-sm text-muted-foreground max-w-md mx-auto">{description}</p>
      )}
      {action && <div className="pt-2">{action}</div>}
    </div>
  );
}
