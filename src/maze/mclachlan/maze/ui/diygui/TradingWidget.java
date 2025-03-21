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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.ui.diygui.render.maze.MazeRendererFactory;

/**
 *
 */
public class TradingWidget extends DIYPane
{
	private List<TradingItemWidget> itemWidgets;
	private final ActionListener listener = new TradingListener();
	private TradingItemWidget selected;
	private List<Item> items;
	private final int priceMultiplier;
	private final int goldAmount;
	private final ActionListener exteriorListener;
	private final PlayerCharacter pc;
	private int maxRows;
	private boolean showPrices;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param inventory
	 * 	the list of inventory to display
	 * @param priceMultiplier
	 * 	the price multiplier to apply to item prices
	 * @param goldAmount
	 * 	the amount of gold to display (<=0 for none)
	 * @param exteriorListener
	 * 	any exterior action listener to attach to the widgets buttons
	 * @param showPrices
	 * 	true if prices should be displayed
	 */
	public TradingWidget(
		PlayerCharacter pc,
		Inventory inventory,
		int priceMultiplier,
		int goldAmount,
		int maxRows,
		ActionListener exteriorListener,
		boolean showPrices)
	{
		this.pc = pc;
		this.maxRows = maxRows;
		this.showPrices = showPrices;

		if (maxRows == -1)
		{
			if (inventory != null)
			{
				this.maxRows = inventory.size()+5;
			}
			else
			{
				this.maxRows = 15;
			}
		}

		this.height = this.maxRows * 20;

		if (inventory == null)
		{
			inventory = new Inventory(0);
		}
		this.priceMultiplier = priceMultiplier;
		this.goldAmount = goldAmount;
		this.exteriorListener = exteriorListener;
		this.items = new ArrayList<>(inventory.getItems());

		if (this.goldAmount > 0)
		{
			this.items.add(0, new GoldPieces(this.goldAmount));
		}

		this.buildGui();
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui()
	{
		itemWidgets = new ArrayList<>(maxRows);
		int inset = 3;
		this.setLayoutManager(new DIYGridLayout(1, maxRows, inset, inset));

		for (int i=0; i< maxRows; i++)
		{
			TradingItemWidget tradingItemWidget = new TradingItemWidget();
			itemWidgets.add(tradingItemWidget);

			tradingItemWidget.addActionListener(listener);
			this.add(tradingItemWidget);

			if (exteriorListener != null)
			{
				tradingItemWidget.addActionListener(exteriorListener);
			}
		}

		refresh(items);

		setSelected(itemWidgets.get(0));
	}

	/*-------------------------------------------------------------------------*/
	void refresh(List<Item> newItems)
	{
		for (TradingItemWidget itemWidget : itemWidgets)
		{
			itemWidget.setItem(null);
		}

		this.items = new ArrayList<>(newItems);

		if (goldAmount>0 && !(items.get(0) instanceof GoldPieces))
		{
			items.add(0, new GoldPieces(goldAmount));
		}

		int widgetIndex=0;
		for (Item item : items)
		{
			if (item != null)
			{
				itemWidgets.get(widgetIndex++).setItem(item);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getWidgetName()
	{
		return MazeRendererFactory.TRADING_WIDGET;
	}

	/*-------------------------------------------------------------------------*/
	private void popupItemDetailsDialog(ItemWidget itemWidget)
	{
		DiyGuiUserInterface.instance.popupItemDetailsWidget(itemWidget.getItem());
	}

	/*-------------------------------------------------------------------------*/
	public void setSelected(TradingItemWidget iw)
	{
		if (iw == null)
		{
			this.selected = null;
		}
		else if (iw.getItem() != null)
		{
			this.selected = iw;
		}

		// bit of a hack
		for (TradingItemWidget tiw : itemWidgets)
		{
			if (tiw.price != null)
			{
				tiw.price.setForegroundColour(MazeRendererFactory.LABEL_FOREGROUND);
			}
			if (tiw != null)
			{
				tiw.setForegroundColour(MazeRendererFactory.LABEL_FOREGROUND);
			}
		}
		if (selected != null)
		{
			if (selected.price != null)
			{
				selected.price.setForegroundColour(Color.DARK_GRAY);
			}
			if (selected != null)
			{
				selected.setForegroundColour(Color.DARK_GRAY);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	boolean resetSelected()
	{
		int selectedIndex = -1;

		selectedIndex = getSelectedIndex();
		
		if (selectedIndex == -1)
		{
			for (TradingItemWidget itemWidget : itemWidgets)
			{
				if (itemWidget.getItem() != null)
				{
					setSelected(itemWidget);
					return true;
				}
			}
		}
		else
		{
			for (int i=selectedIndex; i>=0; i--)
			{
				if (itemWidgets.get(i).getItem() != null)
				{
					setSelected(itemWidgets.get(i));
					return true;
				}
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private int getSelectedIndex()
	{
		for (int i = 0; i < itemWidgets.size(); i++)
		{
			if (itemWidgets.get(i) == getSelected())
			{
				return i;
			}
		}
		
		return -1;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isFull()
	{
		return getNrItems() == maxRows;
	}

	/*-------------------------------------------------------------------------*/
	public int getNrItems()
	{
		int result = 0;
		for (TradingItemWidget i : itemWidgets)
		{
			if (i.getItem() != null)
			{
				result++;
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getID() != KeyEvent.KEY_PRESSED)
		{
			return;
		}

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_UP -> { e.consume(); moveSelectionUp(); }
			case KeyEvent.VK_DOWN -> { e.consume(); moveSelectionDown(); }
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void moveSelectionUp()
	{
		int index;
		
		if (getSelected() == null)
		{
			index = itemWidgets.size();
		}
		else
		{
			index = getSelectedIndex();
		}
		
		while (index > 0)
		{
			index--;
			if (itemWidgets.get(index).getItem() != null)
			{
				setSelected(itemWidgets.get(index));
				return;
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void moveSelectionDown()
	{
		int index;
		
		if (getSelected() == null)
		{
			index = 0;
		}
		else
		{
			index = getSelectedIndex();
		}
		
		while (index < itemWidgets.size()-1)
		{
			index++;
			if (itemWidgets.get(index).getItem() != null)
			{
				setSelected(itemWidgets.get(index));
				return;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		int itemWidgetSize =
			DIYToolkit.getInstance().getRendererProperties().getProperty(RendererProperties.Property.ITEM_WIDGET_SIZE);

		return new Dimension(
			150,
			maxRows * (itemWidgetSize+2));
	}

	/*-------------------------------------------------------------------------*/
	public ItemWidget getSelected()
	{
		return selected;
	}

	/*-------------------------------------------------------------------------*/
	private class TradingListener implements ActionListener
	{
		public boolean actionPerformed(ActionEvent event)
		{
			if (event.getSource() instanceof ItemWidget
				&& event.getEvent() instanceof MouseEvent)
			{
				MouseEvent e = (MouseEvent)event.getEvent();
				TradingItemWidget itemWidget = (TradingItemWidget)event.getSource();
				if (e.getButton() == MouseEvent.BUTTON3)
				{
					// right click
					GameSys.getInstance().attemptManualIdentify(itemWidget.getItem(), Maze.getInstance().getParty());
					itemWidget.refresh();

					popupItemDetailsDialog(itemWidget);
					setSelected(itemWidget);
					return true;
				}
				else if (e.getButton() == MouseEvent.BUTTON1)
				{
					// left click
					setSelected(itemWidget);
					return true;
				}
			}

			return false;
		}
	}

	/*-------------------------------------------------------------------------*/
	class TradingItemWidget extends ItemWidget
	{
		DIYLabel price;

		/*----------------------------------------------------------------------*/
		public TradingItemWidget()
		{
		}

		/*----------------------------------------------------------------------*/
		public void setBounds(int x, int y, int width, int height)
		{
			if (price == null)
			{
				price = new DIYLabel("", DIYToolkit.Align.RIGHT);
			}
			
			super.setBounds(x, y, width, height);
			this.price.setBounds(x+ getImageWidth(), y, width- getImageWidth(), height);
		}

		/*----------------------------------------------------------------------*/
		public void draw(Graphics2D g)
		{
			synchronized(getItemMutex())
			{
				super.draw(g);
				this.price.draw(g);
			}
		}

		/*----------------------------------------------------------------------*/
		public void setItem(Item item)
		{
			synchronized(getItemMutex())
			{
				super.setItem(item);

				if (item == null)
				{
					this.price.setText("");
				}
			}
		}

		/*----------------------------------------------------------------------*/
		protected void setText(Item item)
		{
			synchronized(getItemMutex())
			{
				super.setText(item);

				if (showPrices)
				{
					this.price.setText("" + GameSys.getInstance().getItemCost(item, priceMultiplier, pc));
				}
			}
		}
	}
}
