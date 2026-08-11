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
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.Map;
import mclachlan.crusader.Map.SkyConfig;
import mclachlan.maze.game.GameTime;

/**
 * Applies cosmetic day/night presentation to a zone from the global turn clock.
 * Visual state is derived from {@link GameTime#getTurnNr()} on each call so it
 * survives zone reloads and save/load without persisting light maps.
 */
public class DayNightPresentation
{
	public static final int DEFAULT_NIGHT_BOTTOM_COLOUR = 0xFF1a2040;
	public static final int DEFAULT_NIGHT_TOP_COLOUR = 0xFF080818;

	/**
	 * Default night fog shade: muted cool slate rather than black, so misty
	 * outdoor zones keep haze without staying glaring white.
	 */
	public static final int DEFAULT_NIGHT_SHADE_TARGET = 0xFF6A7B8C;

	/*-------------------------------------------------------------------------*/
	/**
	 * Optional night palette overrides from zone script data; null fields use
	 * {@link #DEFAULT_NIGHT_*} values.
	 */
	public static final class NightPalette
	{
		private final Integer nightShadeArgb;
		private final Integer nightSkyBottomArgb;
		private final Integer nightSkyTopArgb;

		public NightPalette(
			Integer nightShadeArgb,
			Integer nightSkyBottomArgb,
			Integer nightSkyTopArgb)
		{
			this.nightShadeArgb = nightShadeArgb;
			this.nightSkyBottomArgb = nightSkyBottomArgb;
			this.nightSkyTopArgb = nightSkyTopArgb;
		}

		public static NightPalette from(DefaultZoneScript script)
		{
			return new NightPalette(
				colorToArgb(script.getNightShadeTargetColor()),
				colorToArgb(script.getNightSkyBottomColor()),
				colorToArgb(script.getNightSkyTopColor()));
		}

		private static Integer colorToArgb(Color color)
		{
			return color == null ? null : color.getRGB();
		}

		int nightShadeArgb()
		{
			return nightShadeArgb != null ? nightShadeArgb : DEFAULT_NIGHT_SHADE_TARGET;
		}

		int nightSkyBottomArgb()
		{
			return nightSkyBottomArgb != null ? nightSkyBottomArgb : DEFAULT_NIGHT_BOTTOM_COLOUR;
		}

		int nightSkyTopArgb()
		{
			return nightSkyTopArgb != null ? nightSkyTopArgb : DEFAULT_NIGHT_TOP_COLOUR;
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Daytime anchor colours for each {@link SkyConfig.Type#CYLINDER_GRADIENT}
	 * layer, captured once per zone instance after load.
	 */
	public static final class GradientAnchors
	{
		private final int[] configIndices;
		private final int[] dayBottomColours;
		private final int[] dayTopColours;

		private GradientAnchors(
			int[] configIndices,
			int[] dayBottomColours,
			int[] dayTopColours)
		{
			this.configIndices = configIndices;
			this.dayBottomColours = dayBottomColours;
			this.dayTopColours = dayTopColours;
		}

		public boolean isEmpty()
		{
			return configIndices.length == 0;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static GradientAnchors captureGradientAnchors(Map map)
	{
		SkyConfig[] configs = map.getSkyConfigs();
		if (configs == null)
		{
			return new GradientAnchors(new int[0], new int[0], new int[0]);
		}

		int count = 0;
		for (SkyConfig config : configs)
		{
			if (config.getType() == SkyConfig.Type.CYLINDER_GRADIENT)
			{
				count++;
			}
		}

		int[] indices = new int[count];
		int[] bottom = new int[count];
		int[] top = new int[count];
		int j = 0;
		for (int i = 0; i < configs.length; i++)
		{
			SkyConfig config = configs[i];
			if (config.getType() == SkyConfig.Type.CYLINDER_GRADIENT)
			{
				indices[j] = i;
				bottom[j] = config.getBottomColour();
				top[j] = config.getTopColour();
				j++;
			}
		}

		return new GradientAnchors(indices, bottom, top);
	}

	/*-------------------------------------------------------------------------*/
	public static void apply(
		Zone zone,
		long turnNr,
		int lightAmplitude,
		GradientAnchors anchors,
		NightPalette palette)
	{
		if (lightAmplitude <= 0)
		{
			return;
		}

		if (palette == null)
		{
			palette = new NightPalette(null, null, null);
		}

		double nightAmount = GameTime.getNightAmount(turnNr);
		int delta = -(int)Math.round(lightAmplitude * nightAmount);
		Map map = zone.getMap();
		map.setLightLevelFromBaseline(delta);
		map.setSkyLightLevel(CrusaderEngine.NORMAL_LIGHT_LEVEL + delta);
		applyShadeTargetShift(zone, map, nightAmount, palette);

		if (anchors != null && !anchors.isEmpty())
		{
			applyGradientShift(map, anchors, nightAmount, palette);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void applyShadeTargetShift(
		Zone zone,
		Map map,
		double nightAmount,
		NightPalette palette)
	{
		Color authored = zone.getShadeTargetColor();
		if (authored == null)
		{
			return;
		}

		map.captureDayShade(authored);
		map.setShadeTargetColor(lerpColour(
			map.getDayShadeArgb(),
			palette.nightShadeArgb(),
			nightAmount));
	}

	/*-------------------------------------------------------------------------*/
	private static void applyGradientShift(
		Map map,
		GradientAnchors anchors,
		double nightAmount,
		NightPalette palette)
	{
		SkyConfig[] configs = map.getSkyConfigs();
		if (configs == null)
		{
			return;
		}

		for (int j = 0; j < anchors.configIndices.length; j++)
		{
			SkyConfig config = configs[anchors.configIndices[j]];
			config.setBottomColour(lerpColour(
				anchors.dayBottomColours[j],
				palette.nightSkyBottomArgb(),
				nightAmount));
			config.setTopColour(lerpColour(
				anchors.dayTopColours[j],
				palette.nightSkyTopArgb(),
				nightAmount));
		}
	}

	/*-------------------------------------------------------------------------*/
	static int lerpColour(int fromArgb, int toArgb, double amount)
	{
		double t = amount;
		if (t <= 0.0)
		{
			return fromArgb;
		}
		if (t >= 1.0)
		{
			return toArgb;
		}

		int a1 = (fromArgb >> 24) & 0xFF;
		int r1 = (fromArgb >> 16) & 0xFF;
		int g1 = (fromArgb >> 8) & 0xFF;
		int b1 = fromArgb & 0xFF;

		int a2 = (toArgb >> 24) & 0xFF;
		int r2 = (toArgb >> 16) & 0xFF;
		int g2 = (toArgb >> 8) & 0xFF;
		int b2 = toArgb & 0xFF;

		int a = (int)Math.round(a1 + t * (a2 - a1));
		int r = (int)Math.round(r1 + t * (r2 - r1));
		int g = (int)Math.round(g1 + t * (g2 - g1));
		int b = (int)Math.round(b1 + t * (b2 - b1));

		return (a << 24) | (r << 16) | (g << 8) | b;
	}
}
