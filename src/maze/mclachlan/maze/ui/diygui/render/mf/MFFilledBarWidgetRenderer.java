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

package mclachlan.maze.ui.diygui.render.mf;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.data.Database;
import mclachlan.maze.ui.diygui.FilledBarWidget;
import mclachlan.maze.ui.diygui.render.maze.MazeRendererFactory;
import mclachlan.maze.util.MazeException;

public class MFFilledBarWidgetRenderer extends Renderer
{
	/*-------------------------------------------------------------------------*/
	@Override
	public void render(Graphics2D g, int x, int y, int width, int height,
		Widget widget)
	{
		FilledBarWidget fbw = (FilledBarWidget)widget;

		Color col = fbw.getBackgroundColour();
		if (col == null)
		{
			col = Color.GRAY;
		}

		drawBar(g, fbw, x, y, width, height);

		// text
		String text = null;

		Color foreground = fbw.getForegroundColour();
		if (foreground == null)
		{
			foreground = MazeRendererFactory.LABEL_FOREGROUND;
		}
		g.setColor(foreground);

		switch (fbw.getTextType())
		{
			case NONE:
				break;
			case CURRENT:
				text = String.valueOf(fbw.getCurrent());
				break;
			case CURRENT_AND_MAX:
				text = fbw.getCurrent()+" / "+fbw.getMax();
				break;
			case PERCENT:
				text = fbw.getCurrent()+"%";
				break;
			case CUSTOM:
				text = fbw.getCustomText();
				break;
			default:
				throw new MazeException(fbw.getTextType().toString());
		}

		if (text != null)
		{
			FontMetrics fm = g.getFontMetrics();

			int textInset = 10;
			int textHeight = fm.getHeight();

			if (fbw.getOrientation() == FilledBarWidget.Orientation.HORIZONTAL)
			{
				// center the text
				DIYToolkit.drawStringCentered(g, text,
					new Rectangle(x +textInset, y, width-textInset, height),
					DIYToolkit.Align.LEFT, foreground, null);
			}
			else
			{
				// draw text vertically
				DIYToolkit.drawRotate(g, x +width/2 +textHeight/2, y +height -textInset, -90, text);
			}
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
	private void drawBar(Graphics2D g, FilledBarWidget fbw, int x, int y, int width, int height)
	{
		Component comp = DIYToolkit.getInstance().getComponent();

		int border = 3;
		int inset = 1;
		int arc = 10;
		int innerArc = arc / 2;

		int maxFillDistance;

		if (fbw.getOrientation() == FilledBarWidget.Orientation.HORIZONTAL)
		{
			maxFillDistance = width -border*2 -inset*2;
		}
		else
		{
			maxFillDistance = height -border*2 -inset*2;
		}

		int fillDistance = getFillDistance(maxFillDistance, fbw.getCurrent(), fbw.getMax());
		int fillDistanceSub = (fbw.getSub() > 0) ?  fillDistanceSub = getFillDistance(maxFillDistance, fbw.getSub(), fbw.getMax()) : 0;

		// border
		RoundRectangle2D outerBorder = new RoundRectangle2D.Double(
			x, y, width, height, arc, arc);

		RoundRectangle2D innerBorder = new RoundRectangle2D.Double(
			x+1, y+1, width-2, height-2, arc, arc);

		RoundRectangle2D filledBorder = new RoundRectangle2D.Double(
			x +border +inset,
			y +border +inset,
			width -border*2 -inset*2 +1,
			height -border*2 -inset*2,
			arc, arc);

		Object origAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

		// Set anti-aliasing for smoother edges
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Set the stroke to be 3 pixels wide
		g.setStroke(new BasicStroke(2));

		g.setColor(Colours.MF_DARK_BROWN);
		g.draw(innerBorder);

		g.setColor(Colours.MF_BROWN);
		g.draw(outerBorder);

		g.setStroke(new BasicStroke());
		g.draw(filledBorder);

		// filler bar
		RoundRectangle2D filler;
		if (fbw.getOrientation() == FilledBarWidget.Orientation.HORIZONTAL)
		{
			filler = new RoundRectangle2D.Double(
				filledBorder.getX(),
				filledBorder.getY(),
				fillDistance,
				filledBorder.getHeight() +1, // this weird +1 to make sure?
				innerArc, innerArc);
		}
		else
		{
			filler = new RoundRectangle2D.Double(
				filledBorder.getX(),
				filledBorder.getY() +filledBorder.getHeight() -fillDistance,
				filledBorder.getWidth(),
				fillDistance,
				innerArc, innerArc);
		}

		// Define the gradient: original color -> white -> original color -> black
		Color barColour = fbw.getBarColour();
		setGradientPaint(g, fbw, x, y, width, height, barColour);

		g.fill(filler);

		// sub bar
		if (fillDistanceSub > 0)
		{
			RoundRectangle2D sub;
			if (fbw.getOrientation() == FilledBarWidget.Orientation.HORIZONTAL)
			{
				sub = new RoundRectangle2D.Double(
					x +border +inset,
					y +border +inset,
					fillDistanceSub,
					height -border*2 -inset*2,
					innerArc, innerArc);
			}
			else
			{
				sub = new RoundRectangle2D.Double(
					x +border +inset,
					y +height -border -inset -fillDistanceSub,
					width -border*2 -inset*2,
					fillDistanceSub,
					innerArc, innerArc);
			}

			setGradientPaint(g, fbw, x, y, width, height, fbw.getSubBarColour());
			g.fill(sub);
		}

		// paint the glowy end-of-filler effect

		if (fbw.getCurrent() < fbw.getMax())
		{
			if (fbw.getOrientation() == FilledBarWidget.Orientation.HORIZONTAL)
			{
				BufferedImage glowCenter = Database.getInstance().getImage("ui/mf/fbw/fbw_horiz_effect1_center");

				if (fillDistance >= glowCenter.getWidth())
				{
					BufferedImage glowTop = Database.getInstance().getImage("ui/mf/fbw/fbw_horiz_effect1_top");
					BufferedImage glowBottom = Database.getInstance().getImage("ui/mf/fbw/fbw_horiz_effect1_bottom");

					int glowX = x + border + inset + fillDistance - glowCenter.getWidth();

					g.drawImage(glowTop, glowX, y, comp);
					g.drawImage(glowBottom, glowX, y +height -border -inset +1, comp);
					DIYToolkit.drawImageTiled(g, glowCenter,
						glowX, y +border +inset, glowCenter.getWidth(), height -border*2 -inset*2 +1);
				}
			}
			else
			{
				BufferedImage glowCenter = Database.getInstance().getImage("ui/mf/fbw/fbw_vert_effect1_center");

				if (fillDistance >= glowCenter.getWidth())
				{
					BufferedImage glowLeft = Database.getInstance().getImage("ui/mf/fbw/fbw_vert_effect1_left");
					BufferedImage glowRight = Database.getInstance().getImage("ui/mf/fbw/fbw_vert_effect1_right");

					int glowY = y + height - border - inset - fillDistance;

					g.drawImage(glowLeft, x, glowY, comp);
					g.drawImage(glowRight, x +width -border -inset +1, glowY, comp);
					DIYToolkit.drawImageTiled(g, glowCenter,
						x +border +inset, glowY, width -border*2 -inset*2 +1, glowCenter.getHeight());
				}
			}
		}

		// reset graphic state
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, origAA);

		if (DIYToolkit.debug)
		{
			g.setColor(Color.PINK);
			g.drawRect(x, y, width, height);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void setGradientPaint(Graphics2D g, FilledBarWidget fbw, int x, int y,
		int width, int height, Color barColour)
	{
		Color[] colors = {barColour, barColour.brighter(), barColour, barColour.darker()};
		float[] fractions = {0.0f, 0.3f, 0.4f, 1.0f};  // The relative positions of each color stop

		// Define the gradient paint across the width of the rectangle
		LinearGradientPaint gradientPaint;
		if (fbw.getOrientation() == FilledBarWidget.Orientation.HORIZONTAL)
		{
			gradientPaint = new LinearGradientPaint(x, y, x, y + height, fractions, colors);
		}
		else
		{
			gradientPaint = new LinearGradientPaint(x, y, x + width, y, fractions, colors);
		}
		g.setPaint(gradientPaint);
	}
}
