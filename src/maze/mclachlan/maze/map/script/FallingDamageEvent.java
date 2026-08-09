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

import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.UiMessageEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.event.DamageEvent;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.NullActor;

/**
 * Deals configurable damage to every living party member who is not flying.
 */
public class FallingDamageEvent extends MazeEvent
{
	private Dice damage;

	public FallingDamageEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public FallingDamageEvent(Dice damage)
	{
		this.damage = damage;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getText()
	{
		return StringUtil.getEventText("msg.falling.damage");
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<>();
		int amount = damage.roll("falling damage");

		for (UnifiedActor actor : Maze.getInstance().getParty().getActors())
		{
			if (!actor.isAlive())
			{
				continue;
			}
			if (actor.getModifier(Stats.Modifier.FLIER) > 0)
			{
				result.add(new UiMessageEvent(StringUtil.getEventText("msg.avoids.falling.damage", actor.getName())));
				continue;
			}

			result.add(new DamageEvent(
				null,
				actor,
				new NullActor(),
				new DamagePacket(amount, 1),
				MagicSys.SpellEffectType.BLUDGEONING,
				MagicSys.SpellEffectSubType.NORMAL_DAMAGE,
				null,
				null,
				null));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public Dice getDamage()
	{
		return damage;
	}

	public void setDamage(Dice damage)
	{
		this.damage = damage;
	}

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

		FallingDamageEvent that = (FallingDamageEvent)o;

		return Objects.equals(getDamage(), that.getDamage());
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(getDamage());
	}
}
