
package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.MazeScriptEvent;
import mclachlan.maze.game.event.MovePartyEvent;
import mclachlan.maze.game.event.RemoveItemEvent;
import mclachlan.maze.map.script.*;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.*;

/**
 * COC Director, Ichiba.
 */
public class WePickett extends NpcScript
{
	public static final String ICHIBA_CHAMBER_OF_COMMERCE = "Ichiba Chamber Of Commerce";

	private static final String WAITING_FOR_SIGN_UP_REPLY = "we.pickett.waiting.for.quest.1.reply";
	private static final String BEEN_GIVEN_HAIL_CIDER = "we.pickett.been.given.hail.cider";
	public static final String QUEST_1_BILL = "C.O.C Bill For Red Ear";
	public static final String SIGNED_UP_COC = "coc.signed.up";

	public static final String QUEST_1_COLLECTED_TAXES = "coc.quest.1.collected.taxes";
	public static final String QUEST_1_REWARDED = "coc.quest.1.rewarded";

	public static final String QUEST_2_STARTED = "coc.quest.2.started";
	public static final String QUEST_2_COMPLETED = "coc.quest.2.completed";
	public static final String QUEST_2_REWARDED = "coc.quest.2.rewarded";

	public static final String QUEST_3_COMPLETED = "coc.quest.3.completed";
	public static final String QUEST_3_REWARDED = "coc.quest.3.rewarded";

	public static final String QUEST_4_COMPLETED = "coc.quest.4.completed";
	public static final String QUEST_4_REWARDED = "coc.quest.4.rewarded";

	public static final String FREE_BOOZE_AT_THE_INN = "coc.free.booze.at.the.inn";
	public static final String LAST_QUEST_COUNTER = "coc.last.quest.counter";

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
		String itemName = QUEST_1_BILL;
		Item bill = createItem(itemName);

