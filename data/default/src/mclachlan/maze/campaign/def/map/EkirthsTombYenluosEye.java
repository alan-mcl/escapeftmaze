package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.RemoveWallEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.combat.event.SoundEffectEvent;

/**
 *
 */
public class EkirthsTombYenluosEye extends TileScript
{
	public static final String ILLUSION_WALL_SPOTTED = "ekirths.tomb.illusion.wall.spotted"; 

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handleUseItem(Maze maze, Point tile, int facing,
		Item item, PlayerCharacter user)
	{
		if (facing == CrusaderEngine.Facing.NORTH &&
			item.getName().equals("Yenluo's Eye") &&
			!MazeVariables.getBoolean(ILLUSION_WALL_SPOTTED))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(
				new FlavourTextEvent("You raise the jewelled eye of the demon " +
					"Yenluo to your own, and peer through it..."));
			result.add(new SoundEffectEvent("22267__zeuss__The_Chime"));
			result.add(
				new FlavourTextEvent("\n\nBehold, the wall in front of you is " +
					"revealed as an illusion!"));
			result.add(new SetMazeVariableEvent(ILLUSION_WALL_SPOTTED, "true"));
			result.add(getEvent());

			return result;
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	private RemoveWallEvent getEvent()
	{
		return new RemoveWallEvent("ekirths.tomb.illusion.wall", true, 365);
	}

	/*-------------------------------------------------------------------------*/
	public void initialise(Maze maze, Point tile, int tileIndex)
	{
		if (MazeVariables.getBoolean(ILLUSION_WALL_SPOTTED))
		{
			getEvent().resolve();
		}
	}
}