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

import mclachlan.maze.data.v1.DataObject;

/**
 * A profile of where an actor can be struck in combat.
 */
public class BodyPart extends DataObject
{
	private String name;
	private String displayName;
	private StatModifier modifiers;
	private int damagePrevention;
	private int damagePreventionChance;
	private int nrWeaponHardpoints;
	private EquipableSlot.Type equipableSlotType;

	/*-------------------------------------------------------------------------*/

	public BodyPart()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param name
	 * 	The name of this body part
	 * @param displayName
	 * 	The description name of this body part
	 * @param modifiers
	 * 	Modifiers to an attack on this body part
	 * @param damagePrevention
	 * 	How much damage the natural armour on this body part prevents
	 * @param damagePreventionChance
	 * @param nrWeaponHardpoints
	 * @param equipableSlotType
	 */
	public BodyPart(
		String name,
		String displayName,
		StatModifier modifiers,
		int damagePrevention,
		int damagePreventionChance, int nrWeaponHardpoints,
		EquipableSlot.Type equipableSlotType)
	{
		this.displayName = displayName;
		this.modifiers = modifiers;
		this.name = name;
		this.damagePrevention = damagePrevention;
		this.damagePreventionChance = damagePreventionChance;
		this.nrWeaponHardpoints = nrWeaponHardpoints;
		this.equipableSlotType = equipableSlotType;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public String getDisplayName()
	{
		return displayName;
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getModifiers()
	{
		return modifiers;
	}

	/*-------------------------------------------------------------------------*/
	public int getDamagePrevention()
	{
		return damagePrevention;
	}

	/*-------------------------------------------------------------------------*/
	public int getDamagePreventionChance()
	{
		return damagePreventionChance;
	}

	/*-------------------------------------------------------------------------*/
	public void setDamagePrevention(int damagePrevention)
	{
		this.damagePrevention = damagePrevention;
	}

	public void setDamagePreventionChance(int damagePreventionChance)
	{
		this.damagePreventionChance = damagePreventionChance;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public void setModifiers(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/

	public EquipableSlot.Type getEquipableSlotType()
	{
		return equipableSlotType;
	}

	public void setEquipableSlotType(EquipableSlot.Type equipableSlotType)
	{
		this.equipableSlotType = equipableSlotType;
	}

	public int getNrWeaponHardpoints()
	{
		return nrWeaponHardpoints;
	}

	public void setNrWeaponHardpoints(int nrWeaponHardpoints)
	{
		this.nrWeaponHardpoints = nrWeaponHardpoints;
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

		BodyPart bodyPart = (BodyPart)o;

		if (getDamagePrevention() != bodyPart.getDamagePrevention())
		{
			return false;
		}
		if (getDamagePreventionChance() != bodyPart.getDamagePreventionChance())
		{
			return false;
		}
		if (getNrWeaponHardpoints() != bodyPart.getNrWeaponHardpoints())
		{
			return false;
		}
		if (!getName().equals(bodyPart.getName()))
		{
			return false;
		}
		if (getDisplayName() != null ? !getDisplayName().equals(bodyPart.getDisplayName()) : bodyPart.getDisplayName() != null)
		{
			return false;
		}
		if (getModifiers() != null ? !getModifiers().equals(bodyPart.getModifiers()) : bodyPart.getModifiers() != null)
		{
			return false;
		}
		return getEquipableSlotType() == bodyPart.getEquipableSlotType();
	}

	@Override
	public int hashCode()
	{
		int result = getName().hashCode();
		result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
		result = 31 * result + (getModifiers() != null ? getModifiers().hashCode() : 0);
		result = 31 * result + getDamagePrevention();
		result = 31 * result + getDamagePreventionChance();
		result = 31 * result + getNrWeaponHardpoints();
		result = 31 * result + (getEquipableSlotType() != null ? getEquipableSlotType().hashCode() : 0);
		return result;
	}
}
