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

/**
 * Used whenever we need to hack stuff by treating gold pieces as an item
 */
public class GoldPieces extends Item
{
	private int amount;

	public GoldPieces(int amount)
	{
		super(new ItemTemplate());
		this.amount = amount;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return "Gold";
	}

	@Override
	public int getType()
	{
		return ItemTemplate.Type.MONEY;
	}

	@Override
	public String getUnidentifiedName()
	{
		return "?coins";
	}

	@Override
	public String getImage()
	{
		return "item/defaultitem";
	}

	public int getAmount()
	{
		return amount;
	}

	@Override
	public int getBaseCost()
	{
		return getAmount();
	}

	@Override
	public BitSet getEquipableSlots()
	{
		return new BitSet();
	}
}
