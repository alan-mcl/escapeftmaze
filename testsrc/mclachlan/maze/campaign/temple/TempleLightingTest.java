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
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.crusader.script.RandomLightingScript;
import mclachlan.dungeongen.DungeonRoom;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Zone;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Persist-once light fixtures and radial pools for generated temple floors.
 */
public class TempleLightingTest extends MazeTestSupport
{
	private static final int ROOM = Noise4jDungeonGen.ROOM_THRESHOLD;
	private static final int CORRIDOR = Noise4jDungeonGen.CORRIDOR_THRESHOLD;

	/*-------------------------------------------------------------------------*/
	@Test
	void fixturePickPersistsPerDepth() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);
		MazeVariables.set(TempleSeeds.FLOOR_SEED_PREFIX + "2", "555");

		TempleLighting.FixtureType first = TempleLighting.pickFixture(2);
		TempleLighting.FixtureType second = TempleLighting.pickFixture(2);

		assertNotNull(first);
		assertEquals(first, second);
		assertNotNull(MazeVariables.get(
			TempleSeededPicks.pickVar(2, TempleLighting.LIGHT_PURPOSE)));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void lightPoolRaisesSourceAndNeighbour()
	{
		Map map = buildMap(5, 5, 16);
		TempleLighting.applyLightPools(map, Set.of(new Point(2, 2)), 16);

		Tile[] tiles = map.getTiles();
		assertEquals(24, tiles[2 * 5 + 2].getLightLevel());
		assertEquals(20, tiles[2 * 5 + 3].getLightLevel());
		assertEquals(18, tiles[0 * 5 + 2].getLightLevel());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void flameCandidatesAreRoomTilesOnly()
	{
		Grid grid = new Grid(7, 7);
		for (int x = 0; x < 7; x++)
		{
			for (int y = 0; y < 7; y++)
			{
				grid.set(x, y, Noise4jDungeonGen.WALL_THRESHOLD / 10F);
			}
		}
		for (int x = 1; x <= 3; x++)
		{
			for (int y = 1; y <= 3; y++)
			{
				grid.set(x, y, ROOM / 10F);
			}
		}
		grid.set(0, 2, CORRIDOR / 10F);

		DungeonRoom room = new DungeonRoom(1, 1, 3, 3);
		for (Point tile : TempleLighting.flameCandidateTiles(room, grid))
		{
			assertEquals(ROOM, TempleLighting.cellType(grid, tile.x, tile.y),
				"flame candidates must be room tiles, not corridors");
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void generatedFloorPlacesMatchingLightObjects() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.RUN_SEED, "42");
		MazeVariables.set(TempleSeeds.DEPTH, "1");

		Zone zone = db.getZone("temple.1");
		zone.getScript().init(zone, 0);

		TempleLighting.FixtureType fixture = TempleLighting.pickFixture(1);
		assertNotNull(fixture);

		String encoded = MazeVariables.get(
			TempleSeededPicks.pickVar(1, TempleEnvironment.ENV_PURPOSE));
		assertNotNull(encoded);
		TempleEnvironment env = TempleEnvironment.decode(encoded);
		int peak = Math.min(env.ambientLight() + 8, 32);

		assertTrue(countMatchingObjects(zone, fixture.textureName()) >= 1,
			"expected at least one " + fixture + " object");

		boolean hasElevatedTile = false;
		for (Tile tile : zone.getMap().getTiles())
		{
			if (tile.getLightLevel() > env.ambientLight())
			{
				hasElevatedTile = true;
				break;
			}
		}
		assertTrue(hasElevatedTile, "expected radial light pools above ambient");

		if (fixture.flickers())
		{
			assertTrue(hasFlickerScript(zone.getMap()), "expected RandomLightingScript");
			RandomLightingScript script = findFlickerScript(zone.getMap());
			assertEquals(env.ambientLight(), script.getMinLightLevel());
			assertEquals(peak, script.getMaxLightLevel());
		}
		else
		{
			assertFalse(hasFlickerScript(zone.getMap()), "ceiling fittings are constant light");
		}
	}

	/*-------------------------------------------------------------------------*/
	private static Map buildMap(int width, int length, int ambient)
	{
		Tile[] tiles = new Tile[width * length];
		for (int i = 0; i < tiles.length; i++)
		{
			tiles[i] = new Tile(null, null, ambient);
		}
		Wall wall = new Wall(
			new mclachlan.crusader.Texture[]{Map.NO_WALL},
			null,
			false,
			false,
			1,
			null,
			null,
			null);
		Wall[] horiz = new Wall[width * (length + 1)];
		Wall[] vert = new Wall[(width + 1) * length];
		Arrays.fill(horiz, wall);
		Arrays.fill(vert, wall);
		return new Map(
			length,
			width,
			32,
			tiles,
			new mclachlan.crusader.Texture[]{Map.NO_WALL},
			horiz,
			vert,
			new Map.SkyConfig[0],
			new ArrayList<>(),
			new mclachlan.crusader.MapScript[0]);
	}

	/*-------------------------------------------------------------------------*/
	private static int countMatchingObjects(Zone zone, String textureName)
	{
		int count = 0;
		for (EngineObject obj : zone.getMap().getExpandedObjects())
		{
			if (textureName.equals(textureName(obj)))
			{
				count++;
			}
		}
		return count;
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

	/*-------------------------------------------------------------------------*/
	private static boolean hasFlickerScript(Map map)
	{
		return findFlickerScript(map) != null;
	}

	/*-------------------------------------------------------------------------*/
	private static RandomLightingScript findFlickerScript(Map map)
	{
		if (map.getScripts() == null)
		{
			return null;
		}
		for (var script : map.getScripts())
		{
			if (script instanceof RandomLightingScript rls)
			{
				return rls;
			}
		}
		return null;
	}
}
