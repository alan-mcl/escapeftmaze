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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Gender;
import mclachlan.maze.stat.Race;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class MagicSys
{
	public static final int MAX_CASTING_LEVEL = 7;
	public static final int MAX_SPELL_LEVEL = 10;

	public static MagicSys getInstance()
	{
		return Maze.getInstance().getMagicSys();
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A list of the names of all spell books in this magic system.
	 */
	public List<MagicSys.SpellBook> getSpellBooks()
	{
		return new ArrayList<SpellBook>(SpellBook.spellBooks.values());
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A list of the names of all spell types in this magic system.
	 */
	public List<String> getSpellSchools()
	{
		return SpellSchool.spellSchools;
	}

	/*-------------------------------------------------------------------------*/
	public int getMagicPointCost(Spell spell, int castingLevel, UnifiedActor caster)
	{
		return spell.getCastingCost()*castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	public static class SpellBook
	{
		private String name;
		private String displayNameKey;
		private String castingAbilityModifier;
		private Set<String> genders;
		private Set<String> races;

		private static final HashSet<String> MALE = new HashSet<String>();
		private static final HashSet<String> FEMALE = new HashSet<String>();

		public static final SpellBook SORCERY =
			new SpellBook("Sorcery", "sorcery", Stats.Modifiers.SORCERY_SPELLS, MALE, null);
		public static final SpellBook BLACK_MAGIC =
			new SpellBook("Black Magic", "black_magic", Stats.Modifiers.BLACK_MAGIC_SPELLS, null, null);
		public static final SpellBook WITCHCRAFT =
			new SpellBook("Witchcraft", "witchcraft", Stats.Modifiers.WITCHCRAFT_SPELLS, FEMALE, null);
		public static final SpellBook ENCHANTMENT =
			new SpellBook("Enchantment", "enchantment", Stats.Modifiers.ENCHANTMENT_SPELLS, null, null);
		public static final SpellBook WHITE_MAGIC =
			new SpellBook("White Magic", "white_magic", Stats.Modifiers.WHITE_MAGIC_SPELLS, null, null);
		public static final SpellBook DRUIDISM =
			new SpellBook("Druidism", "druidism", Stats.Modifiers.DRUIDISM_SPELLS, null, null);
		public static final SpellBook ELEMENTALISM =
			new SpellBook("Elementalism", "elementalism", Stats.Modifiers.ELEMENTAL_SPELLS, null, null);

		private static Map<String, SpellBook> spellBooks = new HashMap<String, SpellBook>();

		static
		{
			spellBooks.put(SORCERY.getName(), SORCERY);
			spellBooks.put(BLACK_MAGIC.getName(), BLACK_MAGIC);
			spellBooks.put(WITCHCRAFT.getName(), WITCHCRAFT);
			spellBooks.put(ENCHANTMENT.getName(), ENCHANTMENT);
			spellBooks.put(WHITE_MAGIC.getName(), WHITE_MAGIC);
			spellBooks.put(DRUIDISM.getName(), DRUIDISM);
			spellBooks.put(ELEMENTALISM.getName(), ELEMENTALISM);

			MALE.add("Male");
			FEMALE.add("Female");
		}

		/*----------------------------------------------------------------------*/
		public SpellBook(String name,
			String displayNameKey,
			String castingAbilityModifier,
			Set<String> genders,
			Set<String> races)
		{
			this.name = name;
			this.displayNameKey = displayNameKey;
			this.castingAbilityModifier = castingAbilityModifier;
			this.genders = genders;
			this.races = races;
		}

		/*----------------------------------------------------------------------*/
		public boolean isAllowedRace(Race race)
		{
			return races == null || races.contains(race.getName());
		}

		/*----------------------------------------------------------------------*/
		public boolean isAllowedGender(Gender gender)
		{
			return genders == null || genders.contains(gender.getName());
		}

		/*----------------------------------------------------------------------*/
		public String getName()
		{
			return name;
		}

		/*-------------------------------------------------------------------------*/
		public String getDisplayNameKey()
		{
			return displayNameKey;
		}

		/*----------------------------------------------------------------------*/
		public static SpellBook valueOf(String name)
		{
			return spellBooks.get(name);
		}

		/*----------------------------------------------------------------------*/
		public static List<SpellBook> getAllBooks()
		{
			return new ArrayList<SpellBook>(spellBooks.values());
		}

		/*-------------------------------------------------------------------------*/
		public String getCastingAbilityModifier()
		{
			return castingAbilityModifier;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class SpellSchool
	{
		public static final String BLESSING = "Blessing";
		public static final String CURSE = "Curse";
		public static final String CONJURATION = "Conjuration";
		public static final String EVOCATION = "Evocation";
		public static final String ILLUSION = "Illusion";
		public static final String TRANSMUTATION = "Transmutation";
		public static final String BEGUILMENT = "Beguilement";

		static List<String> spellSchools = new ArrayList<String>();

		static
		{
			spellSchools.add(BLESSING);
			spellSchools.add(CURSE);
			spellSchools.add(CONJURATION);
			spellSchools.add(EVOCATION);
			spellSchools.add(ILLUSION);
			spellSchools.add(TRANSMUTATION);
			spellSchools.add(BEGUILMENT);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class SpellTargetType
	{
		public static final int CASTER = 0;
		public static final int ALLY = 1;
		public static final int PARTY = 2;
		public static final int FOE = 3;
		public static final int FOE_GROUP = 4;
		public static final int ALL_FOES = 5;
		public static final int TILE = 6;
		public static final int LOCK_OR_TRAP = 7;
		public static final int NPC = 8;
		public static final int ITEM = 9;
		public static final int CLOUD_ONE_GROUP = 10;
		public static final int CLOUD_ALL_GROUPS = 11;

		public static final int MAX = 12;

		/*----------------------------------------------------------------------*/
		public static String describe(int targetType)
		{
			switch (targetType)
			{
				case CASTER: return "caster";
				case ALLY: return "ally";
				case PARTY: return "party";
				case FOE: return "one foe";
				case FOE_GROUP: return "one group of foes";
				case ALL_FOES: return "all foes";
				case TILE: return "current tile";
				case LOCK_OR_TRAP: return "lock or trap";
				case NPC: return "NPC";
				case ITEM: return "item";
				case CLOUD_ONE_GROUP: return "cloud: one group";
				case CLOUD_ALL_GROUPS: return "cloud: all groups";
				default: throw new MazeException("Invalid target type: "+targetType);
			}
		}

		/*----------------------------------------------------------------------*/
		public int valueOf(String s)
		{
			if (s.equals("caster")) { return CASTER; }
			else if (s.equals("ally")) { return ALLY; }
			else if (s.equals("party")) { return PARTY; }
			else if (s.equals("one foe")) { return FOE; }
			else if (s.equals("foe group")) { return FOE_GROUP; }
			else if (s.equals("all foes")) { return ALL_FOES; }
			else if (s.equals("current tile")) { return TILE; }
			else if (s.equals("NPC")) { return NPC; }
			else if (s.equals("item")) { return ITEM; }
			else if (s.equals("cloud: one group")) { return CLOUD_ONE_GROUP; }
			else if (s.equals("cloud: all groups")) { return CLOUD_ALL_GROUPS; }
			else
			{
				throw new MazeException("Invalid spell target type ["+s+"]");
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class SpellEffectType
	{
		public static final int NONE = 0;
		public static final int FIRE = 1;
		public static final int WATER = 2;
		public static final int EARTH = 3;
		public static final int AIR = 4;
		public static final int MENTAL = 5;
		public static final int ENERGY = 6;
		public static final int BLUDGEONING = 7;
		public static final int PIERCING = 8;
		public static final int SLASHING = 9;

		public static final int MAX = 10;

		/*----------------------------------------------------------------------*/
		public static String describe(int effectType)
		{
			switch (effectType)
			{
				case NONE: return "none";
				case FIRE: return "fire";
				case WATER: return "water";
				case EARTH: return "earth";
				case AIR: return "air";
				case MENTAL: return "mental";
				case ENERGY: return "energy";
				case BLUDGEONING: return "bludgeoning";
				case PIERCING: return "piercing";
				case SLASHING: return "slashing";
				default: throw new MazeException("Invalid effect type: "+effectType);
			}
		}

		/*----------------------------------------------------------------------*/
		public static int valueOf(String s)
		{
			if (s.equals("none")) { return NONE; }
			else if (s.equals("bludgeoning")) {return BLUDGEONING; }
			else if (s.equals("piercing")) {return PIERCING; }
			else if (s.equals("slashing")) {return SLASHING; }
			else if (s.equals("fire")) { return FIRE; }
			else if (s.equals("water")) { return WATER; }
			else if (s.equals("earth")) { return EARTH; }
			else if (s.equals("air")) { return AIR; }
			else if (s.equals("mental")) { return MENTAL; }
			else if (s.equals("energy")) { return ENERGY; }
			else
			{
				throw new MazeException("Invalid spell effect type ["+s+"]");
			}
		}

		/*----------------------------------------------------------------------*/
		public static String getResistanceModifier(int effectType)
		{
			switch (effectType)
			{
				case NONE: return null;
				case BLUDGEONING: return Stats.Modifiers.RESIST_BLUDGEONING;
				case PIERCING: return Stats.Modifiers.RESIST_PIERCING;
				case SLASHING: return Stats.Modifiers.RESIST_SLASHING;
				case FIRE: return Stats.Modifiers.RESIST_FIRE;
				case WATER: return Stats.Modifiers.RESIST_WATER;
				case EARTH: return Stats.Modifiers.RESIST_EARTH;
				case AIR: return Stats.Modifiers.RESIST_AIR;
				case MENTAL: return Stats.Modifiers.RESIST_MENTAL;
				case ENERGY: return Stats.Modifiers.RESIST_ENERGY;
				default: throw new MazeException("Invalid effect type: "+ effectType);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public static enum SpellEffectSubType
	{
		NONE,
		NORMAL_DAMAGE,
		HEAT,
		COLD,
		POISON,
		ACID,
		LIGHTNING,
		PSYCHIC;

		/*----------------------------------------------------------------------*/
		public static String getImmunityModifier(SpellEffectSubType subType)
		{
			switch (subType)
			{
				case NONE: return null;
				case NORMAL_DAMAGE: return Stats.Modifiers.IMMUNE_TO_DAMAGE;
				case HEAT: return Stats.Modifiers.IMMUNE_TO_HEAT;
				case COLD: return Stats.Modifiers.IMMUNE_TO_COLD;
				case POISON: return Stats.Modifiers.IMMUNE_TO_POISON;
				case ACID: return Stats.Modifiers.IMMUNE_TO_ACID;
				case LIGHTNING: return Stats.Modifiers.IMMUNE_TO_LIGHTNING;
				case PSYCHIC: return Stats.Modifiers.IMMUNE_TO_PSYCHIC;
				default: throw new MazeException(subType.toString());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class ManaType
	{
		public static final int RED = 0;
		public static final int BLACK = 1;
		public static final int PURPLE = 2;
		public static final int GOLD = 3;
		public static final int WHITE = 4;
		public static final int GREEN = 5;
		public static final int BLUE = 6;

		private static Map<String, Integer> map = new HashMap<String, Integer>();

		/*----------------------------------------------------------------------*/
		static
		{
			for (int i=RED; i<=BLUE; i++)
			{
				map.put(describe(i), i);
			}
		}

		/*----------------------------------------------------------------------*/
		public static String describe(int manaType)
		{
			switch (manaType)
			{
				case MagicSys.ManaType.RED:  return "Red mana present";
				case MagicSys.ManaType.BLACK: return "Black mana present";
				case MagicSys.ManaType.PURPLE: return "Purple mana present";
				case MagicSys.ManaType.GOLD: return "Gold mana present";
				case MagicSys.ManaType.WHITE: return "White mana present";
				case MagicSys.ManaType.GREEN: return "Green mana present";
				case MagicSys.ManaType.BLUE: return "Blue mana present";
				default: throw new MazeException("Invalid mana colour "+manaType);
			}
		}

		/*----------------------------------------------------------------------*/
		public static String getModifier(int manaType)
		{
			switch (manaType)
			{
				case MagicSys.ManaType.RED:  return Stats.Modifiers.RED_MAGIC_GEN;
				case MagicSys.ManaType.BLACK: return Stats.Modifiers.BLACK_MAGIC_GEN;
				case MagicSys.ManaType.PURPLE: return Stats.Modifiers.PURPLE_MAGIC_GEN;
				case MagicSys.ManaType.GOLD: return Stats.Modifiers.GOLD_MAGIC_GEN;
				case MagicSys.ManaType.WHITE: return Stats.Modifiers.WHITE_MAGIC_GEN;
				case MagicSys.ManaType.GREEN: return Stats.Modifiers.GREEN_MAGIC_GEN;
				case MagicSys.ManaType.BLUE: return Stats.Modifiers.BLUE_MAGIC_GEN;
				default: throw new MazeException("Invalid mana colour "+manaType);
			}
		}

		/*----------------------------------------------------------------------*/
		public static int valueOf(String name)
		{
			return map.get(name);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class SpellUsabilityType
	{
		public static final int ANY_TIME = 0;
		public static final int COMBAT_ONLY = 1;
		public static final int NON_COMBAT_ONLY = 2;
		public static final int NPC_ONLY = 3;
		public static final int LOCKS_TRAPS_ONLY = 4;
		public static final int INVENTORY_SCREEN_ONLY = 5;

		public static final int MAX = 6;

		private static Map<String, Integer> map = new HashMap<String, Integer>();

		/*----------------------------------------------------------------------*/
		static
		{
			for (int i=ANY_TIME; i<=INVENTORY_SCREEN_ONLY; i++)
			{
				map.put(describe(i), i);
			}
		}

		/*----------------------------------------------------------------------*/
		public static String describe(int type)
		{
			switch (type)
			{
				case ANY_TIME:  return "Any time";
				case COMBAT_ONLY: return "Combat only";
				case NON_COMBAT_ONLY: return "Non-combat only";
				case NPC_ONLY: return "NPC only";
				case LOCKS_TRAPS_ONLY: return "Locks/Traps only";
				case INVENTORY_SCREEN_ONLY: return "Inventory screen only";
				default: throw new MazeException("Invalid type "+type);
			}
		}

		/*----------------------------------------------------------------------*/
		public static int valueOf(String name)
		{
			return map.get(name);
		}
	}
}
