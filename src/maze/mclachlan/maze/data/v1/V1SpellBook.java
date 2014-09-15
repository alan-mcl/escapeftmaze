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

package mclachlan.maze.data.v1;

import mclachlan.maze.data.Database;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellBook;

/**
 *
 */
public class V1SpellBook
{
	/*-------------------------------------------------------------------------*/
	static V1List<Spell> spellsList = new V1List<Spell>()
	{
		public String typeToString(Spell spell)
		{
			return spell.getName();
		}

		public Spell typeFromString(String s)
		{
			return Database.getInstance().getSpell(s);
		}
	};

	/*-------------------------------------------------------------------------*/
	public static String toString(SpellBook sb)
	{
		StringBuilder s = new StringBuilder();

		s.append(spellsList.toString(sb.getSpells()));

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static SpellBook fromString(String s)
	{
		return new SpellBook(spellsList.fromString(s));
	}
}
