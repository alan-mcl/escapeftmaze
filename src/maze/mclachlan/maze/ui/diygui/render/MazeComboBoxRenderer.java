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

package mclachlan.maze.ui.diygui.render;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import mclachlan.diygui.DIYComboBox;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.ui.diygui.Constants.Colour;

/**
 *
 */
public class MazeComboBoxRenderer extends Renderer
{
	public static final Color GRADIENT_COLOUR_1 = Colour.GOLD.brighter();
	public static final Color GRADIENT_COLOUR_2 = Colour.GOLD.darker();

	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYComboBox combo = (DIYComboBox)widget;
		String text = combo.getSelected().toString();

		if (combo.getEditorText() != null)
		{
			text = combo.getEditorText();
		}

		// draw the combo
		Color col1, col2;

		if (combo.isEnabled())
		{
			switch (combo.getEditorState())
			{
				case DEFAULT:
					col1 = GRADIENT_COLOUR_1;
					col2 = GRADIENT_COLOUR_2;
					break;
				case HOVER:
					col1 = Color.WHITE;
					col2 = GRADIENT_COLOUR_1;
					break;
				case DEPRESSED:
					col1 = GRADIENT_COLOUR_2;
					col2 = GRADIENT_COLOUR_1;
					break;
				default:
					throw new MazeException("invalid state "+combo.getEditorState());
			}
		}
		else
		{
			col1 = Color.LIGHT_GRAY.brighter();
			col2 = Color.LIGHT_GRAY.darker();
		}

		int rounding = 10;
		RoundRectangle2D r = new RoundRectangle2D.Double(x, y, width, height, rounding, rounding);
		g.setPaint(new LinearGradientPaint(
			x, y, x, y+height,
			new float[]{0.0f, 0.7f, 1.0f},
			new Color[]{col2, col1, col2},
			MultipleGradientPaint.CycleMethod.REPEAT));
		g.fill(r);

//		RoundRectangle2D r = new RoundRectangle2D.Double(x, y, width, height, 4, 4);
//		g.setPaint(new GradientPaint(x, y, col1, x, y+height, col2, true));
//		g.fill(r);

		// draw the border
		g.setPaint(new GradientPaint(x, y, col1, x + width, y + height, col2, true));
		g.draw(r);

		FontMetrics fm = g.getFontMetrics();

		int textHeight = fm.getAscent();
		int textWidth = fm.stringWidth(text);

		// center the text on the Y axis
		int textY = y + height / 2 + textHeight / 2;

		int textX = x;
		if (combo.getAlignment() == DIYToolkit.Align.CENTER)
		{
			// center the text on the X axis
			textX = x + width / 2 - textWidth / 2;
		}
		else if (combo.getAlignment() == DIYToolkit.Align.RIGHT)
		{
			// align right
			textX = x + width - textWidth;
		}

		g.setColor(Color.DARK_GRAY);
		g.drawString(text, textX, textY);

		// popup direction indicator
		if (combo.isEnabled())
		{
			int inset = 5;

			if (combo.getPopupDirection() != DIYComboBox.PopupDirection.LEFT)
			{
				drawPopupIndicator(g,
					x+width-height, y+inset, height-inset*2, height-inset*2,
					col1, col2,
					combo.getPopupDirection());
			}
			else
			{
				drawPopupIndicator(g,
					x+inset, y+inset, height-inset*2, height-inset*2,
					col1, col2,
					combo.getPopupDirection());
			}
		}

		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
		}
	}

	public static void drawPopupIndicator(Graphics2D g,
		int x, int y, int width, int height,
		Color col1, Color col2,
		DIYComboBox.PopupDirection dir)
	{
		// draw a triangular indicator

		int x1, y1, x2, y2, x3, y3;

		switch (dir)
		{
			case LEFT:
				x1 = x+width; y1 = y;
				x2 = x; y2 = y+height/2;
				x3 = x+width; y3 = y+height;
				break;
			case RIGHT:
				x1 = x; y1 = y;
				x2 = x+width; y2 = y+height/2;
				x3 = x; y3 = y+height;
				break;
			case UP:
				x1 = x; y1 = y+height;
				x2 = x+width/2; y2 = y;
				x3 = x+width; y3 = y+height;
				break;
			case DOWN:
				x1 = x; y1 = y;
				x2 = x+width/2; y2 = y+height;
				x3 = x+width; y3 = y;
				break;
			default: throw new MazeException(dir.toString());
		}

		g.setColor(GRADIENT_COLOUR_2);
		g.drawPolyline(new int[]{x1-1,x2-1,x3-1}, new int[]{y1-1,y2-1,y3-1}, 3);

		g.setColor(GRADIENT_COLOUR_2);
		g.drawPolyline(new int[]{x1,x2,x3}, new int[]{y1,y2,y3}, 3);

		g.setColor(GRADIENT_COLOUR_1);
		g.drawPolyline(new int[]{x1+1,x2+1,x3+1}, new int[]{y1+1,y2+1,y3+1}, 3);
	}
}
