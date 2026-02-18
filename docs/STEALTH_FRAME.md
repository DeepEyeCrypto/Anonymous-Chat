# PHANTOM NET: STEALTH ICON & APP DISGUISE FRAME

## Phase F-Stealth: FRAME (Threat Model, Modes, and Safety Constraints)

> **Status**: AUTHORITATIVE BASELINE (STEALTH FRAME)  
> **Version**: 1.0.0  
> **Role**: Security-Focused Android Architecture Squad  
> **Mission**: Hide app visibility in plain sight while preserving full E2EE security guarantees.

---

## 1. ROLE, GOAL, AND SECURITY BOUNDARY

This document defines a no-code architecture for stealth and disguise capabilities in Phantom Net.

### 1.1 Product goal

Enable users to:

1. Change app icon and label to harmless identities (calculator, notes, tools, etc.).
2. Optionally hide launcher presence completely.
3. Show a decoy front-end until a secret unlock sequence is entered.
4. Reduce shoulder-surfing and casual inspection exposure in recent apps and notifications.

### 1.2 Security boundary (non-negotiable)

1. Stealth changes **appearance only**, not cryptographic trust.
2. E2EE protocols, key hierarchy, and storage controls are unchanged.
3. No stealth feature may weaken transport security or identity verification.

---

## 2. STAGE 1 — THREAT MODEL & PRINCIPLES

## 2.1 Threats covered

1. Casual home-screen/app-drawer inspection by known contacts.
2. Short “open a few apps” checks by low-sophistication inspectors.
3. Passive exposure through recent-app thumbnails and notification previews.

## 2.2 Non-goals

1. Full forensic resistance against expert lab extraction.
2. Defeating OS-level telemetry outside app control.
3. Anti-reversing/anti-tamper hardening (separate workstream).

## 2.3 Core design principles

1. **Innocent shell, strong core**: decoy UX externally, uncompromised secure messenger internally.
2. **Opt-in and reversible**: stealth features are user-controlled and clearly recoverable.
3. **Fail-safe UX**: users must not lock themselves out permanently.
4. **Transparent limitations**: clearly explain what stealth can and cannot hide.

---

## 3. STAGE 2 — STEALTH MODES OVERVIEW

Modes are configured per persona/profile.

| Mode | Label | Launcher Visibility | First Screen | Intended Risk Tier |
| :--- | :--- | :--- | :--- | :--- |
| **0** | Normal | Visible as Phantom Net | Messenger UI | Default privacy users |
| **1** | Disguised | Visible as innocent app alias | Decoy UI (calculator/notes) | Medium social risk |
| **2** | Hidden | No launcher icon | Secret trigger required | High social-coercion risk |

## 3.1 Mode 0 — Normal (privacy baseline)

1. Standard branding and launcher identity.
2. App lock and screen-security controls available.
3. Recent-app privacy and notification redaction still supported.

## 3.2 Mode 1 — Disguised (visible but harmless)

1. App appears with user-selected icon and label.
2. Opening app launches decoy frontend first.
3. Secret unlock path transitions to real messenger workspace.

## 3.3 Mode 2 — Hidden (no launcher icon)

1. All launcher aliases disabled.
2. Entry only through configured secret trigger paths.
3. Mandatory recovery fallback path documented and tested by user.

---

## 4. STAGE 3 — IMPLEMENTATION CONCEPT (NO CODE)

## 4.1 Launcher alias architecture

Use multiple launcher aliases and dynamic component enablement policy.

Reference alias set:

* `PhantomNetMainAlias` (real brand)
* `PhantomNetCalcAlias`
* `PhantomNetNotesAlias`
* `PhantomNetToolsAlias`

Runtime policy:

1. Only one visible launcher alias enabled in Normal/Disguised mode.
2. Real alias disabled when disguise alias is active.
3. Hidden mode disables all launcher entry aliases.
4. Persist desired state in local encrypted config and reconcile on boot/update.

### Compatibility notes

1. Launcher/OEM behavior differs by Android version/vendor.
2. Some system surfaces may still expose package identity in settings.
3. This is accepted and must be disclosed in UX warnings.

## 4.2 Decoy frontend architecture

Decoy must behave like a plausible app, not a fake splash screen.

Minimum decoy types:

1. Calculator
2. Notes pad
3. Utility-style simple tool

Unlock mechanisms (user selectable):

1. Secret PIN sequence in calculator input.
2. Long-press gesture on specific decoy control.
3. Hidden tap pattern zone.

Post-unlock behavior:

1. Transition to messenger home.
2. Immediately require app lock/passphrase if lock timer expired.
3. Clear decoy input traces from in-memory buffers.

## 4.3 Hidden mode launch architecture

When Hidden mode is active, launcher icons are absent and entry requires secret trigger.

Supported trigger classes:

1. Secret dial pattern (where platform restrictions permit).
2. NFC tag containing secure app URI token.
3. Trusted notification action or local quick action.
4. Optional environment trigger (e.g., known Wi-Fi SSID) where reliable.

Reliability policy:

