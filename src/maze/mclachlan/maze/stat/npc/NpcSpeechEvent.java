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

package mclachlan.maze.stat.npc;

import java.awt.Color;
import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.ui.diygui.animation.SpeechBubble;
import mclachlan.maze.ui.diygui.animation.SpeechBubbleAnimation;
import mclachlan.maze.ui.diygui.animation.SpeechBubbleDialog;
import mclachlan.maze.util.MazeException;


/**
 *
 */
public class NpcSpeechEvent extends MazeEvent
{
	private Foe npc;
	private String speechText;
	private int delay;

	public NpcSpeechEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public NpcSpeechEvent(String speechText, Foe npc)
	{
		this(npc, speechText, Delay.WAIT_ON_CLICK);
	}
	
	/*-------------------------------------------------------------------------*/
	public NpcSpeechEvent(Foe npc, String speechText, int delay)
	{
		this.npc = npc;
		this.speechText = speechText;
		this.delay = delay;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		if (npc == null)
		{
			// select the leader of the current encounter
			npc = Maze.getInstance().getCurrentActorEncounter().getLeader();
		}

		SpeechBubble.Orientation orientation = switch (npc.getSprite().getVerticalAlignment())
			{
				case TOP -> SpeechBubble.Orientation.BELOW;
				case CENTER, BOTTOM -> SpeechBubble.Orientation.ABOVE;
				default ->
					throw new MazeException("Invalid vertical alignment: " + npc.getSprite().getVerticalAlignment());
			};

		Color colour = npc.getSpeechColour();

		if (delay == Delay.WAIT_ON_CLICK)
		{
			SpeechBubbleDialog dialog = new SpeechBubbleDialog(
				colour == null ? Constants.Colour.STEALTH_GREEN : colour,
				speechText,
				npc.getSprite(),
				orientation);

			Maze.getInstance().getUi().showDialog(dialog);
		}
		else
		{
			Animation a = new SpeechBubbleAnimation(colour, speechText,
				Maze.getInstance().getUi().getObjectBounds(npc.getSprite()), orientation, delay);

			Maze.getInstance().startAnimation(a, null, new AnimationContext(null));
		}

		Maze.getInstance().journalInContext(
			StringUtil.getUiLabel("j.npc.speech",
				npc.getName(), speechText));
		
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return delay;
	}

	/*-------------------------------------------------------------------------*/
	public boolean shouldClearText()
	{
		return true;
	}

	public Foe getNpc()
	{
		return npc;
	}

	public void setNpc(Foe npc)
	{
		this.npc = npc;
	}

	public String getSpeechText()
	{
		return speechText;
	}

	public void setSpeechText(String speechText)
	{
		this.speechText = speechText;
	}

	public void setDelay(int delay)
	{
		this.delay = delay;
	}
}
