export const OPEN_PREFERENCES_EVENT = "fourfight:open-cookie-preferences";

export function openCookiePreferences() {
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event(OPEN_PREFERENCES_EVENT));
  }
}
