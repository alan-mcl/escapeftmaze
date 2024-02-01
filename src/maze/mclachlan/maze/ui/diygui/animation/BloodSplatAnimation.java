/*
 * Copyright (c) 2012 Alan McLachlan
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

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.DiyGuiUserInterface;

/**
 *
 */
public class BloodSplatAnimation extends Animation
{
	private final String text;
	private Rectangle origination;

	private final Color colour;
	// instance parameters
	private final long startTime = System.currentTimeMillis();
	private UnifiedActor actor;
	private final int duration;
	private int splatX, splatY;
	private int splatHeight, splatWidth;
	private Color col1;
	private Color col2;
	private RoundRectangle2D rect;

	private final boolean debug = false;

	/*-------------------------------------------------------------------------*/

	/**
	 * Orientation of the speech bubble relative to it's origination bounds
	 */
	public enum Orientation
	{
		LEFT, RIGHT, ABOVE, BELOW, ABOVE_LEFT, ABOVE_RIGHT, BELOW_LEFT, BELOW_RIGHT, CENTERED
	}

	/*-------------------------------------------------------------------------*/
	public BloodSplatAnimation(
		Color colour,
		String text,
		Rectangle origination,
		int duration)
	{
		this.colour = colour;
		this.text = text;
		this.origination = origination;
		this.duration = duration;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void draw(Graphics2D g)
	{
		long diff = System.currentTimeMillis() - startTime;

		if (diff > duration)
		{
			return;
		}

		g.setPaint(new GradientPaint(splatX, splatY, col1, splatX + splatWidth, splatY + splatHeight, col2, true));
		g.fill(rect);

		// debug
		if (debug)
		{
			g.drawRect(origination.x, origination.y, origination.width, origination.height);
		}

		g.setColor(Color.DARK_GRAY);
		g.drawString(text, splatX + splatWidth/2, splatY + splatHeight/2);
	}

	/*-------------------------------------------------------------------------*/
	private void computeBounds(Graphics2D g)
	{
		FontMetrics fm = g.getFontMetrics();

		int maxWidth = 100;
		int textWidth = Math.min(fm.stringWidth(text), maxWidth);

		int textHeight = fm.getAscent();

		splatHeight = textHeight *2;
		splatWidth = textWidth * 3;

		splatX = origination.x + origination.width / 2 - splatWidth / 2;
		splatY = origination.y + origination.height / 2 - splatHeight / 2;

		col1 = colour.brighter();
		col2 = colour.darker();

		rect = new RoundRectangle2D.Double(splatX, splatY, splatWidth, splatHeight, 10, 10);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Animation spawn(AnimationContext context)
	{
		BloodSplatAnimation result = new BloodSplatAnimation(
			colour, text, origination, duration);

		if (this.origination == null)
		{
			// originate from the whole screen
			result.origination = new Rectangle(
				DiyGuiUserInterface.SCREEN_WIDTH / 2 - 50,
				DiyGuiUserInterface.SCREEN_HEIGHT / 2 - 50,
				100, 100);
		}

		result.actor = context.getCaster();

		result.computeBounds((Graphics2D)getUi().getGraphics());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isFinished()
	{
		return System.currentTimeMillis() - startTime > duration;
	}

	/*-------------------------------------------------------------------------*/
	public Color getColour()
	{
		return colour;
	}
}
