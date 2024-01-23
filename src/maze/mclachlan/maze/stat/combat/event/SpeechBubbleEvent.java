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

import java.awt.Color;
import java.awt.Rectangle;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.ui.diygui.animation.SpeechBubbleAnimation;

/**
 *
 */
public class SpeechBubbleEvent extends MazeEvent
{
	private PlayerCharacter playerCharacter;
	private Rectangle origination;
	private String speech;
	private int duration;
	private Color colour;

	/*-------------------------------------------------------------------------*/
	public SpeechBubbleEvent(
		PlayerCharacter pc,
		String speech,
		Rectangle origination,
		int duration,
		Color colour)
	{
		this.playerCharacter = pc;
		this.speech = speech;
		this.origination = origination;
		this.duration = duration;
		this.colour = colour;
	}

	/*-------------------------------------------------------------------------*/
	public boolean shouldClearText()
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		if (duration == Delay.WAIT_ON_CLICK)
		{
			return Delay.WAIT_ON_CLICK;
		}
		else
		{
			// animation implementation delay takes care of this, we do not want
			// to delay other events on the queue
			return Delay.NONE;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		SpeechBubbleAnimation.Orientation orientation = switch (Maze.getInstance().getParty().getPlayerCharacterIndex(playerCharacter))
			{
				case 0,2 -> SpeechBubbleAnimation.Orientation.RIGHT;
				case 1,3 -> SpeechBubbleAnimation.Orientation.LEFT;
				case 4 -> SpeechBubbleAnimation.Orientation.ABOVE_RIGHT;
				case 5 -> SpeechBubbleAnimation.Orientation.ABOVE_LEFT;
				default -> null;
			};

		Animation a = new SpeechBubbleAnimation(colour, speech, origination, orientation, duration);
		Object eventMutex = null;
		if (duration == Delay.WAIT_ON_CLICK)
		{
			eventMutex = Maze.getInstance().getEventMutex();
		}

		Maze.getInstance().startAnimation(a, eventMutex, new AnimationContext(playerCharacter));

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getPlayerCharacter()
	{
		return playerCharacter;
	}

	public void setPlayerCharacter(PlayerCharacter playerCharacter)
	{
		this.playerCharacter = playerCharacter;
	}

	public String getSpeech()
	{
		return speech;
	}

	public void setSpeech(String speech)
	{
		this.speech = speech;
	}
}
