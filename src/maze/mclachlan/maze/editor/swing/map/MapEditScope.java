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

package mclachlan.maze.editor.swing.map;

import java.awt.Point;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Zone;

/**
 * Describes which parts of a zone are captured in a {@link MapZoneSnapshot}.
 */
public final class MapEditScope
{
	private final Set<Point> tileCoords;
	private final boolean captureTiles;
	private final boolean captureWalls;
	private final boolean captureObjects;
	private final boolean includePortals;
	private final boolean includeMapScripts;

	/*-------------------------------------------------------------------------*/
	private MapEditScope(
		Set<Point> tileCoords,
		boolean captureTiles,
		boolean captureWalls,
		boolean captureObjects,
		boolean includePortals,
		boolean includeMapScripts)
	{
		this.tileCoords = Collections.unmodifiableSet(new HashSet<>(tileCoords));
		this.captureTiles = captureTiles;
		this.captureWalls = captureWalls;
		this.captureObjects = captureObjects;
		this.includePortals = includePortals;
		this.includeMapScripts = includeMapScripts;
	}

	/*-------------------------------------------------------------------------*/
	public Set<Point> getTileCoords()
	{
		return tileCoords;
	}

	/*-------------------------------------------------------------------------*/
	public boolean capturesTiles()
	{
		return captureTiles;
	}

	/*-------------------------------------------------------------------------*/
	public boolean capturesWalls()
	{
		return captureWalls;
	}

	/*-------------------------------------------------------------------------*/
	public boolean capturesObjects()
	{
		return captureObjects;
	}

	/*-------------------------------------------------------------------------*/
	public boolean includesPortals()
	{
		return includePortals;
	}

	/*-------------------------------------------------------------------------*/
	public boolean includesMapScripts()
	{
		return includeMapScripts;
	}

	/*-------------------------------------------------------------------------*/
	public static MapEditScope forSelection(MapEditor editor)
	{
		Set<Point> coords = new HashSet<>();
		boolean hasTile = false;
		boolean hasWall = false;
		boolean hasObject = false;
		Zone zone = editor.getZone();

		for (Object obj : editor.getSelection())
		{
			if (obj instanceof Tile)
			{
				hasTile = true;
				coords.add(getTileCoords(editor, (Tile)obj));
			}
			else if (obj instanceof Wall)
			{
				hasWall = true;
				coords.add(getWallCoords(zone, (Wall)obj));
			}
			else if (obj instanceof EngineObject)
			{
				hasObject = true;
				coords.add(getObjectCoords(zone, (EngineObject)obj));
			}
		}

		return new MapEditScope(
			coords,
			hasTile,
			hasWall,
			hasObject || hasTile,
			false,
			false);
	}

	/*-------------------------------------------------------------------------*/
	public static MapEditScope forTileCoords(Set<Point> coords)
	{
		return new MapEditScope(coords, true, true, true, false, false);
	}

	/*-------------------------------------------------------------------------*/
	public static MapEditScope forPasteFootprint(
		MapSelectionClipboard clipboard, int destX, int destY, int width, int length)
	{
		Set<Point> coords = new HashSet<>();

		for (MapSelectionClipboard.TileEntry entry : clipboard.getTiles())
		{
			addIfInBounds(coords, destX + entry.relX, destY + entry.relY, width, length);
		}
		for (MapSelectionClipboard.WallEntry entry : clipboard.getWalls())
		{
			addIfInBounds(coords, destX + entry.relX, destY + entry.relY, width, length);
		}
		for (MapSelectionClipboard.ObjectEntry entry : clipboard.getObjects())
		{
			addIfInBounds(coords, destX + entry.relX, destY + entry.relY, width, length);
		}

		return new MapEditScope(coords, true, true, true, false, false);
	}

	/*-------------------------------------------------------------------------*/
	public static MapEditScope forPortals()
	{
		return new MapEditScope(Collections.emptySet(), false, false, false, true, false);
	}

	/*-------------------------------------------------------------------------*/
	public static MapEditScope forMapScripts()
	{
		return new MapEditScope(Collections.emptySet(), false, false, false, false, true);
	}

	/*-------------------------------------------------------------------------*/
	public static MapEditScope combined(MapEditScope... scopes)
	{
		Set<Point> coords = new HashSet<>();
		boolean captureTiles = false;
		boolean captureWalls = false;
		boolean captureObjects = false;
		boolean includePortals = false;
		boolean includeMapScripts = false;

		for (MapEditScope scope : scopes)
		{
			coords.addAll(scope.tileCoords);
			captureTiles |= scope.captureTiles;
			captureWalls |= scope.captureWalls;
			captureObjects |= scope.captureObjects;
			includePortals |= scope.includePortals;
			includeMapScripts |= scope.includeMapScripts;
		}

		return new MapEditScope(
			coords,
			captureTiles,
			captureWalls,
			captureObjects,
			includePortals,
			includeMapScripts);
	}

	/*-------------------------------------------------------------------------*/
	private static void addIfInBounds(Set<Point> coords, int x, int y, int width, int length)
	{
		if (x >= 0 && y >= 0 && x < width && y < length)
		{
			coords.add(new Point(x, y));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static Point getTileCoords(MapEditor editor, Tile tile)
	{
		int index = editor.getCrusaderIndexOfTile(tile);
		int width = editor.getMap().getWidth();
		return new Point(index % width, index / width);
	}

	/*-------------------------------------------------------------------------*/
	private static Point getObjectCoords(Zone zone, EngineObject object)
	{
		int index = object.getTileIndex();
		int width = zone.getWidth();
		return new Point(index % width, index / width);
	}

	/*-------------------------------------------------------------------------*/
	private static Point getWallCoords(Zone zone, Wall wall)
	{
		MapSelectionCopier.WallLocation location = MapSelectionCopier.resolveWallLocation(zone, wall);
		return new Point(location.x, location.y);
	}
}
