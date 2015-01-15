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

import java.awt.Point;
import java.util.*;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Zone;

/**
 * A class for managing caches of items that the player leaves lying around.
 */
public class ItemCacheManager implements GameCache
{
	/**
	 * Key  : Zone name <br>
	 * Value: Map of zone coords to list of items left at those points
	 */
	private Map<String, Map<Point, List<Item>>> caches = new HashMap<String, Map<Point, List<Item>>>();

	private final Object mutex = new Object();

	private static ItemCacheManager instance = new ItemCacheManager();

	/*-------------------------------------------------------------------------*/
	public static ItemCacheManager getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Loads item caches state.
	 */
	public void loadGame(String name, Loader loader) throws Exception
	{
		this.caches = loader.loadItemCaches(name);
	}

	/*-------------------------------------------------------------------------*/
	public void saveGame(String saveGameName, Saver saver) throws Exception
	{
		saver.saveItemCaches(saveGameName, caches);
	}

	/*-------------------------------------------------------------------------*/
	public void endOfTurn(long turnNr)
	{
		updateItemCaches(turnNr);
	}

	/*-------------------------------------------------------------------------*/
	public void dropOnTile(Zone zone, Point tile, List<Item> items)
	{
		synchronized(mutex)
		{
			Map<Point, List<Item>> zoneMap = getZoneMap(zone);
			List<Item> itemList = zoneMap.get(tile);

			if (itemList == null)
			{
				itemList = new ArrayList<Item>();
				zoneMap.put(tile, itemList);
			}

			itemList.addAll(items);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<Item> getItemsOnTile(Zone zone, Point tile)
	{
		synchronized(mutex)
		{
			Map<Point, List<Item>> zoneMap = getZoneMap(zone);
			return zoneMap.get(tile);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void clearItemsOnTile(Zone zone, Point tile)
	{
		synchronized(mutex)
		{
			Map<Point, List<Item>> zoneMap = getZoneMap(zone);
			zoneMap.put(tile, new ArrayList<Item>());
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Called each turn, to expire items that are conceptually 'stolen' out of
	 * the caches.
	 */
	private void updateItemCaches(long turnNr)
	{
		Maze.log("updating item caches...");

		synchronized(mutex)
		{
			for (Map<Point, List<Item>> zoneMap : caches.values())
			{
				for (List<Item> itemList : zoneMap.values())
				{
					ListIterator<Item> li = itemList.listIterator();
					while (li.hasNext())
					{
						Item item = li.next();
						// give each non-critical items a 5% chance per turn to vanish
						// critical items never vanish
						if (!item.isQuestItem())
						{
							if (Dice.d100.roll() <= 5)
							{
								Maze.log(item.getName()+" is stolen");
								li.remove();
							}
						}
					}
				}
			}
		}

		Maze.log("finished updating item caches");
	}

	/*-------------------------------------------------------------------------*/
	private Map<Point, List<Item>> getZoneMap(Zone zone)
	{
		Map<Point, List<Item>> result = caches.get(zone.getName());

		if (result == null)
		{
			result = new HashMap<Point, List<Item>>();
			caches.put(zone.getName(), result);
		}

		return result;
	}
}
