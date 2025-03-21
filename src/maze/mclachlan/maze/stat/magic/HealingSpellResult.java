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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.HealingEvent;
import mclachlan.maze.stat.combat.event.RestoreActionPointsEvent;
import mclachlan.maze.stat.combat.event.RestoreMagicEvent;
import mclachlan.maze.stat.combat.event.StaminaEvent;

/**
 *
 */
public class HealingSpellResult extends SpellResult
{
	private ValueList hitPointHealing;
	private ValueList staminaHealing;
	private ValueList actionPointHealing;
	private ValueList magicPointHealing;

	public HealingSpellResult()
	{
	}

	/*-------------------------------------------------------------------------*/
	public HealingSpellResult(
		ValueList hitPointHealing,
		ValueList staminaHealing,
		ValueList actionPointHealing,
		ValueList magicPointHealing)
	{
		this.magicPointHealing = magicPointHealing;
		this.actionPointHealing = actionPointHealing;
		this.staminaHealing = staminaHealing;
		this.hitPointHealing = hitPointHealing;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target,
		int castingLevel, SpellEffect parent, Spell spell)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		int empoweredHealing = source.getModifier(Stats.Modifier.EMPOWERED_HEALING);

		if (hitPointHealing != null)
		{
			int amount = hitPointHealing.compute(source, castingLevel) + empoweredHealing;
			result.add(new HealingEvent(target, amount));
		}
		
		if (staminaHealing != null)
		{
			int amount = staminaHealing.compute(source, castingLevel) + empoweredHealing;
			result.add(new StaminaEvent(target, amount));
		}
		
		if (actionPointHealing != null)
		{
			int amount = actionPointHealing.compute(source, castingLevel) + empoweredHealing;
			result.add(new RestoreActionPointsEvent(target, amount));
		}
		
		if (magicPointHealing != null)
		{
			int amount = magicPointHealing.compute(source, castingLevel) + empoweredHealing;
			result.add(new RestoreMagicEvent(target, amount));
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getHitPointHealing()
	{
		return hitPointHealing;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getMagicPointHealing()
	{
		return magicPointHealing;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getStaminaHealing()
	{
		return staminaHealing;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getActionPointHealing()
	{
		return actionPointHealing;
	}

	public void setHitPointHealing(ValueList hitPointHealing)
	{
		this.hitPointHealing = hitPointHealing;
	}

	public void setStaminaHealing(ValueList staminaHealing)
	{
		this.staminaHealing = staminaHealing;
	}

	public void setActionPointHealing(
		ValueList actionPointHealing)
	{
		this.actionPointHealing = actionPointHealing;
	}

	public void setMagicPointHealing(ValueList magicPointHealing)
	{
		this.magicPointHealing = magicPointHealing;
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

		HealingSpellResult that = (HealingSpellResult)o;

		if (getHitPointHealing() != null ? !getHitPointHealing().equals(that.getHitPointHealing()) : that.getHitPointHealing() != null)
		{
			return false;
		}
		if (getStaminaHealing() != null ? !getStaminaHealing().equals(that.getStaminaHealing()) : that.getStaminaHealing() != null)
		{
			return false;
		}
		if (getActionPointHealing() != null ? !getActionPointHealing().equals(that.getActionPointHealing()) : that.getActionPointHealing() != null)
		{
			return false;
		}
		return getMagicPointHealing() != null ? getMagicPointHealing().equals(that.getMagicPointHealing()) : that.getMagicPointHealing() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getHitPointHealing() != null ? getHitPointHealing().hashCode() : 0);
		result = 31 * result + (getStaminaHealing() != null ? getStaminaHealing().hashCode() : 0);
		result = 31 * result + (getActionPointHealing() != null ? getActionPointHealing().hashCode() : 0);
		result = 31 * result + (getMagicPointHealing() != null ? getMagicPointHealing().hashCode() : 0);
		return result;
	}
}
