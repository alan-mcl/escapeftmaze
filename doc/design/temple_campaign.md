# The Temple of Wasud — Campaign Design Spec

> Status: living document for the `temple` campaign under
> [`data/temple/`](../../data/temple/). Agent-maintained; update as phases land.
> Phases 0–3 done; Phase 4 catalog/seam delivered, dungeon-design remaining.
> §15 holds phase contracts (current: Phase 4 layout; quests wait until floors
> are worth crawling).
> Companion: [backlog.md](backlog.md), [theology.adoc](default_campaign/theology.adoc)
> (Wasud), [architecture.md](architecture.md) (inheritance / zones).

## 1. Elevator pitch

A second campaign that inherits the Default campaign’s items, foes, textures, and
rules, but delivers a **rogue-like dungeon crawl** instead of a story-driven
Realm tour. Dual goals:

1. **Play** — descend Wasud’s temple, fight, loot, reassemble the slain war god.
2. **Balance lab** — seeded, reproducible floors for automated combat/economy
   testing (feeds backlog P2-8) without rewriting Default content.

## 2. Cosmology and framing

Same cosmology as Default. Wasud is defined in
[theology.adoc](default_campaign/theology.adoc) (Knight of Swords, Warbrinder).

**Framing:** Aided by the trickster Hugen, the Giants have ambushed and slain
Wasud and prepare to invade heaven. Without him the Gods are in disarray. The
party enters Wasud’s main temple to find the dismembered parts of his body and
revive him.

**Not the same as** Default’s *Temple of the Gate* (endgame Great Gate zone).
Shared name fragment only; no shared plotscripts or zones.

## 3. Inheritance contract

| Concern | Behaviour |
|---------|-----------|
| Folder / short name | `temple` (`data/temple/`) |
| `parentCampaign` | `default` |
| Merged templates | Items, foes, spells, textures, classes, difficulty modes, etc. via `Database.mergeMaps` (parent first, child overwrites) |
| Zones | **No inheritance** — temple ships its own zones only |
| Guild | Current-campaign only |
| Images / music / fonts / strings | Child first, then parent fall-through |
| Sparse silos | Empty `[]` JSON stubs for every `V2Files` map silo temple does not override (**no** `V2Loader` engine change) |

**Hard rule:** Temple features live under `data/temple/` (data +
`data/temple/src/...`). Do not pollute engine game rules or `data/default/` for
temple behaviour. Allowed shared infra: Ant/launcher classpath so temple sources
compile.

## 4. Player fantasy and session loop

```
App start → main menu (create/guild/quick start/save/load)
  → Start Game / Quick Start → intro storyboard → Temple Hub
  → stairs down → generated floor(s)
  → fight / loot / stairs → (die | retreat to hub | assemble Wasud)
  → optional post-victory endless delve
```

**Script contract (same as Default):**

| `campaign.cfg` key | Fires when | Temple value | Must not |
|--------------------|------------|--------------|----------|
| `introScript` | App startup (alongside GUI build / main menu) | `temple.main.menu` (music only) | Contain click-wait `StoryboardEvent`s — that hangs startup on the EDT |
| `startingScript` | New game / quick start only | `campaign.start` (storyboard → Temple Hub) | Run at boot |

Create character, guild, load game, and settings are campaign-agnostic and must keep working for `temple`.
## 5. Hub design

Authored zone **`Temple Hub`**: framing, safe resting, stairs into the delve.
Later phases may add identify/sell/heal services as economy sinks.

**Phase 1–2:** Hub is player-authored. `temple.1` is a **31×31 indoor dungeon
palette shell** (dungeon floor/ceiling, `DEFAULT_SKY`); gen replaces walls/doors
and dresses encounters/loot/stairs. Required hooks:

| Zone | Must keep |
|------|-----------|
| `Temple Hub` | Name exact; `playerOrigin`; step-on script / tile that runs `temple.descend.1` |
| `temple.1` | Name exact; `script.IMPL` = `mclachlan.maze.campaign.temple.TempleGeneratorMazeScript`; floor/ceiling textures as gen palette |

### Inheritance / audio note

Child campaigns without their own `sound/` must fall through to the parent.
`Database.cacheSound` is first-success (like `getMusic`). Symlinking
`data/temple/font` → `data/default/font` avoids exception fallthrough when
loading fonts at GUI build. Hot-string namespace misses are cached in
`TextRepository.hasHotNamespace` so sparse children do not `File.exists` on
every UI label lookup (critical for Quick Start spell rolling).
## 6. Procedural floors

