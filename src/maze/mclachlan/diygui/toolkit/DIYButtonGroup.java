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

package mclachlan.diygui.toolkit;

import java.util.*;
import mclachlan.diygui.DIYRadioButton;

/**
 *
 */
public class DIYButtonGroup
{
	int maxSelected;
	private LinkedList<DIYRadioButton> selections;
	List<DIYRadioButton> buttons = new ArrayList<DIYRadioButton>();

	/*-------------------------------------------------------------------------*/
	public DIYButtonGroup()
	{
		this(1);
	}

	/*-------------------------------------------------------------------------*/
	public DIYButtonGroup(int maxSelected)
	{
		this.maxSelected = maxSelected;
		selections = new LinkedList<DIYRadioButton>();
	}

	/*-------------------------------------------------------------------------*/
	public void addButton(DIYRadioButton button)
	{
		this.buttons.add(button);
		button.setButtonGroup(this);
	}
	
	/*-------------------------------------------------------------------------*/
	public boolean removeButton(DIYRadioButton button)
	{
		button.setButtonGroup(null);
		return this.buttons.remove(button);
	}
	
	/*-------------------------------------------------------------------------*/
	public Iterator<DIYRadioButton> getButtons()
	{
		return this.buttons.iterator();
	}

	/*-------------------------------------------------------------------------*/
	public void setSelected(DIYRadioButton button)
	{
		if (selections.contains(button))
		{
			// already selected
			return;
		}

		selections.offer(button);

		if (selections.size() <= maxSelected)
		{
			// nothing to do, return
			return;
		}

		// deselect the last in the queue.
		selections.remove().setSelected(false);
/*		Iterator<DIYRadioButton> i = this.getButtons();
		while (i.hasNext())
		{
			DIYRadioButton rb = i.next();
			if (rb != button)
			{
				rb.setSelected(false);
			}
		}*/
	}

	/*-------------------------------------------------------------------------*/
	public void setSelected(int index)
	{
		this.setSelected(buttons.get(index));
	}

	/*-------------------------------------------------------------------------*/
	public List<DIYRadioButton> getSelected()
	{
		return Collections.unmodifiableList(this.selections);
	}
}