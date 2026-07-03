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

import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import mclachlan.maze.audio.AudioPlayer;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.TextRepository;
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
 * A {@link Loader} backed entirely by in-memory maps, so that a synthetic
 * {@link mclachlan.maze.data.Database} can be assembled with no dependency on
 * the {@code data/} directory. {@link TestData} populates the maps it cares
 * about; everything else defaults to empty.
 */
public class InMemoryLoader extends Loader
{
	public final Map<String, Gender> genders = new HashMap<>();
	public final Map<String, Race> races = new HashMap<>();
	public final Map<String, BodyPart> bodyParts = new HashMap<>();
	public final Map<String, CharacterClass> characterClasses = new HashMap<>();
	public final Map<String, ExperienceTable> experienceTables = new HashMap<>();
	public final Map<String, AttackType> attackTypes = new HashMap<>();
	public final Map<String, ConditionEffect> conditionEffects = new HashMap<>();
	public final Map<String, ConditionTemplate> conditionTemplates = new HashMap<>();
	public final Map<String, SpellEffect> spellEffects = new HashMap<>();
	public final Map<String, MazeScript> mazeScripts = new HashMap<>();
	public final Map<String, LootEntry> lootEntries = new HashMap<>();
	public final Map<String, LootTable> lootTables = new HashMap<>();
	public final Map<String, Spell> spells = new HashMap<>();
	public final Map<String, PlayerSpellBook> playerSpellBooks = new HashMap<>();
	public final Map<String, MazeTexture> mazeTextures = new HashMap<>();
	public final Map<String, ObjectAnimations> objectAnimations = new HashMap<>();
	public final Map<String, FoeTemplate> foeTemplates = new HashMap<>();
	public final Map<String, FoeSpeech> foeSpeech = new HashMap<>();
	public final Map<String, Trap> traps = new HashMap<>();
	public final Map<String, FoeEntry> foeEntries = new HashMap<>();
	public final Map<String, EncounterTable> encounterTables = new HashMap<>();
	public final Map<String, NpcFactionTemplate> npcFactionTemplates = new HashMap<>();
	public final Map<String, NpcTemplate> npcTemplates = new HashMap<>();
	public final Map<String, WieldingCombo> wieldingCombos = new HashMap<>();
	public final Map<String, ItemTemplate> itemTemplates = new HashMap<>();
	public final Map<String, DifficultyLevel> difficultyLevels = new HashMap<>();
	public final Map<String, CraftRecipe> craftRecipes = new HashMap<>();
	public final Map<String, ItemEnchantments> itemEnchantments = new HashMap<>();
	public final Map<String, Personality> personalities = new HashMap<>();
	public final Map<String, StartingKit> startingKits = new HashMap<>();
	public final Map<String, FoeType> foeTypes = new HashMap<>();
	public final Map<String, NaturalWeapon> naturalWeapons = new HashMap<>();
	public final Map<String, PlayerCharacter> characterGuild = new HashMap<>();

	public final TestTextRepository textRepository = new TestTextRepository();

