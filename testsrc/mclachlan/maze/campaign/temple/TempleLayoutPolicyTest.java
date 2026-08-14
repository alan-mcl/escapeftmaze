/*
 * Copyright (c) 2026 Alan McLachlan
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

package mclachlan.maze.campaign.temple;

import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.util.MazeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TempleLayoutPolicy} resolves per-depth layout generators.
 */
public class TempleLayoutPolicyTest extends MazeTestSupport
{
	@Test
	void depthsOneThroughFourUseNoise4j()
	{
		for (int depth = 1; depth <= 4; depth++)
		{
			assertTrue(TempleLayoutPolicy.forDepth(depth) instanceof Noise4jDungeonGen,
				"depth " + depth);
			assertEquals(TempleLayoutPolicy.NOISE4J,
				TempleLayoutPolicy.generatorIdForDepth(depth));
		}
	}

	@Test
	void unknownGeneratorIdThrows()
	{
		assertThrows(MazeException.class,
			() -> TempleLayoutPolicy.create("not-a-generator"));
	}
}
