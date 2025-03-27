package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.RemoveObjectEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.DelayEvent;
import mclachlan.maze.stat.combat.event.SoundEffectEvent;

/**
 *
 */
public class EkirthsTombSpearOfIneoth extends TileScript
{
	private static final String ENGINE_OBJECT_NAME = "ekirths.tomb.wall.of.fire";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handleUseItem(
		Maze maze,
		Point tile,
		int facing,
		Item item,
		UnifiedActor user)
	{
		if (facing == CrusaderEngine.Facing.WEST &&
			item.getName().equals("Spear Of Ineoth") &&
			!MazeVariables.getBoolean(EkirthsTombWallOfFire.WALL_OF_FIRE_DEACTIVATED))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(
				new FlavourTextEvent("You grip the chilling spear that you " +
					"took from the snarling dog demon Ineoth-Em-Risiss-Mar, " +
					"and hold it before the fiery wall.\n\nIs it your " +
					"imagination, or do the the flames bend away from it's " +
					"icy shaft?", MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(
				new FlavourTextEvent(
					"Bending your back, you fling the bone spear at the flames.",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(
				new FlavourTextEvent(
					"\n\nAs it flies straight and true, the spear inexplicably " +
						"slows down...",
					MazeEvent.Delay.WAIT_ON_CLICK, false));
			result.add(
				new FlavourTextEvent(
					"You watch in fascination as the flames seem to struggle " +
						"against the chill dart aimed at their heart. The dread " +
						"spear of Ineoth inches inexorably forward, ever slowing..." +
						"\n\nFor a second it comes to a halt in mid air, and it " +
						"seems like the fires may win...",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(new SoundEffectEvent("47252__nthompson__rocketexpl"));
			result.add(new DelayEvent(500));
			result.add(new RemoveObjectEvent(ENGINE_OBJECT_NAME));
			result.add(
				new FlavourTextEvent(
					"Suddenly there is a flash and a booming explosion that " +
						"throws you sprawling down the passage.\n\n" +
						"Picking yourselves up, you can see no sign of either " +
						"the spear or the wall of fire.",
					MazeEvent.Delay.WAIT_ON_CLICK, true));

			user.removeItem(item, true);

			MazeVariables.set(
				EkirthsTombWallOfFire.WALL_OF_FIRE_DEACTIVATED, "true");

			return result;
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public void initialise(Maze maze, Point tile, int tileIndex)
	{
		if (MazeVariables.getBoolean(EkirthsTombWallOfFire.WALL_OF_FIRE_DEACTIVATED))
		{
			Maze.getInstance().removeObject(ENGINE_OBJECT_NAME);
		}
	}
}