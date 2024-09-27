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

package mclachlan.diygui.render.dflt;

import java.awt.Color;
import java.awt.Graphics2D;
import mclachlan.diygui.DIYTooltip;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;

/**
 *
 */
public class DefaultTooltipRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height,
		Widget widget)
	{
		DIYTooltip tooltip = (DIYTooltip)widget;
		String text = tooltip.getText();

		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
		}

		int textY = y -2;
		int textX = x +2;

		Color foreground = DefaultRendererFactory.LABEL_FOREGROUND;
		g.setColor(foreground);
		g.drawString(text, textX, textY);
	}
}
