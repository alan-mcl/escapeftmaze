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

import static mclachlan.maze.stat.Stats.ModifierType.*;

/**
 *
 */
public class Stats
{
	private StatModifier modifiers = new StatModifier();

	// attributes
	private CurMaxSub hitPoints = new CurMaxSub();
	private CurMax actionPoints = new CurMax();
	private CurMax magicPoints = new CurMax();

	/** Resources */
	public static List<Modifier> resourceModifiers = new ArrayList<Modifier>();
	
	/** Attributes */
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

	public static List<Modifier> resistancesAndImmunities = new ArrayList<Modifier>();
	public static List<Modifier> touches = new ArrayList<Modifier>();
	public static List<Modifier> weaponAbilities = new ArrayList<Modifier>();
	public static List<Modifier> favouredEnemies = new ArrayList<Modifier>();

	/** Spell casting level modifiers */
	public static List<Modifier> spellCastingLevels = new ArrayList<Modifier>();

	/** properties of a character - boolean flags that can't be edited */
	public static List<Modifier> propertiesModifiers = new ArrayList<Modifier>();

	/** all modifiers */
	public static List<Modifier> allModifiers = new ArrayList<Modifier>();

	/** all modifiers except attributes and resistances */
	public static List<Modifier> middleModifiers = new ArrayList<Modifier>();

