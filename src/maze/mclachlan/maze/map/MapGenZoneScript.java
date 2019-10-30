package mclachlan.maze.map;

import java.util.*;
import mclachlan.dungeongen.DungeonGen;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.maze.game.MazeEvent;

/**
 *
 */
public class MapGenZoneScript extends ZoneScript
{
	@Override
	public void init(Zone zone, long turnNr)
	{
		DungeonGen gen = new Noise4jDungeonGen();

		gen.generate(zone, new Random().nextInt(2));
	}

	@Override
	public List<MazeEvent> endOfTurn(Zone zone, long turnNr)
	{
		return null;
	}
}
