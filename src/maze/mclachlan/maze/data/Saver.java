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

package mclachlan.maze.data;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.*;
import mclachlan.maze.game.journal.Journal;
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
public abstract class Saver
{
	public void init(Campaign c)
	{
	}

	// static data
	public abstract void saveGenders(Map<String, Gender> genders) throws Exception;
	public abstract void saveBodyParts(Map<String, BodyPart> bodyParts) throws Exception;
	public abstract void saveRaces(Map<String, Race> races) throws Exception;
	public abstract void saveExperienceTables(Map<String, ExperienceTable> map) throws Exception;
	public abstract void saveCharacterClasses(Map<String, CharacterClass> classes) throws Exception;
	public abstract void saveAttackTypes(Map<String, AttackType> map) throws Exception;
	public abstract void saveConditionEffects(Map<String, ConditionEffect> map) throws Exception;
	public abstract void saveConditionTemplates(Map<String, ConditionTemplate> map) throws Exception;
	public abstract void saveSpellEffects(Map<String, SpellEffect> map) throws Exception;
	public abstract void saveLootEntries(Map<String, LootEntry> map) throws Exception;
	public abstract void saveLootTables(Map<String, LootTable> map) throws Exception;
	public abstract void saveMazeScripts(Map<String, MazeScript> map) throws Exception;
	public abstract void saveSpells(Map<String, Spell> map) throws Exception;
	public abstract void savePlayerSpellBooks(Map<String, PlayerSpellBook> map) throws Exception;
	public abstract void saveMazeTextures(Map<String, MazeTexture> map) throws Exception;
	public abstract void saveFoeTemplates(Map<String, FoeTemplate> map) throws Exception;
	public abstract void saveTraps(Map<String, Trap> map) throws Exception;
	public abstract void saveFoeEntries(Map<String, FoeEntry> map) throws Exception;
	public abstract void saveEncounterTables(Map<String, EncounterTable> map) throws Exception;
	public abstract void saveNpcFactionTemplates(Map<String, NpcFactionTemplate> map) throws Exception;
	public abstract void saveNpcTemplates(Map<String, NpcTemplate> map) throws Exception;
	public abstract void saveWieldingCombos(Map<String, WieldingCombo> map) throws Exception;
	public abstract void saveItemTemplates(Map<String, ItemTemplate> map) throws Exception;
	public abstract void saveDifficultyLevels(Map<String, DifficultyLevel> map) throws Exception;
	public abstract void saveCraftRecipes(Map<String, CraftRecipe> craftRecipes) throws Exception;
	public abstract void saveItemEnchantments(Map<String, ItemEnchantments> itemEnchantments) throws Exception;
	public abstract void saveNaturalWeapons(Map<String, NaturalWeapon> naturalWeapons) throws Exception;
	public abstract void saveStartingKits(Map<String, StartingKit> kits) throws Exception;
	public abstract void savePersonalities(Map<String, Personality> p) throws Exception;
	public abstract void saveZone(Zone zone) throws Exception;
	// bit of a special case
	public abstract void deleteZone(String zoneName) throws Exception;

	// guild files
	public abstract void saveCharacterGuild(Map<String, PlayerCharacter> guild) throws Exception;

	// saving a game
	public abstract void saveGameState(String saveGameName, GameState gameState) throws Exception;
	public abstract void savePlayerCharacters(String saveGameName, Map<String, PlayerCharacter> playerCharacters) throws Exception;
	public abstract void saveNpcs(String saveGameName, Map<String, Npc> npcs) throws Exception;
	public abstract void saveNpcFactions(String saveGameName, Map<String, NpcFaction> npcFactions) throws Exception;
	public abstract void saveMazeVariables(String saveGameName) throws Exception;
	public abstract void saveItemCaches(String saveGameName, Map<String, Map<Point, List<Item>>> caches) throws Exception;
	public abstract void savePlayerTilesVisited(String name, PlayerTilesVisited playerTilesVisited) throws Exception;
	public abstract void saveConditions(String saveGameName, Map<ConditionBearer, List<Condition>> conditions) throws Exception;
	public abstract void saveJournal(String saveGameName, Journal journal) throws Exception;

	// user config
	public abstract void saveUserConfig(UserConfig userConfig) throws Exception;
}
