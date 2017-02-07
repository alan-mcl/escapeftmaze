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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.LocatePersonEvent;
import mclachlan.maze.ui.UserInterface;
import mclachlan.maze.ui.diygui.GetPlayerSpeechDialog;
import mclachlan.maze.ui.diygui.TextDialogCallback;

/**
 * Divine the location of an NPC
 */
public class LocatePersonSpellResult extends SpellResult
{
	private ValueList value;

	/*-------------------------------------------------------------------------*/
	public LocatePersonSpellResult(ValueList value)
	{
		this.value = value;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getValue()
	{
		return value;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		final UnifiedActor source,
		UnifiedActor target,
		final int castingLevel,
		SpellEffect parent)
	{
		// this spell result only applies to the caster
		UserInterface ui = Maze.getInstance().getUi();

		ui.showDialog(new GetPlayerSpeechDialog(source, new TextDialogCallback()
		{
			@Override
			public void textEntered(String text)
			{
				Maze.getInstance().appendEvents(new LocatePersonEvent(source, text, value, castingLevel));
			}

			@Override
			public void textEntryCancelled() { }
		}));

		return new ArrayList<MazeEvent>();
	}
}
