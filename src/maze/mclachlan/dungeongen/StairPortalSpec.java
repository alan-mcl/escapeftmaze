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
import mclachlan.crusader.CrusaderEngine;

/**
 * One stair portal: party stands on {@link #from()}, steps {@link #fromFacing()}
 * into a masked wall toward {@link #to()}.
 */
public record StairPortalSpec(
	Point from,
	int fromFacing,
	Point to,
	int toFacing,
	boolean horizontalWall,
	StairMask mask)
{
	public enum StairMask
	{
		STAIR_UP("DUNGEON_STAIR_UP"),
		STAIR_DOWN("DUNGEON_STAIR_DOWN");

		private final String textureName;

		StairMask(String textureName)
		{
			this.textureName = textureName;
		}

		public String textureName()
		{
			return textureName;
		}
	}

	/*-------------------------------------------------------------------------*/
	/** Facing when arriving on {@link #from()}: away from the stair-mask wall. */
	public int spawnFacing()
	{
		return oppositeFacing(fromFacing);
	}

	/*-------------------------------------------------------------------------*/
	public static int oppositeFacing(int facing)
	{
		return switch (facing)
		{
			case CrusaderEngine.Facing.NORTH -> CrusaderEngine.Facing.SOUTH;
			case CrusaderEngine.Facing.SOUTH -> CrusaderEngine.Facing.NORTH;
			case CrusaderEngine.Facing.EAST -> CrusaderEngine.Facing.WEST;
			case CrusaderEngine.Facing.WEST -> CrusaderEngine.Facing.EAST;
			default -> facing;
		};
	}

	/*-------------------------------------------------------------------------*/
	/** Compact persisted form: fromX:fromY:fromF:toX:toY:toF:h|v:up|down */
	public String encode()
	{
		return from.x + ":" + from.y + ":" + fromFacing + ":"
			+ to.x + ":" + to.y + ":" + toFacing + ":"
			+ (horizontalWall ? "h" : "v") + ":"
			+ (mask == StairMask.STAIR_UP ? "up" : "down");
	}

	/*-------------------------------------------------------------------------*/
	public static StairPortalSpec decode(String encoded)
	{
		if (encoded == null || encoded.isEmpty())
		{
			return null;
		}
		String[] parts = encoded.split(":");
		if (parts.length != 8)
		{
			throw new IllegalArgumentException("Bad stair portal encoding [" + encoded + "]");
		}
		return new StairPortalSpec(
			new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])),
			Integer.parseInt(parts[2]),
			new Point(Integer.parseInt(parts[3]), Integer.parseInt(parts[4])),
			Integer.parseInt(parts[5]),
			"h".equals(parts[6]),
			"up".equals(parts[7]) ? StairMask.STAIR_UP : StairMask.STAIR_DOWN);
	}
}
