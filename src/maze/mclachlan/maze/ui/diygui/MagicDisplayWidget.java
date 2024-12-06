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

import java.awt.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.diygui.DIYPane;

import static java.awt.Color.WHITE;
import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class MagicDisplayWidget extends DIYPane implements ActionListener
{
	private PlayerCharacter character;

	private final SpellDisplayWidget sdw;
	private final DIYLabel nameLabel;
	private final ManaDisplayWidget mana;

	/*-------------------------------------------------------------------------*/
	public MagicDisplayWidget(Rectangle bounds)
	{
		super(bounds);

		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();

		int inset = rp.getProperty(RendererProperties.Property.INSET);
//		int titleHeight = rp.getProperty(RendererProperties.Property.TITLE_PANE_HEIGHT);
		int titleHeight = 20;
		int buttonPaneHeight = rp.getProperty(RendererProperties.Property.BUTTON_PANE_HEIGHT);
		int headerOffset = titleHeight + DiyGuiUserInterface.SCREEN_EDGE_INSET;
		int contentTop = headerOffset + inset;
		int contentHeight = height - contentTop - buttonPaneHeight - inset - DiyGuiUserInterface.SCREEN_EDGE_INSET;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int frameBorderInset = rp.getProperty(RendererProperties.Property.PANEL_LIGHT_BORDER);

		int column1x = bounds.x + inset;
		int columnWidth = (width - 4 * inset) / 2;

		int column2x = column1x + columnWidth + inset;

		// screen title
		DIYLabel title = getSubTitle(StringUtil.getUiLabel("madw.title"));
		title.setBounds(
			200, DiyGuiUserInterface.SCREEN_EDGE_INSET,
			DiyGuiUserInterface.SCREEN_WIDTH - 400, titleHeight);

		nameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
		nameLabel.addActionListener(this);

		// personal info
		DIYPanel personalPanel = new DIYPanel();
		personalPanel.setStyle(DIYPanel.Style.PANEL_LIGHT);
		personalPanel.setLayoutManager(null);
		personalPanel.setBounds(
			column1x,
			contentTop,
			(width - 5 * inset) / 3 * 2,
			frameBorderInset * 2 + 80);

		nameLabel.setBounds(
			personalPanel.x + frameBorderInset + inset / 2,
			personalPanel.y + frameBorderInset,
			personalPanel.width / 2,
			20);

		mana = new ManaDisplayWidget("present");

		mana.setBounds(
			personalPanel.x +panelBorderInset,
			nameLabel.y +nameLabel.height +inset,
			mana.getPreferredSize().width,
			mana.getPreferredSize().height);

		personalPanel.add(nameLabel);
		personalPanel.add(mana);

		// spells
		DIYPanel spellsPanel = new DIYPanel();
		spellsPanel.setStyle(DIYPanel.Style.PANEL_MED);
		spellsPanel.setLayoutManager(null);
		spellsPanel.setInsets(new Insets(panelBorderInset, panelBorderInset + inset / 2, panelBorderInset, panelBorderInset + inset / 2));
		spellsPanel.setBounds(
			column1x,
			personalPanel.y + personalPanel.height + inset,
			columnWidth * 2 + inset,
			contentHeight - personalPanel.height - inset);

		sdw = new SpellDisplayWidget(null, new Rectangle(
			spellsPanel.x + panelBorderInset,
			spellsPanel.y + panelBorderInset,
			spellsPanel.width - panelBorderInset * 2,
			spellsPanel.height - panelBorderInset * 2));

		spellsPanel.add(sdw);

		this.add(title);
		this.add(personalPanel);
		this.add(spellsPanel);
	}

	/*-------------------------------------------------------------------------*/
	public void setCharacter(PlayerCharacter pc)
	{
		this.character = pc;
		sdw.setPlayerCharacter(pc);

		if (character != null)
		{
			refreshData();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setBounds(Rectangle r)
	{
		super.setBounds(r);
		sdw.setBounds(r);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getSubTitle(String titleText)
	{
		DIYLabel title = new DIYLabel(titleText);
		title.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize() + 3);
		title.setFont(f);
		return title;
	}

	/*-------------------------------------------------------------------------*/
	public void refreshData()
	{
		if (this.character == null)
		{
			return;
		}

		nameLabel.setForegroundColour(WHITE);
		nameLabel.setText(StringUtil.getUiLabel(
			"idw.character.details",
			this.character.getName(),
			String.valueOf(this.character.getLevel()),
			character.getGender().getName(),
			character.getRace().getName(),
			character.getCharacterClass().getName()));

		mana.refresh(
			character.getAmountRedMagic(),
			character.getAmountBlackMagic(),
			character.getAmountPurpleMagic(),
			character.getAmountGoldMagic(),
			character.getAmountWhiteMagic(),
			character.getAmountGreenMagic(),
			character.getAmountBlueMagic());

		sdw.setPlayerCharacter(character);
	}
}