- All layout goes through the pluggable **`DungeonGen`** interface
  (`mclachlan.dungeongen.DungeonGen`). Default campaigns:
  `MapGenZoneScript.createDungeonGen()` → `Noise4jDungeonGen`.
- Temple: `TempleLayoutPolicy.forDepth(depth)` picks the layout algorithm per
  depth. **Phase 4 focus:** make the live dungeon worth crawling. Iterate
  **Noise4j first** (room/corridor topology, density, loops, dead-ends, scale),
  then register other `DungeonGen` impls (WFC, BSP, …) behind the same policy.
  Orthogonal to `TempleDepthScaler` (content bands vs layout algorithm).
- Temple subclass: `mclachlan.maze.campaign.temple.TempleGeneratorMazeScript`
  (decorator for walls/doors/encounters; overrides `init` for run-seed logic).
- **Floor prototype** zone `temple.1` (blank palette shell). Runtime gen uses
  `TempleFloorShell.GEN_SIZE` (15×15 for testing; raise for full-size floors).
- Dual map model: gen must keep Crusader `Map` and maze `Tile[][]` consistent.
- Post-layout dressing (loot) is **outside** `DungeonGen` — `TempleFloorDressing`
  after `generate()`: wall **chests** on blank room walls (not walk-on loot).
  **Encounters:** one cleared flag per room (`temple.d.<depth>.enc.<roomIndex>`);
  starting room is quiet (no monsters at the first door). **Foe roster:**
  `TempleFoeRoster` clones the band table and picks a persist-once subset via
  `TempleSeededPicks` (`temple.d.<depth>.pick.roster`). **Stair portals** are
  planned by `StairwellPlanner` (temple: `Noise4jStairwellPlanner`), applied by
  `TempleStairwellDresser`, and linked across depths via maze variables
  (`TempleStairLinks`).

## 7. Seeds and persistence

| Variable | Role |
|----------|------|
| `temple.run.seed` | One seed per run (new game) |
| `temple.depth` | Current delve depth (`0` = hub) |
| `temple.floor.seed.<depth>` | Derived from `(runSeed, depth)`; used for Noise4j |
| `temple.d.<depth>.enc.<room>` | Cleared-encounter mutation per **room** (boolean) |
| `temple.d.<depth>.loot.<i>` | Chest state (`untouched` / `empty`) |
| `temple.d.<depth>.pick.roster` | Persist-once foe-entry subset for the depth |
| `temple.d.<depth>.pick.chest.<room>` | Persist-once chest wall slot for a room |
| `temple.d.<depth>.pick.env` | Persist-once floor atmosphere (palette, fog, shade, light) |
| `temple.d.<depth>.startRoom` | Starting room index (no encounters) |
| `temple.d.<depth>.portal.up` | Encoded up-stair portal (`StairPortalSpec`) |
| `temple.d.<depth>.portal.down` | Encoded down-stair portal |
| `temple.transition.mode` | Transient entry hint: `from_hub`, `from_above`, `from_below` |
| `temple.hub.portal.down` | Cached hub descend portal (from `Temple Hub` zone) |

Pure regen-from-seed is not enough once loot/encounters matter — mutation keys
above persist across re-entry of the same depth. Portal coords persist for the
run so vertical transitions spawn on the paired stair tile.

Helpers: `TempleSeeds`, `TempleStairLinks`, `TempleSeededPicks`, `TempleFoeRoster`, `TempleEnvironment`.

### Floor atmosphere (per depth)

Each generated depth picks a persist-once **`TempleEnvironment`** (`pick.env`):
coherent **palette** (dungeon / city / dirt wall-floor-ceiling-door
textures from inherited Default art), **fog colour** (black common; grey and
white uncommon; red haze very rare), **shade multiplier** rolled from
`{0.4, 0.7, 1.0}` (shade distance fixed at `0`), and **ambient tile light**
`{16, 20, 24}` with `20` common (lower baseline; fixture pools add contrast).
Applied during `TempleGeneratorMazeScript.init` before the raycaster is built;
hub zone stays authored. On the **first visit** to each depth, a persist-once
flavour line (`pick.flavour`) is rolled from the environment palette, fog colour,
and ambient light and placed as a once-only `FlavourText` tile script on the
spawn tile so it fires after the zone change (`temple.d.N.visited` suppresses
repeats).

