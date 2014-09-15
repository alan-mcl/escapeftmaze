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

package mclachlan.maze.ui.diygui;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.*;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Item;

/**
 *
 */
public class LootWidget extends DIYPane
{
	private ItemWidget[] itemWidgets;
	private ActionListener listener = new LootActionListener();

	/*-------------------------------------------------------------------------*/
	public LootWidget(Rectangle bounds, List<Item> items)
	{
		super(bounds);
		this.buildGui(items);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui(List<Item> items)
	{
		itemWidgets = new ItemWidget[items.size()];
		int inset = 1;
		this.setLayoutManager(new DIYGridLayout(1, items.size(), inset, inset));

		for (int i = 0; i < itemWidgets.length; i++)
		{
			itemWidgets[i] = new ItemWidget();
			itemWidgets[i].setItem(items.get(i));
			itemWidgets[i].addActionListener(listener);
			this.add(itemWidgets[i]);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void grabItem(ItemWidget itemWidget)
	{
		Item item = itemWidget.getItem();

		if (DIYToolkit.getInstance().getCursorContents() != null)
		{
			if (!dropItem(itemWidget))
			{
				return;
			}
		}
		else
		{
			itemWidget.setItem(null);
			// todo: drop
		}

		setCursorToItem(item);
	}

	/*-------------------------------------------------------------------------*/
	private void setCursorToItem(Item item)
	{
		InventoryDisplayWidget.setCursorToItem(item);
	}

	/*-------------------------------------------------------------------------*/
	private boolean dropItem(ItemWidget itemWidget)
	{
		Object cursorContents = DIYToolkit.getInstance().getCursorContents();

		if (cursorContents != null
			&& cursorContents instanceof Item)
		{
			Item item = (Item)cursorContents;
			DIYToolkit.getInstance().clearCursor();

			if (item.isStackable()
				&& itemWidget.getItem() != null
				&& itemWidget.getItem().getName().equalsIgnoreCase(item.getName()))
			{
				// we're dropping one stackable item onto another of the same
				// type.  Merge them instead.

				int current = itemWidget.getItem().getStack().getCurrent();
				int other = item.getStack().getCurrent();

				if (other+current <= item.getStack().getMaximum())
				{
					// simply merge the two,
					itemWidget.getItem().getStack().incCurrent(other);
					itemWidget.setItem(itemWidget.getItem()); //refresh the text
					item = null;
					return false;
				}
				else
				{
					// a remainder.
					itemWidget.getItem().getStack().setCurrentToMax();
					itemWidget.setItem(itemWidget.getItem()); //refresh the text
					item.getStack().setCurrent(other+current - item.getStack().getMaximum());
					setCursorToItem(item);
					return false;
				}
			}
			else
			{
				itemWidget.setItem(item);
				// todo: drop item
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	List<Item> getRemainingItems()
	{
		List<Item> result = new ArrayList<Item>();

		for (ItemWidget itemWidget : itemWidgets)
		{
			if (itemWidget.getItem() != null)
			{
				result.add(itemWidget.getItem());
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void popupItemDetailsDialog(ItemWidget itemWidget)
	{
		DiyGuiUserInterface.instance.popupItemDetailsWidget(itemWidget.getItem());
	}
	
	/*-------------------------------------------------------------------------*/
	private class LootActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if (event.getSource() instanceof ItemWidget
				&& event.getEvent() instanceof MouseEvent)
			{
				MouseEvent e = (MouseEvent)event.getEvent();
				ItemWidget itemWidget = (ItemWidget)event.getSource();
				if (e.getButton() == MouseEvent.BUTTON3)
				{
					// right click
					if (itemWidget.getItem() != null)
					{
						GameSys.getInstance().attemptManualIdentify(itemWidget.getItem(), Maze.getInstance().getParty());
						itemWidget.refresh();
						popupItemDetailsDialog(itemWidget);
					}
				}
				else if (e.getButton() == MouseEvent.BUTTON1)
				{
					// left click

					if (itemWidget.getItem() == null)
					{
						// drop the item
						dropItem(itemWidget);
						Maze.getInstance().refreshCharacterData();
					}
					else
					{
						// grab the item
						grabItem(itemWidget);
						Maze.getInstance().refreshCharacterData();
					}
				}
			}
		}
	}
}
