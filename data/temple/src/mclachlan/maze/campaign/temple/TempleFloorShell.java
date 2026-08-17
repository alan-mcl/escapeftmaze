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
import mclachlan.crusader.Map;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.StatModifier;

/**
 * Shrinks the {@code temple.1} palette shell before Noise4j generation for faster testing.
 */
public final class TempleFloorShell
{
	/** Odd size friendly to Noise4j; raise when full 31×31 floors are desired. */
	public static final int GEN_SIZE = 15;

	private TempleFloorShell()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void ensureGenSize(Zone zone)
	{
		ensureGenSize(zone, GEN_SIZE);
	}

	/*-------------------------------------------------------------------------*/
	public static void ensureGenSize(Zone zone, int size)
	{
		if (zone.getWidth() == size && zone.getLength() == size)
		{
			return;
		}

		Map oldMap = zone.getMap();
		Tile prototype = zone.getTile(new Point(1, 1));
		if (prototype == null)
		{
			prototype = zone.getTile(new Point(0, 0));
		}

		Tile[][] logicTiles = new Tile[size][size];
		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				logicTiles[x][y] = clonePaletteTile(prototype, x, y, zone.getName());
			}
		}

		mclachlan.crusader.Tile protoCt = oldMap.getTiles()[0];
		mclachlan.crusader.Tile[] crusaderTiles = new mclachlan.crusader.Tile[size * size];
		for (int i = 0; i < crusaderTiles.length; i++)
		{
			crusaderTiles[i] = new mclachlan.crusader.Tile(
				protoCt.getCeilingTexture(),
				protoCt.getFloorTexture(),
				protoCt.getLightLevel());
		}

		Wall[] horiz = new Wall[size * size];
		Wall[] vert = new Wall[(size + 1) * size];
		Wall empty = new Wall(
			new Texture[]{Map.NO_WALL},
			null,
			false,
			false,
			1,
			null,
			null,
			null);
		Arrays.fill(horiz, empty);
		Arrays.fill(vert, empty);

		Map newMap = new Map(
			size,
			size,
			oldMap.getBaseImageSize(),
			crusaderTiles,
			oldMap.getTextures(),
			horiz,
			vert,
			oldMap.getSkyConfigs(),
			oldMap.getExpandedObjects(),
			oldMap.getScripts());
		newMap.init();

		zone.setTiles(logicTiles);
		zone.setMap(newMap);
		zone.setWidth(size);
		zone.setLength(size);
		zone.setPortals(new mclachlan.maze.map.Portal[0]);
	}

	/*-------------------------------------------------------------------------*/
	private static Tile clonePaletteTile(Tile prototype, int x, int y, String zoneName)
	{
		Tile tile = new Tile();
		tile.setCoords(new Point(x, y));
		tile.setZone(zoneName);
		if (prototype != null)
		{
			tile.setTerrainType(prototype.getTerrainType());
			tile.setTerrainSubType(prototype.getTerrainSubType());
			if (prototype.getStatModifier() != null)
			{
				tile.setStatModifier(new StatModifier(prototype.getStatModifier()));
			}
			else
			{
				tile.setStatModifier(prototype.getStatModifier());
			}
			tile.setRandomEncounterChance(prototype.getRandomEncounterChance());
			tile.setRestingDanger(prototype.getRestingDanger());
			tile.setRestingEfficiency(prototype.getRestingEfficiency());
		}
		tile.setScripts(new ArrayList<>());
		return tile;
	}
}
