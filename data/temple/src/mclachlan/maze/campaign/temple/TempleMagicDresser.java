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
import java.util.Random;
import mclachlan.dungeongen.DungeonGenResult;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.MagicSys;

/**
 * Post-processes generated temple floors with ambient colour magic on every
 * walkable tile. General tiles jitter all seven colours around a depth mean;
 * set-piece overrides are out of scope here.
 */
public final class TempleMagicDresser
{
	public static final String MAGIC_PURPOSE = "magic";

	private static final int ROOM = Noise4jDungeonGen.ROOM_THRESHOLD;
	private static final int CORRIDOR = Noise4jDungeonGen.CORRIDOR_THRESHOLD;

	private TempleMagicDresser()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void dress(Zone zone, int depth, DungeonGenResult layout)
	{
		if (zone == null || layout == null || layout.layoutGrid() == null)
		{
			return;
		}

		Grid grid = layout.layoutGrid();
		Random random = TempleSeededPicks.rng(depth, MAGIC_PURPOSE);
		int mean = TempleDepthScaler.meanTileMagic(depth);

		for (int x = 0; x < grid.getWidth(); x++)
		{
			for (int y = 0; y < grid.getHeight(); y++)
			{
				int cell = getGrid(grid, x, y);
				if (cell != ROOM && cell != CORRIDOR)
				{
					continue;
				}

				Tile tile = zone.getTile(new Point(x, y));
				if (tile == null)
				{
					continue;
				}

				for (int colour = MagicSys.MagicColour.RED;
					colour <= MagicSys.MagicColour.BLUE;
					colour++)
				{
					Stats.Modifier modifier = MagicSys.MagicColour.getModifier(colour);
					int amount = TempleDepthScaler.rollTileMagicAmount(mean, random);
					tile.getStatModifier().setModifier(modifier, amount);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static int getGrid(Grid grid, int x, int y)
	{
		return Math.round(grid.get(x, y) * 10F);
	}
}
