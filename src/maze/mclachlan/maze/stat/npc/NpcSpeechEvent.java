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

import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.ui.diygui.animation.SpeechBubbleAnimation;


/**
 *
 */
public class NpcSpeechEvent extends MazeEvent
{
	private Foe npc;
	private String text;
	private int delay;

	/*-------------------------------------------------------------------------*/
	public NpcSpeechEvent(String text, Foe npc)
	{
		this(npc, text, Delay.WAIT_ON_CLICK);
	}
	
	/*-------------------------------------------------------------------------*/
	public NpcSpeechEvent(Foe npc, String text, int delay)
	{
		this.npc = npc;
		this.text = text;
		this.delay = delay;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		String s = npc.getDisplayName() + ":\n" + text;

		Animation a = new SpeechBubbleAnimation(
			Constants.Colour.STEALTH_GREEN, //todo: NPC speech colour
			s,
			null,
			SpeechBubbleAnimation.WAIT_FOR_CLICK);

		Maze.getInstance().startAnimation(a, this, new AnimationContext(null));

		Maze.getInstance().journalInContext(
			StringUtil.getUiLabel("j.npc.speech",
				npc.getName(), text));
		
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
}
