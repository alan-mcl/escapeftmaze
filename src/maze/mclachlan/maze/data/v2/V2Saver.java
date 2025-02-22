package mclachlan.maze.data.v2;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.Saver;
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
import mclachlan.maze.stat.npc.*;

import static mclachlan.maze.data.v2.serialisers.SerialiserFactory.*;
import static mclachlan.maze.data.v2.serialisers.V2Files.*;

/**
 *
 */
public class V2Saver extends Saver
{
	private String path;
	private String savePath;
	private Campaign campaign;
	private Database db;

	/*-------------------------------------------------------------------------*/
	@Override
	public void init(Campaign c)
	{
		// todo: remove v2
		path = "data/v2/"+ c.getName()+"/db/";
		savePath = "data/"+ c.getName()+"/save/";
		campaign = c;
		db = Database.getInstance();
	}

	/*-------------------------------------------------------------------------*/
	private String getSaveGamePath(String saveGameName)
	{
		String result = savePath+saveGameName+'/';
		new File(result).mkdirs();
		return result;
	}

	/*-------------------------------------------------------------------------*/

	private void v2Crud(Map map, String fileName, V2SiloMap silo) throws Exception
	{
		File f = new File(path + fileName);
		f.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));

		silo.save(writer, map, db);

		writer.flush();
		writer.close();
	}

	private void v2Crud(Object obj, String fileName, V2SiloSingleton silo) throws Exception
	{
		File f = new File(path + fileName);
		f.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));

		silo.save(writer, obj, db);

		writer.flush();
		writer.close();
	}

	public StringManager getStringManager()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	//
	// data
	//

	@Override
	public void saveGenders(Map<String, Gender> genders) throws Exception
	{
		v2Crud(genders, GENDERS, new SimpleMapSilo<>(getGenderSerialiser()));
	}

	@Override
	public void saveBodyParts(Map<String, BodyPart> bodyParts) throws Exception
	{
		v2Crud(bodyParts, BODY_PARTS, new SimpleMapSilo<>(getBodyPartSerialiser()));
	}

	@Override
	public void saveRaces(Map<String, Race> races) throws Exception
	{
		v2Crud(races, RACES, new SimpleMapSilo<>(getRaceSerialiser(db)));
	}

	@Override
	public void saveExperienceTables(
		Map<String, ExperienceTable> experienceTables) throws Exception
	{
		v2Crud(experienceTables, EXPERIENCE_TABLES, new SimpleMapSilo<>(getExperienceTableSerialiser()));
	}

	@Override
	public void saveCharacterClasses(
		Map<String, CharacterClass> classes) throws Exception
	{
		v2Crud(classes, CHARACTER_CLASSES, new SimpleMapSilo<>(getCharacterClassSerialiser(db)));
	}

	@Override
	public void saveAttackTypes(Map<String, AttackType> map) throws Exception
	{
		v2Crud(map, ATTACK_TYPES, new SimpleMapSilo<>(getAttackTypeSerialiser()));
	}

	@Override
	public void saveConditionEffects(
		Map<String, ConditionEffect> map) throws Exception
	{
		v2Crud(map, CONDITION_EFFECTS, new SimpleMapSilo<>(getConditionEffectSerialiser()));
	}

	@Override
	public void saveConditionTemplates(
		Map<String, ConditionTemplate> map) throws Exception
	{
		v2Crud(map, CONDITION_TEMPLATES, new SimpleMapSilo<>(getConditionTemplateSerialiser(db)));
	}

	@Override
	public void saveSpellEffects(Map<String, SpellEffect> map) throws Exception
	{
		v2Crud(map, SPELL_EFFECTS, new SimpleMapSilo<>(getSpellEffectSerialiser(db)));
	}

	@Override
	public void saveLootEntries(Map<String, LootEntry> map) throws Exception
	{
		v2Crud(map, LOOT_ENTRIES, new SimpleMapSilo<>(getLootEntrySerialiser(db)));
	}

	@Override
	public void saveLootTables(Map<String, LootTable> map) throws Exception
	{
		v2Crud(map, LOOT_TABLES, new SimpleMapSilo<>(getLootTableSerialiser(db)));
	}

	@Override
	public void saveMazeScripts(Map<String, MazeScript> map) throws Exception
	{
		v2Crud(map, MAZE_SCRIPTS, new SimpleMapSilo<>(getMazeScriptSerialiser(db)));
	}

	@Override
	public void saveSpells(Map<String, Spell> map) throws Exception
	{
		v2Crud(map, SPELLS, new SimpleMapSilo<>(getSpellsSerialiser(db)));
	}

	@Override
	public void savePlayerSpellBooks(
		Map<String, PlayerSpellBook> map) throws Exception
	{
		v2Crud(map, PLAYER_SPELL_BOOKS, new SimpleMapSilo<>(getPlayerSpellBooksSerialiser(db)));
	}

	@Override
	public void saveMazeTextures(Map<String, MazeTexture> map) throws Exception
	{
		v2Crud(map, MAZE_TEXTURES, new SimpleMapSilo<>(getMazeTextureSerialiser()));
	}

	@Override
	public void saveFoeTemplates(Map<String, FoeTemplate> map) throws Exception
	{
		v2Crud(map, FOE_TEMPLATES, new SimpleMapSilo<>(getFoeTemplateSerialiser(db)));
	}

	@Override
	public void saveTraps(Map<String, Trap> map) throws Exception
	{
		v2Crud(map, TRAPS, new SimpleMapSilo<>(getTrapSerialiser(db)));
	}

	@Override
	public void saveFoeEntries(Map<String, FoeEntry> map) throws Exception
	{
		v2Crud(map, FOE_ENTRIES, new SimpleMapSilo<>(getFoeEntrySerialiser(db)));
	}

	@Override
	public void saveEncounterTables(
		Map<String, EncounterTable> map) throws Exception
	{
		v2Crud(map, ENCOUNTER_TABLES, new SimpleMapSilo<>(getEncounterTableSerialiser(db)));
	}

	@Override
	public void saveNpcFactionTemplates(
		Map<String, NpcFactionTemplate> map) throws Exception
	{
		v2Crud(map, NPC_FACTION_TEMPLATES, new SimpleMapSilo<>(getNpcFactionTemplatesSerialiser(db)));
	}

	@Override
	public void saveNpcTemplates(Map<String, NpcTemplate> map) throws Exception
	{
		v2Crud(map, NPC_TEMPLATES, new SimpleMapSilo<>(getNpcTemplatesSerialiser(db)));
	}

	@Override
	public void saveWieldingCombos(
		Map<String, WieldingCombo> map) throws Exception
	{
		v2Crud(map, WIELDING_COMBOS, new SimpleMapSilo<>(getWieldingComboSerialiser()));
	}

	@Override
	public void saveItemTemplates(Map<String, ItemTemplate> map) throws Exception
	{
		v2Crud(map, ITEM_TEMPLATES, new SimpleMapSilo<>(getItemTemplateSerialiser(db)));
	}

	@Override
	public void saveDifficultyLevels(
		Map<String, DifficultyLevel> map) throws Exception
	{
		v2Crud(map, DIFFICULTY_LEVELS, new SimpleMapSilo<>(getDifficultyLevelSerialiser()));
	}

	@Override
	public void saveCraftRecipes(
		Map<String, CraftRecipe> craftRecipes) throws Exception
	{
		v2Crud(craftRecipes, CRAFT_RECIPES, new SimpleMapSilo<>(getCraftRecipeSerialiser()));
	}

	@Override
	public void saveItemEnchantments(
		Map<String, ItemEnchantments> itemEnchantments) throws Exception
	{
		v2Crud(itemEnchantments, ITEM_ENCHANTMENTS, new SimpleMapSilo<>(getItemEnchantmentsSerialiser()));
	}

	@Override
	public void saveNaturalWeapons(
		Map<String, NaturalWeapon> naturalWeapons) throws Exception
	{
		v2Crud(naturalWeapons, NATURAL_WEAPONS, new SimpleMapSilo<>(getNaturalWeaponSerialiser(db)));
	}

	@Override
	public void saveStartingKits(Map<String, StartingKit> kits) throws Exception
	{
		v2Crud(kits, STARTING_KITS, new SimpleMapSilo<>(getStartingKitSerialiser()));
	}

	@Override
	public void savePersonalities(Map<String, Personality> p) throws Exception
	{
		v2Crud(p, PERSONALITIES, new SimpleMapSilo<>(getPersonalitySerialiser()));
	}

	@Override
	public void saveFoeTypes(Map<String, FoeType> foeTypes) throws Exception
	{
		v2Crud(foeTypes, FOE_TYPES, new SimpleMapSilo<>(getFoeTypeSerialiser(db)));
	}

	@Override
	public void saveZone(Zone zone) throws Exception
	{
		v2Crud(zone, ZONES+zone.getName()+".json", new SingletonSilo<>(getZoneSerialiser(db)));
	}

	@Override
	public void deleteZone(String zoneName) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void saveCharacterGuild(
		Map<String, PlayerCharacter> guild) throws Exception
	{
		v2Crud(guild, CHARACTER_GUILD, new SimpleMapSilo<>(getGuildSerialiser(db)));
	}

	@Override
	public void saveGameState(String saveGameName,
		GameState gameState) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void savePlayerCharacters(String saveGameName,
		Map<String, PlayerCharacter> playerCharacters) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void saveNpcs(String saveGameName,
		Map<String, Npc> npcs) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void saveNpcFactions(String saveGameName,
		Map<String, NpcFaction> npcFactions) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void saveMazeVariables(String saveGameName) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void saveItemCaches(String saveGameName,
		Map<String, Map<Point, List<Item>>> caches) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void savePlayerTilesVisited(String name,
		PlayerTilesVisited playerTilesVisited) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void saveConditions(String saveGameName,
		Map<ConditionBearer, List<Condition>> conditions) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void saveJournal(String saveGameName,
		Journal journal) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void saveUserConfig(UserConfig userConfig) throws Exception
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}
}
