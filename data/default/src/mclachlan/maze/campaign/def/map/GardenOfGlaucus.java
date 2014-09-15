package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.HealingEvent;

/**
 *
 */
public class GardenOfGlaucus extends TileScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handleUseItem(Maze maze, Point tile, int facing,
		Item item, PlayerCharacter user)
	{
		if (item.getName().equals("Mistletoe Berry"))
		{
			List<MazeEvent> events = new ArrayList<MazeEvent>();

			events.add(new FlavourTextEvent(
				"You cup the waxy berries in your hand and stand for a moment, " +
					"feeling rather foolish.",
				MazeEvent.Delay.WAIT_ON_CLICK,
				true));

			events.add(new FlavourTextEvent(
				"\n\nSuddenly though, you feel a tingling in " +
					"your hand and arm... then an unexpected warmth radiating out " +
					"of your palm. You reflexively open your hand, but the berries " +
					"are gone...",
				MazeEvent.Delay.WAIT_ON_CLICK,
				false));

			user.removeItem(item, true);

			events.add(new FlavourTextEvent(
				"You feel strangely refreshed.",
				MazeEvent.Delay.NONE,
				true));

			for (UnifiedActor actor : maze.getParty().getActors())
			{
				events.add(new HealingEvent(actor, Dice.d12.roll()));
			}

			return events;
		}
		else if (item.getName().equals("Taflen Seeds"))
		{
			user.removeItem(item, false);
			MazeScript script = Database.getInstance().getScript("Ichiba City to Dalen");
			return script.getEvents();
		}

		return null;
	}
}