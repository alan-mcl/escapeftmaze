/*
 * Copyright (c) 2011 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.maze.util.tarot;

import java.util.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic tests for the tarot {@link Deck}; shuffles use an injected seeded
 * {@link Random} so they are deterministic.
 */
public class DeckTest
{
	private Card card(String name, int ordinal)
	{
		return new Card(name, ordinal, "desc", null, null, null);
	}

	private List<Card> cards(int n)
	{
		List<Card> list = new ArrayList<>();
		for (int i = 0; i < n; i++)
		{
			list.add(card("card" + i, i));
		}
		return list;
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void emptyDeckRejected()
	{
		assertThrows(IllegalArgumentException.class, () -> new Deck(new ArrayList<>()));
		assertThrows(IllegalArgumentException.class, () -> new Deck(null));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void drawReducesSize()
	{
		Deck deck = new Deck(cards(10));
		assertEquals(10, deck.size());
		List<Card> drawn = deck.draw(3);
		assertEquals(3, drawn.size());
		assertEquals(7, deck.size());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void drawMoreThanAvailableReturnsAll()
	{
		Deck deck = new Deck(cards(3));
		assertEquals(3, deck.draw(10).size());
		assertEquals(0, deck.size());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void drawZeroRejected()
	{
		assertThrows(IllegalArgumentException.class, () -> new Deck(cards(3)).draw(0));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void drawOne()
	{
		Deck deck = new Deck(cards(2));
		assertNotNull(deck.drawOne());
		assertEquals(1, deck.size());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void seededShuffleIsDeterministic()
	{
		Deck a = new Deck(cards(20), new Random(7));
		Deck b = new Deck(cards(20), new Random(7));
		a.shuffle();
		b.shuffle();

		List<String> aNames = new ArrayList<>();
		List<String> bNames = new ArrayList<>();
		while (a.size() > 0)
		{
			aNames.add(a.drawOne().getName());
		}
		while (b.size() > 0)
		{
			bNames.add(b.drawOne().getName());
		}
		assertEquals(aNames, bNames);
	}
}
