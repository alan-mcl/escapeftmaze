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

package mclachlan.maze.stat;

import java.util.*;

/**
 * Picks exactly one character with the highest value of {@link #modifier}
 * among candidates (ties broken by seeded shuffle).
 */
public class HighestModifierSelection extends CharacterSelectionMethod
{
	private Stats.Modifier targetModifier;

	public HighestModifierSelection()
	{
	}

	public HighestModifierSelection(Stats.Modifier targetModifier)
	{
		this.targetModifier = targetModifier;
	}

	public Stats.Modifier getTargetModifier()
	{
		return targetModifier;
	}

	public void setTargetModifier(Stats.Modifier targetModifier)
	{
		this.targetModifier = targetModifier;
	}

	@Override
	public List<PlayerCharacter> select(List<PlayerCharacter> candidates)
	{
		if (candidates == null || candidates.isEmpty() || targetModifier == null)
		{
			return List.of();
		}

		List<PlayerCharacter> shuffled = new ArrayList<>(candidates);
		Collections.shuffle(shuffled);

		PlayerCharacter best = null;
		int bestValue = Integer.MIN_VALUE;
		for (PlayerCharacter pc : shuffled)
		{
			int value = pc.getBaseModifier(targetModifier);
			if (value > bestValue)
			{
				bestValue = value;
				best = pc;
			}
		}

		return best == null ? List.of() : List.of(best);
	}

	@Override
	public String describe()
	{
		return "Highest " + (targetModifier == null ? "?" : targetModifier.name());
	}
}
