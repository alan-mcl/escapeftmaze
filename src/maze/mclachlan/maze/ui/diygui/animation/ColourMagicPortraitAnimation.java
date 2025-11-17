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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.*;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.ui.diygui.Animation;

/**
 *
 */
public class ColourMagicPortraitAnimation extends Animation
{
	static int period = 100;
	static int duration = 400;

	// animation properties
	Color colour;

	// instance parameters
	long startTime = System.currentTimeMillis();
	Rectangle[] bounds;

	public ColourMagicPortraitAnimation()
	{
	}

	/*-------------------------------------------------------------------------*/
	public ColourMagicPortraitAnimation(Color colour)
	{
		this.colour = colour;
	}

	/*-------------------------------------------------------------------------*/
	public void update(Graphics2D g)
	{
		long diff = System.currentTimeMillis() - startTime;

		if ((diff/period) % 2 == 0)
		{
			g.setColor(colour);
			for (Rectangle bound : bounds)
			{
				g.fillRect(bound.x, bound.y, bound.width, bound.height);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public Animation spawn(AnimationContext context)
	{
		ColourMagicPortraitAnimation result = new ColourMagicPortraitAnimation(colour);
		ArrayList bb = new ArrayList();

		if (context != null)
		{
			for (UnifiedActor a : context.getTargets())
			{
				if (a instanceof PlayerCharacter)
				{
					bb.add(getUi().getPlayerCharacterWidgetBounds((PlayerCharacter)a));
				}
			}
		}

		result.bounds = new Rectangle[bb.size()];
		bb.toArray(result.bounds);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isFinished()
	{
		long diff = System.currentTimeMillis() - startTime;

		return diff > duration;
	}

	/*-------------------------------------------------------------------------*/
	public Color getColour()
	{
		return colour;
	}

	public void setColour(Color colour)
	{
		this.colour = colour;
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

		ColourMagicPortraitAnimation that = (ColourMagicPortraitAnimation)o;

		return getColour() != null ? getColour().equals(that.getColour()) : that.getColour() == null;
	}

	@Override
	public int hashCode()
	{
		return getColour() != null ? getColour().hashCode() : 0;
	}
}
