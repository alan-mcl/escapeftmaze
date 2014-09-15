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
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.ActorGroup;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.PurifyAirEvent;
import mclachlan.maze.stat.condition.CloudSpell;

/**
 *
 */
public class PurifyAirSpellResult extends SpellResult
{
	/** The strength of the purification */
	private Value strength;

	/*-------------------------------------------------------------------------*/
	public PurifyAirSpellResult(Value strength)
	{
		this.strength = strength;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent)
	{
		List<MazeEvent> events = new ArrayList<MazeEvent>();

		int str = strength.compute(source, castingLevel);
		ActorGroup group = Maze.getInstance().getActorGroup(target);

		List<CloudSpell> cloudSpells = group.getCloudSpells();

		for (CloudSpell spell : cloudSpells)
		{
			events.add(new PurifyAirEvent(spell));
		}

		int index = 0;
		while (str > 0 && cloudSpells.size()>0)
		{
			CloudSpell spell = cloudSpells.get(index%cloudSpells.size());
			spell.decDuration(1);
			spell.decStrength(1);

			if (spell.getDuration() == 0)
			{
				spell.expire();
			}
			else
			{
				index++;
			}

			str--;
		}

		return events;
	}

	/*-------------------------------------------------------------------------*/
	public Value getStrength()
	{
		return strength;
	}

	/*-------------------------------------------------------------------------*/
	public void setStrength(Value strength)
	{
		this.strength = strength;
	}
}
