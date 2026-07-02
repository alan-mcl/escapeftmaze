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

package mclachlan.maze.test.support;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.Saver;
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
import mclachlan.maze.stat.npc.*;

/**
 * A no-op {@link Saver}: the hermetic suite never writes to disk. Serialisation
 * round-trip tests drive the serialiser layer directly through in-memory
 * readers/writers instead.
 */
public class InMemorySaver extends Saver
{
	@Override public void saveGenders(Map<String, Gender> genders) { }
	@Override public void saveBodyParts(Map<String, BodyPart> bodyParts) { }
	@Override public void saveRaces(Map<String, Race> races) { }
	@Override public void saveExperienceTables(Map<String, ExperienceTable> map) { }
	@Override public void saveCharacterClasses(Map<String, CharacterClass> classes) { }
	@Override public void saveAttackTypes(Map<String, AttackType> map) { }
	@Override public void saveConditionEffects(Map<String, ConditionEffect> map) { }
	@Override public void saveConditionTemplates(Map<String, ConditionTemplate> map) { }
	@Override public void saveSpellEffects(Map<String, SpellEffect> map) { }
	@Override public void saveLootEntries(Map<String, LootEntry> map) { }
	@Override public void saveLootTables(Map<String, LootTable> map) { }
	@Override public void saveMazeScripts(Map<String, MazeScript> map) { }
	@Override public void saveSpells(Map<String, Spell> map) { }
	@Override public void savePlayerSpellBooks(Map<String, PlayerSpellBook> map) { }
	@Override public void saveMazeTextures(Map<String, MazeTexture> map) { }
	@Override public void saveObjectAnimations(Map<String, ObjectAnimations> map) { }
	@Override public void saveFoeTemplates(Map<String, FoeTemplate> map) { }
	@Override public void saveFoeSpeech(Map<String, FoeSpeech> map) { }
	@Override public void saveTraps(Map<String, Trap> map) { }
	@Override public void saveFoeEntries(Map<String, FoeEntry> map) { }
	@Override public void saveEncounterTables(Map<String, EncounterTable> map) { }
	@Override public void saveNpcFactionTemplates(Map<String, NpcFactionTemplate> map) { }
	@Override public void saveNpcTemplates(Map<String, NpcTemplate> map) { }
	@Override public void saveWieldingCombos(Map<String, WieldingCombo> map) { }
	@Override public void saveItemTemplates(Map<String, ItemTemplate> map) { }
	@Override public void saveDifficultyLevels(Map<String, DifficultyLevel> map) { }
	@Override public void saveCraftRecipes(Map<String, CraftRecipe> craftRecipes) { }
	@Override public void saveItemEnchantments(Map<String, ItemEnchantments> itemEnchantments) { }
	@Override public void saveNaturalWeapons(Map<String, NaturalWeapon> naturalWeapons) { }
	@Override public void saveStartingKits(Map<String, StartingKit> kits) { }
	@Override public void savePersonalities(Map<String, Personality> p) { }
	@Override public void saveFoeTypes(Map<String, FoeType> foeTypes) { }
	@Override public void saveZone(Zone zone) { }
	@Override public void deleteZone(String zoneName) { }
	@Override public void saveCharacterGuild(Map<String, PlayerCharacter> guild) { }
	@Override public void saveGameState(String saveGameName, GameState gameState) { }
	@Override public void savePlayerCharacters(String saveGameName, Map<String, PlayerCharacter> playerCharacters) { }
	@Override public void saveNpcs(String saveGameName, Map<String, Npc> npcs) { }
	@Override public void saveNpcFactions(String saveGameName, Map<String, NpcFaction> npcFactions) { }
	@Override public void saveMazeVariables(String saveGameName) { }
	@Override public void saveMazeVariables(String saveGameName, Map<String, String> vars) { }
	@Override public void saveItemCaches(String saveGameName, Map<String, Map<Point, List<Item>>> caches) { }
	@Override public void savePlayerTilesVisited(String name, PlayerTilesVisited playerTilesVisited) { }
	@Override public void saveConditions(String saveGameName, Map<ConditionBearer, List<Condition>> conditions) { }
	@Override public void saveJournal(String saveGameName, Journal journal) { }
	@Override public void saveUserConfig(UserConfig userConfig) { }
}
