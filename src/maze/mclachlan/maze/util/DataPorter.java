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

import java.util.List;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Launcher;

/**
 *
 */
public class DataPorter
{
	public static void main(String[] args) throws Exception
	{
		Campaign campaign = null;
		List<Campaign> campaigns = Launcher.loadCampaigns();

		Loader loader = null;//new HardCodedLoader();
		Saver saver = new V1Saver();
		// haxor to get the arena campaign
		for (Campaign c : campaigns)
		{
			if (c.getName().equals("arena"))
			{
				saver.init(c);
				campaign = c;
				break;
			}
		}

		if (campaign == null)
		{
			throw new RuntimeException("Cannot find Arena campaign");
		}

		new Database(loader, saver);


		Loader test = new V1Loader();

		System.out.println("porting genders...");
		saver.saveGenders(loader.loadGenders());

		System.out.println("porting body parts...");
		saver.saveBodyParts(loader.loadBodyParts());

		System.out.println("porting races...");
		saver.saveRaces(loader.loadRaces());

		System.out.println("porting experience tables...");
		saver.saveExperienceTables(loader.loadExperienceTables());

		System.out.println("porting character classes...");
		saver.saveCharacterClasses(loader.loadCharacterClasses());

		System.out.println("porting attack types...");
		saver.saveAttackTypes(loader.loadAttackTypes());

		System.out.println("porting condition effects...");
		saver.saveConditionEffects(loader.loadConditionEffects());

		System.out.println("porting condition templates...");
		saver.saveConditionTemplates(loader.loadConditionTemplates());

		System.out.println("porting spell effects...");
		saver.saveSpellEffects(loader.loadSpellEffects());

		System.out.println("porting loot entries...");
		saver.saveLootEntries(loader.loadLootEntries());

		System.out.println("porting loot tables...");
		saver.saveLootTables(loader.loadLootTables());

		System.out.println("porting scripts...");
		saver.saveMazeScripts(loader.loadMazeScripts());

		System.out.println("porting spells...");
		saver.saveSpells(loader.loadSpells());

		System.out.println("porting player spell books...");
		saver.savePlayerSpellBooks(loader.loadPlayerSpellBooks());

		System.out.println("porting textures...");
		saver.saveMazeTextures(loader.loadMazeTextures());

		System.out.println("porting foe templates...");
		saver.saveFoeTemplates(loader.loadFoeTemplates());

		System.out.println("porting traps...");
		saver.saveTraps(loader.loadTraps());

		System.out.println("porting foe entries...");
		saver.saveFoeEntries(loader.loadFoeEntries());

		System.out.println("porting encounter tables...");
		saver.saveEncounterTables(loader.loadEncounterTables());

		System.out.println("porting npc faction templates...");
		saver.saveNpcFactionTemplates(loader.loadNpcFactionTemplates());

		System.out.println("porting npc templates...");
		saver.saveNpcTemplates(loader.loadNpcTemplates());

		System.out.println("porting wielding combos...");
		saver.saveWieldingCombos(loader.loadWieldingCombos());

		System.out.println("porting item templates...");
		saver.saveItemTemplates(loader.loadItemTemplates());

		System.out.println("porting zones...");
		List<String> zoneNames = loader.getZoneNames();
		for (String zoneName : zoneNames)
		{
			System.out.println(" - "+zoneName);
			saver.saveZone(loader.getZone(zoneName));
		}

		System.out.println("porting guild...");
		saver.saveCharacterGuild(loader.loadCharacterGuild());

		//----------------
		System.out.println("test..");
		test.init(campaign);
		System.out.println("genders: "+test.loadGenders().size());
		System.out.println("body parts: "+test.loadBodyParts().size());
		System.out.println("races: "+test.loadRaces().size());
		System.out.println("xp tables: "+test.loadExperienceTables().size());
		System.out.println("character classes: "+test.loadCharacterClasses().size());
		System.out.println("attack types: "+test.loadAttackTypes().size());
		System.out.println("condition effects: "+test.loadConditionEffects().size());
		System.out.println("condition templates: "+test.loadConditionTemplates().size());
		System.out.println("spell effects: "+test.loadSpellEffects().size());
		System.out.println("loot entries: "+test.loadLootEntries().size());
		System.out.println("loot tables: "+test.loadLootTables().size());
		System.out.println("scripts: "+test.loadMazeScripts().size());
		System.out.println("spells: "+test.loadSpells().size());
		System.out.println("player spell books: "+test.loadPlayerSpellBooks().size());
		System.out.println("maze textures: "+test.loadMazeTextures().size());
		System.out.println("foe templates: "+test.loadFoeTemplates().size());
		System.out.println("traps: "+test.loadTraps().size());
		System.out.println("foe entries: "+test.loadFoeEntries().size());
		System.out.println("encounter tables: "+test.loadEncounterTables().size());
		System.out.println("npc faction templates: "+test.loadNpcFactionTemplates().size());
		System.out.println("npc templates: "+test.loadNpcTemplates().size());
		System.out.println("wielding combos: "+test.loadWieldingCombos().size());
		System.out.println("item templates: "+test.loadItemTemplates().size());
		System.out.println("zones:");
		zoneNames = test.getZoneNames();
		for (String zoneName : zoneNames)
		{
			System.out.println(" - "+zoneName);
			test.getZone(zoneName);
		}
		System.out.println("guild: "+test.loadCharacterGuild().size());
	}
}
