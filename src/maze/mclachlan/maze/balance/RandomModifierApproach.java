/*
 * Copyright (c) 2012 Alan McLachlan
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

package mclachlan.maze.balance;

import java.util.*;
import mclachlan.maze.stat.*;

/**
 * Represents a random approach to increasing modifiers. Takes as input
 * a set of modifiers, on level up a random selection from the set are
 * increased.
 */
public class RandomModifierApproach extends CharacterBuilder.ModifierApproach
{
	private List<Stats.Modifier> modifiers;

	/*-------------------------------------------------------------------------*/
	public RandomModifierApproach(Stats.Modifier... modifiers)
	{
		this.modifiers = Arrays.asList(modifiers);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void incModifiers(PlayerCharacter pc, StatModifier sm, int points)
	{
		Dice d = new Dice(1,modifiers.size(),-1);

		int i = d.roll();
		int assignedToCurrent = 0;
		int maxAssignable = GameSys.getInstance().getMaxAssignableToAModifierOnLevelUp();
		while (points > 0)
		{
			Stats.Modifier modifier = modifiers.get(i);
			int current = pc.getModifier(modifier) + assignedToCurrent;
			int cost = GameSys.getInstance().getModifierIncreaseCost(modifier, pc, current);

			if (cost > points)
			{
				// can't afford to raise this modifier
				i = d.roll();
				assignedToCurrent = 0;
			}
			else
			{
				incModifier(sm, modifier);
				points -= cost;
				assignedToCurrent++;
				if (assignedToCurrent >= maxAssignable)
				{
					i = d.roll();
					assignedToCurrent = 0;
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void incModifier(StatModifier sm, Stats.Modifier modifier)
	{
		int current = sm.getModifier(modifier);
		sm.setModifier(modifier, current + 1);
	}
}
