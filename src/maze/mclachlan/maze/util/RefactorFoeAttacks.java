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
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.map.SingleItemLootEntry;
import mclachlan.maze.stat.*;

/**
 *
 */
public class RefactorFoeAttacks
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		V1Loader loader = new V1Loader();
		V1Saver saver = new V1Saver();
		Database db = new Database(loader, saver);
		Campaign campaign = Maze.getStubCampaign();
		loader.init(campaign);
		saver.init(campaign);

		int count = 0;

		// get all the items for searching
		Map<String, ItemTemplate> searchableItems = new HashMap<String, ItemTemplate>();
		Map<String, ItemTemplate> items = db.getItemTemplates();
		for (Map.Entry<String, ItemTemplate> entry : items.entrySet())
		{
			searchableItems.put(entry.getKey().toLowerCase(), entry.getValue());
		}

		// get all natural weapons, for adding
		Map<String,NaturalWeapon> naturalWeapons = db.getNaturalWeapons();

		// get all loot tables, for adding and updating
		Map<String,LootTable> lootTables = db.getLootTables();

		// key: foe name; value: natural weapon to add
		Map<String, List<NaturalWeapon>> naturalWeaponsToAdd = new HashMap<String, List<NaturalWeapon>>();

		// key: foe name; value: item template name to add
		Map<String, List<ItemTemplate>> itemsToAdd = new HashMap<String, List<ItemTemplate>>();

		// loop through all foe attacks, converting to natural weapons or
		// items as needed
		Map<String, FoeTemplate> foes = db.getFoeTemplates();
		for (String s : foes.keySet())
		{
			FoeTemplate foe = foes.get(s);
			String foeName = foe.getName();

			/*for (FoeAttack fa : foe.getAttacks().getItems())
			{
				if (fa.getType() == FoeAttack.Type.MELEE_ATTACK ||
					fa.getType() == FoeAttack.Type.RANGED_ATTACK)
				{
					List<String> tokens = getTokens(fa.getDescription());

					boolean foundItem = false;
					for (String token : tokens)
					{
						if (searchableItems.containsKey(token))
						{
							// this is a foe attack with an item.
							List<ItemTemplate> i = itemsToAdd.get(foeName);
							if (i == null)
							{
								i = new ArrayList<ItemTemplate>();
								itemsToAdd.put(foeName, i);
							}
							ItemTemplate itemTemplate = searchableItems.get(token);
							if (!i.contains(itemTemplate))
							{
								i.add(itemTemplate);

								if (itemTemplate.getAmmoType() == ItemTemplate.AmmoType.ARROW)
								{
									// add a long bow for these guys
									i.add(items.get("Long Bow"));
								}
								else if (itemTemplate.getAmmoType() == ItemTemplate.AmmoType.BOLT)
								{
									i.add(items.get("Light Crossbow"));
								}
								else if (itemTemplate.getAmmoType() == ItemTemplate.AmmoType.STONE)
								{
									i.add(items.get("Sling"));
								}
							}
							foundItem = true;
							break;
						}
					}

					if (!foundItem)
					{
						// no item found, this must be a natural weapon
						List<NaturalWeapon> i = naturalWeaponsToAdd.get(foeName);
						if (i == null)
						{
							i = new ArrayList<NaturalWeapon>();
							naturalWeaponsToAdd.put(foeName, i);
						}
						i.add(
							new NaturalWeapon(
								fa.getName(),
								fa.getDescription(),
								fa.isRanged(),
								fa.getDamage(),
								fa.getDefaultDamageType(),
								fa.getModifiers(),
								fa.getMinRange(),
								fa.getMaxRange(),
								fa.getSpellEffects(),
								fa.getSpellEffectLevel(),
								fa.getAttacks(),
								fa.slaysFoeType(),
								fa.getAttackScript()));
					}
				}
			}

			foe.setAttacks(null);*/
		}

		// apply the new data
		System.out.println("NATURAL WEAPONS TO ADD");
		for (Map.Entry<String, List<NaturalWeapon>> entry: naturalWeaponsToAdd.entrySet())
		{
			FoeTemplate foeTemplate = foes.get(entry.getKey());

			for (NaturalWeapon nw : entry.getValue())
			{
				System.out.println(foeTemplate.getName()+": "+nw.getName());
				naturalWeapons.put(nw.getName(), nw);
				List<String> fnw = foeTemplate.getNaturalWeapons();
				if (fnw == null)
				{
					fnw = new ArrayList<String>();
					foeTemplate.setNaturalWeapons(fnw);
				}
				fnw.add(nw.getName());
			}
		}

		System.out.println();

		System.out.println("ITEMS ADDED TO LOOT ENTRIES");
		for (Map.Entry<String, List<ItemTemplate>> entry : itemsToAdd.entrySet())
		{
			FoeTemplate foeTemplate = foes.get(entry.getKey());

			for (ItemTemplate it : entry.getValue())
			{
				System.out.println(foeTemplate.getName()+": "+it.getName());

				GroupOfPossibilities<ILootEntry> gop = new GroupOfPossibilities<ILootEntry>();
				String lootTableName = foeTemplate.getName()+".loot";
				lootTableName = lootTableName.replaceAll(" ",".");
				LootTable result = new LootTable(lootTableName, gop);

				LootTable loot = foeTemplate.getLoot();
				if (loot != null && !loot.getLootEntries().isEmpty())
				{
					// add whatever is there already to this foes loot table
					gop.addAll(loot.getLootEntries());
				}

				gop.add(new SingleItemLootEntry(it.getName()), 100);

				foeTemplate.setLoot(result);
				lootTables.put(result.getName(), result);
			}
		}

		saver.saveFoeTemplates(foes);
		saver.saveLootTables(lootTables);
		saver.saveNaturalWeapons(naturalWeapons);
	}

	/*-------------------------------------------------------------------------*/
	private static List<String> getTokens(String description)
	{
		List<String> result = new ArrayList<String>();

		// add all single tokens
		String[] split = description.split(" ");
		result.addAll(Arrays.asList(split));

		// prepend combo tokens (to ensure that they match first if possible)
		for (int i = 0; i < split.length-1; i++)
		{
			result.add(0, split[i] + " " + split[i+1]);
		}

		return result;
	}
}