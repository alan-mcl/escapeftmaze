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
import mclachlan.maze.stat.DamagePacket;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.DamageEvent;
import mclachlan.maze.stat.combat.event.HealingEvent;
import mclachlan.maze.stat.combat.event.NoEffectEvent;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.magic.SpellResult;

/**
 * A spell result for the warlock inversion kata ability:
 * swap the warlock HP with the target HP
 */
public class InversionKataSpellResult extends SpellResult
{
	@Override
	public List<MazeEvent> apply(UnifiedActor source, UnifiedActor target,
		int castingLevel, SpellEffect parent, Spell spell)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (target.getModifier(Stats.Modifier.IMMUNE_TO_CRITICALS) > 0)
		{
			result.add(new NoEffectEvent());
			return result;
		}

		int targetHp = target.getHitPoints().getCurrent();
		int sourceHp = source.getHitPoints().getCurrent();

		if (targetHp == sourceHp)
		{
			result.add(new NoEffectEvent());
		}
		else if (targetHp > sourceHp)
		{
			int diff = targetHp - sourceHp;

			result.add(new DamageEvent(
				null,
				target,
				source,
				new DamagePacket(diff, 1),
				parent.getType(),
				parent.getSubType(),
				null,
				null,
				null));

			result.add(new HealingEvent(source, diff));
		}
		else
		{
			int diff = sourceHp - targetHp;

			result.add(new DamageEvent(
				null,
				source,
				source,
				new DamagePacket(diff, 1),
				parent.getType(),
				parent.getSubType(),
				null,
				null,
				null));

			result.add(new HealingEvent(target, diff));
		}

		return result;
	}
}
