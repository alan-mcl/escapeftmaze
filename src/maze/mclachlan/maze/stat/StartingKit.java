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

/**
 *
 */
public class StartingKit extends DataObject
{
	private String name;
	private String displayName;

	private String description;
	/** modifiers when chosen by a combat-focus class */
	private StatModifier combatModifiers;
	/** modifiers when chosen by a stealth-focus class */
	private StatModifier stealthModifiers;
	/** modifiers when chosen by a magic-focus class */
	private StatModifier magicModifiers;

	private String primaryWeapon;
	private String secondaryWeapon;
	private String helm;
	private String torsoArmour;
	private String legArmour;
	private String gloves;
	private String boots;
	private String miscItem1;
	private String miscItem2;
	private String bannerItem;
	private List<String> packItems;

	/**
	 * Which heroic classes can use this item. See
	 * {@link mclachlan.maze.data.Database#getCharacterClasses()}
	 */
	private Set<String> usableByCharacterClass;

	public StartingKit()
	{
	}

	/*-------------------------------------------------------------------------*/
	public StartingKit(
		String name,
		String displayName,
		String primaryWeapon,
		String secondaryWeapon,
		String helm,
		String torsoArmour,
		String legArmour,
		String gloves,
		String boots,
		String miscItem1,
		String miscItem2,
		String bannerItem,
		List<String> packItems,
		String description,
		StatModifier combatModifiers,
		StatModifier stealthModifiers,
		StatModifier magicModifiers,
		Set<String> usableByCharacterClass)
	{
		this.name = name;
		this.displayName = displayName;
		this.primaryWeapon = primaryWeapon;
		this.secondaryWeapon = secondaryWeapon;
		this.helm = helm;
		this.torsoArmour = torsoArmour;
		this.legArmour = legArmour;
		this.gloves = gloves;
		this.boots = boots;
		this.miscItem1 = miscItem1;
		this.miscItem2 = miscItem2;
		this.bannerItem = bannerItem;
		this.packItems = packItems;
		this.description = description;
		this.combatModifiers = combatModifiers;
		this.stealthModifiers = stealthModifiers;
		this.magicModifiers = magicModifiers;
		this.usableByCharacterClass = usableByCharacterClass;
	}

	/*-------------------------------------------------------------------------*/

	public void setPrimaryWeapon(String primaryWeapon)
	{
		this.primaryWeapon = primaryWeapon;
	}

	public void setSecondaryWeapon(String secondaryWeapon)
	{
		this.secondaryWeapon = secondaryWeapon;
	}

	public void setHelm(String helm)
	{
		this.helm = helm;
	}

	public void setTorsoArmour(String torsoArmour)
	{
		this.torsoArmour = torsoArmour;
	}

	public void setLegArmour(String legArmour)
	{
		this.legArmour = legArmour;
	}

	public void setGloves(String gloves)
	{
		this.gloves = gloves;
	}

	public void setBoots(String boots)
	{
		this.boots = boots;
	}

	public void setMiscItem1(String miscItem1)
	{
		this.miscItem1 = miscItem1;
	}

	public void setMiscItem2(String miscItem2)
	{
		this.miscItem2 = miscItem2;
	}

	public void setBannerItem(String bannerItem)
	{
		this.bannerItem = bannerItem;
	}

	public void setPackItems(List<String> packItems)
	{
		this.packItems = packItems;
	}

	public String getPrimaryWeapon()
	{
		return primaryWeapon;
	}

	public String getBannerItem()
	{
		return bannerItem;
	}

	public String getBoots()
	{
		return boots;
	}

	public String getGloves()
	{
		return gloves;
	}

	public String getHelm()
	{
		return helm;
	}

	public String getLegArmour()
	{
		return legArmour;
	}

	public String getMiscItem1()
	{
		return miscItem1;
	}

	public String getMiscItem2()
	{
		return miscItem2;
	}

	public List<String> getPackItems()
	{
		return packItems;
	}

	public String getSecondaryWeapon()
	{
		return secondaryWeapon;
	}

