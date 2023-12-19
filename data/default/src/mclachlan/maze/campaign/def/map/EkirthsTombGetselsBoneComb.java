package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.EncounterActorsEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.RemoveObjectEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.combat.event.SoundEffectEvent;

/**
 *
 */
public class EkirthsTombGetselsBoneComb extends TileScript
{
	public static final String BONE_COMB_USED = "ekirths.tomb.getsels.bone.comb.used";

	/*-------------------------------------------------------------------------*/
	public void initialise(Maze maze, Point tile, int tileIndex)
	{
		if (MazeVariables.getBoolean(BONE_COMB_USED))
		{
			new RemoveObjectEvent("ekirths.tomb.ageyrs.bones").resolve();
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handleUseItem(Maze maze, Point tile, int facing,
		Item item, PlayerCharacter user)
	{
		if (item.getName().equals("Getsel's Bone Comb") &&
			!MazeVariables.getBoolean(BONE_COMB_USED))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(
				new FlavourTextEvent("Stooping down, you tentatively " +
					"hold the gruesome bone comb you took from the hag " +
					"Getsel to the jawless skull of the skeleton.",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(
				new FlavourTextEvent("\n\nIt fits well. Disturbingly well.",
					MazeEvent.Delay.WAIT_ON_CLICK, false));
			result.add(
				new FlavourTextEvent("Suddenly, the jawbone jerks out of your " +
					"hand, clattering under it's own power against the upper " +
					"teeth of the skull!",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(new SoundEffectEvent("575__aarondbaron__messed_1"));
			result.add(
				new FlavourTextEvent("You watch in horror as the remains stir " +
					"to life, and the undead creature rises to its feet. With one " +
					"bony hand it incongruously readjusts the jaw as if to a " +
					"more comfortable position, then it moans in insensate hate " +
					"and stumbles to attack you!",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(new SetMazeVariableEvent(BONE_COMB_USED, "true"));
			result.add(new RemoveObjectEvent("ekirths.tomb.ageyrs.bones"));
			result.add(new EncounterActorsEvent(null, "ageyrs.bones.1", null, null, null));
			
			user.removeItem(item, true);

			return result;
		}

		return null;
	}

}
