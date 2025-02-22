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

	/** @deprecated */
	ImageGroup paletteImage;

	int skyTextureIndex;
	
	Tile[] tiles;
	Wall[] horizontalWalls;
	Wall[] verticalWalls;
	
	EngineObject[] objects;
	List<EngineObject> originalObjects;
	Texture[] textures;
	MapScript[] scripts;
	SkyTextureType skyTextureType;

	private final Object scriptMutex = new Object();
	int currentSkyImage;

	public enum SkyTextureType
	{
		/** sky texture is rendered as if it were a cylinder around the map */
		CYLINDER,
		/** sky texture is rendered as if it were a very high flat ceiling */
		HIGH_CEILING
	}

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
	 * @param skyTextureIndex
	 * 	The sky texture.
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
		int skyTextureIndex,
		SkyTextureType skyTextureType,
		Tile[] tiles,
		Texture[] textures,
		Wall[] horizontalWalls,
		Wall[] verticalWalls,
		EngineObject[] objects,
		MapScript[] scripts)
	{
		this.length = length;
		this.width = width;
		this.baseImageSize = baseImageSize;
		this.skyTextureIndex = skyTextureIndex;
		this.skyTextureType = skyTextureType;
		this.tiles = tiles;
		this.textures = textures;
		this.horizontalWalls = horizontalWalls;
		this.verticalWalls = verticalWalls;
		this.objects = objects;
		this.scripts = scripts;

		if (objects != null)
		{
			this.initObjectsFromArray();
		}

		initTextures();
	}

	/*-------------------------------------------------------------------------*/
	public void init()
	{
		initObjectsFromArray();
		initTextures();
	}

	/*-------------------------------------------------------------------------*/
	private void initTextures()
	{
		java.util.Map<String, Texture> texturesMap = new HashMap<>();

		Texture skyTexture = getSkyTexture();
		this.textures = new Texture[0];

		texturesMap.put(skyTexture.getName(), skyTexture);
		addTexture(skyTexture);

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

		for (EngineObject obj : objects)
		{
			for (Texture t : obj.getTextures())
			{
				_addTexture(t, texturesMap);
			}
		}

		// add to the array
		for (Texture t : texturesMap.values())
		{
			addTexture(t);
		}

		setSkyTexture(skyTexture);
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
		List<EngineObject> newObjects = new ArrayList<>();

		// place each object in the middle of it's grid block
		for (int i = 0; i < this.objects.length; i++)
		{
			EngineObject obj = objects[i];
			
			if (obj.placementMask == null)
			{
				initObject(obj, EngineObject.Placement.CENTER);
				newObjects.add(obj);
			}
			else
			{
				for (int j=0; j<=9; j++)
				{
					if (obj.placementMask.get(j))
					{
						EngineObject clone = new EngineObject(obj);
						initObject(clone, j);
						newObjects.add(clone);
					}
				}
			}
		}
		
		this.originalObjects = new ArrayList<>(Arrays.asList(objects));
		this.objects = (EngineObject[])newObjects.toArray(new EngineObject[0]);
	}

	/*-------------------------------------------------------------------------*/
	void initObject(EngineObject obj, int placement)
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
		originalObjects.add(obj);
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * For map editing only
	 */
	public void removeObject(int tileIndex)
	{
		List<EngineObject> newObjects = new ArrayList<EngineObject>();
		
		for (EngineObject obj : originalObjects)
		{
			if (obj.getTileIndex() != tileIndex)
			{
				newObjects.add(obj);
			}
		}
		
		originalObjects = newObjects;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * For map editing only
	 */
	public EngineObject getObject(int tileIndex)
	{
		for (EngineObject obj : originalObjects)
		{
			if (obj.getTileIndex() == tileIndex)
			{
				return obj;
			}
		}
		
		return null;
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
	public void setCurrentSkyImage(int index)
	{
		this.currentSkyImage = index;
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

	public EngineObject[] getObjects()
	{
		return objects;
	}

	public ImageGroup getPaletteImage()
	{
		return paletteImage;
	}

	public MapScript[] getScripts()
	{
		return scripts;
	}

	public int getSkyTextureIndex()
	{
		return skyTextureIndex;
	}

	public void setSkyTextureIndex(int skyTextureIndex)
	{
		this.skyTextureIndex = skyTextureIndex;
	}

	public Texture getSkyTexture()
	{
		return textures[skyTextureIndex];
	}

	public void setSkyTexture(Texture txt)
	{
		skyTextureIndex = Arrays.binarySearch(textures, txt);

		if (skyTextureIndex < 0)
		{
			skyTextureIndex = addTexture(txt);
		}
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

	public List<EngineObject> getOriginalObjects()
	{
		return originalObjects;
	}

	public SkyTextureType getSkyTextureType()
	{
		return skyTextureType;
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

	public void setPaletteImage(ImageGroup paletteImage)
	{
		this.paletteImage = paletteImage;
	}

	public void setObjects(EngineObject[] objects)
	{
		this.objects = objects;
	}

	public void setOriginalObjects(
		List<EngineObject> originalObjects)
	{
		this.originalObjects = originalObjects;
	}

	public void setTextures(Texture[] textures)
	{
		this.textures = textures;
	}

	public void setSkyTextureType(SkyTextureType skyTextureType)
	{
		this.skyTextureType = skyTextureType;
	}

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

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Map))
		{
			return false;
		}

		Map map = (Map)o;

		if (getWidth() != map.getWidth())
		{
			return false;
		}
		if (getLength() != map.getLength())
		{
			return false;
		}
		if (getBaseImageSize() != map.getBaseImageSize())
		{
			return false;
		}
		if (getSkyTextureIndex() != map.getSkyTextureIndex())
		{
			return false;
		}
		if (getPaletteImage() != null ? !getPaletteImage().equals(map.getPaletteImage()) : map.getPaletteImage() != null)
		{
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(getTiles(), map.getTiles()))
		{
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(getHorizontalWalls(), map.getHorizontalWalls()))
		{
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(getVerticalWalls(), map.getVerticalWalls()))
		{
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(getObjects(), map.getObjects()))
		{
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(getTextures(), map.getTextures()))
		{
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(getScripts(), map.getScripts()))
		{
			return false;
		}
		return getSkyTextureType() == map.getSkyTextureType();
	}

	@Override
	public int hashCode()
	{
		int result = getWidth();
		result = 31 * result + getLength();
		result = 31 * result + getBaseImageSize();
		result = 31 * result + (getPaletteImage() != null ? getPaletteImage().hashCode() : 0);
		result = 31 * result + getSkyTextureIndex();
		result = 31 * result + Arrays.hashCode(getTiles());
		result = 31 * result + Arrays.hashCode(getHorizontalWalls());
		result = 31 * result + Arrays.hashCode(getVerticalWalls());
		result = 31 * result + Arrays.hashCode(getObjects());
		result = 31 * result + Arrays.hashCode(getTextures());
		result = 31 * result + Arrays.hashCode(getScripts());
		result = 31 * result + (getSkyTextureType() != null ? getSkyTextureType().hashCode() : 0);
		return result;
	}
}
