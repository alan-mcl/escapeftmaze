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

package mclachlan.maze.data.v1;

import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.npc.NpcInventoryTemplate;
import mclachlan.maze.stat.npc.NpcInventoryTemplateRow;
import mclachlan.maze.stat.npc.NpcInventoryTemplateRowItem;
import mclachlan.maze.stat.npc.NpcInventoryTemplateRowLootEntry;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1NpcInventoryTemplate
{
	public static final String ROW_SEP = "/";
	public static final String COL_SEP = ":";

	public static final int TYPE_ITEM = 0;
	public static final int TYPE_LOOT = 1;

	/*-------------------------------------------------------------------------*/
	public static String toString(NpcInventoryTemplate b)
	{
		if (b == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		for (NpcInventoryTemplateRow row : b.getRows())
		{
			s.append(rowToString(row));
			s.append(ROW_SEP);
		}

		if (s.length() > 0)
		{
			// trim last sep
			return s.toString().substring(0, s.length()-1);
		}
		else
		{
			return s.toString();
		}
	}

	/*-------------------------------------------------------------------------*/
	public static NpcInventoryTemplate fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		NpcInventoryTemplate result = new NpcInventoryTemplate();
		String[] strs = s.split(ROW_SEP);
		for (int i = 0; i < strs.length; i++)
		{
			result.add(rowFromString(strs[i]));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	static String rowToString(NpcInventoryTemplateRow r)
	{
		StringBuilder s = new StringBuilder();

		if (r instanceof NpcInventoryTemplateRowItem)
		{
			NpcInventoryTemplateRowItem row = (NpcInventoryTemplateRowItem)r;

			s.append(TYPE_ITEM);
			s.append(COL_SEP);
			s.append(row.getItemName());
			s.append(COL_SEP);
			s.append(row.getChanceOfSpawning());
			s.append(COL_SEP);
			s.append(row.getPartyLevelAppearing());
			s.append(COL_SEP);
			s.append(row.getMaxStocked());
			s.append(COL_SEP);
			s.append(row.getChanceOfVanishing());
			s.append(COL_SEP);
			s.append(V1Dice.toString(row.getStackSize()));

			return s.toString();
		}
		else if (r instanceof NpcInventoryTemplateRowLootEntry)
		{
			NpcInventoryTemplateRowLootEntry row = (NpcInventoryTemplateRowLootEntry)r;

			s.append(TYPE_LOOT);
			s.append(COL_SEP);
			s.append(row.getLootEntry());
			s.append(COL_SEP);
			s.append(row.getChanceOfSpawning());
			s.append(COL_SEP);
			s.append(row.getPartyLevelAppearing());
			s.append(COL_SEP);
			s.append(row.getMaxStocked());
			s.append(COL_SEP);
			s.append(row.getChanceOfVanishing());
			s.append(COL_SEP);
			s.append(V1Dice.toString(row.getItemsToSpawn()));

			return s.toString();
		}
		else
		{
			throw new MazeException(r.toString());
		}
	}

	/*-------------------------------------------------------------------------*/
	static NpcInventoryTemplateRow rowFromString(String s)
	{
		String[] strs = s.split(COL_SEP);

		int i=0;

		int type = Integer.valueOf(strs[i++]);

		if (type == TYPE_ITEM)
		{
			String itemName = strs[i++];
			int chanceOfSpawning = Integer.parseInt(strs[i++]);
			int partyLevelAppearing = Integer.parseInt(strs[i++]);
			int maxStocked = Integer.parseInt(strs[i++]);
			int chanceOfVanishing = Integer.parseInt(strs[i++]);
			Dice stackSize = null;
			if (strs.length > 6)
			{
				stackSize = V1Dice.fromString(strs[i++]);
			}

			return new NpcInventoryTemplateRowItem(
				itemName,
				chanceOfSpawning,
				partyLevelAppearing,
				maxStocked,
				chanceOfVanishing,
				stackSize);
		}
		else if (type == TYPE_LOOT)
		{
			String lootEntry = strs[i++];
			int chanceOfSpawning = Integer.parseInt(strs[i++]);
			int partyLevelAppearing = Integer.parseInt(strs[i++]);
			int maxStocked = Integer.parseInt(strs[i++]);
			int chanceOfVanishing = Integer.parseInt(strs[i++]);
			Dice itemsToSpawn = V1Dice.fromString(strs[i++]);

			return new NpcInventoryTemplateRowLootEntry(
				chanceOfSpawning,
				partyLevelAppearing,
				maxStocked,
				chanceOfVanishing,
				lootEntry,
				itemsToSpawn);
		}
		else
		{
			throw new MazeException(s);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * for testing only
	 */
	public static void main(String[] args)
	{

	}
}
