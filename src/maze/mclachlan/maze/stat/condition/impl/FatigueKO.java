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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.CurMaxSub;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.ui.diygui.Constants;

/**
 * A custom condition impl for when an actor passes out from fatigue.
 */
public class FatigueKO extends Condition
{
	/*-------------------------------------------------------------------------*/
	public FatigueKO()
	{
		setDuration(1);
		setStrength(Integer.MAX_VALUE);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return Constants.Conditions.FATIGUE_KO;
	}

	@Override
	public String getDisplayName()
	{
		return "Fatigue KO";
	}

	@Override
	public String getIcon()
	{
		return "condition/ko";
	}

	@Override
	public String getAdjective()
	{
		return "unconscious";
	}

	@Override
	public int getModifier(String modifier, ConditionBearer bearer)
	{
		return 0;
	}

	@Override
	public ConditionEffect getEffect()
	{
		return Database.getInstance().getConditionEffect("ko");
	}

	@Override
	public boolean strengthWanes()
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

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		// check and see if the target can awaken yet
		ConditionBearer target = getTarget();

		if (target instanceof UnifiedActor)
		{
			CurMaxSub hp = ((UnifiedActor)target).getHitPoints();
			// 50% chance of waking up
			if (hp.getSub() < hp.getCurrent() && Dice.d2.roll() == 1)
			{
				setDuration(-1);
			}
		}

		return null;
	}
}
