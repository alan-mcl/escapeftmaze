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

package mclachlan.maze.game;

import java.awt.Point;
import java.util.List;
import mclachlan.maze.map.Zone;
import mclachlan.maze.test.support.MazeTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auto-map exploration is keyed by {@link Zone#getTilesVisitedKey()}.
 */
public class PlayerTilesVisitedTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void defaultsToZoneName()
	{
		Zone zone = new Zone();
		zone.setName("Gatehouse");
		assertEquals("Gatehouse", zone.getTilesVisitedKey());

		PlayerTilesVisited visited = new PlayerTilesVisited();
		Point tile = new Point(3, 4);
		visited.visitTile(zone.getTilesVisitedKey(), tile);
		assertTrue(visited.hasVisited(zone.getTilesVisitedKey(), tile));
		assertEquals(List.of(tile), visited.getTilesVisited(zone.getTilesVisitedKey()));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void overrideKeepsSeparateVisitSets()
	{
		Zone zone = new Zone();
		zone.setName("shared");
		PlayerTilesVisited visited = new PlayerTilesVisited();
		Point tile = new Point(2, 2);

		zone.setTilesVisitedKey("shared#1");
		visited.visitTile(zone.getTilesVisitedKey(), tile);
		assertTrue(visited.hasVisited("shared#1", tile));

		zone.setTilesVisitedKey("shared#2");
		assertFalse(visited.hasVisited(zone.getTilesVisitedKey(), tile),
			"a different tilesVisitedKey should not inherit the other instance's map");

		visited.visitTile(zone.getTilesVisitedKey(), tile);
		assertTrue(visited.hasVisited("shared#2", tile));

		zone.setTilesVisitedKey("shared#1");
		assertTrue(visited.hasVisited(zone.getTilesVisitedKey(), tile),
			"returning to the first key should restore that auto-map");
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void recentTrailResetsWithoutClearingZoneHistory()
	{
		PlayerTilesVisited visited = new PlayerTilesVisited();
		Point tile = new Point(1, 1);
		visited.visitTile("shared#1", tile);
		assertEquals(1, visited.getRecentTiles().size());

		visited.resetRecentTiles();
		assertTrue(visited.getRecentTiles().isEmpty());
		assertTrue(visited.hasVisited("shared#1", tile));
	}
}
