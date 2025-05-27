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
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.FoeTemplate;
import mclachlan.maze.stat.PlayerCharacter;

import static mclachlan.maze.balance.MockCombat.getRefCombatPC;

/**
 *
 */
public class FoeScorer
{
	private final Database db;
	private final MockCombat mockCombat;
	private final Maze maze;
	private final CharacterBuilder characterBuilder;

	/*-------------------------------------------------------------------------*/
	public FoeScorer(Database db) throws Exception
	{
		this.db = db;
		mockCombat = new MockCombat();
		maze = MockCombat.getMockMaze(db);
		characterBuilder = new CharacterBuilder(db);
	}

	/*-------------------------------------------------------------------------*/
	public double scoreFoe(FoeTemplate ft) throws Exception
	{
		double result = 0D;

		// foe level
		result += ft.getLevelRange().getAverage();

		// damage per round
		result += avgDamagePerRound(ft);

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Average Damage per round vs a combat focus PC of the same level
	 */
	public double avgDamagePerRound(FoeTemplate ft) throws Exception
	{
		double result = 0D;

		Foe foe = ft.create();

		int max = 5;
		for (int i=0; i< max; i++)
		{
			PlayerCharacter pc = getRefCombatPC(characterBuilder, foe.getLevel());
			mockCombat.singleTest(db, maze, pc, foe);

			result += (pc.getHitPoints().getMaximum() - pc.getHitPoints().getCurrent());
		}

		return result/max;
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V2Loader();
		Saver saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		Map<String, FoeTemplate> map = db.getFoeTemplates();

		FoeScorer s = new FoeScorer(db);

		for (FoeTemplate t : map.values())
		{
			try
			{
				System.out.println(t.getName() + "," + s.scoreFoe(t));
			}
			catch (Exception e)
			{
				System.out.println("!!! " + t.getName());
				throw e;
			}
		}
	}
}
