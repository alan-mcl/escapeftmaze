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

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.MapScript;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.DefaultZoneScript;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.test.support.MazeTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests for {@link MapZoneSnapshot}.
 */
public class MapZoneSnapshotTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	public void tileAndObjectSnapshotRestoresMutatedState()
	{
		Zone zone = buildTestZone(2, 2);
		mclachlan.crusader.Map map = zone.getMap();

		EngineObject object = new EngineObject(
			"test.object",
			16,
			16,
			null,
			null,
			null,
			null,
			0,
			false,
			null,
			EngineObject.Alignment.BOTTOM);
		map.addObject(object);
		map.initObjectFromXY(object);

		Set<Point> coords = new HashSet<>();
		coords.add(new Point(0, 0));
		MapEditScope scope = MapEditScope.forTileCoords(coords);

		MapZoneSnapshot before = MapZoneSnapshot.capture(zone, scope, null);
		int originalLight = map.getTiles()[0].getLightLevel();

		map.getTiles()[0].setLightLevel(originalLight + 5);
		map.removeObject(object);

		before.apply(zone);

		assertEquals(originalLight, map.getTiles()[0].getLightLevel());
		assertEquals(1, map.getObjects(0).size());
		assertEquals("test.object", map.getObjects(0).get(0).getName());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	public void portalSnapshotRestoresPortalList()
	{
		Zone zone = buildTestZone(2, 2);
		assertEquals(0, zone.getPortals().length);

		zone.addPortal(new Portal(
			"door.one",
			Portal.State.UNLOCKED,
			new Point(0, 0),
			1,
			new Point(1, 0),
			3,
			true,
			true,
			true,
			true,
			5,
			0,
			new int[8],
			new java.util.BitSet(),
			null,
			false,
			null,
			null));

		MapZoneSnapshot before = MapZoneSnapshot.capture(zone, MapEditScope.forPortals(), null);
		assertEquals(1, zone.getPortals().length);

		zone.addPortal(new Portal(
			"door.two",
			Portal.State.LOCKED,
			new Point(0, 1),
			1,
			new Point(1, 1),
			3,
			false,
			false,
			false,
			false,
			0,
			0,
			new int[8],
			new java.util.BitSet(),
			null,
			false,
			null,
			null));
		assertEquals(2, zone.getPortals().length);

		before.apply(zone);

		assertEquals(1, zone.getPortals().length);
		assertEquals("door.one", zone.getPortals()[0].getMazeVariable());
	}

	/*-------------------------------------------------------------------------*/
	private static Zone buildTestZone(int length, int width)
	{
		Tile[] tiles = new Tile[length * width];
		for (int i = 0; i < tiles.length; i++)
		{
			tiles[i] = new Tile(null, null, 16);
		}

		TextureArray textureArray = new TextureArray(new mclachlan.crusader.Texture[]{Map.NO_WALL});
		Wall[] horiz = new Wall[length * width + width];
		Wall[] vert = new Wall[length * width + length];
		for (int i = 0; i < horiz.length; i++)
		{
			horiz[i] = new Wall(new mclachlan.crusader.Texture[]{Map.NO_WALL}, null, false, false, 1, null, null, null);
		}
		for (int i = 0; i < vert.length; i++)
		{
			vert[i] = new Wall(new mclachlan.crusader.Texture[]{Map.NO_WALL}, null, false, false, 1, null, null, null);
		}

		mclachlan.maze.map.Tile[][] mazeTiles = new mclachlan.maze.map.Tile[length][width];
		for (int x = 0; x < length; x++)
		{
			for (int y = 0; y < width; y++)
			{
				mazeTiles[x][y] = new mclachlan.maze.map.Tile(
					new ArrayList<>(),
					null,
					new StatModifier(),
					mclachlan.maze.map.Tile.TerrainType.DUNGEON,
					null,
					0,
					mclachlan.maze.map.Tile.RestingDanger.MEDIUM,
					mclachlan.maze.map.Tile.RestingEfficiency.AVERAGE);
			}
		}

		Map map = new Map(
			length,
			width,
			32,
			tiles,
			textureArray.textures,
			horiz,
			vert,
			new Map.SkyConfig[0],
			new ArrayList<>(),
			new MapScript[0]);

		return new Zone(
			"test",
			map,
			mazeTiles,
			new Portal[0],
			new DefaultZoneScript(-1, 0, null),
			Color.BLACK,
			Color.BLACK,
			true,
			true,
			2.5,
			2.5,
			-40,
			60,
			0.65,
			0,
			new Point(0, 0));
	}

	/*-------------------------------------------------------------------------*/
	private static final class TextureArray
	{
		private final mclachlan.crusader.Texture[] textures;

		private TextureArray(mclachlan.crusader.Texture[] textures)
		{
			this.textures = textures;
		}
	}
}
