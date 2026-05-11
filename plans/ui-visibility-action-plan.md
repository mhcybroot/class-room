# UI Visibility Issues Action Plan - ClassRoom Application

## Executive Summary

Critical visibility problems identified where buttons with white backgrounds blend into white surface backgrounds, making them invisible or nearly invisible. This affects the `.secondary-btn` class used throughout the application.

---

## Priority 1: Critical Button Visibility Issues

### Issue 1.1: Secondary Button Blending Into Surface Cards

**Location:** All views using `.secondary-btn` on `.surface-card` backgrounds

**Root Cause:**
```css
/* styles.css lines 424-428 */
.secondary-btn {
  background: #fff;           /* White background */
  color: var(--app-primary); /* Blue text only */
  border: 1px solid var(--app-primary);
}
```

When `.secondary-btn` is placed on `.surface-card` (which also has white background `#ffffff`), the button becomes nearly invisible because:
1. Both have `background: #fff`
2. Only border and text provide visual distinction
3. Thin 1px border on white surface card with white background creates minimal contrast

**Affected Locations:**

| View | Usage | Problem Severity |
|------|-------|-----------------|
| RoomsView.java:128 | Invite Details button | HIGH |
| LiveRoomView.java:179 | Join Live Class button | HIGH |
| LiveRoomView.java:219 | Share Screen button | HIGH |
| LiveRoomView.java:240 | Leave Room button | MEDIUM |
| ClassesView.java:50 | Create Class button | HIGH |
| AboutView.java:89 | Teacher login link | MEDIUM |

**Remediation Steps:**

Change `.secondary-btn` background from `#fff` to `--app-surface-muted` (#f8fafc):
```css
.secondary-btn {
  background: var(--app-surface-muted);
  color: var(--app-primary);
  border: 1px solid var(--app-primary);
}
```

---

### Issue 1.2: Ghost Button Visibility

**Location:** LiveRoomView control bar buttons

**Root Cause:**
```css
.ghost-btn {
  background: var(--app-primary-soft);
  color: var(--app-primary);
  border: 1px solid #bfd0e3;
}
```

Light blue background may blend with surface cards in certain contexts.

**Remediation Steps:**
```css
.ghost-btn {
  background: var(--app-primary-soft);
  color: var(--app-primary);
  border: 1px solid var(--app-primary);
}
```

---

### Issue 1.3: Danger Button Contrast

**Location:** ClassesView.java:96 - Delete Class button

**Remediation Steps:**
```css
.danger-btn {
  background: #fee4e2;
  color: var(--app-danger);
  border: 1px solid var(--app-danger);
}
```

---

## Priority 2: Input Field Visibility

### Issue 2.1: Text Fields on White Background

**Location:** StudentJoinView, LoginView form fields

**Root Cause:**
```css
vaadin-text-field::part(input-field),
vaadin-password-field::part(input-field) {
  background: #fff;
  border: 1px solid var(--app-border);
}
```

**Remediation Steps:**
```css
vaadin-text-field::part(input-field),
vaadin-password-field::part(input-field) {
  background: #fff;
  border: 1px solid var(--app-border-strong);
  box-shadow: inset 0 1px 2px rgba(0,0,0,0.04);
}
```

---

## Priority 3: Navigation Visibility

### Issue 3.1: Nav Links on White Header

**Location:** MainLayout.java header navigation

**Root Cause:** Nav links are transparent by default and only show background on hover.

**Remediation Steps:**
```css
.nav-link {
  background: var(--app-surface);
  border: 1px solid var(--app-border);
}

.auth-link {
  background: transparent;
  border-color: var(--app-primary);
}
```

---

## Priority 4: Accessibility Compliance

### Required Focus States
```css
.secondary-btn:focus-visible,
.primary-btn:focus-visible,
.ghost-btn:focus-visible,
.danger-btn:focus-visible {
  outline: 3px solid var(--app-primary);
  outline-offset: 2px;
}
```

---

## Implementation Priority Matrix

| Priority | Issue | User Impact | Effort |
|----------|-------|-------------|--------|
| P1 | Secondary button visibility | CRITICAL | Low |
| P2 | Input field border visibility | HIGH | Low |
| P3 | Navigation link discovery | HIGH | Low |
| P4 | Focus states for accessibility | HIGH | Low |
| P5 | Danger button contrast | MEDIUM | Low |

---

## Immediate Fixes Required

### Fix 1: secondary-btn visibility
**File:** `src/main/frontend/themes/classroomapp/styles.css`

From:
```css
.secondary-btn {
  background: #fff;
  color: var(--app-primary);
  border: 1px solid var(--app-primary);
}
```

To:
```css
.secondary-btn {
  background: var(--app-surface-muted);
  color: var(--app-primary);
  border: 1px solid var(--app-primary);
}
```

### Fix 2: Add focus states
Add focus-visible styles for keyboard navigation accessibility.

### Fix 3: Input field visibility
Increase border weight from `--app-border` to `--app-border-strong` and add inset shadow.