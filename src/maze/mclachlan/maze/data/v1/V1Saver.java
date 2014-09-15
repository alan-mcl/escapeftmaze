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

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.Saver;
import mclachlan.maze.game.*;
import mclachlan.maze.map.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.combat.WieldingCombo;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.PlayerSpellBook;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.stat.npc.NpcFactionTemplate;
import mclachlan.maze.stat.npc.NpcTemplate;

/**
 *
 */
public class V1Saver extends Saver
{
	private String path = null;
	private String savePath = null;

	/*-------------------------------------------------------------------------*/
	public void init(Campaign c)
	{
		path = "data/"+ c.getName()+"/db/";
		savePath = "data/"+ c.getName()+"/save/";
	}

	/*-------------------------------------------------------------------------*/
	private String getSaveGamePath(String saveGameName)
	{
		String result = savePath+saveGameName+'/';
		new File(result).mkdirs();
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void saveGenders(Map<String, Gender> genders) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.GENDERS));
		V1Gender.save(writer, genders);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveBodyParts(Map<String, BodyPart> bodyParts) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.BODY_PARTS));
		V1BodyPart.save(writer, bodyParts);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveStartingKits(Map<String, StartingKit> kits) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.STARTING_KITS));
		V1StartingKit.save(writer, kits);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveRaces(Map<String, Race> races) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.RACES));
		V1Race.save(writer, races);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveExperienceTables(Map<String, ExperienceTable> xTables) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.EXPERIENCE_TABLES));
		V1ExperienceTable.save(writer, xTables);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveCharacterClasses(Map<String, CharacterClass> classes) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.CHARACTER_CLASSES));
		V1CharacterClass.save(writer, classes);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveAttackTypes(Map<String, AttackType> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.ATTACK_TYPES));
		V1AttackType.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveConditionEffects(Map<String, ConditionEffect> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.CONDITION_EFFECTS));
		V1ConditionEffect.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveConditionTemplates(Map<String, ConditionTemplate> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.CONDITION_TEMPLATES));
		V1ConditionTemplate.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveSpellEffects(Map<String, SpellEffect> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.SPELL_EFFECTS));
		V1SpellEffect.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveLootEntries(Map<String, LootEntry> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.LOOT_ENTRIES));
		V1LootEntry.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveLootTables(Map<String, LootTable> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.LOOT_TABLES));
		V1LootTable.save(writer, map);
		writer.flush();
		writer.close();
	}
	/*-------------------------------------------------------------------------*/
	public void saveMazeScripts(Map<String, MazeScript> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.MAZE_SCRIPTS));
		V1Script.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveSpells(Map<String, Spell> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.SPELLS));
		V1Spell.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void savePlayerSpellBooks(Map<String, PlayerSpellBook> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.PLAYER_SPELL_BOOKS));
		V1PlayerSpellBook.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveMazeTextures(Map<String, MazeTexture> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.MAZE_TEXTURES));
		V1MazeTexture.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveFoeAttacks(Map<String, FoeAttack> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.FOE_ATTACKS));
		V1FoeAttack.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveFoeTemplates(Map<String, FoeTemplate> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.FOE_TEMPLATES));
		V1FoeTemplate.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveTraps(Map<String, Trap> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.TRAPS));
		V1Trap.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveFoeEntries(Map<String, FoeEntry> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.FOE_ENTRIES));
		V1FoeEntry.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveEncounterTables(Map<String, EncounterTable> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.ENCOUNTER_TABLES));
		V1EncounterTable.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveNpcFactionTemplates(Map<String, NpcFactionTemplate> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.NPC_FACTION_TEMPLATES));
		V1NpcFactionTemplate.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveNpcTemplates(Map<String, NpcTemplate> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.NPC_TEMPLATES));
		V1NpcTemplate.save(path, writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveWieldingCombos(Map<String, WieldingCombo> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.WIELDING_COMBOS));
		V1WieldingCombo.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveItemTemplates(Map<String, ItemTemplate> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.ITEM_TEMPLATES));
		V1ItemTemplate.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveDifficultyLevels(Map<String, DifficultyLevel> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.DIFFICULTY_LEVELS));
		V1DifficultyLevel.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveCraftRecipes(Map<String, CraftRecipe> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.CRAFT_RECIPES));
		V1CraftRecipe.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveItemEnchantments(Map<String, ItemEnchantments> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.ITEM_ENCHANTMENTS));
		V1ItemEnchantments.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveNaturalWeapons(
		Map<String, NaturalWeapon> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.NATURAL_WEAPONS));
		V1NaturalWeapons.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void savePersonalities(Map<String, Personality> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+V1Utils.PERSONALITIES));
		V1Personalities.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveZone(Zone zone) throws Exception
	{
		File file = new File(path+V1Utils.ZONES+zone.getName()+".txt");
		file.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		V1Zone.save(writer, zone);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void deleteZone(String zoneName) throws Exception
	{
		File file = new File(path+V1Utils.ZONES+zoneName+".txt");
		file.delete();
	}

	/*-------------------------------------------------------------------------*/
	public void saveCharacterGuild(Map<String, PlayerCharacter> map) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(savePath+V1Utils.CHARACTER_GUILD));
		V1PlayerCharacter.save(writer, map);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveGameState(String saveGameName, GameState gameState) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(getSaveGamePath(saveGameName)+V1Utils.GAME_STATE));
		V1GameState.save(writer, gameState);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void savePlayerCharacters(String saveGameName, Map<String, PlayerCharacter> playerCharacters) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(getSaveGamePath(saveGameName)+V1Utils.PLAYER_CHARACTERS));
		V1PlayerCharacter.save(writer, playerCharacters);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveNpcs(String saveGameName, Map<String, Npc> npcs) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(getSaveGamePath(saveGameName)+V1Utils.NPCS));
		V1Npc.save(writer, npcs);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveNpcFactions(String saveGameName, Map<String, NpcFaction> npcFactions) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(getSaveGamePath(saveGameName)+V1Utils.NPC_FACTIONS));
		V1NpcFaction.save(writer, npcFactions);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveMazeVariables(String saveGameName) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(getSaveGamePath(saveGameName)+V1Utils.MAZE_VARIABLES));
		V1MazeVariables.save(writer);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveItemCaches(String saveGameName, Map<String, Map<Point, java.util.List<Item>>> caches) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(getSaveGamePath(saveGameName)+V1Utils.ITEM_CACHES));
		V1ItemCache.save(writer, caches);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void savePlayerTilesVisited(String saveGameName,
		PlayerTilesVisited playerTilesVisited) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(getSaveGamePath(saveGameName)+V1Utils.TILES_VISITED));
		V1TilesVisited.save(writer, playerTilesVisited);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveConditions(String saveGameName, Map<ConditionBearer, java.util.List<Condition>> conditions) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(getSaveGamePath(saveGameName)+V1Utils.CONDITIONS));
		V1ConditionManager.save(writer, conditions);
		writer.flush();
		writer.close();
	}

	/*-------------------------------------------------------------------------*/
	public void saveUserConfig(UserConfig userConfig) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(V1Utils.USER_CONFIG));
		Properties p = userConfig.toProperties();
		p.store(writer, "Written by V1Saver");
		writer.flush();
		writer.close();
	}
}
