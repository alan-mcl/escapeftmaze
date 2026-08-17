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

import java.nio.file.Files;
import java.nio.file.Path;
import mclachlan.maze.balance.CombatDriver;
import mclachlan.maze.balance.HarnessUi;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Zone;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bounded automated run through temple depths 1–4 with harness combat AI.
 */
public class TempleFloorRunTest extends MazeTestSupport
{
	private static final long RUN_SEED = 42L;

	/*-------------------------------------------------------------------------*/
	@Test
	void boundedRunThroughPlayableDepths() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		HarnessUi ui = new HarnessUi();
		Maze maze = TempleCampaignHarness.bootMaze(db, ui);

		TempleRunDriver.Config config = new TempleRunDriver.Config();
		config.runSeed = RUN_SEED;
		config.maxDepth = TempleDepthScaler.PLAYABLE_MAX_DEPTH;
		config.maxTileStepsPerFloor = 400;
		config.maxCombatRounds = 200;
		Path reportPath = Path.of("build", "test-reports",
			"temple-floor-run-" + RUN_SEED + ".html");
		config.htmlReportPath = reportPath;

		TempleRunMetrics metrics = new TempleRunDriver(maze, db).run(config);
		CombatDriver.drainEvents(maze);

		if (!metrics.isWiped())
		{
			assertNotNull(maze.getParty(), "surviving run should still have a party");
			assertEquals(TempleRunDriver.PARTY_SIZE, maze.getParty().size(),
				"temple run must use a full party of 6");
		}
		assertTrue(Files.isRegularFile(reportPath),
			"headless dungeon run should write an HTML report");
		String html = Files.readString(reportPath);
		assertTrue(html.contains("Temple of Wasud"));
		assertTrue(html.contains("Hero"));
		assertTrue(html.contains("Paladin"));
		assertTrue(html.contains("Burglar"));
		assertTrue(html.contains("Ranger"));
		assertTrue(html.contains("Priest"));
		assertTrue(html.contains("Sorcerer"));

		assertTrue(metrics.getMaxDepthReached() >= 1,
			"run should reach at least depth 1");
		assertFalse(metrics.getFloors().isEmpty(),
			"metrics should record per-floor data");
		assertTrue(metrics.getMaxDepthReached() == TempleDepthScaler.PLAYABLE_MAX_DEPTH
				|| metrics.isWiped(),
			"run should clear depth 4 or record a wipe");

		if (metrics.getMaxDepthReached() >= TempleDepthScaler.PLAYABLE_MAX_DEPTH
			&& !metrics.isWiped())
		{
			assertEquals(TempleDepthScaler.PLAYABLE_MAX_DEPTH,
				metrics.getMaxDepthReached());
			Zone depth4 = maze.getCurrentZone();
			assertNotNull(depth4);
			assertNull(TempleFloorDressing.findStairsDownPortalFrom(depth4),
				"depth 4 should have no down stairs after run");
		}

		assertTrue(metrics.getEndTurn() > metrics.getStartTurn(),
			"run should advance game time via incTurn on each step");
		assertTrue(metrics.getTotalCombats() > 0,
			"run should trigger at least one encounter combat (seed " + RUN_SEED + ")");
		assertEquals(RUN_SEED, metrics.getRunSeed());

		assertTrue(html.contains("log/") || html.contains("log\\"),
			"HTML report should reference the session log path");
		String logPath = extractLogPath(html);
		assertNotNull(logPath, "HTML report should include a log file path");
		assertTrue(Files.isRegularFile(Path.of(logPath)),
			"session log file should exist: " + logPath);

		System.out.println(metrics);
		System.out.println("Session log: " + logPath);
	}

	private static String extractLogPath(String html)
	{
		int start = html.indexOf("<code>", html.indexOf(" · log "));
		if (start < 0)
		{
			return null;
		}
		start += "<code>".length();
		int end = html.indexOf("</code>", start);
		if (end < 0)
		{
			return null;
		}
		return html.substring(start, end);
	}
}
