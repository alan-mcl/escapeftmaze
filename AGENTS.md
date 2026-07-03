# AGENTS.md - Escape From The Maze

Guidance for AI coding agents working in this repository.

## What this project is

Escape From The Maze is a party-based, first-person, phased-combat fantasy CRPG
(Wizardry lineage), written in Java. It ships as a game plus a separate Swing
content editor. It uses no game engine - rendering is a custom raycaster and a
custom GUI toolkit.

- Language/runtime: **Java 21**, AWT/Swing.
- Build: **Apache Ant** (`build.xml`).
- Dependencies: vendored jars in `oem/` (`gson-2.8.6`, `jorbis`).
  No package manager; do not add Maven/Gradle.
- Code namespace: `mclachlan.*`, rooted at `src/maze/`.

Read these first for deeper context:
- [doc/design/architecture.md](doc/design/architecture.md) - high-level design.
- [doc/design/data_dictionary.md](doc/design/data_dictionary.md) - data model reference.
- [doc/design/rpg_system.md](doc/design/rpg_system.md) - the RPG rules reference.
- [doc/design/backlog.md](doc/design/backlog.md) - prioritised work backlog.

## Repository layout

| Path | Contents |
|------|----------|
| `src/maze/mclachlan/maze/game/` | Engine core: `Maze`, `Launcher`, `MazeEvent`, `GameTime`, `GameState`, `game/event/` |
| `src/maze/mclachlan/maze/map/` | World model: `Zone`, `Tile`, `Portal`, `TileScript`, `map/script/`, `map/crusader/` |
| `src/maze/mclachlan/maze/stat/` | Rules/model: actors, items, `combat/`, `magic/`, `condition/`, `npc/` |
| `src/maze/mclachlan/maze/ui/diygui/` | Game UI: `DiyGuiUserInterface`, `MazeWidget`, screens, renderers |
| `src/maze/mclachlan/maze/data/` | Persistence: `Database`, `Loader`, `Saver`, `v1/`, `v2/` |
| `src/maze/mclachlan/maze/editor/swing/` | Content editor: `SwingEditor`, `EditorPanel`, `swing/map/` |
| `src/maze/mclachlan/crusader/` | Crusader raycaster engine (game-agnostic) |
| `src/maze/mclachlan/diygui/` | DIYGUI widget toolkit (game-agnostic) |
| `src/maze/mclachlan/dungeongen/`, `jgpgoap/` | Dungeon generation; GOAP planner (experimental) |
| `data/<campaign>/` | Campaign content: `db/*.json`, `save/<slot>/*.json`, `img/`, `sound/`, `font/`, `src/` |
| `oem/` | Third-party jars |
| `doc/` | Documentation (`design/` for specs and campaign design) |

## Build, run, and edit

Build (Ant). The default target builds full distributions; for a plain compile use:
```bash
ant compile        # compiles engine -> build/classes and campaign -> build/default/classes
ant clean          # removes build/
ant                # default 'dist' target: builds zip distributions (needs launch4j/jre config)
```

Run the game (classpath must include the compiled classes and all `oem` jars):
```bash
java -Xmx2048M \
  -cp build/classes:build/default/classes:oem/jorbis/jorbis0.0.17.jar:oem/gson/gson-2.8.6.jar \
  mclachlan.maze.game.Launcher
```

Run the content editor:
```bash
java \
  -cp build/classes:build/default/classes:oem/jorbis/jorbis0.0.17.jar:oem/gson/gson-2.8.6.jar \
  mclachlan.maze.editor.swing.SwingEditor
```

Notes:
- Both must run from the **repo root** (data paths like `data/<campaign>/...` are
  resolved relative to the working directory).
- `run.sh` / `maze.sh` / `editor.sh` exist but reference packaged-distribution
  classpaths; prefer the commands above when running from source.
- The game requires a display (AWT/Swing); it cannot run fully headless.

## Configuration

| File | Purpose |
|------|---------|
| `maze.cfg` | App wiring: `db.loader.impl`/`db.saver.impl` (V2), `ui.impl`, `ui.renderer` (`MazeRendererFactory` or `MFRendererFactory`), screen size, `campaign`, log levels |
| `user.cfg` | Per-user audio/UI preferences (Java Properties) |
| `data/<campaign>/campaign.cfg` | Campaign metadata and inheritance (`parentCampaign`) |

