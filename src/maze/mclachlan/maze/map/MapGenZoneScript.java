package mclachlan.maze.map;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.DungeonGen;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.Encounter;

/**
 *
 */
public abstract class MapGenZoneScript extends ZoneScript
{
	private DungeonDecorator decorator;
	private static String SEED_PREFIX = "map.seed.";

	/*-------------------------------------------------------------------------*/
	public MapGenZoneScript(DungeonDecorator decorator)
	{
		this.decorator = decorator;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> init(Zone zone, long turnNr)
	{
		DungeonGen gen = new Noise4jDungeonGen();

		int dungeonLevel = getDungeonLevel(zone);

		String seed = MazeVariables.get(SEED_PREFIX + zone.getName());
		if (seed == null)
		{
//			seed = ""+new Random().nextInt();
			seed = ""+new Random().nextInt(2);
			MazeVariables.set(SEED_PREFIX+zone.getName(), seed);
		}

		return gen.generate(zone, Integer.parseInt(seed), dungeonLevel, decorator);
	}

	/*-------------------------------------------------------------------------*/
	public abstract int getDungeonLevel(Zone zone);

	@Override
	public List<MazeEvent> endOfTurn(Zone zone, long turnNr)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public interface DungeonDecorator
	{
		Wall getRoomWall(Grid grid, int x, int y);

		Wall getCorridorWall(Grid grid, int x, int y);

		List<Object> handlePortal(Grid grid,
			Point from,
			int fromFacing,
			Point to,
			int toFacing);

		Encounter getEncounter(Zone zone, int x, int y,
			int dungeonLevel);
	}
}
