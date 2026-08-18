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
  (`mclachlan.dungeongen.DungeonGen`). Campaigns select generators via
  `campaign.cfg` (`dungeonGenerators`, `defaultDungeonGenerator`) and
  `MapGenZoneScript.createDungeonGen()` → `DungeonGens.createDefault(campaign)`.
  Built-ins: **`noise4j`** (`Noise4jDungeonGen`) and **`fragment`**
  (`FragmentDungeonGen` in `mclachlan.dungeongen.fragment`).
- Temple live depths 1–4 still default to **Noise4j**
  (`defaultDungeonGenerator=noise4j`). Fragment assembly is engine-generic;
  temple supplies only data (`fragment.barracks.*` zones with `fragment.usage`
  metadata) and campaign pipeline code (seeds, dressing, stairs). Switch live layout by
  changing `defaultDungeonGenerator` or per-depth policy later.
  **Phase 4 focus:** make the live dungeon worth crawling. Iterate
  **Noise4j first** (room/corridor topology, density, loops, dead-ends, scale),
  then roll out fragment assembly and other `DungeonGen` impls (WFC, BSP, …)
  via campaign config. Orthogonal to `TempleDepthScaler` (content bands vs
  layout algorithm).
- Editor **Tools → DungeonGen Test** previews any campaign-enabled generator on
  a cloned shell zone (layout-only or full `ZoneScript.init` pipeline). Form
  fields pack left like other editor tabs; generator-specific knobs sit in a
  CardLayout card (Noise4j vs fragment). Seed row includes **Randomise**; map
  size spinner applies to layout-only (`ZoneShell.ensureSize`) and full pipeline
  (`MazeVariables` `dungeongen.size` → `TempleFloorShell.ensureGenSize`, cleared
  after preview; live crawls stay 15×15). Full pipeline honors Tools generator,
  seed, and fragment knobs via `DungeonGenPreview` maze variables (not campaign
  default alone). Fragment assembly on authored barracks kit needs ~31×31 map
  size and higher max attempts (default 32); layout-only failures show a dialog
  instead of crashing.
- Temple subclass: `mclachlan.maze.campaign.temple.TempleGeneratorMazeScript`
  (decorator for walls/doors/encounters; overrides `init` for run-seed logic).
  When fragment gen is selected, passes `TempleLayoutUsageTheme.usageId()` into
  `FragmentDungeonGen.Options`.
- **Floor prototype** zone `temple.1` (blank palette shell). Runtime gen uses
  `TempleFloorShell.GEN_SIZE` (15×15 for testing; delegates to `ZoneShell.ensureSize`).
- Dual map model: gen must keep Crusader `Map` and maze `Tile[][]` consistent.
- Post-layout dressing (loot) is **outside** `DungeonGen` — `TempleFloorDressing`
  after `generate()`: wall **chests** on blank room walls (not walk-on loot).
  **Encounters:** one cleared flag per room (`temple.d.<depth>.enc.<roomIndex>`);
  starting room is quiet (no monsters at the first door). **Foe roster:**
  `TempleFoeRoster` clones the band table and picks a persist-once subset via
  `TempleSeededPicks` (`temple.d.<depth>.pick.roster`). **Stair portals** are
  planned by `StairwellPlanner` (engine: `Noise4jStairwellPlanner`), applied by
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
| `temple.d.<depth>.pick.usage` | Persist-once floor usage theme (`storage`, `library`, `mystery`, `garden`, `mixed`) — **Noise4j dressing only** |
| `temple.d.<depth>.pick.layout.usage` | Persist-once **layout** theme for fragment assembly (`barracks` today; later `worship`, `sanctum`, `arena`) |
| `temple.d.<depth>.pick.usage.room.<i>` | Persist-once per-room theme when floor theme is `mixed` |
| `temple.d.<depth>.pick.loot.container.<i>` | Persist-once barrel/crate tile for hidden storage loot |
| `temple.d.<depth>.startRoom` | Starting room index (no encounters) |
| `temple.d.<depth>.portal.up` | Encoded up-stair portal (`StairPortalSpec`) |
| `temple.d.<depth>.portal.down` | Encoded down-stair portal |
| `temple.transition.mode` | Transient entry hint: `from_hub`, `from_above`, `from_below` |
| `temple.hub.portal.down` | Cached hub descend portal (from `Temple Hub` zone) |

