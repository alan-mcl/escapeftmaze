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
 * Per-user preferences persisted as V2 JSON ({@code user.json}).
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

	private Map<String, String> extras = new HashMap<>();

	/*-------------------------------------------------------------------------*/
	public UserConfig()
	{
	}

	/*-------------------------------------------------------------------------*/
	public UserConfig(UserConfig other)
	{
		this.combatDelay = other.combatDelay;
		this.personalityChattiness = other.personalityChattiness;
		this.musicVolume = other.musicVolume;
		this.currentTipIndex = other.currentTipIndex;
		this.autoAddConsumables = other.autoAddConsumables;
		this.extras = new HashMap<>(other.extras);
	}

	/*-------------------------------------------------------------------------*/
	/** Upgrade path from legacy {@code user.cfg} Properties. */
	public static UserConfig fromProperties(Properties p)
	{
		UserConfig result = new UserConfig();
		result.combatDelay = Integer.parseInt(p.getProperty(Key.COMBAT_DELAY.getValue()));
		result.personalityChattiness = Integer.parseInt(
			p.getProperty(Key.PERSONALITY_CHATTINESS.getValue()));
		result.musicVolume = Integer.parseInt(p.getProperty(Key.MUSIC_VOLUME.getValue()));
		result.currentTipIndex = Integer.parseInt(
			p.getProperty(Key.CURRENT_TIP_INDEX.getValue()));
		result.autoAddConsumables = Boolean.valueOf(
			p.getProperty(Key.AUTO_ADD_CONSUMABLES.getValue()));

		for (String name : p.stringPropertyNames())
		{
			if (!result.isKnownKey(name))
			{
				result.extras.put(name, p.getProperty(name));
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static UserConfig defaultsForTesting()
	{
		UserConfig result = new UserConfig();
		result.setCombatDelay(0);
		result.setPersonalityChattiness(0);
		result.setMusicVolume(0);
		result.setCurrentTipIndex(0);
		result.setAutoAddConsumables(false);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public boolean getBoolean(String var)
	{
		if (var == null)
		{
			return false;
		}

		String value = getProperty(var);
		return value != null && Boolean.valueOf(value);
	}

	/*-------------------------------------------------------------------------*/
	public String getProperty(String var)
	{
		if (var == null)
		{
			return null;
		}

		Key key = keyFor(var);
		if (key != null)
		{
			return switch (key)
			{
				case COMBAT_DELAY -> String.valueOf(combatDelay);
				case PERSONALITY_CHATTINESS -> String.valueOf(personalityChattiness);
				case MUSIC_VOLUME -> String.valueOf(musicVolume);
				case CURRENT_TIP_INDEX -> String.valueOf(currentTipIndex);
				case AUTO_ADD_CONSUMABLES -> String.valueOf(autoAddConsumables);
			};
		}

		return extras.get(var);
	}

	/*-------------------------------------------------------------------------*/
	public void setProperty(String var, String value)
	{
		if (var == null)
		{
			return;
		}

		Key key = keyFor(var);
		if (key != null)
		{
			switch (key)
			{
				case COMBAT_DELAY:
					combatDelay = Integer.parseInt(value);
					break;
				case PERSONALITY_CHATTINESS:
					personalityChattiness = Integer.parseInt(value);
					break;
				case MUSIC_VOLUME:
					musicVolume = Integer.parseInt(value);
					break;
				case CURRENT_TIP_INDEX:
					currentTipIndex = Integer.parseInt(value);
					break;
				case AUTO_ADD_CONSUMABLES:
					autoAddConsumables = Boolean.valueOf(value);
					break;
			}
		}
		else
		{
			extras.put(var, value);
		}
	}

	/*-------------------------------------------------------------------------*/
	private boolean isKnownKey(String var)
	{
		return keyFor(var) != null;
	}

	private static Key keyFor(String var)
	{
		for (Key key : Key.values())
		{
			if (key.getValue().equals(var))
			{
				return key;
			}
		}
		return null;
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

	public Map<String, String> getExtras()
	{
		return extras;
	}

	public void setExtras(Map<String, String> extras)
	{
		this.extras = extras != null ? new HashMap<>(extras) : new HashMap<>();
	}
}
