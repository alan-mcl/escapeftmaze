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
	public static List<String> resourceModifiers = new ArrayList<String>();
	
	/** Str, Agl, and so on */
	public static List<String> attributeModifiers = new ArrayList<String>();

	/** swing, thrust, and so on */
	public static List<String> martialModifiers = new ArrayList<String>();

	/** steal, stealth, and so on */
	public static List<String> stealthModifiers = new ArrayList<String>();

	/** chant, gesture, and so on */
	public static List<String> magicModifiers = new ArrayList<String>();

	/** The set of modifiers that can be edited by the player */
	public static List<String> regularModifiers = new ArrayList<String>();

	/** The various stats that can't be edited by the player */
	public static List<String> statistics = new ArrayList<String>();

	/** Resistance modifiers */
	public static List<String> resistances = new ArrayList<String>();

	/** properties of a character - boolean flags that can't be edited */
	public static List<String> propertiesModifiers = new ArrayList<String>();

	/** all modifiers */
	public static List<String> allModifiers = new ArrayList<String>();

	/** all modifiers except attributes and resistances */
	public static List<String> middleModifiers = new ArrayList<String>();

	/*-------------------------------------------------------------------------*/
	static
	{
		resourceModifiers.add(Modifiers.HIT_POINTS);
		resourceModifiers.add(Modifiers.ACTION_POINTS);
		resourceModifiers.add(Modifiers.MAGIC_POINTS);
		
		attributeModifiers.add(Modifiers.BRAWN);
		attributeModifiers.add(Modifiers.SKILL);
		attributeModifiers.add(Modifiers.THIEVING);
		attributeModifiers.add(Modifiers.SNEAKING);
		attributeModifiers.add(Modifiers.BRAINS);
		attributeModifiers.add(Modifiers.POWER);

		martialModifiers.add(Modifiers.SWING);
		martialModifiers.add(Modifiers.THRUST);
		martialModifiers.add(Modifiers.CUT);
		martialModifiers.add(Modifiers.LUNGE);
		martialModifiers.add(Modifiers.BASH);
		martialModifiers.add(Modifiers.PUNCH);
		martialModifiers.add(Modifiers.KICK);
		martialModifiers.add(Modifiers.SHOOT);
		martialModifiers.add(Modifiers.THROW);
		martialModifiers.add(Modifiers.FIRE);
		martialModifiers.add(Modifiers.DUAL_WEAPONS);
		martialModifiers.add(Modifiers.CHIVALRY);
		martialModifiers.add(Modifiers.KENDO);

		stealthModifiers.add(Modifiers.STEAL);
		stealthModifiers.add(Modifiers.LOCK_AND_TRAP);
		stealthModifiers.add(Modifiers.DUNGEONEER);
		stealthModifiers.add(Modifiers.STREETWISE);
		stealthModifiers.add(Modifiers.WILDERNESS_LORE);
		stealthModifiers.add(Modifiers.SURVIVAL);
		stealthModifiers.add(Modifiers.BACKSTAB);
		stealthModifiers.add(Modifiers.SNIPE);
		stealthModifiers.add(Modifiers.MARTIAL_ARTS);
		stealthModifiers.add(Modifiers.MELEE_CRITICALS);
		stealthModifiers.add(Modifiers.THROWN_CRITICALS);
		stealthModifiers.add(Modifiers.RANGED_CRITICALS);
		stealthModifiers.add(Modifiers.SCOUTING);

		magicModifiers.add(Modifiers.CHANT);
		magicModifiers.add(Modifiers.RHYME);
		magicModifiers.add(Modifiers.GESTURE);
		magicModifiers.add(Modifiers.POSTURE);
		magicModifiers.add(Modifiers.THOUGHT);
		magicModifiers.add(Modifiers.ALCHEMIC);
		magicModifiers.add(Modifiers.HERBAL);
		magicModifiers.add(Modifiers.ARTIFACTS);
		magicModifiers.add(Modifiers.MYTHOLOGY);
		magicModifiers.add(Modifiers.CRAFT);
		magicModifiers.add(Modifiers.POWER_CAST);
		magicModifiers.add(Modifiers.ENGINEERING);
		magicModifiers.add(Modifiers.MUSIC);

		regularModifiers.addAll(attributeModifiers);
		regularModifiers.addAll(martialModifiers);
		regularModifiers.addAll(stealthModifiers);
		regularModifiers.addAll(magicModifiers);

		statistics.add(Modifiers.INITIATIVE);
		statistics.add(Modifiers.ATTACK);
		statistics.add(Modifiers.DEFENCE);
		statistics.add(Modifiers.DAMAGE);
		statistics.add(Modifiers.TO_PENETRATE);
		statistics.add(Modifiers.VS_PENETRATE);
		statistics.add(Modifiers.VS_AMBUSH);
		statistics.add(Modifiers.VS_DODGE);
		statistics.add(Modifiers.VS_HIDE);
		statistics.add(Modifiers.TO_THREATEN);
		statistics.add(Modifiers.TO_BRIBE);
		statistics.add(Modifiers.TO_RUN_AWAY);

		statistics.add(Modifiers.RED_MAGIC_GEN);
		statistics.add(Modifiers.BLACK_MAGIC_GEN);
		statistics.add(Modifiers.PURPLE_MAGIC_GEN);
		statistics.add(Modifiers.GOLD_MAGIC_GEN);
		statistics.add(Modifiers.WHITE_MAGIC_GEN);
		statistics.add(Modifiers.GREEN_MAGIC_GEN);
		statistics.add(Modifiers.BLUE_MAGIC_GEN);

		statistics.add(Modifiers.SORCERY_SPELLS);
		statistics.add(Modifiers.BLACK_MAGIC_SPELLS);
		statistics.add(Modifiers.WITCHCRAFT_SPELLS);
		statistics.add(Modifiers.ENCHANTMENT_SPELLS);
		statistics.add(Modifiers.WHITE_MAGIC_SPELLS);
		statistics.add(Modifiers.DRUIDISM_SPELLS);
		statistics.add(Modifiers.ELEMENTAL_SPELLS);

		statistics.add(Modifiers.HIT_POINT_REGEN);
		statistics.add(Modifiers.ACTION_POINT_REGEN);
		statistics.add(Modifiers.MAGIC_POINT_REGEN);
		statistics.add(Modifiers.STAMINA_REGEN);

		resistances.add(Modifiers.RESIST_BLUDGEONING);
		resistances.add(Modifiers.RESIST_PIERCING);
		resistances.add(Modifiers.RESIST_SLASHING);
		resistances.add(Modifiers.RESIST_FIRE);
		resistances.add(Modifiers.RESIST_WATER);
		resistances.add(Modifiers.RESIST_EARTH);
		resistances.add(Modifiers.RESIST_AIR);
		resistances.add(Modifiers.RESIST_MENTAL);
		resistances.add(Modifiers.RESIST_ENERGY);

		statistics.addAll(resistances);

		propertiesModifiers.add(Modifiers.IMMUNE_TO_DAMAGE);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_HEAT);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_COLD);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_LIGHTNING);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_POISON);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_PSYCHIC);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_ACID);

		propertiesModifiers.add(Modifiers.IMMUNE_TO_BLIND);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_DISEASE);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_FEAR);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_HEX);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_INSANE);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_IRRITATE);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_KO);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_NAUSEA);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_PARALYSE);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_POSSESSION);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_SILENCE);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_SLEEP);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_STONE);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_SWALLOW);
		propertiesModifiers.add(Modifiers.IMMUNE_TO_WEB);

		propertiesModifiers.add(Modifiers.LIGHT_SLEEPER);
		propertiesModifiers.add(Modifiers.BLIND_FIGHTING);
		propertiesModifiers.add(Modifiers.EXTRA_GOLD);
		propertiesModifiers.add(Modifiers.CHEAT_DEATH);
		propertiesModifiers.add(Modifiers.MAGIC_ABSORPTION);
		propertiesModifiers.add(Modifiers.ARROW_CUTTING);
		propertiesModifiers.add(Modifiers.AMBUSHER);
		propertiesModifiers.add(Modifiers.ENTERTAINER);
		propertiesModifiers.add(Modifiers.DIPLOMAT);
		propertiesModifiers.add(Modifiers.BLINK);
		propertiesModifiers.add(Modifiers.TIRELESS_AXE);
		propertiesModifiers.add(Modifiers.TIRELESS_BOW);
		propertiesModifiers.add(Modifiers.TIRELESS_DAGGER);
		propertiesModifiers.add(Modifiers.TIRELESS_MACE);
		propertiesModifiers.add(Modifiers.TIRELESS_SPEAR);
		propertiesModifiers.add(Modifiers.TIRELESS_STAFF);
		propertiesModifiers.add(Modifiers.TIRELESS_SWORD);
		propertiesModifiers.add(Modifiers.TIRELESS_THROWN);
		propertiesModifiers.add(Modifiers.TIRELESS_UNARMED);
		propertiesModifiers.add(Modifiers.TOUCH_BLIND);
		propertiesModifiers.add(Modifiers.TOUCH_DISEASE);
		propertiesModifiers.add(Modifiers.TOUCH_FEAR);
		propertiesModifiers.add(Modifiers.TOUCH_HEX);
		propertiesModifiers.add(Modifiers.TOUCH_INSANE);
		propertiesModifiers.add(Modifiers.TOUCH_IRRITATE);
		propertiesModifiers.add(Modifiers.TOUCH_NAUSEA);
		propertiesModifiers.add(Modifiers.TOUCH_PARALYSE);
		propertiesModifiers.add(Modifiers.TOUCH_SILENCE);
		propertiesModifiers.add(Modifiers.TOUCH_SLEEP);
		propertiesModifiers.add(Modifiers.TOUCH_STONE);
		propertiesModifiers.add(Modifiers.TOUCH_WEB);
		propertiesModifiers.add(Modifiers.TOUCH_POISON);
		propertiesModifiers.add(Modifiers.RAZOR_CLOAK);
		propertiesModifiers.add(Modifiers.CC_PENALTY);
		propertiesModifiers.add(Modifiers.DAMAGE_MULTIPLIER);
		propertiesModifiers.add(Modifiers.LIGHTNING_STRIKE_SWORD);
		propertiesModifiers.add(Modifiers.LIGHTNING_STRIKE_AXE);
		propertiesModifiers.add(Modifiers.LIGHTNING_STRIKE_SPEAR);
		propertiesModifiers.add(Modifiers.LIGHTNING_STRIKE_MACE);
		propertiesModifiers.add(Modifiers.LIGHTNING_STRIKE_DAGGER);
		propertiesModifiers.add(Modifiers.LIGHTNING_STRIKE_STAFF);
		propertiesModifiers.add(Modifiers.LIGHTNING_STRIKE_UNARMED);
		propertiesModifiers.add(Modifiers.BERSERKER);
		propertiesModifiers.add(Modifiers.DEADLY_STRIKE);
		propertiesModifiers.add(Modifiers.DODGE);
		propertiesModifiers.add(Modifiers.MASTER_ARCHER);
		propertiesModifiers.add(Modifiers.DIVINE_PROTECTION);
		propertiesModifiers.add(Modifiers.KI_FURY);
		propertiesModifiers.add(Modifiers.FEY_AFFINITY);
		propertiesModifiers.add(Modifiers.ARCANE_BLOOD);
		propertiesModifiers.add(Modifiers.DISPLACER);
		propertiesModifiers.add(Modifiers.PARRY);
		propertiesModifiers.add(Modifiers.MELEE_MASTER);
		propertiesModifiers.add(Modifiers.DEADLY_AIM);
		propertiesModifiers.add(Modifiers.MASTER_THIEF);
		propertiesModifiers.add(Modifiers.OBFUSCATION);
		propertiesModifiers.add(Modifiers.SHADOW_MASTER);
		propertiesModifiers.add(Modifiers.CHARMED_DESTINY);
		propertiesModifiers.add(Modifiers.CHANNELLING);
		propertiesModifiers.add(Modifiers.SIGNATURE_WEAPON_ENGINEERING);
		propertiesModifiers.add(Modifiers.AMPHIBIOUS);
		propertiesModifiers.add(Modifiers.BONUS_ATTACKS);
		propertiesModifiers.add(Modifiers.BONUS_STRIKES);
		propertiesModifiers.add(Modifiers.LARGE_SIZE);
		propertiesModifiers.add(Modifiers.THREATEN);
		propertiesModifiers.add(Modifiers.DRINKING_FIT);
		propertiesModifiers.add(Modifiers.IAJUTSU);

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
	public static String descModifier(String modifier, int value)
	{
		ModifierMetric metric = ModifierMetric.getMetric(modifier);
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
	
	public int getModifier(String modifier)
	{
		Integer result = this.modifiers.getModifier(modifier);
		if (result == null)
		{
			return 0;
		}
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setModifier(String modifier, int value)
	{
		this.modifiers.setModifier(modifier, value);
	}
	
	/*-------------------------------------------------------------------------*/
	public void incModifier(String modifier, int amount)
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
	public static class Modifiers
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
		public static final String TO_THREATEN = "toThreaten";
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
	}

	/*-------------------------------------------------------------------------*/
	public static enum ModifierMetric
	{
		PLAIN, BOOLEAN, PERCENTAGE;

		private static Set<String> percentageModifiers = new HashSet<String>();
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
			percentageModifiers.add(Modifiers.DEADLY_AIM);

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
			booleanModifiers.add(Modifiers.BERSERKER);
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
			booleanModifiers.add(Modifiers.THREATEN);
			booleanModifiers.add(Modifiers.DRINKING_FIT);
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
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public static void main(String[] args)
	{
		System.out.println(allModifiers.size());
	}
}
