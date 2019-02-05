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

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.maze.stat.condition.CloudSpell;

/**
 * 
 */
public interface ActorGroup extends SpellTarget
{
	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The number of actors still alive in this group.
	 */
	int numAlive();

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A description of this actor group, for display on the UI
	 */
	String getDescription();

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A list of all the actors in this group
	 */
	List<UnifiedActor> getActors();

	/*-------------------------------------------------------------------------*/
	/**
	 * @param engagementRange
	 * 	The range at which the attacker is engaging this group
	 * @param minRange
	 * 	The min range at which the attacker can attack
	 * @param maxRange
	 * 	The max range at which the attacker can attack
	 *
	 * @return
	 * 	A list of all the actors within the given range constraints, or NULL if
	 * 	there are no possible targets.
	 */
	List<UnifiedActor> getActors(int engagementRange, int minRange, int maxRange);

	/*-------------------------------------------------------------------------*/
	void addCloudSpell(CloudSpell cloudSpell);

	/*-------------------------------------------------------------------------*/
	void removeCloudSpell(CloudSpell cloudSpell);

	/*-------------------------------------------------------------------------*/
	List<CloudSpell> getCloudSpells();

	int numActive();

	/*-------------------------------------------------------------------------*/
	int getAverageLevel();

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The best value for the given modifier in this actor group
	 */
	int getBestModifier(Stats.Modifier modifier);

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The best value for the given modifier in this actor group, excluding the
	 * 	given actor.
	 */
	int getBestModifier(Stats.Modifier modifier, UnifiedActor excluded);

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The actor with the highest modifier in the group. If there is a tie
	 * 	a random tied highest actor will be returned.
	 */
	UnifiedActor getActorWithBestModifier(Stats.Modifier modifier);

	/**
	 * @return
	 * 	The actor with the highest modifier in the group (ecluding the given actor).
	 * 	If there is a tie a random tied highest actor will be returned.
	 */
	UnifiedActor getActorWithBestModifier(Stats.Modifier modifier, UnifiedActor excluded);

	/**
	 * @return
	 * 	All actors in the group with a positive value for the given modifier
	 */
	List<UnifiedActor> getActorsWithModifier(Stats.Modifier modifier);
}
