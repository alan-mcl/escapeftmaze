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
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class FoeType extends Race implements TypeDescriptor
{
	/*-------------------------------------------------------------------------*/
	public FoeType(String name, String description, int startingHitPointPercent,
		int startingActionPointPercent, int startingMagicPointPercent,
		StatModifier startingModifiers,
		StatModifier constantModifiers,
		StatModifier bannerModifiers,
		StatModifier attributeCeilings, BodyPart head,
		BodyPart torso, BodyPart leg, BodyPart hand,
		BodyPart foot, String leftHandIcon, String rightHandIcon,
		List<Gender> allowedGenders, boolean isMagicDead,
		Spell specialAbility,
		List<StartingKit> startingItems,
		List<NaturalWeapon> naturalWeapons,
		Map<String, List<String>> suggestedNames,
		String unlockVariable, String unlockDescription,
		String favouredEnemyModifer)
	{
		super(name, description, startingHitPointPercent,
			startingActionPointPercent, startingMagicPointPercent, startingModifiers,
			constantModifiers, bannerModifiers, attributeCeilings, head, torso, leg,
			hand, foot, leftHandIcon, rightHandIcon, allowedGenders, isMagicDead,
			specialAbility, startingItems, naturalWeapons, suggestedNames,
			unlockVariable, unlockDescription,
			favouredEnemyModifer);
	}

	/*-------------------------------------------------------------------------*/
	public FoeType(Race race)
	{
		super(
			race.getName(),
			race.getDescription(),
			race.getStartingHitPointPercent(),
			race.getStartingActionPointPercent(),
			race.getStartingMagicPointPercent(),
			race.getStartingModifiers(),
			race.getConstantModifiers(),
			race.getBannerModifiers(),
			race.getAttributeCeilings(),
			race.getHead(),
			race.getTorso(),
			race.getLeg(),
			race.getHand(),
			race.getFoot(),
			race.getLeftHandIcon(),
			race.getRightHandIcon(),
			race.getAllowedGenders(),
			race.isMagicDead(),
			race.getSpecialAbility(),
			race.getStartingItems(),
			race.getNaturalWeapons(),
			race.getSuggestedNames(),
			race.getUnlockVariable(),
			race.getUnlockDescription(),
			race.getFavouredEnemyModifier());
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		return getName();
	}
}
