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
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class ActorLearnsSpellEvent extends MazeEvent
{
	PlayerCharacter pc;
	Spell spell;

	/*-------------------------------------------------------------------------*/
	public ActorLearnsSpellEvent(PlayerCharacter pc, Spell spell)
	{
		this.pc = pc;
		this.spell = spell;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getPlayerCharacter()
	{
		return pc;
	}
	
	/*-------------------------------------------------------------------------*/
	public Spell getSpell()
	{
		return spell;
	}

	/*-------------------------------------------------------------------------*/
	public boolean shouldClearText()
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		pc.getSpellBook().addSpell(spell);
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return getPlayerCharacter().getName()+" learns "+getSpell().getDisplayName()+"!";
	}
}
