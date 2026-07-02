# Escape From The Maze - Prioritised Backlog

> A working, agent-maintained backlog synthesised from the human-authored
> [`stufftodo.txt`](../../stufftodo.txt) and from observations of the codebase.
>
> Important: `stufftodo.txt` is the author's own roadmap and is **human-controlled** -
> it is never edited by agents. This file is the agent-facing companion: it
> reorganises and prioritises that roadmap, adds engineering/code-health items found
> in the source, and is updated as work is done. When an item here is completed,
> update its status; when the author adds to `stufftodo.txt`, fold new items in here.

## How to read this

- **Priority:** P0 (correctness/blocking) > P1 (high value) > P2 (medium) > P3 (nice
  to have / polish / content).
- **Type:** `bug`, `code` (engineering/refactor), `feature`, `editor`, `raycaster`,
  `data`, `content`, `spell`, `ability`, `art/sound`, `balance`.
- **Status:** `todo`, `in-progress`, `blocked`, `done`.
- **Effort:** rough size - S (hours), M (a day or two), L (multi-day), XL (project).

Items tagged "(stufftodo)" come from the author's roadmap; items tagged "(code)" were
identified from the source tree and are not necessarily in `stufftodo.txt`.

---

## P0 - Correctness & blocking issues

| # | Type | Item | Notes | Effort | Status |
|---|------|------|-------|--------|--------|
| P0-1 | bug | Zone-change flickering | Visible flicker on zone transitions; engine is recreated per zone in `DiyGuiUserInterface.setZone()`. (stufftodo) | M | todo |
| P0-2 | bug | Damage multipliers applied after armour soak | Should be applied **before** soak per design; current order in `GameSys` damage calc changes balance. Affects every hit. (stufftodo/code) | M | todo |
| P0-3 | bug | Foes can be evaded while resting | Resting evasion should not be possible (or stealth should be adjusted). (stufftodo) | S | todo |
| P0-4 | bug | "Hidden stuff" tile script broken | Secret/hidden discovery no longer triggers; see `HiddenStuff` / Scouting path. (stufftodo) | M | todo |

> A JUnit 5 suite now exists (`ant test`), covering pure logic, V2 serialisation
> round-trips, `GameSys`/`MagicSys` formulas, and a headless combat/leveling
> harness (P1-7, done). P0 regressions in these areas can now be caught
> automatically; broaden combat/AI coverage as those items are addressed.

---

## P1 - High value (engine, AI, core systems)

| # | Type | Item | Notes | Effort | Status |
|---|------|------|-------|--------|--------|
| P1-1 | code | Better Foe AI for harder difficulty | `BasicFoeAi` is the only production AI; Heroic mode needs smarter foes. (stufftodo) | L | todo |
| P1-2 | code | Finish or remove GOAP foe AI | `game/goapai/GOAPFoeAI` is a stub (`initPossibleActions`/`getCombatIntention` return empty). Decide: complete it (ties into P1-1) or delete the dead path. (code) | L | todo |
| P1-3 | code | Audio system unification (one Ogg player) | Consolidate to a single Ogg playback path. (stufftodo) | M | todo |
| P1-4 | feature | Quicksave / quickload | (stufftodo) | M | todo |
| P1-5 | feature | Ironman mode (single save) | (stufftodo) | M | todo |
| P1-6 | code | Comprehensive practising of modifiers | Practice currently fires for a subset of skills; make it consistent across all active modifiers. (stufftodo) | M | todo |
| P1-7 | code | Introduce a real test harness | **Done.** JUnit 5 suite in `testsrc/` (`ant compile-tests` / `ant test`): hermetic fixtures, seeded RNG, tiers for pure logic, V2 serialisation round-trips, `GameSys`/`MagicSys` formulas, and a `HeadlessMaze` combat/leveling smoke harness. Follow-up: broaden combat/AI/spell coverage. (code) | L | done |
| P1-8 | code | Different combat ranges | Make engagement-range rules richer/cleaner. (stufftodo) | M | todo |
| P1-9 | code | Validate PC combat actions on Repeat | Re-validate a repeated action before resolving (target/AP/state may have changed). (stufftodo) | S | todo |
| P1-10 | code | Berserk replaces in-flight combat actions | Berserk currently overrides intentions but not already-queued actions for the same round. (stufftodo) | S | todo |

