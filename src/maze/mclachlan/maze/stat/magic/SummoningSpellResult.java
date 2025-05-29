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
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.combat.event.SummoningSucceedsEvent;
import mclachlan.maze.stat.combat.event.SummoningFailsEvent;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.data.Database;


/**
 *
 */
public class SummoningSpellResult extends SpellResult
{
	//
	// Summoning spell results work as follows:
	//  - 10 different encounter tables
	//  - a calculated strength for the spell that should be a number from 0..9
	//  - there is a 10% chance of giving the strength +1, and a 10% chance of -1
	//  - a foe group is then generated from the indicated encounter table
	//
	private String[] encounterTable;
	private ValueList strength;

	public SummoningSpellResult()
	{
	}

	/*-------------------------------------------------------------------------*/
	public SummoningSpellResult(String[] encounterTable, ValueList strength)
	{
		this.encounterTable = encounterTable;
		this.strength = strength;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target,
		int castingLevel, SpellEffect parent, Spell spell)
	{
		// summoning spell results should pretty much be from spells with the
		// CASTER target type

		int str = strength.compute(source, castingLevel);
		int roll = Dice.d100.roll("summoning spell result");
		if (roll <= 10)
		{
			str--;
		}
		else if (roll >= 90)
		{
			str++;
		}

		str = Math.min(9, str);
		str = Math.max(0, str);

		EncounterTable table = Database.getInstance().getEncounterTable(this.encounterTable[str]);
		FoeEntry fe = table.getEncounterTable().getRandomItem();
		List<FoeGroup> foeGroups = fe.generate();

		if (Maze.getInstance().canSummon(source, foeGroups.size()))
		{
			for (FoeGroup fg : foeGroups)
			{
				for (UnifiedActor a : fg.getActors())
				{
					Foe foe = (Foe)a;
					foe.setSummoned(true);
					if (source instanceof PlayerCharacter)
					{
						foe.setIdentificationState(Item.IdentificationState.IDENTIFIED);
					}
				}
			}

			return getList(
				new SummoningSucceedsEvent(foeGroups, source));
		}
		else
		{
			return getList(
				new SummoningFailsEvent());
		}
	}

	/*-------------------------------------------------------------------------*/
	public String[] getEncounterTable()
	{
		return encounterTable;
	}

	public ValueList getStrength()
	{
		return strength;
	}

	public void setEncounterTable(String[] encounterTable)
	{
		this.encounterTable = encounterTable;
	}

	public void setStrength(ValueList strength)
	{
		this.strength = strength;
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
		if (!super.equals(o))
		{
			return false;
		}

		SummoningSpellResult that = (SummoningSpellResult)o;

		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(getEncounterTable(), that.getEncounterTable()))
		{
			return false;
		}
		return getStrength() != null ? getStrength().equals(that.getStrength()) : that.getStrength() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + Arrays.hashCode(getEncounterTable());
		result = 31 * result + (getStrength() != null ? getStrength().hashCode() : 0);
		return result;
	}
}
