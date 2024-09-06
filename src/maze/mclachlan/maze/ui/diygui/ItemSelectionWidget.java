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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.render.maze.MazeRendererFactory;

/**
 *
 */
public class ItemSelectionWidget extends DIYPane
{
	private int maxItems;
	private ActionListener listener = new ItemSelectionListener();
	private ItemWidget selected;
	private boolean showEquippedItems;
	private boolean showPackItems;
	private ItemWidget[] itemWidgets;
	private static final int COLS = 2;
	private static final int ROWS = 15;

	/*-------------------------------------------------------------------------*/
	public ItemSelectionWidget(
		Rectangle bounds,
		PlayerCharacter pc,
		boolean showEquippedItems,
		boolean showPackItems)
	{
		super(bounds);
		this.showEquippedItems = showEquippedItems;
		this.showPackItems = showPackItems;
		this.buildGui(pc);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui(PlayerCharacter pc)
	{
		if (showEquippedItems)
		{
			maxItems += 10;
		}
		if (showPackItems)
		{
			maxItems += PlayerCharacter.MAX_PACK_ITEMS;
		}

		itemWidgets = new ItemWidget[maxItems];
		int inset = 3;
		this.setLayoutManager(new DIYGridLayout(COLS, ROWS, inset, inset));

		for (int i = 0; i < itemWidgets.length; i++)
		{
			itemWidgets[i] = new ItemWidget();
			itemWidgets[i].setItem(null);
			itemWidgets[i].addActionListener(listener);
			this.add(itemWidgets[i]);
		}

		int i=0;
		if (showEquippedItems)
		{
			itemWidgets[i++].setItem(pc.getPrimaryWeapon());
			itemWidgets[i++].setItem(pc.getSecondaryWeapon());
			itemWidgets[i++].setItem(pc.getHelm());
			itemWidgets[i++].setItem(pc.getGloves());
			itemWidgets[i++].setItem(pc.getTorsoArmour());
			itemWidgets[i++].setItem(pc.getLegArmour());
			itemWidgets[i++].setItem(pc.getBoots());
			itemWidgets[i++].setItem(pc.getMiscItem1());
			itemWidgets[i++].setItem(pc.getMiscItem2());
			itemWidgets[i++].setItem(pc.getBannerItem());
		}

		if (showPackItems)
		{
			int invIndex=0;
			for (; i<maxItems; i++)
			{
				Item item = pc.getInventory().get(invIndex++);
				itemWidgets[i].setItem(item);
			}
		}

		for (ItemWidget itemWidget : itemWidgets)
		{
			if (itemWidget.getItem() != null)
			{
				setSelected(itemWidget);
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String getWidgetName()
	{
		return MazeRendererFactory.ITEM_SELECTION_WIDGET;
	}

	/*-------------------------------------------------------------------------*/
	private void popupItemDetailsDialog(ItemWidget itemWidget)
	{
		DiyGuiUserInterface.instance.popupItemDetailsWidget(itemWidget.getItem());
	}

	/*-------------------------------------------------------------------------*/
	public ItemWidget getSelected()
	{
		return selected;
	}

	/*-------------------------------------------------------------------------*/
	private void setSelected(ItemWidget iw)
	{
		if (iw.getItem() != null)
		{
			this.selected = iw;

			for (ItemWidget widget : itemWidgets)
			{
				if (widget != null)
				{
					widget.setForegroundColour(MazeRendererFactory.LABEL_FOREGROUND);
				}
			}

			this.selected.setForegroundColour(Color.DARK_GRAY);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Item getSelectedItem()
	{
		if (getSelected() != null)
		{
			return getSelected().getItem();
		}
		else
		{
			return null;
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private int indexOf(ItemWidget iw)
	{
		for (int i = 0; i < itemWidgets.length; i++)
		{
			if (itemWidgets[i] == iw)
			{
				return i;
			}
		}
		
		return -1;
	}
	
	/*-------------------------------------------------------------------------*/
	private int indexOf(int row, int col)
	{
		return row*COLS + col;
	}
	
	/*-------------------------------------------------------------------------*/
	public void moveSelectionUp()
	{
		int currentCol, currentRow; 
			
		if (getSelected() == null)
		{
			currentCol=0;
			currentRow=ROWS-1;
		}
		else
		{
			int index = indexOf(getSelected());
			currentCol = index%COLS;
			currentRow = index/COLS;
		}
		
		while (currentRow > 0)
		{
			currentRow--;
			int i = indexOf(currentRow, currentCol);
			if (i<maxItems && itemWidgets[i].getItem() != null)
			{
				setSelected(itemWidgets[indexOf(currentRow, currentCol)]);
				return;
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void moveSelectionDown()
	{
		int currentCol, currentRow; 
			
		if (getSelected() == null)
		{
			currentCol=0;
			currentRow=0;
		}
		else
		{
			int index = indexOf(getSelected());
			currentCol = index%COLS;
			currentRow = index/COLS;
		}
		
		while (currentRow < ROWS-1)
		{
			currentRow++;
			int i = indexOf(currentRow, currentCol);
			if (i<maxItems && itemWidgets[i].getItem() != null)
			{
				setSelected(itemWidgets[indexOf(currentRow, currentCol)]);
				return;
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void moveSelectionLeft()
	{
		int currentCol, currentRow; 
			
		if (getSelected() == null)
		{
			currentCol=COLS-1;
			currentRow=0;
		}
		else
		{
			int index = indexOf(getSelected());
			currentCol = index%COLS;
			currentRow = index/COLS;
		}
		
		while (currentCol > 0)
		{
			currentCol--;
			int i = indexOf(currentRow, currentCol);
			if (i<maxItems && itemWidgets[i].getItem() != null)
			{
				setSelected(itemWidgets[indexOf(currentRow, currentCol)]);
				return;
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void moveSelectionRight()
	{
		int currentCol, currentRow; 
			
		if (getSelected() == null)
		{
			currentCol=COLS-1;
			currentRow=0;
		}
		else
		{
			int index = indexOf(getSelected());
			currentCol = index%COLS;
			currentRow = index/COLS;
		}
		
		while (currentCol < COLS-1)
		{
			currentCol++;
			int i = indexOf(currentRow, currentCol);
			if (i<maxItems && itemWidgets[i].getItem() != null)
			{
				setSelected(itemWidgets[indexOf(currentRow, currentCol)]);
				return;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private class ItemSelectionListener implements ActionListener
	{
		public boolean actionPerformed(ActionEvent event)
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
					}
					popupItemDetailsDialog(itemWidget);
					setSelected(itemWidget);
				}
				else if (e.getButton() == MouseEvent.BUTTON1)
				{
					// left click
					setSelected(itemWidget);
				}

				return true;
			}

			return false;
		}
	}
}
