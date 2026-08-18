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

package mclachlan.maze.campaign.temple;

import java.awt.Color;
import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.MapScript;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.DefaultZoneScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.test.support.TempleCampaignHarness;

/**
 * One-shot writer for starter barracks fragment zone JSON. Run from repo root:
 * {@code java -cp ... mclachlan.maze.campaign.temple.BarracksFragmentKitWriter}
 */
public final class BarracksFragmentKitWriter
{
	private static final int TILE_SIZE = 512;

	private BarracksFragmentKitWriter()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		Texture wall = db.getMazeTexture("DUNGEON_WALL_1").getTexture();
		Texture floor = db.getMazeTexture("DUNGEON_FLOOR_1").getTexture();
		Texture ceiling = db.getMazeTexture("DUNGEON_CEILING_1").getTexture();
		Texture sky = db.getMazeTexture("DEFAULT_SKY").getTexture();
		Texture bed = db.getMazeTexture("objects.bed.1").getTexture();
		Texture bedSide = db.getMazeTexture("objects.bed.1.side").getTexture();
		Texture table = db.getMazeTexture("objects.table.1").getTexture();
		Texture chair = db.getMazeTexture("objects.chair.1").getTexture();
		Texture crate = db.getMazeTexture("objects.crate.1").getTexture();

		write(db, roomEntry(db, wall, floor, ceiling, sky));
		write(db, roomDorm(db, wall, floor, ceiling, sky, bed, bedSide));
		write(db, roomDormThru(db, wall, floor, ceiling, sky, bed, bedSide));
		write(db, roomMess(db, wall, floor, ceiling, sky, table, chair));
		write(db, roomArmory(db, wall, floor, ceiling, sky, crate));
		write(db, roomOffice(db, wall, floor, ceiling, sky, table, chair));
		write(db, corrStraight(db, wall, floor, ceiling, sky));
		write(db, corrBend(db, wall, floor, ceiling, sky));
		write(db, corrTee(db, wall, floor, ceiling, sky));
		write(db, corrCross(db, wall, floor, ceiling, sky));

