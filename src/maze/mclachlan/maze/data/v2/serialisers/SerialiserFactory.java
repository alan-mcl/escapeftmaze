package mclachlan.maze.data.v2.serialisers;

import java.awt.Color;
import java.awt.Point;
import java.util.*;
import mclachlan.crusader.ObjectScript;
import mclachlan.crusader.script.JagObjectVertically;
import mclachlan.crusader.script.JagObjectWithinRadius;
import mclachlan.crusader.script.SinusoidalStretch;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.v2.*;
import mclachlan.maze.game.DifficultyLevel;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.event.*;
import mclachlan.maze.map.*;
import mclachlan.maze.map.script.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.combat.WieldingCombo;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.condition.RepeatedSpellEffect;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.stat.npc.*;
import mclachlan.maze.ui.diygui.animation.ColourMagicPortraitAnimation;
import mclachlan.maze.ui.diygui.animation.ProjectileAnimation;

/**
 *
 */
public class SerialiserFactory
{
	/*-------------------------------------------------------------------------*/
	private static ReflectiveSerialiser getReflectiveSerialiser(
		Class<?> clazz, String... fields)
	{
		// some default custom serialised for the Maze db

		ReflectiveSerialiser result = new ReflectiveSerialiser(clazz, fields);
		result.addCustomSerialiser(StatModifier.class, new StatModifierSerialiser());
		result.addCustomSerialiser(Dice.class, new DiceSerialiser());
		result.addCustomSerialiser(ValueList.class, new ValueListSerialiser());
		result.addCustomSerialiser(TypeDescriptor.class, new TypeDescriptorSerialiser());
		result.addCustomSerialiser(Point.class, new PointSerialiser());
		result.addCustomSerialiser(Color.class, new ColorSerialiser());
		result.addCustomSerialiser(BitSet.class, new BitSetSerialiser());
		result.addCustomSerialiser(Set.class, new StringSetSerialiser());
		result.addCustomSerialiser(String[].class, new StringArraySerialiser());
		result.addCustomSerialiser(int[].class, new IntArraySerialiser());
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<Gender> getGenderSerialiser()
	{
		return (ReflectiveSerialiser<Gender>)getReflectiveSerialiser(
			Gender.class,
			"name",
			"startingModifiers",
			"constantModifiers",
			"bannerModifiers");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<BodyPart> getBodyPartSerialiser()
	{
		return (ReflectiveSerialiser<BodyPart>)getReflectiveSerialiser(
			BodyPart.class,
			"name",
			"displayName",
			"modifiers",
			"damagePrevention",
			"damagePreventionChance",
			"nrWeaponHardpoints",
			"equipableSlotType");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<Race> getRaceSerialiser(Database db)
	{
		ReflectiveSerialiser<Race> result = getReflectiveSerialiser(
			Race.class,
			"name",
			"description",
			"startingHitPointPercent",
			"startingActionPointPercent",
			"startingMagicPointPercent",
			"startingModifiers",
			"constantModifiers",
			"bannerModifiers",
			"attributeCeilings",
			"head",
			"torso",
			"leg",
			"hand",
			"foot",
			"leftHandIcon",
			"rightHandIcon",
			"allowedGenders",
			"magicDead",
			"specialAbility",
			"startingItems",
			"naturalWeapons",
			"suggestedNames",
			"unlockVariable",
			"unlockDescription",
			"favouredEnemyModifier",
			"characterCreationImage");

		result.addCustomSerialiser(BodyPart.class, new NameSerialiser<>(db::getBodyPart));
		result.addCustomSerialiser("allowedGenders", new NameListSerialiser<>(db::getGender));
		result.addCustomSerialiser("specialAbility", new NameSerialiser<>(db::getSpell));
		result.addCustomSerialiser("startingItems", new NameListSerialiser<>(db::getStartingKit));
		result.addCustomSerialiser("naturalWeapons", new NameListSerialiser<>(db::getNaturalWeapon));
		result.addCustomSerialiser("suggestedNames", new DirectObjectSerialiser());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<ExperienceTable> getExperienceTableSerialiser()
	{
		ReflectiveSerialiser<ExperienceTable> expTableArraySerialiser =
			getReflectiveSerialiser(
				ExperienceTableArray.class,
				"name",
				"levels",
				"postGygaxIncrement");

		expTableArraySerialiser.addCustomSerialiser("levels", new IntArraySerialiser());

		HashMap<Class, V2SerialiserMap<ExperienceTable>> map = new HashMap<>();
		map.put(ExperienceTableArray.class, expTableArraySerialiser);
		return new MazeObjectImplSerialiser<>(map);
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<CharacterClass> getCharacterClassSerialiser(
		Database db)
	{
		ReflectiveSerialiser<CharacterClass> result = getReflectiveSerialiser(
			CharacterClass.class,
			"name",
			"focus",
			"description",
			"startingHitPoints",
			"startingActionPoints",
			"startingMagicPoints",
			"startingActiveModifiers",
			"startingModifiers",
			"unlockModifiers",
			"allowedGenders",
			"allowedRaces",
			"experienceTable",
			"levelUpHitPoints",
			"levelUpActionPoints",
			"levelUpMagicPoints",
			"levelUpAssignableModifiers",
			"levelUpModifiers",
			"progression");

		result.addCustomSerialiser("allowedGenders", new StringSetSerialiser());
		result.addCustomSerialiser("allowedRaces", new StringSetSerialiser());
		result.addCustomSerialiser("experienceTable", new NameSerialiser<>(db::getExperienceTable));
		result.addCustomSerialiser(LevelAbilityProgression.class, getLevelAbilitySerialiser(db));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserObject getLevelAbilitySerialiser(Database db)
	{
		HashMap<Class, V2SerialiserMap<LevelAbility>> map = new HashMap<>();

		map.put(StatModifierLevelAbility.class, getReflectiveSerialiser(
			StatModifierLevelAbility.class, "key", "displayName", "description", "modifier"));
		map.put(BannerModifierLevelAbility.class, getReflectiveSerialiser(
			BannerModifierLevelAbility.class, "key", "displayName", "description", "bannerModifier"));

		ReflectiveSerialiser salas1 = getReflectiveSerialiser(
			SpecialAbilityLevelAbility.class, "key", "displayName", "description", "ability");

		ReflectiveSerialiser slas = getSpellLikeAbilitySerialiser(db);
		salas1.addCustomSerialiser(SpellLikeAbility.class, slas);
		ReflectiveSerialiser salas = salas1;

		map.put(SpecialAbilityLevelAbility.class, salas);

		map.put(AddSpellPicks.class, getReflectiveSerialiser(
			AddSpellPicks.class, "key", "displayName", "description", "spellPicks"));

		MazeObjectImplSerialiser<LevelAbility> laSerialiser = new MazeObjectImplSerialiser<>(
			map, "key", "displayName", "description", "characterClass");

		ReflectiveSerialiser<LevelAbilityProgression> lapSerialiser = new ReflectiveSerialiser<>(
			LevelAbilityProgression.class,
			"progression");

		lapSerialiser.addCustomSerialiser("progression", new ListSerialiser(new ListSerialiser(laSerialiser)));

		return lapSerialiser;
	}

	/*-------------------------------------------------------------------------*/

	private static ReflectiveSerialiser getSpellLikeAbilitySerialiser(
		Database db)
	{
		ReflectiveSerialiser slas = getReflectiveSerialiser(
			SpellLikeAbility.class, "spell", "castingLevel");
		slas.addCustomSerialiser("spell", new NameSerialiser<>(db::getSpell));
		return slas;
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<AttackType> getAttackTypeSerialiser()
	{
		return getReflectiveSerialiser(
			AttackType.class,
			"name",
			"verb",
			"attackModifier",
			"modifiers",
			"damageType");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<AttackType> getConditionEffectSerialiser()
	{
		return new MazeObjectImplSerialiser<>(new HashMap<>(), "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<ConditionTemplate> getConditionTemplateSerialiser(
		Database db)
	{
		ReflectiveSerialiser result = getReflectiveSerialiser(
			ConditionTemplate.class,
			"name",
			"displayName",
			"duration",
			"strength",
			"conditionEffect",
			"statModifier",
			"bannerModifier",
			"hitPointDamage",
			"staminaDamage",
			"actionPointDamage",
			"magicPointDamage",
			"icon",
			"adjective",
			"scaleModifierWithStrength",
			"strengthWanes",
			"exitCondition",
			"exitConditionChance",
			"exitSpellEffect",
			"repeatedSpellEffects",
			"impl");

		result.addCustomSerialiser("conditionEffect", new NameSerialiser<>(
			db::getConditionEffect, "none", ConditionEffect.NONE));

		result.addCustomSerialiser("repeatedSpellEffects",
			new ListSerialiser(
				new ReflectiveSerialiser(RepeatedSpellEffect.class,
					"startTurn", "endTurn", "turnMod", "probability", "spellEffect")));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<SpellEffect> getSpellEffectSerialiser(
		Database db)
	{
		ReflectiveSerialiser result = getReflectiveSerialiser(
			SpellEffect.class,
			"name",
			"displayName",
			"type",
			"subType",
			"application",
			"saveAdjustment",
			"savedResult",
			"unsavedResult",
			"targetType");

		V2SerialiserObject<SpellResult> srs = getSpellResultSerialiser(db);
		result.addCustomSerialiser("savedResult", srs);
		result.addCustomSerialiser("unsavedResult", srs);

		return result;
	}

	private static V2SerialiserObject<SpellResult> getSpellResultSerialiser(
		Database db)
	{
		HashMap<Class, V2SerialiserMap<SpellResult>> map = new HashMap<>();

//		map.put(AmazingAmmoStashSpellResult.class, getReflectiveSerialiser(AmazingAmmoStashSpellResult.class));
//		map.put(GreaterAmmoStashSpellResult.class, getReflectiveSerialiser(GreaterAmmoStashSpellResult.class));
//		map.put(AmmoStashSpellResult.class, getReflectiveSerialiser(AmmoStashSpellResult.class));
//		map.put(DestroyTrapSpellResult.class, getReflectiveSerialiser(DestroyTrapSpellResult.class));
//		map.put(EnlighteningKataSpellResult.class, getReflectiveSerialiser(EnlighteningKataSpellResult.class));
//		map.put(GuardianSpiritSpellResult.class, getReflectiveSerialiser(GuardianSpiritSpellResult.class));
//		map.put(InversionKataSpellResult.class, getReflectiveSerialiser(InversionKataSpellResult.class));
//		map.put(MatyrSpellResult.class, getReflectiveSerialiser(MatyrSpellResult.class));
//		map.put(LayOnHandsSpellResult.class, getReflectiveSerialiser(LayOnHandsSpellResult.class));
//		map.put(PrankSpellResult.class, getReflectiveSerialiser(PrankSpellResult.class));
//		map.put(PrayForMiracleSpellResult.class, getReflectiveSerialiser(PrayForMiracleSpellResult.class));
//		map.put(SpellStealingSpellResult.class, getReflectiveSerialiser(SpellStealingSpellResult.class));

		ReflectiveSerialiser awwsrs = getReflectiveSerialiser(AttackWithWeaponSpellResult.class,
			"foeType", "focusAffinity", "nrStrikes", "modifiers", "attackType", "damageType", "attackScript", "requiresBackstabWeapon", "requiresSnipeWeapon", "consumesWeapon", "requiredWeaponType", "spellEffects");
		awwsrs.addCustomSerialiser("spellEffects", new GroupOfPossibiltiesSerialiser<>(new DirectObjectSerialiser<String>()));
		awwsrs.addCustomSerialiser("attackType", new NameSerialiser<>(db::getAttackType));
		map.put(AttackWithWeaponSpellResult.class, awwsrs);

		map.put(BoozeSpellResult.class, getReflectiveSerialiser(BoozeSpellResult.class, "foeType", "focusAffinity"));
		map.put(CharmSpellResult.class, getReflectiveSerialiser(CharmSpellResult.class, "foeType", "focusAffinity", "value"));
		map.put(CloudSpellResult.class, getReflectiveSerialiser(CloudSpellResult.class, "foeType", "focusAffinity", "duration", "strength", "icon", "spell"));
		map.put(ConditionIdentificationSpellResult.class, getReflectiveSerialiser(ConditionIdentificationSpellResult.class, "foeType", "focusAffinity", "strength", "canIdentifyConditionStrength"));

		ReflectiveSerialiser crsrs = getReflectiveSerialiser(ConditionRemovalSpellResult.class, "foeType", "focusAffinity", "strength", "effects");
		crsrs.addCustomSerialiser("effects", new ListSerialiser(new NameSerialiser<>(db::getConditionEffect)));
		map.put(ConditionRemovalSpellResult.class, crsrs);

		ReflectiveSerialiser ctsrs = getReflectiveSerialiser(ConditionSpellResult.class, "foeType", "focusAffinity", "conditionTemplate");
		ctsrs.addCustomSerialiser("conditionTemplate", new NameSerialiser<>(db::getConditionTemplate));
		map.put(ConditionSpellResult.class, ctsrs);

		ReflectiveSerialiser condtsrs = getReflectiveSerialiser(ConditionTransferSpellResult.class, "foeType", "focusAffinity", "effects", "deliver");
		condtsrs.addCustomSerialiser("effects", new ListSerialiser(new NameSerialiser<>(db::getConditionEffect)));
		map.put(ConditionTransferSpellResult.class, condtsrs);

		map.put(CreateItemSpellResult.class, getReflectiveSerialiser(CreateItemSpellResult.class, "foeType", "focusAffinity", "lootTable", "equipItems"));
		map.put(DamageFoeTypeSpellResult.class, getReflectiveSerialiser(DamageFoeTypeSpellResult.class, "foeType", "focusAffinity", "damage", "multiplier", "type"));
		map.put(DamageSpellResult.class, getReflectiveSerialiser(DamageSpellResult.class,
			"foeType", "focusAffinity", "hitPointDamage", "fatigueDamage", "actionPointDamage", "magicPointDamage", "multiplier", "transferToCaster"));
		map.put(DeathSpellResult.class, getReflectiveSerialiser(DeathSpellResult.class, "foeType", "focusAffinity"));
		map.put(DrainSpellResult.class, getReflectiveSerialiser(DrainSpellResult.class, "foeType", "focusAffinity", "drain", "modifier"));
		map.put(ForgetSpellResult.class, getReflectiveSerialiser(ForgetSpellResult.class, "foeType", "focusAffinity", "strength"));
		map.put(HealingSpellResult.class, getReflectiveSerialiser(HealingSpellResult.class,
			"foeType", "focusAffinity", "hitPointHealing", "staminaHealing", "actionPointHealing", "magicPointHealing"));
		map.put(IdentifySpellResult.class, getReflectiveSerialiser(IdentifySpellResult.class, "foeType", "focusAffinity", "value", "revealCurses"));
		map.put(LocatePersonSpellResult.class, getReflectiveSerialiser(LocatePersonSpellResult.class, "foeType", "focusAffinity", "value"));
		map.put(MindReadFailedSpellResult.class, getReflectiveSerialiser(MindReadFailedSpellResult.class, "foeType", "focusAffinity", "value"));
		map.put(MindReadSpellResult.class, getReflectiveSerialiser(MindReadSpellResult.class, "foeType", "focusAffinity", "value"));
		map.put(PurifyAirSpellResult.class, getReflectiveSerialiser(PurifyAirSpellResult.class, "foeType", "focusAffinity", "strength"));
		map.put(RechargeSpellResult.class, getReflectiveSerialiser(RechargeSpellResult.class, "foeType", "focusAffinity", "value"));
		map.put(RemoveCurseSpellResult.class, getReflectiveSerialiser(RemoveCurseSpellResult.class, "foeType", "focusAffinity", "value"));
		map.put(RemoveItemSpellResult.class, getReflectiveSerialiser(RemoveItemSpellResult.class, "foeType", "focusAffinity", "itemName"));
		map.put(ResurrectionSpellResult.class, getReflectiveSerialiser(ResurrectionSpellResult.class, "foeType", "focusAffinity"));
		map.put(SingleUseSpellSpellResult.class, getReflectiveSerialiser(SingleUseSpellSpellResult.class, "foeType", "focusAffinity"));

		ReflectiveSerialiser srss = getReflectiveSerialiser(SummoningSpellResult.class, "foeType", "focusAffinity", "encounterTable", "strength");
		srss.addCustomSerialiser("encounterTable", new StringArraySerialiser());
		map.put(SummoningSpellResult.class, srss);
		map.put(TheftFailedSpellResult.class, getReflectiveSerialiser(TheftFailedSpellResult.class, "foeType", "focusAffinity", "value"));
		map.put(TheftSpellResult.class, getReflectiveSerialiser(TheftSpellResult.class, "foeType", "focusAffinity", "value"));
		map.put(UnlockSpellResult.class, getReflectiveSerialiser(UnlockSpellResult.class, "foeType", "focusAffinity", "value"));

		return new MazeObjectImplSerialiser<>(map, "foeType", "focusAffinity");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<LootEntry> getLootEntrySerialiser(Database db)
	{
		ReflectiveSerialiser defaultSerialiser = getReflectiveSerialiser(LootEntry.class, "name", "contains");

		defaultSerialiser.addCustomSerialiser("contains",
			new PercentageTableSerialiser<LootEntryRow>(
				getReflectiveSerialiser(LootEntryRow.class, "itemName", "quantity")));

		HashMap<Object, Object> map = new HashMap<>();
		map.put(LootEntry.class, defaultSerialiser);

		return new MazeObjectImplSerialiser(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<LootTable> getLootTableSerialiser(Database db)
	{
		ReflectiveSerialiser defaultSerialiser = getReflectiveSerialiser(
			LootTable.class,
			"name",
			"lootEntries");

		HashMap map1 = new HashMap();
		map1.put(SingleItemLootEntry.class, getReflectiveSerialiser(SingleItemLootEntry.class, "name"));
		map1.put(LootEntry.class, new NameSerialiserMap<>(db::getLootEntry));
		MazeObjectImplSerialiser<ILootEntry> lootTableRowSerialiser =
			new MazeObjectImplSerialiser<>(map1);

		defaultSerialiser.addCustomSerialiser("lootEntries", new GroupOfPossibiltiesSerialiser<>(lootTableRowSerialiser));

		HashMap<Object, Object> map = new HashMap<>();
		map.put(LootTable.class, defaultSerialiser);

		return new MazeObjectImplSerialiser(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<MazeScript> getMazeScriptSerialiser(
		Database db)
	{
		ReflectiveSerialiser result = getReflectiveSerialiser(MazeScript.class, "name", "events");

		result.addCustomSerialiser("events", new ListSerialiser(getMazeEventSerialiser(db)));

		return result;
	}

	private static V2SerialiserObject getMazeEventSerialiser(Database db)
	{
		HashMap<Class, V2SerialiserMap<MazeEvent>> map = new HashMap<>();

		map.put(ZoneChangeEvent.class, getReflectiveSerialiser(ZoneChangeEvent.class, "zone", "pos", "facing"));
		map.put(CastSpellEvent.class, getReflectiveSerialiser(CastSpellEvent.class, "spellName", "casterLevel", "castingLevel"));
		map.put(EncounterActorsEvent.class, getReflectiveSerialiser(EncounterActorsEvent.class,
			"mazeVariable", "encounterTable", "attitude", "ambushStatus", "preScript", "postAppearanceScript"));
		map.put(FlavourTextEvent.class, getReflectiveSerialiser(FlavourTextEvent.class, "flavourText", "delay", "shouldClearText"));
		map.put(GrantExperienceEvent.class, getReflectiveSerialiser(GrantExperienceEvent.class, "amount", "pc"));
		map.put(GrantGoldEvent.class, getReflectiveSerialiser(GrantGoldEvent.class, "amount"));
		map.put(SignBoardEvent.class, getReflectiveSerialiser(SignBoardEvent.class, "signBoardText"));

		ReflectiveSerialiser ltes = getReflectiveSerialiser(LootTableEvent.class, "lootTable");
		ltes.addCustomSerialiser("lootTable", new NameSerialiser<>(db::getLootTable));
		map.put(LootTableEvent.class, ltes);

		map.put(DelayEvent.class, getReflectiveSerialiser(DelayEvent.class, "delay"));
		map.put(MovePartyEvent.class, getReflectiveSerialiser(MovePartyEvent.class, "pos", "facing"));

//		map.put(CharacterClassKnowledgeEvent.class, getReflectiveSerialiser(CharacterClassKnowledgeEvent.class));

		map.put(MazeScriptEvent.class, getReflectiveSerialiser(MazeScriptEvent.class, "script"));
		map.put(RemoveWall.class, getReflectiveSerialiser(RemoveWall.class, "mazeVariable", "wallIndex", "horizontalWall"));
		map.put(BlockingScreenEvent.class, getReflectiveSerialiser(BlockingScreenEvent.class, "imageResource", "mode"));
		map.put(EndGameEvent.class, getReflectiveSerialiser(EndGameEvent.class));
		map.put(SetMazeVariableEvent.class, getReflectiveSerialiser(SetMazeVariableEvent.class, "mazeVariable", "value"));
		map.put(PersonalitySpeechBubbleEvent.class, getReflectiveSerialiser(PersonalitySpeechBubbleEvent.class, "speechKey", "modal"));
		map.put(StoryboardEvent.class, getReflectiveSerialiser(StoryboardEvent.class, "imageResource", "textResource", "textPlacement"));
		map.put(SetUserConfigEvent.class, getReflectiveSerialiser(SetUserConfigEvent.class, "var", "value"));
		map.put(TogglePortalStateEvent.class, getReflectiveSerialiser(TogglePortalStateEvent.class, "tile", "facing"));
		map.put(RemoveObjectEvent.class, getReflectiveSerialiser(RemoveObjectEvent.class, "objectName"));
		map.put(SkillTestEvent.class, getReflectiveSerialiser(SkillTestEvent.class,
			"keyModifier", "skill", "successValue", "successScript", "failureScript"));

		HashMap<Class, V2SerialiserMap<MazeEvent>> map2 = new HashMap<>();
		ReflectiveSerialiser pas = getReflectiveSerialiser(ProjectileAnimation.class, "projectileImages", "frameDelay");
		pas.addCustomSerialiser("projectileImages", new ListSerialiser(new DirectObjectSerialiser<String>()));
		map2.put(ProjectileAnimation.class, pas);
		map2.put(ColourMagicPortraitAnimation.class, getReflectiveSerialiser(ColourMagicPortraitAnimation.class, "colour"));
		MazeObjectImplSerialiser<MazeEvent> animationSerialiser = new MazeObjectImplSerialiser<>(map2);
		ReflectiveSerialiser aes = getReflectiveSerialiser(AnimationEvent.class, "animation");
		aes.addCustomSerialiser("animation", animationSerialiser);
		map.put(AnimationEvent.class, aes);

		ReflectiveSerialiser mes = getReflectiveSerialiser(MusicEvent.class, "trackNames", "musicState");
		mes.addCustomSerialiser("trackNames", new ListSerialiser(new DirectObjectSerialiser<String>()));
		map.put(MusicEvent.class, mes);

		ReflectiveSerialiser ses = getReflectiveSerialiser(SoundEffectEvent.class, "clipNames", "die");
		ses.addCustomSerialiser("clipNames", new ListSerialiser(new DirectObjectSerialiser<String>()));
		map.put(SoundEffectEvent.class, ses);

		return new MazeObjectImplSerialiser<>(map);
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<Spell> getSpellsSerialiser(Database db)
	{
		ReflectiveSerialiser defaultSerialiser = getReflectiveSerialiser(Spell.class,
			"name",
			"displayName",
			"hitPointCost",
			"actionPointCost",
			"magicPointCost",
			"description",
			"level",
			"targetType",
			"usabilityType",
			"school",
			"book",
			"effects",
			"requirementsToCast",
			"requirementsToLearn",
			"castByPlayerScript",
			"castByFoeScript",
			"primaryModifier",
			"secondaryModifier",
			"wildMagicValue",
			"wildMagicTable",
			"projectile");

		defaultSerialiser.addCustomSerialiser("book", new NameSerialiser<>(MagicSys.SpellBook::valueOf));
		defaultSerialiser.addCustomSerialiser("effects", new GroupOfPossibiltiesSerialiser<>(new NameSerialiser<>(db::getSpellEffect)));
		defaultSerialiser.addCustomSerialiser("requirementsToCast",
			new ListSerialiser(getReflectiveSerialiser(ColourMagicRequirement.class, "colour", "amount")));
		defaultSerialiser.addCustomSerialiser("castByPlayerScript", new NameSerialiser<>(db::getMazeScript));
		defaultSerialiser.addCustomSerialiser("castByFoeScript", new NameSerialiser<>(db::getMazeScript));
		defaultSerialiser.addCustomSerialiser("wildMagicTable", new StringArraySerialiser());

		HashMap<Class, V2SerialiserMap<Spell>> map = new HashMap<>();
		map.put(Spell.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<PlayerSpellBook> getPlayerSpellBooksSerialiser(
		Database db)
	{
		ReflectiveSerialiser defaultSerialiser =
			getReflectiveSerialiser(PlayerSpellBook.class, "name", "description", "spellNames");

		defaultSerialiser.addCustomSerialiser("spellNames", new ListSerialiser(new DirectObjectSerialiser<String>()));

		HashMap<Class, V2SerialiserMap<PlayerSpellBook>> map = new HashMap<>();
		map.put(PlayerSpellBook.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<MazeTexture> getMazeTextureSerialiser()
	{
		ReflectiveSerialiser defaultSerialiser =
			getReflectiveSerialiser(MazeTexture.class, "name", "imageResources", "animationDelay", "scrollBehaviour", "scrollSpeed");

		defaultSerialiser.addCustomSerialiser("imageResources", new ListSerialiser(new DirectObjectSerialiser<String>()));

		HashMap<Class, V2SerialiserMap<MazeTexture>> map = new HashMap<>();
		map.put(MazeTexture.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<FoeTemplate> getFoeTemplateSerialiser(
		Database db)
	{
		ReflectiveSerialiser defaultSerialiser =
			getReflectiveSerialiser(FoeTemplate.class,
				"name",
				"pluralName",
				"unidentifiedName",
				"unidentifiedPluralName",
				"types",
				"race",
				"characterClass",
				"hitPointsRange",
				"actionPointsRange",
				"magicPointsRange",
				"levelRange",
				"experience",
				"stats",
				"bodyParts",
				"playerBodyParts",
				"baseTexture",
				"meleeAttackTexture",
				"rangedAttackTexture",
				"castSpellTexture",
				"specialAbilityTexture",
				"verticalAlignment",
				"textureTint",
				"loot",
				"evasionBehaviour",
				"cannotBeEvaded",
				"identificationDifficulty",
				"foeGroupBannerModifiers",
				"allFoesBannerModifiers",
				"fleeChance",
				"stealthBehaviour",
				"faction",
				"npc",
				"appearanceScript",
				"animationScripts",
				"appearanceDirection",
				"deathScript",
				"naturalWeapons",
				"spellBook",
				"spellLikeAbilities",
				"focus",
				"defaultAttitude",
				"alliesOnCall");

		defaultSerialiser.addCustomSerialiser("types", new ListSerialiser(new NameSerialiser<>(db::getFoeType)));
		defaultSerialiser.addCustomSerialiser("race", new NameSerialiser<>(db::getRace));
		defaultSerialiser.addCustomSerialiser("characterClass", new NameSerialiser<>(db::getCharacterClass));
		defaultSerialiser.addCustomSerialiser("bodyParts", new PercentageTableSerialiser<>(new NameSerialiser<>(db::getBodyPart)));
		defaultSerialiser.addCustomSerialiser("playerBodyParts", new PercentageTableSerialiser<>(new DirectObjectSerialiser<String>()));
		defaultSerialiser.addCustomSerialiser("baseTexture", new NameSerialiserMap<>(db::getMazeTexture));
		defaultSerialiser.addCustomSerialiser("meleeAttackTexture", new NameSerialiserMap<>(db::getMazeTexture));
		defaultSerialiser.addCustomSerialiser("rangedAttackTexture", new NameSerialiserMap<>(db::getMazeTexture));
		defaultSerialiser.addCustomSerialiser("castSpellTexture", new NameSerialiserMap<>(db::getMazeTexture));
		defaultSerialiser.addCustomSerialiser("specialAbilityTexture", new NameSerialiserMap<>(db::getMazeTexture));
		defaultSerialiser.addCustomSerialiser("loot", new NameSerialiserMap<>(db::getLootTable));
		defaultSerialiser.addCustomSerialiser("animationScripts", new ListSerialiser(getAnimationScriptSerialiser()));
		defaultSerialiser.addCustomSerialiser("appearanceScript", new NameSerialiserMap<>(db::getMazeScript));
		defaultSerialiser.addCustomSerialiser("deathScript", new NameSerialiserMap<>(db::getMazeScript));
		defaultSerialiser.addCustomSerialiser("naturalWeapons", new ListSerialiser(new DirectObjectSerialiser<String>()));
		defaultSerialiser.addCustomSerialiser("spellBook", getSpellBookSerialiser(db));
		defaultSerialiser.addCustomSerialiser("spellLikeAbilities", new ListSerialiser(getSpellLikeAbilitySerialiser(db)));

		HashMap<Class, V2SerialiserMap<FoeTemplate>> map = new HashMap<>();
		map.put(FoeTemplate.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	private static ReflectiveSerialiser<SpellBook> getSpellBookSerialiser(
		Database db)
	{
		ReflectiveSerialiser<SpellBook> result = getReflectiveSerialiser(SpellBook.class, "spells");

		result.addCustomSerialiser("spells", new ListSerialiser(new NameSerialiser<>(db::getSpell)));

		return result;
	}

	private static V2SerialiserObject<ObjectScript> getAnimationScriptSerialiser()
	{
		HashMap<Class, V2SerialiserMap<ObjectScript>> map = new HashMap<>();

		map.put(JagObjectVertically.class, new ReflectiveSerialiser<>(
			JagObjectVertically.class,
			"minOffset",
			"maxOffset",
			"minSpeed",
			"maxSpeed",
			"minPause",
			"maxPause",
			"pauseTop",
			"pauseBottom",
			"homeTop",
			"homeBottom"));

		map.put(JagObjectWithinRadius.class, new ReflectiveSerialiser<>(
			JagObjectWithinRadius.class,
			"maxRadius",
			"minSpeed",
			"maxSpeed",
			"minPause",
			"maxPause"));

		map.put(SinusoidalStretch.class, new ReflectiveSerialiser<>(
			SinusoidalStretch.class,
			"speed",
			"minStretch",
			"maxStretch",
			"vertical",
			"horizontal"));

		return new MazeObjectImplSerialiser<>(map);
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<Trap> getTrapSerialiser(Database db)
	{
		ReflectiveSerialiser<Trap> defaultSerialiser = getReflectiveSerialiser(
			Trap.class,
			"name",
			"difficulty",
			"required",
			"payload");

		defaultSerialiser.addCustomSerialiser("difficulty", new IntArraySerialiser());
		defaultSerialiser.addCustomSerialiser("payload", getTileScriptSerialiser(db));

		HashMap<Class, V2SerialiserMap<Trap>> map = new HashMap<>();
		map.put(Trap.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	private static V2SerialiserObject<TileScript> getTileScriptSerialiser(
		Database db)
	{
		HashMap<Class, V2SerialiserMap<TileScript>> map = new HashMap<>();

		map.put(CastSpell.class, getReflectiveSerialiser(CastSpell.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty",
			"spellName", "castingLevel", "casterLevel"));

		ReflectiveSerialiser chestSerialiser = getReflectiveSerialiser(Chest.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty",
			"chestContents", "traps", "mazeVariable", "northTexture", "southTexture", "eastTexture", "westTexture", "preScript");
		chestSerialiser.addCustomSerialiser("traps", new PercentageTableSerialiser<>(new NameSerialiser<>(db::getTrap)));
		map.put(Chest.class, chestSerialiser);

		ReflectiveSerialiser encounterSerialiser = getReflectiveSerialiser(Encounter.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty",
			"encounterTable", "mazeVariable", "attitude", "ambushStatus", "preScript", "postAppearanceScript");
		encounterSerialiser.addCustomSerialiser("encounterTable", new NameSerialiser<>(db::getEncounterTable));
		map.put(Encounter.class, encounterSerialiser);

		map.put(FlavourText.class, getReflectiveSerialiser(FlavourText.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty", "text"));

		map.put(PersonalitySpeech.class, getReflectiveSerialiser(PersonalitySpeech.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty", "speechKey", "modal"));

		ReflectiveSerialiser optionsSerialiser = getReflectiveSerialiser(DisplayOptions.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty",
			"forceSelection", "title", "options", "scripts");
		optionsSerialiser.addCustomSerialiser("options", new ListSerialiser(new DirectObjectSerialiser<String>()));
		optionsSerialiser.addCustomSerialiser("scripts", new ListSerialiser(new DirectObjectSerialiser<String>()));
		map.put(DisplayOptions.class, optionsSerialiser);

		map.put(Loot.class, getReflectiveSerialiser(Loot.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty", "lootTable"));

		map.put(RemoveWall.class, getReflectiveSerialiser(RemoveWall.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty",
			"mazeVariable", "wallIndex", "horizontalWall"));

		map.put(ExecuteMazeScript.class, getReflectiveSerialiser(ExecuteMazeScript.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty", "mazeScript"));

		map.put(SignBoard.class, getReflectiveSerialiser(SignBoard.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty", "text"));
		map.put(SetMazeVariable.class, getReflectiveSerialiser(SetMazeVariable.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty", "mazeVariable", "value"));
		map.put(HiddenStuff.class, getReflectiveSerialiser(HiddenStuff.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty", "mazeVariable",
			"findDifficulty", "mazeVariable", "preScript", "content"));
		map.put(Water.class, getReflectiveSerialiser(Water.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty", "mazeVariable"));

		ReflectiveSerialiser leverSerialiser = getReflectiveSerialiser(Lever.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty", "mazeVariable",
			"northTexture", "southTexture", "eastTexture", "westTexture", "mazeVariable",
			"preTransitionScript", "postTransitionScript");
		leverSerialiser.addCustomSerialiser("preTransitionScript", new NameSerialiser<>(db::getMazeScript));
		leverSerialiser.addCustomSerialiser("postTransitionScript", new NameSerialiser<>(db::getMazeScript));
		map.put(Lever.class, leverSerialiser);

		ReflectiveSerialiser toggleWallSerialiser = getReflectiveSerialiser(ToggleWall.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty", "mazeVariable",
			"mazeVariable",
			"wallIndex",
			"horizontalWall",
			"state1Texture",
			"state1MaskTexture",
			"state1Visible",
			"state1Solid",
			"state1Secret",
			"state1Height",
			"state2Texture",
			"state2MaskTexture",
			"state2Visible",
			"state2Solid",
			"state2Secret",
			"state2Height",
			"preToggleScript",
			"postToggleScript");
		toggleWallSerialiser.addCustomSerialiser("state1Texture", new NameSerialiser<>(db::getMazeTexture));
		toggleWallSerialiser.addCustomSerialiser("state1MaskTexture", new NameSerialiser<>(db::getMazeTexture));
		toggleWallSerialiser.addCustomSerialiser("state2Texture", new NameSerialiser<>(db::getMazeTexture));
		toggleWallSerialiser.addCustomSerialiser("state2MaskTexture", new NameSerialiser<>(db::getMazeTexture));
		map.put(ToggleWall.class, toggleWallSerialiser);

		map.put(SkillTest.class, getReflectiveSerialiser(SkillTest.class,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty", "mazeVariable",
			"keyModifier", "skill", "successValue", "successScript", "failureScript"));

		return new MazeObjectImplSerialiser<>(map,
			"executeOnceMazeVariable", "facings", "reexecuteOnSameTile", "scoutSecretDifficulty");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<FoeEntry> getFoeEntrySerialiser(Database db)
	{
		ReflectiveSerialiser<FoeEntry> defaultSerialiser = getReflectiveSerialiser(
			FoeEntry.class, "name", "contains");

		defaultSerialiser.addCustomSerialiser("contains",
			new GroupOfPossibiltiesSerialiser<FoeEntry>(
				getReflectiveSerialiser(FoeEntryRow.class, "foeName", "quantity")));

		HashMap<Class, V2SerialiserMap<FoeEntry>> map = new HashMap<>();
		map.put(FoeEntry.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<EncounterTable> getEncounterTableSerialiser(
		Database db)
	{
		ReflectiveSerialiser<EncounterTable> defaultSerialiser = getReflectiveSerialiser(
			EncounterTable.class, "name", "encounterTable");

		defaultSerialiser.addCustomSerialiser("encounterTable",
			new PercentageTableSerialiser<>(new NameSerialiser<>(db::getFoeEntry)));

		HashMap<Class, V2SerialiserMap<EncounterTable>> map = new HashMap<>();
		map.put(EncounterTable.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<NpcFactionTemplate> getNpcFactionTemplatesSerialiser(
		Database db)
	{
		ReflectiveSerialiser<NpcFactionTemplate> defaultSerialiser = getReflectiveSerialiser(
			NpcFactionTemplate.class, "name", "startingAttitude");

		HashMap<Class, V2SerialiserMap<NpcFactionTemplate>> map = new HashMap<>();
		map.put(NpcFactionTemplate.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<NpcTemplate> getNpcTemplatesSerialiser(
		Database db)
	{
		ReflectiveSerialiser<NpcTemplate> defaultSerialiser = getReflectiveSerialiser(
			NpcTemplate.class,
			"name",
			"foeName",
			"faction",
			"attitude",
			"script",
			"alliesOnCall",
			"buysAt",
			"sellsAt",
			"maxPurchasePrice",
			"willBuyItemTypes",
			"inventoryTemplate",
			"resistThreats",
			"resistBribes",
			"resistSteal",
			"theftCounter",
			"dialogue",
			"zone",
			"tile",
			"found",
			"dead",
			"guildMaster");

		defaultSerialiser.addCustomSerialiser("script", new MazeObjectImplSerialiser<>(new HashMap<>()));

		ReflectiveSerialiser npcInvTemplateSerialiser = getReflectiveSerialiser(NpcInventoryTemplate.class, "rows");

		HashMap<Object, Object> npcInvRowItemSerialiser = new HashMap<>();
		npcInvRowItemSerialiser.put(NpcInventoryTemplateRowItem.class,
			getReflectiveSerialiser(NpcInventoryTemplateRowItem.class,
				"chanceOfSpawning", "partyLevelAppearing", "maxStocked", "chanceOfVanishing", "itemName", "stackSize"));
		npcInvRowItemSerialiser.put(NpcInventoryTemplateRowLootEntry.class,
			getReflectiveSerialiser(NpcInventoryTemplateRowLootEntry.class,
				"chanceOfSpawning", "partyLevelAppearing", "maxStocked", "chanceOfVanishing", "lootEntry", "itemsToSpawn"));

		npcInvTemplateSerialiser.addCustomSerialiser("rows",
			new ListSerialiser(
				new MazeObjectImplSerialiser(npcInvRowItemSerialiser,
					"chanceOfSpawning", "partyLevelAppearing", "maxStocked", "chanceOfVanishing")));

		defaultSerialiser.addCustomSerialiser("inventoryTemplate", npcInvTemplateSerialiser);
		defaultSerialiser.addCustomSerialiser("dialogue", getNpcSpeechSerialiser());

		HashMap<Class, V2SerialiserMap<NpcTemplate>> map = new HashMap<>();
		map.put(NpcTemplate.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<NpcSpeech> getNpcSpeechSerialiser()
	{
		ReflectiveSerialiser result = getReflectiveSerialiser(NpcSpeech.class, "dialogue");

		ReflectiveSerialiser rowSerialiser = getReflectiveSerialiser(NpcSpeechRow.class, "priority", "keywords", "speech");

		rowSerialiser.addCustomSerialiser("keywords", new StringSetSerialiser());

		result.addCustomSerialiser("dialogue", new ListSerialiser(rowSerialiser));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<WieldingCombo> getWieldingComboSerialiser()
	{
		ReflectiveSerialiser<WieldingCombo> defaultSerialiser = getReflectiveSerialiser(
			WieldingCombo.class, "name", "primaryHand", "secondaryHand", "modifiers");

		HashMap<Class, V2SerialiserMap<WieldingCombo>> map = new HashMap<>();
		map.put(WieldingCombo.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<ItemTemplate> getItemTemplateSerialiser(
		Database db)
	{
		ReflectiveSerialiser<ItemTemplate> defaultSerialiser = getReflectiveSerialiser(
			ItemTemplate.class,
			"name",
			"name",
			"pluralName",
			"unidentifiedName",
			"type",
			"subtype",
			"description",
			"modifiers",
			"image",
			"equipableSlots",
			"weight",
			"maxItemsPerStack",
			"baseCost",
			"invokedSpell",
			"invokedSpellLevel",
			"charges",
			"chargesType",
			"usableByCharacterClass",
			"usableByRace",
			"usableByGender",
			"questItem",
			"curseStrength",
			"identificationDifficulty",
			"rechargeDifficulty",
			"equipRequirements",
			"useRequirements",
			"attackScript",
			"damage",
			"defaultDamageType",
			"attackTypes",
			"twoHanded",
			"ranged",
			"returning",
			"backstabCapable",
			"snipeCapable",
			"toHit",
			"toPenetrate",
			"toCritical",
			"toInitiative",
			"minRange",
			"maxRange",
			"ammo",
			"spellEffects",
			"bonusAttacks",
			"bonusStrikes",
			"discipline",
			"slaysFoeType",
			"ammoType",
			"damagePrevention",
			"damagePreventionChance",
			"enchantmentChance",
			"enchantmentCalculation",
			"enchantmentScheme",
			"disassemblyLootTable",
			"conversionRate");

		defaultSerialiser.addCustomSerialiser("invokedSpell", new NameSerialiser<>(db::getSpell));
		defaultSerialiser.addCustomSerialiser("attackScript", new NameSerialiser<>(db::getMazeScript));
		defaultSerialiser.addCustomSerialiser("ammo", new ListSerialiser(new NameSerialiser<>(ItemTemplate.AmmoType::valueOf)));
		defaultSerialiser.addCustomSerialiser("spellEffects", new GroupOfPossibiltiesSerialiser<>(new NameSerialiser<>(db::getSpellEffect)));

		HashMap<Class, V2SerialiserMap<ItemTemplate>> map = new HashMap<>();
		map.put(ItemTemplate.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<DifficultyLevel> getDifficultyLevelSerialiser()
	{
		HashMap<Class, V2SerialiserMap<DifficultyLevel>> map = new HashMap<>();
		map.put(DifficultyLevel.class, null);
		return new MazeObjectImplSerialiser<>(map, "name", "sortOrder");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<CraftRecipe> getCraftRecipeSerialiser()
	{
		return getReflectiveSerialiser(
			CraftRecipe.class, "name", "requirements", "item1", "item2", "resultingItem");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<ItemEnchantments> getItemEnchantmentsSerialiser()
	{
		ReflectiveSerialiser result = getReflectiveSerialiser(
			ItemEnchantments.class, "name", "enchantments");

		result.addCustomSerialiser("enchantments",
			new PercentageTableSerialiser<>(
				getReflectiveSerialiser(ItemEnchantment.class,
					"name", "modifiers", "prefix", "suffix", "costModifier")));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<NaturalWeapon> getNaturalWeaponSerialiser(
		Database db)
	{
		ReflectiveSerialiser<NaturalWeapon> defaultSerialiser = getReflectiveSerialiser(
			NaturalWeapon.class,
			"name",
			"name",
			"description",
			"ranged",
			"damage",
			"damageType",
			"modifiers",
			"minRange",
			"maxRange",
			"spellEffects",
			"spellEffectLevel",
			"attacks",
			"slaysFoeType",
			"attackScript");

		defaultSerialiser.addCustomSerialiser("spellEffects",
			new GroupOfPossibiltiesSerialiser<>(new NameSerialiser<>(db::getSpellEffect)));
		defaultSerialiser.addCustomSerialiser("attackScript",
			new NameSerialiser<>(db::getMazeScript));

		HashMap<Class, V2SerialiserMap<NaturalWeapon>> map = new HashMap<>();
		map.put(NaturalWeapon.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<StartingKit> getStartingKitSerialiser()
	{
		ReflectiveSerialiser<StartingKit> defaultSerialiser = getReflectiveSerialiser(
			StartingKit.class,
			"name",
			"displayName",
			"primaryWeapon",
			"secondaryWeapon",
			"helm",
			"torsoArmour",
			"legArmour",
			"gloves",
			"boots",
			"miscItem1",
			"miscItem2",
			"bannerItem",
			"packItems",
			"description",
			"combatModifiers",
			"stealthModifiers",
			"magicModifiers",
			"usableByCharacterClass");

		defaultSerialiser.addCustomSerialiser("packItems", new ListSerialiser(new DirectObjectSerialiser<String>()));

		HashMap<Class, V2SerialiserMap<StartingKit>> map = new HashMap<>();
		map.put(StartingKit.class, defaultSerialiser);
		return new MazeObjectImplSerialiser<>(map, "name");
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<Personality> getPersonalitySerialiser()
	{
		ReflectiveSerialiser<Personality> result = getReflectiveSerialiser(
			Personality.class, "name", "description", "colour", "speech");

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<FoeType> getFoeTypeSerialiser(Database db)
	{
		ReflectiveSerialiser<FoeType> result = getReflectiveSerialiser(
			FoeType.class,
			"name",
			"description",
			"startingHitPointPercent",
			"startingActionPointPercent",
			"startingMagicPointPercent",
			"startingModifiers",
			"constantModifiers",
			"bannerModifiers",
			"attributeCeilings",
			"head",
			"torso",
			"leg",
			"hand",
			"foot",
			"leftHandIcon",
			"rightHandIcon",
			"allowedGenders",
			"magicDead",
			"specialAbility",
			"startingItems",
			"naturalWeapons",
			"suggestedNames",
			"unlockVariable",
			"unlockDescription",
			"favouredEnemyModifier",
			"characterCreationImage");

		result.addCustomSerialiser(BodyPart.class, new NameSerialiser<>(db::getBodyPart));
		result.addCustomSerialiser("allowedGenders", new NameListSerialiser<>(db::getGender));
		result.addCustomSerialiser("specialAbility", new NameSerialiser<>(db::getSpell));
		result.addCustomSerialiser("startingItems", new NameListSerialiser<>(db::getStartingKit));
		result.addCustomSerialiser("naturalWeapons", new NameListSerialiser<>(db::getNaturalWeapon));
//		result.addCustomSerialiser("suggestedNames", new DirectObjectSerialiser());

		return result;
	}
}
