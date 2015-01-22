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

/**
 * Stores a current/maximum pair
 */
public class CurMax
{
	private int current, maximum;
	
	/*-------------------------------------------------------------------------*/
	public CurMax()
	{
		this(0,0);
	}

	/*-------------------------------------------------------------------------*/
	public CurMax(int maximum)
	{
		this(maximum, maximum);
	}
	
	/*-------------------------------------------------------------------------*/
	public CurMax(int current, int maximum)
	{
		this.current = current;
		this.maximum = maximum;
	}

	/*-------------------------------------------------------------------------*/
	public CurMax(CurMax cm)
	{
		this.current = cm.current;
		this.maximum = cm.maximum;
	}

	/*-------------------------------------------------------------------------*/
	public int getCurrent()
	{
		return current;
	}

	/*-------------------------------------------------------------------------*/
	public void setCurrent(int current)
	{
		this.current = current;
	}

	/*-------------------------------------------------------------------------*/
	public int getMaximum()
	{
		return maximum;
	}

	/*-------------------------------------------------------------------------*/
	public void setMaximum(int maximum)
	{
		this.maximum = maximum;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Decrease the current value by the given amount
	 */
	public void decCurrent(int amount)
	{
		this.current -= amount;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Increase the current value by the given amount (not exceeding the max)
	 */
	public void incCurrent(int amount)
	{
		this.current += amount;
		this.current = Math.min(current, maximum);
		this.current = Math.max(current, 0);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Increase the maximum by the given amount.
	 */
	public void incMaximum(int amount)
	{
		this.maximum += amount;
	}

	/*-------------------------------------------------------------------------*/
	public void setCurrentToMax()
	{
		this.current = this.maximum;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	the ratio of current/maximum
	 */
	public double getRatio()
	{
		return (double)current/(double)maximum;
	}

	/*-------------------------------------------------------------------------*/
	public String toString()
	{
		return this.current+"/"+this.maximum;
	}
}
