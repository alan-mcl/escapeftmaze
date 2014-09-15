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

package mclachlan.maze.campaign.def.game;

import mclachlan.maze.game.DifficultyLevel;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.Stats;

/**
 *
 */
public class HardDifficulty extends DifficultyLevel
{
	/*-------------------------------------------------------------------------*/
	public void foeIsSpawned(Foe foe)
	{
		int value = 1;

		// +1 hits, stealth and magic
		incMaximum(foe.getHitPoints(), value);
		incMaximum(foe.getActionPoints(), value);
		incMaximum(foe.getMagicPoints(), value);

		// +1 attack and defence
		foe.incModifier(Stats.Modifiers.ATTACK, value);
		foe.incModifier(Stats.Modifiers.DEFENCE, value);

		// +5 on all resistances
		foe.incModifier(Stats.Modifiers.RESIST_ENERGY, value*5);
		foe.incModifier(Stats.Modifiers.RESIST_BLUDGEONING, value*5);
		foe.incModifier(Stats.Modifiers.RESIST_PIERCING, value*5);
		foe.incModifier(Stats.Modifiers.RESIST_SLASHING, value*5);
		foe.incModifier(Stats.Modifiers.RESIST_FIRE, value*5);
		foe.incModifier(Stats.Modifiers.RESIST_WATER, value*5);
		foe.incModifier(Stats.Modifiers.RESIST_AIR, value*5);
		foe.incModifier(Stats.Modifiers.RESIST_EARTH, value*5);
		foe.incModifier(Stats.Modifiers.RESIST_MENTAL, value*5);
	}

	/*-------------------------------------------------------------------------*/
	private void incMaximum(CurMax hp, int value)
	{
		hp.incMaximum(value);
		hp.setCurrentToMax();
	}
}