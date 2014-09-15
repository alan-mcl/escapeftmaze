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

package mclachlan.maze.stat;

/**
 *
 */
public class AddSpellPicks extends LevelAbility
{
	private int spellPicks;

	public AddSpellPicks(String key, String displayName, String description,
		int spellPicks)
	{
		super(key, displayName, description);
		this.spellPicks = spellPicks;
	}

	public int getSpellPicks()
	{
		return spellPicks;
	}

	public void setSpellPicks(int spellPicks)
	{
		this.spellPicks = spellPicks;
	}
}
