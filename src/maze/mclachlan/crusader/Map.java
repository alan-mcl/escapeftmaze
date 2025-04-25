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

/**
 * A aggregation of walls and tiles.
 */
public class Map
{
	/**
	 * Can be used in the place of a Tile texture index to indicate that there
	 * should be no wall on that face.
	 */ 
	public static final Texture NO_WALL = new Texture("No_WAll", null, -1, null, -1, null);
	
	/** The width of this map (ie east-west), in grid blocks */
	int width;
	/** The length of this map (ie north-south), in grid blocks */
	int length;
	/** The base width and height of images in this map (in pixels) */
	int baseImageSize;

	Tile[] tiles;
	Wall[] horizontalWalls;
	Wall[] verticalWalls;

	/** these are the objects that are rendered */
	EngineObject[] renderObjects;
	/* the original configured objects from the db */
//	List<EngineObject> originalObjects;
	List<EngineObject> expandedObjects;

	/** Sky configs, in render order */
	Map.SkyConfig[] skyConfigs;

	Texture[] textures;
	MapScript[] scripts;

	private final Object scriptMutex = new Object();

	public Map()
	{
		this.textures = new Texture[0];
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * A Map based on the given set of Tiles and Walls.  Wall information in the
	 * Tiles is ignored, the given wall data is used instead.
	 * 
	 * @param length
	 * 	The length of the map (ie north-south), in grid blocks.
	 * @param width
	 * 	The width of the map (ie east-west), in grid blocks.
	 * @param baseImageSize
	 * 	The base width and height of images in this map (in pixels) 
	 * @param skyConfigs
	 * 	The sky configs, in render order.
	 * @param tiles
	 * 	The grid blocks in this map.  Wall data in this is ignored.
	 * @param horizontalWalls
	 * 	The horizontal walls in this map.  
	 * @param verticalWalls
	 * 	The vertical walls in this map
	 * @param objects
	 * 	The objects in this map
	 */ 
	public Map(
		int length, 
		int width, 
		int baseImageSize,
		Tile[] tiles,
		Texture[] textures,
		Wall[] horizontalWalls,
		Wall[] verticalWalls,
		SkyConfig[] skyConfigs,
		List<EngineObject> objects,
		MapScript[] scripts)
	{
		this.length = length;
		this.width = width;
		this.baseImageSize = baseImageSize;
		this.tiles = tiles;
		this.textures = textures;
		this.horizontalWalls = horizontalWalls;
		this.verticalWalls = verticalWalls;
		this.skyConfigs = skyConfigs;
		this.expandedObjects = objects;
		this.scripts = scripts;

		if (this.expandedObjects != null)
		{
			this.initObjectsFromArray();
		}

		initTextures();
	}

	/*-------------------------------------------------------------------------*/
	public void init()
	{
		validate();
		initObjectsFromArray();
		initTextures();
	}

	private void validate()
	{
		StringBuilder errors = new StringBuilder();

		for (int i = 0; i < horizontalWalls.length; i++)
		{
			Wall w = horizontalWalls[i];
			if (w.isVisible() && w.getTextures().length == 0)
			{
				errors.append("Invalid wall textures: horiz ").append(i).append("\n");
			}
		}
		for (int i = 0; i < verticalWalls.length; i++)
		{
			Wall w = verticalWalls[i];
			if (w.isVisible() && w.getTextures().length == 0)
			{
				errors.append("Invalid wall textures: vert ").append(i).append("\n");
			}
		}

		if (errors.length() > 0)
		{
			throw new MazeException(errors.toString());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void initTextures()
	{
		java.util.Map<String, Texture> texturesMap = new HashMap<>();

		this.textures = new Texture[0];

		for (Tile t : tiles)
		{
			_addTexture(t.getFloorTexture(), texturesMap);
			_addTexture(t.getFloorMaskTexture(), texturesMap);
			_addTexture(t.getCeilingTexture(), texturesMap);
			_addTexture(t.getCeilingMaskTexture(), texturesMap);
			_addTexture(t.getEastWallTexture(), texturesMap);
			_addTexture(t.getWestWallTexture(), texturesMap);
			_addTexture(t.getNorthWallTexture(), texturesMap);
			_addTexture(t.getSouthWallTexture(), texturesMap);
		}

		for (Wall w : horizontalWalls)
		{
			for (Texture t : w.getTextures())
			{
				_addTexture(t, texturesMap);
			}
			if (w.getMaskTextures() != null)
			{
				for (Texture t : w.getMaskTextures())
				{
					_addTexture(t, texturesMap);
				}
			}
		}

		for (Wall w : verticalWalls)
		{
			for (Texture t : w.getTextures())
			{
				_addTexture(t, texturesMap);
			}
			if (w.getMaskTextures() != null)
			{
				for (Texture t : w.getMaskTextures())
				{
					_addTexture(t, texturesMap);
				}
			}
		}

		for (EngineObject obj : expandedObjects)
		{
			for (Texture t : obj.getTextures())
			{
				_addTexture(t, texturesMap);
			}
		}

		for (SkyConfig skyConfig : skyConfigs)
		{
			_addTexture(skyConfig.cylinderSkyImage, texturesMap);
			_addTexture(skyConfig.ceilingImage, texturesMap);
			_addTexture(skyConfig.cubeNorth, texturesMap);
			_addTexture(skyConfig.cubeSouth, texturesMap);
			_addTexture(skyConfig.cubeEast, texturesMap);
			_addTexture(skyConfig.cubeWest, texturesMap);
		}

		// add to the array
		for (Texture t : texturesMap.values())
		{
			addTexture(t);
		}
	}

	private void _addTexture(Texture t, java.util.Map<String, Texture> texturesMap)
	{
		if (t != null && t != NO_WALL)
		{
			texturesMap.put(t.getName(), t);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Extract object data from the tile array
	 */ 
	private void initObjectsFromArray()
	{
		for (EngineObject eo : expandedObjects)
		{
			initObjectFromXY(eo);
		}

		this.renderObjects = (EngineObject[])expandedObjects.toArray(new EngineObject[0]);
	}

	/*-------------------------------------------------------------------------*/
	public void initObjectFromXY(EngineObject obj)
	{
		obj.tileIndex = obj.xPos/baseImageSize + (obj.yPos/baseImageSize)*width;
	}

	/*-------------------------------------------------------------------------*/
	public void initObjectFromTileIndex(EngineObject obj, int placement)
	{
		obj.gridX = obj.tileIndex%width;
		obj.gridY = obj.tileIndex/width;

		int xOffset;
		int yOffset;
		int n = baseImageSize/3;

		switch (placement)
		{
			case EngineObject.Placement.CENTER ->
			{
				xOffset = 0;
				yOffset = 0;
			}
			case EngineObject.Placement.NORTH_WEST ->
			{
				xOffset = -n;
				yOffset = -n;
			}
			case EngineObject.Placement.NORTH ->
			{
				xOffset = 0;
				yOffset = -n;
			}
			case EngineObject.Placement.NORTH_EAST ->
			{
				xOffset = n;
				yOffset = -n;
			}
			case EngineObject.Placement.WEST ->
			{
				xOffset = -n;
				yOffset = 0;
			}
			case EngineObject.Placement.EAST ->
			{
				xOffset = n;
				yOffset = 0;
			}
			case EngineObject.Placement.SOUTH_WEST ->
			{
				xOffset = -n;
				yOffset = n;
			}
			case EngineObject.Placement.SOUTH ->
			{
				xOffset = 0;
				yOffset = n;
			}
			case EngineObject.Placement.SOUTH_EAST ->
			{
				xOffset = n;
				yOffset = n;
			}
			default ->
				throw new CrusaderException("Invalid placement index: " + placement);
		}
		
		obj.xPos = obj.gridX*baseImageSize + baseImageSize/2 + xOffset;
		obj.yPos = obj.gridY*baseImageSize + baseImageSize/2 + yOffset;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * For map editing only
	 */
	public void addObject(EngineObject obj)
	{
		expandedObjects.add(obj);
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * For map editing only
	 */
	public void removeObject(int tileIndex)
	{
		List<EngineObject> newObjects = new ArrayList<>();
		
		for (EngineObject obj : expandedObjects)
		{
			if (obj.getTileIndex() != tileIndex)
			{
				newObjects.add(obj);
			}
		}

		expandedObjects = newObjects;
	}

	public void removeObject(EngineObject obj)
	{
		expandedObjects.remove(obj);
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * For map editing only
	 */
	public List<EngineObject> getObjects(int tileIndex)
	{
		List<EngineObject> result = new ArrayList<>();

		for (EngineObject obj : expandedObjects)
		{
			if (obj.getTileIndex() == tileIndex)
			{
				result.add(obj);
			}
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the index in the {@link #horizontalWalls} array for the south wall
	 * of the given grid index.  This is defined as <code>i + mapWidth</code>.
	 */ 
	public int getSouthWall(int i)
	{
		return i + width;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the index in the {@link #horizontalWalls} array for the north wall
	 * of the given grid index.  This is defined as simply <code>i</code>.
	 */ 
	public int getNorthWall(int i)
	{
		return i;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the index in the {@link #verticalWalls} array for the east wall
	 * of the given grid index.  This is defined as <code>i + i/mapWidth +1</code>.
	 */ 
	public int getEastWall(int i)
	{
		return i + i/width + 1;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the index in the {@link #verticalWalls} array for the west wall
	 * of the given grid index.  This is defined as <code>i + i/mapWidth</code>.
	 */ 
	public int getWestWall(int i)
	{
		return i + i/width;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the index of the block north of the given horizontal wall.
	 */ 
	public int getNorthBlock(int horizontalWall)
	{
		return horizontalWall - width;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the index of the block south of the given horizontal wall.
	 */ 
	public int getSouthBlock(int horizontalWall)
	{
		return horizontalWall;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the index of the block east of the given vertical wall.
	 */ 
	public int getEastBlock(int verticalWall)
	{
		return verticalWall - verticalWall/(width + 1);
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the index of the block west of the given vertical wall.
	 */ 
	public int getWestBlock(int verticalWall)
	{
		return verticalWall - 1 - verticalWall/(width+1);
	}
	
	/*-------------------------------------------------------------------------*/
	void executeScripts(long frameCount)
	{
		synchronized(scriptMutex)
		{
			if (scripts != null)
			{
				for (MapScript script : scripts)
				{
					script.execute(frameCount, this);
				}
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void addScript(MapScript script)
	{
		synchronized(scriptMutex)
		{
			MapScript[] temp;
			
			if (scripts == null)
			{
				temp = new MapScript[]{script};
			}
			else
			{
				temp = new MapScript[scripts.length+1];
				System.arraycopy(scripts, 0, temp, 0, scripts.length);
				temp[scripts.length] = script;
			}
			
			this.scripts = temp;
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public MapScript removeScript(MapScript script)
	{
		synchronized(scriptMutex)
		{
			MapScript[] temp = null;
			
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
			
				temp = new MapScript[scripts.length-1];
				System.arraycopy(scripts, 0, temp, 0, index);
				System.arraycopy(scripts, index+1, temp, index, scripts.length-index-1);
				this.scripts = temp;
				return script;
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Replace one of the map walls with the given wall
	 */
	public void setWall(int index, boolean horizontal, Wall wall)
	{
		Wall[] array = horizontal ? horizontalWalls : verticalWalls;
		array[index] = wall;
	}

	/*-------------------------------------------------------------------------*/
	public Wall getWall(int index, boolean horizontal)
	{
		return horizontal ? horizontalWalls[index] : verticalWalls[index];
	}

	/*-------------------------------------------------------------------------*/
	public Wall[] getWalls(Point tile)
	{
		int gridIndex = getGridIndex(tile.y * width, tile.x);

		return new Wall[]
			{
				horizontalWalls[getNorthWall(gridIndex)],
				horizontalWalls[getSouthWall(gridIndex)],
				verticalWalls[getEastWall(gridIndex)],
				verticalWalls[getWestWall(gridIndex)],
			};
	}

	/*-------------------------------------------------------------------------*/
	private int getGridIndex(int y, int tile)
	{
		return y + tile;
	}

	/*-------------------------------------------------------------------------*/
	/** The width of this map (ie east-west), in grid blocks */
	public int getWidth()
	{
		return width;
	}

	/*-------------------------------------------------------------------------*/
	/** The length of this map (ie north-south), in grid blocks */
	public int getLength()
	{
		return length;
	}
	
	/*-------------------------------------------------------------------------*/
/*
	public void setRenderObjects(EngineObject[] renderObjects)
	{
		this.renderObjects = renderObjects;

		if (renderObjects != null)
		{
			this.originalObjects = new ArrayList<>(Arrays.asList(renderObjects));
		}
		else
		{
			this.originalObjects = new ArrayList<>();
		}
	}

	public EngineObject[] getRenderObjects()
	{
		return renderObjects;
	}
*/

/*
	public void setOriginalObjects(
		List<EngineObject> originalObjects)
	{
		this.originalObjects = originalObjects;
	}
*/

//	public List<EngineObject> getOriginalObjects()
//	{
//		return originalObjects;
//	}

	public List<EngineObject> getExpandedObjects()
	{
		return expandedObjects;
	}

	public void setExpandedObjects(
		List<EngineObject> expandedObjects)
	{
		this.expandedObjects = expandedObjects;
	}

	/*-------------------------------------------------------------------------*/
	public int getBaseImageSize()
	{
		return baseImageSize;
	}

	public Wall[] getHorizontalWalls()
	{
		return horizontalWalls;
	}

	public MapScript[] getScripts()
	{
		return scripts;
	}

	public SkyConfig[] getSkyConfigs()
	{
		return skyConfigs;
	}

	public void setSkyConfigs(SkyConfig[] skyConfigs)
	{
		this.skyConfigs = skyConfigs;
	}

	public int addTexture(Texture txt)
	{
		Texture[] temp = new Texture[textures.length+1];
		System.arraycopy(textures, 0, temp, 0, textures.length);
		temp[textures.length] = txt;
		Arrays.sort(temp);
		textures = temp;

		return Arrays.binarySearch(textures, txt);
	}

	public Texture[] getTextures()
	{
		return textures;
	}

	public Tile[] getTiles()
	{
		return tiles;
	}

	public Wall[] getVerticalWalls()
	{
		return verticalWalls;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public void setTiles(Tile[] tiles)
	{
		this.tiles = tiles;
	}

	public void setHorizontalWalls(Wall[] horizontalWalls)
	{
		this.horizontalWalls = horizontalWalls;
	}

	public void setVerticalWalls(Wall[] verticalWalls)
	{
		this.verticalWalls = verticalWalls;
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public void setBaseImageSize(int baseImageSize)
	{
		this.baseImageSize = baseImageSize;
	}

	public void setTextures(Texture[] textures)
	{
		this.textures = textures;
	}

	/*-------------------------------------------------------------------------*/

	/*-------------------------------------------------------------------------*/
	public int getIndex(Tile t)
	{
		for (int i = 0; i < tiles.length; i++)
		{
			if (tiles[i] == t)
			{
				return i;
			}
		}
		throw new CrusaderException("Tile not found "+t);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	the Tile index that corresponds to the given Point
	 */
	public int getIndex(Point p)
	{
		return p.x + width*p.y;
	}

	/*-------------------------------------------------------------------------*/
	public Point getPoint(int i)
	{
		return new Point(i%width, i/width);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Modifies the light level on all tiles.
	 */
	public void incLightLevel(int inc)
	{
		for (int i = 0; i < tiles.length; i++)
		{
			tiles[i].setCurrentLightLevel(tiles[i].getCurrentLightLevel()+inc);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setScripts(MapScript[] mapScripts)
	{
		this.scripts = mapScripts;
	}

	/*-------------------------------------------------------------------------*/
	public static class SkyConfig
	{
		public enum Type
		{
			CYLINDER_IMAGE,
			CYLINDER_GRADIENT,
			HIGH_CEILING_IMAGE,
			CUBEMAP_IMAGES,
		}

		Type type;

		// cylinder image
		Texture cylinderSkyImage;

		// cylinder gradient
		int bottomColour, topColour;

		// ceiling image
		Texture ceilingImage;
		int ceilingHeight;
		int imageScale;

		// cubemap images
		Texture cubeNorth, cubeSouth, cubeEast, cubeWest;

		public SkyConfig()
		{
		}

		public SkyConfig(Type type, Texture cylinderSkyImage, int bottomColour,
			int topColour, Texture ceilingImage, int ceilingHeight,
			Texture cubeNorth,
			Texture cubeSouth, Texture cubeEast, Texture cubeWest, int imageScale)
		{
			this.type = type;
			this.cylinderSkyImage = cylinderSkyImage;
			this.bottomColour = bottomColour;
			this.topColour = topColour;
			this.ceilingImage = ceilingImage;
			this.ceilingHeight = ceilingHeight;
			this.cubeNorth = cubeNorth;
			this.cubeSouth = cubeSouth;
			this.cubeEast = cubeEast;
			this.cubeWest = cubeWest;
			this.imageScale = imageScale;
		}

		/*----------------------------------------------------------------------*/

		public Type getType()
		{
			return type;
		}

		public void setType(Type type)
		{
			this.type = type;
		}

		public Texture getCylinderSkyImage()
		{
			return cylinderSkyImage;
		}

		public void setCylinderSkyImage(Texture cylinderSkyImage)
		{
			this.cylinderSkyImage = cylinderSkyImage;
		}

		public int getBottomColour()
		{
			return bottomColour;
		}

		public void setBottomColour(int bottomColour)
		{
			this.bottomColour = bottomColour;
		}

		public int getTopColour()
		{
			return topColour;
		}

		public void setTopColour(int topColour)
		{
			this.topColour = topColour;
		}

		public Texture getCeilingImage()
		{
			return ceilingImage;
		}

		public void setCeilingImage(Texture ceilingImage)
		{
			this.ceilingImage = ceilingImage;
		}

		public int getCeilingHeight()
		{
			return ceilingHeight;
		}

		public void setCeilingHeight(int ceilingHeight)
		{
			this.ceilingHeight = ceilingHeight;
		}

		public Texture getCubeNorth()
		{
			return cubeNorth;
		}

		public void setCubeNorth(Texture cubeNorth)
		{
			this.cubeNorth = cubeNorth;
		}

		public Texture getCubeSouth()
		{
			return cubeSouth;
		}

		public void setCubeSouth(Texture cubeSouth)
		{
			this.cubeSouth = cubeSouth;
		}

		public Texture getCubeEast()
		{
			return cubeEast;
		}

		public void setCubeEast(Texture cubeEast)
		{
			this.cubeEast = cubeEast;
		}

		public Texture getCubeWest()
		{
			return cubeWest;
		}

		public void setCubeWest(Texture cubeWest)
		{
			this.cubeWest = cubeWest;
		}

		public int getImageScale()
		{
			return imageScale;
		}

		public void setImageScale(int imageScale)
		{
			this.imageScale = imageScale;
		}
	}
}
