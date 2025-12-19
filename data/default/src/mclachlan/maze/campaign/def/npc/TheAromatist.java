
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.InitiateTradeEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.WaitForPlayerSpeech;

/**
 * Mysterious vendor in the Crater Bazaar in Ichiba
 */
public class TheAromatist extends NpcScript
{
	private List<MazeEvent> getFlavourText(String s, PlayerCharacter pc)
	{
		return getList(
			new FlavourTextEvent(s, MazeEvent.Delay.WAIT_ON_CLICK, true),
			new WaitForPlayerSpeech(npc, pc));
	}

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
			new FlavourTextEvent("The Aromatist lifts a familiar tin of warm brown spice and tilts it in greeting. The label reads, \"Hearthroot Blend - Best shared with those we know well.\"", MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	@Override
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new FlavourTextEvent("The Aromatist gives a courteous nod and gestures toward a tin of ochre-colored tea marked, \"Marketday Blend - For routine visits and ordinary hours.\"", MazeEvent.Delay.WAIT_ON_CLICK, true));

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

	/*-------------------------------------------------------------------------*/

	@Override
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		// look for these keywords and respond with flavour text events hard coded here

		String key = super.getNpcSpeech().lookupPlayerSentence(speech);

		if (key == null || key.isEmpty())
		{
			List<MazeEvent> mazeEvents = doesntKnowAbout(speech);
			mazeEvents.add(new WaitForPlayerSpeech(npc, pc));
			return mazeEvents;
		}

