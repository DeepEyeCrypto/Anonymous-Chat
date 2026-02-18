# PHANTOM NET: UI/UX ARCHITECTURE

## Phase L: LAYOUT (Information Architecture & Screen Journeys)

> **Status**: DRAFT (LAYOUT PHASE START)  
> **Version**: 1.0.0  
> **Focus**: Designing the "Messages without Metadata" user experience.

---

## 1. DESIGN ETHOS: "OBSIDIAN & EMERALD"

Phantom Net uses a premium, high-integrity aesthetic designed to convey security and technical superiority.

* **Theme**: Dark Mode only by default (Obsidian base #0B0E11).
* **Accents**: Glowing Emerald Green (#00E676) for active security indicators.
* **Visual Language**: Glassmorphism, subtle micro-animations for message "dissolving," and cryptographic status badges.
* **Typography**: Clean, monospace-skewed sans-serif (Inter with specific Mono ligatures for keys/routing IDs).

---

## 2. INFORMATION ARCHITECTURE (IA)

### 2.1 Tab Navigation

1. **Personas (The Identity Hub)**: Switch between active personas; view sharded backup status.
2. **Chats**: Conversation list with "Active Routing" status indicators.
3. **Discovery**: PSI-based contact search; DC-Net public room directory.
4. **Vault**: Encrypted file storage; credentials manager; privacy-preserving bots.

### 2.2 Global HUD (Heads-Up Display)

Present at the top of the chat list or inside conversations:

* **Routing Status**: Icon showing Onion (Spiral) vs. Mixnet (Grid) vs. DC-Net (Shield).
* **Entropy Pulse**: A subtle animation showing cover traffic injection.
* **Panic Trigger**: Long-press on the Phantom mask to trigger profile wipe.

---

## 3. CORE SCREEN JOURNEYS

### 3.1 Persona Onboarding & The "Anon Sanity Checker"

1. **First Launch**: Animated intro showing metadata trails vanishing into a phantom mask.
2. **Sanity Checker**:
    * *Question*: "Who is your primary adversary?" (Local Thief / ISP / Global State).
    * *Visual*: A threat map updates in real-time based on selection.
3. **Creation**: App generates a local Ed25519 Persona root key.
4. **Shard Wizard**: Interactive prompt to split the recovery shard across 3-of-5 locations (QR codes/Friends/Cloud).

### 3.2 The Privacy Slider (Speed ↔ Stealth)

Found inside individual chat settings:

* **Fast (Onion)**: Green indicator. 3-hop entry/exit. Real-time typing enabled.
* **Balanced**: Yellow indicator. 3-hop + random jitter. Typing indicators disabled.
* **Paranoia (Mixnet)**: Deep Emerald pulsing. Mandatory cover traffic. Messages released in 10s epochs.
* **Anonymous Room (DC-Net)**: Author-hidden mode. All participants appear as "Phantom [X]".

### 3.3 PSI Contact Discovery

1. **Entry Point**: Discovery Tab -> "Find Friends via PSI".
2. **The Protocol**: A "scanning" animation showing blinded hashes being compared locally.
3. **The Result**: "4 contacts matched."
4. **Action**: User chooses to "Initialize Relationship" via a one-time routing token link. No phone number or email is ever stored or displayed again.

### 3.4 Stealth Persona & Panic Switch

* **Double Passphrase**: Entering the "Cover" PIN opens a benign chat list. Entering the "Hidden" PIN opens the real workspace.
* **Panic Gesture**: Triple-tap on the status bar or secret app drawer swipe.
* **Outcome**: High-risk persona keys are overwritten with zeros (shredded) and the DB is dropped. UI reverts to "First Launch" or "Cover" state.

---

## 4. COMPONENT LIBRARY (MOCKUP REQUIREMENTS)

### 4.1 Message Envelopes

* **Standard**: Glassmorphic bubble with delivery timestamp.
* **Dissolving**: A particle effect animation when the disappearing timer hits zero.
* **DC-Net Envelope**: A "Locked Shield" icon showing the message is cryptographically untraceable to a specific sender in the room.

### 4.2 The Routing Map (BottomSheet)

A technical visualization showing:

* Your IP (Hidden).
* Entry Node (Pseudonymous PubKey).
* Mixnet Shuffles (Graphic representing packet reordering).
* Recipient (Relationship ID).

---

## 5. PLUGINS & LOCAL BOTS UI

* **Bot Center**: List of local-only bots (Edge AI Summary, Auto-Archive, Security Audit).
* **Bot Sandbox**: Permissions screen showing the bot has "Read-Only" access to specific chat threads and "Zero Network Access."

---
*Next Step: Generate high-fidelity visual mockups for the Privacy Dashboard and Sanity Checker.*
