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

package mclachlan.maze.game;

import java.util.*;

/**
 *
 */
public class UserConfig
{
	public enum Key
	{
		COMBAT_DELAY("mclachlan.maze.ui.combat_delay"),
		PERSONALITY_CHATTINESS("mclachlan.maze.ui.personality_chattiness"),
		MUSIC_VOLUME("mclachlan.maze.music.volume"),
		CURRENT_TIP_INDEX ("mclachlan.maze.current.tip.index"),
		AUTO_ADD_CONSUMABLES ("mclachlan.maze.auto.add.consumables");

		private String value;

		Key(String value)
		{
			this.value = value;
		}

		public String getValue()
		{
			return value;
		}
	};

	/*-------------------------------------------------------------------------*/
	private int combatDelay;
	private int personalityChattiness;
	private int musicVolume;
	private int currentTipIndex;
	private boolean autoAddConsumables;

	/*-------------------------------------------------------------------------*/
	public UserConfig(Properties p)
	{
		fromProperties(p);
	}

	/*-------------------------------------------------------------------------*/
	public Properties toProperties()
	{
		Properties result = new Properties();

		result.setProperty(Key.COMBAT_DELAY.getValue(), String.valueOf(combatDelay));
		result.setProperty(Key.PERSONALITY_CHATTINESS.getValue(), String.valueOf(personalityChattiness));
		result.setProperty(Key.MUSIC_VOLUME.getValue(), String.valueOf(musicVolume));
		result.setProperty(Key.CURRENT_TIP_INDEX.getValue(), String.valueOf(currentTipIndex));
		result.setProperty(Key.AUTO_ADD_CONSUMABLES.getValue(), String.valueOf(autoAddConsumables));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void fromProperties(Properties p)
	{
		combatDelay = Integer.parseInt(p.getProperty(Key.COMBAT_DELAY.getValue()));
		personalityChattiness = Integer.parseInt(p.getProperty(Key.PERSONALITY_CHATTINESS.getValue()));
		musicVolume = Integer.parseInt(p.getProperty(Key.MUSIC_VOLUME.getValue()));
		currentTipIndex = Integer.parseInt(p.getProperty(Key.CURRENT_TIP_INDEX.getValue()));
		autoAddConsumables = Boolean.valueOf(p.getProperty(Key.AUTO_ADD_CONSUMABLES.getValue()));
	}

	/*-------------------------------------------------------------------------*/
	public int getCombatDelay()
	{
		return combatDelay;
	}

	public void setCombatDelay(int combatDelay)
	{
		this.combatDelay = combatDelay;
	}

	public int getMusicVolume()
	{
		return musicVolume;
	}

	public void setMusicVolume(int musicVolume)
	{
		this.musicVolume = musicVolume;
	}

	public int getPersonalityChattiness()
	{
		return personalityChattiness;
	}

	public void setPersonalityChattiness(int personalityChattiness)
	{
		this.personalityChattiness = personalityChattiness;
	}

	public int getCurrentTipIndex()
	{
		return currentTipIndex;
	}

	public void setCurrentTipIndex(int currentTipIndex)
	{
		this.currentTipIndex = currentTipIndex;
	}

	public boolean isAutoAddConsumables()
	{
		return autoAddConsumables;
	}

	public void setAutoAddConsumables(boolean autoAddConsumables)
	{
		this.autoAddConsumables = autoAddConsumables;
	}
}
