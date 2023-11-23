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

package mclachlan.crusader.client;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import mclachlan.crusader.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.script.RandomLightingScript;
import mclachlan.crusader.script.SinusoidalLightingScript;

/**
 * Saves and loads the map in a plain text format.
 */
public class ClientMapLoader
{
	public static final String SEP = ": ";
	
	public static final String MAP_HEADER = "-map-";
	public static final String MAP_NAME = "name";
	public static final String MAP_WIDTH = "width";
	public static final String MAP_LENGTH = "length";
	public static final String MAP_IMG_SIZE = "imageSize";
	public static final String SKY_TEXTURE_TYPE = "skyTextureType";

	public static final String BASE_IMG_HEADER = "-base images-";
	public static final String SKY_IMG_HEADER = "-sky image-";
	public static final String PALETTE_IMG_HEADER = "-palette image-";
	public static final String TEXTURE_HEADER = "-textures-";
	public static final String TILE_HEADER = "-tiles-";
	public static final String H_WALL_HEADER = "-horizontal walls-";
	public static final String V_WALL_HEADER = "-vertical walls-";
	public static final String OBJECT_HEADER = "-objects-";
	public static final String SCRIPT_HEADER = "-scripts-";
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Saves the map to the given file.
	 */ 
/*
	public void saveMap(Map map, File file) throws IOException
	{
		MyWriter writer = new MyWriter(new BufferedWriter(new FileWriter(file)));
		
		String mapName = "NoName";
		
		writer.writeln(MAP_HEADER);
		
		writer.writeln(MAP_NAME+SEP+mapName);
		writer.writeln(MAP_WIDTH+SEP+map.width);
		writer.writeln(MAP_LENGTH+SEP+map.length);
		writer.writeln(MAP_IMG_SIZE+SEP+map.imageSize);
		
		writer.writeln("");
		writer.writeln(IMAGE_HEADER);
		
		for (int i = 0; i < map.imageNames.length; i++)
		{
			String texture = map.imageNames[i];
			writer.writeln(i+SEP+texture);
		}
		
		writer.writeln("");
		writer.writeln(TILE_HEADER);
		
		for (int i = 0; i < map.tiles.length; i++)
		{
			Tile tile = map.tiles[i];
			String line = i+SEP+tile.floorTexture+","+tile.ceilingTexture+","+tile.lightLevel;
			writer.writeln(line);
		}
		
		writer.writeln("");
		writer.writeln(H_WALL_HEADER);
		
		for (int i = 0; i < map.horizontalWalls.length; i++)
		{
			Wall wall = map.horizontalWalls[i];
			if (wall.visible)
			{
				String line = i+SEP+wall.texture;
				writer.writeln(line);				
			}
		}
		
		writer.writeln("");
		writer.writeln(V_WALL_HEADER);
		
		for (int i = 0; i < map.verticalWalls.length; i++)
		{
			Wall wall = map.verticalWalls[i];
			if (wall.visible)
			{
				String line = i+SEP+wall.texture;
				writer.writeln(line);				
			}
		}
		
		writer.writeln("");
		writer.writeln(OBJECT_HEADER);
		
		for (int i = 0; i < map.objects.length; i++)
		{
			BillboardObject object = map.objects[i];
			String line = i+SEP+object.texture+","+object.tileIndex;
			writer.writeln(line);
		}
		
		writer.flush();
		writer.close();
	}
*/
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Loads the map from the given file.
	 */ 
	public Map loadMap(File file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		
		String mapName = null;
		Map.SkyTextureType skyTextureType = null;
		int mapWidth = -1, mapLength = -1;
		Tile[] tiles = null;
		BufferedImage[] baseImages = null;
		int skyTextureIndex = 0;
		String[] imageNames = null;
		Wall[] horizontalWalls = null, verticalWalls = null;
		EngineObject[] objects = null;
		Texture[] textures = null;
		MapScript[] scripts = null;
		
		boolean foundHeader = false;
		
		while(line != null)
		{
			if (line.equalsIgnoreCase(MAP_HEADER))
			{
				Properties mapProp = getProperties(reader);
				mapName = mapProp.getProperty(MAP_NAME);
				mapWidth = Integer.parseInt(mapProp.getProperty(MAP_WIDTH));
				mapLength = Integer.parseInt(mapProp.getProperty(MAP_LENGTH));
				skyTextureType = Map.SkyTextureType.valueOf(mapProp.getProperty(SKY_TEXTURE_TYPE));
				foundHeader = true;
			}
			else if (line.equalsIgnoreCase(BASE_IMG_HEADER))
			{
				ArrayList list = new ArrayList();
				line = reader.readLine();
				while (!line.equals(""))
				{
					String[] detail = line.split(SEP);
					list.add(detail[1]);
					line = reader.readLine();
				}
				
				imageNames = new String[list.size()];
				list.toArray(imageNames);
				baseImages = getImages(imageNames);
			}
			else if (line.equalsIgnoreCase(SKY_IMG_HEADER))
			{
				ArrayList list = new ArrayList();
				line = reader.readLine();
				while (!line.equals(""))
				{
					String[] detail = line.split(SEP);
					list.add(detail[1]);
					line = reader.readLine();
				}
				
				skyTextureIndex = Integer.valueOf((String)list.get(0));
			}
			else if (line.equalsIgnoreCase(TEXTURE_HEADER))
			{
				Properties txtProp = getProperties(reader);
				int max = txtProp.keySet().size();
				textures = new Texture[max];
				for (int i=0; i<max; i++)
				{
					String texture = txtProp.getProperty(""+i);
					String[] detail = texture.split(",");
					int nrFrames = Integer.parseInt(detail[0]);
					String scrollB = detail[1];
					int scrollSpeed = Integer.parseInt(detail[2]);
					BufferedImage[] frames = new BufferedImage[nrFrames];
					for (int j = 0; j < frames.length; j++)
					{
						int frameNr = Integer.parseInt(detail[j+3]);
						frames[j] = baseImages[frameNr];
					}
					int animDelay = Integer.parseInt(detail[detail.length-1]);

					Texture.ScrollBehaviour scrollBehaviour = null;

					if (scrollB.length() > 0)
					{
						scrollBehaviour = Texture.ScrollBehaviour.valueOf(scrollB);
					}


					textures[i] = new Texture(
						mapName+"_texture_"+i,
						frames,
						animDelay,
						scrollBehaviour,
						scrollSpeed);
				}
			}
			else if (line.equalsIgnoreCase(TILE_HEADER))
			{
				if (!foundHeader)
				{
					throw new RuntimeException("Not found header");
				}
				
				Properties tileProp = getProperties(reader);
				
				int max = mapLength*mapWidth;
				tiles = new Tile[max];
				for (int i=0; i<max; i++)
				{
					String tile = tileProp.getProperty(""+i);
					String[] detail = tile.split(",");
					int floorTextureNr = Integer.parseInt(detail[0]);
					int lightLevel =  Integer.parseInt(detail[2]);
					Texture ceilingTexture = textures[Integer.parseInt(detail[1])];
					Texture floorTexture = textures[floorTextureNr];
					tiles[i] = new Tile(ceilingTexture, floorTexture, lightLevel);
				}
			}
			else if (line.equalsIgnoreCase(H_WALL_HEADER))
			{
				if (!foundHeader)
				{
					throw new RuntimeException("Not found header");
				}
				
				Properties wallProp = getProperties(reader);
				int max = mapWidth*mapLength + mapWidth;
				horizontalWalls = new Wall[max];
				for (int i = 0; i < horizontalWalls.length; i++)
				{
					horizontalWalls[i] = new Wall(Map.NO_WALL, null, false, false,1, null, null, null);
				}
				
				for (int i=0; i<max; i++)
				{
					String str = wallProp.getProperty("" + i);
					if (str != null)
					{
						String[] wall = str.split(",", -1);
						int textureNr = Integer.parseInt(wall[0]);
						Texture texture = textures[textureNr];
						Texture maskTexture = "".equals(wall[1]) ? null : textures[Integer.parseInt(wall[1])];
						int height = Integer.parseInt(wall[2]);
						horizontalWalls[i] = new Wall(texture, maskTexture, true, true, height, null, null, null);
					}
				}
			}
			else if (line.equalsIgnoreCase(V_WALL_HEADER))
			{
				if (!foundHeader)
				{
					throw new RuntimeException("Not found header");
				}
				
				Properties wallProp = getProperties(reader);
				int max = mapWidth*mapLength + mapLength;
				verticalWalls = new Wall[max];
				for (int i = 0; i < verticalWalls.length; i++)
				{
					verticalWalls[i] = new Wall(Map.NO_WALL, null, false, false, 1, null, null, null);
				}
				
				for (int i=0; i<max; i++)
				{
					String str = wallProp.getProperty("" + i);
					if (str != null)
					{
						String[] wall = str.split(",", -1);
						int textureNr = Integer.parseInt(wall[0]);
						Texture texture = textures[textureNr];
						Texture maskTexture = "".equals(wall[1]) ? null : textures[Integer.parseInt(wall[1])];
						int height = Integer.parseInt(wall[2]);
						verticalWalls[i] = new Wall(texture, maskTexture, true, true, height, null, null, null);
					}
				}
			}
			else if (line.equalsIgnoreCase(OBJECT_HEADER))
			{
				// order of objects doesn't matter
				Properties objProp = getProperties(reader);
				Iterator i = objProp.keySet().iterator();
				ArrayList result = new ArrayList();
				while (i.hasNext())
				{
					String key = (String)i.next();
					String object = objProp.getProperty(key);
					String[] detail = object.split(",");
					int northTextureNr = Integer.parseInt(detail[0]);
					int southTextureNr = Integer.parseInt(detail[1]);
					int eastTextureNr = Integer.parseInt(detail[2]);
					int westTextureNr = Integer.parseInt(detail[3]);
					int tileIndex = Integer.parseInt(detail[4]);
					boolean isLightSource = Boolean.valueOf(detail[5]).booleanValue();
					Texture northTexture = textures[northTextureNr];
					Texture southTexture = textures[southTextureNr];
					Texture eastTexture = textures[eastTextureNr];
					Texture westTexture = textures[westTextureNr];
					result.add(
						new EngineObject(
							null,
							northTexture, 
							southTexture, 
							eastTexture, 
							westTexture, 
							tileIndex, 
							isLightSource,
							null,
							null,
							EngineObject.Alignment.BOTTOM));
				}
				
				objects = new EngineObject[result.size()];
				result.toArray(objects);
			}
			else if (line.equalsIgnoreCase(SCRIPT_HEADER))
			{
				Properties scriptProp = getProperties(reader);
				int max = scriptProp.keySet().size();
				scripts = new MapScript[max];
				for (int i = 0; i < scripts.length; i++)
				{
					String script = scriptProp.getProperty(""+i);
					String[] detail = script.split(",");
					int scriptID = Integer.parseInt(detail[0]);
					scripts[i] = getScript(scriptID, detail);
				}
			}
			
			line = reader.readLine();
		}
		
		reader.close();
		
		int baseImageSize = baseImages[0].getHeight();

		if (objects == null)
		{
			objects = new EngineObject[0];
		}
		if (scripts == null)
		{
			scripts = new MapScript[0];
		}
		
		return new Map(
			mapLength,
			mapWidth,
			baseImageSize, 
			skyTextureIndex,
			skyTextureType,
			tiles,
			textures,
			horizontalWalls,
			verticalWalls,
			objects,
			scripts);
	}

