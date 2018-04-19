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
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class AttackHitEvent extends MazeEvent
{
	private UnifiedActor attacker;
	private UnifiedActor defender;
	private AttackWith attackWith;
	private BodyPart bodyPart;

	/*-------------------------------------------------------------------------*/
	public AttackHitEvent(UnifiedActor attacker, UnifiedActor defender,
		AttackWith attackWith, BodyPart bodyPart)
	{
		this.attacker = attacker;
		this.defender = defender;
		this.attackWith = attackWith;
		this.bodyPart = bodyPart;
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getAttacker()
	{
		return attacker;
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getDefender()
	{
		return defender;
	}
	
	/*-------------------------------------------------------------------------*/
	public BodyPart getBodyPart()
	{
		return bodyPart;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		// check for TOE_TO_TOE
		if (attacker.getModifier(Stats.Modifier.TOE_TO_TOE) > 0 &&
			!attackWith.isRanged())
		{
			int roll = Dice.d3.roll("toe to toe effect");
			switch (roll)
			{
				// restore 1 stamina
				case 1:
				case 2: attacker.getHitPoints().incSub(-1); break;
				// restore 1 action point
				case 3: attacker.getActionPoints().incCurrent(1); break;
				default: throw new MazeException("invalid die roll "+roll);
			}
		}

		MazeScript script = Database.getInstance().getScript("_WEAPON_HIT_");
		return script.getEvents();
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return 0;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return StringUtil.getEventText(
			"msg.attack.hit",
			getDefender().getDisplayName(),
			getBodyPart().getDisplayName());
	}
}
