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

package mclachlan.maze.data.v1;

import java.math.BigInteger;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.util.MazeException;
import static mclachlan.maze.stat.Stats.Modifier.*;

/**
 *
 */
public class V1StatModifier
{
	private static final int MAX_MODIFIER = Byte.MAX_VALUE;
	private static final int MIN_MODIFIER = Byte.MIN_VALUE;
	private static final String DEFAULT_SEPARATOR = ",";

	/*-------------------------------------------------------------------------*/
	public static String toString(StatModifier sm)
	{
		return toString(sm, DEFAULT_SEPARATOR);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(StatModifier sm, String separator)
	{
		if (sm == null)
		{
			return "";
		}

		BigInteger bi = BigInteger.valueOf(0);
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < INDEX.length; i++)
		{
			int modifier = (INDEX[i] instanceof Stats.Modifier) ? sm.getModifier((Stats.Modifier)INDEX[i]) : 0;
			if (modifier != 0)
			{
				if (modifier > MAX_MODIFIER || modifier < MIN_MODIFIER)
				{
					throw new MazeException("modifier ["+INDEX[i]+"] out of bounds: "+modifier);
				}

				// inefficient
				bi = bi.setBit(i);
				String hex = Integer.toHexString(modifier);
				if (hex.length() == 1)
				{
					buffer.append('0');
				}
				else if (hex.length() > 2)
				{
					// a negative number: just take the last two hex characters.
					// this does of course limit us to -128..127
					hex = hex.substring(hex.length()-2, hex.length());
				}
				buffer.append(hex);
			}
		}

		buffer.insert(0, toString(bi)+separator);

		return buffer.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static StatModifier fromString(String s)
	{
		return fromString(s, DEFAULT_SEPARATOR);
	}

	/*-------------------------------------------------------------------------*/
	public static StatModifier fromString(String s, String separator)
	{
		if (s.equals(""))
		{
			return null;
		}
		
		StatModifier result = new StatModifier();
		String[] strs = s.split(separator);
		BigInteger bi = new BigInteger(strs[0], 16);

		int counter = 0;
		for (int i=0; i<INDEX.length; i++)
		{
			if (bi.testBit(i))
			{
				if (INDEX[i] instanceof Stats.Modifier)
				{
					String modifier = strs[1].substring(counter, counter + 2);
					result.setModifier((Stats.Modifier)INDEX[i], (byte)(Integer.parseInt(modifier, 16)));
				}
				counter += 2;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	static String toString(BigInteger bi)
	{
		return bi.toString(16);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * for testing only
	 */
	public static void main(String[] args)
	{
		StatModifier sm = new StatModifier();
		sm.setModifier(BRAWN, 127);

		String s = toString(sm);
		System.out.println("s = [" + s + "]");

		StatModifier test = fromString(s);
		boolean equals = test.equals(sm);
		System.out.println("equals = [" + equals + "]");

//		for (int i = 0; i < INDEX.length; i++)
//		{
//			sm.setModifier(INDEX[i], 1);
//		}
//		s = toString(sm);
//		System.out.println("s = [" + s + "]");
//
//		test = fromString(s);
//		equals = test.equals(sm);
//		System.out.println("equals = [" + equals + "]");
	}

	/*-------------------------------------------------------------------------*/
	// todo: hackery!
	public static final Object[] INDEX =
		{
			HIT_POINTS,
			ACTION_POINTS,
			MAGIC_POINTS,
			BRAWN,
			SKILL,
			THIEVING,
			SNEAKING,
			BRAINS,
			POWER,
			SWING,
			THRUST,
			CUT,
			LUNGE,
			BASH,
			PUNCH,
			KICK,
			THROW,
			SHOOT,
			FIRE,
			"reserved1",
			DUAL_WEAPONS,
			CHIVALRY,
			KENDO,
			STREETWISE,
			DUNGEONEER,
			WILDERNESS_LORE,
			SURVIVAL,
			BACKSTAB,
			SNIPE,
			LOCK_AND_TRAP,
			STEAL,
			MARTIAL_ARTS,
			MELEE_CRITICALS,
			THROWN_CRITICALS,
			RANGED_CRITICALS,
			SCOUTING,
			"reserved3",
			CHANT,
			RHYME,
			GESTURE,
			POSTURE,
			THOUGHT,
			HERBAL,
			ALCHEMIC,
			ARTIFACTS,
			MYTHOLOGY,
			CRAFT,
			POWER_CAST,
			RESIST_BLUDGEONING,
			RESIST_PIERCING,
			RESIST_SLASHING,
			INITIATIVE,
			ATTACK,
			"reserved15",
			DEFENCE,
			DAMAGE,
			TO_PENETRATE,
			VS_PENETRATE,
			VS_AMBUSH,
			VS_DODGE,
			VS_HIDE,
			"reserved16",
			TO_BRIBE,
			TO_RUN_AWAY,
			"reserved14",
			RESIST_FIRE,
			RESIST_WATER,
			RESIST_EARTH,
			RESIST_AIR,
			RESIST_MENTAL,
			RESIST_ENERGY,
			RED_MAGIC_GEN,
			BLACK_MAGIC_GEN,
			PURPLE_MAGIC_GEN,
			GOLD_MAGIC_GEN,
			WHITE_MAGIC_GEN,
			GREEN_MAGIC_GEN,
			BLUE_MAGIC_GEN,
			HIT_POINT_REGEN,
			ACTION_POINT_REGEN,
			MAGIC_POINT_REGEN,
			ENGINEERING,
			MUSIC,
			"reserved9",
			"reserved10",
			"reserved11",
			"reserved12",
			"reserved13",
			IMMUNE_TO_DAMAGE,
			IMMUNE_TO_HEAT,
			IMMUNE_TO_COLD,
			IMMUNE_TO_POISON,
			IMMUNE_TO_LIGHTNING,
			IMMUNE_TO_PSYCHIC,
			IMMUNE_TO_ACID,
			IMMUNE_TO_BLIND,
			IMMUNE_TO_DISEASE,
			IMMUNE_TO_FEAR,
			IMMUNE_TO_HEX,
			IMMUNE_TO_INSANE,
			IMMUNE_TO_INVISIBLE,
			IMMUNE_TO_IRRITATE,
			IMMUNE_TO_KO,
			IMMUNE_TO_NAUSEA,
			IMMUNE_TO_PARALYSE,
			IMMUNE_TO_POSSESSION,
			IMMUNE_TO_SILENCE,
			IMMUNE_TO_SLEEP,
			IMMUNE_TO_STONE,
			IMMUNE_TO_SWALLOW,
			IMMUNE_TO_WEB,
			LIGHT_SLEEPER,
			BLIND_FIGHTING,
			EXTRA_GOLD,
			CHEAT_DEATH,
			MAGIC_ABSORPTION,
			ARROW_CUTTING,
			AMBUSHER,
			ENTERTAINER,
			DIPLOMAT,
			BLINK,
			TIRELESS_AXE,
			TIRELESS_BOW,
			TIRELESS_DAGGER,
			TIRELESS_MACE,
			TIRELESS_SPEAR,
			TIRELESS_STAFF,
			TIRELESS_SWORD,
			TIRELESS_THROWN,
			TIRELESS_UNARMED,
			TOUCH_BLIND,
			TOUCH_DISEASE,
			TOUCH_FEAR,
			TOUCH_HEX,
			TOUCH_INSANE,
			TOUCH_IRRITATE,
			TOUCH_NAUSEA,
			TOUCH_PARALYSE,
			TOUCH_SILENCE,
			TOUCH_SLEEP,
			TOUCH_STONE,
			TOUCH_WEB,
			RAZOR_CLOAK,
			CC_PENALTY,
			STAMINA_REGEN,
			DAMAGE_MULTIPLIER,
			LIGHTNING_STRIKE_AXE,
			LIGHTNING_STRIKE_DAGGER,
			LIGHTNING_STRIKE_MACE,
			LIGHTNING_STRIKE_SPEAR,
			LIGHTNING_STRIKE_STAFF,
			LIGHTNING_STRIKE_SWORD,
			LIGHTNING_STRIKE_UNARMED,
			BERSERKER,
			DEADLY_STRIKE,
			DODGE,
			MASTER_ARCHER,
			DIVINE_PROTECTION,
			KI_FURY,
			FEY_AFFINITY,
			ARCANE_BLOOD,
			DISPLACER,
			PARRY,
			MELEE_MASTER,
			DEADLY_AIM,
			MASTER_THIEF,
			TOUCH_POISON,
			OBFUSCATION,
			SHADOW_MASTER,
			CHARMED_DESTINY,
			CHANNELLING,
			SIGNATURE_WEAPON_ENGINEERING,
			AMPHIBIOUS,
			BONUS_ATTACKS,
			BONUS_STRIKES,
			SORCERY_SPELLS,
			BLACK_MAGIC_SPELLS,
			WITCHCRAFT_SPELLS,
			ENCHANTMENT_SPELLS,
			WHITE_MAGIC_SPELLS,
			DRUIDISM_SPELLS,
			ELEMENTAL_SPELLS,
			LARGE_SIZE,
			THREATEN,
			DRINKING_FIT,
			IAJUTSU,
			FAVOURED_ENEMY_BEAST,        
			FAVOURED_ENEMY_CONSTRUCT,    
			FAVOURED_ENEMY_MAZE_CREATURE,
			FAVOURED_ENEMY_CRYPTOBESTIA, 
			FAVOURED_ENEMY_CONSTRUCT,    
			FAVOURED_ENEMY_DRAGON,       
			FAVOURED_ENEMY_ELEMENTAL,    
			FAVOURED_ENEMY_FEY,          
			FAVOURED_ENEMY_GIANT,        
			FAVOURED_ENEMY_HORROR,       
			FAVOURED_ENEMY_HUMANOID,     
			FAVOURED_ENEMY_ILLUSION,     
			FAVOURED_ENEMY_MONSTROSITY,  
			FAVOURED_ENEMY_OOZE,         
			FAVOURED_ENEMY_OUTSIDER,     
			FAVOURED_ENEMY_PLANT,        
			FAVOURED_ENEMY_UNDEAD,       
			FAVOURED_ENEMY_VERMIN,       
			POWER_OF_DARKNESS,
			FLIER,
			STRONG_SWIMMER,
			FURIOUS_PURPOSE,
			SWORD_PARRY,
			AXE_PARRY,
			MACE_PARRY,
			POLEARM_PARRY,
			STAFF_PARRY,
			AMAZON_COURAGE,
			AMAZON_WILLPOWER,
			SWORD_1H_WIELD,
			AXE_1H_WIELD,
			MACE_1H_WIELD,
			POLEARM_1H_WIELD,
			STAFF_1H_WIELD,
			AMAZON_FURY,
			BERSERK_POWERS
		};
}
