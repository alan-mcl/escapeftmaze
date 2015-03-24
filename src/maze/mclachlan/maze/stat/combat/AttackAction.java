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

package mclachlan.maze.stat.combat;

import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.ActorGroup;
import mclachlan.maze.stat.AttackWith;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.magic.MagicSys;

/**
 *
 */
public class AttackAction extends CombatAction
{
	private ActorGroup targetGroup;
	private AttackWith attackWith;
	private int nrStrikes;
	private MazeScript attackScript;
	private boolean isFirstAttack;
	private boolean isLightningStrike;
	private UnifiedActor defender;
	private AttackType attackType;
	private MagicSys.SpellEffectType damageType;

	/*-------------------------------------------------------------------------*/
	public AttackAction(
		ActorGroup targetGroup,
		AttackWith attackWith,
		int nrStrikes,
		MazeScript attackScript,
		boolean isFirstAttack,
		boolean lightningStrike,
		MagicSys.SpellEffectType damageType)
	{
		this.damageType = damageType;
		this.setTargetGroup(targetGroup);
		this.setAttackWith(attackWith);
		this.setNrStrikes(nrStrikes);
		this.setAttackScript(attackScript);
		this.setFirstAttack(isFirstAttack);
		this.setLightningStrike(lightningStrike);
	}

	/*-------------------------------------------------------------------------*/

	public ActorGroup getTargetGroup()
	{
		return targetGroup;
	}

	public void setTargetGroup(ActorGroup targetGroup)
	{
		this.targetGroup = targetGroup;
	}

	public AttackWith getAttackWith()
	{
		return attackWith;
	}

	public void setAttackWith(AttackWith attackWith)
	{
		this.attackWith = attackWith;
	}

	public int getNrStrikes()
	{
		return nrStrikes;
	}

	public void setNrStrikes(int nrStrikes)
	{
		this.nrStrikes = nrStrikes;
	}

	public MazeScript getAttackScript()
	{
		return attackScript;
	}

	public void setAttackScript(MazeScript attackScript)
	{
		this.attackScript = attackScript;
	}

	public boolean isFirstAttack()
	{
		return isFirstAttack;
	}

	public void setFirstAttack(boolean firstAttack)
	{
		isFirstAttack = firstAttack;
	}

	public boolean isLightningStrike()
	{
		return isLightningStrike;
	}

	public void setLightningStrike(boolean lightningStrike)
	{
		isLightningStrike = lightningStrike;
	}

	public UnifiedActor getDefender()
	{
		return defender;
	}

	public void setDefender(UnifiedActor defender)
	{
		this.defender = defender;
	}

	public AttackType getAttackType()
	{
		return attackType;
	}

	public void setAttackType(AttackType attackType)
	{
		this.attackType = attackType;
	}

	public MagicSys.SpellEffectType getDamageType()
	{
		return damageType;
	}

	public void setDamageType(MagicSys.SpellEffectType damageType)
	{
		this.damageType = damageType;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("AttackAction");
		sb.append("{targetGroup=").append(targetGroup);
		sb.append(", attackWith=").append(attackWith);
		sb.append(", nrStrikes=").append(nrStrikes);
		sb.append(", attackScript=").append(attackScript);
		sb.append(", isFirstAttack=").append(isFirstAttack);
		sb.append(", isLightningStrike=").append(isLightningStrike);
		sb.append(", defender=").append(defender);
		sb.append(", attackType=").append(attackType);
		sb.append(", damageType=").append(damageType);
		sb.append('}');
		return sb.toString();
	}
}
