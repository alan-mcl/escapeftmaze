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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.ItemSelectionWidget;
import mclachlan.maze.ui.diygui.ItemWidget;

public class ItemSelectionWidgetRenderer extends Renderer
{
	/*-------------------------------------------------------------------------*/
	@Override
	public void render(Graphics2D g, int x, int y, int width, int height, Widget w)
	{
		ItemSelectionWidget widget = (ItemSelectionWidget)w;

		ItemWidget selected = widget.getSelected();
		if (selected != null)
		{
			RoundRectangle2D rect = new RoundRectangle2D.Double(
				selected.x, selected.y, selected.width, selected.height, 5, 5);
			g.setPaint(new GradientPaint(
				selected.x, selected.y, Color.WHITE,
				selected.x, selected.y+selected.height, Constants.Colour.GOLD, true));
			g.fill(rect);
		}
	}
}