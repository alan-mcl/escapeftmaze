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
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.combat.event.SoundEffectEvent;

/**
 *
 */
public class EkirthsTombMedallionOfBel extends TileScript
{
	public static final String MEDALLION_OF_BEL_USED = "ekirths.tomb.medalion.of.bel.used";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handleUseItem(Maze maze, Point tile, int facing,
		Item item, PlayerCharacter user)
	{
		if (item.getName().equals("Medallion Of Bel") &&
			!MazeVariables.getBoolean(MEDALLION_OF_BEL_USED))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(
				new FlavourTextEvent("On a hunch, you brandish the tarnished " +
					"medallion that you found in the hovel of the mad " +
					"sorcerer Bel Ies, deep in the Writhing Mire.",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(
				new FlavourTextEvent("For a second of silent expectation, " +
					"nothing happens...", MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(new SoundEffectEvent("575__aarondbaron__messed_1"));
			result.add(
				new FlavourTextEvent("\n\nThen suddenly, a forlorn bell tolls from " +
					"somewhere nearby, echoing loudly through the silent dungeon.  " +
					"You jump in surprise, and " +
					"fumble with the amulet... only to find that it has " +
					"mysteriously vanished!"));
			result.add(new SetMazeVariableEvent(MEDALLION_OF_BEL_USED, "true"));

			user.removeItem(item, true);

			return result;
		}

		return null;
	}
}