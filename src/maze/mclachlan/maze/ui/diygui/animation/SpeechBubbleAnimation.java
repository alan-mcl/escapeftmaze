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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.DiyGuiUserInterface;

/**
 *
 */
public class SpeechBubbleAnimation extends Animation
{
	private static List<SpeechBubbleAnimation> bubbles = new ArrayList<SpeechBubbleAnimation>();

	private String text;
	private Rectangle origination;

	private Color colour;
	// instance parameters
	private long startTime = System.currentTimeMillis();
	private PlayerCharacter pc;
	private boolean leftHanded;
	private int duration;
	private int bHeight;
	private int bWidth;
	private int bX;
	private Color col1;
	private Color col2;
	private static final int INSET = 40;
	private static final int PADDING = 5;
	private static final int POLY_INTRUSION = 30;
	private static final int POLY_FATNESS = 5;
	private RoundRectangle2D rect;
	private Polygon poly;
	private int textHeight;
	private int textWidth;
	private int bY;

	private Rectangle bounds;
	private int index;
	private ArrayList<String> strings;

	public static final int WAIT_FOR_CLICK = -1;

	/*-------------------------------------------------------------------------*/
	public SpeechBubbleAnimation(
		Color colour,
		String text,
		Rectangle origination,
		int duration)
	{
		this.colour = colour;
		this.text = text;
		this.origination = origination;
		this.duration = duration;

		synchronized (bubbles)
		{
			bubbles.add(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void draw(Graphics2D g)
	{
		long diff = System.currentTimeMillis() - startTime;

		if (duration != WAIT_FOR_CLICK && diff > duration)
		{
			synchronized (bubbles)
			{
				bubbles.remove(this);
			}
			return;
		}

		g.setPaint(new GradientPaint(bX, bY, col1, bX, bY + bHeight, col2, true));
		g.fill(poly);
		g.fill(rect);

		if (duration == WAIT_FOR_CLICK)
		{
			g.setPaint(colour);
			g.draw(poly);
			g.draw(rect);
		}
		
		g.setColor(Color.DARK_GRAY);
		int line = 1;
		for (String s : strings)
		{
			g.drawString(s, bX + PADDING, bY + PADDING + line*textHeight);
			line++;
		}
	}

	/*-------------------------------------------------------------------------*/
	private void computeBounds(Graphics2D g)
	{
		// todo: character slots 5 and 6 need special handling in movement mode

		FontMetrics fm = g.getFontMetrics();

		int maxWidth;
		if (Maze.getInstance().getState() == Maze.State.MOVEMENT ||
			Maze.getInstance().getState() == Maze.State.ENCOUNTER_TILE)
		{
			maxWidth = DiyGuiUserInterface.SCREEN_WIDTH/3;
		}
		else
		{
			maxWidth = DiyGuiUserInterface.SCREEN_WIDTH/4;
		}
		textWidth = Math.min(fm.stringWidth(text), maxWidth);

		strings = new ArrayList<String>();
		String[] paragraphs = text.split("\\n");

		for (int i = 0; i < paragraphs.length; i++)
		{
			String paragraph = paragraphs[i];
			strings.addAll(DIYToolkit.wrapText(
				paragraph, Maze.getInstance().getComponent().getGraphics(), maxWidth));
		}

		textHeight = fm.getAscent();

		bHeight = textHeight *strings.size() + PADDING *2;
		bWidth = textWidth + PADDING *3;
		if (leftHanded)
		{
			bX = origination.x + origination.width + INSET;
		}
		else
		{
			bX = origination.x - INSET - bWidth;
		}
		bY = origination.y + origination.height/2 - bHeight /2;

		bounds = new Rectangle(bX, bY, bWidth, bHeight);

		//
		// check and see if these bounds overlap an existing speech bubble
		//
		synchronized (bubbles)
		{
			int moves = 0;

			ListIterator<SpeechBubbleAnimation> li = bubbles.listIterator();
			while (li.hasNext())
			{
				if (moves > 2)
				{
					// prevent infinite looping
					break;
				}

				SpeechBubbleAnimation a = li.next();

				if (a == this)
				{
					continue;
				}

				if (a.pc == this.pc)
				{
					// same origin. kill the other one.
					a.duration = 0;
					li.remove();
					break;
				}

				if (a != this && this.pc != a.pc && this.index != a.index &&
					a.bounds != null &&
					this.bounds.intersects(a.bounds))
				{
					if (this.index < a.index)
					{
						// place this bubble above the other one
						this.bY = a.bY -this.bHeight -PADDING;
					}
					else
					{
						// place this bubble below the other one
						this.bY = a.bY +a.bHeight +PADDING;
					}

					// reset to the start again
					li = bubbles.listIterator();
					moves++;
				}
			}
		}

		col1 = colour.brighter();
		col2 = colour.darker();

		rect = new RoundRectangle2D.Double(bX, bY, bWidth, bHeight, 10, 10);

		if (leftHanded)
		{
			poly = new Polygon(
				new int[]
					{
						origination.x + origination.width - POLY_INTRUSION,
						bX,
						bX,
					},
				new int[]
					{
						origination.y + origination.height/2,
						bY + bHeight /2 - POLY_FATNESS,
						bY + bHeight /2 + POLY_FATNESS,
					},
				3);
		}
		else
		{
			poly = new Polygon(
				new int[]
					{
						origination.x + POLY_INTRUSION,
						bX + bWidth,
						bX + bWidth,
					},
				new int[]
					{
						origination.y + origination.height/2,
						bY + bHeight /2 - POLY_FATNESS,
						bY + bHeight /2 + POLY_FATNESS,
					},
				3);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Animation spawn(AnimationContext context)
	{
		SpeechBubbleAnimation result = new SpeechBubbleAnimation(
			colour, text, origination, duration);

		result.pc = (PlayerCharacter)context.getCaster();
		if (Maze.getInstance().getParty() != null)
		{
			result.index = Maze.getInstance().getParty().getPlayerCharacterIndex(result.pc);
		}

		result.origination = origination;
		result.leftHanded = this.origination.x < DiyGuiUserInterface.SCREEN_WIDTH/2;
		result.computeBounds((Graphics2D)getUi().getGraphics());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isFinished()
	{
		long diff = System.currentTimeMillis() - startTime;

		return duration != WAIT_FOR_CLICK && diff > duration;
	}

	/*-------------------------------------------------------------------------*/
	public Color getColour()
	{
		return colour;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processMouseEvent(MouseEvent event)
	{
		if (duration == WAIT_FOR_CLICK)
		{
			duration = 0;
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processKeyEvent(KeyEvent event)
	{
		if (duration == WAIT_FOR_CLICK)
		{
			duration = 0;
		}
	}
}
