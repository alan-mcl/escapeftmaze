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

package mclachlan.maze.test.support;

import java.io.InputStream;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.audio.AudioPlayer;
import mclachlan.maze.balance.HeadlessUi;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Launcher;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.UserConfig;

/**
 * A headless {@link Maze} harness for Tier 4 tests.
 * <p>
 * This is the {@code MockCombat.getMockMaze()} pattern, corrected for hermetic
 * use: it boots against a caller-supplied in-memory {@link Database} (built via
 * {@link TestData}) rather than the default campaign, installs a quiet log and
 * perf-log, and supplies a default {@link UserConfig} (combat reads
 * {@code getUserConfig().getCombatDelay()}).
 * <p>
 * Future agents can extend this to run more of the engine without a display.
 */
public class HeadlessMaze
{
	/*-------------------------------------------------------------------------*/
	/**
	 * Boots a headless {@link Maze} on the supplied database. The database must
	 * already be the active singleton (i.e. constructed via {@link TestData}).
	 */
	public static Maze boot(Database db) throws Exception
	{
		Maze maze = new Maze(Launcher.getConfig(), Maze.getStubCampaign());

		maze.initAudio(new NoOpAudioPlayer());
		maze.initLog(null); // null is fine: Maze.log(...) is null-safe
		maze.initPerfLog(new QuietPerfLog());
		maze.initState();

		db.initImpls();

		maze.initSystems();
		maze.initUi(new HeadlessUi());

		maze.setUserConfig(new UserConfig(defaultUserConfigProperties()));

		// the GUI toolkit is a process-wide singleton some widgets reach for
		if (DIYToolkit.getInstance() == null)
		{
			new DIYToolkit();
		}

		return maze;
	}

	/*-------------------------------------------------------------------------*/
	private static java.util.Properties defaultUserConfigProperties()
	{
		java.util.Properties p = new java.util.Properties();
		p.setProperty(UserConfig.Key.COMBAT_DELAY.getValue(), "0");
		p.setProperty(UserConfig.Key.PERSONALITY_CHATTINESS.getValue(), "0");
		p.setProperty(UserConfig.Key.MUSIC_VOLUME.getValue(), "0");
		p.setProperty(UserConfig.Key.CURRENT_TIP_INDEX.getValue(), "0");
		p.setProperty(UserConfig.Key.AUTO_ADD_CONSUMABLES.getValue(), "false");
		return p;
	}

	/*-------------------------------------------------------------------------*/
	private static class NoOpAudioPlayer implements AudioPlayer
	{
		@Override
		public void playSound(String soundName, int volume) { }

		@Override
		public void cacheSound(String soundName, InputStream stream) { }
	}
}
