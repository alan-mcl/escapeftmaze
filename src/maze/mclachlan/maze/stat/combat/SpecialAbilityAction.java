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

package mclachlan.maze.stat.combat;

import mclachlan.maze.stat.SpellTarget;
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class SpecialAbilityAction extends CombatAction
{
	private String description;
	private SpellTarget target;
	private Spell spell;
	private int castingLevel;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param description
	 * 	How the special ability is described
	 * @param target
	 * 	The selected target group
	 * @param spell
	 * 	The spell to resolve
	 * @param castingLevel
	 * 	The level the spell is being cast at
	 */ 
	public SpecialAbilityAction(String description, SpellTarget target, Spell spell, int castingLevel)
	{
		this.description = description;
		this.target = target;
		this.spell = spell;
		this.castingLevel = castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	public int getCastingLevel()
	{
		return castingLevel;
	}

	public String getDescription()
	{
		return description;
	}

	public Spell getSpell()
	{
		return spell;
	}

	public SpellTarget getTarget()
	{
		return target;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("SpecialAbilityAction");
		sb.append("{targetGroup=").append(target);
		sb.append(", spell=").append(spell);
		sb.append(", castingLevel=").append(castingLevel);
		sb.append('}');
		return sb.toString();
	}
}
