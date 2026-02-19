# PHANTOM NET v2.0: LAYOUT

> **Status**: ACTIVE  
> **Version**: 2.0.0  
> **Date**: 2026-02-20  
> **Focus**: Screen inventory, navigation graph, per-screen specs, and design system for Phase 1 (SOLID GROUND).

---

## 1. DESIGN DIRECTION

| Aspect | Decision |
|--------|----------|
| Theme | Dark-only. Obsidian base `#0B0E11`, Emerald accents `#00E676` |
| Toolkit | Jetpack Compose + Material 3 |
| Navigation | Bottom Navigation Bar (4 tabs) + nested stack per tab |
| Typography | Inter for UI, JetBrains Mono for keys/fingerprints/crypto data |
| Motion | Shared element transitions list→detail; fade for tab switches |
| Shape system | 16dp corners (cards), 24dp (inputs), full-round (avatars, FABs) |

---

## 2. NAVIGATION GRAPH

```
App Launch
  │
  ├── [First Launch?] ──YES──→ Onboarding Flow
  │                              ├── Welcome Screen (1/3)
  │                              ├── Privacy Promise Screen (2/3)
  │                              └── Identity Creation Screen (3/3) ──→ Main Shell
  │
  └── [Returning?] ──────────→ Main Shell (Bottom Tabs)
                                  ├── 💬 Chats Tab
                                  │     ├── Conversation List
                                  │     ├── Chat Detail → Contact Info
                                  │     └── DC-Net Room Detail
                                  │
                                  ├── 🔍 Discovery Tab
                                  │     └── Discovery Home (Empty MVP)
                                  │
                                  ├── 🔒 Vault Tab
                                  │     └── Vault Home (Empty MVP)
                                  │
                                  └── ⚙️ Settings Tab
                                        ├── Settings Home
                                        ├── Privacy Dashboard
                                        │     └── Shard Wizard
                                        └── Identity Detail
```

---

## 3. SCREEN SPECS

### 3.0 Splash Screen

- **Visual**: Phantom mask icon with emerald radial glow animation, centered
- **Duration**: 1.5s or until DB init completes
- **Transition**: Crossfade to Onboarding or Main Shell
- **Background**: Solid `#0B0E11` with subtle radial emerald gradient

### 3.1 Onboarding — Welcome (1/3)

- **Hero**: Shield animation (Compose animated vector or Lottie)
- **Headline**: "Messages Without Metadata" (white + emerald)
- **Body**: "Your conversations leave no trace. No phone number. No email. No servers."
- **Controls**: Page indicator (● ○ ○), "Next →" button, "Skip" text button
- **Gesture**: Horizontal swipe between pages

### 3.2 Onboarding — Privacy Promise (2/3)

- **Hero**: Lock animation
- **Headline**: "Your Keys. Your Rules."
- **Feature list**: 4 items with emoji icons in a card:
  - 🔐 End-to-End Encrypted
  - 🌐 Zero Servers
  - 👻 Deniable Identities
  - 🧅 Onion Routed
- **Controls**: Page indicator (● ● ○), "Next →"

### 3.3 Onboarding — Identity Creation (3/3)

- **Headline**: "Create Your Phantom Identity"
- **Visual**: Fingerprint hex grid (4×2) generated from public key, emerald monospace
- **Sublabel**: "Kyber-768 + X25519" (subtle)
- **Body**: "Your identity exists only on this device. No account. No email."
- **CTA**: "Enter Phantom →" (emerald, bold)
- **States**:
  - Default: CTA enabled
  - Generating: CTA shows spinner, text = "Generating Identity..."
  - Error: Red text below CTA, retry
  - Success: Navigate to Main Shell with fingerprint shared-element transition

### 3.4 Main Shell (Tab Container)

- **Bottom Nav**: 4 tabs — Chats 💬, Discover 🔍, Vault 🔒, Settings ⚙️
- **Active tab**: Emerald tint on icon + label
- **Inactive tab**: Gray `#8B949E`
- **Badge**: Chats tab shows emerald dot for unread count
- **Back stack**: Each tab maintains independent navigation stack

### 3.5 Chats Tab — Conversation List

- **Top bar**: "PHANTOM NET" (emerald, bold) + search icon
- **Network banner**: TOR/DHT/MESH status pills
- **Sections**: "UNTRACEABLE ROOMS" (emerald header) + "DIRECT CHATS" (gray header)
- **Room item**: Shield icon + room name + status
- **Chat item**: Avatar circle (initials, emerald=online/gray=offline) + name + last message + timestamp + unread badge
- **FAB**: "+" for new chat (future)
- **Empty state**: Ghost icon + "No conversations yet" + "Add Contact" CTA

