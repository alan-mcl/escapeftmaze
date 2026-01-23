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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.data.v2.V2Serialisable;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.script.LockOrTrap;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.SpellTargetUtils;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.util.MazeException;

/**
 * The consequences of a spell.
 */
public abstract class SpellResult implements V2Serialisable
{
	/**
	 * The foe types that this spell result affects, null if all
	 */
	private TypeDescriptor foeType;

	/**
	 * Any class focus affinity for this spell result. Empty or null means none.
	 */
	private CharacterClass.Focus focusAffinity;

	/*-------------------------------------------------------------------------*/
	public TypeDescriptor getFoeType()
	{
		return foeType;
	}

	/*-------------------------------------------------------------------------*/
	public void setFoeType(TypeDescriptor foeType)
	{
		this.foeType = foeType;
	}

	/*-------------------------------------------------------------------------*/
	public CharacterClass.Focus getFocusAffinity()
	{
		return focusAffinity;
	}

	/*-------------------------------------------------------------------------*/
	public void setFocusAffinity(CharacterClass.Focus focusAffinity)
	{
		this.focusAffinity = focusAffinity;
	}

	/*-------------------------------------------------------------------------*/
	public boolean appliesTo(UnifiedActor actor)
	{
		boolean appliesToFoeType = this.foeType == null || actor.getTypes().contains(this.foeType);

		boolean appliesToClassFocus = this.focusAffinity == null ||
				(actor.getCharacterClass() != null ||
				this.focusAffinity == actor.getCharacterClass().getFocus());

		return appliesToFoeType && appliesToClassFocus;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Apply this spell result to the given target Actor.
	 *
	 * @return
	 * 	A sequence of combat events.  NULL can be returned to indicate no
	 * 	events occur.
	 */ 
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent,
		Spell spell)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Apply this spell result to the given target Item.
	 *
	 * @return
	 * 	A sequence of combat events.  NULL can be returned to indicate no
	 * 	events occur.
	 */
	public List<MazeEvent> apply(
		UnifiedActor source,
		Item item,
		int castingLevel,
		SpellEffect parent)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Apply this spell result to the given lock/trap.
	 *
	 * @return
	 * 	A sequence of combat events.  NULL can be returned to indicate no
	 * 	events occur.
	 */
	public List<MazeEvent> apply(
		UnifiedActor source,
		LockOrTrap target,
		int castingLevel,
		SpellEffect parent)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Apply this spell result to the given target Tile.
	 *
	 * @return
	 * 	A sequence of combat events.  NULL can be returned to indicate no
	 * 	events occur.
	 */
	public List<MazeEvent> apply(
		UnifiedActor source,
		Tile tile,
		int castingLevel,
		SpellEffect parent)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		UnifiedActor source,
		ConditionBearer target,
		int castingLevel,
		SpellEffect spellEffect,
		Spell spell)
	{
		if (target instanceof UnifiedActor)
		{
			return apply(source, (UnifiedActor)target, castingLevel, spellEffect, spell);
		}
		else if (target instanceof Tile)
		{
			return apply(source, (Tile)target, castingLevel, spellEffect);
		}
		else if (target instanceof Item)
		{
			return apply(source, (Item)target, castingLevel, spellEffect);
		}
		else
		{
			throw new MazeException(target.toString());
		}
	}


	/*-------------------------------------------------------------------------*/
	protected List<MazeEvent> getList(MazeEvent... events)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();
		result.addAll(Arrays.asList(events));
		return result;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("SpellResult [").append(this.getClass()).append("]");
		sb.append("{foeType='").append(foeType).append('\'');
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		SpellResult that = (SpellResult)o;

		if (getFoeType() != null ? !getFoeType().equals(that.getFoeType()) : that.getFoeType() != null)
		{
			return false;
		}
		return getFocusAffinity() == that.getFocusAffinity();
	}

	@Override
	public int hashCode()
	{
		int result = getFoeType() != null ? getFoeType().hashCode() : 0;
		result = 31 * result + (getFocusAffinity() != null ? getFocusAffinity().hashCode() : 0);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public boolean meetsRequirements(UnifiedActor actor)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public SpellTarget getRandomSensibleSpellTarget(
		UnifiedActor caster,
		Spell spell,
		Combat combat)
	{
		// todo should do more implementations of this
		return SpellTargetUtils.getRandomLegalSpellTarget(caster, spell, combat);
	}

}
