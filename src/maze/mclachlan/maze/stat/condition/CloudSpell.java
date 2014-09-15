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

package mclachlan.maze.stat.condition;

import mclachlan.maze.data.Database;
import mclachlan.maze.stat.ActorGroup;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.magic.CloudSpellResult;
import mclachlan.maze.stat.magic.Spell;

/**
 * CloudSpells can be attached to: an ActorGroup
 */
public class CloudSpell
{
	private UnifiedActor source;
	private ActorGroup target;
	private int duration;
	private int strength;
	private int castingLevel;
	private CloudSpellResult template;
	private boolean isAttackingAllies;

	/*-------------------------------------------------------------------------*/
	public CloudSpell(
		CloudSpellResult template,
		UnifiedActor source,
		ActorGroup target,
		int castingLevel,
		boolean attackingAllies)
	{
		this.template = template;
		this.source = source;
		isAttackingAllies = attackingAllies;
		this.duration = template.getDuration().compute(source, castingLevel);
		this.strength = template.getStrength().compute(source, castingLevel);
		this.castingLevel = castingLevel;
		this.target = target;
	}

	/*-------------------------------------------------------------------------*/
	public void setTemplate(CloudSpellResult template)
	{
		this.template = template;
	}

	public void setSource(UnifiedActor source)
	{
		this.source = source;
	}

	public void setTarget(ActorGroup target)
	{
		this.target = target;
	}

	public void setCastingLevel(int castingLevel)
	{
		this.castingLevel = castingLevel;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	public void setStrength(int strength)
	{
		this.strength = strength;
	}

	public void setAttackingAllies(boolean attackingAllies)
	{
		isAttackingAllies = attackingAllies;
	}

	/*-------------------------------------------------------------------------*/
	public int getDuration()
	{
		return duration;
	}

	public String getIcon()
	{
		return template.getIcon();
	}

	public int getCastingLevel()
	{
		return castingLevel;
	}

	public int getStrength()
	{
		return strength;
	}

	public ActorGroup getTarget()
	{
		return target;
	}

	public UnifiedActor getSource()
	{
		return source;
	}

	public Spell getSpell()
	{
		return Database.getInstance().getSpell(template.getSpell());
	}

	public void decDuration(int value)
	{
		duration -= value;
	}

	public void decStrength(int value)
	{
		strength -= value;
	}

	public void expire()
	{
		target.removeCloudSpell(this);
	}

	public CloudSpellResult getTemplate()
	{
		return template;
	}

	public boolean isAttackingAllies()
	{
		return isAttackingAllies;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Called at the end of each turn.
	 */
	public void endOfTurn()
	{
		decDuration(1);
		decStrength(1);
	}
}