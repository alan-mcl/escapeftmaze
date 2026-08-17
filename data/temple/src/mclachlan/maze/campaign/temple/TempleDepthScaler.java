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

/**
 * Maps delve depth to content bands and table names. Orthogonal to inherited
 * Easy/Normal/Hard/Heroic {@code DifficultyLevel}. Deeper than the highest
 * authored band soft-caps (endless delve uses the top band until Phase 5).
 */
public final class TempleDepthScaler
{
	/** Highest authored encounter/loot band under {@code data/temple/db}. */
	public static final int MAX_CONTENT_BAND = 4;

	/** Deepest Noise4j crawl floor with a down stair (boss at 5+ uses other gens). */
	public static final int PLAYABLE_MAX_DEPTH = 4;

	/** Authoring range for ambient tile magic on procedural temple floors. */
	public static final int MAX_TILE_MAGIC = 8;

	private TempleDepthScaler()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Soft-clamped content band for tables and dressing density.
	 * Depth 1→1, 2→2, …, 4+→{@link #MAX_CONTENT_BAND}.
	 */
	public static int contentBand(int depth)
	{
		return Math.min(MAX_CONTENT_BAND, Math.max(1, depth));
	}

	/*-------------------------------------------------------------------------*/
	/** Whether procedural gen should place down stairs at this depth. */
	public static boolean hasDownStairs(int depth)
	{
		return depth > 0 && depth < PLAYABLE_MAX_DEPTH;
	}

	/*-------------------------------------------------------------------------*/
	public static String encounterTableName(int depth)
	{
		return "temple.depth." + contentBand(depth);
	}

	/*-------------------------------------------------------------------------*/
	public static String lootTableName(int depth)
	{
		return encounterTableName(depth) + ".loot";
	}

	/*-------------------------------------------------------------------------*/
	/** How many once-only loot scripts to dress onto a floor. */
	public static int lootPlacements(int depth)
	{
		return 1 + contentBand(depth); // 2 / 3 / 4 / 5
	}

	/**
	 * How many foe-entry names to pick from the depth band pool for this run.
	 * Depth N is aimed at party level N; bands soft-cap at {@link #MAX_CONTENT_BAND}.
	 */
	public static int foeSubsetSize(int depth)
	{
		return switch (contentBand(depth))
		{
			case 1 -> 3;
			case 2 -> 3;
			default -> 4;
		};
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Scout difficulty for hidden storage loot on procedural floors. Depth N
	 * targets party level N ({@code SCOUTING >= difficulty} spots the stash).
	 */
	public static int scoutSecretDifficulty(int depth)
	{
		return Math.max(1, depth);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Mean ambient magic per colour on general procedural floor tiles.
	 * Depth 1→1, 2→2, …, 4+→{@link #MAX_CONTENT_BAND}.
	 */
	public static int meanTileMagic(int depth)
	{
		return contentBand(depth);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Jittered ambient magic amount for one colour: mean ±1, clamped to
	 * {@link #MAX_TILE_MAGIC}.
	 */
	public static int rollTileMagicAmount(int mean, java.util.Random random)
	{
		int jitter = random.nextInt(3) - 1;
		return Math.max(0, Math.min(MAX_TILE_MAGIC, mean + jitter));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Asymptotic packing hint (1.0 at depth 1 → approaches ~1.5). Used for
	 * docs/harness; encounter tables themselves carry pack size for Phase 3.
	 */
	public static double foePackMultiplier(int depth)
	{
		int d = Math.max(1, depth);
		return 1.0 + 0.5 * (1.0 - 1.0 / d);
	}
}
