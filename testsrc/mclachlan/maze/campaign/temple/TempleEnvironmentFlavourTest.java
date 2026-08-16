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
import mclachlan.maze.test.support.MazeTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Theme-driven first-visit flavour lines for generated temple depths.
 */
public class TempleEnvironmentFlavourTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void rollReflectsPaletteAndFog()
	{
		TempleEnvironment dungeonRed = new TempleEnvironment(
			TempleEnvironment.Palette.DUNGEON,
			TempleEnvironment.FogColour.RED,
			0,
			0.7,
			20);
		String text = TempleEnvironmentFlavour.rollFlavourText(new Random(42), dungeonRed);
		assertTrue(text.contains("musty") || text.contains("damp") || text.contains("Stale"),
			"expected dungeon palette line: " + text);
		assertTrue(text.contains("red") || text.contains("Crimson") || text.contains("sullen"),
			"expected red fog line: " + text);

		TempleEnvironment cityGrey = new TempleEnvironment(
			TempleEnvironment.Palette.CITY,
			TempleEnvironment.FogColour.GREY,
			0,
			1.0,
			32);
		text = TempleEnvironmentFlavour.rollFlavourText(new Random(7), cityGrey);
		assertTrue(text.contains("stone") || text.contains("masonry") || text.contains("blocks"),
			"expected city palette line: " + text);
		assertTrue(text.contains("grey") || text.contains("ashen") || text.contains("colourless"),
			"expected grey fog line: " + text);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void flavourTextPersistsPerDepth()
	{
		MazeVariables.clearAll();
		TempleEnvironment env = new TempleEnvironment(
			TempleEnvironment.Palette.DIRT,
			TempleEnvironment.FogColour.WHITE,
			0,
			0.4,
			26);

		String first = TempleEnvironmentFlavour.flavourText(2, env);
		String second = TempleEnvironmentFlavour.flavourText(2, env);
		assertEquals(first, second);
		assertNotNull(MazeVariables.get(
			TempleSeededPicks.pickVar(2, TempleEnvironmentFlavour.FLAVOUR_PURPOSE)));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void attachPlacesOnceOnlyFlavourOnOriginTile()
	{
		MazeVariables.clearAll();
		TempleEnvironment env = new TempleEnvironment(
			TempleEnvironment.Palette.DUNGEON,
			TempleEnvironment.FogColour.BLACK,
			0,
			1.0,
			32);

		Tile landing = new Tile(
			new ArrayList<>(),
			null,
			new mclachlan.maze.stat.StatModifier(),
			Tile.TerrainType.DUNGEON,
			null,
			0,
			Tile.RestingDanger.NONE,
			Tile.RestingEfficiency.AVERAGE);
		Zone zone = new Zone();
		zone.setTiles(new Tile[][]{{landing}});
		zone.setPlayerOrigin(new Point(0, 0));

		TempleEnvironmentFlavour.attachToLandingTile(zone, 1, env);

		List<TileScript> scripts = landing.getScripts();
		assertEquals(1, scripts.size());
		assertInstanceOf(FlavourText.class, scripts.get(0));
		FlavourText flavour = (FlavourText)scripts.get(0);
		assertFalse(flavour.getText().isBlank());
		assertEquals(TempleSeeds.visitedVar(1), flavour.getExecuteOnceMazeVariable());
		assertTrue(flavour.isReexecuteOnSameTile());
	}
}
