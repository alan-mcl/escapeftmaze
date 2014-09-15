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
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class QueryItems
{

	private static V1Saver saver;

	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		V1Loader loader = new V1Loader();
		saver = new V1Saver();
		Database db = new Database(loader, saver);
		Campaign campaign = Maze.getStubCampaign();
		loader.init(campaign);
		saver.init(campaign);

		Map<String, ItemTemplate> map = db.getItemTemplates();

		queryReservedModifiers(map);
//		sortOutExorcist(map);
//		sortOutBlackguard(map);
//		sortOutGadgeteer(map);
	}

	/*-------------------------------------------------------------------------*/
	static void queryReservedModifiers(Map<String, ItemTemplate> map) throws Exception
	{
		for (ItemTemplate t : map.values())
		{
			for (int i=0; i<99; i++)
			{
				if (t.getModifiers().getModifier("reserved"+i) != 0)
				{
					System.out.println(t.getName());
					System.out.println(t.getModifiers());
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	static void sortOutGadgeteer(Map<String, ItemTemplate> map) throws Exception
	{
		Set<String> thief = getItemsUsableBy("Thief", map);

		Set<String> gadgy = new HashSet<String>(thief);

		for (ItemTemplate t : map.values())
		{
			if (gadgy.contains(t.getName()))
			{
				System.out.println(t.getName());
			}
		}

		reassignUsability(map, gadgy, "Gadgeteer");
	}

	/*-------------------------------------------------------------------------*/
	static void sortOutBlackguard(Map<String, ItemTemplate> map) throws Exception
	{
		Set<String> hero = getItemsUsableBy("Hero", map);

		Set<String> bg = new HashSet<String>(hero);

		for (ItemTemplate t : map.values())
		{
			if (bg.contains(t.getName()))
			{
				System.out.println(t.getName());
			}
		}

//		reassignUsability(map, bg, "Blackguard");
	}

	/*-------------------------------------------------------------------------*/
	static void sortOutExorcist(Map<String, ItemTemplate> map) throws Exception
	{
		Set<String> adept = getItemsUsableBy("Adept", map);
		Set<String> thief = getItemsUsableBy("Thief", map);

		Set<String> exorcist = new HashSet<String>(adept);
		exorcist.addAll(thief);

		for (ItemTemplate t : map.values())
		{
			if (thief.contains(t.getName()) && !adept.contains(t.getName()))
			{
				System.out.println(t.getName());
			}
		}

//		reassignUsability(map, exorcist, "Exorcist");
	}

	/*-------------------------------------------------------------------------*/
	private static void reassignUsability(Map<String, ItemTemplate> map,
		Set<String> allowed, String className) throws Exception
	{
		for (String s : map.keySet())
		{
			ItemTemplate it = map.get(s);

			Set<String> usability = it.getUsableByCharacterClass();

			if (allowed.contains(s))
			{
				usability.add(className);
			}
			else
			{
				usability.remove(className);
			}

			it.setUsableByCharacterClass(usability);
		}

		saver.saveItemTemplates(map);
	}

	/*-------------------------------------------------------------------------*/
	private static Set<String> getItemsUsableBy(
		String className,
		Map<String, ItemTemplate> map)
	{
		Set<String> result = new HashSet<String>();

		for (String s : map.keySet())
		{
			ItemTemplate it = map.get(s);

			Set<String> set = it.getUsableByCharacterClass();

			if (set.contains(className))
			{
				result.add(s);
			}
		}

		return result;
	}

}