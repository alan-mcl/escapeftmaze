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

import java.awt.event.MouseEvent;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.diygui.toolkit.DIYToolkit;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 *
 */
public class DIYTextField extends Widget
{
	private boolean hover;
	private String text;
	private int maxLength=-1;

	/*-------------------------------------------------------------------------*/
	public DIYTextField()
	{
		this("", -1);
	}

	/*-------------------------------------------------------------------------*/
	public DIYTextField(String text, int maxLength)
	{
		super(0, 0, 1, 1);
		this.text = text;
		this.maxLength = maxLength;
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.TEXT_FIELD;
	}

	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		Dimension textDim = DIYToolkit.getDimension(maxLength);
		return new Dimension(textDim.width+4, 35);
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return text;
	}

	/*-------------------------------------------------------------------------*/
	public void setText(String text)
	{
		this.text = text;
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (!isEnabled())
		{
			return;
		}
		
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_BACK_SPACE:
				if (text.length() > 0)
					text = text.substring(0,text.length()-1);
				break;
			case KeyEvent.VK_SHIFT:
			case KeyEvent.VK_CONTROL:
			case KeyEvent.VK_ALT:
				// ignore
				break;
			case KeyEvent.VK_ENTER:
				// notify any action listeners
				notifyListeners(e);
				break;
			default:
				if (maxLength == -1 || this.text.length() < maxLength)
				{
					this.text += e.getKeyChar();
				}
		}
	}

	/*-------------------------------------------------------------------------*/

	public boolean isHover()
	{
		return hover;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void processMouseEntered(MouseEvent e)
	{
		this.hover = true;
	}

	@Override
	public void processMouseExited(MouseEvent e)
	{
		this.hover = false;
	}
}