		switch (key)
		{
			case "_greeting_" ->
			{
				List<MazeEvent> result = friendlyGreeting();
				result.add(new WaitForPlayerSpeech(npc, pc));
				return result;
			}
			case "_farewell_" ->
			{
				return getList(
					new FlavourTextEvent("The mysterious vendor examines a vial of mellow amber oil, letting it catch the light. The handwritten label reads, \"Fragrance for Paths We Hope Cross Again.\"",
						MazeEvent.Delay.WAIT_ON_CLICK, true));
			}
			case "_insult_" ->
			{
				return getFlavourText("The Aromatist meets your gaze for a long moment, then gestures toward a elegant crystal bottle labeled, \"Not for Crude Handling.\"", pc);
			}
			case "_trade_" ->
			{
				return initiateTrade(pc);
			}
			case "_thanks_" ->
			{
				return getFlavourText("Without comment, the masked figure sets out a bundle of plain sandalwood sticks. A small tag tied with twine reads, \"Burns Evenly.\"", pc);
			}
			case "_scrymgeour_" ->
			{
				return getFlavourText("The Aromatist turns a tin of pepper mix so you can read the writing on its lid: \"Reliable Heat. No Surprises.\"", pc);
			}
			case "_aromatist_" ->
			{
				return getFlavourText("The Aromatist turns a small clay jar so you can read the writing along its base: \"Maker Unimportant. Scent Endures.\"", pc);
			}
			case "_mazza_" ->
			{
				return getFlavourText("The Aromatist lifts a tin of smoked chili flakes and gives it a small shake. The label reads, \"Best Applied Liberally.\"", pc);
			}
			case "_ichiba_" ->
			{
				return getFlavourText("The silent vendor indicates a bottle of neutral carrier oil kept among stronger scents. The card on its neck reads, \"Holds Everything Together.\"", pc);
			}
			case "_quest_" ->
			{
				return getFlavourText("The Aromatist considers, then points to a small tin of plain incense. Its label reads, \"No Special Orders.\"", pc);
			}
			case "_gnoll_" ->
			{
				return getFlavourText("The vendor gestures toward a bundle of smoke-heavy incense sticks stored apart from the rest. The label reads, \"Masks Strong Odors. Use Outdoors.\"", pc);
			}
			case "_leonal_" ->
			{
				return getFlavourText("The vendor taps a bottle of clear citrus oil set apart from heavier scents. Its label says, \"Sharp. Uplifting. Not Subtle.\"", pc);
			}
			case "_gnome_" ->
			{
				return getFlavourText("The Aromatist taps a jar of finely ground spice, almost powder. The label reads, \"Milled Extra Fine.\"", pc);
			}
			case "_gsc_" ->
			{
				return getFlavourText("Without meeting your eyes, the masked figure turns a small vial so the writing faces you: \"Unscented Oil - Leaves No Trace.\"", pc);
			}
			case "_scrimshaw_" ->
			{
				return getFlavourText("The Aromatist turns a jar of cured resin beads in their hand. The writing on the lid says, \"Old Stock. Still Serviceable.\"", pc);
			}
			case "_glaucus_" ->
			{
				return getFlavourText("The vendor taps a tin of sweet-smelling resin stored beside sharper scents. Its label reads, \"Pleasant Enough. Hard to Classify.\"", pc);
			}
			case "_rumour_" ->
			{
				return getFlavourText("Without a word, the masked figure points to a bundle of incense sticks wrapped in thin parchment. The tag tied to them says, \"Whispers Travel in Smoke.\"", pc);
			}
			case "_coc_" ->
			{
				return getFlavourText("The Aromatist straightens a row of identical tins, each one stamped neatly on the lid: \"Inspected. Approved. Logged.\"", pc);
			}
			case "_imogen_" ->
			{
				return getFlavourText("The Aromatist pauses longer than usual, then points to a dark glass vial kept well away from the others. Its label reads, \"Potent. Measure Precisely.\"", pc);
			}
			case "_hoshiko_" ->
			{
				return getFlavourText("The vendor gestures toward a jar of dried blossoms in a gauze packet. The label reads, \"For Quiet Rooms and Open Minds.\"", pc);
			}
			case "_maze_" ->
			{
				return getFlavourText("The masked figure turns a small vial so the warning etched into the glass catches the light: \"Prolonged Exposure Alters Perception.\"", pc);
			}
			case "_rennik_" ->
			{
				return getFlavourText("The silent figure turns a squat tin so you can read the simple note scratched into it: \"Good Work Speaks for Itself.\"", pc);
			}
			case "_three_eyes_" ->
			{
				return getFlavourText("The silent figure slides a sealed tin halfway out from the shadows. Its label is plain and unsettling: \"Eyes Opened Once Do Not Close Easily.\"", pc);
			}
			case "_wilds_" ->
			{
				return getFlavourText("The silent figure points to a jar left unmarked except for a single line scratched into the glass: \"Nothing Returns the Same.\"", pc);
			}
			default ->
			{
				List<MazeEvent> mazeEvents = doesntKnowAbout(speech);
				mazeEvents.add(new WaitForPlayerSpeech(npc, pc));
				return mazeEvents;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> doesntKnowAbout(String speech)
	{
		return getList(
			new FlavourTextEvent("The masked figure lifts a ledger from beneath a carpet, flips a few pages, then turns it to show a margin note: \"No Entry Recorded.\"", MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	@Override
	public List<MazeEvent> doesntWantItem()
	{
		return getList(
			new FlavourTextEvent("The Aromatist examines the item only briefly, then gently shakes their head.", MazeEvent.Delay.WAIT_ON_CLICK, true));
	}


	@Override
	public List<MazeEvent> npcInventoryFull(Item item)
	{
		return getList(
			new FlavourTextEvent("The Aromatist examines the item only briefly, then gently shakes their head.", MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	@Override
	public List<MazeEvent> cantAffordToBuyItem(Item item)
	{
		return getList(
			new FlavourTextEvent("The Aromatist examines the item only briefly, then gently shakes their head.", MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	@Override
	public List<MazeEvent> notInterestedInBuyingItem(Item item)
	{
		return getList(
			new FlavourTextEvent("The Aromatist examines the item only briefly, then gently shakes their head.", MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	@Override
	public List<MazeEvent> characterInventoryFull(PlayerParty party, Item item)
	{
		return getList(
			new FlavourTextEvent("The Aromatist regards you in silence for a moment, then gently shakes their head.", MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	@Override
	public List<MazeEvent> partyCantAffordItem(PlayerParty party, Item item)
	{
		return getList(
			new FlavourTextEvent("With a polite nod, the silent figure points to a small wooden sign near their wares: \"No Credit Under Any Circumstances.\"", MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	/*-------------------------------------------------------------------------*/
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
