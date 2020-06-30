package mclachlan.maze.map;

import java.util.*;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.DungeonGen;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.game.MazeEvent;

/**
 *
 */
public class MapGenZoneScript extends ZoneScript
{
	private DungeonDecorator decorator;

	public MapGenZoneScript(DungeonDecorator decorator)
	{
		this.decorator = decorator;
	}

	@Override
	public void init(Zone zone, long turnNr)
	{
		DungeonGen gen = new Noise4jDungeonGen();

		// todo: seed
		gen.generate(zone, new Random().nextInt(2), decorator);
	}

	@Override
	public List<MazeEvent> endOfTurn(Zone zone, long turnNr)
	{
		return null;
	}

	public  interface DungeonDecorator
	{
		Wall getRoomWall(Grid grid, int x, int y);

		Wall getCorridorWall(Grid grid, int x, int y);

		Wall getPortal(Grid grid, int x, int y);
	}
}
