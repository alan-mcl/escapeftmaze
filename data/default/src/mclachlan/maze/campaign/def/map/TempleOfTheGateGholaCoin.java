package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class TempleOfTheGateGholaCoin extends TileScript
{
	public static final String GHOLA_COIN_USED = "temple.of.the.gate.ghola.coin.used";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handleUseItem(Maze maze, Point tile, int facing,
		Item item, UnifiedActor user)
	{
		if (item.getName().equals("Ghola Coin") &&
			!MazeVariables.getBoolean(GHOLA_COIN_USED))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(
				new FlavourTextEvent("You deposit the small coin that the " +
					"undead horror Ghul was bearing into the slot " +
					"in the stone altar.",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(
				new FlavourTextEvent("\n\nWith a chink and a clink and a rattle " +
					"it vanishes from sight.",
					MazeEvent.Delay.WAIT_ON_CLICK, false));
			result.add(new SetMazeVariableEvent(GHOLA_COIN_USED, "true"));

			user.removeItem(item, true);

			return result;
		}

		return null;
	}

}