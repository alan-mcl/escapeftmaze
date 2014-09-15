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
 * Stores a current/maximum pair, with a sub-value that can range from 0 to current
 */
public class CurMaxSub extends CurMax
{
	int sub;
	
	/*-------------------------------------------------------------------------*/
	public CurMaxSub()
	{
		this(0,0,0);
	}

	/*-------------------------------------------------------------------------*/
	public CurMaxSub(int maximum)
	{
		this(maximum, maximum, 0);
	}
	
	/*-------------------------------------------------------------------------*/
	public CurMaxSub(int current, int maximum, int sub)
	{
		super(current, maximum);
		this.sub = sub;
	}

	/*-------------------------------------------------------------------------*/
	public CurMaxSub(CurMaxSub cms)
	{
		this(cms.current, cms.maximum, cms.sub);
	}

	/*-------------------------------------------------------------------------*/
	public void setCurrent(int current)
	{
		super.setCurrent(current);
		checkSub();
	}

	/*-------------------------------------------------------------------------*/
	private void checkSub()
	{
		if (sub > current)
		{
			sub = current;
		}

		if (sub < 0)
		{
			sub = 0;
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Decrease the current value by the given amount
	 */
	public void decCurrent(int amount)
	{
		super.decCurrent(amount);
		checkSub();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Increase the current value by the given amount (not exceeding the max)
	 */
	public void incCurrent(int amount)
	{
		super.incCurrent(amount);
		checkSub();
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Increase the maximum value by the given amount (not exceeding the max)
	 */
	public void incMaximum(int amount)
	{
		super.incMaximum(amount);
		checkSub();
	}

	/*-------------------------------------------------------------------------*/
	public int getSub()
	{
		return sub;
	}

	/*-------------------------------------------------------------------------*/
	public void setSub(int sub)
	{
		this.sub = sub;
	}

	/*-------------------------------------------------------------------------*/
	public void incSub(int amount)
	{
		sub += amount;
		checkSub();
	}

	/*-------------------------------------------------------------------------*/
	public double getSubRatio()
	{
		return (double)sub/(double)maximum;
	}

	/*-------------------------------------------------------------------------*/
	public String toString()
	{
		return this.current+"/"+this.maximum;
	}
}
