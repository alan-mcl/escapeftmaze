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

package mclachlan.maze.util;

import java.util.*;
import mclachlan.maze.stat.*;

/**
 * Just for suggestions.
 */
public class ItemCostCalculator
{
	/*-------------------------------------------------------------------------*/
	public static int calcItemBaseCost(ItemTemplate itemTemplate)
	{
		Item item = itemTemplate.create();

		if (item.isWeapon())
		{
			return calcWeaponCost(item);
		}
		else if (item.isArmour())
		{
			return calcArmourCost(item);
		}
		else if (item.isShield())
		{
			return calcShieldCost(item);
		}
		else if (item.isAmmo())
		{
			return calcAmmoCost(item);
		}
		else
		{
			return calcGenericItemCost(item);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static int calcGenericItemCost(Item item)
	{
		// todo
		return 100;
	}

	/*-------------------------------------------------------------------------*/
	private static int calcAmmoCost(Item ammo)
	{
		// todo
		return 1;
	}

	/*-------------------------------------------------------------------------*/
	private static int calcShieldCost(Item shield)
	{
		// todo
		return 100;
	}

	/*-------------------------------------------------------------------------*/
	private static int calcArmourCost(Item armour)
	{
		// todo
		return 100;
	}

	/*-------------------------------------------------------------------------*/
	private static int calcWeaponCost(Item weapon)
	{
		int result = 0;

		// for base damage
		result += weapon.getDamage().getMinPossible()*5;
		result += weapon.getDamage().getMaxPossible()*15;

		// for modifiers
		result += weapon.getToHit() * 20;
		result += weapon.getToPenetrate() * 20;

		// for max range
		result += (weapon.getMaxRange()-1)*10;

		// for min range
		result -= (weapon.getMinRange()-1)*10;

		// disadvantages
		result -= weapon.isTwoHanded() ? 20 : 0;
		result -= weapon.isRanged() ? 5 : 0;
		result -= (weapon.getAttackTypes().length-1)*5;

		// invoked effects?
		if (weapon.getInvokedSpell() != null)
		{
			// todo, cost smarter
			result += weapon.getInvokedSpellLevel()*100;
		}

		// modifiers
		Map<Stats.Modifier, Integer> modifiers = weapon.getModifiers().getModifiers();
		for (Stats.Modifier modifier : modifiers.keySet())
		{
			result += modifiers.get(modifier) * 10;
		}

		return result;
	}
}
