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


package mclachlan.maze.editor.swing;

import java.util.*;
import mclachlan.maze.stat.*;

/**
 *
 */
public class ItemEnchantmentScheme
{
	/*-------------------------------------------------------------------------*/
	public List<ItemEnchantment> quickFillEnchantments()
	{
		List<ItemEnchantment> result = new ArrayList<ItemEnchantment>();
		StatModifier modifiersToEnchant = getModifiersToEnchant();

		for (String mod : modifiersToEnchant.getModifiers().keySet())
		{
			int value = modifiersToEnchant.getModifier(mod);

			StatModifier temp = new StatModifier();
			temp.setModifier(mod, value);

			result.add(0, new ItemEnchantment(
				mod,
				getPrefix(mod),
				getSuffix(mod),
				temp,
				getCostModifier(mod)));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getCostModifier(String mod)
	{
		return costs.get(mod);
	}

	public String getSuffix(String mod)
	{
		return suffices.get(mod);
	}

	public String getPrefix(String mod)
	{
		return prefices.get(mod);
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getModifiersToEnchant()
	{
		return prettyMuchEverything;
	}

	/*-------------------------------------------------------------------------*/
	static StatModifier prettyMuchEverything = new StatModifier();
	static Map<String, Integer> costs = new HashMap<String, Integer>();
	static Map<String, String> prefices = new HashMap<String, String>();
	static Map<String, String> suffices = new HashMap<String, String>();
	static
	{
		setModifiers(prettyMuchEverything,
			Stats.Modifiers.BRAWN,
			Stats.Modifiers.SKILL,
			Stats.Modifiers.THIEVING,
			Stats.Modifiers.SNEAKING,
			Stats.Modifiers.BRAINS,
			Stats.Modifiers.POWER,
			Stats.Modifiers.SWING,
			Stats.Modifiers.THRUST,
			Stats.Modifiers.CUT,
			Stats.Modifiers.LUNGE,
			Stats.Modifiers.BASH,
			Stats.Modifiers.PUNCH,
			Stats.Modifiers.KICK,
			Stats.Modifiers.THROW,
			Stats.Modifiers.SHOOT,
			Stats.Modifiers.FIRE,
			Stats.Modifiers.DUAL_WEAPONS,
			Stats.Modifiers.CHIVALRY,
			Stats.Modifiers.KENDO,
			Stats.Modifiers.STREETWISE,
			Stats.Modifiers.DUNGEONEER,
			Stats.Modifiers.WILDERNESS_LORE,
			Stats.Modifiers.SURVIVAL,
			Stats.Modifiers.BACKSTAB,
			Stats.Modifiers.SNIPE,
			Stats.Modifiers.LOCK_AND_TRAP,
			Stats.Modifiers.STEAL,
			Stats.Modifiers.MARTIAL_ARTS,
			Stats.Modifiers.MELEE_CRITICALS,
			Stats.Modifiers.THROWN_CRITICALS,
			Stats.Modifiers.RANGED_CRITICALS,
			Stats.Modifiers.CHANT,
			Stats.Modifiers.RHYME,
			Stats.Modifiers.GESTURE,
			Stats.Modifiers.POSTURE,
			Stats.Modifiers.THOUGHT,
			Stats.Modifiers.HERBAL,
			Stats.Modifiers.ALCHEMIC,
			Stats.Modifiers.ARTIFACTS,
			Stats.Modifiers.MYTHOLOGY,
			Stats.Modifiers.CRAFT,
			Stats.Modifiers.POWER_CAST,
			Stats.Modifiers.ENGINEERING,
			Stats.Modifiers.MUSIC,
			Stats.Modifiers.INITIATIVE,
			Stats.Modifiers.ATTACK,
			Stats.Modifiers.DEFENCE,
			Stats.Modifiers.DAMAGE,
			Stats.Modifiers.TO_PENETRATE,
			Stats.Modifiers.VS_PENETRATE,
			Stats.Modifiers.VS_AMBUSH,
			Stats.Modifiers.VS_DODGE,
			Stats.Modifiers.VS_HIDE,
			Stats.Modifiers.TO_THREATEN,
			Stats.Modifiers.TO_BRIBE,
			Stats.Modifiers.TO_RUN_AWAY,
			Stats.Modifiers.RESIST_BLUDGEONING,
			Stats.Modifiers.RESIST_PIERCING,
			Stats.Modifiers.RESIST_SLASHING,
			Stats.Modifiers.RESIST_FIRE,
			Stats.Modifiers.RESIST_WATER,
			Stats.Modifiers.RESIST_EARTH,
			Stats.Modifiers.RESIST_AIR,
			Stats.Modifiers.RESIST_MENTAL,
			Stats.Modifiers.RESIST_ENERGY,
			Stats.Modifiers.RED_MAGIC_GEN,
			Stats.Modifiers.BLACK_MAGIC_GEN,
			Stats.Modifiers.PURPLE_MAGIC_GEN,
			Stats.Modifiers.GOLD_MAGIC_GEN,
			Stats.Modifiers.WHITE_MAGIC_GEN,
			Stats.Modifiers.GREEN_MAGIC_GEN,
			Stats.Modifiers.BLUE_MAGIC_GEN,
			Stats.Modifiers.HIT_POINT_REGEN,
			Stats.Modifiers.ACTION_POINT_REGEN,
			Stats.Modifiers.MAGIC_POINT_REGEN);

		costs.put(Stats.Modifiers.HIT_POINTS,1000);
		costs.put(Stats.Modifiers.ACTION_POINTS,1000);
		costs.put(Stats.Modifiers.MAGIC_POINTS,1000);
		costs.put(Stats.Modifiers.BRAWN,1000);
		costs.put(Stats.Modifiers.SKILL,1000);
		costs.put(Stats.Modifiers.THIEVING,1000);
		costs.put(Stats.Modifiers.SNEAKING,1000);
		costs.put(Stats.Modifiers.BRAINS,1000);
		costs.put(Stats.Modifiers.POWER,1000);
		costs.put(Stats.Modifiers.SWING,1000);
		costs.put(Stats.Modifiers.THRUST,1000);
		costs.put(Stats.Modifiers.CUT,1000);
		costs.put(Stats.Modifiers.LUNGE,1000);
		costs.put(Stats.Modifiers.BASH,1000);
		costs.put(Stats.Modifiers.PUNCH,1000);
		costs.put(Stats.Modifiers.KICK,1000);
		costs.put(Stats.Modifiers.THROW,1000);
		costs.put(Stats.Modifiers.SHOOT,1000);
		costs.put(Stats.Modifiers.FIRE,1000);
		costs.put(Stats.Modifiers.DUAL_WEAPONS,2000);
		costs.put(Stats.Modifiers.CHIVALRY,2000);
		costs.put(Stats.Modifiers.KENDO,2000);
		costs.put(Stats.Modifiers.STREETWISE,1000);
		costs.put(Stats.Modifiers.DUNGEONEER,1000);
		costs.put(Stats.Modifiers.WILDERNESS_LORE,1000);
		costs.put(Stats.Modifiers.SURVIVAL,1000);
		costs.put(Stats.Modifiers.BACKSTAB,1000);
		costs.put(Stats.Modifiers.SNIPE,1000);
		costs.put(Stats.Modifiers.LOCK_AND_TRAP,1000);
		costs.put(Stats.Modifiers.STEAL,1000);
		costs.put(Stats.Modifiers.MARTIAL_ARTS,2000);
		costs.put(Stats.Modifiers.MELEE_CRITICALS,2000);
		costs.put(Stats.Modifiers.THROWN_CRITICALS,2000);
		costs.put(Stats.Modifiers.RANGED_CRITICALS,2000);
		costs.put(Stats.Modifiers.CHANT,1000);
		costs.put(Stats.Modifiers.RHYME,1000);
		costs.put(Stats.Modifiers.GESTURE,1000);
		costs.put(Stats.Modifiers.POSTURE,1000);
		costs.put(Stats.Modifiers.THOUGHT,1000);
		costs.put(Stats.Modifiers.HERBAL,1000);
		costs.put(Stats.Modifiers.ALCHEMIC,1000);
		costs.put(Stats.Modifiers.ARTIFACTS,1000);
		costs.put(Stats.Modifiers.MYTHOLOGY,1000);
		costs.put(Stats.Modifiers.CRAFT,1000);
		costs.put(Stats.Modifiers.POWER_CAST,2000);
		costs.put(Stats.Modifiers.ENGINEERING,2000);
		costs.put(Stats.Modifiers.MUSIC,2000);
		costs.put(Stats.Modifiers.INITIATIVE,2000);
		costs.put(Stats.Modifiers.ATTACK,1000);
		costs.put(Stats.Modifiers.DEFENCE,1000);
		costs.put(Stats.Modifiers.DAMAGE,1000);
		costs.put(Stats.Modifiers.TO_PENETRATE,1000);
		costs.put(Stats.Modifiers.VS_PENETRATE,1000);
		costs.put(Stats.Modifiers.VS_AMBUSH,1000);
		costs.put(Stats.Modifiers.VS_DODGE,1000);
		costs.put(Stats.Modifiers.VS_HIDE,1000);
		costs.put(Stats.Modifiers.TO_THREATEN,1000);
		costs.put(Stats.Modifiers.TO_BRIBE,1000);
		costs.put(Stats.Modifiers.TO_RUN_AWAY,1000);
		costs.put(Stats.Modifiers.RESIST_BLUDGEONING,1000);
		costs.put(Stats.Modifiers.RESIST_PIERCING,1000);
		costs.put(Stats.Modifiers.RESIST_SLASHING,1000);
		costs.put(Stats.Modifiers.RESIST_FIRE,1000);
		costs.put(Stats.Modifiers.RESIST_WATER,1000);
		costs.put(Stats.Modifiers.RESIST_EARTH,1000);
		costs.put(Stats.Modifiers.RESIST_AIR,1000);
		costs.put(Stats.Modifiers.RESIST_MENTAL,1000);
		costs.put(Stats.Modifiers.RESIST_ENERGY,1000);
		costs.put(Stats.Modifiers.RED_MAGIC_GEN,3000);
		costs.put(Stats.Modifiers.BLACK_MAGIC_GEN,3000);
		costs.put(Stats.Modifiers.PURPLE_MAGIC_GEN,3000);
		costs.put(Stats.Modifiers.GOLD_MAGIC_GEN,3000);
		costs.put(Stats.Modifiers.WHITE_MAGIC_GEN,3000);
		costs.put(Stats.Modifiers.GREEN_MAGIC_GEN,3000);
		costs.put(Stats.Modifiers.BLUE_MAGIC_GEN,3000);
		costs.put(Stats.Modifiers.HIT_POINT_REGEN,2000);
		costs.put(Stats.Modifiers.ACTION_POINT_REGEN,2000);
		costs.put(Stats.Modifiers.MAGIC_POINT_REGEN,2000);
		costs.put(Stats.Modifiers.IMMUNE_TO_DAMAGE,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_HEAT,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_COLD,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_POISON,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_LIGHTNING,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_PSYCHIC,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_ACID,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_BLIND,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_DISEASE,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_FEAR,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_HEX,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_INSANE,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_INVISIBLE,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_IRRITATE,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_KO,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_NAUSEA,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_PARALYSE,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_POSSESSION,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_SILENCE,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_SLEEP,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_STONE,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_SWALLOW,10000);
		costs.put(Stats.Modifiers.IMMUNE_TO_WEB,10000);
		costs.put(Stats.Modifiers.LIGHT_SLEEPER,10000);
		costs.put(Stats.Modifiers.BLIND_FIGHTING,10000);
		costs.put(Stats.Modifiers.EXTRA_GOLD,10000);
		costs.put(Stats.Modifiers.CHEAT_DEATH,10000);
		costs.put(Stats.Modifiers.MAGIC_ABSORPTION,10000);
		costs.put(Stats.Modifiers.ARROW_CUTTING,10000);
		costs.put(Stats.Modifiers.AMBUSHER,10000);
		costs.put(Stats.Modifiers.ENTERTAINER,10000);
		costs.put(Stats.Modifiers.DIPLOMAT,10000);
		costs.put(Stats.Modifiers.BLINK,10000);
		costs.put(Stats.Modifiers.TIRELESS_SWORD,10000);
		costs.put(Stats.Modifiers.TIRELESS_AXE,10000);
		costs.put(Stats.Modifiers.TIRELESS_SPEAR,10000);
		costs.put(Stats.Modifiers.TIRELESS_MACE,10000);
		costs.put(Stats.Modifiers.TIRELESS_DAGGER,10000);
		costs.put(Stats.Modifiers.TIRELESS_STAFF,10000);
		costs.put(Stats.Modifiers.TIRELESS_BOW,10000);
		costs.put(Stats.Modifiers.TIRELESS_THROWN,10000);
		costs.put(Stats.Modifiers.TIRELESS_UNARMED,10000);
		costs.put(Stats.Modifiers.TOUCH_BLIND,1000);
		costs.put(Stats.Modifiers.TOUCH_FEAR,1000);
		costs.put(Stats.Modifiers.TOUCH_HEX,1000);
		costs.put(Stats.Modifiers.TOUCH_INSANE,1000);
		costs.put(Stats.Modifiers.TOUCH_IRRITATE,1000);
		costs.put(Stats.Modifiers.TOUCH_NAUSEA,1000);
		costs.put(Stats.Modifiers.TOUCH_SILENCE,1000);
		costs.put(Stats.Modifiers.TOUCH_SLEEP,1000);
		costs.put(Stats.Modifiers.TOUCH_STONE,1000);
		costs.put(Stats.Modifiers.TOUCH_PARALYSE,1000);
		costs.put(Stats.Modifiers.TOUCH_WEB,1000);
		costs.put(Stats.Modifiers.TOUCH_DISEASE,1000);
		costs.put(Stats.Modifiers.RAZOR_CLOAK,10000);
		costs.put(Stats.Modifiers.CC_PENALTY,1000);

		prefices.put(Stats.Modifiers.HIT_POINTS,"Vital");
		prefices.put(Stats.Modifiers.ACTION_POINTS,"Furtive");
		prefices.put(Stats.Modifiers.MAGIC_POINTS,"Potent");
		prefices.put(Stats.Modifiers.BRAWN,null);
		prefices.put(Stats.Modifiers.SKILL,"Masterwork");
		prefices.put(Stats.Modifiers.THIEVING,"Thieves");
		prefices.put(Stats.Modifiers.SNEAKING,"Camouflaged");
		prefices.put(Stats.Modifiers.BRAINS,"Genius");
		prefices.put(Stats.Modifiers.POWER,null);
		prefices.put(Stats.Modifiers.SWING,"Slashing");
		prefices.put(Stats.Modifiers.THRUST,"Piercing");
		prefices.put(Stats.Modifiers.CUT,"Weighted");
		prefices.put(Stats.Modifiers.LUNGE,"Balanced");
		prefices.put(Stats.Modifiers.BASH,"Heavy");
		prefices.put(Stats.Modifiers.PUNCH,"Open Hand");
		prefices.put(Stats.Modifiers.KICK,"Tornado");
		prefices.put(Stats.Modifiers.THROW,"Accurate");
		prefices.put(Stats.Modifiers.SHOOT,"Archer's");
		prefices.put(Stats.Modifiers.FIRE,"Deadeye");
		prefices.put(Stats.Modifiers.DUAL_WEAPONS,"Ranger's");
		prefices.put(Stats.Modifiers.CHIVALRY,"Knight's");
		prefices.put(Stats.Modifiers.KENDO,"Ronin's");
		prefices.put(Stats.Modifiers.STREETWISE,null);
		prefices.put(Stats.Modifiers.DUNGEONEER,"Adventurer's");
		prefices.put(Stats.Modifiers.WILDERNESS_LORE,"Hunter's");
		prefices.put(Stats.Modifiers.SURVIVAL,null);
		prefices.put(Stats.Modifiers.BACKSTAB,"Backstabbing");
		prefices.put(Stats.Modifiers.SNIPE,"Sniper's");
		prefices.put(Stats.Modifiers.LOCK_AND_TRAP,null);
		prefices.put(Stats.Modifiers.STEAL,null);
		prefices.put(Stats.Modifiers.MARTIAL_ARTS,"Monk's");
		prefices.put(Stats.Modifiers.MELEE_CRITICALS,"Kirijutsu");
		prefices.put(Stats.Modifiers.THROWN_CRITICALS,"Deadly");
		prefices.put(Stats.Modifiers.RANGED_CRITICALS,"Headshot");
		prefices.put(Stats.Modifiers.CHANT,"Priest's");
		prefices.put(Stats.Modifiers.RHYME,"Poet's");
		prefices.put(Stats.Modifiers.GESTURE,"Somatic");
		prefices.put(Stats.Modifiers.POSTURE,null);
		prefices.put(Stats.Modifiers.THOUGHT,"Mental");
		prefices.put(Stats.Modifiers.HERBAL,"Druid's");
		prefices.put(Stats.Modifiers.ALCHEMIC,"Immortal");
		prefices.put(Stats.Modifiers.ARTIFACTS,"Sage");
		prefices.put(Stats.Modifiers.MYTHOLOGY,"Bard's");
		prefices.put(Stats.Modifiers.CRAFT,null);
		prefices.put(Stats.Modifiers.POWER_CAST,null);
		prefices.put(Stats.Modifiers.ENGINEERING,"Tinker's");
		prefices.put(Stats.Modifiers.MUSIC,null);
		prefices.put(Stats.Modifiers.INITIATIVE,"Quick");
		prefices.put(Stats.Modifiers.ATTACK,"Keen");
		prefices.put(Stats.Modifiers.DEFENCE,null);
		prefices.put(Stats.Modifiers.DAMAGE,"Tempered");
		prefices.put(Stats.Modifiers.TO_PENETRATE,"Piercing");
		prefices.put(Stats.Modifiers.VS_PENETRATE,"Steady");
		prefices.put(Stats.Modifiers.VS_AMBUSH,null);
		prefices.put(Stats.Modifiers.VS_DODGE,"True Striking");
		prefices.put(Stats.Modifiers.VS_HIDE,"True Seeing");
		prefices.put(Stats.Modifiers.TO_THREATEN,"Bluff");
		prefices.put(Stats.Modifiers.TO_BRIBE,"Corrupt");
		prefices.put(Stats.Modifiers.TO_RUN_AWAY,"Coward's");
		prefices.put(Stats.Modifiers.RESIST_BLUDGEONING,"Moonstone");
		prefices.put(Stats.Modifiers.RESIST_PIERCING,"Quartz");
		prefices.put(Stats.Modifiers.RESIST_SLASHING,"Diamond");
		prefices.put(Stats.Modifiers.RESIST_FIRE,"Ruby");
		prefices.put(Stats.Modifiers.RESIST_WATER,"Sapphire");
		prefices.put(Stats.Modifiers.RESIST_EARTH,"Emerald");
		prefices.put(Stats.Modifiers.RESIST_AIR,"Topaz");
		prefices.put(Stats.Modifiers.RESIST_MENTAL,"Crystal");
		prefices.put(Stats.Modifiers.RESIST_ENERGY,"Opal");
		prefices.put(Stats.Modifiers.RED_MAGIC_GEN,null);
		prefices.put(Stats.Modifiers.BLACK_MAGIC_GEN,"Unholy");
		prefices.put(Stats.Modifiers.PURPLE_MAGIC_GEN,null);
		prefices.put(Stats.Modifiers.GOLD_MAGIC_GEN,null);
		prefices.put(Stats.Modifiers.WHITE_MAGIC_GEN,"Holy");
		prefices.put(Stats.Modifiers.GREEN_MAGIC_GEN,null);
		prefices.put(Stats.Modifiers.BLUE_MAGIC_GEN,null);
		prefices.put(Stats.Modifiers.HIT_POINT_REGEN,null);
		prefices.put(Stats.Modifiers.ACTION_POINT_REGEN,null);
		prefices.put(Stats.Modifiers.MAGIC_POINT_REGEN,null);
		prefices.put(Stats.Modifiers.IMMUNE_TO_DAMAGE,"Achilles'");
		prefices.put(Stats.Modifiers.IMMUNE_TO_HEAT,"Dragon");
		prefices.put(Stats.Modifiers.IMMUNE_TO_COLD,"Wyrm");
		prefices.put(Stats.Modifiers.IMMUNE_TO_POISON,"Snake");
		prefices.put(Stats.Modifiers.IMMUNE_TO_LIGHTNING,"Thor's");
		prefices.put(Stats.Modifiers.IMMUNE_TO_PSYCHIC,"Mindflayer");
		prefices.put(Stats.Modifiers.IMMUNE_TO_ACID,"Corroded");
		prefices.put(Stats.Modifiers.IMMUNE_TO_BLIND,null);
		prefices.put(Stats.Modifiers.IMMUNE_TO_DISEASE,"Immortal");
		prefices.put(Stats.Modifiers.IMMUNE_TO_FEAR,"Fearless");
		prefices.put(Stats.Modifiers.IMMUNE_TO_HEX,"Teflon");
		prefices.put(Stats.Modifiers.IMMUNE_TO_INSANE,null);
		prefices.put(Stats.Modifiers.IMMUNE_TO_INVISIBLE,"Amber");
		prefices.put(Stats.Modifiers.IMMUNE_TO_IRRITATE,"Rhino");
		prefices.put(Stats.Modifiers.IMMUNE_TO_KO,"Bull");
		prefices.put(Stats.Modifiers.IMMUNE_TO_NAUSEA,"Amethyst");
		prefices.put(Stats.Modifiers.IMMUNE_TO_PARALYSE,"Agate");
		prefices.put(Stats.Modifiers.IMMUNE_TO_POSSESSION,"Azurite");
		prefices.put(Stats.Modifiers.IMMUNE_TO_SILENCE,"Magician's");
		prefices.put(Stats.Modifiers.IMMUNE_TO_SLEEP,"Citrine");
		prefices.put(Stats.Modifiers.IMMUNE_TO_STONE,"Medusa's");
		prefices.put(Stats.Modifiers.IMMUNE_TO_SWALLOW,"Onyx");
		prefices.put(Stats.Modifiers.IMMUNE_TO_WEB,"Turquoise");
		prefices.put(Stats.Modifiers.LIGHT_SLEEPER,null);
		prefices.put(Stats.Modifiers.BLIND_FIGHTING,null);
		prefices.put(Stats.Modifiers.EXTRA_GOLD,null);
		prefices.put(Stats.Modifiers.CHEAT_DEATH,"Hero's");
		prefices.put(Stats.Modifiers.MAGIC_ABSORPTION,"Channeling");
		prefices.put(Stats.Modifiers.ARROW_CUTTING,null);
		prefices.put(Stats.Modifiers.AMBUSHER,null);
		prefices.put(Stats.Modifiers.ENTERTAINER,"Storytelling");
		prefices.put(Stats.Modifiers.DIPLOMAT,"Diplomat's");
		prefices.put(Stats.Modifiers.BLINK,"Phasing");
		prefices.put(Stats.Modifiers.TIRELESS_SWORD,"Relentless");
		prefices.put(Stats.Modifiers.TIRELESS_AXE,"Beserker's");
		prefices.put(Stats.Modifiers.TIRELESS_SPEAR,"Amazon's");
		prefices.put(Stats.Modifiers.TIRELESS_MACE,"Bludgeoning");
		prefices.put(Stats.Modifiers.TIRELESS_DAGGER,"Bloody");
		prefices.put(Stats.Modifiers.TIRELESS_STAFF,"Whirling");
		prefices.put(Stats.Modifiers.TIRELESS_BOW,"Darkening");
		prefices.put(Stats.Modifiers.TIRELESS_THROWN,"Fusillade");
		prefices.put(Stats.Modifiers.TIRELESS_UNARMED,"Ninja's");
		prefices.put(Stats.Modifiers.TOUCH_BLIND,"Blinding");
		prefices.put(Stats.Modifiers.TOUCH_FEAR,"Fearsome");
		prefices.put(Stats.Modifiers.TOUCH_HEX,"Hexing");
		prefices.put(Stats.Modifiers.TOUCH_INSANE,"Confusing");
		prefices.put(Stats.Modifiers.TOUCH_IRRITATE,"Irritating");
		prefices.put(Stats.Modifiers.TOUCH_NAUSEA,"Nauseating");
		prefices.put(Stats.Modifiers.TOUCH_SILENCE,"Silencing");
		prefices.put(Stats.Modifiers.TOUCH_SLEEP,"Drowsy");
		prefices.put(Stats.Modifiers.TOUCH_STONE,"Petrifying");
		prefices.put(Stats.Modifiers.TOUCH_PARALYSE,"Paralysing");
		prefices.put(Stats.Modifiers.TOUCH_WEB,"Entangling");
		prefices.put(Stats.Modifiers.TOUCH_DISEASE,"Corrupting");
		prefices.put(Stats.Modifiers.RAZOR_CLOAK,null);
		prefices.put(Stats.Modifiers.CC_PENALTY,null);

		suffices.put(Stats.Modifiers.HIT_POINTS,null);
		suffices.put(Stats.Modifiers.ACTION_POINTS,null);
		suffices.put(Stats.Modifiers.MAGIC_POINTS,null);
		suffices.put(Stats.Modifiers.BRAWN,"Of Strength");
		suffices.put(Stats.Modifiers.SKILL,null);
		suffices.put(Stats.Modifiers.THIEVING,null);
		suffices.put(Stats.Modifiers.SNEAKING,null);
		suffices.put(Stats.Modifiers.BRAINS,null);
		suffices.put(Stats.Modifiers.POWER,"Of Potency");
		suffices.put(Stats.Modifiers.SWING,null);
		suffices.put(Stats.Modifiers.THRUST,null);
		suffices.put(Stats.Modifiers.CUT,null);
		suffices.put(Stats.Modifiers.LUNGE,null);
		suffices.put(Stats.Modifiers.BASH,null);
		suffices.put(Stats.Modifiers.PUNCH,null);
		suffices.put(Stats.Modifiers.KICK,null);
		suffices.put(Stats.Modifiers.THROW,null);
		suffices.put(Stats.Modifiers.SHOOT,null);
		suffices.put(Stats.Modifiers.FIRE,null);
		suffices.put(Stats.Modifiers.DUAL_WEAPONS,null);
		suffices.put(Stats.Modifiers.CHIVALRY,null);
		suffices.put(Stats.Modifiers.KENDO,null);
		suffices.put(Stats.Modifiers.STREETWISE,"Of Disguise");
		suffices.put(Stats.Modifiers.DUNGEONEER,null);
		suffices.put(Stats.Modifiers.WILDERNESS_LORE,null);
		suffices.put(Stats.Modifiers.SURVIVAL,"Of Survival");
		suffices.put(Stats.Modifiers.BACKSTAB,null);
		suffices.put(Stats.Modifiers.SNIPE,null);
		suffices.put(Stats.Modifiers.LOCK_AND_TRAP,"Of Looting");
		suffices.put(Stats.Modifiers.STEAL,"Of Theft");
		suffices.put(Stats.Modifiers.MARTIAL_ARTS,null);
		suffices.put(Stats.Modifiers.MELEE_CRITICALS,null);
		suffices.put(Stats.Modifiers.THROWN_CRITICALS,null);
		suffices.put(Stats.Modifiers.RANGED_CRITICALS,null);
		suffices.put(Stats.Modifiers.CHANT,null);
		suffices.put(Stats.Modifiers.RHYME,null);
		suffices.put(Stats.Modifiers.GESTURE,null);
		suffices.put(Stats.Modifiers.POSTURE,"Of Stance");
		suffices.put(Stats.Modifiers.THOUGHT,null);
		suffices.put(Stats.Modifiers.HERBAL,null);
		suffices.put(Stats.Modifiers.ALCHEMIC,null);
		suffices.put(Stats.Modifiers.ARTIFACTS,null);
		suffices.put(Stats.Modifiers.MYTHOLOGY,null);
		suffices.put(Stats.Modifiers.CRAFT,"Of Engineering");
		suffices.put(Stats.Modifiers.POWER_CAST,"Of Domination");
		suffices.put(Stats.Modifiers.ENGINEERING,null);
		suffices.put(Stats.Modifiers.MUSIC,"Of Melody");
		suffices.put(Stats.Modifiers.INITIATIVE,null);
		suffices.put(Stats.Modifiers.ATTACK,null);
		suffices.put(Stats.Modifiers.DEFENCE,"Of Parrying");
		suffices.put(Stats.Modifiers.DAMAGE,null);
		suffices.put(Stats.Modifiers.TO_PENETRATE,null);
		suffices.put(Stats.Modifiers.VS_PENETRATE,null);
		suffices.put(Stats.Modifiers.VS_AMBUSH,"Of Alertness");
		suffices.put(Stats.Modifiers.VS_DODGE,null);
		suffices.put(Stats.Modifiers.VS_HIDE,null);
		suffices.put(Stats.Modifiers.TO_THREATEN,null);
		suffices.put(Stats.Modifiers.TO_BRIBE,null);
		suffices.put(Stats.Modifiers.TO_RUN_AWAY,null);
		suffices.put(Stats.Modifiers.RESIST_BLUDGEONING,null);
		suffices.put(Stats.Modifiers.RESIST_PIERCING,null);
		suffices.put(Stats.Modifiers.RESIST_SLASHING,null);
		suffices.put(Stats.Modifiers.RESIST_FIRE,null);
		suffices.put(Stats.Modifiers.RESIST_WATER,null);
		suffices.put(Stats.Modifiers.RESIST_EARTH,null);
		suffices.put(Stats.Modifiers.RESIST_AIR,null);
		suffices.put(Stats.Modifiers.RESIST_MENTAL,null);
		suffices.put(Stats.Modifiers.RESIST_ENERGY,null);
		suffices.put(Stats.Modifiers.RED_MAGIC_GEN,"Of Sorcery");
		suffices.put(Stats.Modifiers.BLACK_MAGIC_GEN,null);
		suffices.put(Stats.Modifiers.PURPLE_MAGIC_GEN,"Of Witchcraft");
		suffices.put(Stats.Modifiers.GOLD_MAGIC_GEN,"Of Enchantment");
		suffices.put(Stats.Modifiers.WHITE_MAGIC_GEN,null);
		suffices.put(Stats.Modifiers.GREEN_MAGIC_GEN,"Of Druidism");
		suffices.put(Stats.Modifiers.BLUE_MAGIC_GEN,"Of The Elements");
		suffices.put(Stats.Modifiers.HIT_POINT_REGEN,"Of Healing");
		suffices.put(Stats.Modifiers.ACTION_POINT_REGEN,"Of Hiding");
		suffices.put(Stats.Modifiers.MAGIC_POINT_REGEN,"Of Mana");
		suffices.put(Stats.Modifiers.IMMUNE_TO_DAMAGE,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_HEAT,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_COLD,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_POISON,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_LIGHTNING,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_PSYCHIC,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_ACID,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_BLIND,"Of Vision");
		suffices.put(Stats.Modifiers.IMMUNE_TO_DISEASE,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_FEAR,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_HEX,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_INSANE,"Of Logic");
		suffices.put(Stats.Modifiers.IMMUNE_TO_INVISIBLE,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_IRRITATE,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_KO,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_NAUSEA,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_PARALYSE,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_POSSESSION,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_SILENCE,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_SLEEP,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_STONE,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_SWALLOW,null);
		suffices.put(Stats.Modifiers.IMMUNE_TO_WEB,null);
		suffices.put(Stats.Modifiers.LIGHT_SLEEPER,"Of Watching");
		suffices.put(Stats.Modifiers.BLIND_FIGHTING,"Of Kkill");
		suffices.put(Stats.Modifiers.EXTRA_GOLD,"Of Wealth");
		suffices.put(Stats.Modifiers.CHEAT_DEATH,null);
		suffices.put(Stats.Modifiers.MAGIC_ABSORPTION,null);
		suffices.put(Stats.Modifiers.ARROW_CUTTING,"Of Deflection");
		suffices.put(Stats.Modifiers.AMBUSHER,"Of Murder");
		suffices.put(Stats.Modifiers.ENTERTAINER,null);
		suffices.put(Stats.Modifiers.DIPLOMAT,null);
		suffices.put(Stats.Modifiers.BLINK,null);
		suffices.put(Stats.Modifiers.TIRELESS_SWORD,null);
		suffices.put(Stats.Modifiers.TIRELESS_AXE,null);
		suffices.put(Stats.Modifiers.TIRELESS_SPEAR,null);
		suffices.put(Stats.Modifiers.TIRELESS_MACE,null);
		suffices.put(Stats.Modifiers.TIRELESS_DAGGER,null);
		suffices.put(Stats.Modifiers.TIRELESS_STAFF,null);
		suffices.put(Stats.Modifiers.TIRELESS_BOW,null);
		suffices.put(Stats.Modifiers.TIRELESS_THROWN,null);
		suffices.put(Stats.Modifiers.TIRELESS_UNARMED,null);
		suffices.put(Stats.Modifiers.TOUCH_BLIND,null);
		suffices.put(Stats.Modifiers.TOUCH_FEAR,null);
		suffices.put(Stats.Modifiers.TOUCH_HEX,null);
		suffices.put(Stats.Modifiers.TOUCH_INSANE,null);
		suffices.put(Stats.Modifiers.TOUCH_IRRITATE,null);
		suffices.put(Stats.Modifiers.TOUCH_NAUSEA,null);
		suffices.put(Stats.Modifiers.TOUCH_SILENCE,null);
		suffices.put(Stats.Modifiers.TOUCH_SLEEP,null);
		suffices.put(Stats.Modifiers.TOUCH_STONE,null);
		suffices.put(Stats.Modifiers.TOUCH_PARALYSE,null);
		suffices.put(Stats.Modifiers.TOUCH_WEB,null);
		suffices.put(Stats.Modifiers.TOUCH_DISEASE,null);
		suffices.put(Stats.Modifiers.RAZOR_CLOAK,"Of Razors");
		suffices.put(Stats.Modifiers.CC_PENALTY,"Of Weight");
	}

	static void setModifiers(StatModifier sm, String... modifiers)
	{
		for (String mod : modifiers)
		{
			sm.setModifier(mod,1);
		}
	}
}