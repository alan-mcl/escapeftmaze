
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import java.awt.Point;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.RemoveItemEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.GrantExperienceEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.map.script.GrantItemsEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.*;

/**
 * Witch, Ichiba.
 */
public class Imogen extends NpcScript
{
	public static final String SIGNED_UP_WITH_IMOGEN = "imogen.signed.up";
	public static final String WAITING_FOR_SIGN_UP_REPLY = "imogen.waiting.for.sign.up.reply";

	private static final String EBONY_AMULET = "Ebony Amulet";

	public static final String QUEST_1_COMPLETE = "imogen.quest.1.complete";
	public static final String QUEST_1_REWARD = "imogen.quest.1.reward";
	public static final String QUEST_2_COMPLETE = "imogen.quest.2.complete";
	public static final String QUEST_2_REWARD = "imogen.quest.2.reward";
	public static final String QUEST_3_COMPLETE = "imogen.quest.3.complete";
	public static final String QUEST_3_REWARD = "imogen.quest.3.reward";
	public static final String QUEST_4_COMPLETE = "imogen.quest.4.complete";
	public static final String QUEST_4_REWARD = "imogen.quest.4.reward";

	/*-------------------------------------------------------------------------*/
	protected void start()
	{
		initInternal();
	}

	/*-------------------------------------------------------------------------*/
	private void initInternal()
	{
		QuestManager qm = npc.getQuestManager();

		qm.addQuest(createQuest1());
		qm.addQuest(createQuest2());
		qm.addQuest(createQuest3());
		qm.addQuest(createQuest4());

		qm.start();
	}

	/*-------------------------------------------------------------------------*/
	public void initialise()
	{
		initInternal();
	}

