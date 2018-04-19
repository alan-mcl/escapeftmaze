/*
 * Copyright (c) 2013 Alan McLachlan
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

package mclachlan.maze.util;

import mclachlan.maze.stat.Dice;

/**
 *
 */
public class AutomataNameGenerator
{
	static String[] pre =
		{
			"broken", "lost", "fixed", "new", "old", "worn", "rusty",
			"keeps", "grasps", "holds", "tools", "with",
			"red", "blue", "silver", "grey", "black", "rust",
			"hot", "dead", "cold", "expired", "stuck",
			"wired", "welded", "bolted", "screwed", "riveted",
		};

	static String[] post =
		{
			"cog", "lever", "wheel", "axel", "tread", "eye", "leg", "hand",
			"head", "screen", "voice", "joint", "sensor", "battery", "piston",
			"brain", "circuit", "plate", "pressure", "leg", "foot"
		};

	public static void main(String[] args) throws Exception
	{
		Dice preD = new Dice(1, pre.length, -1);
		Dice postD = new Dice(1, post.length, -1);

		int nr = Integer.parseInt(args[0]);

		for (int i=0; i<nr; i++)
		{
			String preS = pre[preD.roll("automata name gen 1")];
			String postS = post[postD.roll("automata name gen 2")];

			System.out.println(Capitaliser.capitaliseWord(preS+postS));
		}
	}
}
