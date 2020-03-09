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
import mclachlan.maze.stat.BodyPart;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class AttackDeflectedEvent extends MazeEvent
{
	private UnifiedActor attacker;
	private UnifiedActor defender;
	private BodyPart bodyPart;

	/*-------------------------------------------------------------------------*/
	public AttackDeflectedEvent(UnifiedActor attacker, UnifiedActor defender, BodyPart bodyPart)
	{
		this.attacker = attacker;
		this.defender = defender;
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
		MazeScript script = Database.getInstance().getMazeScript("_WEAPON_HIT_");
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
		return StringUtil.getEventText("msg.deflected", getDefender().getDisplayName());
	}
}
