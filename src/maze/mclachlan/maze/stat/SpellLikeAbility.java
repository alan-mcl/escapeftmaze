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

package mclachlan.maze.stat;

import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SpellLikeAbility
{
	private Spell spell;
	private Value castingLevel;

	/*-------------------------------------------------------------------------*/
	public SpellLikeAbility(Spell spell, Value castingLevel)
	{
		this.spell = spell;
		this.castingLevel = castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return spell.getName();
	}

	/*-------------------------------------------------------------------------*/
	public Spell getSpell()
	{
		return spell;
	}

	/*-------------------------------------------------------------------------*/
	public Value getCastingLevel()
	{
		return castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		return spell.getName() + " (" + castingLevel + ")";
	}

	/*-------------------------------------------------------------------------*/
	public boolean meetsRequirements(UnifiedActor actor)
	{
		return this.spell.meetsRequirements(actor);
	}

	/*-------------------------------------------------------------------------*/
	public boolean isUsableDuringMovement()
	{
		int usabilityType = spell.getUsabilityType();

		switch (usabilityType)
		{
			case MagicSys.SpellUsabilityType.ANY_TIME:
			case MagicSys.SpellUsabilityType.NON_COMBAT_ONLY:
				return true;
			case MagicSys.SpellUsabilityType.COMBAT_ONLY:
			case MagicSys.SpellUsabilityType.INVENTORY_SCREEN_ONLY:
			case MagicSys.SpellUsabilityType.LOCKS_TRAPS_ONLY:
			case MagicSys.SpellUsabilityType.NPC_ONLY:
				return false;
			default:
				throw new MazeException("invalid " + usabilityType);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean isUsableDuringCombat()
	{
		int usabilityType = spell.getUsabilityType();

		switch (usabilityType)
		{
			case MagicSys.SpellUsabilityType.ANY_TIME:
			case MagicSys.SpellUsabilityType.COMBAT_ONLY:
				return true;
			case MagicSys.SpellUsabilityType.NON_COMBAT_ONLY:
			case MagicSys.SpellUsabilityType.INVENTORY_SCREEN_ONLY:
			case MagicSys.SpellUsabilityType.LOCKS_TRAPS_ONLY:
			case MagicSys.SpellUsabilityType.NPC_ONLY:
				return false;
			default:
				throw new MazeException("invalid " + usabilityType);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean isUsableDuringEncounterActors()
	{
		int usabilityType = spell.getUsabilityType();

		switch (usabilityType)
		{
			case MagicSys.SpellUsabilityType.ANY_TIME:
			case MagicSys.SpellUsabilityType.NPC_ONLY:
				return true;
			case MagicSys.SpellUsabilityType.COMBAT_ONLY:
			case MagicSys.SpellUsabilityType.NON_COMBAT_ONLY:
			case MagicSys.SpellUsabilityType.INVENTORY_SCREEN_ONLY:
			case MagicSys.SpellUsabilityType.LOCKS_TRAPS_ONLY:
				return false;
			default:
				throw new MazeException("invalid " + usabilityType);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean isUsableDuringEncounterChest()
	{
		int usabilityType = spell.getUsabilityType();

		switch (usabilityType)
		{
			case MagicSys.SpellUsabilityType.ANY_TIME:
			case MagicSys.SpellUsabilityType.LOCKS_TRAPS_ONLY:
				return true;
			case MagicSys.SpellUsabilityType.NPC_ONLY:
			case MagicSys.SpellUsabilityType.COMBAT_ONLY:
			case MagicSys.SpellUsabilityType.NON_COMBAT_ONLY:
			case MagicSys.SpellUsabilityType.INVENTORY_SCREEN_ONLY:
				return false;
			default:
				throw new MazeException("invalid " + usabilityType);
		}
	}
}
