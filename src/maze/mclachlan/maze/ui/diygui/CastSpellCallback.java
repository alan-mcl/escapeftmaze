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

package mclachlan.maze.ui.diygui;

import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public interface CastSpellCallback
{
	/**
	 * @param spell
	 * 	The spell to cast
	 * @param caster
	 * 	The player character casting this spell
	 * @param casterIndex
	 * 	The index of the caster in the party
	 * @param castingLevel
	 * 	The casting level chosen
	 * @param target
	 * 	The target of this spell casting, meaning varies according to the
	 * 	spell target type
	 * @return
	 * 	true if this class handled the spell casting, false if the default 
	 * 	out of combat behaviour must take place.
	 */
	public boolean castSpell(
		Spell spell,
		PlayerCharacter caster, int casterIndex,
		int castingLevel,
		int target);
}
