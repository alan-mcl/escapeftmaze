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

package mclachlan.maze.stat.combat.event;

import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.ui.diygui.Animation;
import java.util.*;
import mclachlan.maze.ui.diygui.animation.AnimationContext;

/**
 *
 */
public class AnimationEvent extends MazeEvent
{
	private Animation animation;
	private AnimationContext animationContext;

	public AnimationEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public AnimationEvent(Animation anim)
	{
		this.animation = anim;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Delay.WAIT_ON_CLICK;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		Maze.getInstance().startAnimation(animation, Maze.getInstance().getEventMutex(), animationContext);
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public Animation getAnimation()
	{
		return animation;
	}

	public void setAnimationContext(AnimationContext animationContext)
	{
		this.animationContext = animationContext;
	}

	public void setAnimation(Animation animation)
	{
		this.animation = animation;
	}

	public AnimationContext getAnimationContext()
	{
		return animationContext;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		AnimationEvent that = (AnimationEvent)o;

		if (getAnimation() != null ? !getAnimation().equals(that.getAnimation()) : that.getAnimation() != null)
		{
			return false;
		}
		return getAnimationContext() != null ? getAnimationContext().equals(that.getAnimationContext()) : that.getAnimationContext() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getAnimation() != null ? getAnimation().hashCode() : 0;
		result = 31 * result + (getAnimationContext() != null ? getAnimationContext().hashCode() : 0);
		return result;
	}
}
