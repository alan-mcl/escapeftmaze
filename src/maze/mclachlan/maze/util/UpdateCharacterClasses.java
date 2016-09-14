/*
 * Copyright (c) 2014 Alan McLachlan
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
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.MagicSys;

/**
 *
 */
public class UpdateCharacterClasses
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		V1Loader loader = new V1Loader();
		V1Saver saver = new V1Saver();
		Database db = new Database(loader, saver);
		Campaign campaign = Maze.getStubCampaign();
		loader.init(campaign);
		saver.init(campaign);

		Map<String, MagicSys.SpellBook> hybrids = new HashMap<String, MagicSys.SpellBook>();
		hybrids.put("Samurai", MagicSys.SpellBook.SORCERY);
		hybrids.put("Warlock", MagicSys.SpellBook.SORCERY);
		hybrids.put("Blackguard", MagicSys.SpellBook.BLACK_MAGIC);
		hybrids.put("Ninja", MagicSys.SpellBook.BLACK_MAGIC);
		hybrids.put("Amazon", MagicSys.SpellBook.WITCHCRAFT);
		hybrids.put("Gypsy", MagicSys.SpellBook.WITCHCRAFT);
		hybrids.put("Skald", MagicSys.SpellBook.ENCHANTMENT);
		hybrids.put("Troubadour", MagicSys.SpellBook.ENCHANTMENT);
		hybrids.put("Paladin", MagicSys.SpellBook.WHITE_MAGIC);
		hybrids.put("Exorcist", MagicSys.SpellBook.WHITE_MAGIC);
		hybrids.put("Sohei", MagicSys.SpellBook.DRUIDISM);
		hybrids.put("Ranger", MagicSys.SpellBook.DRUIDISM);
		hybrids.put("Shaman", MagicSys.SpellBook.ELEMENTALISM);
		hybrids.put("Shugenja", MagicSys.SpellBook.ELEMENTALISM);

		Map<String, MagicSys.SpellBook[]> duals = new HashMap<String, MagicSys.SpellBook[]>();
		duals.put("Adept", new MagicSys.SpellBook[]{MagicSys.SpellBook.DRUIDISM, MagicSys.SpellBook.WHITE_MAGIC});
		duals.put("Magician", new MagicSys.SpellBook[]{MagicSys.SpellBook.SORCERY, MagicSys.SpellBook.ELEMENTALISM});
		duals.put("Cultist", new MagicSys.SpellBook[]{MagicSys.SpellBook.WITCHCRAFT, MagicSys.SpellBook.BLACK_MAGIC});

		Map<String,CharacterClass> characterClasses = db.getCharacterClasses();

		for (CharacterClass cc : characterClasses.values())
		{
			if (hybrids.containsKey(cc.getName()))
			{
//				hybridCasterProgression(cc, hybrids.get(cc.getName()));
			}
			else if (duals.containsKey(cc.getName()))
			{
				dualCasterProgression(cc, duals.get(cc.getName()));
			}
			else
			{
				/*if (cc.getName().equals("Druid"))
				{
					specialistCasterProgression(cc, MagicSys.SpellBook.DRUIDISM);
				}
				else if (cc.getName().equals("Elemental"))
				{
					specialistCasterProgression(cc, MagicSys.SpellBook.ELEMENTALISM);
				}
				else if (cc.getName().equals("Illusionist"))
				{
					specialistCasterProgression(cc, MagicSys.SpellBook.ENCHANTMENT);
				}
				else if (cc.getName().equals("Sorcerer"))
				{
					specialistCasterProgression(cc, MagicSys.SpellBook.SORCERY);
				}
				else if (cc.getName().equals("Witch"))
				{
					specialistCasterProgression(cc, MagicSys.SpellBook.WITCHCRAFT);
				}
				else if (cc.getName().equals("Priest"))
				{
					specialistCasterProgression(cc, MagicSys.SpellBook.WHITE_MAGIC);
				}*/
			}
		}

		saver.saveCharacterClasses(characterClasses);
	}

	/*-------------------------------------------------------------------------*/
	static void specialistCasterProgression(CharacterClass cc, MagicSys.SpellBook book)
	{
		System.out.println("specialist caster: "+cc.getName());
		LevelAbilityProgression progression = cc.getProgression();

		Stats.Modifier modifier = book.getCastingAbilityModifier();
		String s = book.getDisplayNameKey();

		// casting ability progression
		progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_1","lap_desc_"+s,getStatModifier(modifier, 1)), 1);
		progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_2","lap_desc_"+s,getStatModifier(modifier, 2)), 3);
		progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_3","lap_desc_"+s,getStatModifier(modifier, 3)), 5);
		progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_4","lap_desc_"+s,getStatModifier(modifier, 4)), 7);
		progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_5","lap_desc_"+s,getStatModifier(modifier, 5)), 9);
		progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_6","lap_desc_"+s,getStatModifier(modifier, 6)), 11);
		progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_7","lap_desc_"+s,getStatModifier(modifier, 7)), 13);
		progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_8","lap_desc_"+s,getStatModifier(modifier, 8)), 15);

		// spellpicks
		progression.add(new AddSpellPicks("","lap_name_spellpicks+2","lap_desc_spellpicks+2", 2), 1);
		for (int i=2; i<=LevelAbilityProgression.MAX_LEVELS; i++)
		{
			progression.add(new AddSpellPicks("","lap_name_spellpicks+1","lap_desc_spellpicks+1", 1), i);
		}
	}

	/*-------------------------------------------------------------------------*/
	static void hybridCasterProgression(CharacterClass cc, MagicSys.SpellBook book)
	{
		System.out.println("hybrid: "+cc.getName());
		LevelAbilityProgression progression = cc.getProgression();

		Stats.Modifier modifier = book.getCastingAbilityModifier();
		String s = book.getDisplayNameKey();

		// casting ability progression
		progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_1","lap_desc_"+s,getStatModifier(modifier, 1)), 1);
		progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_2","lap_desc_"+s,getStatModifier(modifier, 2)), 6);
		progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_3","lap_desc_"+s,getStatModifier(modifier, 3)), 12);

		// spellpicks
		progression.add(new AddSpellPicks("","lap_name_spellpicks+1","lap_desc_spellpicks+1", 1), 1);
		for (int i=2; i<=LevelAbilityProgression.MAX_LEVELS; i+=2)
		{
			progression.add(new AddSpellPicks("","lap_name_spellpicks+1","lap_desc_spellpicks+1", 1), i);
		}
	}

	/*-------------------------------------------------------------------------*/
	static void dualCasterProgression(CharacterClass cc, MagicSys.SpellBook... books)
	{
		System.out.println("dual: "+cc.getName());
		LevelAbilityProgression progression = cc.getProgression();

		for (MagicSys.SpellBook book : books)
		{
			Stats.Modifier modifier = book.getCastingAbilityModifier();
			String s = book.getDisplayNameKey();

			// casting ability progression
			progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_1","lap_desc_"+s,getStatModifier(modifier, 1)), 1);
			progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_2","lap_desc_"+s,getStatModifier(modifier, 2)), 5);
			progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_3","lap_desc_"+s,getStatModifier(modifier, 3)), 10);
			progression.add(new StatModifierLevelAbility(s,"lap_name_"+s+"_4","lap_desc_"+s,getStatModifier(modifier, 4)), 15);
		}

		// spellpicks
		progression.add(new AddSpellPicks("","lap_name_spellpicks+2","lap_desc_spellpicks+2", 2), 1);
		for (int i=2; i<=LevelAbilityProgression.MAX_LEVELS; i++)
		{
			progression.add(new AddSpellPicks("","lap_name_spellpicks+1","lap_desc_spellpicks+1", 1), i);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static StatModifier getStatModifier(Stats.Modifier modifier, int i)
	{
		StatModifier result = new StatModifier();
		result.setModifier(modifier, i);
		return result;
	}
}
