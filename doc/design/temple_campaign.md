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
| `temple.depth` | Current delve depth (`0` = hub) |
| `temple.floor.seed.<depth>` | Derived from `(runSeed, depth)`; used for Noise4j |
| `temple.d.<depth>.enc.<i>` | Cleared-encounter mutation (boolean) |
| `temple.d.<depth>.loot.<i>` | Once-only loot mutation |

Pure regen-from-seed is not enough once loot/encounters matter — mutation keys
above persist across re-entry of the same depth.

Helper: `mclachlan.maze.campaign.temple.TempleSeeds`.

## 8. Depth model

`TempleDepthScaler` (temple package): maps depth → content band (1–3 soft-cap),
encounter/loot table names, loot placement count, foe-pack multiplier hint.
**Orthogonal** to inherited Easy/Normal/Hard/Heroic `DifficultyLevel`. Depths
beyond band 3 reuse band-3 tables (endless until Phase 5 quest bands).

Multi-depth loop (single palette shell `temple.1`, no `temple.2`…`N` JSON):

```
Hub --temple.descend.1--> depth 1 floor
floor --temple.descend--> depth+1 (regen shell)
floor --temple.ascend--> depth-1 floor, or Hub if leaving depth 1
```

`TempleDescendEvent` / `TempleAscendEvent` adjust `temple.depth` then
`ZoneChangeEvent` to `temple.1` (`-1:-1`) or `Temple Hub`.

## 9. Encounters and loot

- Tables `temple.depth.{1,2,3}` / `.loot` reference inherited Default foe/loot
  entries by name. Scaler picks the band; decorator/dressing apply it.
- Stairs: hub `temple.descend.1`; floors dress `temple.ascend` (near spawn) and
  `temple.descend` (far encounter tile).
- Optional threat-budget picker later (may read scorers; must not modify them).

### Phase 2 playtest checklist

1. Select temple campaign → main menu. **done**
2. Start / quick start → aurora → Temple Hub. **done**
3. Descend → generated `temple.1`. **done**
4–7. Fight / loot / ascend / re-descend seeded. Manual / smoke.

Automated: `TempleFloorGenTest`.

### Phase 3 playtest checklist

1. Depth 1 floor has stairs **up** (hub) and **down** (deeper).
2. Take stairs down → depth 2 layout (different seed), tougher table foes.
3. Clear an encounter / loot on depth 2; leave and return → stays cleared/looted.
4. Ascend from depth 1 → Temple Hub.
5. Depth 4+ still generates (soft-caps to band-3 tables).

Automated: `TempleDepthPhase3Test`.
## 10. Map fragments

Authored fragment zones under `data/temple/db/zones/` plus temple-only
`fragments.json` (depthMin/Max, role, weight, maxPerFloor).
`TempleFragmentAssembler` stamps into **Noise4j base floors** (Phase 4 hybrid:
Noise4j rooms/corridors + authored guardian/loot/quest/flavour stamps). Roles:
`guardian`, `loot`, `quest`, `flavour`.

Noise4j alone is the Phase 2–3 floor engine; fragments layer on once depth-1
crawl feel is solid.

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
| 3 | Multi-depth + `TempleDepthScaler` + mutations | **done** — scaler bands 1–3, stairs down/up, per-depth enc/loot vars |
| 4 | Fragments + assembler | todo |
| 5 | Wasud quest + victory / endless | todo |
| 6 | Full balance harness | todo |
| 7 | Meta polish | todo |

## 16. Non-goals / open questions

- No `V2Loader` missing-file softening (use `[]` stubs).
- No coupling to Default story progression or Temple of the Gate.
- GOAP / Heroic AI opportunistic only (backlog P1-1).
- Dist packaging of temple deferred until Phase 2+ is stable.
- Floor prototype size is **31×31** (odd, Noise4j-friendly); enlarge further if
  delve feel needs it.
