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
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.FlavourText;
import mclachlan.maze.map.script.FlavourTextEvent;

/**
 * First-visit flavour text for generated temple floors, rolled from the
 * persisted {@link TempleEnvironment} and remembered per depth.
 */
public final class TempleEnvironmentFlavour
{
	public static final String FLAVOUR_PURPOSE = "flavour";

	private static final Map<TempleEnvironment.Palette, String[]> PALETTE_LINES = Map.of(
		TempleEnvironment.Palette.DUNGEON, new String[]{
			"The dungeon is dark and musty.",
			"Ancient stonework weeps with damp.",
			"Stale chill air fills the rough-hewn passages."},
		TempleEnvironment.Palette.CITY, new String[]{
			"Cut-stone passages echo softly underfoot.",
			"The masonry here feels unnervingly regular.",
			"Worked blocks line corridors built for habitation."},
		TempleEnvironment.Palette.DIRT, new String[]{
			"The floor is packed earth, damp underfoot.",
			"Roots and grit crumble from earthen walls.",
			"A raw cave smell hangs in the stagnant air."});

	private static final Map<TempleEnvironment.FogColour, String[]> FOG_LINES = Map.of(
		TempleEnvironment.FogColour.BLACK, new String[]{
			"Shadows cling to every corner.",
			"Darkness presses in beyond your light.",
			"You can barely make out the walls ahead."},
		TempleEnvironment.FogColour.GREY, new String[]{
			"A grey mist dulls the torchlight.",
			"Everything fades into ashen gloom.",
			"The air is thick with colourless haze."},
		TempleEnvironment.FogColour.WHITE, new String[]{
			"A pale luminous haze fills the air.",
			"White mist softens every edge and corner.",
			"Ghostly light diffuses through the passages."},
		TempleEnvironment.FogColour.RED, new String[]{
			"A strange red haze hangs in the air.",
			"Crimson mist stains the torchlight.",
			"The air glows with a sullen red light."});

	private static final Map<Integer, String[]> LIGHT_LINES = Map.of(
		16, new String[]{
			"Your torch barely pierces the gloom.",
			"You struggle to see more than a few steps ahead."},
		20, new String[]{
			"Flickering light casts long, uneasy shadows.",
			"The passages feel dim and close."},
		24, new String[]{
			"The passages are dimly but evenly lit.",
			"A faint ambient glow relieves the darkness."});

	private static final Map<TempleUsageTheme.Theme, String[]> USAGE_LINES = Map.of(
		TempleUsageTheme.Theme.STORAGE, new String[]{
			"Crates and barrels are stacked in the rooms.",
			"Someone used these chambers as a storeroom.",
			"Dusty goods sit abandoned among the boxes."},
		TempleUsageTheme.Theme.LIBRARY, new String[]{
			"Shelves of books line many of the walls.",
			"The air smells of old paper and dry leather.",
			"This looks like it was once a library."},
		TempleUsageTheme.Theme.MYSTERY, new String[]{
			"Strange shrines and pillars crowd the rooms.",
			"Something ritual was practiced in these halls.",
			"Odd monuments stand where furniture ought to be."},
		TempleUsageTheme.Theme.GARDEN, new String[]{
			"Rows of plants grow under the lights.",
			"Someone tended a garden in these earthen rooms.",
			"Orderly beds of greenery fill the chambers."},
		TempleUsageTheme.Theme.MIXED, new String[]{
			"Each room seems put to a different use.",
			"The chambers beyond do not share one purpose.",
			"Storage, shrines, and odd clutter mix from room to room."});

	private TempleEnvironmentFlavour()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Places a once-only {@link FlavourText} on the spawn tile so it fires from
	 * {@code encounterTile} after the zone change, not from zone-script init.
	 */
	public static void attachToLandingTile(Zone zone, int depth, TempleEnvironment environment)
	{
		attachToLandingTile(zone, depth, environment, null);
	}

	/*-------------------------------------------------------------------------*/
	public static void attachToLandingTile(
		Zone zone,
		int depth,
		TempleEnvironment environment,
		TempleUsageTheme usageTheme)
	{
		Point origin = zone.getPlayerOrigin();
		if (origin == null)
		{
			return;
		}
		Tile tile = zone.getTile(origin);
		if (tile == null)
		{
			return;
		}

		FlavourText script = new FlavourText(
			flavourText(depth, environment, usageTheme),
			FlavourTextEvent.Alignment.CENTER);
		script.setExecuteOnceMazeVariable(TempleSeeds.visitedVar(depth));
		script.setReexecuteOnSameTile(true);

		List<TileScript> scripts = tile.getScripts();
		if (scripts == null)
		{
			scripts = new ArrayList<>();
			tile.setScripts(scripts);
		}
		scripts.add(script);
	}

	/*-------------------------------------------------------------------------*/
	static String flavourText(int depth, TempleEnvironment environment)
	{
		return flavourText(depth, environment, null);
	}

	/*-------------------------------------------------------------------------*/
	static String flavourText(int depth, TempleEnvironment environment, TempleUsageTheme usageTheme)
	{
		String var = TempleSeededPicks.pickVar(depth, FLAVOUR_PURPOSE);
		String existing = MazeVariables.get(var);
		if (existing != null && !existing.isEmpty())
		{
			return existing;
		}

		String text = rollFlavourText(
			TempleSeededPicks.rng(depth, FLAVOUR_PURPOSE),
			environment,
			usageTheme);
		MazeVariables.set(var, text);
		return text;
	}

	/*-------------------------------------------------------------------------*/
	static String rollFlavourText(Random random, TempleEnvironment environment)
	{
		return rollFlavourText(random, environment, null);
	}

	/*-------------------------------------------------------------------------*/
	static String rollFlavourText(
		Random random,
		TempleEnvironment environment,
		TempleUsageTheme usageTheme)
	{
		TempleEnvironment.Palette palette = environment.palette();
		StringBuilder sb = new StringBuilder();
		sb.append(pick(random, PALETTE_LINES.get(palette)));
		sb.append(' ');
		sb.append(pick(random, FOG_LINES.get(environment.fogColour())));

		String[] lightLines = LIGHT_LINES.get(environment.ambientLight());
		if (lightLines != null && (environment.ambientLight() <= 20 || random.nextBoolean()))
		{
			sb.append(' ');
			sb.append(pick(random, lightLines));
		}

		if (usageTheme != null)
		{
			String[] usageLines = USAGE_LINES.get(usageTheme.floorTheme());
			if (usageLines != null)
			{
				sb.append(' ');
				sb.append(pick(random, usageLines));
			}
		}

		return sb.toString().trim();
	}

	/*-------------------------------------------------------------------------*/
	private static String pick(Random random, String[] lines)
	{
		return lines[random.nextInt(lines.length)];
	}
}