Pure regen-from-seed is not enough once loot/encounters matter — mutation keys
above persist across re-entry of the same depth. Portal coords persist for the
run so vertical transitions spawn on the paired stair tile.

Helpers: `TempleSeeds`, `TempleStairLinks`, `TempleSeededPicks`, `TempleFoeRoster`, `TempleEnvironment`, `TempleUsageTheme`.

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
ambient light, and usage theme and placed as a once-only `FlavourText` tile script on the
spawn tile so it fires after the zone change (`temple.d.N.visited` suppresses
repeats).

**Lighting (`TempleLighting`):** each depth picks one persist-once fixture type
(`pick.light`: torch, brazier, or ceiling fitting). Torch/brazier sit in rooms
only (centre or corner tiles) with `RandomLightingScript` flicker; ceiling
fittings also line corridors at equal intervals (`floor(length/4)`, minimum 4
tiles apart). All fixtures use centre-of-tile placement. Source tiles peak at `min(ambient+8, 32)` with radial pools at
`+4` and `+2` on neighbouring tiles.

### Usage themes (generic Noise4j floors)

Each generated depth picks a persist-once **`TempleUsageTheme`** (`pick.usage`)
after atmosphere is known. This is a **post-layout dressing layer** on the
Noise4j path only (future fragment-assembled signature levels skip it).

| Theme | Palette constraint | Weight (non-DIRT) | Dressing |
|-------|-------------------|-------------------|----------|
| **Storage** | any | 40 | Barrels (groups of 1–4 around a tile), crates, tables, chairs, market stalls. Loot in that room is **hidden** in a barrel/crate (`ExecuteMazeScript` + `scoutSecretDifficulty` = depth), not a wall chest. |
| **Library** | not DIRT | 15 | Bookshelf wall masks on room/corridor walls; a few chairs. |
| **Mystery** | any | 20 | Altars/shrines, pillars (not on doors or under ceiling lights), rare ruined head; braziers as real lights (pools + flicker). |
| **Garden** | DIRT only | 25 (+15 on DIRT) | Plants/fungus in orderly bed rows (9 objects per tile), under light pools; ceiling fittings do not block floor plants. |
| **Mixed** | any | 25 | Per-room theme via `pick.usage.room.<i>`. |

**Placement:** starting room may have clutter except on the spawn tile. **Door-front tiles** (both sides of every portal, including encounter tiles) stay clear of chests, lights, and usage props. Floor-standing lights (torch/brazier) block other floor objects on that tile; ceiling fittings do not, except **pillars**, which reach the ceiling and cannot share a tile with a ceiling fitting or sit in front of a door.

**Ambient colour magic:** after layout, `TempleMagicDresser` sets all seven `*_MAGIC_GEN` modifiers on every walkable floor tile. General tiles jitter each colour independently around `TempleDepthScaler.meanTileMagic(depth)` (depth band 1–4) with ±1 noise, clamped to **0–8** (engine cap remains 13). Set-piece colour overrides are future work.

**Loot:** non-Storage loot rooms keep wall `Chest` scripts. Storage rooms skip chests; `TempleUsageDressing` attaches hidden loot to one placed barrel/crate using the same `temple.d.N.loot.i` mutation keys.

## 8. Depth model

**Campaign arc (locked):** 20 generated delve floors; depth **N** targets party level **N**.
Boss floors at depths **5, 10, 15, 20** each use a different `DungeonGen` via
`TempleLayoutPolicy` and grant one godly body part (arms, legs, torso, head).
Phase 5 owns quest items and assembly; this doc tracks the cadence only.

**Playable today:** Noise4j crawl floors **1–4** only. Depth **4** has no down
stairs until the depth-5 boss generator exists (`TempleDepthScaler.PLAYABLE_MAX_DEPTH`).

