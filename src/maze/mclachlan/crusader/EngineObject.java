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

import static mclachlan.crusader.CrusaderEngine.NORMAL_LIGHT_LEVEL;

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

	// volatile data calculated each rendering cycle
	double distance;
	int center;
	double apparentDistance;
	int projectedObjectHeight;
	int startScreenX, endScreenX;
	int adjustedLightLevel;
	double shadeMult;
	Texture renderTexture;

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
	void prepareForRender(
		int projectionPlaneWidth,
		float[] fishbowlTable,
		int tile_size,
		int playerDistToProjectionPlane,
		Map map,
		boolean doLighting,
		boolean doShading,
		int shadingDistance,
		int shadingThickness,
		int movementMode,
		int playerFacing)
	{
		apparentDistance = distance;

		// correct distance (compensate for the fishbowl effect)
		if (this.center > 0 && this.center < projectionPlaneWidth)
		{
			this.apparentDistance /= fishbowlTable[this.center];
		}
		else if (this.center < 0)
		{
			// otherwise approximate it.
			this.apparentDistance /= fishbowlTable[0];
		}
		else
		{
			// center > PROJECTION_PLANE_WIDTH, approximate it
			this.apparentDistance /= fishbowlTable[projectionPlaneWidth-1];
		}

		this.projectedObjectHeight = (int)(tile_size *
			playerDistToProjectionPlane / apparentDistance);

		if (this.center + projectedObjectHeight/2 < 0)
		{
			// off screen left
			this.projectedObjectHeight = -1;
			return;
		}
		else if (this.center - projectedObjectHeight/2 > projectionPlaneWidth)
		{
			// off screen right
			this.projectedObjectHeight = -1;
			return;
		}

		this.startScreenX = this.center - this.projectedObjectHeight/2;
		this.endScreenX = startScreenX + this.projectedObjectHeight;

		if (this.endScreenX > projectionPlaneWidth)
		{
			this.endScreenX = projectionPlaneWidth;
		}

		if (this.isLightSource)
		{
			// "Light source" objects always appear at normal light level,
			// or the tile light level, whichever is higher
			adjustedLightLevel = Math.max(
				NORMAL_LIGHT_LEVEL,
				map.tiles[this.tileIndex].currentLightLevel);
		}
		else
		{
			if (doLighting)
			{
				this.adjustedLightLevel = map.tiles[this.tileIndex].currentLightLevel;
			}
			else
			{
				this.adjustedLightLevel = NORMAL_LIGHT_LEVEL;
			}

			if (doShading)
			{
				// Shading: work out the effective light level for this wall slice
				shadeMult = CrusaderEngine32.calcShadeMult(this.apparentDistance, shadingDistance, shadingThickness);
			}
		}

		// pick the object texture
		if (movementMode == CrusaderEngine.MovementMode.DISCRETE && this.currentTexture == -1)
		{
			switch(playerFacing)
			{
				case CrusaderEngine.Facing.NORTH:
					renderTexture = this.textures[this.northTexture];
					break;
				case CrusaderEngine.Facing.SOUTH:
					renderTexture = this.textures[this.southTexture];
					break;
				case CrusaderEngine.Facing.EAST:
					renderTexture = this.textures[this.eastTexture];
					break;
				case CrusaderEngine.Facing.WEST:
					renderTexture = this.textures[this.westTexture];
					break;
				default:
					throw new CrusaderException(
						"invalid facing: "+playerFacing);
			}
		}
		else if (movementMode == CrusaderEngine.MovementMode.CONTINUOUS)
		{
			// object facings only supported for DISCRETE mode
			renderTexture = this.textures[this.northTexture];
		}
		else
		{
			// the current texture has been set.
			renderTexture = this.textures[this.currentTexture];
		}
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