**Lighting (`TempleLighting`):** each depth picks one persist-once fixture type
(`pick.light`: torch, brazier, or ceiling fitting). Torch/brazier sit in rooms
only (centre or corner tiles) with `RandomLightingScript` flicker; ceiling
fittings also line corridors at equal intervals (`floor(length/4)`, minimum 4
tiles apart). All fixtures use centre-of-tile placement. Source tiles peak at `min(ambient+8, 32)` with radial pools at
`+4` and `+2` on neighbouring tiles.

## 8. Depth model

`TempleDepthScaler` (temple package): maps depth → content band (1–3 soft-cap),
encounter/loot table names, loot placement count, foe subset size, foe-pack
multiplier hint. **Depth N targets party level N** (via band pools). **Orthogonal** to inherited Easy/Normal/Hard/Heroic `DifficultyLevel`. Depths
beyond band 3 reuse band-3 tables (endless until Phase 5 quest bands).

Multi-depth loop uses one procedural shell (`temple.1`) plus maze variables for depth:

```
Hub --temple.descend.1--> Temple Depth 1
Temple Depth N --temple.descend.next--> Temple Depth N+1
Temple Depth N --temple.ascend.prev--> Temple Depth N-1  (N > 1)
Temple Depth 1 --temple.ascend.1--> Hub
```

Authored scripts use `SetMazeVariableEvent`, `IncrementMazeVariableEvent`, and `ZoneChangeEvent` only. Each transition sets `temple.depth` and a transient `temple.transition.mode` (`from_hub`, `from_above`, `from_below`). Layout regens from `temple.floor.seed.N` on each visit; cleared enc/loot and stair portal coords persist via maze variables. Zone identity stays **`temple.1`**; generation sets `displayName` to **`Temple Depth N`** (`TempleFloorLabels`) for the HUD / map title. Saves store `temple.1`.

## 9. Encounters and loot

- Tables `temple.depth.{1,2,3}` / `.loot` reference inherited Default foe/loot
  entries by name. Scaler picks the band; `TempleFoeRoster` picks a
  persist-once subset (`foeSubsetSize`: 3 / 3 / 4 for bands 1–3).
- **Encounters:** Noise4j places a script on each room-side door tile, but all
  doors into the same room share one maze variable — clearing the fight clears the
  room. The **starting room** (spawn layout origin) has doors but **no** encounter.
- **Loot:** `TempleFloorDressing` places **chests** on blank room walls (not doors
  or stairs), farthest rooms first, seeded via `TempleSeededPicks`. Chests hug
  the wall (`EngineObject.Placement`) with the lid facing into the room. Contents
  use `TempleChestLootEvent` so a failed independent GOP roll still grants one
  weighted loot entry (procedural chests are never empty).
- Stairs: **wall portals** on **blank room walls** (solid wall behind the mask),
  not on corridor door junctions. Hub `temple.descend.1` → depth 1.
  Depth 1 up → `temple.ascend.1` → Temple Hub; depth 2+ up → `temple.ascend.prev`.
  All generated floors down → `temple.descend.next`. Arrival faces **away** from
  the stair texture (into the room / hub / deeper level). All floor transitions
  zone-change to `temple.1` with spawn from init `MovePartyEvent`.
- Optional threat-budget picker later (may read scorers; must not modify them).

### Phase 2 playtest checklist

1. Select temple campaign → main menu. **done**
2. Start / quick start → aurora → Temple Hub. **done**
3. Descend → generated `temple.1`. **done**
4–7. Fight / loot / ascend / re-descend seeded. Manual / smoke.

Automated: `TempleFloorGenTest`.

### Phase 3 playtest checklist

1. Depth 1 floor has stairs **up** back to the hub and **down** to depth 2.
2. Depth 2 zone-changes from depth 1 via `temple.descend.next` / regen.
3. Clear an encounter / loot on depth 1; leave and return → stays cleared/looted.
4. Ascend from depth 1 → Temple Hub.
5. Depth 4+ still generates (soft-caps to band-3 tables).

Automated: `TempleDepthPhase3Test`, `TempleStairPortalTest`, `TempleStairLinksTest`.

### Phase 4 playtest checklist

**Seam (delivered):**

1. Editor: Zones → Metadata tab; edit keys; save; reload shows same metadata.
2. Fragment zones exist as catalog seed content (peek in tests); not stamped on
   live Noise4j floors.
3. `TempleLayoutPolicy` selects a `DungeonGen` (Noise4j today).

**Playable layout (in progress — 4a.1 delivered):**