`TempleDepthScaler` (temple package): maps depth → content band (1–4 soft-cap),
encounter/loot table names, loot placement count, foe subset size, ambient tile
magic mean (`meanTileMagic`), scout secret difficulty, foe-pack multiplier hint. **Depth N targets party level N** (via band pools). **Orthogonal** to inherited Easy/Normal/Hard/Heroic `DifficultyLevel`. Depths
beyond band 4 reuse band-4 tables until more bands or Phase 5 quest bands land.

Multi-depth loop uses one procedural shell (`temple.1`) plus maze variables for depth:

```
Hub --temple.descend.1--> Temple Depth 1
Temple Depth N --temple.descend.next--> Temple Depth N+1
Temple Depth N --temple.ascend.prev--> Temple Depth N-1  (N > 1)
Temple Depth 1 --temple.ascend.1--> Hub
```

Authored scripts use `SetMazeVariableEvent`, `IncrementMazeVariableEvent`, and `ZoneChangeEvent` only. Each transition sets `temple.depth` and a transient `temple.transition.mode` (`from_hub`, `from_above`, `from_below`). Layout regens from `temple.floor.seed.N` on each visit; cleared enc/loot and stair portal coords persist via maze variables. Zone identity stays **`temple.1`**; generation sets `displayName` to **`Temple Depth N`** (`TempleFloorLabels`) for the HUD / map title, and `tilesVisitedKey` to **`temple.1#N`** so each depth has its own auto-map. Saves store zone identity `temple.1`.

## 9. Encounters and loot

- Tables `temple.depth.{1,2,3,4}` / `.loot` reference inherited Default foe/loot
  entries by name. Scaler picks the band; `TempleFoeRoster` picks a
  persist-once subset (`foeSubsetSize`: 3 / 3 / 4 / 4 for bands 1–4).
- **Encounters:** Noise4j places a script on each room-side door tile, but all
  doors into the same room share one maze variable — clearing the fight clears the
  room. The **starting room** (spawn layout origin) has doors but **no** encounter.
- **Loot:** `TempleFloorDressing` places **chests** on blank room walls in non-Storage
  loot rooms (Storage uses hidden barrel/crate click loot). Not on doors
  or stairs), farthest rooms first, seeded via `TempleSeededPicks`. Chests hug
  the wall (`EngineObject.Placement`) with the lid facing into the room. Contents
  use `TempleChestLootEvent` so a failed independent GOP roll still grants one
  weighted loot entry (procedural chests are never empty).
- Stairs: **wall portals** on **blank room walls** (solid wall behind the mask),
  not on corridor door junctions. Hub `temple.descend.1` → depth 1.
  Depth 1 up → `temple.ascend.1` → Temple Hub; depth 2+ up → `temple.ascend.prev`.
  All generated floors down → `temple.descend.next` (when down stairs exist).
  **No down stairs on depth 4** until boss floor at depth 5. Arrival faces **away** from
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
3. Campaign `dungeonGenerators` / `DungeonGens` selects built-in generators
   (Noise4j live; fragment available for editor Tools and tests).

**Playable layout (in progress — 4a.1 delivered; 4b fragment gen engine lift done):**

4. Room-shared encounters; quiet starting room; wall chests; seeded foe roster.
   **done** (automated: `TempleFloorDressingPhase4Test`, `TempleFoeRosterTest`,
   `TempleSeededPicksTest`).
5. Depth-1 crawl has distinct rooms, readable corridors, and a reason to
   explore beyond the shortest path to stairs.
6. Several fixed seeds feel different (not the same bland grid).
7. Stairs still sit on blank walls; spawn/facing and hub ↔ N ↔ N±1 still work.
8. After Noise4j is good enough: switch `defaultDungeonGenerator` or add per-depth
   policy without changing stair/mutation contracts.
   **4b delivered:** engine `FragmentDungeonGen` + barracks JSON kit + editor
   DungeonGen Test tool; live rollout still waived (Noise4j default).

