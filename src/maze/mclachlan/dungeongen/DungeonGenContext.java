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

/**
 * Hints for layout generation: how the party is entering and any restored stair
 * portal coordinates from a prior visit.
 */
public final class DungeonGenContext
{
	public enum EntryMode
	{
		FRESH,
		FROM_HUB,
		FROM_ABOVE,
		FROM_BELOW
	}

	public static final DungeonGenContext FRESH = new DungeonGenContext(EntryMode.FRESH, -1, null, null, null, null);

	private final EntryMode entryMode;
	private final int sourceDepth;
	private final Point sourcePortalFrom;
	private final StairPortalSpec restoredUp;
	private final StairPortalSpec restoredDown;
	private final StairwellPlanner stairwellPlanner;

	private DungeonGenContext(
		EntryMode entryMode,
		int sourceDepth,
		Point sourcePortalFrom,
		StairPortalSpec restoredUp,
		StairPortalSpec restoredDown,
		StairwellPlanner stairwellPlanner)
	{
		this.entryMode = entryMode;
		this.sourceDepth = sourceDepth;
		this.sourcePortalFrom = sourcePortalFrom;
		this.restoredUp = restoredUp;
		this.restoredDown = restoredDown;
		this.stairwellPlanner = stairwellPlanner;
	}

	/*-------------------------------------------------------------------------*/
	public static Builder builder()
	{
		return new Builder();
	}

	public EntryMode getEntryMode()
	{
		return entryMode;
	}

	public int getSourceDepth()
	{
		return sourceDepth;
	}

	public Point getSourcePortalFrom()
	{
		return sourcePortalFrom;
	}

	public StairPortalSpec getRestoredUp()
	{
		return restoredUp;
	}

	public StairPortalSpec getRestoredDown()
	{
		return restoredDown;
	}

	public StairwellPlanner getStairwellPlanner()
	{
		return stairwellPlanner;
	}

	/*-------------------------------------------------------------------------*/
	public static final class Builder
	{
		private EntryMode entryMode = EntryMode.FRESH;
		private int sourceDepth = -1;
		private Point sourcePortalFrom;
		private StairPortalSpec restoredUp;
		private StairPortalSpec restoredDown;
		private StairwellPlanner stairwellPlanner;

		public Builder entryMode(EntryMode mode)
		{
			this.entryMode = mode;
			return this;
		}

		public Builder sourceDepth(int depth)
		{
			this.sourceDepth = depth;
			return this;
		}

		public Builder sourcePortalFrom(Point from)
		{
			this.sourcePortalFrom = from;
			return this;
		}

		public Builder restoredUp(StairPortalSpec spec)
		{
			this.restoredUp = spec;
			return this;
		}

		public Builder restoredDown(StairPortalSpec spec)
		{
			this.restoredDown = spec;
			return this;
		}

		public Builder stairwellPlanner(StairwellPlanner planner)
		{
			this.stairwellPlanner = planner;
			return this;
		}

		public DungeonGenContext build()
		{
			return new DungeonGenContext(
				entryMode, sourceDepth, sourcePortalFrom,
				restoredUp, restoredDown, stairwellPlanner);
		}
	}
}
