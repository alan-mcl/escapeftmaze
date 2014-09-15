
package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
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
	private static final String ICHIBA_CITY = "Ichiba City";
	private static final String ICHIBA_CROSSROAD= "Ichiba Crossroad";

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
			new NpcSpeechEvent("So hey. It just so happens I have " +
				"a job for you. Specially suited to your talents, I think."),
			new NpcSpeechEvent("We have a safe house built " +
				"against the west wall. Right behind the big old Chamber of " +
				"Commerce building! Ha ha ha! Priceless, but anyway."),
			new NpcSpeechEvent("It has access to the sewers " +
				"below the city - handy for us thieves to move " +
				"around, you know."),
			new NpcSpeechEvent("But here's the problem - " +
				"some kind of nasty slime creature has turned up. " +
				"Personally I suspect it escaped from the blasted witch's " +
				"tower."),
			new NpcSpeechEvent("So, I need you to go down " +
				"there and waste the slime. Come find me again when " +
				"you're done."),
			new SetMazeVariableEvent(SPAWN_CORROSIVE_SLIME, "true"),
			new NpcLeavesEvent());

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("Still haven't cleared out that slime, eh?"),
			new NpcSpeechEvent("The way down to the sewers is in the safe " +
				"house on the west wall."));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("I hear you cleaned out that slime down below."),
			new NpcSpeechEvent("Good work. Here's some cash, don't spend it " +
				"all in one place!"),
			new GrantGoldEvent(200),
			new GrantExperienceEvent(100, null));

		return new Quest(
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
			new NpcSpeechEvent("Coincidentally, I have more work for you."),
			new NpcSpeechEvent("That crazy witch Imogen is in bed with the gnolls " +
				"again. Um, figuratively of course. Anyway, I can't have that, " +
				"her schemes are as bad for business as the Chamber and " +
				"their goons."),
			new NpcSpeechEvent("To put a damper on things, I need you to " +
				"go and steal something from their head shaman. He's called " +
				"Three Eyes - smarter than he looks, albeit for a gnoll that " +
				"particular bar is rather low."),
			new NpcSpeechEvent("In his hut you will find a skull totem that he " +
				"has to use in a ceremony before the gnolls can go to war. They " +
				"call it the Dreamer Skull. Nab it " +
				"and the breaks will be on, at least until he can change the " +
				"myths or whatever shamen do."),
			new NpcSpeechEvent("The main gnoll village is north of here. " +
				"But don't go through the forest, you won't find it that way. " +
				"There's a way through the sewers under the north wall there, " +
				"look for an entry on of the shacks over that side."),
			new NpcSpeechEvent("Not to be pushy, but the sooner the better, " +
				"if you know what I mean."),
			new NpcLeavesEvent());

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("I need you to steal the Dreamer Skull from " +
				"Three Eyes. Come find me when you do."));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("Excellent work. We needn't worry about hordes " +
				"of screaming gnolls disrupting things any time soon."),
			new NpcSpeechEvent("Here is suitable compensation."),
			new GrantGoldEvent(500),
			new GrantExperienceEvent(200, null));

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
			new NpcSpeechEvent("Say, I have some news you might not like."),
			new NpcSpeechEvent("Looks like you lot have annoyed the wrong " +
				"people. Perhaps it has to do with our association?"),
			new NpcSpeechEvent("In any case, that worthy association the " +
				"Ichiba Chamber of Commerce has put a price on your head. A " +
				"mere one thousand gold - I would be quite insulted if I were you."),
			new NpcSpeechEvent("That's not all - I am reliably informed that " +
				"they've retained a group of mercenaries to hunt you down!"),
			new NpcSpeechEvent("Savour the feeling my friends. I remember the " +
				"first time I was hunted..."),
			new NpcSpeechEvent("Be that as it may. You'd better lie low for a while. " +
				"I can't really use you at all with the amount of heat that " +
				"you've attracted. Don't come asking me for any more work until " +
				"things have quieted down a bit. Would appreciate it if you'd " +
				"avoid using our Ichiba safehouses, and not be seen with me in " +
				"public either. No offense intended, of course."),
			new SetMazeVariableEvent(QUEST_3_STARTED, "true"),
			new NpcLeavesEvent());

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("Look, come back when the heat is off ok."),
			new NpcLeavesEvent());

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("A little bird told me that you wasted the team " +
				"sent to hunt you down. Nice work."),
			new NpcSpeechEvent("You may be interested to hear that your " +
				"bounty has been upped to five thousand... but the Chamber has " +
				"no takers for the job! Heh heh, you really made an example of " +
				"that lot."),
			new GrantExperienceEvent(400, null));

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
			new NpcSpeechEvent("Well, you have proved youselves quite the " +
				"heros. I am impressed."),
			new NpcSpeechEvent("I have some very disturbing news, and a new task " +
				"for you. A difficult one."),
			new NpcSpeechEvent("It concerns that cussed witch, Imogen. For years she " +
				"has played her cards close to her chest, and I've had to " +
				"resort to reacting to her moves."),
			new NpcSpeechEvent("Even without this new " +
				"information, it has recently been obvious that she is nearing " +
				"some kind of goal - the gnolls are mobilising, and there have " +
				"been more than the usual strange comings and goings from the " +
				"tower."),
			new NpcSpeechEvent("Finally, I got a body into her damned tower " +
				"who was able to poke around for a few minutes. Her master plan " +
				"is as crazy as you might expect - she is planning an incantation " +
				"to grant her eternal life, and at the same time planning the " +
				"overthrow of the status quo in Ichiba - the destruction of the " +
				"Chamber and her ruling unchecked over us all."),
			new NpcSpeechEvent("Needless to say, that would be bad for business. " +
				"Much as I despise the Chamber of Commerce, they are good prey. " +
				"Rather the devil you know, and all that."),
			new NpcSpeechEvent("So, I need you to slip into the tower and " +
				"off the witch."),
			new NpcSpeechEvent("Needless to say, she's a dangerous target. But I " +
				"think that you're up to it."),
			new NpcSpeechEvent("In her paranoia she has fortified the tower " +
				"quite impenetrably, so the plan is simple - you go in through " +
				"the front door, locate her and kill her. Needless to say, you " +
				"can keep any loot that you come by in the place."),
			new NpcSpeechEvent("Heh, and here's a little something to help you deal " +
				"with the golem at the door. Wear it as you enter, and if you're " +
				"lucky it'll deactivate the construct. The last intruder used it, " +
				"hopefully it still has enough charge..."),
			new GrantItemsEvent(createItem("Rock Salt Amulet")),
			new NpcSpeechEvent("We will not speak of this any more - Imogen has " +
				"ears everywhere. Good luck"),
			new NpcLeavesEvent());

		List<MazeEvent> encouragement = getList(
			new NpcSpeechEvent("You lot really need to take care of that task " +
				"I mentioned to you... the situation is speeding up."));

		List<MazeEvent> reward = getList(
			new NpcSpeechEvent("Imogen is dead. Skillfully managed, if I do say " +
				"so myself."),
			new NpcSpeechEvent("I trust that you found some good stuff in her " +
				"tower? Keep it."),
			new GrantExperienceEvent(1000, null),
			new NpcSpeechEvent("The Chamber of Commerce has no idea that you " +
				"have saved them too, and have upped the price on your head to " +
				"ten thousand gold - almost as much as me!"),
			new NpcSpeechEvent("That almost impressed me more, ha ha ha! However, " +
				"Ichiba is too hot for you lot right now. I have no more work for " +
				"you right now, and I if I were you I'd leave town for a while " +
				"until things calm down a bit."));

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
		return getList(new FlavourTextEvent("From out of the shadows, a " +
			"slim figure silently emerges and blocks your path."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Salutations, travellers."),
			new NpcSpeechEvent("Your reputation precedes you, we don't see " +
				"many come through the Gate from the first Realm."),
			new NpcSpeechEvent("My name is Kay."));

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
					"isn't one of your strong suits.\n\nPrepare to die."));
				result.add(new NpcAttacksEvent(npc));
				return;
			}
			else
			{
				result.add(new NpcSpeechEvent("I'll cut to the chase. Here in " +
					"Ichiba, my Gentlemen's Social Club is the only gig in town, " +
					"if you know what I mean."));
				result.add(new NpcSpeechEvent("That's right, I know about your " +
					"skulduggery. Don't look so surprised. You've been watched " +
					"since you arrived in town. My merry men are very good at that."));

				if (MazeVariables.getBoolean(WePickett.SIGNED_UP_COC))
				{
					MazeVariables.clear(SIR_KAY_PARTY_DETECTED_STEALING);
					MazeVariables.set(SIR_KAY_PARTY_WARNING,"true");
					result.add(new NpcSpeechEvent("For the same reason, I already " +
						"know that you're a paid lacky of the Chamber of Commerce."));
					result.add(new NpcSpeechEvent("In a way I appreciate your " +
						"duplicity, working for them on the one hand, and " +
						"stealing from them on the side. Still, business is " +
						"business, and any lackey of the Chamber is my enemy."));
					result.add(new NpcSpeechEvent("I am giving you one warning. " +
						"Steal anything else on my turf, and I will kill you.\n\n" +
						"Good day."));
					result.add(new NpcLeavesEvent());
				}
				else if (MazeVariables.getBoolean(Imogen.SIGNED_UP_WITH_IMOGEN))
				{
					MazeVariables.clear(SIR_KAY_PARTY_DETECTED_STEALING);
					MazeVariables.set(SIR_KAY_PARTY_WARNING,"true");
					result.add(new NpcSpeechEvent("For the same reason, I already " +
						"know that you're associated with the crazy witch, " +
						"Imogen."));
					result.add(new NpcSpeechEvent("That one is no friend of mine, " +
						"and for all I know she is watching me through your eyes " +
						"as we speak. Watch your soul in her employ!"));
					result.add(new NpcSpeechEvent("Anyway. I am giving you one " +
						"warning. Steal anything else on my turf, and I will " +
						"kill you.\n\nGood day."));
					result.add(new NpcLeavesEvent());
				}
				else
				{
					result.add(new NpcSpeechEvent("I can't have just any old thief who " +
						"feels like it mucking about on my turf, stealing anything and " +
						"causing trouble. You've been caught, friends, " +
						"and now you have a choice. Join or die.\n\nWhich is it?"));
					result.add(new SetMazeVariableEvent(SIR_KAY_WAIT_FOR_RESPONSE_1, "true"));
					result.add(new WaitForPlayerSpeech(npc, getPlayerCharacter(0)));
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		List<MazeEvent> result = getList(
			new NpcSpeechEvent("Salutations, travellers."));

		checkPartyHasStolen(result);

		if (MazeVariables.getBoolean(SIR_KAY_OWED_500GP))
		{
			result.addAll(
				getList(
					new NpcSpeechEvent("Say, you still owe me membership fees.\n\n" +
						"Got the money yet?"),
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
				"mine! Thank you kindly, I nearly dropped it."),
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
					new NpcSpeechEvent("I thought you would."),
					new NpcSpeechEvent("Membership fees are a mere five hundred " +
						"gold, for life. Until death do us part, so to speak. " +
						"Heh heh!"),
					new NpcSpeechEvent("Got the money on you?"),
					new SetMazeVariableEvent(SIR_KAY_WAIT_FOR_RESPONSE_2, "true"),
					new WaitForPlayerSpeech(npc, getPlayerCharacter(0)));
			}
			else if (NpcSpeech.sentenceContainsKeywords(speech, "die"))
			{
				MazeVariables.clear(SIR_KAY_WAIT_FOR_RESPONSE_1);
				return getList(
					new NpcSpeechEvent("There's a fool born every minute.\n\n" +
						"Defend yourselves!"),
					new NpcAttacksEvent(npc));
			}
			else
			{
				return getList(
					new NpcSpeechEvent("Wrong answer cully! Try again."),
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
						new NpcSpeechEvent("Excellent, I'll take that."));

					signUpForGsc(result);

					return result;
				}
				else
				{
					List<MazeEvent> result = getList(
						new NpcSpeechEvent("Looks like you can't, actually."),
						new NpcSpeechEvent("No matter, you can pay when you have " +
							"the funds."),
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
							"have here!"),
						new NpcSpeechEvent("Isn't this your purse, cully? " +
							"Sounds to me like it's full of chinking gold pieces..."),
						new NpcSpeechEvent("I'll just take the five hundred for the " +
							"guild, then you can have it back. Good thing I found " +
							"it, you need to watch where you put that thing in " +
							"this town!"));

					signUpForGsc(result);

					return result;
				}
				else
				{
					List<MazeEvent> result = getList(
						new NpcSpeechEvent("No matter, you can pay it when " +
							"you have it."),
						new SetMazeVariableEvent(SIR_KAY_OWED_500GP, "true"));

					signUpForGsc(result);

					return result;
				}
			}
			else
			{
				return getList(
					new NpcSpeechEvent("Eh what? Speak up, you're muttering..."),
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
			"Gentlemen's Social Club! Congratulations! Ha ha ha!"));
		result.add(new NpcSpeechEvent("Rules are simple. One, do what I say."));
		result.add(new NpcSpeechEvent("Two, keep a low profile, don't get " +
			"into trouble with the authorities."));
		result.add(new NpcSpeechEvent("Every so often I may have something " +
			"for you to do - and I'll come and find you when I do."));
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
			boolean hasNewQuest = npc.getQuestManager().hasNewQuestAvailable();
			boolean questCompleted = npc.getQuestManager().isCurrentQuestCompleted();
			boolean questRewarded = npc.getQuestManager().isCurrentQuestRewarded();

			boolean moveTowardsParty = (detectedStealing && !signedUp) ||
				(signedUp && (hasNewQuest || (questCompleted && !questRewarded) || turnNr % 50 == 0));

			if (moveTowardsParty)
			{
				return moveSirKayTowardsParty();
			}
			else
			{
				// random movement
				if (Dice.d20.roll() == 1)
				{
					// change zone
					return changeSirKayZone();
				}
				else
				{
					return moveSirKayWithinZone();
				}
			}
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> moveSirKayTowardsParty()
	{
		String partyZone = Maze.getInstance().getZone().getName();

		if (!(ICHIBA_CITY.equals(partyZone) || ICHIBA_CROSSROAD.equals(partyZone)))
		{
			return moveSirKayWithinZone();
		}

		Point partyTile = Maze.getInstance().getPlayerPos();
		Point tile = npc.getTile();

		// halve the distance between Kay and the party
		int diffX = (partyTile.x - tile.x) / 2;
		int diffY = (partyTile.y - tile.y) / 2;

		int nX = diffX<5 ? partyTile.x : tile.x + diffX;
		int nY = diffY<5 ? partyTile.y : tile.y + diffY;

		return getList(new ChangeNpcLocationEvent(npc, new Point(nX, nY), partyZone));
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> moveSirKayWithinZone()
	{
		String zone = npc.getZone();

		Point newTile;

		if (ICHIBA_CITY.equals(zone))
		{
			newTile = getRandomIchibaCrossroadTile();
		}
		else if (ICHIBA_CROSSROAD.equals(zone))
		{
			newTile = getRandomIchibaCityTile();
		}
		else
		{
			throw new MazeException("Invalid zone for Sir Kay ["+zone+"]");
		}

		return getList(new ChangeNpcLocationEvent(npc, newTile, zone));
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> changeSirKayZone()
	{
		String zone = npc.getZone();

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

		return getList(new ChangeNpcLocationEvent(npc, newTile, newZone));
	}

	/*-------------------------------------------------------------------------*/
	private Point getRandomIchibaCityTile()
	{
		Point newTile;
		newTile = new Point(Dice.d20.roll()+10, Dice.d20.roll()+10);
		return newTile;
	}

	/*-------------------------------------------------------------------------*/
	private Point getRandomIchibaCrossroadTile()
	{
		Point newTile;
		newTile = new Point(Dice.d20.roll()+5, Dice.d20.roll()+5);
		return newTile;
	}
}
