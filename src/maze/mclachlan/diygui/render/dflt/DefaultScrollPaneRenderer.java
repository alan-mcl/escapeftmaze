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

import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.diygui.DIYScrollPane;
import java.awt.*;

/**
 *
 */
public class DefaultScrollPaneRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYScrollPane pane = (DIYScrollPane)widget;
		
		g.setColor(Color.LIGHT_GRAY);
		drawRect(pane.scrollBarBounds, g);
		drawRect(pane.sliderBounds, g);

		pane.upButton.draw(g);
		pane.downButton.draw(g);
		
		Widget w = pane.getContents();

		Graphics2D cg = (Graphics2D)g.create();
		try
		{
			cg.clipRect(x, y, width, height);
			cg.translate(x, y-pane.getRelativePosition());
			w.draw(cg);
		}
		finally
		{
			cg.dispose();
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private void drawRect(Rectangle r, Graphics g)
	{
		g.drawRect(r.x, r.y, r.width, r.height);
	}
}
