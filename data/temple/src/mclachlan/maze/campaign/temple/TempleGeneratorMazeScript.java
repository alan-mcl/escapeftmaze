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
import mclachlan.dungeongen.DungeonGenContext;
import mclachlan.dungeongen.DungeonGenResult;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.*;
import mclachlan.maze.map.script.Encounter;

/**
 * Procedural temple floor zone script. Layout via {@link TempleLayoutPolicy}
 * ({@link mclachlan.dungeongen.DungeonGen}); stair portals and loot dressing after gen.
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

		TempleFloorShell.ensureGenSize(zone);
		zone.setDisplayName(TempleFloorLabels.displayName(dungeonLevel));

		int seed = TempleSeeds.floorSeed(dungeonLevel);

		DECORATOR.resetCounters();

		DungeonGenContext ctx = TempleStairLinks.buildGenContext(dungeonLevel);
		DungeonGenResult result = createDungeonGen(zone, dungeonLevel)
			.generate(zone, seed, dungeonLevel, DECORATOR, ctx);

		TempleStairwellDresser.apply(zone, dungeonLevel, result.stairwells());
		TempleStairLinks.persistPlan(dungeonLevel, result.stairwells());
		zone.setPlayerOrigin(result.playerOrigin());

		Set<Point> avoid = stairAvoidTiles(result.stairwells());
		TempleFloorDressing.dress(zone, dungeonLevel, avoid);
		TempleStairLinks.clearTransition();

		return result.events();
	}

	/*-------------------------------------------------------------------------*/
	private static Set<Point> stairAvoidTiles(mclachlan.dungeongen.StairwellPlan plan)
	{
		Set<Point> avoid = new HashSet<>();
		if (plan == null)
		{
			return avoid;
		}
		if (plan.stairsUp() != null)
		{
			avoid.add(plan.stairsUp().from());
		}
		if (plan.stairsDown() != null)
		{
			avoid.add(plan.stairsDown().from());
		}
		return avoid;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	protected mclachlan.dungeongen.DungeonGen createDungeonGen(Zone zone, int dungeonLevel)
	{
		return TempleLayoutPolicy.forDepth(dungeonLevel);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getDungeonLevel(Zone zone)
	{
		int depth = TempleSeeds.getDepth();
		if (depth > 0)
		{
			return depth;
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
		public mclachlan.crusader.Wall getRoomWall(mclachlan.dungeongen.noise4j.map.Grid grid, int x, int y)
		{
			return wall(null);
		}

		@Override
		public mclachlan.crusader.Wall getCorridorWall(mclachlan.dungeongen.noise4j.map.Grid grid, int x, int y)
		{
			return wall(null);
		}

		@Override
		public List<Object> handlePortal(mclachlan.dungeongen.noise4j.map.Grid grid,
			Point from,
			int fromFacing,
			Point to,
			int toFacing)
		{
			mclachlan.crusader.Wall wall = wall(
				mclachlan.maze.data.Database.getInstance().getMazeTexture("CITY_DOOR_1").getTexture());

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
			int index = encounterCounter++;
			String mazeVar = TempleSeeds.encounterVar(dungeonLevel, index);
			String tableName = TempleDepthScaler.encounterTableName(dungeonLevel);
			EncounterTable table = mclachlan.maze.data.Database.getInstance().getEncounterTables().get(tableName);
			if (table == null)
			{
				table = mclachlan.maze.data.Database.getInstance().getEncounterTable(
					TempleDepthScaler.encounterTableName(1));
			}
			if (table == null)
			{
				throw new mclachlan.maze.util.MazeException("Missing temple encounter table [" + tableName + "]");
			}

			return new Encounter(
				table,
				mazeVar,
				mclachlan.maze.stat.npc.NpcFaction.Attitude.ATTACKING,
				mclachlan.maze.stat.combat.Combat.AmbushStatus.NONE,
				null,
				null,
				null,
				null,
				false);
		}

		private mclachlan.crusader.Wall wall(mclachlan.crusader.Texture doorTexture)
		{
			mclachlan.crusader.Texture wallTex =
				mclachlan.maze.data.Database.getInstance().getMazeTexture("DUNGEON_WALL_1").getTexture();
			mclachlan.crusader.Texture[] door = doorTexture == null ? null : new mclachlan.crusader.Texture[]{doorTexture};
			return new mclachlan.crusader.Wall(
				new mclachlan.crusader.Texture[]{wallTex},
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