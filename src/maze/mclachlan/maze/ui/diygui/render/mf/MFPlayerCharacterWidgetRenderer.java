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
import java.util.*;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.CurMaxSub;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.PlayerCharacterWidget;

public class MFPlayerCharacterWidgetRenderer extends Renderer
{
	/*-------------------------------------------------------------------------*/
	@Override
	public void render(Graphics2D g, int x, int y, int width, int height, Widget w)
	{
		PlayerCharacterWidget widget = (PlayerCharacterWidget)w;
		Component comp = Maze.getInstance().getComponent();

/*		int frameBorder = 12;
		int inset = 5;

		int oneThirdWidth = width/3;
		int oneThirdHeight = height/3;
		int halfHeight = height/2;
		int halfWidth = width/2;
		int twoThirdsWidth = width*2/3;
		int twoThirdsHeight = height*2/3;

		int portraitWidth = twoThirdsWidth -frameBorder*2;
		int portraitHeight = twoThirdsHeight -frameBorder*2;
		int remainingWidth = oneThirdWidth;
		int remainingHeight = oneThirdHeight;

		// portrait bounds
		Rectangle portraitBounds = new Rectangle(
			x +frameBorder +inset,
			y +frameBorder +inset,
			portraitWidth,
			portraitHeight);
		widget.setPortraitBounds(portraitBounds);

		// name label bounds
		int barSep = inset;
		int startX = x + frameBorder + portraitWidth + inset;
		int barTop = y + frameBorder + inset;
		int barWidth = remainingWidth/3 -inset;
		int barHeight = portraitHeight;

		int nameLabelHeight = remainingHeight/3;

		Rectangle nameLabelBounds = new Rectangle(
			x + frameBorder,
			y + frameBorder + portraitHeight + inset,
			portraitWidth,
			nameLabelHeight);
		Rectangle classLabelBounds = new Rectangle(
			nameLabelBounds.x + nameLabelBounds.width,
			y + frameBorder + portraitHeight + inset,
			remainingWidth,
			nameLabelHeight);

		// hand icon bounds
		int handWidth = 40;
		int rightHandX = x +frameBorder +inset;
		int leftHandX = portraitBounds.x +portraitBounds.width -handWidth;
		int rightHandY = portraitBounds.y +portraitBounds.height - handWidth;
		int leftHandY = rightHandY;

		Rectangle rightHandBounds = new Rectangle(
			rightHandX,
			rightHandY,
			handWidth,
			handWidth);
		Rectangle leftHandBounds = new Rectangle(
			leftHandX,
			leftHandY,
			handWidth,
			handWidth);

		// action button bounds
		Rectangle actionBounds = new Rectangle(
			x + frameBorder,
			y + frameBorder + portraitHeight + nameLabelHeight + inset,
			width -frameBorder*2 -handWidth -inset*2,
			remainingHeight*//*//*3 -inset*//*);
		widget.getAction().setBounds(actionBounds);


		// stance button bounds
		Rectangle stanceBounds = new Rectangle(
			x + frameBorder,
			y + frameBorder + portraitHeight + nameLabelHeight*2 + inset*2,
			width -frameBorder*2 -handWidth -inset*2,
			remainingHeight/3 -inset);
		widget.getStance().setBounds(stanceBounds);


		// lvl up button
		DIYButton levelUp = widget.getLevelUp();
		Dimension ps = levelUp.getPreferredSize();
		levelUp.setBounds(
			x +portraitWidth/2 -ps.width/2 +frameBorder/2,
			y +portraitHeight -ps.height -inset*2,
			ps.width,
			ps.height);*/

		widget.getAction().setVisible(false);
		widget.getStance().setVisible(false);
		widget.getLevelUp().setVisible(false);

		synchronized(widget.getPcMutex())
		{
			// draw the bounds of the whole widget

			drawBorderWithTextures(g, x, y, width, height, comp,
				Database.getInstance().getImage("ui/mf/panel_light/border_top"),
				Database.getInstance().getImage("ui/mf/panel_light/border_bottom"),
				Database.getInstance().getImage("ui/mf/panel_light/border_left"),
				Database.getInstance().getImage("ui/mf/panel_light/border_right"),
				Database.getInstance().getImage("ui/mf/panel_light/corner_top_left"),
				Database.getInstance().getImage("ui/mf/panel_light/corner_top_right"),
				Database.getInstance().getImage("ui/mf/panel_light/corner_bottom_left"),
				Database.getInstance().getImage("ui/mf/panel_light/corner_bottom_right"),
				Database.getInstance().getImage("ui/mf/panel_light/center"),
				null);

			PlayerCharacter playerCharacter = widget.getPlayerCharacter();
			if (playerCharacter != null)
			{
				// stats bars
				drawBar(g, Constants.Colour.COMBAT_RED, Constants.Colour.FATIGUE_PINK, playerCharacter.getHitPoints(),
					widget.getHpBarBounds().x, widget.getHpBarBounds().y, widget.getHpBarBounds().width, widget.getHpBarBounds().height);
				drawBar(g, Constants.Colour.STEALTH_GREEN, null, playerCharacter.getActionPoints(),
					widget.getApBarBounds().x, widget.getApBarBounds().y, widget.getApBarBounds().width, widget.getApBarBounds().height);
				drawBar(g, Constants.Colour.MAGIC_BLUE, null, playerCharacter.getMagicPoints(),
					widget.getMpBarBounds().x, widget.getMpBarBounds().y, widget.getMpBarBounds().width, widget.getMpBarBounds().height);

				// other buttons (set up by the widget class)
				for (Widget kid : widget.getChildren())
				{
					kid.draw(g);
				}

				// name labels
				DIYToolkit.drawStringCentered(g, playerCharacter.getName(),
					widget.getNameLabelBounds(), DIYToolkit.Align.CENTER, Color.WHITE, null);
				DIYToolkit.drawStringCentered(g, "("+playerCharacter.getCharacterClass().getName()+")",
					widget.getClassLabelBounds(), DIYToolkit.Align.RIGHT, Color.WHITE.darker(), null);

				// Portrait
				Image img;
				img = Database.getInstance().getImage(
					playerCharacter.getPortrait());
				DIYToolkit.drawImageCentered(g, img, widget.getPortraitBounds(), DIYToolkit.Align.CENTER);

				// hand item slots
				Image slotImage = DIYToolkit.getInstance().getRendererProperties().getImageResource("icon/itemslot");
				DIYToolkit.drawImageCentered(g, slotImage, widget.getLeftHandBounds(), DIYToolkit.Align.CENTER);
				DIYToolkit.drawImageCentered(g, slotImage, widget.getRightHandBounds(), DIYToolkit.Align.CENTER);
//				widget.setLeftHandBounds(leftHandBounds);
//				widget.setRightHandBounds(rightHandBounds);

				// left hand item
				img = playerCharacter.getSecondaryWeapon() != null ?
					Database.getInstance().getImage(playerCharacter.getSecondaryWeapon().getImage()) :
					Database.getInstance().getImage(playerCharacter.getLeftHandIcon());
				DIYToolkit.drawImageCentered(g, img, widget.getLeftHandBounds(), DIYToolkit.Align.CENTER);

				// right hand item
				img = playerCharacter.getPrimaryWeapon() != null ?
					Database.getInstance().getImage(playerCharacter.getPrimaryWeapon().getImage()) :
					Database.getInstance().getImage(playerCharacter.getRightHandIcon());
				DIYToolkit.drawImageCentered(g, img, widget.getRightHandBounds(), DIYToolkit.Align.CENTER);

				//
				// Conditions
				//
				ArrayList<Condition> list = new ArrayList<>(playerCharacter.getConditions());
				if (!list.isEmpty())
				{
//					int bestMax = (twoThirdsHeight -frameBorder) / (handWidth+inset);
					int bestMax = 8;

					Rectangle[] conditionBounds = new Rectangle[list.size()];

					Rectangle ca = widget.getConditionsArea();
					int sX = ca.x;
					int sY = ca.y;

					for (int i = 0; i < conditionBounds.length; i++)
					{
						int yInc;
						if (list.size() <= bestMax)
						{
							yInc = i * (ca.width +3);
						}
						else
						{
							int squashHeight = (ca.height) / list.size();
							yInc = i * squashHeight;
						}
						conditionBounds[i] = new Rectangle(
							sX,
							sY + yInc,
							ca.width,
							ca.width);
					}

					int i = 0;
					widget.clearConditionBounds();
					for (Condition c : list)
					{
						Rectangle bounds = conditionBounds[i];
						widget.addConditionBounds(bounds, c);
						img = Database.getInstance().getImage(c.getDisplayIcon());
						g.drawImage(img, bounds.x, bounds.y,
							Maze.getInstance().getComponent());
						if (++i == conditionBounds.length)
						{
							// can't display any more conditions
							break;
						}
					}
				}

				if (playerCharacter.isLevelUpPending())
				{
					widget.getLevelUp().setVisible(true);
					widget.getLevelUp().setEnabled(true);
				}
				else
				{
					widget.getLevelUp().setVisible(false);
					widget.getLevelUp().setEnabled(false);
				}

				widget.getAction().setVisible(true);
				widget.getStance().setVisible(true);
			}
		}

		if (DIYToolkit.debug)
		{
			// draw the bounds of the whole widget
			g.setColor(Color.YELLOW);
			g.drawRect(x, y, width, height);

			// draw the portrait bounds
			g.setColor(Color.WHITE);
			g.drawRect(widget.getPortraitBounds().x, widget.getPortraitBounds().y,
				widget.getPortraitBounds().width, widget.getPortraitBounds().height);

			// draw the hand bounds
//			g.setColor(Color.WHITE);
//			g.drawRect(leftHandX, leftHandY,
//				handWidth, handWidth);
//			g.drawRect(rightHandX, rightHandY,
//				handWidth, handWidth);

//			g.setColor(Color.LIGHT_GRAY);
//			for (Rectangle cb : conditionBounds)
//			{
//				g.drawRect(cb.x, cb.y, cb.width, cb.height);
//			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void drawBorderWithTextures(Graphics2D g, int x, int y, int width, int height,
		Component comp, BufferedImage borderTop, BufferedImage borderBottom,
		BufferedImage borderLeft, BufferedImage borderRight,
		BufferedImage cornerTopLeft, BufferedImage cornerTopRight,
		BufferedImage cornerBottomLeft, BufferedImage cornerBottomRight,
		BufferedImage center, BufferedImage titleBar)
	{
		// corners
		g.drawImage(cornerTopLeft, x, y, comp);
		g.drawImage(cornerTopRight, x + width - cornerTopRight.getWidth(), y, comp);
		g.drawImage(cornerBottomLeft, x, y + height - cornerBottomLeft.getHeight(), comp);
		g.drawImage(cornerBottomRight, x + width - cornerBottomRight.getWidth(), y + height - cornerBottomRight.getHeight(), comp);

		// horiz borders
		DIYToolkit.drawImageTiled(g, borderTop,
			x + cornerTopLeft.getWidth(), y,
			width - cornerTopLeft.getWidth() - cornerTopRight.getWidth(), borderTop.getHeight());
		DIYToolkit.drawImageTiled(g, borderBottom,
			x + cornerBottomLeft.getWidth(), y + height - borderBottom.getHeight(),
			width - cornerBottomLeft.getWidth() - cornerBottomRight.getWidth(), borderBottom.getHeight());

		// vert borders
		DIYToolkit.drawImageTiled(g, borderLeft,
			x, y + cornerTopLeft.getHeight(),
			borderLeft.getWidth(), height - cornerTopLeft.getHeight() - cornerBottomLeft.getHeight());
		DIYToolkit.drawImageTiled(g, borderRight,
			x + width - borderRight.getWidth(), y + cornerTopRight.getHeight(),
			borderRight.getWidth(), height - cornerTopRight.getHeight() - cornerBottomRight.getHeight());

		// center
		DIYToolkit.drawImageTiled(g, center,
			x + borderLeft.getWidth(), y + borderTop.getHeight(),
			width - borderLeft.getWidth() - borderRight.getWidth(),
			height - borderTop.getHeight() - borderBottom.getHeight());

		// title bar
		if (titleBar != null)
		{
			DIYToolkit.drawImageTiled(g, titleBar,
				x + borderLeft.getWidth(), y + borderTop.getHeight(),
				width - borderLeft.getWidth() - borderRight.getWidth(),
				titleBar.getHeight());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void drawBar(Graphics2D g, Color colour, Color subColour, CurMax stat,
		int x, int y, int barWidth, int barHeight)
	{
		Color borderCol1 = Color.LIGHT_GRAY;
		Color borderCol2 = Color.LIGHT_GRAY.darker();

		Color col1 = colour;
		Color col2 = colour.darker();

		int solidHeight = (int)((barHeight)*stat.getRatio());

		if (solidHeight < 0)
		{
			solidHeight = 0;
		}

		RoundRectangle2D border = new RoundRectangle2D.Double(
			x, y, barWidth, barHeight, 5, 5);
		RoundRectangle2D filler = new RoundRectangle2D.Double(
			x, y+barHeight-solidHeight, barWidth, solidHeight, 5, 5);

		g.setPaint(new GradientPaint(x, y, col1, x+barWidth, y+barHeight, col2));
		g.fill(filler);

		g.setPaint(new GradientPaint(x, y, borderCol1, x+barWidth, y+barHeight, borderCol2));
		g.draw(border);

		if (stat instanceof CurMaxSub)
		{
			solidHeight = (int)((barHeight)*((CurMaxSub)stat).getSubRatio());
			RoundRectangle2D sub = new RoundRectangle2D.Double(
				x, y+barHeight-solidHeight, barWidth, solidHeight, 5, 5);

			col1 = subColour;
			col2 = subColour.darker();

			g.setPaint(new GradientPaint(x, y, col1, x+barWidth, y+barHeight, col2));
			g.fill(sub);
		}

		// draw text vertically
		g.setColor(Color.LIGHT_GRAY);
		FontMetrics fm = g.getFontMetrics();
		int textHeight = fm.getAscent();
		drawRotate(g, x +barWidth/2 +textHeight/2, y+barHeight-2, -90, String.valueOf(stat.getCurrent()));
	}

	public static void drawRotate(Graphics2D g2d, double x, double y, int angle, String text)
	{
		g2d.translate((float)x,(float)y);
		g2d.rotate(Math.toRadians(angle));
		g2d.drawString(text,0,0);
		g2d.rotate(-Math.toRadians(angle));
		g2d.translate(-(float)x,-(float)y);
	}
}