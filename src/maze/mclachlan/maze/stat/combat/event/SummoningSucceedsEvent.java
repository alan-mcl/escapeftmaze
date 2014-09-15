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
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.FoeGroup;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class SummoningSucceedsEvent extends MazeEvent
{
	UnifiedActor source;
	private List<FoeGroup> foeGroups;

	/*-------------------------------------------------------------------------*/
	public SummoningSucceedsEvent(List<FoeGroup> foeGroups, UnifiedActor source)
	{
		this.foeGroups = foeGroups;
		this.source = source;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		source.addAllies(foeGroups);
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<FoeGroup> getFoeGroups()
	{
		return foeGroups;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		StringBuilder result = new StringBuilder();
		for (FoeGroup fg : getFoeGroups())
		{
			// assume all foes are of the same type
			Foe sample = (Foe)fg.getActors().get(0);
			int size = fg.getActors().size();
			String name = (size>1) ? sample.getDisplayNamePlural() : sample.getDisplayName();
			String verb = (size>1) ? "appear" : "appears";
			result.append(size).append(" ").append(name).append(" ").append(verb).append("\n");
		}
		return result.toString();
	}
}
