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

import java.awt.Point;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.dungeongen.DungeonGenContext;
import mclachlan.dungeongen.StairPortalSpec;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TempleCampaignHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stair portal encoding and transition spawn resolution.
 */
public class TempleStairLinksTest extends MazeTestSupport
{
	@Test
	void portalSpecRoundTripEncodesAndDecodes()
	{
		StairPortalSpec spec = new StairPortalSpec(
			new Point(5, 8),
			CrusaderEngine.Facing.WEST,
			new Point(4, 8),
			CrusaderEngine.Facing.NORTH,
			true,
			StairPortalSpec.StairMask.STAIR_DOWN);

		StairPortalSpec decoded = StairPortalSpec.decode(spec.encode());
		assertEquals(spec, decoded);
	}

	@Test
	void writeAndReadPortalUsesMazeVariables()
	{
		StairPortalSpec spec = new StairPortalSpec(
			new Point(10, 12),
			CrusaderEngine.Facing.NORTH,
			new Point(10, 11),
			CrusaderEngine.Facing.SOUTH,
			true,
			StairPortalSpec.StairMask.STAIR_UP);

		TempleStairLinks.writePortal(2, true, spec);
		assertEquals(spec, TempleStairLinks.readPortal(2, true));
		assertNull(TempleStairLinks.readPortal(2, false));
	}

	@Test
	void hubSpawnUsesHubDownPortalFromTile()
	{
		MazeVariables.clearAll();
		TempleStairLinks.SpawnSpec spawn = TempleStairLinks.hubSpawn();
		StairPortalSpec hub = TempleStairLinks.hubDownPortal();
		assertEquals(hub.from(), spawn.pos());
		assertEquals(hub.spawnFacing(), spawn.facing());
	}

	@Test
	void transitionModeClearsAfterClearTransition() throws Exception
	{
		Database db = TempleCampaignHarness.bootDatabase();
		TempleCampaignHarness.bootMaze(db);
		MazeVariables.set(TempleStairLinks.TRANSITION_MODE, "from_above");
		MazeVariables.set(TempleSeeds.TRANSITION_SOURCE_DEPTH, "1");
		assertEquals(DungeonGenContext.EntryMode.FROM_ABOVE, TempleStairLinks.currentEntryMode());
		TempleStairLinks.clearTransition();
		assertEquals(DungeonGenContext.EntryMode.FRESH, TempleStairLinks.currentEntryMode());
	}
}
