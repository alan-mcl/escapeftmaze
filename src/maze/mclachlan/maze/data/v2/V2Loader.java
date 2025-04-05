package mclachlan.maze.data.v2;

import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;
import mclachlan.maze.audio.AudioPlayer;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.StringManager;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.data.v1.V1ConditionBearer;
import mclachlan.maze.data.v1.V1StringManager;
import mclachlan.maze.data.v1.V1Utils;
import mclachlan.maze.data.v2.serialisers.ListSerialiser;
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

import static mclachlan.maze.data.v2.serialisers.V2Files.*;
import static mclachlan.maze.data.v2.serialisers.V2SerialiserFactory.*;

/**
 *
 */
public class V2Loader extends Loader
{
	// to avoid loading properties files over and over again we still
	// cache strings at this level
	private V1StringManager stringManager;

	private Campaign campaign;
	private Database db;

	/*-------------------------------------------------------------------------*/
	public void init(Campaign campaign) throws Exception
	{
		this.campaign = campaign;
		db = Database.getInstance();
		initStringManager();
	}

	/*-------------------------------------------------------------------------*/
	private String getPath(String file)
	{
		return "data/"+this.campaign.getName()+"/db/"+file;
	}

	/*-------------------------------------------------------------------------*/
	private String getSavePath()
	{
		return "data/"+this.campaign.getName()+"/save/";
	}

	/*-------------------------------------------------------------------------*/
	private BufferedReader getReader(String str)
		throws IOException
	{
		BufferedReader reader;
		File file = new File(str);
		if (!file.exists())
		{
			// fake it out
			throw new MazeException("invalid: ["+str+"]");
		}
		reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
		return reader;
	}

	/*-------------------------------------------------------------------------*/

