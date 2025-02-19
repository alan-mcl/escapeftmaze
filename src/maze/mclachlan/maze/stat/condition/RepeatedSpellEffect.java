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

package mclachlan.maze.stat.condition;

import mclachlan.maze.data.v2.V2Seralisable;

/**
 * Represents spell effects that are repeatedly applied to the bearer of
 * this condition.
 */
public class RepeatedSpellEffect implements V2Seralisable
{
	/** turn this effect starts */
	private int startTurn;

	/** turn this effect ends */
	private int endTurn;

	/** effect occurs every n-th turn. e.g. turnMod=2 for every second turn */
	private int turnMod;

	/** probability of spell effect occurring */
	private int probability;

	/** the spell effect to be applied to the bearer of this condition */
	private String spellEffect;

	public RepeatedSpellEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public RepeatedSpellEffect(
		int startTurn,
		int endTurn,
		int turnMod,
		int probability,
		String spellEffect)
	{
		this.startTurn = startTurn;
		this.endTurn = endTurn;
		this.turnMod = turnMod;
		this.probability = probability;
		this.spellEffect = spellEffect;
	}

	/*-------------------------------------------------------------------------*/
	public int getStartTurn()
	{
		return startTurn;
	}

	public void setStartTurn(int startTurn)
	{
		this.startTurn = startTurn;
	}

	public int getEndTurn()
	{
		return endTurn;
	}

	public void setEndTurn(int endTurn)
	{
		this.endTurn = endTurn;
	}

	public int getTurnMod()
	{
		return turnMod;
	}

	public void setTurnMod(int turnMod)
	{
		this.turnMod = turnMod;
	}

	public String getSpellEffect()
	{
		return spellEffect;
	}

	public void setSpellEffect(String spellEffect)
	{
		this.spellEffect = spellEffect;
	}

	public int getProbability()
	{
		return probability;
	}

	public void setProbability(int probability)
	{
		this.probability = probability;
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

		RepeatedSpellEffect that = (RepeatedSpellEffect)o;

		if (getStartTurn() != that.getStartTurn())
		{
			return false;
		}
		if (getEndTurn() != that.getEndTurn())
		{
			return false;
		}
		if (getTurnMod() != that.getTurnMod())
		{
			return false;
		}
		if (getProbability() != that.getProbability())
		{
			return false;
		}
		return getSpellEffect() != null ? getSpellEffect().equals(that.getSpellEffect()) : that.getSpellEffect() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getStartTurn();
		result = 31 * result + getEndTurn();
		result = 31 * result + getTurnMod();
		result = 31 * result + getProbability();
		result = 31 * result + (getSpellEffect() != null ? getSpellEffect().hashCode() : 0);
		return result;
	}
}
