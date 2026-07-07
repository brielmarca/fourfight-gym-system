export const COOKIE_CONSENT_STORAGE_KEY = "fourfight_cookie_consent_v1";

export type CookieConsent = {
  version: 1;
  necessary: true;
  analytics: boolean;
  updatedAt: string;
};

function isValidCookieConsent(value: unknown): value is CookieConsent {
  if (!value || typeof value !== "object") {
    return false;
  }

  const consent = value as Partial<CookieConsent>;

  return (
    consent.version === 1 &&
    consent.necessary === true &&
    typeof consent.analytics === "boolean" &&
    typeof consent.updatedAt === "string"
  );
}

export function getCookieConsent(): CookieConsent | null {
  if (typeof window === "undefined") {
    return null;
  }

  const raw = window.localStorage.getItem(COOKIE_CONSENT_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed: unknown = JSON.parse(raw);
    if (!isValidCookieConsent(parsed)) {
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

export function saveCookieConsent(
  consent: Omit<CookieConsent, "updatedAt"> & { updatedAt?: string },
): CookieConsent {
  const normalized: CookieConsent = {
    version: 1,
    necessary: true,
    analytics: consent.analytics,
    updatedAt: consent.updatedAt ?? new Date().toISOString(),
  };

  if (typeof window !== "undefined") {
    window.localStorage.setItem(COOKIE_CONSENT_STORAGE_KEY, JSON.stringify(normalized));
  }

  return normalized;
}

export function clearCookieConsent() {
  if (typeof window !== "undefined") {
    window.localStorage.removeItem(COOKIE_CONSENT_STORAGE_KEY);
  }
}

export function hasAnalyticsConsent(): boolean {
  const consent = getCookieConsent();
  return consent?.analytics === true;
}
