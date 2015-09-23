
package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.MazeScriptEvent;
import mclachlan.maze.game.event.MovePartyEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import mclachlan.maze.stat.npc.NpcTakesItemEvent;

/**
 * Door keeper at Ichiba Chamber Of Commerce
 */
public class CocSheriffNpc extends NpcScript
{
	public static final String INVITATION_FROM_COC = "coc.invitation.to.party";
	public static final String COC_QUEST_1_PRIMER = "coc.quest.1.primer";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("You path is blocked by a scowling, heavily " +
				"armoured guardsman. He looks you up and down dismissively " +
				"and holds out his hand to halt your progress..."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("This is a restricted area. " +
				"Authorization is required.", npc),
			new NpcSpeechEvent("Papers please...", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		if (MazeVariables.getBoolean(INVITATION_FROM_COC))
		{
			return getList(
				new NpcSpeechEvent("Yes?", npc));
		}
		else
		{
			return getList(
				new NpcSpeechEvent("Papers please...", npc));
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return subsequentGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		if (MazeVariables.get(INVITATION_FROM_COC) != null)
		{
			// party is authorized to enter
			return getList(
				new NpcSpeechEvent("Your visit is authorized.", npc),
				new ActorsLeaveEvent());
		}
		else
		{
			// party is not authorized
			return getList(
				new NpcSpeechEvent("No papers - no entry", npc),
				new FlavourTextEvent("The guard watches you suspiciously on " +
					"your way out."),
				new MazeScriptEvent("generic door creak"),
				new MovePartyEvent(new Point(15, 19), CrusaderEngine.Facing.EAST),
				new ActorsLeaveEvent());
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return partyLeavesNeutral();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		if (item.getName().equals("C.O.C Paper Slip"))
		{
			return getList(
				new NpcSpeechEvent("Hmmm, you're here about the mercenary position.", npc),
				new NpcSpeechEvent("Alright, I'll take that.", npc),
				new NpcTakesItemEvent(owner, item, npc),
				new NpcSpeechEvent("You need to head upstairs and see the " +
					"Director, Mr Pickett.", npc),
				new NpcSpeechEvent("Don't cause any trouble.", npc),
				new SetMazeVariableEvent(COC_QUEST_1_PRIMER, "true"),
				new ActorsLeaveEvent());
		}
		else
		{
			return super.givenItemByParty(owner, item);
		}
	}
}