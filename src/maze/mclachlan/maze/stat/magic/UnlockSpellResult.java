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
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Trap;
import mclachlan.maze.map.script.LockOrTrap;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.FailureEvent;
import mclachlan.maze.stat.combat.event.NoEffectEvent;
import mclachlan.maze.stat.combat.event.SuccessEvent;
import mclachlan.maze.util.MazeException;

/**
 * A spell result that opens lock or disarms traps
 */
public class UnlockSpellResult extends SpellResult
{
	private ValueList value;

	public UnlockSpellResult()
	{
	}

	/*-------------------------------------------------------------------------*/
	public UnlockSpellResult(ValueList value)
	{
		this.value = value;
	}

	public ValueList getValue()
	{
		return value;
	}

	public void setValue(ValueList value)
	{
		this.value = value;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> apply(
		UnifiedActor caster,
		LockOrTrap lockOrTrap,
		int castingLevel,
		SpellEffect spellEffect)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		//
		// Deal with traps first. Multiple spells needed to deal with something
		// that's both locked and trapped
		//

		if (lockOrTrap.isTrapped())
		{
			Trap trap = lockOrTrap.getCurrentTrap();
			if (trap == null)
			{
				result.addAll(disarmResultEvents(Trap.DisarmResult.NOTHING));
				result.addAll(lockOrTrap.executeTrapDisarmed());
				return result;
			}

			ValueList modifier = ((UnlockSpellResult)spellEffect.getUnsavedResult()).getValue();

			BitSet disarmed = new BitSet(8);
			for (int tool=0; tool<8; tool++)
			{
				if (!trap.getRequired().get(tool))
				{
					continue;
				}

				int disarmWithSpellResult = GameSys.getInstance().disarmWithSpell(
					caster, castingLevel, modifier, trap, tool);

				if (disarmWithSpellResult == Trap.DisarmResult.SPRING_TRAP)
				{
					result.addAll(disarmResultEvents(disarmWithSpellResult));
					result.addAll(lockOrTrap.springTrap());
					return result;
				}
				else if (disarmWithSpellResult == Trap.DisarmResult.DISARMED)
				{
					disarmed.set(tool);
				}
			}

			if (disarmed.equals(trap.getRequired()))
			{
				result.addAll(disarmResultEvents(Trap.DisarmResult.DISARMED));
				result.addAll(lockOrTrap.executeTrapDisarmed());
			}
			else
			{
				result.addAll(disarmResultEvents(Trap.DisarmResult.NOTHING));
			}
		}
		else if (lockOrTrap.isLocked())
		{
			ValueList modifier = ((UnlockSpellResult)spellEffect.getUnsavedResult()).getValue();

			BitSet disarmed = new BitSet(8);
			for (int tool=0; tool<8; tool++)
			{
				if (!lockOrTrap.getPickLockToolsRequired().get(tool))
				{
					continue;
				}

				int unlockResult = GameSys.getInstance().pickLockWithSpell(
					caster, castingLevel, modifier, lockOrTrap, tool);

				if (unlockResult == Trap.DisarmResult.SPRING_TRAP)
				{
					result.addAll(disarmResultEvents(Trap.DisarmResult.SPRING_TRAP));
				}
				else if (unlockResult == Trap.DisarmResult.DISARMED)
				{
					disarmed.set(tool);
				}
			}

			if (disarmed.equals(lockOrTrap.getPickLockToolsRequired()))
			{
				result.addAll(disarmResultEvents(Trap.DisarmResult.DISARMED));
				lockOrTrap.setLockState(Portal.State.UNLOCKED);
			}
			else
			{
				result.addAll(disarmResultEvents(Trap.DisarmResult.NOTHING));
			}
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
			case Trap.DisarmResult.NOTHING -> result.add(new NoEffectEvent());
			case Trap.DisarmResult.DISARMED -> result.add(new SuccessEvent());
			case Trap.DisarmResult.SPRING_TRAP -> result.add(new FailureEvent());
			default -> throw new MazeException("Invalid result: " + disarmResult);
		}

		return result;
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

		UnlockSpellResult that = (UnlockSpellResult)o;

		return getValue() != null ? getValue().equals(that.getValue()) : that.getValue() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
		return result;
	}
}
