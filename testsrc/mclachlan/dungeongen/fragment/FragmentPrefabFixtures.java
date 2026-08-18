/*
 * Copyright (c) 2026 Alan McLachlan
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

package mclachlan.dungeongen.fragment;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.DefaultZoneScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.StatModifier;

/**
 * In-memory barracks fragment zones for {@link FragmentDungeonGenTest}.
 */
public final class FragmentPrefabFixtures
{
	public record TestKit(
		List<FragmentCatalog.Entry> catalog,
		java.util.Map<String, Zone> zonesByName)
	{
	}

	private FragmentPrefabFixtures()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static TestKit barracksTestKit(Database db)
	{
		Texture wall = db.getMazeTexture("DUNGEON_WALL_1").getTexture();
		Texture floor = db.getMazeTexture("DUNGEON_FLOOR_1").getTexture();
		Texture ceiling = db.getMazeTexture("DUNGEON_CEILING_1").getTexture();
		Texture bed = db.getMazeTexture("objects.bed.1").getTexture();
		Texture bedSide = db.getMazeTexture("objects.bed.1.side").getTexture();

		java.util.Map<String, Zone> zones = new LinkedHashMap<>();

		Zone entry = buildRoom(
			"test.barracks.room.entry",
			3,
			3,
			wall,
			floor,
			ceiling,
			Set.of(new Opening(1, 2, CrusaderEngine.Facing.SOUTH)),
			List.of(bedPair(1, 1, bed, bedSide, 3)));
		zones.put(entry.getName(), entry);

		Zone dorm = buildRoom(
			"test.barracks.room.dorm",
			3,
			3,
			wall,
			floor,
			ceiling,
			Set.of(new Opening(1, 2, CrusaderEngine.Facing.SOUTH)),
			List.of(bedPair(1, 1, bed, bedSide, 3)));
		zones.put(dorm.getName(), dorm);

		Zone corrTee = buildCorridor(
			"test.barracks.corr.tee",
			3,
			3,
			wall,
			floor,
			ceiling,
			Set.of(
				new Opening(1, 0, CrusaderEngine.Facing.NORTH),
				new Opening(0, 1, CrusaderEngine.Facing.WEST),
				new Opening(2, 1, CrusaderEngine.Facing.EAST)),
			Set.of(new Point(1, 1), new Point(0, 1), new Point(2, 1), new Point(1, 0)));
		zones.put(corrTee.getName(), corrTee);

		List<FragmentCatalog.Entry> catalog = List.of(
			entry("test.barracks.room.entry", FragmentCatalog.Kind.ROOM, true, 10, 1),
			entry("test.barracks.room.dorm", FragmentCatalog.Kind.ROOM, false, 8, 3),
			entry("test.barracks.corr.tee", FragmentCatalog.Kind.CORRIDOR, false, 10, 2));

		return new TestKit(catalog, zones);
	}

	/*-------------------------------------------------------------------------*/
	public static Zone bendFixture(Database db)
	{
		Texture wall = db.getMazeTexture("DUNGEON_WALL_1").getTexture();
		Texture floor = db.getMazeTexture("DUNGEON_FLOOR_1").getTexture();
		Texture ceiling = db.getMazeTexture("DUNGEON_CEILING_1").getTexture();
		return buildCorridor(
			"test.bend",
			2,
			2,
			wall,
			floor,
			ceiling,
			Set.of(
				new Opening(0, 0, CrusaderEngine.Facing.NORTH),
				new Opening(1, 1, CrusaderEngine.Facing.EAST)),
			Set.of(new Point(0, 0), new Point(0, 1), new Point(1, 1)));
	}

	/*-------------------------------------------------------------------------*/
	public static Zone straightFixture(Database db)
	{
		Texture wall = db.getMazeTexture("DUNGEON_WALL_1").getTexture();
		Texture floor = db.getMazeTexture("DUNGEON_FLOOR_1").getTexture();
		Texture ceiling = db.getMazeTexture("DUNGEON_CEILING_1").getTexture();
		return buildCorridor(
			"test.straight",
			1,
			3,
			wall,
			floor,
			ceiling,
			Set.of(
				new Opening(0, 0, CrusaderEngine.Facing.NORTH),
				new Opening(0, 2, CrusaderEngine.Facing.SOUTH)),
			Set.of(new Point(0, 0), new Point(0, 1), new Point(0, 2)));
	}

	/*-------------------------------------------------------------------------*/
	private static FragmentCatalog.Entry entry(
		String name,
		FragmentCatalog.Kind kind,
		boolean start,
		int weight,
		int maxPerFloor)
	{
		return new FragmentCatalog.Entry(
			name,
			"flavour",
			"barracks",
			kind,
			start,
			1,
			99,
			weight,
			maxPerFloor);
	}

	/*-------------------------------------------------------------------------*/
	private static EngineObject bedPair(
		int x,
		int y,
		Texture bed,
		Texture bedSide,
		int width)
	{
		EngineObject obj = new EngineObject(
			"bed",
			x * 64,
			y * 64,
			bed,
			bed,
			bedSide,
			bedSide,
			y * width + x,
			false,
			null,
			EngineObject.Alignment.BOTTOM);
		obj.setGridX(x);
		obj.setGridY(y);
		return obj;
	}

	/*-------------------------------------------------------------------------*/
	private record Opening(int x, int y, int facing)
	{
	}