		List<MazeEvent> intro = getList(
			new NpcSpeechEvent("Very well. Your first job is revenue " +
				"collection, very important to our operations."),
			new NpcSpeechEvent("In town there is a shop run by a gnoll " +
				"who goes by the uncouth name of 'Red Ear'. You can't miss it - " +
				"the little junk shop near the waterfront."),
			new NpcSpeechEvent("Gnolls. They're a primitive, savage bunch. " +
				"This 'Red Ear' isn't the brightest gnoll I've met. In " +
				"particular, he seems to have some problems with concepts " +
				"like taxes, trading licenses, and rental."),
			new NpcSpeechEvent("Let's see... as of this today, he owes us " +
				"345g in overdue " +
				"rental on the waterfront shack, 17.50g for the last " +
				"quarterly trading license, 2820g in " +
				"retail back taxes, and 8.50g interest on the outstanding " +
				"amount.\n\nAlong with a 5g administration fee and the " +
				"7g late collection levy, that comes to a total of " +
				"3203 gold pieces."),
			new NpcSpeechEvent("Go and collect that amount from him, and " +
				"bring it back to me."),
			new SetMazeVariableEvent(CocSheriffNpc.INVITATION_FROM_COC, "true"),
			new NpcSpeechEvent("Payment for the job will be a flat 8% of " +
				"the taking, standard for revenue collection, plus sundry " +
				"expenses of 10g. Minimal because the job is in town. " +
				"That comes to 266g.\n\nYou do what it takes to get the " +
				"money, but try not to kill him. If he can't pay the rent, " +
				"evict him and confiscate enough goods to make up the debt."),
			new NpcSpeechEvent("Here's the documentation, you'll find it all " +
				"in order. Our Accounts department is very efficient."),
			new GrantItemsEvent(bill),
			new NpcLeavesEvent(),
			new MazeScriptEvent("generic door creak"),
			new MovePartyEvent(new Point(2, 3), CrusaderEngine.Facing.WEST));

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("Come back when you've collected from that gnoll."),
			new NpcSpeechEvent("You're authorized to use whatever means are " +
				"necessary. Savages like him usually respond to threats of " +
				"violence."),
			new NpcLeavesEvent(),
			new MazeScriptEvent("generic door creak"),
			new MovePartyEvent(new Point(2, 3), CrusaderEngine.Facing.WEST));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("Excellent. I'll take that, thank you."),
			new RemoveItemEvent("Red Ear's Taxes"),
			new NpcSpeechEvent("Good work. Revenue collection jobs " +
				"take a steady hand."),
			new NpcSpeechEvent("Here's your collection fee, 266 gold " +
				"pieces. For a job well done."),
			new GrantGoldEvent(266),
			new NpcSpeechEvent("Say, I have a tab down at the old " +
				"Adventurer's Armoury. Why don't you head over there and " +
				"have a pint on me, eh? After a hard day's work. I'll " +
				"tell Scrymgeour to expect you."),
			new SetMazeVariableEvent(FREE_BOOZE_AT_THE_INN, "true"),
			new GrantExperienceEvent(100, null));

		return new Quest(
			QUEST_1_COLLECTED_TAXES,
			QUEST_1_REWARDED,
			intro,
			reward,
			encouragement);
	}

	/*-------------------------------------------------------------------------*/
	private Quest createQuest2()
	{
		List<MazeEvent> intro = getList(
			new NpcSpeechEvent("Well. Another job has come up."),
			new NpcSpeechEvent("One of our affiliated merchants passing " +
				"through Ichiba " +
				"has been hit by a band of robbers. Nasty business, lucky he " +
				"wasn't present at the time."),
			new NpcSpeechEvent("Our regular law enforcement fellows have " +
				"been over the warehouse and so on, but the criminals are " +
				"still at large.\n\nI need you to find them and bring them in, " +
				"or kill them if they resist arrest."),
			new NpcSpeechEvent("The grunts think they're still in Ichiba, " +
				"but don't know where.\n\nWhat's more interesting is what was " +
				"taken. The criminals emptied out the money bags and made off " +
				"with a number of gems and items of jewelry, usual stuff " +
				"that, probably all fenced already.  " +
				"But they also took a priceless set of antique china plates " +
				"and cups. A very distinctive item."),
			new NpcSpeechEvent("The pay is standard rates, 350 gold for a " +
				"law enforcement job in town. You've got a living allowance " +
				"of ten gold per day for three days, paid up front. If it " +
				"takes longer than that expenses are your own.\n\nIn addition, " +
				"the merchant involved has offered a reward of 1500 gold for " +
				"the undamaged return of his china."),
			new GrantGoldEvent(30),
			new NpcSpeechEvent("That's all. Get to it, and good luck."),
			new SetMazeVariableEvent(QUEST_2_STARTED, "true"),
			new NpcLeavesEvent(),
			new MazeScriptEvent("generic door creak"),
			new MovePartyEvent(new Point(2, 3), CrusaderEngine.Facing.WEST));

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("Come back when you've tracked down those " +
				"criminals."),
			new NpcLeavesEvent(),
			new MazeScriptEvent("generic door creak"),
			new MovePartyEvent(new Point(2, 3), CrusaderEngine.Facing.WEST));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("I'm told that you tracked down and " +
				"eliminated the criminals."),
			new NpcSpeechEvent("Good riddance. Don't worry too much about not " +
				"finding the goods. It'll teach those involved to hire proper " +
				"protection next time."),
			new NpcSpeechEvent("Law enforcement rates, 350 gold. Here we are." +
				"\n\nToo bad about the china."),
			new GrantGoldEvent(350),
			new NpcSpeechEvent("Why don't you get a round on my tab down at " +
				"old Scrymgeours?"),
			new SetMazeVariableEvent(FREE_BOOZE_AT_THE_INN, "true"),
			new GrantExperienceEvent(200, null));

		return new Quest(
			QUEST_2_COMPLETED,
			QUEST_2_REWARDED,
			intro,
			reward,
			encouragement);
	}

	/*-------------------------------------------------------------------------*/
	private Quest createQuest3()
	{
		List<MazeEvent> intro = getList(
			new NpcSpeechEvent("You lot seem like an enterprising bunch. I " +
				"have a special mission for you."),
			new NpcSpeechEvent("You may have heard about the city of Hail. " +
				"Former city, I should probably say."),
			new NpcSpeechEvent("For a long time, Hail was our biggest " +
				"trading partner, and the biggest settlement in the Second " +
				"Realm after Ichiba. It was a younger settlement - a brave " +
				"band of adventurers founded a city deep in the Realm, in a " +
				"mountainous domain. Paths that lead there were perilous at best."),
			new NpcSpeechEvent("Of course, perilous paths mean lucrative " +
				"caravans if you can make it through, and we had good profits " +
				"from Hail for many years."),
			new NpcSpeechEvent("Unfortunately, the Maze has a way about it... " +
				"a way of fighting back. It's easy to forget, here in Ichiba. " +
				"But the Maze has infinite patience and resources to block our " +
				"escape. And when you go too far, chance too much, it cuts you " +
				"down. Such was the case with Hail..."),
			new NpcSpeechEvent("Anyway. Cut a long story short, some years " +
				"back the Maze came in force against Hail, and the town was " +
				"destroyed. Or so we assume. Our caravans stopped returning, so " +
				"we stopped sending them. Rumours of death, destruction and " +
				"doom started doing the rounds, but facts are hard to come by."),
			new NpcSpeechEvent("I'm pretty sure Hail was sacked, but that was " +
				"a while ago now. It's worth sending some scouts to find out " +
				"what's going on there now. That's where you come in."),
			new NpcSpeechEvent("I need you to travel to Hail and find out what " +
				"the situation is. Return and report to me if it's worth " +
				"thinking of a caravan, and what such a caravan might be likely " +
				"to face on the way."),
			new NpcSpeechEvent("Payment for the job is the flat rate for a mid " +
				"range scouting mission, 1200 gold pieces. I've wangled in some " +
				"danger pay as well, for a total of 1950. Plus a week of travel " +
				"allowance at 10 gold per day for two days in urban centers " +
				"and 5 per day on the road, 45 total..."),
			new NpcSpeechEvent("Travel allowance and 16% of the sum is payable " +
				"up front - provisioning for your trip. " +
				"That comes to 357. Here we are."),
			new GrantGoldEvent(357),
			new NpcSpeechEvent("The balance is payable on your return with the " +
				"information."),
			new NpcSpeechEvent("Now, about the route. Unfortunately, the only " +
				"two routes to Hail are controlled by the White Order and the " +
				"Gnomes. It's a pain, but you will have to travel to either " +
				"Danaos or Aenen and barter with the authorities there for " +
				"access to the gates that will transport you towards Hail.\n\n" +
				"The C.O.C. will expense any reasonable claims in this regard " +
				"if necessary, just bring the receipts back."),
			new NpcSpeechEvent("There is some dangerous wilderness between " +
				"those gates and Hail, so best be prepared."),
			new NpcSpeechEvent("Good luck."));

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("Come back when you have information about the " +
				"situation in Hail."),
			new NpcLeavesEvent(),
			new MazeScriptEvent("generic door creak"),
			new MovePartyEvent(new Point(2, 3), CrusaderEngine.Facing.WEST));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("So, you're back. Tell me about Hail..."),
			new NpcSpeechEvent("..."),
			new NpcSpeechEvent("Yes. Well. It sounds like we won't be running " +
				"any caravans out to Hail any time soon. A pity. But good to know."),
			new NpcSpeechEvent("Here we go, the balance payable for a job well " +
				"done. 1638 gold pieces."),
			new GrantGoldEvent(1638),
			new NpcSpeechEvent("I've credited you a round at the Adventurer's " +
				"Armoury, you look like you could use a cold beer."),
			new SetMazeVariableEvent(FREE_BOOZE_AT_THE_INN, "true"),
			new GrantExperienceEvent(300, null));

		return new Quest(
			QUEST_3_COMPLETED,
			QUEST_3_REWARDED,
			intro,
			reward,
			encouragement);
	}

	/*-------------------------------------------------------------------------*/
	private Quest createQuest4()
	{
		List<MazeEvent> intro = getList(
			new NpcSpeechEvent("My favourite employees! I think that this has " +
				"been a productive agreement for both of us."),
			new NpcSpeechEvent("Your next job is a hard one. Others have " +
				"tried and failed. Perhaps you will succeed."),
			new NpcSpeechEvent("I'll cut to the chase. The local scum and " +
				"villainy have a figurehead, and unofficial leader of sorts, who " +
				"goes by the name of 'Sir Kay'."),
			new NpcSpeechEvent("This Kay is an unprincipled rogue, and a " +
				"plague on our operations! He needs to be brought to justice. " +
				"As of today, there is a substantial bounty on his head."),
			new NpcSpeechEvent("Capture him or kill him, I don't care."),
			new NpcSpeechEvent("The bounty is calculated at regular C.O.C. " +
				"rates. Let's see... 35 counts of grand larceny; 26 of breaking " +
				"and entering; 12 of aggravated assault. 38 counts of first " +
				"degree murder. Five of culpable homicide. One count of " +
				"violating the public order, and one of high treason. That " +
				"comes to over twenty thousand... 20982 gold pieces to be precise, " +
				"for the capture or execution of the bandit who calls himself " +
				"Sir Kay."),
			new NpcSpeechEvent("Needless to say, this is a dangerous man. " +
				"The C.O.C. is not liable for any medical bills or other " +
				"expenses you might incur."),
			new NpcSpeechEvent("This is a public bounty, so you're not the " +
				"only team hunting this reprobate Kay. But because of our special " +
				"relationship I've got board approval to sweeten the pot " +
				"just for you, should you get him, to the tune of an extra five " +
				"thousand gold pieces. 25982 total, the biggest bounty in the " +
				"history of Ichiba!"),
			new NpcSpeechEvent("Good hunting, I hope you get him.\n\nKay tends " +
				"to lurk around the city and skulk in the forest outside the " +
				"gates. You may already have seen him..."));

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("My sources say that Kay is still at large."),
			new NpcLeavesEvent(),
			new MazeScriptEvent("generic door creak"),
			new MovePartyEvent(new Point(2, 3), CrusaderEngine.Facing.WEST));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("So you got him! The much spoken about Kay, " +
				"dead at last. A plague on me no longer."),
			new NpcSpeechEvent("Here is the bounty, richly deserved."),
			new GrantGoldEvent(25982),
			new NpcSpeechEvent("Why don't you get a drink at the Armoury on me, " +
				"as usual? I'd join you, but I have a board meeting this evening."),
			new SetMazeVariableEvent(FREE_BOOZE_AT_THE_INN, "true"),
			new GrantExperienceEvent(500, null));

		return new Quest(
			QUEST_4_COMPLETED,
			QUEST_4_REWARDED,
			intro,
			reward,
			encouragement);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("You step into a plush if somewhat musty " +
				"smelling office. Tasteful pot plants and portraits decorate " +
				"the walls, framing a polished wooden desk at the far end.\n"),
			new FlavourTextEvent("A sharply dressed fellow smiles and " +
				"rises from the luxurious leather chair behind the desk to " +
				"approach you, hand extended in greeting..."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Yes, yes, good to meet you. I'm Pickett, W E " +
				"Pickett, Director, Ichiba Chamber of Commerce. I've heard " +
				"of your arrival. How are you settling into Ichiba? " +
				"Please to finally meet you."));

		checkQuests(result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected void checkQuests(List<MazeEvent> result)
	{
		if (MazeVariables.getBoolean(CocSheriffNpc.COC_QUEST_1_PRIMER))
		{
			if (MazeVariables.getBoolean(SirKay.SIGNED_UP_WITH_THIEVES_GUILD))
			{
				MazeVariables.clear(CocSheriffNpc.COC_QUEST_1_PRIMER);
				result.add(new NpcSpeechEvent("So, yes. I hear you're here for the " +
					"mercenary position."));
				result.add(new NpcSpeechEvent("Unfortunately, I've heard rumours " +
					"that you have some association with that scum, Kay, and his " +
					"band of unscrupulous brigands."));
				result.add(new NpcSpeechEvent("As a result, I cannot consider " +
					"you for this position. Goodbye."));
				result.add(new NpcLeavesEvent());
				result.add(new MazeScriptEvent("generic door creak"));
				result.add(new MovePartyEvent(new Point(2, 3), CrusaderEngine.Facing.WEST));
			}
			else if (MazeVariables.getBoolean(Imogen.SIGNED_UP_WITH_IMOGEN))
			{
				MazeVariables.clear(CocSheriffNpc.COC_QUEST_1_PRIMER);
				result.add(new NpcSpeechEvent("So, yes. I hear you're here for the " +
					"mercenary position."));
				result.add(new NpcSpeechEvent("Unfortunately, I've heard that " +
					"you have some kind of association with the witch Imogen."));
				result.add(new NpcSpeechEvent("As a result, I cannot consider " +
					"you for this position. Goodbye."));
				result.add(new NpcLeavesEvent());
				result.add(new MazeScriptEvent("generic door creak"));
				result.add(new MovePartyEvent(new Point(2, 3), CrusaderEngine.Facing.WEST));
			}
			else
			{
				MazeVariables.clear(CocSheriffNpc.COC_QUEST_1_PRIMER);
				result.add(new NpcSpeechEvent("So, yes. I hear you're here for the " +
					"mercenary position? Good, good."));
				result.add(new NpcSpeechEvent("Work will be sporadic, you'll get " +
					"your jobs directly from me. Like to keep my finger on the pulse. " +
					"The nature of the jobs will vary, " +
					"there will always be an element of risk. " +
					"Payment will be on a per-job basis, competitive " +
					"rates plus reasonable expenses."));
				result.add(new NpcSpeechEvent("Well. Do you want to enlist?"));
				result.add(new SetMazeVariableEvent(WAITING_FOR_SIGN_UP_REPLY, "true"));
				result.add(new WaitForPlayerSpeech(npc, getPlayerCharacter(0)));
			}
		}
		else
		{
			if (MazeVariables.getBoolean(SIGNED_UP_COC))
			{
				super.checkQuests(result);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> attacksParty()
	{
		if (MazeVariables.getBoolean(Imogen.QUEST_3_COMPLETE))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();
			result.add(new NpcSpeechEvent("So, Imogen has had her way!"));
			result.add(new NpcSpeechEvent("And you were the tool of the witch. " +
				"I hope you enjoyed the compensation you received for your " +
				"services, because you do not have long to live."));
			result.add(new NpcSpeechEvent("Indeed, I do no think that you are " +
				"here to talk!\n\nGuards!!!!"));
			result.add(new NpcAttacksEvent(npc));
			return result;
		}
		else
		{
			return super.attacksParty();
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Yes, yes, how are you? Nice to see you again, " +
				"trust you're well."));

		checkQuests(result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Yes, you again? " +
				"I have a meeting in ten minutes..."));

		checkQuests(result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Goodbye."),
			new NpcLeavesEvent(),
			new MazeScriptEvent("generic door creak"),
			new MovePartyEvent(new Point(2, 3), CrusaderEngine.Facing.WEST));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Farewell, pop in again when you're back in " +
				"town."),
			new NpcLeavesEvent(),
			new MazeScriptEvent("generic door creak"),
			new MovePartyEvent(new Point(2, 3), CrusaderEngine.Facing.WEST));
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
					new SetMazeVariableEvent(SIGNED_UP_COC, "true"),
					new NpcSpeechEvent("Good stuff. Capital."));

				checkQuests(result);
				
				return result;
			}
			else if (NpcSpeech.sentenceContainsKeywords(speech, "no", "nay"))
			{
				MazeVariables.clear(WAITING_FOR_SIGN_UP_REPLY);
				List<MazeEvent> result = getList(
					new NpcSpeechEvent("Oh. Well, then. Excuse me, I have a " +
						"meeting soon."));
				result.addAll(partyLeavesNeutral());
				return result;
			}
			else
			{
				return getList(
					new NpcSpeechEvent("Eh what? Speak up there."),
					new WaitForPlayerSpeech(npc, pc));
			}
		}
		else
		{
			return super.parsePartySpeech(pc, speech);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		if (item.getName().equals("Hail Cider") && !MazeVariables.getBoolean(BEEN_GIVEN_HAIL_CIDER))
		{
			return getList(
				new NpcSpeechEvent("Oh my. Hail Cider! I haven't had any for a " +
					"very, very long time!"),
				new NpcTakesItemEvent(owner, item, npc),
				new NpcSpeechEvent("Much appreciated, to be sure."),
				new NpcSpeechEvent("Say, um, why don't you take this? I've been " +
					"keeping it as a momento of my younger days, you know. A long " +
					"time ago now, those warm nights at the Black Dog Inn. Heh heh " +
					"heh. Um, I don't really need it any more, but you look " +
					"like you might use it."),
				new GrantItemsEvent(createItem("Pickett's Brooch")));
		}
		else
		{
			return super.givenItemByParty(owner, item);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}