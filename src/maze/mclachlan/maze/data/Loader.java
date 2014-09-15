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

import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import javax.sound.sampled.Clip;
import mclachlan.maze.audio.AudioPlayer;
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
public abstract class Loader
{
	public void init(Campaign campaign) throws Exception
	{
	}

	// stuff that should be loaded up front
	public abstract Map<String, Gender> loadGenders();
	public abstract Map<String, Race> loadRaces();
	public abstract Map<String, BodyPart> loadBodyParts();
	public abstract Map<String, CharacterClass> loadCharacterClasses();
	public abstract Map<String, ExperienceTable> loadExperienceTables();
	public abstract Map<String, AttackType> loadAttackTypes();
	public abstract Map<String, ConditionEffect> loadConditionEffects();
	public abstract Map<String, ConditionTemplate> loadConditionTemplates();
	public abstract Map<String, SpellEffect> loadSpellEffects();
	public abstract Map<String, MazeScript> loadMazeScripts();
	public abstract Map<String, LootEntry> loadLootEntries();
	public abstract Map<String, LootTable> loadLootTables();
	public abstract Map<String, Spell> loadSpells();
	public abstract Map<String, PlayerSpellBook> loadPlayerSpellBooks();
	public abstract Map<String, MazeTexture> loadMazeTextures();
	public abstract Map<String, FoeAttack> loadFoeAttacks();
	public abstract Map<String, FoeTemplate> loadFoeTemplates();
	public abstract Map<String, Trap> loadTraps();
	public abstract Map<String, FoeEntry> loadFoeEntries();
	public abstract Map<String, EncounterTable> loadEncounterTables();
	public abstract Map<String, NpcFactionTemplate> loadNpcFactionTemplates();
	public abstract Map<String, NpcTemplate> loadNpcTemplates();
	public abstract Map<String, WieldingCombo> loadWieldingCombos();
	public abstract Map<String, ItemTemplate> loadItemTemplates();
	public abstract Map<String, DifficultyLevel> loadDifficultyLevels();
	public abstract Map<String, CraftRecipe> loadCraftRecipes();
	public abstract Map<String, ItemEnchantments> loadItemEnchantments();
	public abstract Map<String, Personality> loadPersonalities();
	public abstract Map<String, StartingKit> getStartingKits();
	public abstract Map<String, NaturalWeapon> getNaturalWeapons();

	public abstract StringManager getStringManager();
	// stuff that can be optionally lazy loaded
	public abstract BufferedImage getImage(String resourceName);
	public abstract List<String> getPortraitNames();
	public abstract Clip getClip(String clipName, AudioPlayer audioPlayer);
	public abstract InputStream getMusic(String trackName);

	public abstract List<String> getZoneNames();

	public abstract Zone getZone(String name);
	public abstract Font getFont(String name);
	// guild files
	public abstract Map<String, PlayerCharacter> loadCharacterGuild();
	// saved games
	public abstract List<String> getSaveGames();
	public abstract GameState loadGameState(String saveGameName) throws Exception;
	public abstract Map<String, PlayerCharacter> loadPlayerCharacters(String saveGameName) throws Exception;
	public abstract Map<String, Npc> loadNpcs(String saveGameName) throws Exception;
	public abstract Map<String, NpcFaction> loadNpcFactions(String saveGameName) throws Exception;
	public abstract void loadMazeVariables(String saveGameName) throws Exception;

	public abstract Map<String, Map<Point, List<Item>>> loadItemCaches(String saveGameName) throws Exception;

	public abstract PlayerTilesVisited loadPlayerTilesVisited(String saveGameName) throws Exception;

	public abstract Map<ConditionBearer, List<Condition>> loadConditions(String saveGameName) throws Exception;

	public abstract UserConfig loadUserConfig() throws Exception;
}
