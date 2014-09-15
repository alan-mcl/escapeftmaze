/*
 * Copyright (c) 2013 Alan McLachlan
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

package mclachlan.maze.game.goapai;

import java.util.*;
import mclachlan.jgpgoap.goap.Action;
import mclachlan.jgpgoap.goap.Atom;
import mclachlan.jgpgoap.goap.GOAP;
import mclachlan.jgpgoap.goap.WorldState;
import mclachlan.maze.game.FoeCombatAi;
import mclachlan.maze.stat.ActorActionIntention;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.Combat;

/**
 * Foe AI implemented using a goal oriented action planner.
 */
public class GOAPFoeAI extends FoeCombatAi
{

	private static final String IS_ALIVE = "_IS_ALIVE";
	private static final String IS_CONSCIOUS = "_IS_CONSCIOUS";

	/*-------------------------------------------------------------------------*/
	@Override
	public ActorActionIntention getCombatIntention(Foe foe, Combat combat)
	{
		WorldState start = initStartingWorldState(foe, combat);
		WorldState end = initTargetWorldState(foe, combat);
		Set<Action> actions = initPossibleActions(foe, combat);
		
		GOAP goap = new GOAP(start, end, actions);

		List<Action> plan = goap.plan();

		return getCombatIntention(plan.get(0));
	}

	/*-------------------------------------------------------------------------*/
	private ActorActionIntention getCombatIntention(Action action)
	{
		// todo

		return null;
	}

	/*-------------------------------------------------------------------------*/
	private Set<Action> initPossibleActions(Foe foe, Combat combat)
	{
		Set<Action> result = new HashSet<Action>();

		// todo

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private WorldState initStartingWorldState(Foe foe, Combat combat)
	{
		WorldState ws = new WorldState();

		for (UnifiedActor a : combat.getAllActors())
		{
			ws.add(new Atom(getUniqueId(a)+IS_ALIVE, a.isAlive()));
			ws.add(new Atom(getUniqueId(a)+IS_CONSCIOUS, a.isConscious()));
		}

		return ws;
	}

	/*-------------------------------------------------------------------------*/
	private WorldState initTargetWorldState(Foe foe, Combat combat)
	{
		WorldState ws = new WorldState();

		ws.add(new Atom(getUniqueId(foe)+IS_ALIVE, true));

		for (UnifiedActor a : combat.getAllFoesOf(foe))
		{
			ws.add(new Atom(getUniqueId(a)+IS_ALIVE, false));
		}

		return ws;
	}

	/*-------------------------------------------------------------------------*/
	private int getUniqueId(UnifiedActor a)
	{
		return a.hashCode();
	}
}
