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

package mclachlan.maze.util;

import java.util.*;

/**
 *
 */
public class SpellGen
{
	static Random r = new Random();
	
	/**
	 * Key: effect cost
	 * Value: effect
	 */ 
	static Map<String, Double> effects = new HashMap<String, Double>();
	static List<String> minorConditions = new ArrayList<String>();
	static List<String> majorConditions = new ArrayList<String>();
	static List<String> fatalConditions = new ArrayList<String>();
	static List<String> effectList;
	
	static
	{
		effects.put("1-3 damage to 1 target (1/2 on save)", 0.5D);
		effects.put("1-6 damage to 1 target (1/2 on save)", 1D);
		effects.put("5% per level chance MINOR (save to prevent)", 0.5D);
		effects.put("5% per level chance MAJOR (save to prevent)", 1D);
		effects.put("5% per level chance FATAL (save to prevent)", 2D);
		effects.put("damages all targets in group", 1D);
		effects.put("damages all foes in group", 2D);
		effects.put("recurs 1 round per level", 1D);
		
		effectList = new ArrayList<String>(effects.keySet());
		
		minorConditions.add("Irration");
		minorConditions.add("Fear");
		minorConditions.add("Sleep");
		minorConditions.add("Nausea");
		
		majorConditions.add("Insane");
		majorConditions.add("Disease");
		majorConditions.add("Paralyse");
		majorConditions.add("Blind");
		majorConditions.add("Poison");
		majorConditions.add("KO");
		
		fatalConditions.add("Death");
		fatalConditions.add("Stone");
	}
	
	/*-------------------------------------------------------------------------*/
	public static void main(String[] args)
	{
		int numSpells = 10;
		
		for (int i=0; i<numSpells; i++)
		{
			genSpell();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void genSpell()
	{
		List<String> spell = new ArrayList<String>();
		int spellLevel = r.nextInt(7)+1;
		int nrEffects = effects.keySet().size();
		
		double cumLvl = 0;
		
		while (cumLvl < spellLevel)
		{
			String effect = effectList.get(r.nextInt(nrEffects));
			double cost = effects.get(effect);
			
			if (cumLvl + cost <= spellLevel)
			{
				spell.add(effect);
				cumLvl = cumLvl + cost;
			}
		}
		
		for (int i=0; i<spell.size(); i++)
		{
			String effect = spell.get(i);
			effect = effect.replaceAll("MAJOR", majorConditions.get(r.nextInt(majorConditions.size())));
			effect = effect.replaceAll("MINOR", minorConditions.get(r.nextInt(minorConditions.size())));
			effect = effect.replaceAll("FATAL", fatalConditions.get(r.nextInt(fatalConditions.size())));
			spell.set(i, effect);
		}
		
		System.out.println("---------------------------------------------------");
		System.out.println("LVL: "+spellLevel+" ("+cumLvl+")");
		for (String effect : spell)
		{
			System.out.println(effect);
		}
	}
}
