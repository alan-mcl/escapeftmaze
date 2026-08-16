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
import mclachlan.dungeongen.DungeonGenResult;
import mclachlan.dungeongen.DungeonRoom;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Trap;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.map.script.Encounter;
import mclachlan.maze.stat.PercentageTable;

/**
 * Post-processes a generated temple floor: wall chests keyed by depth. Stair
 * portals are applied by {@link TempleStairwellDresser}. Mutation keys are per-depth.
 */
public final class TempleFloorDressing
{
	public static final String ASCEND_SCRIPT = TempleStairLinks.HUB_ASCEND_SCRIPT;
	public static final String ASCEND_PREV_SCRIPT = TempleStairLinks.ASCEND_PREV_SCRIPT;
	public static final String DESCEND_SCRIPT = TempleStairLinks.DESCEND_NEXT_SCRIPT;

	private static final int ROOM = Noise4jDungeonGen.ROOM_THRESHOLD;
	private static final int WALL = Noise4jDungeonGen.WALL_THRESHOLD;
	private static final int CORRIDOR = Noise4jDungeonGen.CORRIDOR_THRESHOLD;

	private TempleFloorDressing()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void dress(Zone zone, int dungeonLevel)
	{
		dress(zone, dungeonLevel, Set.of(), null);
	}

	/*-------------------------------------------------------------------------*/
	public static void dress(Zone zone, int dungeonLevel, Set<Point> avoidTiles)
	{
		dress(zone, dungeonLevel, avoidTiles, null);
	}

