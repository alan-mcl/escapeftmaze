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
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.DiyGuiUserInterface;

/**
 * Animation of a speech bubble - it is non modal and vanishes after a time.
 */
public class SpeechBubbleAnimation extends Animation
{
	private final SpeechBubble speechBubble;

	// instance parameters
	private final int duration;
	private final long startTime = System.currentTimeMillis();

	/*-------------------------------------------------------------------------*/
	public SpeechBubbleAnimation(
		Color colour,
		String text,
		Rectangle origination,
		SpeechBubble.Orientation orientation,
		int duration)
	{
		this.speechBubble = new SpeechBubble(colour, text, origination, orientation);
		this.duration = duration;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void draw(Graphics2D g)
	{
		long diff = System.currentTimeMillis() - startTime;

		if (diff > duration)
		{
			SpeechBubble.remove(this.speechBubble);
			return;
		}

		this.speechBubble.draw(g, false);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Animation spawn(AnimationContext context)
	{
		Rectangle origination = this.speechBubble.getOrigination();
		SpeechBubble.Orientation orientation = this.speechBubble.getOrientation();

		if (this.speechBubble.getOrigination() == null)
		{
			// originate from the whole screen
			origination = new Rectangle(
				DiyGuiUserInterface.SCREEN_WIDTH / 2 - 50,
				DiyGuiUserInterface.SCREEN_HEIGHT / 2 - 50,
				100, 100);
		}

		if (this.speechBubble.getOrientation() == null)
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
					orientation = SpeechBubble.Orientation.ABOVE_LEFT;
				}
				else if (origination.y < belowLimit)
				{
					orientation = SpeechBubble.Orientation.BELOW_LEFT;
				}
				else
				{
					orientation = SpeechBubble.Orientation.LEFT;
				}
			}
			else if (origination.x < rightLimit)
			{
				if (origination.y > aboveLimit)
				{
					orientation = SpeechBubble.Orientation.ABOVE_RIGHT;
				}
				else if (origination.y < belowLimit)
				{
					orientation = SpeechBubble.Orientation.BELOW_RIGHT;
				}
				else
				{
					orientation = SpeechBubble.Orientation.RIGHT;
				}
			}
			else
			{
				if (origination.y > aboveLimit)
				{
					orientation = SpeechBubble.Orientation.ABOVE;
				}
				else if (origination.y < belowLimit)
				{
					orientation = SpeechBubble.Orientation.BELOW;
				}
				else
				{
					orientation = SpeechBubble.Orientation.CENTERED;
				}
			}
		}

		SpeechBubbleAnimation result = new SpeechBubbleAnimation(
			this.speechBubble.getColour(),
			this.speechBubble.getText(),
			origination,
			orientation,
			duration);

		PlayerCharacter pc = (PlayerCharacter)context.getCaster();
		int index = -1;
		if (Maze.getInstance().getParty() != null)
		{
			index = Maze.getInstance().getParty().getPlayerCharacterIndex(pc);
		}

		result.speechBubble.computeBounds((Graphics2D)getUi().getGraphics(), pc, index);

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
		return this.speechBubble.getColour();
	}

}
