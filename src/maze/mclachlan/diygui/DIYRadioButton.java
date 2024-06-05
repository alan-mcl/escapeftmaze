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

package mclachlan.diygui;

import java.awt.*;
import java.awt.event.MouseEvent;
import mclachlan.diygui.toolkit.DIYButtonGroup;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Widget;

/**
 *
 */
public class DIYRadioButton extends Widget
{
	private boolean selected = false;
	private String caption;
	private DIYButtonGroup buttonGroup;

	/*-------------------------------------------------------------------------*/
	public DIYRadioButton(String caption)
	{
		this(caption, false);
	}
	
	/*-------------------------------------------------------------------------*/
	public DIYRadioButton(String caption, boolean selected)
	{
		super(0,0,1,1);
		this.caption = caption;
		this.setSelected(selected);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.RADIO_BUTTON;
	}

	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		Dimension dimension = DIYToolkit.getDimension(this.caption);
		dimension.setSize(dimension.getWidth()+40, dimension.getHeight()+5);
		return dimension;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setButtonGroup(DIYButtonGroup buttonGroup)
	{
		this.buttonGroup = buttonGroup;

		if (selected && this.buttonGroup != null)
		{
			this.buttonGroup.setSelected(this);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public String getCaption()
	{
		return caption;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;

		if (selected && this.buttonGroup != null)
		{
			this.buttonGroup.setSelected(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void processMouseClicked(MouseEvent e)
	{
		if (!this.isEnabled())
		{
			return;
		}
		
		if (!this.selected)
		{
			this.selected = true;
			
			if (this.buttonGroup != null)
			{
				this.buttonGroup.setSelected(this);
			}
		}

		super.processMouseClicked(e);
	}
}
