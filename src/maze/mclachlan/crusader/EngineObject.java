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

import java.util.*;

/**
 * 
 */
public class EngineObject
{
	String name;

	Texture[] textures;

	/** texture when viewed from the north */
	int northTexture;
	/** texture when viewed from the west */
	int southTexture;
	/** texture when viewed from the east */
	int eastTexture;
	/** texture when viewed from the west */
	int westTexture;

	/** if not -1, this is the texture index that will be used from all angles */
	int currentTexture = -1;

	int xPos, yPos;
	int gridX, gridY;
	int tileIndex;
	double distance;
	
	/** whether or not this object is subject to shading */
	boolean isLightSource;

	/** The current image */
	int currentTextureFrame;

	/** When this texture last changed */
	long textureLastChanged = System.currentTimeMillis();
	
	/** Any script to run when the user clicks on this object */
	MouseClickScript mouseClickScript;
	
	/** a bitmap used when adding this object to the Map, determining where to 
	 * place it inside the tile and whether or not to spawn additional objects*/
	BitSet placementMask;

	/*-------------------------------------------------------------------------*/
	/**
	 * Note that different texture views is only supported for DISCRETE mode
	 *
	 * @param name
	 * 	A unique identifier for this object
	 * @param northTexture
	 * 	The texture of this object when viewed from the north
	 * @param southTexture
	 * 	The texture of this object when viewed from the south
	 * @param eastTexture
	 * 	The texture of this object when viewed from the east
	 * @param westTexture
	 * 	The texture of this object when viewed from the west
	 * @param tileIndex
	 * 	The position of this object in the map
	 * @param isLightSource
	 * 	Sets whether or not this object is a light source
	 * @param mouseClickScript
	 * 	Any script to run when the user clicks on this object
	 */ 
	public EngineObject(
		String name,
		Texture northTexture,
		Texture southTexture,
		Texture eastTexture,
		Texture westTexture,
		int tileIndex,
		boolean isLightSource,
		MouseClickScript mouseClickScript,
		BitSet placementMask)
	{
		this.name = name;
		this.mouseClickScript = mouseClickScript;
		this.placementMask = placementMask;
		this.textures = new Texture[]
		{
			northTexture,
			southTexture,
			eastTexture,
			westTexture
		};
		this.northTexture = 0;
		this.southTexture = 1;
		this.eastTexture = 2;
		this.westTexture = 3;
		this.tileIndex = tileIndex;
		this.isLightSource = isLightSource;
	}
	
	/*-------------------------------------------------------------------------*/
	public EngineObject(EngineObject clone)
	{
		this.name = clone.name;
		this.textures = clone.textures;
		this.mouseClickScript = clone.mouseClickScript;
		this.northTexture = clone.northTexture;
		this.southTexture = clone.southTexture;
		this.eastTexture = clone.eastTexture;
		this.westTexture = clone.westTexture;
		this.tileIndex = clone.tileIndex;
		this.isLightSource = clone.isLightSource;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param texture
	 * 	The texture of this object when viewed from any angle
	 * @param tileIndex
	 * 	The position of this object in the map
	 * @param isLightSource
	 * 	Sets whether or not this object is a light source
	 */ 
	public EngineObject(
		Texture texture,
		int tileIndex, 
		boolean isLightSource)
	{
		this.textures = new Texture[]
		{
			texture
		};
		northTexture = southTexture = eastTexture = westTexture = 0;
		this.tileIndex = tileIndex;
		this.isLightSource = isLightSource;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param textures
	 * 	The textures of this object, the first one is the default
	 * @param tileIndex
	 * 	The position of this object in the map
	 * @param isLightSource
	 * 	Sets whether or not this object is a light source
	 */
	public EngineObject(
		Texture[] textures,
		int tileIndex,
		boolean isLightSource)
	{
		this.textures = textures;
		this.tileIndex = tileIndex;
		northTexture = southTexture = eastTexture = westTexture = currentTexture = 0;
		this.isLightSource = isLightSource;
	}

	/*-------------------------------------------------------------------------*/
	public void setCurrentTexture(int currentTexture)
	{
		this.currentTexture = currentTexture;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public Texture getNorthTexture()
	{
		return textures[northTexture];
	}

	public Texture getSouthTexture()
	{
		return textures[southTexture];
	}

	public Texture getEastTexture()
	{
		return textures[eastTexture];
	}

	public Texture getWestTexture()
	{
		return textures[westTexture];
	}

	public void setLightSource(boolean lightSource)
	{
		isLightSource = lightSource;
	}

	public void setMouseClickScript(MouseClickScript mouseClickScript)
	{
		this.mouseClickScript = mouseClickScript;
	}

	public void setNorthTexture(Texture northTexture)
	{
		this.textures[this.northTexture] = northTexture;
	}

	public void setSouthTexture(Texture southTexture)
	{
		this.textures[this.southTexture] = southTexture;
	}
	
	public void setEastTexture(Texture eastTexture)
	{
		this.textures[this.eastTexture] = eastTexture;
	}

	public void setWestTexture(Texture westTexture)
	{
		this.textures[this.westTexture] = westTexture;
	}

	public int getTileIndex()
	{
		return tileIndex;
	}

	public boolean isLightSource()
	{
		return isLightSource;
	}

	public int getGridX()
	{
		return gridX;
	}

	public int getGridY()
	{
		return gridY;
	}

	public int getXPos()
	{
		return xPos;
	}

	public int getYPos()
	{
		return yPos;
	}

	public void setTileIndex(int tileIndex)
	{
		this.tileIndex = tileIndex;
	}

	public void setCurrentTextureFrame(int currentTextureFrame)
	{
		this.currentTextureFrame = currentTextureFrame;
	}

	public void setXPos(int xPos)
	{
		this.xPos = xPos;
	}

	public void setYPos(int yPos)
	{
		this.yPos = yPos;
	}

	public void setGridX(int gridX)
	{
		this.gridX = gridX;
	}

	public void setGridY(int gridY)
	{
		this.gridY = gridY;
	}

	public int getCurrentTexture()
	{
		return currentTexture;
	}

	public long getTextureLastChanged()
	{
		return textureLastChanged;
	}

	public void setTextureLastChanged(long textureLastChanged)
	{
		this.textureLastChanged = textureLastChanged;
	}

	public Texture[] getTextures()
	{
		return textures;
	}

	public MouseClickScript getMouseClickScript()
	{
		return mouseClickScript;
	}

	public BitSet getPlacementMask()
	{
		return placementMask;
	}

	public void setPlacementMask(BitSet placementMask)
	{
		this.placementMask = placementMask;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * There are nine possible placement positions.
	 */
	public static class Placement
	{
		public static final int NORTH_WEST = 0;
		public static final int NORTH = 1;
		public static final int NORTH_EAST = 2;
		public static final int WEST = 3;
		public static final int CENTER = 4;
		public static final int EAST = 5;
		public static final int SOUTH_WEST = 6;
		public static final int SOUTH = 7;
		public static final int SOUTH_EAST = 8;
	}
}
