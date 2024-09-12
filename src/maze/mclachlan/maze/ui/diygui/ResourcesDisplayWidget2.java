/*
 * Copyright (c) 2014 Alan McLachlan
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

package mclachlan.maze.ui.diygui;

import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;

/**
 *
 */
public class ResourcesDisplayWidget2 extends StatModifierDisplayWidget
{
	private final boolean percent;

	/*-------------------------------------------------------------------------*/
	public ResourcesDisplayWidget2(
		String title,
		int hitPoints,
		int actionPoints,
		int magicPoints,
		boolean percent,
		boolean unknown)
	{
		super(title, null, 3, Stats.resourceModifiers, true, unknown);

		this.percent = percent;
		setResources(hitPoints, actionPoints, magicPoints, unknown);
	}

	/*-------------------------------------------------------------------------*/
	public void setResources(
		int hitPoints,
		int actionPoints,
		int magicPoints,
		boolean unknown)
	{
		StatModifier current = new StatModifier();
		current.setModifier(Stats.Modifier.HIT_POINTS, hitPoints);
		current.setModifier(Stats.Modifier.ACTION_POINTS, actionPoints);
		current.setModifier(Stats.Modifier.MAGIC_POINTS, magicPoints);

		this.setStatModifier(current, unknown);
	}

	/*-------------------------------------------------------------------------*/
	protected String descModifier(Stats.Modifier modifier, int value)
	{
		if (percent)
		{
			return value+"%";
		}
		else
		{
			return ""+value;
		}
	}
}
