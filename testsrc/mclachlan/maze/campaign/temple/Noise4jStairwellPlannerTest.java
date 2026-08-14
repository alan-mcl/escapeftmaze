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
import mclachlan.dungeongen.DungeonGenContext;
import mclachlan.dungeongen.StairPortalSpec;
import mclachlan.dungeongen.StairwellPlan;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.test.support.MazeTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hermetic checks for blank-wall stair placement on a tiny synthetic layout.
 */
public class Noise4jStairwellPlannerTest extends MazeTestSupport
{
	private static final float WALL = Noise4jDungeonGen.WALL_THRESHOLD / 10F;
	private static final float ROOM = Noise4jDungeonGen.ROOM_THRESHOLD / 10F;
	private static final float CORRIDOR = Noise4jDungeonGen.CORRIDOR_THRESHOLD / 10F;

	@Test
	void upStairsPreferBlankWallOverCorridorDoorSite()
	{
		Grid grid = roomWithNorthCorridorStub();
		Point origin = new Point(3, 3);

		StairwellPlan plan = new Noise4jStairwellPlanner().planStairwells(
			null,
			grid,
			1,
			origin,
			DungeonGenContext.FRESH);

		StairPortalSpec up = plan.stairsUp();
		assertNotNull(up);
		assertNotEquals(new Point(3, 2), up.from(),
			"stairs-up must not sit on the room tile facing the corridor door");
		assertEquals(WALL, grid.get(up.to().x, up.to().y),
			"stairs-up to-tile should be solid wall, not walkable corridor");
	}

	@Test
	void fromHubSpawnFacesAwayFromUpStairWall()
	{
		Grid grid = roomWithNorthCorridorStub();
		Point origin = new Point(3, 3);

		StairwellPlan plan = new Noise4jStairwellPlanner().planStairwells(
			null,
			grid,
			1,
			origin,
			DungeonGenContext.builder()
				.entryMode(DungeonGenContext.EntryMode.FROM_HUB)
				.build());

		StairPortalSpec up = plan.stairsUp();
		assertNotNull(up);
		assertEquals(up.from(), plan.spawn());
		assertEquals(StairPortalSpec.oppositeFacing(up.fromFacing()), plan.spawnFacing(),
			"spawn facing should look away from the stair mask");
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * 7×7: walled border, 5×4 starting room, corridor stub on the north edge.
	 * Origin sits in the room centre; the tile north of origin faces the corridor.
	 */
	private static Grid roomWithNorthCorridorStub()
	{
		Grid grid = new Grid(WALL, 7, 7);
		for (int x = 1; x <= 5; x++)
		{
			for (int y = 2; y <= 5; y++)
			{
				grid.set(x, y, ROOM);
			}
			grid.set(x, 1, CORRIDOR);
		}
		return grid;
	}
}
