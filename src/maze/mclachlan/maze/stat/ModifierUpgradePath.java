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

package mclachlan.maze.stat;

import java.util.*;

/**
 *
 */
public class ModifierUpgradePath
{
	private List<ModifierUpgrade> upgradePath = new ArrayList<ModifierUpgrade>();

	/*-------------------------------------------------------------------------*/
	public void addUpgrade(StatModifier required, List<Stats.Modifier> upgradeOptions)
	{
		upgradePath.add(new ModifierUpgrade(new StatModifier(required), upgradeOptions));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The name of the upgraded weapon, or null if no upgrade applies.
	 */
	public String getUpgrade(PlayerCharacter pc, int newLevel)
	{
//		ItemUpgrade iu = upgradePath.get(fromItem);
//
//		if (iu == null)
//		{
//			return null;
//		}
//
//		if (!pc.meetsRequirements(iu.required))
//		{
//			return null;
//		}
//
//		return iu.toItem;

		return null; // todo
	}

	/*-------------------------------------------------------------------------*/
	private static class ModifierUpgrade
	{
		private StatModifier required;
		private List<Stats.Modifier> upgradeOptions;

		private ModifierUpgrade(StatModifier required, List<Stats.Modifier> upgradeOptions)
		{
			this.upgradeOptions = upgradeOptions;
			this.required = required;
		}
	}
}
