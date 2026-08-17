/*
 * Copyright (c) 2026 Alan McLachlan
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

package mclachlan.maze.balance;

import java.util.List;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.AttackIntention;
import mclachlan.maze.stat.combat.Combat;

/**
 * Minimal player combat AI for balance harnesses: attack the first foe group
 * with the first weapon option.
 */
public final class BasicPlayerCombatAi
{
	private BasicPlayerCombatAi()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static ActorActionIntention getCombatIntention(
		PlayerCharacter pc,
		List<FoeGroup> foeGroups,
		Combat combat)
	{
		if (foeGroups == null || foeGroups.isEmpty())
		{
			return ActorActionIntention.INTEND_NOTHING;
		}

		List<AttackWith> attackWithOptions = pc.getAttackWithOptions();
		if (attackWithOptions == null || attackWithOptions.isEmpty())
		{
			return ActorActionIntention.INTEND_NOTHING;
		}

		return new AttackIntention(foeGroups.get(0), combat, attackWithOptions.get(0));
	}
}
