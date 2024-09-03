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
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.SpellEffect;

/**
 *
 */
public class NaturalWeapon extends DataObject implements AttackWith
{
	private String name;
	private String description;
	private boolean isRanged;
	private Dice damage;
	private MagicSys.SpellEffectType damageType;
	private TypeDescriptor slaysFoeType;
	private int[] attacks;
	private GroupOfPossibilities<SpellEffect> spellEffects;
	private int spellEffectLevel;
	private StatModifier modifiers;
	private int minRange;
	private int maxRange;
	private MazeScript attackScript;

	/*-------------------------------------------------------------------------*/
	public NaturalWeapon(
		String name,
		String description,
		boolean ranged,
		Dice damage,
		MagicSys.SpellEffectType damageType,
		StatModifier modifiers,
		int minRange,
		int maxRange,
		GroupOfPossibilities<SpellEffect> spellEffects,
		int spellEffectLevel,
		int[] attacks,
		TypeDescriptor slaysFoeType,
		MazeScript attackScript)
	{
		this.name = name;
		this.description = description;
		this.damageType = damageType;
		this.isRanged = ranged;
		this.modifiers = (modifiers!=null)?modifiers:new StatModifier();
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.spellEffects = spellEffects;
		this.spellEffectLevel = spellEffectLevel;
		this.attacks = attacks;
		this.slaysFoeType = slaysFoeType;
		this.attackScript = attackScript;
		this.damage = damage;
	}

	/*-------------------------------------------------------------------------*/
	public String getDisplayName()
	{
		return description;
	}

	public int getToHit()
	{
		return modifiers.getModifier(Stats.Modifier.ATTACK);
	}

	public int getToPenetrate()
	{
		return modifiers.getModifier(Stats.Modifier.TO_PENETRATE);
	}

	public int getToCritical()
	{
		if (isRanged)
		{
			return modifiers.getModifier(Stats.Modifier.RANGED_CRITICALS);
		}
		else
		{
			return modifiers.getModifier(Stats.Modifier.MELEE_CRITICALS);
		}
	}

	public Dice getDamage()
	{
		return damage;
	}

	public String describe(AttackEvent e)
	{
		String s = e.getAttacker().getDisplayName()+" "+description;
		if (e.getNrStrikes() > 1)
		{
			s += " x"+e.getNrStrikes();
		}
		return s;
	}

	public String[] getAttackTypes()
	{
		return null;
	}

	public int getMaxRange()
	{
		return maxRange;
	}

	public int getMinRange()
	{
		return minRange;
	}

	public boolean isRanged()
	{
		return isRanged;
	}

	public void setRanged(boolean ranged)
	{
		isRanged = ranged;
	}

	public boolean isBackstabCapable()
	{
		return false;
	}

	public boolean isSnipeCapable()
	{
		return false;
	}

	public GroupOfPossibilities<SpellEffect> getSpellEffects()
	{
		return spellEffects;
	}

	public int getSpellEffectLevel()
	{
		return spellEffectLevel;
	}

	public TypeDescriptor slaysFoeType()
	{
		return slaysFoeType;
	}

	public String getDescription()
	{
		return description;
	}

	public MazeScript getAttackScript()
	{
		return attackScript;
	}

	public ItemTemplate.AmmoType isAmmoType()
	{
		return null;
	}

	public List<ItemTemplate.AmmoType> getAmmoRequired()
	{
		return null;
	}

	@Override
	public int getToInitiative()
	{
		return 0;
	}

	@Override
	public int getWeaponType()
	{
		return ItemTemplate.WeaponSubType.NONE;
	}

	@Override
	public int getActionPointCost(UnifiedActor defender)
	{
		return 0;
	}

	public int[] getAttacks()
	{
		return attacks;
	}

	public StatModifier getModifiers()
	{
		return modifiers;
	}

	public String getName()
	{
		return name;
	}

	public TypeDescriptor getSlaysFoeType()
	{
		return slaysFoeType;
	}

	public MagicSys.SpellEffectType getDefaultDamageType()
	{
		return damageType;
	}

	/*-------------------------------------------------------------------------*/
	public void setAttacks(int[] attacks)
	{
		this.attacks = attacks;
	}

	public void setAttackScript(MazeScript attackScript)
	{
		this.attackScript = attackScript;
	}

	public void setDamage(Dice damage)
	{
		this.damage = damage;
	}

	public void setDamageType(MagicSys.SpellEffectType damageType)
	{
		this.damageType = damageType;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setMaxRange(int maxRange)
	{
		this.maxRange = maxRange;
	}

	public void setMinRange(int minRange)
	{
		this.minRange = minRange;
	}

	public void setModifiers(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setSlaysFoeType(TypeDescriptor slaysFoeType)
	{
		this.slaysFoeType = slaysFoeType;
	}

	public void setSpellEffectLevel(int spellEffectLevel)
	{
		this.spellEffectLevel = spellEffectLevel;
	}

	public void setSpellEffects(GroupOfPossibilities<SpellEffect> spellEffects)
	{
		this.spellEffects = spellEffects;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("NaturalWeapon");
		sb.append("{name='").append(name).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
