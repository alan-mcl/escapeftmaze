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
import mclachlan.maze.stat.combat.ActorActionResolver;
import mclachlan.maze.stat.combat.AttackAction;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.ui.diygui.animation.AnimationContext;

/**
 *
 */
public class RunAwayAttemptEvent extends MazeEvent
{
	private UnifiedActor actor;
	private Combat combat;

	/*-------------------------------------------------------------------------*/
	public RunAwayAttemptEvent(UnifiedActor actor, Combat combat)
	{
		this.actor = actor;
		this.combat = combat;
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getActor()
	{
		return actor;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<>();

		if (combat == null)
		{
			result.add(new CowerInFearEvent(actor));
		}
		else if (combat.isPlayerCharacter(actor))
		{
			int nrFoes = 0;
			for (FoeGroup fg : combat.getFoes())
			{
				nrFoes += fg.numAlive();
			}

			boolean success = GameSys.getInstance().attemptToRunAway(actor, nrFoes);

			if (!success)
			{
				result.add(new RunAwayFailedEvent(actor));
			}
			else
			{
				result.add(new SuccessEvent());
				result.add(new RunAwaySuccessEvent(actor));
			}
		}
		else
		{
			int nrFoes = 0;
			nrFoes += combat.getPlayerParty().numAlive();

			for (FoeGroup fg : combat.getPartyAllies())
			{
				nrFoes += fg.numAlive();
			}

			// check for NO_SURVIVORS
			List<UnifiedActor> noSurvivorsOpponents =
				combat.getPlayerParty().getActorsWithModifier(Stats.Modifier.NO_SURVIVORS);

			for (UnifiedActor attacker : noSurvivorsOpponents)
			{
				Item attackerWeapon = getAttackerWeapon(attacker);

				AttackAction attackAction = getAttackAction(
					attacker,
					this.actor,
					this.combat,
					attackerWeapon,
					attackerWeapon.getDefaultDamageType(),
					1,
					attackerWeapon.getAttackScript());

				result.addAll(
					ActorActionResolver.attack(
						combat,
						attacker,
						this.actor,
						attackAction,
						new AnimationContext(attacker)));
			}

			boolean success = GameSys.getInstance().attemptToRunAway(actor, nrFoes);

			if (!success)
			{
				result.add(new RunAwayFailedEvent(actor));
			}
			else
			{
				result.add(new SuccessEvent());
				result.add(new RunAwaySuccessEvent(actor));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Item getAttackerWeapon(UnifiedActor attacker)
	{
		Item result = attacker.getPrimaryWeapon();

		if (result == null)
		{
			result = attacker.getSecondaryWeapon();
		}

		if (result == null)
		{
			result = GameSys.getInstance().getUnarmedWeapon(attacker, true);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private AttackAction getAttackAction(
		UnifiedActor source,
		UnifiedActor target,
		Combat combat,
		AttackWith weapon,
		MagicSys.SpellEffectType actionDamageType,
		int actionNrStrikes,
		MazeScript actionAttackScript)
	{
		AttackType actionAttackType  = GameSys.getInstance().getAttackType(weapon);

		AttackAction action = new AttackAction(
			combat.getActorGroup(target),
			weapon,
			actionNrStrikes,
			actionAttackScript,
			true,
			actionAttackType,
			actionDamageType);
		action.setActor(source);

		return action;
	}


	/*-------------------------------------------------------------------------*/
	public boolean shouldClearText()
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return getActor().getDisplayName() + " runs away...";
	}
}
