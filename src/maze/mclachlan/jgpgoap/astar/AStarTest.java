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
 *
 */
public class AStarTest
{
	public static void main(String[] args) throws Exception
	{
		AStar<String, Integer> test = new AStar<String, Integer>();

		List<Integer> path =
			test.astar(new AStarClient<String, Integer>()
			{
				public String getStart()
				{
					return "start";
				}

				public String getGoal()
				{
					return "the_goal";
				}

				public Map<Integer, String> getNeighbours(String node)
				{
					if (node.equals("start"))
					{
						HashMap<Integer, String> result = new HashMap<Integer, String>();
						result.put(10, "one");
						result.put(20, "two");
						result.put(30, "three");
						return result;
					}
					else if (node.equals("one"))
					{
						HashMap<Integer, String> result = new HashMap<Integer, String>();
						result.put(90, "the_goal");
						result.put(1, "two");
						return result;
					}
					else if (node.equals("two"))
					{
						HashMap<Integer, String> result = new HashMap<Integer, String>();
						result.put(20, "the_goal");
						return result;
					}
					else if (node.equals("three"))
					{
						HashMap<Integer, String> result = new HashMap<Integer, String>();
						result.put(30, "the_goal");
						return result;
					}
					else
					{
						throw new RuntimeException(node);
					}
				}

				public int getCost(String from, Integer edge, String to)
				{
					return edge.intValue();
				}

				public int getHeuristicCost(String from, String to)
				{
					return to.length() - from.length();
				}

				public boolean hasMetGoal(String node)
				{
					return node.equals("the_goal");
				}
			});

		System.out.println("path = [" + path + "]");

	}
}
