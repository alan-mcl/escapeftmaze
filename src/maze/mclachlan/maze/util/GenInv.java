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
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.npc.NpcInventoryTemplate;

import static mclachlan.maze.stat.ItemTemplate.Type;

/**
 * Generate a vendors inventory
 */
public class GenInv
{
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V2Loader();
		Saver saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		int count = 0;

		NpcInventoryTemplate t = new NpcInventoryTemplate();

		Map<String,ItemTemplate> items = db.getItemTemplates();

		for (String s : items.keySet())
		{
			ItemTemplate item = items.get(s);

			int clvl = getDefaultPartyLvlAppearing(item);
			switch (item.getType())
			{
				case Type.SHORT_WEAPON:
				case Type.EXTENDED_WEAPON:
				case Type.THROWN_WEAPON:
				case Type.RANGED_WEAPON:
				case Type.AMMUNITION:
				case Type.SHIELD:
				case Type.HEAD_ARMOUR:
				case Type.TORSO_ARMOUR:
				case Type.LEG_ARMOUR:
				case Type.GLOVES:
				case Type.BOOTS:
					t.add(item.getName(), 10, clvl, 1, 20, Dice.d1);
			}
		}

		String s = t.toString();
		System.out.println(s);
	}

	/*-------------------------------------------------------------------------*/
	public static int getDefaultPartyLvlAppearing(ItemTemplate item)
	{
		int clvl = 0;

		switch (item.getType())
		{
			case Type.SHORT_WEAPON:
			case Type.EXTENDED_WEAPON:
			case Type.THROWN_WEAPON:
			case Type.RANGED_WEAPON:
			case Type.AMMUNITION:
				clvl = item.getDamage().getMaxPossible()/2+1;
				break;
			case Type.SHIELD:
			case Type.HEAD_ARMOUR:
			case Type.TORSO_ARMOUR:
			case Type.LEG_ARMOUR:
			case Type.GLOVES:
			case Type.BOOTS:
				clvl = item.getDamagePrevention();
				break;
		}
		return clvl;
	}
}