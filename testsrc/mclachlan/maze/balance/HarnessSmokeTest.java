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
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.test.support.HeadlessMaze;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tier 4: boots the {@link HeadlessMaze} harness on an in-memory database and
 * exercises the live engine singletons against synthetic actors.
 */
public class HarnessSmokeTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void headlessMazeBootsWithLiveSystems() throws Exception
	{
		Database db = TestData.buildEmptyDatabase();
		Maze maze = HeadlessMaze.boot(db);

		assertSame(maze, Maze.getInstance());
		assertNotNull(maze.getGameSys());
		assertNotNull(maze.getMagicSys());
		assertNotNull(maze.getUserConfig());
		assertEquals(0, maze.getUserConfig().getCombatDelay());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void syntheticFoeRollsVitalsAndIsUsableByLiveGameSys() throws Exception
	{
		Database db = TestData.buildEmptyDatabase();
		HeadlessMaze.boot(db);

		Foe foe = TestData.referenceFoe(5);

		assertTrue(foe.getHitPoints().getMaximum() > 0, "foe should have hp");
		assertTrue(foe.getLevel() > 0, "foe should have a level");

		// the live GameSys singleton (not a hand-built one) drives this
		int initiative = GameSys.getInstance().calcInitiative(foe);
		int skill = foe.getModifier(mclachlan.maze.stat.Stats.Modifier.SKILL);
		int init = foe.getModifier(mclachlan.maze.stat.Stats.Modifier.INITIATIVE);
		assertTrue(initiative >= skill + init + 1 && initiative <= skill + init + 6,
			"initiative out of range: " + initiative);
	}
}
