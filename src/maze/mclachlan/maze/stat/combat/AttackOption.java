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

package mclachlan.maze.stat.combat;


import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;

/**
 *
 */
public class AttackOption extends ActorActionOption
{
	private AttackIntention intention;
	private AttackWith attackWith;

	/*-------------------------------------------------------------------------*/
	public AttackOption(AttackWith attackWith)
	{
		super(
			"Attack ("+attackWith.getDisplayName()+")",
			"aao.attack");
		this.attackWith = attackWith;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void select(UnifiedActor actor, Combat combat,
		ActionOptionCallback callback)
	{
		if (combat.getNrOfLivingEnemies(actor) > 0)
		{
			ActorGroup foeGroup = Maze.getInstance().getUi().getSelectedFoeGroup();
			this.intention = new AttackIntention(
				foeGroup, combat, attackWith);
		}

		callback.selected(getIntention());
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public ActorActionIntention getIntention()
	{
		return this.intention;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		return StringUtil.getUiLabel(
			getDisplayName(),
			StringUtil.truncateWithEllipses(attackWith.getDisplayName(), 15));
	}
}
