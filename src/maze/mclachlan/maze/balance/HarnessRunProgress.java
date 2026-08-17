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

package mclachlan.maze.balance;

/**
 * Optional stdout progress for headless dungeon runs. Enabled by
 * {@link mclachlan.maze.campaign.temple.TempleRunDriver} (and similar drivers).
 */
public final class HarnessRunProgress
{
	private static boolean enabled;

	private HarnessRunProgress()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void setEnabled(boolean on)
	{
		enabled = on;
	}

	/*-------------------------------------------------------------------------*/
	public static boolean isEnabled()
	{
		return enabled;
	}

	/*-------------------------------------------------------------------------*/
	public static void line(String message)
	{
		if (!enabled)
		{
			return;
		}
		System.out.println(message);
		System.out.flush();
	}

	/*-------------------------------------------------------------------------*/
	public static void line(String format, Object... args)
	{
		if (!enabled)
		{
			return;
		}
		line(String.format(format, args));
	}
}
