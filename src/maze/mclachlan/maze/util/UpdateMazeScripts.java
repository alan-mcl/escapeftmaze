/*
 * Copyright (c) 2013 Alan McLachlan
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
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;

/**
 *
 */
public class UpdateMazeScripts
{
	static String[] scripts =
	{
		"Aenen City intro",
		"Aenen City to Tornado Mountain",
		"Aenen to Ichiba Domain South",
		"Aenen Outskirts solar array flava",
		"Aenen Outskirts clearing intro",
		"Aenen City to Outskirts",
		"Aenen Outskirts solar array flava",
		"Aenen Outskirts clearing intro",
		"Aenen Outskirts charred stump",
		"Aenen Outskirts charred stump search",
		"Aenen Outskirts clearing intro",
		"Aenen Outskirts solar array flava",
		"Aenen Outskirts solar array flava",
		"Aenen Outskirts clearing intro",
		"Aenen Outskirts clearing intro",
		"Aenen Outskirts clearing intro",
		"Aenen Outskirts clearing intro",
		"Aenen Outskirts clearing intro",
		"Aenen Outskirts solar array flava",
		"Aenen Outskirts solar array flava",
		"test script",
		"Encounter.preScript",
		"Encounter.postAppearanceScript",
		"Chest.preScript",
		"HiddenStuff.preScript",
		"HiddenStuff.contents",
		"Lever.preTransScript",
		"Lever.postTransScript",
		"ExecuteMazeScript.script",
		"ExecuteMazeScript.script",
		"Castle Fangorn castle intro",
		"Castle Fangorn intro",
		"Caves Of Ilast Brass Pillar",
		"Dalen intro",
		"Danaos Dungeon to Stygios Forest",
		"Danaos Dungeon Portal intro",
		"Danaos Castle feasting hall flava",
		"Danaos Castle feasting hall flava",
		"Danaos Castle feasting hall flava",
		"Danaos Castle intro",
		"Danaos Castle intro",
		"Danaos Castle intro",
		"Danoas Village intro",
		"Danaos to Ichiba Domain North",
		"Gatehouse malignant widow encounter",
		"Gatehouse angry swarms encounter",
		"Gatehouse pre widows cck",
		"Gatehouse block golem encounter",
		"Gatehouse hidden stuff 1 pre",
		"Gatehouse hidden stuff 1 contents",
		"Gatehouse supplies 1 eels leave",
		"Gatehouse supplies 1 cave eels skill test",
		"Gatehouse supplies 1 cave eels leave alone",
		"Gatehouse hidden pool contents",
		"Gatehouse hidden pool pass",
		"Lever.preTransScript",
		"Lever.postTransScript",
		"Gatehouse encounter 1 bats",
		"Gatehouse first room window",
		"Gatehouse welcome sign",
		"Gatehouse first room",
		"Gatehouse intro",
		"Gatehouse Bonebrood bones",
		"Gatehouse encounter 1 cruds",
		"Gatehouse bones2 hidden stuff",
		"Gatehouse supplies 1 ambercaps",
		"Gatehouse feral familiar encounter",
		"Gatehouse bones2 hidden stuff",
		"Gnoll Village intro",
		"Gnoll Village intro",
		"Gnoll Village intro",
		"Gnoll Village intro",
		"Gnoll Village intro",
		"Gnoll Village to Ichiba Depths",
		"Gnoll Village intro",
		"Gnoll Village intro",
		"Gnoll Village intro",
		"Gnoll Village intro",
		"Hianbian intro",
		"Hianbian intro",
		"Hianbian Yenluo encounter",
		"Ichiba Depths to City (gnolls)",
		"Ichiba City thieves guild cck doors",
		"Ichiba City gnome garden cck",
		"Ichiba City COC tear off paper slip",
		"Ichiba City COC tear off paper slip",
		"Ichiba City wanted signboard",
		"Ichiba City Imogens tower lvl1",
		"Ichiba City villa cck",
		"Ichiba City thieves guild cck doors",
		"Ichiba City thieves guild cck doors",
		"Ichiba City Imogen Door cck",
		"Ichiba City Temple of The Lady sign",
		"Ichiba City thieves guild cck doors",
		"Ichiba City cck",
		"Ichiba City Intro",
		"Ichiba City to Crossroad",
		"Ichiba Crossroad to City",
		"Ichiba Crossroad to Domain North",
		"Ichiba Crossroad toothy velox enc",
		"Ichiba Crossroad toothy velox enc",
		"Ichiba Crossroad omnifid glade",
		"Ichiba Crossroad omnifid glade",
		"Ichiba Crossroad omnifid glade",
		"Ichiba Crossroad omnifid glade",
		"Ichiba Crossroad intro",
		"Ichiba Crossroad pre mantis cck",
		"Ichiba Crossroad dun draco enc",
		"Ichiba Crossroad hawk moths enc",
		"Ichiba Crossroad to Domain South",
		"Ichiba Crossroad ulgoth enc",
		"Ichiba Domain North to Danaos",
		"Ichiba Domain North chest 1 pre",
		"Ichiba Crossroad intro",
		"Ichiba Domain North to Crossroad",
		"Ichiba Domain South to Crossroad",
		"Ichiba Crossroad intro",
		"Ichiba Domain South antenor bones",
		"Ichiba Domain South antenor loot",
		"Ichiba Domain South shrine",
		"Ichiba Domain South to Aenen",
		"Isle Of The Gate intro",
		"Isle Of The Gate ladder flava",
		"Isle Of The Gate to Ekirth's Tomb",
		"Plain Of Pillars intro",
		"Plain Of Pillars intro",
		"Ruins Of Hail hidden stuff 1",
		"Ruins Of Hail to Plain Of Pillars",
		"Ruins Of Hail to Writhing Mire",
		"Ruins Of Hail intro",
		"Ruins Of Hail to Tornado Mountain",
		"Ruins Of Hail intro",
		"Ruins Of Hail to Stygios Forest",
		"Ruins Of Hail to Hianbian",
		"Ruins Of Hail damla's bones hidden stuff",
		"Stygios Forest shrine to nergal",
		"Stygios Forest faerie circle",
		"Stygios Forest faerie circle",
		"Stygios Forest intro",
		"Stygios Forest to Ruins Of Hail",
		"Stygios Forest intro",
		"Stygios Forest to Danaos Dungeon",
		"Default Campaign Outtro",
		"Temple Of The Gate endgame cck",
		"Tornado Mountain intro",
		"Tornado Mountain to Ruins Of Hail",
		"Tornado Mountain dragon cave flava",
		"Tornado Mountain intro",
		"Tornado Mountain to Aenen City",
		"Writhing Mire intro",
		"",
	};

	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		V2Loader loader = new V2Loader();
		V2Saver saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		db.initImpls();
		db.initCaches(null);

		int count = 0;

		Map<String, MazeScript> set = db.getMazeScripts();
		System.out.println("set.size() = " + set.size());

		for (String s : scripts)
		{
			set.remove(s);
			count++;
		}

		System.out.println("set.size() = " + set.size());
		
		saver.saveMazeScripts(set);
		System.out.println("count = [" + count + "]");
	}
}
