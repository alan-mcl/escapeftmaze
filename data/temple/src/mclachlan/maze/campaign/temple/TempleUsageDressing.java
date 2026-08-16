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
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.DungeonGenResult;
import mclachlan.dungeongen.DungeonRoom;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.crusader.MouseClickScriptAdapter;
import mclachlan.maze.map.script.ExecuteMazeScript;

/**
 * Post-processes generated temple floors with usage-themed objects, wall masks,
 * garden beds, mystery brazier lights, and hidden storage loot.
 */
public final class TempleUsageDressing
{
	private static final String PLACE_PURPOSE = "usage.place";
	private static final String LOOT_CONTAINER_PURPOSE = "loot.container.";

	private static final int ROOM = Noise4jDungeonGen.ROOM_THRESHOLD;
	private static final int WALL = Noise4jDungeonGen.WALL_THRESHOLD;
	private static final int CORRIDOR = Noise4jDungeonGen.CORRIDOR_THRESHOLD;

	private static final int[] ALL_PLACEMENTS = {
		EngineObject.Placement.NORTH_WEST,
		EngineObject.Placement.NORTH,
		EngineObject.Placement.NORTH_EAST,
		EngineObject.Placement.WEST,
		EngineObject.Placement.CENTER,
		EngineObject.Placement.EAST,
		EngineObject.Placement.SOUTH_WEST,
		EngineObject.Placement.SOUTH,
		EngineObject.Placement.SOUTH_EAST
	};

	private TempleUsageDressing()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void dress(
		Zone zone,
		int depth,
		TempleEnvironment environment,
		TempleUsageTheme usageTheme,
		Set<Point> avoidTiles,
		DungeonGenResult layout,
		TempleLighting.DressResult lighting)
	{
		if (layout == null || layout.layoutGrid() == null || layout.rooms().isEmpty()
			|| usageTheme == null)
		{
			return;
		}

		Map map = zone.getMap();
		Grid grid = layout.layoutGrid();
		List<DungeonRoom> rooms = layout.rooms();
		int ambient = environment.ambientLight();
		int peak = Math.min(ambient + 8, 32);
		Random random = TempleSeededPicks.rng(depth, PLACE_PURPOSE);

		Set<Point> spawnAvoid = buildAvoid(zone, avoidTiles, lighting);
		Set<Point> occupiedFloor = new HashSet<>(spawnAvoid);
		if (lighting != null)
		{
			occupiedFloor.addAll(lighting.floorStandingTiles());
		}
		occupiedFloor.addAll(TempleFloorDressing.findChestTiles(zone));
		Set<Point> doorTiles = TempleFloorDressing.findDoorTiles(zone);
		Set<Point> ceilingLightTiles = lighting == null || lighting.fixture() == null
			|| !lighting.fixture().ceiling()
			? Set.of()
			: lighting.placedTiles();

		List<TempleFloorDressing.LootSlot> lootSlots = TempleFloorDressing.pickLootSlots(
			depth, zone.getPlayerOrigin(), layout);
		Set<Integer> storageLootRooms = new HashSet<>();
		for (TempleFloorDressing.LootSlot slot : lootSlots)
		{
			if (usageTheme.themeForRoom(depth, slot.roomIndex()) == TempleUsageTheme.Theme.STORAGE)
			{
				storageLootRooms.add(slot.roomIndex());
			}
		}

		List<StashCandidate> stashCandidates = new ArrayList<>();
		Set<Point> mysteryBraziers = new LinkedHashSet<>();

		for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++)
		{
			DungeonRoom room = rooms.get(roomIndex);
			TempleUsageTheme.Theme theme = usageTheme.themeForRoom(depth, roomIndex);
			boolean lootRoom = storageLootRooms.contains(roomIndex);
			switch (theme)
			{
				case STORAGE -> dressStorageRoom(
					zone, depth, grid, room, roomIndex, occupiedFloor, doorTiles,
					random, lootRoom, stashCandidates);
				case LIBRARY -> dressLibraryRoom(
					zone, depth, grid, room, roomIndex, occupiedFloor, random);
				case MYSTERY -> dressMysteryRoom(
					zone, depth, grid, room, roomIndex, occupiedFloor, doorTiles,
					ceilingLightTiles, random, mysteryBraziers);
				case GARDEN -> dressGardenRoom(
					zone, depth, grid, room, roomIndex, spawnAvoid, lighting, ambient, random);
				default -> { }
			}
		}