	/*-------------------------------------------------------------------------*/
	private MapScript getScript(int scriptID, String[] detail)
	{
		switch(scriptID)
		{
			case 1: 
				{
					int nrAffectedTiles = Integer.parseInt(detail[1]);
					int[] affectedTiles = new int[nrAffectedTiles];
					for (int i = 0; i < affectedTiles.length; i++)
					{
						affectedTiles[i] = Integer.parseInt(detail[i+2]);
					}
					int frequency = Integer.parseInt(detail[nrAffectedTiles+2]);
					int min = Integer.parseInt(detail[nrAffectedTiles+3]);
					int max = Integer.parseInt(detail[nrAffectedTiles+4]);

					return new SinusoidalLightingScript(affectedTiles, frequency, min, max);
				}
			case 2:
				{
					int nrAffectedTiles = Integer.parseInt(detail[1]);
					int[] affectedTiles = new int[nrAffectedTiles];
					for (int i = 0; i < affectedTiles.length; i++)
					{
						affectedTiles[i] = Integer.parseInt(detail[i+2]);
					}
					int frequency = Integer.parseInt(detail[nrAffectedTiles+2]);
					int min = Integer.parseInt(detail[nrAffectedTiles+3]);
					int max = Integer.parseInt(detail[nrAffectedTiles+4]);
					
					return new RandomLightingScript(affectedTiles, frequency, min, max);
				}
			default:
				throw new RuntimeException("Unrecognised script ID: "+scriptID);
		}
	}

