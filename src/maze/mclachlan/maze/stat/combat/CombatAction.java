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

package mclachlan.maze.stat.combat;

import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class CombatAction //extends StatModifier
{
	private UnifiedActor actor;
	private boolean isAttackingAllies;
	private int initiative;
	private boolean initiativeSet = false;
	private StatModifier modifiers;

	/*-------------------------------------------------------------------------*/
	public static final CombatAction DO_NOTHING = new CombatAction()
	{
		@Override
		public String toString()
		{
			return "DO_NOTHING";
		}
	};

	/*-------------------------------------------------------------------------*/
	public CombatAction()
	{
		this.modifiers = new StatModifier();
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getActor()
	{
		return actor;
	}

	/*-------------------------------------------------------------------------*/
	public void setActor(UnifiedActor actor)
	{
		this.actor = actor;
	}

	public boolean isAttackingAllies()
	{
		return isAttackingAllies;
	}

	public void setAttackingAllies(boolean isAttackingAllies)
	{
		this.isAttackingAllies = isAttackingAllies;
	}

	public int getInitiative()
	{
		return initiative;
	}

	public void setInitiative(int initiative)
	{
		this.initiative = initiative;
		this.initiativeSet = true;
	}

	public boolean isInitiativeSet()
	{
		return initiativeSet;
	}

	public void setInitiativeSet(boolean initiativeSet)
	{
		this.initiativeSet = initiativeSet;
	}

	public StatModifier getModifiers()
	{
		return modifiers;
	}

	public void setModifiers(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}

	public void setModifier(Stats.Modifier mod, int value)
	{
		modifiers.setModifier(mod, value);
	}

	public int getModifier(Stats.Modifier mod)
	{
		return modifiers.getModifier(mod);
	}
}
