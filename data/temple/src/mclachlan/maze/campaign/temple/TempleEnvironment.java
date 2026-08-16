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

import java.awt.Color;
import java.util.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Tile;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Zone;

/**
 * Persist-once visual atmosphere for a generated temple floor: coherent
 * wall/floor/ceiling/door palette plus fog colour, shade multiplier, and
 * ambient tile light. Shade distance is fixed at {@code 0} (multiplier-only fog).
 */
public record TempleEnvironment(
	Palette palette,
	FogColour fogColour,
	double shadingDistance,
	double shadingMultiplier,
	int ambientLight)
{
	public static final String ENV_PURPOSE = "env";

	private static final double SHADING_DISTANCE = 0;
	private static final double[] SHADE_MULTIPLIER_BANDS = {0.4, 0.7, 1.0};

	/*-------------------------------------------------------------------------*/
	public static TempleEnvironment forFloor(int depth)
	{
		String var = TempleSeededPicks.pickVar(depth, ENV_PURPOSE);
		String existing = MazeVariables.get(var);
		if (existing != null && !existing.isEmpty())
		{
			return decode(existing);
		}

		Random random = TempleSeededPicks.rng(depth, ENV_PURPOSE);
		TempleEnvironment env = roll(random);
		MazeVariables.set(var, env.encode());
		return env;
	}

	/*-------------------------------------------------------------------------*/
	static TempleEnvironment roll(Random random)
	{
		List<Palette> palettes = availablePalettes();
		Palette palette = palettes.get(random.nextInt(palettes.size()));
		return rollAtmosphere(random, palette);
	}

	/*-------------------------------------------------------------------------*/
	/** Rolls fog/shade/light only — for tests that do not boot {@link Database}. */
	static TempleEnvironment rollAtmosphere(Random random, Palette palette)
	{
		FogColour fog = pickFogColour(random);
		double multiplier = SHADE_MULTIPLIER_BANDS[random.nextInt(SHADE_MULTIPLIER_BANDS.length)];
		int light = pickAmbientLight(random);
		return new TempleEnvironment(palette, fog, SHADING_DISTANCE, multiplier, light);
	}

	/*-------------------------------------------------------------------------*/
	static boolean isPaletteLoadable(Palette palette)
	{
		if (palette == null)
		{
			return false;
		}
		java.util.Map<String, ?> textures = Database.getInstance().getMazeTextures();
		for (String name : palette.allTextureNames())
		{
			if (!textures.containsKey(name))
			{
				return false;
			}
		}
		return true;
	}

	/*-------------------------------------------------------------------------*/
	private static List<Palette> availablePalettes()
	{
		List<Palette> result = new ArrayList<>();
		for (Palette palette : Palette.values())
		{
			if (isPaletteLoadable(palette))
			{
				result.add(palette);
			}
		}
		if (result.isEmpty())
		{
			result.add(Palette.DUNGEON);
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	Palette validatedPalette()
	{
		return isPaletteLoadable(palette) ? palette : Palette.DUNGEON;
	}

	/*-------------------------------------------------------------------------*/
	private static FogColour pickFogColour(Random random)
	{
		int roll = random.nextInt(100);
		if (roll < 70)
		{
			return FogColour.BLACK;
		}
		if (roll < 82)
		{
			return FogColour.GREY;
		}
		if (roll < 94)
		{
			return FogColour.WHITE;
		}
		return FogColour.RED;
	}

	/*-------------------------------------------------------------------------*/
	private static int pickAmbientLight(Random random)
	{
		int roll = random.nextInt(100);
		if (roll < 70)
		{
			return 32;
		}
		if (roll < 85)
		{
			return 26;
		}
		return 20;
	}

	/*-------------------------------------------------------------------------*/
	public void applyToZone(Zone zone)
	{
		zone.setShadeTargetColor(fogColour.toColor());
		zone.setShadingDistance(SHADING_DISTANCE);
		zone.setShadingMultiplier(shadingMultiplier);
	}

	/*-------------------------------------------------------------------------*/
	public void applyToCrusaderTiles(Map map)
	{
		Database db = Database.getInstance();
		Palette use = validatedPalette();
		Texture floor = db.getMazeTexture(use.floorTexture()).getTexture();
		Texture ceiling = db.getMazeTexture(use.ceilingTexture()).getTexture();
		for (Tile tile : map.getTiles())
		{
			tile.setFloorTexture(floor);
			tile.setCeilingTexture(ceiling);
			tile.setLightLevel(ambientLight);
		}
		registerPaletteTextures(map);
	}

	/*-------------------------------------------------------------------------*/
	public Texture wallTexture()
	{
		return Database.getInstance().getMazeTexture(validatedPalette().wallTexture()).getTexture();
	}

	/*-------------------------------------------------------------------------*/
	public Texture doorTexture()
	{
		return Database.getInstance().getMazeTexture(validatedPalette().doorTexture()).getTexture();
	}

	/*-------------------------------------------------------------------------*/
	public void registerPaletteTextures(Map map)
	{
		Database db = Database.getInstance();
		for (String name : validatedPalette().allTextureNames())
		{
			Texture t = db.getMazeTexture(name).getTexture();
			if (t != null)
			{
				map.addTexture(t);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	String encode()
	{
		return palette.id + ':'
			+ fogColour.id + ':'
			+ String.format(Locale.ROOT, "%.1f", shadingDistance) + ':'
			+ String.format(Locale.ROOT, "%.1f", shadingMultiplier) + ':'
			+ ambientLight;
	}

	/*-------------------------------------------------------------------------*/
	static TempleEnvironment decode(String encoded)
	{
		String[] parts = encoded.split(":");
		if (parts.length != 5)
		{
			throw new IllegalArgumentException("invalid env encoding: " + encoded);
		}
		return new TempleEnvironment(
			resolvePalette(parts[0]),
			FogColour.fromId(parts[1]),
			Double.parseDouble(parts[2]),
			Double.parseDouble(parts[3]),
			Integer.parseInt(parts[4]));
	}

	/*-------------------------------------------------------------------------*/
	private static Palette resolvePalette(String id)
	{
		if ("slime".equals(id))
		{
			return Palette.DUNGEON;
		}
		return Palette.fromId(id);
	}

	/*-------------------------------------------------------------------------*/
	public enum Palette
	{
		DUNGEON(
			"dungeon",
			"DUNGEON_WALL_1",
			"DUNGEON_FLOOR_1",
			"DUNGEON_CEILING_1",
			"CITY_DOOR_1"),
		CITY(
			"city",
			"CITY_WALL_1",
			"CITY_FLOOR_1",
			"CITY_CEILING_1",
			"CITY_DOOR_1"),
		DIRT(
			"dirt",
			"DIRT_FLOOR_1",
			"DIRT_FLOOR_1",
			"DUNGEON_CEILING_1",
			"CITY_DOOR_1");

		private final String id;
		private final String wallTexture;
		private final String floorTexture;
		private final String ceilingTexture;
		private final String doorTexture;

		Palette(
			String id,
			String wallTexture,
			String floorTexture,
			String ceilingTexture,
			String doorTexture)
		{
			this.id = id;
			this.wallTexture = wallTexture;
			this.floorTexture = floorTexture;
			this.ceilingTexture = ceilingTexture;
			this.doorTexture = doorTexture;
		}

		static Palette fromId(String id)
		{
			for (Palette p : values())
			{
				if (p.id.equals(id))
				{
					return p;
				}
			}
			throw new IllegalArgumentException("unknown palette: " + id);
		}

		public String wallTexture()
		{
			return wallTexture;
		}

		public String floorTexture()
		{
			return floorTexture;
		}

		public String ceilingTexture()
		{
			return ceilingTexture;
		}

		public String doorTexture()
		{
			return doorTexture;
		}

		List<String> allTextureNames()
		{
			LinkedHashSet<String> names = new LinkedHashSet<>();
			names.add(wallTexture);
			names.add(floorTexture);
			names.add(ceilingTexture);
			names.add(doorTexture);
			return List.copyOf(names);
		}
	}

	/*-------------------------------------------------------------------------*/
	public enum FogColour
	{
		BLACK("black", 0xFF000000),
		GREY("grey", 0xFF666666),
		WHITE("white", 0xFFFFFFFF),
		RED("red", 0xFF800000);

		private final String id;
		private final int argb;

		FogColour(String id, int argb)
		{
			this.id = id;
			this.argb = argb;
		}

		static FogColour fromId(String id)
		{
			for (FogColour c : values())
			{
				if (c.id.equals(id))
				{
					return c;
				}
			}
			throw new IllegalArgumentException("unknown fog colour: " + id);
		}

		Color toColor()
		{
			return new Color(argb, true);
		}
	}
}
