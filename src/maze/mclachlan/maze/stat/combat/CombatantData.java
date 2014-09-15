
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

import java.util.*;
import mclachlan.maze.stat.*;

/**
 * Temp data for each combat round
 */
public class CombatantData
{
	private CombatAction currentAction;
	private ActorActionIntention currentIntention;
	private StatModifier miscModifiers;
	private Combat combat;
	private ActorGroup group;
	private List<FoeGroup> summonedGroups;

	private int intiative;
	private boolean active=true;
	private boolean displaced =false;

	public void startRound()
	{
		this.currentAction = null;
		this.currentIntention = null;
		this.miscModifiers = new StatModifier();
	}

	public void endRound()
	{
		this.displaced = false;
	}

	public CombatAction getCurrentAction()
	{
		return currentAction;
	}

	public ActorActionIntention getCurrentIntention()
	{
		return currentIntention;
	}

	public StatModifier getMiscModifiers()
	{
		return miscModifiers;
	}

	public Combat getCombat()
	{
		return combat;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}


	public List<FoeGroup> getSummonedGroups()
	{
		return summonedGroups;
	}

	public void setSummonedGroup(List<FoeGroup> summonedGroups)
	{
		this.summonedGroups = summonedGroups;
	}

	public boolean canSummon()
	{
		if (summonedGroups != null)
		{
			for (FoeGroup fg : summonedGroups)
			{
				if (fg.numAlive() > 0)
				{
					return false;
				}
			}
		}
		
		return true;
	}

	public boolean isDisplaced()
	{
		return displaced;
	}

	public void setDisplaced(boolean displaced)
	{
		this.displaced = displaced;
	}

	public void setCombat(Combat combat)
	{
		this.combat = combat;
	}

	public void setGroup(ActorGroup group)
	{
		this.group = group;
	}

	public void setCurrentIntention(ActorActionIntention currentIntention)
	{
		this.currentIntention = currentIntention;
	}

	public void setCurrentAction(CombatAction currentAction)
	{
		this.currentAction = currentAction;
	}

	public boolean isActive()
	{
		return active;
	}

	public ActorGroup getGroup()
	{
		return group;
	}

	public int getIntiative()
	{
		return intiative;
	}

	public void setIntiative(int intiative)
	{
		this.intiative = intiative;
	}
}
