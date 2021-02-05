package mclachlan.dungeongen;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.MapGenZoneScript;
import mclachlan.maze.map.Zone;

public interface DungeonGen
{
	List<MazeEvent> generate(Zone base, long seed, int dungeonLevel,
		MapGenZoneScript.DungeonDecorator decorator);
}
