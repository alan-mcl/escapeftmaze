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

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Tile;
import mclachlan.crusader.script.RandomLightingScript;
import mclachlan.dungeongen.DungeonGenResult;
import mclachlan.dungeongen.DungeonRoom;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.Zone;

/**
 * Post-processes generated temple floors with persist-once light fixtures,
 * radial tile-light pools, and flicker scripts for flame types.
 */
public final class TempleLighting
{
	public static final String LIGHT_PURPOSE = "light";
	public static final String PLACE_PURPOSE = "light.place";

	private static final int ROOM = Noise4jDungeonGen.ROOM_THRESHOLD;
	private static final int CORRIDOR = Noise4jDungeonGen.CORRIDOR_THRESHOLD;
	private static final int MIN_LIGHT_SEPARATION = 4;
	private static final int FLICKER_FREQUENCY = 25;

	private TempleLighting()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void dress(
		Zone zone,
		int depth,
		TempleEnvironment environment,
		Set<Point> avoidTiles,
		DungeonGenResult layout)
	{
		if (layout == null || layout.layoutGrid() == null || layout.rooms().isEmpty())
		{
			return;
		}

		FixtureType fixture = pickFixture(depth);
		if (fixture == null)
		{
			return;
		}

		int ambient = environment.ambientLight();
		int peak = Math.min(ambient + 8, 32);
		Random random = TempleSeededPicks.rng(depth, PLACE_PURPOSE);
		Set<Point> avoid = new HashSet<>(avoidTiles);
		avoid.addAll(TempleFloorDressing.findChestTiles(zone));
		Set<Point> placed = new LinkedHashSet<>();
		Map map = zone.getMap();
		Grid grid = layout.layoutGrid();
		List<DungeonRoom> rooms = layout.rooms();

		if (fixture.ceiling())
		{
			placeCeilingRoomLights(zone, grid, rooms, fixture, avoid, placed, ambient, random);
			placeCeilingCorridorLights(zone, grid, fixture, avoid, placed);
		}
		else
		{
			placeFlameRoomLights(zone, grid, rooms, fixture, avoid, placed, ambient, random);
		}

		applyLightPools(map, placed, ambient);
		if (fixture.flickers())
		{
			addFlickerScripts(map, placed, ambient, peak);
		}

		registerTexture(map, fixture.textureName());
		map.init();
	}

	/*-------------------------------------------------------------------------*/
	static FixtureType pickFixture(int depth)
	{
		List<FixtureType> available = new ArrayList<>();
		java.util.Map<String, ?> textures = Database.getInstance().getMazeTextures();
		for (FixtureType type : FixtureType.values())
		{
			if (textures.containsKey(type.textureName()))
			{
				available.add(type);
			}
		}
		if (available.isEmpty())
		{
			return null;
		}
		return TempleSeededPicks.pickOneAndRemember(
			depth,
			LIGHT_PURPOSE,
			available,
			FixtureType::id,
			FixtureType::fromId);
	}

