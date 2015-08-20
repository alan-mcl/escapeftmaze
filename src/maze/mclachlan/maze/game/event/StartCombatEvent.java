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
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.FoeGroup;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.stat.SpeechUtil;
import mclachlan.maze.stat.combat.Combat;

/**
 *
 */
public class StartCombatEvent extends MazeEvent
{
	private Maze maze;
	private PlayerParty party;
	private List<FoeGroup> actors;
	private String mazeVar;

	/*-------------------------------------------------------------------------*/
	public StartCombatEvent(Maze maze, PlayerParty party, List<FoeGroup> actors,
		String mazeVar)
	{
		this.maze = maze;
		this.party = party;
		this.actors = actors;
		this.mazeVar = mazeVar;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		// play the encounter fanfare
		MazeScript script = Database.getInstance().getScript("_ENCOUNTER_");
		maze.resolveEvents(script.getEvents());

		// begin encounter speech
		int avgFoeLevel = 0;
		for (FoeGroup fg : actors)
		{
			avgFoeLevel += fg.getAverageLevel();
		}
		avgFoeLevel /= actors.size();
		SpeechUtil.getInstance().startCombatSpeech(
			avgFoeLevel,
			maze.getParty().getPartyLevel());

		// execute any appearance scripts, picking from the first foe
		Foe f = (Foe)(actors.get(0).getActors().get(0));
		if (f.getAppearanceScript() != null)
		{
			maze.resolveEvents(f.getAppearanceScript().getEvents());
		}

		//
		// Combat is go!
		//
		maze.setCurrentCombat(new Combat(party, actors, true));
		maze.setState(Maze.State.COMBAT);

		return null;
	}
}
