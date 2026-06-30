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

package mclachlan.maze.stat.magic;

import mclachlan.maze.stat.Stats;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.StubActor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tier 3: {@link MagicSys} constants and point-cost computation.
 */
public class MagicSysTest extends MazeTestSupport
{
	private final MagicSys magicSys = new MagicSys();

	/*-------------------------------------------------------------------------*/
	@Test
	void maxCastingLevelConstant()
	{
		assertEquals(7, MagicSys.MAX_CASTING_LEVEL);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void nullValueCostsNothing()
	{
		assertEquals(0, magicSys.getPointCost(null, 3, new StubActor("a")));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void constantValueCostIgnoresCastingLevel()
	{
		ValueList vl = new ValueList(new Value(5, Value.SCALE.NONE));
		assertEquals(5, magicSys.getPointCost(vl, 7, new StubActor("a")));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void castingLevelScaledValueCost()
	{
		ValueList vl = new ValueList(
			new Value(5, Value.SCALE.SCALE_WITH_CASTING_LEVEL));
		assertEquals(15, magicSys.getPointCost(vl, 3, new StubActor("a")));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void pointCostCombinesConstantAndModifier()
	{
		StubActor a = new StubActor("a").withModifier(Stats.Modifier.POWER, 4);
		ValueList vl = new ValueList(
			new Value(2, Value.SCALE.NONE),
			new ModifierValue(Stats.Modifier.POWER));
		assertEquals(6, magicSys.getPointCost(vl, 1, a));
	}
}
