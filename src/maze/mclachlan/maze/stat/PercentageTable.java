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
 * Represents a list of items each with an associated percentage probability
 * of occurring.  Calling code can then ask for a single random item from
 * the list, which is returned according to the probabilities assigned to each
 * item.  It can optionally enforce that the probabilities must sum up to 100%.
 */
public class PercentageTable<T>
{
	private List<T> items;
	private boolean shouldSumTo100;
	private List<Integer> cumulative;

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a new, empty PercentageTable that must sum up to 100%.
	 */
	public PercentageTable()
	{
		this(new ArrayList<T>(), new ArrayList<Integer>(), true);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a new, empty PercentageTable.
	 *
	 * @param shouldSumTo100
	 * 	set to true if the percentage chances of items in this table should
	 * 	sum to 100.
	 */
	public PercentageTable(boolean shouldSumTo100)
	{
		this(new ArrayList<T>(), new ArrayList<Integer>(), shouldSumTo100);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param items
	 * 	items in this PercentageTable
	 * @param percentages
	 * 	percentage chance associated with each item
	 * @param shouldSumTo100
	 * 	set to true if the percentage chances of items in this table should
	 * 	sum to 100.
	 */
	public PercentageTable(T[] items, Integer[] percentages, boolean shouldSumTo100)
	{
		this(Arrays.asList(items), Arrays.asList(percentages), shouldSumTo100);
	}

	/*-------------------------------------------------------------------------*/
	public PercentageTable(PercentageTable<T> other)
	{
		this.items = new ArrayList<T>(other.items);
		this.cumulative = new ArrayList<Integer>(other.cumulative);
		this.shouldSumTo100 = other.shouldSumTo100;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param items
	 * 	items in this PercentageTable
	 * @param percentages
	 * 	percentage chance associated with each item
	 * @param shouldSumTo100
	 * 	set to true if the percentage chances of items in this table should
	 * 	sum to 100.
	 */ 
	public PercentageTable(
		List<T> items,
		List<Integer> percentages,
		boolean shouldSumTo100)
	{
		this.items = items;
		this.shouldSumTo100 = shouldSumTo100;
		this.cumulative = new ArrayList<Integer>();

		int sum = 0;
		for (int i = 0; i < items.size(); i++)
		{
			if (sum > 100)
			{
				throw new MazeException("Percentages sum up to > 100! ["+percentages+"] ["+items+"]");
			}

			int percent = percentages.get(i);
			
			if (percent < 0 || percent > 100)
			{
				throw new MazeException(
					"Invalid percent for ["+items.get(i)+"]: "+percentages.get(i));
			}
			this.cumulative.add(sum + percent);
			sum += percent;
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param items
	 * 	items in this percentage table
	 * @param weights
	 * 	A parallel list of weight associated with each item. These can be
	 * 	any Double value. Items are assigned a % within the table based on
	 * 	their respective weights.
	 */
	public PercentageTable(List<T> items, List<Double> weights)
	{
		this.items = items;
		this.shouldSumTo100 = true;
		this.cumulative = new ArrayList<Integer>();

		double total = 0;
		for (Double weight : weights)
		{
			total += weight;
		}

		int sum = 0;
		for (int i = 0; i < items.size(); i++)
		{
			int percent = (int)(weights.get(i)/total*100);
			this.cumulative.add(sum + percent);
			sum += percent;
		}

		// adjust for any rounding errors
		// todo: perhaps adjust the largest weighting upwards rather than the last one?
		if (sum < 100)
		{
			this.cumulative.set(this.cumulative.size()-1, 100);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void add(T item, int percent)
	{
		int sum = 0;
		if (!this.items.isEmpty())
		{
			sum = this.cumulative.get(this.cumulative.size()-1);
		}
		this.items.add(item);
		this.cumulative.add(sum+percent);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The items in this percentage table
	 */
	public List<T> getItems()
	{
		return items;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The percentages for each item
	 */
	public List<Integer> getPercentages()
	{
		List<Integer> result = new ArrayList<Integer>(items.size());

		int sum = 0;
		for (Integer i : cumulative)
		{
			result.add(i - sum);
			sum = i;
		}

		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The percentage for the given item
	 */
	public int getPercentage(T t)
	{
		int sum = 0;
		for (int i=0; i<items.size(); i++)
		{
			int current = cumulative.get(i) - sum;
			if (t.equals(items.get(i)))
			{
				return current;
			}
			sum = cumulative.get(i);
		}
		
		throw new MazeException("Invalid key ["+t+"]");
	}

	/*-------------------------------------------------------------------------*/
	public boolean shouldSumTo100()
	{
		return shouldSumTo100;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A random item from this percentage table.  If this table does not need
	 * 	to sum up to 100% then this method can return a null to indicate no
	 * 	item was rolled.  If the table must sum up to 100%, a MazeException is
	 * 	thrown in the case the table has been illegally constructed.
	 */
	public T getRandomItem()
	{
		if (shouldSumTo100 && cumulative.get(cumulative.size()-1) != 100)
		{
			throw new MazeException("Percents sum up to "+cumulative.get(cumulative.size()-1));
		}

		int roll = Dice.d100.roll();
		
		for (int i = 0; i < items.size(); i++)
		{
			if (roll <= cumulative.get(i))
			{
				return items.get(i);
			}
		}

		if (!shouldSumTo100)
		{
			// return null to indicate no result
			return null;
		}
		else
		{
			// a fuck up
			throw new MazeException("Unable to get a random item on roll "+roll);
		}
	}

	/*-------------------------------------------------------------------------*/
	public int size()
	{
		return this.getItems().size();
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("PercentageTable");
		sb.append("{items=").append(items);
		sb.append(", shouldSumTo100=").append(shouldSumTo100);
		sb.append(", cumulative=").append(cumulative);
		sb.append('}');
		return sb.toString();
	}
}

