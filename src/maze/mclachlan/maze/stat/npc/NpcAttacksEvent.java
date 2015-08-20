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

package mclachlan.maze.stat.npc;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.FoeGroup;
import mclachlan.maze.stat.FoeTemplate;
import mclachlan.maze.stat.combat.event.SummoningSucceedsEvent;

/**
 *
 */
public class NpcAttacksEvent extends MazeEvent
{
	Npc npc;

	/*-------------------------------------------------------------------------*/
	public NpcAttacksEvent(Npc npc)
	{
		this.npc = npc;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		// not your friend any more
		if (npc.getFaction() != null)
		{
			NpcFaction nf = NpcManager.getInstance().getNpcFaction(npc.getFaction());
			nf.setAttitude(NpcFaction.Attitude.ATTACKING);
		}
		else
		{
			npc.setAttitude(NpcFaction.Attitude.ATTACKING);
		}

		// add the NPC to the UI.
		FoeTemplate npcFoeTemplate = Database.getInstance().getFoeTemplate(npc.getFoeName());
		Foe foe = new Foe(npcFoeTemplate);

		// init foes
		ArrayList<FoeGroup> allFoes = new ArrayList<FoeGroup>();
		for (int i=0; i<1; i++)
		{
			List foes = new ArrayList();
			foes.add(foe);
			FoeGroup foesGroup = new FoeGroup(foes);
			allFoes.add(foesGroup);
		}

		if (npc.getAlliesOnCall() != null)
		{
			EncounterTable table =
				Database.getInstance().getEncounterTable(npc.getAlliesOnCall());
			FoeEntry foeEntry = table.getEncounterTable().getRandomItem();
			List<FoeGroup> foeGroups = foeEntry.generate();

			allFoes.addAll(foeGroups);
			List<MazeEvent> evts = getList(
				new FlavourTextEvent(npc.getName() + " calls for help!!",
					Maze.getInstance().getUserConfig().getCombatDelay(), true),
				new SummoningSucceedsEvent(foeGroups, npc));

			Maze.getInstance().resolveEvents(evts);
		}

		Maze.getInstance().getUi().setFoes(null);
		Maze.getInstance().encounterActors(allFoes, null);

		return null;
	}
}
