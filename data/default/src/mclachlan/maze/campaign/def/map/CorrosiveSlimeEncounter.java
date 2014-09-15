package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.campaign.def.npc.SirKay;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.EncounterEvent;
import mclachlan.maze.map.script.FlavourTextEvent;

/**
 *
 */
public class CorrosiveSlimeEncounter extends TileScript
{
	private static final String CORROSIVE_SLIME_ENCOUNTER = "corrosive.slime.encounter";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		if (MazeVariables.getBoolean(SirKay.SPAWN_CORROSIVE_SLIME) &&
			!MazeVariables.getBoolean(SirKay.CORROSIVE_SLIME_SLAIN))
		{
			return getList(
				new FlavourTextEvent("In the depths of the sewers, you " +
					"stumble across a gigantic, living slime!"),
				new EncounterEvent(CORROSIVE_SLIME_ENCOUNTER, CORROSIVE_SLIME_ENCOUNTER));
		}
		else
		{
			return null;
		}
	}
}