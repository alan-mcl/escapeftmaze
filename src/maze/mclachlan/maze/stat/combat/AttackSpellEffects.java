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

package mclachlan.maze.stat.combat;

import java.util.*;
import mclachlan.maze.stat.magic.SpellEffect;

/**
 *
 */
public class AttackSpellEffects
{
	private List<SpellEffect> spellEffects;
	private int castingLevel, spellLevel;

	public AttackSpellEffects(List<SpellEffect> spellEffects, int castingLevel,
		int spellLevel)
	{
		this.spellEffects = spellEffects;
		this.castingLevel = castingLevel;
		this.spellLevel = spellLevel;
	}

	public List<SpellEffect> getSpellEffects()
	{
		return spellEffects;
	}

	public void setSpellEffects(List<SpellEffect> spellEffects)
	{
		this.spellEffects = spellEffects;
	}

	public int getCastingLevel()
	{
		return castingLevel;
	}

	public void setCastingLevel(int castingLevel)
	{
		this.castingLevel = castingLevel;
	}

	public int getSpellLevel()
	{
		return spellLevel;
	}

	public void setSpellLevel(int spellLevel)
	{
		this.spellLevel = spellLevel;
	}
}
