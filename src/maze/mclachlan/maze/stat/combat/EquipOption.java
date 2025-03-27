/*
 * Copyright (c) 2014 Alan McLachlan
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

package mclachlan.maze.stat.combat;

import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class EquipOption extends ActorActionOption
{
	public EquipOption()
	{
		super("Equip", "aao.equip");
	}

	@Override
	public void select(UnifiedActor actor, Combat combat,
		ActionOptionCallback callback)
	{
		if (actor.getModifier(Stats.Modifier.PACK_RAT) > 0)
		{
			Maze.getInstance().getUi().characterSelected((PlayerCharacter)actor);
			Maze.getInstance().setState(Maze.State.INVENTORY, this);

			synchronized (this)
			{
				try
				{
					wait();
				}
				catch (InterruptedException e)
				{
					throw new MazeException(e);
				}
			}
		}
		else
		{
			super.select(actor, combat, callback);
		}
	}

	@Override
	public ActorActionIntention getIntention()
	{
		return new EquipIntention();
	}
}
