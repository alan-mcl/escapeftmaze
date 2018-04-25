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

package mclachlan.maze.stat.condition.impl;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.CurMaxSub;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.ui.diygui.Constants;

/**
 * A custom condition impl for when an actor passes out from fatigue.
 */
public class RestingSleep extends Condition
{
	/*-------------------------------------------------------------------------*/
	public RestingSleep()
	{
		setDuration(1);
		setStrength(1);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return Constants.Conditions.RESTING_SLEEP;
	}

	@Override
	public String getDisplayName()
	{
		return "Sleep";
	}

	@Override
	public String getIcon()
	{
		return "condition/sleep";
	}

	@Override
	public String getAdjective()
	{
		return "asleep";
	}

	@Override
	public int getModifier(Stats.Modifier modifier, ConditionBearer bearer)
	{
		return 0;
	}

	@Override
	public ConditionEffect getEffect()
	{
		return Database.getInstance().getConditionEffect("sleep");
	}

	@Override
	public boolean isStrengthWanes()
	{
		return false;
	}

	@Override
	public MagicSys.SpellEffectType getType()
	{
		return MagicSys.SpellEffectType.NONE;
	}

	@Override
	public MagicSys.SpellEffectSubType getSubtype()
	{
		return MagicSys.SpellEffectSubType.NONE;
	}

	@Override
	public boolean isAffliction()
	{
		return false;
	}

	@Override
	public boolean isIdentified()
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		if (Maze.getInstance().getState() == Maze.State.RESTING)
		{
			return null;
		}

		// check and see if the target can awaken yet
		ConditionBearer target = getTarget();

		if (target instanceof UnifiedActor)
		{
			CurMaxSub hp = ((UnifiedActor)target).getHitPoints();
			// 50% chance of waking up
			if (hp.getSub() < hp.getCurrent() && Dice.d100.roll("resting sleep awaken check") <= 40)
			{
				setDuration(-1);
			}
		}

		return null;
	}
}
