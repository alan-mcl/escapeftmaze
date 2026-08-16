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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Generated temple chests must never resolve to an empty loot list.
 */
public class TempleChestLootEventTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void resolveGuaranteesLootWhenTableRollIsEmpty() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		// d100 rolls of 99 fail every 40/30/30 temple.depth.1.loot entry.
		seed(0xDEADBEEFL);

		TempleChestLootEvent event = new TempleChestLootEvent("temple.depth.1.loot");
		List<MazeEvent> events = event.resolve();

		assertNotNull(events);
		assertFalse(events.isEmpty(), "chest loot should never resolve empty");
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void pickOneReturnsWeightedEntryFromTable() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);

		GroupOfPossibilities<ILootEntry> gop = db.getLootTable("temple.depth.1.loot").getLootEntries();
		ILootEntry picked = TempleChestLootEvent.pickOne(gop);

		assertNotNull(picked);
		assertTrue(picked instanceof LootEntry);
	}
}
