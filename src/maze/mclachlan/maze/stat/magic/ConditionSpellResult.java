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
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.ActorUnaffectedEvent;
import mclachlan.maze.stat.combat.event.ConditionEvent;
import mclachlan.maze.stat.combat.event.NoEffectEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionTemplate;

/**
 * A spell result that applies a condition to the target.
 */
public class ConditionSpellResult extends SpellResult
{
	private ConditionTemplate conditionTemplate;

	/*-------------------------------------------------------------------------*/
	public ConditionSpellResult(ConditionTemplate conditionTemplate)
	{
		this.conditionTemplate = conditionTemplate;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target, int castingLevel, SpellEffect parent)
	{
		Condition c = conditionTemplate.create(
			source, target, castingLevel, parent.getType(), parent.getSubType());

		if (GameSys.getInstance().isActorImmuneToCondition(target, c))
		{
			return getList(new ActorUnaffectedEvent(target));
		}
		else
		{
			return getList(new ConditionEvent(target, c));
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> apply(UnifiedActor source, Tile target, int castingLevel, SpellEffect parent)
	{
		Condition c = conditionTemplate.create(
			source, target, castingLevel, parent.getType(), parent.getSubType());

		// tile immunity is based on the parent effect type
		if (GameSys.getInstance().isTileImmuneToCondition(target, parent.getSubType()))
		{
			return getList(new NoEffectEvent());
		}
		else
		{
			return getList(new ConditionEvent(target, c));
		}
	}

	/*-------------------------------------------------------------------------*/
	public ConditionTemplate getConditionTemplate()
	{
		return conditionTemplate;
	}
}
