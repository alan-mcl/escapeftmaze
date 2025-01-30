
package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.CharacterClassKnowledgeEvent;
import mclachlan.maze.game.event.MazeScriptEvent;
import mclachlan.maze.game.event.MovePartyEvent;
import mclachlan.maze.game.event.RemoveItemEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.*;

/**
 * Door keeper at Imogen's tower
 */
public class TowerGuardianNpc extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("You path is blocked by a hulking golem..."),
			new CharacterClassKnowledgeEvent("This type is magical construct is " +
				"strong but unintelligent. It appears that this particular unit " +
				"has been set to guard the door. You are unlikely to be allowed " +
				"past unless it recognises you.", "Sorcerer"));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		if (MazeVariables.get(Imogen.SIGNED_UP_WITH_IMOGEN) != null)
		{
			return getList(
				new NpcSpeechEvent("YOU * MAY * PASS.", npc));
		}
		else if (Maze.getInstance().getParty().isItemEquipped("Rock Salt Amulet"))
		{
			return getList(
				new NpcSpeechEvent("YOU * MAY * NOT * PASS.", npc),
				new FlavourTextEvent("The amulet around your neck " +
					"starts to glow. The golems empty eyes fix on it intently..."),
				new FlavourTextEvent("Suddenly, there is a flash of light and the " +
					"amulet crumbles to dust!"),
				new RemoveItemEvent("Rock Salt Amulet"),
				new FlavourTextEvent("The golem begins to shake its head " +
					"stiffy and clench its fists, its jaws working wordlessly for " +
					"a moment. Then it moans aloud..."),
				new NpcSpeechEvent("AAAAGGHHHH * EEE * EEENT * INTRUUUU * BZZZZZZT", npc),
				new NpcAttacksEvent(npc));
		}
		else if (Maze.getInstance().getParty().isItemEquipped("Ebony Amulet"))
		{
			return getList(
				new FlavourTextEvent("You feel the ebony amulet around your neck " +
					"grow faintly warm..."),
				new NpcSpeechEvent("YOU * MAY * PASS.", npc));
		}
		else
		{
			return getList(
				new NpcSpeechEvent("YOU * MAY * NOT * PASS.", npc),
				new ActorsLeaveEvent(),
				new MazeScriptEvent("generic door creak"),
				new MovePartyEvent(new Point(15, 29), CrusaderEngine.Facing.NORTH));
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		return firstGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return firstGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("THAT * DOES * NOT * PARSE.", npc));

		if (!NpcSpeech.sentenceContainsKeywords(speech, "bye", "goodbye", "farewell"))
		{
			result.add(new WaitForPlayerSpeech(npc, pc));
		}
		
		return result;
	}
}