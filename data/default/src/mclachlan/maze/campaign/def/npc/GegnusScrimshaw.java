
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.stat.npc.*;

/**
 * merchant in the Crater Bazaar in Ichiba
 */
public class GegnusScrimshaw extends NpcScript
{
	public static final String REGNUS_SCRIMSHAW = "Regnus Scrimshaw";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The market stall is piled high with " +
				"junk and bric-a-brac, to the extent where you are not entirely sure " +
				"what it is that is on offer.", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("Before you can more closely examine the " +
				"wares, you are accosted by two enthusiastic dwarfs.", MazeEvent.Delay.WAIT_ON_CLICK, true)
		);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		Foe gegnus = npc;
		Npc regnus = NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW);

		return getList(
			new NpcSpeechEvent("New face! Always good to see. Welcome to the Crater Bazaar, where the dust never settles and the deals never sleep.", gegnus),
			new NpcSpeechEvent("We’re the Scrimshaw Brothers.", regnus),
			new NpcSpeechEvent("Yes we are - fixtures in the Bazaar! I'm Gegnus, and the surly one is my brother Regnus.", gegnus),
			new NpcSpeechEvent("I'm Regnus.", regnus),
			new NpcSpeechEvent("Been here longer than most.", regnus),
			new NpcSpeechEvent("We certainly have. We trade in odds-and-ends from all over the Realm.", gegnus),
			new NpcSpeechEvent("Some of it old as stone, some of it fresh off the cart.", gegnus),
			new NpcSpeechEvent("Everything priced fair.", regnus),
			new NpcSpeechEvent("Priced fair and sold with a smile! Sometimes even my brother's.", gegnus),
			new NpcSpeechEvent("Mine's the smaller smile.", regnus),
			new NpcSpeechEvent("But very heartfelt!", gegnus),
			new NpcSpeechEvent("Now then, traveler. How can we equip you today?", gegnus)
		);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public List<MazeEvent> neutralGreeting()
	{
		Foe gegnus = npc;
		Npc regnus = NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW);

		return getList(
			new NpcSpeechEvent("Welcome back to Scrimshaw Bros! Stock’s shifted a little since you last looked.", gegnus),
			new NpcSpeechEvent("He keeps rearranging it.", regnus),
			new NpcSpeechEvent("Presentation is important, Regnus.", gegnus)
		);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public List<MazeEvent> friendlyGreeting()
	{
		Foe gegnus = npc;
		Npc regnus = NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW);

		return getList(
			new NpcSpeechEvent("There they are! Our favorite traveler returns!", gegnus),
			new NpcSpeechEvent("Didn't expect you this soon.", regnus),
			new NpcSpeechEvent("But we're glad you proved him wrong!", gegnus)
		);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public List<MazeEvent> partyLeavesNeutral()
	{
		Foe gegnus = npc;
		Npc regnus = NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW);

		return getList(
			new NpcSpeechEvent("Leaving so soon? Well, the Bazaar never sleeps and neither do our wares.", gegnus),
			new NpcSpeechEvent("They do gather dust, though.", regnus),
			new NpcSpeechEvent("Dust adds character, brother.", gegnus),
			new ActorsLeaveEvent()
		);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		List<MazeEvent> result = super.parsePartySpeech(pc, speech);

		int count = 0;
		for (MazeEvent event : result)
		{
			if (event instanceof NpcSpeechEvent)
			{
				// set every other speech to be from Regnus
				if (count%2 == 1)
				{
					((NpcSpeechEvent)event).setNpc(NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW));
				}
				count++;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public List<MazeEvent> partyLeavesFriendly()
	{
		Foe gegnus = npc;
		Npc regnus = NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW);

		return getList(
			new NpcSpeechEvent("Travel well! Come back with stories - or salvage.", gegnus),
			new NpcSpeechEvent("Preferably salvage.", regnus),
			new ActorsLeaveEvent()
		);
	}

	@Override
	public List<MazeEvent> partyCantAffordItem(PlayerParty party, Item item)
	{
		Foe gegnus = npc;
		Npc regnus = NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW);

		return getList(
			new NpcSpeechEvent("Worth every gold piece, that one.", gegnus),
			new NpcSpeechEvent("Gold pieces you don't have.", regnus)
		);
	}

	@Override
	public List<MazeEvent> characterInventoryFull(PlayerParty party, Item item)
	{
		Foe gegnus = npc;
		Npc regnus = NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW);

		return getList(
			new NpcSpeechEvent("No space. You could juggle it?", gegnus),
			new NpcSpeechEvent("They would drop it brother.", regnus)
		);
	}

	@Override
	public List<MazeEvent> notInterestedInBuyingItem(Item item)
	{
		Foe gegnus = npc;
		Npc regnus = NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW);

		return getList(
			new NpcSpeechEvent("Someone out there will want it.", gegnus),
			new NpcSpeechEvent("Not us.", regnus)
		);
	}

	@Override
	public List<MazeEvent> cantAffordToBuyItem(Item item)
	{
		Foe gegnus = npc;
		Npc regnus = NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW);

		return getList(
			new NpcSpeechEvent("Oh, that's a tempting piece.", gegnus),
			new NpcSpeechEvent("Too rich for us though.", regnus)
		);
	}

	@Override
	public List<MazeEvent> npcInventoryFull(Item item)
	{
		Foe gegnus = npc;
		Npc regnus = NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW);

		return getList(
			new NpcSpeechEvent("Lovely item! Normally I'd say yes.", gegnus),
			new NpcSpeechEvent("Nowhere to put it.", regnus)
		);
	}

	@Override
	public List<MazeEvent> doesntWantItem()
	{
		Foe gegnus = npc;
		Npc regnus = NpcManager.getInstance().getNpc(REGNUS_SCRIMSHAW);

		return getList(
			new NpcSpeechEvent("We appreciate the thought!", gegnus),
			new NpcSpeechEvent("We decline the gift.", regnus)
		);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}
