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
public class SpellSilencedAction extends CombatAction
{
	private SpellTarget targetGroup;
	private Spell spell;
	private int castingLevel;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param targetGroup
	 * 	The selected target group, may not be used depending on the spell's 
	 * 	chosen target.
	 * @param spell
	 * 	The spell to resolve
	 * @param castingLevel
	 * 	The level the spell is being cast at
	 */ 
	public SpellSilencedAction(SpellTarget targetGroup, Spell spell, int castingLevel)
	{
		this.targetGroup = targetGroup;
		this.spell = spell;
		this.castingLevel = castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	public int getCastingLevel()
	{
		return castingLevel;
	}

	public Spell getSpell()
	{
		return spell;
	}

	public SpellTarget getTargetGroup()
	{
		return targetGroup;
	}
}
