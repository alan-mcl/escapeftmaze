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

package mclachlan.maze.test;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.stat.UnifiedActor;

import static mclachlan.maze.stat.ItemTemplate.WeaponRange.*;

/**
 *
 */
public class TestPlayerParty
{
	static PlayerParty party;

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
//		new Database(new HardCodedLoader(), null);

		PlayerCharacter p1 = null;
		PlayerCharacter p2 = null;
		PlayerCharacter p3 = null;
		PlayerCharacter p4 = null;
		PlayerCharacter p5 = null;
		PlayerCharacter p6 = null;

		Map<String, PlayerCharacter> guild = Database.getInstance().getCharacterGuild();

		p1 = guild.get("Erdinger");
		p2 = guild.get("Grolsch");
		p3 = guild.get("Urbock");
		p4 = guild.get("Cobra");
		p5 = guild.get("Sterling");
		p6 = guild.get("Raven");

		ArrayList<UnifiedActor> chars = new ArrayList<UnifiedActor>();
		if (p1!=null) chars.add(p1);
		if (p2!=null) chars.add(p2);
		if (p3!=null) chars.add(p3);
		if (p4!=null) chars.add(p4);
		if (p5!=null) chars.add(p5);
		if (p6!=null) chars.add(p6);

		party = new PlayerParty(chars, 0, 3);
		party.setFormation(3);

		for (int eng=MELEE; eng<=LONG; eng++)
		{
			for (int min=MELEE; min<=LONG; min++)
			{
				for (int max=min; max<=LONG; max++)
				{
					test(eng, min, max);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	static void test(int eng, int min, int max)
	{
		System.out.println("eng = [" + describe(eng) + "]");
		System.out.println("min = [" + describe(min) + "]");
		System.out.println("max = [" + describe(max) + "]");
		List<UnifiedActor> actors = party.getActors(eng, min, max);

		if (actors != null)
		{
			for (UnifiedActor a : actors)
			{
				System.out.println(a.getName());
			}
		}
		else
		{
			System.out.println("no legal targets");
		}
		System.out.println("------------------------------------------------");
	}
}
