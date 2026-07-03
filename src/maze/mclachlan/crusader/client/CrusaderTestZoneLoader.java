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

package mclachlan.crusader.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.v2.SimpleMapSilo;
import mclachlan.maze.data.v2.SingletonSilo;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.data.v2.serialisers.V2SerialiserFactory.*;

/**
 * Loads zones for {@link CrusaderClient}: the default {@code test/crusader/}
 * fixture, or a production zone from {@code data/default/db/zones/}.
 */
public class CrusaderTestZoneLoader
{
	public static final String TEST_DIR = "test/crusader/";
	public static final String TEST_ZONE_JSON = TEST_DIR + "testMap.json";
	public static final String TEST_TEXTURES_JSON = TEST_DIR + "textures.json";

	/*-------------------------------------------------------------------------*/
	public Zone loadTestZone() throws Exception
	{
		CrusaderTestDatabase db = new CrusaderTestDatabase();
		Map<String, MazeTexture> textures = loadTestTextures(db);
		db.setTestTextures(textures);

		try (BufferedReader reader = new BufferedReader(
			new FileReader(TEST_ZONE_JSON, StandardCharsets.UTF_8)))
		{
			Zone zone = (Zone)new SingletonSilo<>(getZoneSerialiser(db)).load(reader, db);
			zone.getMap().init();
			return zone;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Zone loadProductionZone(String zoneName) throws Exception
	{
		Database.resetInstanceForTesting();
		Campaign campaign = Database.getCampaigns().get("default");
		if (campaign == null)
		{
			throw new MazeException("campaign [default] not found");
		}

		Database db = new Database(new V2Loader(), new V2Saver(), campaign);
		db.initImpls();
		db.getMazeTextures();

		Zone zone = db.getZone(zoneName);
		zone.getMap().init();
		return zone;
	}

	/*-------------------------------------------------------------------------*/
	static Map<String, MazeTexture> loadTestTextures(Database db) throws Exception
	{
		try (BufferedReader reader = new BufferedReader(
			new FileReader(TEST_TEXTURES_JSON, StandardCharsets.UTF_8)))
		{
			return new SimpleMapSilo<>(getMazeTextureSerialiser()).load(reader, db);
		}
	}
}
