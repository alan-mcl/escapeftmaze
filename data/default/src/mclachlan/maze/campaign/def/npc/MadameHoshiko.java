
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.DisplayOptionsEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.map.script.GrantGoldEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeech;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import mclachlan.maze.util.tarot.*;

import static mclachlan.maze.map.script.FlavourTextEvent.Alignment.TOP;

/**
 * Fortune teller in Ichiba City
 */
public class MadameHoshiko extends NpcScript
{
	private static final String HOSHIKO_HAS_TOLD_FORTUNE = "hoshiko.has.told.fortune";
	private static final String YES = "Yes, tell my fortune.";
	private static final String NO = "No, not now.";
	public static final int FORTUNE_PRICE = 50;

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The darkened room smells heavily of incense and perfume.", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("Carpets cover the floors and a single brazier burns fitfully near the far wall.", MazeEvent.Delay.WAIT_ON_CLICK, true),
			new FlavourTextEvent("Seated on a cushion behind a low table, a wizened old " +
				"woman regards you impassively.", MazeEvent.Delay.WAIT_ON_CLICK, true)
		);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("Welcome, travelers. I saw you long before you reached my door.", npc),
			new NpcSpeechEvent("The Maze whispered of your coming.", npc),
			new NpcSpeechEvent("You carry questions like stones in your pockets.", npc),
			new NpcSpeechEvent("Ask and perchance the answers can be found.", npc));
	}



	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> parsePartySpeech(PlayerCharacter pc, String speech)
	{
		if (NpcSpeech.sentenceContainsKeywords(speech, "fortune", "future", "reading", "tarot"))
		{
			if (MazeVariables.get(HOSHIKO_HAS_TOLD_FORTUNE) == null)
			{
				if (Maze.getInstance().getParty().getGold() < FORTUNE_PRICE)
				{
					return getList(
						new NpcSpeechEvent("Fortunes are not free, traveler. Return when you have enough gold.", npc));
				}
				else
				{
					return getList(
						new NpcSpeechEvent("There is a price for my services. Will you pay?", npc),
						new DisplayOptionsEvent(this, true,
							"Pay Madame Hoshiko 50gp?", YES, NO));
				}
			}
			else
			{
				return getList(new NpcSpeechEvent("I have already shown you one future. Do not have greed for a second.", npc));
			}
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
		if (YES.equals(optionChosen))
		{
			// Load the Major Arcana
			List<Card> majorArcana = MajorArcanaFactory.createMajorArcana();

			// Create and shuffle deck
			Deck deck = new Deck(majorArcana);

			// Perform the draw
			ThreeCardDraw draw = new ThreeCardDraw();
			ThreeCardDraw.Result tarotDraw = draw.draw(deck);

			String pastCardName = ThreeCardDraw.describeCardAndOrientation(tarotDraw.pastCard, tarotDraw.pastOrientation);
			String pastCardDesc = tarotDraw.pastCard.getDescription();
			String pastCardReading = tarotDraw.pastCard.getReading(Card.SpreadPosition.PAST, tarotDraw.pastOrientation);
			String pastCardFlavour = tarotDraw.pastCard.getRandomAttributeText(tarotDraw.pastOrientation);

			String presentCardName = ThreeCardDraw.describeCardAndOrientation(tarotDraw.presentCard, tarotDraw.presentOrientation);
			String presentCardDesc = tarotDraw.presentCard.getDescription();
			String presentCardReading = tarotDraw.presentCard.getReading(Card.SpreadPosition.PRESENT, tarotDraw.presentOrientation);
			String presentCardFlavour = tarotDraw.presentCard.getRandomAttributeText(tarotDraw.presentOrientation);

			String futureCardName = ThreeCardDraw.describeCardAndOrientation(tarotDraw.futureCard, tarotDraw.futureOrientation);
			String futureCardDesc = tarotDraw.futureCard.getDescription();
			String futureCardReading = tarotDraw.futureCard.getReading(Card.SpreadPosition.FUTURE, tarotDraw.futureOrientation);
			String futureCardFlavour = tarotDraw.futureCard.getRandomAttributeText(tarotDraw.futureOrientation);

			List<MazeEvent> result = new ArrayList<>();
			result.add(new GrantGoldEvent(-FORTUNE_PRICE));
			result.add(new NpcSpeechEvent("Good... good. It is good.", npc));
			result.add(new FlavourTextEvent("She produces a deck of worn tarot cards, shuffles them expertly, and places them on the table before her.", MazeEvent.Delay.WAIT_ON_CLICK, true, TOP));
			result.add(new NpcSpeechEvent("Place your hands on the table. Do not fidget. The cards can smell doubt.", npc));
			result.add(new FlavourTextEvent("She gestures over the deck with slow, deliberate motions.", MazeEvent.Delay.WAIT_ON_CLICK, true, TOP));
			result.add(new NpcSpeechEvent("Three questions hide inside every soul: what shaped you, what grips you, and what waits ahead.", npc));
			result.add(new NpcSpeechEvent("These cards know all three, though they rarely agree with each other.", npc));
			result.add(new NpcSpeechEvent("Let us begin.", npc));

			result.add(new NpcSpeechEvent("First, what lies behind. Just as the future is rooted in the present, so is the present in the past", npc));
			result.add(new FlavourTextEvent("She draws the first card and places it on the table face up.", MazeEvent.Delay.WAIT_ON_CLICK, true, TOP));

			result.add(new NpcSpeechEvent("For the past, the shadow that clings at your heels... we have drawn "+pastCardName+". "+pastCardDesc,npc));
			result.addAll(getSpeechEventsForCardReading(pastCardReading, npc));
			result.addAll(getSpeechEventsForCardReading(pastCardFlavour, npc));

			result.add(new NpcSpeechEvent("The past never stays politely behind us. It drags, it whispers, it bargains.", npc));
			result.add(new NpcSpeechEvent("And you... you have bargained more than most.", npc));

			result.add(new FlavourTextEvent("Her fingers hover over the deck again.", MazeEvent.Delay.WAIT_ON_CLICK, true, TOP));
			result.add(new NpcSpeechEvent("Now, the present. This is the card that dislikes being misunderstood, yet often is.", npc));
			result.add(new FlavourTextEvent("She draws the second card and places it before you.", MazeEvent.Delay.WAIT_ON_CLICK, true, TOP));

			result.add(new NpcSpeechEvent("Before you now stands "+presentCardName+". "+presentCardDesc,npc));
			result.addAll(getSpeechEventsForCardReading(presentCardReading, npc));
			result.add(new NpcSpeechEvent("Hear its warning.",npc));
			result.addAll(getSpeechEventsForCardReading(presentCardFlavour, npc));

			result.add(new NpcSpeechEvent("The present is a knife-edge. Lean too far to either side and it will gladly let you fall.", npc));
			result.add(new FlavourTextEvent("Without breaking eye contact she draws the final card, face down.", MazeEvent.Delay.WAIT_ON_CLICK, true, TOP));
			result.add(new NpcSpeechEvent("And for the future... the trickiest of the three.", npc));
			result.add(new NpcSpeechEvent("Futures behave like shy animals. They are fleeting when stared at too hard.", npc));
			result.add(new FlavourTextEvent("With a practised flip she reveals the last card.", MazeEvent.Delay.WAIT_ON_CLICK, true, TOP));

			result.add(new NpcSpeechEvent("Ah.", npc));
			result.add(new NpcSpeechEvent("It is "+futureCardName+". "+futureCardDesc, npc));
			result.addAll(getSpeechEventsForCardReading(futureCardReading, npc));
			result.addAll(getSpeechEventsForCardReading(futureCardFlavour, npc));
			result.add(new NpcSpeechEvent("Do not cling to it. Futures hate being held. They prefer to arrive uninvited.", npc));

			result.add(new FlavourTextEvent("She gathers the cards back into the deck with a sweep of her arm.", MazeEvent.Delay.WAIT_ON_CLICK, true, TOP));

			result.add(new NpcSpeechEvent("There. The tale is told.", npc));
			result.add(new NpcSpeechEvent("You may have heard it, or you may not... the choice is, as always, yours alone.", npc));
			result.add(new SetMazeVariableEvent(HOSHIKO_HAS_TOLD_FORTUNE, "true"));

			return result;
		}
		else
		{
			return getList(
				new NpcSpeechEvent("As you wish. Return when you are ready.", npc));
		}
	}

	private List<MazeEvent> getSpeechEventsForCardReading(
		String response, Foe npc)
	{
		List<MazeEvent> result = new ArrayList<>();

		String[] rsps = response.split("\n");

		for (String rsp : rsps)
		{
			result.add(new NpcSpeechEvent(npc, rsp.trim(), MazeEvent.Delay.WAIT_ON_CLICK));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> successfulTheft(PlayerCharacter pc, Item item)
	{
		MazeVariables.set(SirKay.SIR_KAY_PARTY_DETECTED_STEALING, "true");
		return super.successfulTheft(pc, item);
	}
}
