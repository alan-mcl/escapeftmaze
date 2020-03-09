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
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.LootEntryRow;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.stat.npc.NpcInventoryTemplate;
import mclachlan.maze.stat.npc.NpcInventoryTemplateRow;
import mclachlan.maze.stat.npc.NpcInventoryTemplateRowItem;
import mclachlan.maze.stat.npc.NpcTemplate;

/**
 *
 */
public class Capitaliser
{
	public static void main(String[] args) throws Exception
	{
		V1Loader loader = new V1Loader();
		V1Saver saver = new V1Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		Map<String, ItemTemplate> itemTemplateMap = db.getItemTemplates();
		Map<String,ItemTemplate> items = new HashMap<String, ItemTemplate>(itemTemplateMap);
		itemTemplateMap.clear();

		for (String s : items.keySet())
		{
			ItemTemplate item = items.get(s);

/*
			PercentageTable<ItemEnchantment> enchantments = item.getEnchantments();

			if (enchantments != null)
			{
				System.out.println(item.getName());
				List<ItemEnchantment> list = enchantments.getItems();

				for (ItemEnchantment e : list)
				{
					e.setPrefix(capitalise(e.getPrefix()));
					e.setSuffix(capitalise(e.getSuffix()));
				}
			}
*/

/*
			String oldName = item.getName();
			String newName = capitalise(oldName);
			item.setName(newName);

			String newPluralName = capitalise(item.getPluralName());
			item.setPluralName(newPluralName);
			
			System.out.println(oldName+" - "+item.getName());

			itemTemplateMap.put(newName, item);
*/
		}

//		Map<String, CharacterClass> heroicClasses = db.getHeroicClasses();
//		for (String s : heroicClasses.keySet())
//		{
//			CharacterClass cc = heroicClasses.get(s);
//
//			List<StartingItems> itemsList = cc.getStartingItems();
//			for (int i = 0; i < itemsList.size(); i++)
//			{
//				StartingItems startingItems = itemsList.get(i);
//				startingItems.
//			}
//		}

		// loot entries
//		Map<String, LootEntry> lootEntries = capitaliseLootEntries(db);

		// npc inventory templates
//		Map<String, NpcTemplate> npcTemplates = capitaliseNpcTemplates(db);

		// persist data
		saver.saveItemTemplates(items);
//		saver.saveLootEntries(lootEntries);
//		saver.saveNpcTemplates(npcTemplates);
	}

	/*-------------------------------------------------------------------------*/
	private static Map<String, NpcTemplate> capitaliseNpcTemplates(Database db)
	{
		Map<String, NpcTemplate> npcTemplates = db.getNpcTemplates();
		for (String s : npcTemplates.keySet())
		{
			NpcTemplate nt = npcTemplates.get(s);
			NpcInventoryTemplate invTemplate = nt.getInventoryTemplate();
			if (invTemplate == null)
			{
				continue;
			}
			List<NpcInventoryTemplateRow> rowList = invTemplate.getRows();
			for (NpcInventoryTemplateRow row : rowList)
			{
				if (row instanceof NpcInventoryTemplateRowItem)
				{
					String newName = capitalise(((NpcInventoryTemplateRowItem)row).getItemName());
					((NpcInventoryTemplateRowItem)row).setItemName(newName);
				}
			}
		}
		return npcTemplates;
	}

	/*-------------------------------------------------------------------------*/
	private static Map<String, LootEntry> capitaliseLootEntries(Database db)
	{
		Map<String, LootEntry> lootEntries = db.getLootEntries();
		for (String s : lootEntries.keySet())
		{
			LootEntry le = lootEntries.get(s);
			PercentageTable<LootEntryRow> pt = le.getContains();
			List<LootEntryRow> ptItems = pt.getItems();
			for (LootEntryRow row : ptItems)
			{
				String newName = capitalise(row.getItemName());
				row.setItemName(newName);
			}
		}
		return lootEntries;
	}

	/*-------------------------------------------------------------------------*/
	static String capitalise(String s)
	{
		if (s == null)
		{
			return s;
		}

		String result = "";

		String[] words = s.split("\\s");
		for (String w : words)
		{
			result += capitaliseWord(w) + " ";
      }

		result = result.trim();

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static String capitaliseWord(String s)
	{
        if (s.length() == 0)
		  {
			  return s;
		  }
		
		  return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}