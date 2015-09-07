
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

package mclachlan.maze.map;

import java.awt.Color;
import java.awt.Point;
import java.util.*;
import mclachlan.crusader.Map;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcManager;

/**
 * Describes a zone in which the party can move around.  Includes the 
 * Crusader Engine map definition.
 */
public class Zone
{
	private String name;
	private Map map;
	private Tile[][] tiles;
	private List<Portal> portals;
	private int width;
	private int length;
	private ZoneScript script;
	
	private Color shadeTargetColor;
	private Color transparentColor;
	private boolean doShading;
	private boolean doLighting;
	private double shadingDistance;
	private double shadingMultiplier;
	private int projectionPlaneOffset;
	private int playerFieldOfView;
	private double scaleDistFromProjPlane;

	// balance details
	private int order;
	private Point playerOrigin;

	/*-------------------------------------------------------------------------*/
	public Zone(
		String name,
		Map map,
		Tile[][] tiles,
		Portal[] portals,
		ZoneScript script,
		Color shadeTargetColor,
		Color transparentColor,
		boolean doShading,
		boolean doLighting,
		double shadingDistance,
		double shadingMultiplier,
		int projectionPlaneOffset,
		int playerFieldOfView,
		double scaleDistFromProjPlane,
		int order,
		Point playerOrigin)
	{
		this.name = name;
		this.map = map;
		this.tiles = tiles;
		this.portals = new ArrayList<Portal>(Arrays.asList(portals));
		this.script = script;
		this.width = tiles.length;
		this.length = tiles[0].length;

		this.projectionPlaneOffset = projectionPlaneOffset;
		this.scaleDistFromProjPlane = scaleDistFromProjPlane;
		this.playerFieldOfView = playerFieldOfView;
		this.shadingMultiplier = shadingMultiplier;
		this.shadingDistance = shadingDistance;
		this.doLighting = doLighting;
		this.doShading = doShading;
		this.transparentColor = transparentColor;
		this.shadeTargetColor = shadeTargetColor;

		for (int x = 0; x < tiles.length; x++)
		{
			for (int y = 0; y < tiles[x].length; y++)
			{
				tiles[x][y].setZone(name);
				tiles[x][y].setCoords(new Point(x, y));
			}
		}

		this.order = order;
		this.playerOrigin = playerOrigin;
	}
	