	/*-------------------------------------------------------------------------*/
	@Override public Map<String, Gender> loadGenders() { return genders; }
	@Override public Map<String, Race> loadRaces() { return races; }
	@Override public Map<String, BodyPart> loadBodyParts() { return bodyParts; }
	@Override public Map<String, CharacterClass> loadCharacterClasses() { return characterClasses; }
	@Override public Map<String, ExperienceTable> loadExperienceTables() { return experienceTables; }
	@Override public Map<String, AttackType> loadAttackTypes() { return attackTypes; }
	@Override public Map<String, ConditionEffect> loadConditionEffects() { return conditionEffects; }
	@Override public Map<String, ConditionTemplate> loadConditionTemplates() { return conditionTemplates; }
	@Override public Map<String, SpellEffect> loadSpellEffects() { return spellEffects; }
	@Override public Map<String, MazeScript> loadMazeScripts() { return mazeScripts; }
	@Override public Map<String, LootEntry> loadLootEntries() { return lootEntries; }
	@Override public Map<String, LootTable> loadLootTables() { return lootTables; }
	@Override public Map<String, Spell> loadSpells() { return spells; }
	@Override public Map<String, PlayerSpellBook> loadPlayerSpellBooks() { return playerSpellBooks; }
	@Override public Map<String, MazeTexture> loadMazeTextures() { return mazeTextures; }
	@Override public Map<String, ObjectAnimations> loadObjectAnimations() { return objectAnimations; }
	@Override public Map<String, FoeTemplate> loadFoeTemplates() { return foeTemplates; }
	@Override public Map<String, FoeSpeech> loadFoeSpeech() { return foeSpeech; }
	@Override public Map<String, Trap> loadTraps() { return traps; }
	@Override public Map<String, FoeEntry> loadFoeEntries() { return foeEntries; }
	@Override public Map<String, EncounterTable> loadEncounterTables() { return encounterTables; }
	@Override public Map<String, NpcFactionTemplate> loadNpcFactionTemplates() { return npcFactionTemplates; }
	@Override public Map<String, NpcTemplate> loadNpcTemplates() { return npcTemplates; }
	@Override public Map<String, WieldingCombo> loadWieldingCombos() { return wieldingCombos; }
	@Override public Map<String, ItemTemplate> loadItemTemplates() { return itemTemplates; }
	@Override public Map<String, DifficultyLevel> loadDifficultyLevels() { return difficultyLevels; }
	@Override public Map<String, CraftRecipe> loadCraftRecipes() { return craftRecipes; }
	@Override public Map<String, ItemEnchantments> loadItemEnchantments() { return itemEnchantments; }
	@Override public Map<String, Personality> loadPersonalities() { return personalities; }
	@Override public Map<String, StartingKit> loadStartingKits() { return startingKits; }
	@Override public Map<String, FoeType> loadFoeTypes() { return foeTypes; }
	@Override public Map<String, NaturalWeapon> loadNaturalWeapons() { return naturalWeapons; }

	@Override public TextRepository getTextRepository() { return textRepository; }
	@Override public void initTextRepository() { textRepository.resetCaches(); }

	/*-------------------------------------------------------------------------*/
	// resources & save games: not needed by the hermetic suite

	@Override public BufferedImage getImage(String resourceName) { return null; }
	@Override public List<String> getPortraitNames() { return new ArrayList<>(); }
	@Override public void cacheSound(String clipName, AudioPlayer audioPlayer) { }
	@Override public InputStream getMusic(String trackName) { return null; }
	@Override public List<String> getZoneNames() { return new ArrayList<>(); }
	@Override public Zone getZone(String name) { return null; }
	@Override public Font getFont(String name) { return null; }
	@Override public Map<String, PlayerCharacter> loadCharacterGuild() { return characterGuild; }
	@Override public List<String> getSaveGames() { return new ArrayList<>(); }
	@Override public GameState loadGameState(String saveGameName) { return null; }
	@Override public Map<String, PlayerCharacter> loadPlayerCharacters(String saveGameName) { return new HashMap<>(); }
	@Override public Map<String, Npc> loadNpcs(String saveGameName) { return new HashMap<>(); }
	@Override public Map<String, NpcFaction> loadNpcFactions(String saveGameName) { return new HashMap<>(); }
	@Override public void loadMazeVariables(String saveGameName) { }
	@Override public Map<String, String> loadMazeVariablesMap(String saveGameName) { return new HashMap<>(); }
	@Override public Map<String, Map<Point, List<Item>>> loadItemCaches(String saveGameName) { return new HashMap<>(); }
	@Override public PlayerTilesVisited loadPlayerTilesVisited(String saveGameName) { return new PlayerTilesVisited(new HashMap<>()); }
	@Override public Map<ConditionBearer, List<Condition>> loadConditions(
		String saveGameName, Map<String, PlayerCharacter> playerCharacterCache) { return new HashMap<>(); }
	@Override public Journal loadJournal(String saveGameName, String journalName) { return null; }
	@Override public UserConfig loadUserConfig() { return UserConfig.defaultsForTesting(); }
}
