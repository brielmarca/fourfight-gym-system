import { useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { getCookieConsent, saveCookieConsent, type CookieConsent } from "@/lib/cookie-consent";
import { disableGoogleAnalytics, loadGoogleAnalytics } from "@/lib/analytics";
import { OPEN_PREFERENCES_EVENT } from "@/components/privacy/open-cookie-preferences";

export function CookieConsentBanner() {
  const [isVisible, setIsVisible] = useState(false);
  const [isPreferencesOpen, setIsPreferencesOpen] = useState(false);
  const [analyticsEnabled, setAnalyticsEnabled] = useState(false);

  useEffect(() => {
    const consent = getCookieConsent();

    if (!consent) {
      setIsVisible(true);
      setAnalyticsEnabled(false);
      disableGoogleAnalytics();
      return;
    }

    setAnalyticsEnabled(consent.analytics);
    if (consent.analytics) {
      loadGoogleAnalytics();
    } else {
      disableGoogleAnalytics();
    }
  }, []);

  useEffect(() => {
    const handleOpenPreferences = () => {
      const consent = getCookieConsent();
      setAnalyticsEnabled(consent?.analytics ?? false);
      setIsVisible(true);
      setIsPreferencesOpen(true);
    };

    window.addEventListener(OPEN_PREFERENCES_EVENT, handleOpenPreferences);
    return () => {
      window.removeEventListener(OPEN_PREFERENCES_EVENT, handleOpenPreferences);
    };
  }, []);

  const applyConsent = (analytics: boolean) => {
    const consent: Omit<CookieConsent, "updatedAt"> = {
      version: 1,
      necessary: true,
      analytics,
    };

    saveCookieConsent(consent);

    if (analytics) {
      loadGoogleAnalytics();
    } else {
      disableGoogleAnalytics();
    }

    setAnalyticsEnabled(analytics);
    setIsPreferencesOpen(false);
    setIsVisible(false);
  };

  if (!isVisible) {
    return null;
  }

  return (
    <div className="fixed inset-x-0 bottom-0 z-[1100] px-4 pb-4 sm:px-6">
      <div className="mx-auto w-full max-w-4xl rounded-xl border border-red-900/60 bg-[#0C0C0C] p-4 text-white shadow-2xl shadow-black/40 sm:p-6">
        <p className="text-sm leading-6 text-zinc-200">
          Usamos cookies para garantir o funcionamento do site, melhorar a segurança e, apenas com o
          teu consentimento, analisar a navegação para melhorar a experiência. Podes aceitar,
          rejeitar ou personalizar as tuas preferências.
        </p>

        <div className="mt-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-end">
          <Button
            type="button"
            variant="outline"
            className="border-zinc-600 bg-transparent text-zinc-100 hover:bg-zinc-800"
            onClick={() => applyConsent(false)}
          >
            Rejeitar todos
          </Button>
          <Button
            type="button"
            variant="outline"
            className="border-zinc-600 bg-transparent text-zinc-100 hover:bg-zinc-800"
            onClick={() => setIsPreferencesOpen((current) => !current)}
          >
            Personalizar
          </Button>
          <Button
            type="button"
            className="bg-[#C1121F] text-white hover:bg-[#A30F1A]"
            onClick={() => applyConsent(true)}
          >
            Aceitar todos
          </Button>
        </div>

        {isPreferencesOpen && (
          <div
            className="mt-4 rounded-lg border border-zinc-800 bg-[#121212] p-4"
            role="dialog"
            aria-label="Preferências de cookies"
          >
            <div className="space-y-4">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="text-sm font-semibold text-zinc-100">
                    Cookies estritamente necessários
                  </p>
                  <p className="text-xs text-zinc-400">
                    Essenciais para segurança, autenticação e funcionamento do site.
                  </p>
                </div>
                <Switch checked disabled aria-label="Cookies estritamente necessários" />
              </div>

              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="text-sm font-semibold text-zinc-100">Cookies analíticos</p>
                  <p className="text-xs text-zinc-400">
                    Ajudam a melhorar conteúdos e experiência de navegação.
                  </p>
                </div>
                <Switch
                  checked={analyticsEnabled}
                  onCheckedChange={setAnalyticsEnabled}
                  aria-label="Ativar cookies analíticos"
                />
              </div>

              <div className="flex flex-col gap-2 pt-2 sm:flex-row sm:justify-end">
                <Button
                  type="button"
                  variant="outline"
                  className="border-zinc-600 bg-transparent text-zinc-100 hover:bg-zinc-800"
                  onClick={() => applyConsent(false)}
                >
                  Rejeitar todos
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  className="border-zinc-600 bg-transparent text-zinc-100 hover:bg-zinc-800"
                  onClick={() => applyConsent(true)}
                >
                  Aceitar todos
                </Button>
                <Button
                  type="button"
                  className="bg-[#C1121F] text-white hover:bg-[#A30F1A]"
                  onClick={() => applyConsent(analyticsEnabled)}
                >
                  Guardar preferências
                </Button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
