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
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Campaign;

/**
 *
 */
public class DataPorter
{
	public static void main(String[] args) throws Exception
	{
		Campaign campaign = null;
		List<Campaign> campaigns = new ArrayList<>(Database.getCampaigns().values());

		Loader v1Loader = new V1Loader();
		Saver v2Saver = new V2Saver();


		// haxor to get the arena campaign
		for (Campaign c : campaigns)
		{
			if (c.getName().equals("default"))
			{
				campaign = c;
				break;
			}
		}

		new Database(v1Loader, v2Saver, campaign);
		v2Saver.init(campaign);

		if (campaign == null)
		{
			throw new RuntimeException("Cannot find Arena campaign");
		}


		Loader v2Loader = new V2Loader();

		System.out.println("porting genders...");
		v2Saver.saveGenders(v1Loader.loadGenders());

		System.out.println("porting body parts...");
		v2Saver.saveBodyParts(v1Loader.loadBodyParts());

		System.out.println("porting races...");
		v2Saver.saveRaces(v1Loader.loadRaces());

		System.out.println("porting experience tables...");
		v2Saver.saveExperienceTables(v1Loader.loadExperienceTables());

		System.out.println("porting character classes...");
		v2Saver.saveCharacterClasses(v1Loader.loadCharacterClasses());

		System.out.println("porting attack types...");
		v2Saver.saveAttackTypes(v1Loader.loadAttackTypes());

		System.out.println("porting condition effects...");
		v2Saver.saveConditionEffects(v1Loader.loadConditionEffects());

		System.out.println("porting condition templates...");
		v2Saver.saveConditionTemplates(v1Loader.loadConditionTemplates());

		System.out.println("porting spell effects...");
		v2Saver.saveSpellEffects(v1Loader.loadSpellEffects());

		System.out.println("porting loot entries...");
		v2Saver.saveLootEntries(v1Loader.loadLootEntries());

		System.out.println("porting loot tables...");
		v2Saver.saveLootTables(v1Loader.loadLootTables());

		System.out.println("porting scripts...");
		v2Saver.saveMazeScripts(v1Loader.loadMazeScripts());

		System.out.println("porting spells...");
		v2Saver.saveSpells(v1Loader.loadSpells());

		System.out.println("porting player spell books...");
		v2Saver.savePlayerSpellBooks(v1Loader.loadPlayerSpellBooks());

		System.out.println("porting textures...");
		v2Saver.saveMazeTextures(v1Loader.loadMazeTextures());

		System.out.println("porting foe templates...");
		v2Saver.saveFoeTemplates(v1Loader.loadFoeTemplates());

		System.out.println("porting traps...");
		v2Saver.saveTraps(v1Loader.loadTraps());

		System.out.println("porting foe entries...");
		v2Saver.saveFoeEntries(v1Loader.loadFoeEntries());

		System.out.println("porting encounter tables...");
		v2Saver.saveEncounterTables(v1Loader.loadEncounterTables());

		System.out.println("porting npc faction templates...");
		v2Saver.saveNpcFactionTemplates(v1Loader.loadNpcFactionTemplates());

		System.out.println("porting npc templates...");
		v2Saver.saveNpcTemplates(v1Loader.loadNpcTemplates());

		System.out.println("porting wielding combos...");
		v2Saver.saveWieldingCombos(v1Loader.loadWieldingCombos());

		System.out.println("porting item templates...");
		v2Saver.saveItemTemplates(v1Loader.loadItemTemplates());

		System.out.println("porting difficulty levels...");
		v2Saver.saveDifficultyLevels(v1Loader.loadDifficultyLevels());

		System.out.println("porting craft recipes...");
		v2Saver.saveCraftRecipes(v1Loader.loadCraftRecipes());

		System.out.println("porting item enchantments...");
		v2Saver.saveItemEnchantments(v1Loader.loadItemEnchantments());

		System.out.println("porting natural weapons...");
		v2Saver.saveNaturalWeapons(v1Loader.loadNaturalWeapons());

		System.out.println("porting starting kits...");
		v2Saver.saveStartingKits(v1Loader.loadStartingKits());

		System.out.println("porting personalities...");
		v2Saver.savePersonalities(v1Loader.loadPersonalities());

		System.out.println("porting foe types...");
		v2Saver.saveFoeTypes(v1Loader.loadFoeTypes());

//		System.out.println("porting guild...");
//		v2Saver.saveCharacterGuild(v1Loader.loadCharacterGuild());

//		System.out.println("porting zones...");
//		List<String> zoneNames = v1Loader.getZoneNames();
//		for (String zoneName : zoneNames)
//		{
//			System.out.println(" - "+zoneName);
//			v2Saver.saveZone(v1Loader.getZone(zoneName));
//		}


		//----------------
		System.out.println("v2Loader..");
		v2Loader.init(campaign);
		System.out.println("genders: "+v2Loader.loadGenders().size());
		assertEquals(v1Loader.loadGenders(), v2Loader.loadGenders());

		System.out.println("body parts: "+v2Loader.loadBodyParts().size());
		assertEquals(v1Loader.loadBodyParts(), v2Loader.loadBodyParts());

		System.out.println("races: "+v2Loader.loadRaces().size());
		assertEquals(v1Loader.loadRaces(), v2Loader.loadRaces());

		System.out.println("xp tables: "+v2Loader.loadExperienceTables().size());
		assertEquals(v1Loader.loadExperienceTables(), v2Loader.loadExperienceTables());

		System.out.println("character classes: "+v2Loader.loadCharacterClasses().size());
		assertEquals(v1Loader.loadCharacterClasses(), v2Loader.loadCharacterClasses());

		System.out.println("attack types: "+v2Loader.loadAttackTypes().size());
		assertEquals(v1Loader.loadAttackTypes(), v2Loader.loadAttackTypes());

		System.out.println("condition effects: "+v2Loader.loadConditionEffects().size());
		assertEquals(v1Loader.loadConditionEffects(), v2Loader.loadConditionEffects());

		System.out.println("condition templates: "+v2Loader.loadConditionTemplates().size());
		assertEquals(v1Loader.loadConditionTemplates(), v2Loader.loadConditionTemplates());

		System.out.println("spell effects: "+v2Loader.loadSpellEffects().size());
		assertEquals(v1Loader.loadSpellEffects(), v2Loader.loadSpellEffects());

		System.out.println("loot entries: "+v2Loader.loadLootEntries().size());
		assertEquals(v1Loader.loadLootEntries(), v2Loader.loadLootEntries());

		System.out.println("loot tables: "+v2Loader.loadLootTables().size());
		assertEquals(v1Loader.loadLootTables(), v2Loader.loadLootTables());

		System.out.println("scripts: "+v2Loader.loadMazeScripts().size());
		assertEquals(v1Loader.loadMazeScripts(), v2Loader.loadMazeScripts());

		System.out.println("spells: "+v2Loader.loadSpells().size());
		assertEquals(v1Loader.loadSpells(), v2Loader.loadSpells());

		System.out.println("player spell books: "+v2Loader.loadPlayerSpellBooks().size());
		assertEquals(v1Loader.loadPlayerSpellBooks(), v2Loader.loadPlayerSpellBooks());

		System.out.println("maze textures: "+v2Loader.loadMazeTextures().size());
		assertEquals(v1Loader.loadMazeTextures(), v2Loader.loadMazeTextures());

		System.out.println("foe templates: "+v2Loader.loadFoeTemplates().size());
		assertEquals(v1Loader.loadFoeTemplates(), v2Loader.loadFoeTemplates());

		System.out.println("traps: "+v2Loader.loadTraps().size());
		assertEquals(v1Loader.loadTraps(), v2Loader.loadTraps());

		System.out.println("foe entries: "+v2Loader.loadFoeEntries().size());
		assertEquals(v1Loader.loadFoeEntries(), v2Loader.loadFoeEntries());

		System.out.println("encounter tables: "+v2Loader.loadEncounterTables().size());
		assertEquals(v1Loader.loadEncounterTables(), v2Loader.loadEncounterTables());

		System.out.println("npc faction templates: "+v2Loader.loadNpcFactionTemplates().size());
		assertEquals(v1Loader.loadNpcFactionTemplates(), v2Loader.loadNpcFactionTemplates());

		System.out.println("npc templates: "+v2Loader.loadNpcTemplates().size());
		assertEquals(v1Loader.loadNpcTemplates(), v2Loader.loadNpcTemplates());

		System.out.println("wielding combos: "+v2Loader.loadWieldingCombos().size());
		assertEquals(v1Loader.loadWieldingCombos(), v2Loader.loadWieldingCombos());

		System.out.println("item templates: "+v2Loader.loadItemTemplates().size());
		assertEquals(v1Loader.loadItemTemplates(), v2Loader.loadItemTemplates());

		System.out.println("difficulty levels: "+v2Loader.loadDifficultyLevels().size());
		assertEquals(v1Loader.loadDifficultyLevels(), v2Loader.loadDifficultyLevels());

		System.out.println("craft recipes: "+v2Loader.loadCraftRecipes().size());
		assertEquals(v1Loader.loadCraftRecipes(), v2Loader.loadCraftRecipes());

		System.out.println("item enchantments: "+v2Loader.loadItemEnchantments().size());
		assertEquals(v1Loader.loadItemEnchantments(), v2Loader.loadItemEnchantments());

		System.out.println("natural weapons: "+v2Loader.loadNaturalWeapons().size());
		assertEquals(v1Loader.loadNaturalWeapons(), v2Loader.loadNaturalWeapons());

		System.out.println("starting kits: "+v2Loader.loadStartingKits().size());
		assertEquals(v1Loader.loadStartingKits(), v2Loader.loadStartingKits());

		System.out.println("personalities: "+v2Loader.loadPersonalities().size());
		assertEquals(v1Loader.loadPersonalities(), v2Loader.loadPersonalities());

		System.out.println("foe types: "+v2Loader.loadFoeTypes().size());
		assertEquals(v1Loader.loadFoeTypes(), v2Loader.loadFoeTypes());

//		System.out.println("zones:");
//		zoneNames = v2Loader.getZoneNames();
//		for (String zoneName : zoneNames)
//		{
//			System.out.println(" - "+zoneName);
//			v2Loader.getZone(zoneName);
//		}
//		System.out.println("guild: "+v2Loader.loadCharacterGuild().size());

	}

	private static void assertEquals(Map<String, ?> v1Map, Map<String, ?> v2Map)
	{
		if (v1Map.size() != v2Map.size())
		{
			System.out.println("ERROR: different nr elements, v1 "+v1Map.size()+" v2 "+v2Map.size());
			return;
		}

		for (String s : v1Map.keySet())
		{
			if (!v1Map.get(s).equals(v2Map.get(s)))
			{
				System.out.println("ERROR: different elements v1 ["+v1Map.get(s)+"] v2 ["+v2Map.get(s)+"]");
			}
		}
	}
}
