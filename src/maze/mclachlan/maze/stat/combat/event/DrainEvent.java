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
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.CombatantData;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.ui.diygui.Constants;

/**
 *
 */
public class DrainEvent extends MazeEvent
{
	private UnifiedActor target;
	private UnifiedActor source;
	private int drain;
	private int type;
	private String modifier;

	/*-------------------------------------------------------------------------*/
	public DrainEvent(
		UnifiedActor target,
		UnifiedActor source,
		int drain, 
		int type, 
		String modifier)
	{
		super();
		this.target = target;
		this.source = source;
		this.drain = drain;
		this.type = type;
		this.modifier = modifier;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		if (modifier.equals(Stats.Modifiers.HIT_POINTS))
		{
			target.getHitPoints().decCurrent(drain);
			target.getHitPoints().incMaximum(-drain);
		}
		else if (modifier.equals(Stats.Modifiers.ACTION_POINTS))
		{
			target.getActionPoints().decCurrent(drain);
			target.getActionPoints().incMaximum(-drain);
		}
		else if (modifier.equals(Stats.Modifiers.MAGIC_POINTS))
		{
			target.getMagicPoints().decCurrent(drain);
			target.getMagicPoints().incMaximum(-drain);
		}
		else
		{
			target.setModifier(modifier, target.getModifier(modifier)-drain);
		}
		
		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();
		if (target.getHitPoints().getCurrent() <= 0)
		{
			// defender is dead (note you can't cheat death if you are drained to a zero max!)
			if (target.getHitPoints().getMaximum() > 0 && GameSys.getInstance().actorCheatsDeath(target))
			{
				GameSys.getInstance().cheatDeath(target);
				result.add(new ActorCheatsDeathEvent(target));
			}
			else
			{
				CombatantData data = target.getCombatantData();
				if (data != null)
				{
					data.setActive(false);
				}
	
				result.add(new ActorDiesEvent(target, source));
			}
		}
		else if (target.getHitPoints().getSub() >= target.getHitPoints().getCurrent())
		{
			ConditionTemplate kot = Database.getInstance().getConditionTemplate(
				Constants.Conditions.FATIGUE_KO);
			Condition ko = kot.create(
				source, target, 1, MagicSys.SpellEffectType.NONE, MagicSys.SpellEffectSubType.NONE);

			// defender is KO
			result.add(new ConditionEvent(target, ko));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return "drained "+getDrain()+" "+
				StringUtil.getModifierName(getModifier())+"!";
	}
	
	/*-------------------------------------------------------------------------*/


	public int getDrain()
	{
		return drain;
	}

	public String getModifier()
	{
		return modifier;
	}

	public UnifiedActor getSource()
	{
		return source;
	}

	public UnifiedActor getTarget()
	{
		return target;
	}

	public int getType()
	{
		return type;
	}
}
