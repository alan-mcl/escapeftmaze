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
import mclachlan.maze.stat.CurMaxSub;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.ConditionEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.ui.diygui.Constants;

/**
 *
 */
public class CheckPartyStatusEvent extends MazeEvent
{
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<>();

		//
		// See if the whole party is dead = GAME OVER
		//
		if (!Maze.getInstance().checkPartyStatus())
		{
			return result;
		}

		//
		// Reorder to place any dead PCs ar the back
		//
		Maze.getInstance().reorderPartyToCompensateForDeadCharacters();

		//
		// check for fatigue KO on any live actors
		//
		for (UnifiedActor actor : Maze.getInstance().getParty().getActors())
		{
			CurMaxSub hp = actor.getHitPoints();

			if (hp.getSub() >= hp.getCurrent() && hp.getCurrent() > 0)
			{
				ConditionTemplate kot = Database.getInstance().getConditionTemplate(
					Constants.Conditions.FATIGUE_KO);
				Condition ko = kot.create(
					actor, actor, 1, MagicSys.SpellEffectType.NONE, MagicSys.SpellEffectSubType.NONE);

				result.add(new ConditionEvent(actor, ko));
			}
		}

		return result;
	}
}
