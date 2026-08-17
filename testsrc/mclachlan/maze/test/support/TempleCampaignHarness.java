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

package mclachlan.maze.test.support;

import mclachlan.maze.audio.AudioPlayer;
import mclachlan.maze.balance.HeadlessHarnessSupport;
import mclachlan.maze.balance.HeadlessUi;
import mclachlan.maze.ui.UserInterface;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Launcher;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.UserConfig;

import java.io.InputStream;

/**
 * Boots a headless Maze against the real {@code temple} campaign (parent
 * {@code default}). Allowed for temple campaign tests only — prefer
 * {@link HeadlessMaze} + {@link TestData} for hermetic suite tests.
 */
public final class TempleCampaignHarness
{
	private TempleCampaignHarness()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static Database bootDatabase() throws Exception
	{
		Campaign temple = Database.getCampaigns().get("temple");
		if (temple == null)
		{
			throw new IllegalStateException(
				"temple campaign not found under data/; run from repo root");
		}

		V2Loader loader = new V2Loader();
		V2Saver saver = new V2Saver();
		Database db = new Database(loader, saver, temple);
		return db;
	}

	/*-------------------------------------------------------------------------*/
	public static Maze bootMaze(Database db) throws Exception
	{
		return bootMaze(db, new HeadlessUi());
	}

	/*-------------------------------------------------------------------------*/
	public static Maze bootMaze(Database db, UserInterface ui) throws Exception
	{
		Campaign temple = Database.getCampaigns().get("temple");
		Maze maze = new Maze(Launcher.getConfig(), temple);

		maze.initAudio(new AudioPlayer()
		{
			@Override
			public void playSound(String soundName, int volume)
			{
			}

			@Override
			public void cacheSound(String soundName, InputStream stream)
			{
			}
		});
		maze.initLog(null);
		maze.initPerfLog(new QuietPerfLog());
		maze.initState();

		db.initImpls();

		// caches needed for gen + combat smoke
		db.getMazeTextures();
		db.getMazeScripts();
		db.getFoeTemplates();
		db.getFoeEntries();
		db.getEncounterTables();
		db.getLootTables();
		db.getLootEntries();
		db.getNaturalWeapons();
		db.getSpellEffects();
		db.getDifficultyLevels();

		maze.initSystems();
		maze.initUi(ui);
		maze.initEventProcessorForTesting();
		maze.setUserConfig(UserConfig.defaultsForTesting());

		HeadlessHarnessSupport.ensureInitialised();

		MazeVariables.clearAll();
		HeadlessHarnessSupport.prepareMaze(maze);
		return maze;
	}
}
