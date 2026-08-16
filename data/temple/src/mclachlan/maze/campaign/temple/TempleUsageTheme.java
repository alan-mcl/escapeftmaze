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
import mclachlan.maze.game.MazeVariables;

/**
 * Persist-once usage theme for a generated Noise4j floor (storage, library,
 * mystery, garden, or mixed per-room). Orthogonal to {@link TempleEnvironment}
 * palette and future fragment-assembled signature levels.
 */
public final class TempleUsageTheme
{
	public static final String USAGE_PURPOSE = "usage";
	private static final String ROOM_PURPOSE_PREFIX = "usage.room.";

	private final Theme floorTheme;
	private final TempleEnvironment.Palette palette;

	TempleUsageTheme(Theme floorTheme, TempleEnvironment.Palette palette)
	{
		this.floorTheme = floorTheme;
		this.palette = palette;
	}

	/*-------------------------------------------------------------------------*/
	public static TempleUsageTheme forFloor(int depth, TempleEnvironment environment)
	{
		TempleEnvironment.Palette palette = environment.validatedPalette();
		String var = TempleSeededPicks.pickVar(depth, USAGE_PURPOSE);
		String existing = MazeVariables.get(var);
		if (existing != null && !existing.isEmpty())
		{
			return new TempleUsageTheme(Theme.fromId(existing), palette);
		}

		Theme picked = rollFloorTheme(TempleSeededPicks.rng(depth, USAGE_PURPOSE), palette);
		MazeVariables.set(var, picked.id());
		return new TempleUsageTheme(picked, palette);
	}

	/*-------------------------------------------------------------------------*/
	public Theme floorTheme()
	{
		return floorTheme;
	}

	/*-------------------------------------------------------------------------*/
	/** Effective theme for a room (resolves {@link Theme#MIXED} per room). */
	public Theme themeForRoom(int depth, int roomIndex)
	{
		if (floorTheme != Theme.MIXED)
		{
			return floorTheme;
		}

		List<Theme> legal = legalThemes(palette);
		legal.remove(Theme.MIXED);
		if (legal.isEmpty())
		{
			return Theme.STORAGE;
		}

		return TempleSeededPicks.pickOneAndRemember(
			depth,
			ROOM_PURPOSE_PREFIX + roomIndex,
			legal,
			Theme::id,
			id -> Theme.fromId(id));
	}

	/*-------------------------------------------------------------------------*/
	static Theme rollFloorTheme(Random random, TempleEnvironment.Palette palette)
	{
		List<Theme> themes = new ArrayList<>();
		List<Integer> weights = new ArrayList<>();
		for (Theme theme : Theme.values())
		{
			int weight = theme.weightFor(palette);
			if (weight > 0)
			{
				themes.add(theme);
				weights.add(weight);
			}
		}
		if (themes.isEmpty())
		{
			return Theme.STORAGE;
		}

		int total = 0;
		for (int w : weights)
		{
			total += w;
		}
		int roll = random.nextInt(total);
		int acc = 0;
		for (int i = 0; i < themes.size(); i++)
		{
			acc += weights.get(i);
			if (roll < acc)
			{
				return themes.get(i);
			}
		}
		return themes.get(themes.size() - 1);
	}

	/*-------------------------------------------------------------------------*/
	static List<Theme> legalThemes(TempleEnvironment.Palette palette)
	{
		List<Theme> result = new ArrayList<>();
		for (Theme theme : Theme.values())
		{
			if (theme.weightFor(palette) > 0)
			{
				result.add(theme);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public enum Theme
	{
		STORAGE("storage", 40),
		LIBRARY("library", 15),
		MYSTERY("mystery", 20),
		GARDEN("garden", 25),
		MIXED("mixed", 25);

		private final String id;
		private final int baseWeight;

		Theme(String id, int baseWeight)
		{
			this.id = id;
			this.baseWeight = baseWeight;
		}

		public String id()
		{
			return id;
		}

		int weightFor(TempleEnvironment.Palette palette)
		{
			if (this == LIBRARY && palette == TempleEnvironment.Palette.DIRT)
			{
				return 0;
			}
			if (this == GARDEN && palette != TempleEnvironment.Palette.DIRT)
			{
				return 0;
			}
			int weight = baseWeight;
			if (this == GARDEN && palette == TempleEnvironment.Palette.DIRT)
			{
				weight += LIBRARY.baseWeight;
			}
			return weight;
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
			return STORAGE;
		}
	}
}
