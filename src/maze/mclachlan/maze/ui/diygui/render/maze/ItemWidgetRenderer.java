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
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.stat.Item;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.ItemWidget;

public class ItemWidgetRenderer extends Renderer
{
	/*-------------------------------------------------------------------------*/
	@Override
	public void render(Graphics2D g, int x, int y, int width, int height, Widget w)
	{
		ItemWidget widget = (ItemWidget)w;
		synchronized (widget.getItemMutex())
		{
			Image slotImage = DIYToolkit.getInstance().getRendererProperties().getImageResource("icon/itemslot");
			int iconSize = widget.height;
			Rectangle iconBounds = new Rectangle(widget.x, widget.y, iconSize, height);

			// icon
			DIYToolkit.drawImageAligned(g, slotImage, iconBounds, DIYToolkit.Align.CENTER);
			if (widget.getItem() != null)
			{
				DIYToolkit.drawImageAligned(g, widget.getImage(), iconBounds, DIYToolkit.Align.CENTER);
			}

			// hover on the icon
			if (widget.isHover())
			{
				Color col1 = Color.WHITE;
				Color col2 = Constants.Colour.GOLD;

				g.setPaint(new GradientPaint(iconBounds.x, iconBounds.y, col1,
					iconBounds.x+iconBounds.width, iconBounds.y+iconBounds.width, col2));
				Rectangle hover = new Rectangle(
					iconBounds.x+3,
					iconBounds.y+3,
					iconBounds.width-6,
					iconBounds.height-6);
				g.draw(hover);
			}

			if (widget.getStyle() == ItemWidget.Style.ICON_AND_TEXT)
			{
				// item display name
				if (widget.getText() != null)
				{
					Rectangle r = widget.getBounds();
					Rectangle textBounds = new Rectangle(r.x + iconSize + 5, r.y, r.width - iconSize - 5, r.height);
					Color col = widget.getForegroundColour();
					if (widget.isHover())
					{
						col = Constants.Colour.GOLD;
					}
					DIYToolkit.drawStringCentered(
						g,
						widget.getText(),
						textBounds,
						DIYToolkit.Align.LEFT,
						col,
						widget.getBackgroundColour());
				}
			}

			// cursed state
			if (widget.getItem() != null && widget.getItem().isCursed() &&
				widget.getItem().getCursedState() == Item.CursedState.DISCOVERED)
			{
				RoundRectangle2D curse = new RoundRectangle2D.Double(
					x + 1, y + 1, width - 2, height - 2, 5, 5);
				g.setPaint(new GradientPaint(
					x, y, Color.RED, x + width / 2, y + width / 2, Color.RED.darker()));
				g.draw(curse);
			}
		}
	}
}