	/*-------------------------------------------------------------------------*/
	private static Zone buildRoom(
		String name,
		int width,
		int length,
		Texture wallTex,
		Texture floorTex,
		Texture ceilingTex,
		Set<Opening> openings,
		List<EngineObject> objects)
	{
		Set<Point> floor = new HashSet<>();
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < length; y++)
			{
				floor.add(new Point(x, y));
			}
		}
		return buildFragment(
			name, width, length, wallTex, floorTex, ceilingTex, floor, openings, objects);
	}

	/*-------------------------------------------------------------------------*/
	private static Zone buildCorridor(
		String name,
		int width,
		int length,
		Texture wallTex,
		Texture floorTex,
		Texture ceilingTex,
		Set<Opening> openings,
		Set<Point> floor)
	{
		return buildFragment(
			name, width, length, wallTex, floorTex, ceilingTex, floor, openings, List.of());
	}

	/*-------------------------------------------------------------------------*/
	private static Zone buildFragment(
		String name,
		int width,
		int length,
		Texture wallTex,
		Texture floorTex,
		Texture ceilingTex,
		Set<Point> floorTiles,
		Set<Opening> openings,
		List<EngineObject> objects)
	{
		int baseImageSize = 64;
		Wall solid = new Wall(
			new Texture[]{wallTex},
			null,
			true,
			true,
			1,
			null,
			null,
			null);
		Wall open = new Wall(
			new Texture[]{mclachlan.crusader.Map.NO_WALL},
			null,
			false,
			false,
			1,
			null,
			null,
			null);

		Set<String> openingKeys = new HashSet<>();
		for (Opening o : openings)
		{
			openingKeys.add(o.x + ":" + o.y + ":" + o.facing);
		}

		Wall[] horiz = new Wall[width * (length + 1)];
		Wall[] vert = new Wall[(width + 1) * length];
		Arrays.fill(horiz, open);
		Arrays.fill(vert, open);

		mclachlan.crusader.Tile[] ctiles = new mclachlan.crusader.Tile[width * length];
		mclachlan.maze.map.Tile[][] logic = new mclachlan.maze.map.Tile[width][length];

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < length; y++)
			{
				Point p = new Point(x, y);
				boolean isFloor = floorTiles.contains(p);

				mclachlan.crusader.Tile ct = new mclachlan.crusader.Tile(
					ceilingTex, floorTex, CrusaderEngine.NORMAL_LIGHT_LEVEL);
				ctiles[y * width + x] = ct;

				mclachlan.maze.map.Tile lt = new mclachlan.maze.map.Tile();
				lt.setCoords(p);
				lt.setZone(name);
				lt.setTerrainType(mclachlan.maze.map.Tile.TerrainType.DUNGEON);
				lt.setTerrainSubType("Temple");
				lt.setStatModifier(new StatModifier());
				lt.setScripts(new ArrayList<>());
				logic[x][y] = lt;

				if (!isFloor)
				{
					continue;
				}

				if (y == 0)
				{
					horiz[x + y * width] = openingKeys.contains(x + ":" + y + ":" + CrusaderEngine.Facing.NORTH)
						? open : solid;
				}
				else if (!floorTiles.contains(new Point(x, y - 1)))
				{
					horiz[x + y * width] = solid;
				}

				if (y == length - 1)
				{
					horiz[x + (y + 1) * width] = openingKeys.contains(x + ":" + y + ":" + CrusaderEngine.Facing.SOUTH)
						? open : solid;
				}
				else if (!floorTiles.contains(new Point(x, y + 1)))
				{
					horiz[x + (y + 1) * width] = solid;
				}

				if (x == 0)
				{
					vert[x + y * (width + 1)] = openingKeys.contains(x + ":" + y + ":" + CrusaderEngine.Facing.WEST)
						? open : solid;
				}
				else if (!floorTiles.contains(new Point(x - 1, y)))
				{
					vert[x + y * (width + 1)] = solid;
				}

				if (x == width - 1)
				{
					vert[x + y * (width + 1) + 1] =
						openingKeys.contains(x + ":" + y + ":" + CrusaderEngine.Facing.EAST)
							? open : solid;
				}
				else if (!floorTiles.contains(new Point(x + 1, y)))
				{
					vert[x + y * (width + 1) + 1] = solid;
				}
			}
		}

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < length; y++)
			{
				if (floorTiles.contains(new Point(x, y)))
				{
					continue;
				}
				if (y == 0)
				{
					horiz[x] = solid;
				}
				if (y == length - 1)
				{
					horiz[x + (y + 1) * width] = solid;
				}
				if (x == 0)
				{
					vert[x + y * (width + 1)] = solid;
				}
				if (x == width - 1)
				{
					vert[x + y * (width + 1) + 1] = solid;
				}
			}
		}

		List<EngineObject> expanded = new ArrayList<>(objects);
		for (EngineObject obj : expanded)
		{
			obj.setTileIndex(obj.getGridY() * width + obj.getGridX());
		}

		mclachlan.crusader.Map map = new mclachlan.crusader.Map(
			length,
			width,
			baseImageSize,
			ctiles,
			new Texture[]{wallTex, floorTex, ceilingTex, mclachlan.crusader.Map.NO_WALL},
			horiz,
			vert,
			new mclachlan.crusader.Map.SkyConfig[0],
			expanded,
			new mclachlan.crusader.MapScript[0]);
		map.init();

		Zone zone = new Zone();
		zone.setName(name);
		zone.setWidth(width);
		zone.setLength(length);
		zone.setTiles(logic);
		zone.setMap(map);
		zone.setPortals(new mclachlan.maze.map.Portal[0]);
		zone.setScript(new DefaultZoneScript());
		zone.setPlayerOrigin(new Point(width / 2, length / 2));
		return zone;
	}
}
