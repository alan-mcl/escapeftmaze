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

import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.ActorGroup;
import mclachlan.maze.stat.condition.CloudSpell;
import java.util.*;

/**
 *
 */
public class CloudSpellEvent extends MazeEvent
{
	private ActorGroup attackedGroup;
	private CloudSpell cloudSpell;

	/*-------------------------------------------------------------------------*/
	public CloudSpellEvent(ActorGroup attackedGroup, CloudSpell spell)
	{
		this.attackedGroup = attackedGroup;
		this.cloudSpell = spell;
	}

	/*-------------------------------------------------------------------------*/
	public boolean shouldClearText()
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Delay.NONE;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		CloudSpell oldCloudSpell = null;

		// check existing cloud spells
		for (CloudSpell spell : attackedGroup.getCloudSpells())
		{
			if (spell.getSpell().equals(cloudSpell.getSpell()))
			{
				oldCloudSpell = spell;
				break;
			}
		}

		if (oldCloudSpell == null ||
			(oldCloudSpell != null &&
			oldCloudSpell.getStrength() < cloudSpell.getStrength()))
		{
			// swap in the new one
			if (oldCloudSpell != null)
			{
				attackedGroup.removeCloudSpell(oldCloudSpell);
			}

			attackedGroup.addCloudSpell(cloudSpell);
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return null;
	}
}