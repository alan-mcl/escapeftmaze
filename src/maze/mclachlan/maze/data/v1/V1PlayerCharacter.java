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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.SpellBook;
import mclachlan.maze.data.Database;

/**
 *
 */
public class V1PlayerCharacter
{
	/*-------------------------------------------------------------------------*/
	static V1Map<String, Integer> levelsMap = new V1Map<String, Integer>()
	{
		public String typeToStringKey(String name)
		{
			return name;
		}

		public String typeToStringValue(Integer integer)
		{
			return integer.toString();
		}

		public String typeFromStringKey(String s)
		{
			return s;
		}

		public Integer typeFromStringValue(String s)
		{
			return Integer.valueOf(s);
		}
	};

	/*-------------------------------------------------------------------------*/
	static V1List<Item> itemsList = new V1List<Item>()
	{
		public String typeToString(Item item)
		{
			return V1Item.toString(item);
		}

		public Item typeFromString(String s)
		{
			return V1Item.fromString(s);
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Map<String, PlayerCharacter> load(BufferedReader reader) throws Exception
	{
		Map <String, PlayerCharacter> result = new HashMap<String, PlayerCharacter>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			PlayerCharacter g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, PlayerCharacter> map) throws Exception
	{
		for (String name : map.keySet())
		{
			PlayerCharacter g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(PlayerCharacter obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		b.append("levels=");
		b.append(levelsMap.toString(obj.getLevels()));
		b.append(V1Utils.NEWLINE);

		b.append("experience=");
		b.append(obj.getExperience());
		b.append(V1Utils.NEWLINE);

		b.append("kills=");
		b.append(obj.getKills());
		b.append(V1Utils.NEWLINE);

		b.append("race=");
		b.append(obj.getRace().getName());
		b.append(V1Utils.NEWLINE);

		b.append("gender=");
		b.append(obj.getGender().getName());
		b.append(V1Utils.NEWLINE);

		b.append("portrait=");
		b.append(obj.getPortrait());
		b.append(V1Utils.NEWLINE);

		b.append("characterClass=");
		b.append(obj.getCharacterClass().getName());
		b.append(V1Utils.NEWLINE);

		b.append("personality=");
		b.append(obj.getPersonality().getName());
		b.append(V1Utils.NEWLINE);

		b.append("torsoArmour=");
		b.append(V1Item.toString(obj.getTorsoArmour()));
		b.append(V1Utils.NEWLINE);

		b.append("legArmour=");
		b.append(V1Item.toString(obj.getLegArmour()));
		b.append(V1Utils.NEWLINE);

		b.append("helm=");
		b.append(V1Item.toString(obj.getHelm()));
		b.append(V1Utils.NEWLINE);

		b.append("boots=");
		b.append(V1Item.toString(obj.getBoots()));
		b.append(V1Utils.NEWLINE);

		b.append("gloves=");
		b.append(V1Item.toString(obj.getGloves()));
		b.append(V1Utils.NEWLINE);

		b.append("secondaryWeapon=");
		b.append(V1Item.toString(obj.getSecondaryWeapon()));
		b.append(V1Utils.NEWLINE);

		b.append("primaryWeapon=");
		b.append(V1Item.toString(obj.getPrimaryWeapon()));
		b.append(V1Utils.NEWLINE);

		b.append("altSecondaryWeapon=");
		b.append(V1Item.toString(obj.getAltSecondaryWeapon()));
		b.append(V1Utils.NEWLINE);

		b.append("altPrimaryWeapon=");
		b.append(V1Item.toString(obj.getAltPrimaryWeapon()));
		b.append(V1Utils.NEWLINE);

		b.append("miscItem1=");
		b.append(V1Item.toString(obj.getMiscItem1()));
		b.append(V1Utils.NEWLINE);

		b.append("miscItem2=");
		b.append(V1Item.toString(obj.getMiscItem2()));
		b.append(V1Utils.NEWLINE);

		b.append("bannerItem=");
		b.append(V1Item.toString(obj.getBannerItem()));
		b.append(V1Utils.NEWLINE);

		b.append("inventory=");
		b.append(itemsList.toString(obj.getInventory().getItems()));
		b.append(V1Utils.NEWLINE);

		b.append("spellbook=");
		b.append(V1SpellBook.toString(obj.getSpellBook()));
		b.append(V1Utils.NEWLINE);

		b.append("spellPicks=");
		b.append(obj.getSpellPicks());
		b.append(V1Utils.NEWLINE);

		b.append("stats=");
		b.append(V1Stats.toString(obj.getStats()));
		b.append(V1Utils.NEWLINE);

		b.append("practice=");
		b.append(V1StatModifier.toString(obj.getPractice().getModifiers()));
		b.append(V1Utils.NEWLINE);

		b.append("activeModifiers=");
		b.append(V1StatModifier.toString(obj.getActiveModifiers()));
		b.append(V1Utils.NEWLINE);

		b.append("removedLevelAbilities=");
		b.append(V1Utils.stringList.toString(obj.getRemovedLevelAbilities()));
		b.append(V1Utils.NEWLINE);

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static PlayerCharacter fromProperties(Properties p) throws Exception
	{
		String name = p.getProperty("name");
		Map<String, Integer> levels = levelsMap.fromString(p.getProperty("levels"));
		int experience = Integer.parseInt(p.getProperty("experience"));
		int kills = Integer.parseInt(p.getProperty("kills"));
		Race race = Database.getInstance().getRace(p.getProperty("race"));
		Gender gender = Database.getInstance().getGender(p.getProperty("gender"));
		CharacterClass characterClass = Database.getInstance().getCharacterClass(p.getProperty("characterClass"));
		Personality personality = Database.getInstance().getPersonalities().get(p.getProperty("personality"));
		String portrait = p.getProperty("portrait");
		Item torsoArmour = V1Item.fromString(p.getProperty("torsoArmour"));
		Item legArmour = V1Item.fromString(p.getProperty("legArmour"));
		Item helm = V1Item.fromString(p.getProperty("helm"));
		Item boots = V1Item.fromString(p.getProperty("boots"));
		Item gloves = V1Item.fromString(p.getProperty("gloves"));
		Item secondaryWeapon = V1Item.fromString(p.getProperty("secondaryWeapon"));
		Item primaryWeapon = V1Item.fromString(p.getProperty("primaryWeapon"));
		Item altSecondaryWeapon = V1Item.fromString(p.getProperty("altSecondaryWeapon"));
		Item altPrimaryWeapon = V1Item.fromString(p.getProperty("altPrimaryWeapon"));
		Item miscItem1 = V1Item.fromString(p.getProperty("miscItem1"));
		Item miscItem2 = V1Item.fromString(p.getProperty("miscItem2"));
		Item bannerItem = V1Item.fromString(p.getProperty("bannerItem"));
		List<Item> items = itemsList.fromString(p.getProperty("inventory"));
		Inventory inventory = new Inventory(PlayerCharacter.MAX_PACK_ITEMS);
		if (items != null)
		{
			inventory.addAll(items);
		}
		SpellBook spellbook = V1SpellBook.fromString(p.getProperty("spellbook"));
		int spellPicks = Integer.parseInt(p.getProperty("spellPicks"));
		Stats stats = V1Stats.fromString(p.getProperty("stats"));
		Practice practice = new Practice(V1StatModifier.fromString(p.getProperty("practice")));
		StatModifier activeModifiers = V1StatModifier.fromString(p.getProperty("activeModifiers"));
		List<String> removedLevelAbilities = V1Utils.stringList.fromString(p.getProperty("removedLevelAbilities"));

		return new PlayerCharacter(
			name,
			gender,
			race,
			characterClass,
			personality, 
			levels,
			experience,
			kills,
			portrait,
			helm,
			torsoArmour,
			legArmour,
			boots,
			gloves,
			miscItem1,
			miscItem2,
			bannerItem,
			primaryWeapon,
			secondaryWeapon,
			altPrimaryWeapon,
			altSecondaryWeapon,
			inventory,
			spellbook,
			spellPicks,
			stats,
			practice,
			activeModifiers,
			removedLevelAbilities);
	}
}
