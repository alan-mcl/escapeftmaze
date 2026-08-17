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

package mclachlan.maze.balance;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.*;
import mclachlan.maze.game.event.MovePartyEvent;
import mclachlan.maze.game.event.ZoneChangeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.combat.CombatStatistics;

/**
 * Walks every navigable tile in a zone. Static analysis via {@link #walk(Zone)}
 * or live movement via {@link #walkLive(Maze, Zone, LiveWalkConfig)}.
 */
public class MazeWalker
{
	public static final class LiveWalkConfig
	{
		public int maxSteps = 500;
		public int maxCombatRounds = CombatDriver.DEFAULT_MAX_ROUNDS;
		public List<CombatStatistics> combatStats = new ArrayList<>();
	}

	/*-------------------------------------------------------------------------*/
	public void walk(Zone zone)
	{
		List<Listener> listeners = getListeners();
		ZoneScorer zs = new ZoneScorer();
		List<Tile> tiles = zs.getNavigableTiles(zone);

		GameState gs = new GameState(
			zone.getName(),
			new DifficultyLevel(),
			zone.getPlayerOrigin(),
			ZoneChangeEvent.Facing.NORTH,
			0,
			0,
			new ArrayList<String>(),
			0,
			0);

		for (Tile t : tiles)
		{
			gs.setPlayerPos(zone.getPoint(t));

			for (Listener listener : listeners)
			{
				listener.walk(gs);
			}
		}

		System.out.println(zone.getName());
		for (Listener listener : listeners)
		{
			System.out.println(listener.describe());
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Cover every navigable tile using {@link MovePartyEvent} on a live {@link Maze}.
	 *
	 * @return number of tile steps taken, or -1 if step cap exceeded
	 */
	public int walkLive(Maze maze, Zone zone, LiveWalkConfig config)
	{
		ZoneScorer zs = new ZoneScorer();
		List<Tile> targets = new ArrayList<>(zs.getNavigableTiles(zone));
		Set<Point> visited = new HashSet<>();
		Point current = maze.getPlayerPos();
		if (current != null)
		{
			visited.add(current);
		}

		if (HarnessRunProgress.isEnabled())
		{
			HarnessRunProgress.line(
				"  walk %s: %d navigable tiles, step cap %d, start %s",
				zone.getName(),
				targets.size(),
				config.maxSteps,
				current);
		}

		int steps = 0;
		while (visited.size() < targets.size() && steps < config.maxSteps)
		{
			Tile nextTarget = null;
			int bestDistance = Integer.MAX_VALUE;
			for (Tile tile : targets)
			{
				Point p = zone.getPoint(tile);
				if (visited.contains(p))
				{
					continue;
				}
				List<Point> path = zs.findPath(zone, current, p);
				if (path.size() >= 2 && path.size() < bestDistance)
				{
					bestDistance = path.size();
					nextTarget = tile;
				}
			}

			if (nextTarget == null)
			{
				break;
			}

			Point goal = zone.getPoint(nextTarget);
			List<Point> path = zs.findPath(zone, current, goal);
			if (path.size() < 2)
			{
				break;
			}

			Point next = path.get(1);
			int facing = facingToward(current, next);
			maze.appendEvents(maze.incTurn(true));
			maze.appendEvents(new MovePartyEvent(next, facing));
			CombatDriver.drainEvents(maze);
			int combatsBefore = config.combatStats.size();
			resolveCombat(maze, config);

			current = next;
			visited.add(current);
			steps++;

			if (HarnessRunProgress.isEnabled()
				&& (steps == 1 || steps % 25 == 0 || config.combatStats.size() > combatsBefore))
			{
				HarnessRunProgress.line(
					"  walk %s: step %d cap %d visited %d/%d pos %s turn %d combats %d",
					zone.getName(),
					steps,
					config.maxSteps,
					visited.size(),
					targets.size(),
					current,
					maze.getTurnNr(),
					config.combatStats.size());
			}

			if (maze.getParty() == null || maze.getParty().numAlive() == 0)
			{
				return steps;
			}
		}

		if (HarnessRunProgress.isEnabled() && visited.size() < targets.size())
		{
			HarnessRunProgress.line(
				"  walk %s: stopped early at %d/%d tiles (%d steps, cap %d)",
				zone.getName(),
				visited.size(),
				targets.size(),
				steps,
				config.maxSteps);
		}

		return steps >= config.maxSteps ? -1 : steps;
	}

	/*-------------------------------------------------------------------------*/
	private static void resolveCombat(Maze maze, LiveWalkConfig config)
	{
		while (maze.getCurrentCombat() != null)
		{
			if (HarnessRunProgress.isEnabled())
			{
				HarnessRunProgress.line(
					"  combat at turn %d (round cap %d)",
					maze.getTurnNr(),
					config.maxCombatRounds);
			}
			CombatStatistics stats = CombatDriver.runToCompletion(maze, config.maxCombatRounds);
			config.combatStats.add(stats);
			if (HarnessRunProgress.isEnabled())
			{
				HarnessRunProgress.line(
					"  combat done: %d rounds, party %d alive, still in combat=%s",
					stats.getCombatRounds(),
					maze.getParty() != null ? maze.getParty().numAlive() : 0,
					maze.getCurrentCombat() != null);
			}
			if (maze.getCurrentCombat() != null)
			{
				break;
			}
		}
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

	private List<Listener> getListeners()
	{
		List<Listener> result = new ArrayList<Listener>();

		result.add(new TileCountListener());
		result.add(new EncounterListener());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Database db = new Database(
			new mclachlan.maze.data.v2.V2Loader(),
			new mclachlan.maze.data.v2.V2Saver(),
			Maze.getStubCampaign());

		new MazeWalker().walk(db.getZone("Gatehouse"));
	}

	/*-------------------------------------------------------------------------*/
	public static abstract class Listener
	{
		public abstract void walk(GameState gs);

		public abstract String describe();
	}
}
