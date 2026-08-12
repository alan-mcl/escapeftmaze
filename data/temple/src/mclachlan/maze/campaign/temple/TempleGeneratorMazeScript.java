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
import mclachlan.crusader.Texture;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.DungeonGen;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.*;
import mclachlan.maze.map.script.Encounter;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.util.MazeException;

/**
 * Procedural temple floor zone script. Uses existing Noise4j generation with a
 * temple-side decorator and {@link TempleSeeds} for reproducible layouts, then
 * {@link TempleFloorDressing} for stairs and loot.
 */
public class TempleGeneratorMazeScript extends MapGenZoneScript
{
	private static final TempleDecorator DECORATOR = new TempleDecorator();

	/*-------------------------------------------------------------------------*/
	public TempleGeneratorMazeScript()
	{
		super(DECORATOR);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> init(Zone zone, long turnNr)
	{
		TempleSeeds.ensureRunSeed();

		int dungeonLevel = getDungeonLevel(zone);
		TempleSeeds.setDepth(dungeonLevel);

		int seed = TempleSeeds.floorSeed(dungeonLevel);

		// Reset per-generation encounter counters (decorator is static).
		DECORATOR.resetCounters();

		DungeonGen gen = new Noise4jDungeonGen();
		List<MazeEvent> events = gen.generate(zone, seed, dungeonLevel, DECORATOR);
		TempleFloorDressing.dress(zone, dungeonLevel);
		return events;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getDungeonLevel(Zone zone)
	{
		String depthVar = mclachlan.maze.game.MazeVariables.get(TempleSeeds.DEPTH);
		if (depthVar != null && !depthVar.isEmpty())
		{
			return Integer.parseInt(depthVar);
		}

		String name = zone.getName();
		int dot = name.lastIndexOf('.');
		if (dot >= 0 && dot < name.length() - 1)
		{
			try
			{
				return Integer.parseInt(name.substring(dot + 1));
			}
			catch (NumberFormatException e)
			{
				// fall through
			}
		}
		return 1;
	}

	/*-------------------------------------------------------------------------*/
	private static class TempleDecorator implements DungeonDecorator
	{
		private int encounterCounter = 0;

		void resetCounters()
		{
			encounterCounter = 0;
		}

		@Override
		public Wall getRoomWall(Grid grid, int x, int y)
		{
			return wall(null);
		}

		@Override
		public Wall getCorridorWall(Grid grid, int x, int y)
		{
			return wall(null);
		}

		@Override
		public List<Object> handlePortal(Grid grid,
			Point from,
			int fromFacing,
			Point to,
			int toFacing)
		{
			Wall wall = wall(
				Database.getInstance().getMazeTexture("CITY_DOOR_1").getTexture());

			Portal portal = new Portal(
				null,
				Portal.State.UNLOCKED,
				from,
				fromFacing,
				to,
				toFacing,
				true,
				true,
				true,
				true,
				1,
				1,
				new int[]{0, 0, 0, 0, 0, 0, 0, 0},
				new BitSet(),
				null,
				false,
				"generic door creak",
				null);

			return Arrays.asList(wall, portal);
		}

		@Override
		public Encounter getEncounter(Zone zone, int x, int y, int dungeonLevel)
		{
			String mazeVar = zone.getName() + ".encounter." + (encounterCounter++);
			String tableName = TempleFloorDressing.encounterTableName(dungeonLevel);
			EncounterTable table = Database.getInstance().getEncounterTables().get(tableName);
			if (table == null)
			{
				// fall back to depth 1 if a deeper table is not authored yet
				table = Database.getInstance().getEncounterTable(
					TempleFloorDressing.encounterTableName(1));
			}
			if (table == null)
			{
				throw new MazeException("Missing temple encounter table [" + tableName + "]");
			}

			return new Encounter(
				table,
				mazeVar,
				NpcFaction.Attitude.ATTACKING,
				Combat.AmbushStatus.NONE,
				null,
				null,
				null,
				null,
				false);
		}

		private Wall wall(Texture doorTexture)
		{
			Texture wallTex =
				Database.getInstance().getMazeTexture("DUNGEON_WALL_1").getTexture();
			Texture[] door = doorTexture == null ? null : new Texture[]{doorTexture};
			return new Wall(
				new Texture[]{wallTex},
				door,
				true,
				true,
				1,
				null,
				null,
				null);
		}
	}
}
