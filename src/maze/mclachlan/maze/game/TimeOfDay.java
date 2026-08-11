/*
 * Copyright (c) 2011 Alan McLachlan
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

package mclachlan.maze.game;

/**
 * Discrete phases of the in-game day ({@link GameTime#TURNS_PER_DAY} turns).
 * Turn 0 is morning; bands are fixed for logic that keys off time of day.
 */
public enum TimeOfDay
{
	DAWN,
	DAY,
	DUSK,
	NIGHT;

	/** Last turn-of-day (inclusive) in the dawn band. */
	public static final long DAWN_END = 36;

	/** Last turn-of-day (inclusive) in the day band. */
	public static final long DAY_END = 186;

	/** Last turn-of-day (inclusive) in the dusk band. */
	public static final long DUSK_END = 224;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param turnOfDay
	 * 	{@link GameTime#getTurnOfDay(long)} value, 0 .. TURNS_PER_DAY-1
	 */
	public static TimeOfDay fromTurnOfDay(long turnOfDay)
	{
		if (turnOfDay <= DAWN_END)
		{
			return DAWN;
		}
		if (turnOfDay <= DAY_END)
		{
			return DAY;
		}
		if (turnOfDay <= DUSK_END)
		{
			return DUSK;
		}
		return NIGHT;
	}
}
