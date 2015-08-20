package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.EncounterActorsEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class TempleOfTheGateDragonRod extends TileScript
{
	public static final String DRAGON_ROD_USED = "temple.of.the.gate.dragon.rod.used";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handleUseItem(Maze maze, Point tile, int facing,
		Item item, PlayerCharacter user)
	{
		if (item.getName().equals("Dragon Rod") &&
			!MazeVariables.getBoolean(DRAGON_ROD_USED))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(
				new FlavourTextEvent("You strike the bell with the heavy " +
					"golden rod that you found in the hoard of the dragon Igor.",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(
				new FlavourTextEvent("\n\nA single clear note rings out, " +
					"deafening in the small room...",
					MazeEvent.Delay.WAIT_ON_CLICK, false));
			result.add(new SetMazeVariableEvent(DRAGON_ROD_USED, "true"));
			result.add(new EncounterActorsEvent(null, "temple.of.the.gate.ghost.of.igor"));

			return result;
		}

		return null;
	}

}