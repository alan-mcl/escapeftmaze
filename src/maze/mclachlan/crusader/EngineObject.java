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

import java.awt.Point;
import java.util.*;
import mclachlan.maze.util.MazeException;

import static mclachlan.crusader.CrusaderEngine.NORMAL_LIGHT_LEVEL;

/**
 * 
 */
public class EngineObject
{
	public enum Alignment {TOP, CENTER, BOTTOM}

	String name;

	/** texture when viewed from the north */
	Texture northTexture;
	/** texture when viewed from the west */
	Texture southTexture;
	/** texture when viewed from the east */
	Texture eastTexture;
	/** texture when viewed from the west */
	Texture westTexture;

	/** whether this object is subject to shading */
	boolean isLightSource;

	/** Any script to run when the user clicks on this object */
	MouseClickScript mouseClickScript;

	/** How to align the object, if the texture size is less than the tile size*/
	Alignment verticalAlignment; // todo configure

	/** scripts associated with this object */
	ObjectScript[] scripts;
	private final Object scriptMutex = new Object();

	/** object location */
	int xPos, yPos;

	//
	// volatile data:
	//

	/** For when we just want one texture at a time, not directional */
	Texture[] alternateTextures;

	// calculate when added to the map
	int gridX, gridY;
	int tileIndex;

	// calculated each rendering cycle
	double distance;
	int center;
	double apparentDistance;
	int projectedWallHeight; // height of a hypothetical wall at the location of this object
	int projectedObjectHeight, projectedObjectWidth;
	int projectedTextureOffset;
	int startScreenX, endScreenX;
	int adjustedLightLevel;
	double shadeMult;
	Texture renderTexture;

	/** if not null, this is the texture index that will be used from all angles */
	Texture currentTexture = null;

	/** The current image */
	int currentTextureFrame;

	/** When this texture last changed */
	long textureLastChanged = System.currentTimeMillis();

	/** Any offset to draw this object at, to support up/down animations */
	int textureOffset = 0;

	public EngineObject()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param textures
	 *      The textures of this object, the first one is the default
	 * @param isLightSource
	 *      Sets whether or not this object is a light source
	 */
	public EngineObject(
		Texture[] textures,
		boolean isLightSource)
	{
		this.alternateTextures = textures;
		this.currentTexture = textures[0];
		this.isLightSource = isLightSource;
		this.verticalAlignment = Alignment.BOTTOM;
	}

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
	 * @param verticalAlignment
	 * 	Where to align the object vertically, if the texture is smaller than the tile size
	 */ 
	public EngineObject(
		String name,
		int xPos,
		int yPos,
		Texture northTexture,
		Texture southTexture,
		Texture eastTexture,
		Texture westTexture,
		int tileIndex,
		boolean isLightSource,
		MouseClickScript mouseClickScript,
		Alignment verticalAlignment)
	{
		this.name = name;
		this.xPos = xPos;
		this.yPos = yPos;
		this.mouseClickScript = mouseClickScript;
		this.verticalAlignment = verticalAlignment;
		this.northTexture = northTexture;
		this.southTexture = southTexture;
		this.eastTexture = eastTexture;
		this.westTexture = westTexture;
		this.tileIndex = tileIndex;
		this.isLightSource = isLightSource;

//		this.currentTexture = northTexture;
	}
	
