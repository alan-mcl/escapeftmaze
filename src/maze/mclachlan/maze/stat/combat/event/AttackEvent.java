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
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.AttackType;

/**
 *
 */
public class AttackEvent extends MazeEvent
{
	private UnifiedActor attacker;
	private UnifiedActor defender;
	private AttackWith attackWith;
	
	/**
	 * Swing, thrust and stuff
	 */ 
	private AttackType attackType;
	
	private BodyPart bodyPart;
	private int actionCost;
	private int nrStrikes;

	/*-------------------------------------------------------------------------*/
	public AttackEvent(
		UnifiedActor attacker,
		UnifiedActor defender,
		AttackWith weapon,
		AttackType attackType,
		BodyPart bodyPart,
		int actionCost,
		int nrStrikes)
	{
		this.attacker = attacker;
		this.defender = defender;
		this.attackWith = weapon;
		this.attackType = attackType;
		this.bodyPart = bodyPart;
		this.actionCost = actionCost;
		this.nrStrikes = nrStrikes;
	}
	
	/*-------------------------------------------------------------------------*/
	public UnifiedActor getAttacker()
	{
		return attacker;
	}

	public UnifiedActor getDefender()
	{
		return defender;
	}

	public AttackWith getAttackWith()
	{
		return attackWith;
	}

	public AttackType getAttackType()
	{
		return attackType;
	}

	public BodyPart getBodyPart()
	{
		return bodyPart;
	}

	public int getNrStrikes()
	{
		return nrStrikes;
	}

	public int getDelay()
	{
		return 0;
	}

	public void incStrikes(int inc)
	{
		this.nrStrikes += inc;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		if (Maze.getInstance() != null)
		{
			Maze.getInstance().actorAttacks(getAttacker());
		}

		// deduct action points
		if (actionCost == -1)
		{
			attacker.getActionPoints().setCurrent(0);
		}
		else
		{
			if (attacker.getActionPoints().getCurrent() < actionCost)
			{
				// action points have changed since the attack was intended
				// todo: fumble condition of some kind?
				attacker.getActionPoints().setCurrent(0);
			}
			else
			{
				attacker.getActionPoints().decCurrent(actionCost);
			}
		}

		// deduct ammo if required
		// we assume that the ammo type matches
		if (attackWith.getAmmoRequired() != null)
		{
			this.attacker.deductAmmo(this);
		}

		// increase fatigue
		GameSys.getInstance().fatigueFromAttack(this);

		if (attacker instanceof PlayerCharacter)
		{
			return SpeechUtil.getInstance().attackEventSpeech((PlayerCharacter)attacker);
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean shouldClearText()
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return getAttackWith().describe(this);
	}
}
