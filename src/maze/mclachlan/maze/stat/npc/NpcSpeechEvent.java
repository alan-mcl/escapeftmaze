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

import java.awt.Rectangle;
import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.animation.SpeechBubble;
import mclachlan.maze.ui.diygui.animation.SpeechBubbleDialog;
import mclachlan.maze.util.MazeException;


/**
 *
 */
public class NpcSpeechEvent extends MazeEvent
{
	private final Foe npc;
	private final String text;
	private final int delay;

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
		Rectangle origination = Maze.getInstance().getUi().getObjectBounds(npc.getSprite());

		SpeechBubble.Orientation orientation = switch (npc.getSprite().getVerticalAlignment())
			{
				case TOP -> SpeechBubble.Orientation.BELOW;
				case CENTER, BOTTOM -> SpeechBubble.Orientation.ABOVE;
				default ->
					throw new MazeException("Invalid vertical alignment: " + npc.getSprite().getVerticalAlignment());
			};

		SpeechBubbleDialog dialog = new SpeechBubbleDialog(
			Constants.Colour.STEALTH_GREEN, //todo: NPC speech colour
			text,
			origination,
			orientation);

		Maze.getInstance().getUi().showDialog(dialog);

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
