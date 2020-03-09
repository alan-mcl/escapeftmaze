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
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.ui.diygui.animation.AnimationContext;

/**
 *
 */
public class AttackRipostedEvent extends MazeEvent
{
	private UnifiedActor attacker;
	private UnifiedActor defender;
	private Combat combat;
	private AnimationContext animationContext;

	/*-------------------------------------------------------------------------*/
	public AttackRipostedEvent(
		UnifiedActor attacker,
		UnifiedActor defender,
		Combat combat, AnimationContext animationContext)
	{
		this.attacker = attacker;
		this.defender = defender;
		this.combat = combat;
		this.animationContext = animationContext;
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
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		// sound effect
		MazeScript script = Database.getInstance().getMazeScript("_WEAPON_HIT_");
		result.addAll(script.getEvents());

		// riposte attack
		Item attackWith = defender.getPrimaryWeapon();
		result.add(
			new AttackEvent(
				combat,
				defender,
				attacker,
				attackWith,
				GameSys.getInstance().getAttackType(attackWith),
				0,
				1,
				attackWith.getAttackScript(),
				attackWith.getDefaultDamageType(),
				animationContext,
				null,
				null));

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
		return StringUtil.getEventText("msg.riposte", getDefender().getDisplayName());
	}
}
