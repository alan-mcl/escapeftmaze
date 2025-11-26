package mclachlan.maze.util.tarot;

import java.util.*;

/**
 *
 */
public class Deck
{

	private final List<Card> cards;
	private final Random rng;

	public Deck(List<Card> cards)
	{
		this(cards, new Random());
	}

	public Deck(List<Card> cards, Random rng)
	{
		if (cards == null || cards.isEmpty())
		{
			throw new IllegalArgumentException("Deck cannot be empty.");
		}
		this.cards = new ArrayList<>(cards);
		this.rng = rng == null ? new Random() : rng;
	}

	/**
	 * Returns the number of cards remaining in the deck.
	 */
	public int size()
	{
		return cards.size();
	}

	/**
	 * Shuffles the deck in place.
	 */
	public void shuffle()
	{
		Collections.shuffle(cards, rng);
	}

	/**
	 * Draws N cards from the top of the deck. Returns a list of exactly N cards
	 * unless the deck runs out.
	 */
	public List<Card> draw(int n)
	{
		if (n < 1)
		{
			throw new IllegalArgumentException("n must be >= 1");
		}

		int count = Math.min(n, cards.size());
		List<Card> drawn = new ArrayList<>(count);

		for (int i = 0; i < count; i++)
		{
			drawn.add(cards.remove(0));
		}

		return drawn;
	}

	/**
	 * Draw one card for convenience.
	 */
	public Card drawOne()
	{
		return draw(1).get(0);
	}

	/**
	 * Resets the deck to a new card list. Useful if you want to reuse a Deck
	 * object but start over.
	 */
	public void reset(List<Card> newCards)
	{
		cards.clear();
		cards.addAll(newCards);
	}
}
