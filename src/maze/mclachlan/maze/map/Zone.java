
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
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.crusader.MouseClickScriptAdapter;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcManager;

/**
 * Describes a zone in which the party can move around.  Includes the 
 * Crusader Engine map definition.
 */
public class Zone extends DataObject
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

	public Zone()
	{
	}

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
		this.portals = new ArrayList<>(Arrays.asList(portals));
		this.script = script;

		this.projectionPlaneOffset = projectionPlaneOffset;
		this.scaleDistFromProjPlane = scaleDistFromProjPlane;
		this.playerFieldOfView = playerFieldOfView;
		this.shadingMultiplier = shadingMultiplier;
		this.shadingDistance = shadingDistance;
		this.doLighting = doLighting;
		this.doShading = doShading;
		this.transparentColor = transparentColor;
		this.shadeTargetColor = shadeTargetColor;

		initTiles();

		this.order = order;
		this.playerOrigin = playerOrigin;
	}

	/*-------------------------------------------------------------------------*/
	private void initTiles()
	{
		this.width = tiles.length;
		this.length = tiles[0].length;

		for (int x = 0; x < tiles.length; x++)
		{
			for (int y = 0; y < tiles[x].length; y++)
			{
				tiles[x][y].setZone(name);
				tiles[x][y].setCoords(new Point(x, y));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> encounterTile(Maze maze, Point tile, Point previousTile, int facing)
	{
		List<MazeEvent> result = new ArrayList<>();

		// NPCs get preference to TileScripts
		Npc[] npcs = NpcManager.getInstance().getNpcsOnTile(name, tile);
		
		if (npcs != null)
		{
			for (Npc npc : npcs)
			{
				// todo: won't work for more than one NPC?

				List<FoeGroup> actors = new ArrayList<FoeGroup>();
				FoeGroup fg = new FoeGroup();
				fg.add(npc);
				actors.add(fg);

				ActorEncounter actorEncounter = new ActorEncounter(
					actors, null, null, Combat.AmbushStatus.NONE, npc.getScript().preAppearance(), null);
				result.addAll(Maze.getInstance().encounterActors(actorEncounter));
			}
		}

		// process any tile scripts present
		List<TileScript> scripts = this.tiles[tile.x][tile.y].getScripts();

		// first, alert if there is any hidden secrets
		PlayerCharacter secretSpottedPc = null;
		for (TileScript script : scripts)
		{
			if (script.isHiddenSecret())
			{
				PlayerCharacter pc = GameSys.getInstance().scoutingSpotsStash(maze, script.getScoutSecretDifficulty());
				if (pc != null)
				{
					secretSpottedPc = pc;
				}
			}
		}
		for (Wall w : maze.getCurrentZone().getMap().getWalls(tile))
		{
			MouseClickScriptAdapter mouseClickScript = (MouseClickScriptAdapter)w.getMouseClickScript();
			MouseClickScriptAdapter maskTextureMouseClickScript = (MouseClickScriptAdapter)w.getMaskTextureMouseClickScript();
			MouseClickScriptAdapter internalScript = (MouseClickScriptAdapter)w.getInternalScript();

			if (mouseClickScript != null && mouseClickScript.getScript().isHiddenSecret())
			{
				PlayerCharacter pc = GameSys.getInstance().scoutingSpotsStash(maze,
					mouseClickScript.getScript().getScoutSecretDifficulty());
				if (pc != null)
				{
					secretSpottedPc = pc;
				}
			}
			if (maskTextureMouseClickScript != null && maskTextureMouseClickScript.getScript().isHiddenSecret())
			{
				PlayerCharacter pc = GameSys.getInstance().scoutingSpotsStash(maze,
					maskTextureMouseClickScript.getScript().getScoutSecretDifficulty());
				if (pc != null)
				{
					secretSpottedPc = pc;
				}
			}
			if (internalScript != null && internalScript.getScript().isHiddenSecret())
			{
				PlayerCharacter pc = GameSys.getInstance().scoutingSpotsStash(maze,
					internalScript.getScript().getScoutSecretDifficulty());
				if (pc != null)
				{
					secretSpottedPc = pc;
				}
			}
		}
		List<EngineObject> objects = maze.getCurrentZone().getMap().getObjects(getTileIndex(tile));

		for (EngineObject object : objects)
		{
			if (object != null && object.getMouseClickScript() != null &&
				object.getMouseClickScript() instanceof MouseClickScriptAdapter)
			{
				TileScript objMouseClickScript = ((MouseClickScriptAdapter)object.getMouseClickScript()).getScript();
				if (objMouseClickScript.isHiddenSecret())
				{
					PlayerCharacter pc = GameSys.getInstance().scoutingSpotsStash(maze,
						objMouseClickScript.getScoutSecretDifficulty());
					if (pc != null)
					{
						secretSpottedPc = pc;
					}
				}
			}
		}

		// if anyone spotted a secret, add the speech event
		if (secretSpottedPc != null)
		{
			result.addAll(SpeechUtil.getInstance().spotStashSpeech(secretSpottedPc));
		}

		// then execute any tile scripts that should trigger
		for (TileScript script : scripts)
		{
			if (script != null && Maze.getInstance().getCurrentCombat() == null)
			{
				if (script.shouldExecute(maze, tile, previousTile, facing, -1))
				{
					List<MazeEvent> events = script.execute(maze, tile, previousTile, facing);
					if (events != null)
					{
						result.addAll(events);
					}
				}
			}
			if (!maze.checkPartyStatus())
			{
				break;
			}
		}

		return result;
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
	public List<MazeEvent> processUseItem(
		Maze maze, Point tile, int facing, Item item, UnifiedActor user)
	{
		List<MazeEvent> result = new ArrayList<>();
		List<TileScript> scripts = this.tiles[tile.x][tile.y].getScripts();

		for (TileScript script : scripts)
		{
			if (script != null)
			{
				List<MazeEvent> events = script.handleUseItem(maze, tile, facing, item, user);
				if (events == TileScript.PREVENT_ACTION)
				{
					return result;
				}
				else if (events != null)
				{
					result.addAll(events);
				}
			}
		}

		return result;
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

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	public Tile getTile(Point p)
	{
		if (tiles == null)
		{
			return null;
		}
		
		return tiles[p.x][p.y];
	}

	public int getTileIndex(Point p)
	{
		return p.y*width+p.x;
	}

	public Portal[] getPortals()
	{
		return portals.toArray(new Portal[0]);
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

	public void setTiles(Tile[][] tiles)
	{
		this.tiles = tiles;
		initTiles();
	}

	public void setPortalsList(List<Portal> portals)
	{
		this.portals = portals;
	}

	public void setPortals(Portal[] portals)
	{
		this.portals = new ArrayList<>(Arrays.asList(portals));
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public boolean isDoShading()
	{
		return doShading;
	}

	public boolean isDoLighting()
	{
		return doLighting;
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
			if (portal.getFrom().equals(oldTile) && portal.getFromFacing() == facing ||
				portal.isTwoWay() && portal.getTo().equals(oldTile) && portal.getToFacing() == facing)
			{
				return portal;
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> initZoneScript(long turnNr)
	{
		return this.script.init(this, turnNr);
	}

	/*-------------------------------------------------------------------------*/
	public void initialise(long turnNr)
	{
		// init tile scripts
		for (int x = 0; x < tiles.length; x++)
		{
			for (int y = 0; y < tiles[x].length; y++)
			{
				if (tiles[x][y].getScripts() != null)
				{
					List<TileScript> scripts = tiles[x][y].getScripts();

					for (TileScript script : scripts)
					{
						script.initialise(Maze.getInstance(), new Point(x, y), y*width+x);
					}
				}
			}
		}

		// init any mouse click scripts
		List<Wall> walls = new ArrayList<>();
		walls.addAll(Arrays.asList(map.getHorizontalWalls()));
		walls.addAll(Arrays.asList(map.getVerticalWalls()));

		for (Wall w : walls)
		{
			if (w.getMouseClickScript() instanceof MouseClickScriptAdapter)
			{
				((MouseClickScriptAdapter)w.getMouseClickScript()).getScript().initialise(
					Maze.getInstance(), null, -1);
			}

			if (w.getMaskTextureMouseClickScript() instanceof MouseClickScriptAdapter)
			{
				((MouseClickScriptAdapter)w.getMaskTextureMouseClickScript()).getScript().initialise(
					Maze.getInstance(), null, -1);
			}
		}

		// init portals
		for (Portal p : portals)
		{
			p.initialise(turnNr, Maze.getInstance());
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(long turnNr)
	{
//		Maze.log("processing zone end of turn...");
		List<MazeEvent> mazeEvents = this.script.endOfTurn(this, turnNr);
		return mazeEvents==null ? new ArrayList<>() : mazeEvents;
//		Maze.log("finished zone end of turn");
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
			Wall wall = map.getHorizontalWalls()[map.getNorthWall(map.getIndex(p1))];
			return wall.isVisible() || wall.isSolid();
		}
		else if (p2.x == p1.x && p2.y == p1.y+1)
		{
			// t2 is adjacent to the south
			Wall wall = map.getHorizontalWalls()[map.getSouthWall(map.getIndex(p1))];
			return wall.isVisible() || wall.isSolid();
		}
		else if (p2.y == p1.y && p2.x == p1.x-1)
		{
			// t2 is adjacent to the west
			Wall wall = map.getVerticalWalls()[map.getWestWall(map.getIndex(p1))];
			return wall.isVisible() || wall.isSolid();
		}
		else if (p2.y == p1.y && p2.x == p1.x+1)
		{
			// t2 is adjacent to the east
			Wall wall = map.getVerticalWalls()[map.getEastWall(map.getIndex(p1))];
			return wall.isVisible() || wall.isSolid();
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
	public void setMap(Map map)
	{
		this.map = map;
	}

	/*-------------------------------------------------------------------------*/
	public List<TileScript> getAllTileScripts()
	{
		List<TileScript> result = new ArrayList<>();

		Tile[][] tiles = getTiles();
		for (int i = 0, tilesLength = tiles.length; i < tilesLength; i++)
		{
			Tile[] x = tiles[i];
			for (int j = 0, xLength = x.length; j < xLength; j++)
			{
				Tile y = x[j];
				if (y.getScripts() != null && !y.getScripts().isEmpty())
				{
					result.addAll(y.getScripts());

					Point tilePoint = new Point(i, j);
					for (Wall w : getMap().getWalls(tilePoint))
					{
						MouseClickScriptAdapter mouseClickScript = (MouseClickScriptAdapter)w.getMouseClickScript();
						MouseClickScriptAdapter maskTextureMouseClickScript = (MouseClickScriptAdapter)w.getMaskTextureMouseClickScript();
						MouseClickScriptAdapter internalScript = (MouseClickScriptAdapter)w.getInternalScript();

						if (mouseClickScript != null)
						{
							result.add(mouseClickScript.getScript());
						}
						if (maskTextureMouseClickScript != null)
						{
							result.add(maskTextureMouseClickScript.getScript());
						}
						if (internalScript != null)
						{
							result.add(internalScript.getScript());
						}
					}
					List<EngineObject> objects = getMap().getObjects(getTileIndex(tilePoint));

					for (EngineObject object : objects)
					{
						if (object != null && object.getMouseClickScript() != null &&
							object.getMouseClickScript() instanceof MouseClickScriptAdapter)
						{
							result.add(((MouseClickScriptAdapter)object.getMouseClickScript()).getScript());
						}
					}
				}
			}
		}


		return result;
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