	/*-------------------------------------------------------------------------*/
	private static void placeFlameRoomLights(
		Zone zone,
		Grid grid,
		List<DungeonRoom> rooms,
		FixtureType fixture,
		Set<Point> avoid,
		Set<Point> placed,
		int ambient,
		Random random)
	{
		for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++)
		{
			DungeonRoom room = rooms.get(roomIndex);
			int count = lightsPerRoom(ambient, random);
			if (count <= 0)
			{
				continue;
			}
			List<Point> candidates = roomCandidates(room, grid);
			candidates.removeIf(tile -> avoid.contains(tile) || tooClose(tile, placed));
			if (candidates.isEmpty())
			{
				continue;
			}
			Collections.shuffle(candidates, new Random(random.nextLong() ^ roomIndex));
			for (int i = 0; i < Math.min(count, candidates.size()); i++)
			{
				Point tile = candidates.get(i);
				if (placeObject(zone, fixture, tile))
				{
					placed.add(tile);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void placeCeilingRoomLights(
		Zone zone,
		Grid grid,
		List<DungeonRoom> rooms,
		FixtureType fixture,
		Set<Point> avoid,
		Set<Point> placed,
		int ambient,
		Random random)
	{
		for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++)
		{
			DungeonRoom room = rooms.get(roomIndex);
			int count = lightsPerRoom(ambient, random);
			if (count <= 0)
			{
				continue;
			}
			List<Point> candidates = roomCandidates(room, grid);
			candidates.removeIf(tile -> avoid.contains(tile) || tooClose(tile, placed));
			if (candidates.isEmpty())
			{
				continue;
			}
			Collections.shuffle(candidates, new Random(random.nextLong() ^ roomIndex));
			for (int i = 0; i < Math.min(count, candidates.size()); i++)
			{
				Point tile = candidates.get(i);
				if (placeObject(zone, fixture, tile))
				{
					placed.add(tile);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void placeCeilingCorridorLights(
		Zone zone,
		Grid grid,
		FixtureType fixture,
		Set<Point> avoid,
		Set<Point> placed)
	{
		int w = zone.getWidth();
		int h = zone.getLength();
		boolean[][] visited = new boolean[w][h];

		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				if (visited[x][y] || getGrid(grid, x, y) != CORRIDOR)
				{
					continue;
				}
				if (x + 1 < w && getGrid(grid, x + 1, y) == CORRIDOR
					&& (y == 0 || getGrid(grid, x, y - 1) != CORRIDOR)
					&& (y + 1 >= h || getGrid(grid, x, y + 1) != CORRIDOR))
				{
					int end = x;
					while (end + 1 < w && getGrid(grid, end + 1, y) == CORRIDOR)
					{
						end++;
					}
					placeCorridorRun(zone, fixture, avoid, placed, x, end, y, true, visited);
					x = end;
					continue;
				}
				if (y + 1 < h && getGrid(grid, x, y + 1) == CORRIDOR
					&& (x == 0 || getGrid(grid, x - 1, y) != CORRIDOR)
					&& (x + 1 >= w || getGrid(grid, x + 1, y) != CORRIDOR))
				{
					int end = y;
					while (end + 1 < h && getGrid(grid, x, end + 1) == CORRIDOR)
					{
						end++;
					}
					placeCorridorRun(zone, fixture, avoid, placed, y, end, x, false, visited);
					y = end;
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void placeCorridorRun(
		Zone zone,
		FixtureType fixture,
		Set<Point> avoid,
		Set<Point> placed,
		int start,
		int end,
		int fixed,
		boolean horizontal,
		boolean[][] visited)
	{
		int length = end - start + 1;
		int count = length / MIN_LIGHT_SEPARATION;
		if (count <= 0)
		{
			return;
		}

		double segmentLen = (double)length / count;
		for (int i = 0; i < count; i++)
		{
			int offset = start + (int)(i * segmentLen + segmentLen / 2);
			Point tile = horizontal ? new Point(offset, fixed) : new Point(fixed, offset);
			visited[tile.x][tile.y] = true;
			if (avoid.contains(tile) || tooClose(tile, placed))
			{
				continue;
			}
			if (placeObject(zone, fixture, tile))
			{
				placed.add(tile);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static int lightsPerRoom(int ambient, Random random)
	{
		return switch (ambient)
		{
			case 16 -> random.nextBoolean() ? 1 : 0;
			case 20 -> 1;
			case 24 -> 2 + random.nextInt(2);
			default -> 1;
		};
	}

	/*-------------------------------------------------------------------------*/
	private static List<Point> roomCandidates(DungeonRoom room, Grid grid)
	{
		List<Point> result = new ArrayList<>();
		Point center = new Point(room.centerX(), room.centerY());
		if (isRoomTile(grid, center.x, center.y))
		{
			result.add(center);
		}
		addCornerTiles(room, grid, result);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static void addCornerTiles(DungeonRoom room, Grid grid, List<Point> out)
	{
		int x0 = room.getX();
		int y0 = room.getY();
		int x1 = room.getX() + room.getWidth() - 1;
		int y1 = room.getY() + room.getHeight() - 1;
		tryAddRoomTile(grid, out, x0, y0);
		tryAddRoomTile(grid, out, x1, y0);
		tryAddRoomTile(grid, out, x0, y1);
		tryAddRoomTile(grid, out, x1, y1);
	}

	/*-------------------------------------------------------------------------*/
	private static void tryAddRoomTile(Grid grid, List<Point> out, int x, int y)
	{
		if (isRoomTile(grid, x, y))
		{
			out.add(new Point(x, y));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static boolean isRoomTile(Grid grid, int x, int y)
	{
		return getGrid(grid, x, y) == ROOM;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean placeObject(Zone zone, FixtureType fixture, Point tile)
	{
		Database db = Database.getInstance();
		Texture tex = db.getMazeTexture(fixture.textureName()).getTexture();
		if (tex == null)
		{
			return false;
		}

		Map map = zone.getMap();
		int tileIndex = zone.getTileIndex(tile);
		EngineObject obj = new EngineObject(
			null,
			0,
			0,
			tex,
			tex,
			tex,
			tex,
			tileIndex,
			true,
			null,
			fixture.alignment());
		map.initObjectFromTileIndex(obj, EngineObject.Placement.CENTER);
		map.addObject(obj);
		return true;
	}

	/*-------------------------------------------------------------------------*/
	static void applyLightPools(Map map, Set<Point> sources, int ambient)
	{
		int width = map.getWidth();
		int length = map.getLength();
		Tile[] tiles = map.getTiles();

		for (Point source : sources)
		{
			for (int dx = -2; dx <= 2; dx++)
			{
				for (int dy = -2; dy <= 2; dy++)
				{
					int x = source.x + dx;
					int y = source.y + dy;
					if (x < 0 || y < 0 || x >= width || y >= length)
					{
						continue;
					}
					int dist = Math.max(Math.abs(dx), Math.abs(dy));
					int boost = switch (dist)
					{
						case 0 -> 8;
						case 1 -> 4;
						case 2 -> 2;
						default -> 0;
					};
					int level = Math.min(ambient + boost, 32);
					Tile tile = tiles[y * width + x];
					tile.setLightLevel(Math.max(tile.getLightLevel(), level));
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void addFlickerScripts(Map map, Set<Point> sources, int ambient, int peak)
	{
		int width = map.getWidth();
		for (Point source : sources)
		{
			int tileIndex = source.y * width + source.x;
			map.addScript(new RandomLightingScript(
				new int[]{tileIndex},
				FLICKER_FREQUENCY,
				ambient,
				peak));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void registerTexture(Map map, String textureName)
	{
		Texture tex = Database.getInstance().getMazeTexture(textureName).getTexture();
		if (tex != null)
		{
			map.addTexture(tex);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static boolean tooClose(Point candidate, Set<Point> placed)
	{
		for (Point existing : placed)
		{
			if (chebyshev(candidate, existing) < MIN_LIGHT_SEPARATION)
			{
				return true;
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	static int chebyshev(Point a, Point b)
	{
		return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
	}

	/*-------------------------------------------------------------------------*/
	static int cellType(Grid grid, int x, int y)
	{
		return getGrid(grid, x, y);
	}

	/*-------------------------------------------------------------------------*/
	static List<Point> flameCandidateTiles(DungeonRoom room, Grid grid)
	{
		return roomCandidates(room, grid);
	}

	/*-------------------------------------------------------------------------*/
	private static int getGrid(Grid grid, int x, int y)
	{
		return (int)(grid.get(x, y) * 10);
	}

	/*-------------------------------------------------------------------------*/
	public enum FixtureType
	{
		TORCH("torch", "objects.torch.1", false, EngineObject.Alignment.BOTTOM),
		BRAZIER("brazier", "objects.brazier.1", false, EngineObject.Alignment.BOTTOM),
		CEILING_1("ceiling1", "CEILING_LIGHT_1", true, EngineObject.Alignment.TOP),
		CEILING_2("ceiling2", "CEILING_LIGHT_2", true, EngineObject.Alignment.TOP);

		private final String id;
		private final String textureName;
		private final boolean ceiling;
		private final EngineObject.Alignment alignment;

		FixtureType(
			String id,
			String textureName,
			boolean ceiling,
			EngineObject.Alignment alignment)
		{
			this.id = id;
			this.textureName = textureName;
			this.ceiling = ceiling;
			this.alignment = alignment;
		}

		String id()
		{
			return id;
		}

		String textureName()
		{
			return textureName;
		}

		boolean ceiling()
		{
			return ceiling;
		}

		boolean flickers()
		{
			return !ceiling;
		}

		EngineObject.Alignment alignment()
		{
			return alignment;
		}

		static FixtureType fromId(String id)
		{
			for (FixtureType type : values())
			{
				if (type.id.equals(id))
				{
					return type;
				}
			}
			return null;
		}
	}
}