## Testing

There is a **JUnit 5 test suite** in `testsrc/`, driven by Ant (the JUnit jar is
vendored at `oem/junit/junit-platform-console-standalone-*.jar`):

```bash
ant compile-tests   # compile testsrc/ -> build/test-classes
ant test            # run headless; writes build/test-reports
```

Suite conventions (follow these when adding tests):

- **Hermetic fixtures, not `data/default/`.** Build synthetic in-memory content
  via `mclachlan.maze.test.support.TestData` and `InMemoryLoader`/`InMemorySaver`.
  Do not load the default campaign.
- **Deterministic.** Extend `MazeTestSupport`; it seeds `Dice`
  (`Dice.setRandomSeed`) before each test and resets the `Maze`/`Database`
  singletons afterwards.
- **Tiers:** (1) pure logic, (2) V2 serialisation round-trips (assert equality of
  serialised JSON maps, since many domain classes lack value `equals`),
  (3) `GameSys`/`MagicSys` rules formulas with synthetic actors, (4) a reusable
  headless harness (`HeadlessMaze`) plus combat/leveling smoke tests.
- **Test seams** added to production code are minimal and clearly commented:
  `Dice.setRandomSeed`, `Maze.setPerfLog`/`setUserConfig`/`setGameStateNoZone`,
  `Database.resetInstanceForTesting`. Prefer extending fixtures over adding seams.

The legacy `*Test*` classes (under `maze/test/`, `maze/balance/`, `jgpgoap/`,
`crusader/client/`) are standalone `main()` harnesses for manual exploration and
are superseded by the suite for automated checking.

## Conventions for agents

- **Event thread rule:** all game-state mutation flows through `MazeEvent`s on the
  single event thread (`Maze.appendEvents(...)`). Do not mutate party/world state
  directly from UI/AWT code; enqueue events instead.
- **Dual map model:** a `Zone` holds both a Crusader `Map` (render geometry) and a
  `Tile[][]` (game logic). Keep them consistent.
- **Template vs instance:** author content as `*Template` in `db/*.json`; runtime
  objects reference templates by name.
- **Persistence:** add/rename a persisted field by updating the relevant serialiser
  in `data/v2/serialisers/V2SerialiserFactory.java`. JSON stores primitives/enums as
  **strings**; cross-references are stored by name and resolved via `Database`;
  polymorphic types use a `TYPE_KEY` class name.
- **Editor:** new data-type panels extend `EditorPanel`/`IEditorPanel`, register a
  dirty bit via `SwingEditor.Tab`, and persist through `Database.saveXxx()`.
- **Style:** match surrounding code (allman-style braces, tabs as used in nearby
  files). Avoid introducing new third-party dependencies.
- **Licensing:** project is GPLv3; keep the existing license headers on source files.

## Documentation maintenance

Two classes of documents, with different rules:

- **Human-controlled (never edit these).** `stufftodo.txt`, `README.md`, and the
  `doc/readme_*.txt` / `readme.txt` files (and `doc/release_notes.txt`) are owned by
  the author. Do not modify them. If something in them is outdated, mention it to the
  user rather than editing.
- **Agent-maintained (keep these up to date).** When your changes affect them, update
  the design docs under `doc/design/`:
  - `architecture.md` - when components, packages, or their interactions change.
  - `data_dictionary.md` - when persisted entities, fields, or serialisers change.
  - `rpg_system.md` - when game rules/formulas (combat, magic, skills, etc.) change.
  - `backlog.md` - when you complete a backlog item (update its status) or discover
    new work. The backlog mirrors `stufftodo.txt` but is the agent-editable copy;
    fold relevant `stufftodo.txt` items into `backlog.md` instead of editing
    `stufftodo.txt`.

## Safety

- Do not commit changes unless explicitly asked.
- Treat everything under `data/<campaign>/save/` as user data; do not delete save
  slots. `build/` and `log/` are git-ignored build artifacts.
