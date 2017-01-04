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
	int getBestModifier(Stats.Modifier modifier);
}