### 3.6 Discovery Tab (Phase 1 — Empty MVP)

- **Hero**: Radar sweep animation (Compose Canvas)
- **Headline**: "Private Discovery Coming Soon"
- **Body**: "Find contacts without uploading your address book, using Private Set Intersection (PSI)."
- **Info chips**: Zero-Knowledge, Local-Only, No Server

### 3.7 Vault Tab (Phase 1 — Empty MVP)

- **Hero**: Lock animation
- **Headline**: "Encrypted Vault Coming Soon"
- **Body**: "Store files, keys, and credentials in an encrypted local vault. Zero cloud."

### 3.8 Settings Tab

- **Identity card**: FingerprintGrid (4×2 hex), "Tap to copy" label
- **Sections**:
  - SECURITY: Privacy Dashboard →, Secure Backup (SSS) →, Network Status →
  - APPLICATION: App Version (1.4.5), Rust Core status (✓/✗)
  - DANGER ZONE: "⚠ WIPE IDENTITY" red button
- **Wipe flow**: AlertDialog with "Type DELETE to confirm" TextField

---

## 4. UI STATES (ALL SCREENS)

| State | Visual |
|-------|--------|
| Loading | Centered emerald CircularProgressIndicator on background |
| Empty | Branded icon + title + body text + optional CTA (see EmptyState component) |
| Error | Red card with error message + "Retry" button |
| Normal | Content as specified above |
| Sensitive | Key material uses JetBrains Mono, can be hidden behind tap-to-reveal |

---

## 5. DATA BINDING MAP

| Screen | Data Source | ViewModel | Key Events |
|--------|-----------|-----------|------------|
| Splash | DB init, persona check | SplashViewModel | initComplete → navigate |
| Onboarding | Static + key gen | OnboardingViewModel | generateIdentity() on IO thread |
| Chats List | ConversationDao, NetworkStatus flows | ChatsViewModel | tap → navigate, pull-refresh |
| Chat Detail | MessageDao, CryptoEngine | ChatViewModel | send → encrypt → store → publish |
| Discovery | — | — | Phase 1 static |
| Vault | — | — | Phase 1 static |
| Settings | PersonaDao, PhantomCore.isAvailable | SettingsViewModel | copy fingerprint, wipe identity |

---

## 6. DESIGN SYSTEM

### 6.1 Color Palette

| Token | Hex | Usage |
|-------|-----|-------|
| background | #0B0E11 | App background |
| surface | #1C1F26 | Cards, sheets, inputs |
| surfaceVariant | #2D333B | Elevated cards, selected states |
| primary | #00E676 | CTAs, active indicators |
| primaryVariant | #00C853 | Pressed state |
| onPrimary | #0B0E11 | Text on emerald buttons |
| secondary | #58A6FF | Links, info badges |
| error | #FF5252 | Destructive actions |
| onBackground | #C9D1D9 | Primary text |
| onBackgroundMuted | #8B949E | Secondary text, placeholders |
| onSurface | #FFFFFF | Headings |
| divider | #30363D | Separator lines |

### 6.2 Typography

| Style | Font | Weight | Size |
|-------|------|--------|------|
| displayLarge | Inter | Bold | 32sp |
| headlineMedium | Inter | SemiBold | 24sp |
| titleMedium | Inter | SemiBold | 16sp |
| bodyLarge | Inter | Regular | 16sp |
| bodyMedium | Inter | Regular | 14sp |
| bodySmall | Inter | Regular | 12sp |
| labelSmall | Inter | Medium | 11sp |
| mono | JetBrains Mono | Regular | 14sp |

### 6.3 Spacing

- Grid: 8dp
- Screen padding: 16dp horizontal
- Card padding: 16dp internal
- Section gap: 24dp

### 6.4 Reusable Components

| Component | Variants |
|-----------|----------|
| PhantomButton | Primary (emerald fill), Secondary (outline), Destructive (red) |
| PhantomCard | Surface bg, 16dp radius, optional emerald left-border accent |
| StatusPill | Dot + label, colors: active (green/purple/blue), inactive (red) |
| FingerprintGrid | 4×2 monospace hex from pubkey bytes, emerald text |
| EmptyState | Icon + title + body + optional CTA, centered vertically |
| NetworkBanner | Row of StatusPills for TOR/DHT/MESH |
| DestructiveDialog | Red AlertDialog + "type DELETE to confirm" input |

---

## 7. HANDOFF TO ORCHESTRATION

- Define Room/SQLCipher schema for Persona, Conversation, Message entities
- Define ViewModel contracts for each screen
- Define navigation route types (sealed class or type-safe args)
- Define persistence lifecycle (when to read/write/purge)

---
*Next: ORCHESTRATION — Backend/Frontend split, data contracts, state management.*
