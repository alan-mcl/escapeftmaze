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
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.SpellEffect;

/**
 * 
 */
public interface AttackWith
{
	/** @return the internal unique name */
	String getName();

	/** @return the display name for UI purposes */
	String getDisplayName();

	/** @return any bonus to hit rolls */
	int getToHit();

	/** @return any bonus to armour penetration */
	int getToPenetrate();

	/** @return any bonus to critical rolls */
	int getToCritical();

	/** @return any bonus to initiative with this attack */
	int getToInitiative();

	/** @return the base damage of this attack */
	Dice getDamage();

	/** @return the type of damage this causes */
	MagicSys.SpellEffectType getDefaultDamageType();

	/** @return text for display purposes describing this */
	String describe(AttackEvent e);

	/** @return possible attack types for this attack */
	String[] getAttackTypes();

	/** @return maximum range of this attack, from {@link mclachlan.maze.stat.ItemTemplate.WeaponRange}*/
	int getMaxRange();

	/** @return minimum range of this attack, from {@link mclachlan.maze.stat.ItemTemplate.WeaponRange}*/
	int getMinRange();

	/** @return true if this is a ranged attack, false if it is a melee attack */
	boolean isRanged();

	/** @return true if backstab attacks can be executed with this */
	boolean isBackstabCapable();

	/** @return true if snipe attacks can be executed with this */
	boolean isSnipeCapable();

	/** @return possible spell effects of this attack */
	GroupOfPossibilities<SpellEffect> getSpellEffects();

	/** @return level of any spell effects caused by this attack */
	int getSpellEffectLevel();

	/** @return foe type this is effective against, may be null */
	TypeDescriptor slaysFoeType();

	/** @return attack script to execute */
	MazeScript getAttackScript();

	/** @return the ammo type this represents, null if this is not ammo */
	ItemTemplate.AmmoType isAmmoType();

	/** @return the ammo required by this, null if none required */
	List<ItemTemplate.AmmoType> getAmmoRequired();

	/** @return a constant from {@link mclachlan.maze.stat.ItemTemplate.WeaponSubType} */
	int getWeaponType();

	/** @return any action point cost for this (-1 sets the attacker action points to zero) */
	int getActionPointCost(UnifiedActor defender);
}
