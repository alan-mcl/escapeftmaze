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

package mclachlan.maze.ui.diygui.animation;

import java.util.*;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class AnimationContext
{
	private UnifiedActor caster;
	private List<UnifiedActor> targets = new ArrayList<UnifiedActor>();

	/*-------------------------------------------------------------------------*/
	public AnimationContext(UnifiedActor source)
	{
		this.caster = source;
	}

	/*-------------------------------------------------------------------------*/
	public void addTarget(UnifiedActor target)
	{
		targets.add(target);
	}

	/*-------------------------------------------------------------------------*/

	public UnifiedActor getCaster()
	{
		return caster;
	}

	public void setCaster(UnifiedActor caster)
	{
		this.caster = caster;
	}

	public List<UnifiedActor> getTargets()
	{
		return targets;
	}

	public void setTargets(List<UnifiedActor> targets)
	{
		this.targets = targets;
	}
}
