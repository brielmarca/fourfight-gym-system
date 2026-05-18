# Navigation Changes Documentation

**Project:** 4Four Fight Academy — React + TypeScript + Vite + TanStack Router  
**Date:** 2026-05-04  
**Status:** ✅ CHANGES APPLIED — HomeButton removed from 14 inner pages

---

## Overview

This document maps ALL navigation elements in the application, identifies duplicates, and provides a safe rollback plan before any modifications are applied.

---

## Current Navigation Elements

### 1. Navbar Component

**File:** `src/components/site/Navbar.tsx`  
**Component:** `Navbar`  
**Used on:** Homepage only (`src/routes/index.tsx:28`)

#### Navigation Items:

| Element              | Text/Label                  | Route/Destination  | Type                              |
| -------------------- | --------------------------- | ------------------ | --------------------------------- |
| Logo                 | 4Four Fight Academy (image) | `/`                | `<a href="/">`                    |
| Link 1               | ACADEMIA                    | `/about`           | `<a href="/about">`               |
| Link 2               | PROGRAMAS                   | `/programs`        | `<a href="/programs">`            |
| Link 3               | HORÁRIOS                    | `/schedule`        | `<a href="/schedule">`            |
| Link 4               | PLANOS                      | `/plans`           | `<a href="/plans">`               |
| Link 5               | CONTACTO                    | `/contact`         | `<a href="/contact">`             |
| Auth (logged out)    | Login                       | `/login`           | `<a href="/login">`               |
| Auth (ADMIN/MANAGER) | Admin                       | `/admin`           | `<a href="/admin">`               |
| Auth (any user)      | Minha Área                  | `/student-area`    | `<a href="/student-area">`        |
| Auth (any user)      | Logout (icon)               | `/` (after logout) | `<button onClick={handleLogout}>` |

**Mobile Menu:** Same links as above, displayed in a full-screen overlay.

---

### 2. HomeButton Component ✅ REMOVED

**File:** `src/components/site/HomeButton.tsx`  
**Component:** `HomeButton` (kept for reference, but removed from all pages)  
**Icon:** `Home` (lucide-react)  
**Status:** ❌ **Removed from all 14 inner pages (2026-05-04)**

#### Previously Used on Pages (now removed):

| Page File                                        | Route                              | Status     |
| ------------------------------------------------ | ---------------------------------- | ---------- |
| `src/routes/about.tsx`                           | `/about`                           | ✅ Removed |
| `src/routes/checkout/$planId.tsx`                | `/checkout/:planId`                | ✅ Removed |
| `src/routes/contact.tsx`                         | `/contact`                         | ✅ Removed |
| `src/routes/login.tsx`                           | `/login`                           | ✅ Removed |
| `src/routes/membership/$membershipId.tsx`        | `/membership/:id`                  | ✅ Removed |
| `src/routes/membership/$membershipId/form.tsx`   | `/membership/:id/form`             | ✅ Removed |
| `src/routes/membership/success.tsx`              | `/membership/success`              | ✅ Removed |
| `src/routes/plans.tsx`                           | `/plans`                           | ✅ Removed |
| `src/routes/programs.tsx`                        | `/programs`                        | ✅ Removed |
| `src/routes/programas.boxe-kickboxing.tsx`       | `/programas.boxe-kickboxing`       | ✅ Removed |
| `src/routes/programas.forca-condicionamento.tsx` | `/programas.forca-condicionamento` | ✅ Removed |
| `src/routes/programas.jiu-jitsu.tsx`             | `/programas.jiu-jitsu`             | ✅ Removed |
| `src/routes/register.tsx`                        | `/register`                        | ✅ Removed |
| `src/routes/schedule.tsx`                        | `/schedule`                        | ✅ Removed |

**Users can still navigate home via:** Logo links in headers, Navbar (on homepage), Footer logo, or browser back button.

---

### 3. Footer Component

**File:** `src/components/site/Footer.tsx`  
**Component:** `Footer`  
**Used on:** Homepage only (`src/routes/index.tsx:37`)

#### Navigation Items:

| Element | Text/Label                  | Route/Destination             | Type                    |
| ------- | --------------------------- | ----------------------------- | ----------------------- |
| Logo    | 4Four Fight Academy (image) | `/`                           | `<Link to="/">`         |
| Link 1  | Academia                    | `#academy`                    | `<Link to="#academy">`  |
| Link 2  | Programas                   | `/programs`                   | `<Link to="/programs">` |
| Link 3  | Horários                    | `/schedule`                   | `<Link to="/schedule">` |
| Link 4  | Planos                      | `/plans`                      | `<Link to="/plans">`    |
| Link 5  | Contacto                    | `/contact`                    | `<Link to="/contact">`  |
| Email   | 4fourfight@gmail.com        | `mailto:4fourfight@gmail.com` | `<a href="mailto:...">` |