	public String getTorsoArmour()
	{
		return torsoArmour;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public StatModifier getCombatModifiers()
	{
		return combatModifiers;
	}

	public void setCombatModifiers(StatModifier combatModifiers)
	{
		this.combatModifiers = combatModifiers;
	}

	public StatModifier getMagicModifiers()
	{
		return magicModifiers;
	}

	public void setMagicModifiers(StatModifier magicModifiers)
	{
		this.magicModifiers = magicModifiers;
	}

	public StatModifier getStealthModifiers()
	{
		return stealthModifiers;
	}

	public void setStealthModifiers(StatModifier stealthModifiers)
	{
		this.stealthModifiers = stealthModifiers;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public Set<String> getUsableByCharacterClass()
	{
		return usableByCharacterClass;
	}

	public void setUsableByCharacterClass(Set<String> usableByCharacterClass)
	{
		this.usableByCharacterClass = usableByCharacterClass;
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getStartingItemNames()
	{
		List<String> result = new ArrayList<>();

		addIfNotNull(result, primaryWeapon);
		addIfNotNull(result, secondaryWeapon);
		addIfNotNull(result, helm);
		addIfNotNull(result, torsoArmour);
		addIfNotNull(result, legArmour);
		addIfNotNull(result, gloves);
		addIfNotNull(result, boots);
		addIfNotNull(result, miscItem1);
		addIfNotNull(result, miscItem2);
		addIfNotNull(result, bannerItem);
		result.addAll(packItems);

		return result;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		return this.displayName;
	}

	/*-------------------------------------------------------------------------*/
	private void addIfNotNull(List<String> result, String item)
	{
		if (item != null) result.add(item);
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

		StartingKit that = (StartingKit)o;

		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
		{
			return false;
		}
		if (getDisplayName() != null ? !getDisplayName().equals(that.getDisplayName()) : that.getDisplayName() != null)
		{
			return false;
		}
		if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
		{
			return false;
		}
		if (getCombatModifiers() != null ? !getCombatModifiers().equals(that.getCombatModifiers()) : that.getCombatModifiers() != null)
		{
			return false;
		}
		if (getStealthModifiers() != null ? !getStealthModifiers().equals(that.getStealthModifiers()) : that.getStealthModifiers() != null)
		{
			return false;
		}
		if (getMagicModifiers() != null ? !getMagicModifiers().equals(that.getMagicModifiers()) : that.getMagicModifiers() != null)
		{
			return false;
		}
		if (getPrimaryWeapon() != null ? !getPrimaryWeapon().equals(that.getPrimaryWeapon()) : that.getPrimaryWeapon() != null)
		{
			return false;
		}
		if (getSecondaryWeapon() != null ? !getSecondaryWeapon().equals(that.getSecondaryWeapon()) : that.getSecondaryWeapon() != null)
		{
			return false;
		}
		if (getHelm() != null ? !getHelm().equals(that.getHelm()) : that.getHelm() != null)
		{
			return false;
		}
		if (getTorsoArmour() != null ? !getTorsoArmour().equals(that.getTorsoArmour()) : that.getTorsoArmour() != null)
		{
			return false;
		}
		if (getLegArmour() != null ? !getLegArmour().equals(that.getLegArmour()) : that.getLegArmour() != null)
		{
			return false;
		}
		if (getGloves() != null ? !getGloves().equals(that.getGloves()) : that.getGloves() != null)
		{
			return false;
		}
		if (getBoots() != null ? !getBoots().equals(that.getBoots()) : that.getBoots() != null)
		{
			return false;
		}
		if (getMiscItem1() != null ? !getMiscItem1().equals(that.getMiscItem1()) : that.getMiscItem1() != null)
		{
			return false;
		}
		if (getMiscItem2() != null ? !getMiscItem2().equals(that.getMiscItem2()) : that.getMiscItem2() != null)
		{
			return false;
		}
		if (getBannerItem() != null ? !getBannerItem().equals(that.getBannerItem()) : that.getBannerItem() != null)
		{
			return false;
		}
		if (getPackItems() != null ? !getPackItems().equals(that.getPackItems()) : that.getPackItems() != null)
		{
			return false;
		}
		return getUsableByCharacterClass() != null ? getUsableByCharacterClass().equals(that.getUsableByCharacterClass()) : that.getUsableByCharacterClass() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
		result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
		result = 31 * result + (getCombatModifiers() != null ? getCombatModifiers().hashCode() : 0);
		result = 31 * result + (getStealthModifiers() != null ? getStealthModifiers().hashCode() : 0);
		result = 31 * result + (getMagicModifiers() != null ? getMagicModifiers().hashCode() : 0);
		result = 31 * result + (getPrimaryWeapon() != null ? getPrimaryWeapon().hashCode() : 0);
		result = 31 * result + (getSecondaryWeapon() != null ? getSecondaryWeapon().hashCode() : 0);
		result = 31 * result + (getHelm() != null ? getHelm().hashCode() : 0);
		result = 31 * result + (getTorsoArmour() != null ? getTorsoArmour().hashCode() : 0);
		result = 31 * result + (getLegArmour() != null ? getLegArmour().hashCode() : 0);
		result = 31 * result + (getGloves() != null ? getGloves().hashCode() : 0);
		result = 31 * result + (getBoots() != null ? getBoots().hashCode() : 0);
		result = 31 * result + (getMiscItem1() != null ? getMiscItem1().hashCode() : 0);
		result = 31 * result + (getMiscItem2() != null ? getMiscItem2().hashCode() : 0);
		result = 31 * result + (getBannerItem() != null ? getBannerItem().hashCode() : 0);
		result = 31 * result + (getPackItems() != null ? getPackItems().hashCode() : 0);
		result = 31 * result + (getUsableByCharacterClass() != null ? getUsableByCharacterClass().hashCode() : 0);
		return result;
	}
}
