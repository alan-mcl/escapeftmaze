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

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class UpdateSpells
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		Loader loader = new V2Loader();
		Saver saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		int count = 0;

		Map<String,Spell> spells = db.getSpells();

		for (String s : spells.keySet())
		{
			Spell spell = spells.get(s);

//			if (spell.getDisplayName().length() == 0 || Character.isLowerCase(spell.getDisplayName().charAt(0)))
//			{
//				spell.setDisplayName(spell.getName());
//			}

			count++;
		}
		
		saver.saveSpells(spells);
		System.out.println("count = [" + count + "]");
	}
}
