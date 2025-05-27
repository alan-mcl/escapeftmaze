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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.script.EncounterActorsEvent;
import mclachlan.maze.stat.ActorGroup;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.ui.diygui.ProgressListener;

/**
 *
 */
public class RestingCheckpointEvent extends MazeEvent
{
	private int nrTurns, nextNrTurns;
	private long turnNr;
	private boolean checkRandomEncounters;
	private ProgressListener progress;
	private ActorGroup group;
	private Tile tile;
	private MazeEvent toAppend;

	/*-------------------------------------------------------------------------*/
	public RestingCheckpointEvent(int nextNrTurns, int nrTurns, long turnNr,
		boolean checkRandomEncounters,
		ProgressListener progress, ActorGroup ag, Tile tile, MazeEvent toAppend)
	{
		this.nextNrTurns = nextNrTurns;
		this.nrTurns = nrTurns;
		this.turnNr = turnNr;
		this.checkRandomEncounters = checkRandomEncounters;
		this.progress = progress;
		this.group = ag;
		this.tile = tile;
		this.toAppend = toAppend;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (checkRandomEncounters)
		{
			int perc = GameSys.getInstance().getRestingDangerPercentage(tile);

			// check for GUARD DUTY
			if (Maze.getInstance().getParty().hasModifier(Stats.Modifier.GUARD_DUTY))
			{
				perc /= 2;
			}

			if (Dice.d100.roll("Resting encounter check") <= perc)
			{
				progress.message(StringUtil.getUiLabel("rd.encounter"));

				result.add(
					new EncounterActorsEvent(
						null, tile.getRandomEncounters(), null, null, null, null, null, null));
				return result;
			}
		}

		progress.message(StringUtil.getUiLabel("rd.resting"));
		for (long i=turnNr; i<turnNr+nextNrTurns; i++)
		{
			result.add(
				new RestingTurnEvent(
					nrTurns,
					i,
					false,
					progress,
					group,
					tile));
		}

		if (toAppend != null)
		{
			result.add(toAppend);
		}

		return result;
	}
}
