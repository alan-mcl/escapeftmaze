/*
 * Copyright (c) 2012 Alan McLachlan
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

package mclachlan.maze.balance;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.game.Launcher;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;

/**
 *
 */
public class CharacterBuilder
{
	private Database db;

	/*-------------------------------------------------------------------------*/
	public CharacterBuilder(Database db)
	{
		this.db = db;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter buildCharacter(
		String name,
		String characterClass,
		String characterRace,
		String characterGender,
		int level,
		ModifierApproach modifierApproach
		// todo: spells, bonuses
	)
	{
		Leveler leveler = new Leveler();

		CharacterClass cc = db.getCharacterClass(characterClass);
		Race race = db.getRace(characterRace);
		Gender gender = db.getGenders().get(characterGender);

		// create a new lvl 1 character
		PlayerCharacter pc = new Leveler().createNewPlayerCharacter(
			name, cc, race, gender, "null portrait", null, null);

		pc.setPersonality(db.getPersonalities().get("Team Player"));

		// todo: other character creation stuff
		StatModifier sm = new StatModifier();
		modifierApproach.incModifiers(
			pc, sm, GameSys.getInstance().getAssignableModifiersOnCharacterCreation());
		leveler.applyModifiers(pc, sm);

		// level up the requisite nr of times
		for (int i = 1; i < level; i++)
		{
			levelUp(pc, modifierApproach, leveler);
		}

		return pc;
	}

	/*-------------------------------------------------------------------------*/
	public void levelUp(
		PlayerCharacter pc,
		ModifierApproach modifierApproach,
		Leveler leveler)
	{
		Leveler.LevelUpState state = new Leveler.LevelUpState(pc, 0);

		// apply bonus
		// todo: bonus selection
		leveler.applyBonus(pc, state, Leveler.BONUS_MODIFIERS, null);

		// apply basics
		leveler.applyInitialChanges(pc, state);

		// apply modifiers
		StatModifier sm = new StatModifier();
		modifierApproach.incModifiers(pc, sm, leveler.getAssignableModifiers(pc, state));
		leveler.applyModifiers(pc, sm);

		// apply spells
		// todo: spell selection
		leveler.applySpells(pc, new ArrayList());
	}

	/*-------------------------------------------------------------------------*/
	private static CharacterBuilder getCharacterBuilder() throws Exception
	{
		Loader loader = new V2Loader();
		Saver saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		Maze maze = new Maze(Launcher.getConfig(), Maze.getStubCampaign());

//		maze.initLog();
		maze.initState();
//		maze.initDb();
		maze.initSystems();
//		maze.initUi();
//		maze.startThreads();

		CharacterBuilder cb = new CharacterBuilder(db);
		return cb;
	}

	/*-------------------------------------------------------------------------*/
	public static abstract class ModifierApproach
	{
		public abstract void incModifiers(PlayerCharacter pc, StatModifier sm,
			int nrModifiers);
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		int level = 15;
		PlayerCharacter pc = MockCombat.getRefCombatPC(getCharacterBuilder(), 5);

		System.out.println("pc = [" + pc + "]");
		System.out.println("HP: " + pc.getHitPoints() + ", " +
			"SP: " + pc.getActionPoints() + ", " +
			"MP: " + pc.getMagicPoints());
		System.out.println(pc.getStats().getModifiers());
	}
}

