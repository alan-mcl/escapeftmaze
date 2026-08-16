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
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Seeded per-depth foe roster subset.
 */
public class TempleFoeRosterTest extends MazeTestSupport
{
	private static final long RUN_SEED = 42L;

	/*-------------------------------------------------------------------------*/
	@Test
	void rosterSubsetsBandTableWithoutMutatingCache() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.FLOOR_SEED_PREFIX + "1", Integer.toString(TempleSeeds.floorSeed(1)));

		EncounterTable band = db.getEncounterTable(TempleDepthScaler.encounterTableName(1));
		List<FoeEntry> before = band.getEncounterTable().getItems();

		EncounterTable roster = TempleFoeRoster.forFloor(1);
		assertEquals(TempleDepthScaler.foeSubsetSize(1), roster.getEncounterTable().getItems().size());
		assertNotNull(MazeVariables.get(TempleSeeds.rosterVar(1)));

		assertSame(before, band.getEncounterTable().getItems(),
			"cached band table list must not be replaced");
		for (FoeEntry entry : roster.getEncounterTable().getItems())
		{
			assertTrue(before.contains(entry), entry.getName() + " should be from band pool");
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void rosterRestoresFromPersistedNames() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		MazeVariables.clearAll();
		MazeVariables.set(TempleSeeds.RUN_SEED, Long.toString(RUN_SEED));
		MazeVariables.set(TempleSeeds.FLOOR_SEED_PREFIX + "1", Integer.toString(TempleSeeds.floorSeed(1)));

		EncounterTable first = TempleFoeRoster.forFloor(1);
		List<String> names = first.getEncounterTable().getItems().stream()
			.map(FoeEntry::getName)
			.toList();

		EncounterTable second = TempleFoeRoster.forFloor(1);
		List<String> again = second.getEncounterTable().getItems().stream()
			.map(FoeEntry::getName)
			.toList();
		assertEquals(names, again);
	}
}
