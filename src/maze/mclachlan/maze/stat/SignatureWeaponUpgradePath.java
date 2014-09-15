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

package mclachlan.maze.stat;

import java.util.*;

/**
 *
 */
public class SignatureWeaponUpgradePath
{
	private Map<String, ItemUpgrade> upgradePath = new HashMap<String, ItemUpgrade>();

	/*-------------------------------------------------------------------------*/
	public void addUpgrade(String fromItem, String toItem, StatModifier required)
	{
		upgradePath.put(fromItem, new ItemUpgrade(fromItem, toItem, new StatModifier(required)));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The name of the upgraded weapon, or null if no upgrade applies.
	 */
	public String getUpgrade(String fromItem, PlayerCharacter pc)
	{
		ItemUpgrade iu = upgradePath.get(fromItem);

		if (iu == null)
		{
			return null;
		}

		if (!pc.meetsRequirements(iu.required))
		{
			return null;
		}

		return iu.toItem;
	}

	/*-------------------------------------------------------------------------*/
	private static class ItemUpgrade
	{
		String fromItem, toItem;
		StatModifier required;

		private ItemUpgrade(String fromItem, String toItem, StatModifier required)
		{
			this.fromItem = fromItem;
			this.toItem = toItem;
			this.required = required;
		}
	}
}
