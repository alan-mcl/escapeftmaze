
package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.*;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.*;
import mclachlan.maze.util.MazeException;

/**
 * Boss of the Ichiba Gentlemen's Social Club.
 */
public class SirKay extends NpcScript
{
	public static final String SIR_KAY_PARTY_DETECTED_STEALING = "sir.kay.detects.party.stealing";
	public static final String SIR_KAY_PARTY_WARNING = "sir.kay.warning";
	public static final String SIR_KAY_WAIT_FOR_RESPONSE_1 = "sir.kay.wait.for.response.1";
	public static final String SIR_KAY_WAIT_FOR_RESPONSE_2 = "sir.kay.wait.for.response.2";
	public static final String SIR_KAY_OWED_500GP = "sir.kay.owed.500.gp";
	public static final String SIGNED_UP_WITH_THIEVES_GUILD = "gsc.signed.up";

	public static final String CORROSIVE_SLIME_SLAIN = "sir.kay.corrosive.slime.slain";
	public static final String CORROSIVE_SLIME_REWARD = "sir.kay.corrosive.slime.reward";
	public static final String SPAWN_CORROSIVE_SLIME = "sir.kay.spawn.corrosive.slime";

	public static final String QUEST_2_COMPLETE = "sir.kay.quest.2.complete";
	public static final String QUEST_2_REWARD = "sir.kay.quest.2.reward";

	public static final String QUEST_3_STARTED = "sir.kay.quest.3.started";
	public static final String QUEST_3_COMPLETE = "sir.kay.quest.3.complete";
	public static final String QUEST_3_REWARD = "sir.kay.quest.3.reward";

	public static final String QUEST_4_COMPLETE = "sir.kay.quest.4.complete";
	public static final String QUEST_4_REWARD = "sir.kay.quest.4.reward";

	/*-------------------------------------------------------------------------*/
	protected void start()
	{
		initInternal();
	}

