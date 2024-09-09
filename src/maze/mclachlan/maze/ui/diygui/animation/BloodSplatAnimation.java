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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
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

	private final boolean debug = false;
	private BufferedImage img;
	private int textX;
	private int textY;

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

/*
		g.setPaint(new GradientPaint(splatX, splatY, col1, splatX + splatWidth, splatY + splatHeight, col2, true));
		g.fill(rect);

		// debug
		if (debug)
		{
			g.drawRect(origination.x, origination.y, origination.width, origination.height);
		}
*/

		g.drawImage(img, splatX, splatY, splatWidth, splatHeight, Maze.getInstance().getComponent());

		g.setColor(Color.WHITE);
		g.drawString(text, textX, textY);
	}

	/*-------------------------------------------------------------------------*/
	private void computeBounds(Graphics2D g)
	{
		img = Database.getInstance().getMazeTexture("BLOOD_SPLAT_1").getTexture().getImages()[0];

		splatHeight = img.getHeight(Maze.getInstance().getComponent());
		splatWidth = img.getWidth(Maze.getInstance().getComponent());

		splatX = origination.x + origination.width / 2 - splatWidth / 2;
		splatY = origination.y + origination.height / 2 - splatHeight / 2;

		// center the text in the splat
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D stringBounds = fm.getStringBounds(text, g);

		int textWidth = (int)stringBounds.getWidth();
		int textHeight = (int)stringBounds.getHeight();

		textX = splatX +splatWidth/2 -textWidth/2;
		textY = splatY +splatHeight/2 +textHeight/2 -((fm.getAscent() + fm.getDescent()))/2;
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
