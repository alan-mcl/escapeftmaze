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
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import javax.sound.sampled.Clip;
import mclachlan.maze.game.DifficultyLevel;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.UserConfig;
import mclachlan.maze.map.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.combat.WieldingCombo;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.PlayerSpellBook;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.npc.NpcFactionTemplate;
import mclachlan.maze.stat.npc.NpcTemplate;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Database
{
	private static Database instance;

	private Loader loader;
	private Saver saver;
	private final Object mutex = new Object();
	
	// in mem caches
	private Map<String, CharacterClass> characterClasses;
	private List<String> portraitNames;
	private Map<String, NpcFactionTemplate> npcFactions;
	private Map<String, NpcTemplate> npcTemplates;

	private Map<String, PlayerCharacter> characterGuild;
	private Map<String, AttackType> attackTypes;
	private Map<String, ItemTemplate> itemTemplates;
	private Map<String, FoeTemplate> foeTemplates;
	private StringManager stringManager;
	private Map<String, Spell> spells;
	private Map<String, PlayerSpellBook> playerSpellBooks;
	private Map<String, MazeTexture> textures;
	private Map<String, Race> races;
	private Map<String, Gender> genders;
	private Map<String, LootEntry> lootEntries;
	private Map<String, LootTable> lootTables;

	private Map<String, Trap> traps;
	private Map<String, EncounterTable> encounterTables;
	private Map<String, FoeEntry> foeEntries;
	private Map<String, WieldingCombo> wieldingCombos;
	private Map<String, ConditionEffect> conditionEffects;
	private Map<String, SpellEffect> spellEffects;
	private Map<String, ConditionTemplate> conditionTemplates;
	private Map<String, MazeScript> scripts;
	private Map<String, BodyPart> bodyParts;
	private Map<String, CraftRecipe> craftRecipes;
	private Map<String, ItemEnchantments> itemEnchantments;
	private Map<String, Personality> personalities;
	private Map<String, NaturalWeapon> naturalWeapons;
	private Map<String, StartingKit> startingKits;
	private Map<String, FoeType> foeTypes;

	private Map<String, DifficultyLevel> difficultyLevels;
	private Map<String, ExperienceTable> experienceTables;

	private UserConfig userConfig;

	{
		instance = this;
	}

	/*-------------------------------------------------------------------------*/
	public Saver getSaver()
	{
		return saver;
	}

	/*-------------------------------------------------------------------------*/
	public Loader getLoader()
	{
		return loader;
	}

	/*-------------------------------------------------------------------------*/
	public Database() throws Exception
	{
		Map<String, String> config = Maze.getInstance().getAppConfig();

		String loader_impl = config.get(Maze.AppConfig.DB_LOADER_IMPL);
		String saver_impl = config.get(Maze.AppConfig.DB_SAVER_IMPL);

		Class loader_class = Class.forName(loader_impl);
		Class saver_class = Class.forName(saver_impl);

		Maze.log("init loader: "+loader_impl);
		loader = (Loader)loader_class.newInstance();
		loader.init(Maze.getInstance().getCampaign());

		Maze.log("init saver: "+saver_impl);
		saver = (Saver)saver_class.newInstance();
		saver.init(Maze.getInstance().getCampaign());
	}

	/*-------------------------------------------------------------------------*/
	public Database(Loader loader, Saver saver)
	{
		this.loader = loader;
		this.saver = saver;
	}

	/*-------------------------------------------------------------------------*/
	public static Database getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Gender> getGenders()
	{
		synchronized(mutex)
		{
			if (genders == null)
			{
				genders = loader.loadGenders();
			}
			
			return genders;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getGenderList()
	{
		synchronized(mutex)
		{
			return new ArrayList<String>(getGenders().keySet());
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getRaceList()
	{
		synchronized(mutex)
		{
			if (this.races == null)
			{
				this.races = loader.loadRaces();
			}
			return new ArrayList<String>(this.races.keySet());
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getCharacterClassList()
	{
		synchronized(mutex)
		{
			return new ArrayList<String>(this.getCharacterClasses().keySet());
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public List<String> getPortraitNames()
	{
		synchronized(mutex)
		{
			if (this.portraitNames == null)
			{
				portraitNames = loader.getPortraitNames();
			}
			return this.portraitNames;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getZoneNames()
	{
		synchronized(mutex)
		{
			return this.loader.getZoneNames();
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, PlayerCharacter> getCharacterGuild()
	{
		synchronized(mutex)
		{
			if (characterGuild == null)
			{
				characterGuild = this.loader.loadCharacterGuild();
			}
			
			return characterGuild;
		}
	}

	/*-------------------------------------------------------------------------*/
	public CharacterClass getCharacterClass(String name)
	{
		synchronized(mutex)
		{
			if (characterClasses == null)
			{
				characterClasses = loader.loadCharacterClasses();
			}
			CharacterClass result = this.characterClasses.get(name);
			if(result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public AttackType getAttackType(String name)
	{
		synchronized(mutex)
		{
			if (attackTypes == null)
			{
				attackTypes = this.loader.loadAttackTypes();
			}

			AttackType result = this.attackTypes.get(name);
			if (result == null)
			{
				throw new MazeException("Invalid attack type ["+name+"]");
			}
			return result;
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public ItemTemplate getItemTemplate(String name)
	{
		synchronized(mutex)
		{
			if (itemTemplates == null)
			{
				itemTemplates = this.loader.loadItemTemplates();
			}
			ItemTemplate result = this.itemTemplates.get(name);
			if (result == null)
			{
				throw new MazeException("Invalid item template ["+name+"]");
			}
			return result;
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public Zone getZone(String name)
	{
		synchronized(mutex)
		{
			return this.loader.getZone(name);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Font getFont(String name)
	{
		synchronized(mutex)
		{
			return this.loader.getFont(name);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Image resource names split by forward slashes.
	 */ 
	public BufferedImage getImage(String resourceName)
	{
		synchronized(mutex)
		{
			return this.loader.getImage(resourceName);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public String getString(String namespace, String key, boolean allowNull)
	{
		synchronized(mutex)
		{
			if (stringManager == null)
			{
				stringManager = this.loader.getStringManager();
			}

			String result = this.stringManager.getString(namespace, key);
			if (result == null)
			{
				// special case: retry one time to load the resource bundle
				stringManager = this.loader.getStringManager();

				result = this.stringManager.getString(namespace, key);
				if (result == null && !allowNull)
				{
					throw new MazeException("Invalid key ["+key+"]");
				}
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Spell getSpell(String spellName)
	{
		synchronized(mutex)
		{
			if (spells == null)
			{
				spells = this.loader.loadSpells();
			}

			Spell result = this.spells.get(spellName);
			if (result == null)
			{
				throw new MazeException("Invalid spell ["+spellName+"]");
			}

			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public PlayerSpellBook getPlayerSpellBook(String book)
	{
		synchronized(mutex)
		{
			if (playerSpellBooks == null)
			{
				playerSpellBooks = this.loader.loadPlayerSpellBooks();
			}

			PlayerSpellBook result = this.playerSpellBooks.get(book);
			if (result == null)
			{
				throw new MazeException("Invalid book ["+book+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, PlayerSpellBook> getPlayerSpellBooks()
	{
		synchronized(mutex)
		{
			if (playerSpellBooks == null)
			{
				playerSpellBooks = this.loader.loadPlayerSpellBooks();
			}
			
			return playerSpellBooks;
		}
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getMazeTexture(String textureName)
	{
		synchronized(mutex)
		{
			if (textures == null)
			{
				textures = this.loader.loadMazeTextures();
			}

			MazeTexture result = this.textures.get(textureName);
			if (result == null)
			{
				throw new MazeException("invalid maze texture ["+textureName+"]");
			}
			return result;
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public Clip getClip(String clipName)
	{
		synchronized(mutex)
		{
			return this.loader.getClip(clipName, Maze.getInstance().getAudioPlayer());
		}
	}

	/*-------------------------------------------------------------------------*/
	public InputStream getMusic(String trackName)
	{
		synchronized(mutex)
		{
			return this.loader.getMusic(trackName);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Race getRace(String name)
	{
		synchronized(mutex)
		{
			if (races == null)
			{
				races = this.loader.loadRaces();
			}
			Race result = races.get(name);
			if (result == null)
			{
				throw new MazeException("Invalid name ["+name+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Gender getGender(String name)
	{
		synchronized(mutex)
		{
			if (genders == null)
			{
				genders = this.loader.loadGenders();
			}
			Gender result = genders.get(name);
			if (result == null)
			{
				throw new MazeException("Invalid name ["+name+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, BodyPart> getBodyParts()
	{
		synchronized(mutex)
		{
			if (bodyParts == null)
			{
				bodyParts = loader.loadBodyParts();
			}

			return bodyParts;
		}
	}

	/*-------------------------------------------------------------------------*/
	public BodyPart getBodyPart(String name)
	{
		synchronized(mutex)
		{
			if (bodyParts == null)
			{
				bodyParts = loader.loadBodyParts();
			}
			BodyPart result = bodyParts.get(name);
			if (result == null)
			{
				throw new MazeException("Invalid name ["+name+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public LootTable getLootTable(String name)
	{
		synchronized(mutex)
		{
			if (lootTables == null)
			{
				lootTables = loader.loadLootTables();
			}
			LootTable lootTable = lootTables.get(name);
			if (lootTable == null)
			{
				throw new MazeException("invalid loot table ["+name+"]");
			}
			return lootTable;
		}
	}

	/*-------------------------------------------------------------------------*/
	public LootEntry getLootEntry(String name)
	{
		synchronized(mutex)
		{
			if (lootEntries == null)
			{
				lootEntries = loader.loadLootEntries();
			}
			LootEntry lootEntry = lootEntries.get(name);
			if (lootEntry == null)
			{
				throw new MazeException("invalid loot entry ["+name+"]");
			}
			return lootEntry;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Trap getTrap(String name)
	{
		synchronized(mutex)
		{
			if (traps == null)
			{
				traps = loader.loadTraps();
			}
			Trap trap = traps.get(name);
			if (traps == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return trap;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcTemplate> getNpcTemplates()
	{
		synchronized(mutex)
		{
			if (npcTemplates == null)
			{
				npcTemplates = loader.loadNpcTemplates();
			}
			return npcTemplates;
		}
	}

	/*-------------------------------------------------------------------------*/
	public FoeTemplate getFoeTemplate(String name)
	{
		synchronized(mutex)
		{
			if (foeTemplates == null)
			{
				foeTemplates = this.loader.loadFoeTemplates();
			}
			FoeTemplate result = foeTemplates.get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public EncounterTable getEncounterTable(String name)
	{
		synchronized(mutex)
		{
			if (encounterTables == null)
			{
				encounterTables = this.loader.loadEncounterTables();
			}
			EncounterTable result = encounterTables.get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public FoeEntry getFoeEntry(String name)
	{
		synchronized(name)
		{
			if (foeEntries == null)
			{
				foeEntries = this.loader.loadFoeEntries();
			}
			FoeEntry result = foeEntries.get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public WieldingCombo getWieldingCombo(String name)
	{
		synchronized(mutex)
		{
			if (wieldingCombos == null)
			{
				wieldingCombos = this.loader.loadWieldingCombos();
			}

			// in this case we actually want to return null if one is not present
			return wieldingCombos.get(name);
		}
	}

	/*-------------------------------------------------------------------------*/
	public ConditionEffect getConditionEffect(String name)
	{
		synchronized(mutex)
		{
			if (conditionEffects == null)
			{
				conditionEffects = loader.loadConditionEffects();
			}
			ConditionEffect result = conditionEffects.get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public SpellEffect getSpellEffect(String name)
	{
		synchronized(mutex)
		{
			if (spellEffects == null)
			{
				spellEffects = this.loader.loadSpellEffects();
			}

			SpellEffect result = spellEffects.get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public ConditionTemplate getConditionTemplate(String name)
	{
		synchronized(mutex)
		{
			if (conditionTemplates == null)
			{
				conditionTemplates = loader.loadConditionTemplates();
			}
			ConditionTemplate result = conditionTemplates.get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcFactionTemplate> getNpcFactionTemplates()
	{
		synchronized(mutex)
		{
			if (npcFactions == null)
			{
				npcFactions = loader.loadNpcFactionTemplates();
			}
			return npcFactions;
		}
	}

	/*-------------------------------------------------------------------------*/
	public MazeScript getScript(String name)
	{
		synchronized(mutex)
		{
			if (scripts == null)
			{
				scripts = loader.loadMazeScripts();
			}
			MazeScript result = this.scripts.get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public ExperienceTable getExperienceTable(String name)
	{
		synchronized(mutex)
		{
			if (experienceTables == null)
			{
				experienceTables = loader.loadExperienceTables();
			}

			ExperienceTable result = this.experienceTables.get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ExperienceTable> getExperienceTables()
	{
		synchronized(mutex)
		{
			if (experienceTables == null)
			{
				experienceTables = loader.loadExperienceTables();
			}

			return experienceTables;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getItemList()
	{
		synchronized(mutex)
		{
			if (this.itemTemplates == null)
			{
				itemTemplates = loader.loadItemTemplates();
			}
			return new ArrayList<String>(this.itemTemplates.keySet());
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getSpellList()
	{
		synchronized(mutex)
		{
			if (this.spells == null)
			{
				spells = loader.loadSpells();
			}
			return new ArrayList<String>(this.spells.keySet());
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, CharacterClass> getCharacterClasses()
	{
		synchronized(mutex)
		{
			if (characterClasses == null)
			{
				characterClasses = loader.loadCharacterClasses();
			}
			return characterClasses;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, AttackType> getAttackTypes()
	{
		synchronized(mutex)
		{
			if (attackTypes == null)
			{
				attackTypes = loader.loadAttackTypes();
			}
			return attackTypes;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ConditionTemplate> getConditionTemplates()
	{
		synchronized(mutex)
		{
			if (conditionTemplates == null)
			{
				conditionTemplates = loader.loadConditionTemplates();
			}

			return conditionTemplates;
		}
	}

	/*-------------------------------------------------------------------------*/

	public Map<String, ConditionEffect> getConditionEffects()
	{
		synchronized(mutex)
		{
			if (conditionEffects == null)
			{
				conditionEffects = loader.loadConditionEffects();
			}

			return conditionEffects;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, SpellEffect> getSpellEffects()
	{
		synchronized(mutex)
		{
			if (spellEffects == null)
			{
				spellEffects = loader.loadSpellEffects();
			}

			return spellEffects;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, LootEntry> getLootEntries()
	{
		synchronized(mutex)
		{
			if (lootEntries == null)
			{
				lootEntries = loader.loadLootEntries();
			}

			return lootEntries;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, LootTable> getLootTables()
	{
		synchronized(mutex)
		{
			if (lootTables == null)
			{
				lootTables = loader.loadLootTables();
			}

			return lootTables;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, MazeScript> getMazeScripts()
	{
		synchronized(mutex)
		{
			if (scripts == null)
			{
				scripts = loader.loadMazeScripts();
			}

			return scripts;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Spell> getSpells()
	{
		synchronized(mutex)
		{
			if (spells == null)
			{
				spells = loader.loadSpells();
			}

			return spells;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Race> getRaces()
	{
		synchronized(mutex)
		{
			if (races == null)
			{
				races = loader.loadRaces();
			}

			return races;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, FoeType> getFoeTypes()
	{
		synchronized(mutex)
		{
			if (foeTypes == null)
			{
				foeTypes = loader.loadFoeTypes();
			}

			return foeTypes;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, MazeTexture> getMazeTextures()
	{
		synchronized(mutex)
		{
			if (textures == null)
			{
				textures = loader.loadMazeTextures();
			}

			return textures;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, FoeTemplate> getFoeTemplates()
	{
		synchronized(mutex)
		{
			if (foeTemplates == null)
			{
				foeTemplates = loader.loadFoeTemplates();
			}

			return foeTemplates;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Trap> getTraps()
	{
		synchronized(mutex)
		{
			if (traps == null)
			{
				traps = loader.loadTraps();
			}

			return traps;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, FoeEntry> getFoeEntries()
	{
		synchronized(mutex)
		{
			if (foeEntries == null)
			{
				foeEntries = loader.loadFoeEntries();
			}

			return foeEntries;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, EncounterTable> getEncounterTables()
	{
		synchronized(mutex)
		{
			if (encounterTables == null)
			{
				encounterTables = loader.loadEncounterTables();
			}

			return encounterTables;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, WieldingCombo> getWieldingCombos()
	{
		synchronized(mutex)
		{
			if (wieldingCombos == null)
			{
				wieldingCombos = loader.loadWieldingCombos();
			}

			return wieldingCombos;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ItemTemplate> getItemTemplates()
	{
		synchronized(mutex)
		{
			if (itemTemplates == null)
			{
				itemTemplates = loader.loadItemTemplates();
			}

			return itemTemplates;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, DifficultyLevel> getDifficultyLevels()
	{
		synchronized(mutex)
		{
			if (difficultyLevels == null)
			{
				difficultyLevels = loader.loadDifficultyLevels();
			}

			return difficultyLevels;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, CraftRecipe> getCraftRecipes()
	{
		synchronized(mutex)
		{
			if (craftRecipes == null)
			{
				craftRecipes = loader.loadCraftRecipes();
			}

			return craftRecipes;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ItemEnchantments> getItemEnchantments()
	{
		synchronized(mutex)
		{
			if (itemEnchantments == null)
			{
				itemEnchantments = loader.loadItemEnchantments();
			}

			return itemEnchantments;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Personality> getPersonalities()
	{
		synchronized(mutex)
		{
			if (personalities == null)
			{
				personalities = loader.loadPersonalities();
			}

			return personalities;
		}
	}

	/*-------------------------------------------------------------------------*/
	public UserConfig getUserConfig()
	{
		synchronized (mutex)
		{
			if (userConfig == null)
			{
				try
				{
					userConfig = loader.loadUserConfig();
				}
				catch (Exception e)
				{
					throw new MazeException("Can't load user config.",e);
				}
			}

			return userConfig;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String,NaturalWeapon> getNaturalWeapons()
	{
		synchronized (mutex)
		{
			if (naturalWeapons == null)
			{
				try
				{
					naturalWeapons = loader.getNaturalWeapons();
				}
				catch (Exception e)
				{
					throw new MazeException("Can't load natural weapons.",e);
				}
			}
		}

		return naturalWeapons;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String,StartingKit> getStartingKits()
	{
		synchronized (mutex)
		{
			if (startingKits == null)
			{
				try
				{
					startingKits = loader.getStartingKits();
				}
				catch (Exception e)
				{
					throw new MazeException("Can't load natural weapons.",e);
				}
			}
		}

		return startingKits;
	}

}
