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

package mclachlan.maze.stat.condition;

import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class BloodthirstyEffect extends ConditionEffect
{
	private static StatModifier bloodthirsty1, bloodthirsty2;

	/*-------------------------------------------------------------------------*/
	static
	{
		bloodthirsty1 = new StatModifier();
		bloodthirsty1.setModifier(Stats.Modifier.BRAWN, 2);
		bloodthirsty1.setModifier(Stats.Modifier.ATTACK, 10);
		bloodthirsty1.setModifier(Stats.Modifier.TO_PENETRATE, 10);
		bloodthirsty1.setModifier(Stats.Modifier.RESIST_MENTAL, 10);

		bloodthirsty2 = new StatModifier();
		bloodthirsty1.setModifier(Stats.Modifier.BRAWN, 4);
		bloodthirsty1.setModifier(Stats.Modifier.ATTACK, 20);
		bloodthirsty1.setModifier(Stats.Modifier.TO_PENETRATE, 20);
		bloodthirsty1.setModifier(Stats.Modifier.RESIST_MENTAL, 20);
		bloodthirsty1.setModifier(Stats.Modifier.RESIST_ENERGY, 20);
		bloodthirsty1.setModifier(Stats.Modifier.MELEE_CRITICALS, 10);
	}

	/*-------------------------------------------------------------------------*/
	public BloodthirstyEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public BloodthirstyEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public int getModifier(Stats.Modifier modifier, Condition condition,
		ConditionBearer bearer)
	{
		if (!(bearer instanceof UnifiedActor))
		{
			return 0;
		}

		if (((UnifiedActor)bearer).getModifier(Stats.Modifier.BLOODTHIRSTY) > 1)
		{
			return bloodthirsty2.getModifier(modifier);
		}
		else if (((UnifiedActor)bearer).getModifier(Stats.Modifier.BLOODTHIRSTY) > 0)
		{
			return bloodthirsty1.getModifier(modifier);
		}
		else
		{
			return 0;
		}
	}
}
