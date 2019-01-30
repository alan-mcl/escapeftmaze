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
import mclachlan.maze.campaign.def.stat.condition.impl.EnlighteningKata;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.TypeDescriptor;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.ConditionEvent;
import mclachlan.maze.stat.combat.event.NoEffectEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.magic.SpellResult;

/**
 * Troubadour's Spell Stealing ability
 */
public class EnlighteningKataSpellResult extends SpellResult
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent,
		Spell spell)
	{
		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();

		List<TypeDescriptor> types = target.getTypes();

		if (types == null || types.isEmpty())
		{
			result.add(new NoEffectEvent());
			return result;
		}

		TypeDescriptor td = types.get(Dice.nextInt(types.size()));
		Stats.Modifier favouredEnemyModifier = td.getFavouredEnemyModifier();

		Condition condition = new EnlighteningKata(favouredEnemyModifier);

		result.add(new ConditionEvent(source, condition));

		return result;
	}
}
