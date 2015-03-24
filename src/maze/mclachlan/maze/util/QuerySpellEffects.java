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
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.SpellEffect;

/**
 *
 */
public class QuerySpellEffects
{
	public static void main(String[] args) throws Exception
	{
		V1Loader loader = new V1Loader();
		V1Saver saver = new V1Saver();
		Database db = new Database(loader, saver);
		Campaign campaign = Maze.getStubCampaign();
		loader.init(campaign);
		saver.init(campaign);

//		countSpellEffectTypes(db);
		findObsoleteSpellResults(db);
	}

	/*-------------------------------------------------------------------------*/
	private static void findObsoleteSpellResults(Database db)
	{
		Map<String, SpellEffect> map = db.getSpellEffects();

		for (SpellEffect se : map.values())
		{
//			if (se.getUnsavedResult() instanceof CasterFatigueSpellResult ||
//				se.getSavedResult() instanceof CasterFatigueSpellResult)
//			{
//				System.out.println(se.getName());
//			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void countSpellEffectTypes(Database db)
	{
		Map<String, SpellEffect> map = db.getSpellEffects();

		countType(map, MagicSys.SpellEffectType.NONE);
		countType(map, MagicSys.SpellEffectType.FIRE);
		countType(map, MagicSys.SpellEffectType.WATER);
		countType(map, MagicSys.SpellEffectType.EARTH);
		countType(map, MagicSys.SpellEffectType.AIR);
		countType(map, MagicSys.SpellEffectType.MENTAL);
		countType(map, MagicSys.SpellEffectType.ENERGY);
	}

	/*-------------------------------------------------------------------------*/
	private static void countType(Map<String, SpellEffect> map, MagicSys.SpellEffectType type)
	{
		System.out.println("-----COUNTING "+ MagicSys.SpellEffectType.describe(type));
		int count = 0;

		for (String s : map.keySet())
		{
			SpellEffect se = map.get(s);
			if (se.getType() == type)
			{
				System.out.println(se.getName());
				count++;
			}
		}

		System.out.println("count = [" + count + "]");
	}
}