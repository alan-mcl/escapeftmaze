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

package mclachlan.maze.stat.modifier;

import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.ModifierValue;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class ParryMod extends ModifierModification
{
	@Override
	public void getModification(UnifiedActor actor, List<ModifierValue> result)
	{
		if (actor.getModifier(Stats.Modifier.SWORD_PARRY) > 0 &&
			actor.getPrimaryWeapon() != null &&
			actor.getPrimaryWeapon().getSubType() == ItemTemplate.WeaponSubType.SWORD)
		{
			result.add(new ModifierValue(
				StringUtil.getModifierName(Stats.Modifier.SWORD_PARRY),
				actor.getModifier(Stats.Modifier.SWORD_PARRY)));
		}
		else if (actor.getModifier(Stats.Modifier.AXE_PARRY) > 0 &&
			actor.getPrimaryWeapon() != null &&
			actor.getPrimaryWeapon().getSubType() == ItemTemplate.WeaponSubType.AXE)
		{
			result.add(new ModifierValue(
				StringUtil.getModifierName(Stats.Modifier.AXE_PARRY),
					actor.getModifier(Stats.Modifier.AXE_PARRY)));
		}
		else if (actor.getModifier(Stats.Modifier.MACE_PARRY) > 0 &&
			actor.getPrimaryWeapon() != null &&
			actor.getPrimaryWeapon().getSubType() == ItemTemplate.WeaponSubType.MACE)
		{
			result.add(new ModifierValue(
				StringUtil.getModifierName(Stats.Modifier.MACE_PARRY),
					actor.getModifier(Stats.Modifier.MACE_PARRY)));
		}
		else if (actor.getModifier(Stats.Modifier.POLEARM_PARRY) > 0 &&
			actor.getPrimaryWeapon() != null &&
			actor.getPrimaryWeapon().getSubType() == ItemTemplate.WeaponSubType.POLEARM)
		{
			result.add(new ModifierValue(
				StringUtil.getModifierName(Stats.Modifier.POLEARM_PARRY),
					actor.getModifier(Stats.Modifier.POLEARM_PARRY)));
		}
		else if (actor.getModifier(Stats.Modifier.STAFF_PARRY) > 0 &&
			actor.getPrimaryWeapon() != null &&
			actor.getPrimaryWeapon().getSubType() == ItemTemplate.WeaponSubType.STAFF)
		{
			result.add(new ModifierValue(
				StringUtil.getModifierName(Stats.Modifier.STAFF_PARRY),
					actor.getModifier(Stats.Modifier.STAFF_PARRY)));
		}
		else if (actor.getModifier(Stats.Modifier.UNARMED_PARRY) > 0 &&
			actor.getPrimaryWeapon() == null && actor.getSecondaryWeapon() == null)
		{
			result.add(new ModifierValue(
				StringUtil.getModifierName(Stats.Modifier.UNARMED_PARRY),
				actor.getModifier(Stats.Modifier.UNARMED_PARRY)));
		}
	}
}
