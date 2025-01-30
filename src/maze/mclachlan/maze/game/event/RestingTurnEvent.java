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

package mclachlan.maze.game.event;

import java.util.*;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.ActorGroup;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.ui.diygui.ProgressListener;

/**
 *
 */
public class RestingTurnEvent extends MazeEvent
{
	private int nrTurns;
	private long turnNr;
	private boolean checkRandomEncounters;
	private ProgressListener progress;
	private ActorGroup group;
	private Tile tile;

	/*-------------------------------------------------------------------------*/
	public RestingTurnEvent(int nrTurns, long turnNr, boolean checkRandomEncounters,
		ProgressListener progress, ActorGroup ag, Tile tile)
	{
		this.nrTurns = nrTurns;
		this.checkRandomEncounters = checkRandomEncounters;
		this.turnNr = turnNr;
		this.progress = progress;
		this.group = ag;
		this.tile = tile;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = Maze.getInstance().incTurn(checkRandomEncounters);

		// regen resources from resting
		for (UnifiedActor a : group.getActors())
		{
			regenResources(a);
		}

		progress.incProgress(1);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void regenResources(UnifiedActor a)
	{
		int totHpRegen = GameSys.getInstance().getResourcesToRegenerateWhileResting(
			a, a.getHitPoints(), group, tile);

		int totApRegen = GameSys.getInstance().getResourcesToRegenerateWhileResting(
			a, a.getActionPoints(), group, tile);

		int totMpRegen = GameSys.getInstance().getResourcesToRegenerateWhileResting(
			a, a.getMagicPoints(), group, tile);

		int hp = getRegenThisTurn(totHpRegen, nrTurns, turnNr);
		int ap = getRegenThisTurn(totApRegen, nrTurns, turnNr);
		int mp = getRegenThisTurn(totMpRegen, nrTurns, turnNr);

		a.getHitPoints().incCurrent(hp);
		a.getActionPoints().incCurrent(ap);
		a.getMagicPoints().incCurrent(mp);

		Maze.log(Log.DEBUG, "Resting: "+a.getName()+
			" regen hp["+hp+"] ap["+ap+"] mp["+mp+"]");
	}

	/*-------------------------------------------------------------------------*/
	private static int getRegenThisTurn(int total, int nrTurns, long turnNr)
	{
		// the points per turn. Usually this will be zero
		int basePerTurn = total / nrTurns;

		// the turns on which to accrue one extra point
		int mod;
		if (total > nrTurns)
		{
			mod = nrTurns / (total - (nrTurns*basePerTurn));
		}
		else
		{
			mod = nrTurns / total;
		}

		if (turnNr % mod == 0)
		{
			return basePerTurn + 1;
		}
		else
		{
			return basePerTurn;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		for (int i=0; i<100; i++)
		{
			int regenThisTurn = getRegenThisTurn(113, 100, i);
			System.out.println("regenThisTurn ["+i+"] = [" + regenThisTurn + "]");
		}
	}
}
