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

package mclachlan.maze.game.event;

import mclachlan.maze.game.GameTime;
import mclachlan.maze.game.MazeVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hermetic tests for forward-only turn-of-day advancement.
 */
public class AdvanceToTurnOfDayEventTest
{
	private static final String ONCE_VAR = "test.advance.noon.once";

	@AfterEach
	void reset()
	{
		GameTime.setTurnNr(0);
		MazeVariables.clearAll();
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void alreadyAtTargetLeavesTurnNrUnchanged()
	{
		GameTime.setTurnNr(450);
		assertEquals(150, GameTime.getTurnOfDay(GameTime.getTurnNr()));

		new AdvanceToTurnOfDayEvent(150, null).resolve();

		assertEquals(450, GameTime.getTurnNr());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void beforeTargetAdvancesSameDay()
	{
		GameTime.setTurnNr(100);

		new AdvanceToTurnOfDayEvent(150, null).resolve();

		assertEquals(150, GameTime.getTurnNr());
		assertEquals(150, GameTime.getTurnOfDay(GameTime.getTurnNr()));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void afterTargetRollsToNextDayNoon()
	{
		GameTime.setTurnNr(191);

		new AdvanceToTurnOfDayEvent(150, null).resolve();

		assertEquals(450, GameTime.getTurnNr());
		assertEquals(150, GameTime.getTurnOfDay(GameTime.getTurnNr()));
		assertTrue(GameTime.getTurnNr() > 191);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void onceFlagSkipsSubsequentRuns()
	{
		GameTime.setTurnNr(0);
		MazeVariables.set(ONCE_VAR, "true");

		new AdvanceToTurnOfDayEvent(150, ONCE_VAR).resolve();

		assertEquals(0, GameTime.getTurnNr());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void onceFlagSetAfterFirstAdvance()
	{
		GameTime.setTurnNr(0);

		new AdvanceToTurnOfDayEvent(150, ONCE_VAR).resolve();

		assertEquals(150, GameTime.getTurnNr());
		assertTrue(MazeVariables.getBoolean(ONCE_VAR));

		GameTime.setTurnNr(200);
		new AdvanceToTurnOfDayEvent(150, ONCE_VAR).resolve();
		assertEquals(200, GameTime.getTurnNr());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void neverDecreasesTurnNr()
	{
		for (long start = 0; start < 600; start += 17)
		{
			GameTime.setTurnNr(start);
			long before = GameTime.getTurnNr();
			new AdvanceToTurnOfDayEvent(150, null).resolve();
			assertTrue(GameTime.getTurnNr() >= before);
		}
	}
}
