package mclachlan.dungeongen;

import mclachlan.maze.map.MapGenZoneScript;
import mclachlan.maze.map.Zone;

public interface DungeonGen
{
	DungeonGenResult generate(
		Zone base,
		long seed,
		int dungeonLevel,
		MapGenZoneScript.DungeonDecorator decorator,
		DungeonGenContext context);
}