	/*-------------------------------------------------------------------------*/
	public static void dress(
		Zone zone,
		int dungeonLevel,
		Set<Point> avoidTiles,
		DungeonGenResult layout)
	{
		if (layout == null || layout.layoutGrid() == null || layout.rooms().isEmpty())
		{
			return;
		}

		Point origin = zone.getPlayerOrigin();
		Grid grid = layout.layoutGrid();
		List<DungeonRoom> rooms = layout.rooms();
		int startingRoom = layout.startingRoomIndex();

		Set<Point> avoid = new HashSet<>(avoidTiles);
		Point upPortal = findStairsUpPortalFrom(zone);
		Point downPortal = findStairsDownPortalFrom(zone);
		if (upPortal != null)
		{
			avoid.add(upPortal);
		}
		if (downPortal != null)
		{
			avoid.add(downPortal);
		}

		Set<Point> doorTiles = findDoorTiles(zone);
		avoid.addAll(doorTiles);

		int lootCount = TempleDepthScaler.lootPlacements(dungeonLevel);
		List<Integer> lootRooms = pickLootRooms(origin, rooms, startingRoom, lootCount);
		String lootTableName = TempleDepthScaler.lootTableName(dungeonLevel);
		int i = 0;
		for (int roomIndex : lootRooms)
		{
			WallSlot slot = pickChestWall(dungeonLevel, roomIndex, grid, rooms, avoid, doorTiles);
			if (slot == null)
			{
				continue;
			}
			placeChest(zone, slot, lootTableName, TempleSeeds.lootVar(dungeonLevel, i++), dungeonLevel);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static String encounterTableName(int dungeonLevel)
	{
		return TempleDepthScaler.encounterTableName(dungeonLevel);
	}

	/*-------------------------------------------------------------------------*/
	public static String lootTableName(int dungeonLevel)
	{
		return TempleDepthScaler.lootTableName(dungeonLevel);
	}

	/*-------------------------------------------------------------------------*/
	public static List<Point> findEncounterTiles(Zone zone)
	{
		List<Point> result = new ArrayList<>();
		Tile[][] tiles = zone.getTiles();
		for (int x = 0; x < tiles.length; x++)
		{
			for (int y = 0; y < tiles[x].length; y++)
			{
				Tile tile = tiles[x][y];
				if (tile == null || tile.getScripts() == null)
				{
					continue;
				}
				for (TileScript script : tile.getScripts())
				{
					if (script instanceof Encounter)
					{
						result.add(new Point(x, y));
						break;
					}
				}
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<Point> findChestTiles(Zone zone)
	{
		List<Point> result = new ArrayList<>();
		Tile[][] tiles = zone.getTiles();
		for (int x = 0; x < tiles.length; x++)
		{
			for (int y = 0; y < tiles[x].length; y++)
			{
				Tile tile = tiles[x][y];
				if (tile == null || tile.getScripts() == null)
				{
					continue;
				}
				for (TileScript script : tile.getScripts())
				{
					if (script instanceof Chest)
					{
						result.add(new Point(x, y));
						break;
					}
				}
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static boolean isInStartingRoom(DungeonGenResult layout, Point tile)
	{
		if (layout == null || tile == null || layout.startingRoomIndex() < 0)
		{
			return false;
		}
		int idx = layout.startingRoomIndex();
		if (idx >= layout.rooms().size())
		{
			return false;
		}
		return layout.rooms().get(idx).contains(tile.x, tile.y);
	}

	/*-------------------------------------------------------------------------*/
	public static Point findStairsUpPortalFrom(Zone zone)
	{
		Point up = findPortalFrom(zone, ASCEND_SCRIPT);
		if (up != null)
		{
			return up;
		}
		return findPortalFrom(zone, ASCEND_PREV_SCRIPT);
	}

	/*-------------------------------------------------------------------------*/
	public static Point findStairsDownPortalFrom(Zone zone)
	{
		return findPortalFrom(zone, DESCEND_SCRIPT);
	}

	/*-------------------------------------------------------------------------*/
	/** @deprecated use {@link #findStairsUpPortalFrom} */
	public static Point findStairsUpTile(Zone zone)
	{
		return findStairsUpPortalFrom(zone);
	}

	/*-------------------------------------------------------------------------*/
	/** @deprecated use {@link #findStairsDownPortalFrom} */
	public static Point findStairsDownTile(Zone zone)
	{
		return findStairsDownPortalFrom(zone);
	}

	/*-------------------------------------------------------------------------*/
	/** @deprecated use {@link #findStairsUpPortalFrom} */
	public static Point findStairsTile(Zone zone)
	{
		return findStairsUpPortalFrom(zone);
	}

	/*-------------------------------------------------------------------------*/
	private static Point findPortalFrom(Zone zone, String mazeScript)
	{
		if (zone.getPortals() == null)
		{
			return null;
		}
		for (Portal portal : zone.getPortals())
		{
			if (mazeScript.equals(portal.getMazeScript()))
			{
				return portal.getFrom();
			}
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private static Set<Point> findDoorTiles(Zone zone)
	{
		Set<Point> result = new HashSet<>();
		if (zone.getPortals() == null)
		{
			return result;
		}
		for (Portal portal : zone.getPortals())
		{
			if (portal.getFrom() != null)
			{
				result.add(portal.getFrom());
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static List<Integer> pickLootRooms(
		Point origin,
		List<DungeonRoom> rooms,
		int startingRoom,
		int count)
	{
		List<Integer> candidates = new ArrayList<>();
		for (int i = 0; i < rooms.size(); i++)
		{
			if (i == startingRoom)
			{
				continue;
			}
			candidates.add(i);
		}
		candidates.sort((a, b) -> Integer.compare(
			roomDist(origin, rooms.get(b)),
			roomDist(origin, rooms.get(a))));
		if (candidates.size() > count)
		{
			return new ArrayList<>(candidates.subList(0, count));
		}
		return candidates;
	}

	/*-------------------------------------------------------------------------*/
	private static int roomDist(Point origin, DungeonRoom room)
	{
		return Math.abs(origin.x - room.centerX()) + Math.abs(origin.y - room.centerY());
	}

	/*-------------------------------------------------------------------------*/
	private static WallSlot pickChestWall(
		int depth,
		int roomIndex,
		Grid grid,
		List<DungeonRoom> rooms,
		Set<Point> avoid,
		Set<Point> doorTiles)
	{
		DungeonRoom room = rooms.get(roomIndex);
		List<WallSlot> candidates = new ArrayList<>();
		for (int x = room.getX(); x < room.getX() + room.getWidth(); x++)
		{
			for (int y = room.getY(); y < room.getY() + room.getHeight(); y++)
			{
				if (getGrid(grid, x, y) != ROOM)
				{
					continue;
				}
				Point tile = new Point(x, y);
				if (avoid.contains(tile) || doorTiles.contains(tile))
				{
					continue;
				}
				addBlankWallSlots(grid, x, y, candidates);
			}
		}
		if (candidates.isEmpty())
		{
			return null;
		}

		return TempleSeededPicks.pickOneAndRemember(
			depth,
			"chest." + roomIndex,
			candidates,
			WallSlot::encode,
			WallSlot::decode);
	}

	/*-------------------------------------------------------------------------*/
	private static void addBlankWallSlots(Grid grid, int x, int y, List<WallSlot> out)
	{
		if (getGrid(grid, x, y - 1) == WALL)
		{
			out.add(new WallSlot(new Point(x, y), CrusaderEngine.Facing.NORTH));
		}
		if (getGrid(grid, x, y + 1) == WALL)
		{
			out.add(new WallSlot(new Point(x, y), CrusaderEngine.Facing.SOUTH));
		}
		if (getGrid(grid, x - 1, y) == WALL)
		{
			out.add(new WallSlot(new Point(x, y), CrusaderEngine.Facing.WEST));
		}
		if (getGrid(grid, x + 1, y) == WALL)
		{
			out.add(new WallSlot(new Point(x, y), CrusaderEngine.Facing.EAST));
		}
	}

	/*-------------------------------------------------------------------------*/
	private static int getGrid(Grid grid, int x, int y)
	{
		return (int)(grid.get(x, y) * 10);
	}

	/*-------------------------------------------------------------------------*/
	private static void placeChest(
		Zone zone,
		WallSlot slot,
		String lootTableName,
		String mazeVar,
		int depth)
	{
		MazeScript contents = new MazeScript(
			"temple.generated.chest",
			List.of(new TempleChestLootEvent(lootTableName)));

		ChestTextures tex = ChestTextures.forWallFacing(slot.wallFacing());
		Chest chest = new Chest(
			contents,
			trapTable(depth),
			mazeVar,
			tex.north,
			tex.south,
			tex.east,
			tex.west,
			null);
		chest.setObjectPlacement(placementForWall(slot.wallFacing()));
		zone.getTile(slot.tile()).getScripts().add(chest);
	}

	/*-------------------------------------------------------------------------*/
	private static PercentageTable<Trap> trapTable(int depth)
	{
		int band = TempleDepthScaler.contentBand(depth);
		List<Trap> traps = new ArrayList<>();
		List<Integer> weights = new ArrayList<>();
		addTrapIfPresent(traps, weights, "0 - Daggers", 50);
		if (band >= 2)
		{
			addTrapIfPresent(traps, weights, "1 - Glowing Curse", 25);
			addTrapIfPresent(traps, weights, "0 - Fire Trap", 25);
		}
		if (traps.isEmpty())
		{
			return new PercentageTable<>(false);
		}
		return new PercentageTable<>(
			traps.toArray(new Trap[0]),
			weights.toArray(new Integer[0]),
			false);
	}

	/*-------------------------------------------------------------------------*/
	private static void addTrapIfPresent(
		List<Trap> traps,
		List<Integer> weights,
		String name,
		int weight)
	{
		Trap trap = Database.getInstance().getTraps().get(name);
		if (trap != null)
		{
			traps.add(trap);
			weights.add(weight);
		}
	}

	/*-------------------------------------------------------------------------*/
	/** For tests: texture shown when the party faces the wall the chest sits against. */
	static String chestTextureFacingWall(int wallFacing)
	{
		return ChestTextures.forWallFacing(wallFacing).forDirection(wallFacing);
	}

	/*-------------------------------------------------------------------------*/
	/** For tests: texture on the side that opens into the room. */
	static String chestTextureIntoRoom(int wallFacing)
	{
		return ChestTextures.forWallFacing(wallFacing).forDirection(oppositeFacing(wallFacing));
	}

	/*-------------------------------------------------------------------------*/
	private static int oppositeFacing(int facing)
	{
		return switch (facing)
		{
			case CrusaderEngine.Facing.NORTH -> CrusaderEngine.Facing.SOUTH;
			case CrusaderEngine.Facing.SOUTH -> CrusaderEngine.Facing.NORTH;
			case CrusaderEngine.Facing.EAST -> CrusaderEngine.Facing.WEST;
			case CrusaderEngine.Facing.WEST -> CrusaderEngine.Facing.EAST;
			default -> CrusaderEngine.Facing.NORTH;
		};
	}

	/*-------------------------------------------------------------------------*/
	private static int placementForWall(int wallFacing)
	{
		return switch (wallFacing)
		{
			case CrusaderEngine.Facing.NORTH -> EngineObject.Placement.NORTH;
			case CrusaderEngine.Facing.SOUTH -> EngineObject.Placement.SOUTH;
			case CrusaderEngine.Facing.WEST -> EngineObject.Placement.WEST;
			case CrusaderEngine.Facing.EAST -> EngineObject.Placement.EAST;
			default -> EngineObject.Placement.CENTER;
		};
	}

	/*-------------------------------------------------------------------------*/
	private record WallSlot(Point tile, int wallFacing)
	{
		String encode()
		{
			return tile.x + ":" + tile.y + ":" + wallFacing;
		}

		static WallSlot decode(String key)
		{
			String[] parts = key.split(":");
			return new WallSlot(
				new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])),
				Integer.parseInt(parts[2]));
		}
	}

	/*-------------------------------------------------------------------------*/
	private record ChestTextures(String north, String south, String east, String west)
	{
		static ChestTextures forWallFacing(int wallFacing)
		{
			// Crusader picks the texture from party facing. When approaching a
			// wall chest the party faces the wall, so that direction gets FRONT
			// (lid toward the room, back against the wall).
			return switch (wallFacing)
			{
				case CrusaderEngine.Facing.NORTH -> new ChestTextures(
					"CHEST_1_FRONT", "CHEST_1_BACK", "CHEST_1_SIDE", "CHEST_1_SIDE");
				case CrusaderEngine.Facing.SOUTH -> new ChestTextures(
					"CHEST_1_BACK", "CHEST_1_FRONT", "CHEST_1_SIDE", "CHEST_1_SIDE");
				case CrusaderEngine.Facing.WEST -> new ChestTextures(
					"CHEST_1_SIDE", "CHEST_1_SIDE", "CHEST_1_BACK", "CHEST_1_FRONT");
				case CrusaderEngine.Facing.EAST -> new ChestTextures(
					"CHEST_1_SIDE", "CHEST_1_SIDE", "CHEST_1_FRONT", "CHEST_1_BACK");
				default -> new ChestTextures(
					"CHEST_1_FRONT", "CHEST_1_BACK", "CHEST_1_SIDE", "CHEST_1_SIDE");
			};
		}

		String forDirection(int facing)
		{
			return switch (facing)
			{
				case CrusaderEngine.Facing.NORTH -> north;
				case CrusaderEngine.Facing.SOUTH -> south;
				case CrusaderEngine.Facing.EAST -> east;
				case CrusaderEngine.Facing.WEST -> west;
				default -> north;
			};
		}
	}
}
