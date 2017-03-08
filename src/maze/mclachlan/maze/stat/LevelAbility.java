/*
 * Copyright (c) 2014 Alan McLachlan
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

package mclachlan.maze.stat;

import java.util.*;

/**
 * Represents an ability gained at a certain level. Can compose stat modifiers,
 * a spell-like ability, or both.
 */
public abstract class LevelAbility
{
	/**
	 * Abilities with a given key are superseded by abilities with the same key
	 * from higher character levels.
	 */
	private String key;

	/**
	 * The display name of this level ability. A key from the GAMESYS resource.
	 */
	private String displayName;

	/**
	 * The description of this level ability. A key from the GAMESYS resource.
	 */
	private String description;

	/**
	 * The name of the character class to which this ability belongs
	 */
	private String characterClass;

	/*-------------------------------------------------------------------------*/

	public LevelAbility()
	{
		// to support custom implementations that need to be instantiated via reflection
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param key
	 * 	Unique key for the LA, determine which other LAs it supersedes
	 * @param displayName
	 * 	The display name of this level ability. A key from the GAMESYS resource.
	 * @param description
	 * 	The description of this level ability. A key from the GAMESYS resource.
	 */
	public LevelAbility(
		String key,
		String displayName,
		String description)
	{
		this.key = key;
		this.displayName = displayName;
		this.description = description;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The display name of this level ability. A key from the GAMESYS resource.
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * @return
	 * 	The description of this level ability. A key from the GAMESYS resource.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param displayName
	 * 	The display name of this level ability. A key from the GAMESYS resource.
	 */
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	/**
	 * @param description
	 * 	The description of this level ability. A key from the GAMESYS resource.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getCharacterClass()
	{
		return characterClass;
	}

	public void setCharacterClass(String characterClass)
	{
		this.characterClass = characterClass;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	Any special ability added by this LA, or null if none.
	 */
	public SpellLikeAbility getAbility()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	Any stats modified by this LA. Should not return null.
	 */
	public StatModifier getModifier()
	{
		return new StatModifier();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	Any banner modifiers from this LA. Should not return null.
	 */
	public StatModifier getBannerModifier()
	{
		return new StatModifier();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	Any natural weapon provided by this LA, or null if none
	 */
	public NaturalWeapon getNaturalWeapon()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public Object[] getDisplayArgs()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	Any type descriptors that this LA adds to an actor
	 */
	public Collection<TypeDescriptor> getTypeDescriptors()
	{
		return null;
	}
}

