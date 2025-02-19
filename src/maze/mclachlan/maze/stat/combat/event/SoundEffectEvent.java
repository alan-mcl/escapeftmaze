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

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Dice;
import java.util.*;

/**
 *
 */
public class SoundEffectEvent extends MazeEvent
{
	private List<String> clipNames;
	private Dice die;

	public SoundEffectEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public SoundEffectEvent(String clipName)
	{
		List list = new ArrayList(1);
		list.add(clipName);
		init(list);
	}

	/*-------------------------------------------------------------------------*/
	public SoundEffectEvent(List<String> clipNames)
	{
		init(clipNames);
	}

	/*-------------------------------------------------------------------------*/
	public SoundEffectEvent(String... strings)
	{
		this(Arrays.asList(strings));
	}

	/*-------------------------------------------------------------------------*/
	private void init(List<String> clipNames)
	{
		this.clipNames = clipNames;
		die = new Dice(1, clipNames.size(), 0);
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Delay.NONE;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		Maze.getInstance().getAudioPlayer().playSound(
			clipNames.get(die.roll("random sound effect selection")-1),
			Maze.getInstance().getUserConfig().getMusicVolume());
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getClipNames()
	{
		return clipNames;
	}

	public void setClipNames(List<String> clipNames)
	{
		this.clipNames = clipNames;
	}

	public Dice getDie()
	{
		return die;
	}

	public void setDie(Dice die)
	{
		this.die = die;
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

		SoundEffectEvent that = (SoundEffectEvent)o;

		if (getClipNames() != null ? !getClipNames().equals(that.getClipNames()) : that.getClipNames() != null)
		{
			return false;
		}
		return getDie() != null ? getDie().equals(that.getDie()) : that.getDie() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getClipNames() != null ? getClipNames().hashCode() : 0;
		result = 31 * result + (getDie() != null ? getDie().hashCode() : 0);
		return result;
	}
}
