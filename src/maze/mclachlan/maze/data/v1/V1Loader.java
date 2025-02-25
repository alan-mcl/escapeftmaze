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
import java.util.function.*;
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

	// to avoid loading properties files over and over again we still
	// cache strings at this level
	private V1StringManager stringManager;

	/*-------------------------------------------------------------------------*/
	public V1Loader()
	{
	}

	/*-------------------------------------------------------------------------*/
	public void init(Campaign campaign) throws Exception
	{
		this.campaign = campaign;
		initStringManager();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void initStringManager()
	{
		stringManager = new V1StringManager(getPath());
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
		return (Map<String, Gender>)doV1Crud(V1Utils.GENDERS, V1Gender::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String,Race> loadRaces()
	{
		return (Map<String, Race>)doV1Crud(V1Utils.RACES, V1Race::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, BodyPart> loadBodyParts()
	{
		return (Map<String, BodyPart>)doV1Crud(V1Utils.BODY_PARTS, V1BodyPart::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, CharacterClass> loadCharacterClasses()
	{
		return (Map<String, CharacterClass>)doV1Crud(V1Utils.CHARACTER_CLASSES, V1CharacterClass::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ExperienceTable> loadExperienceTables()
	{
		return (Map<String, ExperienceTable>)doV1Crud(V1Utils.EXPERIENCE_TABLES, V1ExperienceTable::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, AttackType> loadAttackTypes()
	{
		return (Map<String, AttackType>)doV1Crud(V1Utils.ATTACK_TYPES, V1AttackType::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ConditionEffect> loadConditionEffects()
	{
		return (Map<String, ConditionEffect>)doV1Crud(V1Utils.CONDITION_EFFECTS, V1ConditionEffect::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ConditionTemplate> loadConditionTemplates()
	{
		return (Map<String, ConditionTemplate>)doV1Crud(V1Utils.CONDITION_TEMPLATES, V1ConditionTemplate::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, SpellEffect> loadSpellEffects()
	{
		return (Map<String, SpellEffect>)doV1Crud(V1Utils.SPELL_EFFECTS, V1SpellEffect::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, MazeScript> loadMazeScripts()
	{
		return (Map<String, MazeScript>)doV1Crud(V1Utils.MAZE_SCRIPTS, V1MazeScript::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, LootEntry> loadLootEntries()
	{
		return (Map<String, LootEntry>)doV1Crud(V1Utils.LOOT_ENTRIES, V1LootEntry::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, LootTable> loadLootTables()
	{
		return (Map<String, LootTable>)doV1Crud(V1Utils.LOOT_TABLES, V1LootTable::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Spell> loadSpells()
	{
		return (Map<String, Spell>)doV1Crud(V1Utils.SPELLS, V1Spell::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, PlayerSpellBook> loadPlayerSpellBooks()
	{
		return (Map<String, PlayerSpellBook>)doV1Crud(V1Utils.PLAYER_SPELL_BOOKS, V1PlayerSpellBook::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, MazeTexture> loadMazeTextures()
	{
		return (Map<String, MazeTexture>)doV1Crud(V1Utils.MAZE_TEXTURES, V1MazeTexture::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, FoeTemplate> loadFoeTemplates()
	{
		return (Map<String, FoeTemplate>)doV1Crud(V1Utils.FOE_TEMPLATES, V1FoeTemplate::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Trap> loadTraps()
	{
		return (Map<String, Trap>)doV1Crud(V1Utils.TRAPS, V1Trap::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, EncounterTable> loadEncounterTables()
	{
		return (Map<String, EncounterTable>)doV1Crud(V1Utils.ENCOUNTER_TABLES, V1EncounterTable::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, FoeEntry> loadFoeEntries()
	{
		return (Map<String, FoeEntry>)doV1Crud(V1Utils.FOE_ENTRIES, V1FoeEntry::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcFactionTemplate> loadNpcFactionTemplates()
	{
		return (Map<String, NpcFactionTemplate>)doV1Crud(V1Utils.NPC_FACTION_TEMPLATES, V1NpcFactionTemplate::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcTemplate> loadNpcTemplates()
	{
		try (BufferedReader reader = getReader(getPath() + V1Utils.NPC_TEMPLATES))
		{
			Map<String, NpcTemplate> map = V1NpcTemplate.load(this, reader);
			map.forEach((key, value) -> value.setCampaign(campaign.getName()));
			return map;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, WieldingCombo> loadWieldingCombos()
	{
		return (Map<String, WieldingCombo>)doV1Crud(V1Utils.WIELDING_COMBOS, V1WieldingCombo::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ItemTemplate> loadItemTemplates()
	{
		return (Map<String, ItemTemplate>)doV1Crud(V1Utils.ITEM_TEMPLATES, V1ItemTemplate::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, DifficultyLevel> loadDifficultyLevels()
	{
		return (Map<String, DifficultyLevel>)doV1Crud(V1Utils.DIFFICULTY_LEVELS, V1DifficultyLevel::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, CraftRecipe> loadCraftRecipes()
	{
		return (Map<String, CraftRecipe>)doV1Crud(V1Utils.CRAFT_RECIPES, V1CraftRecipe::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, ItemEnchantments> loadItemEnchantments()
	{
		return (Map<String, ItemEnchantments>)doV1Crud(V1Utils.ITEM_ENCHANTMENTS, V1ItemEnchantments::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Personality> loadPersonalities()
	{
		return (Map<String, Personality>)doV1Crud(V1Utils.PERSONALITIES, V1Personalities::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NaturalWeapon> loadNaturalWeapons()
	{
		return (Map<String, NaturalWeapon>)doV1Crud(V1Utils.NATURAL_WEAPONS, V1NaturalWeapons::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, StartingKit> loadStartingKits()
	{
		return (Map<String, StartingKit>)doV1Crud(V1Utils.STARTING_KITS, V1StartingKit::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Map<String, FoeType> loadFoeTypes()
	{
		return (Map<String, FoeType>)doV1Crud(V1Utils.FOE_TYPES, V1FoeTypes::load, campaign);
	}

	/*-------------------------------------------------------------------------*/
	public StringManager getStringManager()
	{
		return stringManager;
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getZoneNames()
	{
		List<String> result = new ArrayList<>();
		File dir = new File(getPath()+V1Utils.ZONES);
		if (dir.exists())
		{
			File[] files = dir.listFiles();
			for (File file : files)
			{
				String name = file.getName();
				result.add(name.substring(0, name.lastIndexOf(".")));
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
		try
		{
			BufferedReader reader = getReader(getSavePath() +V1Utils.CHARACTER_GUILD);
			Map<String, PlayerCharacter> characterGuild = V1PlayerCharacter.load(reader);
			reader.close();

			return characterGuild;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getSaveGames()
	{
		List<String> result = new ArrayList<>();
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
		try (BufferedReader reader = getReader(getSavePath() + saveGameName + "/" + V1Utils.GAME_STATE))
		{
			return V1GameState.load(reader);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, PlayerCharacter> loadPlayerCharacters(String saveGameName) throws Exception
	{
		try (BufferedReader reader = getReader(getSavePath() + saveGameName + "/" + V1Utils.PLAYER_CHARACTERS))
		{
			return V1PlayerCharacter.load(reader);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void loadMazeVariables(String saveGameName) throws Exception
	{
		try (BufferedReader reader = getReader(getSavePath() + saveGameName + "/" + V1Utils.MAZE_VARIABLES))
		{
			V1MazeVariables.load(reader);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, NpcFaction> loadNpcFactions(String saveGameName) throws Exception
	{
		try (BufferedReader reader = getReader(getSavePath() + saveGameName + "/" + V1Utils.NPC_FACTIONS))
		{
			return V1NpcFaction.load(reader);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, Npc> loadNpcs(String saveGameName) throws Exception
	{
		try (BufferedReader reader = getReader(getSavePath() + saveGameName + "/" + V1Utils.NPCS))
		{
			return V1Npc.load(reader);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Map<ConditionBearer, List<Condition>> loadConditions(
		String saveGameName, Map<String, PlayerCharacter> playerCharacterCache) throws Exception
	{
		try (BufferedReader reader = getReader(getSavePath() + saveGameName + "/" + V1Utils.CONDITIONS))
		{
			return V1ConditionManager.load(reader, playerCharacterCache);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Journal loadJournal(String saveGameName,
		String journalName) throws Exception
	{
		try (BufferedReader reader = getReader(getSavePath() + saveGameName + "/" + V1Utils.JOURNALS + journalName + ".txt"))
		{
			return V1Journal.load(reader);
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
		try (BufferedReader reader = getReader(getSavePath() + saveGameName + "/" + V1Utils.ITEM_CACHES))
		{
			return V1ItemCache.load(reader);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public PlayerTilesVisited loadPlayerTilesVisited(
		String saveGameName) throws Exception
	{
		try (BufferedReader reader = getReader(getSavePath() + saveGameName + "/" + V1Utils.TILES_VISITED))
		{
			return V1TilesVisited.load(reader);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public BufferedImage getImage(String resourceName)
	{
		try
		{
			// try both png and jpg extensions
			File file = new File("data/"+campaign.getName()+"/img/"+resourceName+".png");
			if (!file.exists())
			{
				file = new File("data/"+campaign.getName()+"/img/"+resourceName+".jpg");
				if (!file.exists())
				{
					return null;
				}
			}

			return ImageIO.read(file);
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
		List<String> result = new ArrayList<>();
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
	private Map<String, ? extends DataObject> doV1Crud(
		String path,
		Function<BufferedReader, Map<String, ? extends DataObject>> loadMethod,
		Campaign campaign)
	{
		try (BufferedReader reader = getReader(getPath() + path))
		{
			Map<String, ? extends DataObject> map = loadMethod.apply(reader);
			map.forEach((key, value) -> value.setCampaign(campaign.getName()));
			return map;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	void debug(String s)
	{
//		System.out.println(s);
	}
}
