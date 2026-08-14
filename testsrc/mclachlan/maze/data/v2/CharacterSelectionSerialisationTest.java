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

package mclachlan.maze.data.v2;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.serialisers.V2SerialiserFactory;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.ForcePartySplitEvent;
import mclachlan.maze.game.event.ZoneChangeEvent;
import mclachlan.maze.map.script.FallingDamageEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static mclachlan.maze.data.v2.serialisers.V2SerialiserFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V2 round-trip tests for {@link CharacterSelection} and related types.
 */
public class CharacterSelectionSerialisationTest extends MazeTestSupport
{
	private static Database db;

	@BeforeAll
	void setUp() throws Exception
	{
		db = TestData.buildEmptyDatabase();
	}

	@AfterAll
	void tearDown()
	{
		Database.resetInstanceForTesting();
		db = null;
	}

	/*-------------------------------------------------------------------------*/
	private <T> void assertRoundTrips(V2SerialiserMap<T> serialiser, T object)
	{
		Map map1 = serialiser.toObject(object, db);
		T restored = serialiser.fromObject(map1, db);
		Map map2 = serialiser.toObject(restored, db);
		assertEquals(map1, map2);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void characterSelectionMethodRoundTrip()
	{
		assertRoundTrips(
			getCharacterSelectionMethodSerialiser(),
			new ModifierComparisonSelection(
				Stats.Modifier.FLIER, ComparisonOperator.GT, 0));
		assertRoundTrips(
			getCharacterSelectionMethodSerialiser(),
			new LowestModifierSelection(Stats.Modifier.SNEAKING));
		assertRoundTrips(
			getCharacterSelectionMethodSerialiser(),
			new PlayerCharacterSelection());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void characterSelectionRoundTrip()
	{
		CharacterSelection original = new CharacterSelection(
			List.of(
				new PlayerCharacterSelection(),
				new RandomCharacterSelection(),
				new ModifierComparisonSelection(
					Stats.Modifier.FLIER, ComparisonOperator.GTE, 1)),
			List.of(new HighestModifierSelection(Stats.Modifier.BRAWN)));

		assertRoundTrips(getCharacterSelectionSerialiser(), original);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void forcePartySplitEventRoundTrip()
	{
		V2SerialiserMap<MazeEvent> eventSerialiser =
			V2SerialiserFactory.getRegisteredMazeEventSerialiser(
				db, ForcePartySplitEvent.class);

		ForcePartySplitEvent original = new ForcePartySplitEvent(
			new CharacterSelection(
				List.of(new LowestModifierSelection(Stats.Modifier.BRAWN)),
				List.of()));

		assertRoundTrips(eventSerialiser, original);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void fallingDamageEventRoundTrip()
	{
		V2SerialiserMap<MazeEvent> eventSerialiser =
			V2SerialiserFactory.getRegisteredMazeEventSerialiser(
				db, FallingDamageEvent.class);

		FallingDamageEvent original = new FallingDamageEvent(new Dice(2, 6, 3));

		assertRoundTrips(eventSerialiser, original);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void zoneChangeEventRoundTrip()
	{
		V2SerialiserMap<MazeEvent> eventSerialiser =
			V2SerialiserFactory.getRegisteredMazeEventSerialiser(
				db, ZoneChangeEvent.class);

		assertRoundTrips(eventSerialiser,
			new ZoneChangeEvent("Gatehouse", new Point(14, 30), 1));
		assertRoundTrips(eventSerialiser,
			new ZoneChangeEvent("Temple Hub", new Point(13, 8), 4, "screen.aurora"));
	}
}
