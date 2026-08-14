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

package mclachlan.maze.map.script;

import java.util.List;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.test.support.MazeTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hermetic checks for integer maze-variable increments.
 */
public class IncrementMazeVariableEventTest extends MazeTestSupport
{
	@Test
	void incrementsMissingVariableFromZero()
	{
		MazeVariables.clearAll();
		List<?> events = new IncrementMazeVariableEvent("temple.depth", 1).resolve();
		assertNull(events);
		assertEquals("1", MazeVariables.get("temple.depth"));
	}

	@Test
	void incrementsExistingIntegerValue()
	{
		MazeVariables.clearAll();
		MazeVariables.set("temple.depth", "2");
		new IncrementMazeVariableEvent("temple.depth", -1).resolve();
		assertEquals("1", MazeVariables.get("temple.depth"));
	}
}
