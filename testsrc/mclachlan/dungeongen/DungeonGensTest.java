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

package mclachlan.dungeongen;

import mclachlan.dungeongen.fragment.FragmentDungeonGen;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.util.MazeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Campaign-driven {@link DungeonGens} factory.
 */
public class DungeonGensTest extends MazeTestSupport
{
	@Test
	void defaultCampaignUsesNoise4j()
	{
		Campaign campaign = new Campaign(
			"test",
			"Test",
			"",
			null,
			"start",
			"Human",
			"portrait",
			"intro");
		assertTrue(DungeonGens.createDefault(campaign) instanceof Noise4jDungeonGen);
		assertEquals(DungeonGens.NOISE4J, DungeonGens.idsFor(campaign).get(0));
	}

	@Test
	void configuredCampaignListsBuiltInsOnly()
	{
		Campaign campaign = new Campaign(
			"test",
			"Test",
			"",
			null,
			"start",
			"Human",
			"portrait",
			"intro");
		campaign.setDungeonGenerators(Campaign.parseCommaList("fragment,noise4j,wfc"));
		campaign.setDefaultDungeonGenerator(DungeonGens.FRAGMENT);

		assertEquals(DungeonGens.FRAGMENT, campaign.getDefaultDungeonGenerator());
		assertEquals(
			java.util.List.of(DungeonGens.FRAGMENT, DungeonGens.NOISE4J),
			DungeonGens.idsFor(campaign));
		assertTrue(DungeonGens.createDefault(campaign) instanceof FragmentDungeonGen);
	}

	@Test
	void builtInIdsListsRegisteredGenerators()
	{
		assertEquals(
			java.util.List.of(DungeonGens.NOISE4J, DungeonGens.FRAGMENT),
			DungeonGens.builtInIds());
	}

	@Test
	void unknownGeneratorIdThrows()
	{
		assertThrows(MazeException.class, () -> DungeonGens.create("not-a-generator"));
	}
}
