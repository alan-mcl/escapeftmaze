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
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.DamagePacket;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.*;

/**
 *
 */
public class DamageSpellResult extends SpellResult
{
	private ValueList hitPointDamage;
	private ValueList fatigueDamage;
	private ValueList actionPointDamage;
	private ValueList magicPointDamage;
	
	private double multiplier;

	private boolean transferToCaster;

	/*-------------------------------------------------------------------------*/	
	public DamageSpellResult(
		ValueList hitPointDamage,
		ValueList fatigueDamage,
		ValueList actionPointDamage,
		ValueList magicPointDamage)
	{
		this(hitPointDamage, fatigueDamage, actionPointDamage, magicPointDamage, 1, false);
	}
	
	/*-------------------------------------------------------------------------*/
	public DamageSpellResult(
		ValueList hitPointDamage,
		ValueList fatigueDamage,
		ValueList actionPointDamage,
		ValueList magicPointDamage,
		double multiplier,
		boolean transferToCaster)
	{
		this.magicPointDamage = magicPointDamage;
		this.actionPointDamage = actionPointDamage;
		this.fatigueDamage = fatigueDamage;
		this.hitPointDamage = hitPointDamage;
		this.multiplier = multiplier;
		this.transferToCaster = transferToCaster;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target, int castingLevel, SpellEffect parent)
	{
		Maze.log(Log.DEBUG, "Applying DamageSpellResult from ["+source.getName()+
			"] to ["+target.getName()+"] casting lvl ["+castingLevel+"]");

		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (hitPointDamage != null)
		{
			int damage = calcDamage(this.hitPointDamage, source, castingLevel);
			result.add(new DamageEvent(
				null,
				target,
				source,
				new DamagePacket(damage, 1), 
				parent.getType(),
				parent.getSubType(),
				null,
				null));
			if (transferToCaster)
			{
				result.add(new HealingEvent(source, damage));
			}
		}
		
		if (fatigueDamage != null)
		{
			int damage = calcDamage(fatigueDamage, source, castingLevel);
			result.add(new FatigueEvent(
				target,
				source,
				damage,
				parent.getType(),
				parent.getSubType()));
			if (transferToCaster)
			{
				result.add(new StaminaEvent(source, damage));
			}
		}

		if (actionPointDamage != null)
		{
			int damage = calcDamage(actionPointDamage, source, castingLevel);
			result.add(new DamageActionPointsEvent(
				target,
				source,
				damage,
				parent.getType(),
				parent.getSubType()));
			if (transferToCaster)
			{
				result.add(new RestoreStealthEvent(source, damage));
			}
		}
		
		if (magicPointDamage != null)
		{
			int damage = calcDamage(magicPointDamage, source, castingLevel);
			result.add(new DamageMagicEvent(
				target,
				source,
				damage,
				parent.getType(),
				parent.getSubType()));
			if (transferToCaster)
			{
				result.add(new RestoreMagicEvent(source, damage));
			}
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private int calcDamage(ValueList value, UnifiedActor source, int castingLevel)
	{
		int damage = value.compute(source, castingLevel);

		if (multiplier != 0)
		{
			damage = (int)Math.round(damage*multiplier);
		}
		return damage;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getHitPointDamage()
	{
		return hitPointDamage;
	}

	/*-------------------------------------------------------------------------*/
	public double getMultiplier()
	{
		return multiplier;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getFatigueDamage()
	{
		return fatigueDamage;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getMagicPointDamage()
	{
		return magicPointDamage;
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getActionPointDamage()
	{
		return actionPointDamage;
	}

	/*-------------------------------------------------------------------------*/
	public boolean transferToCaster()
	{
		return transferToCaster;
	}
}