	/*-------------------------------------------------------------------------*/
	public void encounterTile(Maze maze, Point tile, Point previousTile, int facing)
	{
		// NPCs get preference to TileScripts
		Npc[] npcs = NpcManager.getInstance().getNpcsOnTile(name, tile);
		
		if (npcs != null)
		{
			for (Npc npc : npcs)
			{
				// todo: won't work for more than one NPC?
				Maze.getInstance().encounterNpc(npc, tile, previousTile);
			}
		}

		// process any tile scripts present
		List<TileScript> scripts = this.tiles[tile.x][tile.y].getScripts();

		for (TileScript script : scripts)
		{
			if (script != null && Maze.getInstance().getCurrentCombat() == null)
			{
				if (script.shouldExecute(maze, tile, previousTile, facing, -1))
				{
					Maze.getInstance().appendEvents(
						script.execute(maze, tile, previousTile, facing));
				}
			}
			maze.checkPartyStatus();
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean processPlayerAction(
		Maze maze, Point tile, int facing, int playerAction)
	{
		List<TileScript> scripts = this.tiles[tile.x][tile.y].getScripts();

		// scripts take precedence over portals
		for (TileScript script : scripts)
		{
			if (script != null)
			{
				if (script.shouldExecute(maze, tile, tile, facing, playerAction))
				{
					List<MazeEvent> events = script.handlePlayerAction(maze, tile, facing, playerAction);
					if (events == TileScript.PREVENT_ACTION)
					{
						return false;
					}
					maze.appendEvents(events);
				}
			}
		}

		// check if the player is facing a locked portal
		Portal portal = getPortal(tile, facing);

		if (portal != null && portal.getState().equalsIgnoreCase(Portal.State.LOCKED))
		{
			Maze.getInstance().encounterPortal(portal);
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	public boolean processUseItem(
		Maze maze, Point tile, int facing, Item item, PlayerCharacter user)
	{
		List<TileScript> scripts = this.tiles[tile.x][tile.y].getScripts();

		for (TileScript script : scripts)
		{
			if (script != null)
			{
				List<MazeEvent> events = script.handleUseItem(maze, tile, facing, item, user);
				if (events == TileScript.PREVENT_ACTION)
				{
					return false;
				}
				maze.appendEvents(events);
			}
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	an iterator over all tiles in this zone. order not defined.
	 */
	public Iterator<Tile> getTilesIterator()
	{
		List<Tile> list = new ArrayList<Tile>();

		for (Tile[] i : tiles)
		{
			for (Tile j : i)
			{
				list.add(j);
			}
		}

		return list.iterator();
	}

	/*-------------------------------------------------------------------------*/
	public Map getMap()
	{
		return map;
	}

	public String getName()
	{
		return name;
	}

	public Tile getTile(Point p)
	{
		if (tiles == null)
		{
			return null;
		}
		
		return tiles[p.x][p.y];
	}

	public Portal[] getPortals()
	{
		return portals.toArray(new Portal[portals.size()]);
	}

	public Tile[][] getTiles()
	{
		return tiles;
	}

	public int getLength()
	{
		return length;
	}

	public int getWidth()
	{
		return width;
	}

	public ZoneScript getScript()
	{
		return script;
	}

	public boolean doLighting()
	{
		return doLighting;
	}

	public boolean doShading()
	{
		return doShading;
	}

	public int getPlayerFieldOfView()
	{
		return playerFieldOfView;
	}

	public int getProjectionPlaneOffset()
	{
		return projectionPlaneOffset;
	}

	public double getScaleDistFromProjPlane()
	{
		return scaleDistFromProjPlane;
	}

	public Color getShadeTargetColor()
	{
		return shadeTargetColor;
	}

	public double getShadingDistance()
	{
		return shadingDistance;
	}

	public double getShadingMultiplier()
	{
		return shadingMultiplier;
	}

	public Color getTransparentColor()
	{
		return transparentColor;
	}

	public void setDoLighting(boolean doLighting)
	{
		this.doLighting = doLighting;
	}

	public void setDoShading(boolean doShading)
	{
		this.doShading = doShading;
	}

	public void setPlayerFieldOfView(int playerFieldOfView)
	{
		this.playerFieldOfView = playerFieldOfView;
	}

	public void setProjectionPlaneOffset(int projectionPlaneOffset)
	{
		this.projectionPlaneOffset = projectionPlaneOffset;
	}

	public void setScaleDistFromProjPlane(double scaleDistFromProjPlane)
	{
		this.scaleDistFromProjPlane = scaleDistFromProjPlane;
	}

	public void setShadeTargetColor(Color shadeTargetColor)
	{
		this.shadeTargetColor = shadeTargetColor;
	}

	public void setShadingDistance(double shadingDistance)
	{
		this.shadingDistance = shadingDistance;
	}

	public void setShadingMultiplier(double shadingMultiplier)
	{
		this.shadingMultiplier = shadingMultiplier;
	}

	public void setTransparentColor(Color transparentColor)
	{
		this.transparentColor = transparentColor;
	}

	public void setScript(ZoneScript script)
	{
		this.script = script;
	}

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	public Point getPlayerOrigin()
	{
		return playerOrigin;
	}

	public void setPlayerOrigin(Point playerOrigin)
	{
		this.playerOrigin = playerOrigin;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param facing
	 * 	A constant from {@link mclachlan.crusader.CrusaderEngine.Facing}
	 * @return
	 * 	Any Portal that exists if the player steps from the given tile in the
	 * 	given direction, otherwise null.
	 */
	public Portal getPortal(Point oldTile, int facing)
	{
		for (Portal portal : portals)
		{
			if (portal.from.equals(oldTile) && portal.fromFacing == facing ||
				portal.twoWay && portal.to.equals(oldTile) && portal.toFacing == facing)
			{
				return portal;
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public void init(long turnNr)
	{
		this.script.init(this, turnNr);
		
		for (int x = 0; x < tiles.length; x++)
		{
			for (int y = 0; y < tiles[x].length; y++)
			{
				if (tiles[x][y].getScripts() != null)
				{
					List<TileScript> scripts = tiles[x][y].getScripts();

					for (TileScript script : scripts)
					{
						script.initialise(
							Maze.getInstance(),
							new Point(x, y),
							y*width+x);
					}
				}
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void endOfTurn(long turnNr)
	{
		Maze.log("processing zone end of turn...");
		Maze.getInstance().resolveEvents(this.script.endOfTurn(this, turnNr));
		Maze.log("finished zone end of turn");
	}

	/*-------------------------------------------------------------------------*/
	public void removePortal(Portal portal)
	{
		portals.remove(portal);
	}

	/*-------------------------------------------------------------------------*/
	public void addPortal(Portal portal)
	{
		portals.add(portal);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param t
	 * 	The given tile
	 * @param incX
	 * 	A relative X axis measure (west-east)
	 * @param incY
	 * 	A relative Y axis measure (north-south)
	 * @param checkAccess
	 * 	true if walls should restrict access, false if not. If the tiles are
	 * 	not adjacent this has no effect.
	 *
	 * @return
	 * 	The tile relative to the given tile, or null if none exists or one
	 * 	exists but is not accessible.
	 */
	public Tile getTileRelativeTo(Tile t, int incX, int incY, boolean checkAccess)
	{
		Point p = t.getCoords();

		Point newP = new Point(p.x+incX, p.y+incY);

		if (newP.x < 0 || newP.x >= width || newP.y < 0 || newP.y >= length)
		{
			return null;
		}

		Tile result = getTile(newP);

		if (result != null)
		{
			if (checkAccess)
			{
				if (getWallBetween(t, result))
				{
					return null;
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	True if there exists a wall between the given tiles, false otherwise
	 * 	(including if the tiles are not adjacent)
	 */
	public boolean getWallBetween(Tile t1, Tile t2)
	{
		Point p1 = t1.getCoords();
		Point p2 = t2.getCoords();
		if (p2.x == p1.x && p2.y == p1.y-1)
		{
			// t2 is adjacent to the north
			return map.getHorizontalWalls()[map.getNorthWall(map.getIndex(p1))].isVisible();
		}
		else if (p2.x == p1.x && p2.y == p1.y+1)
		{
			// t2 is adjacent to the south
			return map.getHorizontalWalls()[map.getSouthWall(map.getIndex(p1))].isVisible();
		}
		else if (p2.y == p1.y && p2.x == p1.x-1)
		{
			// t2 is adjacent to the west
			return map.getVerticalWalls()[map.getWestWall(map.getIndex(p1))].isVisible();
		}
		else if (p2.y == p1.y && p2.x == p1.x+1)
		{
			// t2 is adjacent to the east
			return map.getVerticalWalls()[map.getEastWall(map.getIndex(p1))].isVisible();
		}
		else
		{
			return false;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Point getPoint(Tile t)
	{
		return t.getCoords();
	}

	/*-------------------------------------------------------------------------*/
	public static class Vector
	{
		public Point location;
		public Portal portal;
		public int facing;

		public Vector(Point location, int facing, Portal portal)
		{
			this.facing = facing;
			this.location = location;
			this.portal = portal;
		}
	}
}
