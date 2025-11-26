package mclachlan.maze.util.tarot;

import java.util.*;

public class MazeTarot
{
	public static void main(String[] args)
	{
		// Load the Major Arcana
		List<Card> majorArcana = MajorArcanaFactory.createMajorArcana();

		// Create and shuffle deck
		Deck deck = new Deck(majorArcana);

		// Perform the draw
		ThreeCardDraw draw = new ThreeCardDraw();
		ThreeCardDraw.Result result = draw.draw(deck);

		// Print header
		System.out.println("=== Three Card Tarot Reading ===\n");

		// PAST
		printCardReading(
			"Past",
			result.pastCard,
			result.pastOrientation,
			Card.SpreadPosition.PAST
		);

		// PRESENT
		printCardReading(
			"Present",
			result.presentCard,
			result.presentOrientation,
			Card.SpreadPosition.PRESENT
		);

		// FUTURE
		printCardReading(
			"Future",
			result.futureCard,
			result.futureOrientation,
			Card.SpreadPosition.FUTURE
		);
	}

	private static void printCardReading(
		String label,
		Card card,
		Card.Orientation orientation,
		Card.SpreadPosition position
	)
	{
		System.out.println(label + ": " + card.getName()
			+ " (" + (orientation == Card.Orientation.UPRIGHT ? "Upright" : "Reversed") + ")");
		System.out.println("Meaning: " + card.getReading(position, orientation));

		List<String> attrs = orientation == Card.Orientation.UPRIGHT
			? card.getAttributes()
			: card.getReversedAttributes();

		if (!attrs.isEmpty())
		{
			System.out.println(attrs.get(new Random().nextInt(attrs.size())));
		}

		System.out.println();
	}
}
