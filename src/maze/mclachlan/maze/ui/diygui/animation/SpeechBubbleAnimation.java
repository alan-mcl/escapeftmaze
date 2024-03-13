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
import java.util.List;
import java.util.*;
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
	private static final List<SpeechBubbleAnimation> bubbles = new ArrayList<>();

	private final String text;
	private Rectangle origination;

	private final Color colour;
	// instance parameters
	private final long startTime = System.currentTimeMillis();
	private PlayerCharacter pc;
	private Orientation orientation;
	private int duration;
	private int bHeight, bWidth;
	private int bX, bY;
	private Color col1;
	private Color col2;
	private static final int INSET = 40;
	private static final int PADDING = 5;
	private static final int POLY_INTRUSION = 30;
	private static final int POLY_FATNESS = 5;
	private RoundRectangle2D rect;
	private Polygon poly;
	private int textHeight, textWidth;

	private Rectangle bounds;
	private int index;
	private ArrayList<String> strings;

	public static final int WAIT_FOR_CLICK = -1;

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
	public SpeechBubbleAnimation(
		Color colour,
		String text,
		Rectangle origination,
		Orientation orientation,
		int duration)
	{
		this.colour = colour;
		this.text = text;
		this.origination = origination;
		this.duration = duration;
		this.orientation = orientation;

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
		if (poly != null)
		{
			g.fill(poly);
		}
		g.fill(rect);

		if (duration == WAIT_FOR_CLICK)
		{
			g.setPaint(colour);
//			if (poly != null)
//			{
//				g.draw(poly);
//			}
			g.draw(rect);
		}

		// debug
		if (debug)
		{
			g.drawRect(origination.x, origination.y, origination.width, origination.height);
		}

		g.setColor(Color.DARK_GRAY);
		int line = 1;
		for (String s : strings)
		{
			g.drawString(s, bX + PADDING, bY + PADDING + line * textHeight);
			line++;
		}
	}

	/*-------------------------------------------------------------------------*/
	private void computeBounds(Graphics2D g)
	{
		FontMetrics fm = g.getFontMetrics();

		int maxWidth;
		if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			maxWidth = DiyGuiUserInterface.SCREEN_WIDTH / 3;
		}
		else
		{
			maxWidth = DiyGuiUserInterface.SCREEN_WIDTH / 4;
		}
		textWidth = Math.min(fm.stringWidth(text), maxWidth);

		strings = new ArrayList<>();
		String[] paragraphs = text.split("\\n");

		for (int i = 0; i < paragraphs.length; i++)
		{
			String paragraph = paragraphs[i];
			strings.addAll(DIYToolkit.wrapText(
				paragraph, Maze.getInstance().getComponent().getGraphics(), maxWidth));
		}

		textHeight = fm.getAscent();

		bHeight = textHeight * strings.size() + PADDING * 2;
		bWidth = textWidth + PADDING * 3;
		switch (orientation)
		{
			case RIGHT:
				bX = origination.x + origination.width + INSET;
				bY = origination.y + origination.height / 2 - bHeight / 2;
				break;
			case LEFT:
				bX = origination.x - INSET - bWidth;
				bY = origination.y + origination.height / 2 - bHeight / 2;
				break;
			case ABOVE:
				bX = origination.x + origination.width / 2 - bWidth / 2;
				bY = origination.y - INSET - bHeight;
				break;
			case BELOW:
				bX = origination.x + origination.width / 2 - bWidth / 2;
				bY = origination.y + origination.height + INSET;
				break;
			case ABOVE_LEFT:
				bX = origination.x - INSET/2 - bWidth;
				bY = origination.y - INSET/2 - bHeight;
				break;
			case ABOVE_RIGHT:
				bX = origination.x + origination.width + INSET/2;
				bY = origination.y - INSET/2 - bHeight;
				break;
			case BELOW_LEFT:
				bX = origination.x - INSET/2 - bWidth;
				bY = origination.y + origination.height + INSET/2;
				break;
			case BELOW_RIGHT:
				bX = origination.x + origination.width + INSET/2;
				bY = origination.y + origination.height + INSET/2;
				break;
			case CENTERED:
				bX = origination.x + origination.width / 2 - bWidth / 2;
				bY = origination.y + origination.height / 2 - bHeight / 2;
				break;
		}


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
						this.bY = a.bY - this.bHeight - PADDING;
					}
					else
					{
						// place this bubble below the other one
						this.bY = a.bY + a.bHeight + PADDING;
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

		switch (orientation)
		{
			case LEFT:
				poly = new Polygon(
					new int[]
						{
							origination.x + POLY_INTRUSION,
							bX + bWidth,
							bX + bWidth,
						},
					new int[]
						{
							origination.y + origination.height / 2,
							bY + bHeight / 2 - POLY_FATNESS,
							bY + bHeight / 2 + POLY_FATNESS,
						},
					3);
				break;
			case RIGHT:
				poly = new Polygon(
					new int[]
						{
							origination.x + origination.width - POLY_INTRUSION,
							bX,
							bX,
						},
					new int[]
						{
							origination.y + origination.height / 2,
							bY + bHeight / 2 - POLY_FATNESS,
							bY + bHeight / 2 + POLY_FATNESS,
						},
					3);
				break;
			case ABOVE:
				poly = new Polygon(
					new int[]
						{
							origination.x + origination.width / 2,
							bX + bWidth/2 - POLY_FATNESS,
							bX + bWidth/2 + POLY_FATNESS,
						},
					new int[]
						{
							origination.y + POLY_INTRUSION,
							bY + bHeight,
							bY + bHeight,
						},
					3);
				break;
			case BELOW:
				poly = new Polygon(
					new int[]
						{
							origination.x + origination.width / 2,
							bX + bWidth/2 - POLY_FATNESS,
							bX + bWidth/2 + POLY_FATNESS,
						},
					new int[]
						{
							origination.y + origination.height - POLY_INTRUSION,
							bY,
							bY,
						},
					3);
				break;
			case ABOVE_LEFT:
				poly = new Polygon(
					new int[]
						{
							origination.x + POLY_INTRUSION,
							bX + bWidth,
							bX + bWidth - POLY_FATNESS,
						},
					new int[]
						{
							origination.y + POLY_INTRUSION,
							bY + bHeight - POLY_FATNESS /2,
							bY + bHeight,
						},
					3);
				break;
			case ABOVE_RIGHT:
				poly = new Polygon(
					new int[]
						{
							origination.x +origination.width - POLY_INTRUSION,
							bX,
							bX + POLY_FATNESS,
						},
					new int[]
						{
							origination.y + POLY_INTRUSION,
							bY + bHeight - POLY_FATNESS /2,
							bY + bHeight,
						},
					3);
				break;
			case BELOW_LEFT:
				poly = new Polygon(
					new int[]
						{
							origination.x + POLY_INTRUSION,
							bX + bWidth - POLY_FATNESS,
							bX + bWidth,
						},
					new int[]
						{
							origination.y + origination.height - POLY_INTRUSION,
							bY,
							bY + POLY_FATNESS,
						},
					3);
				break;
			case BELOW_RIGHT:
				poly = new Polygon(
					new int[]
						{
							origination.x +origination.width - POLY_INTRUSION,
							bX + POLY_FATNESS,
							bX,
						},
					new int[]
						{
							origination.y + origination.height - POLY_INTRUSION,
							bY,
							bY + POLY_FATNESS,
						},
					3);
				break;


			case CENTERED:
			default:
				// no prong
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Animation spawn(AnimationContext context)
	{
		SpeechBubbleAnimation result = new SpeechBubbleAnimation(
			colour, text, origination, orientation, duration);

		if (this.origination == null)
		{
			// originate from the whole screen
			result.origination = new Rectangle(
				DiyGuiUserInterface.SCREEN_WIDTH / 2 - 50,
				DiyGuiUserInterface.SCREEN_HEIGHT / 2 - 50,
				100, 100);
		}

		if (this.orientation == null)
		{
			// guess the orientation
			int leftLimit = DiyGuiUserInterface.SCREEN_WIDTH / 3 * 2;
			int rightLimit = DiyGuiUserInterface.SCREEN_WIDTH / 3;
			int aboveLimit = DiyGuiUserInterface.SCREEN_HEIGHT / 3 *2;
			int belowLimit = DiyGuiUserInterface.SCREEN_HEIGHT / 3;

			if (origination.x > leftLimit)
			{
				if (origination.y > aboveLimit)
				{
					result.orientation = Orientation.ABOVE_LEFT;
				}
				else if (origination.y < belowLimit)
				{
					result.orientation = Orientation.BELOW_LEFT;
				}
				else
				{
					result.orientation = Orientation.LEFT;
				}
			}
			else if (origination.x < rightLimit)
			{
				if (origination.y > aboveLimit)
				{
					result.orientation = Orientation.ABOVE_RIGHT;
				}
				else if (origination.y < belowLimit)
				{
					result.orientation = Orientation.BELOW_RIGHT;
				}
				else
				{
					result.orientation = Orientation.RIGHT;
				}
			}
			else
			{
				if (origination.y > aboveLimit)
				{
					result.orientation = Orientation.ABOVE;
				}
				else if (origination.y < belowLimit)
				{
					result.orientation = Orientation.BELOW;
				}
				else
				{
					result.orientation = Orientation.CENTERED;
				}
			}
		}

		result.pc = (PlayerCharacter)context.getCaster();
		if (Maze.getInstance().getParty() != null)
		{
			result.index = Maze.getInstance().getParty().getPlayerCharacterIndex(result.pc);
		}

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
	public boolean processMouseEvent(MouseEvent event)
	{
		if (duration == WAIT_FOR_CLICK)
		{
			if (event.getID() == MouseEvent.MOUSE_CLICKED)
			{
				duration = 0;
			}

			// we want the modal speech bubble to consume any other mouse events
			return true;
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean processKeyEvent(KeyEvent event)
	{
		int keyCode = event.getKeyCode();
		if (duration == WAIT_FOR_CLICK)
		{
			if (event.getID() == KeyEvent.KEY_PRESSED && (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE))
			{
				duration = 0;

				synchronized (Maze.getInstance().getEventMutex())
				{
					Maze.getInstance().getEventMutex().notifyAll();
				}
			}

			// we want the modal speech bubble to prevent any other actions even if
			// the key pressed doesn't clear it
			return true;
		}
		return false;
	}
}
