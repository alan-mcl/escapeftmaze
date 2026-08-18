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

import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Zone;

/**
 * Walkability checks for assembled fragment layouts.
 */
public final class FragmentConnectivity
{
	private FragmentConnectivity()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static boolean isOpenCell(Zone zone, int x, int y)
	{
		Map map = zone.getMap();
		int width = map.getWidth();
		int length = map.getLength();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();

		if (x > 0)
		{
			Wall w = vert[x + y * (width + 1)];
			if (w == null || !w.isSolid())
			{
				return true;
			}
		}
		if (x < width - 1)
		{
			Wall w = vert[x + y * (width + 1) + 1];
			if (w == null || !w.isSolid())
			{
				return true;
			}
		}
		if (y > 0)
		{
			Wall w = horiz[x + y * width];
			if (w == null || !w.isSolid())
			{
				return true;
			}
		}
		if (y < length - 1)
		{
			Wall w = horiz[x + (y + 1) * width];
			if (w == null || !w.isSolid())
			{
				return true;
			}
		}
		return false;
	}
}
