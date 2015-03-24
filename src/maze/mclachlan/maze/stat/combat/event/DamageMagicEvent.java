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

package mclachlan.maze.stat.combat.event;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.magic.MagicSys;

/**
 *
 */
public class DamageMagicEvent extends MazeEvent
{
	private UnifiedActor defender;
	private UnifiedActor attacker;
	private int damage;
	private MagicSys.SpellEffectType type;
	private MagicSys.SpellEffectSubType subtype;

	/*-------------------------------------------------------------------------*/
	public DamageMagicEvent(
		UnifiedActor defender,
		UnifiedActor attacker,
		int damage,
		MagicSys.SpellEffectType type,
		MagicSys.SpellEffectSubType subtype)
	{
		this.defender = defender;
		this.attacker = attacker;
		this.damage = damage;
		this.type = type;
		this.subtype = subtype;
	}
	
	/*-------------------------------------------------------------------------*/
	public int getDamage()
	{
		return damage;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return getDamage()+" magic points lost";
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Applies the damage to the defender. 
	 */ 
	public List<MazeEvent> resolve()
	{
		// apply resistances and immunities
		if (GameSys.getInstance().isActorImmuneToSpellEffect(defender, subtype))
		{
			damage = 0;
		}
		else
		{
			damage -= (damage * GameSys.getInstance().getResistance(defender, attacker, type) / 100);
		}

		CurMax magicPoints = defender.getMagicPoints();
		magicPoints.decCurrent(damage);
		if (magicPoints.getCurrent() < 0)
		{
			magicPoints.setCurrent(0);
		}
		
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}
}
