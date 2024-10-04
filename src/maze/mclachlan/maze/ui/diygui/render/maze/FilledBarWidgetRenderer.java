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

package mclachlan.maze.ui.diygui.render.maze;

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
		FilledBarWidget fbw = (FilledBarWidget)widget;

		int inset = 3;

		Color col = fbw.getBackgroundColour();
		if (col == null)
		{
			col = Color.GRAY;
		}


		int fillDistance, fillDistanceSub=0;
		if (fbw.getOrientation() == FilledBarWidget.Orientation.HORIZONTAL)
		{
			fillDistance = getFillDistance(width, fbw.getCurrent(), fbw.getMax());
		}
		else
		{
			fillDistance = getFillDistance(height-inset*2, fbw.getCurrent(), fbw.getMax());
		}

		if (fbw.getSub() > 0)
		{
			if (fbw.getOrientation() == FilledBarWidget.Orientation.HORIZONTAL)
			{
				fillDistanceSub = getFillDistance(width, fbw.getSub(), fbw.getMax());
			}
			else
			{
				fillDistanceSub = getFillDistance(height-inset*2, fbw.getSub(), fbw.getMax());
			}
		}

		drawBar(g, fbw, fillDistance, fillDistanceSub,
			x, y+inset, width, height-inset*2);

		String text = null;
		switch (fbw.getText())
		{
			case NONE:
				break;
			case CUR_MAX:
				text = fbw.getCurrent()+" / "+fbw.getMax();
				break;
			case PERCENT:
				text = fbw.getCurrent()+"%";
				break;
			case CUSTOM:
				text = fbw.getCustomText();
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

			Color foreground = fbw.getForegroundColour();
			if (foreground == null)
			{
				foreground = MazeRendererFactory.LABEL_FOREGROUND;
			}
			g.setColor(foreground);
			g.drawString(text, textX, textY);
		}
	}

	/*-------------------------------------------------------------------------*/
	private int getFillDistance(int maxDistance, int val, int max)
	{
		int result;
		if (max == 0)
		{
			result = 0;
		}
		else
		{
			result = maxDistance * val / max;
		}
		if (result > maxDistance)
		{
			result = maxDistance;
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void drawBar(Graphics2D g, FilledBarWidget fbw, int fillDistance, int fillDistanceSub,
		int x, int y, int barWidth, int barHeight)
	{
		Color borderCol1 = Color.LIGHT_GRAY.brighter();
		Color borderCol2 = Color.LIGHT_GRAY.darker();

		// filler bar
		RoundRectangle2D filler;
		if (fbw.getOrientation() == FilledBarWidget.Orientation.HORIZONTAL)
		{
			filler = new RoundRectangle2D.Double(x, y, fillDistance, barHeight, 5, 5);
		}
		else
		{
			filler = new RoundRectangle2D.Double(x, y +barHeight-fillDistance, barWidth, fillDistance, 5, 5);
		}

		g.setPaint(new GradientPaint(x, y, fbw.getBarColour(), x+barWidth, y+barHeight, fbw.getBarColour().darker()));
		g.fill(filler);

		// sub bar
		if (fillDistanceSub > 0)
		{
			RoundRectangle2D sub;
			if (fbw.getOrientation() == FilledBarWidget.Orientation.HORIZONTAL)
			{
				sub = new RoundRectangle2D.Double(x, y, fillDistanceSub, barHeight, 5, 5);
			}
			else
			{
				sub = new RoundRectangle2D.Double(x, y +barHeight -fillDistanceSub, barWidth, fillDistanceSub, 5, 5);
			}

			g.setColor(fbw.getSubBarColour());
			g.fill(sub);
		}

		// border
		RoundRectangle2D border = new RoundRectangle2D.Double(
			x, y, barWidth, barHeight, 5, 5);
		g.setPaint(new GradientPaint(x, y, borderCol1, x+barWidth, y+barHeight, borderCol2));
		g.draw(border);
	}
}