	/*-------------------------------------------------------------------------*/
	public EngineObject(EngineObject clone)
	{
		this.name = clone.name;
		this.xPos = clone.xPos;
		this.yPos = clone.yPos;
		this.mouseClickScript = clone.mouseClickScript;
		this.northTexture = clone.northTexture;
		this.southTexture = clone.southTexture;
		this.eastTexture = clone.eastTexture;
		this.westTexture = clone.westTexture;
		this.tileIndex = clone.tileIndex;
		this.isLightSource = clone.isLightSource;
		this.verticalAlignment = clone.verticalAlignment;
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
		//
		// pick the object texture
		//
		if (movementMode == CrusaderEngine.MovementMode.DISCRETE && this.currentTexture == null)
		{
			switch (playerFacing)
			{
				case CrusaderEngine.Facing.NORTH ->
					renderTexture = this.northTexture;
				case CrusaderEngine.Facing.SOUTH ->
					renderTexture = this.southTexture;
				case CrusaderEngine.Facing.EAST ->
					renderTexture = this.eastTexture;
				case CrusaderEngine.Facing.WEST ->
					renderTexture = this.westTexture;
				default -> throw new CrusaderException(
					"invalid facing: " + playerFacing);
			}
		}
		else if (movementMode == CrusaderEngine.MovementMode.OCTO && this.currentTexture == null)
		{
			// yeah this fudges it a bit
			switch (playerFacing)
			{
				case CrusaderEngine.Facing.NORTH, CrusaderEngine.Facing.NORTH_EAST, CrusaderEngine.Facing.NORTH_WEST ->
					renderTexture = this.northTexture;
				case CrusaderEngine.Facing.SOUTH, CrusaderEngine.Facing.SOUTH_EAST, CrusaderEngine.Facing.SOUTH_WEST ->
					renderTexture = this.southTexture;
				case CrusaderEngine.Facing.EAST ->
					renderTexture = this.eastTexture;
				case CrusaderEngine.Facing.WEST ->
					renderTexture = this.westTexture;
				default -> throw new CrusaderException(
					"invalid facing: " + playerFacing);
			}
		}
		else if (movementMode == CrusaderEngine.MovementMode.CONTINUOUS)
		{
			// object facings only supported for DISCRETE mode
			renderTexture = this.northTexture;
		}
		else
		{
			// the current texture has been set.
			renderTexture = this.currentTexture;
		}

		//
		// Calculate data needed for the render
		//
		apparentDistance = distance;
		int textureHeight = renderTexture.imageHeight;
		int textureWidth = renderTexture.imageWidth;

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

		double scale = playerDistToProjectionPlane / apparentDistance;
		this.projectedObjectHeight = (int)(textureHeight * scale);
		this.projectedObjectWidth = (int)(textureWidth * scale);
		this.projectedWallHeight = (int)(tile_size * scale);
		this.projectedTextureOffset = (int)(textureOffset * scale);

		if (this.center + projectedObjectWidth/2 < 0)
		{
			// off screen left
			this.projectedObjectWidth = this.projectedObjectHeight = -1;
			return;
		}
		else if (this.center - projectedObjectWidth/2 > projectionPlaneWidth)
		{
			// off screen right
			this.projectedObjectWidth = this.projectedObjectHeight = -1;
			return;
		}

		this.startScreenX = this.center - this.projectedObjectWidth/2;
		this.endScreenX = startScreenX + this.projectedObjectWidth;


		// negative startScreenX is ok
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
	}

	/*-------------------------------------------------------------------------*/
	public int calcFromTopTextureOffset(
		CrusaderEngine engine)
	{
		int tile_size = engine.getTileSize();
		int projectionPlaneWidth = ((CrusaderEngine32)engine).getProjectionPlaneWidth();
		int playerDistToProjectionPlane = ((CrusaderEngine32)engine).getPlayerDistToProjectionPlane();
		float[] fishbowlTable = ((CrusaderEngine32)engine).getFishbowlTable();

		double xDiff = engine.getPlayerPos().x - this.xPos;
		double yDiff = engine.getPlayerPos().y - this.yPos;

		double apparentDistance = Math.sqrt(xDiff*xDiff + yDiff*yDiff);
		int textureHeight = currentTexture.imageHeight;

		// correct distance (compensate for the fishbowl effect)
		if (this.center > 0 && this.center < projectionPlaneWidth)
		{
			apparentDistance /= fishbowlTable[this.center];
		}
		else if (this.center < 0)
		{
			// otherwise approximate it.
			apparentDistance /= fishbowlTable[0];
		}
		else
		{
			// center > PROJECTION_PLANE_WIDTH, approximate it
			apparentDistance /= fishbowlTable[projectionPlaneWidth-1];
		}

		double scale = playerDistToProjectionPlane / apparentDistance;
		int projectedObjectHeight = (int)(textureHeight * scale);
		int projectedWallHeight = (int)(tile_size * scale);

		int skySize = ((CrusaderEngine32)engine).getProjectionPlaneHeight()/2 - projectedWallHeight/2;

		return switch (getVerticalAlignment())
			{
				case TOP		-> -projectedObjectHeight - skySize;
				case CENTER	-> -(projectedWallHeight / 2 + projectedObjectHeight / 2) - skySize;
				case BOTTOM	-> -projectedWallHeight - skySize;
				default ->
					throw new MazeException("invalid " + getVerticalAlignment());
			};

	}

