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
import mclachlan.maze.stat.DamagePacket;
import mclachlan.maze.stat.TypeDescriptor;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.DamageEvent;

/**
 * Damages a specific foe type, eg "undead"
 */
public class DamageFoeTypeSpellResult extends SpellResult
{
	private ValueList damage;
	private double multiplier;
	private TypeDescriptor type;

	/*-------------------------------------------------------------------------*/	
	public DamageFoeTypeSpellResult(ValueList damage, TypeDescriptor type)
	{
		this(damage, 1, type);
	}
	
	/*-------------------------------------------------------------------------*/
	public DamageFoeTypeSpellResult(ValueList damage, double multiplier, TypeDescriptor type)
	{
		this.damage = damage;
		this.multiplier = multiplier;
		this.type = type;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target,
		int castingLevel, SpellEffect parent, Spell spell)
	{
		if (target.getTypes().contains(this.type))
		{
			int damage = this.damage.compute(source, castingLevel);

			if (multiplier != 0)
			{
				damage = (int)Math.round(damage*multiplier);
			}

			return getList(
					new DamageEvent(
						null,
						target,
						source,
						new DamagePacket(damage, 1), 
						parent.getType(),
						parent.getSubType(), 
						null,
						null,
						null));
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getDamage()
	{
		return damage;
	}

	/*-------------------------------------------------------------------------*/
	public double getMultiplier()
	{
		return multiplier;
	}
	
	/*-------------------------------------------------------------------------*/
	public TypeDescriptor getType()
	{
		return type;
	}
}
