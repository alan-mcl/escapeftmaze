/*
 * Copyright (c) 2012 Alan McLachlan
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
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.ui.diygui.FilledBarWidget;

public class FilledBarWidgetRenderer extends Renderer
{
	/*-------------------------------------------------------------------------*/
	@Override
	public void render(Graphics2D g, int x, int y, int width, int height,
		Widget widget)
	{
		FilledBarWidget w = (FilledBarWidget)widget;

		int inset = 3;

		Color col = w.getBackgroundColour();
		if (col == null)
		{
			col = Color.GRAY;
		}
		int fillWidth;
		if (w.getMax() == 0)
		{
			fillWidth = 0;
		}
		else
		{
			fillWidth = width*w.getCurrent()/w.getMax();
		}
		if (fillWidth > width)
		{
			fillWidth = width;
		}

		drawBar(g, col, fillWidth, x, y+inset, width, height-inset*2);

		String text = null;
		switch (w.getText())
		{
			case NONE:
				break;
			case CUR_MAX:
				text = w.getCurrent()+" / "+w.getMax();
				break;
			case PERCENT:
				text = w.getCurrent()+"%";
				break;
			case CUSTOM:
				text = w.getCustomText();
				break;
		}

		if (text != null)
		{
			FontMetrics fm = g.getFontMetrics();

			int textHeight = fm.getAscent();
			int textWidth = fm.stringWidth(text);

			// center the text
			int textY = y + height/2 + textHeight/2;
			int textX = x + width/2 - textWidth/2;

			Color foreground = w.getForegroundColour();
			if (foreground == null)
			{
				foreground = MazeRendererFactory.LABEL_FOREGROUND;
			}
			g.setColor(foreground);
			g.drawString(text, textX, textY);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void drawBar(Graphics2D g, Color colour, int fillWidth,
		int x, int y, int barWidth, int barHeight)
	{
		Color borderCol1 = Color.LIGHT_GRAY.brighter();
		Color borderCol2 = Color.LIGHT_GRAY.darker();

		Color col2 = colour.darker();

		RoundRectangle2D border = new RoundRectangle2D.Double(
			x, y, barWidth, barHeight, 5, 5);
		RoundRectangle2D filler = new RoundRectangle2D.Double(
			x, y, fillWidth, barHeight, 5, 5);

		g.setPaint(new GradientPaint(x, y, colour, x+barWidth, y+barHeight, col2));
		g.fill(filler);

		g.setPaint(new GradientPaint(x, y, borderCol1, x+barWidth, y+barHeight, borderCol2));
		g.draw(border);
	}
}