		if (usageTheme.floorTheme() == TempleUsageTheme.Theme.LIBRARY)
		{
			dressLibraryCorridors(zone, grid, depth, random);
		}

		attachStorageLoot(depth, lootSlots, storageLootRooms, stashCandidates);

		if (!mysteryBraziers.isEmpty())
		{
			TempleLighting.applyLightPools(map, mysteryBraziers, ambient);
			TempleLighting.addFlickerScripts(map, mysteryBraziers, ambient, peak);
		}

		map.init();
	}

	/*-------------------------------------------------------------------------*/
	private static Set<Point> buildAvoid(
		Zone zone,
		Set<Point> avoidTiles,
		TempleLighting.DressResult lighting)
	{
		Set<Point> avoid = new HashSet<>(avoidTiles);
		Point spawn = zone.getPlayerOrigin();
		if (spawn != null)
		{
			avoid.add(spawn);
		}
		avoid.addAll(TempleFloorDressing.findChestTiles(zone));
		if (lighting != null)
		{
			avoid.addAll(lighting.floorStandingTiles());
		}
		return avoid;
	}

	/*-------------------------------------------------------------------------*/
	private static void dressStorageRoom(
		Zone zone,
		int depth,
		Grid grid,
		DungeonRoom room,
		int roomIndex,
		Set<Point> occupiedFloor,
		Set<Point> doorTiles,
		Random random,
		boolean lootRoom,
		List<StashCandidate> stashCandidates)
	{
		int area = room.getWidth() * room.getHeight();
		int count = Math.min(5, Math.max(2, area / 6));
		if (lootRoom)
		{
			count = Math.max(count, 2);
		}

		List<Point> tiles = shuffleRoomTiles(grid, room, occupiedFloor, random, roomIndex);
		int placed = 0;
		for (Point tile : tiles)
		{
			if (placed >= count)
			{
				break;
			}
			String texture = pickStorageProp(random, placed == 0 && lootRoom);
			if (blocksDoor(texture) && doorTiles.contains(tile))
			{
				continue;
			}
			if ("objects.barrel.1".equals(texture))
			{
				List<EngineObject> barrels = placeBarrelGroup(zone, tile, random);
				if (!barrels.isEmpty())
				{
					occupiedFloor.add(tile);
					placed++;
					if (lootRoom)
					{
						for (EngineObject barrel : barrels)
						{
							stashCandidates.add(new StashCandidate(barrel, roomIndex));
						}
					}
				}
			}
			else
			{
				EngineObject obj = placeObject(zone, texture, tile, EngineObject.Placement.CENTER, false);
				if (obj != null)
				{
					occupiedFloor.add(tile);
					placed++;
					if (lootRoom && isStashContainer(texture))
					{
						stashCandidates.add(new StashCandidate(obj, roomIndex));
					}
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static final int[] BARREL_GROUP_SLOTS = {
		EngineObject.Placement.NORTH_WEST,
		EngineObject.Placement.NORTH_EAST,
		EngineObject.Placement.SOUTH_WEST,
		EngineObject.Placement.SOUTH_EAST
	};

	/*-------------------------------------------------------------------------*/
	private static List<EngineObject> placeBarrelGroup(Zone zone, Point tile, Random random)
	{
		int count = 1 + random.nextInt(4);
		List<Integer> slots = new ArrayList<>();
		for (int slot : BARREL_GROUP_SLOTS)
		{
			slots.add(slot);
		}
		Collections.shuffle(slots, new Random(random.nextLong()));
		List<EngineObject> placed = new ArrayList<>();
		for (int i = 0; i < count; i++)
		{
			EngineObject obj = placeObject(zone, "objects.barrel.1", tile, slots.get(i), false);
			if (obj != null)
			{
				placed.add(obj);
			}
		}
		return placed;
	}

	/*-------------------------------------------------------------------------*/
	private static String pickStorageProp(Random random, boolean needContainer)
	{
		if (needContainer)
		{
			return random.nextBoolean() ? "objects.barrel.1" : "objects.crate.1";
		}
		int roll = random.nextInt(100);
		if (roll < 35)
		{
			return "objects.barrel.1";
		}
		if (roll < 65)
		{
			return "objects.crate.1";
		}
		if (roll < 80)
		{
			return random.nextBoolean() ? "objects.table.1" : "objects.table.2";
		}
		if (roll < 95)
		{
			return "objects.chair.1";
		}
		return "objects.market.stall.1";
	}

	/*-------------------------------------------------------------------------*/
	private static boolean isStashContainer(String textureName)
	{
		return "objects.barrel.1".equals(textureName) || "objects.crate.1".equals(textureName);
	}

	/*-------------------------------------------------------------------------*/
	private static boolean blocksDoor(String textureName)
	{
		return "objects.crate.1".equals(textureName)
			|| "objects.market.stall.1".equals(textureName);
	}

	/*-------------------------------------------------------------------------*/
	private static void dressLibraryRoom(
		Zone zone,
		int depth,
		Grid grid,
		DungeonRoom room,
		int roomIndex,
		Set<Point> occupiedFloor,
		Random random)
	{
		List<WallSlot> walls = blankWallSlots(grid, room);
		if (!walls.isEmpty())
		{
			int maskCount = Math.min(walls.size(), 2 + random.nextInt(3));
			Collections.shuffle(walls, new Random(random.nextLong() ^ roomIndex));
			for (int i = 0; i < maskCount; i++)
			{
				applyBookshelfMask(zone, walls.get(i));
			}
		}

		int chairs = random.nextInt(3);
		List<Point> tiles = shuffleRoomTiles(grid, room, occupiedFloor, random, roomIndex ^ 0xBEEF);
		for (int i = 0; i < Math.min(chairs, tiles.size()); i++)
		{
			Point tile = tiles.get(i);
			if (placeObject(zone, "objects.chair.1", tile, EngineObject.Placement.CENTER, false) != null)
			{
				occupiedFloor.add(tile);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void dressLibraryCorridors(Zone zone, Grid grid, int depth, Random random)
	{
		int w = zone.getWidth();
		int h = zone.getLength();
		List<WallSlot> candidates = new ArrayList<>();
		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				if (getGrid(grid, x, y) != CORRIDOR)
				{
					continue;
				}
				addBlankWallSlots(grid, x, y, candidates);
			}
		}
		if (candidates.isEmpty())
		{
			return;
		}
		int count = Math.min(candidates.size(), 1 + random.nextInt(3));
		Collections.shuffle(candidates, new Random(random.nextLong() ^ depth));
		for (int i = 0; i < count; i++)
		{
			applyBookshelfMask(zone, candidates.get(i));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void dressMysteryRoom(
		Zone zone,
		int depth,
		Grid grid,
		DungeonRoom room,
		int roomIndex,
		Set<Point> occupiedFloor,
		Set<Point> doorTiles,
		Set<Point> ceilingLightTiles,
		Random random,
		Set<Point> mysteryBraziers)
	{
		List<Point> tiles = shuffleRoomTiles(grid, room, occupiedFloor, random, roomIndex);
		if (!tiles.isEmpty())
		{
			Point focal = tiles.get(0);
			String focalTex = random.nextInt(100) < 60 ? "objects.altar.1" : "objects.shrine.1.front";
			if ("objects.shrine.1.front".equals(focalTex))
			{
				placeDirectionalObject(
					zone, focal,
					"objects.shrine.1.front", "objects.shrine.1.front",
					"objects.shrine.1.side", "objects.shrine.1.side",
					false);
			}
			else
			{
				placeDirectionalObject(
					zone, focal,
					"objects.altar.1", "objects.altar.1",
					"objects.altar.1.side", "objects.altar.1.side",
					false);
			}
			occupiedFloor.add(focal);
		}

		if (room.getWidth() >= 3 && room.getHeight() >= 3 && random.nextInt(100) < 70)
		{
			for (Point corner : cornerTiles(room, grid))
			{
				if (occupiedFloor.contains(corner)
					|| doorTiles.contains(corner)
					|| ceilingLightTiles.contains(corner))
				{
					continue;
				}
				if (placeObject(zone, "objects.pillar.1", corner, EngineObject.Placement.CENTER, false) != null)
				{
					occupiedFloor.add(corner);
				}
			}
		}

		if (random.nextInt(100) < 8 && !tiles.isEmpty())
		{
			for (int i = 1; i < tiles.size(); i++)
			{
				Point tile = tiles.get(i);
				if (placeObject(zone, "objects.ruin.head", tile, EngineObject.Placement.CENTER, false) != null)
				{
					occupiedFloor.add(tile);
					break;
				}
			}
		}

		if (random.nextBoolean() && tiles.size() > 1)
		{
			for (int i = 1; i < tiles.size(); i++)
			{
				Point tile = tiles.get(i);
				if (occupiedFloor.contains(tile))
				{
					continue;
				}
				if (placeObject(
					zone, "objects.brazier.1", tile, EngineObject.Placement.CENTER, true) != null)
				{
					mysteryBraziers.add(tile);
					occupiedFloor.add(tile);
					break;
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void dressGardenRoom(
		Zone zone,
		int depth,
		Grid grid,
		DungeonRoom room,
		int roomIndex,
		Set<Point> spawnAvoid,
		TempleLighting.DressResult lighting,
		int ambient,
		Random random)
	{
		boolean horizontalRows = room.getWidth() >= room.getHeight();
		java.util.Map<Integer, List<Point>> rows = new java.util.TreeMap<>();
		for (int x = room.getX(); x < room.getX() + room.getWidth(); x++)
		{
			for (int y = room.getY(); y < room.getY() + room.getHeight(); y++)
			{
				if (getGrid(grid, x, y) != ROOM)
				{
					continue;
				}
				Point tile = new Point(x, y);
				if (spawnAvoid.contains(tile))
				{
					continue;
				}
				int key = horizontalRows ? y : x;
				rows.computeIfAbsent(key, k -> new ArrayList<>()).add(tile);
			}
		}
		if (rows.isEmpty())
		{
			return;
		}

		Set<Point> lightTiles = lighting == null ? Set.of() : lighting.placedTiles();
		List<Integer> rowKeys = new ArrayList<>(rows.keySet());
		int rowStride = 2;
		int offset = Math.floorMod(random.nextInt() ^ roomIndex, rowStride);

		for (int i = 0; i < rowKeys.size(); i++)
		{
			if (Math.floorMod(i - offset, rowStride) != 0)
			{
				continue;
			}
			List<Point> rowTiles = rows.get(rowKeys.get(i));
			rowTiles.sort(horizontalRows
				? Comparator.comparingInt(p -> p.x)
				: Comparator.comparingInt(p -> p.y));
			for (Point tile : rowTiles)
			{
				if (spawnAvoid.contains(tile))
				{
					continue;
				}
				if (!isLitTile(zone, tile, ambient, lightTiles))
				{
					continue;
				}
				plantGardenBed(zone, tile, random);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static boolean isLitTile(Zone zone, Point tile, int ambient, Set<Point> lightTiles)
	{
		if (lightTiles.contains(tile))
		{
			return true;
		}
		int tileIndex = zone.getTileIndex(tile);
		mclachlan.crusader.Tile crusaderTile = zone.getMap().getTiles()[tileIndex];
		return crusaderTile.getLightLevel() > ambient;
	}

	/*-------------------------------------------------------------------------*/
	private static void plantGardenBed(Zone zone, Point tile, Random random)
	{
		String texture = random.nextInt(100) < 25
			? pickFungus(random)
			: pickPlant(random);
		for (int placement : ALL_PLACEMENTS)
		{
			placeObject(zone, texture, tile, placement, false);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String pickPlant(Random random)
	{
		return switch (random.nextInt(5))
		{
			case 0 -> "objects.plant.1";
			case 1 -> "objects.plant.2";
			case 2 -> "objects.plant.3";
			case 3 -> "objects.plant.4";
			default -> "objects.plant.5";
		};
	}

	/*-------------------------------------------------------------------------*/
	private static String pickFungus(Random random)
	{
		return switch (random.nextInt(4))
		{
			case 0 -> "objects.fungus.1";
			case 1 -> "objects.fungus.2";
			case 2 -> "objects.fungus.3";
			default -> "objects.fungus.ambercap";
		};
	}

	/*-------------------------------------------------------------------------*/
	private static void attachStorageLoot(
		int depth,
		List<TempleFloorDressing.LootSlot> lootSlots,
		Set<Integer> storageLootRooms,
		List<StashCandidate> stashCandidates)
	{
		if (stashCandidates.isEmpty())
		{
			return;
		}

		String lootTableName = TempleDepthScaler.lootTableName(depth);
		for (TempleFloorDressing.LootSlot slot : lootSlots)
		{
			if (!storageLootRooms.contains(slot.roomIndex()))
			{
				continue;
			}

			List<StashCandidate> inRoom = stashCandidates.stream()
				.filter(c -> c.roomIndex() == slot.roomIndex())
				.toList();
			if (inRoom.isEmpty())
			{
				continue;
			}

			StashCandidate picked = TempleSeededPicks.pickOneAndRemember(
				depth,
				LOOT_CONTAINER_PURPOSE + slot.lootIndex(),
				inRoom,
				c -> Integer.toString(c.object().getTileIndex()),
				key -> inRoom.stream()
					.filter(c -> Integer.toString(c.object().getTileIndex()).equals(key))
					.findFirst()
					.orElse(null));
			if (picked == null)
			{
				continue;
			}

			ExecuteMazeScript script = new ExecuteMazeScript(new MazeScript(
				"temple.generated.stash",
				List.of(new TempleChestLootEvent(lootTableName))));
			script.setScoutSecretDifficulty(TempleDepthScaler.scoutSecretDifficulty(depth));
			script.setExecuteOnceMazeVariable(slot.lootVar());
			script.setClickMaxDistance(1);
			picked.object().setMouseClickScript(new MouseClickScriptAdapter(script));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static List<Point> shuffleRoomTiles(
		Grid grid,
		DungeonRoom room,
		Set<Point> occupied,
		Random random,
		int salt)
	{
		List<Point> tiles = new ArrayList<>();
		for (int x = room.getX(); x < room.getX() + room.getWidth(); x++)
		{
			for (int y = room.getY(); y < room.getY() + room.getHeight(); y++)
			{
				if (getGrid(grid, x, y) != ROOM)
				{
					continue;
				}
				Point tile = new Point(x, y);
				if (!occupied.contains(tile))
				{
					tiles.add(tile);
				}
			}
		}
		Collections.shuffle(tiles, new Random(random.nextLong() ^ salt));
		return tiles;
	}

	/*-------------------------------------------------------------------------*/
	private static List<Point> cornerTiles(DungeonRoom room, Grid grid)
	{
		List<Point> corners = new ArrayList<>();
		int x0 = room.getX();
		int y0 = room.getY();
		int x1 = room.getX() + room.getWidth() - 1;
		int y1 = room.getY() + room.getHeight() - 1;
		tryAddRoomTile(grid, corners, x0, y0);
		tryAddRoomTile(grid, corners, x1, y0);
		tryAddRoomTile(grid, corners, x0, y1);
		tryAddRoomTile(grid, corners, x1, y1);
		return corners;
	}

	/*-------------------------------------------------------------------------*/
	private static void tryAddRoomTile(Grid grid, List<Point> out, int x, int y)
	{
		if (getGrid(grid, x, y) == ROOM)
		{
			out.add(new Point(x, y));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static List<WallSlot> blankWallSlots(Grid grid, DungeonRoom room)
	{
		List<WallSlot> candidates = new ArrayList<>();
		for (int x = room.getX(); x < room.getX() + room.getWidth(); x++)
		{
			for (int y = room.getY(); y < room.getY() + room.getHeight(); y++)
			{
				if (getGrid(grid, x, y) != ROOM)
				{
					continue;
				}
				addBlankWallSlots(grid, x, y, candidates);
			}
		}
		return candidates;
	}

	/*-------------------------------------------------------------------------*/
	private static void addBlankWallSlots(Grid grid, int x, int y, List<WallSlot> out)
	{
		if (getGrid(grid, x, y - 1) == WALL)
		{
			out.add(new WallSlot(new Point(x, y), CrusaderEngine.Facing.NORTH, true));
		}
		if (getGrid(grid, x, y + 1) == WALL)
		{
			out.add(new WallSlot(new Point(x, y), CrusaderEngine.Facing.SOUTH, true));
		}
		if (getGrid(grid, x - 1, y) == WALL)
		{
			out.add(new WallSlot(new Point(x, y), CrusaderEngine.Facing.WEST, false));
		}
		if (getGrid(grid, x + 1, y) == WALL)
		{
			out.add(new WallSlot(new Point(x, y), CrusaderEngine.Facing.EAST, false));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void applyBookshelfMask(Zone zone, WallSlot slot)
	{
		Map map = zone.getMap();
		int width = map.getWidth();
		int gridIndex = slot.tile().y * width + slot.tile().x;
		int wallIndex = switch (slot.wallFacing())
		{
			case CrusaderEngine.Facing.NORTH -> map.getNorthWall(gridIndex);
			case CrusaderEngine.Facing.SOUTH -> map.getSouthWall(gridIndex);
			case CrusaderEngine.Facing.EAST -> map.getEastWall(gridIndex);
			case CrusaderEngine.Facing.WEST -> map.getWestWall(gridIndex);
			default -> -1;
		};
		if (wallIndex < 0)
		{
			return;
		}

		Wall wall = map.getWall(wallIndex, slot.horizontal());
		if (wall == null || wall.getMaskTextures() != null)
		{
			return;
		}

		Texture mask = Database.getInstance().getMazeTexture("objects.bookshelf.1").getTexture();
		if (mask == null)
		{
			return;
		}

		Wall copy = wall.copyWall();
		copy.setMaskTextures(new Texture[]{mask});
		map.setWall(wallIndex, slot.horizontal(), copy);
		map.addTexture(mask);
	}

	/*-------------------------------------------------------------------------*/
	private static EngineObject placeObject(
		Zone zone,
		String textureName,
		Point tile,
		int placement,
		boolean lightSource)
	{
		Database db = Database.getInstance();
		if (!db.getMazeTextures().containsKey(textureName))
		{
			return null;
		}
		Texture tex = db.getMazeTexture(textureName).getTexture();
		if (tex == null)
		{
			return null;
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
			lightSource,
			null,
			EngineObject.Alignment.BOTTOM);
		map.initObjectFromTileIndex(obj, placement);
		map.addObject(obj);
		map.addTexture(tex);
		return obj;
	}

	/*-------------------------------------------------------------------------*/
	private static void placeDirectionalObject(
		Zone zone,
		Point tile,
		String north,
		String south,
		String east,
		String west,
		boolean lightSource)
	{
		Database db = Database.getInstance();
		Texture n = textureOrNull(db, north);
		Texture s = textureOrNull(db, south);
		Texture e = textureOrNull(db, east);
		Texture w = textureOrNull(db, west);
		if (n == null)
		{
			return;
		}
		if (s == null)
		{
			s = n;
		}
		if (e == null)
		{
			e = n;
		}
		if (w == null)
		{
			w = n;
		}

		Map map = zone.getMap();
		int tileIndex = zone.getTileIndex(tile);
		EngineObject obj = new EngineObject(
			null, 0, 0, n, s, e, w, tileIndex, lightSource, null, EngineObject.Alignment.BOTTOM);
		map.initObjectFromTileIndex(obj, EngineObject.Placement.CENTER);
		map.addObject(obj);
		map.addTexture(n);
		map.addTexture(s);
		map.addTexture(e);
		map.addTexture(w);
	}

	/*-------------------------------------------------------------------------*/
	private static Texture textureOrNull(Database db, String name)
	{
		if (!db.getMazeTextures().containsKey(name))
		{
			return null;
		}
		return db.getMazeTexture(name).getTexture();
	}

	/*-------------------------------------------------------------------------*/
	private static int getGrid(Grid grid, int x, int y)
	{
		return (int)(grid.get(x, y) * 10);
	}

	/*-------------------------------------------------------------------------*/
	private record WallSlot(Point tile, int wallFacing, boolean horizontal)
	{
	}

	/*-------------------------------------------------------------------------*/
	private record StashCandidate(EngineObject object, int roomIndex)
	{
	}
}
