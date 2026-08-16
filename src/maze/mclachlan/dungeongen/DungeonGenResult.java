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

package mclachlan.dungeongen;

import java.awt.Point;
import java.util.*;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.game.MazeEvent;

/**
 * Output of {@link DungeonGen#generate}: layout events, stair plan, spawn, and
 * optional layout metadata for post-gen dressing (Noise4j today).
 */
public record DungeonGenResult(
	List<MazeEvent> events,
	StairwellPlan stairwells,
	Point playerOrigin,
	int playerFacing,
	List<DungeonRoom> rooms,
	Grid layoutGrid,
	int startingRoomIndex)
{
	public DungeonGenResult(
		List<MazeEvent> events,
		StairwellPlan stairwells,
		Point playerOrigin,
		int playerFacing)
	{
		this(events, stairwells, playerOrigin, playerFacing, List.of(), null, -1);
	}

	public static DungeonGenResult of(List<MazeEvent> events, Point origin, int facing)
	{
		return new DungeonGenResult(events, StairwellPlan.empty(), origin, facing);
	}
}
