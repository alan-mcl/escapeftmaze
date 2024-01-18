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
import mclachlan.maze.audio.Music;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;

/**
 *
 */
public class MusicEvent extends MazeEvent
{
	private List<String> trackNames;
	private String musicState;

	/*-------------------------------------------------------------------------*/
	public MusicEvent(String clipName, String musicState)
	{
		List<String> list = new ArrayList<>(1);
		list.add(clipName);

		this.musicState = musicState;

		init(list);
	}

	/*-------------------------------------------------------------------------*/
	public MusicEvent(List<String> clipNames, String musicState)
	{
		this.musicState = musicState;
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

	public String getMusicState()
	{
		return musicState;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		Music music = Maze.getInstance().getUi().getMusic();
		if (trackNames == null || trackNames.isEmpty())
		{
			music.stop();
			return null;
		}
		else
		{

			List<String> t = new ArrayList<String>(trackNames);
			Collections.shuffle(t);

			music.playLooped(
				Maze.getInstance().getUserConfig().getMusicVolume(),
				t.toArray(new String[0]));

			music.setState(musicState);
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getTrackNames()
	{
		return trackNames;
	}
}
