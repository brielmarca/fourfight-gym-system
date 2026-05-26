import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { RouterProvider, createRouter } from "@tanstack/react-router";
import { routeTree } from "./routeTree.gen";
import { restoreAuthSession } from "@/lib/api";
import "./styles.css";

const router = createRouter({
  routeTree,
  context: {},
  scrollRestoration: true,
  defaultPreloadStaleTime: 0,
});

declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}

const rootElement = document.getElementById("root");

const protectedPrefixes = ["/admin", "/student-area", "/checkout", "/membership"];

function shouldBlockForAuthRestore(pathname: string): boolean {
  return protectedPrefixes.some((prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`));
}

function registerServiceWorker() {
  if (import.meta.env.PROD && typeof window !== "undefined" && "serviceWorker" in navigator) {
    void navigator.serviceWorker.register("/sw.js").catch(() => undefined);
  }
}

async function bootstrap() {
  if (typeof window !== "undefined" && shouldBlockForAuthRestore(window.location.pathname)) {
    await Promise.race([
      restoreAuthSession(),
      new Promise<boolean>((resolve) => {
        setTimeout(() => resolve(false), 4000);
      }),
    ]).catch(() => false);
  }

  if (rootElement) {
    createRoot(rootElement).render(
      <StrictMode>
        <RouterProvider router={router} />
      </StrictMode>,
    );

    registerServiceWorker();
  }
}

void bootstrap();
