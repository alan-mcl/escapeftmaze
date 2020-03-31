package mclachlan.dungeongen;

import mclachlan.maze.map.MapGenZoneScript;
import mclachlan.maze.map.Zone;

public interface DungeonGen
{
	Zone generate(Zone base, long seed, MapGenZoneScript.DungeonDecorator decorator);
}
