/*
 * Copyright (c) 2013 Alan McLachlan
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

package mclachlan.maze.ui.diygui.render.maze;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import mclachlan.diygui.DIYComboBox;
import mclachlan.diygui.util.MutableTree;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.util.MazeException;

/**
 * Renderer for combo box items
 */
public class MazeComboItemRenderer extends Renderer
{
	/*-------------------------------------------------------------------------*/
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYComboBox.ComboItem comboItem = (DIYComboBox.ComboItem)widget;
		
		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
		}

		Object item = comboItem.getItem();
		String text = item.toString();

		MutableTree comboModel = comboItem.getParent().getModel();

		Color col1, col2;

		if (comboItem.isEnabled())
		{
			switch (comboItem.getState())
			{
				case DEFAULT:
					col1 = MazeComboBoxRenderer.GRADIENT_COLOUR_1;
					col2 = MazeComboBoxRenderer.GRADIENT_COLOUR_2;
					break;
				case HOVER:
					col1 = MazeComboBoxRenderer.GRADIENT_COLOUR_1.brighter();
					col2 = MazeComboBoxRenderer.GRADIENT_COLOUR_2.brighter();
					break;
				default:
					throw new MazeException("invalid state "+comboItem.getState());
			}
		}
		else
		{
			col1 = Color.LIGHT_GRAY.brighter();
			col2 = Color.LIGHT_GRAY.darker();
		}

		RoundRectangle2D r = new RoundRectangle2D.Double(x, y, width, height, 4, 4);
		g.setPaint(new GradientPaint(x, y, col1, x, y+height, col2, true));
		g.fill(r);

		int indicatorInset = 5;
		boolean hasChildren = !comboModel.getChildren(item).isEmpty();
		if (hasChildren)
		{
			if (comboItem.getParent().getPopupExpansionDirection() == DIYComboBox.PopupExpansionDirection.RIGHT)
			{
				MazeComboBoxRenderer.drawPopupIndicator(g,
					x + width - height, y + indicatorInset, height - indicatorInset * 2, height - indicatorInset * 2,
					col1, col2,
					DIYComboBox.PopupDirection.RIGHT);
			}
			else
			{
				MazeComboBoxRenderer.drawPopupIndicator(g,
					x + indicatorInset, y + indicatorInset, height - indicatorInset * 2, height - indicatorInset * 2,
					col1, col2,
					DIYComboBox.PopupDirection.LEFT);
			}
		}

		int textInset = 2;
		int textX;
		if (comboItem.getParent().getPopupExpansionDirection() == DIYComboBox.PopupExpansionDirection.RIGHT)
		{
			textX = x + textInset;
		}
		else
		{
			if (hasChildren)
			{
				textX = x +(indicatorInset +height) +textInset;
			}
			else
			{
				textX = x +textInset;
			}
		}

		g.setColor(Color.DARK_GRAY);
		drawString(g, text, textX, y, width, height);
	}

	/*-------------------------------------------------------------------------*/
	private void drawString(Graphics g, String text, int x, int y, int width, int height)
	{
		FontMetrics fm = g.getFontMetrics();
		
		int textHeight = fm.getAscent();
		
		// center the text on the Y axis
		int textY = y + height/2 + textHeight/2;

		g.drawString(text, x, textY);
	}
}
