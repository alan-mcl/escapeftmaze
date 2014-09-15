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

import mclachlan.maze.stat.ActorGroup;
import mclachlan.maze.stat.FoeAttack;

/**
 *
 */
public class FoeAttackIntention extends AttackIntention
{
	private FoeAttack foeAttack;
	private FoeAttack.FoeAttackSpell spell;

	/*-------------------------------------------------------------------------*/
	public FoeAttackIntention(
		ActorGroup targetGroup,
		Combat combat,
		FoeAttack foeAttack,
		FoeAttack.FoeAttackSpell spell)
	{
		super(targetGroup, combat, foeAttack);
		this.foeAttack = foeAttack;
		this.spell = spell;
	}
	
	/*-------------------------------------------------------------------------*/
	public FoeAttack getFoeAttack()
	{
		return foeAttack;
	}

	public FoeAttack.FoeAttackSpell getSpell()
	{
		return spell;
	}

	/*-------------------------------------------------------------------------*/
//	@Override
//	public String toString()
//	{
//		final StringBuilder sb = new StringBuilder();
//		sb.append("FoeAttackIntention");
//		sb.append("{foeAttack=").append(foeAttack);
//		sb.append(", spell=").append(spell);
//		sb.append('}');
//		return sb.toString();
//	}
}
