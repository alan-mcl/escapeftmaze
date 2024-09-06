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

package mclachlan.maze.ui.diygui.render.maze;

import java.awt.geom.RoundRectangle2D;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.DIYButton;
import java.awt.*;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.ui.diygui.Constants.Colour;

/**
 *
 */
public class MazeButtonRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYButton button = (DIYButton)widget;
		String text = button.getText();

		// draw the button
		Color col1, col2;

		if (button.isEnabled())
		{
			switch (button.getState())
			{
				case DEFAULT:
					col1 = Colour.GOLD.brighter();
					col2 = Colour.GOLD.darker();
					break;
				case HOVER:
					col1 = Color.WHITE;
					col2 = Colour.GOLD;
					break;
				case DEPRESSED:
					col1 = Color.ORANGE;
					col2 = Colour.GOLD.darker();
					break;
				default:
					throw new MazeException("invalid state "+button.getState());
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
			new float[]{0.0f, 0.3f, 1.0f},
			new Color[]{col2, col1, col2},
			MultipleGradientPaint.CycleMethod.REPEAT));
		g.fill(r);

		// draw the border
		g.setPaint(new GradientPaint(x, y, col1, x + width, y + height, col2, true));
		g.draw(r);

		FontMetrics fm = g.getFontMetrics();

		int textHeight = fm.getAscent();
		int textWidth = fm.stringWidth(text);

		// center the text on the Y axis
		int textY = y + height / 2 + textHeight / 2;

		int textX = x;
		if (button.getAlignment() == DIYToolkit.Align.CENTER)
		{
			// center the text on the X axis
			textX = x + width / 2 - textWidth / 2;
		}
		else if (button.getAlignment() == DIYToolkit.Align.RIGHT)
		{
			// align right
			textX = x + width - textWidth;
		}

		g.setColor(Color.DARK_GRAY);
		g.drawString(text, textX, textY);

		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
		}
	}
}
