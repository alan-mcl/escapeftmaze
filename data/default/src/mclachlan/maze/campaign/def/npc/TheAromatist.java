
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.InitiateTradeEvent;
import mclachlan.maze.stat.npc.NpcScript;

/**
 * Mysterious vendor in the Crater Bazaar in Ichiba
 */
public class TheAromatist extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("A slender, masked figure is seated on these carpets amid curling plumes of scented incense.",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("The lacquered mask is carved from wood, stylized into something serene and unreadable.",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("Dozens of clay jars, tins, copper vials, and parchment packets are stacked around him or her, " +
				"each with a neat handwritten label.",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("You scan the labels.", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("\"Red Ember Paprika - A spark for dull meals.\"\n\n\"Sleeping Fern Sachet - Do not open near strong memories.\"\n", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("\"Rosewater Essence - A single drop is enough.\"\n\n\"Copperroot Spice - Adds warmth when firewood fails.\"", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("There are many more, but you turn your attention to the masked vendor.", MazeEvent.Delay.WAIT_ON_CLICK, true)
		);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new FlavourTextEvent("The Aromatist regards you silently for a moment, then inclines their head in greeting.",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("With a graceful gesture, the masked figure gestures toward a squat clay jar sealed with wax. " +
				"The label reads, \"New Dawn Incense - For those beginning a long journey.\"",
				MazeEvent.Delay.WAIT_ON_CLICK, true)
		);
	}

	@Override
	public List<MazeEvent> friendlyGreeting()
	{
		return getList(
			new FlavourTextEvent("The Aromatist lifts a familiar tin of warm brown spice and tilts it in greeting. The label reads, \"Hearthroot Blend - Best shared with those we know well.\"",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	@Override
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new FlavourTextEvent("The Aromatist gives a courteous nod and gestures toward a tin of ochre-colored tea marked, \"Marketday Blend - For routine visits and ordinary hours.\"",
				MazeEvent.Delay.WAIT_ON_CLICK, true));

	}

	@Override
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new FlavourTextEvent("The masked figure gives a slight incline of the head and tilts a clay jar, revealing a label saying: \"Return And Repeat As Needed.\"",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new ActorsLeaveEvent());
	}

	@Override
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new FlavourTextEvent("The mysterious vendor examines a vial of mellow amber oil, letting it catch the light. The handwritten label reads, \"Fragrance for Paths We Hope Cross Again.\"",
				MazeEvent.Delay.WAIT_ON_CLICK, true),
			new ActorsLeaveEvent());
	}

	@Override
	public List<MazeEvent> initiateTrade(PlayerCharacter pc)
	{
		return getList(
			new FlavourTextEvent("With a fluid motion, the Aromatist produces a large lacquer box. Embossed on the lacquer lid " +
				"you see the text \"For adventurers seeking Items of Utility.\"", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new InitiateTradeEvent(npc, pc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}
