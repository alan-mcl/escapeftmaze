package mclachlan.maze.campaign.temple;

import mclachlan.crusader.Wall;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.MapGenZoneScript;

/**
 *
 */
public class TempleGeneratorMazeScript extends MapGenZoneScript
{
	public TempleGeneratorMazeScript()
	{
		super(new TempleDecorator());
	}

	private static class TempleDecorator implements DungeonDecorator
	{
		@Override
		public Wall getRoomWall(Grid grid, int x, int y)
		{
			return new Wall(
				Database.getInstance().getMazeTexture("CITY_WALL_1").getTexture(),
				null,
				true,
				true,
				1,
				null,
				null);
		}

		@Override
		public Wall getCorridorWall(Grid grid, int x, int y)
		{
			return new Wall(
				Database.getInstance().getMazeTexture("DUNGEON_WALL_1").getTexture(),
				null,
				true,
				true,
				1,
				null,
				null);
		}
	}
}
