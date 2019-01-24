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
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.AbstractActor;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.ValueList;

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

		s.append(c.getTemplate() == null ? "" : c.getTemplate().getName());
		s.append(SEP);
		s.append(c.getDuration());
		s.append(SEP);
		s.append(c.getStrength());
		s.append(SEP);
		s.append(c.getCastingLevel());
		s.append(SEP);
		s.append(V1Value.toString(c.getHitPointDamage()));
		s.append(SEP);
		s.append(V1Value.toString(c.getActionPointDamage()));
		s.append(SEP);
		s.append(V1Value.toString(c.getMagicPointDamage()));
		s.append(SEP);
		s.append(V1Value.toString(c.getStaminaDamage()));
		s.append(SEP);
		s.append(c.getType().name());
		s.append(SEP);
		s.append(c.getSubtype().name());
		s.append(SEP);
		if (c.getSource() != null && c.getSource().getName() != null)
		{
			// todo: better linking up of sources
			s.append(c.getSource().getName());
		}
		s.append(SEP);
		s.append(c.isIdentified());
		s.append(SEP);
		s.append(c.isStrengthIdentified());
		s.append(SEP);
		s.append(c.getCreatedTurn());
		s.append(SEP);
		s.append(c.isHostile());

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static Condition fromString(String s)
	{
		String[] strs = s.split(SEP, -1);

		int i=0;
		String templateStr = strs[i++];
		ConditionTemplate template;
		if ("".equals(templateStr))
		{
			template = null;
		}
		else
		{
			template = Database.getInstance().getConditionTemplate(templateStr);
		}
		int duration = Integer.parseInt(strs[i++]);
		int strength = Integer.parseInt(strs[i++]);
		int castingLevel = Integer.parseInt(strs[i++]);
		ValueList hpDamage = V1Value.fromString(strs[i++]);
		ValueList apDamage = V1Value.fromString(strs[i++]);
		ValueList mpDamage = V1Value.fromString(strs[i++]);
		ValueList staminaDamage = V1Value.fromString(strs[i++]);
		MagicSys.SpellEffectType type = MagicSys.SpellEffectType.valueOf(strs[i++]);
		MagicSys.SpellEffectSubType subtype = MagicSys.SpellEffectSubType.valueOf(strs[i++]);
		UnifiedActor source;
		String sourceName = strs[i++];
		source = getPlayerCharacter(sourceName);
		if (source == null)
		{
			// fake it
			source = new AbstractActor(){};
		}
		boolean isIdentified = Boolean.valueOf(strs[i++]);
		boolean isStrengthIdentified = Boolean.valueOf(strs[i++]);
		long createdTurn = Long.valueOf(strs[i++]);
		boolean hostile = Boolean.valueOf(strs[i++]);

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
				hpDamage,
				apDamage,
				mpDamage,
				staminaDamage,
				type,
				subtype,
				source,
				isIdentified,
				isStrengthIdentified,
				createdTurn,
				hostile);
		}
	}

	public static UnifiedActor getPlayerCharacter(String sourceName)
	{
		Maze instance = Maze.getInstance();
		if (instance != null)
		{
			return instance.getPlayerCharacter(sourceName);
		}
		else
		{
			return null;
		}
	}
}
