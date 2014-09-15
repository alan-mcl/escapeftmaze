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

import mclachlan.maze.ui.diygui.Animation;
import java.awt.*;

/**
 *
 */
public class TestAnimation extends Animation
{
	long startTime;
	int counter = 10;

	public TestAnimation()
	{
		startTime = System.currentTimeMillis();
	}

	public void draw(Graphics2D g)
	{
		counter += counter;

		g.setColor(Color.WHITE);
		g.fillRect(counter,counter,counter,counter);
	}

	/*-------------------------------------------------------------------------*/
	public Animation spawn(AnimationContext context)
	{
		return new TestAnimation();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isFinished()
	{
		return System.currentTimeMillis() - startTime > 20000;
	}
}
