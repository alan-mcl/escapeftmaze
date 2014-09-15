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

import mclachlan.diygui.toolkit.Widget;
import mclachlan.diygui.toolkit.DIYToolkit;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 *
 */
public class DIYButton extends Widget
{
	private String text;
	private State state = State.DEFAULT;
	private DIYToolkit.Align align = DIYToolkit.Align.CENTER;

	public enum State
	{
		DEFAULT, HOVER, DEPRESSED 
	}

	/*-------------------------------------------------------------------------*/
	public DIYButton(String label)
	{
		super(0, 0, 1, 1);
		this.text = label;
	}
	
	/*-------------------------------------------------------------------------*/
	public DIYButton(String text, DIYToolkit.Align align)
	{
		super(0, 0, 1, 1);
		this.align = align;
		this.text = text;
	}

	/*-------------------------------------------------------------------------*/
	public void setAlignment(DIYToolkit.Align align)
	{
		this.align = align;
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.BUTTON;
	}
	
	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		Dimension dimension = DIYToolkit.getDimension(this.text);
		
		if (dimension == null)
		{
			// make a guess.  There must be a better way than this.
			dimension = new Dimension(this.text.length()*5, 15);
		}
		
		dimension.setSize(dimension.getWidth()+10, dimension.getHeight()+5);
		return dimension;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setText(String text)
	{
		this.text = text;
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return text;
	}
	
	/*-------------------------------------------------------------------------*/
	public State getState()
	{
		return state;
	}
	
	/*-------------------------------------------------------------------------*/
	public DIYToolkit.Align getAlignment()
	{
		return align;
	}

	/*-------------------------------------------------------------------------*/
	public void processMousePressed(MouseEvent e)
	{
		if (!isEnabled())
		{
			return;
		}
		this.state = State.DEPRESSED;
	}
	
	/*-------------------------------------------------------------------------*/
	public void processMouseReleased(MouseEvent e)
	{
		if (!isEnabled())
		{
			return;
		}
		this.state = State.HOVER;
	}

	/*-------------------------------------------------------------------------*/
	public void processMouseEntered(MouseEvent e)
	{
		if (!isEnabled())
		{
			return;
		}
		this.state = State.HOVER;
	}

	/*-------------------------------------------------------------------------*/
	public void processMouseExited(MouseEvent e)
	{
		if (!isEnabled())
		{
			return;
		}
		this.state = State.DEFAULT;
	}
}
