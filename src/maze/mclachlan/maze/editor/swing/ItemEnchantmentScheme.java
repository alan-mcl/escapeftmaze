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

		for (Stats.Modifier mod : modifiersToEnchant.getModifiers().keySet())
		{
			int value = modifiersToEnchant.getModifier(mod);

			StatModifier temp = new StatModifier();
			temp.setModifier(mod, value);

			result.add(0, new ItemEnchantment(
				mod.getResourceBundleKey(),
				getPrefix(mod),
				getSuffix(mod),
				temp,
				getCostModifier(mod)));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getCostModifier(Stats.Modifier mod)
	{
		return costs.get(mod);
	}

	public String getSuffix(Stats.Modifier mod)
	{
		return suffices.get(mod);
	}

	public String getPrefix(Stats.Modifier mod)
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
	static Map<Stats.Modifier, Integer> costs = new HashMap<Stats.Modifier, Integer>();
	static Map<Stats.Modifier, String> prefices = new HashMap<Stats.Modifier, String>();
	static Map<Stats.Modifier, String> suffices = new HashMap<Stats.Modifier, String>();
	static
	{
		setModifiers(prettyMuchEverything,
			Stats.Modifier.BRAWN,
			Stats.Modifier.SKILL,
			Stats.Modifier.THIEVING,
			Stats.Modifier.SNEAKING,
			Stats.Modifier.BRAINS,
			Stats.Modifier.POWER,
			Stats.Modifier.SWING,
			Stats.Modifier.THRUST,
			Stats.Modifier.CUT,
			Stats.Modifier.LUNGE,
			Stats.Modifier.BASH,
			Stats.Modifier.PUNCH,
			Stats.Modifier.KICK,
			Stats.Modifier.THROW,
			Stats.Modifier.SHOOT,
			Stats.Modifier.FIRE,
			Stats.Modifier.DUAL_WEAPONS,
			Stats.Modifier.CHIVALRY,
			Stats.Modifier.KENDO,
			Stats.Modifier.STREETWISE,
			Stats.Modifier.DUNGEONEER,
			Stats.Modifier.WILDERNESS_LORE,
			Stats.Modifier.SURVIVAL,
			Stats.Modifier.BACKSTAB,
			Stats.Modifier.SNIPE,
			Stats.Modifier.LOCK_AND_TRAP,
			Stats.Modifier.STEAL,
			Stats.Modifier.MARTIAL_ARTS,
			Stats.Modifier.MELEE_CRITICALS,
			Stats.Modifier.THROWN_CRITICALS,
			Stats.Modifier.RANGED_CRITICALS,
			Stats.Modifier.CHANT,
			Stats.Modifier.RHYME,
			Stats.Modifier.GESTURE,
			Stats.Modifier.POSTURE,
			Stats.Modifier.THOUGHT,
			Stats.Modifier.HERBAL,
			Stats.Modifier.ALCHEMIC,
			Stats.Modifier.ARTIFACTS,
			Stats.Modifier.MYTHOLOGY,
			Stats.Modifier.CRAFT,
			Stats.Modifier.POWER_CAST,
			Stats.Modifier.ENGINEERING,
			Stats.Modifier.MUSIC,
			Stats.Modifier.INITIATIVE,
			Stats.Modifier.ATTACK,
			Stats.Modifier.DEFENCE,
			Stats.Modifier.DAMAGE,
			Stats.Modifier.TO_PENETRATE,
			Stats.Modifier.VS_PENETRATE,
			Stats.Modifier.VS_AMBUSH,
			Stats.Modifier.VS_DODGE,
			Stats.Modifier.VS_HIDE,
			Stats.Modifier.THREATEN,
			Stats.Modifier.TO_BRIBE,
			Stats.Modifier.TO_RUN_AWAY,
			Stats.Modifier.RESIST_BLUDGEONING,
			Stats.Modifier.RESIST_PIERCING,
			Stats.Modifier.RESIST_SLASHING,
			Stats.Modifier.RESIST_FIRE,
			Stats.Modifier.RESIST_WATER,
			Stats.Modifier.RESIST_EARTH,
			Stats.Modifier.RESIST_AIR,
			Stats.Modifier.RESIST_MENTAL,
			Stats.Modifier.RESIST_ENERGY,
			Stats.Modifier.RED_MAGIC_GEN,
			Stats.Modifier.BLACK_MAGIC_GEN,
			Stats.Modifier.PURPLE_MAGIC_GEN,
			Stats.Modifier.GOLD_MAGIC_GEN,
			Stats.Modifier.WHITE_MAGIC_GEN,
			Stats.Modifier.GREEN_MAGIC_GEN,
			Stats.Modifier.BLUE_MAGIC_GEN,
			Stats.Modifier.HIT_POINT_REGEN,
			Stats.Modifier.ACTION_POINT_REGEN,
			Stats.Modifier.MAGIC_POINT_REGEN);

		costs.put(Stats.Modifier.HIT_POINTS,1000);
		costs.put(Stats.Modifier.ACTION_POINTS,1000);
		costs.put(Stats.Modifier.MAGIC_POINTS,1000);
		costs.put(Stats.Modifier.BRAWN,1000);
		costs.put(Stats.Modifier.SKILL,1000);
		costs.put(Stats.Modifier.THIEVING,1000);
		costs.put(Stats.Modifier.SNEAKING,1000);
		costs.put(Stats.Modifier.BRAINS,1000);
		costs.put(Stats.Modifier.POWER,1000);
		costs.put(Stats.Modifier.SWING,1000);
		costs.put(Stats.Modifier.THRUST,1000);
		costs.put(Stats.Modifier.CUT,1000);
		costs.put(Stats.Modifier.LUNGE,1000);
		costs.put(Stats.Modifier.BASH,1000);
		costs.put(Stats.Modifier.PUNCH,1000);
		costs.put(Stats.Modifier.KICK,1000);
		costs.put(Stats.Modifier.THROW,1000);
		costs.put(Stats.Modifier.SHOOT,1000);
		costs.put(Stats.Modifier.FIRE,1000);
		costs.put(Stats.Modifier.DUAL_WEAPONS,2000);
		costs.put(Stats.Modifier.CHIVALRY,2000);
		costs.put(Stats.Modifier.KENDO,2000);
		costs.put(Stats.Modifier.STREETWISE,1000);
		costs.put(Stats.Modifier.DUNGEONEER,1000);
		costs.put(Stats.Modifier.WILDERNESS_LORE,1000);
		costs.put(Stats.Modifier.SURVIVAL,1000);
		costs.put(Stats.Modifier.BACKSTAB,1000);
		costs.put(Stats.Modifier.SNIPE,1000);
		costs.put(Stats.Modifier.LOCK_AND_TRAP,1000);
		costs.put(Stats.Modifier.STEAL,1000);
		costs.put(Stats.Modifier.MARTIAL_ARTS,2000);
		costs.put(Stats.Modifier.MELEE_CRITICALS,2000);
		costs.put(Stats.Modifier.THROWN_CRITICALS,2000);
		costs.put(Stats.Modifier.RANGED_CRITICALS,2000);
		costs.put(Stats.Modifier.CHANT,1000);
		costs.put(Stats.Modifier.RHYME,1000);
		costs.put(Stats.Modifier.GESTURE,1000);
		costs.put(Stats.Modifier.POSTURE,1000);
		costs.put(Stats.Modifier.THOUGHT,1000);
		costs.put(Stats.Modifier.HERBAL,1000);
		costs.put(Stats.Modifier.ALCHEMIC,1000);
		costs.put(Stats.Modifier.ARTIFACTS,1000);
		costs.put(Stats.Modifier.MYTHOLOGY,1000);
		costs.put(Stats.Modifier.CRAFT,1000);
		costs.put(Stats.Modifier.POWER_CAST,2000);
		costs.put(Stats.Modifier.ENGINEERING,2000);
		costs.put(Stats.Modifier.MUSIC,2000);
		costs.put(Stats.Modifier.INITIATIVE,2000);
		costs.put(Stats.Modifier.ATTACK,1000);
		costs.put(Stats.Modifier.DEFENCE,1000);
		costs.put(Stats.Modifier.DAMAGE,1000);
		costs.put(Stats.Modifier.TO_PENETRATE,1000);
		costs.put(Stats.Modifier.VS_PENETRATE,1000);
		costs.put(Stats.Modifier.VS_AMBUSH,1000);
		costs.put(Stats.Modifier.VS_DODGE,1000);
		costs.put(Stats.Modifier.VS_HIDE,1000);
		costs.put(Stats.Modifier.THREATEN,1000);
		costs.put(Stats.Modifier.TO_BRIBE,1000);
		costs.put(Stats.Modifier.TO_RUN_AWAY,1000);
		costs.put(Stats.Modifier.RESIST_BLUDGEONING,1000);
		costs.put(Stats.Modifier.RESIST_PIERCING,1000);
		costs.put(Stats.Modifier.RESIST_SLASHING,1000);
		costs.put(Stats.Modifier.RESIST_FIRE,1000);
		costs.put(Stats.Modifier.RESIST_WATER,1000);
		costs.put(Stats.Modifier.RESIST_EARTH,1000);
		costs.put(Stats.Modifier.RESIST_AIR,1000);
		costs.put(Stats.Modifier.RESIST_MENTAL,1000);
		costs.put(Stats.Modifier.RESIST_ENERGY,1000);
		costs.put(Stats.Modifier.RED_MAGIC_GEN,3000);
		costs.put(Stats.Modifier.BLACK_MAGIC_GEN,3000);
		costs.put(Stats.Modifier.PURPLE_MAGIC_GEN,3000);
		costs.put(Stats.Modifier.GOLD_MAGIC_GEN,3000);
		costs.put(Stats.Modifier.WHITE_MAGIC_GEN,3000);
		costs.put(Stats.Modifier.GREEN_MAGIC_GEN,3000);
		costs.put(Stats.Modifier.BLUE_MAGIC_GEN,3000);
		costs.put(Stats.Modifier.HIT_POINT_REGEN,2000);
		costs.put(Stats.Modifier.ACTION_POINT_REGEN,2000);
		costs.put(Stats.Modifier.MAGIC_POINT_REGEN,2000);
		costs.put(Stats.Modifier.IMMUNE_TO_DAMAGE,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_HEAT,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_COLD,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_POISON,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_LIGHTNING,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_PSYCHIC,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_ACID,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_BLIND,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_DISEASE,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_FEAR,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_HEX,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_INSANE,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_INVISIBLE,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_IRRITATE,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_KO,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_NAUSEA,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_PARALYSE,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_POSSESSION,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_SILENCE,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_SLEEP,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_STONE,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_SWALLOW,10000);
		costs.put(Stats.Modifier.IMMUNE_TO_WEB,10000);
		costs.put(Stats.Modifier.LIGHT_SLEEPER,10000);
		costs.put(Stats.Modifier.BLIND_FIGHTING,10000);
		costs.put(Stats.Modifier.EXTRA_GOLD,10000);
		costs.put(Stats.Modifier.CHEAT_DEATH,10000);
		costs.put(Stats.Modifier.MAGIC_ABSORPTION,10000);
		costs.put(Stats.Modifier.ARROW_CUTTING,10000);
		costs.put(Stats.Modifier.AMBUSHER,10000);
		costs.put(Stats.Modifier.ENTERTAINER,10000);
		costs.put(Stats.Modifier.DIPLOMAT,10000);
		costs.put(Stats.Modifier.BLINK,10000);
		costs.put(Stats.Modifier.TIRELESS_SWORD,10000);
		costs.put(Stats.Modifier.TIRELESS_AXE,10000);
		costs.put(Stats.Modifier.TIRELESS_SPEAR,10000);
		costs.put(Stats.Modifier.TIRELESS_MACE,10000);
		costs.put(Stats.Modifier.TIRELESS_DAGGER,10000);
		costs.put(Stats.Modifier.TIRELESS_STAFF,10000);
		costs.put(Stats.Modifier.TIRELESS_BOW,10000);
		costs.put(Stats.Modifier.TIRELESS_THROWN,10000);
		costs.put(Stats.Modifier.TIRELESS_UNARMED,10000);
		costs.put(Stats.Modifier.TOUCH_BLIND,1000);
		costs.put(Stats.Modifier.TOUCH_FEAR,1000);
		costs.put(Stats.Modifier.TOUCH_HEX,1000);
		costs.put(Stats.Modifier.TOUCH_INSANE,1000);
		costs.put(Stats.Modifier.TOUCH_IRRITATE,1000);
		costs.put(Stats.Modifier.TOUCH_NAUSEA,1000);
		costs.put(Stats.Modifier.TOUCH_SILENCE,1000);
		costs.put(Stats.Modifier.TOUCH_SLEEP,1000);
		costs.put(Stats.Modifier.TOUCH_STONE,1000);
		costs.put(Stats.Modifier.TOUCH_PARALYSE,1000);
		costs.put(Stats.Modifier.TOUCH_WEB,1000);
		costs.put(Stats.Modifier.TOUCH_DISEASE,1000);
		costs.put(Stats.Modifier.RAZOR_CLOAK,10000);
		costs.put(Stats.Modifier.CC_PENALTY,1000);

		prefices.put(Stats.Modifier.HIT_POINTS,"Vital");
		prefices.put(Stats.Modifier.ACTION_POINTS,"Furtive");
		prefices.put(Stats.Modifier.MAGIC_POINTS,"Potent");
		prefices.put(Stats.Modifier.BRAWN,null);
		prefices.put(Stats.Modifier.SKILL,"Masterwork");
		prefices.put(Stats.Modifier.THIEVING,"Thieves");
		prefices.put(Stats.Modifier.SNEAKING,"Camouflaged");
		prefices.put(Stats.Modifier.BRAINS,"Genius");
		prefices.put(Stats.Modifier.POWER,null);
		prefices.put(Stats.Modifier.SWING,"Slashing");
		prefices.put(Stats.Modifier.THRUST,"Piercing");
		prefices.put(Stats.Modifier.CUT,"Weighted");
		prefices.put(Stats.Modifier.LUNGE,"Balanced");
		prefices.put(Stats.Modifier.BASH,"Heavy");
		prefices.put(Stats.Modifier.PUNCH,"Open Hand");
		prefices.put(Stats.Modifier.KICK,"Tornado");
		prefices.put(Stats.Modifier.THROW,"Accurate");
		prefices.put(Stats.Modifier.SHOOT,"Archer's");
		prefices.put(Stats.Modifier.FIRE,"Deadeye");
		prefices.put(Stats.Modifier.DUAL_WEAPONS,"Ranger's");
		prefices.put(Stats.Modifier.CHIVALRY,"Knight's");
		prefices.put(Stats.Modifier.KENDO,"Ronin's");
		prefices.put(Stats.Modifier.STREETWISE,null);
		prefices.put(Stats.Modifier.DUNGEONEER,"Adventurer's");
		prefices.put(Stats.Modifier.WILDERNESS_LORE,"Hunter's");
		prefices.put(Stats.Modifier.SURVIVAL,null);
		prefices.put(Stats.Modifier.BACKSTAB,"Backstabbing");
		prefices.put(Stats.Modifier.SNIPE,"Sniper's");
		prefices.put(Stats.Modifier.LOCK_AND_TRAP,null);
		prefices.put(Stats.Modifier.STEAL,null);
		prefices.put(Stats.Modifier.MARTIAL_ARTS,"Monk's");
		prefices.put(Stats.Modifier.MELEE_CRITICALS,"Kirijutsu");
		prefices.put(Stats.Modifier.THROWN_CRITICALS,"Deadly");
		prefices.put(Stats.Modifier.RANGED_CRITICALS,"Headshot");
		prefices.put(Stats.Modifier.CHANT,"Priest's");
		prefices.put(Stats.Modifier.RHYME,"Poet's");
		prefices.put(Stats.Modifier.GESTURE,"Somatic");
		prefices.put(Stats.Modifier.POSTURE,null);
		prefices.put(Stats.Modifier.THOUGHT,"Mental");
		prefices.put(Stats.Modifier.HERBAL,"Druid's");
		prefices.put(Stats.Modifier.ALCHEMIC,"Immortal");
		prefices.put(Stats.Modifier.ARTIFACTS,"Sage");
		prefices.put(Stats.Modifier.MYTHOLOGY,"Bard's");
		prefices.put(Stats.Modifier.CRAFT,null);
		prefices.put(Stats.Modifier.POWER_CAST,null);
		prefices.put(Stats.Modifier.ENGINEERING,"Tinker's");
		prefices.put(Stats.Modifier.MUSIC,null);
		prefices.put(Stats.Modifier.INITIATIVE,"Quick");
		prefices.put(Stats.Modifier.ATTACK,"Keen");
		prefices.put(Stats.Modifier.DEFENCE,null);
		prefices.put(Stats.Modifier.DAMAGE,"Tempered");
		prefices.put(Stats.Modifier.TO_PENETRATE,"Piercing");
		prefices.put(Stats.Modifier.VS_PENETRATE,"Steady");
		prefices.put(Stats.Modifier.VS_AMBUSH,null);
		prefices.put(Stats.Modifier.VS_DODGE,"True Striking");
		prefices.put(Stats.Modifier.VS_HIDE,"True Seeing");
		prefices.put(Stats.Modifier.THREATEN,"Intimidating");
		prefices.put(Stats.Modifier.TO_BRIBE,"Corrupt");
		prefices.put(Stats.Modifier.TO_RUN_AWAY,"Coward's");
		prefices.put(Stats.Modifier.RESIST_BLUDGEONING,"Moonstone");
		prefices.put(Stats.Modifier.RESIST_PIERCING,"Quartz");
		prefices.put(Stats.Modifier.RESIST_SLASHING,"Diamond");
		prefices.put(Stats.Modifier.RESIST_FIRE,"Ruby");
		prefices.put(Stats.Modifier.RESIST_WATER,"Sapphire");
		prefices.put(Stats.Modifier.RESIST_EARTH,"Emerald");
		prefices.put(Stats.Modifier.RESIST_AIR,"Topaz");
		prefices.put(Stats.Modifier.RESIST_MENTAL,"Crystal");
		prefices.put(Stats.Modifier.RESIST_ENERGY,"Opal");
		prefices.put(Stats.Modifier.RED_MAGIC_GEN,null);
		prefices.put(Stats.Modifier.BLACK_MAGIC_GEN,"Unholy");
		prefices.put(Stats.Modifier.PURPLE_MAGIC_GEN,null);
		prefices.put(Stats.Modifier.GOLD_MAGIC_GEN,null);
		prefices.put(Stats.Modifier.WHITE_MAGIC_GEN,"Holy");
		prefices.put(Stats.Modifier.GREEN_MAGIC_GEN,null);
		prefices.put(Stats.Modifier.BLUE_MAGIC_GEN,null);
		prefices.put(Stats.Modifier.HIT_POINT_REGEN,null);
		prefices.put(Stats.Modifier.ACTION_POINT_REGEN,null);
		prefices.put(Stats.Modifier.MAGIC_POINT_REGEN,null);
		prefices.put(Stats.Modifier.IMMUNE_TO_DAMAGE,"Achilles'");
		prefices.put(Stats.Modifier.IMMUNE_TO_HEAT,"Dragon");
		prefices.put(Stats.Modifier.IMMUNE_TO_COLD,"Wyrm");
		prefices.put(Stats.Modifier.IMMUNE_TO_POISON,"Snake");
		prefices.put(Stats.Modifier.IMMUNE_TO_LIGHTNING,"Thor's");
		prefices.put(Stats.Modifier.IMMUNE_TO_PSYCHIC,"Mindflayer");
		prefices.put(Stats.Modifier.IMMUNE_TO_ACID,"Corroded");
		prefices.put(Stats.Modifier.IMMUNE_TO_BLIND,null);
		prefices.put(Stats.Modifier.IMMUNE_TO_DISEASE,"Immortal");
		prefices.put(Stats.Modifier.IMMUNE_TO_FEAR,"Fearless");
		prefices.put(Stats.Modifier.IMMUNE_TO_HEX,"Teflon");
		prefices.put(Stats.Modifier.IMMUNE_TO_INSANE,null);
		prefices.put(Stats.Modifier.IMMUNE_TO_INVISIBLE,"Amber");
		prefices.put(Stats.Modifier.IMMUNE_TO_IRRITATE,"Rhino");
		prefices.put(Stats.Modifier.IMMUNE_TO_KO,"Bull");
		prefices.put(Stats.Modifier.IMMUNE_TO_NAUSEA,"Amethyst");
		prefices.put(Stats.Modifier.IMMUNE_TO_PARALYSE,"Agate");
		prefices.put(Stats.Modifier.IMMUNE_TO_POSSESSION,"Azurite");
		prefices.put(Stats.Modifier.IMMUNE_TO_SILENCE,"Magician's");
		prefices.put(Stats.Modifier.IMMUNE_TO_SLEEP,"Citrine");
		prefices.put(Stats.Modifier.IMMUNE_TO_STONE,"Medusa's");
		prefices.put(Stats.Modifier.IMMUNE_TO_SWALLOW,"Onyx");
		prefices.put(Stats.Modifier.IMMUNE_TO_WEB,"Turquoise");
		prefices.put(Stats.Modifier.LIGHT_SLEEPER,null);
		prefices.put(Stats.Modifier.BLIND_FIGHTING,null);
		prefices.put(Stats.Modifier.EXTRA_GOLD,null);
		prefices.put(Stats.Modifier.CHEAT_DEATH,"Hero's");
		prefices.put(Stats.Modifier.MAGIC_ABSORPTION,"Channeling");
		prefices.put(Stats.Modifier.ARROW_CUTTING,null);
		prefices.put(Stats.Modifier.AMBUSHER,null);
		prefices.put(Stats.Modifier.ENTERTAINER,"Storytelling");
		prefices.put(Stats.Modifier.DIPLOMAT,"Diplomat's");
		prefices.put(Stats.Modifier.BLINK,"Phasing");
		prefices.put(Stats.Modifier.TIRELESS_SWORD,"Relentless");
		prefices.put(Stats.Modifier.TIRELESS_AXE,"Beserker's");
		prefices.put(Stats.Modifier.TIRELESS_SPEAR,"Amazon's");
		prefices.put(Stats.Modifier.TIRELESS_MACE,"Bludgeoning");
		prefices.put(Stats.Modifier.TIRELESS_DAGGER,"Bloody");
		prefices.put(Stats.Modifier.TIRELESS_STAFF,"Whirling");
		prefices.put(Stats.Modifier.TIRELESS_BOW,"Darkening");
		prefices.put(Stats.Modifier.TIRELESS_THROWN,"Fusillade");
		prefices.put(Stats.Modifier.TIRELESS_UNARMED,"Ninja's");
		prefices.put(Stats.Modifier.TOUCH_BLIND,"Blinding");
		prefices.put(Stats.Modifier.TOUCH_FEAR,"Fearsome");
		prefices.put(Stats.Modifier.TOUCH_HEX,"Hexing");
		prefices.put(Stats.Modifier.TOUCH_INSANE,"Confusing");
		prefices.put(Stats.Modifier.TOUCH_IRRITATE,"Irritating");
		prefices.put(Stats.Modifier.TOUCH_NAUSEA,"Nauseating");
		prefices.put(Stats.Modifier.TOUCH_SILENCE,"Silencing");
		prefices.put(Stats.Modifier.TOUCH_SLEEP,"Drowsy");
		prefices.put(Stats.Modifier.TOUCH_STONE,"Petrifying");
		prefices.put(Stats.Modifier.TOUCH_PARALYSE,"Paralysing");
		prefices.put(Stats.Modifier.TOUCH_WEB,"Entangling");
		prefices.put(Stats.Modifier.TOUCH_DISEASE,"Corrupting");
		prefices.put(Stats.Modifier.RAZOR_CLOAK,null);
		prefices.put(Stats.Modifier.CC_PENALTY,null);

		suffices.put(Stats.Modifier.HIT_POINTS,null);
		suffices.put(Stats.Modifier.ACTION_POINTS,null);
		suffices.put(Stats.Modifier.MAGIC_POINTS,null);
		suffices.put(Stats.Modifier.BRAWN,"Of Strength");
		suffices.put(Stats.Modifier.SKILL,null);
		suffices.put(Stats.Modifier.THIEVING,null);
		suffices.put(Stats.Modifier.SNEAKING,null);
		suffices.put(Stats.Modifier.BRAINS,null);
		suffices.put(Stats.Modifier.POWER,"Of Potency");
		suffices.put(Stats.Modifier.SWING,null);
		suffices.put(Stats.Modifier.THRUST,null);
		suffices.put(Stats.Modifier.CUT,null);
		suffices.put(Stats.Modifier.LUNGE,null);
		suffices.put(Stats.Modifier.BASH,null);
		suffices.put(Stats.Modifier.PUNCH,null);
		suffices.put(Stats.Modifier.KICK,null);
		suffices.put(Stats.Modifier.THROW,null);
		suffices.put(Stats.Modifier.SHOOT,null);
		suffices.put(Stats.Modifier.FIRE,null);
		suffices.put(Stats.Modifier.DUAL_WEAPONS,null);
		suffices.put(Stats.Modifier.CHIVALRY,null);
		suffices.put(Stats.Modifier.KENDO,null);
		suffices.put(Stats.Modifier.STREETWISE,"Of Disguise");
		suffices.put(Stats.Modifier.DUNGEONEER,null);
		suffices.put(Stats.Modifier.WILDERNESS_LORE,null);
		suffices.put(Stats.Modifier.SURVIVAL,"Of Survival");
		suffices.put(Stats.Modifier.BACKSTAB,null);
		suffices.put(Stats.Modifier.SNIPE,null);
		suffices.put(Stats.Modifier.LOCK_AND_TRAP,"Of Looting");
		suffices.put(Stats.Modifier.STEAL,"Of Theft");
		suffices.put(Stats.Modifier.MARTIAL_ARTS,null);
		suffices.put(Stats.Modifier.MELEE_CRITICALS,null);
		suffices.put(Stats.Modifier.THROWN_CRITICALS,null);
		suffices.put(Stats.Modifier.RANGED_CRITICALS,null);
		suffices.put(Stats.Modifier.CHANT,null);
		suffices.put(Stats.Modifier.RHYME,null);
		suffices.put(Stats.Modifier.GESTURE,null);
		suffices.put(Stats.Modifier.POSTURE,"Of Stance");
		suffices.put(Stats.Modifier.THOUGHT,null);
		suffices.put(Stats.Modifier.HERBAL,null);
		suffices.put(Stats.Modifier.ALCHEMIC,null);
		suffices.put(Stats.Modifier.ARTIFACTS,null);
		suffices.put(Stats.Modifier.MYTHOLOGY,null);
		suffices.put(Stats.Modifier.CRAFT,"Of Engineering");
		suffices.put(Stats.Modifier.POWER_CAST,"Of Domination");
		suffices.put(Stats.Modifier.ENGINEERING,null);
		suffices.put(Stats.Modifier.MUSIC,"Of Melody");
		suffices.put(Stats.Modifier.INITIATIVE,null);
		suffices.put(Stats.Modifier.ATTACK,null);
		suffices.put(Stats.Modifier.DEFENCE,"Of Parrying");
		suffices.put(Stats.Modifier.DAMAGE,null);
		suffices.put(Stats.Modifier.TO_PENETRATE,null);
		suffices.put(Stats.Modifier.VS_PENETRATE,null);
		suffices.put(Stats.Modifier.VS_AMBUSH,"Of Alertness");
		suffices.put(Stats.Modifier.VS_DODGE,null);
		suffices.put(Stats.Modifier.VS_HIDE,null);
		suffices.put(Stats.Modifier.THREATEN,null);
		suffices.put(Stats.Modifier.TO_BRIBE,null);
		suffices.put(Stats.Modifier.TO_RUN_AWAY,null);
		suffices.put(Stats.Modifier.RESIST_BLUDGEONING,null);
		suffices.put(Stats.Modifier.RESIST_PIERCING,null);
		suffices.put(Stats.Modifier.RESIST_SLASHING,null);
		suffices.put(Stats.Modifier.RESIST_FIRE,null);
		suffices.put(Stats.Modifier.RESIST_WATER,null);
		suffices.put(Stats.Modifier.RESIST_EARTH,null);
		suffices.put(Stats.Modifier.RESIST_AIR,null);
		suffices.put(Stats.Modifier.RESIST_MENTAL,null);
		suffices.put(Stats.Modifier.RESIST_ENERGY,null);
		suffices.put(Stats.Modifier.RED_MAGIC_GEN,"Of Sorcery");
		suffices.put(Stats.Modifier.BLACK_MAGIC_GEN,null);
		suffices.put(Stats.Modifier.PURPLE_MAGIC_GEN,"Of Witchcraft");
		suffices.put(Stats.Modifier.GOLD_MAGIC_GEN,"Of Enchantment");
		suffices.put(Stats.Modifier.WHITE_MAGIC_GEN,null);
		suffices.put(Stats.Modifier.GREEN_MAGIC_GEN,"Of Druidism");
		suffices.put(Stats.Modifier.BLUE_MAGIC_GEN,"Of The Elements");
		suffices.put(Stats.Modifier.HIT_POINT_REGEN,"Of Healing");
		suffices.put(Stats.Modifier.ACTION_POINT_REGEN,"Of Hiding");
		suffices.put(Stats.Modifier.MAGIC_POINT_REGEN,"Of Mana");
		suffices.put(Stats.Modifier.IMMUNE_TO_DAMAGE,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_HEAT,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_COLD,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_POISON,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_LIGHTNING,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_PSYCHIC,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_ACID,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_BLIND,"Of Vision");
		suffices.put(Stats.Modifier.IMMUNE_TO_DISEASE,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_FEAR,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_HEX,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_INSANE,"Of Logic");
		suffices.put(Stats.Modifier.IMMUNE_TO_INVISIBLE,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_IRRITATE,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_KO,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_NAUSEA,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_PARALYSE,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_POSSESSION,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_SILENCE,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_SLEEP,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_STONE,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_SWALLOW,null);
		suffices.put(Stats.Modifier.IMMUNE_TO_WEB,null);
		suffices.put(Stats.Modifier.LIGHT_SLEEPER,"Of Watching");
		suffices.put(Stats.Modifier.BLIND_FIGHTING,"Of Killing");
		suffices.put(Stats.Modifier.EXTRA_GOLD,"Of Wealth");
		suffices.put(Stats.Modifier.CHEAT_DEATH,null);
		suffices.put(Stats.Modifier.MAGIC_ABSORPTION,null);
		suffices.put(Stats.Modifier.ARROW_CUTTING,"Of Deflection");
		suffices.put(Stats.Modifier.AMBUSHER,"Of Murder");
		suffices.put(Stats.Modifier.ENTERTAINER,null);
		suffices.put(Stats.Modifier.DIPLOMAT,null);
		suffices.put(Stats.Modifier.BLINK,null);
		suffices.put(Stats.Modifier.TIRELESS_SWORD,null);
		suffices.put(Stats.Modifier.TIRELESS_AXE,null);
		suffices.put(Stats.Modifier.TIRELESS_SPEAR,null);
		suffices.put(Stats.Modifier.TIRELESS_MACE,null);
		suffices.put(Stats.Modifier.TIRELESS_DAGGER,null);
		suffices.put(Stats.Modifier.TIRELESS_STAFF,null);
		suffices.put(Stats.Modifier.TIRELESS_BOW,null);
		suffices.put(Stats.Modifier.TIRELESS_THROWN,null);
		suffices.put(Stats.Modifier.TIRELESS_UNARMED,null);
		suffices.put(Stats.Modifier.TOUCH_BLIND,null);
		suffices.put(Stats.Modifier.TOUCH_FEAR,null);
		suffices.put(Stats.Modifier.TOUCH_HEX,null);
		suffices.put(Stats.Modifier.TOUCH_INSANE,null);
		suffices.put(Stats.Modifier.TOUCH_IRRITATE,null);
		suffices.put(Stats.Modifier.TOUCH_NAUSEA,null);
		suffices.put(Stats.Modifier.TOUCH_SILENCE,null);
		suffices.put(Stats.Modifier.TOUCH_SLEEP,null);
		suffices.put(Stats.Modifier.TOUCH_STONE,null);
		suffices.put(Stats.Modifier.TOUCH_PARALYSE,null);
		suffices.put(Stats.Modifier.TOUCH_WEB,null);
		suffices.put(Stats.Modifier.TOUCH_DISEASE,null);
		suffices.put(Stats.Modifier.RAZOR_CLOAK,"Of Razors");
		suffices.put(Stats.Modifier.CC_PENALTY,"Of Weight");
	}

	static void setModifiers(StatModifier sm, Stats.Modifier... modifiers)
	{
		for (Stats.Modifier mod : modifiers)
		{
			sm.setModifier(mod,1);
		}
	}
}