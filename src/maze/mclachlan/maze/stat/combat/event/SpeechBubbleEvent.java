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

package mclachlan.maze.stat.combat.event;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Personality;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.SpeechUtil;
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
	private Personality personality;
	private String speechKey;
	private boolean modal;

	private boolean suppressExtraChattiness = false;

	/*-------------------------------------------------------------------------*/
	public SpeechBubbleEvent(String speechKey, boolean modal)
	{
		this.speechKey = speechKey;
		this.modal = modal;
	}

	/*-------------------------------------------------------------------------*/
	public SpeechBubbleEvent(
		PlayerCharacter playerCharacter,
		String speechKey)
	{
		this(playerCharacter,
			playerCharacter.getPersonality(),
			speechKey,
			Maze.getInstance().getUi().getPlayerCharacterWidgetBounds(playerCharacter),
			false);
	}

	/*-------------------------------------------------------------------------*/
	public SpeechBubbleEvent(
		PlayerCharacter pc,
		Personality personality,
		String speechKey,
		Rectangle origination,
		boolean modal)
	{
		this.playerCharacter = pc;
		this.personality = personality;
		this.speechKey = speechKey;
		this.origination = origination;
		this.modal = modal;
	}

	/*-------------------------------------------------------------------------*/
	public boolean shouldClearText()
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		if (modal && playerCharacter != null)
		{
			return Delay.WAIT_ON_CLICK;
		}
		else
		{
			return Delay.NONE;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		// local proxies so that the state of the event does not get poked
		
		PlayerCharacter playerCharacter = this.playerCharacter;
		Personality personality = this.personality;
		Rectangle origination = this.origination;
		Color colour = null;

		if (personality != null &&
			personality.getWords(speechKey) == null)
		{
			// personality is specified, but this character has nothing to say
			return null;
		}

		// do we need a character?
		if (playerCharacter == null && (personality == null || origination == null))
		{
			playerCharacter = SpeechUtil.getInstance().getRandomPlayerCharacterForSpeech(speechKey);

			if (playerCharacter == null)
			{
				// no available character to say this
				return null;
			}
		}

		if (personality == null)
		{
			personality = playerCharacter.getPersonality();
		}

		if (origination == null)
		{
			origination =  Maze.getInstance().getUi().getPlayerCharacterWidgetBounds(playerCharacter);
		}

		if (colour == null)
		{
			colour = personality.getColour();
		}

		String text = personality.getWords(speechKey);

		if (text == null)
		{
			// bit of a hack, to support character free types speech
			text = speechKey;
		}

		if (playerCharacter != null)
		{
			text = playerCharacter.modifyPersonalitySpeech(speechKey, text);
		}

		int duration;
		if (modal)
		{
			duration = SpeechBubbleAnimation.WAIT_FOR_CLICK;
		}
		else
		{
			duration = SpeechUtil.getInstance().getSpeechBubbleDuration(text);
		}
		Animation a = new SpeechBubbleAnimation(colour, text, origination, duration);

		Maze.getInstance().startAnimation(a, this, new AnimationContext(playerCharacter));

		if (this.isModal() && !this.suppressExtraChattiness &&
			SpeechUtil.getInstance().getChattiness() == SpeechUtil.HIGH)
		{
			// try and return a second speech bubble animation for a different PC

			PlayerCharacter pc2 = null;

			for (int i=0; i<6; i++)
			{
				pc2 = SpeechUtil.getInstance().getRandomPlayerCharacterForSpeech(speechKey);
				if (pc2 != null && pc2 != playerCharacter)
				{
					break;
				}
			}

			if (pc2 != null && pc2 != playerCharacter)
			{
				SpeechBubbleEvent e2 = new SpeechBubbleEvent(
					pc2, null, speechKey, null, this.isModal());
				e2.suppressExtraChattiness = true;
				return getList(e2);
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
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

	public String getSpeechKey()
	{
		return speechKey;
	}

	public void setSpeechKey(String speechKey)
	{
		this.speechKey = speechKey;
	}

	public boolean isModal()
	{
		return modal;
	}

	public void setModal(boolean modal)
	{
		this.modal = modal;
	}
}
