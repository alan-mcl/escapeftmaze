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
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Stats;

import static java.awt.Color.*;
import static mclachlan.maze.stat.Stats.Modifier.*;
import static mclachlan.maze.ui.diygui.Constants.Colour.*;

/**
 *
 */
public class StatsDisplayWidget extends ContainerWidget
	implements ActionListener, PortraitCallback, NameCallback
{
	private PlayerCharacter character;

	private final Map<Stats.Modifier, DIYLabel> labelMap = new HashMap<>();
	private final Map<Stats.Modifier, DIYLabel> nameLabelMap = new HashMap<>();
	private DIYLabel nameLabel;
	private final DIYButton portraitButton;
	private final DIYButton nameButton;
	private final DIYButton personalityButton;
	private DIYLabel kills;
	private final ActionListener modifiersDisplayActionListener;

	private FilledBarWidget hitPoints;
	private FilledBarWidget actionPoints;
	private FilledBarWidget magicPoints;
	private FilledBarWidget experience;
	//	private ManaDisplayWidget mana = new ManaDisplayWidget();
	private FilledBarWidget resistBludgeoning;
	private FilledBarWidget resistPiercing;
	private FilledBarWidget resistSlashing;
	private FilledBarWidget resistFire;
	private FilledBarWidget resistWater;
	private FilledBarWidget resistAir;
	private FilledBarWidget resistEarth;
	private FilledBarWidget resistMental;
	private FilledBarWidget resistEnergy;

	/*-------------------------------------------------------------------------*/
	public StatsDisplayWidget(Rectangle bounds)
	{
		super(bounds);

		this.modifiersDisplayActionListener = new ModifiersDisplayActionListener();

		portraitButton = new DIYButton("Change Portrait");
		portraitButton.addActionListener(this);
		nameButton = new DIYButton("Change Name");
		nameButton.addActionListener(this);
		personalityButton = new DIYButton("Change Personality");
		personalityButton.addActionListener(this);

		this.buildGUI(bounds);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGUI(Rectangle bounds)
	{
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
		int columnWidth = (width - 5 * inset) / 3;

		int column2x = column1x + columnWidth + inset;
		int column3x = column2x + columnWidth + inset;

		// screen title
		DIYLabel title = getSubTitle(StringUtil.getUiLabel("sdw.title"));
		title.setBounds(
			200, DiyGuiUserInterface.SCREEN_EDGE_INSET,
			DiyGuiUserInterface.SCREEN_WIDTH - 400, titleHeight);

		nameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
		nameLabel.addActionListener(this);

		experience = new FilledBarWidget(0, 0);

		experience.setBarColour(GOLD);
		experience.setForegroundColour(GRAY);
		experience.setTextType(FilledBarWidget.InnerTextType.CUSTOM);

		kills = new DIYLabel("", DIYToolkit.Align.LEFT);

		hitPoints = new FilledBarWidget(0, 0);
		actionPoints = new FilledBarWidget(0, 0);
		magicPoints = new FilledBarWidget(0, 0);

		hitPoints.setBarColour(COMBAT_RED);
		actionPoints.setBarColour(STEALTH_GREEN);
		magicPoints.setBarColour(MAGIC_BLUE);

		hitPoints.setForegroundColour(WHITE);
		actionPoints.setForegroundColour(WHITE);
		magicPoints.setForegroundColour(WHITE);

		hitPoints.setTextType(FilledBarWidget.InnerTextType.CURRENT_AND_MAX);
		actionPoints.setTextType(FilledBarWidget.InnerTextType.CURRENT_AND_MAX);
		magicPoints.setTextType(FilledBarWidget.InnerTextType.CURRENT_AND_MAX);

		resistBludgeoning = new FilledBarWidget(0, 0);
		resistPiercing = new FilledBarWidget(0, 0);
		resistSlashing = new FilledBarWidget(0, 0);
		resistFire = new FilledBarWidget(0, 0);
		resistWater = new FilledBarWidget(0, 0);
		resistAir = new FilledBarWidget(0, 0);
		resistEarth = new FilledBarWidget(0, 0);
		resistMental = new FilledBarWidget(0, 0);
		resistEnergy = new FilledBarWidget(0, 0);

		Color col = GOLD;
		resistBludgeoning.setBarColour(col);
		resistPiercing.setBarColour(col);
		resistSlashing.setBarColour(col);
		resistFire.setBarColour(col);
		resistWater.setBarColour(col);
		resistAir.setBarColour(col);
		resistEarth.setBarColour(col);
		resistMental.setBarColour(col);
		resistEnergy.setBarColour(col);

		resistBludgeoning.setTextType(FilledBarWidget.InnerTextType.PERCENT);
		resistPiercing.setTextType(FilledBarWidget.InnerTextType.PERCENT);
		resistSlashing.setTextType(FilledBarWidget.InnerTextType.PERCENT);
		resistFire.setTextType(FilledBarWidget.InnerTextType.PERCENT);
		resistWater.setTextType(FilledBarWidget.InnerTextType.PERCENT);
		resistAir.setTextType(FilledBarWidget.InnerTextType.PERCENT);
		resistEarth.setTextType(FilledBarWidget.InnerTextType.PERCENT);
		resistMental.setTextType(FilledBarWidget.InnerTextType.PERCENT);
		resistEnergy.setTextType(FilledBarWidget.InnerTextType.PERCENT);

		resistBludgeoning.setForegroundColour(GRAY);
		resistPiercing.setForegroundColour(GRAY);
		resistSlashing.setForegroundColour(GRAY);
		resistFire.setForegroundColour(GRAY);
		resistWater.setForegroundColour(GRAY);
		resistAir.setForegroundColour(GRAY);
		resistEarth.setForegroundColour(GRAY);
		resistMental.setForegroundColour(GRAY);
		resistEnergy.setForegroundColour(GRAY);


		// personal info & experience
		DIYPanel personalPanel = new DIYPanel();
		personalPanel.setStyle(DIYPanel.Style.PANEL_LIGHT);
		personalPanel.setLayoutManager(null);
		personalPanel.setBounds(
			column1x,
			contentTop,
			columnWidth * 2 + inset,
			panelBorderInset * 2 + 30);

		nameLabel.setBounds(
			personalPanel.x + frameBorderInset + inset / 2,
			personalPanel.y + frameBorderInset,
			personalPanel.width / 2,
			20);

		experience.setBounds(
			personalPanel.x + frameBorderInset + inset * 2,
			personalPanel.y + personalPanel.height / 2 - inset,
			personalPanel.width - frameBorderInset * 2 - inset * 4,
			personalPanel.height / 3);

		personalPanel.add(nameLabel);
		personalPanel.add(experience);

		// kills & deaths (todo)
		DIYPanel kdPanel = new DIYPanel();
		kdPanel.setStyle(DIYPanel.Style.PANEL_LIGHT);
		kdPanel.setBounds(
			column3x,
			contentTop,
			columnWidth,
			personalPanel.height);

		kdPanel.setLayoutManager(new DIYGridLayout(1, 2, 0, 0));
		kdPanel.setInsets(new Insets(frameBorderInset, frameBorderInset + inset / 2, frameBorderInset, frameBorderInset));

		kills.setForegroundColour(WHITE);

		kdPanel.add(kills);
		kdPanel.add(new DIYLabel("Deaths: TODO"));

		// resources and resistances
		int rows = 15;
		DIYPanel resourcesPanel = new DIYPanel();
		resourcesPanel.setStyle(DIYPanel.Style.PANEL_MED);
		resourcesPanel.setLayoutManager(new DIYGridLayout(1, rows, inset, inset));
		resourcesPanel.setInsets(new Insets(panelBorderInset, panelBorderInset +inset/2, panelBorderInset, panelBorderInset +inset/2));
		resourcesPanel.setBounds(
			column1x,
			personalPanel.y + personalPanel.height + inset,
			columnWidth*2 +inset,
			contentHeight - personalPanel.height - inset*4 -buttonPaneHeight);

		DIYLabel resourcesTitle = new DIYLabel(StringUtil.getUiLabel("sdw.resources"), DIYToolkit.Align.CENTER);
		resourcesTitle.setForegroundColour(CYAN);
		resourcesPanel.add(resourcesTitle);

		addResistance(resourcesPanel, HIT_POINTS, hitPoints);
		addResistance(resourcesPanel, ACTION_POINTS, actionPoints);
		addResistance(resourcesPanel, MAGIC_POINTS, magicPoints);

		resourcesPanel.add(new DIYLabel());

		DIYLabel resistancesTitle = new DIYLabel(StringUtil.getUiLabel("sdw.resistances"), DIYToolkit.Align.CENTER);
		resistancesTitle.setForegroundColour(CYAN);
		resourcesPanel.add(resistancesTitle);

		addResistance(resourcesPanel, RESIST_BLUDGEONING, resistBludgeoning);
		addResistance(resourcesPanel, RESIST_PIERCING, resistPiercing);
		addResistance(resourcesPanel, RESIST_SLASHING, resistSlashing);
		addResistance(resourcesPanel, RESIST_FIRE, resistFire);
		addResistance(resourcesPanel, RESIST_WATER, resistWater);
		addResistance(resourcesPanel, RESIST_AIR, resistAir);
		addResistance(resourcesPanel, RESIST_EARTH, resistEarth);
		addResistance(resourcesPanel, RESIST_MENTAL, resistMental);
		addResistance(resourcesPanel, RESIST_ENERGY, resistEnergy);

		DIYPanel modPanel = new DIYPanel();
		modPanel.setStyle(DIYPanel.Style.PANEL_MED);
		modPanel.setLayoutManager(new DIYGridLayout(1, rows, inset, inset));
		modPanel.setInsets(new Insets(panelBorderInset, panelBorderInset +inset/2, panelBorderInset, panelBorderInset +inset/2));
		modPanel.setBounds(
			column3x,
			resourcesPanel.y,
			columnWidth,
			resourcesPanel.height);

		DIYPane buttonPane = new DIYPane(new DIYGridLayout(3, 1, inset*2, inset));
		buttonPane.setInsets(new Insets(0, inset, 0, inset));
		buttonPane.setBounds(
			column1x,
			resourcesPanel.y +resourcesPanel.height +inset,
			columnWidth*2 +inset,
			buttonPaneHeight);

//		DIYLabel w = new DIYLabel("Mana Available", DIYToolkit.Align.CENTER);
//		w.setForegroundColour(CYAN);
//		bottomLeft.add(w);
//		bottomLeft.add(mana);
//		bottomLeft.add(new DIYLabel());

		buttonPane.add(nameButton);
		buttonPane.add(portraitButton);
		buttonPane.add(personalityButton);

		Stats.Modifier[] modifiers =
			{
				HIT_POINT_REGEN,
				ACTION_POINT_REGEN,
				MAGIC_POINT_REGEN,
				STAMINA_REGEN,
				INITIATIVE,
				ATTACK,
				DEFENCE,
				DAMAGE,
				TO_PENETRATE,
				VS_PENETRATE,
				VS_AMBUSH,
				VS_DODGE,
				VS_HIDE,
				TO_BRIBE,
				TO_RUN_AWAY,
			};

		for (Stats.Modifier s : modifiers)
		{
			this.addModifierToScreen(modPanel, s);
		}

		this.add(title);
		this.add(personalPanel);
		this.add(kdPanel);
		this.add(resourcesPanel);
		this.add(modPanel);
		this.add(buttonPane);

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	private void addResistance(ContainerWidget parent, Stats.Modifier modifier,
		FilledBarWidget bar)
	{
		DIYPane temp = new DIYPane(new DIYGridLayout(2, 1, 0, 0));
		DIYLabel label = getLabel(StringUtil.getModifierName(modifier));

		label.setActionMessage(modifier.toString());
		label.addActionListener(this.modifiersDisplayActionListener);
		label.setActionPayload(this.character);

		bar.setActionMessage(modifier.toString());
		bar.addActionListener(this.modifiersDisplayActionListener);
		bar.setActionPayload(this.character);

		this.nameLabelMap.put(modifier, label);

		temp.add(label);
		temp.add(bar);
		parent.add(temp);
	}

	/*-------------------------------------------------------------------------*/
	public void addModifierToScreen(ContainerWidget pane,
		Stats.Modifier modifier)
	{
		String modName = StringUtil.getModifierName(modifier);
		DIYPane temp = new DIYPane(new DIYGridLayout(2, 1, 0, 0));
		this.addDescLabel(temp, modifier, getLabel(modName));
		this.addStatLabel(temp, modifier, getModifierLabel());
		pane.add(temp);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Adds a static text desc label.
	 */
	private void addDescLabel(ContainerWidget parent, Stats.Modifier name,
		DIYLabel label)
	{
		parent.add(label);
		label.setActionMessage(name.toString());
		label.addActionListener(this.modifiersDisplayActionListener);
		label.setActionPayload(this.character);
		this.nameLabelMap.put(name, label);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Adds a volatile stats label.
	 */
	private void addStatLabel(ContainerWidget parent, Stats.Modifier name,
		DIYLabel label)
	{
		parent.add(label);
		label.setActionMessage(name.toString());
		label.addActionListener(this.modifiersDisplayActionListener);
		label.setActionPayload(this.character);
		this.labelMap.put(name, label);
	}

	/*-------------------------------------------------------------------------*/
	public void setCharacter(PlayerCharacter character)
	{
		this.character = character;

		if (character != null)
		{
			refreshData();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refreshData()
	{
		if (this.character == null)
		{
			return;
		}

		// refresh the action messages and event payloads
		for (Stats.Modifier modifier : this.labelMap.keySet())
		{
			DIYLabel label = this.labelMap.get(modifier);
			if (label != null)
			{
				label.setText(toString(modifier, label));
				label.setActionPayload(character);
			}
		}
		for (Stats.Modifier modifier : this.nameLabelMap.keySet())
		{
			DIYLabel label = this.nameLabelMap.get(modifier);
			if (label != null)
			{
				label.setActionPayload(character);
			}
		}
		resistBludgeoning.setActionPayload(character);
		resistPiercing.setActionPayload(character);
		resistSlashing.setActionPayload(character);
		resistFire.setActionPayload(character);
		resistWater.setActionPayload(character);
		resistAir.setActionPayload(character);
		resistEarth.setActionPayload(character);
		resistMental.setActionPayload(character);
		resistEnergy.setActionPayload(character);

		nameLabel.setForegroundColour(WHITE);
		nameLabel.setText(this.character.getName() + ", " +
			"level " + this.character.getLevel() + " " +
			character.getGender().getName() + " " +
			character.getRace().getName() + " " +
			character.getCharacterClass().getName());

		hitPoints.setFromCurMax(character.getHitPoints());
		actionPoints.setFromCurMax(character.getActionPoints());
		magicPoints.setFromCurMax(character.getMagicPoints());

		int lvlXp = character.getNextLevel() - character.getLastLevel();
		int lvlProgress = character.getExperience() - character.getLastLevel();
		experience.setPercent(lvlProgress * 100 / lvlXp);
		experience.setCustomText(StringUtil.getUiLabel("sdw.experience", character.getExperience(), character.getNextLevel()));

		resistBludgeoning.set(character.getModifier(RESIST_BLUDGEONING), 100);
		resistPiercing.set(character.getModifier(RESIST_PIERCING), 100);
		resistSlashing.set(character.getModifier(RESIST_SLASHING), 100);
		resistFire.set(character.getModifier(RESIST_FIRE), 100);
		resistWater.set(character.getModifier(RESIST_WATER), 100);
		resistAir.set(character.getModifier(RESIST_AIR), 100);
		resistEarth.set(character.getModifier(RESIST_EARTH), 100);
		resistMental.set(character.getModifier(RESIST_MENTAL), 100);
		resistEnergy.set(character.getModifier(RESIST_ENERGY), 100);

		kills.setText(StringUtil.getUiLabel("sdw.kills", character.getKills()));
	}

	/*-------------------------------------------------------------------------*/
	private String toString(Stats.Modifier mod, DIYLabel label)
	{
		int modifier = character.getModifier(mod);
		int intrinsicModifier = character.getIntrinsicModifier(mod);

		Color colour = null;
		if (modifier < intrinsicModifier)
		{
			colour = COMBAT_RED;
		}
		else if (modifier == intrinsicModifier)
		{
			colour = LIGHT_GRAY;
		}
		else if (modifier > intrinsicModifier)
		{
			colour = STEALTH_GREEN;
		}

		label.setForegroundColour(colour);

		return
			((modifier >= 0) ? "+" + modifier : "" + modifier);
//				+ "/" +
//			((baseModifier>=0) ? "+"+baseModifier : ""+baseModifier);
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getModifierLabel()
	{
		return new DIYLabel();
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getLabel(String text)
	{
		return new DIYLabel(text, DIYToolkit.Align.LEFT);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == nameLabel && event.getEvent() instanceof KeyEvent)
		{
			// player has pressed enter in the name text field: rename the pc
			Maze.getInstance().renamePlayerCharacter(character, nameLabel.getText());
			return true;
		}
		else if (event.getSource() == portraitButton)
		{
			Maze.getInstance().getUi().showDialog(
				new PortraitSelectionDialog(
					this,
					character.getPortrait()));
			return true;
		}
		else if (event.getSource() == nameButton)
		{
			Maze.getInstance().getUi().showDialog(
				new NameEditDialog(
					this,
					StringUtil.getUiLabel("sdw.enter.new.name")));
			return true;
		}
		else if (event.getSource() == personalityButton)
		{
			Maze.getInstance().getUi().showDialog(
				new PersonalitySelectionDialog(
					character.getPersonality().getName(),
					character));
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public void setPortrait(String image)
	{
		character.setPortrait(image);
		DiyGuiUserInterface.instance.partyDisplay.setParty(Maze.getInstance().getParty());
		DiyGuiUserInterface.instance.partyDisplay.setSelectedCharacter(character);
		DiyGuiUserInterface.instance.refreshCharacterData();
		refreshData();
	}

	/*-------------------------------------------------------------------------*/

	public void setName(String name)
	{
		character.setName(name);
		DiyGuiUserInterface.instance.partyDisplay.setParty(Maze.getInstance().getParty());
		DiyGuiUserInterface.instance.partyDisplay.setSelectedCharacter(character);
		DiyGuiUserInterface.instance.refreshCharacterData();
		refreshData();
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

}
