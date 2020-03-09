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
import mclachlan.maze.stat.BodyPart;
import mclachlan.maze.stat.EquipableSlot;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1BodyPart
{
	/*-------------------------------------------------------------------------*/
	public static Map<String, BodyPart> load(BufferedReader reader)
	{
		try
		{
			Map <String, BodyPart> result = new HashMap<String, BodyPart>();
			while (true)
			{
				Properties p = V1Utils.getProperties(reader);
				if (p.isEmpty())
				{
					break;
				}
				BodyPart g = fromProperties(p);
				result.put(g.getName(), g);
			}

			return result;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, BodyPart> BodyParts) throws Exception
	{
		for (String name : BodyParts.keySet())
		{
			BodyPart g = BodyParts.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(BodyPart obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != BodyPart.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("displayName=");
			b.append(obj.getDisplayName());
			b.append(V1Utils.NEWLINE);
			b.append("damagePrevention=");
			b.append(obj.getDamagePrevention());
			b.append(V1Utils.NEWLINE);
			b.append("damagePreventionChance=");
			b.append(obj.getDamagePreventionChance());
			b.append(V1Utils.NEWLINE);
			b.append("modifiers=");
			b.append(V1StatModifier.toString(obj.getModifiers()));
			b.append(V1Utils.NEWLINE);
			b.append("nrWeaponHardpoints=");
			b.append(obj.getNrWeaponHardpoints());
			b.append(V1Utils.NEWLINE);
			b.append("equipableSlotType=");
			b.append(obj.getEquipableSlotType().name());
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static BodyPart fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom BodyPart impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (BodyPart)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			String displayName = p.getProperty("displayName");
			int damagePrevention = Integer.parseInt(p.getProperty("damagePrevention"));
			int damagePreventionChance = Integer.parseInt(p.getProperty("damagePreventionChance"));
			StatModifier modifiers = V1StatModifier.fromString(p.getProperty("modifiers"));
			int nrWeaponHardpoints = Integer.parseInt(p.getProperty("nrWeaponHardpoints"));
			EquipableSlot.Type equipableSlotType =
				EquipableSlot.Type.valueOf(p.getProperty("equipableSlotType"));

			return new BodyPart(
				name,
				displayName,
				modifiers,
				damagePrevention,
				damagePreventionChance,
				nrWeaponHardpoints,
				equipableSlotType);
		}
	}
}
