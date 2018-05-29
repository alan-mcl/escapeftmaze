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

package mclachlan.maze.campaign.def.stat.magic;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.ActorGroup;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.magic.SpellResult;

/**
 * Priest's Matyr ability
 */
public class MatyrSpellResult extends SpellResult
{
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent, Spell spell)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// restore the rest of the party to life and health
		ActorGroup actorGroup = source.getActorGroup();
		for (UnifiedActor actor : actorGroup.getActors())
		{
			if (actor == source)
			{
				continue;
			}

			if (!GameSys.getInstance().isActorAlive(actor))
			{
				result.add(new ResurrectionEvent(actor, source.getLevel("Priest")));
			}

			result.add(new HealingEvent(actor, actor.getHitPoints().getMissing()));
			result.add(new StaminaEvent(actor, actor.getHitPoints().getSub()));
			result.add(new RestoreActionPointsEvent(actor, actor.getActionPoints().getMissing()));
			result.add(new RestoreMagicEvent(actor, actor.getMagicPoints().getMissing()));

			for (Condition c : actor.getConditions())
			{
				if (c.isAffliction())
				{
					result.add(new ConditionRemovalEvent(actor, c));
				}
			}
		}

		// remove the priest from the game
		result.add(new ActorDiesEvent(source, null));
		result.add(new RemoveActorFromGameEvent(source));

		return result;
	}
}
