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
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.map.Tile;

/**
 *
 */
public class HeroicDifficulty extends DifficultyLevel
{
	/*-------------------------------------------------------------------------*/
	public void foeIsSpawned(Foe foe)
	{
		// foes scale up with level
		int value = Math.max(1, foe.getLevel()/2);

		// + hits, stealth and magic
		incMaximum(foe.getHitPoints(), value);
		incMaximum(foe.getActionPoints(), value);
		incMaximum(foe.getMagicPoints(), value);

		// + various mods
		foe.incModifier(Stats.Modifier.ATTACK, value);
		foe.incModifier(Stats.Modifier.DEFENCE, value);
		foe.incModifier(Stats.Modifier.INITIATIVE, value);
		foe.incModifier(Stats.Modifier.TO_PENETRATE, value);
		foe.incModifier(Stats.Modifier.POWER_CAST, value);

		// + on all resistances
		foe.incModifier(Stats.Modifier.RESIST_BLUDGEONING, value*2);
		foe.incModifier(Stats.Modifier.RESIST_PIERCING, value*2);
		foe.incModifier(Stats.Modifier.RESIST_SLASHING, value*2);
		foe.incModifier(Stats.Modifier.RESIST_ENERGY, value*5);
		foe.incModifier(Stats.Modifier.RESIST_FIRE, value*5);
		foe.incModifier(Stats.Modifier.RESIST_WATER, value*5);
		foe.incModifier(Stats.Modifier.RESIST_AIR, value*5);
		foe.incModifier(Stats.Modifier.RESIST_EARTH, value*5);
		foe.incModifier(Stats.Modifier.RESIST_MENTAL, value*5);

		// tireless
		foe.incModifier(Stats.Modifier.TIRELESS_UNARMED, value);

		// random funky shit to thwart the player
		foe.incModifier(Stats.Modifier.ARROW_CUTTING, value);
		if (foe.getLevel() >= 16)
		{
			foe.incModifier(Stats.Modifier.BLIND_FIGHTING, value);
		}
		if (foe.getLevel() >= 20)
		{
			foe.incModifier(Stats.Modifier.CHEAT_DEATH, value);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void incMaximum(CurMax hp, int value)
	{
		hp.incMaximum(value);
		hp.setCurrentToMax();
	}

	/*-------------------------------------------------------------------------*/
	public int getRandomEncounterChance(Tile t)
	{
		return t.getRandomEncounterChance()+2;
	}
}