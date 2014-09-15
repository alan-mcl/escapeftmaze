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

package mclachlan.maze.stat;

import mclachlan.maze.stat.magic.MagicSys;

/**
 *
 */
public class StartingSpellBook
{
	private int maxLevel;
	private MagicSys.SpellBook spellBook;
	private int levelOffset;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param spellBook
	 * 	The type of spell book
	 * @param maxLevel
	 * 	The max level of spells from this book the character can learn
	 * @param levelOffset
	 * 	The character level at which he starts learning spells.
	 */
	public StartingSpellBook(
		MagicSys.SpellBook spellBook,
		int maxLevel,
		int levelOffset)
	{
		this.maxLevel = maxLevel;
		this.spellBook = spellBook;
		this.levelOffset = levelOffset;
	}

	/*-------------------------------------------------------------------------*/
	public int getMaxLevel()
	{
		return maxLevel;
	}

	/*-------------------------------------------------------------------------*/
	public int getLevelOffset()
	{
		return levelOffset;
	}

	/*-------------------------------------------------------------------------*/
	public MagicSys.SpellBook getSpellBook()
	{
		return spellBook;
	}

	public void setLevelOffset(int levelOffset)
	{
		this.levelOffset = levelOffset;
	}

	public void setMaxLevel(int maxLevel)
	{
		this.maxLevel = maxLevel;
	}

	public void setSpellBook(MagicSys.SpellBook spellBook)
	{
		this.spellBook = spellBook;
	}

	@Override
	public String toString()
	{
		return spellBook.getName() + "("+maxLevel+")";
	}
}
