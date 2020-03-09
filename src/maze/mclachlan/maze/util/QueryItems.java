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
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.Stats;

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
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		Map<String, ItemTemplate> map = db.getItemTemplates();

		sortOutGnome(map);
//		queryReservedModifiers(map);
//		sortOutExorcist(map);
//		sortOutBlackguard(map);
//		sortOutGadgeteer(map);
	}

	/*-------------------------------------------------------------------------*/
	static void queryReservedModifiers(Map<Stats.Modifier, ItemTemplate> map) throws Exception
	{
//		for (ItemTemplate t : map.values())
//		{
//			for (int i=0; i<99; i++)
//			{
//				if (t.getModifiers().getModifier("reserved"+i) != 0)
//				{
//					System.out.println(t.getName());
//					System.out.println(t.getModifiers());
//				}
//			}
//		}
	}

	/*-------------------------------------------------------------------------*/
	static void sortOutGnome(Map<String, ItemTemplate> map) throws Exception
	{
		Set<String> other = getItemsUsableByRace("Dwarf", map);

		Set<String> thisOne = new HashSet<String>(other);

		for (ItemTemplate t : map.values())
		{
			if (thisOne.contains(t.getName()))
			{
				System.out.println(t.getName());
			}
		}

		reassignRaceUsability(map, thisOne, "Gnome");
	}

	/*-------------------------------------------------------------------------*/
	static void sortOutGadgeteer(Map<String, ItemTemplate> map) throws Exception
	{
		Set<String> thief = getItemsUsableByClass("Thief", map);

		Set<String> gadgy = new HashSet<String>(thief);

		for (ItemTemplate t : map.values())
		{
			if (gadgy.contains(t.getName()))
			{
				System.out.println(t.getName());
			}
		}

		reassignClassUsability(map, gadgy, "Gadgeteer");
	}

	/*-------------------------------------------------------------------------*/
	static void sortOutBlackguard(Map<String, ItemTemplate> map) throws Exception
	{
		Set<String> hero = getItemsUsableByClass("Hero", map);

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
		Set<String> adept = getItemsUsableByClass("Adept", map);
		Set<String> thief = getItemsUsableByClass("Thief", map);

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
	private static void reassignClassUsability(Map<String, ItemTemplate> map,
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
	private static void reassignRaceUsability(Map<String, ItemTemplate> map,
		Set<String> allowed, String raceName) throws Exception
	{
		for (String s : map.keySet())
		{
			ItemTemplate it = map.get(s);

			Set<String> usability = it.getUsableByRace();

			if (allowed.contains(s))
			{
				usability.add(raceName);
			}
			else
			{
				usability.remove(raceName);
			}

			it.setUsableByRace(usability);
		}

		saver.saveItemTemplates(map);
	}

	/*-------------------------------------------------------------------------*/
	private static Set<String> getItemsUsableByClass(
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

	/*-------------------------------------------------------------------------*/
	private static Set<String> getItemsUsableByRace(
		String className,
		Map<String, ItemTemplate> map)
	{
		Set<String> result = new HashSet<String>();

		for (String s : map.keySet())
		{
			ItemTemplate it = map.get(s);

			Set<String> set = it.getUsableByRace();

			if (set.contains(className))
			{
				result.add(s);
			}
		}

		return result;
	}

}