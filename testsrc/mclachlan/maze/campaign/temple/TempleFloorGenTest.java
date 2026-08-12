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
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.DifficultyLevel;
import mclachlan.maze.game.GameState;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Loot;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.AttackIntention;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.CombatAction;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 2 temple campaign smoke: fixed-seed floor gen (encounters, stairs, loot,
 * reachability) and a thin combat pass against depth-1 encounter foes.
 * <p>
 * Loads real {@code data/temple} + inherited {@code data/default} content.
 */
public class TempleFloorGenTest extends MazeTestSupport
{
	private static final long RUN_SEED = 42L;

	/*-------------------------------------------------------------------------*/
	@Test
	void generatedFloorHasEncountersStairsLootAndReachableExit() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Zone zone = generateDepth1(db);
		Point origin = zone.getPlayerOrigin();
		assertNotNull(origin);

		List<Point> encounters = TempleFloorDressing.findEncounterTiles(zone);
		assertFalse(encounters.isEmpty(), "expected door-room encounters");

		Point stairs = TempleFloorDressing.findStairsTile(zone);
		assertNotNull(stairs, "expected stairs-up script on a tile");
		assertFalse(stairs.equals(origin), "stairs should not sit on the spawn tile");

		assertTrue(countLootScripts(zone) >= 1, "expected at least one loot script");

		assertTrue(canReach(zone, origin, stairs),
			"spawn should reach stairs via open tiles/portals");

		assertEquals("1", MazeVariables.get(TempleSeeds.DEPTH));
		assertNotNull(MazeVariables.get(TempleSeeds.RUN_SEED));
		assertNotNull(MazeVariables.get(TempleSeeds.FLOOR_SEED_PREFIX + "1"));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void depth1EncounterTableSpawnsFoesAndCombatRuns() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		Maze maze = TempleCampaignHarness.bootMaze(db);

		EncounterTable table = db.getEncounterTable(TempleFloorDressing.encounterTableName(1));
		assertNotNull(table);

		seed(99L);
		FoeEntry entry = table.getEncounterTable().getRandomItem();
		assertNotNull(entry);
		List<FoeGroup> foeGroups = entry.generate();
		assertFalse(foeGroups.isEmpty());

		int foeCount = 0;
		int foeHpBefore = 0;
		for (FoeGroup fg : foeGroups)
		{
			for (UnifiedActor a : fg.getActors())
			{
				foeCount++;
				foeHpBefore += a.getHitPoints().getCurrent();
			}
		}
		assertTrue(foeCount > 0, "encounter should spawn foes");
		assertTrue(foeHpBefore > 0, "spawned foes should have hit points");

		PlayerCharacter pc = TestData.newCombatPc("TempleTester");
		PlayerParty party = new PlayerParty(new ArrayList<>(List.of((UnifiedActor)pc)));
		maze.setParty(party);

		DifficultyLevel difficulty = db.getDifficultyLevels().values().iterator().next();
		assertNotNull(difficulty);
		maze.setGameStateNoZone(new GameState(
			"temple.1",
			difficulty,
			new Point(0, 0),
			0,
			0,
			0,
			List.of("TempleTester"),
			1,
			0));

		Combat combat = new Combat(party, foeGroups, null);
		int resolved = runBoundedCombat(combat, party, foeGroups);

		assertTrue(resolved > 0, "combat should resolve at least one event");