	/*-------------------------------------------------------------------------*/
	public int getCurrentRenderTextureData(int textureX, int textureY, long timeNow)
	{
		int resultX = textureX;
		int resultY = textureY;

		if (scripts != null)
		{
			synchronized (scriptMutex)
			{
				for (ObjectScript script : scripts)
				{
					Point p = script.getCurrentRenderTextureData(this, resultX, resultY, renderTexture.imageWidth, renderTexture.imageHeight);
					if (p != null)
					{
						resultX = p.x;
						resultY = p.y;
					}
				}
			}
		}

		return renderTexture.getCurrentImageData(resultX, resultY, timeNow);
	}

	/*-------------------------------------------------------------------------*/
	public void setCurrentTexture(Texture currentTexture)
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
		return northTexture;
	}

	public Texture getSouthTexture()
	{
		return southTexture;
	}

	public Texture getEastTexture()
	{
		return eastTexture;
	}

	public Texture getWestTexture()
	{
		return westTexture;
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
		this.northTexture = northTexture;
	}

	public void setSouthTexture(Texture southTexture)
	{
		this.southTexture = southTexture;
	}
	
	public void setEastTexture(Texture eastTexture)
	{
		this.eastTexture = eastTexture;
	}

	public void setWestTexture(Texture westTexture)
	{
		this.westTexture = westTexture;
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

	public Texture getCurrentTexture()
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
		List<Texture> result = new ArrayList<>();

		if (northTexture != null)
		{
			result.add(northTexture);
		}
		if (southTexture != null)
		{
			result.add(southTexture);
		}
		if (eastTexture != null)
		{
			result.add(eastTexture);
		}
		if (westTexture != null)
		{
			result.add(westTexture);
		}

		if (alternateTextures != null)
		{
			result.addAll(Arrays.asList(alternateTextures));
		}

		return result.toArray(Texture[]::new);
	}

	public MouseClickScript getMouseClickScript()
	{
		return mouseClickScript;
	}

	public Alignment getVerticalAlignment()
	{
		return verticalAlignment;
	}

	public void setVerticalAlignment(
		Alignment verticalAlignment)
	{
		this.verticalAlignment = verticalAlignment;
	}

	public int getTextureOffset()
	{
		return textureOffset;
	}

	public void setTextureOffset(int textureOffset)
	{
		this.textureOffset = textureOffset;
	}

	public ObjectScript[] getScripts()
	{
		return scripts;
	}

	public void setScripts(ObjectScript[] scripts)
	{
		this.scripts = scripts;
	}

	/*-------------------------------------------------------------------------*/
	void executeScripts(long frameCount)
	{
		if (scripts != null)
		{
			synchronized (scriptMutex)
			{
				for (ObjectScript script : scripts)
				{
					script.execute(frameCount, this);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addScript(ObjectScript script)
	{
		synchronized(scriptMutex)
		{
			ObjectScript[] temp;

			if (scripts == null)
			{
				temp = new ObjectScript[]{script};
			}
			else
			{
				temp = new ObjectScript[scripts.length+1];
				System.arraycopy(scripts, 0, temp, 0, scripts.length);
				temp[scripts.length] = script;
			}

			this.scripts = temp;
		}
	}

	/*-------------------------------------------------------------------------*/
	public ObjectScript removeScript(ObjectScript script)
	{
		synchronized(scriptMutex)
		{
			ObjectScript[] temp = null;

			if (scripts == null)
			{
				return null;
			}
			else
			{
				int index = -1;
				for (int i = 0; i < scripts.length; i++)
				{
					if (scripts[i] == script)
					{
						index = i;
						break;
					}
				}

				if (index == -1)
				{
					// script not in array
					return null;
				}

				temp = new ObjectScript[scripts.length-1];
				System.arraycopy(scripts, 0, temp, 0, index);
				System.arraycopy(scripts, index+1, temp, index, scripts.length-index-1);
				this.scripts = temp;
				return script;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public ObjectScript[] removeAllScripts()
	{
		synchronized (scriptMutex)
		{
			ObjectScript[] temp = this.scripts;
			this.scripts = new ObjectScript[0];
			return temp;
		}
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
