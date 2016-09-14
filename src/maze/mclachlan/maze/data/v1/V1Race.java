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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.List;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.data.Database;

/**
 *
 */
public class V1Race
{
	static V1PercentageTable<BodyPart> percTable = new V1PercentageTable<BodyPart>()
	{
		public BodyPart typeFromString(String s)
		{
			return Database.getInstance().getBodyPart(s);
		}

		public String typeToString(BodyPart bodyPart)
		{
			return bodyPart.getName();
		}
	};

	static V1List<Gender> genders = new V1List<Gender>()
	{
		public String typeToString(Gender gender)
		{
			return gender.getName();
		}

		public Gender typeFromString(String s)
		{
			return Database.getInstance().getGender(s);
		}
	};

	static V1List<NaturalWeapon> naturalWeaponsList = new V1List<NaturalWeapon>()
	{
		@Override
		public String typeToString(NaturalWeapon naturalWeapon)
		{
			return naturalWeapon.getName();
		}

		@Override
		public NaturalWeapon typeFromString(String s)
		{
			return Database.getInstance().getNaturalWeapons().get(s);
		}
	};

	static V1Map<String, List<String>> suggestedNamesMap = new V1Map<String, List<String>>("~",":")
	{
		@Override
		public String typeToStringKey(String s)
		{
			return s;
		}

		@Override
		public String typeToStringValue(List<String> strings)
		{
			return V1Utils.stringList.toString(strings);
		}

		@Override
		public String typeFromStringKey(String s)
		{
			return s;
		}

		@Override
		public List<String> typeFromStringValue(String s)
		{
			return V1Utils.stringList.fromString(s);
		}
	};
	static V1List<StartingKit> startingKits = new V1List<StartingKit>()
	{
		public String typeToString(StartingKit s)
		{
			return s.getName();
		}

		public StartingKit typeFromString(String s)
		{
			return Database.getInstance().getStartingKits().get(s);
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Map<String, Race> load(BufferedReader reader) throws Exception
	{
		Map <String, Race> result = new HashMap<String, Race>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			Race g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, Race> races) throws Exception
	{
		for (String name : races.keySet())
		{
			Race g = races.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(Race obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != Race.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("description=");
			b.append(V1Utils.escapeNewlines(obj.getDescription()));
			b.append(V1Utils.NEWLINE);

			b.append("startingHitPointPercentage=");
			b.append(obj.getStartingHitPointPercent());
			b.append(V1Utils.NEWLINE);

			b.append("startingActionPointPercentage=");
			b.append(obj.getStartingActionPointPercent());
			b.append(V1Utils.NEWLINE);

			b.append("startingMagicPointPercentage=");
			b.append(obj.getStartingMagicPointPercent());
			b.append(V1Utils.NEWLINE);

			b.append("startingModifiers=");
			b.append(V1StatModifier.toString(obj.getStartingModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("constantModifiers=");
			b.append(V1StatModifier.toString(obj.getConstantModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("bannerModifiers=");
			b.append(V1StatModifier.toString(obj.getBannerModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("attributeCeilings=");
			b.append(V1StatModifier.toString(obj.getAttributeCeilings()));
			b.append(V1Utils.NEWLINE);

			b.append("head=");
			b.append(obj.getHead().getName());
			b.append(V1Utils.NEWLINE);
			
			b.append("torso=");
			b.append(obj.getTorso().getName());
			b.append(V1Utils.NEWLINE);
			
			b.append("leg=");
			b.append(obj.getLeg().getName());
			b.append(V1Utils.NEWLINE);
			
			b.append("hand=");
			b.append(obj.getHand().getName());
			b.append(V1Utils.NEWLINE);
			
			b.append("foot=");
			b.append(obj.getFoot().getName());
			b.append(V1Utils.NEWLINE);

			b.append("rightHandIcon=");
			b.append(obj.getRightHandIcon());
			b.append(V1Utils.NEWLINE);

			b.append("leftHandIcon=");
			b.append(obj.getLeftHandIcon());
			b.append(V1Utils.NEWLINE);

			b.append("allowedGenders=");
			b.append(genders.toString(obj.getAllowedGenders()));
			b.append(V1Utils.NEWLINE);

			b.append("magicDead=");
			b.append(obj.isMagicDead());
			b.append(V1Utils.NEWLINE);

			Spell spell = obj.getSpecialAbility();
			b.append("specialAbility=");
			b.append(spell==null?"":spell.getName());
			b.append(V1Utils.NEWLINE);
			
			b.append("startingItems=");
			b.append(startingKits.toString(obj.getStartingItems()));
			b.append(V1Utils.NEWLINE);

			b.append("naturalWeapons=");
			b.append(naturalWeaponsList.toString(obj.getNaturalWeapons()));
			b.append(V1Utils.NEWLINE);

			b.append("suggestedNames=");
			b.append(suggestedNamesMap.toString(obj.getSuggestedNames()));
			b.append(V1Utils.NEWLINE);

			b.append("unlockVariable=");
			b.append(obj.getUnlockVariable()==null?"":obj.getUnlockVariable());
			b.append(V1Utils.NEWLINE);

			b.append("unlockDescription=");
			b.append(obj.getUnlockDescription()==null?"":obj.getUnlockDescription());
			b.append(V1Utils.NEWLINE);

			b.append("favouredEnemyModifier=");
			b.append(obj.getFavouredEnemyModifier()==null?"":obj.getFavouredEnemyModifier());
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static Race fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom Race impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (Race)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			String desc = V1Utils.replaceNewlines(p.getProperty("description"));
			int startingHitPointPercentage = Integer.parseInt(p.getProperty("startingHitPointPercentage"));
			int startingActionPointPercentage = Integer.parseInt(p.getProperty("startingActionPointPercentage"));
			int startingMagicPointPercentage = Integer.parseInt(p.getProperty("startingMagicPointPercentage"));
			StatModifier sm = V1StatModifier.fromString(p.getProperty("startingModifiers"));
			StatModifier cm = V1StatModifier.fromString(p.getProperty("constantModifiers"));
			StatModifier bm = V1StatModifier.fromString(p.getProperty("bannerModifiers"));
			StatModifier ac = V1StatModifier.fromString(p.getProperty("attributeCeilings"));
			BodyPart head = Database.getInstance().getBodyPart(p.getProperty("head"));
			BodyPart torso = Database.getInstance().getBodyPart(p.getProperty("torso"));
			BodyPart leg = Database.getInstance().getBodyPart(p.getProperty("leg"));
			BodyPart hand = Database.getInstance().getBodyPart(p.getProperty("hand"));
			BodyPart foot = Database.getInstance().getBodyPart(p.getProperty("foot"));
			String rightHandIcon = p.getProperty("rightHandIcon");
			String leftHandIcon = p.getProperty("leftHandIcon");
			List<Gender> allowedGenders = genders.fromString(p.getProperty("allowedGenders"));
			boolean magicDead = Boolean.valueOf(p.getProperty("magicDead"));

			String spellName = p.getProperty("specialAbility");
			Spell specialAbility = spellName.equals("") ? null : Database.getInstance().getSpell(spellName);
			
			List<StartingKit> startingItems = startingKits.fromString(p.getProperty("startingItems"));
			List<NaturalWeapon> naturalWeapons = naturalWeaponsList.fromString(p.getProperty("naturalWeapons"));

			Map<String, List<String>> suggestedNames = suggestedNamesMap.fromString(p.getProperty("suggestedNames"));

			String unlockVariable = p.getProperty("unlockVariable");
			if ("".equals(unlockVariable))
			{
				unlockVariable = null;
			}

			String unlockDescription = p.getProperty("unlockDescription");
			Stats.Modifier favouredEnemyModifier = "".equals(p.getProperty("favouredEnemyModifier"))
				?
				null:
				Stats.Modifier.valueOf(p.getProperty("favouredEnemyModifier"));

			return new Race(
				name,
				desc,
				startingHitPointPercentage,
				startingActionPointPercentage,
				startingMagicPointPercentage,
				sm,
				cm,
				bm,
				ac,
				head, 
				torso, 
				leg, 
				hand, 
				foot, 
				leftHandIcon,
				rightHandIcon,
				allowedGenders,
				magicDead,
				specialAbility,
				startingItems,
				naturalWeapons,
				suggestedNames,
				unlockVariable,
				unlockDescription,
				favouredEnemyModifier);
		}
	}
}