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

package mclachlan.maze.ui.diygui.render.mf;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import mclachlan.diygui.DIYListBox;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.game.Maze;
import mclachlan.maze.ui.diygui.Constants;

/**
 * This actually ends up rendering the list box item
 */
public class MFListBoxRenderer extends Renderer
{
	/*-------------------------------------------------------------------------*/
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYListBox.ListItem listBoxItem = (DIYListBox.ListItem)widget;
		
		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
		}

		Object item = listBoxItem.getItem();
		Color textCol;
		if (listBoxItem.getParent().getSelected() == item)
		{
			Color col1, col2;
			
			if (listBoxItem.isEnabled())
			{
				col1 = Color.WHITE;
				col2 = Constants.Colour.GOLD;
				textCol = Colours.LIST_BOX_TEXT_SELECTED;
			}
			else
			{
				col1 = Color.LIGHT_GRAY.brighter();
				col2 = Color.LIGHT_GRAY.darker();
				textCol = Colours.LIST_BOX_TEXT_DISABLED;
			}

			RoundRectangle2D r = new RoundRectangle2D.Double(x, y, width, height, 4, 4);
			g.setPaint(new GradientPaint(x, y, col1, x, y+height, col2, true));
			g.fill(r);
		}
		else
		{
			if (listBoxItem.isEnabled())
			{
				if (listBoxItem.isHover())
				{
					textCol = Colours.LIST_BOX_TEXT_HIGHLIGHTED;
				}
				else
				{
					textCol = Colours.LIST_BOX_TEXT;
				}
			}
			else
			{
				textCol = Colours.LIST_BOX_TEXT_DISABLED;
			}
		}

		drawItem(g, x, y, width, height, listBoxItem, textCol);
	}

	/*-------------------------------------------------------------------------*/
	private void drawItem(Graphics2D g, int x, int y, int width, int height,
		DIYListBox.ListItem item, Color textCol)
	{
		Component comp = Maze.getInstance().getComponent();
		int textX = x;

		Image icon = item.getIcon();
		if (icon != null)
		{
			// draw the icon, then the text
			g.drawImage(icon, x, y+height/2-icon.getHeight(comp)/2, comp);
			textX += icon.getWidth(comp);
		}

		g.setColor(textCol);
		drawString(g, item, textX+4, y, width, height);
	}

	/*-------------------------------------------------------------------------*/
	private void drawString(Graphics g, Object item, int x, int y, int width, int height)
	{
		FontMetrics fm = g.getFontMetrics();
		
		String text = item.toString();
		int textHeight = fm.getAscent();
		
		// center the text on the Y axis
		int textY = y + height/2 + textHeight/2;

		g.drawString(text, x, textY);
	}
}
