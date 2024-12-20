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

import java.util.*;

/**
 * An inventory is an ordered, indexed collection with a fixed number of slots.
 * Slots are indexed from zero.
 */
public class Inventory implements Iterable<Item>
{
	/**
	 * Max number of slots in this inventory.
	 */
	private final int nrSlots;

	/**
	 * Items in this inventory. This list has a fixed size of {@link #nrSlots},
	 * and item slots that are empty in this inventory contain a <code>null</code>.
	 */
	private final List<Item> items;

	/*-------------------------------------------------------------------------*/

	/**
	 * Creates a new inventory with the specified number of empty slots
	 */
	public Inventory(int nrSlots)
	{
		this.nrSlots = nrSlots;
		items = new ArrayList<>(nrSlots);
		for (int i=0; i<nrSlots; i++)
		{
			items.add(null);
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Creates a new inventory with the same contents as the given inventory
	 */
	public Inventory(Inventory other)
	{
		this.nrSlots = other.nrSlots;
		this.items = new ArrayList<>(other.items);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Creates a new inventory that contains the given items. Nulls in the list
	 * become open slots in the inventory. The max size of the inventory is
	 * set to the size of the list.
	 */
	public Inventory(List<Item> items)
	{
		this.nrSlots = items.size();
		this.items = new ArrayList<>(items);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Add the given item to the first open slot of this inventory.
	 * @return false if the item could not be added (usually because the inventory
	 * 	is full.
	 */
	public boolean add(Item item)
	{
		for (int i=0; i<nrSlots; i++)
		{
			if (items.get(i) == null)
			{
				items.set(i, item);
				return true;
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Add the given item to the first open slot of this inventory, or to the
	 * first stack of the same type of item.
	 * @return false if the item could not be added (usually because the inventory
	 * 	is full.
	 */
	public boolean addAndStack(Item item)
	{
		if (!item.isStackable())
		{
			return add(item);
		}

		for (int i=0; i<nrSlots; i++)
		{
			Item it = items.get(i);
			if (it != null && it.getName().equals(item.getName()))
			{
				CurMax stack = it.getStack();
				int space = stack.getMaximum() - stack.getCurrent();

				if (space >= item.getStack().getCurrent())
				{
					// we're done
					stack.incCurrent(item.getStack().getCurrent());
					return true;
				}
				else
				{
					//overflow: reduce the stack and continue
					stack.setCurrentToMax();
					item.getStack().decCurrent(space);
				}
			}
		}

		// didn't find an existing stack to soak up the items, add to the next open slot
		for (int i=0; i<nrSlots; i++)
		{
			if (items.get(i) == null)
			{
				items.set(i, item);
				return true;
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Add the given item to the given slot of this inventory.
	 * @return any item previously in this slot, or null if this was an open slot
	 */
	public Item add(Item item, int index)
	{
		Item result = items.get(index);
		items.set(index, item);
		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Removes the given item from the inventory, where ever it exists.
	 * @return false if the item could not be removed (usually because it does
	 * not exist in the inventory)
	 */
	public boolean remove(Item item)
	{
		for (int i=0; i<nrSlots; i++)
		{
			if (items.get(i) == item)
			{
				items.set(i, null);
				return true;
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public void remove(String itemName)
	{
		for (int i=0; i<nrSlots; i++)
		{
			Item item = items.get(i);
			if (item != null && item.getName().equals(itemName))
			{
				items.set(i, null);
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return The item at the given index in this inventory, or null if it is
	 * an open slot
	 */
	public Item get(int index)
	{
		return items.get(index);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return an iterator over the items in this inventory. Empty slots are
	 * not iterated over, and indexes of items in this iterator are not
	 * guaranteed to match their indexes within this iterator
	 */
	public Iterator<Item> iterator()
	{
		return getItems().iterator();
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * All of items in this inventory. Empty slots are not returned and indexes
	 * in the returned list are not guaranteed to match indexes in this inventory.
	 */
	public List<Item> getItems()
	{
		List<Item> result = new ArrayList<>();

		for (Item item : items)
		{
			if (item != null)
			{
				result.add(item);
			}
		}

		return Collections.unmodifiableList(result);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return true if this inventory contains the given item
	 */
	public boolean contains(Item item)
	{
		return items.contains(item);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return the index of the given item within this inventory
	 */
	public int indexOf(Item item)
	{
		return items.indexOf(item);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return the size of this inventory, i.e. the max nr of slots
	 */
	public int size()
	{
		return nrSlots;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Adds as many items as possible to the inventory, returning the others
	 *
	 * @param items Items to add.
	 * @return List of any items not added.
	 */
	public List<Item> addAll(List<Item> items)
	{
		List<Item> result = new ArrayList<>();

		for (Item i : items)
		{
			if (!add(i))
			{
				result.add(i);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		return this.items.toString();
	}

	/*-------------------------------------------------------------------------*/
	public void clear()
	{
		Collections.fill(items, null);
	}

	/*-------------------------------------------------------------------------*/
	public void sort(Comparator<Item> cmp)
	{
		this.items.sort(cmp);
	}
}
