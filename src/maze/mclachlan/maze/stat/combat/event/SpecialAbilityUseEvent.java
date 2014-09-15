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

package mclachlan.maze.stat.combat.event;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class SpecialAbilityUseEvent extends MazeEvent
{
	private UnifiedActor caster;
	private Spell spell;
	private int castingLevel;
	private String description;

	/*-------------------------------------------------------------------------*/
	public SpecialAbilityUseEvent(UnifiedActor attacker, Spell spell, int castingLevel, String description)
	{
		this.caster = attacker;
		this.spell = spell;
		this.castingLevel = castingLevel;
		this.description = description;
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getCaster()
	{
		return caster;
	}

	/*-------------------------------------------------------------------------*/
	public Spell getSpell()
	{
		return spell;
	}

	/*-------------------------------------------------------------------------*/
	public String getDescription()
	{
		return description;
	}

	/*-------------------------------------------------------------------------*/
	public boolean shouldClearText()
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Delay.NONE;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		int cost = MagicSys.getInstance().getMagicPointCost(spell, castingLevel, caster);
		caster.getMagicPoints().decCurrent(cost);
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return getCaster().getDisplayName() + " " + getDescription();
	}
}