		System.out.println("Wrote barracks fragment kit under data/temple/db/zones/");
	}

	/*-------------------------------------------------------------------------*/
	private static void write(Database db, Zone zone) throws Exception
	{
		db.getSaver().saveZone(zone);
		System.out.println("  " + zone.getName());
	}

	/*-------------------------------------------------------------------------*/
	private static Zone roomEntry(
		Database db,
		Texture wall,
		Texture floor,
		Texture ceiling,
		Texture sky)
	{
		return buildZone(
			db,
			wall,
			floor,
			ceiling,
			sky,
			"fragment.barracks.room.entry",
			5,
			5,
			fullRoom(5, 5),
			Set.of(
				opening(2, 4, CrusaderEngine.Facing.SOUTH),
				opening(4, 2, CrusaderEngine.Facing.EAST)),
			List.of(),
			meta(true, "room", true, 1, 12, 1));
	}

	/*-------------------------------------------------------------------------*/
	private static Zone roomDorm(
		Database db,
		Texture wall,
		Texture floor,
		Texture ceiling,
		Texture sky,
		Texture bed,
		Texture bedSide)
	{
		return buildZone(
			db,
			wall,
			floor,
			ceiling,
			sky,
			"fragment.barracks.room.dorm",
			5,
			5,
			fullRoom(5, 5),
			Set.of(opening(2, 4, CrusaderEngine.Facing.SOUTH)),
			List.of(
				object("bed", 1, 2, bed, bedSide, 5),
				object("bed", 3, 2, bed, bedSide, 5)),
			meta(true, "room", false, 1, 10, 3));
	}

	/*-------------------------------------------------------------------------*/
	private static Zone roomDormThru(
		Database db,
		Texture wall,
		Texture floor,
		Texture ceiling,
		Texture sky,
		Texture bed,
		Texture bedSide)
	{
		return buildZone(
			db,
			wall,
			floor,
			ceiling,
			sky,
			"fragment.barracks.room.dorm.thru",
			5,
			5,
			fullRoom(5, 5),
			Set.of(
				opening(2, 0, CrusaderEngine.Facing.NORTH),
				opening(2, 4, CrusaderEngine.Facing.SOUTH)),
			List.of(
				object("bed", 1, 2, bed, bedSide, 5),
				object("bed", 3, 2, bed, bedSide, 5)),
			meta(true, "room", false, 1, 8, 2));
	}

	/*-------------------------------------------------------------------------*/
	private static Zone roomMess(
		Database db,
		Texture wall,
		Texture floor,
		Texture ceiling,
		Texture sky,
		Texture table,
		Texture chair)
	{
		return buildZone(
			db,
			wall,
			floor,
			ceiling,
			sky,
			"fragment.barracks.room.mess",
			7,
			5,
			fullRoom(7, 5),
			Set.of(opening(3, 4, CrusaderEngine.Facing.SOUTH)),
			List.of(
				object("table", 3, 2, table, table, 7),
				object("chair", 2, 2, chair, chair, 7),
				object("chair", 4, 2, chair, chair, 7)),
			meta(true, "room", false, 1, 6, 1));
	}

	/*-------------------------------------------------------------------------*/
	private static Zone roomArmory(
		Database db,
		Texture wall,
		Texture floor,
		Texture ceiling,
		Texture sky,
		Texture crate)
	{
		return buildZone(
			db,
			wall,
			floor,
			ceiling,
			sky,
			"fragment.barracks.room.armory",
			5,
			5,
			fullRoom(5, 5),
			Set.of(opening(2, 4, CrusaderEngine.Facing.SOUTH)),
			List.of(
				object("crate", 1, 1, crate, crate, 5),
				object("crate", 3, 1, crate, crate, 5),
				object("crate", 2, 3, crate, crate, 5)),
			meta(true, "room", false, 1, 6, 1));
	}

	/*-------------------------------------------------------------------------*/
	private static Zone roomOffice(
		Database db,
		Texture wall,
		Texture floor,
		Texture ceiling,
		Texture sky,
		Texture table,
		Texture chair)
	{
		return buildZone(
			db,
			wall,
			floor,
			ceiling,
			sky,
			"fragment.barracks.room.office",
			5,
			5,
			fullRoom(5, 5),
			Set.of(opening(2, 4, CrusaderEngine.Facing.SOUTH)),
			List.of(
				object("table", 2, 2, table, table, 5),
				object("chair", 2, 3, chair, chair, 5)),
			meta(true, "room", false, 1, 5, 1));
	}

	/*-------------------------------------------------------------------------*/
	private static Zone corrStraight(
		Database db,
		Texture wall,
		Texture floor,
		Texture ceiling,
		Texture sky)
	{
		return buildZone(
			db,
			wall,
			floor,
			ceiling,
			sky,
			"fragment.barracks.corr.straight",
			1,
			5,
			lineRoom(1, 5),
			Set.of(
				opening(0, 0, CrusaderEngine.Facing.NORTH),
				opening(0, 4, CrusaderEngine.Facing.SOUTH)),
			List.of(),
			meta(true, "corridor", false, 1, 8, 6));
	}

	/*-------------------------------------------------------------------------*/
	private static Zone corrBend(
		Database db,
		Texture wall,
		Texture floor,
		Texture ceiling,
		Texture sky)
	{
		return buildZone(
			db,
			wall,
			floor,
			ceiling,
			sky,
			"fragment.barracks.corr.bend",
			2,
			2,
			Set.of(new Point(0, 0), new Point(0, 1), new Point(1, 1)),
			Set.of(
				opening(0, 0, CrusaderEngine.Facing.NORTH),
				opening(1, 1, CrusaderEngine.Facing.EAST)),
			List.of(),
			meta(true, "corridor", false, 1, 6, 4));
	}

	/*-------------------------------------------------------------------------*/
	private static Zone corrTee(
		Database db,
		Texture wall,
		Texture floor,
		Texture ceiling,
		Texture sky)
	{
		return buildZone(
			db,
			wall,
			floor,
			ceiling,
			sky,
			"fragment.barracks.corr.tee",
			3,
			3,
			Set.of(
				new Point(1, 0),
				new Point(0, 1),
				new Point(1, 1),
				new Point(2, 1),
				new Point(1, 2)),
			Set.of(
				opening(1, 0, CrusaderEngine.Facing.NORTH),
				opening(0, 1, CrusaderEngine.Facing.WEST),
				opening(2, 1, CrusaderEngine.Facing.EAST)),
			List.of(),
			meta(true, "corridor", false, 1, 5, 3));
	}

	/*-------------------------------------------------------------------------*/
	private static Zone corrCross(
		Database db,
		Texture wall,
		Texture floor,
		Texture ceiling,
		Texture sky)
	{
		return buildZone(
			db,
			wall,
			floor,
			ceiling,
			sky,
			"fragment.barracks.corr.cross",
			3,
			3,
			Set.of(
				new Point(1, 0),
				new Point(0, 1),
				new Point(1, 1),
				new Point(2, 1),
				new Point(1, 2)),
			Set.of(
				opening(1, 0, CrusaderEngine.Facing.NORTH),
				opening(1, 2, CrusaderEngine.Facing.SOUTH),
				opening(0, 1, CrusaderEngine.Facing.WEST),
				opening(2, 1, CrusaderEngine.Facing.EAST)),
			List.of(),
			meta(true, "corridor", false, 1, 4, 1, false));
	}

	/*-------------------------------------------------------------------------*/
	private static Map<String, String> meta(
		boolean fragment,
		String kind,
		boolean start,
		int depthMin,
		int weight,
		int maxPerFloor)
	{
		return meta(fragment, kind, start, depthMin, weight, maxPerFloor, true);
	}

	/*-------------------------------------------------------------------------*/
	private static Map<String, String> meta(
		boolean fragment,
		String kind,
		boolean start,
		int depthMin,
		int weight,
		int maxPerFloor,
		boolean rotate)
	{
		Map<String, String> metadata = new LinkedHashMap<>();
		metadata.put("fragment", Boolean.toString(fragment));
		metadata.put("fragment.usage", "barracks");
		metadata.put("fragment.kind", kind);
		if (start)
		{
			metadata.put("fragment.start", "true");
		}
		metadata.put("fragment.depthMin", Integer.toString(depthMin));
		metadata.put("fragment.depthMax", "99");
		metadata.put("fragment.weight", Integer.toString(weight));
		metadata.put("fragment.maxPerFloor", Integer.toString(maxPerFloor));
		if (!rotate)
		{
			metadata.put("fragment.rotate", "false");
		}
		return metadata;
	}

	/*-------------------------------------------------------------------------*/
	private static Set<Point> fullRoom(int width, int length)
	{
		Set<Point> floor = new HashSet<>();
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < length; y++)
			{
				floor.add(new Point(x, y));
			}
		}
		return floor;
	}

	/*-------------------------------------------------------------------------*/
	private static Set<Point> lineRoom(int width, int length)
	{
		return fullRoom(width, length);
	}

	/*-------------------------------------------------------------------------*/
	private record Opening(int x, int y, int facing)
	{
	}

	/*-------------------------------------------------------------------------*/
	private static Opening opening(int x, int y, int facing)
	{
		return new Opening(x, y, facing);
	}

	/*-------------------------------------------------------------------------*/
	private static EngineObject object(
		String name,
		int x,
		int y,
		Texture face,
		Texture side,
		int width)
	{
		EngineObject obj = new EngineObject(
			name,
			x * TILE_SIZE,
			y * TILE_SIZE,
			face,
			face,
			side,
			side,
			y * width + x,
			false,
			null,
			EngineObject.Alignment.BOTTOM);
		obj.setGridX(x);
		obj.setGridY(y);
		return obj;
	}

	/*-------------------------------------------------------------------------*/
	private static Zone buildZone(
		Database db,
		Texture wallTex,
		Texture floorTex,
		Texture ceilingTex,
		Texture skyTex,
		String name,
		int width,
		int length,
		Set<Point> floorTiles,
		Set<Opening> openings,
		List<EngineObject> objects,
		Map<String, String> metadata)
	{
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
		Arrays.fill(horiz, solid);
		Arrays.fill(vert, solid);

		Tile[] ctiles = new Tile[width * length];
		mclachlan.maze.map.Tile[][] logic = new mclachlan.maze.map.Tile[width][length];

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < length; y++)
			{
				Point p = new Point(x, y);
				boolean isFloor = floorTiles.contains(p);

				Tile ct = new Tile(ceilingTex, floorTex, CrusaderEngine.NORMAL_LIGHT_LEVEL);
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
				else
				{
					horiz[x + y * width] = open;
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
				else
				{
					horiz[x + (y + 1) * width] = open;
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
				else
				{
					vert[x + y * (width + 1)] = open;
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
				else
				{
					vert[x + y * (width + 1) + 1] = open;
				}
			}
		}

		mclachlan.crusader.Map.SkyConfig sky = new mclachlan.crusader.Map.SkyConfig(
			mclachlan.crusader.Map.SkyConfig.Type.CYLINDER_IMAGE,
			skyTex,
			0,
			0,
			null,
			0,
			null,
			null,
			null,
			null,
			0);

		mclachlan.crusader.Map map = new mclachlan.crusader.Map(
			length,
			width,
			TILE_SIZE,
			ctiles,
			new Texture[]{wallTex, floorTex, ceilingTex, mclachlan.crusader.Map.NO_WALL},
			horiz,
			vert,
			new mclachlan.crusader.Map.SkyConfig[]{sky},
			new ArrayList<>(objects),
			new MapScript[0]);
		map.init();

		Zone zone = new Zone();
		zone.setName(name);
		zone.setWidth(width);
		zone.setLength(length);
		zone.setTiles(logic);
		zone.setMap(map);
		zone.setPortals(new mclachlan.maze.map.Portal[0]);
		zone.setScript(new DefaultZoneScript());
		zone.setMetadata(metadata);
		zone.setShadeTargetColor(new Color(0, 0, 0, 255));
		zone.setTransparentColor(new Color(255, 255, 255, 255));
		zone.setDoShading(true);
		zone.setDoLighting(true);
		zone.setShadingDistance(2.5);
		zone.setShadingMultiplier(2.5);
		zone.setProjectionPlaneOffset(-40);
		zone.setPlayerFieldOfView(2);
		zone.setScaleDistFromProjPlane(0.65);
		zone.setOrder(0);
		zone.setPlayerOrigin(new Point(width / 2, length / 2));
		return zone;
	}
}
