package mclachlan.maze.data.v2;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.StringManager;
import mclachlan.maze.data.v1.V1ConditionBearer;
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

import static mclachlan.maze.data.v2.serialisers.V2SerialiserFactory.*;
import static mclachlan.maze.data.v2.serialisers.V2Files.*;

/**
 *
 */
public class V2Saver extends Saver
{
	private String path, savePath;
	private Campaign campaign;
	private Database db;

	/*-------------------------------------------------------------------------*/
	@Override
	public void init(Campaign c)
	{
		// todo: remove v2
		path = "data/"+ c.getName()+"/db/";
		savePath = "data/"+ c.getName()+"/save/";
		campaign = c;
		db = Database.getInstance();
	}

	/*-------------------------------------------------------------------------*/
	private String getPath(String fileName)
	{
		return path+fileName;
	}

	/*-------------------------------------------------------------------------*/
	private String getSaveGamePath(String saveGameName, String filename)
	{
		String result = savePath +saveGameName+'/'+filename;
		new File(result).getParentFile().mkdirs();
		return result;
	}

	/*-------------------------------------------------------------------------*/

	private void v2Crud(Map map, String path, V2SiloMap silo) throws Exception
	{
		File f = new File(path);
		f.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8));

		silo.save(writer, map, db);

		writer.flush();
		writer.close();
	}

	private void v2Crud(Object obj, String path, V2SiloSingleton silo) throws Exception
	{
		File f = new File(path);
		f.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8));

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
		v2Crud(genders, getPath(GENDERS), new SimpleMapSilo<>(getGenderSerialiser()));
	}

	@Override
	public void saveBodyParts(Map<String, BodyPart> bodyParts) throws Exception
	{
		v2Crud(bodyParts, getPath(BODY_PARTS), new SimpleMapSilo<>(getBodyPartSerialiser()));
	}

	@Override
	public void saveRaces(Map<String, Race> races) throws Exception
	{
		v2Crud(races, getPath(RACES), new SimpleMapSilo<>(getRaceSerialiser(db)));
	}

	@Override
	public void saveExperienceTables(
		Map<String, ExperienceTable> experienceTables) throws Exception
	{
		v2Crud(experienceTables, getPath(EXPERIENCE_TABLES), new SimpleMapSilo<>(getExperienceTableSerialiser()));
	}

	@Override
	public void saveCharacterClasses(
		Map<String, CharacterClass> classes) throws Exception
	{
		v2Crud(classes, getPath(CHARACTER_CLASSES), new SimpleMapSilo<>(getCharacterClassSerialiser(db)));
	}

	@Override
	public void saveAttackTypes(Map<String, AttackType> map) throws Exception
	{
		v2Crud(map, getPath(ATTACK_TYPES), new SimpleMapSilo<>(getAttackTypeSerialiser()));
	}

	@Override
	public void saveConditionEffects(
		Map<String, ConditionEffect> map) throws Exception
	{
		v2Crud(map, getPath(CONDITION_EFFECTS), new SimpleMapSilo<>(getConditionEffectSerialiser()));
	}

	@Override
	public void saveConditionTemplates(
		Map<String, ConditionTemplate> map) throws Exception
	{
		v2Crud(map, getPath(CONDITION_TEMPLATES), new SimpleMapSilo<>(getConditionTemplateSerialiser(db)));
	}

	@Override
	public void saveSpellEffects(Map<String, SpellEffect> map) throws Exception
	{
		v2Crud(map, getPath(SPELL_EFFECTS), new SimpleMapSilo<>(getSpellEffectSerialiser(db)));
	}

	@Override
	public void saveLootEntries(Map<String, LootEntry> map) throws Exception
	{
		v2Crud(map, getPath(LOOT_ENTRIES), new SimpleMapSilo<>(getLootEntrySerialiser(db)));
	}

	@Override
	public void saveLootTables(Map<String, LootTable> map) throws Exception
	{
		v2Crud(map, getPath(LOOT_TABLES), new SimpleMapSilo<>(getLootTableSerialiser(db)));
	}

	@Override
	public void saveMazeScripts(Map<String, MazeScript> map) throws Exception
	{
		v2Crud(map, getPath(MAZE_SCRIPTS), new SimpleMapSilo<>(getMazeScriptSerialiser(db)));
	}

	@Override
	public void saveSpells(Map<String, Spell> map) throws Exception
	{
		v2Crud(map, getPath(SPELLS), new SimpleMapSilo<>(getSpellsSerialiser(db)));
	}

	@Override
	public void savePlayerSpellBooks(
		Map<String, PlayerSpellBook> map) throws Exception
	{
		v2Crud(map, getPath(PLAYER_SPELL_BOOKS), new SimpleMapSilo<>(getPlayerSpellBooksSerialiser(db)));
	}

	@Override
	public void saveMazeTextures(Map<String, MazeTexture> map) throws Exception
	{
		v2Crud(map, getPath(MAZE_TEXTURES), new SimpleMapSilo<>(getMazeTextureSerialiser()));
	}

	@Override
	public void saveObjectAnimations(
		Map<String, ObjectAnimations> map) throws Exception
	{
		v2Crud(map, getPath(OBJECT_ANIMATIONS), new SimpleMapSilo<>(getFoeSpriteAnimationSerialiser()));
	}

	@Override
	public void saveFoeTemplates(Map<String, FoeTemplate> map) throws Exception
	{
		v2Crud(map, getPath(FOE_TEMPLATES), new SimpleMapSilo<>(getFoeTemplateSerialiser(db)));
	}

	@Override
	public void saveTraps(Map<String, Trap> map) throws Exception
	{
		v2Crud(map, getPath(TRAPS), new SimpleMapSilo<>(getTrapSerialiser(db)));
	}

	@Override
	public void saveFoeEntries(Map<String, FoeEntry> map) throws Exception
	{
		v2Crud(map, getPath(FOE_ENTRIES), new SimpleMapSilo<>(getFoeEntrySerialiser(db)));
	}

	@Override
	public void saveEncounterTables(
		Map<String, EncounterTable> map) throws Exception
	{
		v2Crud(map, getPath(ENCOUNTER_TABLES), new SimpleMapSilo<>(getEncounterTableSerialiser(db)));
	}

	@Override
	public void saveNpcFactionTemplates(
		Map<String, NpcFactionTemplate> map) throws Exception
	{
		v2Crud(map, getPath(NPC_FACTION_TEMPLATES), new SimpleMapSilo<>(getNpcFactionTemplatesSerialiser(db)));
	}

	@Override
	public void saveNpcTemplates(Map<String, NpcTemplate> map) throws Exception
	{
		v2Crud(map, getPath(NPC_TEMPLATES), new SimpleMapSilo<>(getNpcTemplatesSerialiser(db)));
	}

	@Override
	public void saveWieldingCombos(
		Map<String, WieldingCombo> map) throws Exception
	{
		v2Crud(map, getPath(WIELDING_COMBOS), new SimpleMapSilo<>(getWieldingComboSerialiser()));
	}

	@Override
	public void saveItemTemplates(Map<String, ItemTemplate> map) throws Exception
	{
		v2Crud(map, getPath(ITEM_TEMPLATES), new SimpleMapSilo<>(getItemTemplateSerialiser(db)));
	}

	@Override
	public void saveDifficultyLevels(
		Map<String, DifficultyLevel> map) throws Exception
	{
		v2Crud(map, getPath(DIFFICULTY_LEVELS), new SimpleMapSilo<>(getDifficultyLevelSerialiser()));
	}

	@Override
	public void saveCraftRecipes(
		Map<String, CraftRecipe> craftRecipes) throws Exception
	{
		v2Crud(craftRecipes, getPath(CRAFT_RECIPES), new SimpleMapSilo<>(getCraftRecipeSerialiser()));
	}

	@Override
	public void saveItemEnchantments(
		Map<String, ItemEnchantments> itemEnchantments) throws Exception
	{
		v2Crud(itemEnchantments, getPath(ITEM_ENCHANTMENTS), new SimpleMapSilo<>(getItemEnchantmentsSerialiser()));
	}

	@Override
	public void saveNaturalWeapons(
		Map<String, NaturalWeapon> naturalWeapons) throws Exception
	{
		v2Crud(naturalWeapons, getPath(NATURAL_WEAPONS), new SimpleMapSilo<>(getNaturalWeaponSerialiser(db)));
	}

	@Override
	public void saveStartingKits(Map<String, StartingKit> kits) throws Exception
	{
		v2Crud(kits, getPath(STARTING_KITS), new SimpleMapSilo<>(getStartingKitSerialiser()));
	}

	@Override
	public void savePersonalities(Map<String, Personality> p) throws Exception
	{
		v2Crud(p, getPath(PERSONALITIES), new SimpleMapSilo<>(getPersonalitySerialiser()));
	}

	@Override
	public void saveFoeTypes(Map<String, FoeType> foeTypes) throws Exception
	{
		v2Crud(foeTypes, getPath(FOE_TYPES), new SimpleMapSilo<>(getFoeTypeSerialiser(db)));
	}

	@Override
	public void saveZone(Zone zone) throws Exception
	{
		v2Crud(zone, getPath(ZONES+zone.getName()+".json"), new SingletonSilo<>(getZoneSerialiser(db)));
	}

	@Override
	public void deleteZone(String zoneName) throws Exception
	{
		String pathname = path + V1Utils.ZONES + zoneName + ".txt";
		File file = new File(pathname);
		if (!file.delete())
		{
			throw new MazeException("can't delete '"+pathname+"'");
		}
	}

	@Override
	public void saveCharacterGuild(
		Map<String, PlayerCharacter> guild) throws Exception
	{
		v2Crud(guild, getPath(CHARACTER_GUILD), new SimpleMapSilo<>(getPlayerCharacterSerialiser(db)));
	}

	@Override
	public void saveGameState(String saveGameName, GameState gameState) throws Exception
	{
		v2Crud(gameState, getSaveGamePath(saveGameName, GAME_STATE), new SingletonSilo<>(getGameStateSerialiser(db)));
	}

	@Override
	public void savePlayerCharacters(String saveGameName,
		Map<String, PlayerCharacter> playerCharacters) throws Exception
	{
		v2Crud(playerCharacters, getSaveGamePath(saveGameName, PLAYER_CHARACTERS),
			new SimpleMapSilo<>(getPlayerCharacterSerialiser(db)));
	}

	@Override
	public void saveNpcs(String saveGameName,
		Map<String, Npc> npcs) throws Exception
	{
		v2Crud(npcs, getSaveGamePath(saveGameName, NPCS),
			new SimpleMapSilo<>(getNpcSerialiser(db)));
	}

	@Override
	public void saveNpcFactions(String saveGameName,
		Map<String, NpcFaction> npcFactions) throws Exception
	{
		v2Crud(npcFactions, getSaveGamePath(saveGameName, NPC_FACTIONS),
			new SimpleMapSilo<>(getNpcFactionSerialiser(db)));
	}

	@Override
	public void saveMazeVariables(String saveGameName) throws Exception
	{
		v2Crud(MazeVariables.getVars(), getSaveGamePath(saveGameName, MAZE_VARIABLES),
			new MapSingletonSilo());
	}

	@Override
	public void saveItemCaches(String saveGameName,
		Map<String, Map<Point, List<Item>>> caches) throws Exception
	{
		v2Crud(caches, getSaveGamePath(saveGameName, ITEM_CACHES),
			new MapSingletonSilo(getItemCacheSerialiser(db)));
	}

	@Override
	public void savePlayerTilesVisited(String saveGameName,
		PlayerTilesVisited playerTilesVisited) throws Exception
	{
		v2Crud(playerTilesVisited.getTilesVisited(), getSaveGamePath(saveGameName, TILES_VISITED),
			new MapSingletonSilo(getTilesVisitedSerialiser()));
	}

	@Override
	public void saveConditions(String saveGameName,
		Map<ConditionBearer, List<Condition>> conditions) throws Exception
	{
		File f = new File(getSaveGamePath(saveGameName, CONDITIONS));
		f.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8));

		Map<Object, Object> temp = new HashMap<>();
		ListSerialiser conditionsSerialiser = getConditionsSerialiser(db, new HashMap<>());

		for (ConditionBearer cb : conditions.keySet())
		{
			if (cb != null)
			{
				String key = V1ConditionBearer.toString(cb);
				Object value = conditionsSerialiser.toObject(conditions.get(cb), db);

				temp.put(key, value);
			}
		}

		V2Utils.writeJson(temp, writer);

		writer.flush();
		writer.close();
	}

	@Override
	public void saveJournal(String saveGameName,
		Journal journal) throws Exception
	{
		v2Crud(journal, getSaveGamePath(saveGameName, JOURNALS+journal.getName()+".json"),
			new SingletonSilo<>(getJournalSerialiser()));
	}

	@Override
	public void saveUserConfig(UserConfig userConfig) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(USER_CONFIG, StandardCharsets.UTF_8));
		Properties p = userConfig.toProperties();
		p.store(writer, "Written by V2Saver");
		writer.flush();
		writer.close();
	}
}
