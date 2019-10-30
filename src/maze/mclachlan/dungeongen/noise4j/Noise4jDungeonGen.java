package mclachlan.dungeongen.noise4j;

import java.util.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.DungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.dungeongen.noise4j.map.generator.room.dungeon.DungeonGenerator;
import mclachlan.dungeongen.noise4j.map.generator.util.Generators;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class Noise4jDungeonGen implements DungeonGen
{
	@Override
	public Zone generate(Zone base, long seed)
	{
		Map baseMap = base.getMap();
		int width = baseMap.getWidth();
		int length = baseMap.getLength();

		Generators.setRandom(new Random(seed));
		DungeonGenerator dg = new DungeonGenerator();

		final Grid grid = new Grid(width, length); // This algorithm likes odd-sized maps, although it works either way.

		dg.setRoomGenerationAttempts(500);
		dg.setMaxRoomSize(7);
		dg.setTolerance(3); // Max difference between width and height.
		dg.setMinRoomSize(3);
		dg.generate(grid);

		Map map = new Map(
			length,
			width,
			baseMap.getBaseImageSize(),
			baseMap.getSkyTextureIndex(),
			baseMap.getSkyTextureType(),
			baseMap.getTiles(),
			baseMap.getTextures(),
			baseMap.getHorizontalWalls(),
			baseMap.getVerticalWalls(),
			baseMap.getObjects(),
			baseMap.getScripts());

		Tile[] crusaderTiles = new Tile[width*length];
		Wall[] horizWalls = new Wall[baseMap.getHorizontalWalls().length];
		Wall[] vertWalls = new Wall[baseMap.getVerticalWalls().length];

		MazeTexture wallTexture = Database.getInstance().getMazeTexture("DUNGEON_WALL_1");
		initWalls(grid, width, length, horizWalls, vertWalls, wallTexture);

		map.setHorizontalWalls(horizWalls);
		map.setVerticalWalls(vertWalls);

		base.setMap(map);

		return base;
	}

	private void initWalls(
		Grid grid,
		int width,
		int length,
		Wall[] horizWalls,
		Wall[] vertWalls,
		MazeTexture wallTexture)
	{
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

		for (int x=1; x<width-1; x++)
		{
			for (int y=1; y<length-1; y++)
			{
				// boundary walls
				if (x == 1)
				{
					vertWalls[x +y*(width+1)] = getWall(wallTexture);
				}
				else if (x == width - 2)
				{
					vertWalls[x +y*(width+1) +1] = getWall(wallTexture);
				}

				if (y == 1)
				{
					horizWalls[x +y*width] = getWall(wallTexture);
				}
				else if (y == length - 2)
				{
					horizWalls[x +(y+1)*width] = getWall(wallTexture);
				}

				// draw the room/passage walls

				if (grid.get(x,y) == 1)
				{
					// this is a room/passage tile

					if (grid.get(x,y-1) < 1)
					{
						horizWalls[x +y*width] = getWall(wallTexture);
					}

					if (grid.get(x,y+1) < 1)
					{
						horizWalls[x +(y+1)*width] = getWall(wallTexture);
					}

					if (grid.get(x-1,y) <1)
					{
						vertWalls[x +y*(width+1)] = getWall(wallTexture);
					}

					if (grid.get(x+1, y) <1)
					{
						vertWalls[x +y*(width+1) +1] = getWall(wallTexture);
					}
				}
			}
		}
	}

	private Wall getWall(MazeTexture wallTexture)
	{
		return new Wall(
			wallTexture.getTexture(),
			null,
			true,
			true,
			1,
			null,
			null);
	}
}
