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
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.condition.ConditionTemplate;

/**
 *
 */
public class QueryConditionTemplates
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

		queryOldSaves(db);
		queryResistances(db);
	}

	private static void queryResistances(Database db)
	{
		System.out.println("QueryConditionTemplates.queryResistances");

		int count = 0;
		Map<String, ConditionTemplate> map = db.getConditionTemplates();
		for (String s : map.keySet())
		{
			ConditionTemplate ct = map.get(s);

			if (ct.getStatModifier() != null &&
				(ct.getStatModifier().getModifier(Stats.Modifier.RESIST_FIRE) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifier.RESIST_WATER) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifier.RESIST_EARTH) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifier.RESIST_AIR) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifier.RESIST_MENTAL) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifier.RESIST_ENERGY) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifier.RESIST_BLUDGEONING) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifier.RESIST_PIERCING) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifier.RESIST_PIERCING) != 0))
			{
				count++;
				System.out.println(ct.getName());
			}
		}

		System.out.println("county = [" + count + "]");
	}

	private static void queryOldSaves(Database db)
	{
/*
		System.out.println("QueryConditionTemplates.queryOldSaves");

		int count = 0;
		Map<String, ConditionTemplate> map = db.getConditionTemplates();
		for (String s : map.keySet())
		{
			ConditionTemplate ct = map.get(s);

			if (ct.getStatModifier() != null &&
				(ct.getStatModifier().getModifier(Stats.Modifiers.SAVE_VS_BEGUILEMENT) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifiers.SAVE_VS_BLESSINGS) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifiers.SAVE_VS_CONJURATION) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifiers.SAVE_VS_CURSES) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifiers.SAVE_VS_EVOCATION) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifiers.SAVE_VS_ILLUSION) != 0 ||
				ct.getStatModifier().getModifier(Stats.Modifiers.SAVE_VS_TRANSMUTATION) != 0))
			{
				count++;
				System.out.println(ct.getName());
			}
		}

		System.out.println("county = [" + count + "]");
*/
	}
}