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

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;

/**
 *
 */
public class MusicEvent extends MazeEvent
{
	private List<String> trackNames;

	/*-------------------------------------------------------------------------*/
	public MusicEvent(String clipName)
	{
		List list = new ArrayList(1);
		list.add(clipName);
		init(list);
	}

	/*-------------------------------------------------------------------------*/
	public MusicEvent(List<String> clipNames)
	{
		init(clipNames);
	}

	/*-------------------------------------------------------------------------*/
	private void init(List<String> clipNames)
	{
		this.trackNames = clipNames;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Delay.NONE;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		if (trackNames == null || trackNames.isEmpty())
		{
			Maze.getInstance().getUi().getMusic().stop();
			return null;
		}
		else
		{

			List<String> t = new ArrayList<String>(trackNames);
			Collections.shuffle(t);

			Maze.getInstance().getUi().getMusic().playLooped(
				Maze.getInstance().getUserConfig().getMusicVolume(),
				t.toArray(new String[t.size()]));
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getTrackNames()
	{
		return trackNames;
	}
}
