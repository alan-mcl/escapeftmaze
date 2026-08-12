# The Temple of Wasud — Campaign Design Spec

> Status: living document for the `temple` campaign under
> [`data/temple/`](../../data/temple/). Agent-maintained; update as phases land.
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

**Phase 1–2 placeholder:** hub and `temple.1` shells were cloned from the Default
`arena` test map so the campaign could boot. They look like the arena until
replaced. Preferred: author a proper temple entrance (and a blank floor palette
shell) in the Swing editor under campaign `temple`. Required hooks:

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

- Engine reuse only: `MapGenZoneScript` + `Noise4jDungeonGen` (no temple-specific
  engine forks).
- Temple subclass: `mclachlan.maze.campaign.temple.TempleGeneratorMazeScript`
  (decorator for walls/doors/encounters; overrides `init` for run-seed logic).
- **Floor prototype** zone `temple.1` (blank palette shell). Later: runtime-cloned
  shells parameterized by depth (avoid `temple.2`…`N` JSON copies).
- Dual map model: gen must keep Crusader `Map` and maze `Tile[][]` consistent.

## 7. Seeds and persistence

| Variable | Role |
|----------|------|
| `temple.run.seed` | One seed per run (new game) |
| `temple.depth` | Current delve depth |
| `temple.floor.seed.<depth>` | Derived from `(runSeed, depth)`; used for Noise4j |
| Mutation keys (later) | Looted caches, cleared encounters, door state |

Pure regen-from-seed is not enough once loot/encounters matter — track mutations
in maze variables / item caches (Phase 2–3).

Helper: `mclachlan.maze.campaign.temple.TempleSeeds`.

## 8. Depth model

`TempleDepthScaler` (temple package, Phase 3): encounter tier, foe packing, loot
multiplier, trap chance by depth. **Orthogonal** to inherited Easy/Normal/Hard/
Heroic `DifficultyLevel`. Soft-cap / asymptotic curve; story win at depth bands
with optional endless delve after victory.

## 9. Encounters and loot

- Phase 2: temple tables `temple.depth.1` / `temple.depth.1.loot` reference
  inherited Default foe entries and loot entries by name (gatehouse-tier vermin /
  gold/food/trinkets). `TempleDecorator.getEncounter` looks up the depth table;
  `TempleFloorDressing` places once-only `Loot` scripts on room-door tiles.
- Stairs: hub `temple.descend.1` ↔ floor `temple.ascend.1` (`ZoneChangeEvent`).
  Ascend is dressed onto an open tile beside spawn (or closest encounter tile).
- Later: optional threat-budget picker in temple package (may read existing
  scorers; must not modify them).

### Phase 2 playtest checklist

1. Select temple campaign → main menu (create/guild/quick start/load all work).
2. Start / quick start → aurora storyboard → Temple Hub.
3. Step onto stairs north of spawn → generated `temple.1` with doors/encounters.
4. Fight a door encounter (Fruit Bat / Mud Spider / Crud / Roach mix).
5. Step onto a loot tile → gold/food/trinkets once.
6. Step onto ascend stairs near spawn → return to Temple Hub.
7. Descend again → same layout for this run (seeded).

Automated: `TempleFloorGenTest` (gen reachability + thin combat).
## 10. Map fragments

Authored fragment zones under `data/temple/db/zones/` plus temple-only
`fragments.json` (depthMin/Max, role, weight, maxPerFloor).
`TempleFragmentAssembler` stamps into Noise4j floors (Phase 4). Roles:
`guardian`, `loot`, `quest`, `flavour`.

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

| Phase | Goal | Status |
|-------|------|--------|
| 0 | This design doc + backlog row | done |
| 1 | Loadable sparse campaign: intro → hub → generating `temple.1` | done |
| 2 | Playable depth-1 crawl + early tests | done |
| 3 | Multi-depth + `TempleDepthScaler` + mutations | todo |
| 4 | Fragments + assembler | todo |
| 5 | Wasud quest + victory / endless | todo |
| 6 | Full balance harness | todo |
| 7 | Meta polish | todo |

## 16. Non-goals / open questions

- No `V2Loader` missing-file softening (use `[]` stubs).
- No coupling to Default story progression or Temple of the Gate.
- GOAP / Heroic AI opportunistic only (backlog P1-1).
- Dist packaging of temple deferred until Phase 2+ is stable.
- Floor prototype size is currently 16×16 (arena-derived); enlarge when delve
  feel needs it.
