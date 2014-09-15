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
import java.awt.image.DataBuffer;
import java.awt.color.ColorSpace;

/**
 * An basic RGBA colour model that uses 32 bits per pixel.  Tests show it's much 
 * faster than ColorModel.getRGBDefault, even though it's effectively doing the
 * same thing (with about 2000 less lines of code too) 
 */
public class CrusaderColourModel extends ColorModel
{
	private static int[] opaqueBits = {8, 8, 8};
	
	static int alpha_mask = 0xFF000000;
	static int red_mask = 0x00FF0000;
	static int green_mask = 0x0000FF00;
	static int blue_mask = 0x000000FF;
	
	/*-------------------------------------------------------------------------*/
	public CrusaderColourModel()
	{
		super(16,
			opaqueBits,
			ColorSpace.getInstance(ColorSpace.CS_sRGB),
			false,
			false,
			ColorModel.OPAQUE,
			DataBuffer.TYPE_INT);
	}
	
	/*-------------------------------------------------------------------------*/
	public int getAlpha(int pixel)
	{
		return ((pixel>>24) & 0xFF);
	}

	/*-------------------------------------------------------------------------*/
	public int getRed(int pixel)
	{
		return ((pixel>>16) & 0xFF);
	}

	/*-------------------------------------------------------------------------*/
	public int getGreen(int pixel)
	{
		return ((pixel>>8) & 0xFF);
	}

	/*-------------------------------------------------------------------------*/
	public int getBlue(int pixel)
	{
		return pixel & 0xFF;
	}
}
