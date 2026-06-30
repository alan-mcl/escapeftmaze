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

package mclachlan.maze.test.support;

import mclachlan.crusader.EngineObject;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.npc.NpcFaction;

/**
 * Builds a synthetic, in-memory {@link Database} for the hermetic test suite.
 * <p>
 * The returned database depends only on {@code maze.cfg} (read by
 * {@code Launcher.getConfig()} for the standard app wiring) and never touches
 * the {@code data/} campaign content. Tests populate the {@link InMemoryLoader}
 * maps with just the fixtures they need before (or after) building the database.
 */
public class TestData
{
	/*-------------------------------------------------------------------------*/
	/**
	 * A campaign with no parent so the database only ever consults the single
	 * in-memory loader.
	 */
	public static Campaign campaign()
	{
		return new Campaign(
			"test",
			"Test Campaign",
			"Synthetic campaign for unit tests",
			null,            // no parent campaign
			null,            // no starting script
			null,            // no default race
			null,            // no default portrait
			null);           // no intro script
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Construct a {@link Database} backed by the given loader and a no-op saver,
	 * and initialise the single campaign cache. Sets the {@code Database}
	 * singleton as a side effect (cleared by {@code MazeTestSupport}).
	 */
	public static Database buildDatabase(InMemoryLoader loader) throws Exception
	{
		Database db = new Database(loader, new InMemorySaver(), campaign());
		db.initImpls();
		return db;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Convenience: an empty but fully-valid database.
	 */
	public static Database buildEmptyDatabase() throws Exception
	{
		return buildDatabase(new InMemoryLoader());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * A self-contained synthetic combat {@link Foe} that needs no database
	 * cross-references. Mirrors the reference foe used by the manual
	 * {@code MockCombat} harness, scaled by level.
	 */
	public static Foe referenceFoe(int level)
	{
		return referenceFoe(level, null);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * As {@link #referenceFoe(int)} but with the named natural weapons (which
	 * must be registered in the active {@link Database}).
	 */
	public static Foe referenceFoe(int level, java.util.List<String> naturalWeapons)
	{
		PercentageTable<BodyPart> bodyParts = new PercentageTable<>();
		bodyParts.add(new BodyPart("bp", "bp", new StatModifier(), 0, 0, 0,
			EquipableSlot.Type.MISC_ITEM), 100);

		PercentageTable<String> playerBodyParts = new PercentageTable<>();
		playerBodyParts.add("head", 18);
		playerBodyParts.add("torso", 33);
		playerBodyParts.add("leg", 31);
		playerBodyParts.add("hand", 8);
		playerBodyParts.add("foot", 10);

		StatModifier stats = new StatModifier();
		stats.setModifier(Stats.Modifier.BRAWN, level);
		stats.setModifier(Stats.Modifier.SKILL, level);

		FoeTemplate ft = new FoeTemplate(
			"TestingFoe",
			"TestingFoes",
			"???",
			"???s",
			null,
			null,
			null,
			new Dice(20, 1, level * 9),
			new Dice(20, 1, level * 9),
			new Dice(20, 1, level * 9),
			new Dice(level, 1, 0),
			level * 100,
			stats,
			bodyParts,
			playerBodyParts,
			null,
			null,
			null,
			null,
			null,
			EngineObject.Alignment.BOTTOM,
			null,
			null,
			Foe.EvasionBehaviour.NEVER_EVADE,
			true,
			0,
			new StatModifier(),
			new StatModifier(),
			0,
			Foe.StealthBehaviour.NOT_STEALTHY,
			null,
			false,
			null,
			new ObjectAnimations(),
			FoeTemplate.AppearanceDirection.FROM_LEFT_OR_RIGHT,
			null,                  // death script
			naturalWeapons,        // natural weapons (by name)
			null,                  // spell book
			null,                  // spell-like abilities
			CharacterClass.Focus.COMBAT,
			NpcFaction.Attitude.ATTACKING,
			null,                  // allies on call
			null);                 // foe speech

		return new Foe(ft);
	}

	/*-------------------------------------------------------------------------*/
	/** A minimal {@link Gender} with no modifiers. */
	public static Gender basicGender(String name)
	{
		return new Gender(name, new StatModifier(), new StatModifier(),
			new StatModifier());
	}

	/*-------------------------------------------------------------------------*/
	private static BodyPart basicBodyPart(String name, EquipableSlot.Type type)
	{
		return new BodyPart(name, name, new StatModifier(), 0, 0, 0, type);
	}

	/*-------------------------------------------------------------------------*/
	/** A simple melee {@link NaturalWeapon} (1d6, no attack-type strings). */
	public static NaturalWeapon clawNaturalWeapon()
	{
		NaturalWeapon nw = new NaturalWeapon();
		nw.setName("claw");
		nw.setDescription("a claw");
		nw.setRanged(false);
		nw.setDamage(new Dice(1, 6, 0));
		nw.setDamageType(MagicSys.SpellEffectType.NONE);
		nw.setModifiers(new StatModifier());
		nw.setMinRange(0);
		nw.setMaxRange(16);
		nw.setAttacks(new int[]{1});
		nw.setAttackScript(new mclachlan.maze.game.MazeScript(
			"claw attack", new java.util.ArrayList<>()));
		return nw;
	}

	/*-------------------------------------------------------------------------*/
	/** A minimal {@link Race} with all five body parts and the given gender. */
	public static Race basicRace(String name, Gender gender)
	{
		return basicRace(name, gender, null);
	}

	/*-------------------------------------------------------------------------*/
	/** As {@link #basicRace(String, Gender)} but with explicit natural weapons. */
	public static Race basicRace(String name, Gender gender,
		java.util.List<NaturalWeapon> naturalWeapons)
	{
		return new Race(
			name,
			name,
			100, 100, 100,             // hp/ap/mp percent
			new StatModifier(),        // starting modifiers
			new StatModifier(),        // constant modifiers
			new StatModifier(),        // banner modifiers
			new StatModifier(),        // attribute ceilings
			basicBodyPart("head", EquipableSlot.Type.HELM),
			basicBodyPart("torso", EquipableSlot.Type.TORSO_ARMOUR),
			basicBodyPart("leg", EquipableSlot.Type.LEG_ARMOUR),
			basicBodyPart("hand", EquipableSlot.Type.GLOVES),
			basicBodyPart("foot", EquipableSlot.Type.BOOTS),
			null, null,
			new java.util.ArrayList<>(java.util.List.of(gender)),
			false,                     // not magic dead
			null,                      // no special ability
			null,                      // no starting items
			naturalWeapons,            // natural weapons
			null,                      // no suggested names
			null, null,                // unlock variable/description
			null,                      // no favoured enemy modifier
			null);                     // no character creation image
	}

	/*-------------------------------------------------------------------------*/
	/** A minimal COMBAT-focus {@link CharacterClass} usable by everyone. */
	public static CharacterClass basicCharacterClass(String name)
	{
		return new CharacterClass(
			name,
			CharacterClass.Focus.COMBAT,
			name,
			20, 10, 0,                 // starting hp/ap/mp
			new StatModifier(),        // starting active modifiers
			new StatModifier(),        // starting modifiers
			new StatModifier(),        // unlock modifiers
			null,                      // allowed genders (null = all)
			null,                      // allowed races (null = all)
			new ExperienceTableArray(name, new int[]{0, 0, 1000}, 2000),
			new Dice(1, 6, 2),         // level-up hp
			new Dice(1, 4, 0),         // level-up ap
			new Dice(1, 1, 0),         // level-up mp
			2,                         // level-up assignable modifiers
			new StatModifier(),        // level-up modifiers
			new LevelAbilityProgression());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Builds a fresh level-1 {@link PlayerCharacter} from synthetic
	 * race/class/gender via the production {@link Leveler}. Requires the
	 * {@link mclachlan.maze.game.Maze} systems to be booted (see
	 * {@link HeadlessMaze}).
	 */
	public static PlayerCharacter newLevel1Pc(String name)
	{
		Gender gender = basicGender("Neuter");
		Race race = basicRace("Testarossa", gender);
		CharacterClass cc = basicCharacterClass("Tester");

		return new Leveler().createNewPlayerCharacter(
			name, cc, race, gender, "no-portrait", null, null);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * A combat-ready level-1 {@link PlayerCharacter}: like {@link #newLevel1Pc}
	 * but armed with a natural weapon so it can attack without the database's
	 * unarmed-combat content. Some {@code BRAWN}/{@code SKILL} is granted so it
	 * actually lands hits in a smoke test.
	 */
	public static PlayerCharacter newCombatPc(String name)
	{
		Gender gender = basicGender("Neuter");
		Race race = basicRace("Testarossa", gender,
			new java.util.ArrayList<>(java.util.List.of(clawNaturalWeapon())));
		CharacterClass cc = basicCharacterClass("Tester");

		PlayerCharacter pc = new Leveler().createNewPlayerCharacter(
			name, cc, race, gender, "no-portrait", null, null);

		StatModifier sm = new StatModifier();
		sm.setModifier(Stats.Modifier.BRAWN, 5);
		sm.setModifier(Stats.Modifier.SKILL, 5);
		pc.applyPermanentStatModifier(sm);

		return pc;
	}
}
