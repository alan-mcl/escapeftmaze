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

package mclachlan.maze.test;

import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.data.v1.V1StatModifier;

/**
 *
 */
public class TestStatModifier
{
	public static void main(String[] args)
	{
//		StatModifier sm = V1StatModifier.fromString("fe000000000000000,02020202020202");
		StatModifier sm = new StatModifier();
		sm.setModifier(Stats.Modifiers.BLACK_MAGIC_GEN, 2);
		sm.setModifier(Stats.Modifiers.BLUE_MAGIC_GEN, 2);
		sm.setModifier(Stats.Modifiers.RED_MAGIC_GEN, 2);
		sm.setModifier(Stats.Modifiers.WHITE_MAGIC_GEN, 2);
		sm.setModifier(Stats.Modifiers.GREEN_MAGIC_GEN, 2);
		sm.setModifier(Stats.Modifiers.PURPLE_MAGIC_GEN, 2);
		sm.setModifier(Stats.Modifiers.GOLD_MAGIC_GEN, 2);
		String s = V1StatModifier.toString(sm);
		
		System.out.println("s = [" + s + "]");
	}
}
