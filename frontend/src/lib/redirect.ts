/**
 * Redirect utilities for safe internal navigation
 * Prevents open redirect vulnerabilities
 */

/**
 * Validates that a redirect path is safe (internal only)
 * @param path - The redirect path to validate
 * @returns true if the path is safe for redirect
 */
export function isSafeRedirectPath(path: unknown): path is string {
  if (!path || typeof path !== "string") return false;
  if (!path.startsWith("/")) return false;

  // Reject external URLs
  if (path.includes("://")) return false;
  if (path.startsWith("//")) return false;

  return true;
}

/**
 * Gets a safe redirect URL with fallback
 * @param redirect - The requested redirect path
 * @param fallback - Fallback path if redirect is unsafe
 * @returns Safe redirect URL or fallback
 */
export function getSafeRedirect(redirect: unknown, fallback: string = "/student-area"): string {
  if (isSafeRedirectPath(redirect)) {
    return redirect;
  }
  return fallback;
}
