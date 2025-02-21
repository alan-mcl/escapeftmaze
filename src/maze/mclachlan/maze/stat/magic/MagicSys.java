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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.data.v2.V2DataObject;
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
//	public static final int MAX_SPELL_LEVEL = 10;

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
		return new ArrayList<>(SpellBook.spellBooks.values());
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
	public int getPointCost(ValueList value, int castingLevel, UnifiedActor caster)
	{
		if (value == null)
		{
			return 0;
		}
		else
		{
			return value.compute(caster, castingLevel);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class SpellBook implements V2DataObject
	{
		private final String name;
		private final String displayNameKey;
		private final Stats.Modifier castingAbilityModifier;
		private final Set<String> genders;
		private final Set<String> races;

		private static final HashSet<String> MALE = new HashSet<>();
		private static final HashSet<String> FEMALE = new HashSet<>();

		public static final SpellBook RED_MAGIC =
			new SpellBook("Red Magic", "red_magic", Stats.Modifier.RED_MAGIC_SPELLS, MALE, null);
		public static final SpellBook BLACK_MAGIC =
			new SpellBook("Black Magic", "black_magic", Stats.Modifier.BLACK_MAGIC_SPELLS, null, null);
		public static final SpellBook PURPLE_MAGIC =
			new SpellBook("Purple Magic", "purple_magic", Stats.Modifier.PURPLE_MAGIC_SPELLS, FEMALE, null);
		public static final SpellBook GOLD_MAGIC =
			new SpellBook("Gold Magic", "gold_magic", Stats.Modifier.GOLD_MAGIC_SPELLS, null, null);
		public static final SpellBook WHITE_MAGIC =
			new SpellBook("White Magic", "white_magic", Stats.Modifier.WHITE_MAGIC_SPELLS, null, null);
		public static final SpellBook GREEN_MAGIC =
			new SpellBook("Green Magic", "green_magic", Stats.Modifier.GREEN_MAGIC_SPELLS, null, null);
		public static final SpellBook BLUE_MAGIC =
			new SpellBook("Blue Magic", "blue_magic", Stats.Modifier.BLUE_MAGIC_SPELLS, null, null);

		private static final Map<String, SpellBook> spellBooks = new HashMap<>();

		static
		{
			spellBooks.put(RED_MAGIC.getName(), RED_MAGIC);
			spellBooks.put(BLACK_MAGIC.getName(), BLACK_MAGIC);
			spellBooks.put(PURPLE_MAGIC.getName(), PURPLE_MAGIC);
			spellBooks.put(GOLD_MAGIC.getName(), GOLD_MAGIC);
			spellBooks.put(WHITE_MAGIC.getName(), WHITE_MAGIC);
			spellBooks.put(GREEN_MAGIC.getName(), GREEN_MAGIC);
			spellBooks.put(BLUE_MAGIC.getName(), BLUE_MAGIC);

			MALE.add("Male");
			FEMALE.add("Female");
		}

		/*----------------------------------------------------------------------*/
		public SpellBook(String name,
			String displayNameKey,
			Stats.Modifier castingAbilityModifier,
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

		@Override
		public void setName(String newName)
		{
			throw new MazeException("not supported");
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
			return new ArrayList<>(spellBooks.values());
		}

		/*-------------------------------------------------------------------------*/
		public Stats.Modifier getCastingAbilityModifier()
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

		static List<String> spellSchools = new ArrayList<>();

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
		public static final int PARTY_BUT_NOT_CASTER = 12;

		public static final int MAX = 13;

		/*----------------------------------------------------------------------*/
		public static String describe(int targetType)
		{
			return switch (targetType)
				{
					case CASTER -> "caster";
					case ALLY -> "ally";
					case PARTY -> "party";
					case FOE -> "one foe";
					case FOE_GROUP -> "one group of foes";
					case ALL_FOES -> "all foes";
					case TILE -> "current tile";
					case LOCK_OR_TRAP -> "lock or trap";
					case NPC -> "NPC";
					case ITEM -> "item";
					case CLOUD_ONE_GROUP -> "cloud: one group";
					case CLOUD_ALL_GROUPS -> "cloud: all groups";
					case PARTY_BUT_NOT_CASTER -> "party (but not caster)";
					default ->
						throw new MazeException("Invalid target type: " + targetType);
				};
		}

		/*----------------------------------------------------------------------*/
		public int valueOf(String s)
		{
			return switch (s)
				{
					case "caster" -> CASTER;
					case "ally" -> ALLY;
					case "party" -> PARTY;
					case "one foe" -> FOE;
					case "foe group" -> FOE_GROUP;
					case "all foes" -> ALL_FOES;
					case "current tile" -> TILE;
					case "NPC" -> NPC;
					case "item" -> ITEM;
					case "cloud: one group" -> CLOUD_ONE_GROUP;
					case "cloud: all groups" -> CLOUD_ALL_GROUPS;
					case "party (but not caster)" -> PARTY_BUT_NOT_CASTER;
					default ->
						throw new MazeException("Invalid spell target type [" + s + "]");
				};
		}
	}

	/*-------------------------------------------------------------------------*/
	public enum SpellEffectType
	{
		NONE("set.none", null),
		FIRE("set.fire", Stats.Modifier.RESIST_FIRE),
		WATER("set.water", Stats.Modifier.RESIST_WATER),
		EARTH("set.earth", Stats.Modifier.RESIST_EARTH),
		AIR("set.air", Stats.Modifier.RESIST_AIR),
		MENTAL("set.mental", Stats.Modifier.RESIST_MENTAL),
		ENERGY("set.energy", Stats.Modifier.RESIST_ENERGY),
		BLUDGEONING("set.bludgeon", Stats.Modifier.RESIST_BLUDGEONING),
		PIERCING("set.pierce", Stats.Modifier.RESIST_PIERCING),
		SLASHING("set.slash", Stats.Modifier.RESIST_SLASHING);

		private final String descKey;
		private final Stats.Modifier resistanceModifier;

		/*----------------------------------------------------------------------*/

		SpellEffectType(String descKey, Stats.Modifier resistanceModifier)
		{
			this.descKey = descKey;
			this.resistanceModifier = resistanceModifier;
		}

		/*----------------------------------------------------------------------*/
		public String describe()
		{
			return StringUtil.getGamesysString(descKey);
		}

		/*----------------------------------------------------------------------*/
		public static String describe(SpellEffectType effectType)
		{
			return effectType.describe();
		}

		public Stats.Modifier getResistanceModifier()
		{
			return resistanceModifier;
		}
	}

	/*-------------------------------------------------------------------------*/
	public enum SpellEffectSubType
	{
		NONE("sest.none"),
		NORMAL_DAMAGE("sest.normal_damage"),
		HEAT("sest.heat"),
		COLD("sest.cold"),
		POISON("sest.poison"),
		DISEASE("sest.disease"),
		CURSE("sest.curse"),
		ACID("sest.acid"),
		LIGHTNING("sest.lightning"),
		PSYCHIC("sest.psychic");

		private final String descKey;

		/*----------------------------------------------------------------------*/
		SpellEffectSubType(String descKey)
		{
			this.descKey = descKey;
		}

		/*----------------------------------------------------------------------*/
		public static Stats.Modifier getImmunityModifier(SpellEffectSubType subType)
		{
			return switch (subType)
				{
					case NONE -> null;
					case NORMAL_DAMAGE -> Stats.Modifier.IMMUNE_TO_DAMAGE;
					case HEAT -> Stats.Modifier.IMMUNE_TO_HEAT;
					case COLD -> Stats.Modifier.IMMUNE_TO_COLD;
					case POISON -> Stats.Modifier.IMMUNE_TO_POISON;
					case DISEASE -> Stats.Modifier.IMMUNE_TO_DISEASE;
					case CURSE -> Stats.Modifier.IMMUNE_TO_HEX;
					case ACID -> Stats.Modifier.IMMUNE_TO_ACID;
					case LIGHTNING -> Stats.Modifier.IMMUNE_TO_LIGHTNING;
					case PSYCHIC -> Stats.Modifier.IMMUNE_TO_PSYCHIC;
					default -> throw new MazeException(subType.toString());
				};
		}

		/*----------------------------------------------------------------------*/
		public String describe()
		{
			return StringUtil.getGamesysString(descKey);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class MagicColour
	{
		public static final int RED = 0;
		public static final int BLACK = 1;
		public static final int PURPLE = 2;
		public static final int GOLD = 3;
		public static final int WHITE = 4;
		public static final int GREEN = 5;
		public static final int BLUE = 6;

		private static final Map<String, Integer> map = new HashMap<>();

		/*----------------------------------------------------------------------*/
		static
		{
			for (int i=RED; i<=BLUE; i++)
			{
				map.put(describe(i), i);
			}
		}

		/*----------------------------------------------------------------------*/
		public static String describe(int magicColour)
		{
			return switch (magicColour)
				{
					case MagicColour.RED -> "Red";
					case MagicColour.BLACK -> "Black";
					case MagicColour.PURPLE -> "Purple";
					case MagicColour.GOLD -> "Gold";
					case MagicColour.WHITE -> "White";
					case MagicColour.GREEN -> "Green";
					case MagicColour.BLUE -> "Blue";
					default ->
						throw new MazeException("Invalid magic colour " + magicColour);
				};
		}

		/*----------------------------------------------------------------------*/
		public static Stats.Modifier getModifier(int magicColour)
		{
			return switch (magicColour)
				{
					case MagicColour.RED -> Stats.Modifier.RED_MAGIC_GEN;
					case MagicColour.BLACK -> Stats.Modifier.BLACK_MAGIC_GEN;
					case MagicColour.PURPLE -> Stats.Modifier.PURPLE_MAGIC_GEN;
					case MagicColour.GOLD -> Stats.Modifier.GOLD_MAGIC_GEN;
					case MagicColour.WHITE -> Stats.Modifier.WHITE_MAGIC_GEN;
					case MagicColour.GREEN -> Stats.Modifier.GREEN_MAGIC_GEN;
					case MagicColour.BLUE -> Stats.Modifier.BLUE_MAGIC_GEN;
					default ->
						throw new MazeException("Invalid magic colour " + magicColour);
				};
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

		private static final Map<String, Integer> map = new HashMap<>();

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
			return switch (type)
				{
					case ANY_TIME -> "Any time";
					case COMBAT_ONLY -> "Combat only";
					case NON_COMBAT_ONLY -> "Non-combat only";
					case NPC_ONLY -> "NPC only";
					case LOCKS_TRAPS_ONLY -> "Locks/Traps only";
					case INVENTORY_SCREEN_ONLY -> "Inventory screen only";
					default -> throw new MazeException("Invalid type " + type);
				};
		}

		/*----------------------------------------------------------------------*/
		public static int valueOf(String name)
		{
			return map.get(name);
		}
	}
}
