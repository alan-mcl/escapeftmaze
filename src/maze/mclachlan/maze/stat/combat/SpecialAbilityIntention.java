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

import mclachlan.maze.stat.ActorActionIntention;
import mclachlan.maze.stat.SpellTarget;
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class SpecialAbilityIntention extends ActorActionIntention
{
	private SpellTarget target;
	private Spell spell;
	private int castingLevel;
	private String displayName;

	/*-------------------------------------------------------------------------*/

	/**
	 * @param target The selected target
	 * @param spell The spell to cast
	 * @param castingLevel The level to cast it at
	 */
	public SpecialAbilityIntention(SpellTarget target, Spell spell,
		int castingLevel)
	{
		this.target = target;
		this.spell = spell;
		this.castingLevel = castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	public Spell getSpell()
	{
		return spell;
	}

	public SpellTarget getTarget()
	{
		return target;
	}

	public int getCastingLevel()
	{
		return castingLevel;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
}
