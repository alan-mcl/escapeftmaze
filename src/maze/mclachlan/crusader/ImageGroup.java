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

import java.awt.image.BufferedImage;

/**
 * Represents a group of images that all have the same dimensions.
 */
public class ImageGroup
{
	String name;
	int imageWidth, imageHeight;
	BufferedImage[] images;
	private String[] imageNames;
	boolean squareImages;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param name
	 * 	The name of this image group
	 * @param images
	 * 	The images in this group
	 * @param imageNames
	 * @param squareImages
	 */ 
	public ImageGroup(
		String name,
		BufferedImage[] images,
		String[] imageNames, 
		boolean squareImages)
	{
		this.name = name;
		this.images = images;
		this.imageNames = imageNames;
		this.squareImages = squareImages;
		
		init();
	}
	
	/*-------------------------------------------------------------------------*/
	private void init()
	{
		imageWidth = images[0].getWidth();
		imageHeight = images[0].getHeight();
		
		if (squareImages && imageHeight != imageWidth)
		{
			throw new CrusaderException(
				"image[0] of group ["+name+"] is not square");
		}
		
		for (int i = 0; i < images.length; i++)
		{
			if (images[i].getHeight() != imageHeight
				|| images[i].getWidth() != imageWidth)
			{
				throw new CrusaderException(
					"image["+i+"] of group ["+name+"] has invalid dimensions");
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getImageHeight()
	{
		return imageHeight;
	}

	public BufferedImage[] getImages()
	{
		return images;
	}

	public int getImageWidth()
	{
		return imageWidth;
	}

	public String getName()
	{
		return name;
	}

	public boolean isSquareImages()
	{
		return squareImages;
	}

	public String[] getImageNames()
	{
		return imageNames;
	}
}