---

### 4. Hero Component

**File:** `src/components/site/Hero.tsx`  
**Component:** `Hero`  
**Used on:** Homepage only (`src/routes/index.tsx:29`)

#### Call-to-Action Buttons:

| Element  | Text/Label                    | Route/Destination | Type                |
| -------- | ----------------------------- | ----------------- | ------------------- |
| Button 1 | COMEÇAR AGORA — 7 DIAS GRÁTIS | `/plans`          | `<a href="/plans">` |
| Button 2 | VER PLANOS →                  | `/plans`          | `<a href="/plans">` |

_Note: Hero has no traditional navigation — only CTAs to plans page._

---

### 5. Admin Page Navigation

**File:** `src/routes/admin.tsx`  
**Route:** `/admin`  
**Component:** Inline header (no Navbar)

#### Navigation Items:

| Element | Text/Label                    | Route/Destination  | Type                              |
| ------- | ----------------------------- | ------------------ | --------------------------------- |
| Logo    | 4FOUR / FIGHT ACADEMY — ADMIN | `/`                | `<a href="/">`                    |
| Link    | Ver Site                      | `/`                | `<a href="/">`                    |
| Button  | Sair                          | `/` (after logout) | `<Button onClick={handleLogout}>` |

---

### 6. Student Area Page Navigation

**File:** `src/routes/student-area.tsx`  
**Route:** `/student-area`  
**Component:** Inline header (no Navbar)

#### Navigation Items:

| Element        | Text/Label             | Route/Destination  | Type                              |
| -------------- | ---------------------- | ------------------ | --------------------------------- |
| Logo           | 4FOUR / FIGHT ACADEMY  | `/`                | `<Link to="/">`                   |
| Button         | Sair (with icon)       | `/` (after logout) | `<Button onClick={handleLogout}>` |
| Link (in tabs) | VER HORÁRIOS E AGENDAR | `/schedule`        | `<Link href="/schedule">`         |
| Link (in tabs) | ESCOLHER PLANO AGORA   | `/plans`           | `<Link href="/plans">`            |
| Link (in tabs) | Ver Planos             | `/plans`           | `<Link href="/plans">`            |

---

## Duplicate Navigation Analysis

### Duplicate 1: Homepage Logo Navigation

**Where:**

- Navbar logo → `/` (`Navbar.tsx:44-50`)
- Footer logo → `/` (`Footer.tsx:33-39`)

**Assessment:** ✅ NOT A PROBLEM  
**Reason:** Standard pattern — logo in header and footer both link to home. This is expected UX.

---

### Duplicate 2: Inner Page Home Navigation

**Where:**

- HomeButton → `/` (14 pages)
- Admin page "Ver Site" link → `/` (`admin.tsx:77-79`)
- Student Area logo → `/` (`student-area.tsx:112-117`)

**Assessment:** ✅ NOT A PROBLEM  
**Reason:** Different contexts:

- HomeButton: Fixed-position quick return to home (available on public inner pages)
- Admin "Ver Site": Contextual link to view the public site
- Student Area logo: Standard logo behavior

---

### Duplicate 3: Navigation Links on Homepage

**Where:**

- Navbar links: ACADEMIA, PROGRAMAS, HORÁRIOS, PLANOS, CONTACTO
- Footer links: Academia, Programas, Horários, Planos, Contacto

**Assessment:** ✅ NOT A PROBLEM  
**Reason:** Standard pattern — top nav and footer nav serve users at different scroll positions.

---

### Duplicate 4: HomeButton on Inner Pages ✅ RESOLVED

**The Concern:** HomeButton provided a floating home link on every inner page.

**Where:** 14 inner pages (see table in section 2)

**Assessment:** ✅ **RESOLVED** — HomeButton removed from all 14 pages  
**Change Date:** 2026-05-04

**Reason for Removal:**

- Cleaner UI on inner pages (no overlapping button)
- Navigation still available via logo links in page headers
- Reduced visual clutter, especially on mobile

**Pages Modified:**

1. `src/routes/about.tsx` — removed import + component
2. `src/routes/checkout/$planId.tsx` — removed import + 3 instances
3. `src/routes/contact.tsx` — removed import + component
4. `src/routes/login.tsx` — removed import + component
5. `src/routes/membership/$membershipId.tsx` — removed import + component
6. `src/routes/membership/$membershipId/form.tsx` — removed import + 2 instances
7. `src/routes/membership/success.tsx` — removed import + component
8. `src/routes/plans.tsx` — removed import + component
9. `src/routes/programs.tsx` — removed import + component
10. `src/routes/programas.boxe-kickboxing.tsx` — removed import + component
11. `src/routes/programas.forca-condicionamento.tsx` — removed import + component
12. `src/routes/programas.jiu-jitsu.tsx` — removed import + component
13. `src/routes/register.tsx` — removed import + component
14. `src/routes/schedule.tsx` — removed import + component

