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

package mclachlan.maze.stat;

/**
 * A level ability that grants a spell-like ability to the character
 */
public class SpecialAbilityLevelAbility extends LevelAbility
{
	private final SpellLikeAbility ability;

	/*-------------------------------------------------------------------------*/
	public SpecialAbilityLevelAbility(
		String key, String displayName,
		String description,
		SpellLikeAbility ability)
	{
		super(key, displayName, description);
		this.ability = ability;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public SpellLikeAbility getAbility()
	{
		return ability;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("SpecialAbilityLevelAbility{");
		sb.append("ability=").append(ability);
		sb.append('}');
		return sb.toString();
	}
}
