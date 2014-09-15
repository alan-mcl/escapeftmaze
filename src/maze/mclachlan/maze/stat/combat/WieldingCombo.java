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

import mclachlan.maze.stat.*;

/**
 *
 */
public class WieldingCombo
{
	private String name;
	private String primaryHand, secondaryHand;
	private StatModifier modifiers;

	/*-------------------------------------------------------------------------*/
	public WieldingCombo(
		String name,
		String primaryHand,
		String secondaryHand,
		StatModifier modifiers)
	{
		this.name = name;
		this.primaryHand = primaryHand;
		this.secondaryHand = secondaryHand;
		this.modifiers = modifiers;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	public StatModifier getModifiers()
	{
		return modifiers;
	}

	public String getPrimaryHand()
	{
		return primaryHand;
	}

	public String getSecondaryHand()
	{
		return secondaryHand;
	}

	public void setModifiers(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPrimaryHand(String primaryHand)
	{
		this.primaryHand = primaryHand;
	}

	public void setSecondaryHand(String secondaryHand)
	{
		this.secondaryHand = secondaryHand;
	}

	/*-------------------------------------------------------------------------*/
	public int getModifier(String modifier)
	{
		return modifiers.getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/
	public boolean matches(Item primaryHand, Item secondaryHand)
	{
		return handMatches(this.primaryHand, primaryHand) &&
			handMatches(this.secondaryHand, secondaryHand);
	}

	/*-------------------------------------------------------------------------*/
	private boolean handMatches(String hand, Item item)
	{
		if (item == null && !Key.NONE.equals(hand))
		{
			// early exit
			return false;
		}

		return ((Key.NONE.equals(hand) && item == null) ||
			(Key.ANYTHING.equals(hand) && item != null) ||

			(Key.SHORT_WEAPON.equals(hand) && item.getType() == Item.Type.SHORT_WEAPON) ||
			(Key.EXTENDED_WEAPON.equals(hand) && item.getType() == Item.Type.EXTENDED_WEAPON) ||
			(Key.THROWN_WEAPON.equals(hand) && item.getType() == Item.Type.THROWN_WEAPON) ||
			(Key.THROWN_WEAPON.equals(hand) && item.getSubType() == ItemTemplate.WeaponSubType.THROWN) ||
			(Key.RANGED_WEAPON.equals(hand) && item.getType() == Item.Type.RANGED_WEAPON) ||
			(Key.AMMUNITION.equals(hand) && item.getType() == Item.Type.AMMUNITION) ||
			(Key.SHIELD.equals(hand) && item.getType() == Item.Type.SHIELD) ||

			(Key.SWORD.equals(hand) && item.getSubType() == ItemTemplate.WeaponSubType.SWORD) ||
			(Key.AXE.equals(hand) && item.getSubType() == ItemTemplate.WeaponSubType.AXE) ||
			(Key.POLEARM.equals(hand) && item.getSubType() == ItemTemplate.WeaponSubType.POLEARM) ||
			(Key.MACE.equals(hand) && item.getSubType() == ItemTemplate.WeaponSubType.MACE) ||
			(Key.STAFF.equals(hand) && item.getSubType() == ItemTemplate.WeaponSubType.STAFF) ||
			(Key.WAND.equals(hand) && item.getSubType() == ItemTemplate.WeaponSubType.WAND) ||
			(Key.MODERN.equals(hand) && item.getSubType() == ItemTemplate.WeaponSubType.MODERN) ||
			(Key.BOW.equals(hand) && item.getSubType() == ItemTemplate.WeaponSubType.BOW));

	}

	/*-------------------------------------------------------------------------*/
	public static class Key
	{
		public static final String NONE = "<nothing>";
		public static final String ANYTHING = "<anything>";

		public static final String SHORT_WEAPON = "<any short weapon>";
		public static final String EXTENDED_WEAPON = "<any extended weapon>";
		public static final String THROWN_WEAPON = "<any thrown weapon>";
		public static final String RANGED_WEAPON = "<any ranged weapon>";
		public static final String AMMUNITION = "<any ammunition>";
		public static final String SHIELD = "<any shield>";

		public static final String SWORD = "<any sword>";
		public static final String AXE = "<any axe>";
		public static final String POLEARM = "<any polearm>";
		public static final String MACE = "<any mace>";
		public static final String DAGGER = "<any dagger>";
		public static final String STAFF = "<any staff>";
		public static final String WAND = "<any wand>";
		public static final String MODERN = "<any modern weapon>";
		public static final String BOW = "<any bow>";
	}
}