	/*-------------------------------------------------------------------------*/
	private Quest createQuest1()
	{
		List<MazeEvent> intro = getList(
			new NpcSpeechEvent("Without further ado, I have a quest for you."),
			new NpcSpeechEvent("You will need the amulet again. I have always " +
				"thought of ebony as my birth flower, ha ha ha!"),
			new NpcSpeechEvent("I need you to journey to the Forest of Stygios and " +
				"find the altar of the godling Nergal that was built centuries " +
				"ago beneath it's eaves."),
			new NpcSpeechEvent("Once there, you must place the Ebony Amulet on " +
				"the altar. Observe what happens, collect anything of " +
				"significance that appears, and return to me here, bringing the " +
				"amulet back if possible."),
			new NpcSpeechEvent("The quickest way to Stygios Forest is through " +
				"the Gate that lies below the Leonal castle that they call Danaos. " +
				"Leave Ichiba and take the road north to get there. The Leonals " +
				"guard the Gate fiercely, and you will have to deal with them " +
				"somehow to get access to it."),
			new NpcSpeechEvent("Now, go quickly - time is of the essence. It " +
				"will be a dangerous journey; here is the amulet, and some of " +
				"Imogen's magic to take with you on the road...."),
			new GrantItemsEvent(
				createItem(EBONY_AMULET),
				createItem("Potion Of Barkskin", 5)));

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("Have you been to the altar of Nergal in " +
				"Stygios Forest yet? Time grows short..."));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("You have returned from Stygios? Good, tell me " +
				"what happened at the altar of Nergal..."),
			new NpcSpeechEvent("..."),
			new NpcSpeechEvent("Fascinating. A spirit of bone, you say? I had " +
				"expected that the amulet would be unharmed, but this gem should " +
				"suffice for my purposes."),
			new NpcSpeechEvent("You have done well. The magic of Imogen is " +
				"strong in this Realm. Here is your reward."),
			new GrantItemsEvent(
				createItem("Snake Dust", 1),
				createItem("Acid Bomb", 1),
				createItem("Potion Of Mirror Image", 2)),
			new GrantExperienceEvent(100, null),
			new NpcLeavesEvent());

		return new Quest(
			QUEST_1_COMPLETE,
			QUEST_1_REWARD,
			intro,
			reward,
			encouragement);
	}

	/*-------------------------------------------------------------------------*/
	private Quest createQuest2()
	{
		List<MazeEvent> intro = getList(
			new NpcSpeechEvent("I have another task for you; again it will take " +
				"you far from the walls of Ichiba."),
			new NpcSpeechEvent("Have you heard of the town called Hail?\n\nIt was " +
				"once a bold settlement deep withing this Realm, beyond the " +
				"Forest of Stygios and Tornado Mountain. There was trade between " +
				"Ichiba and Hail, and the brave settlers pushed the darkness of " +
				"the Maze to the further reaches of the wastelands."),
			new NpcSpeechEvent("The Maze fought back, as it does. Hail now " +
				"lies in ruins, its buildings burned, its heroes slain and " +
				"its treasures plundered."),
			new NpcSpeechEvent("Yet, there is still a chance...\n\nHail had a " +
				"library, in its zenith the biggest in the Second Realm. There " +
				"was a book there that I need for my incantation - thus, I need " +
				"you to travel to Hail and search until you find it."),
			new NpcSpeechEvent("The book is called \"Al-Khydr's Grimoire\". I " +
				"doubt that you will be able to read the ancient abjad script in " +
				"which it is written, but you will recognise it by the motif " +
				"of a bearded face surrounded by leaves scored on its leather " +
				"cover, which is bound with mahogony and shining copper."),
			new NpcSpeechEvent("The journey will again be a dangerous one, and " +
				"Hail these days is stalked by the monsters of the Maze. Do not " +
				"linger there, return at once when you find the book."),
			new NpcSpeechEvent("Here, take these to aid your travel..."),
			new GrantItemsEvent(
				createItem("Potion Of Regeneration", 5)));

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("Have you yet retrieved the Grimoire of Al-Khydr " +
				"from the ruins of Hail?"));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("Most excellent work. This grimoire was the last " +
				"that I needed... the final piece of the puzzle..."),
			new NpcSpeechEvent("Return in one day and I will have another task " +
				"for you. In the mean time, here is your reward, richly deserved..."),
			new GrantItemsEvent(
				createItem("Potion Of Curing", 3),
				createItem("Scroll Of Call Treant", 1),
				createItem("Ancient Dust", 2),
				createItem("Brooch")),
			new GrantExperienceEvent(200, null),
			new NpcLeavesEvent());

		return new Quest(
			QUEST_2_COMPLETE,
			QUEST_2_REWARD,
			intro,
			reward,
			encouragement);
	}

	/*-------------------------------------------------------------------------*/
	private Quest createQuest3()
	{
		List<MazeEvent> intro = getList(
			new NpcSpeechEvent("Events move towards their conclusion. I need " +
				"you to carry a message for me."),
			new NpcSpeechEvent("Go to the gnoll village, north of Ichiba. " +
				"Find their chieftain Broken Fang, and tell him the Imogen " +
				"says that the washing of the spears is come."),
			new NpcSpeechEvent("Those exact words, mind - \"the washing of " +
				"the spears is come\". He will understand."),
			new NpcSpeechEvent("The quickest way to the gnoll village is a " +
				"tunnel under the north wall - search the dwellings there until " +
				"you find it."),
			new NpcSpeechEvent("Go now, with haste. Return when you have " +
				"delivered the message, I may have more tasks for you."));

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("Go to Broken Fang in the gnoll village and " +
				"tell him that \"the washing of the spears is come\". " +
				"Make haste!"));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("Your message was well delivered. Broken Fang " +
				"and his braves met little resistance. I expected as much. " +
				"Surprise and preparation paid me well... they were inside " +
				"the walls without warning."),
			new NpcSpeechEvent("The precious mercenaries hired by the Chamber " +
				"fled as soon as they were faced with a real fight instead of " +
				"tax collecting duties.\n\n" +
				"Ichiba is always home to hardy characters of all " +
				"kinds, and battle will continue here for some time."),
			new NpcSpeechEvent("Yet in truth, I care little of the fate of " +
				"Ichiba or who wins the day here. Let the gnolls have their " +
				"fun - I have fulfilled my pact with them. Imogen is as good as " +
				"her word. But this sack is a diversion, a device to buy time " +
				"while I complete my final incantation.... For years I " +
				"have prepared for this day, now none can stop me!"),
			new NpcSpeechEvent("You have served me well. Here is a reward. " +
				"I have little need of these trinkets.\n\n" +
				"There is one last task for you. Meet me at the exit to the " +
				"tower and I will explain. Go now, I have much to prepare..."),
			new GrantItemsEvent(
				createItem("Potion Of Restoration", 4),
				createItem("Demolition Stix", 3),
				createItem("Bone Necklace"),
				createItem("Grimoire Of Summon Demon")),
			new GrantExperienceEvent(300, null),
			new ChangeNpcLocationEvent(npc, new Point(15,32), "Ichiba City"),
			new NpcLeavesEvent());

		return new Quest(
			QUEST_3_COMPLETE,
			QUEST_3_REWARD,
			intro,
			reward,
			encouragement);
	}

	/*-------------------------------------------------------------------------*/
	private Quest createQuest4()
	{
		List<MazeEvent> intro = getList(
			new NpcSpeechEvent("So, the end game is here."),
			new NpcSpeechEvent("For years the Chamber of Commerce and the " +
				"blasted Thieves Guild have opposed me. They are both scattered " +
				"now."),
			new NpcSpeechEvent("Much has been revealed to me - the most shocking " +
				"is this: the Chamber of Commerce has a hidden leader."),
			new NpcSpeechEvent("Pickett is a puppet, a talking head for the " +
				"adminstration. The truth is far darker. I know not how it came " +
				"to Ichiba, but for years a spirit of evil has been behind the " +
				"Chambers every move."),
			new NpcSpeechEvent("I wondered, often, how that bumbling fool " +
				"Pickett managed to thwart my every move. I did not know of " +
				"his dark master, a vampire. It's name is 'Rhys'."),
			new NpcSpeechEvent("We are unlikely to meet again, so I cannot " +
				"hold you to another task. Yet if you are willing and able, you " +
				"might strike a blow for all of Ichiba if you slay this monster."),
			new NpcSpeechEvent("The Chamber of Commerce is finished as a power in " +
				"Ichiba however this skirmish plays out. Pickett and a " +
				"last few mercenaries are barricaded in their offices, the rest " +
				"have fled. I do not know the way, but hidden in those offices " +
				"you will find a passage to confront the fiend. Storm the " +
				"building and slay whoever you find!\n\nBy way of reward, return " +
				"to my tower and take whatever you can find. I will have " +
				"little need of it."),
			new NpcSpeechEvent("Slay the vampire Rhys and rest assured that you " +
				"are doing good.\n\nOr flee Ichiba and never return, I care not!"),
			new NpcSpeechEvent("I go to complete my incantation. We will not " +
				"meet again in this life or the next. You have served me well, I " +
				"wish you good fortune whatever path you choose.\n\nFarewell!"),
			new ChangeNpcLocationEvent(npc, new Point(1,1), "Ichiba City"),
			new NpcLeavesEvent());

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("Slay the vampie Rhys!"));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("Well done!"),
			new NpcLeavesEvent());

		return new Quest(
			QUEST_4_COMPLETE,
			QUEST_4_REWARD,
			intro,
			reward,
			encouragement);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (!MazeVariables.getBoolean(QUEST_3_COMPLETE))
		{
			result.add(
				new FlavourTextEvent("The overgrown chamber is moist and dank. " +
					"Vines and creepers festoon the walls and floor and ceiling; " +
					"bulbous stalks and strange sinister flowers nod lazily in " +
					"your direction as you you enter.\n"));

			result.add(
				new FlavourTextEvent("At first you think the room is empty, but " +
					"then you make out a slender feminine figure reclining at the " +
					"far end, so enwrapped in her throne of foliage that you missed " +
					"her at first."));

			result.add(
				new FlavourTextEvent("She regards you with interest for a moment, " +
					"then rises from her leafy boudoir, shoots and stems and tendrils " +
					"unwrapping themselves sensuously and reluctantly from " +
					"her form, and saunters towards you...",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
		}
		else
		{
			result.add(
				new FlavourTextEvent("Imogen awaits you, an expression of " +
					"preternatural calm on her porcelain features..."));
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Ah, the new arrivals. I am Imogen."));

		if (Maze.getInstance().getParty().isItemEquipped(EBONY_AMULET) &&
			!(MazeVariables.getBoolean(SirKay.SIGNED_UP_WITH_THIEVES_GUILD) || 
				MazeVariables.getBoolean(WePickett.SIGNED_UP_COC)))
		{
			result.add(new NpcSpeechEvent("Ha ha, I see that you have passed " +
				"my little test and found the amulet."));
			result.add(new NpcSpeechEvent("You have my gratitude for bringing " +
				"it, there was an empty space in my jewelry box."));
			result.add(new NpcSpeechEvent("I'll take it back now, thanks!"));
			result.add(new RemoveItemEvent(EBONY_AMULET));
			result.add(new NpcSpeechEvent("Now, to business. I hoped that the " +
				"amulet would bring me someone... talented. And so it seems.  I " +
				"have need of such talents as you possess."));
			result.add(new NpcSpeechEvent("Though it may not seem so on the " +
				"surface, great things are afoot in this grubby town. I will " +
				"play my part in due course, and no doubt so will you."));
			result.add(new NpcSpeechEvent("Yet, the demands on my time here " +
				"are increasingly onerous, and recently I can seldom leave the " +
				"city."));
			result.add(new NpcSpeechEvent("Consequently, I need to rely on " +
				"others to see to my business outside the walls and far afield. " +
				"These could be dangerous errands, for it is a dangerous game " +
				"that I play. More dangerous that you can imagine."));
			result.add(new NpcSpeechEvent("And so, the amulet has brought me you." +
				" Will you aid me? You will be amply rewarded. Decide now, " +
				"yes or no?"));
			result.add(new SetMazeVariableEvent(WAITING_FOR_SIGN_UP_REPLY, "true"));
			result.add(new WaitForPlayerSpeech(npc, getPlayerCharacter(0)));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Greetings."));

		if (MazeVariables.getBoolean(SIGNED_UP_WITH_IMOGEN))
		{
			checkQuests(result);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("Yes?"));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Goodbye."),
			new NpcLeavesEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Farewell."),
			new NpcLeavesEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		if (item.getName().equals("Spagyric Gem"))
		{
			owner.removeItem(item, true);
			MazeVariables.set(QUEST_1_COMPLETE, "true");
			ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();
			checkQuests(result);
			return result;
		}
		else if (item.getName().equals("Al-Khydr's Grimoire"))
		{
			owner.removeItem(item, true);
			MazeVariables.set(QUEST_2_COMPLETE, "true");
			ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();
			checkQuests(result);
			return result;
		}

		return super.givenItemByParty(owner, item);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		if (MazeVariables.getBoolean(WAITING_FOR_SIGN_UP_REPLY))
		{
			if (NpcSpeech.sentenceContainsKeywords(speech, "yes", "yay", "yeah"))
			{
				NpcFaction faction = getNpcFaction();
				faction.setAttitude(100);
				MazeVariables.clear(WAITING_FOR_SIGN_UP_REPLY);

				List<MazeEvent> result = getList(
					new SetMazeVariableEvent(SIGNED_UP_WITH_IMOGEN, "true"),
					new NpcSpeechEvent("The lady fortune favours the bold. " +
						"You will not regret it."));

				checkQuests(result);

				return result;
			}
			else if (NpcSpeech.sentenceContainsKeywords(speech, "no", "nay"))
			{
				MazeVariables.clear(WAITING_FOR_SIGN_UP_REPLY);
				return getList(
					new NpcSpeechEvent("Very well. I regret your decision, but " +
						"life is a castle build on regret and missed opportunities."),
					new NpcSpeechEvent("Please leave my tower now, and do not " +
						"return. The golem at the door will see you out."));
			}
			else
			{
				return getList(
					new NpcSpeechEvent("Excuse me?"),
					new WaitForPlayerSpeech(npc, pc));
			}
		}
		else
		{
			return super.parsePartySpeech(pc, speech);
		}
	}
}