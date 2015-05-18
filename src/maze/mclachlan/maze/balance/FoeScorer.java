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
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.FoeTemplate;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class FoeScorer
{
	/*-------------------------------------------------------------------------*/
	public double scoreFoe(FoeTemplate ft)
	{
		double result = 0D;

		// foe level
		result += ft.getLevelRange().getAverage();

		// damage per round
		result += scoreDamagePerRound(ft);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public double scoreDamagePerRound(FoeTemplate ft)
	{
		double result = 0D;

		throw new MazeException("todo: implement this");

		/*PercentageTable<FoeAttack> attacks = ft.getAttacks();

		for (FoeAttack fa : attacks.getItems())
		{
			double perc = attacks.getPercentage(fa)/100D;

			switch (fa.getType())
			{
				case MELEE_ATTACK:
				case RANGED_ATTACK:

					double aveDam = fa.getDamage().getAverage();
					int swings = 0;
					for (int i : fa.getAttacks())
					{
						swings += i;
					}

					result += (aveDam * swings * perc);
					break;
				case CAST_SPELL:
					// todo
					break;
				case SPECIAL_ABILITY:
					// todo
					break;
				default: throw new MazeException(fa.getType().name());
			}
		}

		return result; */
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		Database db = new Database(loader, saver);
		Campaign campaign = Maze.getStubCampaign();
		loader.init(campaign);

		Map<String, FoeTemplate> map = db.getFoeTemplates();

		FoeScorer s = new FoeScorer();

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
