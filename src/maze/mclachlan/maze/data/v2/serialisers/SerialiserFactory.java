package mclachlan.maze.data.v2.serialisers;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.condition.RepeatedSpellEffect;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.ValueList;

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
		return new MazeObjectImplSerialiser<>(map );
	}

	/*-------------------------------------------------------------------------*/
	public static V2SerialiserMap<CharacterClass> getCharacterClassSerialiser(Database db)
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

		ReflectiveSerialiser salas = getReflectiveSerialiser(
			SpecialAbilityLevelAbility.class, "key", "displayName", "description", "ability");

		ReflectiveSerialiser slas = getReflectiveSerialiser(
			SpellLikeAbility.class, "spell", "castingLevel");
		slas.addCustomSerialiser("spell", new NameSerialiser<Spell>(db::getSpell));
		salas.addCustomSerialiser(SpellLikeAbility.class, slas);

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
	public static V2SerialiserMap<ConditionTemplate> getConditionTemplateSerialiser(Database db)
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
}
