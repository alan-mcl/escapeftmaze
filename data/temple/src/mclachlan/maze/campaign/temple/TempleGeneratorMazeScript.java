package mclachlan.maze.campaign.temple;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.*;
import mclachlan.maze.map.script.Encounter;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.NpcFaction;

/**
 *
 */
public class TempleGeneratorMazeScript extends MapGenZoneScript
{
	public TempleGeneratorMazeScript()
	{
		super(new TempleDecorator());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getDungeonLevel(Zone zone)
	{
		// expects zone names like "xxxxx.lvl"

		String name = zone.getName();

		return Integer.parseInt(name.substring(name.lastIndexOf(".")+1));
	}

	/*-------------------------------------------------------------------------*/
	private static class TempleDecorator implements DungeonDecorator
	{
		private static int portalCounter = 0;
		private static int encounterCounter = 0;

		@Override
		public Wall getRoomWall(Grid grid, int x, int y)
		{
			return new Wall(
				new Texture[]{Database.getInstance().getMazeTexture("DUNGEON_WALL_1").getTexture()},
				null,
				true,
				true,
				1,
				null,
				null,
				null);
		}

		@Override
		public Wall getCorridorWall(Grid grid, int x, int y)
		{
			return new Wall(
				new Texture[]{Database.getInstance().getMazeTexture("DUNGEON_WALL_1").getTexture()},
				null,
				true,
				true,
				1,
				null,
				null,
				null);
		}

		@Override
		public List<Object> handlePortal(Grid grid,
					Point from,
					int fromFacing,
					Point to,
					int toFacing)
		{
			Wall wall = new Wall(
				new Texture[]{Database.getInstance().getMazeTexture("DUNGEON_WALL_1").getTexture()},
				new Texture[]{Database.getInstance().getMazeTexture("CITY_DOOR_1").getTexture()},
				true,
				true,
				1,
				null,
				null,
				null);

			Portal portal = new Portal(
				null, // todo: maze var
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
				new int[]{0,0,0,0,0,0,0,0},
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
			// todo

			String mazeVar = zone.getName()+".encounter."+(encounterCounter++);

			FoeEntryRow fer = new FoeEntryRow("Fruit Bat", Dice.d4);

			GroupOfPossibilities<FoeEntryRow> gop = new GroupOfPossibilities<>();
			gop.add(fer, 100);

			FoeEntry fe = new FoeEntry(mazeVar, gop);

			PercentageTable<FoeEntry> percT = new PercentageTable<>();
			percT.add(fe, 100);

			EncounterTable encounterTable = new EncounterTable(
				mazeVar,
				percT);

			return new Encounter(
				encounterTable,
				mazeVar,
				NpcFaction.Attitude.ATTACKING,
				Combat.AmbushStatus.NONE,
				null,
				null);
		}
	}
}
