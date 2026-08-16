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

/**
 * Axis-aligned room rectangle from procedural layout (Noise4j and similar).
 */
public final class DungeonRoom
{
	private final int x;
	private final int y;
	private final int width;
	private final int height;

	public DungeonRoom(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public boolean contains(int tileX, int tileY)
	{
		return tileX >= x && tileX < x + width && tileY >= y && tileY < y + height;
	}

	public int centerX()
	{
		return x + width / 2;
	}

	public int centerY()
	{
		return y + height / 2;
	}
}
