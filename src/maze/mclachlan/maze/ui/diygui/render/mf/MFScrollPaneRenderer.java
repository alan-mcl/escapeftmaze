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

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import mclachlan.diygui.DIYScrollPane;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class MFScrollPaneRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height,
		Widget widget)
	{
		DIYScrollPane pane = (DIYScrollPane)widget;
		Component comp = Maze.getInstance().getComponent();

		pane.upButton.setText(null);
		pane.upButton.setImage("scrollbar/button_up");
		pane.downButton.setText(null);
		pane.downButton.setImage("scrollbar/button_down");
		pane.slider.setText(null);
		pane.slider.setImage("scrollbar/slider");

		Rectangle r = pane.scrollBarBounds;

		BufferedImage barTop = Database.getInstance().getImage("ui/mf/scrollbar/bar_top");
		BufferedImage barCenter = Database.getInstance().getImage("ui/mf/scrollbar/bar_center");
		BufferedImage barBottom = Database.getInstance().getImage("ui/mf/scrollbar/bar_bottom");

		g.drawImage(barTop, r.x, r.y, comp);
		g.drawImage(barBottom, r.x, r.y + r.height - barBottom.getHeight(), comp);

		DIYToolkit.drawImageTiled(g, barCenter,
			r.x,
			r.y + barTop.getHeight(),
			barTop.getWidth(),
			r.height - barTop.getHeight() - barBottom.getHeight());

		pane.upButton.draw(g);
		pane.downButton.draw(g);
		pane.slider.draw(g);

		g.setColor(Colours.MF_BROWN);
		g.drawRect(x, y, width, height);

		Widget w = pane.getContents();

		Graphics2D cg = (Graphics2D)g.create();
		try
		{
			cg.clipRect(x, y, width, height);
			cg.translate(x, y - pane.getRelativePosition());
			w.draw(cg);
		}
		finally
		{
			cg.dispose();
		}
	}
}