1. User must validate trigger once during setup (“Test Launch”).
2. Provide emergency temporary icon reveal (time-limited).
3. Provide offline recovery instructions (including ADB-style path for advanced users).

## 4.4 Recent-app and screenshot protection

1. Messenger surfaces use secure window policy to block screenshots and sensitive switcher thumbnails.
2. App switcher card for messenger should render blank/obfuscated preview.
3. Decoy activity may optionally permit normal screenshots for realism.
4. Sensitive notifications default to redacted/generic text.

---

## 5. STAGE 4 — USER FLOWS & SETTINGS

## 5.1 Onboarding/security wizard additions

Prompt: **“How visible should this app be on your phone?”**

Choices:

1. Normal
2. Disguised
3. Hidden

Disguised setup requires:

1. Pick icon family and label.
2. Set decoy unlock secret.
3. Preview launcher appearance before applying.

Hidden setup requires:

1. Select trigger method.
2. Perform mandatory test-launch workflow.
3. Confirm recovery instructions saved.

## 5.2 “Stealth & Appearance” settings page

Required controls:

1. Current mode indicator and quick mode switch.
2. Alias preview and instant disguise switcher.
3. Hidden mode trigger selector.
4. “Temporarily show icon for 60 seconds” safety action.
5. Global content-protection toggles:
   * screenshot blocking,
   * app-switcher privacy,
   * notification content redaction.

## 5.3 Failure prevention UX

1. Before entering Hidden mode, force two-step confirmation.
2. Show clear warning that OS updates/OEM behavior may affect triggers.
3. Require one known-good fallback method at all times.

---

## 6. STAGE 5 — PANIC & DENIABILITY FEATURES

## 6.1 Panic gesture policy

User-configurable panic actions:

1. Hardware button pattern.
2. Shake gesture.
3. Quick in-app panic control.

On panic:

1. Immediately foreground decoy UI or lockscreen facade.
2. Remove sensitive screen from recent-apps.
3. Clear in-memory plaintext buffers and transient drafts.
4. Keep cryptographic key material lifecycle unchanged unless explicit wipe mode is selected.

## 6.2 Decoy persona model

1. Provide optional low-sensitivity decoy persona with benign sample chats.
2. Decoy PIN opens decoy persona only.
3. Real persona requires stronger unlock policy (long passphrase/biometric+passphrase policy).
4. UX must state this is social deniability, not forensic invisibility.

---

## 7. STAGE 6 — SAFETY, POLICY, AND DISTRIBUTION CONSTRAINTS

## 7.1 Distribution policy strategy

Because app-store policies may limit deceptive app behavior:

1. **Store flavor**: conservative stealth subset, explicit disclosures.
2. **Stealth flavor (side-load/F-Droid)**: advanced hidden/disguise controls for high-risk users.

## 7.2 User education requirements

Add an in-app **“About Stealth Mode”** page covering:

1. What is hidden (icon/name/previews/notification content).
2. What is not hidden (system package listings, backup traces, platform logs).
3. Recovery steps back to Normal mode.
4. Operational safety tips (test your trigger path before travel/checkpoints).

---

## 8. STAGE 7 — TESTING PLAN (STEALTH-SPECIFIC)

## 8.1 Functional matrix

Validate across Android versions and OEM launchers:

1. Alias switching correctness.
2. Decoy activity reliability.
3. Hidden trigger reliability across reboot/force-stop/update.
4. Recovery path success when trigger fails.

## 8.2 Adversary simulation

1. **Casual inspector test**: 30-second device scan should reveal only decoy/no obvious messenger.
2. **Quick-check test**: recent apps and notification shade must reveal no sensitive content.

## 8.3 Regression guardrails

1. Stealth mode transitions must never corrupt keys/chats/personas.
2. App updates preserve mode/alias settings where OS permits.
3. Disable/enable cycles must not break login/unlock pathways.

---

## 9. MODULE CONTRACTS FOR NEXT PHASES

If **LAYOUT** is requested next, design:

* Stealth & Appearance settings flows,
* disguise picker (icon + label + preview),
* Hidden mode trigger and test-launch flow,
* decoy calculator/notes interaction states,
* panic actions and recovery UI.

If **ORCHESTRATION** is requested next, split into modules:

* `LauncherAliasManager`
* `StealthConfigStore`
* `DecoyFrontEnd`
* `SecretUnlockCoordinator`
* `PanicGestureDetector`
* `NotificationPrivacyController`

If **WORLD** is requested next, define:

* `store` vs `stealth` build flavors,
* CI stealth regression matrix across OEM launchers,
* release checklist for hidden-mode recoverability and policy compliance.

---

## 10. DECISION SUMMARY (LOCK)

1. Stealth is an appearance layer; crypto and storage security remain constant.
2. Three explicit modes: Normal, Disguised, Hidden.
3. Disguised mode uses launcher alias + believable decoy frontend.
4. Hidden mode requires mandatory tested recovery path.
5. Panic and decoy persona features target social-level deniability.
6. Distribution strategy separates store-safe and high-risk feature sets.

---

*Authorized as the Phantom Net Stealth FRAME baseline.*
