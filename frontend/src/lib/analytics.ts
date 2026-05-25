const analyticsScriptId = "fourfight-ga-script";

declare global {
  interface Window {
    dataLayer?: unknown[];
    gtag?: (...args: unknown[]) => void;
    [key: `ga-disable-${string}`]: boolean | undefined;
  }
}

function getGaMeasurementId(): string | null {
  const id = import.meta.env.VITE_GA_MEASUREMENT_ID;
  if (!id || typeof id !== "string") {
    return null;
  }

  const trimmed = id.trim();
  return trimmed.length > 0 ? trimmed : null;
}

export function disableGoogleAnalytics() {
  const gaId = getGaMeasurementId();
  if (!gaId || typeof window === "undefined") {
    return;
  }

  window[`ga-disable-${gaId}`] = true;
}

export function loadGoogleAnalytics() {
  const gaId = getGaMeasurementId();
  if (!gaId || typeof window === "undefined" || typeof document === "undefined") {
    return;
  }

  window[`ga-disable-${gaId}`] = false;

  if (document.getElementById(analyticsScriptId)) {
    return;
  }

  window.dataLayer = window.dataLayer || [];
  window.gtag = function gtag(...args: unknown[]) {
    window.dataLayer?.push(args);
  };
  window.gtag("js", new Date());
  window.gtag("config", gaId);

  const script = document.createElement("script");
  script.id = analyticsScriptId;
  script.async = true;
  script.src = `https://www.googletagmanager.com/gtag/js?id=${encodeURIComponent(gaId)}`;
  document.head.appendChild(script);
}
