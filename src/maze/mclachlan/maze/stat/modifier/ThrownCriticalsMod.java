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

package mclachlan.maze.stat.modifier;

import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.ModifierValue;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class ThrownCriticalsMod extends ModifierModification
{
	@Override
	public void getModification(UnifiedActor actor, List<ModifierValue> result)
	{
		if (actor.getModifier(Stats.Modifier.SHADOW_MASTER) > 0)
		{
			result.add(new ModifierValue(
				StringUtil.getModifierName(Stats.Modifier.SHADOW_MASTER),
				actor.getModifier(Stats.Modifier.BLACK_MAGIC_GEN)));
		}
	}
}
