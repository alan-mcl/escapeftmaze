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

package mclachlan.maze.map.script;

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.data.Database;
import java.util.*;

/**
 *
 */
public class CastSpellEvent extends MazeEvent
{
	private String spellName;
	private int casterLevel;
	private int castingLevel;

	public CastSpellEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public CastSpellEvent(String spellName, int casterLevel, int castingLevel)
	{
		this.spellName = spellName;
		this.casterLevel = casterLevel;
		this.castingLevel = castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		Spell spell = Database.getInstance().getSpell(spellName);
		GameSys.getInstance().castSpellOnPartyOutsideCombat(
			spell, casterLevel, castingLevel,
			new GameSys.TrapCaster(spell, casterLevel, castingLevel));

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public int getCasterLevel()
	{
		return casterLevel;
	}

	public int getCastingLevel()
	{
		return castingLevel;
	}

	public String getSpellName()
	{
		return spellName;
	}

	public void setSpellName(String spellName)
	{
		this.spellName = spellName;
	}

	public void setCasterLevel(int casterLevel)
	{
		this.casterLevel = casterLevel;
	}

	public void setCastingLevel(int castingLevel)
	{
		this.castingLevel = castingLevel;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		CastSpellEvent that = (CastSpellEvent)o;

		if (getCasterLevel() != that.getCasterLevel())
		{
			return false;
		}
		if (getCastingLevel() != that.getCastingLevel())
		{
			return false;
		}
		return getSpellName() != null ? getSpellName().equals(that.getSpellName()) : that.getSpellName() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getSpellName() != null ? getSpellName().hashCode() : 0;
		result = 31 * result + getCasterLevel();
		result = 31 * result + getCastingLevel();
		return result;
	}
}
