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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import mclachlan.crusader.Texture;
import mclachlan.maze.data.v1.DataObject;

/**
 * The textures used by the raycaster to render walls, floors, ceilings and
 * objects/foes in the map.
 */
public class MazeTexture extends DataObject
{
	private String name;
	private List<String> imageResources;
	private Texture texture;
	private int imageWidth; // todo remove
	private int imageHeight; // todo remove
	private int animationDelay;
	private Texture.ScrollBehaviour scrollBehaviour;
	private int scrollSpeed;

	public MazeTexture()
	{
		this.texture = new Texture(null, new BufferedImage[0], 0, Texture.ScrollBehaviour.NONE, 0, null);
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture(
		String name, 
		List<String> imageResources,
		int imageWidth,
		int imageHeight,
		int animationDelay,
		Texture.ScrollBehaviour scrollBehaviour,
		int scrollSpeed)
	{
		this.name = name;
		this.imageResources = imageResources;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.animationDelay = animationDelay;
		this.scrollBehaviour = scrollBehaviour;
		this.scrollSpeed = scrollSpeed;

		initRenderTexture();
	}

	/*-------------------------------------------------------------------------*/
	public void initRenderTexture()
	{
		BufferedImage[] images = new BufferedImage[this.imageResources.size()];
		for (int i = 0; i < imageResources.size(); i++)
		{
			images[i] = Database.getInstance().getImage(imageResources.get(i));
		}

		this.texture = new Texture(this.name, images, this.animationDelay, this.scrollBehaviour, this.scrollSpeed, null);
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


	public Texture.ScrollBehaviour getScrollBehaviour()
	{
		return this.texture.getScrollBehaviour();
	}

	public int getScrollSpeed()
	{
		return this.texture.getScrollSpeed();
	}

	public void setScrollBehaviour(
		Texture.ScrollBehaviour scrollBehaviour)
	{
		this.scrollBehaviour = scrollBehaviour;
		this.texture.setScrollBehaviour(scrollBehaviour);
	}

	public void setScrollSpeed(int scrollSpeed)
	{
		this.scrollSpeed = scrollSpeed;
		this.texture.setScrollSpeed(scrollSpeed);
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture cloneWithTint(Color tint)
	{
		MazeTexture result = new MazeTexture(name, imageResources, imageWidth, imageHeight, animationDelay, scrollBehaviour, scrollSpeed);
		result.texture.setTint(tint);
		return result;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		MazeTexture that = (MazeTexture)o;

		if (getAnimationDelay() != that.getAnimationDelay())
		{
			return false;
		}
		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
		{
			return false;
		}
		if (getImageResources() != null ? !getImageResources().equals(that.getImageResources()) : that.getImageResources() != null)
		{
			return false;
		}
		return getTexture() != null ? getTexture().equals(that.getTexture()) : that.getTexture() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getImageResources() != null ? getImageResources().hashCode() : 0);
		result = 31 * result + (getTexture() != null ? getTexture().hashCode() : 0);
		result = 31 * result + getAnimationDelay();
		return result;
	}
}
