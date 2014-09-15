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

import mclachlan.maze.stat.ExperienceTableFormulaic;
import mclachlan.maze.stat.ExperienceTable;
import mclachlan.maze.stat.ExperienceTableArray;

/**
 *
 */
public class TestExperienceTable
{
	/*-------------------------------------------------------------------------*/
	public static void main(String[] args)
	{
		System.out.println("Formulaic...");
		ExperienceTable test = new ExperienceTableFormulaic("test", 1000, 1000, 2, 20, 3);

		print(test);

		System.out.println();
		System.out.println("Array...");
		test = new ExperienceTableArray("test",
			new int[]
			{
				0,
				0,
				1000,
				2000,
				4000,
				8000,
				16000,
				32000,
				64000,
				128000,
				256000,
				512000,
				768000,
				1168000,
				1568000,
				1968000,
				2368000,
				2968000
			},
			60000);

		print(test);
	}

	/*-------------------------------------------------------------------------*/
	private static void print(ExperienceTable test)
	{
		for (int i=1; i<30; i++)
		{
			int nextLevelUp = test.getNextLevelUp(i);
			System.out.println("to get to "+(i+1)+": "+nextLevelUp);
		}
	}
}
