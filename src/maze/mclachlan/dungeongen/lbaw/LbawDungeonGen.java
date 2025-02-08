package mclachlan.dungeongen.lbaw;


import java.util.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.DungeonGen;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.MapGenZoneScript;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class LbawDungeonGen implements DungeonGen
{
	@Override
	public List<MazeEvent> generate(Zone base, long seed, int dungeonLevel,
		MapGenZoneScript.DungeonDecorator decorator)
	{
		MapBuilder mb = new MapBuilder(seed);

		Map baseMap = base.getMap();
		int width = baseMap.getWidth();
		int length = baseMap.getLength();

		int[][] grid = mb.buildMap(width, length);

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



//		map.setTiles(crusaderTiles);
		map.setHorizontalWalls(horizWalls);
		map.setVerticalWalls(vertWalls);

		base.setMap(map);

		return null;
	}

	private void initWalls(
		int[][] grid,
		int width,
		int length,
		Wall[] horizWalls,
		Wall[] vertWalls,
		MazeTexture wallTexture)
	{
		for (int i = 0; i < vertWalls.length; i++)
		{
			vertWalls[i] = new Wall(
				new Texture[]{Map.NO_WALL},
				null,
				false,
				false,
				1,
				null,
				null,
				null);
		}

		for (int i = 0; i < horizWalls.length; i++)
		{
			horizWalls[i] = new Wall(
				new Texture[]{Map.NO_WALL},
				null,
				false,
				false,
				1,
				null,
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

				if (grid[x][y] == 1)
				{
					// this is a room/passage tile

					if (grid[x][y-1] == 0)
					{
						horizWalls[x +y*width] = getWall(wallTexture);
					}

					if (grid[x][y+1] == 0)
					{
						horizWalls[x +(y+1)*width] = getWall(wallTexture);
					}

					if (grid[x-1][y] == 0)
					{
						vertWalls[x +y*(width+1)] = getWall(wallTexture);
					}

					if (grid[x+1][y] == 0)
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
			new Texture[]{wallTexture.getTexture()},
			null,
			true,
			true,
			1,
			null,
			null,
			null);
	}
}
