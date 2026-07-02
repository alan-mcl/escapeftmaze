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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.MapScript;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;

/**
 * A deep snapshot of a scoped region of zone map data for undo/redo.
 */
public class MapZoneSnapshot
{
	private final Map<Point, TilePair> tiles = new HashMap<>();
	private final Map<WallKey, Wall> walls = new HashMap<>();
	private final List<ObjectEntry> objects = new ArrayList<>();
	private final List<Portal> portals;
	private final MapScript[] mapScripts;

	/*-------------------------------------------------------------------------*/
	private MapZoneSnapshot(List<Portal> portals, MapScript[] mapScripts)
	{
		this.portals = portals;
		this.mapScripts = mapScripts;
	}

	/*-------------------------------------------------------------------------*/
	public static MapZoneSnapshot capture(Zone zone, MapEditScope scope, MapEditor editor)
	{
		List<Portal> portalSnapshot = null;
		MapScript[] scriptSnapshot = null;

		if (scope.includesPortals())
		{
			portalSnapshot = new ArrayList<>();
			for (Portal portal : zone.getPortals())
			{
				portalSnapshot.add(MapElementCloner.clonePortal(portal));
			}
		}

		if (scope.includesMapScripts())
		{
			MapScript[] scripts = zone.getMap().getScripts();
			scriptSnapshot = scripts == null ? new MapScript[0] : Arrays.copyOf(scripts, scripts.length);
		}

		MapZoneSnapshot snapshot = new MapZoneSnapshot(portalSnapshot, scriptSnapshot);
		mclachlan.crusader.Map map = zone.getMap();
		int width = zone.getWidth();

		for (Point coord : scope.getTileCoords())
		{
			int index = coord.y * width + coord.x;
			if (scope.capturesTiles())
			{
				Tile crusaderTile = map.getTiles()[index];
				mclachlan.maze.map.Tile mazeTile = zone.getTiles()[coord.x][coord.y];
				snapshot.tiles.put(new Point(coord), new TilePair(
					MapElementCloner.cloneCrusaderTile(crusaderTile),
					MapElementCloner.cloneMazeTile(mazeTile)));
			}

			if (scope.capturesWalls())
			{
				for (MapSelectionClipboard.WallSide side : MapSelectionClipboard.WallSide.values())
				{
					Wall wall = getWallAt(map, coord.x, coord.y, side);
					snapshot.walls.put(new WallKey(coord.x, coord.y, side), MapElementCloner.cloneWall(wall));
				}
			}

			if (scope.capturesObjects())
			{
				for (EngineObject object : map.getObjects(index))
				{
					snapshot.objects.add(new ObjectEntry(
						coord.x,
						coord.y,
						object.getXPos(),
						object.getYPos(),
						MapElementCloner.cloneObject(object)));
				}
			}
		}

		return snapshot;
	}

