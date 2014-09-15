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
 * Implements an A* shortest-path search of a graph with nodes of type NodeType and
 * edges of type EdgeType.
 */
public class AStar<NodeType,EdgeType>
{
	/**
	 * @return the best list of edges that take us from the start to the goal
	 */
	public List<EdgeType> astar(AStarClient<NodeType,EdgeType> client)
	{
		Queue<AStarNode<NodeType,EdgeType>> open = new PriorityQueue<AStarNode<NodeType,EdgeType>>();
		Set<AStarNode<NodeType,EdgeType>> closed = new HashSet<AStarNode<NodeType,EdgeType>>();

		NodeType start = client.getStart();
		NodeType goal = client.getGoal();

		open.offer(new AStarNode<NodeType, EdgeType>(start, null, null, 0, 0));


		while (!client.hasMetGoal(open.peek().getContents()))
		{
			AStarNode<NodeType, EdgeType> current = open.poll();
			System.out.println("open = [" + open + "]");
			closed.add(current);

			Map<EdgeType,NodeType> neighbours = client.getNeighbours(current.getContents());
			for (EdgeType edge : neighbours.keySet())
			{
				NodeType neighbour = neighbours.get(edge);

				int cost = current.getG() + client.getCost(current.getContents(), edge, neighbour);

				boolean found = false;

				// if neighbor in OPEN and cost less than g(neighbor):
				// remove neighbor from OPEN, because new path is better
				for (AStarNode openNode : open)
				{
					if (openNode.getContents().equals(neighbour))
					{
						if (cost < openNode.getG())
						{
							open.remove(openNode);
							break;
						}
						else
						{
							found = true;
							break;
						}
					}
				}

				// if neighbor in CLOSED and cost less than g(neighbor):
				// remove neighbor from CLOSED
				for (AStarNode closedNode : closed)
				{
					if (closedNode.getContents().equals(neighbour))
					{
						if (cost < closedNode.getG())
						{
							closed.remove(closedNode);
							break;
						}
						else
						{
							found = true;
							break;
						}
					}
				}

				//if neighbor not in OPEN and neighbor not in CLOSED:
				if (!found)
				{
					open.offer(new AStarNode<NodeType,EdgeType>(
						neighbour, edge, current, cost, client.getHeuristicCost(neighbour, goal)));
				}
			}
		}

		// reconstruct the path backwards
		AStarNode<NodeType,EdgeType> node = open.peek();

		List<EdgeType> result = new ArrayList<EdgeType>();
		while (!node.getContents().equals(start))
		{
			result.add(0, node.getEdge());
			node = node.getSource();
		}

		return result;
	}
}
