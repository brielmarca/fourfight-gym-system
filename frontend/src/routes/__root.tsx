import { Outlet, Link, createRootRoute, useLocation } from "@tanstack/react-router";

import { HomeButton } from "../components/site/HomeButton";
import { Footer } from "../components/site/Footer";
import { AuthProvider, useAuth } from "../contexts/auth-context";
import { QueryProvider } from "../providers/query-provider";

const publicFooterRoutes = new Set(["/", "/about", "/programs", "/schedule", "/plans", "/contact"]);

function shouldShowFooter(pathname: string) {
  const normalizedPath = pathname.length > 1 ? pathname.replace(/\/$/, "") : pathname;
  return publicFooterRoutes.has(normalizedPath) || normalizedPath.startsWith("/programas/");
}

function NotFoundComponent() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="max-w-md text-center">
        <h1 className="text-7xl font-bold text-foreground">404</h1>
        <h2 className="mt-4 text-xl font-semibold text-foreground">Página não encontrada</h2>
        <p className="mt-2 text-sm text-muted-foreground">
          A página que procuras não existe ou foi movida.
        </p>
        <div className="mt-6">
          <Link
            to="/"
            className="inline-flex items-center justify-center rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
          >
            Ir para a página inicial
          </Link>
        </div>
      </div>
    </div>
  );
}

export const Route = createRootRoute({
  component: RootComponent,
  notFoundComponent: NotFoundComponent,
});

  function RootLayoutContent() {
    const location = useLocation();
    const isHomePage = location.pathname === "/";
    const showFooter = shouldShowFooter(location.pathname);
    const { isLoading } = useAuth();

    // Block route rendering while auth is restoring
    // Show minimal loading state (no navigation, no content)
    if (isLoading) {
      return (
        <div className="flex h-screen items-center justify-center bg-neutral-950">
          {/* Minimal loading UI - no navigation, no content */}
        </div>
      );
    }

    return (
      <div className="overflow-x-hidden">
        {!isHomePage && <HomeButton />}
        <Outlet />
        {showFooter && <Footer />}
      </div>
    );
  }

  function RootComponent() {
    return (
      <QueryProvider>
        <AuthProvider>
          <RootLayoutContent />
        </AuthProvider>
      </QueryProvider>
    );
  }