	/*-------------------------------------------------------------------------*/
	public void apply(Zone zone)
	{
		mclachlan.crusader.Map map = zone.getMap();
		int width = zone.getWidth();

		for (Map.Entry<Point, TilePair> entry : tiles.entrySet())
		{
			Point coord = entry.getKey();
			int index = coord.y * width + coord.x;
			MapElementApplier.applyCrusaderTile(map.getTiles()[index], entry.getValue().crusaderTile);
			MapElementApplier.applyMazeTile(zone.getTiles()[coord.x][coord.y], entry.getValue().mazeTile);
		}

		for (Map.Entry<WallKey, Wall> entry : walls.entrySet())
		{
			WallKey key = entry.getKey();
			Wall destWall = getWallAt(map, key.x, key.y, key.side);
			MapElementApplier.applyWall(destWall, entry.getValue());
		}

		if (!objects.isEmpty())
		{
			Set<Point> objectCoords = new HashSet<>();
			for (Point coord : tiles.keySet())
			{
				objectCoords.add(coord);
			}
			for (ObjectEntry entry : objects)
			{
				objectCoords.add(new Point(entry.x, entry.y));
			}

			for (Point coord : objectCoords)
			{
				int index = coord.y * width + coord.x;
				map.removeObject(index);
			}

			for (ObjectEntry entry : objects)
			{
				EngineObject eo = MapElementCloner.cloneObject(entry.object);
				eo.setXPos(entry.xPos);
				eo.setYPos(entry.yPos);
				map.addObject(eo);
				map.initObjectFromXY(eo);
			}
		}

		if (portals != null)
		{
			List<Portal> restored = new ArrayList<>();
			for (Portal portal : portals)
			{
				restored.add(MapElementCloner.clonePortal(portal));
			}
			zone.setPortalsList(restored);
		}

		if (mapScripts != null)
		{
			map.setScripts(mapScripts.length == 0 ? new MapScript[0] : Arrays.copyOf(mapScripts, mapScripts.length));
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean sameContentAs(MapZoneSnapshot other)
	{
		if (other == null)
		{
			return false;
		}

		if (!Objects.equals(tiles.keySet(), other.tiles.keySet()))
		{
			return false;
		}

		for (Point key : tiles.keySet())
		{
			TilePair a = tiles.get(key);
			TilePair b = other.tiles.get(key);
			if (!tilePairEquals(a, b))
			{
				return false;
			}
		}

		if (!Objects.equals(walls.keySet(), other.walls.keySet()))
		{
			return false;
		}

		for (WallKey key : walls.keySet())
		{
			if (!wallEquals(walls.get(key), other.walls.get(key)))
			{
				return false;
			}
		}

		if (objects.size() != other.objects.size())
		{
			return false;
		}

		for (int i = 0; i < objects.size(); i++)
		{
			if (!objectEntryEquals(objects.get(i), other.objects.get(i)))
			{
				return false;
			}
		}

		if (!portalListEquals(portals, other.portals))
		{
			return false;
		}

		return Arrays.equals(mapScripts, other.mapScripts);
	}

	/*-------------------------------------------------------------------------*/
	private static boolean tilePairEquals(TilePair a, TilePair b)
	{
		return crusaderTileEquals(a.crusaderTile, b.crusaderTile)
			&& mazeTileEquals(a.mazeTile, b.mazeTile);
	}

	/*-------------------------------------------------------------------------*/
	private static boolean crusaderTileEquals(Tile a, Tile b)
	{
		Tile ca = MapElementCloner.cloneCrusaderTile(a);
		Tile cb = MapElementCloner.cloneCrusaderTile(b);
		Tile dest = new Tile();
		MapElementApplier.applyCrusaderTile(dest, ca);
		Tile dest2 = new Tile();
		MapElementApplier.applyCrusaderTile(dest2, cb);
		return dest.getLightLevel() == dest2.getLightLevel()
			&& dest.getCeilingHeight() == dest2.getCeilingHeight()
			&& Objects.equals(dest.getFloorTexture(), dest2.getFloorTexture())
			&& Objects.equals(dest.getFloorMaskTexture(), dest2.getFloorMaskTexture())
			&& Objects.equals(dest.getCeilingTexture(), dest2.getCeilingTexture())
			&& Objects.equals(dest.getCeilingMaskTexture(), dest2.getCeilingMaskTexture());
	}

	/*-------------------------------------------------------------------------*/
	private static boolean mazeTileEquals(mclachlan.maze.map.Tile a, mclachlan.maze.map.Tile b)
	{
		return Objects.equals(a.getTerrainType(), b.getTerrainType())
			&& Objects.equals(a.getTerrainSubType(), b.getTerrainSubType())
			&& Objects.equals(a.getSector(), b.getSector())
			&& a.getRandomEncounterChance() == b.getRandomEncounterChance()
			&& Objects.equals(a.getRestingDanger(), b.getRestingDanger())
			&& Objects.equals(a.getRestingEfficiency(), b.getRestingEfficiency())
			&& Objects.equals(
				a.getRandomEncounters() == null ? null : a.getRandomEncounters().getName(),
				b.getRandomEncounters() == null ? null : b.getRandomEncounters().getName());
	}

	/*-------------------------------------------------------------------------*/
	private static boolean wallEquals(Wall a, Wall b)
	{
		return a.isVisible() == b.isVisible()
			&& a.isSolid() == b.isSolid()
			&& a.getHeight() == b.getHeight()
			&& wallTextureNamesEqual(a.getTextures(), b.getTextures())
			&& wallTextureNamesEqual(a.getMaskTextures(), b.getMaskTextures());
	}

	/*-------------------------------------------------------------------------*/
	private static boolean wallTextureNamesEqual(Texture[] a, Texture[] b)
	{
		if (a == null && b == null)
		{
			return true;
		}
		if (a == null || b == null || a.length != b.length)
		{
			return false;
		}

		for (int i = 0; i < a.length; i++)
		{
			String nameA = a[i] == null ? null : a[i].getName();
			String nameB = b[i] == null ? null : b[i].getName();
			if (!Objects.equals(nameA, nameB))
			{
				return false;
			}
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean objectEntryEquals(ObjectEntry a, ObjectEntry b)
	{
		return a.x == b.x
			&& a.y == b.y
			&& a.xPos == b.xPos
			&& a.yPos == b.yPos
			&& Objects.equals(a.object.getName(), b.object.getName());
	}

	/*-------------------------------------------------------------------------*/
	private static boolean portalListEquals(List<Portal> a, List<Portal> b)
	{
		if (a == null && b == null)
		{
			return true;
		}
		if (a == null || b == null)
		{
			return false;
		}
		if (a.size() != b.size())
		{
			return false;
		}
		for (int i = 0; i < a.size(); i++)
		{
			if (!a.get(i).equals(b.get(i)))
			{
				return false;
			}
		}
		return true;
	}

	/*-------------------------------------------------------------------------*/
	private static Wall getWallAt(
		mclachlan.crusader.Map map,
		int x,
		int y,
		MapSelectionClipboard.WallSide side)
	{
		int tileIndex = y * map.getWidth() + x;
		int wallIndex;

		switch (side)
		{
			case NORTH:
				wallIndex = map.getNorthWall(tileIndex);
				return map.getHorizontalWalls()[wallIndex];
			case SOUTH:
				wallIndex = map.getSouthWall(tileIndex);
				return map.getHorizontalWalls()[wallIndex];
			case EAST:
				wallIndex = map.getEastWall(tileIndex);
				return map.getVerticalWalls()[wallIndex];
			case WEST:
				wallIndex = map.getWestWall(tileIndex);
				return map.getVerticalWalls()[wallIndex];
			default:
				throw new IllegalStateException("Unknown wall side "+side);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static final class TilePair
	{
		private final Tile crusaderTile;
		private final mclachlan.maze.map.Tile mazeTile;

		private TilePair(Tile crusaderTile, mclachlan.maze.map.Tile mazeTile)
		{
			this.crusaderTile = crusaderTile;
			this.mazeTile = mazeTile;
		}
	}

	/*-------------------------------------------------------------------------*/
	private static final class WallKey
	{
		private final int x;
		private final int y;
		private final MapSelectionClipboard.WallSide side;

		private WallKey(int x, int y, MapSelectionClipboard.WallSide side)
		{
			this.x = x;
			this.y = y;
			this.side = side;
		}

		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof WallKey))
			{
				return false;
			}
			WallKey other = (WallKey)o;
			return x == other.x && y == other.y && side == other.side;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(x, y, side);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static final class ObjectEntry
	{
		private final int x;
		private final int y;
		private final int xPos;
		private final int yPos;
		private final EngineObject object;

		private ObjectEntry(int x, int y, int xPos, int yPos, EngineObject object)
		{
			this.x = x;
			this.y = y;
			this.xPos = xPos;
			this.yPos = yPos;
			this.object = object;
		}
	}
}
