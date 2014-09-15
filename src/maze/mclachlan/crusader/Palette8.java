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

package mclachlan.crusader;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.*;

/**
 * A 256-colour palette 
 */
class Palette8 implements Palette
{
	static final int MAX_COLOURS = 256;
	
	byte[] reds = new byte[MAX_COLOURS];
	byte[] greens = new byte[MAX_COLOURS];
	byte[] blues = new byte[MAX_COLOURS];
	ColorModel colourModel;

	int counter;

	/*-------------------------------------------------------------------------*/
	public Palette8()
	{
		this.counter = 0;
	}
	
	/*-------------------------------------------------------------------------*/
	void init()
	{
/*		Color[] colors =
			{
				Color.WHITE,
				Color.RED,
				Color.ORANGE,
				Color.YELLOW,
				Color.GREEN,
				Color.BLUE,
				Color.MAGENTA,
				Color.CYAN,
				Color.PINK,
				Color.LIGHT_GRAY,
				Color.DARK_GRAY,
				new Color(255, 215, 0), //gold
				new Color(128, 0, 128), // purple
				new Color(93, 131, 253), // light blue
				new Color(165, 42, 42) // brown
			};
		
		for (int i=0; i<colors.length; i++)
		{
			for (int j=0; j<16; j++)
			{
				double percent = (j+1)/17.0;
				
				byte red = (byte)(((int)(colors[i].getRed()*percent))&0xFF);
				byte green = (byte)(((int)(colors[i].getGreen()*percent))&0xFF);
				byte blue = (byte)(((int)(colors[i].getBlue()*percent))&0xFF);
				
				this.add(red, green, blue);
			}
		}
		*/
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Adds the given RGB to the palette
	 * 
	 * @return the palette index for this RGB
	 * @throws CrusaderException
	 * 	if the palette is full.
	 */ 
	public int add(byte red, byte green, byte blue)
	{
		if (this.colourModel != null)
		{
			throw new CrusaderException(
				"Cannot add a new colour, this palette has been finalised.");
		}
		
		if (this.counter == MAX_COLOURS)
		{
			throw new CrusaderException("Too many colours in palette.");
		}
		
		reds[counter] = red;
		greens[counter] = green;
		blues[counter] = blue;
		
		return this.counter++;
	}

	/*-------------------------------------------------------------------------*/
	public ColorModel getColourModel()
	{
		if (colourModel == null)
		{
			colourModel = new IndexColorModel(8, MAX_COLOURS, reds, greens, blues);
		}
		return this.colourModel;
	}
	
	/*-------------------------------------------------------------------------*/
	public int getColourIndex(int red, int green, int blue, int tolerance)
	{
		int smallestDifference = Integer.MAX_VALUE;
		int smallestDiffColour = 0;
		
		// Find the closest match
		for (int i=0; i<MAX_COLOURS; i++)
		{
			int absDifference = 
				Math.abs(reds[i]-red) +
				Math.abs(greens[i]-green) +
				Math.abs(blues[i]-blue);
			
			if (absDifference < smallestDifference)
			{
				smallestDifference = absDifference;
				smallestDiffColour = i;
			}
		}
		
		if (smallestDifference <= tolerance)
		{
			return smallestDiffColour;
		}
		else
		{
			return -1;
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getExactMatch(byte red, byte green, byte blue)
	{
		for (int i=0; i<counter; i++)
		{
			if (reds[i] == red 
				&& greens[i] == green
				&& blues[i] == blue)
			{
				return i;
			}
		}
		return -1;
	}
	
	/*-------------------------------------------------------------------------*/
	public boolean isFull()
	{
		return this.counter == MAX_COLOURS;
	}
	
	/*-------------------------------------------------------------------------*/
	public byte getRed(int colour)
	{
		return reds[colour];
	}

	public byte getGreen(int colour)
	{
		return greens[colour];
	}

	/*-------------------------------------------------------------------------*/
	public byte getBlue(int colour)
	{
		return blues[colour];
	}
}
