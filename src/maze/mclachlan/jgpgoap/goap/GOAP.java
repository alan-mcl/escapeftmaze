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

package mclachlan.jgpgoap.goap;

import mclachlan.jgpgoap.astar.AStar;
import mclachlan.jgpgoap.astar.AStarClient;
import java.util.*;

/**
 * A Goal Oriented Action Planner implementation. Give a starting and goal
 * world state and a set of actions that change the world state, the planner
 * will return the least-cost list of actions to get from the start to the
 * goal.
 */
public class GOAP implements AStarClient<WorldState, Action>
{
	private Set<Action> actions;
	private WorldState start, goal;

	/*-------------------------------------------------------------------------*/

	/**
	 * @param start
	 * 	The starting world state
	 * @param goal
	 * 	The target world state
	 * @param actions
	 * 	A set of all possible actions available to the AI to change the world state
	 */
	public GOAP(WorldState start, WorldState goal, Set<Action> actions)
	{
		this.actions = actions;
		this.start = start;
		this.goal = goal;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The least-cost list of actions that move the world from the start
	 * 	state to the desired goal state. Actions are returned in the order in
	 * 	which the AI should execute them
	 */
	public List<Action> plan()
	{
		AStar<WorldState,Action> astar = new AStar<WorldState, Action>();
		return astar.astar(this);
	}

	/*-------------------------------------------------------------------------*/

	public WorldState getStart()
	{
		return start;
	}

	public WorldState getGoal()
	{
		return goal;
	}

	public Map<Action, WorldState> getNeighbours(WorldState node)
	{
		Map<Action, WorldState> result = new HashMap<Action, WorldState>();

		for (Action action : actions)
		{
			if (node.meetsPreCondition(action.getPre()))
			{
				result.put(action, node.applyPostCondition(action.getPost()));
			}
		}

		return result;
	}

	public int getCost(WorldState from, Action edge, WorldState to)
	{
		return edge.getCost();
	}

	public int getHeuristicCost(WorldState from, WorldState to)
	{
		return from.countUnmatchedAtoms(to);
	}

	public boolean hasMetGoal(WorldState node)
	{
		return node.meetsPreCondition(goal);
	}
}
