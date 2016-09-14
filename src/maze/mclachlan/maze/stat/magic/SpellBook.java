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
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SpellBook
{
	private List<Spell> spells = new ArrayList<Spell>();

	/*-------------------------------------------------------------------------*/
	public SpellBook()
	{
	}

	/*-------------------------------------------------------------------------*/
	public SpellBook(List<Spell> spells)
	{
		this.spells = spells;

		if (this.spells == null)
		{
			this.spells = new ArrayList<Spell>();
		}
	}

	/*-------------------------------------------------------------------------*/
	public SpellBook(SpellBook sb)
	{
		if (sb.spells == null)
		{
			this.spells = new ArrayList<Spell>();
		}
		else
		{
			this.spells = new ArrayList<Spell>(sb.spells);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addSpell(Spell s)
	{
		// assert legality
		if (spells.contains(s))
		{
			throw new MazeException("Spellbook already contains ["+s+"]");
		}

		this.spells.add(s);
	}

	/*-------------------------------------------------------------------------*/
	public void addSpells(List<Spell> spells)
	{
		for (Spell s : spells)
		{
			addSpell(s);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void removeSpell(Spell s)
	{
		if (!this.spells.contains(s))
		{
			throw new MazeException("Spellbook does not contain ["+s+"]");
		}

		this.spells.remove(s);
	}

	/*-------------------------------------------------------------------------*/
	public void removeSpells(List<Spell> spells)
	{
		for (Spell s : spells)
		{
			removeSpell(s);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<Spell> getSpells(int level, String school)
	{
		List<Spell> result = new ArrayList<Spell>();
		
		for (Spell s : spells)
		{
			if (s.getLevel() == level && s.getSchool().equals(school))
			{
				result.add(s);
			}
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<Spell> getSpellsThatCanBeLearned(PlayerCharacter pc)
	{
		List<Spell> result = new ArrayList<Spell>();

		for (MagicSys.SpellBook book : MagicSys.SpellBook.getAllBooks())
		{
			PlayerSpellBook psb = Database.getInstance().getPlayerSpellBook(book.getName());

			for (String s : psb.getSpellNames())
			{
				Spell spell = Database.getInstance().getSpell(s);

				Stats.Modifier modifier = book.getCastingAbilityModifier();
				int maxSpellLevel = pc.getModifier(modifier);

				if (spell.getLevel() <= maxSpellLevel
					&& pc.meetsRequirements(spell.getRequirementsToLearn())
					&& !this.spells.contains(spell))
				{
					result.add(spell);
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<Spell> getSpells(String school)
	{
		List<Spell> result = new ArrayList<Spell>();

		for (Spell s : spells)
		{
			if (s.getSchool().equals(school))
			{
				result.add(s);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<Spell> getSpells()
	{
		return Collections.unmodifiableList(spells);
	}

	/*-------------------------------------------------------------------------*/
	public int size()
	{
		return this.spells.size();
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @deprecated
	 */
	public static class Limits
	{
		int maxLevel;
		int levelOffset;

		public Limits(int maxLevel, int levelOffset)
		{
			this.maxLevel = maxLevel;
			this.levelOffset = levelOffset;
		}

		public int getLevelOffset()
		{
			return levelOffset;
		}

		public int getMaxLevel()
		{
			return maxLevel;
		}


		public void setLevelOffset(int levelOffset)
		{
			this.levelOffset = levelOffset;
		}

		public void setMaxLevel(int maxLevel)
		{
			this.maxLevel = maxLevel;
		}

		public String toString()
		{
			return "max ["+maxLevel+"] offset ["+levelOffset+"]";
		}
	}
}
