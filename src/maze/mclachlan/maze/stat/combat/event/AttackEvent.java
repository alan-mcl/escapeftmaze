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
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.ui.diygui.animation.AnimationContext;

/**
 * Resolves one attack, which may consist of multiple strikes
 */
public class AttackEvent extends MazeEvent
{
	private final Combat combat;
	private final UnifiedActor attacker;
	private final UnifiedActor defender;
	private final AttackWith attackWith;
	private final AttackType attackType;
	private final int actionCost;
	private final int nrStrikes;
	private final MazeScript attackScript;
	private final MagicSys.SpellEffectType damageType;
	private final AnimationContext animationContext;
	private final StatModifier modifiers;

	/** a bag of random other state carried along with the attack */
	private final Set<String> tags = new HashSet<>();

	/*-------------------------------------------------------------------------*/
	public AttackEvent(
		Combat combat,
		UnifiedActor attacker,
		UnifiedActor defender,
		AttackWith weapon,
		AttackType attackType,
		int actionCost,
		int nrStrikes,
		MazeScript attackScript,
		MagicSys.SpellEffectType damageType,
		AnimationContext animationContext,
		StatModifier modifiers,
		String tag)
	{
		this.combat = combat;
		this.attacker = attacker;
		this.defender = defender;
		this.attackWith = weapon;
		this.attackType = attackType;
		this.actionCost = actionCost;
		this.nrStrikes = nrStrikes;
		this.attackScript = attackScript;
		this.damageType = damageType;
		this.animationContext = animationContext;
		this.modifiers = modifiers;

		if (tag != null)
		{
			this.tags.add(tag);
		}
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

	public int getNrStrikes()
	{
		return nrStrikes;
	}

	public int getDelay()
	{
		return 0;
	}

	public StatModifier getModifiers()
	{
		return modifiers;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		boolean fumble = false;
		List<MazeEvent> result = new ArrayList<>();

		if (Maze.getInstance() != null)
		{
			Maze.getInstance().actorAttacks(getAttacker(), getDefender(), getAttackWith());
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
				fumble = true;;
				attacker.getActionPoints().setCurrent(0);
			}
			else
			{
				attacker.getActionPoints().decCurrent(actionCost);
			}
		}

		// increase fatigue
		GameSys.getInstance().fatigueFromAttack(this);

		// Player Character attack lines
		if (attacker instanceof PlayerCharacter)
		{
			List<MazeEvent> speech = SpeechUtil.getInstance().attackEventSpeech((PlayerCharacter)attacker);
			if (speech != null)
			{
				result.addAll(speech);
			}
		}

		if (fumble)
		{
			result.add(new FumblesEvent(attacker));
		}
		else
		{
			for (int i = 0; i < nrStrikes; i++)
			{
				if (shouldAppendDelayEvent(attackScript.getEvents()))
				{
					result.add(new DelayEvent(Maze.getInstance().getUserConfig().getCombatDelay()));
				}

				result.add(
					new StrikeEvent(
						combat,
						attacker,
						defender,
						attackWith,
						attackType,
						damageType,
						animationContext,
						modifiers,
						tags));
			}
		}

		return result;
	}


	/*-------------------------------------------------------------------------*/
	private static boolean shouldAppendDelayEvent(List<MazeEvent> script)
	{
		if (script == null || script.isEmpty())
		{
			return false;
		}

		return !(script.get(script.size()-1) instanceof AnimationEvent);
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