	/*-------------------------------------------------------------------------*/
	private BufferedImage[] getImages(String[] imageNames)
	{
		BufferedImage[] result = new BufferedImage[imageNames.length];
		
		for (int i = 0; i < result.length; i++)
		{
			result[i] = getImage(imageNames[i]);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public BufferedImage getImage(String fileName)
	{
		try
		{
			return ImageIO.read(new File(fileName));
		}
		catch (IOException e)
		{
			throw new CrusaderException("["+fileName+"]",e);
		}
	}

	/*-------------------------------------------------------------------------*/
	private Properties getProperties(BufferedReader reader)
		throws IOException
	{
		String line;
		StringBuilder s = new StringBuilder();
		line = reader.readLine();
		while (line != null && !line.equals(""))
		{
			s.append(line);
			s.append("\n");
			line = reader.readLine();
		}
				
		Properties p = new Properties();
		ByteArrayInputStream inStream = new ByteArrayInputStream(s.toString().getBytes());
		p.load(inStream);
		inStream.close();
		
		return p;
	}

	/*-------------------------------------------------------------------------*/
/*
	private static class MyWriter extends Writer
	{
		BufferedWriter writer;

		public MyWriter(BufferedWriter writer)
		{
			this.writer = writer;
		}
		
		private void writeln(String s) throws IOException
		{
			writer.write(s);
			writer.newLine();
		}

		public void close() throws IOException
		{
			writer.close();
		}

		public void flush() throws IOException
		{
			writer.flush();
		}

		public void write(char cbuf[], int off, int len) throws IOException
		{
			writer.write(cbuf, off, len);
		}
	}
*/
	
	/*-------------------------------------------------------------------------*/
	public static void main(String[] args)
//		throws AWTException, IOException
	{
//		CrusaderClient c = new CrusaderClient(args);
//		Map m = OldMap.getOldMap(c);
//		
//		new ClientMapLoader().saveMap(m, new File("testMap.txt"));
//		
//		c.run();
	}
}