	/*-------------------------------------------------------------------------*/
	private void initInternal()
	{
		QuestManager qm = ((Npc)npc).getQuestManager();

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
			new NpcSpeechEvent("So hey. It just so happens I have " +
				"a job for you. Specially suited to your talents, I think.", npc),
			new NpcSpeechEvent("We have a safe house built " +
				"against the west wall. Right behind the big old Chamber of " +
				"Commerce building! Ha ha ha! Priceless, but anyway.", npc),
			new NpcSpeechEvent("It has access to the sewers " +
				"below the city - handy for us thieves to move " +
				"around, you know.", npc),
			new NpcSpeechEvent("But here's the problem - " +
				"some kind of nasty slime creature has turned up. " +
				"Personally I suspect it escaped from the blasted witch's " +
				"tower.", npc),
			new NpcSpeechEvent("So, I need you to go down " +
				"there and waste the slime. Come find me again when " +
				"you're done.", npc),
			new SetMazeVariableEvent(SPAWN_CORROSIVE_SLIME, "true"),
			new ActorsLeaveEvent());

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("Still haven't cleared out that slime, eh?", npc),
			new NpcSpeechEvent("The way down to the sewers is in the safe " +
				"house on the west wall.", npc));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("I hear you cleaned out that slime down below.", npc),
			new NpcSpeechEvent("Good work. Here's some cash, don't spend it " +
				"all in one place!", npc),
			new GrantGoldEvent(200),
			new GrantExperienceEvent(100, null));

		return new Quest(
			"Kay And The Slime",
			"Go down to the sewers and waste the slime creature.",
			CORROSIVE_SLIME_SLAIN,
			CORROSIVE_SLIME_REWARD,
			intro,
			reward,
			encouragement);
	}

	/*-------------------------------------------------------------------------*/
	private Quest createQuest2()
	{
		List<MazeEvent> intro = getList(
			new NpcSpeechEvent("Coincidentally, I have more work for you.", npc),
			new NpcSpeechEvent("That crazy witch Imogen is in bed with the gnolls " +
				"again. Um, figuratively of course. Anyway, I can't have that, " +
				"her schemes are as bad for business as the Chamber and " +
				"their goons.", npc),
			new NpcSpeechEvent("To put a damper on things, I need you to " +
				"go and steal something from their head shaman. He's called " +
				"Three Eyes - smarter than he looks, albeit for a gnoll that " +
				"particular bar is rather low.", npc),
			new NpcSpeechEvent("In his hut you will find a skull totem that he " +
				"has to use in a ceremony before the gnolls can go to war. They " +
				"call it the Dreamer Skull. Nab it " +
				"and the breaks will be on, at least until he can change the " +
				"myths or whatever shamen do.", npc),
			new NpcSpeechEvent("The main gnoll village is north of here. " +
				"But don't go through the forest, you won't find it that way. " +
				"There's a way through the sewers under the north wall there, " +
				"look for an entry on of the shacks over that side.", npc),
			new NpcSpeechEvent("Not to be pushy, but the sooner the better, " +
				"if you know what I mean.", npc),
			new ActorsLeaveEvent());

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("I need you to steal the Dreamer Skull from " +
				"Three Eyes. Come find me when you do.", npc));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("Excellent work. We needn't worry about hordes " +
				"of screaming gnolls disrupting things any time soon.", npc),
			new NpcSpeechEvent("Here is suitable compensation.", npc),
			new GrantGoldEvent(500),
			new GrantExperienceEvent(200, null));

		return new Quest(
			"Kay And The Skull",

			"Steal the Dreamer Skull from Three Eyes, and Gnoll shaman.",
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
			new NpcSpeechEvent("Say, I have some news you might not like.", npc),
			new NpcSpeechEvent("Looks like you lot have annoyed the wrong " +
				"people. Perhaps it has to do with our association?", npc),
			new NpcSpeechEvent("In any case, that worthy association the " +
				"Ichiba Chamber of Commerce has put a price on your head. A " +
				"mere one thousand gold - I would be quite insulted if I were you.", npc),
			new NpcSpeechEvent("That's not all - I am reliably informed that " +
				"they've retained a group of mercenaries to hunt you down!", npc),
			new NpcSpeechEvent("Savour the feeling my friends. I remember the " +
				"first time I was hunted...", npc),
			new NpcSpeechEvent("Be that as it may. You'd better lie low for a while. " +
				"I can't really use you at all with the amount of heat that " +
				"you've attracted. Don't come asking me for any more work until " +
				"things have quieted down a bit. Would appreciate it if you'd " +
				"avoid using our Ichiba safehouses, and not be seen with me in " +
				"public either. No offense intended, of course.", npc),
			new SetMazeVariableEvent(QUEST_3_STARTED, "true"),
			new ActorsLeaveEvent());

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("Look, come back when the heat is off ok.", npc),
			new ActorsLeaveEvent());

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("A little bird told me that you wasted the team " +
				"sent to hunt you down. Nice work.", npc),
			new NpcSpeechEvent("You may be interested to hear that your " +
				"bounty has been upped to five thousand... but the Chamber has " +
				"no takers for the job! Heh heh, you really made an example of " +
				"that lot.", npc),
			new GrantExperienceEvent(400, null));

		return new Quest(
			"Kay's Advice",
			"Lay low until the heat is off.",
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
			new NpcSpeechEvent("Well, you have proved yourselves quite the " +
				"heros. I am impressed.", npc),
			new NpcSpeechEvent("I have some very disturbing news, and a new task " +
				"for you. A difficult one.", npc),
			new NpcSpeechEvent("It concerns that cussed witch, Imogen. For years she " +
				"has played her cards close to her chest, and I've had to " +
				"resort to reacting to her moves.", npc),
			new NpcSpeechEvent("Even without this new " +
				"information, it has recently been obvious that she is nearing " +
				"some kind of goal - the gnolls are mobilising, and there have " +
				"been more than the usual strange comings and goings from the " +
				"tower.", npc),
			new NpcSpeechEvent("Finally, I got a body into her damned tower " +
				"who was able to poke around for a few minutes. Her master plan " +
				"is as crazy as you might expect - she is planning an incantation " +
				"to grant her eternal life, and at the same time planning the " +
				"overthrow of the status quo in Ichiba - the destruction of the " +
				"Chamber and her ruling unchecked over us all.", npc),
			new NpcSpeechEvent("Needless to say, that would be bad for business. " +
				"Much as I despise the Chamber of Commerce, they are good prey. " +
				"Rather the devil you know, and all that.", npc),
			new NpcSpeechEvent("So, I need you to slip into the tower and " +
				"off the witch.", npc),
			new NpcSpeechEvent("Needless to say, she's a dangerous target. But I " +
				"think that you're up to it.", npc),
			new NpcSpeechEvent("In her paranoia she has fortified the tower " +
				"quite impenetrably, so the plan is simple - you go in through " +
				"the front door, locate her and kill her. You " +
				"can keep any loot that you come by in the place, of course.", npc),
			new NpcSpeechEvent("Heh, and here's a little something to help you deal " +
				"with the golem at the door. Wear it as you enter, and if you're " +
				"lucky it'll deactivate the construct. The last intruder used it, " +
				"hopefully it still has enough charge...", npc),
			new GrantItemsEvent(createItem("Rock Salt Amulet")),
			new NpcSpeechEvent("We will not speak of this any more - Imogen has " +
				"ears everywhere. Good luck", npc),
			new ActorsLeaveEvent());

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("You lot really need to take care of that task " +
				"I mentioned to you... the situation is speeding up.", npc));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("Imogen is dead. Skillfully managed, if I do say " +
				"so myself.", npc),
			new NpcSpeechEvent("I trust that you found some good stuff in her " +
				"tower? Keep it.", npc),
			new GrantExperienceEvent(1000, null),
			new NpcSpeechEvent("The Chamber of Commerce has no idea that you " +
				"have saved them too, and have upped the price on your head to " +
				"ten thousand gold - almost as much as me!", npc),
			new NpcSpeechEvent("That almost impressed me more, ha ha ha! However, " +
				"Ichiba is too hot for you lot right now. I have no more work for " +
				"you right now, and I if I were you I'd leave town for a while " +
				"until things calm down a bit.", npc));

		return new Quest(
			"Kay And The Witch",
			"Find and slay Imogen in her tower.",
			QUEST_4_COMPLETE,
			QUEST_4_REWARD,
			intro,
			reward,
			encouragement);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(new FlavourTextEvent("From out of the shadows, a " +
			"slim figure silently emerges and blocks your path."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Salutations, travellers.", npc),
			new NpcSpeechEvent("Your reputation precedes you, we don't see " +
				"many come through the Gate from the First Realm.", npc),
			new NpcSpeechEvent("My name is Kay.", npc));

		checkPartyHasStolen(result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void checkPartyHasStolen(List<MazeEvent> result)
	{
		if (MazeVariables.getBoolean(SIR_KAY_PARTY_DETECTED_STEALING) &&
			!MazeVariables.getBoolean(SIGNED_UP_WITH_THIEVES_GUILD))
		{
			if (MazeVariables.getBoolean(SIR_KAY_PARTY_WARNING))
			{
				result.add(new NpcSpeechEvent("I warned you once, and you've " +
					"still got your fingers in the pie. Good sense clearly " +
					"isn't one of your strong suits.\n\nPrepare to die.", npc));
				result.add(new NpcAttacksEvent(npc));
				return;
			}
			else
			{
				result.add(new NpcSpeechEvent("I'll cut to the chase. Here in " +
					"Ichiba, my Gentlemen's Social Club is the only gig in town, " +
					"if you know what I mean.", npc));
				result.add(new NpcSpeechEvent("That's right, I know about your " +
					"skulduggery. Don't look so surprised. You've been watched " +
					"since you arrived in town. My merry men are very good at that.", npc));

				if (MazeVariables.getBoolean(WePickett.SIGNED_UP_COC))
				{
					MazeVariables.clear(SIR_KAY_PARTY_DETECTED_STEALING);
					MazeVariables.set(SIR_KAY_PARTY_WARNING,"true");
					result.add(new NpcSpeechEvent("For the same reason, I already " +
						"know that you're a paid lacky of the Chamber of Commerce.", npc));
					result.add(new NpcSpeechEvent("In a way I appreciate your " +
						"duplicity, working for them on the one hand, and " +
						"stealing from them on the side. Still, business is " +
						"business, and any lackey of the Chamber is my enemy.", npc));
					result.add(new NpcSpeechEvent("I am giving you one warning. " +
						"Steal anything else on my turf, and I will kill you.\n\n" +
						"Good day.", npc));
					result.add(new ActorsLeaveEvent());
				}
				else if (MazeVariables.getBoolean(Imogen.SIGNED_UP_WITH_IMOGEN))
				{
					MazeVariables.clear(SIR_KAY_PARTY_DETECTED_STEALING);
					MazeVariables.set(SIR_KAY_PARTY_WARNING,"true");
					result.add(new NpcSpeechEvent("For the same reason, I already " +
						"know that you're associated with the crazy witch, " +
						"Imogen.", npc));
					result.add(new NpcSpeechEvent("That one is no friend of mine, " +
						"and for all I know she is watching me through your eyes " +
						"as we speak. Watch your soul in her employ!", npc));
					result.add(new NpcSpeechEvent("Anyway. I am giving you one " +
						"warning. Steal anything else on my turf, and I will " +
						"kill you.\n\nGood day.", npc));
					result.add(new ActorsLeaveEvent());
				}
				else
				{
					result.add(new NpcSpeechEvent("I can't have just any old thief who " +
						"feels like it mucking about on my turf, stealing anything and " +
						"causing trouble. You've been caught, friends, " +
						"and now you have a choice. Join or die.\n\nWhich is it?", npc));
					result.add(new SetMazeVariableEvent(SIR_KAY_WAIT_FOR_RESPONSE_1, "true"));
					result.add(new WaitForPlayerSpeech(npc, getPlayerCharacter(0)));
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> friendlyGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Salutations, travellers.", npc));

		checkPartyHasStolen(result);

		if (MazeVariables.getBoolean(SIR_KAY_OWED_500GP))
		{
			result.addAll(
				getList(
					new NpcSpeechEvent("Say, you still owe me membership fees.\n\n" +
						"Got the money yet?", npc),
					new SetMazeVariableEvent(SIR_KAY_WAIT_FOR_RESPONSE_2, "true"),
					new WaitForPlayerSpeech(npc, getPlayerCharacter(0))));
		}

		if (MazeVariables.getBoolean(SIGNED_UP_WITH_THIEVES_GUILD))
		{
			checkQuests(result);
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		return getList(
			new NpcSpeechEvent("Oh dear, I think that "+item.getName()+" is " +
				"mine! Thank you kindly, I nearly dropped it.", npc),
			new ChangeNpcTheftCounter(npc, 1));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		if (MazeVariables.getBoolean(SIR_KAY_WAIT_FOR_RESPONSE_1))
		{
			if (NpcSpeech.sentenceContainsKeywords(speech, "join"))
			{
				MazeVariables.clear(SIR_KAY_WAIT_FOR_RESPONSE_1);
				return getList(
					new NpcSpeechEvent("I thought you would.", npc),
					new NpcSpeechEvent("Membership fees are a mere five hundred " +
						"gold, for life. Until death do us part, so to speak. " +
						"Heh heh!", npc),
					new NpcSpeechEvent("Got the money on you?", npc),
					new SetMazeVariableEvent(SIR_KAY_WAIT_FOR_RESPONSE_2, "true"),
					new WaitForPlayerSpeech(npc, getPlayerCharacter(0)));
			}
			else if (NpcSpeech.sentenceContainsKeywords(speech, "die"))
			{
				MazeVariables.clear(SIR_KAY_WAIT_FOR_RESPONSE_1);
				return getList(
					new NpcSpeechEvent("There's a fool born every minute.\n\n" +
						"Defend yourselves!", npc),
					new NpcAttacksEvent(npc));
			}
			else
			{
				return getList(
					new NpcSpeechEvent("Wrong answer cully! Try again.", npc),
					new WaitForPlayerSpeech(npc, getPlayerCharacter(0)));
			}
		}
		else if (MazeVariables.getBoolean(SIR_KAY_WAIT_FOR_RESPONSE_2))
		{
			if (NpcSpeech.sentenceContainsKeywords(speech, "yes", "yay", "yeah"))
			{
				MazeVariables.clear(SIR_KAY_WAIT_FOR_RESPONSE_2);

				if (getParty().getGold() >= 500)
				{
					getParty().incGold(-500);
					MazeVariables.clear(SIR_KAY_OWED_500GP);
					List<MazeEvent> result = getList(
						new NpcSpeechEvent("Excellent, I'll take that.", npc));

					signUpForGsc(result);

					return result;
				}
				else
				{
					List<MazeEvent> result = getList(
						new NpcSpeechEvent("Looks like you can't, actually.", npc),
						new NpcSpeechEvent("No matter, you can pay when you have " +
							"the funds.", npc),
						new SetMazeVariableEvent(SIR_KAY_OWED_500GP, "true"));

					signUpForGsc(result);

					return result;
				}
			}
			else if (NpcSpeech.sentenceContainsKeywords(speech, "no", "nay"))
			{
				MazeVariables.clear(SIR_KAY_WAIT_FOR_RESPONSE_2);

				if (getParty().getGold() >= 500)
				{
					getParty().incGold(-500);
					MazeVariables.clear(SIR_KAY_OWED_500GP);
					List<MazeEvent> result = getList(
						new NpcSpeechEvent("Oh.\n\nHmmmm, that's odd, look what I " +
							"have here!", npc),
						new NpcSpeechEvent("Isn't this your purse, cully? " +
							"Sounds to me like it's full of chinking gold pieces...", npc),
						new NpcSpeechEvent("I'll just take the five hundred for the " +
							"guild, then you can have it back. Good thing I found " +
							"it, you need to watch where you put that thing in " +
							"this town!", npc));

					signUpForGsc(result);

					return result;
				}
				else
				{
					List<MazeEvent> result = getList(
						new NpcSpeechEvent("No matter, you can pay it when " +
							"you have it.", npc),
						new SetMazeVariableEvent(SIR_KAY_OWED_500GP, "true"));

					signUpForGsc(result);

					return result;
				}
			}
			else
			{
				return getList(
					new NpcSpeechEvent("Eh what? Speak up, you're muttering...", npc),
					new WaitForPlayerSpeech(npc, getPlayerCharacter(0)));
			}
		}
		else
		{
			return super.parsePartySpeech(pc, speech);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void signUpForGsc(List<MazeEvent> result)
	{
		result.add(new SetMazeVariableEvent(SIGNED_UP_WITH_THIEVES_GUILD, "true"));
		result.add(new NpcSpeechEvent("I hereby dub thee Fellows of the " +
			"Gentlemen's Social Club! Congratulations! Ha ha ha!", npc));
		result.add(new NpcSpeechEvent("Rules are simple. One, do what I say.", npc));
		result.add(new NpcSpeechEvent("Two, keep a low profile, don't get " +
			"into trouble with the authorities.", npc));
		result.add(new NpcSpeechEvent("Every so often I may have something " +
			"for you to do - and I'll come and find you when I do.", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		if (item.getName().equals("Dreamer Skull"))
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
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		// by default, Sir Kay wanders in Ichiba City and Ichiba Crossroads
		// if the party steals anything from a vendor in Ichiba, he tracks them
		// and appears shortly

		boolean timeToMove = turnNr % 10 == 0;

		if (timeToMove)
		{
			boolean detectedStealing = MazeVariables.getBoolean(SIR_KAY_PARTY_DETECTED_STEALING);
			boolean signedUp = MazeVariables.getBoolean(SIGNED_UP_WITH_THIEVES_GUILD);
			boolean hasNewQuest = ((Npc)npc).getQuestManager().hasNewQuestAvailable();
			boolean questCompleted = ((Npc)npc).getQuestManager().isCurrentQuestCompleted();
			boolean questRewarded = ((Npc)npc).getQuestManager().isCurrentQuestRewarded();

			boolean moveTowardsParty = (detectedStealing && !signedUp) ||
				(signedUp && (hasNewQuest || (questCompleted && !questRewarded) || turnNr % 50 == 0));

			if (moveTowardsParty)
			{
				List<MazeEvent> mazeEvents = moveNpcTowardsParty();
				if (mazeEvents != null)
				{
					return mazeEvents;
				}
			}
			// random movement
			if (Dice.d20.roll("Sir Kay zone") == 1)
			{
				// change zone
				return changeSirKayZone();
			}
			else
			{
				return moveSirKayWithinZone();
			}
		}
		else
		{
			return new ArrayList<>();
		}
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> moveSirKayWithinZone()
	{
		String zone = ((Npc)npc).getZone();

		Point newTile;

		if (ICHIBA_CITY.equals(zone))
		{
			newTile = getRandomIchibaCityTile();
		}
		else if (ICHIBA_CROSSROAD.equals(zone))
		{
			newTile = getRandomIchibaCrossroadTile();
		}
		else
		{
			throw new MazeException("Invalid zone for Sir Kay ["+zone+"]");
		}

		return getList(new ChangeNpcLocationEvent(((Npc)npc), newTile, zone));
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> changeSirKayZone()
	{
		String zone = ((Npc)npc).getZone();

		String newZone;
		Point newTile;

		if (ICHIBA_CITY.equals(zone))
		{
			// move to the crossroads, random tile in (5..25,2..25)
			newZone = ICHIBA_CROSSROAD;
			newTile = getRandomIchibaCrossroadTile();
		}
		else if (ICHIBA_CROSSROAD.equals(zone))
		{
			// move to the city, random tile in (11..30,11..30)
			newZone = ICHIBA_CITY;
			newTile = getRandomIchibaCityTile();
		}
		else
		{
			throw new MazeException("Invalid zone for Sir Kay ["+zone+"]");
		}

		return getList(new ChangeNpcLocationEvent(((Npc)npc), newTile, newZone));
	}

	/*-------------------------------------------------------------------------*/
	private Point getRandomIchibaCityTile()
	{
		Point newTile;
		newTile = new Point(Dice.d20.roll("Sir Kay city coords")+10, Dice.d20.roll("Sir Kay city coords")+10);
		return newTile;
	}

	/*-------------------------------------------------------------------------*/
	private Point getRandomIchibaCrossroadTile()
	{
		Point newTile;
		newTile = new Point(Dice.d20.roll("Sir Kay xroads coords")+5, Dice.d20.roll("Sir Kay xroads coords")+5);
		return newTile;
	}
}
