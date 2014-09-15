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
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1NpcInventoryTemplate;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.npc.NpcInventoryTemplate;

/**
 * Generate a vendors inventory
 */
public class GenInv
{
	public static void main(String[] args) throws Exception
	{
		V1Loader loader = new V1Loader();
		V1Saver saver = new V1Saver();
		Database db = new Database(loader, saver);
		Campaign campaign = Maze.getStubCampaign();
		loader.init(campaign);
		saver.init(campaign);

		int count = 0;

		NpcInventoryTemplate t = new NpcInventoryTemplate();

		Map<String,ItemTemplate> items = db.getItemTemplates();

		for (String s : items.keySet())
		{
			ItemTemplate item = items.get(s);

			int clvl = getDefaultPartyLvlAppearing(item);
			switch (item.getType())
			{
				case Item.Type.SHORT_WEAPON:
				case Item.Type.EXTENDED_WEAPON:
				case Item.Type.THROWN_WEAPON:
				case Item.Type.RANGED_WEAPON:
				case Item.Type.AMMUNITION:
				case Item.Type.SHIELD:
				case Item.Type.HEAD_ARMOUR:
				case Item.Type.TORSO_ARMOUR:
				case Item.Type.LEG_ARMOUR:
				case Item.Type.GLOVES:
				case Item.Type.BOOTS:
					t.add(item.getName(), 10, clvl, 1, 20, Dice.d1);
			}
		}

		String s = V1NpcInventoryTemplate.toString(t);
		System.out.println(s);
	}

	/*-------------------------------------------------------------------------*/
	public static int getDefaultPartyLvlAppearing(ItemTemplate item)
	{
		int clvl = 0;

		switch (item.getType())
		{
			case Item.Type.SHORT_WEAPON:
			case Item.Type.EXTENDED_WEAPON:
			case Item.Type.THROWN_WEAPON:
			case Item.Type.RANGED_WEAPON:
			case Item.Type.AMMUNITION:
				clvl = item.getDamage().getMaxPossible()/2+1;
				break;
			case Item.Type.SHIELD:
			case Item.Type.HEAD_ARMOUR:
			case Item.Type.TORSO_ARMOUR:
			case Item.Type.LEG_ARMOUR:
			case Item.Type.GLOVES:
			case Item.Type.BOOTS:
				clvl = item.getDamagePrevention();
				break;
		}
		return clvl;
	}
}