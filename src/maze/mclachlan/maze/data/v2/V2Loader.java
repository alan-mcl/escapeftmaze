package mclachlan.maze.data.v2;

import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.sound.sampled.Clip;
import mclachlan.maze.audio.AudioPlayer;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.StringManager;
import mclachlan.maze.data.v1.DataObject;
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

import static mclachlan.maze.data.v2.serialisers.SerialiserFactory.*;
import static mclachlan.maze.data.v2.serialisers.V2Files.*;

/**
 *
 */
public class V2Loader extends Loader
{
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
	private String getPath()
	{
		// todo: remove v2
		return "data/v2/"+this.campaign.getName()+"/db/";
	}

	/*-------------------------------------------------------------------------*/
	private String getSavePath()
	{
		// todo: remove v2
		return "data/v2/"+this.campaign.getName()+"/save/";
	}

	/*-------------------------------------------------------------------------*/
	private BufferedReader getReader(String str)
		throws FileNotFoundException
	{
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

	private Map v2Crud(String fileName, V2SiloMap silo)
	{
		try (BufferedReader reader = getReader(getPath() + fileName))
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

	//
	// data
	//

	@Override
	public Map<String, Gender> loadGenders()
	{
		return v2Crud(GENDERS, new SimpleMapSilo<>(getGenderSerialiser()));
	}

	@Override
	public Map<String, Race> loadRaces()
	{
		Map<String, Race> result = v2Crud(RACES, new SimpleMapSilo<>(getRaceSerialiser(db)));
		result.forEach((key, value) -> value.initBodyParts());
		return result;
	}

	@Override
	public Map<String, BodyPart> loadBodyParts()
	{
		return v2Crud(BODY_PARTS, new SimpleMapSilo<>(getBodyPartSerialiser()));
	}

	@Override
	public Map<String, CharacterClass> loadCharacterClasses()
	{
		return v2Crud(CHARACTER_CLASSES, new SimpleMapSilo<>(getCharacterClassSerialiser(db)));
	}

	@Override
	public Map<String, ExperienceTable> loadExperienceTables()
	{
		return v2Crud(EXPERIENCE_TABLES, new SimpleMapSilo<>(getExperienceTableSerialiser()));
	}

	@Override
	public Map<String, AttackType> loadAttackTypes()
	{
		return v2Crud(ATTACK_TYPES, new SimpleMapSilo<>(getAttackTypeSerialiser()));
	}

	@Override
	public Map<String, ConditionEffect> loadConditionEffects()
	{
		return v2Crud(CONDITION_EFFECTS, new SimpleMapSilo<>(getConditionEffectSerialiser()));
	}

	@Override
	public Map<String, ConditionTemplate> loadConditionTemplates()
	{
		return v2Crud(CONDITION_TEMPLATES, new SimpleMapSilo<>(getConditionTemplateSerialiser(db)));
	}

	@Override
	public Map<String, SpellEffect> loadSpellEffects()
	{
		return v2Crud(SPELL_EFFECTS, new SimpleMapSilo<>(getSpellEffectSerialiser(db)));
	}

	@Override
	public Map<String, MazeScript> loadMazeScripts()
	{
		return v2Crud(MAZE_SCRIPTS, new SimpleMapSilo<>(getMazeScriptSerialiser(db)));
	}

	@Override
	public Map<String, LootEntry> loadLootEntries()
	{
		return v2Crud(LOOT_ENTRIES, new SimpleMapSilo<>(getLootEntrySerialiser(db)));
	}

	@Override
	public Map<String, LootTable> loadLootTables()
	{
		return v2Crud(LOOT_TABLES, new SimpleMapSilo<>(getLootTableSerialiser(db)));
	}

	@Override
	public Map<String, Spell> loadSpells()
	{
		return v2Crud(SPELLS, new SimpleMapSilo<>(getSpellsSerialiser(db)));
	}

	@Override
	public Map<String, PlayerSpellBook> loadPlayerSpellBooks()
	{
		return v2Crud(PLAYER_SPELL_BOOKS, new SimpleMapSilo<>(getPlayerSpellBooksSerialiser(db)));
	}

	@Override
	public Map<String, MazeTexture> loadMazeTextures()
	{
		Map<String, MazeTexture> map = v2Crud(MAZE_TEXTURES, new SimpleMapSilo<>(getMazeTextureSerialiser()));
		map.forEach((k, v) -> v.initRenderTexture());
		return map;
	}

	@Override
	public Map<String, FoeTemplate> loadFoeTemplates()
	{
		return v2Crud(FOE_TEMPLATES, new SimpleMapSilo<>(getFoeTemplateSerialiser(db)));
	}

	@Override
	public Map<String, Trap> loadTraps()
	{
		return v2Crud(TRAPS, new SimpleMapSilo<>(getTrapSerialiser(db)));
	}

	@Override
	public Map<String, FoeEntry> loadFoeEntries()
	{
		return v2Crud(FOE_ENTRIES, new SimpleMapSilo<>(getFoeEntrySerialiser(db)));
	}

	@Override
	public Map<String, EncounterTable> loadEncounterTables()
	{
		return v2Crud(ENCOUNTER_TABLES, new SimpleMapSilo<>(getEncounterTableSerialiser(db)));
	}

	@Override
	public Map<String, NpcFactionTemplate> loadNpcFactionTemplates()
	{
		return v2Crud(NPC_FACTION_TEMPLATES, new SimpleMapSilo<>(getNpcFactionTemplatesSerialiser(db)));
	}

	@Override
	public Map<String, NpcTemplate> loadNpcTemplates()
	{
		return v2Crud(NPC_TEMPLATES, new SimpleMapSilo<>(getNpcTemplatesSerialiser(db)));
	}

	@Override
	public Map<String, WieldingCombo> loadWieldingCombos()
	{
		return v2Crud(WIELDING_COMBOS, new SimpleMapSilo<>(getWieldingComboSerialiser()));
	}

	@Override
	public Map<String, ItemTemplate> loadItemTemplates()
	{
		return v2Crud(ITEM_TEMPLATES, new SimpleMapSilo<>(getItemTemplateSerialiser(db)));
	}

	@Override
	public Map<String, DifficultyLevel> loadDifficultyLevels()
	{
		return v2Crud(DIFFICULTY_LEVELS, new SimpleMapSilo<>(getDifficultyLevelSerialiser()));
	}

	@Override
	public Map<String, CraftRecipe> loadCraftRecipes()
	{
		return v2Crud(CRAFT_RECIPES, new SimpleMapSilo<>(getCraftRecipeSerialiser()));
	}

	@Override
	public Map<String, ItemEnchantments> loadItemEnchantments()
	{
		return v2Crud(ITEM_ENCHANTMENTS, new SimpleMapSilo<>(getItemEnchantmentsSerialiser()));
	}

	@Override
	public Map<String, Personality> loadPersonalities()
	{
		return v2Crud(PERSONALITIES, new SimpleMapSilo<>(getPersonalitySerialiser()));
	}

	@Override
	public Map<String, StartingKit> loadStartingKits()
	{
		return v2Crud(STARTING_KITS, new SimpleMapSilo<>(getStartingKitSerialiser()));
	}

	@Override
	public Map<String, FoeType> loadFoeTypes()
	{
		Map<String, FoeType> result = v2Crud(FOE_TYPES, new SimpleMapSilo<>(getFoeTypeSerialiser(db)));
		result.forEach((key, value) -> value.initBodyParts());
		return result;
	}

	@Override
	public Map<String, NaturalWeapon> loadNaturalWeapons()
	{
		return v2Crud(NATURAL_WEAPONS, new SimpleMapSilo<>(getNaturalWeaponSerialiser(db)));
	}

	@Override
	public StringManager getStringManager()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void initStringManager()
	{
//		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public BufferedImage getImage(String resourceName)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public List<String> getPortraitNames()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public Clip getClip(String clipName, AudioPlayer audioPlayer)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public InputStream getMusic(String trackName)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public List<String> getZoneNames()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public Zone getZone(String name)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public Font getFont(String name)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public Map<String, PlayerCharacter> loadCharacterGuild()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public List<String> getSaveGames()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public GameState loadGameState(String saveGameName) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public Map<String, PlayerCharacter> loadPlayerCharacters(
		String saveGameName) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public Map<String, Npc> loadNpcs(String saveGameName) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public Map<String, NpcFaction> loadNpcFactions(
		String saveGameName) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void loadMazeVariables(String saveGameName) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public Map<String, Map<Point, List<Item>>> loadItemCaches(
		String saveGameName) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public PlayerTilesVisited loadPlayerTilesVisited(
		String saveGameName) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public Map<ConditionBearer, List<Condition>> loadConditions(
		String saveGameName,
		Map<String, PlayerCharacter> playerCharacterCache) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public Journal loadJournal(String saveGameName,
		String journalName) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public UserConfig loadUserConfig() throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}
}
