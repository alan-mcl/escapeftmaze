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
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.ui.diygui.animation.SpeechBubble;
import mclachlan.maze.ui.diygui.animation.SpeechBubbleAnimation;
import mclachlan.maze.ui.diygui.animation.SpeechBubbleDialog;

/**
 *
 */
public class SpeechBubbleEvent extends MazeEvent
{
	private PlayerCharacter playerCharacter;
	private final Rectangle origination;
	private SpeechBubble.Orientation orientation;
	private String speech;
	private final int duration;
	private final Color colour;

	/*-------------------------------------------------------------------------*/
	public SpeechBubbleEvent(
		PlayerCharacter pc,
		String speech,
		Rectangle origination,
		SpeechBubble.Orientation orientation,
		int duration,
		Color colour)
	{
		this.playerCharacter = pc;
		this.speech = speech;
		this.origination = origination;
		this.orientation = orientation;
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
		PlayerParty party = Maze.getInstance().getParty();
		if (orientation == null)
		{
			if (party != null)
			{
				orientation = switch (party.getPlayerCharacterIndex(playerCharacter))
					{
						case 0, 2 -> SpeechBubble.Orientation.RIGHT;
						case 1, 3 -> SpeechBubble.Orientation.LEFT;
						case 4 -> SpeechBubble.Orientation.ABOVE_RIGHT;
						case 5 -> SpeechBubble.Orientation.ABOVE_LEFT;
						default -> null;
					};
			}
			else
			{
				orientation = null;
			}
		}


		if (duration == Delay.WAIT_ON_CLICK)
		{
			SpeechBubbleDialog dialog = new SpeechBubbleDialog(
				colour,
				speech,
				origination,
				orientation);

			Maze.getInstance().getUi().showDialog(dialog);
		}
		else
		{
			Animation a = new SpeechBubbleAnimation(colour, speech, origination, orientation, duration);

			Maze.getInstance().startAnimation(a, null, new AnimationContext(playerCharacter));
		}



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
