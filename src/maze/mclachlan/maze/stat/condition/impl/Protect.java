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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.UiMessageEvent;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.AttackAction;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.ui.diygui.Constants;

/**
 *
 */
public class Protect extends Condition
{
	private static ConditionEffect effect = new ProtectEffect();

	/*-------------------------------------------------------------------------*/
	public Protect()
	{
		setDuration(1);
		setStrength(1);
	}

		/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return Constants.Conditions.PROTECT;
	}

	@Override
	public String getDisplayName()
	{
		return "Protect";
	}

	@Override
	public String getIcon()
	{
		return "condition/protect";
	}

	@Override
	public String getAdjective()
	{
		return "protected";
	}

	@Override
	public int getModifier(Stats.Modifier modifier, ConditionBearer bearer)
	{
		return 0;
	}

	@Override
	public ConditionEffect getEffect()
	{
		return effect;
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

	@Override
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		setDuration(-1);
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private static class ProtectEffect extends ConditionEffect
	{
		@Override
		public List<MazeEvent> attackOnConditionBearer(AttackAction attackAction, Condition condition)
		{
			UnifiedActor source = condition.getSource();

			ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();
			if (!GameSys.getInstance().isActorHelpless(source))
			{
				attackAction.setDefender(source);

				result.add(new UiMessageEvent(StringUtil.getEventText("msg.protect", source.getDisplayName())));
			}
			return result;
		}
	}
}
