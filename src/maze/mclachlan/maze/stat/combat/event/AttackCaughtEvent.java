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
import mclachlan.maze.stat.AttackWith;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class AttackCaughtEvent extends MazeEvent
{
	private UnifiedActor attacker;
	private UnifiedActor defender;
	private AttackWith attackWith;
	private Item ammo;

	/*-------------------------------------------------------------------------*/
	public AttackCaughtEvent(
		UnifiedActor attacker,
		UnifiedActor defender,
		AttackWith attackWith,
		Item ammo)
	{
		this.attacker = attacker;
		this.defender = defender;
		this.attackWith = attackWith;
		this.ammo = ammo;
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
	public AttackWith getAttackWith()
	{
		return attackWith;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		MazeScript script = Database.getInstance().getMazeScript("generic weapon swish");
		result.addAll(script.getEvents());

		if (attackWith instanceof Item)
		{
			// attacker ammo has already been deducted in StrikeEvent
			Item item = ammo.getTemplate().create(1);
			defender.getInventory().addAndStack(item);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return 0;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return StringUtil.getEventText("msg.caught", getDefender().getDisplayName());
	}
}
