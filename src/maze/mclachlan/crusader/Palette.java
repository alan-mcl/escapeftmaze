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


/**
 * 
 */
interface Palette
{
	/*-------------------------------------------------------------------------*/
	/**
	 * Adds the given RGB to the palette
	 * 
	 * @return the palette index for this RGB
	 * @throws CrusaderException
	 * 	if the palette is full.
	 */ 
	public int add(byte red, byte green, byte blue);
	
	/*-------------------------------------------------------------------------*/
	public ColorModel getColourModel();
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return the index in this palette of the given RGB, or -1 if it this 
	 * 	colour is not in the palette.
	 */ 
	public int getExactMatch(byte red, byte green, byte blue);

	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the index in this palette of the given RGB colour.
	 * 
	 * @param tolerance
	 * 	A measure of how close to tolerate.  If an exact match is required, 
	 * 	set to 0.
	 * @return 
	 * 	The index of the colour.  If it does not exist and exactMatch is true,
	 * 	-1 is returned. Else the index of the nearest colour in this palette is
	 * 	returned. 
	 */
	public int getColourIndex(int red, int green, int blue, int tolerance);
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if this palette is full
	 */ 
	public boolean isFull();
	
	/*-------------------------------------------------------------------------*/
	public byte getRed(int colour);
	public byte getGreen(int colour);
	public byte getBlue(int colour);
}
