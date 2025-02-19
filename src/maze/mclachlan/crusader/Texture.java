
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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 *
 */
public class Texture implements Comparable<Texture>
{
	/** The number of different images in this texture */ 
	int nrFrames;
	
	/** The delay between changing images, in millis. */ 
	int animationDelay;
	
	/** The images involved. */ 
	BufferedImage[] images;
	
	/** Used by the engine */
	int[][] imageData;

	private final String name;

	/** Width of all the images, in pixels */
	public int imageWidth;
	
	/** Height of all the images, in pixels */
	public int imageHeight;

	/** Any scrolling behaviour on this texture, null if none */
	ScrollBehaviour scrollBehaviour;

	/** how fast the texture scrolls */
	int scrollSpeed;

	/** any tint to this texture */
	Color tint;

	//
	// Dodgy hack alert.  EngineObjects keep their own versions of these two so
	// that their frames can change independently.  These texture-wide counters
	// are used by walls floors and ceilings
	//

	/** The current image */
	int currentFrame;

	/** When this texture last changed */
	long lastChanged = System.currentTimeMillis();

	/*-------------------------------------------------------------------------*/
	public Texture(
		String name,
		BufferedImage[] frames,
		int animationDelay,
		ScrollBehaviour scrollBehaviour,
		int textureScrollSpeed,
		Color tint)
	{
		this.name = name;
		this.animationDelay = animationDelay;
		this.images = frames;
		this.scrollBehaviour = scrollBehaviour;
		this.scrollSpeed = textureScrollSpeed;
		this.tint = tint;
		if (frames != null && frames.length>0)
		{
			this.nrFrames = frames.length;
			this.imageWidth = frames[0].getWidth();
			this.imageHeight = frames[0].getHeight();
		}
		else
		{
			this.imageWidth = 0;
			this.imageHeight = 0;
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getAnimationDelay()
	{
		return animationDelay;
	}

	public int getCurrentFrame()
	{
		return currentFrame;
	}

	public void setCurrentFrame(int currentFrame)
	{
		this.currentFrame = currentFrame;
	}

	public int[][] getImageData()
	{
		return imageData;
	}

	public void setImageData(int[][] imageData)
	{
		this.imageData = imageData;
	}

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

	public long getLastChanged()
	{
		return lastChanged;
	}

	public String getName()
	{
		return name;
	}

	public int getNrFrames()
	{
		return nrFrames;
	}

	public ScrollBehaviour getScrollBehaviour()
	{
		return scrollBehaviour;
	}

	public int getScrollSpeed()
	{
		return scrollSpeed;
	}

	public void setScrollBehaviour(
		ScrollBehaviour scrollBehaviour)
	{
		this.scrollBehaviour = scrollBehaviour;
	}

	public void setScrollSpeed(int scrollSpeed)
	{
		this.scrollSpeed = scrollSpeed;
	}

	public Color getTint()
	{
		return tint;
	}

	public void setTint(Color tint)
	{
		this.tint = tint;
	}

	public void setNrFrames(int nrFrames)
	{
		this.nrFrames = nrFrames;
	}

	public void setAnimationDelay(int animationDelay)
	{
		this.animationDelay = animationDelay;
	}

	/*-------------------------------------------------------------------------*/
	public void applyTint(Color tint)
	{
		for (int[] imagePixels : imageData)
		{
			applyTint(tint, imagePixels);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void applyTint(Color tint, int[] imagePixels)
	{
		int tintRGB = tint.getRGB();
		int tintedRed = (tintRGB >> 16) & 0xFF;
		int tintedGreen = (tintRGB >> 8) & 0xFF;
		int tintedBlue = tintRGB & 0xFF;

		for (int i = 0; i < imagePixels.length; i++)
		{
			// ignore transparent pixels
			if (imagePixels[i] != 0)
			{
				// grayscale the pixel first
				int alpha = (imagePixels[i] >> 24) & 0xFF;
				int red = (imagePixels[i] >> 16) & 0xFF;
				int green = (imagePixels[i] >> 8) & 0xFF;
				int blue = imagePixels[i] & 0xFF;

				int grayscaleValue = (int)(0.299 * red + 0.587 * green + 0.114 * blue);

				// the grayscale pixel
				imagePixels[i] = (alpha << 24) | (grayscaleValue << 16) | (grayscaleValue << 8) | grayscaleValue;

				// then tint with the given colour RGB
				// "darken only" and retain the alpha channel
				int finalRed = (int)(grayscaleValue * (tintedRed / 255.0));
				int finalGreen = (int)(grayscaleValue * (tintedGreen / 255.0));
				int finalBlue = (int)(grayscaleValue * (tintedBlue / 255.0));

				imagePixels[i] = (alpha << 24) | (finalRed << 16) | (finalGreen << 8) | finalBlue;
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	Current image data, taking all frames and transformations into account
	 */
	public int getCurrentImageData(int textureX, int textureY, long timeNow)
	{
		if (scrollBehaviour != null)
		{
			switch (scrollBehaviour)
			{
				case LEFT:
					textureX = Math.abs((int)((textureX - (timeNow / scrollSpeed)) % imageWidth));
					textureY = Math.abs(textureY);
					break;
				case RIGHT:
					textureX = Math.abs((int)((textureX + (timeNow / scrollSpeed)) % imageWidth));
					textureY = Math.abs(textureY);
					break;
				case DOWN:
					textureY = Math.abs((int)((textureY - (timeNow / scrollSpeed)) % imageHeight));
					textureX = Math.abs(textureX);
					break;
				case UP:
					textureY = Math.abs((int)((textureY + (timeNow / scrollSpeed)) % imageHeight));
					textureX = Math.abs(textureX);
					break;
				default:
					throw new CrusaderException("invalid scroll behaviour: " + scrollBehaviour);
			}
		}

		if (textureX < 0 || textureX > imageWidth-1)
		{
			textureX = Math.min(Math.abs(textureX % imageWidth), imageWidth-1);
		}

		if (textureY < 0 || textureY > imageHeight-1)
		{
			textureY = Math.min(Math.abs(textureY % imageHeight), imageHeight-1);
		}

		return this.imageData[currentFrame][textureX + textureY * imageWidth];
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		return "Texture{" +
			"name='" + name + '\'' +
			", nrFrames=" + nrFrames +
			", currentFrame=" + currentFrame +
			'}';
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int compareTo(Texture other)
	{
		return this.getName().compareTo(other.getName());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Texture))
		{
			return false;
		}
		Texture texture = (Texture)o;
		return name.equals(texture.name);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name);
	}

	/*-------------------------------------------------------------------------*/
	public enum ScrollBehaviour
	{
		NONE, LEFT, RIGHT, UP, DOWN
	}
}
