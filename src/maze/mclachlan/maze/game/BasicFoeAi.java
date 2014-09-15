/*
 * Copyright (c) 2012 Alan McLachlan
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

package mclachlan.maze.game;

import mclachlan.maze.stat.ActorActionIntention;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.DefendIntention;
import mclachlan.maze.stat.combat.HideIntention;
import mclachlan.maze.stat.combat.RunAwayIntention;

/**
 * Foe AI that simply picks a random attack with a random legal target, or
 * an appropriate stealth action.
 */
public class BasicFoeAi extends FoeCombatAi
{
	/*-------------------------------------------------------------------------*/

	@Override
	public ActorActionIntention getCombatIntention(Foe foe, Combat combat)
	{
		if (!foe.isSummoned() && Dice.d100.roll() <= foe.getFleeChance())
		{
			// summoned foes never run away
			return new RunAwayIntention();
		}

		int engagementRange = combat.getFoeEngagementRange(foe);
		int possibleGroups = combat.getNrOfEnemyGroups(foe);
		Dice possDice = new Dice(1, possibleGroups, -1);

		if (foe.getStealthBehaviour() == Foe.StealthBehaviour.STEALTH_RELIANT &&
			foe.getActionPoints().getRatio() < 0.5)
		{
			// stealth foe, needs to hide
			return new HideIntention();
		}

		if (foe.getStealthBehaviour() == Foe.StealthBehaviour.OPPORTUNISTIC &&
			foe.getActionPoints().getCurrent() < 10 &&
			Dice.d2.roll() == 1)
		{
			// opportunistic hide attempt
			return new HideIntention();
		}

		// just attack, if possible
		if (foe.canAttack(engagementRange))
		{
			return foe.getFoeAttackIntention(engagementRange, possDice, combat);
		}
		else
		{
			return new DefendIntention();
		}
	}
}
