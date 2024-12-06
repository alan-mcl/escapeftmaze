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

package mclachlan.maze.ui.diygui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.ManaRequirement;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ManaDisplayWidget extends DIYPane implements ActionListener
{
	public static final int ICON_SIZE = 32;
	public static final int HGAP = 8;
	private final DIYLabel red, black, purple, gold, white, green, blue;
	private final ArrayList<DIYLabel> iconLabels;

	/*-------------------------------------------------------------------------*/
	public ManaDisplayWidget(String tooltipSuffix)
	{
		iconLabels = new ArrayList<>(7);

		red = createLabel("icon/mana_icon_red", Color.BLACK, "madw.red.tooltip."+tooltipSuffix);
		black = createLabel("icon/mana_icon_black", Color.WHITE, "madw.black.tooltip."+tooltipSuffix);
		purple = createLabel("icon/mana_icon_purple", Color.YELLOW, "madw.purple.tooltip."+tooltipSuffix);
		gold = createLabel("icon/mana_icon_gold", Color.BLACK, "madw.gold.tooltip."+tooltipSuffix);
		white = createLabel("icon/mana_icon_white", Color.BLACK, "madw.white.tooltip."+tooltipSuffix);
		green = createLabel("icon/mana_icon_green", Color.BLACK, "madw.green.tooltip."+tooltipSuffix);
		blue = createLabel("icon/mana_icon_blue", Color.WHITE, "madw.blue.tooltip."+tooltipSuffix);
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel createLabel(String imageName, Color fore, String tooltip)
	{
		DIYLabel iconLabel = new DIYLabel();
		iconLabel.setAlignment(DIYToolkit.Align.CENTER);
		iconLabel.setIconAlign(DIYToolkit.Align.CENTER);
		iconLabel.setIcon(DIYToolkit.getInstance().getRendererProperties().getImageResource(imageName));
		iconLabel.setHoverIcon(DIYToolkit.getInstance().getRendererProperties().getImageResource(imageName+"_hover"));
		iconLabel.setTooltip(StringUtil.getUiLabel(tooltip));

		iconLabels.add(iconLabel);

		DIYLabel textLabel = new DIYLabel()
		{
			@Override
			public void setHover(boolean hover)
			{
				iconLabel.setHover(hover);
			}
		};
		textLabel.setForegroundColour(fore);
		textLabel.addActionListener(this);
		textLabel.setTooltip(StringUtil.getUiLabel(tooltip));

		this.add(iconLabel);
		this.add(textLabel);

		return textLabel;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(ICON_SIZE*7 + HGAP*6, ICON_SIZE);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);

		if (red != null)
		{
			DIYLabel[] icons = {red, black, purple, gold, white, green, blue};
			for (int i = 0; i < icons.length; i++)
			{
				Rectangle r = new Rectangle(
					x + (i * (ICON_SIZE + HGAP)),
					y,
					ICON_SIZE,
					ICON_SIZE);
				icons[i].setBounds(r);

				iconLabels.get(i).setBounds(r);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(
		int red,
		int black,
		int purple,
		int gold,
		int white,
		int green,
		int blue)
	{
		this.red.setText("" + red);
		this.black.setText("" + black);
		this.purple.setText("" + purple);
		this.gold.setText("" + gold);
		this.white.setText("" + white);
		this.green.setText("" + green);
		this.blue.setText("" + blue);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<ManaRequirement> requirementsToCast)
	{
		this.refresh(0, 0, 0, 0, 0, 0, 0);

		if (requirementsToCast != null)
		{
			for (ManaRequirement m : requirementsToCast)
			{
				switch (m.getColour())
				{
					case MagicSys.ManaType.RED -> red.setText("" + m.getAmount());
					case MagicSys.ManaType.BLACK -> black.setText("" + m.getAmount());
					case MagicSys.ManaType.PURPLE -> purple.setText("" + m.getAmount());
					case MagicSys.ManaType.GOLD -> gold.setText("" + m.getAmount());
					case MagicSys.ManaType.WHITE -> white.setText("" + m.getAmount());
					case MagicSys.ManaType.GREEN -> green.setText("" + m.getAmount());
					case MagicSys.ManaType.BLUE -> blue.setText("" + m.getAmount());
					default ->
						throw new MazeException("invalid [" + m.getColour() + "]");
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void popupSpellBookDesc(MagicSys.SpellBook spellBook)
	{
		TextDialogWidget dialog = new TextDialogWidget(
			spellBook.getName(),
			Database.getInstance().getPlayerSpellBook(spellBook.getName()).getDescription(),
			false);

		Maze.getInstance().getUi().showDialog(dialog);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == red)
		{
			popupSpellBookDesc(MagicSys.SpellBook.RED_MAGIC);
		}
		else if (event.getSource() == black)
		{
			popupSpellBookDesc(MagicSys.SpellBook.BLACK_MAGIC);
		}
		else if (event.getSource() == purple)
		{
			popupSpellBookDesc(MagicSys.SpellBook.PURPLE_MAGIC);
		}
		else if (event.getSource() == gold)
		{
			popupSpellBookDesc(MagicSys.SpellBook.GOLD_MAGIC);
		}
		else if (event.getSource() == white)
		{
			popupSpellBookDesc(MagicSys.SpellBook.WHITE_MAGIC);
		}
		else if (event.getSource() == green)
		{
			popupSpellBookDesc(MagicSys.SpellBook.GREEN_MAGIC);
		}
		else if (event.getSource() == blue)
		{
			popupSpellBookDesc(MagicSys.SpellBook.BLUE_MAGIC);
		}

		return true;
	}
}
