/*
 * Copyright (c) 2013 Alan McLachlan
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

/**
 *
 */
public class EquipableSlot
{
	private String name;
	private String displayName;
	private Type type;
	private Item item;

	public EquipableSlot(String name, String displayName, Type type)
	{
		this.name = name;
		this.displayName = displayName;
		this.type = type;
	}

	public Item getItem()
	{
		return item;
	}

	public void setItem(Item item)
	{
		this.item = item;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Type getType()
	{
		return type;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasItemEquipped()
	{
		return this.item != null;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("EquipableSlot");
		sb.append("{type=").append(type);
		sb.append('}');
		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	public enum Type
	{
		NONE,
		PRIMARY_WEAPON,
		SECONDARY_WEAPON,
		HELM,
		TORSO_ARMOUR,
		LEG_ARMOUR,
		GLOVES,
		BOOTS,
		BANNER_ITEM,
		MISC_ITEM,
	}
}
