package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.RemoveItemEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.EncounterActorsEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class StygiosForestAltarToNergal extends TileScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handleUseItem(Maze maze, Point tile, int facing,
		Item item, PlayerCharacter user)
	{
		if (item.getName().equals("Ebony Amulet"))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(new FlavourTextEvent("You place Imogen's amulet " +
				"on the gloomy altar and stand back nervously...", MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(new FlavourTextEvent("Is it your imagination or are " +
				"the shadows in the trees moving slowly, gathering around the " +
				"dark altar? With a start you realise that you can no longer see " +
				"the amulet in the darkness...", MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(new RemoveItemEvent("Ebony Amulet"));
			result.add(new FlavourTextEvent("Luminous mist begins to creep from the " +
				"twisted roots and fungi surrounding the stone altar. Your " +
				"skin crawls with dread as you watch it grow into a column before " +
				"you...", MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(new FlavourTextEvent("Suddenly, a face appears in the " +
				"mist! Empty eyes regard you with chilling malice before the " +
				"creature reaches ghostly hands towards you...",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(new EncounterActorsEvent("stygios.forest.altar.of.nergal.spirit",
				"stygios.forest.altar.of.nergal.spirit", null, null));

			return result;
		}

		return null;
	}
}