4. Room-shared encounters; quiet starting room; wall chests; seeded foe roster.
   **done** (automated: `TempleFloorDressingPhase4Test`, `TempleFoeRosterTest`,
   `TempleSeededPicksTest`).
5. Depth-1 crawl has distinct rooms, readable corridors, and a reason to
   explore beyond the shortest path to stairs.
6. Several fixed seeds feel different (not the same bland grid).
7. Stairs still sit on blank walls; spawn/facing and hub ↔ N ↔ N±1 still work.
8. After Noise4j is good enough: at least one alternate `DungeonGen` can be
   selected via `TempleLayoutPolicy` without changing stair/mutation contracts.

Automated today: `TempleFragmentPhase4Test`, `TempleLayoutPolicyTest`,
`ZoneMetadataPeekTest`, `TempleFloorGenTest`, `TempleFloorDressingPhase4Test`.
Layout-topology tests land with Noise4j retune (4a.2+).

## 10. Map fragments

Authored fragment zones under `data/temple/db/zones/` tagged with optional
**zone metadata** (`Zone.metadata` map in each zone JSON). No separate
`fragments.json` — the temple catalog reads `fragment.*` keys via
`Database.peekZoneMetadataByPrefix("fragment.")` without loading tiles/map.

Convention keys (temple; other campaigns may use other keys):

| Key | Purpose |
|-----|---------|
| `fragment` | `true` marks a stamp template |
| `fragment.role` | `guardian` \| `loot` \| `quest` \| `flavour` |
| `fragment.depthMin` / `fragment.depthMax` | Depth eligibility |
| `fragment.weight` | Weighted pick among eligible |
| `fragment.maxPerFloor` | Cap per zone name per floor |

`TempleFragmentCatalog`, `TempleFragmentStamp`, and `TempleFragmentAssembler`
are **reserved** — not stamped onto live Noise4j floors. Phase 4 layout work
iterates the generator itself first; a later algorithm (WFC or similar) may
consume the catalog. Starter zones: `fragment.flavour.chapel`,
`fragment.guardian.reliquary`, `fragment.quest.altar` (quest stub until Phase 5).

Editor: Zones panel → **Metadata** tab (generic key/value). Full contract: §15.4.

## 11. Quest: reassemble Wasud

Body-part quest items / maze vars; depth-band placement; guaranteed quest
fragments; assembly ritual; victory then optional endless (Phase 5).

## 12. Meta and modes

Phase 7: ironman-friendly default for temple (coordinate with backlog P1-5
without forcing engine changes), hub services, daily/seeded challenge via run
seed, temple-only art as needed.

## 13. Balance lab

Early smoke (Phase 2–3): fixed-seed connectivity + thin combat smoke.
Full harness (Phase 6): seeds × depths × party archetypes; metrics (TTK, HP
drain, loot, threat). Informs Default balance decisions; temple code never
writes `data/default/`.

## 14. Content pipeline

- Author hub/fragments in the Swing editor against campaign `temple`.
- Inherit Default templates by name; add temple overlays only when needed.
- Do not edit Default zones/NPCs/items for temple plot.

## 15. Phased delivery

This campaign is built **one phase at a time**. Phases 0–3 are complete.
Phase 4’s catalog/seam is delivered; **playable dungeon design is the current
work** (iterate Noise4j, then other generators) before Phase 5 quests. Each
remaining slice gets its own **plan-mode plan** before implementation. Do not
start Phase 5 until Phase 4’s playable-layout exit is met (or explicitly waived).

### 15.1 Working process

1. Spec (this doc) states the phase goal, deliverables, and exit criteria.
2. Before coding a remaining phase: produce a dedicated plan-mode plan scoped
   only to that phase (files, risks, test/playtest checklist).
3. Execute in Agent mode; keep temple-only surface area (§3 / hard constraint).
4. Update this section’s status + any phase playtest checklist when the phase
   exits; fold discoveries into §§6–14 as needed.
5. Backlog **P2-28** tracks the campaign as a whole; phase detail lives here.

### 15.2 Design choices locked (all phases)

- Authored **hub** + generated delve floors (`DungeonGen`; Noise4j today).
- **Run seed → derived floor seeds**; persist **mutations**, not only seeds.
- **`TempleDepthScaler`** in the temple package; orthogonal to difficulty modes.
- **Single generated floor zone** (`temple.1` shell); depth via maze variables;
  hub ↔ depth 1 and depth N ↔ N±1 via authored stair scripts. Generation sets
  `Zone.displayName` (`Temple Depth N`); identity stays `temple.1`.
