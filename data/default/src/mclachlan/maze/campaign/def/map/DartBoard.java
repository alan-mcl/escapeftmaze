package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Stats;

/**
 *
 */
public class DartBoard extends TileScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handleUseItem(Maze maze, Point tile, int facing,
		Item item, PlayerCharacter user)
	{
		List<MazeEvent> events = new ArrayList<MazeEvent>();

		if (item.getName().equals("Feather Dart"))
		{
			events.add(new FlavourTextEvent(
				"You throw a dart at the board...",
				1000,
				true));

			user.removeItem(item, false);

			String hit;
			int mod = user.getModifier(Stats.Modifier.THROW);

			int die = Dice.d100.roll();
			if (die > 50+mod*5)
			{
				hit = "You miss the dart board completely.";
			}
			else if (die <= mod)
			{
				hit = "BULL'S EYE!";
			}
			else
			{
				int nr = Dice.d20.roll();
				int x = Dice.d100.roll();

				if (x <= mod)
				{
					hit = "Triple "+nr+"!!";
				}
				else if (x <= mod*2)
				{
					hit = "Double "+nr+"!";
				}
				else
				{
					hit = "The dart hits number "+String.valueOf(nr);
				}
			}

			events.add(new FlavourTextEvent(
				hit,
				1500,
				false));
		}

		return events;
	}
}
