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

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.ActorActionIntention;
import mclachlan.maze.stat.Personality;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.AttackAction;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.CombatAction;
import mclachlan.maze.stat.combat.event.DamageEvent;

/**
 * Expresses the effect of a condition on an actor in terms of the actions
 * it forces him or her to take.
 */
public class ConditionEffect
{
	public static final ConditionEffect NONE = new ConditionEffect("none");

	private String name;

	/*-------------------------------------------------------------------------*/
	public ConditionEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public ConditionEffect(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Gives this condition effect a chance to substitute a character intention.
	 * @return
	 * 	The intention. If no change, the original intention should be returned.
	 */
	public ActorActionIntention checkIntention(
		UnifiedActor actor,
		Combat combat,
		ActorActionIntention intention,
		Condition condition)
	{
		return intention;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Gives this condition effect a chance to substitute an character action.
	 * @return
	 * 	The action to take.  If no change, the original action should be
	 * 	returned
	 */
	public CombatAction checkAction(UnifiedActor actor, CombatAction action, Condition condition)
	{
		return action;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	true if multiples of this condition effect are allowed
	 */
	public boolean isMultiplesAllowed()
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if this condition renders the character immobile.
	 */
	public boolean isImmobile(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	True if this condition renders the character helpless in the face of
	 * 	attacks
	 */
	public boolean isHelpless(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the given actor can be asked for combat intentions.
	 */
	public boolean askForCombatIntentions(UnifiedActor actor, Condition condition)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if this condition is removed by a Revitalise level up choice.
	 * @param actor
	 * @param condition
	 */
	public boolean isRemovedByRevitalise(UnifiedActor actor, Condition condition)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the given actor is aware of his surroundings.
	 */
	public boolean isAware(UnifiedActor actor, Condition condition)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the given actor is present, rather than elsewhere like blinked
	 * 	out or swallowed.
	 */
	public boolean isPresent(UnifiedActor actor, Condition condition)
	{
		return true;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the given actor is blinked out
	 */
	public boolean isBlinkedOut(UnifiedActor actor, Condition condition)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	Any stat modifier that applies to characters afflicted by this condition.
	 */
	public int getModifier(Stats.Modifier modifier, Condition condition, ConditionBearer bearer)
	{
		return 0;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The modifier (from {@link mclachlan.maze.stat.Stats.Modifier}) that defines
	 * 	immunity to this condition, or null if there is none.
	 */
	public Stats.Modifier getImmunityModifier()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the given actor can be attacked.
	 */
	public boolean canBeAttacked(UnifiedActor actor, Condition condition)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Called at the end of each turn.
	 */
	public List<MazeEvent> endOfTurn(Condition condition, long turnNr)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Called when the actor bearing this condition is dealt damage.
	 * @return
	 * 	an adjusted amount of damage
	 */
	public int damageToTarget(UnifiedActor actor, Condition condition, int damage, DamageEvent event)
	{
		return damage;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	Any personality speech that the character has when this condition
	 * 	afflicts him or her.
	 */
	public String getSpeechKey()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public String modifyPersonalitySpeech(String speechKey, String text, Personality p)
	{
		return text;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> attackOnConditionBearer(AttackAction attackAction, Condition condition)
	{
		return new ArrayList<MazeEvent>();
	}
}