- Soft-cap bands + **post-victory endless** delve (Phase 5+).
- **Fragments:** catalog + stamp helpers exist; not wired into live gen.
  Phase 4 layout iterates **Noise4j first**, then other `DungeonGen` impls via
  `TempleLayoutPolicy`. Do not stamp fragments onto Noise4j as a substitute
  for a better generator.
- Encounter/loot tables reference inherited Default templates by name.
- Early connectivity + combat smoke in Phases 2–3; full metrics harness in
  Phase 6. Temple code never writes `data/default/`.

### 15.3 Status summary

| Phase | Goal (one line) | Status |
|-------|-----------------|--------|
| 0 | Living design doc + backlog row | **done** |
| 1 | Loadable sparse campaign: menu → intro → hub → gen floor | **done** |
| 2 | Playable depth-1 crawl + early tests | **done** |
| 3 | Multi-depth + scaler + mutations | **done** |
| 4 | Playable dungeon design (Noise4j, then other gens) | **in-progress** — catalog/seam done; layout remaining |
| 5 | Wasud quest + victory / endless | **todo** — after Phase 4 layout exits |
| 6 | Full balance harness | **todo** |
| 7 | Meta polish (ironman, hub services, dailies) | **todo** |

### 15.4 Phase contracts

#### Phase 0 — Design document + backlog *(done)*

**Goal:** Living campaign spec and backlog hook before any restore work.

**Delivered:** this doc; backlog **P2-28**; brief architecture / AGENTS mentions.

**Exit:** Spec + backlog exist; no code required beyond doc pointers.

#### Phase 1 — Loadable sparse campaign *(done)*

**Goal:** Select Temple; main menu works; new game / quick start → intro →
**Temple Hub**; stairs → generating floor.

**Delivered:** `data/temple/` tree (`parentCampaign=default`, `[]` stubs), hub +
`temple.1` shell, `TempleSeeds` / `TempleGeneratorMazeScript`, Ant + launcher
classpath for temple sources.

**Exit met:** Second campaign runs; inheritance works; hub + one generated
floor; no gameplay edits under `data/default/`.

#### Phase 2 — Playable hub ↔ depth-1 crawl *(done)*

**Goal:** Explore, fight, loot, return to hub (or die) on depth 1.

**Delivered:** Temple depth-1 encounter/loot tables; dressing of encounters /
loot / stairs; 31×31 indoor palette shell; Noise4j wired as floor engine;
`TempleFloorGenTest` + playtest checklist (§9).

**Exit met:** Single-floor loop playable; gen connected enough to crawl; smoke /
connectivity tests green. (Bland Noise4j layout accepted until Phase 4 design.)

#### Phase 3 — Multi-level descent + TempleDepthScaler *(done)*

**Goal:** Vertical progress with orthogonal depth scaling and persistent
per-depth mutations.

**Delivered:** `TempleDepthScaler` bands 1–3; depth 2–3 tables; hub ↔ depth-1
stairs (`temple.descend.1` / `temple.ascend.1`); per-depth enc/loot
mutation keys; `TempleDepthPhase3Test`.

**Exit met:** Multi-floor delve with clearer difficulty/loot ramp; re-entry
preserves cleared/looted state; depth 4+ soft-caps to band 3.

#### Phase 4 — Playable dungeon design *(in-progress)*

**Goal:** Generated floors that are interesting to crawl — before quests or
shipping polish. Bland rooms-and-corridors is not an exit.

**Already delivered (seam / catalog):** Shared `Zone.metadata` + V2 serialiser;
streaming `Database.peekZoneMetadata` / `peekZoneMetadataByPrefix`; editor
Metadata tab; `temple` fragment zones with metadata; `TempleFragmentCatalog`,
`TempleFragmentStamp`, `TempleFragmentAssembler` (not wired into live gen);
`TempleLayoutPolicy` + `MapGenZoneScript.createDungeonGen`; hybrid Noise4j
overlay **removed** from live path; `TempleFragmentPhase4Test`,
`TempleLayoutPolicyTest`.

**Delivered — 4a.1 encounter / loot / roster dressing:** Room-shared encounter
maze vars (`enc.<roomIndex>`); quiet starting room; wall chests via
`TempleFloorDressing`; persist-once foe subset (`TempleSeededPicks`,
`TempleFoeRoster`); per-floor atmosphere (`TempleEnvironment`); persist-once
light fixtures and radial pools (`TempleLighting`); `DungeonRoom` +
extended `DungeonGenResult`; tests `TempleFloorDressingPhase4Test`,
`TempleFoeRosterTest`, `TempleSeededPicksTest`, `TempleEnvironmentTest`,
`TempleLightingTest`.

