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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.event.AttackEvent;

/**
 *
 */
public class Spell extends DataObject
{
	/** spell name, eg Force Bolt */ 
	private String name;
	
	/** spell display name */
	private String displayName;
	
	/** costs to cast this spell */
	private ValueList hitPointCost;
	private ValueList actionPointCost;
	private ValueList magicPointCost;

	/** spell target type, eg FOE.  A constant from {@link MagicSys.SpellTargetType} */
	private int targetType;
	
	/** spell school, eg Evocation.  A constant from {@link MagicSys.SpellSchool} */ 
	private String school;
	
	/** spell book, eg Red Magic.  A constant from {@link MagicSys.SpellBook}  */
	private MagicSys.SpellBook book;
	
	/** spell effects */ 
	private GroupOfPossibilities<SpellEffect> effects;

	/** spell description */
	private String description;

	/** spell level */
	private int level;

	/** spell requirements to learn */
	private StatModifier requirementsToLearn;

	/** spell requirements to cast */
	private List<ManaRequirement> requirementsToCast;

	/** the script that happens when the player casts this spell, typically animations */
	private MazeScript castByPlayerScript;

	/** the script that happens when a foe casts this spell, typically animations */
	private MazeScript castByFoeScript;

	/** when this spell can be cast,  a constant from {@link MagicSys.SpellUsabilityType} */
	private int usabilityType;

	/** the primary modifier involved in casting this spell */
	private Stats.Modifier primaryModifier;

	/** the secondary modifier involved in casting this spell */
	private Stats.Modifier secondaryModifier;

	/** if non null, this is a wild magic spell */
	private ValueList wildMagicValue;

	/** a table of spell names keyed on the wild magic value */
	private String[] wildMagicTable;

	/** projectile spells use projectile rules instead of saving throws */
	private boolean projectile;

	/*-------------------------------------------------------------------------*/
	public Spell(
		String name,
		String displayName,
		ValueList hitPointCost,
		ValueList actionPointCost,
		ValueList magicPointCost,
		String description,
		int level,
		int targetType,
		int usabilityType,
		String school,
		MagicSys.SpellBook book,
		GroupOfPossibilities<SpellEffect> effects,
		List<ManaRequirement> requirementsToCast,
		StatModifier requirementsToLearn,
		MazeScript castingScript,
		MazeScript castByFoeScript,
		Stats.Modifier primaryModifier,
		Stats.Modifier secondaryModifier,
		ValueList wildMagicValue,
		String[] wildMagicTable,
		boolean projectile)
	{
		this.displayName = displayName;
		this.hitPointCost = hitPointCost;
		this.actionPointCost = actionPointCost;
		this.magicPointCost = magicPointCost;
		this.description = description;
		this.level = level;
		this.name = name;
		this.targetType = targetType;
		this.school = school;
		this.book = book;
		this.effects = effects;
		this.requirementsToCast = requirementsToCast;
		this.requirementsToLearn = requirementsToLearn;
		this.castByPlayerScript = castingScript;
		this.castByFoeScript = castByFoeScript;
		this.usabilityType = usabilityType;
		this.primaryModifier = primaryModifier;
		this.secondaryModifier = secondaryModifier;
		this.wildMagicValue = wildMagicValue;
		this.wildMagicTable = wildMagicTable;
		this.projectile = projectile;
	}

	/*-------------------------------------------------------------------------*/
	public int getLevel()
	{
		return level;
	}

	public String getName()
	{
		return name;
	}

	public ValueList getHitPointCost()
	{
		return hitPointCost;
	}

	public void setHitPointCost(ValueList hitPointCost)
	{
		this.hitPointCost = hitPointCost;
	}

	public ValueList getActionPointCost()
	{
		return actionPointCost;
	}

	public void setActionPointCost(ValueList actionPointCost)
	{
		this.actionPointCost = actionPointCost;
	}

