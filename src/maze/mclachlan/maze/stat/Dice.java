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

import java.util.Random;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.Log;
import mclachlan.maze.data.v1.V1Dice;

/**
 *
 */
public class Dice
{
	private int numberOfDice, diceMax, modifier;
	private static Random r = new Random();
	
	public static final Dice d1 = new Dice(1,1,0);
	public static final Dice d2 = new Dice(1,2,0);
	public static final Dice d3 = new Dice(1,3,0);
	public static final Dice d4 = new Dice(1,4,0);
	public static final Dice d5 = new Dice(1,5,0);
	public static final Dice d6 = new Dice(1,6,0);
	public static final Dice d7 = new Dice(1,7,0);
	public static final Dice d8 = new Dice(1,8,0);
	public static final Dice d9 = new Dice(1,9,0);
	public static final Dice d10 = new Dice(1,10,0);
	public static final Dice d12 = new Dice(1,12,0);
	public static final Dice d20 = new Dice(1,20,0);
	public static final Dice d100 = new Dice(1,100,0);
	public static final Dice d1000 = new Dice(1,1000,0);
	public static final Dice ONE = d1;

	/*-------------------------------------------------------------------------*/
	public Dice(int numberOfDice, int diceMax, int modifier)
	{
		this.diceMax = diceMax;
		this.modifier = modifier;
		this.numberOfDice = numberOfDice;
	}
	
	/*-------------------------------------------------------------------------*/
	public int roll()
	{
		int result = 0;
		for (int i=0; i<numberOfDice; i++)
		{
			result += this.rollDie();
		}
		
		result += modifier;

		if (result < 0)
		{
			result = 0;
		}

		Maze.log(Log.DEBUG, this.toString()+" rolls "+result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getMinPossible()
	{
		return numberOfDice + modifier;
	}

	/*-------------------------------------------------------------------------*/
	public double getAverage()
	{
		return numberOfDice*diceMax/2.0 + modifier;
	}

	/*-------------------------------------------------------------------------*/
	public int getMaxPossible()
	{
		return numberOfDice*diceMax +modifier;
	}

	/*-------------------------------------------------------------------------*/
	private int rollDie()
	{
		return r.nextInt(this.diceMax)+1;
	}
	
	/*-------------------------------------------------------------------------*/
	public String toString()
	{
		String modStr = null;
		if (this.modifier == 0)
		{
			modStr = "";
		}
		else if (this.modifier > 0)
		{
			modStr = "+"+this.modifier;
		}
		else if (this.modifier < 0)
		{
			modStr = ""+this.modifier;
		}
		
		return this.numberOfDice+"d"+this.diceMax+""+modStr;
	}
	
	/*-------------------------------------------------------------------------*/
	public static int nextInt(int lessThan)
	{
		return r.nextInt(lessThan);
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args)
	{
		Dice d = V1Dice.fromString(args[0]);
		int i = d.roll();
		System.out.println("d = [" + d + "]");
		System.out.println("i = [" + i + "]");
	}
}
