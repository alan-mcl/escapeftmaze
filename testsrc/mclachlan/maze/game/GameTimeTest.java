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

package mclachlan.maze.game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hermetic tests for global time-of-day helpers.
 */
public class GameTimeTest
{
	@AfterEach
	void resetTurn()
	{
		GameTime.setTurnNr(0);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void turnOfDayAndDayNrWrapAtTurnsPerDay()
	{
		assertEquals(0, GameTime.getTurnOfDay(0));
		assertEquals(1, GameTime.getDayNr(0));

		assertEquals(0, GameTime.getTurnOfDay(GameTime.TURNS_PER_DAY));
		assertEquals(2, GameTime.getDayNr(GameTime.TURNS_PER_DAY));

		assertEquals(149, GameTime.getTurnOfDay(449));
		assertEquals(2, GameTime.getDayNr(449));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void dayFractionSpansZeroToOne()
	{
		assertEquals(0.0, GameTime.getDayFraction(0), 0.0001);
		assertEquals(0.5, GameTime.getDayFraction(150), 0.0001);
		assertTrue(GameTime.getDayFraction(299) < 1.0);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void timeOfDayBandsAtEdges()
	{
		assertEquals(TimeOfDay.DAWN, TimeOfDay.fromTurnOfDay(0));
		assertEquals(TimeOfDay.DAWN, TimeOfDay.fromTurnOfDay(TimeOfDay.DAWN_END));
		assertEquals(TimeOfDay.DAY, TimeOfDay.fromTurnOfDay(TimeOfDay.DAWN_END + 1));
		assertEquals(TimeOfDay.DAY, TimeOfDay.fromTurnOfDay(TimeOfDay.DAY_END));
		assertEquals(TimeOfDay.DUSK, TimeOfDay.fromTurnOfDay(TimeOfDay.DAY_END + 1));
		assertEquals(TimeOfDay.DUSK, TimeOfDay.fromTurnOfDay(TimeOfDay.DUSK_END));
		assertEquals(TimeOfDay.NIGHT, TimeOfDay.fromTurnOfDay(TimeOfDay.DUSK_END + 1));
		assertEquals(TimeOfDay.NIGHT, TimeOfDay.fromTurnOfDay(GameTime.TURNS_PER_DAY - 1));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void nightAmountPeaksAtMidnightAndIsZeroAtNoon()
	{
		assertEquals(1.0, GameTime.getNightAmount(0), 0.0001);
		assertEquals(0.0, GameTime.getNightAmount(150), 0.0001);
		assertEquals(1.0, GameTime.getNightAmount(300), 0.0001);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void gameDateExposesTurnOfDayAndPhase()
	{
		GameTime.GameDate date = GameTime.getGameDate(187);
		assertEquals(1, date.getDayNr());
		assertEquals(187, date.getTurnOfDay());
		assertEquals(TimeOfDay.DUSK, date.getTimeOfDay());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void computeForwardAdvanceToTurnOfDayNeverRewinds()
	{
		assertEquals(150, GameTime.computeForwardAdvanceToTurnOfDay(0, 150));
		assertEquals(150, GameTime.computeForwardAdvanceToTurnOfDay(100, 150));
		assertEquals(150, GameTime.computeForwardAdvanceToTurnOfDay(150, 150));
		assertEquals(259, GameTime.computeForwardAdvanceToTurnOfDay(191, 150) - 191);
		assertEquals(450, GameTime.computeForwardAdvanceToTurnOfDay(191, 150));
		assertTrue(GameTime.computeForwardAdvanceToTurnOfDay(191, 150) >= 191);
	}
}
