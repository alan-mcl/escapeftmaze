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

package mclachlan.maze.data;

import mclachlan.crusader.Texture;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 */
public class MazeTexture
{
	String name;
	List<String> imageResources;
	Texture texture;
	int imageWidth;
	int imageHeight;
	int animationDelay;

	/*-------------------------------------------------------------------------*/
	public MazeTexture(
		String name, 
		List<String> imageResources,
		int imageWidth,
		int imageHeight,
		int animationDelay)
	{
		this.name = name;
		this.imageResources = imageResources;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.animationDelay = animationDelay;

		BufferedImage[] images = new BufferedImage[imageResources.size()];
		for (int i = 0; i < imageResources.size(); i++)
		{
			images[i] = Database.getInstance().getImage(imageResources.get(i));
		}

		this.texture = new Texture(name, imageWidth, imageHeight, images, animationDelay);
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture(
		String name,
		List<String> imageResources,
		int animationDelay)
	{
		this.name = name;
		this.imageResources = imageResources;
		this.animationDelay = animationDelay;

		BufferedImage[] images = new BufferedImage[imageResources.size()];
		for (int i = 0; i < imageResources.size(); i++)
		{
			images[i] = Database.getInstance().getImage(imageResources.get(i));
		}

		this.imageWidth = images[0].getWidth();
		this.imageHeight = images[0].getHeight();

		this.texture = new Texture(name, imageWidth, imageHeight, images, animationDelay);
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getImageResources()
	{
		return imageResources;
	}

	public String getName()
	{
		return name;
	}

	public Texture getTexture()
	{
		return texture;
	}

	public int getAnimationDelay()
	{
		return animationDelay;
	}

	public int getImageHeight()
	{
		return imageHeight;
	}

	public int getImageWidth()
	{
		return imageWidth;
	}

	/*-------------------------------------------------------------------------*/
	public void setAnimationDelay(int animationDelay)
	{
		this.animationDelay = animationDelay;
	}

	public void setImageHeight(int imageHeight)
	{
		this.imageHeight = imageHeight;
	}

	public void setImageResources(List<String> imageResources)
	{
		this.imageResources = imageResources;
	}

	public void setImageWidth(int imageWidth)
	{
		this.imageWidth = imageWidth;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setTexture(Texture texture)
	{
		this.texture = texture;
	}
	
	
}