Automated today: `TempleFragmentPhase4Test`, `FragmentDungeonGenTest`,
`FragmentRotateTest`, `DungeonGensTest`, `TempleLayoutPolicyTest`,
`ZoneMetadataPeekTest`, `TempleFloorGenTest`, `TempleFloorDressingPhase4Test`.
Layout-topology tests land with Noise4j retune (4a.2+).

## 10. Map fragments

Authored fragment zones under `data/temple/db/zones/` tagged with optional
**zone metadata** (`Zone.metadata` map in each zone JSON). No separate
`fragments.json` — the engine catalog (`FragmentCatalog`) reads `fragment.*`
keys via `Database.peekZoneMetadataByPrefix("fragment.")` without loading tiles/map.

Two **usage** concepts stay separate:

| Concept | Key / class | Purpose |
|---------|-------------|---------|
| Noise4j dressing theme | `temple.d.<depth>.pick.usage` / `TempleUsageTheme` | Storage, library, mystery, garden, mixed — beds, barrels, etc. on Noise4j floors |
| Fragment layout theme | `temple.d.<depth>.pick.layout.usage` / `TempleLayoutUsageTheme` | Barracks (today); worship / sanctum / arena later — picks assembly kit for `FragmentDungeonGen` (from `FragmentCatalog.usageIds()` on zone metadata) |

Convention keys (temple; other campaigns may use other keys):

| Key | Purpose |
|-----|---------|
| `fragment` | `true` marks a stamp template |
| `fragment.role` | `guardian` \| `loot` \| `quest` \| `flavour` (catalog flavour; optional on assembly fragments) |
| `fragment.usage` | Layout theme id (`barracks`, …) — required for assembly picks |
| `fragment.kind` | `room` \| `corridor` — required for assembly picks |
| `fragment.start` | `true` marks an entry / stair room |
| `fragment.depthMin` / `fragment.depthMax` | Depth eligibility |
| `fragment.weight` | Weighted pick among eligible |
| `fragment.maxPerFloor` | Cap per source zone name per floor (rotation variants share the cap) |
| `fragment.rotate` | `false` skips quarter-turn clones (default: rotate to four facings) |

**Sockets** are inferred from 1-tile non-solid perimeter openings (no separate
socket map). Opposite facings weld (`N↔S`, `E↔W`). **`FragmentRotate`**
clones each authored assembly fragment to `#r90` / `#r180` / `#r270` variants
in memory (walls, tiles, objects, and directional bed textures rotate with the
geometry). Set `fragment.rotate=false` on symmetric pieces (e.g. cross junctions).

Engine components (`mclachlan.dungeongen.fragment`):

| Component | Role |
|-----------|------|
| `FragmentCatalog` | Peek metadata; filter by depth, role, usage, kind; expand rotations |
| `FragmentRotate` | Deep-clone + 90° CW quarter turns for assembly picks |
| `FragmentStamp` | Copy tiles, walls, and `EngineObject`s onto a floor |
| `FragmentDungeonGen` | Socket-grow assembly `DungeonGen`; synthesizes `Grid` + `DungeonRoom`s for dressing/stairs |
| `FragmentConnectivity` | Walkability / open-cell checks after assembly |

Temple-only: `TempleFragmentAssembler` (stamp-onto-Noise4j overlay — **not** on live path).

Chapel / reliquary / altar starter zones (`fragment.flavour.chapel`,
`fragment.guardian.reliquary`, `fragment.quest.altar`) remain catalog-only
flavour — no `fragment.usage` / `fragment.kind`, excluded from assembly.

**Starter barracks kit (authored under `data/temple/db/zones/` — hand-edit in
Swing editor as needed):**

One facing per shape; rotation fills the other orientations at assembly time.

Rooms (5×5 unless noted; dungeon wall/floor/ceiling; 1-tile sockets):

- `fragment.barracks.room.entry` — south + east sockets; `fragment.start=true`
- `fragment.barracks.room.dorm` — south socket; two beds
- `fragment.barracks.room.dorm.thru` — north + south sockets; beds
- `fragment.barracks.room.mess` — 7×5; south socket; table + chairs
- `fragment.barracks.room.armory` — south socket; crates
- `fragment.barracks.room.office` — south socket; desk (table + chair)

