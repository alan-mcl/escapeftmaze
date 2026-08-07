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
 * Adds every candidate whose {@link #modifier} satisfies {@link #op}
 * {@link #value}.
 */
public class ModifierComparisonSelection extends CharacterSelectionMethod
{
	private Stats.Modifier targetModifier;
	private ComparisonOperator op = ComparisonOperator.GT;
	private int value;

	public ModifierComparisonSelection()
	{
	}

	public ModifierComparisonSelection(
		Stats.Modifier targetModifier,
		ComparisonOperator op,
		int value)
	{
		this.targetModifier = targetModifier;
		this.op = op;
		this.value = value;
	}

	public Stats.Modifier getTargetModifier()
	{
		return targetModifier;
	}

	public void setTargetModifier(Stats.Modifier targetModifier)
	{
		this.targetModifier = targetModifier;
	}

	public ComparisonOperator getOp()
	{
		return op;
	}

	public void setOp(ComparisonOperator op)
	{
		this.op = op;
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value = value;
	}

	@Override
	public List<PlayerCharacter> select(List<PlayerCharacter> candidates)
	{
		if (candidates == null || candidates.isEmpty() || targetModifier == null || op == null)
		{
			return List.of();
		}

		List<PlayerCharacter> result = new ArrayList<>();
		for (PlayerCharacter pc : candidates)
		{
			if (op.compare(pc.getBaseModifier(targetModifier), value))
			{
				result.add(pc);
			}
		}
		return result;
	}

	@Override
	public String describe()
	{
		return (targetModifier == null ? "?" : targetModifier.name())
			+ " "
			+ (op == null ? "?" : op.name())
			+ " "
			+ value;
	}
}
