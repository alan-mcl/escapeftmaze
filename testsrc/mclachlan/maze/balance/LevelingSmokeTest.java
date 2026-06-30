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

package mclachlan.maze.balance;

import mclachlan.maze.data.Database;
import mclachlan.maze.stat.Leveler;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.test.support.HeadlessMaze;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tier 4: a synthetic character is created and levelled via the production
 * {@link Leveler}, asserting HP and level advance as configured.
 */
public class LevelingSmokeTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void createsLevelOneCharacterFromSyntheticTemplates() throws Exception
	{
		Database db = TestData.buildEmptyDatabase();
		HeadlessMaze.boot(db);

		PlayerCharacter pc = TestData.newLevel1Pc("Hero");

		assertEquals(1, pc.getCurrentClassLevel());
		// class 20 hp * race 100% = 20
		assertEquals(20, pc.getHitPoints().getMaximum());
		assertEquals(10, pc.getActionPoints().getMaximum());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void levelUpAdvancesLevelAndHitPoints() throws Exception
	{
		Database db = TestData.buildEmptyDatabase();
		HeadlessMaze.boot(db);

		PlayerCharacter pc = TestData.newLevel1Pc("Hero");

		int hpBefore = pc.getHitPoints().getMaximum();
		int levelBefore = pc.getCurrentClassLevel();

		Leveler leveler = new Leveler();
		Leveler.LevelUpState state = new Leveler.LevelUpState(pc, 0);
		leveler.applyInitialChanges(pc, state);

		assertEquals(levelBefore + 1, pc.getCurrentClassLevel());
		// level-up hp dice is 1d6+2, so HP must strictly increase
		assertTrue(pc.getHitPoints().getMaximum() > hpBefore,
			"hp should increase on level up: " + hpBefore + " -> " +
				pc.getHitPoints().getMaximum());
		assertEquals(state.getHpInc(),
			pc.getHitPoints().getMaximum() - hpBefore);
	}
}
