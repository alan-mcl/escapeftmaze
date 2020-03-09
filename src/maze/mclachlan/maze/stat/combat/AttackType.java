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

package mclachlan.maze.stat.combat;

import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.MagicSys;

/**
 *
 */
public class AttackType extends DataObject
{
	public static final AttackType NULL_ATTACK_TYPE = new AttackType(
		null, null, Stats.Modifier.NONE, MagicSys.SpellEffectType.NONE);

	private String name;
	private String verb;
	private Stats.Modifier attackModifier;
	private StatModifier modifiers;
	private MagicSys.SpellEffectType damageType;

	/*-------------------------------------------------------------------------*/
	public AttackType(String name, String verb, Stats.Modifier attackModifier,
		MagicSys.SpellEffectType damageType)
	{
		this.name = name;
		this.attackModifier = attackModifier;
		this.damageType = damageType;
		this.modifiers = new StatModifier();
		this.verb = verb;
	}

	/*-------------------------------------------------------------------------*/
	public AttackType(String name, String verb, Stats.Modifier attackModifier,
		MagicSys.SpellEffectType damageType, StatModifier statModifier)
	{
		this.name = name;
		this.attackModifier = attackModifier;
		this.damageType = damageType;
		this.modifiers = statModifier;
		this.verb = verb;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	public String getVerb()
	{
		return verb;
	}

	public StatModifier getModifiers()
	{
		return modifiers;
	}

	public Stats.Modifier getAttackModifier()
	{
		return attackModifier;
	}

	/*-------------------------------------------------------------------------*/
	public void setModifiers(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setVerb(String verb)
	{
		this.verb = verb;
	}

	public MagicSys.SpellEffectType getDamageType()
	{
		return damageType;
	}

	public void setDamageType(MagicSys.SpellEffectType damageType)
	{
		this.damageType = damageType;
	}

	public void setAttackModifier(Stats.Modifier attackModifier)
	{
		this.attackModifier = attackModifier;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("AttackType");
		sb.append("{name='").append(name).append('\'');
		sb.append(", verb='").append(verb).append('\'');
		sb.append(", modifiers=").append(modifiers);
		sb.append(", damageType=").append(damageType);
		sb.append('}');
		return sb.toString();
	}
}
