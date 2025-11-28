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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.event.SummoningSucceedsEvent;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.stat.npc.NpcManager;

/**
 *
 */
public class StartCombatEvent extends MazeEvent
{
	private final Maze maze;
	private final PlayerParty party;
	private final ActorEncounter actorEncounter;

	/*-------------------------------------------------------------------------*/
	public StartCombatEvent(Maze maze, PlayerParty party,
		ActorEncounter actorEncounter)
	{
		this.maze = maze;
		this.party = party;
		this.actorEncounter = actorEncounter;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<>();

		Maze.getInstance().getUi().clearDialog();

		final List<FoeGroup> actors = actorEncounter.getActors();
		Foe leader = actorEncounter.getLeader();

		// not your friend anymore
		if (leader.getFaction() != null)
		{
			NpcFaction nf = NpcManager.getInstance().getNpcFaction(leader.getFaction());
			nf.setAttitude(NpcFaction.Attitude.ATTACKING);
		}
		actorEncounter.setEncounterAttitude(NpcFaction.Attitude.ATTACKING);

		//maze.getUi().setFoes(actors, true);

		// call for help?
		if (leader.getAlliesOnCall() != null)
		{
			EncounterTable table =
				Database.getInstance().getEncounterTable(leader.getAlliesOnCall());
			FoeEntry foeEntry = table.getEncounterTable().getRandomItem();
			List<FoeGroup> foeGroups = foeEntry.generate();

			actors.addAll(foeGroups);
			List<MazeEvent> evts = getList(
				new FlavourTextEvent(StringUtil.getEventText("msg.call.for.help", leader.getDisplayName())),
				new SummoningSucceedsEvent(foeGroups, leader));

			result.addAll(evts);
			result.add(new MazeEvent()
			{
				@Override
				public List<MazeEvent> resolve()
				{
					maze.getUi().addFoes(foeGroups, true);
					return null;
				}
			});
		}

		result.add(new MazeEvent()
		{
			@Override
			public List<MazeEvent> resolve()
			{
				maze.getUi().refreshCharacterData();
				return null;
			}
		});

		// play the encounter fanfare
		MazeScript script = Database.getInstance().getMazeScript("_ENCOUNTER_");
		result.addAll(script.getEvents());

		// begin encounter speech
		int avgFoeLevel = 0;
		for (ActorGroup ag : actors)
		{
			avgFoeLevel += ag.getAverageLevel();
		}
		avgFoeLevel /= actors.size();
		SpeechUtil.getInstance().startCombatSpeech(
			avgFoeLevel,
			maze.getParty().getPartyLevel());

		// execute any appearance scripts, picking from the leader
		if (leader.getAppearanceScript() != null)
		{
			result.addAll(leader.getAppearanceScript().getEvents());
		}

		//
		// Combat is go!
		//
		result.add(new MazeEvent()
		{
			@Override
			public List<MazeEvent> resolve()
			{
				Combat combat = new Combat(party, actors, actorEncounter.getAmbushStatus());
				maze.setCurrentCombat(combat);
				maze.setState(Maze.State.COMBAT);

				maze.executePreCombatActions(combat);

				boolean hasQuickWits = party.hasModifier(Stats.Modifier.QUICK_WITS);

				boolean partyIsSurprised =
					actorEncounter.getAmbushStatus() == Combat.AmbushStatus.FOES_MAY_AMBUSH_PARTY ||
					actorEncounter.getAmbushStatus() == Combat.AmbushStatus.FOES_MAY_AMBUSH_OR_EVADE_PARTY;

				if (partyIsSurprised && !hasQuickWits)
				{
					// party has no actions in the surprise round
					maze.getUi().disableInput();
					maze.executeCombatRound(combat);
				}

				maze.getUi().refreshPcActionOptions();

				return null;
			}
		});

		return result;
	}
}