---

## P2 - Medium (features, editor, data systems)

### Gameplay features

| # | Type | Item | Notes | Effort | Status |
|---|------|------|-------|--------|--------|
| P2-1 | feature | Scavenger mode | Start with nothing; loot replaced with trash; crafting is survival. (stufftodo) | L | todo |
| P2-2 | feature | Secure chests (leave items safely) | Player-owned storage. (stufftodo) | M | todo |
| P2-3 | feature | Party camps: leave characters behind | (stufftodo) | M | todo |
| P2-4 | feature | Designate spokesman / assayer / scout | Roles that pick which PC performs social/identify/scouting checks. (stufftodo) | M | todo |
| P2-5 | feature | Guilds bury dead characters in graveyards | (stufftodo) | M | todo |
| P2-6 | code | Martyr ability drops items on the ground | (stufftodo) | S | todo |
| P2-7 | code | Character creation: restrict portraits by race | Portraits filtered by race/gender. (stufftodo) | S | todo |
| P2-8 | balance | Comprehensive combat/economy balance pass | Uses the `maze/balance/` headless tools. (stufftodo) | L | todo |
| P2-9 | feature | Demo / training mode | (stufftodo) | M | todo |

### Editor

| # | Type | Item | Notes | Effort | Status |
|---|------|------|-------|--------|--------|
| P2-10 | editor | Edit save-game item caches, NPCs, maze variables, journals | Round out `SaveGamePanel`. (stufftodo) | M | done |
| P2-11 | editor | Proper dynamic FK support (cascading rename & delete) | Cross-reference integrity across data types. (stufftodo) | L | todo |
| P2-12 | editor | Resize maps | (stufftodo) | M | todo |
| P2-13 | editor | Copy / rename zones | Currently shows "Not supported". (stufftodo/code) | M | todo |
| P2-14 | editor | Undo/redo | MapEditor undo/redo via scoped zone snapshots (`MapEditHistory`, Ctrl+Z/Y). Init Zone Script excluded. (code) | L | done |
| P2-15 | editor | Validation framework | Validation is ad-hoc; add consistent pre-save validation. (code) | M | todo |
| P2-16 | editor | Map editor copy/paste | Copy/paste selected tiles (Crusader + maze layers), walls, and objects within a zone via Select-tab tools and Ctrl+C/Ctrl+V. | M | done |

### Data / crafting systems

| # | Type | Item | Notes | Effort | Status |
|---|------|------|-------|--------|--------|
| P2-16 | data/code | Split CRAFT into Smithcraft + Herbal + Alchemic crafting | Herbal for plants, Alchemic for reagents/potions, rename CRAFT -> SMITHCRAFT for arms/armour. (stufftodo) | M | todo |
| P2-17 | code | Quest journal / general journal cleanup | (stufftodo) | M | todo |
| P2-18 | code | Refactor: remove DIYGUI impl action messages | Drop legacy action-message coupling. (stufftodo) | M | todo |
| P2-19 | ability | DIPLOMACY rework + PERSUASION / CODE_OF_DISHONOUR hooks | Make social abilities meaningful. (stufftodo) | M | todo |
| P2-20 | data | Foe resistances, inventories, mind-read results | Author missing foe data; "Sort out NPC inventories". (stufftodo) | M | todo |
| P2-21 | data | More wielding combos; puzzle boxes; hidden stashes | Content-data depth. (stufftodo) | M | todo |
| P2-22 | code | Retire/contain legacy V1 persistence | V1 is reduced to parsers + a zone-only loader; the `DataPorter` migration tool is commented out. Decide whether to finish migration tooling or remove dead V1 code. Strings & `user.cfg` still use V1 Properties. (code) | M | todo |
| P2-23 | code | Wire up or remove `SensitiveStore` | JCEKS keystore wrapper exists but is unreferenced. (code) | S | todo |

