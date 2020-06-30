package mclachlan.dungeongen.noise4j;

import java.util.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.DungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.dungeongen.noise4j.map.generator.room.dungeon.DungeonGenerator;
import mclachlan.dungeongen.noise4j.map.generator.util.Generators;
import mclachlan.maze.map.MapGenZoneScript;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class Noise4jDungeonGen implements DungeonGen
{
	public static final int WALL_THRESHOLD = 10;
	public static final int ROOM_THRESHOLD = 5;
	public static final int CORRIDOR_THRESHOLD = 1;

	@Override
	public Zone generate(Zone base, long seed,
		MapGenZoneScript.DungeonDecorator decorator)
	{
		Map baseMap = base.getMap();
		int width = baseMap.getWidth();
		int length = baseMap.getLength();

		Generators.setRandom(new Random(seed));
		DungeonGenerator dg = new DungeonGenerator();

		// This algorithm likes odd-sized maps, although it works either way.
		final Grid grid = new Grid(width, length);

		dg.setWallThreshold(WALL_THRESHOLD / 10F);
		dg.setCorridorThreshold(CORRIDOR_THRESHOLD / 10F);
		dg.setFloorThreshold(ROOM_THRESHOLD / 10F);

		dg.setRoomGenerationAttempts(500);
		dg.setMaxRoomSize(7);
		dg.setTolerance(3); // Max difference between width and height.
		dg.setMinRoomSize(3);

		dg.generate(grid);

		Tile[] crusaderTiles = new Tile[width * length];
		Wall[] horizWalls = new Wall[baseMap.getHorizontalWalls().length];
		Wall[] vertWalls = new Wall[baseMap.getVerticalWalls().length];

		initWalls(grid, horizWalls, vertWalls, decorator);
		initDoors(grid, horizWalls, vertWalls, decorator);

		Tile[] baseTiles = baseMap.getTiles();
		for (int i = 0; i < crusaderTiles.length; i++)
		{
			crusaderTiles[i] = new Tile(
				baseTiles[i].getCeilingTexture(),
				baseTiles[i].getFloorTexture(),
				baseTiles[i].getLightLevel());
		}

		java.util.Map<String, Texture> textures = new HashMap<>();
		for (int i = 0; i < crusaderTiles.length; i++)
		{
			addTexture(textures, crusaderTiles[i].getFloorTexture());
			addTexture(textures, crusaderTiles[i].getFloorMaskTexture());
			addTexture(textures, crusaderTiles[i].getCeilingTexture());
			addTexture(textures, crusaderTiles[i].getCeilingMaskTexture());
			addTexture(textures, crusaderTiles[i].getEastWallTexture());
			addTexture(textures, crusaderTiles[i].getWestWallTexture());
			addTexture(textures, crusaderTiles[i].getNorthWallTexture());
			addTexture(textures, crusaderTiles[i].getSouthWallTexture());
		}
		for (int i = 0; i < horizWalls.length; i++)
		{
			addTexture(textures, horizWalls[i].getTexture());
			addTexture(textures, horizWalls[i].getMaskTexture());
		}
		for (int i = 0; i < vertWalls.length; i++)
		{
			addTexture(textures, vertWalls[i].getTexture());
			addTexture(textures, vertWalls[i].getMaskTexture());
		}

		Map map = new Map(
			length,
			width,
			baseMap.getBaseImageSize(),
			baseMap.getSkyTextureIndex(),
			baseMap.getSkyTextureType(),
			baseMap.getTiles(),
//			crusaderTiles,
			textures.values().toArray(new Texture[]{}),
			horizWalls,
			vertWalls,
			baseMap.getObjects(),
			baseMap.getScripts());

		map.setHorizontalWalls(horizWalls);
		map.setVerticalWalls(vertWalls);

		base.setMap(map);

		return base;
	}

	private void initDoors(Grid grid, Wall[] horizWalls, Wall[] vertWalls,
		MapGenZoneScript.DungeonDecorator decorator)
	{
		int width = grid.getWidth();
		int height = grid.getHeight();

		// Find spots to put doors
		for (int x = 1; x < width - 1; x++)
		{
			for (int y = 1; y < height - 1; y++)
			{
				if (getGrid(grid, x, y) == ROOM_THRESHOLD)
				{
					if (getGrid(grid, x, y - 1) == CORRIDOR_THRESHOLD)
					{
						horizWalls[x + y * width] = decorator.getPortal(grid, x, y);
					}

					if (getGrid(grid, x, y + 1) == CORRIDOR_THRESHOLD)
					{
						horizWalls[x + (y + 1) * width] = decorator.getPortal(grid, x, y);
					}

					if (getGrid(grid, x - 1, y) == CORRIDOR_THRESHOLD)
					{
						vertWalls[x + y * (width + 1)] = decorator.getPortal(grid, x, y);
					}

					if (getGrid(grid, x + 1, y) == CORRIDOR_THRESHOLD)
					{
						vertWalls[x + y * (width + 1) + 1] = decorator.getPortal(grid, x, y);
					}
				}
			}
		}
	}

	private void initWalls(
		Grid grid,
		Wall[] horizWalls,
		Wall[] vertWalls,
		MapGenZoneScript.DungeonDecorator decorator)
	{
		// init everything to NO_WALL
		for (int i = 0; i < vertWalls.length; i++)
		{
			vertWalls[i] = new Wall(
				Map.NO_WALL,
				null,
				false,
				false,
				1,
				null,
				null);
		}

		for (int i = 0; i < horizWalls.length; i++)
		{
			horizWalls[i] = new Wall(
				Map.NO_WALL,
				null,
				false,
				false,
				1,
				null,
				null);
		}

		int width = grid.getWidth();
		int height = grid.getHeight();

		// set the generated walls
		for (int x = 1; x < width - 1; x++)
		{
			for (int y = 1; y < height - 1; y++)
			{
				// draw boundary walls

				if (x == 1)
				{
					vertWalls[x + y * (width + 1)] = decorator.getRoomWall(grid, x, y);
				}
				else if (x == width - 2)
				{
					vertWalls[x + y * (width + 1) + 1] = decorator.getRoomWall(grid, x, y);
				}

				if (y == 1)
				{
					horizWalls[x + y * width] = decorator.getRoomWall(grid, x, y);
				}
				else if (y == height - 2)
				{
					horizWalls[x + (y + 1) * width] = decorator.getRoomWall(grid, x, y);
				}

				// draw the room/passage walls

				boolean isRoom = getGrid(grid, x, y) == ROOM_THRESHOLD;
				boolean isCorridor = getGrid(grid, x, y) == CORRIDOR_THRESHOLD;

				if (isRoom || isCorridor)
				{
					// this is a room/passage tile

					if (getGrid(grid, x, y - 1) == WALL_THRESHOLD)
					{
						horizWalls[x + y * width] = isRoom ?
							decorator.getRoomWall(grid, x, y) :
							decorator.getCorridorWall(grid, x, y);
					}

					if (getGrid(grid, x, y + 1) == WALL_THRESHOLD)
					{
						horizWalls[x + (y + 1) * width] = isRoom ?
							decorator.getRoomWall(grid, x, y) :
							decorator.getCorridorWall(grid, x, y);
					}

					if (getGrid(grid, x - 1, y) == WALL_THRESHOLD)
					{
						vertWalls[x + y * (width + 1)] = isRoom ?
							decorator.getRoomWall(grid, x, y) :
							decorator.getCorridorWall(grid, x, y);
					}

					if (getGrid(grid, x + 1, y) == WALL_THRESHOLD)
					{
						vertWalls[x + y * (width + 1) + 1] = isRoom ?
							decorator.getRoomWall(grid, x, y) :
							decorator.getCorridorWall(grid, x, y);
					}
				}
			}
		}
	}

	private int getGrid(Grid grid, int x, int y)
	{
		return (int)(grid.get(x, y) * 10);
	}

	public void addTexture(java.util.Map<String, Texture> textures, Texture t)
	{
		if (t != null && t != Map.NO_WALL)
		{
			textures.put(t.getName(), t);
		}
	}
}
