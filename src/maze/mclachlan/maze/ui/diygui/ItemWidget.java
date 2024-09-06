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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Item;
import mclachlan.maze.ui.diygui.render.maze.MazeRendererFactory;

/**
 *
 */
public class ItemWidget extends ContainerWidget
{
	private Image image;
	private Item item;
	private String text;

	private final Object itemMutex = new Object();
	private int imageWidth;

	/*-------------------------------------------------------------------------*/
	public ItemWidget()
	{
		super(0,0,1,1);
	}

	/*-------------------------------------------------------------------------*/
	public ItemWidget(Rectangle bounds, Item item)
	{
		super(bounds);
		setItem(item);
	}

	/*-------------------------------------------------------------------------*/
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		if (this.getImage() != null)
		{
			this.setImageWidth(getImage().getWidth(Maze.getInstance().getComponent()));
		}
		else
		{
			// todo: sensible default?
			this.setImageWidth(width);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setItem(Item item)
	{
		synchronized(getItemMutex())
		{
			this.item = item;

			if (item == null)
			{
				image = null;
				text = null;
				return;
			}

			this.setImage(Database.getInstance().getImage(item.getImage()));
			setText(item);

			//reset the bounds so that the label is re-sized if required
			setBounds(x, y, width, height);
		}
	}

	/*-------------------------------------------------------------------------*/
	protected void setText(Item item)
	{
		String displayName = item.getDisplayName();

		if (item.isStackable())
		{
			displayName += " ("+item.getStack().getCurrent()+")";
		}
		else if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED &&
			item.getCharges() != null && item.getCharges().getMaximum() > 1)
		{
			displayName += " ("+item.getCharges().getCurrent()+"/"+
				item.getCharges().getMaximum()+")";
		}

		this.setText(displayName);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getWidgetName()
	{
		return MazeRendererFactory.ITEM_WIDGET;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Dimension getPreferredSize()
	{
		// todo: hard coded. would be nice to figure this out from the actual image
		int itemSlotSize = 25;
		String s = this.getText()==null?"|":this.getText();
		Dimension textWidth = DIYToolkit.getDimension(s);
		return new Dimension(textWidth.width + itemSlotSize, itemSlotSize);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		setItem(this.getItem());
	}

	public Object getItemMutex()
	{
		return itemMutex;
	}

	public Item getItem()
	{
		return item;
	}

	public Image getImage()
	{
		return image;
	}

	public void setImage(Image image)
	{
		this.image = image;
	}

	public int getImageWidth()
	{
		return imageWidth;
	}

	public void setImageWidth(int imageWidth)
	{
		this.imageWidth = imageWidth;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}
}
