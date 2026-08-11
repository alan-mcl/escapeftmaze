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

package mclachlan.maze.map;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.Map;
import mclachlan.crusader.Map.SkyConfig;
import mclachlan.crusader.MapScript;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.game.GameTime;
import mclachlan.maze.stat.StatModifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hermetic tests for day/night presentation and baseline lighting.
 */
public class DayNightPresentationTest
{
	private static final int DAY_BOTTOM = 0xFF66CCFF;
	private static final int DAY_TOP = 0xFF000099;
	private static final DayNightPresentation.NightPalette DEFAULT_PALETTE =
		new DayNightPresentation.NightPalette(null, null, null);

	/*-------------------------------------------------------------------------*/
	@Test
	void setLightLevelFromBaselineIsIdempotent()
	{
		Map map = buildMapWithLight(32);
		map.setLightLevelFromBaseline(-8);
		int first = map.getTiles()[0].getCurrentLightLevel();
		map.setLightLevelFromBaseline(-8);
		assertEquals(first, map.getTiles()[0].getCurrentLightLevel());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void nightApplyDarkensRelativeToNoon()
	{
		Zone zone = buildZoneWithGradientSky(32);
		DayNightPresentation.GradientAnchors anchors =
			DayNightPresentation.captureGradientAnchors(zone.getMap());

		DayNightPresentation.apply(zone, 150, 24, anchors, DEFAULT_PALETTE);
		int noonLight = zone.getMap().getTiles()[0].getCurrentLightLevel();
		int noonSky = zone.getMap().getSkyLightLevel();

		DayNightPresentation.apply(zone, 0, 24, anchors, DEFAULT_PALETTE);
		int midnightLight = zone.getMap().getTiles()[0].getCurrentLightLevel();
		int midnightSky = zone.getMap().getSkyLightLevel();

		assertEquals(32, noonLight);
		assertEquals(CrusaderEngine.NORMAL_LIGHT_LEVEL, noonSky);
		assertEquals(8, midnightLight);
		assertEquals(8, midnightSky);
		assertTrue(midnightLight < noonLight);
		assertTrue(midnightSky < noonSky);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void setLightLevelFromBaselineClampsToMax()
	{
		Map map = buildMapWithLight(CrusaderEngine.MAX_LIGHT_LEVEL);
		map.setLightLevelFromBaseline(10);
		assertEquals(CrusaderEngine.MAX_LIGHT_LEVEL, map.getTiles()[0].getCurrentLightLevel());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void defaultZoneScriptWithCycleDisabledDoesNotChangeLight()
	{
		Zone zone = buildZoneWithGradientSky(32);
		DefaultZoneScript script = new DefaultZoneScript(-1, 10, null);

		script.init(zone, 0);

		assertEquals(32, zone.getMap().getTiles()[0].getCurrentLightLevel());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void gradientShiftInterpolatesTowardNightPalette()
	{
		Zone zone = buildZoneWithGradientSky(32);
		SkyConfig config = zone.getMap().getSkyConfigs()[0];
		DayNightPresentation.GradientAnchors anchors =
			DayNightPresentation.captureGradientAnchors(zone.getMap());

		DayNightPresentation.apply(zone, 0, 10, anchors, DEFAULT_PALETTE);

		assertNotEquals(DAY_BOTTOM, config.getBottomColour());
		assertNotEquals(DAY_TOP, config.getTopColour());
		assertEquals(0xFF1a2040, config.getBottomColour());
		assertEquals(0xFF080818, config.getTopColour());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void shadeTargetShiftsTowardNightMistWithoutGoingBlack()
	{
		Zone zone = buildZoneWithGradientSky(32);
		zone.setShadeTargetColor(Color.WHITE);
		DayNightPresentation.GradientAnchors anchors =
			DayNightPresentation.captureGradientAnchors(zone.getMap());

		DayNightPresentation.apply(zone, 150, 24, anchors, DEFAULT_PALETTE);
		assertEquals(255, zone.getMap().getShadeTargetRed());
		assertEquals(255, zone.getMap().getShadeTargetGreen());
		assertEquals(255, zone.getMap().getShadeTargetBlue());

		DayNightPresentation.apply(zone, 0, 24, anchors, DEFAULT_PALETTE);
		assertEquals(
			DayNightPresentation.DEFAULT_NIGHT_SHADE_TARGET,
			(0xFF << 24)
				| (zone.getMap().getShadeTargetRed() << 16)
				| (zone.getMap().getShadeTargetGreen() << 8)
				| zone.getMap().getShadeTargetBlue());
		assertTrue(zone.getMap().getShadeTargetRed() > 0);
		assertTrue(zone.getMap().getShadeTargetBlue() > zone.getMap().getShadeTargetRed());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void lerpColourEndpoints()
	{
		assertEquals(DAY_BOTTOM, DayNightPresentation.lerpColour(DAY_BOTTOM, 0xFF000000, 0.0));
		assertEquals(0xFF000000, DayNightPresentation.lerpColour(DAY_BOTTOM, 0xFF000000, 1.0));
	}

	/*-------------------------------------------------------------------------*/
	private static Map buildMapWithLight(int lightLevel)
	{
		Tile[] tiles = new Tile[]{new Tile(null, null, lightLevel)};
		Wall[] horiz = new Wall[]{new Wall(
			new mclachlan.crusader.Texture[]{Map.NO_WALL}, null, false, false, 1, null, null, null)};
		Wall[] vert = new Wall[]{new Wall(
			new mclachlan.crusader.Texture[]{Map.NO_WALL}, null, false, false, 1, null, null, null)};

		return new Map(
			1,
			1,
			32,
			tiles,
			new mclachlan.crusader.Texture[]{Map.NO_WALL},
			horiz,
			vert,
			new SkyConfig[0],
			new ArrayList<>(),
			new MapScript[0]);
	}

	/*-------------------------------------------------------------------------*/
	private static Zone buildZoneWithGradientSky(int lightLevel)
	{
		SkyConfig gradient = new SkyConfig();
		gradient.setType(SkyConfig.Type.CYLINDER_GRADIENT);
		gradient.setBottomColour(DAY_BOTTOM);
		gradient.setTopColour(DAY_TOP);

		Tile[] tiles = new Tile[]{new Tile(null, null, lightLevel)};
		Wall[] horiz = new Wall[]{new Wall(
			new mclachlan.crusader.Texture[]{Map.NO_WALL}, null, false, false, 1, null, null, null)};
		Wall[] vert = new Wall[]{new Wall(
			new mclachlan.crusader.Texture[]{Map.NO_WALL}, null, false, false, 1, null, null, null)};

		Map map = new Map(
			1,
			1,
			32,
			tiles,
			new mclachlan.crusader.Texture[]{Map.NO_WALL},
			horiz,
			vert,
			new SkyConfig[]{gradient},
			new ArrayList<>(),
			new MapScript[0]);

		mclachlan.maze.map.Tile[][] mazeTiles = new mclachlan.maze.map.Tile[1][1];
		mazeTiles[0][0] = new mclachlan.maze.map.Tile(
			new ArrayList<>(),
			null,
			new StatModifier(),
			mclachlan.maze.map.Tile.TerrainType.WILDERNESS,
			null,
			0,
			mclachlan.maze.map.Tile.RestingDanger.LOW,
			mclachlan.maze.map.Tile.RestingEfficiency.AVERAGE);

		return new Zone(
			"test-outdoor",
			map,
			mazeTiles,
			new Portal[0],
			new DefaultZoneScript(1, 10, null),
			Color.BLACK,
			Color.BLACK,
			true,
			true,
			4.0f,
			4.0f,
			-40,
			2,
			0.65f,
			1,
			new Point(0, 0));
	}
}
