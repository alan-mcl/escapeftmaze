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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.*;
import javax.sound.sampled.Clip;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.data.v1.V1Utils;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.game.*;
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
import mclachlan.maze.ui.diygui.ProgressListener;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Database
{
	/** Singleton instance */
	private static Database instance;

	/** Mutex that single-threads all cache access */
	private final Object mutex = new Object();

	/** Map of campaign name to campaigns */
	private static Map<String, Campaign> campaignMap;

	/**
	 * List of campaigns, starting with the selected one at index 0  followed by
	 * it's parent if any, then grandparent etc
	 */
	private final List<CampaignCache> campaignCaches = new ArrayList<>();

	/** User selected configuration */
	private UserConfig userConfig;

	// cached game data

	private Map<String, Gender> genders;
	private Map<String, Race> races;
	private Map<String, BodyPart> bodyParts;
	private Map<String, CharacterClass> characterClasses;
	private Map<String, StartingKit> startingKits;
	private Map<String, ExperienceTable> experienceTables;
	private Map<String, AttackType> attackTypes;
	private Map<String, ConditionEffect> conditionEffects;
	private Map<String, ConditionTemplate> conditionTemplates;
	private Map<String, SpellEffect> spellEffects;
	private Map<String, MazeScript> mazeScripts;
	private Map<String, LootEntry> lootEntries;
	private Map<String, LootTable> lootTables;
	private Map<String, Spell> spells;
	private Map<String, PlayerSpellBook> playerSpellBooks;
	private Map<String, MazeTexture> mazeTextures;
	private Map<String, FoeTemplate> foeTemplates;
	private Map<String, Trap> traps;
	private Map<String, FoeEntry> foeEntries;
	private Map<String, EncounterTable> encounterTables;
	private Map<String, NpcFactionTemplate> npcFactionTemplates;
	private Map<String, NpcTemplate> npcTemplates;
	private Map<String, WieldingCombo> wieldingCombos;
	private Map<String, ItemTemplate> itemTemplates;
	private Map<String, DifficultyLevel> difficultyLevels;
	private Map<String, ItemEnchantments> itemEnchantments;
	private Map<String, Personality> personalities;
	private Map<String, CraftRecipe> craftRecipes;
	private Map<String, FoeType> foeTypes;
	private Map<String, NaturalWeapon> naturalWeapons;

	private Map<String, Map<String, String>> strings = new HashMap<>();
	private Map<String, PlayerCharacter> characterGuild;
	private Map<String, BufferedImage> images = new HashMap<>();
	private Loader loader;
	private Saver saver;
	private Campaign campaign;


	/*-------------------------------------------------------------------------*/
	{
		instance = this;
	}

	/*-------------------------------------------------------------------------*/
	public Saver getSaver()
	{
		return getCurrentCampaign().saver;
	}

	/*-------------------------------------------------------------------------*/
	public Saver getSaver(Campaign campaign)
	{
		// the current campaign should always be index 0
		CampaignCache c = getCurrentCampaign();
		if (!c.campaign.getName().equals(campaign.getName()))
		{
			throw new MazeException("Expected campaign ["+c.campaign.getName()+"], " +
				"got ["+ campaign.getName()+"]");
		}
		return c.saver;
	}

	/*-------------------------------------------------------------------------*/
	public Loader getLoader()
	{
		return getCurrentCampaign().loader;
	}

	/*-------------------------------------------------------------------------*/
	public Database() throws Exception
	{
		this(null, null, Maze.getInstance().getCampaign());
	}

	/*-------------------------------------------------------------------------*/
	public Database(Loader loader, Saver saver, Campaign campaign) throws Exception
	{
		this.loader = loader;
		this.saver = saver;
		this.campaign = campaign;

		if (campaign != null && loader != null && saver != null)
		{
			loader.init(campaign);
			saver.init(campaign);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void initImpls() throws Exception
	{
		Map<String, String> config = Launcher.getConfig();
		initCampaignCache(loader, saver, campaign, config);
	}

	/*-------------------------------------------------------------------------*/
	public void initCaches(ProgressListener p)
	{
		// init caches
		if (p != null) p.message(StringUtil.getUiLabel("ls.load.genders"));
		getGenders();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.body.parts"));
		getBodyParts();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.xp.tables"));
		getExperienceTables();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.attack.types"));
		getAttackTypes();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.condition.effects"));
		getConditionEffects();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.condition.templates"));
		getConditionTemplates();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.spell.effects"));
		getSpellEffects();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.loot.entries"));
		getLootEntries();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.loot.tables"));
		getLootTables();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.maze.scripts"));
		getMazeScripts();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.spells"));
		getSpells();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.starting.kits"));
		getStartingKits();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.character.classes"));
		getCharacterClasses();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.player.spell.books"));
		getPlayerSpellBooks();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.natural.weapons"));
		getNaturalWeapons();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.races"));
		getRaces();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.maze.textures"));
		getMazeTextures();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.foe.types"));
		getFoeTypes();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.foe.templates"));
		getFoeTemplates();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.foe.entries"));
		getFoeEntries();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.encounter.tables"));
		getEncounterTables();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.traps"));
		getTraps();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.npc.faction.templates"));
		getNpcFactionTemplates();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.npc.templates"));
		getNpcTemplates();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.wielding.combos"));
		getWieldingCombos();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.item.templates"));
		getItemTemplates();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.difficulty.levels"));
		getDifficultyLevels();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.craft.recipes"));
		getCraftRecipes();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.item.enchantments"));
		getItemEnchantments();
		if (p != null) p.incProgress(1);

		if (p != null) p.message(StringUtil.getUiLabel("ls.load.personalities"));
		getPersonalities();
		if (p != null) p.incProgress(1);
	}

	/*-------------------------------------------------------------------------*/
	private void initCampaignCache(
		Loader loader,
		Saver saver,
		Campaign campaign,
		Map<String, String> config) throws Exception
	{
//		System.out.println("Database.initCampaignCache: "+campaign);

		if (loader == null)
		{
			String loader_impl = config.get(Maze.AppConfig.DB_LOADER_IMPL);
			Class<Loader> loader_class = (Class<Loader>)Class.forName(loader_impl);
			loader = (Loader)loader_class.newInstance();
		}

		if (saver == null)
		{
			String saver_impl = config.get(Maze.AppConfig.DB_SAVER_IMPL);
			Class<Saver> saver_class = (Class<Saver>)Class.forName(saver_impl);
			saver = (Saver)saver_class.newInstance();
		}

		CampaignCache cache = new CampaignCache(loader, saver, campaign);

		this.campaignCaches.add(cache);

		cache.init();

		if (campaign.getParentCampaign() != null)
		{
			Campaign parent = getCampaigns().get(campaign.getParentCampaign());
			initCampaignCache(null, null, parent, config);
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return the user-selected campaign
	 */
	private CampaignCache getCurrentCampaign()
	{
		return campaignCaches.get(0);
	}

	/*-------------------------------------------------------------------------*/
	public static Database getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/
	public synchronized static Map<String, Campaign> getCampaigns() throws IOException
	{
		if (campaignMap == null)
		{
			campaignMap = new HashMap<>();
			List<Campaign> campaigns = loadCampaigns();

			for (Campaign c : campaigns)
			{
				campaignMap.put(c.getName(), c);
			}
		}

		return campaignMap;
	}

	/*-------------------------------------------------------------------------*/
	private static List<Campaign> loadCampaigns() throws IOException
	{
		File dir = new File("./data");
		if (!dir.isDirectory())
		{
			throw new MazeException("Cannot locate data directory ["+dir.getCanonicalPath()+"]");
		}

		List<File> propertiesFiles = new ArrayList<>();
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isDirectory())
			{
				propertiesFiles.add(new File(files[i], "campaign.cfg"));
			}
		}

		List<Campaign> result = new ArrayList<>();

		for (File f : propertiesFiles)
		{
			if (!f.exists())
			{
				throw new MazeException("Cannot locate campaign file ["+f.getCanonicalPath()+"]");
			}

			Properties p = new Properties();
			FileInputStream fis = new FileInputStream(f);
			p.load(fis);
			fis.close();

			String name = f.getParentFile().getName().split("\\.")[0];
			String displayName = p.getProperty("displayName");
			String parentCampaign = "".equals(p.getProperty("parentCampaign"))?null:p.getProperty("parentCampaign");
			String description = V1Utils.replaceNewlines(p.getProperty("description"));
			String startingScript = p.getProperty("startingScript");
			String defaultRace = p.getProperty("defaultRace");
			String defaultPortrait = p.getProperty("defaultPortrait");
			String introScript = p.getProperty("introScript");

			result.add(new Campaign(
				name,
				displayName,
				description,
				parentCampaign,
				startingScript,
				defaultRace,
				defaultPortrait,
				introScript));
		}

		if (result.size() == 0)
		{
			throw new MazeException("No campaigns found!");
		}

		return result;
	}

	//////////////////////////////////////////////////
	// Up-front cached data access
	//////////////////////////////////////////////////

	/*-------------------------------------------------------------------------*/
	public Gender getGender(String name)
	{
		synchronized(mutex)
		{
			Gender result = getGenders().get(name);
			if(result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, Gender> getGenders()
	{
		synchronized(mutex)
		{
			if (genders == null)
			{
				genders = (Map<String, Gender>)mergeMaps(Loader::loadGenders);
			}
			return genders;
		}
	}

	public void saveGenders(Map<String, Gender> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveGenders((Map<String, Gender>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public CharacterClass getCharacterClass(String name)
	{
		synchronized(mutex)
		{
			CharacterClass result = getCharacterClasses().get(name);
			if(result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, CharacterClass> getCharacterClasses()
	{
		synchronized(mutex)
		{
			if (characterClasses == null)
			{
				characterClasses = (Map<String, CharacterClass>)mergeMaps(Loader::loadCharacterClasses);
			}
			return characterClasses;
		}
	}

	public void saveCharacterClasses(Map<String, CharacterClass> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveCharacterClasses((Map<String, CharacterClass>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public AttackType getAttackType(String name)
	{
		synchronized(mutex)
		{
			AttackType result = getAttackTypes().get(name);
			if (result == null)
			{
				throw new MazeException("Invalid attack type ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, AttackType> getAttackTypes()
	{
		synchronized(mutex)
		{
			if (attackTypes == null)
			{
				attackTypes = (Map<String, AttackType>)mergeMaps(Loader::loadAttackTypes);
			}
			return attackTypes;
		}
	}

	public void saveAttackTypes(Map<String, AttackType> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveAttackTypes((Map<String, AttackType>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public ItemTemplate getItemTemplate(String name)
	{
		synchronized(mutex)
		{
			ItemTemplate result = getItemTemplates().get(name);
			if (result == null)
			{
				throw new MazeException("Invalid item template ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, ItemTemplate> getItemTemplates()
	{
		synchronized(mutex)
		{
			if (itemTemplates == null)
			{
				itemTemplates = (Map<String, ItemTemplate>)mergeMaps(Loader::loadItemTemplates);
			}
			return itemTemplates;
		}
	}

	public List<String> getItemList()
	{
		synchronized(mutex)
		{
			return new ArrayList<>(this.getItemTemplates().keySet());
		}
	}

	public void saveItemTemplates(Map<String, ItemTemplate> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveItemTemplates((Map<String, ItemTemplate>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public Spell getSpell(String spellName)
	{
		synchronized(mutex)
		{
			Spell result = this.getSpells().get(spellName);
			if (result == null)
			{
				throw new MazeException("Invalid spell ["+spellName+"]");
			}

			return result;
		}
	}

	public Map<String, Spell> getSpells()
	{
		synchronized(mutex)
		{
			if (spells == null)
			{
				spells = (Map<String, Spell>)mergeMaps(Loader::loadSpells);
			}
			return spells;
		}

	}

	public List<String> getSpellList()
	{
		synchronized(mutex)
		{
			return new ArrayList<>(this.getSpells().keySet());
		}
	}

	public void saveSpells(Map<String, Spell> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveSpells((Map<String, Spell>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public PlayerSpellBook getPlayerSpellBook(String book)
	{
		synchronized(mutex)
		{
			PlayerSpellBook result = this.getPlayerSpellBooks().get(book);
			if (result == null)
			{
				throw new MazeException("Invalid book ["+book+"]");
			}
			return result;
		}
	}

	public Map<String, PlayerSpellBook> getPlayerSpellBooks()
	{
		synchronized(mutex)
		{
			if (playerSpellBooks == null)
			{
				playerSpellBooks = (Map<String, PlayerSpellBook>)mergeMaps(Loader::loadPlayerSpellBooks);
			}
			return playerSpellBooks;
		}
	}

	public void savePlayerSpellBooks(Map<String, PlayerSpellBook> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).savePlayerSpellBooks((Map<String, PlayerSpellBook>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getMazeTexture(String textureName)
	{
		synchronized(mutex)
		{
			MazeTexture result = this.getMazeTextures().get(textureName);
			if (result == null)
			{
				throw new MazeException("invalid maze texture ["+textureName+"]");
			}
			return result;
		}
	}

	public Map<String, MazeTexture> getMazeTextures()
	{
		synchronized(mutex)
		{
			if (mazeTextures == null)
			{
				mazeTextures = (Map<String, MazeTexture>)mergeMaps(Loader::loadMazeTextures);
			}
			return mazeTextures;
		}
	}

	public void saveMazeTextures(Map<String, MazeTexture> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveMazeTextures((Map<String, MazeTexture>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public Race getRace(String name)
	{
		synchronized(mutex)
		{
			Race result = getRaces().get(name);
			if (result == null)
			{
				throw new MazeException("Invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, Race> getRaces()
	{
		synchronized(mutex)
		{
			if (races == null)
			{
				races = (Map<String, Race>)mergeMaps(Loader::loadRaces);
			}
			return races;
		}
	}

	public void saveRaces(Map<String, Race> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveRaces((Map<String, Race>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public BodyPart getBodyPart(String name)
	{
		synchronized(mutex)
		{
			BodyPart result = getBodyParts().get(name);
			if (result == null)
			{
				throw new MazeException("Invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, BodyPart> getBodyParts()
	{
		synchronized(mutex)
		{
			if (bodyParts == null)
			{
				bodyParts = (Map<String, BodyPart>)mergeMaps(Loader::loadBodyParts);
			}
			return bodyParts;
		}
	}

	public void saveBodyParts(Map<String, BodyPart> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveBodyParts((Map<String, BodyPart>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public LootTable getLootTable(String name)
	{
		synchronized(mutex)
		{
			LootTable lootTable = this.getLootTables().get(name);
			if (lootTable == null)
			{
				throw new MazeException("invalid loot table ["+name+"]");
			}
			return lootTable;
		}
	}

	public Map<String, LootTable> getLootTables()
	{
		synchronized(mutex)
		{
			if (lootTables == null)
			{
				lootTables = (Map<String, LootTable>)mergeMaps(Loader::loadLootTables);
			}
			return lootTables;
		}
	}

	public void saveLootTables(Map<String, LootTable> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveLootTables((Map<String, LootTable>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public LootEntry getLootEntry(String name)
	{
		synchronized(mutex)
		{
			LootEntry lootEntry = this.getLootEntries().get(name);
			if (lootEntry == null)
			{
				throw new MazeException("invalid loot entry ["+name+"]");
			}
			return lootEntry;
		}
	}

	public Map<String, LootEntry> getLootEntries()
	{
		synchronized(mutex)
		{
			if (lootEntries == null)
			{
				lootEntries = (Map<String, LootEntry>)mergeMaps(Loader::loadLootEntries);
			}
			return lootEntries;
		}
	}

	public void saveLootEntries(Map<String, LootEntry> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveLootEntries((Map<String, LootEntry>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public Trap getTrap(String name)
	{
		synchronized(mutex)
		{
			Trap trap = getTraps().get(name);
			if (trap == null)
			{
				throw new MazeException("invalid trap ["+name+"]");
			}
			return trap;
		}
	}

	public Map<String, Trap> getTraps()
	{
		synchronized(mutex)
		{
			if (traps == null)
			{
				traps = (Map<String, Trap>)mergeMaps(Loader::loadTraps);
			}
			return traps;
		}
	}

	public void saveTraps(Map<String, Trap> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveTraps((Map<String, Trap>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcTemplate> getNpcTemplates()
	{
		synchronized(mutex)
		{
			if (npcTemplates == null)
			{
				npcTemplates = (Map<String, NpcTemplate>)mergeMaps(Loader::loadNpcTemplates);
			}
			return npcTemplates;
		}
	}

	public void saveNpcTemplates(Map<String, NpcTemplate> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveNpcTemplates((Map<String, NpcTemplate>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public FoeTemplate getFoeTemplate(String name)
	{
		synchronized(mutex)
		{
			FoeTemplate result = this.getFoeTemplates().get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, FoeTemplate> getFoeTemplates()
	{
		synchronized(mutex)
		{
			if (foeTemplates == null)
			{
				foeTemplates = (Map<String, FoeTemplate>)mergeMaps(Loader::loadFoeTemplates);
			}
			return foeTemplates;
		}
	}

	public void saveFoeTemplates(Map<String, FoeTemplate> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveFoeTemplates((Map<String, FoeTemplate>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public EncounterTable getEncounterTable(String name)
	{
		synchronized(mutex)
		{
			EncounterTable result = this.getEncounterTables().get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, EncounterTable> getEncounterTables()
	{
		synchronized(mutex)
		{
			if (encounterTables == null)
			{
				encounterTables = (Map<String, EncounterTable>)mergeMaps(Loader::loadEncounterTables);
			}
			return encounterTables;
		}
	}

	public void saveEncounterTables(Map<String, EncounterTable> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveEncounterTables((Map<String, EncounterTable>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public FoeEntry getFoeEntry(String name)
	{
		synchronized(mutex)
		{
			FoeEntry result = this.getFoeEntries().get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, FoeEntry> getFoeEntries()
	{
		synchronized(mutex)
		{
			if (foeEntries == null)
			{
				foeEntries = (Map<String, FoeEntry>)mergeMaps(Loader::loadFoeEntries);
			}
			return foeEntries;
		}
	}

	public void saveFoeEntries(Map<String, FoeEntry> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveFoeEntries((Map<String, FoeEntry>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public WieldingCombo getWieldingCombo(String name)
	{
		synchronized(mutex)
		{
			// in this case we actually want to return null if one is not present
			return this.getWieldingCombos().get(name);
		}
	}

	public Map<String, WieldingCombo> getWieldingCombos()
	{
		synchronized(mutex)
		{
			if (wieldingCombos == null)
			{
				wieldingCombos = (Map<String, WieldingCombo>)mergeMaps(Loader::loadWieldingCombos);
			}
			return wieldingCombos;
		}
	}

	public void saveWieldingCombos(Map<String, WieldingCombo> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveWieldingCombos((Map<String, WieldingCombo>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public ConditionEffect getConditionEffect(String name)
	{
		synchronized(mutex)
		{
			ConditionEffect result = this.getConditionEffects().get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, ConditionEffect> getConditionEffects()
	{
		synchronized(mutex)
		{
			if (conditionEffects == null)
			{
				conditionEffects = (Map<String, ConditionEffect>)mergeMaps(Loader::loadConditionEffects);

			}
			return conditionEffects;
		}
	}

	public void saveConditionEffects(Map<String, ConditionEffect> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveConditionEffects((Map<String, ConditionEffect>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public SpellEffect getSpellEffect(String name)
	{
		synchronized(mutex)
		{
			SpellEffect result = this.getSpellEffects().get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, SpellEffect> getSpellEffects()
	{
		synchronized(mutex)
		{
			if (spellEffects == null)
			{
				spellEffects = (Map<String, SpellEffect>)mergeMaps(Loader::loadSpellEffects);
			}
			return spellEffects;
		}
	}

	public void saveSpellEffects(Map<String, SpellEffect> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveSpellEffects((Map<String, SpellEffect>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public ConditionTemplate getConditionTemplate(String name)
	{
		synchronized(mutex)
		{
			ConditionTemplate result = this.getConditionTemplates().get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, ConditionTemplate> getConditionTemplates()
	{
		synchronized(mutex)
		{
			if (conditionTemplates == null)
			{
				conditionTemplates = (Map<String, ConditionTemplate>)mergeMaps(Loader::loadConditionTemplates);
			}
			return conditionTemplates;
		}
	}

	public void saveConditionTemplates(Map<String, ConditionTemplate> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveConditionTemplates((Map<String, ConditionTemplate>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcFactionTemplate> getNpcFactionTemplates()
	{
		synchronized(mutex)
		{
			if (npcFactionTemplates == null)
			{
				npcFactionTemplates = (Map<String, NpcFactionTemplate>)mergeMaps(Loader::loadNpcFactionTemplates);
			}
			return npcFactionTemplates;
		}
	}

	public void saveNpcFactionTemplates(Map<String, NpcFactionTemplate> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveNpcFactionTemplates((Map<String, NpcFactionTemplate>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public MazeScript getMazeScript(String name)
	{
		synchronized(mutex)
		{
			MazeScript result = this.getMazeScripts().get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, MazeScript> getMazeScripts()
	{
		synchronized(mutex)
		{
			if (mazeScripts == null)
			{
				mazeScripts = (Map<String, MazeScript>)mergeMaps(Loader::loadMazeScripts);
			}
			return mazeScripts;
		}
	}

	public void saveMazeScripts(Map<String, MazeScript> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveMazeScripts((Map<String, MazeScript>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public ExperienceTable getExperienceTable(String name)
	{
		synchronized(mutex)
		{
			ExperienceTable result = this.getExperienceTables().get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String, ExperienceTable> getExperienceTables()
	{
		synchronized(mutex)
		{
			synchronized(mutex)
			{
				if (experienceTables == null)
				{
					experienceTables = (Map<String, ExperienceTable>)mergeMaps(Loader::loadExperienceTables);
				}
				return experienceTables;
			}
		}
	}

	public void saveExperienceTables(Map<String, ExperienceTable> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveExperienceTables((Map<String, ExperienceTable>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, FoeType> getFoeTypes()
	{
		synchronized(mutex)
		{
			if (foeTypes == null)
			{
				foeTypes = (Map<String, FoeType>)mergeMaps(Loader::loadFoeTypes);
			}
			return foeTypes;
		}
	}

	public FoeType getFoeType(String name)
	{
		synchronized(mutex)
		{
			FoeType result = this.getFoeTypes().get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public void saveFoeTypes(Map<String, FoeType> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveFoeTypes((Map<String, FoeType>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, DifficultyLevel> getDifficultyLevels()
	{
		synchronized(mutex)
		{
			if (difficultyLevels == null)
			{
				difficultyLevels = (Map<String, DifficultyLevel>)mergeMaps(Loader::loadDifficultyLevels);
			}
			return difficultyLevels;
		}
	}

	public void saveDifficultyLevels(Map<String, DifficultyLevel> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveDifficultyLevels((Map<String, DifficultyLevel>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, CraftRecipe> getCraftRecipes()
	{
		synchronized(mutex)
		{
			if (craftRecipes == null)
			{
				craftRecipes = (Map<String, CraftRecipe>)mergeMaps(Loader::loadCraftRecipes);
			}
			return craftRecipes;
		}
	}

	public void saveCraftRecipes(Map<String, CraftRecipe> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveCraftRecipes((Map<String, CraftRecipe>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ItemEnchantments> getItemEnchantments()
	{
		synchronized(mutex)
		{
			if (itemEnchantments == null)
			{
				itemEnchantments = (Map<String, ItemEnchantments>)mergeMaps(Loader::loadItemEnchantments);
			}
			return itemEnchantments;
		}
	}

	public void saveItemEnchantments(Map<String, ItemEnchantments> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveItemEnchantments((Map<String, ItemEnchantments>)filterMap(map, campaign));
	}


	/*-------------------------------------------------------------------------*/
	public Map<String, Personality> getPersonalities()
	{
		synchronized(mutex)
		{
			if (personalities == null)
			{
				personalities = (Map<String, Personality>)mergeMaps(Loader::loadPersonalities);
			}
			return personalities;
		}
	}

	public void savePersonalities(Map<String, Personality> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).savePersonalities((Map<String, Personality>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public NaturalWeapon getNaturalWeapon(String name)
	{
		synchronized(mutex)
		{
			NaturalWeapon result = this.getNaturalWeapons().get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String,NaturalWeapon> getNaturalWeapons()
	{
		synchronized(mutex)
		{
			if (naturalWeapons == null)
			{
				naturalWeapons = (Map<String, NaturalWeapon>)mergeMaps(Loader::loadNaturalWeapons);
			}
			return naturalWeapons;
		}
	}

	public void saveNaturalWeapons(Map<String, NaturalWeapon> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveNaturalWeapons((Map<String, NaturalWeapon>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public StartingKit getStartingKit(String name)
	{
		synchronized(mutex)
		{
			StartingKit result = this.getStartingKits().get(name);
			if (result == null)
			{
				throw new MazeException("invalid name ["+name+"]");
			}
			return result;
		}
	}

	public Map<String,StartingKit> getStartingKits()
	{
		synchronized(mutex)
		{
			if (startingKits == null)
			{
				startingKits = (Map<String, StartingKit>)mergeMaps(Loader::loadStartingKits);
			}
			return startingKits;
		}
	}

	public void saveStartingKits(Map<String, StartingKit> map, Campaign campaign) throws Exception
	{
		getSaver(campaign).saveStartingKits((Map<String, StartingKit>)filterMap(map, campaign));
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, PlayerCharacter> getCharacterGuild()
	{
		synchronized(mutex)
		{
			if (characterGuild == null)
			{
				// can only play with characters created for this campaign
				characterGuild = getCurrentCampaign().loader.loadCharacterGuild();
			}
			return characterGuild;
		}
	}

	//////////////////////////////////////////////////
	// Non-cached data access
	//////////////////////////////////////////////////

	/*-------------------------------------------------------------------------*/
	public Font getFont(String name)
	{
		// try to load from all campaigns, starting with the current one
		synchronized(mutex)
		{
			List<MazeException> errors = new ArrayList<>();

			for (CampaignCache cc : campaignCaches)
			{
				try
				{
					return cc.loader.getFont(name);
				}
				catch (MazeException e)
				{
					errors.add(e);
				}
			}

			MazeException me = new MazeException("Can't load [" + name + "]");
			errors.forEach(me::addSuppressed);
			throw me;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Clip getClip(String clipName)
	{
		// try to load from all campaigns, starting with the current one
		synchronized(mutex)
		{
			List<MazeException> errors = new ArrayList<>();

			for (CampaignCache cc : campaignCaches)
			{
				try
				{
					return cc.loader.getClip(clipName, Maze.getInstance().getAudioPlayer());
				}
				catch (MazeException e)
				{
					errors.add(e);
				}
			}

			MazeException me = new MazeException("Can't load [" + clipName + "]");
			errors.forEach(me::addSuppressed);
			throw me;
		}
	}

	/*-------------------------------------------------------------------------*/
	public InputStream getMusic(String trackName)
	{
		// try to load from all campaigns, starting with the current one
		synchronized(mutex)
		{
			List<MazeException> errors = new ArrayList<>();

			for (CampaignCache cc : campaignCaches)
			{
				try
				{
					return cc.loader.getMusic(trackName);
				}
				catch (MazeException e)
				{
					errors.add(e);
				}
			}

			MazeException me = new MazeException("Can't load [" + trackName + "]");
			errors.forEach(me::addSuppressed);
			throw me;
		}
	}


	//////////////////////////////////////////////////
	// Lazy-cached data access
	//////////////////////////////////////////////////

	/*-------------------------------------------------------------------------*/
	/**
	 * @param resourceName
	 * 	Image resource name split by forward slashes.
	 */
	public BufferedImage getImage(String resourceName)
	{
		synchronized(mutex)
		{
			if (!images.containsKey(resourceName))
			{
				// check all the campaigns, return the first image we find
				for (CampaignCache campaignCache : campaignCaches)
				{
					BufferedImage image = campaignCache.loader.getImage(resourceName);
					if (image != null)
					{
						images.put(resourceName, image);
						return image;
					}
				}

				throw new MazeException("invalid image resource ["+resourceName+"]");
			}
			else
			{
				return images.get(resourceName);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public String getString(String namespace, String key, boolean allowNull)
	{
		// try to load from all campaigns, starting with the current one
		synchronized(mutex)
		{
			if (strings.containsKey(namespace))
			{
				Map<String, String> namespaceCache = strings.get(namespace);
				String s = namespaceCache.get(key);
				if (s != null)
				{
					return s;
				}
			}
			else
			{
				strings.put(namespace, new HashMap<>());
			}

			// not cached yet, attempt to load
			List<MazeException> errors = new ArrayList<>();

			for (CampaignCache cc : campaignCaches)
			{
				try
				{
					String s = getString(namespace, key, allowNull, cc);
					strings.get(namespace).put(key, s);
					return s;
				}
				catch (MazeException e)
				{
					errors.add(e);
				}
			}

			MazeException me = new MazeException("Can't load [" + key + "]");
			errors.forEach(me::addSuppressed);
			throw me;
		}
	}

	/*-------------------------------------------------------------------------*/
	private String getString(String namespace, String key, boolean allowNull, CampaignCache campaignCache)
	{
		synchronized(mutex)
		{
			String result = campaignCache.loader.getStringManager().getString(namespace, key);
			if (result == null)
			{
				// special case: retry one time to load the resource bundle
				campaignCache.loader.initStringManager();
				result = campaignCache.loader.getStringManager().getString(namespace, key);

				if (result == null && !allowNull)
				{
					throw new MazeException("Invalid key ["+key+"]");
				}
			}
			return result;
		}
	}

	//////////////////////////////////////////////
	// various special cases
	//////////////////////////////////////////////

	/*-------------------------------------------------------------------------*/
	public List<String> getPortraitNames()
	{
		// Go through all the campaigns and get all the portrait names.
		// the lookup and caching in getImage will handle the rest

		synchronized(mutex)
		{
			List<String> result = new ArrayList<>();
			for (CampaignCache cc : campaignCaches)
			{
				result.addAll(cc.loader.getPortraitNames());
			}
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getZoneNames()
	{
		// only zones from the current campaign are available, no inheritance

		synchronized(mutex)
		{
			return this.getCurrentCampaign().loader.getZoneNames();
		}
	}

	/*-------------------------------------------------------------------------*/
	public Zone getZone(String name)
	{
		// only zones from the current campaign are available, no inheritance

		synchronized(mutex)
		{
			return this.getCurrentCampaign().loader.getZone(name);
		}
	}

	//////////////////////////////////////////////
	/// Non-campaign data
	//////////////////////////////////////////////

	/*-------------------------------------------------------------------------*/
	public UserConfig getUserConfig()
	{
		synchronized (mutex)
		{
			if (userConfig == null)
			{
				try
				{
					// we can use any loader to load this
					userConfig = new V2Loader().loadUserConfig();
				}
				catch (Exception e)
				{
					throw new MazeException("Can't load user config.",e);
				}
			}

			return userConfig;
		}
	}

	///////////////////////////////////////////////
	// Utility methods
	///////////////////////////////////////////////

	/*-------------------------------------------------------------------------*/
	private Map<String, ? extends DataObject> mergeMaps(Function<Loader, Map<String, ? extends DataObject>> loadMethod)
	{
		Map<String, DataObject> result = new HashMap<>();

		for (int i = campaignCaches.size()-1; i >= 0; i--)
		{
			CampaignCache c = campaignCaches.get(i);
			loadMethod.apply(c.loader).forEach(result::put);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Map<String, ? extends DataObject> filterMap(
		Map<String, ? extends DataObject> map,
		Campaign campaign)
	{
		Map<String, DataObject> result = new HashMap<>();

		for (DataObject d : map.values())
		{
			if (d.getCampaign() == null)
			{
				throw new MazeException("invalid NULL campaign: "+d.getName()+"\n"+d);
			}
			if (campaign.getName().equals(d.getCampaign()))
			{
				result.put(d.getName(), d);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static class CampaignCache
	{
		private Loader loader;
		private Saver saver;
		private Campaign campaign;

		public CampaignCache(Loader loader, Saver saver, Campaign campaign) throws Exception
		{
			this.loader = loader;
			this.saver = saver;
			this.campaign = campaign;
		}

		public void init() throws Exception
		{
			loader.init(campaign);
			saver.init(campaign);
		}
	}
}
