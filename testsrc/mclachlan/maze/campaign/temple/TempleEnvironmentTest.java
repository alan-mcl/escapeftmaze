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
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Persist-once per-floor atmosphere rolls for generated temple depths.
 */
public class TempleEnvironmentTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void forFloorPersistsPickAcrossCalls() throws Exception
	{
		TempleCampaignHarness.bootDatabase();
		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.FLOOR_SEED_PREFIX + "1", "999");

		TempleEnvironment first = TempleEnvironment.forFloor(1);
		TempleEnvironment second = TempleEnvironment.forFloor(1);

		assertEquals(first, second);
		assertNotNull(MazeVariables.get(TempleSeededPicks.pickVar(1, TempleEnvironment.ENV_PURPOSE)));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void encodeDecodeRoundTrip()
	{
		TempleEnvironment env = new TempleEnvironment(
			TempleEnvironment.Palette.DIRT,
			TempleEnvironment.FogColour.RED,
			0,
			1.0,
			20);

		assertEquals(env, TempleEnvironment.decode(env.encode()));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void rollUsesDungeonScaleKnobs() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		db.getMazeTextures();

		for (long runSeed = 0; runSeed < 200; runSeed++)
		{
			MazeVariables.clearAll();
			MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(runSeed));
			MazeVariables.set(TempleSeeds.FLOOR_SEED_PREFIX + "1", Long.toString(runSeed * 31));

			TempleEnvironment env = TempleEnvironment.forFloor(1);

			assertEquals(0, env.shadingDistance(), 0.001);
			assertTrue(isShadeBand(env.shadingMultiplier()));
			assertTrue(env.ambientLight() == 20 || env.ambientLight() == 26 || env.ambientLight() == 32);
			assertNotNull(env.validatedPalette());
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void fogAndLightWeightsSkewAsConfigured()
	{
		Map<TempleEnvironment.FogColour, Integer> fogCounts = new EnumMap<>(TempleEnvironment.FogColour.class);
		Map<Integer, Integer> lightCounts = new HashMap<>();

		for (int i = 0; i < 5000; i++)
		{
			Random random = new Random(i);
			TempleEnvironment env = TempleEnvironment.rollAtmosphere(
				random, TempleEnvironment.Palette.DUNGEON);
			fogCounts.merge(env.fogColour(), 1, Integer::sum);
			lightCounts.merge(env.ambientLight(), 1, Integer::sum);
		}

		assertTrue(fogCounts.get(TempleEnvironment.FogColour.BLACK) > fogCounts.get(TempleEnvironment.FogColour.RED));
		assertTrue(fogCounts.get(TempleEnvironment.FogColour.BLACK) > fogCounts.get(TempleEnvironment.FogColour.GREY));
		assertTrue(lightCounts.get(32) > lightCounts.get(26));
		assertTrue(lightCounts.get(32) > lightCounts.get(20));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void shadingMultiplierVariesAcrossRolls()
	{
		Set<Double> multipliers = new HashSet<>();
		for (int i = 0; i < 500; i++)
		{
			TempleEnvironment env = TempleEnvironment.rollAtmosphere(
				new Random(i), TempleEnvironment.Palette.DUNGEON);
			multipliers.add(env.shadingMultiplier());
		}
		assertTrue(multipliers.size() > 1, "shade multiplier should vary across rolls");
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void decodeMapsLegacySlimePaletteToDungeon()
	{
		TempleEnvironment env = TempleEnvironment.decode("slime:black:0.7:0.7:32");
		assertEquals(TempleEnvironment.Palette.DUNGEON, env.palette());
	}

	/*-------------------------------------------------------------------------*/
	private static boolean isShadeBand(double value)
	{
		for (double band : new double[]{0.4, 0.7, 1.0})
		{
			if (Math.abs(value - band) < 0.001)
			{
				return true;
			}
		}
		return false;
	}
}
