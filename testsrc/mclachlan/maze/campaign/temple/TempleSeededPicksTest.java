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

import java.util.*;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.test.support.MazeTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Persist-once seeded picks for temple floor dressing.
 */
public class TempleSeededPicksTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void pickAndRememberPersistsAcrossClearAndRestore()
	{
		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.FLOOR_SEED_PREFIX + "1", "999");

		List<String> source = List.of("a", "b", "c", "d", "e");
		List<String> first = TempleSeededPicks.pickAndRemember(
			1, "test", source, 3, s -> s, s -> s);
		assertEquals(3, first.size());
		assertNotNull(MazeVariables.get(TempleSeededPicks.pickVar(1, "test")));

		List<String> again = TempleSeededPicks.pickAndRemember(
			1, "test", source, 3, s -> s, s -> s);
		assertEquals(first, again);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void pickDoesNotMutateSourceList()
	{
		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.FLOOR_SEED_PREFIX + "2", "1234");

		List<String> source = new ArrayList<>(List.of("x", "y", "z"));
		List<String> original = List.copyOf(source);
		TempleSeededPicks.pickAndRemember(2, "shuffle", source, 2, s -> s, s -> s);
		assertEquals(original, source);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void rngIsDeterministicForPurpose()
	{
		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.FLOOR_SEED_PREFIX + "1", "555");

		Random a = TempleSeededPicks.rng(1, "chest.0");
		Random b = TempleSeededPicks.rng(1, "chest.0");
		Random c = TempleSeededPicks.rng(1, "chest.1");

		assertEquals(a.nextInt(), b.nextInt());
		assertNotEquals(a.nextInt(), c.nextInt());
	}
}
