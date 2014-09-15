package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.GrantItemsEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.combat.event.SoundEffectEvent;
import mclachlan.maze.stat.npc.*;

/**
 * Gnoll merchant in Ichiba.
 */
public class RedEar extends NpcScript
{
	public static final String SOUND_GROWL = "23387__ljudman__dog";
	public static final String WAITING_FOR_Q1_REPLY = "red.ear.waiting.for.quest.1.reply";
	public static final String WAITING_FOR_Q1_REPLY2 = "red.ear.waiting.for.quest.1.reply2";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent(
				"You pass through the low doorway and enter a dingy shop of " +
					"some sort. Piles of miscelaneous merchandise are heaped on " +
					"shelves and tables, and hides and skins of various kinds " +
					"are draped over most of the walls and windows. Dust and " +
					"mold assault your nostrils."),
			new FlavourTextEvent(
				"\n\nFrom the shadows bounds a hunched figure clad in scruffy " +
					"rags, tongue lolling and yellow eyes glinting as he " +
					"sizes you up..."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new SoundEffectEvent(SOUND_GROWL),
			new NpcSpeechEvent("Red Ear greets you, strangers!\n\n" +
				"Your smells are new in Ichiba! Few are " +
				"those who come through the Gate, but Red Ear thinks you are " +
				"among them, yes he does!"),
			new NpcSpeechEvent("What trinkets and trifles do they bring to " +
				"Ichiba from beyond the Gate? Red Ear will buy them, he will. " +
				"Red Ear has the best deals in Ichiba! Take a look!"));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new SoundEffectEvent(SOUND_GROWL),
			new NpcSpeechEvent("Red Ear greets you! Red Ear has new deals! " +
				"Take a look, take a look!"));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new SoundEffectEvent(SOUND_GROWL),
			new NpcSpeechEvent("Grrrrrrrrrr! Red Ear does not trust you."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new SoundEffectEvent(SOUND_GROWL),
			new NpcSpeechEvent("Grrrrrrrrrrrrrr! Hrmpf!"),
			new NpcLeavesEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new SoundEffectEvent(SOUND_GROWL),
			new NpcSpeechEvent("Red Ear wishes you good hunting! Also " +
				"remember, Red Ear has the very best deals!"),
			new NpcLeavesEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		if (item.getName().equals(WePickett.QUEST_1_BILL))
		{
			return getList(
				new SoundEffectEvent(SOUND_GROWL),
				new NpcSpeechEvent("Grrrrrr"),
				new NpcSpeechEvent("Grrr. You are paper people. Come with " +
					"laws and papers!"),
				new NpcSpeechEvent("Bah! Red Ear cannot read paper!!"),
				new NpcSpeechEvent("Grrrrrrrr. You tell Red Ear what paper is?"),
				new SetMazeVariableEvent(WAITING_FOR_Q1_REPLY, "true"),
				new WaitForPlayerSpeech(npc, getPlayerCharacter(0)));
		}
		else
		{
			return super.givenItemByParty(owner, item);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		if (MazeVariables.getBoolean(WAITING_FOR_Q1_REPLY))
		{
			MazeVariables.clear(WAITING_FOR_Q1_REPLY);

			if (NpcSpeech.sentenceContainsKeywords(speech, "3203") &&
				NpcSpeech.sentenceContainsKeywords(speech, "owe"))
			{
				return getList(
					new NpcSpeechEvent("Grrrr!!!! Robbers come for take Red Ear's " +
						"money! Soldiers come with papers! Always!"),
					new NpcSpeechEvent("Red Ear not have money! Cannot give money " +
						"for paper! No money! None! Grrrr, not much, not enough! " +
						"Red Ear an honest trader, best deals, no trouble! Why " +
						"always from law paper people? Not done anything wrong!"),
					new NpcSpeechEvent("Cannot pay! What you do to Red Ear " +
						"with no money?"),
					new SetMazeVariableEvent(WAITING_FOR_Q1_REPLY2, "true"),
					new WaitForPlayerSpeech(npc, getPlayerCharacter(0))
				);
			}
			else
			{
				return getList(new NpcSpeechEvent("Bah! Red Ear not understand!"));
			}
		}
		else if (MazeVariables.getBoolean(WAITING_FOR_Q1_REPLY2))
		{
			MazeVariables.clear(WAITING_FOR_Q1_REPLY2);

			if (NpcSpeech.sentenceContainsKeywords(speech,
				"kill", "evict", "fight", "battle", "force", "hurt", "out of town", "rough you up",
				"violence", "threat", "threaten", "deadly force", "lethal force"))
			{
				ItemTemplate it = Database.getInstance().getItemTemplate("Red Ear's Taxes");

				return getList(
					new SoundEffectEvent(SOUND_GROWL),
					new NpcSpeechEvent("Grrrr! Wait! Hrrrrr."),
					new NpcSpeechEvent("This not fair! Red Ear is honest trader! " +
						"Not done anything wrong! Law people rob him! Grrrrrr!"),
					new NpcSpeechEvent("But, let Red Ear look again. Maybe find " +
						"some money."),
					new NpcSpeechEvent("..."),
					new NpcSpeechEvent("... grrrr ..."),
					new NpcSpeechEvent("Red Ear find money. Under pillow."),
					new NpcSpeechEvent("Here, take! Soldier law people! Red Ear " +
						"not like any more! No deals for you! Grrrrrr!!!"),
					new GrantItemsEvent(it.create()),
					new SetMazeVariableEvent(WePickett.QUEST_1_COLLECTED_TAXES, "true"),
					new ChangeNpcFactionAttitudeEvent(getNpcFaction().getName(), 1,
						ChangeNpcFactionAttitudeEvent.SET),
					new NpcLeavesEvent());
			}
			else
			{
				return getList(new NpcSpeechEvent("Grrrr. Red Ear have no money, " +
					"cannot take."),
					new NpcSpeechEvent("Verrrry sorry! Red Ear an honest trader! " +
						"Come back tomorrow, maybe Red Ear have money!"));
			}
		}
		else
		{
			return super.parsePartySpeech(pc, speech);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}
