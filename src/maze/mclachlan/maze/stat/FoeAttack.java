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

import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.util.MazeException;
import java.util.List;

/**
 *
 */
public class FoeAttack implements AttackWith
{
	private String name;
	private String description;
	private Type type;

	public enum Type
	{
		MELEE_ATTACK,
		RANGED_ATTACK,
		CAST_SPELL,
		SPECIAL_ABILITY,
	}
	
	// for physical attacks
	private Dice damage;
	private int damageType;
	private String slaysFoeType;
	private int[] attacks;
	private GroupOfPossibilities<SpellEffect> spellEffects;
	private int spellEffectLevel;
	private StatModifier modifiers;
	private int minRange;
	private int maxRange;
	private MazeScript attackScript;

	// for casting spells
	private PercentageTable<FoeAttackSpell> spells;

	// for special abilities
	private FoeAttackSpell specialAbility;

	/*-------------------------------------------------------------------------*/
	/**
	 * This constructor expects the type to be 
	 * {@link Type#MELEE_ATTACK} or {@link Type#RANGED_ATTACK}
	 */ 
	public FoeAttack(
		String name,
		String description,
		Type type,
		Dice damage,
		int damageType,
		StatModifier modifiers,
		int minRange,
		int maxRange,
		GroupOfPossibilities<SpellEffect> spellEffects,
		int spellEffectLevel,
		int[] attacks,
		String slaysFoeType,
		MazeScript attackScript)
	{
		this.name = name;
		this.description = description;
		this.type = type;
		this.damageType = damageType;
		this.modifiers = (modifiers!=null)?modifiers:new StatModifier();
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.spellEffects = spellEffects;
		this.spellEffectLevel = spellEffectLevel;
		this.attacks = attacks;
		this.slaysFoeType = slaysFoeType;
		this.attackScript = attackScript;
		if (type != Type.MELEE_ATTACK 
			&& type != Type.RANGED_ATTACK)
		{
			throw new MazeException("This constructor expects the type to be " +
				"MELEE_ATTACK or RANGED_ATTACK");
		}
		this.damage = damage;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This constructor expects the type to be
	 * {@link Type#SPECIAL_ABILITY}
	 */
	public FoeAttack(String name,
		String description, Type type,
		FoeAttackSpell specialAbility)
	{
		this.name = name;
		this.description = description;
		this.type = type;
		if (type != Type.SPECIAL_ABILITY)
		{
			throw new MazeException("This constructor expects the type to be " +
				"SPECIAL_ABILITY");
		}
		this.specialAbility = specialAbility;

		// todo: spell ranges?
		this.minRange = ItemTemplate.WeaponRange.MELEE;
		this.maxRange = ItemTemplate.WeaponRange.LONG;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This constructor expects the type to be {@link Type#CAST_SPELL}
	 */ 
	public FoeAttack(String name,
		String description, Type type,
		PercentageTable<FoeAttackSpell> spells)
	{
		this.name = name;
		this.description = description;
		this.spells = spells;
		this.type = type;

		// todo: spell ranges?
		this.minRange = ItemTemplate.WeaponRange.MELEE;
		this.maxRange = ItemTemplate.WeaponRange.LONG;
		
		if (type != Type.CAST_SPELL)
		{
			throw new MazeException("This constructor expects the type to be CAST_SPELL");
		}
	}

	/*-------------------------------------------------------------------------*/
	public String getDisplayName()
	{
		return description;
	}

	public int getToHit()
	{
		return modifiers.getModifier(Stats.Modifiers.ATTACK);
	}

	public int getToPenetrate()
	{
		return modifiers.getModifier(Stats.Modifiers.TO_PENETRATE);
	}

	public int getToCritical()
	{
		if (type == Type.MELEE_ATTACK)
		{
			return modifiers.getModifier(Stats.Modifiers.MELEE_CRITICALS); 
		}
		else if (type == Type.RANGED_ATTACK)
		{
			return modifiers.getModifier(Stats.Modifiers.RANGED_CRITICALS);
		}
		else
		{
			return 0;
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
		return type == Type.RANGED_ATTACK;
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

	public String slaysFoeType()
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

	public FoeAttackSpell getSpecialAbility()
	{
		return specialAbility;
	}

	public PercentageTable<FoeAttackSpell> getSpells()
	{
		return spells;
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

	public String getSlaysFoeType()
	{
		return slaysFoeType;
	}

	public Type getType()
	{
		return type;
	}

	public int getDefaultDamageType()
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

	public void setDamageType(int damageType)
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

	public void setSlaysFoeType(String slaysFoeType)
	{
		this.slaysFoeType = slaysFoeType;
	}

	public void setSpecialAbility(FoeAttackSpell specialAbility)
	{
		this.specialAbility = specialAbility;
	}

	public void setSpellEffectLevel(int spellEffectLevel)
	{
		this.spellEffectLevel = spellEffectLevel;
	}

	public void setSpellEffects(GroupOfPossibilities<SpellEffect> spellEffects)
	{
		this.spellEffects = spellEffects;
	}

	public void setSpells(PercentageTable<FoeAttackSpell> spells)
	{
		this.spells = spells;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("FoeAttack");
		sb.append("{name='").append(name).append('\'');
		sb.append(", type=").append(type);
		sb.append('}');
		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static class FoeAttackSpell
	{
		Spell spell;
		Dice castingLevel;

		public FoeAttackSpell(Spell spell, Dice castingLevel)
		{
			this.spell = spell;
			this.castingLevel = castingLevel;
		}

		public String getName()
		{
			return spell.getName();
		}

		public Spell getSpell()
		{
			return spell;
		}

		public Dice getCastingLevel()
		{
			return castingLevel;
		}

		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder();
			sb.append("FoeAttackSpell");
			sb.append("{spell=").append(spell);
			sb.append(", castingLevel=").append(castingLevel);
			sb.append('}');
			return sb.toString();
		}
	}
}
