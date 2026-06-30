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

import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.StubActor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tier 3: rules formulas in {@link GameSys} exercised with synthetic
 * {@link StubActor}s and seeded {@link Dice}.
 */
public class GameSysTest extends MazeTestSupport
{
	private final GameSys gameSys = new GameSys();

	/*-------------------------------------------------------------------------*/
	@Test
	void initiativeIsDeterministicUnderSeed()
	{
		StubActor a = new StubActor("a")
			.withModifier(Stats.Modifier.SKILL, 3)
			.withModifier(Stats.Modifier.INITIATIVE, 2);

		seed(123L);
		int first = gameSys.calcInitiative(a);
		seed(123L);
		int second = gameSys.calcInitiative(a);

		assertEquals(first, second);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void initiativeIsDiceRollPlusModifiers()
	{
		int skill = 3, initiative = 2;
		StubActor a = new StubActor("a")
			.withModifier(Stats.Modifier.SKILL, skill)
			.withModifier(Stats.Modifier.INITIATIVE, initiative);

		// a single d6 roll plus the static modifiers => [1+mods .. 6+mods]
		int base = skill + initiative;
		for (long s = 0; s < 50; s++)
		{
			seed(s);
			int result = gameSys.calcInitiative(a);
			assertTrue(result >= base + 1 && result <= base + 6,
				"initiative out of range: " + result);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void suppliesNeededToRest()
	{
		assertEquals(2, gameSys.getSuppliesNeededToRest(new StubActor("a")));
		assertEquals(5, gameSys.getSuppliesNeededToRest(
			new StubActor("b").withModifier(Stats.Modifier.SUPPLY_CONSUMPTION, 3)));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void resistanceIsDefenderModifierMinusAttackerPower()
	{
		StubActor defender = new StubActor("def")
			.withModifier(Stats.Modifier.RESIST_FIRE, 40);
		StubActor attacker = new StubActor("att")
			.withModifier(Stats.Modifier.POWER, 10)
			.withModifier(Stats.Modifier.POWER_CAST, 5);

		// 40 - (10 + 2*5) = 20
		assertEquals(20, gameSys.getResistance(defender, attacker,
			MagicSys.SpellEffectType.FIRE));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void resistanceFloorsAtZero()
	{
		StubActor defender = new StubActor("def")
			.withModifier(Stats.Modifier.RESIST_FIRE, 5);
		StubActor attacker = new StubActor("att")
			.withModifier(Stats.Modifier.POWER, 50);

		assertEquals(0, gameSys.getResistance(defender, attacker,
			MagicSys.SpellEffectType.FIRE));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void resistanceWithoutResistanceModifierIsZeroBeforeFloor()
	{
		// NONE has no resistance modifier, so the defender modifier is 0
		StubActor defender = new StubActor("def");
		StubActor attacker = new StubActor("att");

		assertEquals(0, gameSys.getResistance(defender, attacker,
			MagicSys.SpellEffectType.NONE));
	}
}
