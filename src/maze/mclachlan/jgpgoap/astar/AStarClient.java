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

package mclachlan.jgpgoap.astar;

import java.util.*;

/**
 * A client used by the A* implementation to retrieve information about the
 * graph to traverse.
 * <p>
 * Clients may have any underlying graph implementation
 */
public interface AStarClient<NodeType,EdgeType>
{
	/**
	 * @return The start of the search
	 */
	NodeType getStart();

	/**
	 * @return The end of the search
	 */
	NodeType getGoal();

	/**
	 * @return All possible neighbours of the given node, and the edges that
	 * lead to them
	 */
	Map<EdgeType,NodeType> getNeighbours(NodeType node);

	/**
	 * @return The cost of traversing the graph along the given edge.
	 */
	int getCost(NodeType from, EdgeType edge, NodeType to);

	/**
	 * @return An estimated cost of traversing the graph between the given two
	 * nodes. Note that fot the purposes of the A* algorithm is is important
	 * that this is an <b>admissible heuristic</b> - one that does not exceed
	 * the actual cost.
	 */
	int getHeuristicCost(NodeType from, NodeType to);

	/**
	 * @return true if the given node meets the goal of this search
	 */
	boolean hasMetGoal(NodeType node);
}