Corridors (1-tile walkable; unused AABB cells solid):

- `fragment.barracks.corr.straight` — 1×5; north + south sockets
- `fragment.barracks.corr.bend` — 2×2 L; two sockets on open ends
- `fragment.barracks.corr.tee` — 3×3 plus; north + west + east sockets
- `fragment.barracks.corr.cross` — 3×3 plus; four sockets; `fragment.rotate=false`

Regenerate starter JSON from repo root:
`java -cp build/classes:build/default/classes:build/temple/classes:build/test-classes:oem/jorbis/jorbis0.0.17.jar:oem/gson/gson-2.8.6.jar mclachlan.maze.campaign.temple.BarracksFragmentKitWriter`

Each needs `fragment=true`, `fragment.usage=barracks`, `fragment.kind=room|corridor`,
depth 1–99, weight, `maxPerFloor`. Live barracks floors will likely need a shell
larger than 15×15 before `TempleLayoutPolicy.forDepth` switches off Noise4j.

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
drain, loot, threat). Headless dungeon runs always use a **party of 6** and
write a self-contained HTML report (`HarnessHtmlReport`, no external
dependencies). Informs Default balance decisions; temple code never writes
`data/default/`.

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
  Content bands **1–4**; `PLAYABLE_MAX_DEPTH = 4` (no down stairs on 4).
  Boss every 5 depths (5/10/15/20) → alternate `DungeonGen` + body part (Phase 5 quest).
- **Single generated floor zone** (`temple.1` shell); depth via maze variables;
  hub ↔ depth 1 and depth N ↔ N±1 via authored stair scripts. Generation sets
  `Zone.displayName` (`Temple Depth N`) and runtime `tilesVisitedKey`
  (`temple.1#N`); identity stays `temple.1`.
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
`TempleFloorDressing` (non-Storage loot rooms); persist-once foe subset (`TempleSeededPicks`,
`TempleFoeRoster`); per-floor atmosphere (`TempleEnvironment`); persist-once
light fixtures and radial pools (`TempleLighting`); generic usage themes
(`TempleUsageTheme`, `TempleUsageDressing`); `DungeonRoom` +
extended `DungeonGenResult`; tests `TempleFloorDressingPhase4Test`,
`TempleFoeRosterTest`, `TempleSeededPicksTest`, `TempleEnvironmentTest`,
`TempleLightingTest`, `TempleUsageThemeTest`.

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

**Remaining — 4b live rollout (after 4a is good enough):** Switch
`defaultDungeonGenerator=fragment` (or per-depth policy) once barracks kit is
playtested on a larger shell. Candidates still open: WFC, BSP, or other hybrids.
Same stair/mutation/displayName contracts.

**Delivered — 4b engine fragment assembly (not live):** `FragmentDungeonGen`
in `mclachlan.dungeongen.fragment`; grow-from-seed on shell zones; door welding
via `DungeonDecorator.handlePortal`; seals unused sockets; BFS connectivity retry;
synthesized `Grid` / `DungeonRoom`s + engine `Noise4jStairwellPlanner`.
`FragmentRotate` expands authored fragments to four facings in memory;
starter barracks kit JSON under `data/temple/db/zones/fragment.barracks.*`.
Campaign `dungeonGenerators` / `DungeonGens`; editor **Tools → DungeonGen Test**.
`TempleGeneratorMazeScript` already skips `TempleUsageDressing` for non-Noise4j
gens (beds come from fragments).

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
  (`HeadlessMaze` / balance patterns, temple-aware). **Party of 6**
  (Hero, Paladin, Burglar, Ranger, Priest, Sorcerer) on every temple run.
- Metrics: TTK, HP drain, loot value, encounter threat by depth.
- After each headless dungeon run, a self-contained HTML report
  (`build/test-reports/dungeon-run-<seed>.html` by default).
  Rerun with `./temple-run.sh` (writes `build/test-reports/temple-floor-run-42.html`).
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
