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
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.crusader.MouseClickScriptAdapter;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.map.script.ExecuteMazeScript;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Persist-once usage themes and dressing for generated Noise4j floors.
 */
public class TempleUsageThemeTest extends MazeTestSupport
{
	private static final long RUN_SEED = 42L;

	/*-------------------------------------------------------------------------*/
	@Test
	void floorThemePickPersistsPerDepth() throws Exception
	{
		TempleCampaignHarness.bootDatabase();
		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.FLOOR_SEED_PREFIX + "3", "777");

		TempleEnvironment env = TempleEnvironment.forFloor(3);
		TempleUsageTheme first = TempleUsageTheme.forFloor(3, env);
		TempleUsageTheme second = TempleUsageTheme.forFloor(3, env);

		assertEquals(first.floorTheme(), second.floorTheme());
		assertNotNull(MazeVariables.get(
			TempleSeededPicks.pickVar(3, TempleUsageTheme.USAGE_PURPOSE)));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void dirtPaletteNeverRollsLibrary()
	{
		for (int i = 0; i < 200; i++)
		{
			TempleUsageTheme.Theme theme = TempleUsageTheme.rollFloorTheme(
				new Random(i), TempleEnvironment.Palette.DIRT);
			assertNotEquals(TempleUsageTheme.Theme.LIBRARY, theme);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void nonDirtPaletteNeverRollsGarden()
	{
		for (TempleEnvironment.Palette palette : List.of(
			TempleEnvironment.Palette.DUNGEON,
			TempleEnvironment.Palette.CITY))
		{
			for (int i = 0; i < 200; i++)
			{
				TempleUsageTheme.Theme theme = TempleUsageTheme.rollFloorTheme(
					new Random(i ^ palette.hashCode()), palette);
				assertNotEquals(TempleUsageTheme.Theme.GARDEN, theme);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void storageFloorUsesHiddenStashNotChests() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, "1");
		MazeVariables.set(TempleSeededPicks.pickVar(1, TempleUsageTheme.USAGE_PURPOSE), "storage");

		Zone zone = db.getZone("temple.1");
		zone.getScript().init(zone, 0);

		assertEquals(0, countChestScripts(zone), "storage theme should not place wall chests");
		assertTrue(countHiddenStashObjects(zone) >= 1,
			"storage theme should hide loot in a barrel or crate");
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void libraryFloorPlacesBookshelfMasks() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, "1");
		MazeVariables.set(TempleSeededPicks.pickVar(1, TempleUsageTheme.USAGE_PURPOSE), "library");
		MazeVariables.set(
			TempleSeededPicks.pickVar(1, TempleEnvironment.ENV_PURPOSE),
			"dungeon:black:0.0:0.7:20");

		Zone zone = db.getZone("temple.1");
		zone.getScript().init(zone, 0);

		assertTrue(countBookshelfMasks(zone.getMap()) >= 1,
			"library theme should mask walls with bookshelves");
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void gardenFloorPlantsNinePerTile() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.DEPTH, "1");
		MazeVariables.set(TempleSeededPicks.pickVar(1, TempleUsageTheme.USAGE_PURPOSE), "garden");
		MazeVariables.set(
			TempleSeededPicks.pickVar(1, TempleEnvironment.ENV_PURPOSE),
			"dirt:black:0.0:0.7:24");

		Zone zone = db.getZone("temple.1");
		zone.getScript().init(zone, 0);

		Point spawn = zone.getPlayerOrigin();
		assertNotNull(spawn);
		assertFalse(hasPlantObjects(zone, spawn),
			"spawn tile should stay clear of garden clutter");

		assertTrue(hasGardenBedWithNineObjects(zone),
			"garden theme should plant nine objects on at least one bed tile");
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void scoutDifficultyMatchesDepth()
	{
		assertEquals(1, TempleDepthScaler.scoutSecretDifficulty(1));
		assertEquals(5, TempleDepthScaler.scoutSecretDifficulty(5));
	}

	/*-------------------------------------------------------------------------*/
	private static int countChestScripts(Zone zone)
	{
		int n = 0;
		for (var row : zone.getTiles())
		{
			for (var tile : row)
			{
				if (tile == null || tile.getScripts() == null)
				{
					continue;
				}
				for (var script : tile.getScripts())
				{
					if (script instanceof Chest)
					{
						n++;
					}
				}
			}
		}
		return n;
	}

	/*-------------------------------------------------------------------------*/
	static int countHiddenStashObjects(Zone zone)
	{
		int n = 0;
		for (EngineObject obj : zone.getMap().getExpandedObjects())
		{
			if (obj.getMouseClickScript() instanceof MouseClickScriptAdapter adapter
				&& adapter.getScript() instanceof ExecuteMazeScript exec
				&& exec.getScoutSecretDifficulty() > 0)
			{
				n++;
			}
		}
		return n;
	}

	/*-------------------------------------------------------------------------*/
	private static int countBookshelfMasks(Map map)
	{
		int n = 0;
		n += countMasks(map.getHorizontalWalls(), "objects.bookshelf.1");
		n += countMasks(map.getVerticalWalls(), "objects.bookshelf.1");
		return n;
	}

	/*-------------------------------------------------------------------------*/
	private static int countMasks(Wall[] walls, String textureName)
	{
		int n = 0;
		for (Wall wall : walls)
		{
			if (wall.getMaskTextures() == null)
			{
				continue;
			}
			for (var tex : wall.getMaskTextures())
			{
				if (tex != null && textureName.equals(tex.getName()))
				{
					n++;
				}
			}
		}
		return n;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean hasPlantObjects(Zone zone, Point tile)
	{
		int tileIndex = zone.getTileIndex(tile);
		for (EngineObject obj : zone.getMap().getExpandedObjects())
		{
			if (obj.getTileIndex() == tileIndex && isPlantTexture(textureName(obj)))
			{
				return true;
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean hasGardenBedWithNineObjects(Zone zone)
	{
		java.util.Map<Integer, Integer> byTile = new HashMap<>();
		for (EngineObject obj : zone.getMap().getExpandedObjects())
		{
			if (!isPlantTexture(textureName(obj)))
			{
				continue;
			}
			byTile.merge(obj.getTileIndex(), 1, Integer::sum);
		}
		return byTile.values().stream().anyMatch(count -> count >= 9);
	}

	/*-------------------------------------------------------------------------*/
	private static boolean isPlantTexture(String name)
	{
		return name != null && (name.startsWith("objects.plant.") || name.startsWith("objects.fungus."));
	}

	/*-------------------------------------------------------------------------*/
	private static String textureName(EngineObject obj)
	{
		if (obj.getNorthTexture() != null)
		{
			return obj.getNorthTexture().getName();
		}
		return "";
	}
}
