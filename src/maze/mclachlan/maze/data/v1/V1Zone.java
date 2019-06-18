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

package mclachlan.maze.data.v1;

import java.awt.Color;
import java.awt.Point;
import java.io.*;
import java.util.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.ZoneScript;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1Zone
{
	public static final String MAZE_ZONE_HEADER = "-maze zone-";
	public static final String MAZE_ZONE_NAME = "name";
	public static final String MAZE_ZONE_WIDTH = "width";
	public static final String MAZE_ZONE_LENGTH = "length";
	public static final String MAZE_ZONE_SCRIPT = "zoneScript";
	public static final String MAZE_TILE_HEADER = "-maze tiles-";
	public static final String MAZE_PORTAL_HEADER = "-maze portals-";

	public static final String CRUSADER_BASE_IMG_HEADER = "-base images-";
	public static final String CRUSADER_SKY_IMG_HEADER = "-sky image-";
	public static final String CRUSADER_PALETTE_IMG_HEADER = "-palette image-";
	public static final String CRUSADER_TEXTURE_HEADER = "-textures-";
	public static final String CRUSADER_TILE_HEADER = "-tiles-";
	public static final String CRUSADER_H_WALL_HEADER = "-horizontal walls-";
	public static final String CRUSADER_V_WALL_HEADER = "-vertical walls-";
	public static final String CRUSADER_OBJECT_HEADER = "-objects-";
	public static final String CRUSADER_SCRIPT_HEADER = "-scripts-";

	private static final String SEP = ",";
	private static final String WALL_SEP = ":";

	/*-------------------------------------------------------------------------*/
	public static Zone load(BufferedReader reader) throws Exception
	{
		String line = reader.readLine();
		if (!line.equals(MAZE_ZONE_HEADER))
		{
			throw new MazeException("invalid header ["+line+"]");
		}

		String mapName = null;
		int mapWidth = -1;
		int mapLength = -1;
		Texture skyTexture = null;
		ZoneScript script = null;
		Color shadeTargetColor;
		Color transparentColor;
		boolean doShading;
		boolean doLighting;
		double shadingDistance;
		double shadingMultiplier;
		int projectionPlaneOffset;
		int playerFieldOfView;
		double scaleDistFromProjPlane;
		mclachlan.maze.map.Tile[][] mazeTiles = null;
		Portal[] portals = null;
		Tile[] crusaderTiles = null;
		Wall[] horizontalWalls = null;
		Wall[] verticalWalls = null;
		EngineObject[] objects = null;
		java.util.Map<String, Texture> textures = new HashMap<String, Texture>();
		MapScript[] scripts = null;
		int order = 0;
		Point playerOrigin = null;

		Properties p = getProperties(reader);
		if (p.containsKey("impl"))
		{
			// custom zone impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (Zone)clazz.newInstance();
		}
		else
		{
			mapName = p.getProperty(MAZE_ZONE_NAME);
			mapWidth = Integer.parseInt(p.getProperty(MAZE_ZONE_WIDTH));
			mapLength = Integer.parseInt(p.getProperty(MAZE_ZONE_LENGTH));
		 	script = V1ZoneScript.fromString(p.getProperty(MAZE_ZONE_SCRIPT));
			shadeTargetColor = V1Colour.fromString(p.getProperty("shadeTargetColor"));
			transparentColor = V1Colour.fromString(p.getProperty("transparentColor"));
			doLighting = Boolean.valueOf(p.getProperty("doLighting"));
			doShading = Boolean.valueOf(p.getProperty("doShading"));
			shadingDistance = Double.parseDouble(p.getProperty("shadingDistance"));
			shadingMultiplier = Double.parseDouble(p.getProperty("shadingMultiplier"));
			projectionPlaneOffset = Integer.parseInt(p.getProperty("projectionPlaneOffset"));
			playerFieldOfView = Integer.parseInt(p.getProperty("playerFieldOfView"));
			scaleDistFromProjPlane = Double.parseDouble(p.getProperty("scaleDistFromProjPlane"));
			order = Integer.parseInt(p.getProperty("order"));
			playerOrigin = V1Point.fromString(p.getProperty("playerOrigin"));
		}

		line = reader.readLine();
		while (line != null)
		{
			if (line.equals(MAZE_TILE_HEADER))
			{
				p = getProperties(reader);
				mazeTiles = new mclachlan.maze.map.Tile[mapWidth][mapLength];
				for (int y=0; y<mapLength; y++)
				{
					for (int x=0; x<mapWidth; x++)
					{
						mazeTiles[x][y] = V1Tile.fromString(p.getProperty(String.valueOf((x+y*mapWidth))));
					}
				}
			}
			else if (line.equals(MAZE_PORTAL_HEADER))
			{
				p = getProperties(reader);
				List<Portal> list = new ArrayList<Portal>();
				int count=0;
				while (true)
				{
					String s = p.getProperty(String.valueOf(count++));
					if (s == null)
					{
						break;
					}
					list.add(V1Portal.fromString(s));
				}
				portals = list.toArray(new Portal[list.size()]);
			}
			else if (line.equals(CRUSADER_SKY_IMG_HEADER))
			{
				p = getProperties(reader);
				MazeTexture texture = Database.getInstance().getMazeTexture(p.getProperty("0"));
				skyTexture = texture.getTexture();
			}
			else if (line.equals(CRUSADER_TILE_HEADER))
			{
				p = getProperties(reader);

				int max = mapLength*mapWidth;
				crusaderTiles = new Tile[max];
				for (int i=0; i<max; i++)
				{
					String tile = p.getProperty(""+i);
					String[] strs = tile.split(SEP);
					String floorTextureName = strs[0];
					String floorMaskTextureName = strs[1];
					String ceilingTextureName = strs[2];
					String ceilingMaskTextureName = strs[3];
					int lightLevel =  Integer.parseInt(strs[4]);
					Texture ceilingTexture = Database.getInstance().getMazeTexture(ceilingTextureName).getTexture();
					Texture ceilingMaskTexture = null;
					if (ceilingMaskTextureName != null && ceilingMaskTextureName.length() > 0)
					{
						ceilingMaskTexture = Database.getInstance().getMazeTexture(ceilingMaskTextureName).getTexture();
					}
					Texture floorTexture = Database.getInstance().getMazeTexture(floorTextureName).getTexture();
					Texture floorMaskTexture = null;
					if (floorMaskTextureName != null && floorMaskTextureName.length() > 0)
					{
						floorMaskTexture = Database.getInstance().getMazeTexture(floorMaskTextureName).getTexture();
					}
					crusaderTiles[i] = new Tile(
						ceilingTexture,
						ceilingMaskTexture,
						floorTexture,
						floorMaskTexture,
						Map.NO_WALL,
						Map.NO_WALL,
						Map.NO_WALL,
						Map.NO_WALL,
						false,
						lightLevel,
						1);

					addTexture(ceilingTexture, textures);
					addTexture(ceilingMaskTexture, textures);
					addTexture(floorTexture, textures);
					addTexture(floorMaskTexture, textures);
				}
			}
			else if (line.equals(CRUSADER_H_WALL_HEADER))
			{
				p = getProperties(reader);

				int max = mapLength*mapWidth + mapWidth;
				horizontalWalls = new Wall[max];
				for (int i = 0; i < horizontalWalls.length; i++)
				{
					horizontalWalls[i] = new Wall(Map.NO_WALL, null, false, false, 1, null, null);
				}
				for (int i=0; i<max; i++)
				{
					String wall = p.getProperty(""+i);
					if (wall != null)
					{
						String[] strs = wall.split(WALL_SEP, -1);
						String wallTexture = strs[0];
						String maskTexture = strs[1];
						MouseClickScript mcs = V1MouseClickScript.fromString(strs[2]);
						MouseClickScript mtmcs = V1MouseClickScript.fromString(strs[3]);
						boolean solid = Boolean.valueOf(strs[4]);
						Texture texture = Database.getInstance().getMazeTexture(wallTexture).getTexture();
						Texture mask = maskTexture.equals("") ? null 
							:Database.getInstance().getMazeTexture(maskTexture).getTexture();
						horizontalWalls[i] = new Wall(texture, mask, true, solid, 1, mcs, mtmcs);

						addTexture(texture, textures);
						addTexture(mask, textures);
					}
				}
			}
			else if (line.equals(CRUSADER_V_WALL_HEADER))
			{
				p = getProperties(reader);

				int max = mapLength*mapWidth + mapLength;
				verticalWalls = new Wall[max];
				for (int i = 0; i < verticalWalls.length; i++)
				{
					verticalWalls[i] = new Wall(Map.NO_WALL, null, false, false, 1, null, null);
				}
				for (int i=0; i<max; i++)
				{
					String wall = p.getProperty(""+i);
					if (wall != null)
					{
						String[] strs = wall.split(WALL_SEP, -1);
						String wallTexture = strs[0];
						String maskTexture = strs[1];
						MouseClickScript mcs = V1MouseClickScript.fromString(strs[2]);
						MouseClickScript mtmcs = V1MouseClickScript.fromString(strs[3]);
						boolean solid = Boolean.valueOf(strs[4]);
						Texture texture = Database.getInstance().getMazeTexture(wallTexture).getTexture();
						Texture mask = maskTexture.equals("") ? null 
							: Database.getInstance().getMazeTexture(maskTexture).getTexture();
						verticalWalls[i] = new Wall(texture, mask, true, solid, 1, mcs, mtmcs);

						addTexture(texture, textures);
						addTexture(mask, textures);
					}
				}
			}
			else if (line.equals(CRUSADER_OBJECT_HEADER))
			{
				p = getProperties(reader);

				List<EngineObject> list = new ArrayList<EngineObject>();
				int count = 0;
				while(true)
				{
					String s = p.getProperty(String.valueOf(count++));
					if (s == null)
					{
						break;
					}

					String[] strs = s.split(WALL_SEP, -1);
					Texture northTexture = Database.getInstance().getMazeTexture(strs[0]).getTexture();
					Texture southTexture = Database.getInstance().getMazeTexture(strs[1]).getTexture();
					Texture eastTexture = Database.getInstance().getMazeTexture(strs[2]).getTexture();
					Texture westTexture = Database.getInstance().getMazeTexture(strs[3]).getTexture();
					int tileIndex = Integer.parseInt(strs[4]);
					boolean isLightSource = Boolean.valueOf(strs[5]);
					MouseClickScript mouseClickScript = V1MouseClickScript.fromString(strs[6]);
					BitSet placementMask = V1BitSet.fromString(strs[7]);
					String name = "".equals(strs[8])?null:strs[8];
					
					list.add(new EngineObject(
						name,
						northTexture, 
						southTexture, 
						eastTexture, 
						westTexture, 
						tileIndex, 
						isLightSource,
						mouseClickScript,
						placementMask));

					addTexture(northTexture, textures);
					addTexture(southTexture, textures);
					addTexture(eastTexture, textures);
					addTexture(westTexture, textures);
				}
				objects = list.toArray(new EngineObject[list.size()]);
			}
			else if (line.equals(CRUSADER_SCRIPT_HEADER))
			{
				p = getProperties(reader);
				
				List<MapScript> list = new ArrayList<MapScript>();
				int count = 0;
				while(true)
				{
					String s = p.getProperty(String.valueOf(count++));
					if (s == null)
					{
						break;
					}

					list.add(V1CrusaderMapScript.fromString(s));
				}
				scripts = list.toArray(new MapScript[list.size()]);
			}

			line = reader.readLine();
		}

		// hack to get image size
		int baseImageSize = crusaderTiles[0].getFloorTexture().getImageHeight();

		addTexture(skyTexture, textures);
		Texture[] textureArray = textures.values().toArray(new Texture[textures.values().size()]);

		int skyImageIndex = -1;
		for (int i = 0; i < textureArray.length; i++)
		{
			if (textureArray[i].getName().equals(skyTexture.getName()))
			{
				skyImageIndex = i;
				break;
			}
		}

		Map crusaderMap = new Map(
			mapLength,
			mapWidth,
			baseImageSize,
			skyImageIndex,
			Map.SkyTextureType.CYLINDER, // todo
			crusaderTiles,
			textureArray,
			horizontalWalls,
			verticalWalls,
			objects,
			scripts);

		return new Zone(
			mapName, 
			crusaderMap, 
			mazeTiles, 
			portals, 
			script,
			shadeTargetColor, 
			transparentColor, 
			doShading, 
			doLighting, 
			shadingDistance, 
			shadingMultiplier, 
			projectionPlaneOffset, 
			playerFieldOfView, 
			scaleDistFromProjPlane,
			order,
			playerOrigin);
	}

	/*-------------------------------------------------------------------------*/
	private static void addTexture(Texture texture, java.util.Map<String, Texture> map)
	{
		if (texture != null)
		{
			map.put(texture.getName(), texture);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter bw, Zone zone) throws Exception
	{
		String name = zone.getName();
		int width = zone.getWidth();
		int length = zone.getLength();

		MyWriter writer = new MyWriter(bw);
		writer.writeln(MAZE_ZONE_HEADER);
		writer.writeln(MAZE_ZONE_NAME+": "+name);

		if (zone.getClass() != Zone.class)
		{
			writer.writeln("impl="+zone.getClass().getName());
			return;
		}

		writer.writeln(MAZE_ZONE_WIDTH+": "+width);
		writer.writeln(MAZE_ZONE_LENGTH+": "+length);
		writer.writeln(MAZE_ZONE_SCRIPT+": "+V1ZoneScript.toString(zone.getScript()));
		writer.writeln("shadeTargetColor: "+V1Colour.toString(zone.getShadeTargetColor()));
		writer.writeln("transparentColor: "+V1Colour.toString(zone.getTransparentColor()));
		writer.writeln("doShading: "+zone.doShading());
		writer.writeln("doLighting: "+zone.doLighting());
		writer.writeln("shadingDistance: "+zone.getShadingDistance());
		writer.writeln("shadingMultiplier: "+zone.getShadingMultiplier());
		writer.writeln("projectionPlaneOffset: "+zone.getProjectionPlaneOffset());
		writer.writeln("playerFieldOfView: "+zone.getPlayerFieldOfView());
		writer.writeln("scaleDistFromProjPlane: "+zone.getScaleDistFromProjPlane());
		writer.writeln("order: "+zone.getOrder());
		writer.writeln("playerOrigin: "+V1Point.toString(zone.getPlayerOrigin()));

		writer.writeln();
		writer.writeln(MAZE_TILE_HEADER);
		for (int y=0; y<length; y++)
		{
			for (int x=0; x<width; x++)
			{
				writer.writeln((x+y*width)+": "+V1Tile.toString(zone.getTiles()[x][y]));
			}
		}
		writer.writeln();

		writer.writeln(MAZE_PORTAL_HEADER);
		for (int i=0; i<zone.getPortals().length; i++)
		{
			writer.writeln(i+": "+V1Portal.toString(zone.getPortals()[i]));
		}
		writer.writeln();

		Map map = zone.getMap();

		writer.writeln(CRUSADER_SKY_IMG_HEADER);
		writer.writeln(0+": "+ map.getSkyTexture().getName());
		writer.writeln();

		Tile[] tiles = map.getTiles();
		writer.writeln(CRUSADER_TILE_HEADER);
		for (int i = 0; i < tiles.length; i++)
		{
			writer.write(i+": ");
			writer.write(tiles[i].getFloorTexture().getName());
			writer.write(SEP);
			if (tiles[i].getFloorMaskTexture() != null)
			{
				writer.write(tiles[i].getFloorMaskTexture().getName());
			}
			writer.write(SEP);
			writer.write(tiles[i].getCeilingTexture().getName());
			writer.write(SEP);
			if (tiles[i].getCeilingMaskTexture() != null)
			{
				writer.write(tiles[i].getCeilingMaskTexture().getName());
			}
			writer.write(SEP);
			writer.writeln(String.valueOf(tiles[i].getLightLevel()));
		}
		writer.writeln();

		Wall[] horizWalls = map.getHorizontalWalls();
		writer.writeln(CRUSADER_H_WALL_HEADER);
		for (int i = 0; i < horizWalls.length; i++)
		{
			Texture texture = horizWalls[i].getTexture();
			if (horizWalls[i].isVisible() || horizWalls[i].isSolid())
			{
				writer.write(i+": ");
				writer.write(texture.getName());
				writer.write(WALL_SEP);
				if (horizWalls[i].getMaskTexture() != null)
				{
					writer.write(horizWalls[i].getMaskTexture().getName());
				}
				writer.write(WALL_SEP);
				if (horizWalls[i].getMouseClickScript() != null)
				{
					writer.write(V1MouseClickScript.toString(horizWalls[i].getMouseClickScript()));
				}
				writer.write(WALL_SEP);
				if (horizWalls[i].getMaskTextureMouseClickScript() != null)
				{
					writer.write(V1MouseClickScript.toString(horizWalls[i].getMaskTextureMouseClickScript()));
				}
				writer.write(WALL_SEP);
				writer.write(String.valueOf(horizWalls[i].isSolid()));
				writer.writeln();
			}
		}
		writer.writeln();

		Wall[] vertWalls = map.getVerticalWalls();
		writer.writeln(CRUSADER_V_WALL_HEADER);
		for (int i = 0; i < vertWalls.length; i++)
		{
			Texture texture = vertWalls[i].getTexture();
			if (vertWalls[i].isVisible() || vertWalls[i].isSolid())
			{
				writer.write(i+": ");
				writer.write(texture.getName());
				writer.write(WALL_SEP);
				if (vertWalls[i].getMaskTexture() != null)
				{
					writer.write(vertWalls[i].getMaskTexture().getName());
				}
				writer.write(WALL_SEP);
				if (vertWalls[i].getMouseClickScript() != null)
				{
					writer.write(V1MouseClickScript.toString(vertWalls[i].getMouseClickScript()));
				}
				writer.write(WALL_SEP);
				if (vertWalls[i].getMaskTextureMouseClickScript() != null)
				{
					writer.write(V1MouseClickScript.toString(vertWalls[i].getMaskTextureMouseClickScript()));
				}
				writer.write(WALL_SEP);
				writer.write(String.valueOf(vertWalls[i].isSolid()));
				writer.writeln();
			}
		}
		writer.writeln();

		// only persist the original objects
		List<EngineObject> objects = map.getOriginalObjects();
		writer.writeln(CRUSADER_OBJECT_HEADER);
		for (int i = 0; i < objects.size(); i++)
		{
			EngineObject obj = objects.get(i);

			writer.write(i+": ");
			writer.write(obj.getNorthTexture().getName());
			writer.write(WALL_SEP);
			writer.write(obj.getSouthTexture().getName());
			writer.write(WALL_SEP);
			writer.write(obj.getEastTexture().getName());
			writer.write(WALL_SEP);
			writer.write(obj.getWestTexture().getName());
			writer.write(WALL_SEP);
			writer.write(String.valueOf(obj.getTileIndex()));
			writer.write(WALL_SEP);
			writer.write(String.valueOf(obj.isLightSource()));
			writer.write(WALL_SEP);
			writer.write(V1MouseClickScript.toString(obj.getMouseClickScript()));
			writer.write(WALL_SEP);
			writer.write(V1BitSet.toString(obj.getPlacementMask()));
			writer.write(WALL_SEP);
			writer.writeln(obj.getName()==null?"":obj.getName());
		}
		writer.writeln();

		MapScript[] scripts = map.getScripts();
		writer.writeln(CRUSADER_SCRIPT_HEADER);
		for (int i = 0; i < scripts.length; i++)
		{
			writer.writeln(i+": "+V1CrusaderMapScript.toString(scripts[i]));
		}
		writer.writeln();
	}

	/*-------------------------------------------------------------------------*/
	private static Properties getProperties(BufferedReader reader)
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

		public void writeln() throws IOException
		{
			writer.newLine();
		}
	}
}
