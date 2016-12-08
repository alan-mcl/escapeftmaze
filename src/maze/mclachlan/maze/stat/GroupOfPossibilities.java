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

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.maze.util.MazeException;

/**
 * Represents a list of items, each with an associated probability of occurring.
 * Calling code can ask for a random list of items to be returned: each entry
 * in the group is rolled for once individually and may be included in the
 * list.
 */
public class GroupOfPossibilities<T>
{
	private List<T> possibilities;
	private List<Integer> percentages;

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a new, empty GroupOfPossibilities
	 */
	public GroupOfPossibilities()
	{
		this(new ArrayList<T>(), new ArrayList<Integer>());
	}

	/*-------------------------------------------------------------------------*/
	public GroupOfPossibilities(GroupOfPossibilities<T> other)
	{
		this(new ArrayList<T>(other.possibilities), new ArrayList<Integer>(other.percentages));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param possibilities
	 * 	The items in the list
	 * @param percentages
	 * 	The percentage chance of occurrence associated with each item
	 */
	public GroupOfPossibilities(List<T> possibilities, List<Integer> percentages)
	{
		this.possibilities = possibilities;
		this.percentages = percentages;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Add an item to this table
	 * @param possibility
	 * 	The item to add
	 * @param percentage
	 * 	The percentage chance of occurance
	 */
	public void add(T possibility, int percentage)
	{
		if (possibility == null)
		{
			throw new MazeException("Invalid possiblity: "+possibility);
		}

		this.possibilities.add(possibility);
		this.percentages.add(percentage);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A randomly generated, unsorted list of items.  An empty list will
	 * 	be returned if no items occur.
	 */
	public List<T> getRandom()
	{
		List<T> result = new ArrayList<T>();

		for (int i=0; i< possibilities.size(); i++)
		{
			if (Dice.d100.roll() <= percentages.get(i))
			{
				result.add(possibilities.get(i));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<Integer> getPercentages()
	{
		return percentages;
	}

	public List<T> getPossibilities()
	{
		return possibilities;
	}

	public int getPercentage(T possibility)
	{
		int index = possibilities.indexOf(possibility);
		if (index == -1)
		{
			throw new MazeException("Invalid possibility: "+possibility);
		}

		return percentages.get(index);
	}

	/*-------------------------------------------------------------------------*/
	public boolean isEmpty()
	{
		return this.percentages.isEmpty();
	}

	/*-------------------------------------------------------------------------*/
	public void addAll(GroupOfPossibilities<T> other)
	{
		if (other != null)
		{
			this.percentages.addAll(other.getPercentages());
			this.possibilities.addAll(other.getPossibilities());
		}
	}
}