	public ValueList getMagicPointCost()
	{
		return magicPointCost;
	}

	public void setMagicPointCost(ValueList magicPointCost)
	{
		this.magicPointCost = magicPointCost;
	}

	public MagicSys.SpellBook getBook()
	{
		return book;
	}

	public GroupOfPossibilities<SpellEffect> getEffects()
	{
		return effects;
	}

	public String getSchool()
	{
		return school;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/** A constant from {@link MagicSys.SpellTargetType} */
	public int getTargetType()
	{
		return targetType;
	}

	public String getDescription()
	{
		return description;
	}

	public List<ManaRequirement> getRequirementsToCast()
	{
		return requirementsToCast;
	}

	public MazeScript getCastByPlayerScript()
	{
		return castByPlayerScript;
	}

	public MazeScript getCastByFoeScript()
	{
		return castByFoeScript;
	}

	/**
	 * @return
	 * when this spell can be cast,  a constant from {@link MagicSys.SpellUsabilityType}
	 */
	public int getUsabilityType()
	{
		return usabilityType;
	}

	public Stats.Modifier getPrimaryModifier()
	{
		return primaryModifier;
	}

	public StatModifier getRequirementsToLearn()
	{
		return requirementsToLearn;
	}

	public Stats.Modifier getSecondaryModifier()
	{
		return secondaryModifier;
	}

	public ValueList getWildMagicValue()
	{
		return wildMagicValue;
	}

	public String[] getWildMagicTable()
	{
		return wildMagicTable;
	}

	public void setBook(MagicSys.SpellBook book)
	{
		this.book = book;
	}

	public void setCastByFoeScript(MazeScript castByFoeScript)
	{
		this.castByFoeScript = castByFoeScript;
	}

	public void setCastByPlayerScript(MazeScript castByPlayerScript)
	{
		this.castByPlayerScript = castByPlayerScript;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setEffects(GroupOfPossibilities<SpellEffect> effects)
	{
		this.effects = effects;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPrimaryModifier(Stats.Modifier primaryModifier)
	{
		this.primaryModifier = primaryModifier;
	}

	public void setRequirementsToCast(List<ManaRequirement> requirementsToCast)
	{
		this.requirementsToCast = requirementsToCast;
	}

	public void setRequirementsToLearn(StatModifier requirementsToLearn)
	{
		this.requirementsToLearn = requirementsToLearn;
	}

	public void setSchool(String school)
	{
		this.school = school;
	}

	public void setSecondaryModifier(Stats.Modifier secondaryModifier)
	{
		this.secondaryModifier = secondaryModifier;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public void setTargetType(int targetType)
	{
		this.targetType = targetType;
	}

	public void setUsabilityType(int usabilityType)
	{
		this.usabilityType = usabilityType;
	}

	public void setWildMagicValue(ValueList wildMagicValue)
	{
		this.wildMagicValue = wildMagicValue;
	}

	public void setWildMagicTable(String[] wildMagicTable)
	{
		this.wildMagicTable = wildMagicTable;
	}

	public boolean isProjectile()
	{
		return projectile;
	}

	public void setProjectile(boolean projectile)
	{
		this.projectile = projectile;
	}

	/*-------------------------------------------------------------------------*/
	public String toString()
	{
		return name+" ("+level+")";
	}

	/*-------------------------------------------------------------------------*/
	public boolean meetsRequirements(UnifiedActor actor)
	{
		// must meet all mana requirements
		if (requirementsToCast != null)
		{
			for (ManaRequirement m : requirementsToCast)
			{
				if (!meetsRequirement(m, actor))
				{
					Maze.log(Log.DEBUG, this.getName() + ": failed mana present requirement: " + m);
					return false;
				}
			}
		}

		// must meet all spell effect requirements
		if (this.getEffects() != null)
		{
			for (SpellEffect se : this.getEffects().getPossibilities())
			{
				if (!se.meetsRequirements(actor))
				{
					Maze.log(Log.DEBUG, this.getName() + ": failed spell effect requirement: " + se.getName());
					return false;
				}
			}
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	private boolean meetsRequirement(ManaRequirement m, UnifiedActor actor)
	{
		if (actor == null)
		{
			return true;
		}
		
		int present = actor.getAmountMagicPresent(m.colour);
		return present >= m.amount;
	}

	/*-------------------------------------------------------------------------*/
	public static class SpellAttackWith implements AttackWith
	{
		private Spell spell;
		private UnifiedActor caster;
		private int castingLevel;

		public SpellAttackWith(Spell spell, UnifiedActor caster, int castingLevel)
		{
			this.spell = spell;
			this.caster = caster;
			this.castingLevel = castingLevel;
		}

		@Override
		public String getName()
		{
			return spell.getName();
		}

		@Override
		public String getDisplayName()
		{
			return spell.getDisplayName();
		}

		@Override
		public int getToHit()
		{
			int result = caster.getModifier(Stats.Modifier.POWER_CAST);

			if (caster.getModifier(Stats.Modifier.SPELL_SNIPING) > 0)
			{
				result += caster.getModifier(Stats.Modifier.SNIPE);
			}

			return result;
		}

		@Override
		public int getToPenetrate()
		{
			int result = caster.getModifier(Stats.Modifier.POWER_CAST);

			if (caster.getModifier(Stats.Modifier.SPELL_SNIPING) > 0)
			{
				result += caster.getModifier(Stats.Modifier.SNIPE);
			}

			return result;
		}

		@Override
		public int getToCritical()
		{
			return 0;
		}

		@Override
		public int getToInitiative()
		{
			int result = caster.getModifier(Stats.Modifier.POWER_CAST);

			if (caster.getModifier(Stats.Modifier.SPELL_SNIPING) > 0)
			{
				result += caster.getModifier(Stats.Modifier.SNIPE);
			}

			return result;
		}

		@Override
		public boolean isRanged()
		{
			return true;
		}

		@Override
		public boolean isBackstabCapable()
		{
			return false;
		}

		@Override
		public boolean isSnipeCapable()
		{
			return caster.getModifier(Stats.Modifier.SPELL_SNIPING) > 0;
		}

		/*----------------------------------------------------------------------*/
		// following methods return stub values since the Projectile spells
		// only use this for hit rolls

		@Override
		public Dice getDamage()
		{
			return null;
		}

		@Override
		public MagicSys.SpellEffectType getDefaultDamageType()
		{
			return MagicSys.SpellEffectType.NONE;
		}

		@Override
		public String describe(AttackEvent e)
		{
			return "";
		}

		@Override
		public String[] getAttackTypes()
		{
			return new String[]{};
		}

		@Override
		public int getMaxRange()
		{
			return ItemTemplate.WeaponRange.LONG;
		}

		@Override
		public int getMinRange()
		{
			return ItemTemplate.WeaponRange.MELEE;
		}

		@Override
		public GroupOfPossibilities<SpellEffect> getSpellEffects()
		{
			return spell.getEffects();
		}

		@Override
		public int getSpellEffectLevel()
		{
			return castingLevel;
		}

		@Override
		public TypeDescriptor slaysFoeType()
		{
			return null;
		}

		@Override
		public MazeScript getAttackScript()
		{
			if (caster instanceof PlayerCharacter)
			{
				return spell.getCastByPlayerScript();
			}
			else
			{
				return spell.getCastByFoeScript();
			}
		}

		@Override
		public ItemTemplate.AmmoType isAmmoType()
		{
			return null;
		}

		@Override
		public List<ItemTemplate.AmmoType> getAmmoRequired()
		{
			return null;
		}

		@Override
		public int getWeaponType()
		{
			return ItemTemplate.WeaponSubType.NONE;
		}
	}
}
