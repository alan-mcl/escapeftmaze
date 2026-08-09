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

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.test.support.HeadlessMaze;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CharacterSelection} automatic resolution.
 */
public class CharacterSelectionTest extends MazeTestSupport
{
	private Database db;
	private Maze maze;

	@BeforeEach
	void setUp() throws Exception
	{
		// Modifier selection uses getModifier(), which needs live Maze/GameSys/DB.
		db = TestData.buildEmptyDatabase();
		maze = HeadlessMaze.boot(db);
	}

	@AfterEach
	void tearDownLocal()
	{
		db = null;
		maze = null;
	}

	private static PlayerCharacter pc(String name, Stats.Modifier mod, int value)
	{
		PlayerCharacter pc = TestData.newLevel1Pc(name);
		StatModifier sm = new StatModifier();
		sm.setModifier(mod, value);
		pc.applyPermanentStatModifier(sm);
		return pc;
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void exclusionsShrinkPoolBeforeMethods()
	{
		List<PlayerCharacter> party = List.of(
			pc("Low", Stats.Modifier.BRAWN, 1),
			pc("Mid", Stats.Modifier.BRAWN, 5),
			pc("High", Stats.Modifier.BRAWN, 10));

		CharacterSelection selection = new CharacterSelection(
			List.of(new RandomCharacterSelection()),
			List.of(new HighestModifierSelection(Stats.Modifier.BRAWN)));

		List<PlayerCharacter> excluded = selection.getExcluded(party);
		assertEquals(1, excluded.size());
		assertEquals("High", excluded.get(0).getName());

		seed(42L);
		List<PlayerCharacter> selected = selection.selectAutomatic(party);
		assertEquals(1, selected.size());
		assertNotEquals("High", selected.get(0).getName());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void sequentialRandomThenModifierComparison()
	{
		List<PlayerCharacter> party = List.of(
			pc("A", Stats.Modifier.FLIER, 0),
			pc("B", Stats.Modifier.FLIER, 1),
			pc("C", Stats.Modifier.FLIER, 1));

		CharacterSelection selection = new CharacterSelection(
			List.of(
				new RandomCharacterSelection(),
				new ModifierComparisonSelection(
					Stats.Modifier.FLIER, ComparisonOperator.GT, 0)),
			List.of());

		seed(7L);
		List<PlayerCharacter> selected = selection.selectAutomatic(party);
		assertTrue(selected.size() >= 2);
		assertTrue(selected.stream().anyMatch(p -> "B".equals(p.getName())));
		assertTrue(selected.stream().anyMatch(p -> "C".equals(p.getName())));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void twoRandomStepsPickTwoDistinctCharacters()
	{
		List<PlayerCharacter> party = List.of(
			pc("A", Stats.Modifier.BRAWN, 1),
			pc("B", Stats.Modifier.BRAWN, 2),
			pc("C", Stats.Modifier.BRAWN, 3));

		CharacterSelection selection = new CharacterSelection(
			List.of(new RandomCharacterSelection(), new RandomCharacterSelection()),
			List.of());

		seed(99L);
		List<PlayerCharacter> selected = selection.selectAutomatic(party);
		assertEquals(2, selected.size());
		assertNotEquals(selected.get(0).getName(), selected.get(1).getName());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void twoPlayerMethodsRequireWhoSteps()
	{
		CharacterSelection selection = new CharacterSelection(
			List.of(new PlayerCharacterSelection(), new PlayerCharacterSelection()),
			List.of());

		assertTrue(selection.requiresPlayerSelection());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void playerStepCandidatesExcludeAlreadySelected()
	{
		List<PlayerCharacter> party = List.of(
			pc("A", Stats.Modifier.BRAWN, 1),
			pc("B", Stats.Modifier.BRAWN, 2),
			pc("C", Stats.Modifier.BRAWN, 3));

		Set<PlayerCharacter> already = Set.of(party.get(0));
		List<PlayerCharacter> candidates = CharacterSelection.candidatesFor(
			new PlayerCharacterSelection(), party, List.of(), already);

		assertEquals(2, candidates.size());
		assertFalse(candidates.contains(party.get(0)));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void raceLargeSizeExcludedFromPlayerCandidates()
	{
		PlayerCharacter large = pc("Neotroll", Stats.Modifier.BRAWN, 1);
		PlayerCharacter normal = pc("Human", Stats.Modifier.BRAWN, 1);

		Gender gender = TestData.basicGender("Neuter");
		Race largeRace = TestData.basicRace("Neotroll", gender);
		largeRace.getConstantModifiers().setModifier(Stats.Modifier.LARGE_SIZE, 1);
		large.setRace(largeRace);

		// race LARGE_SIZE is not a base modifier — selection must use getModifier()
		assertEquals(0, large.getBaseModifier(Stats.Modifier.LARGE_SIZE));
		assertTrue(large.getModifier(Stats.Modifier.LARGE_SIZE) > 0);

		List<PlayerCharacter> party = List.of(large, normal);
		CharacterSelection selection = new CharacterSelection(
			List.of(new PlayerCharacterSelection()),
			List.of(new ModifierComparisonSelection(
				Stats.Modifier.LARGE_SIZE, ComparisonOperator.GT, 0)));

		List<PlayerCharacter> excluded = selection.getExcluded(party);
		assertEquals(1, excluded.size());
		assertEquals("Neotroll", excluded.get(0).getName());

		List<PlayerCharacter> candidates = CharacterSelection.candidatesFor(
			new PlayerCharacterSelection(), party, excluded, Set.of());
		assertEquals(1, candidates.size());
		assertEquals("Human", candidates.get(0).getName());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void modifierComparisonUsesPartyMinusExclusionsOnly()
	{
		List<PlayerCharacter> party = List.of(
			pc("A", Stats.Modifier.FLIER, 1),
			pc("B", Stats.Modifier.FLIER, 1),
			pc("C", Stats.Modifier.FLIER, 0));

		Set<PlayerCharacter> already = Set.of(party.get(0));
		List<PlayerCharacter> candidates = CharacterSelection.candidatesFor(
			new ModifierComparisonSelection(
				Stats.Modifier.FLIER, ComparisonOperator.GT, 0),
			party,
			List.of(party.get(2)),
			already);

		assertEquals(2, candidates.size());
		assertTrue(candidates.contains(party.get(0)));
		assertTrue(candidates.contains(party.get(1)));
	}
}
