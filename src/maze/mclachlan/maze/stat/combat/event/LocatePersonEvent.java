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

package mclachlan.maze.stat.combat.event;

import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcManager;

/**
 *
 */
public class LocatePersonEvent extends MazeEvent
{
	private UnifiedActor source;
	private String npcName;
	private Value value;
	private int castingLevel;

	/*-------------------------------------------------------------------------*/
	public LocatePersonEvent(UnifiedActor source, String npcName, Value value, int castingLevel)
	{
		this.source = source;
		this.npcName = npcName;
		this.value = value;
		this.castingLevel = castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		int nrResults = value.compute(source, castingLevel);
		NpcManager npcs = NpcManager.getInstance();
		List<Npc> shuffledList = new ArrayList<Npc>(npcs.getNpcs());
		Collections.shuffle(shuffledList);

		Set<String> done = new HashSet<String>();

		if (npcs.hasNpc(npcName))
		{
			Npc npc = npcs.getNpc(npcName);
			locateNpc(result, npc);
			done.add(npcName);
			nrResults--;
		}

		for (int i=0; i<nrResults; i++)
		{
			Npc npc = shuffledList.get(Dice.nextInt(shuffledList.size()));
			if (!done.contains(npc.getName()))
			{
				locateNpc(result, npc);
				done.add(npc.getName());
			}
		}

		if (result.isEmpty())
		{
			result.add(
				new FlavourTextEvent(
					StringUtil.getEventText("msg.locate.person.nothing", source.getDisplayName())));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void locateNpc(List<MazeEvent> result, Npc npc)
	{
		String zone = npc.getZone();

		if (npc.isFound())
		{
			if (npc.isAlive())
			{
				result.add(new FlavourTextEvent(
					StringUtil.getEventText("msg.npc.location", npc.getDisplayName(), zone)));
			}
			else
			{
				result.add(new FlavourTextEvent(
					StringUtil.getEventText("msg.npc.dead", npc.getDisplayName())));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}
}
