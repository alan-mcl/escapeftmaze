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

import java.util.*;
import mclachlan.dungeongen.fragment.FragmentCatalog;
import mclachlan.maze.game.MazeVariables;

/**
 * Persist-once layout theme for fragment-assembly floors (barracks, worship,
 * sanctum, arena). Orthogonal to {@link TempleUsageTheme} Noise4j dressing.
 */
public final class TempleLayoutUsageTheme
{
	public static final String LAYOUT_USAGE_PURPOSE = "layout.usage";

	private final Theme theme;

	TempleLayoutUsageTheme(Theme theme)
	{
		this.theme = theme;
	}

	/*-------------------------------------------------------------------------*/
	public static TempleLayoutUsageTheme forFloor(int depth)
	{
		String var = TempleSeededPicks.pickVar(depth, LAYOUT_USAGE_PURPOSE);
		String existing = MazeVariables.get(var);
		if (existing != null && !existing.isEmpty())
		{
			return new TempleLayoutUsageTheme(Theme.fromId(existing));
		}

		Theme picked = rollTheme(TempleSeededPicks.rng(depth, LAYOUT_USAGE_PURPOSE));
		MazeVariables.set(var, picked.id());
		return new TempleLayoutUsageTheme(picked);
	}

	/*-------------------------------------------------------------------------*/
	public Theme theme()
	{
		return theme;
	}

	/*-------------------------------------------------------------------------*/
	public String usageId()
	{
		return theme.id();
	}

	/*-------------------------------------------------------------------------*/
	static Theme rollTheme(Random random)
	{
		List<String> available = availableUsageIds();
		if (available.isEmpty())
		{
			return Theme.BARRACKS;
		}
		return Theme.fromId(available.get(random.nextInt(available.size())));
	}

	/*-------------------------------------------------------------------------*/
	static List<String> availableUsageIds()
	{
		List<String> usages = FragmentCatalog.usageIds();
		if (!usages.isEmpty())
		{
			return usages;
		}
		return List.of(Theme.BARRACKS.id());
	}

	/*-------------------------------------------------------------------------*/
	public enum Theme
	{
		BARRACKS("barracks"),
		WORSHIP("worship"),
		SANCTUM("sanctum"),
		ARENA("arena");

		private final String id;

		Theme(String id)
		{
			this.id = id;
		}

		public String id()
		{
			return id;
		}

		static Theme fromId(String id)
		{
			for (Theme theme : values())
			{
				if (theme.id.equals(id))
				{
					return theme;
				}
			}
			return BARRACKS;
		}
	}
}