**Remaining — 4a.2 Iterate Noise4j topology:** Tune / extend
`Noise4jDungeonGen` (and temple stair/dressing as needed) so a typical seed
has:

- Distinct rooms of varying size, not a uniform blob
- Corridors that branch, loop, or dead-end with purpose (not one spine)
- Enough floor area and encounter/loot spacing to explore, not just path to stairs
- Stable contracts: blank-wall stairs, `temple.floor.seed.N`, mutations, dual map

Plans for 4a should name concrete generator knobs or code changes (room
attempts, corridor style, loops, map size via `TempleFloorShell.GEN_SIZE`,
etc.) and a playtest seed list.

**Remaining — 4b. Other generators (after 4a is good enough):** Implement
additional `DungeonGen` classes and select them from `TempleLayoutPolicy`
(per-depth or by experiment). Candidates: WFC (may consume the fragment
catalog), BSP, or other room/maze hybrids. Same stair/mutation/displayName
contracts. Do not start 4b until 4a exits or is explicitly waived.

**Exit:** A player can spend time on a generated floor and remember it; several
fixed seeds feel different; vertical transitions still work. Alternate gens
are optional for exit if Noise4j alone meets the crawl bar.

**Not in this phase:** Wasud quest items, guaranteed quest fragments, victory.

#### Phase 5 — Quest: reassemble Wasud *(todo — after Phase 4)*

**Goal:** Completable campaign win, then optional endless delve.

**In scope:**

- Temple quest items / maze vars for body parts; depth-band placement;
  guaranteed `quest` fragments.
- Assembly ritual (hub and/or dedicated fragment).
- Journal / string overlays for temple beats only.
- Victory state; further descent remains available (balance + replay).

**Depends on:** Phase 4 playable layout; fragment roles + mutation/seed
patterns from Phases 3–4.

**Exit:** Player can assemble Wasud and win; optional continue deeper.

#### Phase 6 — Full balance harness *(todo)*

**Goal:** Reproducible combat/economy lab (feeds backlog P2-8) without
modifying Default data.

**In scope:**

- Temple-configured headless runs: seeds × depths × party archetypes
  (`HeadlessMaze` / balance patterns, temple-aware).
- Metrics: TTK, HP drain, loot value, encounter threat by depth.
- Ant/JUnit job, display-free; optional outlier thresholds.
- Reports may *inform* Default balance work; temple code never writes
  `data/default/`.

**Exit:** Repeatable reports usable for temple tuning and external balance
decisions.

#### Phase 7 — Rogue-like meta polish *(todo)*

**Goal:** Session meta that makes the temple feel like a finished rogue-like.

**In scope:**

- Ironman / single-save as temple default policy (coordinate with backlog
  P1-5; prefer temple config/scripts over engine changes).
- Hub services (identify, sell, heal at a cost) as economy sinks.
- Daily / seeded challenge via `temple.run.seed`.
- Temple-only art/audio as needed (Default dungeon textures OK indefinitely).
- Opportunistic deeper foe AI only if P1-1 exists — not a blocker.

**Exit:** Meta loop playable; ironman/daily paths documented; polish does not
block Phases 4–6.

### 15.5 Risk notes (for upcoming plans)

| Area | Note |
|------|------|
| Phase 4 layout | Prefer generator changes over stamping fragments onto bland Noise4j. Dual map + stair contracts must survive retunes. |
| Phase 5 state | Fit quest flags into the same maze-var mutation style as enc/loot. |
| Phase 6 lab | Temple data is content-under-test; do not require Default zone loads. |
| Dist | Keep temple out of `ant dist` until Phase 4+ feels stable enough to ship. |

## 16. Non-goals / open questions

- No `V2Loader` missing-file softening (use `[]` stubs).
- No coupling to Default story progression or Temple of the Gate.
- GOAP / Heroic AI opportunistic only (backlog P1-1).
- Dist packaging of temple deferred until Phase 4+ is stable enough (was
  “Phase 2+”; floors are traversable, shipping wait is for crawl feel).
- Floor prototype size is **31×31** (odd, Noise4j-friendly); enlarge further if
  delve feel needs it.
- Any feature implemented by editing Default content or shared engine rules
  “for temple” is out of scope unless split as a separate infra change.
