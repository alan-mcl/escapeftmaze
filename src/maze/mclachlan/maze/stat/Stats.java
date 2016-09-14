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

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Stats
{
	private StatModifier modifiers = new StatModifier();

	// attributes
	CurMaxSub hitPoints = new CurMaxSub();
	CurMax actionPoints = new CurMax();
	CurMax magicPoints = new CurMax();

	// useful stuff
	/** HP, SP and MP */
	public static List<Modifier> resourceModifiers = new ArrayList<Modifier>();
	
	/** Str, Agl, and so on */
	public static List<Modifier> attributeModifiers = new ArrayList<Modifier>();

	/** swing, thrust, and so on */
	public static List<Modifier> martialModifiers = new ArrayList<Modifier>();

	/** steal, stealth, and so on */
	public static List<Modifier> stealthModifiers = new ArrayList<Modifier>();

	/** chant, gesture, and so on */
	public static List<Modifier> magicModifiers = new ArrayList<Modifier>();

	/** The set of modifiers that can be edited by the player */
	public static List<Modifier> regularModifiers = new ArrayList<Modifier>();

	/** The various stats that can't be edited by the player */
	public static List<Modifier> statistics = new ArrayList<Modifier>();

	/** Resistance modifiers */
	public static List<Modifier> resistances = new ArrayList<Modifier>();

	/** properties of a character - boolean flags that can't be edited */
	public static List<Modifier> propertiesModifiers = new ArrayList<Modifier>();

	/** all modifiers */
	public static List<Modifier> allModifiers = new ArrayList<Modifier>();

	/** all modifiers except attributes and resistances */
	public static List<Modifier> middleModifiers = new ArrayList<Modifier>();

	/*-------------------------------------------------------------------------*/
	static
	{
		resourceModifiers.add(Modifier.HIT_POINTS);
		resourceModifiers.add(Modifier.ACTION_POINTS);
		resourceModifiers.add(Modifier.MAGIC_POINTS);
		
		attributeModifiers.add(Modifier.BRAWN);
		attributeModifiers.add(Modifier.SKILL);
		attributeModifiers.add(Modifier.THIEVING);
		attributeModifiers.add(Modifier.SNEAKING);
		attributeModifiers.add(Modifier.BRAINS);
		attributeModifiers.add(Modifier.POWER);

		martialModifiers.add(Modifier.SWING);
		martialModifiers.add(Modifier.THRUST);
		martialModifiers.add(Modifier.CUT);
		martialModifiers.add(Modifier.LUNGE);
		martialModifiers.add(Modifier.BASH);
		martialModifiers.add(Modifier.PUNCH);
		martialModifiers.add(Modifier.KICK);
		martialModifiers.add(Modifier.SHOOT);
		martialModifiers.add(Modifier.THROW);
		martialModifiers.add(Modifier.FIRE);
		martialModifiers.add(Modifier.DUAL_WEAPONS);
		martialModifiers.add(Modifier.CHIVALRY);
		martialModifiers.add(Modifier.KENDO);

		stealthModifiers.add(Modifier.STEAL);
		stealthModifiers.add(Modifier.LOCK_AND_TRAP);
		stealthModifiers.add(Modifier.DUNGEONEER);
		stealthModifiers.add(Modifier.STREETWISE);
		stealthModifiers.add(Modifier.WILDERNESS_LORE);
		stealthModifiers.add(Modifier.SURVIVAL);
		stealthModifiers.add(Modifier.BACKSTAB);
		stealthModifiers.add(Modifier.SNIPE);
		stealthModifiers.add(Modifier.MARTIAL_ARTS);
		stealthModifiers.add(Modifier.MELEE_CRITICALS);
		stealthModifiers.add(Modifier.THROWN_CRITICALS);
		stealthModifiers.add(Modifier.RANGED_CRITICALS);
		stealthModifiers.add(Modifier.SCOUTING);

		magicModifiers.add(Modifier.CHANT);
		magicModifiers.add(Modifier.RHYME);
		magicModifiers.add(Modifier.GESTURE);
		magicModifiers.add(Modifier.POSTURE);
		magicModifiers.add(Modifier.THOUGHT);
		magicModifiers.add(Modifier.ALCHEMIC);
		magicModifiers.add(Modifier.HERBAL);
		magicModifiers.add(Modifier.ARTIFACTS);
		magicModifiers.add(Modifier.MYTHOLOGY);
		magicModifiers.add(Modifier.CRAFT);
		magicModifiers.add(Modifier.POWER_CAST);
		magicModifiers.add(Modifier.ENGINEERING);
		magicModifiers.add(Modifier.MUSIC);

		regularModifiers.addAll(attributeModifiers);
		regularModifiers.addAll(martialModifiers);
		regularModifiers.addAll(stealthModifiers);
		regularModifiers.addAll(magicModifiers);

		statistics.add(Modifier.INITIATIVE);
		statistics.add(Modifier.ATTACK);
		statistics.add(Modifier.DEFENCE);
		statistics.add(Modifier.DAMAGE);
		statistics.add(Modifier.TO_PENETRATE);
		statistics.add(Modifier.VS_PENETRATE);
		statistics.add(Modifier.VS_AMBUSH);
		statistics.add(Modifier.VS_DODGE);
		statistics.add(Modifier.VS_HIDE);
		statistics.add(Modifier.TO_BRIBE);
		statistics.add(Modifier.TO_RUN_AWAY);

		statistics.add(Modifier.RED_MAGIC_GEN);
		statistics.add(Modifier.BLACK_MAGIC_GEN);
		statistics.add(Modifier.PURPLE_MAGIC_GEN);
		statistics.add(Modifier.GOLD_MAGIC_GEN);
		statistics.add(Modifier.WHITE_MAGIC_GEN);
		statistics.add(Modifier.GREEN_MAGIC_GEN);
		statistics.add(Modifier.BLUE_MAGIC_GEN);

		statistics.add(Modifier.SORCERY_SPELLS);
		statistics.add(Modifier.BLACK_MAGIC_SPELLS);
		statistics.add(Modifier.WITCHCRAFT_SPELLS);
		statistics.add(Modifier.ENCHANTMENT_SPELLS);
		statistics.add(Modifier.WHITE_MAGIC_SPELLS);
		statistics.add(Modifier.DRUIDISM_SPELLS);
		statistics.add(Modifier.ELEMENTAL_SPELLS);

		statistics.add(Modifier.HIT_POINT_REGEN);
		statistics.add(Modifier.ACTION_POINT_REGEN);
		statistics.add(Modifier.MAGIC_POINT_REGEN);
		statistics.add(Modifier.STAMINA_REGEN);

		resistances.add(Modifier.RESIST_BLUDGEONING);
		resistances.add(Modifier.RESIST_PIERCING);
		resistances.add(Modifier.RESIST_SLASHING);
		resistances.add(Modifier.RESIST_FIRE);
		resistances.add(Modifier.RESIST_WATER);
		resistances.add(Modifier.RESIST_EARTH);
		resistances.add(Modifier.RESIST_AIR);
		resistances.add(Modifier.RESIST_MENTAL);
		resistances.add(Modifier.RESIST_ENERGY);

		statistics.addAll(resistances);

		propertiesModifiers.add(Modifier.IMMUNE_TO_DAMAGE);
		propertiesModifiers.add(Modifier.IMMUNE_TO_HEAT);
		propertiesModifiers.add(Modifier.IMMUNE_TO_COLD);
		propertiesModifiers.add(Modifier.IMMUNE_TO_LIGHTNING);
		propertiesModifiers.add(Modifier.IMMUNE_TO_POISON);
		propertiesModifiers.add(Modifier.IMMUNE_TO_PSYCHIC);
		propertiesModifiers.add(Modifier.IMMUNE_TO_ACID);

		propertiesModifiers.add(Modifier.IMMUNE_TO_BLIND);
		propertiesModifiers.add(Modifier.IMMUNE_TO_DISEASE);
		propertiesModifiers.add(Modifier.IMMUNE_TO_FEAR);
		propertiesModifiers.add(Modifier.IMMUNE_TO_HEX);
		propertiesModifiers.add(Modifier.IMMUNE_TO_INSANE);
		propertiesModifiers.add(Modifier.IMMUNE_TO_IRRITATE);
		propertiesModifiers.add(Modifier.IMMUNE_TO_KO);
		propertiesModifiers.add(Modifier.IMMUNE_TO_NAUSEA);
		propertiesModifiers.add(Modifier.IMMUNE_TO_PARALYSE);
		propertiesModifiers.add(Modifier.IMMUNE_TO_POSSESSION);
		propertiesModifiers.add(Modifier.IMMUNE_TO_SILENCE);
		propertiesModifiers.add(Modifier.IMMUNE_TO_SLEEP);
		propertiesModifiers.add(Modifier.IMMUNE_TO_STONE);
		propertiesModifiers.add(Modifier.IMMUNE_TO_SWALLOW);
		propertiesModifiers.add(Modifier.IMMUNE_TO_WEB);

		propertiesModifiers.add(Modifier.LIGHT_SLEEPER);
		propertiesModifiers.add(Modifier.BLIND_FIGHTING);
		propertiesModifiers.add(Modifier.EXTRA_GOLD);
		propertiesModifiers.add(Modifier.CHEAT_DEATH);
		propertiesModifiers.add(Modifier.MAGIC_ABSORPTION);
		propertiesModifiers.add(Modifier.ARROW_CUTTING);
		propertiesModifiers.add(Modifier.AMBUSHER);
		propertiesModifiers.add(Modifier.ENTERTAINER);
		propertiesModifiers.add(Modifier.DIPLOMAT);
		propertiesModifiers.add(Modifier.BLINK);
		propertiesModifiers.add(Modifier.TIRELESS_AXE);
		propertiesModifiers.add(Modifier.TIRELESS_BOW);
		propertiesModifiers.add(Modifier.TIRELESS_DAGGER);
		propertiesModifiers.add(Modifier.TIRELESS_MACE);
		propertiesModifiers.add(Modifier.TIRELESS_SPEAR);
		propertiesModifiers.add(Modifier.TIRELESS_STAFF);
		propertiesModifiers.add(Modifier.TIRELESS_SWORD);
		propertiesModifiers.add(Modifier.TIRELESS_THROWN);
		propertiesModifiers.add(Modifier.TIRELESS_UNARMED);
		propertiesModifiers.add(Modifier.TOUCH_BLIND);
		propertiesModifiers.add(Modifier.TOUCH_DISEASE);
		propertiesModifiers.add(Modifier.TOUCH_FEAR);
		propertiesModifiers.add(Modifier.TOUCH_HEX);
		propertiesModifiers.add(Modifier.TOUCH_INSANE);
		propertiesModifiers.add(Modifier.TOUCH_IRRITATE);
		propertiesModifiers.add(Modifier.TOUCH_NAUSEA);
		propertiesModifiers.add(Modifier.TOUCH_PARALYSE);
		propertiesModifiers.add(Modifier.TOUCH_SILENCE);
		propertiesModifiers.add(Modifier.TOUCH_SLEEP);
		propertiesModifiers.add(Modifier.TOUCH_STONE);
		propertiesModifiers.add(Modifier.TOUCH_WEB);
		propertiesModifiers.add(Modifier.TOUCH_POISON);
		propertiesModifiers.add(Modifier.RAZOR_CLOAK);
		propertiesModifiers.add(Modifier.CC_PENALTY);
		propertiesModifiers.add(Modifier.DAMAGE_MULTIPLIER);
		propertiesModifiers.add(Modifier.LIGHTNING_STRIKE_SWORD);
		propertiesModifiers.add(Modifier.LIGHTNING_STRIKE_AXE);
		propertiesModifiers.add(Modifier.LIGHTNING_STRIKE_SPEAR);
		propertiesModifiers.add(Modifier.LIGHTNING_STRIKE_MACE);
		propertiesModifiers.add(Modifier.LIGHTNING_STRIKE_DAGGER);
		propertiesModifiers.add(Modifier.LIGHTNING_STRIKE_STAFF);
		propertiesModifiers.add(Modifier.LIGHTNING_STRIKE_UNARMED);
		propertiesModifiers.add(Modifier.BERSERKER);
		propertiesModifiers.add(Modifier.BERSERK_POWERS);
		propertiesModifiers.add(Modifier.DEADLY_STRIKE);
		propertiesModifiers.add(Modifier.DODGE);
		propertiesModifiers.add(Modifier.MASTER_ARCHER);
		propertiesModifiers.add(Modifier.DIVINE_PROTECTION);
		propertiesModifiers.add(Modifier.KI_FURY);
		propertiesModifiers.add(Modifier.FEY_AFFINITY);
		propertiesModifiers.add(Modifier.ARCANE_BLOOD);
		propertiesModifiers.add(Modifier.DISPLACER);
		propertiesModifiers.add(Modifier.PARRY);
		propertiesModifiers.add(Modifier.MELEE_MASTER);
		propertiesModifiers.add(Modifier.DEADLY_AIM);
		propertiesModifiers.add(Modifier.MASTER_THIEF);
		propertiesModifiers.add(Modifier.OBFUSCATION);
		propertiesModifiers.add(Modifier.SHADOW_MASTER);
		propertiesModifiers.add(Modifier.CHARMED_DESTINY);
		propertiesModifiers.add(Modifier.CHANNELLING);
		propertiesModifiers.add(Modifier.SIGNATURE_WEAPON_ENGINEERING);
		propertiesModifiers.add(Modifier.AMPHIBIOUS);
		propertiesModifiers.add(Modifier.BONUS_ATTACKS);
		propertiesModifiers.add(Modifier.BONUS_STRIKES);
		propertiesModifiers.add(Modifier.LARGE_SIZE);
		propertiesModifiers.add(Modifier.THREATEN);
		propertiesModifiers.add(Modifier.DRINKING_FIT);
		propertiesModifiers.add(Modifier.IAJUTSU);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_BEAST);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_CONSTRUCT);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_MAZE_CREATURE);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_CRYPTOBESTIA);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_DRAGON);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_ELEMENTAL);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_FEY);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_GIANT);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_HORROR);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_HUMANOID);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_ILLUSION);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_MONSTROSITY);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_OOZE);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_OUTSIDER);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_PLANT);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_UNDEAD);
		propertiesModifiers.add(Modifier.FAVOURED_ENEMY_VERMIN);
		propertiesModifiers.add(Modifier.POWER_OF_DARKNESS);
		propertiesModifiers.add(Modifier.FLIER);
		propertiesModifiers.add(Modifier.STRONG_SWIMMER);
		propertiesModifiers.add(Modifier.FURIOUS_PURPOSE);
		propertiesModifiers.add(Modifier.SWORD_PARRY);
		propertiesModifiers.add(Modifier.AXE_PARRY);
		propertiesModifiers.add(Modifier.MACE_PARRY);
		propertiesModifiers.add(Modifier.POLEARM_PARRY);
		propertiesModifiers.add(Modifier.STAFF_PARRY);
		propertiesModifiers.add(Modifier.AMAZON_COURAGE);
		propertiesModifiers.add(Modifier.AMAZON_WILLPOWER);
		propertiesModifiers.add(Modifier.AMAZON_FURY);
		propertiesModifiers.add(Modifier.SWORD_1H_WIELD);
		propertiesModifiers.add(Modifier.AXE_1H_WIELD);
		propertiesModifiers.add(Modifier.MACE_1H_WIELD);
		propertiesModifiers.add(Modifier.POLEARM_1H_WIELD);
		propertiesModifiers.add(Modifier.STAFF_1H_WIELD);

		allModifiers.addAll(resourceModifiers);
		allModifiers.addAll(regularModifiers);
		allModifiers.addAll(propertiesModifiers);
		allModifiers.addAll(statistics);

		middleModifiers.addAll(allModifiers);
		middleModifiers.removeAll(attributeModifiers);
		middleModifiers.removeAll(resistances);
	}

	/*-------------------------------------------------------------------------*/
	public Stats()
	{
	}
	
	/*-------------------------------------------------------------------------*/
	public Stats(
		CurMaxSub hitPoints, 
		CurMax actionPoints,
		CurMax magicPoints, 
		StatModifier modifiers)
	{
		this.hitPoints = hitPoints;
		this.magicPoints = magicPoints;
		this.modifiers = new StatModifier(modifiers);
		this.actionPoints = actionPoints;
	}

	/*-------------------------------------------------------------------------*/
	public Stats(StatModifier seed)
	{
		this.modifiers = new StatModifier(seed);
	}

	/*-------------------------------------------------------------------------*/
	public Stats(Stats s)
	{
		this.hitPoints = new CurMaxSub(s.hitPoints);
		this.actionPoints = new CurMax(s.actionPoints);
		this.magicPoints = new CurMax(s.magicPoints);
		this.modifiers = new StatModifier(s.modifiers);
	}

	/*-------------------------------------------------------------------------*/
	public static String descModifier(Modifier modifier, int value)
	{
		ModifierMetric metric = modifier.getMetric();
		switch (metric)
		{
			case PLAIN:
				return descPlainModifier(value);
			case BOOLEAN:
				return "";
			case PERCENTAGE:
				return descPlainModifier(value)+"%";
			default:
				throw new MazeException(metric.name());
		}
	}

	/*-------------------------------------------------------------------------*/
	public static String descPlainModifier(int value)
	{
		if (value >= 0)
		{
			return "+"+value;
		}
		else
		{
			return ""+value;
		}
	}

	/*-------------------------------------------------------------------------*/
	
	public int getModifier(Modifier modifier)
	{
		Integer result = this.modifiers.getModifier(modifier);
		if (result == null)
		{
			return 0;
		}
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setModifier(Modifier modifier, int value)
	{
		this.modifiers.setModifier(modifier, value);
	}
	
	/*-------------------------------------------------------------------------*/
	public void incModifier(Modifier modifier, int amount)
	{
		Integer current = getModifier(modifier);
		current += amount;
		this.modifiers.setModifier(modifier, current);
	}

	/*-------------------------------------------------------------------------*/
	public CurMaxSub getHitPoints()
	{
		return hitPoints;
	}

	public void setHitPoints(CurMaxSub hitPoints)
	{
		this.hitPoints = hitPoints;
	}

	public CurMax getMagicPoints()
	{
		return magicPoints;
	}

	public void setMagicPoints(CurMax magicPoints)
	{
		this.magicPoints = magicPoints;
	}

	public CurMax getActionPoints()
	{
		return actionPoints;
	}

	public void setActionPoints(CurMax actionPoints)
	{
		this.actionPoints = actionPoints;
	}

	public StatModifier getModifiers()
	{
		return modifiers;
	}

	public void setModifiers(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}

	/*-------------------------------------------------------------------------*/
	public static enum Modifier
	{
		NONE("-"),

		// resources
		HIT_POINTS("hitPoints"),
		ACTION_POINTS("actionPoints"),
		MAGIC_POINTS("magicPoints"),

		// modifiers: attribute
		BRAWN("brawn"),
		SKILL("skill"),
		THIEVING("thieving"),
		SNEAKING("sneaking"),
		BRAINS("brains"),
		POWER("power"),

		// modifiers: martial skills
		SWING("swing"),
		THRUST("thrust"),
		CUT("cut"),
		LUNGE("lunge"),
		BASH("bash"),
		PUNCH("punch"),
		KICK("kick"),
		THROW("throw"),
		SHOOT("shoot"),
		FIRE("fire"),
		DUAL_WEAPONS("dualWeapons"),
		CHIVALRY("chivalry"),
		KENDO("kendo"),

		// modifiers: stealth
		STREETWISE("streetwise"),
		DUNGEONEER("dungeoneer"),
		WILDERNESS_LORE("wildernessLore"),
		SURVIVAL("survival"),
		BACKSTAB("backstab"),
		SNIPE("snipe"),
		LOCK_AND_TRAP("lock&trap"),
		STEAL("steal"),
		MARTIAL_ARTS("martialArts"),
		MELEE_CRITICALS("meleeCriticals"),
		THROWN_CRITICALS("thrownCriticals"),
		RANGED_CRITICALS("rangedCriticals"),
		SCOUTING("scouting"),

		// modifiers: magic
		CHANT("chant"),
		RHYME("rhyme"),
		GESTURE("gesture"),
		POSTURE("posture"),
		THOUGHT("thought"),
		HERBAL("herbal"),
		ALCHEMIC("alchemic"),
		ARTIFACTS("artifacts"),
		MYTHOLOGY("mythology"),
		CRAFT("craft"),
		POWER_CAST("powerCast"),
		ENGINEERING("engineering"),
		MUSIC("music"),

		// stats:
		INITIATIVE("initiative"),
		ATTACK("attack"),
		DEFENCE("defence"),
		DAMAGE("damage"),
		TO_PENETRATE("toPenetrate"),
		VS_PENETRATE("vsPenetrate"),
		VS_AMBUSH("vsAmbush"),
		VS_DODGE("vsDodge"),
		VS_HIDE("vsHide"),
		TO_BRIBE("toBribe"),
		TO_RUN_AWAY("toRunAway"),

		// resistances
		RESIST_BLUDGEONING("resistBludgeoning", ModifierMetric.PERCENTAGE),
		RESIST_PIERCING("resistPiercing", ModifierMetric.PERCENTAGE),
		RESIST_SLASHING("resistSlashing", ModifierMetric.PERCENTAGE),
		RESIST_FIRE("resistFire", ModifierMetric.PERCENTAGE),
		RESIST_WATER("resistWater", ModifierMetric.PERCENTAGE),
		RESIST_EARTH("resistEarth", ModifierMetric.PERCENTAGE),
		RESIST_AIR("resistAir", ModifierMetric.PERCENTAGE),
		RESIST_MENTAL("resistMental", ModifierMetric.PERCENTAGE),
		RESIST_ENERGY("resistEnergy", ModifierMetric.PERCENTAGE),

		// modifiers: spell casting
		SORCERY_SPELLS("sorcerySpells"),
		BLACK_MAGIC_SPELLS("blackMagicSpells"),
		WITCHCRAFT_SPELLS("witchcraftSpells"),
		ENCHANTMENT_SPELLS("enchantmentSpells"),
		WHITE_MAGIC_SPELLS("whiteMagicSpells"),
		DRUIDISM_SPELLS("druidismSpells"),
		ELEMENTAL_SPELLS("elementalSpells"),

		// modifiers: magic generated
		RED_MAGIC_GEN("redMagicGen"),
		BLACK_MAGIC_GEN("blackMagicGen"),
		PURPLE_MAGIC_GEN("purpleMagicGen"),
		GOLD_MAGIC_GEN("goldMagicGen"),
		WHITE_MAGIC_GEN("whiteMagicGen"),
		GREEN_MAGIC_GEN("greenMagicGen"),
		BLUE_MAGIC_GEN("blueMagicGen"),

		// modifiers: regen
		HIT_POINT_REGEN("hitPointRegenRate"),
		ACTION_POINT_REGEN("actionPointRegenRate"),
		MAGIC_POINT_REGEN("magicPointRegenRate"),
		STAMINA_REGEN("staminaRegenRate"),

		// properties
		IMMUNE_TO_DAMAGE("immuneToDamage", ModifierMetric.BOOLEAN),
		IMMUNE_TO_HEAT("immuneToHeat", ModifierMetric.BOOLEAN),
		IMMUNE_TO_COLD("immuneToCold", ModifierMetric.BOOLEAN),
		IMMUNE_TO_POISON("immuneToPoison", ModifierMetric.BOOLEAN),
		IMMUNE_TO_LIGHTNING("immuneToLightning", ModifierMetric.BOOLEAN),
		IMMUNE_TO_PSYCHIC("immuneToPsychic", ModifierMetric.BOOLEAN),
		IMMUNE_TO_ACID("immuneToAcid", ModifierMetric.BOOLEAN),

		IMMUNE_TO_BLIND("immuneToBlind", ModifierMetric.BOOLEAN),
		IMMUNE_TO_DISEASE("immuneToDisease", ModifierMetric.BOOLEAN),
		IMMUNE_TO_FEAR("immuneToFear", ModifierMetric.BOOLEAN),
		IMMUNE_TO_HEX("immuneToHex", ModifierMetric.BOOLEAN),
		IMMUNE_TO_INSANE("immuneToInsane", ModifierMetric.BOOLEAN),
		IMMUNE_TO_INVISIBLE("immuneToInvisible", ModifierMetric.BOOLEAN),
		IMMUNE_TO_IRRITATE("immuneToIrritate", ModifierMetric.BOOLEAN),
		IMMUNE_TO_KO("immuneToKO", ModifierMetric.BOOLEAN),
		IMMUNE_TO_NAUSEA("immuneToNausea", ModifierMetric.BOOLEAN),
		IMMUNE_TO_PARALYSE("immuneToParalyse", ModifierMetric.BOOLEAN),
		IMMUNE_TO_POSSESSION("immuneToPossession", ModifierMetric.BOOLEAN),
		IMMUNE_TO_SILENCE("immuneToSilence", ModifierMetric.BOOLEAN),
		IMMUNE_TO_SLEEP("immuneToSleep", ModifierMetric.BOOLEAN),
		IMMUNE_TO_STONE("immuneToStone", ModifierMetric.BOOLEAN),
		IMMUNE_TO_SWALLOW("immuneToSwallow", ModifierMetric.BOOLEAN),
		IMMUNE_TO_WEB("immuneToWeb", ModifierMetric.BOOLEAN),

		// abilities
		LIGHT_SLEEPER("lightSleeper", ModifierMetric.BOOLEAN),
		BLIND_FIGHTING("blindFighting", ModifierMetric.BOOLEAN),
		EXTRA_GOLD("extraGold", ModifierMetric.PERCENTAGE),
		CHEAT_DEATH("cheatDeath", ModifierMetric.BOOLEAN),
		MAGIC_ABSORPTION("magicAbsorption"),
		ARROW_CUTTING("arrowCutting", ModifierMetric.PERCENTAGE),
		AMBUSHER("ambusher"),
		ENTERTAINER("entertainer", ModifierMetric.BOOLEAN),
		DIPLOMAT("diplomat"),
		BLINK("blink", ModifierMetric.BOOLEAN),
		TIRELESS_SWORD("tirelessSword", ModifierMetric.BOOLEAN),
		TIRELESS_AXE("tirelessAxe", ModifierMetric.BOOLEAN),
		TIRELESS_SPEAR("tirelessSpear", ModifierMetric.BOOLEAN),
		TIRELESS_MACE("tirelessMace", ModifierMetric.BOOLEAN),
		TIRELESS_DAGGER("tirelessDagger", ModifierMetric.BOOLEAN),
		TIRELESS_STAFF("tirelessStaff", ModifierMetric.BOOLEAN),
		TIRELESS_BOW("tirelessBow", ModifierMetric.BOOLEAN),
		TIRELESS_THROWN("tirelessThrown", ModifierMetric.BOOLEAN),
		TIRELESS_UNARMED("tirelessUnarmed", ModifierMetric.BOOLEAN),
		TOUCH_BLIND("blindTouch", ModifierMetric.PERCENTAGE),
		TOUCH_FEAR("fearTouch", ModifierMetric.PERCENTAGE),
		TOUCH_HEX("hexTouch", ModifierMetric.PERCENTAGE),
		TOUCH_INSANE("insaneTouch", ModifierMetric.PERCENTAGE),
		TOUCH_IRRITATE("irritateTouch", ModifierMetric.PERCENTAGE),
		TOUCH_NAUSEA("nauseaTouch", ModifierMetric.PERCENTAGE),
		TOUCH_SILENCE("silenceTouch", ModifierMetric.PERCENTAGE),
		TOUCH_SLEEP("sleepTouch", ModifierMetric.PERCENTAGE),
		TOUCH_STONE("stoneTouch", ModifierMetric.PERCENTAGE),
		TOUCH_PARALYSE("paralyseTouch", ModifierMetric.PERCENTAGE),
		TOUCH_WEB("webTouch", ModifierMetric.PERCENTAGE),
		TOUCH_DISEASE("diseaseTouch", ModifierMetric.PERCENTAGE),
		RAZOR_CLOAK("razorCloak"),
		CC_PENALTY("ccPenalty"),
		DAMAGE_MULTIPLIER("damageMultiplier", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_SWORD("lightningStrikeSword", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_AXE("lightningStrikeAxe", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_SPEAR("lightningStrikeSpear", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_MACE("lightningStrikeMace", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_DAGGER("lightningStrikeDagger", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_STAFF("lightningStrikeStaff", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_UNARMED("lightningStrikeUnarmed", ModifierMetric.BOOLEAN),
		BERSERKER("berserker", ModifierMetric.PERCENTAGE),
		DEADLY_STRIKE("deadlyStrike", ModifierMetric.PERCENTAGE),
		DODGE("dodge", ModifierMetric.PERCENTAGE),
		MASTER_ARCHER("masterArcher", ModifierMetric.BOOLEAN),
		DIVINE_PROTECTION("divineProtection", ModifierMetric.BOOLEAN),
		KI_FURY("kiFury", ModifierMetric.BOOLEAN),
		FEY_AFFINITY("feyAffinity", ModifierMetric.BOOLEAN),
		ARCANE_BLOOD("arcaneBlood", ModifierMetric.BOOLEAN),
		DISPLACER("displacer", ModifierMetric.BOOLEAN),
		PARRY("parry", ModifierMetric.PERCENTAGE),
		MELEE_MASTER("meleeMaster", ModifierMetric.BOOLEAN),
		DEADLY_AIM("deadlyAim"),
		MASTER_THIEF("masterThief", ModifierMetric.BOOLEAN),
		TOUCH_POISON("poisonTouch", ModifierMetric.PERCENTAGE),
		OBFUSCATION("obfuscation"),
		SHADOW_MASTER("shadowMaster", ModifierMetric.BOOLEAN),
		CHARMED_DESTINY("charmedDestiny", ModifierMetric.BOOLEAN),
		CHANNELLING("channelling", ModifierMetric.BOOLEAN),
		SIGNATURE_WEAPON_ENGINEERING("signatureWeaponEngineering"),
		AMPHIBIOUS("amphibious", ModifierMetric.BOOLEAN),
		BONUS_ATTACKS("bonusAttacks"),
		BONUS_STRIKES("bonusStrikes"),
		LARGE_SIZE("largeSize", ModifierMetric.BOOLEAN),
		THREATEN("threaten"),
		DRINKING_FIT("drinkingFit", ModifierMetric.BOOLEAN),
		IAJUTSU("iajutsu"),
		FAVOURED_ENEMY_HORROR("favouredEnemyHorror", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_BEAST("favouredEnemyBeast", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_CONSTRUCT("favouredEnemyConstruct", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_ELEMENTAL("favouredEnemyElemental", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_MAZE_CREATURE("favouredEnemyMazeCreature", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_DRAGON("favouredEnemyDragon", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_FEY("favouredEnemyFey", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_GIANT("favouredEnemyGiant", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_HUMANOID("favouredEnemyHumanoid", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_ILLUSION("favouredEnemyIllusion", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_CRYPTOBESTIA("favouredEnemyCryptobestia", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_MONSTROSITY("favouredEnemyMonstrosity", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_OOZE("favouredEnemyOoze", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_OUTSIDER("favouredEnemyOutsider", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_PLANT("favouredEnemyPlant", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_UNDEAD("favouredEnemyUndead", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_VERMIN("favouredEnemyVermin", ModifierMetric.BOOLEAN),
		POWER_OF_DARKNESS("powerOfDarkness", ModifierMetric.BOOLEAN),
		FLIER("flier", ModifierMetric.BOOLEAN),
		STRONG_SWIMMER("strongSwimmer", ModifierMetric.BOOLEAN),
		FURIOUS_PURPOSE("furiousPurpose", ModifierMetric.BOOLEAN),
		SWORD_PARRY("swordParry", ModifierMetric.PERCENTAGE),
		AXE_PARRY("axeParry", ModifierMetric.PERCENTAGE),
		MACE_PARRY("maceParry", ModifierMetric.PERCENTAGE),
		POLEARM_PARRY("polearmParry", ModifierMetric.PERCENTAGE),
		STAFF_PARRY("staffParry", ModifierMetric.PERCENTAGE),
		AMAZON_COURAGE("amazonCourage", ModifierMetric.BOOLEAN),
		AMAZON_WILLPOWER("amazonWillpower", ModifierMetric.BOOLEAN),
		AMAZON_FURY("amazonFury", ModifierMetric.BOOLEAN),
		SWORD_1H_WIELD("sword1HWield", ModifierMetric.BOOLEAN),
		AXE_1H_WIELD("axe1HWield", ModifierMetric.BOOLEAN),
		MACE_1H_WIELD("mace1HWield", ModifierMetric.BOOLEAN),
		POLEARM_1H_WIELD("polearm1HWield", ModifierMetric.BOOLEAN),
		STAFF_1H_WIELD("staff1HWield", ModifierMetric.BOOLEAN),
		BERSERK_POWERS("berserkPowers"),
		;

		private String resourceBundleKey;
		private ModifierMetric metric;

		Modifier(String resourceBundleKey)
		{
			this(resourceBundleKey, ModifierMetric.PLAIN);
		}

		Modifier(String resourceBundleKey, ModifierMetric metric)
		{
			this.resourceBundleKey = resourceBundleKey;
			this.metric = metric;
		}

		public ModifierMetric getMetric()
		{
			return metric;
		}

		public String getResourceBundleKey()
		{
			return resourceBundleKey;
		}
	}

	/*-------------------------------------------------------------------------*/
/*	public static class ModifiersX
	{
		// resources
		public static final String HIT_POINTS = "hitPoints";
		public static final String ACTION_POINTS = "actionPoints";
		public static final String MAGIC_POINTS = "magicPoints";

		// modifiers: attribute
		public static final String BRAWN = "brawn";
		public static final String SKILL = "skill";
		public static final String THIEVING = "thieving";
		public static final String SNEAKING = "sneaking";
		public static final String BRAINS = "brains";
		public static final String POWER = "power";

		// modifiers: martial skills
		public static final String SWING = "swing";
		public static final String THRUST = "thrust";
		public static final String CUT = "cut";
		public static final String LUNGE = "lunge";
		public static final String BASH = "bash";
		public static final String PUNCH = "punch";
		public static final String KICK = "kick";
		public static final String THROW = "throw";
		public static final String SHOOT = "shoot";
		public static final String FIRE = "fire";
		public static final String DUAL_WEAPONS = "dualWeapons";
		public static final String CHIVALRY = "chivalry";
		public static final String KENDO = "kendo";

		// modifiers: stealth
		public static final String STREETWISE = "streetwise";
		public static final String DUNGEONEER = "dungeoneer";
		public static final String WILDERNESS_LORE = "wildernessLore";
		public static final String SURVIVAL = "survival";
		public static final String BACKSTAB = "backstab";
		public static final String SNIPE = "snipe";
		public static final String LOCK_AND_TRAP = "lock&trap";
		public static final String STEAL = "steal";
		public static final String MARTIAL_ARTS = "martialArts";
		public static final String MELEE_CRITICALS = "meleeCriticals";
		public static final String THROWN_CRITICALS = "thrownCriticals";
		public static final String RANGED_CRITICALS = "rangedCriticals";
		public static final String SCOUTING = "scouting";

		// modifiers: magic
		public static final String CHANT = "chant";
		public static final String RHYME = "rhyme";
		public static final String GESTURE = "gesture";
		public static final String POSTURE = "posture";
		public static final String THOUGHT = "thought";
		public static final String HERBAL = "herbal";
		public static final String ALCHEMIC = "alchemic";
		public static final String ARTIFACTS = "artifacts";
		public static final String MYTHOLOGY = "mythology";
		public static final String CRAFT = "craft";
		public static final String POWER_CAST = "powerCast";
		public static final String ENGINEERING = "engineering";
		public static final String MUSIC = "music";

		// stats:
		public static final String INITIATIVE = "initiative";
		public static final String ATTACK = "attack";
		public static final String DEFENCE = "defence";
		public static final String DAMAGE = "damage";
		public static final String TO_PENETRATE = "toPenetrate";
		public static final String VS_PENETRATE = "vsPenetrate";
		public static final String VS_AMBUSH = "vsAmbush";
		public static final String VS_DODGE = "vsDodge";
		public static final String VS_HIDE = "vsHide";
		public static final String TO_BRIBE = "toBribe";
		public static final String TO_RUN_AWAY = "toRunAway";

		// resistances
		public static final String RESIST_BLUDGEONING = "resistBludgeoning";
		public static final String RESIST_PIERCING = "resistPiercing";
		public static final String RESIST_SLASHING = "resistSlashing";
		public static final String RESIST_FIRE = "resistFire";
		public static final String RESIST_WATER = "resistWater";
		public static final String RESIST_EARTH = "resistEarth";
		public static final String RESIST_AIR = "resistAir";
		public static final String RESIST_MENTAL = "resistMental";
		public static final String RESIST_ENERGY = "resistEnergy";

		// modifiers: spell casting
		public static final String SORCERY_SPELLS = "sorcerySpells";
		public static final String BLACK_MAGIC_SPELLS = "blackMagicSpells";
		public static final String WITCHCRAFT_SPELLS = "witchcraftSpells";
		public static final String ENCHANTMENT_SPELLS = "enchantmentSpells";
		public static final String WHITE_MAGIC_SPELLS = "whiteMagicSpells";
		public static final String DRUIDISM_SPELLS = "druidismSpells";
		public static final String ELEMENTAL_SPELLS = "elementalSpells";

		// modifiers: magic generated
		public static final String RED_MAGIC_GEN = "redMagicGen";
		public static final String BLACK_MAGIC_GEN = "blackMagicGen";
		public static final String PURPLE_MAGIC_GEN = "purpleMagicGen";
		public static final String GOLD_MAGIC_GEN = "goldMagicGen";
		public static final String WHITE_MAGIC_GEN = "whiteMagicGen";
		public static final String GREEN_MAGIC_GEN = "greenMagicGen";
		public static final String BLUE_MAGIC_GEN = "blueMagicGen";

		// modifiers: regen
		public static final String HIT_POINT_REGEN = "hitPointRegenRate";
		public static final String ACTION_POINT_REGEN = "actionPointRegenRate";
		public static final String MAGIC_POINT_REGEN = "magicPointRegenRate";
		public static final String STAMINA_REGEN = "staminaRegenRate";

		// properties
		public static final String IMMUNE_TO_DAMAGE = "immuneToDamage";
		public static final String IMMUNE_TO_HEAT = "immuneToHeat";
		public static final String IMMUNE_TO_COLD = "immuneToCold";
		public static final String IMMUNE_TO_POISON = "immuneToPoison";
		public static final String IMMUNE_TO_LIGHTNING = "immuneToLightning";
		public static final String IMMUNE_TO_PSYCHIC = "immuneToPsychic";
		public static final String IMMUNE_TO_ACID = "immuneToAcid";

		public static final String IMMUNE_TO_BLIND = "immuneToBlind";
		public static final String IMMUNE_TO_DISEASE = "immuneToDisease";
		public static final String IMMUNE_TO_FEAR = "immuneToFear";
		public static final String IMMUNE_TO_HEX = "immuneToHex";
		public static final String IMMUNE_TO_INSANE = "immuneToInsane";
		public static final String IMMUNE_TO_INVISIBLE = "immuneToInvisible";
		public static final String IMMUNE_TO_IRRITATE = "immuneToIrritate";
		public static final String IMMUNE_TO_KO = "immuneToKO";
		public static final String IMMUNE_TO_NAUSEA = "immuneToNausea";
		public static final String IMMUNE_TO_PARALYSE = "immuneToParalyse";
		public static final String IMMUNE_TO_POSSESSION = "immuneToPossession";
		public static final String IMMUNE_TO_SILENCE = "immuneToSilence";
		public static final String IMMUNE_TO_SLEEP = "immuneToSleep";
		public static final String IMMUNE_TO_STONE = "immuneToStone";
		public static final String IMMUNE_TO_SWALLOW = "immuneToSwallow";
		public static final String IMMUNE_TO_WEB = "immuneToWeb";

		// abilities
		public static final String LIGHT_SLEEPER = "lightSleeper";
		public static final String BLIND_FIGHTING = "blindFighting";
		public static final String EXTRA_GOLD = "extraGold";
		public static final String CHEAT_DEATH = "cheatDeath";
		public static final String MAGIC_ABSORPTION = "magicAbsorption";
		public static final String ARROW_CUTTING = "arrowCutting";
		public static final String AMBUSHER = "ambusher";
		public static final String ENTERTAINER = "entertainer";
		public static final String DIPLOMAT = "diplomat";
		public static final String BLINK = "blink";
		public static final String TIRELESS_SWORD = "tirelessSword";
		public static final String TIRELESS_AXE = "tirelessAxe";
		public static final String TIRELESS_SPEAR = "tirelessSpear";
		public static final String TIRELESS_MACE = "tirelessMace";
		public static final String TIRELESS_DAGGER = "tirelessDagger";
		public static final String TIRELESS_STAFF = "tirelessStaff";
		public static final String TIRELESS_BOW = "tirelessBow";
		public static final String TIRELESS_THROWN = "tirelessThrown";
		public static final String TIRELESS_UNARMED = "tirelessUnarmed";
		public static final String TOUCH_BLIND = "blindTouch";
		public static final String TOUCH_FEAR = "fearTouch";
		public static final String TOUCH_HEX = "hexTouch";
		public static final String TOUCH_INSANE = "insaneTouch";
		public static final String TOUCH_IRRITATE = "irritateTouch";
		public static final String TOUCH_NAUSEA = "nauseaTouch";
		public static final String TOUCH_SILENCE = "silenceTouch";
		public static final String TOUCH_SLEEP = "sleepTouch";
		public static final String TOUCH_STONE = "stoneTouch";
		public static final String TOUCH_PARALYSE = "paralyseTouch";
		public static final String TOUCH_WEB = "webTouch";
		public static final String TOUCH_DISEASE = "diseaseTouch";
		public static final String RAZOR_CLOAK = "razorCloak";
		public static final String CC_PENALTY = "ccPenalty";
		public static final String DAMAGE_MULTIPLIER = "damageMultiplier";
		public static final String LIGHTNING_STRIKE_SWORD = "lightningStrikeSword";
		public static final String LIGHTNING_STRIKE_AXE = "lightningStrikeAxe";
		public static final String LIGHTNING_STRIKE_SPEAR = "lightningStrikeSpear";
		public static final String LIGHTNING_STRIKE_MACE = "lightningStrikeMace";
		public static final String LIGHTNING_STRIKE_DAGGER = "lightningStrikeDagger";
		public static final String LIGHTNING_STRIKE_STAFF = "lightningStrikeStaff";
		public static final String LIGHTNING_STRIKE_UNARMED = "lightningStrikeUnarmed";
		public static final String BERSERKER = "berserker";
		public static final String DEADLY_STRIKE = "deadlyStrike";
		public static final String DODGE = "dodge";
		public static final String MASTER_ARCHER = "masterArcher";
		public static final String DIVINE_PROTECTION = "divineProtection";
		public static final String KI_FURY = "kiFury";
		public static final String FEY_AFFINITY = "feyAffinity";
		public static final String ARCANE_BLOOD = "arcaneBlood";
		public static final String DISPLACER = "displacer";
		public static final String PARRY = "parry";
		public static final String MELEE_MASTER = "meleeMaster";
		public static final String DEADLY_AIM = "deadlyAim";
		public static final String MASTER_THIEF = "masterThief";
		public static final String TOUCH_POISON = "poisonTouch";
		public static final String OBFUSCATION = "obfuscation";
		public static final String SHADOW_MASTER = "shadowMaster";
		public static final String CHARMED_DESTINY = "charmedDestiny";
		public static final String CHANNELLING = "channelling";
		public static final String SIGNATURE_WEAPON_ENGINEERING = "signatureWeaponEngineering";
		public static final String AMPHIBIOUS = "amphibious";
		public static final String BONUS_ATTACKS = "bonusAttacks";
		public static final String BONUS_STRIKES = "bonusStrikes";
		public static final String LARGE_SIZE = "largeSize";
		public static final String THREATEN = "threaten";
		public static final String DRINKING_FIT = "drinkingFit";
		public static final String IAJUTSU = "iajutsu";
		public static final String FAVOURED_ENEMY_HORROR = "favouredEnemyHorror";
		public static final String FAVOURED_ENEMY_BEAST = "favouredEnemyBeast";
		public static final String FAVOURED_ENEMY_CONSTRUCT = "favouredEnemyConstruct";
		public static final String FAVOURED_ENEMY_ELEMENTAL = "favouredEnemyElemental";
		public static final String FAVOURED_ENEMY_MAZE_CREATURE = "favouredEnemyMazeCreature";
		public static final String FAVOURED_ENEMY_DRAGON = "favouredEnemyDragon";
		public static final String FAVOURED_ENEMY_FEY = "favouredEnemyFey";
		public static final String FAVOURED_ENEMY_GIANT = "favouredEnemyGiant";
		public static final String FAVOURED_ENEMY_HUMANOID = "favouredEnemyHumanoid";
		public static final String FAVOURED_ENEMY_ILLUSION = "favouredEnemyIllusion";
		public static final String FAVOURED_ENEMY_CRYPTOBESTIA = "favouredEnemyCryptobestia";
		public static final String FAVOURED_ENEMY_MONSTROSITY = "favouredEnemyMonstrosity";
		public static final String FAVOURED_ENEMY_OOZE = "favouredEnemyOoze";
		public static final String FAVOURED_ENEMY_OUTSIDER = "favouredEnemyOutsider";
		public static final String FAVOURED_ENEMY_PLANT = "favouredEnemyPlant";
		public static final String FAVOURED_ENEMY_UNDEAD = "favouredEnemyUndead";
		public static final String FAVOURED_ENEMY_VERMIN = "favouredEnemyVermin";
		public static final String POWER_OF_DARKNESS = "powerOfDarkness";
		public static final String FLIER = "flier";
		public static final String STRONG_SWIMMER = "strongSwimmer";
		public static final String FURIOUS_PURPOSE = "furiousPurpose";
		public static final String SWORD_PARRY = "swordParry";
		public static final String AXE_PARRY = "axeParry";
		public static final String MACE_PARRY = "maceParry";
		public static final String POLEARM_PARRY = "polearmParry";
		public static final String STAFF_PARRY = "staffParry";
		public static final String AMAZON_COURAGE = "amazonCourage";
		public static final String AMAZON_WILLPOWER = "amazonWillpower";
		public static final String AMAZON_FURY = "amazonFury";
		public static final String SWORD_1H_WIELD = "sword1HWield";
		public static final String AXE_1H_WIELD = "axe1HWield";
		public static final String MACE_1H_WIELD = "mace1HWield";
		public static final String POLEARM_1H_WIELD = "polearm1HWield";
		public static final String STAFF_1H_WIELD = "staff1HWield";
		public static final String BERSERK_POWERS = "berserkPowers";
	}*/

	/*-------------------------------------------------------------------------*/
	public static enum ModifierMetric
	{
		PLAIN, BOOLEAN, PERCENTAGE;

/*		private static Set<String> percentageModifiers = new HashSet<String>();
		private static Set<String> booleanModifiers = new HashSet<String>();

		static
		{
			percentageModifiers.add(Modifiers.RESIST_AIR);
			percentageModifiers.add(Modifiers.RESIST_BLUDGEONING);
			percentageModifiers.add(Modifiers.RESIST_SLASHING);
			percentageModifiers.add(Modifiers.RESIST_PIERCING);
			percentageModifiers.add(Modifiers.RESIST_EARTH);
			percentageModifiers.add(Modifiers.RESIST_ENERGY);
			percentageModifiers.add(Modifiers.RESIST_FIRE);
			percentageModifiers.add(Modifiers.RESIST_MENTAL);
			percentageModifiers.add(Modifiers.RESIST_WATER);
			percentageModifiers.add(Modifiers.EXTRA_GOLD);
			percentageModifiers.add(Modifiers.ARROW_CUTTING);
			percentageModifiers.add(Modifiers.TOUCH_HEX);
			percentageModifiers.add(Modifiers.TOUCH_BLIND);
			percentageModifiers.add(Modifiers.TOUCH_DISEASE);
			percentageModifiers.add(Modifiers.TOUCH_FEAR);
			percentageModifiers.add(Modifiers.TOUCH_INSANE);
			percentageModifiers.add(Modifiers.TOUCH_IRRITATE);
			percentageModifiers.add(Modifiers.TOUCH_NAUSEA);
			percentageModifiers.add(Modifiers.TOUCH_PARALYSE);
			percentageModifiers.add(Modifiers.TOUCH_POISON);
			percentageModifiers.add(Modifiers.TOUCH_SILENCE);
			percentageModifiers.add(Modifiers.TOUCH_SLEEP);
			percentageModifiers.add(Modifiers.TOUCH_STONE);
			percentageModifiers.add(Modifiers.TOUCH_WEB);
			percentageModifiers.add(Modifiers.TOUCH_POISON);
			percentageModifiers.add(Modifiers.DEADLY_STRIKE);
			percentageModifiers.add(Modifiers.DODGE);
			percentageModifiers.add(Modifiers.PARRY);
			percentageModifiers.add(Modifiers.SWORD_PARRY);
			percentageModifiers.add(Modifiers.AXE_PARRY);
			percentageModifiers.add(Modifiers.MACE_PARRY);
			percentageModifiers.add(Modifiers.POLEARM_PARRY);
			percentageModifiers.add(Modifiers.STAFF_PARRY);
			percentageModifiers.add(Modifiers.BERSERKER);

			booleanModifiers.add(Modifiers.IMMUNE_TO_ACID);
			booleanModifiers.add(Modifiers.IMMUNE_TO_BLIND);
			booleanModifiers.add(Modifiers.IMMUNE_TO_COLD);
			booleanModifiers.add(Modifiers.IMMUNE_TO_DAMAGE);
			booleanModifiers.add(Modifiers.IMMUNE_TO_DISEASE);
			booleanModifiers.add(Modifiers.IMMUNE_TO_FEAR);
			booleanModifiers.add(Modifiers.IMMUNE_TO_HEAT);
			booleanModifiers.add(Modifiers.IMMUNE_TO_HEX);
			booleanModifiers.add(Modifiers.IMMUNE_TO_INSANE);
			booleanModifiers.add(Modifiers.IMMUNE_TO_INVISIBLE);
			booleanModifiers.add(Modifiers.IMMUNE_TO_INVISIBLE);
			booleanModifiers.add(Modifiers.IMMUNE_TO_IRRITATE);
			booleanModifiers.add(Modifiers.IMMUNE_TO_KO);
			booleanModifiers.add(Modifiers.IMMUNE_TO_LIGHTNING);
			booleanModifiers.add(Modifiers.IMMUNE_TO_NAUSEA);
			booleanModifiers.add(Modifiers.IMMUNE_TO_PARALYSE);
			booleanModifiers.add(Modifiers.IMMUNE_TO_POISON);
			booleanModifiers.add(Modifiers.IMMUNE_TO_POSSESSION);
			booleanModifiers.add(Modifiers.IMMUNE_TO_PSYCHIC);
			booleanModifiers.add(Modifiers.IMMUNE_TO_SILENCE);
			booleanModifiers.add(Modifiers.IMMUNE_TO_SLEEP);
			booleanModifiers.add(Modifiers.IMMUNE_TO_STONE);
			booleanModifiers.add(Modifiers.IMMUNE_TO_SWALLOW);
			booleanModifiers.add(Modifiers.IMMUNE_TO_WEB);
			booleanModifiers.add(Modifiers.LIGHT_SLEEPER);
			booleanModifiers.add(Modifiers.BLIND_FIGHTING);
			booleanModifiers.add(Modifiers.CHEAT_DEATH);
			booleanModifiers.add(Modifiers.ENTERTAINER);
			booleanModifiers.add(Modifiers.BLINK);
			booleanModifiers.add(Modifiers.TIRELESS_AXE);
			booleanModifiers.add(Modifiers.TIRELESS_BOW);
			booleanModifiers.add(Modifiers.TIRELESS_DAGGER);
			booleanModifiers.add(Modifiers.TIRELESS_MACE);
			booleanModifiers.add(Modifiers.TIRELESS_SPEAR);
			booleanModifiers.add(Modifiers.TIRELESS_STAFF);
			booleanModifiers.add(Modifiers.TIRELESS_SWORD);
			booleanModifiers.add(Modifiers.TIRELESS_THROWN);
			booleanModifiers.add(Modifiers.TIRELESS_UNARMED);
			booleanModifiers.add(Modifiers.LIGHTNING_STRIKE_AXE);
			booleanModifiers.add(Modifiers.LIGHTNING_STRIKE_DAGGER);
			booleanModifiers.add(Modifiers.LIGHTNING_STRIKE_MACE);
			booleanModifiers.add(Modifiers.LIGHTNING_STRIKE_SPEAR);
			booleanModifiers.add(Modifiers.LIGHTNING_STRIKE_STAFF);
			booleanModifiers.add(Modifiers.LIGHTNING_STRIKE_SWORD);
			booleanModifiers.add(Modifiers.LIGHTNING_STRIKE_UNARMED);
			booleanModifiers.add(Modifiers.MASTER_ARCHER);
			booleanModifiers.add(Modifiers.DIVINE_PROTECTION);
			booleanModifiers.add(Modifiers.KI_FURY);
			booleanModifiers.add(Modifiers.FEY_AFFINITY);
			booleanModifiers.add(Modifiers.ARCANE_BLOOD);
			booleanModifiers.add(Modifiers.DISPLACER);
			booleanModifiers.add(Modifiers.MASTER_THIEF);
			booleanModifiers.add(Modifiers.MELEE_MASTER);
			booleanModifiers.add(Modifiers.SHADOW_MASTER);
			booleanModifiers.add(Modifiers.CHANNELLING);
			booleanModifiers.add(Modifiers.CHARMED_DESTINY);
			booleanModifiers.add(Modifiers.SIGNATURE_WEAPON_ENGINEERING);
			booleanModifiers.add(Modifiers.AMPHIBIOUS);
			booleanModifiers.add(Modifiers.LARGE_SIZE);
			booleanModifiers.add(Modifiers.DRINKING_FIT);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_BEAST);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_CONSTRUCT);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_MAZE_CREATURE);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_CRYPTOBESTIA);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_CONSTRUCT);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_DRAGON);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_ELEMENTAL);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_FEY);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_GIANT);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_HORROR);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_HUMANOID);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_ILLUSION);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_MONSTROSITY);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_OOZE);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_OUTSIDER);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_PLANT);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_UNDEAD);
			booleanModifiers.add(Modifiers.FAVOURED_ENEMY_VERMIN);
			booleanModifiers.add(Modifiers.POWER_OF_DARKNESS);
			booleanModifiers.add(Modifiers.FLIER);
			booleanModifiers.add(Modifiers.STRONG_SWIMMER);
			booleanModifiers.add(Modifiers.AMAZON_COURAGE);
			booleanModifiers.add(Modifiers.AMAZON_WILLPOWER);
			booleanModifiers.add(Modifiers.AMAZON_FURY);
			booleanModifiers.add(Modifiers.SWORD_1H_WIELD);
			booleanModifiers.add(Modifiers.AXE_1H_WIELD);
			booleanModifiers.add(Modifiers.MACE_1H_WIELD);
			booleanModifiers.add(Modifiers.POLEARM_1H_WIELD);
			booleanModifiers.add(Modifiers.STAFF_1H_WIELD);
		}

		public static ModifierMetric getMetric(String modifier)
		{
			if (percentageModifiers.contains(modifier))
			{
				return PERCENTAGE;
			}
			else if (booleanModifiers.contains(modifier))
			{
				return BOOLEAN;
			}
			else
			{
				return PLAIN;
			}
		}*/
	}
	
	/*-------------------------------------------------------------------------*/
	public static void main(String[] args)
	{
		System.out.println(allModifiers.size());
	}
}
