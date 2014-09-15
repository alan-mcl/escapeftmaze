/*
 * Copyright (c) 2014 Alan McLachlan
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
import mclachlan.maze.stat.SpellLikeAbility;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.Value;

/**
 *
 */
public class V1SpellLikeAbility
{
	public static final String SEP = "~";

	/*-------------------------------------------------------------------------*/
	public static String toString(SpellLikeAbility sla)
	{
		if (sla == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		s.append(sla.getSpell().getName());
		s.append(SEP);
		s.append(V1Value.toString(sla.getCastingLevel(), "&", "`"));

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static SpellLikeAbility fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		String[] strs = s.split(SEP);
		int index = 0;
		String spellName = strs[index++];
		Value castingLevel = V1Value.fromString(strs[index++], "&", "`");

		Spell spell = Database.getInstance().getSpell(spellName);

		return new SpellLikeAbility(spell, castingLevel);
	}
}