	private Map v2Crud(String path, V2SiloMap silo)
	{
		try (BufferedReader reader = getReader(path))
		{
			Map result = silo.load(reader, db);
			result.forEach((key, value) -> ((DataObject)value).setCampaign(campaign.getName()));
			return result;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	private Object v2Crud(String path, V2SiloSingleton silo)
	{
		try (BufferedReader reader = getReader(path))
		{
			Object result = silo.load(reader, db);
			return result;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	//
	// data
	//

	@Override
	public Map<String, Gender> loadGenders()
	{
		return v2Crud(getPath(GENDERS), new SimpleMapSilo<>(getGenderSerialiser()));
	}

	@Override
	public Map<String, Race> loadRaces()
	{
		Map<String, Race> result = v2Crud(getPath(RACES), new SimpleMapSilo<>(getRaceSerialiser(db)));
		result.forEach((key, value) -> value.initBodyParts());
		return result;
	}

	@Override
	public Map<String, BodyPart> loadBodyParts()
	{
		return v2Crud(getPath(BODY_PARTS), new SimpleMapSilo<>(getBodyPartSerialiser()));
	}

	@Override
	public Map<String, CharacterClass> loadCharacterClasses()
	{
		return v2Crud(getPath(CHARACTER_CLASSES), new SimpleMapSilo<>(getCharacterClassSerialiser(db)));
	}

	@Override
	public Map<String, ExperienceTable> loadExperienceTables()
	{
		return v2Crud(getPath(EXPERIENCE_TABLES), new SimpleMapSilo<>(getExperienceTableSerialiser()));
	}

	@Override
	public Map<String, AttackType> loadAttackTypes()
	{
		return v2Crud(getPath(ATTACK_TYPES), new SimpleMapSilo<>(getAttackTypeSerialiser()));
	}

	@Override
	public Map<String, ConditionEffect> loadConditionEffects()
	{
		return v2Crud(getPath(CONDITION_EFFECTS), new SimpleMapSilo<>(getConditionEffectSerialiser()));
	}

	@Override
	public Map<String, ConditionTemplate> loadConditionTemplates()
	{
		return v2Crud(getPath(CONDITION_TEMPLATES), new SimpleMapSilo<>(getConditionTemplateSerialiser(db)));
	}

	@Override
	public Map<String, SpellEffect> loadSpellEffects()
	{
		return v2Crud(getPath(SPELL_EFFECTS), new SimpleMapSilo<>(getSpellEffectSerialiser(db)));
	}

	@Override
	public Map<String, MazeScript> loadMazeScripts()
	{
		return v2Crud(getPath(MAZE_SCRIPTS), new SimpleMapSilo<>(getMazeScriptSerialiser(db)));
	}

	@Override
	public Map<String, LootEntry> loadLootEntries()
	{
		return v2Crud(getPath(LOOT_ENTRIES), new SimpleMapSilo<>(getLootEntrySerialiser(db)));
	}

	@Override
	public Map<String, LootTable> loadLootTables()
	{
		return v2Crud(getPath(LOOT_TABLES), new SimpleMapSilo<>(getLootTableSerialiser(db)));
	}

	@Override
	public Map<String, Spell> loadSpells()
	{
		return v2Crud(getPath(SPELLS), new SimpleMapSilo<>(getSpellsSerialiser(db)));
	}

	@Override
	public Map<String, PlayerSpellBook> loadPlayerSpellBooks()
	{
		return v2Crud(getPath(PLAYER_SPELL_BOOKS), new SimpleMapSilo<>(getPlayerSpellBooksSerialiser(db)));
	}

	@Override
	public Map<String, MazeTexture> loadMazeTextures()
	{
		Map<String, MazeTexture> map = v2Crud(getPath(MAZE_TEXTURES), new SimpleMapSilo<>(getMazeTextureSerialiser()));
		map.forEach((k, v) -> v.initRenderTexture());
		return map;
	}

	@Override
	public Map<String, FoeTemplate> loadFoeTemplates()
	{
		Map<String, FoeTemplate> map = v2Crud(getPath(FOE_TEMPLATES), new SimpleMapSilo<>(getFoeTemplateSerialiser(db)));
		map.forEach((k, v) -> v.init());
		return map;
	}

	@Override
	public Map<String, Trap> loadTraps()
	{
		return v2Crud(getPath(TRAPS), new SimpleMapSilo<>(getTrapSerialiser(db)));
	}

	@Override
	public Map<String, FoeEntry> loadFoeEntries()
	{
		return v2Crud(getPath(FOE_ENTRIES), new SimpleMapSilo<>(getFoeEntrySerialiser(db)));
	}

	@Override
	public Map<String, EncounterTable> loadEncounterTables()
	{
		return v2Crud(getPath(ENCOUNTER_TABLES), new SimpleMapSilo<>(getEncounterTableSerialiser(db)));
	}

	@Override
	public Map<String, NpcFactionTemplate> loadNpcFactionTemplates()
	{
		return v2Crud(getPath(NPC_FACTION_TEMPLATES), new SimpleMapSilo<>(getNpcFactionTemplatesSerialiser(db)));
	}

	@Override
	public Map<String, NpcTemplate> loadNpcTemplates()
	{
		return v2Crud(getPath(NPC_TEMPLATES), new SimpleMapSilo<>(getNpcTemplatesSerialiser(db)));
	}

	@Override
	public Map<String, WieldingCombo> loadWieldingCombos()
	{
		return v2Crud(getPath(WIELDING_COMBOS), new SimpleMapSilo<>(getWieldingComboSerialiser()));
	}

	@Override
	public Map<String, ItemTemplate> loadItemTemplates()
	{
		return v2Crud(getPath(ITEM_TEMPLATES), new SimpleMapSilo<>(getItemTemplateSerialiser(db)));
	}

	@Override
	public Map<String, DifficultyLevel> loadDifficultyLevels()
	{
		return v2Crud(getPath(DIFFICULTY_LEVELS), new SimpleMapSilo<>(getDifficultyLevelSerialiser()));
	}

	@Override
	public Map<String, CraftRecipe> loadCraftRecipes()
	{
		return v2Crud(getPath(CRAFT_RECIPES), new SimpleMapSilo<>(getCraftRecipeSerialiser()));
	}

	@Override
	public Map<String, ItemEnchantments> loadItemEnchantments()
	{
		return v2Crud(getPath(ITEM_ENCHANTMENTS), new SimpleMapSilo<>(getItemEnchantmentsSerialiser()));
	}

	@Override
	public Map<String, Personality> loadPersonalities()
	{
		return v2Crud(getPath(PERSONALITIES), new SimpleMapSilo<>(getPersonalitySerialiser()));
	}

	@Override
	public Map<String, StartingKit> loadStartingKits()
	{
		return v2Crud(getPath(STARTING_KITS), new SimpleMapSilo<>(getStartingKitSerialiser()));
	}

	@Override
	public Map<String, FoeType> loadFoeTypes()
	{
		Map<String, FoeType> result = v2Crud(getPath(FOE_TYPES), new SimpleMapSilo<>(getFoeTypeSerialiser(db)));
		result.forEach((key, value) -> value.initBodyParts());
		return result;
	}

	@Override
	public Map<String, NaturalWeapon> loadNaturalWeapons()
	{
		return v2Crud(getPath(NATURAL_WEAPONS), new SimpleMapSilo<>(getNaturalWeaponSerialiser(db)));
	}

	@Override
	public StringManager getStringManager()
	{
		return stringManager;
	}

	@Override
	public void initStringManager()
	{
		stringManager = new V1StringManager(getPath(""));
	}

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

	@Override
	public void cacheSound(String soundName, AudioPlayer audioPlayer)
	{
		try
		{
			File file = new File("data/"+campaign.getName()+"/sound/"+soundName+".ogg");
			if (!file.exists())
			{
				throw new MazeException("invalid audio resource ["+file+"]");
			}

			FileInputStream fis = new FileInputStream(file);
			audioPlayer.cacheSound(soundName, fis);
		}
		catch (FileNotFoundException e)
		{
			throw new MazeException(e);
		}
	}

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

	@Override
	public List<String> getZoneNames()
	{
		List<String> result = new ArrayList<>();
		File dir = new File(getPath(V1Utils.ZONES));
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

	@Override
	public Zone getZone(String name)
	{
		Zone result = (Zone)v2Crud(getPath(ZONES) + name + ".json",
			new SingletonSilo<>(getZoneSerialiser(db)));
		result.getMap().init();

		for (Portal p : result.getPortals())
		{
			p.initToolStatus();
		}

		return result;
	}

	@Override
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

	@Override
	public Map<String, PlayerCharacter> loadCharacterGuild()
	{
		return v2Crud(getPath(CHARACTER_GUILD), new SimpleMapSilo<>(getPlayerCharacterSerialiser(db)));
	}

	@Override
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

	@Override
	public GameState loadGameState(String saveGameName) throws Exception
	{
		return (GameState)v2Crud(getSavePath()+saveGameName+"/"+GAME_STATE,
			new SingletonSilo<>(getGameStateSerialiser(db)));
	}

	@Override
	public Map<String, PlayerCharacter> loadPlayerCharacters(
		String saveGameName) throws Exception
	{
		return v2Crud(getSavePath()+saveGameName+"/"+PLAYER_CHARACTERS,
			new SimpleMapSilo<>(getPlayerCharacterSerialiser(db)));
	}

	@Override
	public Map<String, Npc> loadNpcs(String saveGameName) throws Exception
	{
		Map<String, Npc> result = v2Crud(getSavePath() + saveGameName + "/" + NPCS,
			new SimpleMapSilo<>(getNpcSerialiser(db)));

		result.forEach((k, v) -> v.init());

		return result;
	}

	@Override
	public Map<String, NpcFaction> loadNpcFactions(
		String saveGameName) throws Exception
	{
		return v2Crud(getSavePath()+saveGameName+"/"+NPC_FACTIONS,
			new SimpleMapSilo<>(getNpcFactionSerialiser(db)));
	}

	@Override
	public void loadMazeVariables(String saveGameName) throws Exception
	{
		MazeVariables.getVars().clear();

		Map<String, String> map = (Map<String, String>)v2Crud(
			getSavePath() + saveGameName + "/" + MAZE_VARIABLES, new MapSingletonSilo());

		map.forEach(MazeVariables::set);
	}

	@Override
	public Map<String, Map<Point, List<Item>>> loadItemCaches(
		String saveGameName) throws Exception
	{
		return (Map<String, Map<Point, List<Item>>>)v2Crud(getSavePath()+saveGameName+"/"+ITEM_CACHES,
			new MapSingletonSilo(getItemCacheSerialiser(db)));
	}

	@Override
	public PlayerTilesVisited loadPlayerTilesVisited(
		String saveGameName) throws Exception
	{
		Map<String, List<Point>> map = (Map<String, List<Point>>)v2Crud(getSavePath() + saveGameName + "/" + TILES_VISITED,
			new MapSingletonSilo(getTilesVisitedSerialiser()));

		return new PlayerTilesVisited(map);
	}

	@Override
	public Map<ConditionBearer, List<Condition>> loadConditions(
		String saveGameName,
		Map<String, PlayerCharacter> playerCharacterCache) throws Exception
	{
		try (BufferedReader reader = getReader(getSavePath()+saveGameName+"/"+CONDITIONS))
		{
			Map<ConditionBearer, List<Condition>> result = new HashMap<>();
			ListSerialiser<Condition> conditionsSerialiser = getConditionsSerialiser(db, new HashMap<>());

			Map<Object, Object> map = V2Utils.getMap(reader);

			for (Object key : map.keySet())
			{
				ConditionBearer cb = V1ConditionBearer.fromString((String)key, playerCharacterCache);
				List<Condition> list = conditionsSerialiser.fromObject(map.get(key), db);

				if (cb != null)
				{
					for (Condition c : list)
					{
						c.setTarget(cb);
						cb.addCondition(c);
					}

					result.put(cb, list);
				}
			}

			return result;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	@Override
	public Journal loadJournal(String saveGameName,
		String journalName) throws Exception
	{
		return (Journal)v2Crud(getSavePath() + saveGameName + "/" + JOURNALS + journalName+".json",
			new SingletonSilo<>(getJournalSerialiser()));
	}

	@Override
	public UserConfig loadUserConfig() throws Exception
	{
		Reader reader = getReader(USER_CONFIG);
		Properties p = new Properties();
		p.load(reader);
		reader.close();
		return new UserConfig(p);
	}
}
