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

package mclachlan.maze.util;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.FoeTemplate;
import mclachlan.maze.stat.FoeType;

/**
 *
 */
public class QueryFoes
{
	private static V2Saver saver;

	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		V2Loader loader = new V2Loader();
		saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		FoeType vermin = db.getFoeTypes().get("Vermin");

		Map<String, FoeTemplate> foeTemplates = db.getFoeTemplates();

		for (FoeTemplate ft : foeTemplates.values())
		{
			if (ft.getTypes() != null && ft.getTypes().contains(vermin))
			{
				System.out.println(ft.getName()+" ("+ V1Dice.toString(ft.getLevelRange())+")");
			}
		}
	}
}