/*
 * Copyright (c) 2012 Alan McLachlan
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
public class ItemEnchantments extends DataObject
{
	private String name;
	private PercentageTable<ItemEnchantment> enchantments;

	private Map<String, ItemEnchantment> enchantmentsByName = new HashMap<>();

	public ItemEnchantments()
	{
	}

	/*-------------------------------------------------------------------------*/
	public ItemEnchantments(String name,
		PercentageTable<ItemEnchantment> enchantments)
	{
		this.name = name;
		setEnchantments(enchantments);
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public PercentageTable<ItemEnchantment> getEnchantments()
	{
		return enchantments;
	}

	public void setEnchantments(PercentageTable<ItemEnchantment> enchantments)
	{
		this.enchantments = enchantments;
		setEnchantmentsByName(enchantments);
	}

	/*-------------------------------------------------------------------------*/
	private void setEnchantmentsByName(
		PercentageTable<ItemEnchantment> enchantments)
	{
		this.enchantmentsByName = new HashMap<String, ItemEnchantment>();
		if (enchantments != null)
		{
			for (ItemEnchantment e : enchantments.getItems())
			{
				this.enchantmentsByName.put(e.getName(), e);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public ItemEnchantment getEnchantment(String enchantment)
	{
		return enchantmentsByName.get(enchantment);
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

		ItemEnchantments that = (ItemEnchantments)o;

		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
		{
			return false;
		}
		return getEnchantments() != null ? getEnchantments().equals(that.getEnchantments()) : that.getEnchantments() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getEnchantments() != null ? getEnchantments().hashCode() : 0);
		return result;
	}
}
