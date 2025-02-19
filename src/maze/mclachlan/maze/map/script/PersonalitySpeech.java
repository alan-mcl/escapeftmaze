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

package mclachlan.maze.map.script;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.combat.event.PersonalitySpeechBubbleEvent;

/**
 *
 */
public class PersonalitySpeech extends TileScript
{
	private String speechKey;
	private boolean modal;

	public PersonalitySpeech()
	{
	}

	/*-------------------------------------------------------------------------*/
	public PersonalitySpeech(String text, boolean modal)
	{
		this.speechKey = text;
		this.modal = modal;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		List<MazeEvent> result = new ArrayList<>();
		result.add(new PersonalitySpeechBubbleEvent(speechKey, modal));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String getSpeechKey()
	{
		return speechKey;
	}

	public boolean isModal()
	{
		return modal;
	}

	public void setSpeechKey(String speechKey)
	{
		this.speechKey = speechKey;
	}

	public void setModal(boolean modal)
	{
		this.modal = modal;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}

		PersonalitySpeech that = (PersonalitySpeech)o;

		if (isModal() != that.isModal())
		{
			return false;
		}
		return getSpeechKey() != null ? getSpeechKey().equals(that.getSpeechKey()) : that.getSpeechKey() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getSpeechKey() != null ? getSpeechKey().hashCode() : 0);
		result = 31 * result + (isModal() ? 1 : 0);
		return result;
	}
}
