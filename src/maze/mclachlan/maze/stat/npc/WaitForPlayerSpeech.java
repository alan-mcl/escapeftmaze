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
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.combat.event.SpeechBubbleEvent;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class WaitForPlayerSpeech extends MazeEvent
{
	private Npc npc;
	private PlayerCharacter pc;

	/*-------------------------------------------------------------------------*/
	public WaitForPlayerSpeech(Npc npc, PlayerCharacter pc)
	{
		this.npc = npc;
		this.pc = pc;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return pc.getName()+": ";
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		// HACK to let the CombatDisplayWidget know about this class, to avoid a
		// hang when we call wait() below.
		Maze.getInstance().getUi().displayMazeEvent(this, true);
		
		synchronized(this)
		{
			try
			{
				this.wait();
			}
			catch (InterruptedException e)
			{
				throw new MazeException(e);
			}
		}

		String playerSpeech = Maze.getInstance().getPlayerSpeech();

		/*Animation a = new SpeechBubbleAnimation(
			pc.getPersonality().getColour(),
			playerSpeech,
			Maze.getInstance().getUi().getPortraitWidgetBounds(pc),
			3000);

		Maze.getInstance().startAnimation(a, this, new AnimationContext(pc));*/


		List<MazeEvent> result = new ArrayList<MazeEvent>();

		result.add(new SpeechBubbleEvent(pc, playerSpeech));

		result.addAll(npc.getScript().parsePartySpeech(pc, playerSpeech));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return MazeEvent.Delay.WAIT_ON_READLINE;
	}
}
