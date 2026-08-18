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
import mclachlan.dungeongen.*;
import mclachlan.dungeongen.fragment.FragmentDungeonGen;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.*;
import mclachlan.maze.map.script.Encounter;

/**
 * Procedural temple floor zone script. Layout via campaign {@link DungeonGens}
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
		zone.setTilesVisitedKey(TempleFloorLabels.tilesVisitedKey(dungeonLevel));

		TempleEnvironment environment = TempleEnvironment.forFloor(dungeonLevel);
		environment.applyToCrusaderTiles(zone.getMap());

		TempleUsageTheme usageTheme = TempleUsageTheme.forFloor(dungeonLevel, environment);

		int seed = TempleSeeds.floorSeed(dungeonLevel);

		DECORATOR.prepareFloor(dungeonLevel, environment);

		DungeonGenContext ctx = TempleStairLinks.buildGenContext(dungeonLevel);
		DungeonGenResult result = createDungeonGen(zone, dungeonLevel)
			.generate(zone, seed, dungeonLevel, DECORATOR, ctx);

		environment.applyToZone(zone);
		environment.registerPaletteTextures(zone.getMap());

		TempleStairwellDresser.apply(zone, dungeonLevel, result.stairwells());
		TempleStairLinks.persistPlan(dungeonLevel, result.stairwells());
		zone.setPlayerOrigin(result.playerOrigin());
		MazeVariables.set(
			TempleSeeds.startRoomVar(dungeonLevel),
			Integer.toString(result.startingRoomIndex()));

		TempleMagicDresser.dress(zone, dungeonLevel, result);

		Set<Point> avoid = stairAvoidTiles(result.stairwells());
		TempleFloorDressing.dress(zone, dungeonLevel, avoid, result, usageTheme);
		TempleEnvironmentFlavour.attachToLandingTile(zone, dungeonLevel, environment, usageTheme);
		TempleLighting.DressResult lighting = TempleLighting.dress(
			zone, dungeonLevel, environment, avoid, result);
		if (DungeonGens.NOISE4J.equals(defaultGeneratorId(dungeonLevel)))
		{
			TempleUsageDressing.dress(
				zone, dungeonLevel, environment, usageTheme, avoid, result, lighting);
		}
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
	protected DungeonGen createDungeonGen(Zone zone, int dungeonLevel)
	{
		String id = defaultGeneratorId(dungeonLevel);
		if (DungeonGens.FRAGMENT.equals(id))
		{
			TempleLayoutUsageTheme theme = TempleLayoutUsageTheme.forFloor(dungeonLevel);
			return new FragmentDungeonGen(FragmentDungeonGen.Options.of(theme.usageId()));
		}
		return DungeonGens.create(id);
	}

	private static String defaultGeneratorId(int dungeonLevel)
	{
		try
		{
			String id = Maze.getInstance().getCampaign().getDefaultDungeonGenerator();
			if (id != null && !id.isEmpty())
			{
				return id;
			}
		}
		catch (Exception e)
		{
			// fall through
		}
		return DungeonGens.NOISE4J;
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
		private int dungeonLevel;
		private EncounterTable rosterTable;
		private TempleEnvironment environment;

		void prepareFloor(int depth, TempleEnvironment environment)
		{
			this.dungeonLevel = depth;
			this.environment = environment;
			this.rosterTable = TempleFoeRoster.forFloor(depth);
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
				environment.doorTexture());

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
		public Encounter getEncounter(Zone zone, int x, int y, int dungeonLevel, int roomIndex)
		{
			String mazeVar = TempleSeeds.encounterVar(this.dungeonLevel, roomIndex);
			return new Encounter(
				rosterTable,
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
			mclachlan.crusader.Texture wallTex = environment.wallTexture();
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