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

package mclachlan.maze.campaign.def.stat;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.stat.magic.ValueList;

/**
 *
 */
public class AcolyteApotheosis extends LevelAbility
{
	private final StatModifier statModifier;
	private final SpellLikeAbility demonAlly;
	private ArrayList<TypeDescriptor> types;

	/*-------------------------------------------------------------------------*/
	public AcolyteApotheosis()
	{
		statModifier = new StatModifier();
		statModifier.setModifier(Stats.Modifier.RESIST_FIRE, 50);
		statModifier.setModifier(Stats.Modifier.IMMUNE_TO_POISON, 1);
		statModifier.setModifier(Stats.Modifier.THREATEN, 5);
		statModifier.setModifier(Stats.Modifier.FLIER, 1);

		Spell spell = Database.getInstance().getSpell("Demon Ally");
		Value value = new Value(1, Value.SCALE.SCALE_WITH_CLASS_LEVEL);
		value.setReference("Acolyte");
		ValueList castingLevel = new ValueList(value);
		demonAlly = new SpellLikeAbility(spell, castingLevel);

		initTypes();
	}

	public void initTypes()
	{
		FoeType outsider = Database.getInstance().getFoeTypes().get("Outsider");
		types = new ArrayList<>();
		types.add(outsider);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public SpellLikeAbility getAbility()
	{
		return demonAlly;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public StatModifier getModifier()
	{
		return statModifier;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public NaturalWeapon getNaturalWeapon()
	{
		return Database.getInstance().getNaturalWeapons().get("Apotheosis Claw");
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Collection<TypeDescriptor> getTypeDescriptors()
	{
		if (types == null)
		{
			initTypes();
		}
		return types;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}

		AcolyteApotheosis that = (AcolyteApotheosis)o;

		if (!statModifier.equals(that.statModifier))
		{
			return false;
		}
		if (!demonAlly.equals(that.demonAlly))
		{
			return false;
		}
		return types.equals(that.types);
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + statModifier.hashCode();
		result = 31 * result + demonAlly.hashCode();
		result = 31 * result + types.hashCode();
		return result;
	}
}
