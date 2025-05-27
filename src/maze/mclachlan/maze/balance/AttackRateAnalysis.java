/*
 * Copyright (c) 2013 Alan McLachlan
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
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Launcher;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.AttackAction;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.combat.event.StrikeEvent;
import mclachlan.maze.stat.magic.MagicSys;

/**
 *
 */
public class AttackRateAnalysis
{
	public static void main(String[] args) throws Exception
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

		int max = 30;
		analyseWithConstantWeapon(db, cb, max, "Warrior", new PriorityModifierApproach(
			Stats.Modifier.CUT,
			Stats.Modifier.LUNGE,
			Stats.Modifier.SHOOT,
			Stats.Modifier.ARTIFACTS,
			Stats.Modifier.MYTHOLOGY));

		analyseWithConstantWeapon(db, cb, max, "Thief", new PriorityModifierApproach(
			Stats.Modifier.BACKSTAB,
			Stats.Modifier.SNIPE,
			Stats.Modifier.SHOOT,
			Stats.Modifier.ARTIFACTS,
			Stats.Modifier.MYTHOLOGY));
	}

	/*-------------------------------------------------------------------------*/
	private static void analyseWithConstantWeapon(Database db,
		CharacterBuilder cb, int max, String characterClass,
		PriorityModifierApproach modifierApproach)
	{
		System.out.println("-----"+characterClass);
		Item weapon = db.getItemTemplate("Short Sword").create();
		BodyPart torso = db.getBodyPart("human torso");

		for (int level=1; level<=max; level++)
		{
			PlayerCharacter pc = cb.buildCharacter("Tester", characterClass, "Human", "Male", level,
				modifierApproach);

			pc.setPrimaryWeapon(weapon);

			int nrAttacks = GameSys.getInstance().getNrAttacks(pc, true);

			// calculate the average nr of strikes per attack
			float nrStrikes = 0;
			int iterations = 500;
			for (int i=0; i< iterations; i++)
			{
				AttackAction aa = new AttackAction(
					null,
					weapon,
					-1,
					null,
					true,
					GameSys.getInstance().getAttackType(weapon),
					MagicSys.SpellEffectType.NONE);
				aa.setActor(pc);
				aa.setDefender(pc);
				AttackType attackType = aa.getAttackType();

				nrStrikes += GameSys.getInstance().getNrStrikes(pc, null,
					attackType, weapon);
			}
			nrStrikes /= iterations;

			// calculate the average damage output per round
			// assume 50% of strikes miss
			// todo: stealth attacks
			float damage = 0;
			for (int i=0; i<iterations; i++)
			{
				for (int j=0; j<nrAttacks; j++)
				{
					AttackAction aa = new AttackAction(
						null,
						weapon,
						-1,
						null,
						true,
						GameSys.getInstance().getAttackType(weapon),
						MagicSys.SpellEffectType.NONE);
					aa.setActor(pc);
					aa.setDefender(pc);
					AttackType attackType = aa.getAttackType();
					
					int ns = GameSys.getInstance().getNrStrikes(pc, null,
						attackType, weapon);

					for (int k=0; k<ns; k++)
					{
						if (Dice.d100.roll("AttackRateAnalysis") <= 50)
						{
							StrikeEvent ae = new StrikeEvent(
								null, pc, pc, weapon, attackType,
								aa.getDamageType(), null, null, null);
							DamagePacket damagePacket = GameSys.getInstance().calcDamage(ae, new ArrayList());

							// todo: should be using DamageEvent instead?
							damage += (damagePacket.getAmount()*damagePacket.getMultiplier());
						}
					}
				}
			}
			damage /= iterations;

			System.out.format("%2d: CUT%+3d LUN%+3d BAC%+3d nrAttacks %2d nrStrikes %5.2f damage %6.2f%n",
				level,
				pc.getModifier(Stats.Modifier.CUT),
				pc.getModifier(Stats.Modifier.LUNGE),
				pc.getModifier(Stats.Modifier.BACKSTAB),
				nrAttacks, nrStrikes, damage);
		}
	}

}