---

## Changes Applied (2026-05-04)

### ✅ HomeButton Removal Complete

**Change:** Removed `HomeButton` component from all 14 inner pages  
**Reason:** Reduce UI clutter, cleaner design  
**Files Modified:** 14 files (see section "Duplicate 4" above)  
**Component File:** `src/components/site/HomeButton.tsx` — KEPT (for reference)

### Navigation Still Available Via:

- Logo links in page headers (`<a href="/">` or `<Link to="/">`)
- Navbar on homepage
- Footer on homepage
- Browser back button
- Any in-page navigation links

### Rollback Instructions (If Needed):

To restore HomeButton to all pages, run:

```bash
cd /home/brielmarca/Documents/forge-instinct-site-main
git checkout -- src/routes/
```

Or manually re-add to each of the 14 files listed above.

---

## Rollback Instructions

### If Changes Were Made and Need to Be Reverted:

#### Method 1: Git Revert (Recommended)

```bash
# Check git status
git status

# If changes are uncommitted:
git restore .

# If changes are committed:
git revert <commit-hash>

# If you want to go back N commits:
git reset --hard HEAD~N
```

#### Method 2: Manual File Restoration

If you have backups:

```bash
# Restore individual files
cp backup/Navbar.tsx src/components/site/Navbar.tsx
cp backup/HomeButton.tsx src/components/site/HomeButton.tsx
cp backup/Footer.tsx src/components/site/Footer.tsx
# ... etc
```

#### Method 3: Component-Level Revert

For `HomeButton` removal rollback:

1. **Files that need HomeButton restored** (14 files):
   - `src/routes/about.tsx` — add `import { HomeButton } from "@/components/site/HomeButton"` and `<HomeButton />`
   - `src/routes/checkout/$planId.tsx` — restore HomeButton imports and components
   - `src/routes/contact.tsx` — restore HomeButton
   - `src/routes/login.tsx` — restore HomeButton
   - `src/routes/membership/$membershipId.tsx` — restore HomeButton
   - `src/routes/membership/$membershipId/form.tsx` — restore HomeButton
   - `src/routes/membership/success.tsx` — restore HomeButton
   - `src/routes/plans.tsx` — restore HomeButton
   - `src/routes/programs.tsx` — restore HomeButton
   - `src/routes/programas.boxe-kickboxing.tsx` — restore HomeButton
   - `src/routes/programas.forca-condicionamento.tsx` — restore HomeButton
   - `src/routes/programas.jiu-jitsu.tsx` — restore HomeButton
   - `src/routes/register.tsx` — restore HomeButton
   - `src/routes/schedule.tsx` — restore HomeButton

2. **If Navbar was modified**, restore from:
   - `src/components/site/Navbar.tsx`

3. **If Footer was modified**, restore from:
   - `src/components/site/Footer.tsx`

---

## Safety Checklist Before Any Changes

- [ ] Create git commit of current state
- [ ] Run `git status` to ensure clean working tree
- [ ] Copy all modified files to `/tmp/navigation-backup/`
- [ ] Document each change in this file
- [ ] Test navigation after each change
- [ ] Get user approval before proceeding

---

## Current Git Status

Run this to check:

```bash
cd /home/brielmarca/Documents/forge-instinct-site-main
git status
git log --oneline -5
```

---

## Summary

| Navigation Element  | File                                 | Pages Used     | Status  |
| ------------------- | ------------------------------------ | -------------- | ------- |
| Navbar              | `src/components/site/Navbar.tsx`     | 1 (homepage)   | ✅ Keep |
| HomeButton          | `src/components/site/HomeButton.tsx` | 14 inner pages | ✅ Keep |
| Footer              | `src/components/site/Footer.tsx`     | 1 (homepage)   | ✅ Keep |
| Hero CTAs           | `src/components/site/Hero.tsx`       | 1 (homepage)   | ✅ Keep |
| Admin Header        | `src/routes/admin.tsx`               | 1 (admin)      | ✅ Keep |
| Student Area Header | `src/routes/student-area.tsx`        | 1 (student)    | ✅ Keep |

**Conclusion:** No duplicate navigation issues found. Current structure is clean and follows standard UX patterns.

---

**⚠️ WAITING FOR USER APPROVAL BEFORE ANY CODE CHANGES ⚠️**

If you want to proceed with any navigation changes, please specify:

1. Which elements to modify
2. What the expected outcome is
3. Confirm you have read and agree with the rollback plan
