/*
 * Copyright (c) 2026 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.maze.campaign.temple;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.balance.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.*;
import mclachlan.maze.game.event.MovePartyEvent;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.CombatStatistics;

/**
 * Headless automated run through playable temple depths using {@link MazeWalker}.
 */
public final class TempleRunDriver
{
	public static final int DEFAULT_MAX_DEPTH = TempleDepthScaler.PLAYABLE_MAX_DEPTH;
	public static final int PARTY_SIZE = 6;

	public static final class Config
	{
		public long runSeed = 42L;
		public int maxDepth = DEFAULT_MAX_DEPTH;
		public int maxTileStepsPerFloor = 400;
		public int maxCombatRounds = CombatDriver.DEFAULT_MAX_ROUNDS;
		/** When true, progress lines are printed to stdout during the run. */
		public boolean logProgress = true;
		/** When null, written under {@code build/test-reports/}. */
		public Path htmlReportPath;
	}

	private final Maze maze;
	private final Database db;
	private final CharacterBuilder characterBuilder;
	private final CharacterBuilder.ModifierApproach modifierApproach =
		new PriorityModifierApproach(
			Stats.Modifier.BRAWN,
			Stats.Modifier.SKILL,
			Stats.Modifier.CUT);

	public TempleRunDriver(Maze maze, Database db)
	{
		this.maze = maze;
		this.db = db;
		this.characterBuilder = new CharacterBuilder(db);
	}

	/*-------------------------------------------------------------------------*/
	public TempleRunMetrics run(Config config)
	{
		TempleRunMetrics metrics = new TempleRunMetrics();
		metrics.setRunSeed(config.runSeed);

		HarnessRunProgress.setEnabled(config.logProgress);
		try
		{
			return runInternal(config, metrics);
		}
		finally
		{
			HarnessRunProgress.setEnabled(false);
		}
	}

	private TempleRunMetrics runInternal(Config config, TempleRunMetrics metrics)
	{
		Dice.setRandomSeed(config.runSeed);

		if (Maze.getLog() == null)
		{
			Log runLog = new Log();
			runLog.setLevel(Log.MEDIUM);
			maze.initLog(runLog);
		}
		Maze.log("Temple headless run seed=" + config.runSeed);

		HarnessRunProgress.line(
			"temple run: seed=%d depths=1..%d stepCap=%d combatRoundCap=%d",
			config.runSeed,
			config.maxDepth,
			config.maxTileStepsPerFloor,
			config.maxCombatRounds);

		PlayerParty party = buildParty();
		maze.setParty(party);
		HeadlessHarnessSupport.ensureNpcManagerStarted();
		metrics.setStartTurn(maze.getTurnNr());

		int startXp = totalExperience(party);
		int startLevels = totalLevels(party);

		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(config.runSeed));
		HarnessRunProgress.line("entering temple from hub");
		enterFromHub();

		for (int depth = 1; depth <= config.maxDepth; depth++)
		{
			Zone zone = maze.getCurrentZone();
			if (zone == null || !TempleStairLinks.FLOOR_ZONE.equals(zone.getName()))
			{
				HarnessRunProgress.line("depth %d: no floor zone — stopping", depth);
				break;
			}

			metrics.setMaxDepthReached(depth);
			HarnessRunProgress.line(
				"depth %d: walking floor (turn %d, party %d/%d alive)",
				depth,
				maze.getTurnNr(),
				party.numAlive(),
				party.size());
			TempleRunMetrics.FloorMetrics floor = walkFloor(zone, depth, config, party);
			metrics.addFloor(floor);
			HarnessRunProgress.line(
				"depth %d: done — steps=%d combats=%d rounds=%d hpLost=%d xp=%d lootGp=%d",
				depth,
				floor.tileSteps,
				floor.combats,
				floor.combatRounds,
				floor.partyHpLost,
				floor.xpGained,
				floor.lootBaseCost);

			if (party.numAlive() == 0)
			{
				metrics.setWiped(true);
				HarnessRunProgress.line("depth %d: party wiped", depth);
				break;
			}

			HarnessRunProgress.line("depth %d: applying pending level-ups", depth);
			applyPendingLevelUps(party);

			if (depth >= config.maxDepth || !TempleDepthScaler.hasDownStairs(depth))
			{
				break;
			}

			HarnessRunProgress.line("depth %d: descending", depth);
			if (!descendToNextDepth(zone, config))
			{
				HarnessRunProgress.line("depth %d: descend failed — stopping", depth);
				break;
			}
		}

		metrics.setEndTurn(maze.getTurnNr());
		metrics.setLevelsGained(totalLevels(party) - startLevels);
		int xpGained = totalExperience(party) - startXp;
		if (xpGained > 0 && metrics.getTotalXpGained() == 0)
		{
			// fold run-level xp if per-floor tracking missed grants
			for (TempleRunMetrics.FloorMetrics f : metrics.getFloors())
			{
				xpGained -= f.xpGained;
			}
		}

