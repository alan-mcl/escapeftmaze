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

package mclachlan.maze.campaign.def.stat.magic;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.Trap;
import mclachlan.maze.map.script.GrantItemsEvent;
import mclachlan.maze.map.script.LockOrTrap;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.FailureEvent;
import mclachlan.maze.stat.combat.event.NoEffectEvent;
import mclachlan.maze.stat.combat.event.SuccessEvent;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.magic.SpellResult;
import mclachlan.maze.util.MazeException;

/**
 * A spell result that opens lock or disarms traps
 */
public class DestroyTrapSpellResult extends SpellResult
{
	@Override
	public List<MazeEvent> apply(
		UnifiedActor caster,
		LockOrTrap lockOrTrap,
		int castingLevel,
		SpellEffect spellEffect)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (lockOrTrap.isTrapped())
		{
			Trap trap = lockOrTrap.getCurrentTrap();
			if (trap == null)
			{
				result.addAll(disarmResultEvents(Trap.DisarmResult.NOTHING));
				result.addAll(lockOrTrap.executeTrapDisarmed());
				return result;
			}

			// spring the trap

			result.addAll(lockOrTrap.springTrap());

			List<Item> items = getGadgets(castingLevel);

			result.add(new GrantItemsEvent(items));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<Item> getGadgets(int castingLevel)
	{
		ArrayList<Item> result = new ArrayList<Item>();
		int max = new Dice(castingLevel,3,1).roll("destroy trap max");
		for (int i=0; i< max; i++)
		{
			LootEntry gadgets;

			if (Dice.d100.roll("destroy trap table") > 5)
			{
				gadgets = Database.getInstance().getLootEntry("batch.1.gadgets");
			}
			else
			{
				gadgets = Database.getInstance().getLootEntry("batch.2.gadgets");
			}

			result.add(gadgets.generate());
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static List<MazeEvent> disarmResultEvents(
		int disarmResult)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		switch (disarmResult)
		{
			case Trap.DisarmResult.NOTHING:
				result.add(new NoEffectEvent());
				break;
			case Trap.DisarmResult.DISARMED:
				result.add(new SuccessEvent());
				break;
			case Trap.DisarmResult.SPRING_TRAP:
				result.add(new FailureEvent());
				break;
			default:
				throw new MazeException("Invalid result: "+disarmResult);
		}

		return result;
	}

}