	/*-------------------------------------------------------------------------*/
	static
	{
		// collect into the utility collections
		for (Modifier m : Modifier.values())
		{
			switch (m.getType())
			{
				case NONE:
					break;
				case RESOURCE:
					resourceModifiers.add(m);
					break;
				case ATTRIBUTE:
					attributeModifiers.add(m);
					break;
				case COMBAT:
					martialModifiers.add(m);
					break;
				case STEALTH:
					stealthModifiers.add(m);
					break;
				case MAGIC:
					magicModifiers.add(m);
					break;
				case STATISTICS:
					statistics.add(m);
					break;
				case PROPERTIES:
					propertiesModifiers.add(m);
					break;
				default:
					throw new MazeException(m.getType().toString());
			}
		}

		allModifiers.addAll(resourceModifiers);
		allModifiers.addAll(attributeModifiers);
		allModifiers.addAll(martialModifiers);
		allModifiers.addAll(stealthModifiers);
		allModifiers.addAll(magicModifiers);
		allModifiers.addAll(statistics);
		allModifiers.addAll(propertiesModifiers);

		regularModifiers.addAll(attributeModifiers);
		regularModifiers.addAll(martialModifiers);
		regularModifiers.addAll(stealthModifiers);
		regularModifiers.addAll(magicModifiers);

		resistances.add(Modifier.RESIST_BLUDGEONING);
		resistances.add(Modifier.RESIST_PIERCING);
		resistances.add(Modifier.RESIST_SLASHING);
		resistances.add(Modifier.RESIST_FIRE);
		resistances.add(Modifier.RESIST_WATER);
		resistances.add(Modifier.RESIST_EARTH);
		resistances.add(Modifier.RESIST_AIR);
		resistances.add(Modifier.RESIST_MENTAL);
		resistances.add(Modifier.RESIST_ENERGY);

		resistancesAndImmunities.addAll(resistances);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_DAMAGE);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_HEAT);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_COLD);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_POISON);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_LIGHTNING);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_PSYCHIC);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_ACID);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_BLIND);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_DISEASE);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_FEAR);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_HEX);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_INSANE);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_INVISIBLE);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_IRRITATE);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_KO);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_NAUSEA);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_PARALYSE);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_POSSESSION);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_SILENCE);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_SLEEP);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_STONE);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_SWALLOW);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_WEB);
		resistancesAndImmunities.add(Modifier.IMMUNE_TO_CRITICALS);

		touches.add(Modifier.TOUCH_BLIND);
		touches.add(Modifier.TOUCH_DISEASE);
		touches.add(Modifier.TOUCH_FEAR);
		touches.add(Modifier.TOUCH_HEX);
		touches.add(Modifier.TOUCH_INSANE);
		touches.add(Modifier.TOUCH_IRRITATE);
		touches.add(Modifier.TOUCH_NAUSEA);
		touches.add(Modifier.TOUCH_PARALYSE);
		touches.add(Modifier.TOUCH_SILENCE);
		touches.add(Modifier.TOUCH_SLEEP);
		touches.add(Modifier.TOUCH_STONE);
		touches.add(Modifier.TOUCH_WEB);
		touches.add(Modifier.TOUCH_POISON);

		favouredEnemies.add(Modifier.FAVOURED_ENEMY_BEAST);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_CONSTRUCT);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_MAZE_CREATURE);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_CRYPTOBESTIA);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_DRAGON);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_ELEMENTAL);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_FEY);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_GIANT);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_HORROR);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_HUMANOID);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_ILLUSION);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_MONSTROSITY);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_OOZE);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_OUTSIDER);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_PLANT);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_UNDEAD);
		favouredEnemies.add(Modifier.FAVOURED_ENEMY_VERMIN);

		weaponAbilities.add(Modifier.TIRELESS_AXE);
		weaponAbilities.add(Modifier.TIRELESS_BOW);
		weaponAbilities.add(Modifier.TIRELESS_DAGGER);
		weaponAbilities.add(Modifier.TIRELESS_MACE);
		weaponAbilities.add(Modifier.TIRELESS_SPEAR);
		weaponAbilities.add(Modifier.TIRELESS_STAFF);
		weaponAbilities.add(Modifier.TIRELESS_SWORD);
		weaponAbilities.add(Modifier.TIRELESS_THROWN);
		weaponAbilities.add(Modifier.TIRELESS_UNARMED);
		weaponAbilities.add(Modifier.LIGHTNING_STRIKE_AXE);
		weaponAbilities.add(Modifier.LIGHTNING_STRIKE_DAGGER);
		weaponAbilities.add(Modifier.LIGHTNING_STRIKE_MACE);
		weaponAbilities.add(Modifier.LIGHTNING_STRIKE_SPEAR);
		weaponAbilities.add(Modifier.LIGHTNING_STRIKE_STAFF);
		weaponAbilities.add(Modifier.LIGHTNING_STRIKE_SWORD);
		weaponAbilities.add(Modifier.LIGHTNING_STRIKE_UNARMED);
		weaponAbilities.add(Modifier.SWORD_PARRY);
		weaponAbilities.add(Modifier.AXE_PARRY);
		weaponAbilities.add(Modifier.MACE_PARRY);
		weaponAbilities.add(Modifier.POLEARM_PARRY);
		weaponAbilities.add(Modifier.STAFF_PARRY);
		weaponAbilities.add(Modifier.SWORD_1H_WIELD);
		weaponAbilities.add(Modifier.AXE_1H_WIELD);
		weaponAbilities.add(Modifier.MACE_1H_WIELD);
		weaponAbilities.add(Modifier.POLEARM_1H_WIELD);
		weaponAbilities.add(Modifier.STAFF_1H_WIELD);

		spellCastingLevels.add(Modifier.BLACK_MAGIC_SPELLS);
		spellCastingLevels.add(Modifier.BLUE_MAGIC_SPELLS);
		spellCastingLevels.add(Modifier.GREEN_MAGIC_SPELLS);
		spellCastingLevels.add(Modifier.WHITE_MAGIC_SPELLS);
		spellCastingLevels.add(Modifier.GOLD_MAGIC_SPELLS);
		spellCastingLevels.add(Modifier.PURPLE_MAGIC_SPELLS);
		spellCastingLevels.add(Modifier.RED_MAGIC_SPELLS);

		middleModifiers.addAll(allModifiers);
		middleModifiers.removeAll(attributeModifiers);
		middleModifiers.removeAll(resistances);

		// validate modifier IDs
		Set<Integer> ids = new HashSet<Integer>();
		for (Modifier m : Modifier.values())
		{
			if (ids.contains(m.getId()))
			{
				throw new MazeException("Duplicate modifier ID: "+m.getId());
			}
			ids.add(m.getId());
		}
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
		NONE(-1, "-", ModifierType.NONE),

		// resources
		HIT_POINTS(0, "hitPoints", RESOURCE),
		ACTION_POINTS(1, "actionPoints", RESOURCE),
		MAGIC_POINTS(2, "magicPoints", RESOURCE),

		// modifiers: attribute
		BRAWN(3, "brawn", ATTRIBUTE),
		SKILL(4, "skill", ATTRIBUTE),
		THIEVING(5, "thieving", ATTRIBUTE),
		SNEAKING(6, "sneaking", ATTRIBUTE),
		BRAINS(7, "brains", ATTRIBUTE),
		POWER(8, "power", ATTRIBUTE),

		// modifiers: martial skills
		SWING(9, "swing", COMBAT),
		THRUST(10,"thrust", COMBAT),
		CUT(11, "cut", COMBAT),
		LUNGE(12, "lunge", COMBAT),
		BASH(13, "bash", COMBAT),
		PUNCH(14, "punch", COMBAT),
		KICK(15, "kick", COMBAT),
		THROW(16, "throw", COMBAT),
		SHOOT(17, "shoot", COMBAT),
		FIRE(18, "fire", COMBAT),
		// ID 19 unused
		DUAL_WEAPONS(20, "dualWeapons", COMBAT),
		CHIVALRY(21, "chivalry", COMBAT),
		KENDO(22, "kendo", COMBAT),

		// modifiers: stealth
		STREETWISE(23, "streetwise", STEALTH),
		DUNGEONEER(24, "dungeoneer", STEALTH),
		WILDERNESS_LORE(25, "wildernessLore", STEALTH),
		SURVIVAL(26, "survival", STEALTH),
		BACKSTAB(27, "backstab", STEALTH),
		SNIPE(28, "snipe", STEALTH),
		LOCK_AND_TRAP(29, "lock&trap", STEALTH),
		STEAL(30, "steal", STEALTH),
		MARTIAL_ARTS(31, "martialArts", STEALTH),
		MELEE_CRITICALS(32, "meleeCriticals", STEALTH),
		THROWN_CRITICALS(33, "thrownCriticals", STEALTH),
		RANGED_CRITICALS(34, "rangedCriticals", STEALTH),
		SCOUTING(35, "scouting", STEALTH),
		// unused ID 36

		// modifiers: magic
		CHANT(37, "chant", MAGIC),
		RHYME(38, "rhyme", MAGIC),
		GESTURE(39, "gesture", MAGIC),
		POSTURE(40, "posture", MAGIC),
		THOUGHT(41, "thought", MAGIC),
		HERBAL(42, "herbal", MAGIC),
		ALCHEMIC(43, "alchemic", MAGIC),
		ARTIFACTS(44, "artifacts", MAGIC),
		MYTHOLOGY(45, "mythology", MAGIC),
		CRAFT(46, "craft", MAGIC),
		POWER_CAST(47, "powerCast", MAGIC),
		ENGINEERING(81, "engineering", MAGIC),
		MUSIC(82, "music", MAGIC),

		// stats:
		INITIATIVE(51, "initiative", STATISTICS),
		ATTACK(52, "attack", STATISTICS),
		// unused ID 53
		DEFENCE(54, "defence", STATISTICS),
		DAMAGE(55, "damage", STATISTICS),
		TO_PENETRATE(56, "toPenetrate", STATISTICS),
		VS_PENETRATE(57, "vsPenetrate", STATISTICS),
		VS_AMBUSH(58, "vsAmbush", STATISTICS),
		VS_DODGE(59,"vsDodge", STATISTICS),
		VS_HIDE(60, "vsHide", STATISTICS),
		// unused ID 61
		TO_RUN_AWAY(63, "toRunAway", STATISTICS),
		// unused ID 64

		// resistances
		RESIST_BLUDGEONING(48, "resistBludgeoning", STATISTICS, ModifierMetric.PERCENTAGE),
		RESIST_PIERCING(49, "resistPiercing", STATISTICS, ModifierMetric.PERCENTAGE),
		RESIST_SLASHING(50, "resistSlashing", STATISTICS, ModifierMetric.PERCENTAGE),
		RESIST_FIRE(65, "resistFire", STATISTICS, ModifierMetric.PERCENTAGE),
		RESIST_WATER(66, "resistWater", STATISTICS, ModifierMetric.PERCENTAGE),
		RESIST_EARTH(67, "resistEarth", STATISTICS, ModifierMetric.PERCENTAGE),
		RESIST_AIR(68, "resistAir", STATISTICS, ModifierMetric.PERCENTAGE),
		RESIST_MENTAL(69, "resistMental", STATISTICS, ModifierMetric.PERCENTAGE),
		RESIST_ENERGY(70, "resistEnergy", STATISTICS, ModifierMetric.PERCENTAGE),

		// modifiers: spell casting
		RED_MAGIC_SPELLS(175, "redMagicSpells", STATISTICS),
		BLACK_MAGIC_SPELLS(176, "blackMagicSpells", STATISTICS),
		PURPLE_MAGIC_SPELLS(177, "purpleMagicSpells", STATISTICS),
		GOLD_MAGIC_SPELLS(178, "goldMagicSpells", STATISTICS),
		WHITE_MAGIC_SPELLS(179, "whiteMagicSpells", STATISTICS),
		GREEN_MAGIC_SPELLS(180, "greenMagicSpells", STATISTICS),
		BLUE_MAGIC_SPELLS(181, "blueMagicSpells", STATISTICS),

		// modifiers: magic generated
		RED_MAGIC_GEN(71, "redMagicGen", STATISTICS),
		BLACK_MAGIC_GEN(72, "blackMagicGen", STATISTICS),
		PURPLE_MAGIC_GEN(73, "purpleMagicGen", STATISTICS),
		GOLD_MAGIC_GEN(74, "goldMagicGen", STATISTICS),
		WHITE_MAGIC_GEN(75, "whiteMagicGen", STATISTICS),
		GREEN_MAGIC_GEN(76, "greenMagicGen", STATISTICS),
		BLUE_MAGIC_GEN(77, "blueMagicGen", STATISTICS),

		// modifiers: regen
		HIT_POINT_REGEN(78, "hitPointRegenRate", STATISTICS),
		ACTION_POINT_REGEN(79, "actionPointRegenRate", STATISTICS),
		MAGIC_POINT_REGEN(80, "magicPointRegenRate", STATISTICS),
		// unused: 83, 84, 85, 86, 87
		STAMINA_REGEN(144, "staminaRegenRate", STATISTICS),

		// properties
		IMMUNE_TO_DAMAGE(88, "immuneToDamage", ModifierMetric.BOOLEAN),
		IMMUNE_TO_HEAT(89, "immuneToHeat", ModifierMetric.BOOLEAN),
		IMMUNE_TO_COLD(90, "immuneToCold", ModifierMetric.BOOLEAN),
		IMMUNE_TO_POISON(91, "immuneToPoison", ModifierMetric.BOOLEAN),
		IMMUNE_TO_LIGHTNING(92, "immuneToLightning", ModifierMetric.BOOLEAN),
		IMMUNE_TO_PSYCHIC(93,"immuneToPsychic", ModifierMetric.BOOLEAN),
		IMMUNE_TO_ACID(94, "immuneToAcid", ModifierMetric.BOOLEAN),
		IMMUNE_TO_BLIND(95, "immuneToBlind", ModifierMetric.BOOLEAN),
		IMMUNE_TO_DISEASE(96, "immuneToDisease", ModifierMetric.BOOLEAN),
		IMMUNE_TO_FEAR(97, "immuneToFear", ModifierMetric.BOOLEAN),
		IMMUNE_TO_HEX(98, "immuneToHex", ModifierMetric.BOOLEAN),
		IMMUNE_TO_INSANE(99, "immuneToInsane", ModifierMetric.BOOLEAN),
		IMMUNE_TO_INVISIBLE(100, "immuneToInvisible", ModifierMetric.BOOLEAN),
		IMMUNE_TO_IRRITATE(101, "immuneToIrritate", ModifierMetric.BOOLEAN),
		IMMUNE_TO_KO(102, "immuneToKO", ModifierMetric.BOOLEAN),
		IMMUNE_TO_NAUSEA(103, "immuneToNausea", ModifierMetric.BOOLEAN),
		IMMUNE_TO_PARALYSE(104, "immuneToParalyse", ModifierMetric.BOOLEAN),
		IMMUNE_TO_POSSESSION(105, "immuneToPossession", ModifierMetric.BOOLEAN),
		IMMUNE_TO_SILENCE(106, "immuneToSilence", ModifierMetric.BOOLEAN),
		IMMUNE_TO_SLEEP(107, "immuneToSleep", ModifierMetric.BOOLEAN),
		IMMUNE_TO_STONE(108, "immuneToStone", ModifierMetric.BOOLEAN),
		IMMUNE_TO_SWALLOW(109, "immuneToSwallow", ModifierMetric.BOOLEAN),
		IMMUNE_TO_WEB(110, "immuneToWeb", ModifierMetric.BOOLEAN),
		IMMUNE_TO_CRITICALS(228, "immuneToCriticals", ModifierMetric.BOOLEAN),

		// abilities
		LIGHT_SLEEPER(111, "lightSleeper", ModifierMetric.BOOLEAN),
		BLIND_FIGHTING(112, "blindFighting", ModifierMetric.BOOLEAN),
		EXTRA_GOLD(113, "extraGold", ModifierMetric.PERCENTAGE),
		CHEAT_DEATH(114, "cheatDeath", ModifierMetric.BOOLEAN),
		MAGIC_ABSORPTION(115, "magicAbsorption"),
		ARROW_CUTTING(116, "arrowCutting", ModifierMetric.PERCENTAGE),
		AMBUSHER(117, "ambusher"),
		ENTERTAINER(118, "entertainer", ModifierMetric.BOOLEAN),
		DIPLOMAT(119, "diplomat"),
		BLINK(120, "blink", ModifierMetric.BOOLEAN),
		TIRELESS_AXE(121, "tirelessAxe", ModifierMetric.BOOLEAN),
		TIRELESS_BOW(122, "tirelessBow", ModifierMetric.BOOLEAN),
		TIRELESS_DAGGER(123, "tirelessDagger", ModifierMetric.BOOLEAN),
		TIRELESS_MACE(124, "tirelessMace", ModifierMetric.BOOLEAN),
		TIRELESS_SPEAR(125, "tirelessSpear", ModifierMetric.BOOLEAN),
		TIRELESS_STAFF(126, "tirelessStaff", ModifierMetric.BOOLEAN),
		TIRELESS_SWORD(127, "tirelessSword", ModifierMetric.BOOLEAN),
		TIRELESS_THROWN(128, "tirelessThrown", ModifierMetric.BOOLEAN),
		TIRELESS_UNARMED(129, "tirelessUnarmed", ModifierMetric.BOOLEAN),
		TOUCH_BLIND(130, "blindTouch", ModifierMetric.PERCENTAGE),
		TOUCH_DISEASE(131, "diseaseTouch", ModifierMetric.PERCENTAGE),
		TOUCH_FEAR(132, "fearTouch", ModifierMetric.PERCENTAGE),
		TOUCH_HEX(133, "hexTouch", ModifierMetric.PERCENTAGE),
		TOUCH_INSANE(134, "insaneTouch", ModifierMetric.PERCENTAGE),
		TOUCH_IRRITATE(135, "irritateTouch", ModifierMetric.PERCENTAGE),
		TOUCH_NAUSEA(136, "nauseaTouch", ModifierMetric.PERCENTAGE),
		TOUCH_PARALYSE(137, "paralyseTouch", ModifierMetric.PERCENTAGE),
		TOUCH_SILENCE(138, "silenceTouch", ModifierMetric.PERCENTAGE),
		TOUCH_SLEEP(139, "sleepTouch", ModifierMetric.PERCENTAGE),
		TOUCH_STONE(140, "stoneTouch", ModifierMetric.PERCENTAGE),
		TOUCH_WEB(141, "webTouch", ModifierMetric.PERCENTAGE),
		TOUCH_POISON(166, "poisonTouch", ModifierMetric.PERCENTAGE),
		RAZOR_CLOAK(142, "razorCloak"),
		CC_PENALTY(143, "ccPenalty"),
		DAMAGE_MULTIPLIER(145, "damageMultiplier", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_AXE(146, "lightningStrikeAxe", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_DAGGER(147, "lightningStrikeDagger", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_MACE(148, "lightningStrikeMace", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_SPEAR(149, "lightningStrikeSpear", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_STAFF(150, "lightningStrikeStaff", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_SWORD(151, "lightningStrikeSword", ModifierMetric.BOOLEAN),
		LIGHTNING_STRIKE_UNARMED(152, "lightningStrikeUnarmed", ModifierMetric.BOOLEAN),
		BERSERKER(153, "berserker", ModifierMetric.PERCENTAGE),
		DEADLY_STRIKE(154, "deadlyStrike"),
		DODGE(155, "dodge", ModifierMetric.PERCENTAGE),
		MASTER_ARCHER(156, "masterArcher", ModifierMetric.BOOLEAN),
		DIVINE_PROTECTION(157, "divineProtection", ModifierMetric.BOOLEAN),
		KI_FURY(158, "kiFury", ModifierMetric.BOOLEAN),
		FEY_AFFINITY(159, "feyAffinity", ModifierMetric.BOOLEAN),
		ARCANE_BLOOD(160, "arcaneBlood", ModifierMetric.BOOLEAN),
		DISPLACER(161, "displacer", ModifierMetric.BOOLEAN),
		PARRY(162, "parry", ModifierMetric.PERCENTAGE),
		RIPOSTE(237, "riposte", ModifierMetric.PERCENTAGE),
		MELEE_MASTER(163, "meleeMaster", ModifierMetric.BOOLEAN),
		DEADLY_AIM(164, "deadlyAim"),
		MASTER_THIEF(165, "masterThief", ModifierMetric.BOOLEAN),
		OBFUSCATION(167, "obfuscation"),
		SHADOW_MASTER(168, "shadowMaster", ModifierMetric.BOOLEAN),
		CHARMED_DESTINY(169, "charmedDestiny", ModifierMetric.BOOLEAN),
		CHANNELLING(170, "channelling", ModifierMetric.BOOLEAN),
		SIGNATURE_WEAPON_ENGINEERING(171, "signatureWeaponEngineering"),
		AMPHIBIOUS(172, "amphibious", ModifierMetric.BOOLEAN),
		BONUS_ATTACKS(173, "bonusAttacks"),
		BONUS_STRIKES(174, "bonusStrikes"),
		LARGE_SIZE(182, "largeSize", ModifierMetric.BOOLEAN),
		THREATEN(183, "threaten"),
		DRINKING_FIT(184, "drinkingFit", ModifierMetric.BOOLEAN),
		IAJUTSU(185, "iajutsu"),
		FAVOURED_ENEMY_BEAST(186, "favouredEnemyBeast", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_CONSTRUCT(187, "favouredEnemyConstruct", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_MAZE_CREATURE(188, "favouredEnemyMazeCreature", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_CRYPTOBESTIA(189, "favouredEnemyCryptobestia", ModifierMetric.BOOLEAN),
		// unused id 190
		FAVOURED_ENEMY_DRAGON(191, "favouredEnemyDragon", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_ELEMENTAL(192, "favouredEnemyElemental", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_FEY(193, "favouredEnemyFey", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_GIANT(194, "favouredEnemyGiant", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_HORROR(195, "favouredEnemyHorror", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_HUMANOID(196, "favouredEnemyHumanoid", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_ILLUSION(197, "favouredEnemyIllusion", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_MONSTROSITY(198, "favouredEnemyMonstrosity", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_OOZE(199, "favouredEnemyOoze", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_OUTSIDER(200, "favouredEnemyOutsider", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_PLANT(201, "favouredEnemyPlant", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_UNDEAD(202, "favouredEnemyUndead", ModifierMetric.BOOLEAN),
		FAVOURED_ENEMY_VERMIN(203, "favouredEnemyVermin", ModifierMetric.BOOLEAN),
		POWER_OF_DARKNESS(204, "powerOfDarkness", ModifierMetric.BOOLEAN),
		FLIER(205, "flier", ModifierMetric.BOOLEAN),
		STRONG_SWIMMER(206, "strongSwimmer", ModifierMetric.BOOLEAN),
		FURIOUS_PURPOSE(207, "furiousPurpose", ModifierMetric.BOOLEAN),
		SWORD_PARRY(208, "swordParry", ModifierMetric.PERCENTAGE),
		AXE_PARRY(209, "axeParry", ModifierMetric.PERCENTAGE),
		MACE_PARRY(210, "maceParry", ModifierMetric.PERCENTAGE),
		POLEARM_PARRY(211, "polearmParry", ModifierMetric.PERCENTAGE),
		STAFF_PARRY(212, "staffParry", ModifierMetric.PERCENTAGE),
		AMAZON_COURAGE(213, "amazonCourage", ModifierMetric.BOOLEAN),
		AMAZON_WILLPOWER(214, "amazonWillpower", ModifierMetric.BOOLEAN),
		AMAZON_FURY(220, "amazonFury", ModifierMetric.BOOLEAN),
		SWORD_1H_WIELD(215, "sword1HWield", ModifierMetric.BOOLEAN),
		AXE_1H_WIELD(216, "axe1HWield", ModifierMetric.BOOLEAN),
		MACE_1H_WIELD(217, "mace1HWield", ModifierMetric.BOOLEAN),
		POLEARM_1H_WIELD(218, "polearm1HWield", ModifierMetric.BOOLEAN),
		STAFF_1H_WIELD(219, "staff1HWield", ModifierMetric.BOOLEAN),
		BERSERK_POWERS(221, "berserkPowers"),
		CODE_OF_HONOUR(222, "codeOfHonour", ModifierMetric.BOOLEAN),
		CODE_OF_DISHONOUR(223, "codeOfDishonour", ModifierMetric.BOOLEAN),
		DYING_BLOW(224, "dyingBlow", ModifierMetric.BOOLEAN),
		FINISHER(225, "finisher", ModifierMetric.BOOLEAN),
		BLOODTHIRSTY(226, "bloodthirsty"),
		NOTORIETY(227, "notoriety"),
		TERRIFYING_REPUTATION(229, "terrifyingReputation", ModifierMetric.BOOLEAN),
		HIDE(230, "hide", ModifierMetric.BOOLEAN),
		TO_BRIBE(62, "toBribe"),
		ACTION_REGEN_URBAN(231, "actionRegenUrban"),
		ACTION_REGEN_DUNGEON(232, "actionRegenDungeon"),
		ACTION_REGEN_WILDERNESS(233, "actionRegenWilderness"),
		ACTION_REGEN_WASTELAND(234, "actionRegenWasteland"),
		DANGER_SENSE(235, "dangerSense", ModifierMetric.BOOLEAN),
		QUICK_WITS(236, "quickWits", ModifierMetric.BOOLEAN),
		TRAP_SENSE(238, "trapSense", ModifierMetric.BOOLEAN),
		SLIP_AWAY(239, "slipAway", ModifierMetric.BOOLEAN),
		// unused 240
		SURPRISE_PARRY(241, "surpriseParry"),
		BARTER_EXPERT(242, "barterExpert"),
		PERSUASION(243, "persuasion", ModifierMetric.BOOLEAN),
		;

		/**
		 * An integer ID to track these with. Used at the moment as an index
		 * into the storage bitmap
		 */
		private int id;

		/**
		 * The key to use when finding strings in the resource bundle.
		 */
		private String resourceBundleKey;

		/**
		 * What type of modifier this is.
		 */
		private ModifierType type;

		/**
		 * How this modifier is measured.
		 */
		private ModifierMetric metric;

		/**
		 * Defaults to {@link mclachlan.maze.stat.Stats.ModifierMetric#PLAIN} and
		 * {@link mclachlan.maze.stat.Stats.ModifierType#PROPERTIES}
		 */
		Modifier(int id, String resourceBundleKey)
		{
			this(id, resourceBundleKey, PROPERTIES, ModifierMetric.PLAIN);
		}

		/**
		 * Defaults to {@link mclachlan.maze.stat.Stats.ModifierType#PROPERTIES}
		 */
		Modifier(int id, String resourceBundleKey, ModifierMetric metric)
		{
			this(id, resourceBundleKey, PROPERTIES, metric);
		}

		/**
		 * Defaults to {@link mclachlan.maze.stat.Stats.ModifierMetric#PLAIN}
		 */
		Modifier(int id, String resourceBundleKey, ModifierType type)
		{
			this(id, resourceBundleKey, type, ModifierMetric.PLAIN);
		}

		Modifier(int id, String resourceBundleKey, ModifierType type, ModifierMetric metric)
		{
			this.id = id;
			this.resourceBundleKey = resourceBundleKey;
			this.type = type;
			this.metric = metric;
		}

		public ModifierType getType()
		{
			return type;
		}

		public ModifierMetric getMetric()
		{
			return metric;
		}

		public String getResourceBundleKey()
		{
			return resourceBundleKey;
		}

		public int getId()
		{
			return id;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static enum ModifierMetric
	{
		PLAIN, BOOLEAN, PERCENTAGE;
	}

	/*-------------------------------------------------------------------------*/
	public static enum ModifierType
	{
		NONE,
		RESOURCE,
		ATTRIBUTE,
		COMBAT,
		STEALTH,
		MAGIC,
		STATISTICS,
		PROPERTIES,
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args)
	{
		System.out.println(allModifiers.size());
	}
}