		writeHtmlReport(config, metrics, party);

		long turns = metrics.getEndTurn() - metrics.getStartTurn();
		HarnessRunProgress.line(
			"run complete: depth=%d combats=%d turns=%d outcome=%s",
			metrics.getMaxDepthReached(),
			metrics.getTotalCombats(),
			turns,
			metrics.isWiped() ? "wipe" : "clear");
		Log log = Maze.getLog();
		if (log != null)
		{
			HarnessRunProgress.line("session log: %s", log.getLogPath());
		}
		if (config.htmlReportPath != null)
		{
			HarnessRunProgress.line("html report: %s", config.htmlReportPath.toAbsolutePath());
		}

		return metrics;
	}

	/*-------------------------------------------------------------------------*/
	private PlayerParty buildParty()
	{
		List<UnifiedActor> actors = new ArrayList<>();
		// Full party of 6, distinct classes, matching rpg_system suggested balance:
		// two combat, one stealth, one magic, one healer, locks covered by Burglar.
		actors.add(characterBuilder.buildCharacter(
			"Hero", "Hero", "Human", "Male", 1, modifierApproach));
		actors.add(characterBuilder.buildCharacter(
			"Paladin", "Paladin", "Human", "Female", 1, modifierApproach));
		actors.add(characterBuilder.buildCharacter(
			"Burglar", "Burglar", "Human", "Male", 1, modifierApproach));
		actors.add(characterBuilder.buildCharacter(
			"Ranger", "Ranger", "Human", "Female", 1, modifierApproach));
		actors.add(characterBuilder.buildCharacter(
			"Priest", "Priest", "Human", "Male", 1, modifierApproach));
		actors.add(characterBuilder.buildCharacter(
			"Sorcerer", "Sorcerer", "Human", "Female", 1, modifierApproach));
		if (actors.size() != PARTY_SIZE)
		{
			throw new IllegalStateException("temple run requires a party of " + PARTY_SIZE);
		}
		return new PlayerParty(actors);
	}

	/*-------------------------------------------------------------------------*/
	private void enterFromHub()
	{
		MazeScript script = db.getMazeScript(TempleStairLinks.HUB_DESCEND_SCRIPT);
		maze.appendEvents(script.getEvents());
		CombatDriver.drainEvents(maze);
	}

	/*-------------------------------------------------------------------------*/
	private TempleRunMetrics.FloorMetrics walkFloor(
		Zone zone,
		int depth,
		Config config,
		PlayerParty party)
	{
		TempleRunMetrics.FloorMetrics floor = new TempleRunMetrics.FloorMetrics();
		floor.depth = depth;

		HarnessUi harnessUi = null;
		if (maze.getUi() instanceof HarnessUi ui)
		{
			harnessUi = ui;
			harnessUi.resetLootBaseCostTotal();
		}

		int hpBefore = totalCurrentHp(party);
		int xpBefore = totalExperience(party);

		MazeWalker.LiveWalkConfig walkConfig = new MazeWalker.LiveWalkConfig();
		walkConfig.maxSteps = config.maxTileStepsPerFloor;
		walkConfig.maxCombatRounds = config.maxCombatRounds;

		floor.tileSteps = new MazeWalker().walkLive(maze, zone, walkConfig);

		for (CombatStatistics stats : walkConfig.combatStats)
		{
			floor.combats++;
			floor.combatRounds += stats.getCombatRounds();
		}

		floor.partyHpLost = Math.max(0, hpBefore - totalCurrentHp(party));
		floor.xpGained = Math.max(0, totalExperience(party) - xpBefore);
		if (harnessUi != null)
		{
			floor.lootBaseCost = harnessUi.getLootBaseCostTotal();
		}
		return floor;
	}

	/*-------------------------------------------------------------------------*/
	private boolean descendToNextDepth(Zone zone, Config config)
	{
		Point down = TempleFloorDressing.findStairsDownPortalFrom(zone);
		if (down == null)
		{
			return false;
		}

		Portal portal = findPortal(zone, down, TempleStairLinks.DESCEND_NEXT_SCRIPT);
		if (portal == null)
		{
			return false;
		}

		MazeScript script = db.getMazeScript(portal.getMazeScript());
		maze.appendEvents(script.getEvents());
		CombatDriver.drainEvents(maze);
		return maze.getCurrentZone() != null
			&& TempleStairLinks.FLOOR_ZONE.equals(maze.getCurrentZone().getName());
	}

	/*-------------------------------------------------------------------------*/
	private static boolean walkTo(Maze maze, Zone zone, Point goal, int maxSteps)
	{
		ZoneScorer zs = new ZoneScorer();
		Point current = maze.getPlayerPos();
		int steps = 0;

		while (current != null && !current.equals(goal) && steps < maxSteps)
		{
			List<Point> path = zs.findPath(zone, current, goal);
			if (path.size() < 2)
			{
				return current.equals(goal);
			}

			Point next = path.get(1);
			maze.appendEvents(new MovePartyEvent(next, facingToward(current, next)));
			CombatDriver.drainEvents(maze);

			while (maze.getCurrentCombat() != null)
			{
				CombatDriver.runToCompletion(maze, CombatDriver.DEFAULT_MAX_ROUNDS);
			}

			current = maze.getPlayerPos();
			steps++;
		}

		return current != null && current.equals(goal);
	}

	/*-------------------------------------------------------------------------*/
	private static int facingToward(Point from, Point to)
	{
		int dx = to.x - from.x;
		int dy = to.y - from.y;
		if (dx > 0)
		{
			return CrusaderEngine.Facing.EAST;
		}
		if (dx < 0)
		{
			return CrusaderEngine.Facing.WEST;
		}
		if (dy > 0)
		{
			return CrusaderEngine.Facing.SOUTH;
		}
		if (dy < 0)
		{
			return CrusaderEngine.Facing.NORTH;
		}
		return CrusaderEngine.Facing.NORTH;
	}

	/*-------------------------------------------------------------------------*/
	private static Portal findPortal(Zone zone, Point from, String scriptName)
	{
		if (zone.getPortals() == null)
		{
			return null;
		}
		for (Portal portal : zone.getPortals())
		{
			if (scriptName.equals(portal.getMazeScript()) && from.equals(portal.getFrom()))
			{
				return portal;
			}
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private void applyPendingLevelUps(PlayerParty party)
	{
		Leveler leveler = new Leveler();
		for (PlayerCharacter pc : party.getPlayerCharacters())
		{
			while (pc.isLevelUpPending())
			{
				characterBuilder.levelUp(pc, modifierApproach, leveler);
			}
		}
	}

	private static int totalCurrentHp(PlayerParty party)
	{
		int sum = 0;
		for (UnifiedActor a : party.getActors())
		{
			sum += a.getHitPoints().getCurrent();
		}
		return sum;
	}

	private static int totalExperience(PlayerParty party)
	{
		int sum = 0;
		for (PlayerCharacter pc : party.getPlayerCharacters())
		{
			sum += pc.getExperience();
		}
		return sum;
	}

	private static int totalLevels(PlayerParty party)
	{
		int sum = 0;
		for (PlayerCharacter pc : party.getPlayerCharacters())
		{
			sum += pc.getLevel();
		}
		return sum;
	}

	/*-------------------------------------------------------------------------*/
	private void writeHtmlReport(Config config, TempleRunMetrics metrics, PlayerParty party)
	{
		HarnessRunReport report = toReport(metrics, party);
		Path path = config.htmlReportPath != null
			? config.htmlReportPath
			: Path.of("build", "test-reports", "dungeon-run-" + config.runSeed + ".html");
		try
		{
			HarnessHtmlReport.write(path, report);
		}
		catch (IOException e)
		{
			throw new RuntimeException("failed to write headless run HTML report: " + path, e);
		}
	}

	private static HarnessRunReport toReport(TempleRunMetrics metrics, PlayerParty party)
	{
		HarnessRunReport report = new HarnessRunReport();
		report.title = "Temple of Wasud — headless dungeon run";
		report.seed = metrics.getRunSeed();
		if (metrics.isWiped())
		{
			report.outcome = "Wipe";
		}
		else if (metrics.getMaxDepthReached() >= TempleDepthScaler.PLAYABLE_MAX_DEPTH)
		{
			report.outcome = "Clear";
		}
		else
		{
			report.outcome = "Incomplete";
		}
		report.maxDepthReached = metrics.getMaxDepthReached();
		report.startTurn = metrics.getStartTurn();
		report.endTurn = metrics.getEndTurn();
		report.totalCombats = metrics.getTotalCombats();
		report.totalCombatRounds = metrics.getTotalCombatRounds();
		report.totalPartyHpLost = metrics.getTotalPartyHpLost();
		report.totalLootBaseCost = metrics.getTotalLootBaseCost();
		report.totalXpGained = metrics.getTotalXpGained();
		report.levelsGained = metrics.getLevelsGained();
		Log log = Maze.getLog();
		if (log != null)
		{
			report.logPath = log.getLogPath();
		}
		if (party != null)
		{
			report.gold = party.getGold();
			report.partySize = party.size();
			report.partyAlive = party.numAlive();
			report.party.addAll(HarnessRunReport.snapshotParty(party));
		}
		for (TempleRunMetrics.FloorMetrics floor : metrics.getFloors())
		{
			HarnessRunReport.Floor f = new HarnessRunReport.Floor();
			f.depth = floor.depth;
			f.tileSteps = floor.tileSteps;
			f.combats = floor.combats;
			f.combatRounds = floor.combatRounds;
			f.partyHpLost = floor.partyHpLost;
			f.lootBaseCost = floor.lootBaseCost;
			f.xpGained = floor.xpGained;
			report.floors.add(f);
		}
		return report;
	}
}
