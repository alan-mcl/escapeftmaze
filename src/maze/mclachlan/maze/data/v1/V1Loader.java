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

import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import mclachlan.maze.audio.AudioPlayer;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.StringManager;
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
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1Loader extends Loader
{

	private Campaign campaign;

	private V1StringManager stringManager;
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
	private Map<String, MazeScript> scripts;
	private Map<String, LootEntry> lootEntries;
	private Map<String, LootTable> lootTables;
	private Map<String, Spell> spells;
	private Map<String, PlayerSpellBook> playerSpellBooks;
	private Map<String, MazeTexture> textures;
	private Map<String, FoeAttack> foeAttacks;
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
	private Map<String, NaturalWeapon> naturalWeapons;

	private Map<String, PlayerCharacter> characterGuild;
	private Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();

	/*-------------------------------------------------------------------------*/
	public V1Loader()
	{
	}

	/*-------------------------------------------------------------------------*/
	public void init(Campaign campaign) throws Exception
	{
		this.campaign = campaign;

		BufferedReader reader;
		String path = getPath();

		stringManager = new V1StringManager(path);

		reader = getReader(path+V1Utils.GENDERS);
		genders = V1Gender.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.BODY_PARTS);
		bodyParts = V1BodyPart.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.EXPERIENCE_TABLES);
		experienceTables = V1ExperienceTable.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.ATTACK_TYPES);
		attackTypes = V1AttackType.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.CONDITION_EFFECTS);
		conditionEffects = V1ConditionEffect.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.CONDITION_TEMPLATES);
		conditionTemplates = V1ConditionTemplate.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.SPELL_EFFECTS);
		spellEffects = V1SpellEffect.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.LOOT_ENTRIES);
		lootEntries = V1LootEntry.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.LOOT_TABLES);
		lootTables = V1LootTable.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.MAZE_SCRIPTS);
		scripts = V1Script.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.SPELLS);
		spells = V1Spell.load(reader);
		reader.close();
		
		reader = getReader(path+V1Utils.STARTING_KITS);
		startingKits = V1StartingKit.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.CHARACTER_CLASSES);
		characterClasses = V1CharacterClass.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.PLAYER_SPELL_BOOKS);
		playerSpellBooks = V1PlayerSpellBook.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.NATURAL_WEAPONS);
		naturalWeapons = V1NaturalWeapons.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.RACES);
		races = V1Race.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.MAZE_TEXTURES);
		textures = V1MazeTexture.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.FOE_ATTACKS);
		foeAttacks = V1FoeAttack.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.FOE_TEMPLATES);
		foeTemplates = V1FoeTemplate.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.FOE_ENTRIES);
		foeEntries = V1FoeEntry.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.ENCOUNTER_TABLES);
		encounterTables = V1EncounterTable.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.TRAPS);
		traps = V1Trap.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.NPC_FACTION_TEMPLATES);
		npcFactionTemplates = V1NpcFactionTemplate.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.NPC_TEMPLATES);
		npcTemplates = V1NpcTemplate.load(this, reader);
		reader.close();

		reader = getReader(path+V1Utils.WIELDING_COMBOS);
		wieldingCombos = V1WieldingCombo.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.ITEM_TEMPLATES);
		itemTemplates = V1ItemTemplate.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.DIFFICULTY_LEVELS);
		difficultyLevels = V1DifficultyLevel.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.CRAFT_RECIPES);
		craftRecipes = V1CraftRecipe.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.ITEM_ENCHANTMENTS);
		itemEnchantments = V1ItemEnchantments.load(reader);
		reader.close();

		reader = getReader(path+V1Utils.PERSONALITIES);
		personalities = V1Personalities.load(reader);
		reader.close();

		String savePath = getSavePath();

		reader = getReader(savePath+V1Utils.CHARACTER_GUILD);
		characterGuild = V1PlayerCharacter.load(reader);
		reader.close();
	}

	/*-------------------------------------------------------------------------*/
	private BufferedReader getReader(String str)
		throws FileNotFoundException
	{
		debug(str);
		BufferedReader reader;
		File file = new File(str);
		if (!file.exists())
		{
			// fake it out
			return new BufferedReader(new StringReader(""));
		}
		reader = new BufferedReader(new FileReader(file));
		return reader;
	}

	/*-------------------------------------------------------------------------*/
	private String getPath()
	{
		return "data/"+this.campaign.getName()+"/db/";
	}

	/*-------------------------------------------------------------------------*/
	private String getSavePath()
	{
		return "data/"+this.campaign.getName()+"/save/";
	}

	/*-------------------------------------------------------------------------*/
	public Map<String,Gender> loadGenders()
	{
		return genders;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String,Race> loadRaces()
	{
		return races;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, BodyPart> loadBodyParts()
	{
		return bodyParts;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, CharacterClass> loadCharacterClasses()
	{
		return characterClasses;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ExperienceTable> loadExperienceTables()
	{
		return experienceTables;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, AttackType> loadAttackTypes()
	{
		return attackTypes;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ConditionEffect> loadConditionEffects()
	{
		return conditionEffects;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ConditionTemplate> loadConditionTemplates()
	{
		return conditionTemplates;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, SpellEffect> loadSpellEffects()
	{
		return spellEffects;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, MazeScript> loadMazeScripts()
	{
		return scripts;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, LootEntry> loadLootEntries()
	{
		return lootEntries;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, LootTable> loadLootTables()
	{
		return lootTables;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Spell> loadSpells()
	{
		return spells;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, PlayerSpellBook> loadPlayerSpellBooks()
	{
		return playerSpellBooks;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, MazeTexture> loadMazeTextures()
	{
		return textures;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, FoeAttack> loadFoeAttacks()
	{
		return foeAttacks;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, FoeTemplate> loadFoeTemplates()
	{
		return foeTemplates;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Trap> loadTraps()
	{
		return traps;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, EncounterTable> loadEncounterTables()
	{
		return encounterTables;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, FoeEntry> loadFoeEntries()
	{
		return foeEntries;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcFactionTemplate> loadNpcFactionTemplates()
	{
		return npcFactionTemplates;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcTemplate> loadNpcTemplates()
	{
		return npcTemplates;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, WieldingCombo> loadWieldingCombos()
	{
		return wieldingCombos;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ItemTemplate> loadItemTemplates()
	{
		return itemTemplates;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, DifficultyLevel> loadDifficultyLevels()
	{
		return difficultyLevels;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, CraftRecipe> loadCraftRecipes()
	{
		return craftRecipes;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ItemEnchantments> loadItemEnchantments()
	{
		return itemEnchantments;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Personality> loadPersonalities()
	{
		return personalities;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NaturalWeapon> getNaturalWeapons()
	{
		return naturalWeapons;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, StartingKit> getStartingKits()
	{
		return startingKits;
	}

	/*-------------------------------------------------------------------------*/
	public StringManager getStringManager()
	{
		return stringManager;
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getZoneNames()
	{
		List<String> result = new ArrayList<String>();
		File dir = new File(getPath()+V1Utils.ZONES);
		if (dir.exists())
		{
			File[] files = dir.listFiles();
			for (File file : files)
			{
				result.add(file.getName().split("\\.")[0]);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Zone getZone(String name)
	{
		File file = new File(getPath()+V1Utils.ZONES+name+".txt");
		Zone result = null;

		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			result = V1Zone.load(reader);
			reader.close();
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Font getFont(String name)
	{
		try
		{
			String path = "data/"+campaign.getName()+"/font/";
			File fontFile = new File(path, name);
			return Font.createFont(Font.TRUETYPE_FONT, fontFile);
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, PlayerCharacter> loadCharacterGuild()
	{
		return characterGuild;
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getSaveGames()
	{
		List<String> result = new ArrayList<String>();
		File dir = new File(getSavePath());
		if (dir.exists())
		{
			File[] files = dir.listFiles();
			for (File file : files)
			{
				if (file.isDirectory())
				{
					result.add(file.getName().split("\\.")[0]);
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public GameState loadGameState(String saveGameName) throws Exception
	{
		BufferedReader reader = null;
		try
		{
			reader = getReader(getSavePath()+saveGameName+"/"+V1Utils.GAME_STATE);
			return V1GameState.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, PlayerCharacter> loadPlayerCharacters(String saveGameName) throws Exception
	{
		BufferedReader reader = null;
		try
		{
			reader = getReader(getSavePath()+saveGameName+"/"+V1Utils.PLAYER_CHARACTERS);
			return V1PlayerCharacter.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void loadMazeVariables(String saveGameName) throws Exception
	{
		BufferedReader reader = null;
		try
		{
			reader = getReader(getSavePath()+saveGameName+"/"+V1Utils.MAZE_VARIABLES);
			V1MazeVariables.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcFaction> loadNpcFactions(String saveGameName) throws Exception
	{
		BufferedReader reader = null;
		try
		{
			reader = getReader(getSavePath()+saveGameName+"/"+V1Utils.NPC_FACTIONS);
			return V1NpcFaction.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Npc> loadNpcs(String saveGameName) throws Exception
	{
		BufferedReader reader = null;
		try
		{
			reader = getReader(getSavePath()+saveGameName+"/"+V1Utils.NPCS);
			return V1Npc.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Map<ConditionBearer, List<Condition>> loadConditions(String saveGameName) throws Exception
	{
		BufferedReader reader = null;
		try
		{
			reader = getReader(getSavePath()+saveGameName+"/"+V1Utils.CONDITIONS);
			return V1ConditionManager.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Journal loadJournal(String saveGameName,
		String journalName) throws Exception
	{
		BufferedReader reader = null;
		try
		{
			reader = getReader(getSavePath()+saveGameName+"/"+V1Utils.JOURNALS+journalName+".txt");
			return V1Journal.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public UserConfig loadUserConfig() throws Exception
	{
		Reader reader = getReader(V1Utils.USER_CONFIG);
		Properties p = new Properties();
		p.load(reader);
		reader.close();
		return new UserConfig(p);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Map<String, Map<Point, List<Item>>> loadItemCaches(String saveGameName) throws Exception
	{
		BufferedReader reader = null;
		try
		{
			reader = getReader(getSavePath()+saveGameName+"/"+V1Utils.ITEM_CACHES);
			return V1ItemCache.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public PlayerTilesVisited loadPlayerTilesVisited(
		String saveGameName) throws Exception
	{
		BufferedReader reader = null;
		try
		{
			reader = getReader(getSavePath()+saveGameName+"/"+V1Utils.TILES_VISITED);
			return V1TilesVisited.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public BufferedImage getImage(String resourceName)
	{
		try
		{
			if (!images.containsKey(resourceName))
			{
				File file = new File("data/"+campaign.getName()+"/img/"+resourceName+".png");
				if (!file.exists())
				{
					file = new File("data/"+campaign.getName()+"/img/"+resourceName+".jpg");
					if (!file.exists())
					{
						throw new MazeException("invalid image resource ["+file+"]");
					}
				}

				BufferedImage result = ImageIO.read(file);
				images.put(resourceName, result);
				return result;
			}
			else
			{
				return images.get(resourceName);
			}
		}
		catch (IOException e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Clip getClip(String clipName, AudioPlayer audioPlayer)
	{
		try
		{
			File file = new File("data/"+campaign.getName()+"/sound/"+clipName+".wav");
			if (!file.exists())
			{
				throw new MazeException("invalid audio resource ["+file+"]");
			}

			FileInputStream fis = new FileInputStream(file);
			return audioPlayer.getClip(clipName, fis);
		}
		catch (FileNotFoundException e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public InputStream getMusic(String trackName)
	{
		File f = new File("data/"+campaign.getName()+"/sound/track/"+trackName+".ogg");

		if (!f.exists())
		{
			throw new MazeException("track not there: ["+trackName+"]");
		}

		try
		{
			return new FileInputStream(f);
		}
		catch (FileNotFoundException e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<String> getPortraitNames()
	{
		List<String> result = new ArrayList<String>();
		File dir = new File("data/"+campaign.getName()+"/img/portrait");
		if (dir.exists() && dir.isDirectory())
		{
			File[] files = dir.listFiles();
			for (File file : files)
			{
				result.add("portrait/" + file.getName().split("\\.")[0]);
			}
		}

		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	void debug(String s)
	{
//		System.out.println(s);
	}
}
