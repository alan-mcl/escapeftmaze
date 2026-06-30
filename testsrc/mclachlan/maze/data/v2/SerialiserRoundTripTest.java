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

import java.io.*;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.serialisers.V2SerialiserFactory;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.combat.WieldingCombo;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tier 2: serialisation round-trips on synthetic objects.
 * <p>
 * Many domain classes have no value {@code equals}, so each test asserts
 * {@code toObject -> fromObject -> toObject} produces equal JSON maps rather
 * than comparing objects directly.
 */
public class SerialiserRoundTripTest extends MazeTestSupport
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
	/**
	 * Normalises a serialiser-produced map by passing it through the JSON layer,
	 * exactly as the real persistence path does. This is important: the maps
	 * returned by the serialisers use a custom-ordering {@link java.util.TreeMap}
	 * that does not behave like the plain maps gson produces on load.
	 */
	private Map normalise(Map map)
	{
		try
		{
			StringWriter sw = new StringWriter();
			V2Utils.writeJson(map, sw);
			return V2Utils.getMap(new BufferedReader(new StringReader(sw.toString())));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	private <T> void assertRoundTrips(V2SerialiserMap<T> serialiser, T object)
	{
		Map map1 = normalise(serialiser.toObject(object, db));
		T restored = serialiser.fromObject(map1, db);
		Map map2 = normalise(serialiser.toObject(restored, db));
		assertEquals(map1, map2, "serialised form must be stable across a round trip");
	}

	/*-------------------------------------------------------------------------*/
	private StatModifier mods()
	{
		StatModifier sm = new StatModifier();
		sm.setModifier(Stats.Modifier.BRAWN, 2);
		sm.setModifier(Stats.Modifier.SWING, -1);
		return sm;
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void gender()
	{
		Gender g = new Gender("Neuter", mods(), new StatModifier(), new StatModifier());
		assertRoundTrips(V2SerialiserFactory.getGenderSerialiser(), g);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void bodyPart()
	{
		BodyPart bp = new BodyPart("head", "Head", mods(), 2, 50, 0,
			EquipableSlot.Type.HELM);
		assertRoundTrips(V2SerialiserFactory.getBodyPartSerialiser(), bp);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void attackType()
	{
		AttackType at = new AttackType("slash", "slashes", Stats.Modifier.SWING,
			MagicSys.SpellEffectType.NONE, mods());
		assertRoundTrips(V2SerialiserFactory.getAttackTypeSerialiser(), at);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void experienceTableArray()
	{
		ExperienceTable t = new ExperienceTableArray("xp",
			new int[]{0, 0, 1000, 3000}, 5000);
		assertRoundTrips(V2SerialiserFactory.getExperienceTableSerialiser(), t);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void wieldingCombo()
	{
		WieldingCombo wc = new WieldingCombo("dual", "Sword", "Dagger", mods());
		assertRoundTrips(V2SerialiserFactory.getWieldingComboSerialiser(), wc);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void personality()
	{
		Map<String, String> speech = new HashMap<>();
		speech.put("greeting", "Hello there");
		Personality p = new Personality("gruff", "A gruff sort", speech,
			new java.awt.Color(10, 20, 30));
		assertRoundTrips(V2SerialiserFactory.getPersonalitySerialiser(), p);
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void naturalWeapon()
	{
		NaturalWeapon nw = new NaturalWeapon();
		nw.setName("claw");
		nw.setDescription("a vicious claw");
		nw.setRanged(false);
		nw.setDamage(new Dice(1, 4, 0));
		nw.setDamageType(MagicSys.SpellEffectType.NONE);
		nw.setModifiers(mods());
		nw.setMinRange(0);
		nw.setMaxRange(0);
		nw.setAttacks(new int[]{1});
		assertRoundTrips(V2SerialiserFactory.getNaturalWeaponSerialiser(db), nw);
	}
}
