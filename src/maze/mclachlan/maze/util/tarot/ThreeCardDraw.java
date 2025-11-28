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

		public Result(Card pastCard, Card.Orientation pastOrientation,
			Card presentCard, Card.Orientation presentOrientation,
			Card futureCard, Card.Orientation futureOrientation)
		{
			this.pastCard = pastCard;
			this.pastOrientation = pastOrientation;
			this.presentCard = presentCard;
			this.presentOrientation = presentOrientation;
			this.futureCard = futureCard;
			this.futureOrientation = futureOrientation;
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

		return new Result(past, pastOri, present, presentOri, future, futureOri);
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
}
