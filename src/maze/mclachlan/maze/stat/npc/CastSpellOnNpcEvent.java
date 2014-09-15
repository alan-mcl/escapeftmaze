/*
 * Copyright (c) 2013 Alan McLachlan
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

package mclachlan.maze.stat.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class CastSpellOnNpcEvent extends MazeEvent
{
	private Spell spell;
	private int castingLevel;
	private PlayerCharacter caster;
	private Npc npc;

	/*-------------------------------------------------------------------------*/
	public CastSpellOnNpcEvent(Spell spell, PlayerCharacter caster,
		int castingLevel, Npc npc)
	{
		this.caster = caster;
		this.castingLevel = castingLevel;
		this.npc = npc;
		this.spell = spell;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public List<MazeEvent> resolve()
	{
		GameSys.getInstance().castSpellOnNpc(spell, caster, castingLevel, npc);
		return null;
	}
}
