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

package mclachlan.maze.balance;

import java.nio.file.Files;
import java.nio.file.Path;
import mclachlan.maze.test.support.MazeTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hermetic checks that the headless-run HTML report is self-contained and escaped.
 */
public class HarnessHtmlReportTest extends MazeTestSupport
{
	@Test
	void rendersEscapedSelfContainedReport(@TempDir Path dir) throws Exception
	{
		HarnessRunReport report = new HarnessRunReport();
		report.title = "Test <run> & \"seed\"";
		report.seed = 99L;
		report.outcome = "Clear";
		report.maxDepthReached = 2;
		report.startTurn = 0;
		report.endTurn = 12;
		report.totalCombats = 3;
		report.totalCombatRounds = 15;
		report.totalPartyHpLost = 8;
		report.totalLootBaseCost = 40;
		report.totalXpGained = 120;
		report.levelsGained = 1;
		report.gold = 25;
		report.partySize = 6;
		report.partyAlive = 6;

		HarnessRunReport.PartyMember pc = new HarnessRunReport.PartyMember();
		pc.name = "Hero <One>";
		pc.characterClass = "Hero";
		pc.race = "Human";
		pc.gender = "Male";
		pc.level = 2;
		pc.hpCurrent = 10;
		pc.hpMax = 12;
		pc.experience = 80;
		pc.alive = true;
		report.party.add(pc);

		HarnessRunReport.Floor floor = new HarnessRunReport.Floor();
		floor.depth = 1;
		floor.tileSteps = 20;
		floor.combats = 2;
		floor.combatRounds = 10;
		floor.partyHpLost = 5;
		floor.lootBaseCost = 15;
		floor.xpGained = 50;
		report.floors.add(floor);

		Path out = HarnessHtmlReport.write(dir.resolve("run.html"), report);
		String html = Files.readString(out);

		assertTrue(html.startsWith("<!DOCTYPE html>"));
		assertFalse(html.contains("http://") || html.contains("https://"),
			"report must not pull outside resources");
		assertTrue(html.contains("<style>"));
		assertTrue(html.contains("<script>"));
		assertTrue(html.contains("Test &lt;run&gt; &amp; &quot;seed&quot;"));
		assertTrue(html.contains("Hero &lt;One&gt;"));
		assertTrue(html.contains("seed <code>99</code>"));
		assertTrue(html.contains("Clear"));
	}
}
