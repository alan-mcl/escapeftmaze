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
import java.awt.Image;
import java.awt.image.BufferedImage;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class MFPanelRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height,
		Widget widget)
	{
		DIYPanel panel = (DIYPanel)widget;
		Image image = panel.getBackgroundImage();
		Component comp = Maze.getInstance().getComponent();

		switch (panel.getStyle())
		{
			case DIALOG -> drawWithTextures(g, x, y, width, height, comp,
				Database.getInstance().getImage("ui/mf/dialog/border_top"),
				Database.getInstance().getImage("ui/mf/dialog/border_bottom"),
				Database.getInstance().getImage("ui/mf/dialog/border_left"),
				Database.getInstance().getImage("ui/mf/dialog/border_right"),
				null, null,
				Database.getInstance().getImage("ui/mf/dialog/corner_top_left"),
				Database.getInstance().getImage("ui/mf/dialog/corner_top_right"),
				Database.getInstance().getImage("ui/mf/dialog/corner_bottom_left"),
				Database.getInstance().getImage("ui/mf/dialog/corner_bottom_right"),
				null, null, null, null,
				Database.getInstance().getImage("ui/mf/dialog/center"),
				Database.getInstance().getImage("ui/mf/dialog/title_bar"));
			case PANEL_HEAVY -> drawWithTextures(g, x, y, width, height, comp,
				Database.getInstance().getImage("ui/mf/panel_heavy/border_top"),
				Database.getInstance().getImage("ui/mf/panel_heavy/border_bottom"),
				Database.getInstance().getImage("ui/mf/panel_heavy/border_left"),
				Database.getInstance().getImage("ui/mf/panel_heavy/border_right"),
				Database.getInstance().getImage("ui/mf/panel_heavy/border_left_thin"),
				Database.getInstance().getImage("ui/mf/panel_heavy/border_right_thin"),
				Database.getInstance().getImage("ui/mf/panel_heavy/corner_top_left"),
				Database.getInstance().getImage("ui/mf/panel_heavy/corner_top_right"),
				Database.getInstance().getImage("ui/mf/panel_heavy/corner_bottom_left"),
				Database.getInstance().getImage("ui/mf/panel_heavy/corner_bottom_right"),
				Database.getInstance().getImage("ui/mf/panel_heavy/corner_top_left_thin"),
				Database.getInstance().getImage("ui/mf/panel_heavy/corner_top_right_thin"),
				Database.getInstance().getImage("ui/mf/panel_heavy/corner_bottom_left_thin"),
				Database.getInstance().getImage("ui/mf/panel_heavy/corner_bottom_right_thin"),
				Database.getInstance().getImage("ui/mf/panel_heavy/center"),
				null);
			case PANEL_MED -> drawWithTextures(g, x, y, width, height, comp,
				Database.getInstance().getImage("ui/mf/panel_med/border_top"),
				Database.getInstance().getImage("ui/mf/panel_med/border_bottom"),
				Database.getInstance().getImage("ui/mf/panel_med/border_left"),
				Database.getInstance().getImage("ui/mf/panel_med/border_right"),
				null, null,
				Database.getInstance().getImage("ui/mf/panel_med/corner_top_left"),
				Database.getInstance().getImage("ui/mf/panel_med/corner_top_right"),
				Database.getInstance().getImage("ui/mf/panel_med/corner_bottom_left"),
				Database.getInstance().getImage("ui/mf/panel_med/corner_bottom_right"),
				null, null, null, null,
				Database.getInstance().getImage("ui/mf/panel_med/center"),

				null);
			case PANEL_LIGHT -> drawWithTextures(g, x, y, width, height, comp,
				Database.getInstance().getImage("ui/mf/panel_light/border_top"),
				Database.getInstance().getImage("ui/mf/panel_light/border_bottom"),
				Database.getInstance().getImage("ui/mf/panel_light/border_left"),
				Database.getInstance().getImage("ui/mf/panel_light/border_right"),
				null, null,
				Database.getInstance().getImage("ui/mf/panel_light/corner_top_left"),
				Database.getInstance().getImage("ui/mf/panel_light/corner_top_right"),
				Database.getInstance().getImage("ui/mf/panel_light/corner_bottom_left"),
				Database.getInstance().getImage("ui/mf/panel_light/corner_bottom_right"),
				null, null, null, null,
				Database.getInstance().getImage("ui/mf/panel_light/center"),
				null);

			case IMAGE_BACK ->
				g.drawImage(image, x, y, width, height, DIYToolkit.getInstance().getComponent());
			case TRANSPARENT ->
			{
			}
			default -> throw new MazeException("invalid: " + panel.getStyle());
		}
	}

	/*-------------------------------------------------------------------------*/

	private void drawWithTextures(Graphics2D g, int x, int y, int width,
		int height,
		Component comp,
		BufferedImage borderTop,
		BufferedImage borderBottom,
		BufferedImage borderLeft,
		BufferedImage borderRight,
		BufferedImage borderLeftThin,
		BufferedImage borderRightThin,
		BufferedImage cornerTopLeft,
		BufferedImage cornerTopRight,
		BufferedImage cornerBottomLeft,
		BufferedImage cornerBottomRight,
		BufferedImage cornerTopLeftThin,
		BufferedImage cornerTopRightThin,
		BufferedImage cornerBottomLeftThin,
		BufferedImage cornerBottomRightThin,
		BufferedImage center,
		BufferedImage titleBar)
	{
		BufferedImage imgTopLeft;
		BufferedImage imgTopRight;
		BufferedImage imgBottomLeft;
		BufferedImage imgBottomRight;
		BufferedImage imgLeft;
		BufferedImage imgRight;

		if (height <= 100 && cornerTopLeftThin != null)
		{
			imgTopLeft = cornerTopLeftThin;
			imgTopRight = cornerTopRightThin;
			imgBottomLeft = cornerBottomLeftThin;
			imgBottomRight = cornerBottomRightThin;
			imgLeft = borderLeftThin;
			imgRight = borderRightThin;
		}
		else
		{
			imgTopLeft = cornerTopLeft;
			imgTopRight = cornerTopRight;
			imgBottomLeft = cornerBottomLeft;
			imgBottomRight = cornerBottomRight;
			imgLeft = borderLeft;
			imgRight = borderRight;
		}

		// corners
		g.drawImage(imgTopLeft, x, y, comp);
		g.drawImage(imgTopRight, x + width - imgTopRight.getWidth(), y, comp);
		g.drawImage(imgBottomLeft, x, y + height - imgBottomLeft.getHeight(), comp);
		g.drawImage(imgBottomRight, x + width - imgBottomRight.getWidth(), y + height - imgBottomRight.getHeight(), comp);

		// horiz borders
		DIYToolkit.drawImageTiled(g, borderTop,
			x + imgTopLeft.getWidth(), y,
			width - imgTopLeft.getWidth() - imgTopRight.getWidth(), borderTop.getHeight());
		DIYToolkit.drawImageTiled(g, borderBottom,
			x + imgBottomLeft.getWidth(), y + height - borderBottom.getHeight(),
			width - imgBottomLeft.getWidth() - imgBottomRight.getWidth(), borderBottom.getHeight());

		// vert borders
		DIYToolkit.drawImageTiled(g, imgLeft,
			x, y + imgTopLeft.getHeight(),
			imgLeft.getWidth(), height - imgTopLeft.getHeight() - imgBottomLeft.getHeight());
		DIYToolkit.drawImageTiled(g, imgRight,
			x + width - imgRight.getWidth(), y + imgTopRight.getHeight(),
			imgRight.getWidth(), height - imgTopRight.getHeight() - imgBottomRight.getHeight());

		// center
		DIYToolkit.drawImageTiled(g, center,
			x + imgLeft.getWidth(), y + borderTop.getHeight(),
			width - imgLeft.getWidth() - imgRight.getWidth(),
			height - borderTop.getHeight() - borderBottom.getHeight());

		// title bar
		if (titleBar != null)
		{
			DIYToolkit.drawImageTiled(g, titleBar,
				x + imgLeft.getWidth(), y + borderTop.getHeight(),
				width - imgLeft.getWidth() - imgRight.getWidth(),
				titleBar.getHeight());
		}
	}
}
