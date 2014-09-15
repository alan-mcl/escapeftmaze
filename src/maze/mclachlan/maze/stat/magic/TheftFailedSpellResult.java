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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.TheftSpellFailed;
import mclachlan.maze.stat.npc.Npc;

/**
 * Typically cast on an NPC to steal an item
 */
public class TheftFailedSpellResult extends SpellResult
{
	Value value;

	/*-------------------------------------------------------------------------*/
	public TheftFailedSpellResult(Value value)
	{
		this.value = value;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target, int castingLevel, SpellEffect parent)
	{
		if (target instanceof Npc)
		{
			Npc npc = (Npc)target;
			PlayerCharacter pc = (PlayerCharacter)source;
			int strength = this.value.compute(source, castingLevel);

			return getList(
				new TheftSpellFailed(pc, npc, strength));
		}

		//todo: amusing but hard to implement against foes in combat
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public Value getValue()
	{
		return value;
	}
}
