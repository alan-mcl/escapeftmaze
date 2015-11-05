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


import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.ActorActionIntention;
import mclachlan.maze.stat.ActorActionOption;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class ForceOpenOption extends ActorActionOption
{
	private ActorActionIntention intention;

	/*-------------------------------------------------------------------------*/
	public ForceOpenOption()
	{
		super("Force_Open", "aao.force.open");
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void select(UnifiedActor actor, Combat combat,
		ActionOptionCallback callback)
	{
		intention = new ForceOpenIntention();
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
		return StringUtil.getUiLabel(getDisplayName());
	}
}
