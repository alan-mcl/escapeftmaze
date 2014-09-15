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

/**
 * Represents an action available to the AI that changes the world state.
 * An AI can take this action as long as the pre-conditions in the world state
 * are met. The post-conditions define how taking this action alters the
 * world state. Each action has an associated cost.
 */
public class Action
{
	private String name;
	private WorldState pre, post;
	private int cost;

	/*-------------------------------------------------------------------------*/

	/**
	 * @param name
	 * 	The name of this action
	 * @param pre
	 * 	Pre-conditions that must exist in the world state before an AI can
	 * 	take this action
	 * @param post
	 * 	These post-conditions define how taking this action changes the
	 * 	world state
	 * @param cost
	 * 	The cost of the AI taking this action
	 */
	public Action(String name, WorldState pre, WorldState post, int cost)
	{
		this.name = name;
		this.pre = pre;
		this.post = post;
		this.cost = cost;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public WorldState getPre()
	{
		return pre;
	}

	public void setPre(WorldState pre)
	{
		this.pre = pre;
	}

	public WorldState getPost()
	{
		return post;
	}

	public void setPost(WorldState post)
	{
		this.post = post;
	}

	public int getCost()
	{
		return cost;
	}

	public void setCost(int cost)
	{
		this.cost = cost;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("Action");
		sb.append("{name='").append(name).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
