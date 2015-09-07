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
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.Npc;

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

		// make sure we're dealing with Foes
		List<FoeGroup> foeGroups = new ArrayList<FoeGroup>();
		for (ActorGroup ag : actors)
		{
			if (ag instanceof FoeGroup)
			{
				foeGroups.add((FoeGroup)ag);
			}
			else if (ag instanceof NpcActorGroup)
			{
				Npc npc = ((NpcActorGroup)ag).getNpc();
				FoeTemplate npcFoeTemplate = Database.getInstance().getFoeTemplate(npc.getFoeName());
				Foe foe = new Foe(npcFoeTemplate);
				FoeGroup fg = new FoeGroup();
				fg.add(foe);
				foeGroups.add(fg);
			}
		}

		// begin encounter speech
		int avgFoeLevel = 0;
		for (ActorGroup ag : foeGroups)
		{
			avgFoeLevel += ag.getAverageLevel();
		}
		avgFoeLevel /= foeGroups.size();
		SpeechUtil.getInstance().startCombatSpeech(
			avgFoeLevel,
			maze.getParty().getPartyLevel());

		// execute any appearance scripts, picking from the first foe
		Foe f = (Foe)(foeGroups.get(0).getActors().get(0));
		if (f.getAppearanceScript() != null)
		{
			maze.resolveEvents(f.getAppearanceScript().getEvents());
		}

		//
		// Combat is go!
		//
		maze.setCurrentCombat(new Combat(party, foeGroups, true));
		maze.setState(Maze.State.COMBAT);

		return null;
	}
}
