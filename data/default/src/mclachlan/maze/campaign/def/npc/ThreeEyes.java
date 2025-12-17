
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.combat.event.SoundEffectEvent;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Item;

/**
 * Gnoll shaman and vendor, Gnoll Village.
 */
public class ThreeEyes extends NpcScript
{
	public static final String DREAMER_SKULL_MISSING = "three.eyes.dreamer.skull.missing";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		FlavourTextEvent skullPresent = new FlavourTextEvent("\nOn the far wall, " +
			"illuminated by a halo of smoking candles, a horned skull leers at you.",
			MazeEvent.Delay.WAIT_ON_CLICK, false);
		FlavourTextEvent skullMissing = new FlavourTextEvent("\nOn the far wall, " +
			"a small empty niche is illuminated by a halo of smoking candles",
			MazeEvent.Delay.WAIT_ON_CLICK, false);

		List<MazeEvent> result = getList(
			new FlavourTextEvent("You stoop to enter the small hut. " +
				"The interior is dim and smokey, and the walls are lined with " +
				"crude shelves bearing various trinkets and bundles."),
			new FlavourTextEvent("\nSeated crosslegged before a small fire pit " +
				"is a wizened gnoll, grey about the snout. He rises to greet " +
				"you..."));

		if (MazeVariables.getBoolean(DREAMER_SKULL_MISSING))
		{
			result.add(1, skullMissing);
		}
		else
		{
			result.add(1, skullPresent);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		if ("Dreamer Skull".equals(item.getName()))
		{
			MazeVariables.set(DREAMER_SKULL_MISSING, "true");
		}

		return super.successfulTheft(pc, item);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new SoundEffectEvent(RedEar.SOUND_GROWL),
			new NpcSpeechEvent("Greetings, travellers. I am Three Eyes, " +
				"shaman of this tribe.\n\nHow can I help you?", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> friendlyGreeting()
	{
		return getList(
			new NpcSpeechEvent("Greetings, travellers.", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("Yes?", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Behave yourselves, children.", npc),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Farewell friends.", npc),
			new ActorsLeaveEvent());
	}
}