---

## P3 - Polish, content & art/sound

### Raycaster & UI polish

| # | Type | Item | Notes | Effort | Status |
|---|------|------|-------|--------|--------|
| P3-1 | raycaster | Variable ceiling height | (stufftodo) | M | todo |
| P3-2 | raycaster | Proper FXAA implementation | Current filter is FXAA-lite. (stufftodo) | M | todo |
| P3-3 | feature | NPC conversation keyword shortcuts | Player keyword shortcuts in dialogue. (stufftodo) | S | todo |

### Spells

| # | Type | Item | Notes | Effort | Status |
|---|------|------|-------|--------|--------|
| P3-4 | spell | Magic Eye (reveals map) | (stufftodo) | S | todo |
| P3-5 | spell | Oracle (summon to question) | (stufftodo) | M | todo |
| P3-6 | spell | Bind Demon / Genie (summon to perform a task) | (stufftodo) | M | todo |
| P3-7 | spell | Dodgeable spells | Needs engine support for dodging spell projectiles. (stufftodo) | M | todo |
| P3-8 | spell | Greater/Lesser Eye-for-an-Eye | (stufftodo) | S | todo |
| P3-9 | spell | Flying (tie into class abilities) | (stufftodo) | M | todo |
| P3-10 | ability | Favoured terrain (resist surprise, etc.) | (stufftodo) | S | todo |

### Art & sound

| # | Type | Item | Notes | Effort | Status |
|---|------|------|-------|--------|--------|
| P3-11 | art/sound | Sprites & sounds: clockwork beast, winter/lava wolf, owlbear, stone bear, griffon, angel | (stufftodo) | L | todo |
| P3-12 | art | Animations: javelin, throwing axe, throwing hammer | (stufftodo) | M | todo |
| P3-13 | art | Icons: wilderness veil, aura of regeneration | (stufftodo) | S | todo |
| P3-14 | sound | All musical instruments | (stufftodo) | M | todo |
| P3-15 | art | Torch texture (Ichiba City) | (stufftodo) | S | todo |

---

## Content backlog (default campaign)

These are authoring tasks in `data/default/` rather than engine work. Tracked here for
completeness; they live primarily in `stufftodo.txt`.

### Ichiba Crossroads
- Forgotten path / red moss tree; additional encounters.
- Chest contents -> MazeScript; restore hidden-stuff tile script (see P0-4).
- Zone script: day/night cycle; Eva quest -> Gurney.

### Ichiba City
- City secret-passage button; city staircase.
- Foe rebalancing (see P2-8); torch texture (P3-15).
- NPCs: Gurney dialogue, Elsibet.
- The Tumbledown: map, encounters, access (key/secret).
- Library: bookshelf scripts, upstairs.
- Random houses (encounters, locked doors), NPC bedrooms with loot, inn kitchen,
  ambient encounters.

---

## Code-health watch-list (cross-cutting)

Engineering concerns surfaced from the source that should inform the above:

- **Test coverage breadth** - a JUnit suite now exists (P1-7 done); the remaining
  risk is breadth, especially deeper combat/AI/spell scenarios.
- **Single production foe AI** with a dead GOAP path (P1-1, P1-2).
- **Legacy V1 persistence** partly retired but still load-bearing for strings/config
  (P2-22).
- **No editor undo and ad-hoc validation** (P2-14, P2-15).
- **Build assumes a developer-specific environment** - `build.xml`'s `dist` target
  hard-codes launch4j/JRE paths; only `ant compile` is portable. Consider a portable
  packaging path. (code)

---

*Maintenance: keep this backlog in sync with the codebase and with
[`stufftodo.txt`](../../stufftodo.txt). Do not edit `stufftodo.txt` or the readme
files - they are human-controlled (see [AGENTS.md](../../AGENTS.md)).*