		int foeHpAfter = 0;
		for (FoeGroup fg : foeGroups)
		{
			for (UnifiedActor a : fg.getActors())
			{
				foeHpAfter += a.getHitPoints().getCurrent();
			}
		}
		assertTrue(
			foeHpAfter < foeHpBefore
				|| pc.getHitPoints().getCurrent() < pc.getHitPoints().getMaximum()
				|| party.numAlive() == 0
				|| liveFoes(foeGroups) == 0,
			"combat should apply damage or terminate");
	}

	/*-------------------------------------------------------------------------*/
	private Zone generateDepth1(Database db)
	{
		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, "1");

		Zone zone = db.getZone("temple.1");
		assertNotNull(zone);
		assertTrue(zone.getScript() instanceof TempleGeneratorMazeScript);
		zone.getScript().init(zone, 0);
		return zone;
	}

	/*-------------------------------------------------------------------------*/
	private static int countLootScripts(Zone zone)
	{
		int n = 0;
		Tile[][] tiles = zone.getTiles();
		for (Tile[] row : tiles)
		{
			for (Tile tile : row)
			{
				if (tile == null || tile.getScripts() == null)
				{
					continue;
				}
				for (TileScript script : tile.getScripts())
				{
					if (script instanceof Loot)
					{
						n++;
					}
				}
			}
		}
		return n;
	}

	/*-------------------------------------------------------------------------*/
	private int runBoundedCombat(Combat combat, PlayerParty party,
		List<FoeGroup> foeGroups)
	{
		int resolved = 0;
		int round = 0;

		while (party.numAlive() > 0 && liveFoes(foeGroups) > 0 && round < 50)
		{
			round++;

			List<ActorActionIntention[]> foeIntentions = new ArrayList<>();
			for (FoeGroup fg : foeGroups)
			{
				List<UnifiedActor> foes = fg.getActors();
				ActorActionIntention[] arr = new ActorActionIntention[foes.size()];
				for (int i = 0; i < foes.size(); i++)
				{
					arr[i] = ((Foe)foes.get(i)).getCombatIntention();
				}
				foeIntentions.add(arr);
			}

			ActorActionIntention[] partyIntentions =
				new ActorActionIntention[party.getActors().size()];
			for (int i = 0; i < partyIntentions.length; i++)
			{
				PlayerCharacter actor = (PlayerCharacter)party.getActors().get(i);
				List<AttackWith> options = actor.getAttackWithOptions();
				partyIntentions[i] = new AttackIntention(
					foeGroups.get(0), combat, options.get(0));
			}

			Iterator actions = combat.combatRound(partyIntentions, foeIntentions);
			while (actions.hasNext())
			{
				CombatAction action = (CombatAction)actions.next();
				List<MazeEvent> events = combat.resolveAction(action);
				resolved += resolveAll(events);
				party.reorderPartyToCompensateForDeadCharacters();

				if (party.numAlive() == 0 || liveFoes(foeGroups) == 0)
				{
					break;
				}
			}

			resolved += resolveAll(combat.endRound());
		}

		if (liveFoes(foeGroups) == 0)
		{
			combat.endCombat();
		}

		return resolved;
	}

	/*-------------------------------------------------------------------------*/
	private int resolveAll(List<MazeEvent> events)
	{
		if (events == null)
		{
			return 0;
		}

		int count = 0;
		for (MazeEvent event : events)
		{
			count++;
			count += resolveAll(event.resolve());
		}
		return count;
	}

	/*-------------------------------------------------------------------------*/
	private static int liveFoes(List<FoeGroup> foeGroups)
	{
		int n = 0;
		for (FoeGroup fg : foeGroups)
		{
			n += fg.numAlive();
		}
		return n;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean canReach(Zone zone, Point start, Point goal)
	{
		Map map = zone.getMap();
		int width = map.getWidth();
		int length = map.getLength();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();

		boolean[][] seen = new boolean[length][width];
		ArrayDeque<Point> q = new ArrayDeque<>();
		q.add(start);
		seen[start.y][start.x] = true;

		java.util.Map<Point, List<Point>> portalLinks = new HashMap<>();
		if (zone.getPortals() != null)
		{
			for (Portal portal : zone.getPortals())
			{
				portalLinks.computeIfAbsent(portal.getFrom(), k -> new ArrayList<>()).add(portal.getTo());
				portalLinks.computeIfAbsent(portal.getTo(), k -> new ArrayList<>()).add(portal.getFrom());
			}
		}

		while (!q.isEmpty())
		{
			Point cur = q.removeFirst();
			if (cur.equals(goal))
			{
				return true;
			}

			tryStep(cur, CrusaderEngine.Facing.NORTH, width, length, horiz, vert, seen, q);
			tryStep(cur, CrusaderEngine.Facing.SOUTH, width, length, horiz, vert, seen, q);
			tryStep(cur, CrusaderEngine.Facing.WEST, width, length, horiz, vert, seen, q);
			tryStep(cur, CrusaderEngine.Facing.EAST, width, length, horiz, vert, seen, q);

			List<Point> links = portalLinks.get(cur);
			if (links != null)
			{
				for (Point next : links)
				{
					if (next.x >= 0 && next.y >= 0 && next.x < width && next.y < length
						&& !seen[next.y][next.x])
					{
						seen[next.y][next.x] = true;
						q.add(next);
					}
				}
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	private static void tryStep(
		Point cur,
		int facing,
		int width,
		int length,
		Wall[] horiz,
		Wall[] vert,
		boolean[][] seen,
		ArrayDeque<Point> q)
	{
		int nx = cur.x;
		int ny = cur.y;
		Wall wall;
		switch (facing)
		{
			case CrusaderEngine.Facing.NORTH ->
			{
				wall = horiz[cur.x + cur.y * width];
				ny = cur.y - 1;
			}
			case CrusaderEngine.Facing.SOUTH ->
			{
				wall = horiz[cur.x + (cur.y + 1) * width];
				ny = cur.y + 1;
			}
			case CrusaderEngine.Facing.WEST ->
			{
				wall = vert[cur.x + cur.y * (width + 1)];
				nx = cur.x - 1;
			}
			case CrusaderEngine.Facing.EAST ->
			{
				wall = vert[cur.x + cur.y * (width + 1) + 1];
				nx = cur.x + 1;
			}
			default ->
			{
				return;
			}
		}

		if (nx < 0 || ny < 0 || nx >= width || ny >= length)
		{
			return;
		}
		if (wall != null && wall.isSolid())
		{
			return;
		}
		if (!seen[ny][nx])
		{
			seen[ny][nx] = true;
			q.add(new Point(nx, ny));
		}
	}
}
