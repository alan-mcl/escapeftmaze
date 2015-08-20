
package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.DisplayOptionsEvent;
import mclachlan.maze.game.event.MovePartyEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.GrantGoldEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.*;
import mclachlan.maze.util.MazeException;

/**
 * Doorwarden of Danaos Castle
 */
public class Stenelaus extends NpcScript
{
	public static final String INVITATION_FROM_LEONALS = "leonal.invitation.to.party";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("A tall mailed leonal stands with a drawn " +
				"blade before the gateway."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Hail strangers! I am Stenelaus, Doorwarden " +
				"of the mighty Castle Danaos in the lands of the Leonals."),
			new NpcSpeechEvent("Even in these times of relative peace, the " +
				"doors of the castle are open only to those with business here."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> subsequentGreeting()
	{
		return getList(
			new NpcSpeechEvent("Welcome back friends."));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return subsequentGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		if (MazeVariables.get(INVITATION_FROM_LEONALS) != null)
		{
			// party is authorized to enter
			return getList(
				new NpcSpeechEvent("You may pass within."),
				new ActorsLeaveEvent());
		}
		else
		{
			// party is not authorized
			return getList(
				new NpcSpeechEvent("You may not pass within."),
				new MovePartyEvent(new Point(5, 16), CrusaderEngine.Facing.WEST),
				new ActorsLeaveEvent());
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return partyLeavesNeutral();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		if (NpcSpeech.sentenceContainsKeywords(speech, "diomedes") &&
			NpcSpeech.sentenceContainsKeywords(speech, "sent", "send", "sends") &&
			NpcSpeech.sentenceContainsKeywords(speech, "blessing", "blessings", "bless", "blessed") &&
			MazeVariables.getBoolean(Diomedes.INVITATION_FROM_DIOMEDES) &&
			!MazeVariables.getBoolean(INVITATION_FROM_LEONALS))
		{
			return getList(
				new NpcSpeechEvent("So you are those that the pious Diomedes " +
					"sent word of?"),
				new NpcSpeechEvent("A fine Leonal, Diomedes, strong and " +
					"faithful. I know his family well."),
				new NpcSpeechEvent("If you have his trust then you have mine. " +
					"I will grant you admittance to the castle. Your good " +
					"conduct is assumed; do not disgrace me or Diomedes in this " +
					"matter."),
				new ChangeNpcFactionAttitudeEvent(
					npc.getFaction(),
					NpcFaction.Attitude.FRIENDLY,
					null,
					ChangeNpcFactionAttitudeEvent.SET),
				new SetMazeVariableEvent(INVITATION_FROM_LEONALS, "true"),
				new ActorsLeaveEvent());
		}
		else if (NpcSpeech.sentenceContainsKeywords(speech, "gamble", "gambling", "bet"))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(new NpcSpeechEvent("Well, I'm on duty right now..."));
			
			if (getParty().getGold() < 100)
			{
				result.add(new NpcSpeechEvent("In any event, it " +
					"appears you're practically out of cash."));
				result.add(new NpcSpeechEvent("Maybe some other time..."));
				return result;
			}
			else
			{
				// Basically the old dice game Hazard. Look it up.

				result.add(new NpcSpeechEvent("But why not pass a little time?"));
				result.add(new FlavourTextEvent("The Leonal grins a wide toothy grin " +
					"and produces two ivory dice from a small pouch.",
					MazeEvent.Delay.WAIT_ON_CLICK, true));
				result.add(new NpcSpeechEvent("Right then, here's how it works."));
				result.add(new NpcSpeechEvent("Two dice, six sides numbered one to " +
					"six. Fair dice, I do assure you."));
				result.add(new NpcSpeechEvent("You'll be the caster. We'll keep it " +
					"simple - stakes are 100 gold, loser pays winner, take it or leave it."));
				result.add(new NpcSpeechEvent("Caster calls a number from five " +
					"to nine - that's the main - then throws the dice.\n\n" +
					"If you roll the main you nick the stake. On a two or three " +
					"you've thrown out - you lose."));
				result.add(new NpcSpeechEvent("On an eleven or twelve, it depends " +
					"on the main. If the main was five or nine you've then thrown " +
					"out. With a main of six or eight, you've thrown out on an eleven " +
					"but nicked on a twelve. If the main's seven, you've nicked on an " +
					"eleven and thrown out on a twelve."));
				result.add(new NpcSpeechEvent("If the caster neither nicks nor " +
					"throws out the number thrown is the chance. On a chance the " +
					"caster rolls again - if he rolls the chance he wins. If he " +
					"rolls the main, he loses. Caster keeps rolling until either " +
					"the chance or the main come up."));
				result.add(new NpcSpeechEvent("Got it? Good! As I said, you're " +
					"the caster, stake is a hundred gold. Call and roll!"));
				result.add(new DisplayOptionsEvent(this.npc, "Call the main:",
					"Five", "Six", "Seven", "Eight", "Nine"));
			}

			return result;
		}
		else
		{
			return super.parsePartySpeech(pc, speech);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> optionChosen(String optionChosen)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (optionChosen == null)
		{
			result.add(new NpcSpeechEvent("Oh, ok. I guess the dice aren't " +
				"everyone's cup of mead. Perhaps another time."));
		}
		else
		{
			int main;

			if (optionChosen.equals("Five")) { main = 5; }
			else if (optionChosen.equals("Six")) { main = 6; }
			else if (optionChosen.equals("Seven")) { main = 7; }
			else if (optionChosen.equals("Eight")) { main = 8; }
			else if (optionChosen.equals("Nine")) { main = 9; }
			else throw new MazeException("Invalid option ["+optionChosen+"]");

			// implement Hazard
			result.add(new FlavourTextEvent("The leonal spreads a small blanket " +
				"on the ground and hands you the dice, then waits expectantly.",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
			result.add(new FlavourTextEvent("\n\nYou recall that your bet " +
				"was on "+main+", and without further ado roll the dice..."));

			int d1 = Dice.d6.roll();
			int d2 = Dice.d6.roll();

			int total = d1+d2;

			result.add(new FlavourTextEvent("The dice come up "+d1+" and "+d2+", " +
				"for a total of "+total, MazeEvent.Delay.WAIT_ON_CLICK, true));

			if (total == main)
			{
				nick(result);
			}
			else if (total == 2 || total == 3)
			{
				throwOut(result);
			}
			else if (total == 11 || total == 12)
			{
				if (main == 5 || main == 9)
				{
					throwOut(result);
				}
				else if (main == 6 || main == 8)
				{
					if (total == 12) nick(result);
					else if (total == 11) throwOut(result);
					else throw new MazeException("Here's throwing out for you "+total);
				}
				else if (main == 7)
				{
					if (total == 11) nick(result);
					else if (total == 12) throwOut(result);
					else throw new MazeException("Here's throwing out for you "+total);
				}
				else
				{
					throw new MazeException("Invalid main "+main);
				}
			}
			else
			{
				result.add(new NpcSpeechEvent("Oh dear, you're on the chance! " +
					"Roll again, chance is "+total+", main is "+main+"."));

				int roll = -1;

				while (roll != main && roll != total)
				{
					result.add(new FlavourTextEvent("You roll the dice...\n\n",
						MazeEvent.Delay.WAIT_ON_CLICK, true));

					d1 = Dice.d6.roll();
					d2 = Dice.d6.roll();

					roll = d1+d2;
					result.add(new FlavourTextEvent("The dice come up "+d1+" and "+d2+", " +
						"for a total of "+roll+"."));

					if (roll != main && roll != total)
					{
						result.add(new NpcSpeechEvent("No dice! " +
							"Roll again, chance is "+total+", main is "+main+"..."));
					}
				}

				if (roll == main)
				{
					throwOut(result);
				}
				else if (roll == total)
				{
					nick(result);
				}
				else
				{
					throw new MazeException("Invalid roll "+roll);
				}
			}
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void throwOut(List<MazeEvent> result)
	{
		result.add(new NpcSpeechEvent("Oh hard luck, you've thrown out!"));
		result.add(new NpcSpeechEvent("That's 100 gold..."));
		result.add(new FlavourTextEvent("You hand over the losing stake " +
			"to Stenelaus", MazeEvent.Delay.WAIT_ON_CLICK, true));
		getParty().incGold(-500);
	}

	/*-------------------------------------------------------------------------*/
	private void nick(List<MazeEvent> result)
	{
		result.add(new FlavourTextEvent("Stenelaus growls in amazement " +
			"as your numbers come up.", MazeEvent.Delay.WAIT_ON_CLICK, true));
		result.add(new NpcSpeechEvent("You nicked it! Oh, good rolling!"));
		result.add(new NpcSpeechEvent("Here's your stake."));
		result.add(new GrantGoldEvent(100));
	}
}