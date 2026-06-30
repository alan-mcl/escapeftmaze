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

package mclachlan.maze.test.support;

import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Dice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

/**
 * Base class for the hermetic JUnit suite.
 * <p>
 * It seeds {@link Dice} before every test so that any probabilistic logic is
 * reproducible, and clears the {@link Maze} and {@link Database} singletons
 * afterwards so global state never leaks between tests. Subclasses that need a
 * populated {@link Database} should build it through {@link TestData} (see the
 * serialisation, rules and harness tests).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MazeTestSupport
{
	/** A fixed seed; combined with {@link Dice#setRandomSeed} this makes the
	 *  whole suite deterministic. */
	public static final long DEFAULT_SEED = 0xC0FFEEL;

	/*-------------------------------------------------------------------------*/
	@BeforeEach
	public void seedDice()
	{
		Dice.setRandomSeed(DEFAULT_SEED);
		// rules code calls Maze.getPerfLog().enter(...); install a quiet one so
		// hermetic tests need not bootstrap a full Maze.
		Maze.setPerfLog(new QuietPerfLog());
	}

	/*-------------------------------------------------------------------------*/
	@AfterEach
	public void resetSingletons()
	{
		Maze.destroy();
		Maze.setPerfLog(null);
		Database.resetInstanceForTesting();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Reseed the shared RNG to a caller-chosen value for tests that want to pin
	 * a specific roll sequence.
	 */
	protected void seed(long seed)
	{
		Dice.setRandomSeed(seed);
	}
}
