package mclachlan.maze.util.tarot;

import java.util.*;

/**
 *
 */
public class ThreeCardDraw
{

	private final Random rng;

	public ThreeCardDraw()
	{
		this(new Random());
	}

	public ThreeCardDraw(Random rng)
	{
		this.rng = rng == null ? new Random() : rng;
	}

	public class Result
	{
		public final Card pastCard;
		public final Card.Orientation pastOrientation;
		public final Card presentCard;
		public final Card.Orientation presentOrientation;
		public final Card futureCard;
		public final Card.Orientation futureOrientation;
		public final String interpretation;

		public Result(Card pastCard, Card.Orientation pastOrientation,
			Card presentCard, Card.Orientation presentOrientation,
			Card futureCard, Card.Orientation futureOrientation,
			String interpretation)
		{
			this.pastCard = pastCard;
			this.pastOrientation = pastOrientation;
			this.presentCard = presentCard;
			this.presentOrientation = presentOrientation;
			this.futureCard = futureCard;
			this.futureOrientation = futureOrientation;
			this.interpretation = interpretation;
		}

	}

	public static String describeCardAndOrientation(Card card, Card.Orientation orientation)
	{
		// return just the card name if upright, else name + " reversed"
		return (orientation == Card.Orientation.UPRIGHT) ? card.getName() : card.getName() + ", reversed";
	}

	/**
	 * Do a three-card draw. Deck must have at least 3 cards.
	 */
	public Result draw(Deck deck)
	{
		if (deck == null)
		{
			throw new IllegalArgumentException("Deck cannot be null.");
		}
		if (deck.size() < 3)
		{
			throw new IllegalArgumentException("Deck must contain at least 3 cards.");
		}

		// Shuffle and draw
		deck.shuffle();
		List<Card> drawn = deck.draw(3); // returns exactly 3 because we checked size

		Card past = drawn.get(0);
		Card present = drawn.get(1);
		Card future = drawn.get(2);

		Card.Orientation pastOri = rng.nextBoolean() ? Card.Orientation.UPRIGHT : Card.Orientation.REVERSED;
		Card.Orientation presentOri = rng.nextBoolean() ? Card.Orientation.UPRIGHT : Card.Orientation.REVERSED;
		Card.Orientation futureOri = rng.nextBoolean() ? Card.Orientation.UPRIGHT : Card.Orientation.REVERSED;

		StringBuilder sb = new StringBuilder();
		sb.append("Three-Card Tarot Reading\n");
		sb.append("-----------------------\n");

		appendCardReading(sb, "Past", past, Card.SpreadPosition.PAST, pastOri);
		appendCardReading(sb, "Present", present, Card.SpreadPosition.PRESENT, presentOri);
		appendCardReading(sb, "Future", future, Card.SpreadPosition.FUTURE, futureOri);

		// Optional combined synthesis sentence — basic synthesis from the three readings.
		sb.append("\nSynthesis:\n");
		sb.append(makeSynthesis(past, pastOri, present, presentOri, future, futureOri));

		return new Result(past, pastOri, present, presentOri, future, futureOri, sb.toString());
	}

	private void appendCardReading(StringBuilder sb, String label,
		Card card, Card.SpreadPosition pos, Card.Orientation ori)
	{
		sb.append(String.format("%s — %s (%s)\n", label, card.getName(), (ori == Card.Orientation.UPRIGHT ? "Upright" : "Reversed")));
		sb.append("  " + card.getReading(pos, ori) + "\n");
		List<String> attrs = (ori == Card.Orientation.UPRIGHT) ? card.getAttributes() : card.getReversedAttributes();
		if (!attrs.isEmpty())
		{
			sb.append("  Attributes: " + String.join(", ", attrs) + "\n");
		}
		sb.append("\n");
	}

	/**
	 * Very small, generic synthesis generator that combines keywords from the
	 * three cards. You can replace this with a more sophisticated narrative
	 * algorithm if you like.
	 */
	private String makeSynthesis(Card past, Card.Orientation pastOri,
		Card present, Card.Orientation presentOri,
		Card future, Card.Orientation futureOri)
	{
		List<String> seed = new ArrayList<>();
		seed.addAll(selectKeywords(past, pastOri));
		seed.addAll(selectKeywords(present, presentOri));
		seed.addAll(selectKeywords(future, futureOri));

		// Keep up to 6 keywords and produce a single-sentence synthesis
		List<String> top = seed.size() > 6 ? seed.subList(0, 6) : seed;
		if (top.isEmpty())
		{
			return "The cards suggest a quiet flow of events without strong defining themes.";
		}
		return "Themes to watch: " + String.join(", ", top) + ". These indicate a path from past lessons toward present choices and the future's potential.";
	}

	private List<String> selectKeywords(Card card,
		Card.Orientation ori)
	{
		List<String> source = (ori == Card.Orientation.UPRIGHT) ? card.getAttributes() : card.getReversedAttributes();
		if (source == null || source.isEmpty())
		{
			return Collections.emptyList();
		}
		// simple copy; could score or prioritize later
		return new ArrayList<>(source);
	}

	/**
	 * Convenience demo that prints a reading to stdout.
	 */
	public void drawAndPrint(Deck deck)
	{
		Result r = draw(deck);
		System.out.println(r.interpretation);
	}

	// If you want a quick main demo uncomment and supply sample Card objects to test.
	 /*
	 public static void main(String[] args) {
		  List<Card> cards = Arrays.asList(
				new Card("Example I", 1, "An example card", Arrays.asList("beginnings","initiative"), Arrays.asList("hesitation","delay"), Map.of(
					 Card.SpreadPosition.PAST, new Card.Reading("Past upright text","Past reversed text"),
					 Card.SpreadPosition.PRESENT, new Card.Reading("Present upright text","Present reversed text"),
					 Card.SpreadPosition.FUTURE, new Card.Reading("Future upright text","Future reversed text")
				)),
				// add two more sample cards...
		  );
		  Deck deck = new Deck(cards);
		  new ThreeCardDraw().drawAndPrint(deck);
	 }
	 */
}
