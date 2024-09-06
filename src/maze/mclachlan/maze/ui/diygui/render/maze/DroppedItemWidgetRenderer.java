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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.RoundRectangle2D;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Item;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.ZoneDisplayWidget;

public class DroppedItemWidgetRenderer extends Renderer
{
	/*-------------------------------------------------------------------------*/
	@Override
	public void render(Graphics2D g, int x, int y, int width, int height, Widget w)
	{
		ZoneDisplayWidget.DroppedItemWidget widget = (ZoneDisplayWidget.DroppedItemWidget)w;
		Item item = widget.getItem();
		if (item == null)
		{
			return;
		}

		Image image = Database.getInstance().getImage(item.getImage());
		int imageWidth = image.getWidth(Maze.getInstance().getUi().getComponent());

		int imageX = x + width / 2 - imageWidth / 2;
		RoundRectangle2D outer = new RoundRectangle2D.Double(
			imageX, y, imageWidth, imageWidth, 5, 5);
		g.setPaint(new GradientPaint(
			imageX, y, Color.WHITE, x+width/2, y+width/2, Constants.Colour.GOLD));
		g.draw(outer);

		g.drawImage(image, imageX, y, Maze.getInstance().getUi().getComponent());
	}
}