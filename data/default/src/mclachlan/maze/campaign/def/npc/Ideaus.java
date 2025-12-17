
package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.MovePartyEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.*;

/**
 * Gate keeper of Aenen
 */
public class Ideaus extends NpcScript
{
	public static final String INVITATION_FROM_GNOMES = "gnomes.invitation.to.party"; 

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("Before the doorway stands a stern looking gnome " +
				"warrior, clad in mail and bearing a glittering silver poleaxe."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Halt, strangers. I am Ideaus Ap Agorlan, " +
				"by royal decree keeper of the Gate to Aenen.", npc),
			new NpcSpeechEvent("Many are those who wander the woods and " +
				"glades here in the Second Realm, but the Buried City of the Gnomes " +
				"is closed to most.", npc),
			new NpcSpeechEvent("You are free to pass through the lands " +
				"here, but you may not enter this Gate.", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> friendlyGreeting()
	{
		return getList(
			new NpcSpeechEvent("Welcome back to the lands of the Gnomes.", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return friendlyGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		if (MazeVariables.get(INVITATION_FROM_GNOMES) != null)
		{
			// party is authorized to enter
			return getList(
				new NpcSpeechEvent("Farewell friends. May the paths you tread " +
					"in the Maze be safe wherever they may lead you.", npc),
				new ActorsLeaveEvent());
		}
		else
		{
			// party is not authorized
			return getList(
				new NpcSpeechEvent("You may roam the lands of the Gnomes, but " +
					"our city is closed to you.", npc),
				new MovePartyEvent(new Point(15, 17), CrusaderEngine.Facing.NORTH),
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
		if (item.getName().equals("Antenor's Ring"))
		{
			return getList(
				new NpcSpeechEvent("Well well, what have we here?", npc),
				new NpcSpeechEvent("From the wilds you come, bearing the " +
					"seal of Antenor. Yet I do not see that worthy gnome " +
					"with you, or near behind.", npc),
				new NpcSpeechEvent("I think it best if you pass into the " +
					"city below, and bear this ring to King Mnesus. I will " +
					"warn him of your coming.", npc),
				new ChangeNpcFactionAttitudeEvent(
					npc.getFaction(),
					NpcFaction.Attitude.FRIENDLY,
					null,
					ChangeNpcFactionAttitudeEvent.SET),
				new SetMazeVariableEvent(INVITATION_FROM_GNOMES, "true"),
				new ActorsLeaveEvent());
		}
		else
		{
			return super.givenItemByParty(owner, item);
		}
	}
}