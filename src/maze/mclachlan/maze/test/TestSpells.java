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

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.PlayerSpellBook;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class TestSpells
{
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		new Database(loader, saver, Maze.getStubCampaign());

		List result;
		for (MagicSys.SpellBook book : MagicSys.SpellBook.getAllBooks())
		{
			PlayerSpellBook psb = Database.getInstance().getPlayerSpellBook(book.getName());
			System.out.println("book.getName() = [" + book.getName() + "]");
			result = new ArrayList();
			for (String s : psb.getSpellNames())
			{
				result.add(s);
			}
			System.out.println("result = [" + result + "]");
		}

		
	}
}
