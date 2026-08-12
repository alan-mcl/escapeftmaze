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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.ZoneChangeEvent;

/**
 * Shared depth transition used by fieldless ascend/descend MazeEvents (IMPL
 * scripts cannot rely on ReflectiveSerialiser field injection without engine
 * registration).
 */
final class TempleDepthTransition
{
	static final String FLOOR_ZONE = "temple.1";
	static final String HUB_ZONE = "Temple Hub";

	private TempleDepthTransition()
	{
	}

	static List<MazeEvent> change(int delta)
	{
		int next = TempleSeeds.getDepth() + delta;
		List<MazeEvent> result = new ArrayList<>();

		if (next < 1)
		{
			TempleSeeds.setDepth(0);
			result.add(new ZoneChangeEvent(
				HUB_ZONE,
				new Point(8, 8),
				CrusaderEngine.Facing.SOUTH));
		}
		else
		{
			TempleSeeds.setDepth(next);
			result.add(new ZoneChangeEvent(
				FLOOR_ZONE,
				new Point(-1, -1),
				delta > 0 ? CrusaderEngine.Facing.NORTH : CrusaderEngine.Facing.SOUTH));
		}

		return result;
	}
}
