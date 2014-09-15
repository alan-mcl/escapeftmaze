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
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.DamageEvent;

/**
 * Damages a specific foe type, eg "undead"
 */
public class DamageFoeTypeSpellResult extends SpellResult
{
	private Value damage;
	private double multiplier;
	private String type;

	/*-------------------------------------------------------------------------*/	
	public DamageFoeTypeSpellResult(Value damage, String type)
	{
		this(damage, 1, type);
	}
	
	/*-------------------------------------------------------------------------*/
	public DamageFoeTypeSpellResult(Value damage, double multiplier, String type)
	{
		this.damage = damage;
		this.multiplier = multiplier;
		this.type = type;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target, int castingLevel, SpellEffect parent)
	{
		if (target.getType().equals(this.type))
		{
			int damage = this.damage.compute(source, castingLevel);

			if (multiplier != 0)
			{
				damage = (int)Math.round(damage*multiplier);
			}

			return getList(
					new DamageEvent(
						target,
						source,
						new DamagePacket(damage, 1), 
						parent.getType(),
						parent.getSubType(), 
						null, null));
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Value getDamage()
	{
		return damage;
	}

	/*-------------------------------------------------------------------------*/
	public double getMultiplier()
	{
		return multiplier;
	}
	
	/*-------------------------------------------------------------------------*/
	public String getType()
	{
		return type;
	}
}
