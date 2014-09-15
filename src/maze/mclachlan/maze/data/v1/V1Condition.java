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
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.AbstractActor;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1Condition
{
	public static final String SEP = ";";

	/*-------------------------------------------------------------------------*/
	public static String toString(Condition c)
	{
		StringBuilder s = new StringBuilder();

		s.append(c.getTemplate().getName());
		s.append(SEP);
		s.append(c.getDuration());
		s.append(SEP);
		s.append(c.getStrength());
		s.append(SEP);
		s.append(c.getCastingLevel());
		s.append(SEP);
		s.append(V1Value.toString(c.getHitPointDamage()));
		s.append(SEP);
		s.append(c.getType());
		s.append(SEP);
		s.append(c.getSubtype().name());
		s.append(SEP);
		if (c.getSource() instanceof PlayerCharacter)
		{
			// the only case in which we're interested in linking up the source properly
			s.append(c.getSource().getName());
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static Condition fromString(String s)
	{
		String[] strs = s.split(SEP, -1);

		ConditionTemplate template = Database.getInstance().getConditionTemplate(strs[0]);
		int duration = Integer.parseInt(strs[1]);
		int strength = Integer.parseInt(strs[2]);
		int castingLevel = Integer.parseInt(strs[3]);
		Value damage = V1Value.fromString(strs[4]);
		int type = Integer.parseInt(strs[5]);
		MagicSys.SpellEffectSubType subtype = MagicSys.SpellEffectSubType.valueOf(strs[6]);
		UnifiedActor source;
		String sourceName = strs[7];
		if (sourceName.equals(""))
		{
			// fake it
			source = new AbstractActor(){};
		}
		else
		{
			// assume a player character
			source = Maze.getInstance().getPlayerCharacter(sourceName);
			if (source == null)
			{
				throw new MazeException("Invalid source ["+ sourceName +"]");
			}
		}

		if (template.getImpl() != null)
		{
			// this is a custom condition, restore it differently
			return template.create(source, null, castingLevel, type, subtype);
		}
		else
		{
			return new Condition(
				template,
				duration,
				strength,
				castingLevel,
				damage,
				type,
				subtype,
				source);
		}
	}
}
