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

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import mclachlan.diygui.DIYButton;
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

public class PlayerCharacterWidgetRenderer extends Renderer
{
	/*-------------------------------------------------------------------------*/
	@Override
	public void render(Graphics2D g, int x, int y, int width, int height, Widget w)
	{
		PlayerCharacterWidget widget = (PlayerCharacterWidget)w;

		int border = 15;
		int inset = 2;

		// warning: this hand-bounds logic is duplicated in the PCWidget
		int oneThirdWidth = width/3;
		int oneThirdHeight = height/3;
		int twoThirdsWidth = width*2/3;
		int twoThirdsHeight = height*2/3;

		int barWidth = (oneThirdWidth-inset*2-border)/4;
		int handWidth = (oneThirdWidth-inset*2-border)/2;
		int handHeight = handWidth;

		int handY = y+twoThirdsHeight+inset;
		int rightHandX = x +twoThirdsWidth +inset*2;
		int leftHandX = rightHandX +handWidth +inset;

		Rectangle portraitBounds = new Rectangle(x+border, y+border, twoThirdsWidth, twoThirdsHeight);
		Rectangle leftHandBounds = new Rectangle(leftHandX, handY, handWidth, handHeight);
		Rectangle rightHandBounds = new Rectangle(rightHandX, handY, handWidth, handHeight);

		int nameLabelHeight = DIYToolkit.getDimension("|").height;

		Rectangle nameLabelBounds = new Rectangle(
			x + border,
			y + twoThirdsHeight + inset, twoThirdsWidth, nameLabelHeight);
		Rectangle classLabelBounds = new Rectangle(
			x + border,
			y + twoThirdsHeight + inset + nameLabelHeight + inset, twoThirdsWidth, nameLabelHeight);

		Rectangle namePlateBounds = new Rectangle(
			nameLabelBounds.x, nameLabelBounds.y, nameLabelBounds.width,
			nameLabelHeight *2 + inset*2);

		// draw the main background image
		Image back = Database.getInstance().getImage("screen/pcwidget_back");
		g.drawImage(back, x, y, width, height, Maze.getInstance().getComponent());

		// action button bounds
		int actionInset = 10;
		Rectangle actionBounds = new Rectangle(
			x +border +actionInset,
			y +height -border -20,
			width -border*2 -actionInset*2,
			20);
		widget.getAction().setBounds(actionBounds);
		widget.getAction().setVisible(false);

		// lvl up button
		DIYButton levelUp = widget.getLevelUp();
		Dimension ps = levelUp.getPreferredSize();
		levelUp.setBounds(
			x +border +twoThirdsWidth/2 -ps.width/2,
			y +border +twoThirdsHeight -ps.height -inset -20,
			ps.width,
			ps.height);
		levelUp.setVisible(false);

		synchronized(widget.getPcMutex())
		{
			PlayerCharacter playerCharacter = widget.getPlayerCharacter();
			if (playerCharacter != null)
			{
				// stats bars
				int barSep = barWidth/3;
				int startX = x + twoThirdsWidth + inset*3;
				int barTop = y + border + inset;
				drawBar(g, Constants.Colour.COMBAT_RED, Constants.Colour.FATIGUE_PINK, playerCharacter.getHitPoints(),
					startX, barTop, barWidth, twoThirdsHeight-inset*2-border);
				startX += (barWidth+barSep);
				drawBar(g, Constants.Colour.STEALTH_GREEN, null, playerCharacter.getActionPoints(),
					startX, barTop, barWidth, twoThirdsHeight-inset*2-border);
				startX += (barWidth+barSep);
				drawBar(g, Constants.Colour.MAGIC_BLUE, null, playerCharacter.getMagicPoints(),
					startX, barTop, barWidth, twoThirdsHeight-inset*2-border);

				// other buttons (set up by the widget class)
				for (Widget kid : widget.getChildren())
				{
					kid.draw(g);
				}

				// name label plate
				Image nlp = Database.getInstance().getImage("screen/pcnamelabel");
				DIYToolkit.drawImageCentered(g, nlp, namePlateBounds, DIYToolkit.Align.CENTER);

				// name labels
				DIYToolkit.drawStringCentered(g, playerCharacter.getName(),
					nameLabelBounds, DIYToolkit.Align.CENTER, Color.DARK_GRAY, null);
				DIYToolkit.drawStringCentered(g, playerCharacter.getCharacterClass().getName(),
					classLabelBounds, DIYToolkit.Align.CENTER, Color.DARK_GRAY, null);

				// hand item slots
				Image slotImage = Database.getInstance().getImage("screen/itemslot");
				DIYToolkit.drawImageCentered(g, slotImage, leftHandBounds, DIYToolkit.Align.CENTER);
				DIYToolkit.drawImageCentered(g, slotImage, rightHandBounds, DIYToolkit.Align.CENTER);
				widget.setLeftHandBounds(leftHandBounds);
				widget.setRightHandBounds(rightHandBounds);

				Image img;
				// left hand item
				if (playerCharacter.getSecondaryWeapon()!= null)
				{
					img = Database.getInstance().getImage(
						playerCharacter.getSecondaryWeapon().getImage());

				}
				else
				{
					img = Database.getInstance().getImage(
						playerCharacter.getLeftHandIcon());
				}
				DIYToolkit.drawImageCentered(g, img, leftHandBounds, DIYToolkit.Align.CENTER);

				// right hand item
				if (playerCharacter.getPrimaryWeapon()!= null)
				{
					img = Database.getInstance().getImage(
						playerCharacter.getPrimaryWeapon().getImage());
				}
				else
				{
					img = Database.getInstance().getImage(
						playerCharacter.getRightHandIcon());
				}
				DIYToolkit.drawImageCentered(g, img, rightHandBounds, DIYToolkit.Align.CENTER);

				// Portrait
				img = Database.getInstance().getImage(
					playerCharacter.getPortrait());
				DIYToolkit.drawImageCentered(g, img, portraitBounds, DIYToolkit.Align.CENTER);

				//
				// Conditions
				//
				ArrayList<Condition> list = new ArrayList<Condition>(playerCharacter.getConditions());
				if (!list.isEmpty())
				{
					int bestMax = (twoThirdsHeight-border) / (handWidth+inset);

					Rectangle[] conditionBounds = new Rectangle[list.size()];

					int sX = x + border;
					int sY = y + border;

					for (int i = 0; i < conditionBounds.length; i++)
					{
						int yInc;
						if (list.size() <= bestMax)
						{
							yInc = i * (handWidth + inset);
						}
						else
						{
							int squashHeight = (twoThirdsHeight-border) / list.size();
							yInc = i * squashHeight;
						}
						conditionBounds[i] = new Rectangle(
							sX,
							sY + yInc,
							handWidth,
							handHeight);
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
					levelUp.setVisible(true);
					levelUp.setEnabled(true);
				}
				else
				{
					levelUp.setVisible(false);
					levelUp.setEnabled(false);
				}

				widget.getAction().setVisible(true);

				// demarcate formation
				if (Maze.getInstance().getParty() != null
					&& Maze.getInstance().getParty().getFormation() == widget.getIndex())
				{
					g.setColor(Constants.Colour.COMBAT_RED);
					g.drawLine(x, y, x+20, y);
					g.drawLine(x, y, x, y+20);
				}
			}
		}

		if (DIYToolkit.debug)
		{
			// draw the bounds of the whole widget
			g.setColor(Color.YELLOW);
			g.drawRect(x, y, width, height);

			// draw the portrait bounds
			g.setColor(Color.WHITE);
			g.drawRect(portraitBounds.x, portraitBounds.y, portraitBounds.width, portraitBounds.height);

			// draw the hand bounds
			g.setColor(Color.WHITE);
			g.drawRect(leftHandX, handY,
				handWidth, handHeight);
			g.drawRect(rightHandX, handY,
				handWidth, handHeight);

//			g.setColor(Color.LIGHT_GRAY);
//			for (Rectangle cb : conditionBounds)
//			{
//				g.drawRect(cb.x, cb.y, cb.width, cb.height);
//			}
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
	}
}