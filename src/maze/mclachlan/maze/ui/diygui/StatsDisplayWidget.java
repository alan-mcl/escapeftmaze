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

	Map<Stats.Modifier, DIYLabel> labelMap = new HashMap<Stats.Modifier, DIYLabel>();
	Map<Stats.Modifier, DIYLabel> nameLabelMap = new HashMap<Stats.Modifier, DIYLabel>();
	private DIYLabel nameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
	private DIYButton portraitButton = new DIYButton("Change Portrait");
	private DIYButton nameButton = new DIYButton("Change Name");
	private DIYButton personalityButton = new DIYButton("Change Personality");
	private DIYLabel kills = new DIYLabel("", DIYToolkit.Align.LEFT);
	private ActionListener modifiersDisplayActionListener;

	private FilledBarWidget hitPoints = new FilledBarWidget(0,0);
	private FilledBarWidget actionPoints = new FilledBarWidget(0,0);
	private FilledBarWidget magicPoints = new FilledBarWidget(0,0);
	private FilledBarWidget experience = new FilledBarWidget(0,0);
	private ManaDisplayWidget mana = new ManaDisplayWidget();
	private FilledBarWidget resistBludgeoning = new FilledBarWidget(0,0);
	private FilledBarWidget resistPiercing = new FilledBarWidget(0,0);
	private FilledBarWidget resistSlashing = new FilledBarWidget(0,0);
	private FilledBarWidget resistFire = new FilledBarWidget(0,0);
	private FilledBarWidget resistWater = new FilledBarWidget(0,0);
	private FilledBarWidget resistAir = new FilledBarWidget(0,0);
	private FilledBarWidget resistEarth = new FilledBarWidget(0,0);
	private FilledBarWidget resistMental = new FilledBarWidget(0,0);
	private FilledBarWidget resistEnergy = new FilledBarWidget(0,0);

	/*-------------------------------------------------------------------------*/
	public StatsDisplayWidget(Rectangle bounds)
	{
		super(bounds);

		this.modifiersDisplayActionListener = new ModifiersDisplayActionListener();
		this.nameLabel.addActionListener(this);
		this.portraitButton.addActionListener(this);
		this.nameButton.addActionListener(this);
		this.personalityButton.addActionListener(this);

		this.buildGUI();
	}
	
	/*-------------------------------------------------------------------------*/
	private void buildGUI()
	{
		DIYLabel topLabel = new DIYLabel("Statistics", DIYToolkit.Align.CENTER);
		topLabel.setBounds(162, 0, DiyGuiUserInterface.SCREEN_WIDTH - 162, 30);
		topLabel.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize()+5);
		topLabel.setFont(f);
		this.add(topLabel);

		int inset = 2;
		int rows = 25;
		int rowHeight = height / rows;

		nameLabel.setForegroundColour(WHITE);
		nameLabel.setBounds(x, y+rowHeight-inset, width, rowHeight);
		this.add(nameLabel);

		DIYPane topPane = new DIYPane(new DIYGridLayout(2,1,inset,inset));
		topPane.setBounds(x, y+rowHeight*2, width-10, rowHeight*3);
		topPane.setInsets(getInsets(inset));
		
		DIYPane topLeft = new DIYPane(new DIYGridLayout(2,3,inset,inset));
		DIYPane topRight = new DIYPane(new DIYGridLayout(2,3,inset,inset));
		topLeft.setInsets(getInsets(inset));
		topRight.setInsets(getInsets(inset));
		topPane.add(topLeft);
		topPane.add(topRight);

		hitPoints.setBarColour(COMBAT_RED);
		actionPoints.setBarColour(STEALTH_GREEN);
		magicPoints.setBarColour(MAGIC_BLUE);
		experience.setBarColour(LIGHT_BLUE);

		hitPoints.setForegroundColour(LIGHT_GREY);
		actionPoints.setForegroundColour(LIGHT_GREY);
		magicPoints.setForegroundColour(LIGHT_GREY);
		experience.setForegroundColour(LIGHT_GREY);

		hitPoints.setText(FilledBarWidget.InnerText.CUR_MAX);
		actionPoints.setText(FilledBarWidget.InnerText.CUR_MAX);
		magicPoints.setText(FilledBarWidget.InnerText.CUR_MAX);
		experience.setText(FilledBarWidget.InnerText.CUSTOM);

		topLeft.add(getLabel("HIT POINTS:", COMBAT_RED));
		topLeft.add(hitPoints);
		topLeft.add(getLabel("ACTION POINTS:", STEALTH_GREEN));
		topLeft.add(actionPoints);
		topLeft.add(getLabel("MAGIC POINTS:", MAGIC_BLUE));
		topLeft.add(magicPoints);

		topRight.add(getLabel("Experience:"));
		topRight.add(experience);
		topRight.add(getLabel("Kills:"));
		topRight.add(kills);

		DIYPane bottomPane = new DIYPane(new DIYGridLayout(2,1,inset,inset));
		bottomPane.setBounds(x, y+rowHeight*5, width-10, height-rowHeight*5);
		bottomPane.setInsets(getInsets(inset));

		DIYPane bottomLeft = new DIYPane(new DIYGridLayout(1, 20, inset, inset));
		DIYPane bottomRight = new DIYPane(new DIYGridLayout(1, 20, inset, inset));
		bottomLeft.setInsets(getInsets(inset));
		bottomRight.setInsets(getInsets(inset));

		bottomPane.add(bottomLeft);
		bottomPane.add(bottomRight);

		Color col = LIGHT_BLUE;
		resistBludgeoning.setBarColour(col);
		resistPiercing.setBarColour(col);
		resistSlashing.setBarColour(col);
		resistFire.setBarColour(col);
		resistWater.setBarColour(col);
		resistAir.setBarColour(col);
		resistEarth.setBarColour(col);
		resistMental.setBarColour(col);
		resistEnergy.setBarColour(col);

		resistBludgeoning.setText(FilledBarWidget.InnerText.PERCENT);
		resistPiercing.setText(FilledBarWidget.InnerText.PERCENT);
		resistSlashing.setText(FilledBarWidget.InnerText.PERCENT);
		resistFire.setText(FilledBarWidget.InnerText.PERCENT);
		resistWater.setText(FilledBarWidget.InnerText.PERCENT);
		resistAir.setText(FilledBarWidget.InnerText.PERCENT);
		resistEarth.setText(FilledBarWidget.InnerText.PERCENT);
		resistMental.setText(FilledBarWidget.InnerText.PERCENT);
		resistEnergy.setText(FilledBarWidget.InnerText.PERCENT);

		resistBludgeoning.setForegroundColour(LIGHT_GREY);
		resistPiercing.setForegroundColour(LIGHT_GREY);
		resistSlashing.setForegroundColour(LIGHT_GREY);
		resistFire.setForegroundColour(LIGHT_GREY);
		resistWater.setForegroundColour(LIGHT_GREY);
		resistAir.setForegroundColour(LIGHT_GREY);
		resistEarth.setForegroundColour(LIGHT_GREY);
		resistMental.setForegroundColour(LIGHT_GREY);
		resistEnergy.setForegroundColour(LIGHT_GREY);

		DIYLabel w = new DIYLabel("Mana Available", DIYToolkit.Align.CENTER);
		w.setForegroundColour(CYAN);
		bottomLeft.add(w);
		bottomLeft.add(mana);
		DIYLabel w1 = new DIYLabel("Resistances", DIYToolkit.Align.CENTER);
		w1.setForegroundColour(CYAN);
		bottomLeft.add(w1);
		addResistance(bottomLeft, RESIST_BLUDGEONING, resistBludgeoning);
		addResistance(bottomLeft, RESIST_PIERCING, resistPiercing);
		addResistance(bottomLeft, RESIST_SLASHING, resistSlashing);
		addResistance(bottomLeft, RESIST_FIRE, resistFire);
		addResistance(bottomLeft, RESIST_WATER, resistWater);
		addResistance(bottomLeft, RESIST_AIR, resistAir);
		addResistance(bottomLeft, RESIST_EARTH, resistEarth);
		addResistance(bottomLeft, RESIST_MENTAL, resistMental);
		addResistance(bottomLeft, RESIST_ENERGY, resistEnergy);
		bottomLeft.add(new DIYLabel());

		DIYPane buttons1 = new DIYPane(new DIYGridLayout(2,1,inset,inset));
		buttons1.add(nameButton);
		buttons1.add(new DIYLabel());
		bottomLeft.add(buttons1);
		DIYPane buttons2 = new DIYPane(new DIYGridLayout(2,1,inset,inset));
		buttons2.add(portraitButton);
		buttons2.add(new DIYLabel());
		bottomLeft.add(buttons2);
		DIYPane buttons3 = new DIYPane(new DIYGridLayout(2,1,inset,inset));
		buttons3.add(personalityButton);
		buttons3.add(new DIYLabel());
		bottomLeft.add(buttons3);

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
			this.addModifierToScreen(bottomRight, s);
		}

		this.add(topPane);
		this.add(bottomPane);

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	private void addResistance(DIYPane parent, Stats.Modifier modifier, FilledBarWidget bar)
	{
		DIYPane temp = new DIYPane(new DIYGridLayout(2,1,0,0));
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
	private Insets getInsets(int inset)
	{
		return new Insets(inset, inset, inset, inset);
	}

	/*-------------------------------------------------------------------------*/
	public void addModifierToScreen(ContainerWidget pane, Stats.Modifier modifier)
	{
		String modName = StringUtil.getModifierName(modifier);
		DIYPane temp = new DIYPane(new DIYGridLayout(2,1,0,0));
		this.addDescLabel(temp, modifier, getLabel(modName));
		this.addStatLabel(temp, modifier, getModifierLabel());
		pane.add(temp);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Adds a static text desc label.
	 */ 
	private void addDescLabel(ContainerWidget parent, Stats.Modifier name, DIYLabel label)
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
	private void addStatLabel(ContainerWidget parent, Stats.Modifier name, DIYLabel label)
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
		nameLabel.setText(this.character.getName()+", "+
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
		experience.setCustomText(character.getExperience()+" / "+character.getNextLevel());

		mana.refresh(
			character.getAmountRedMagic(),
			character.getAmountBlackMagic(),
			character.getAmountPurpleMagic(),
			character.getAmountGoldMagic(),
			character.getAmountWhiteMagic(),
			character.getAmountGreenMagic(),
			character.getAmountBlueMagic());

		resistBludgeoning.set(character.getModifier(RESIST_BLUDGEONING), 100);
		resistPiercing.set(character.getModifier(RESIST_PIERCING), 100);
		resistSlashing.set(character.getModifier(RESIST_SLASHING), 100);
		resistFire.set(character.getModifier(RESIST_FIRE), 100);
		resistWater.set(character.getModifier(RESIST_WATER), 100);
		resistAir.set(character.getModifier(RESIST_AIR), 100);
		resistEarth.set(character.getModifier(RESIST_EARTH), 100);
		resistMental.set(character.getModifier(RESIST_MENTAL), 100);
		resistEnergy.set(character.getModifier(RESIST_ENERGY), 100);

		kills.setText(String.valueOf(character.getKills()));
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
			((modifier>=0) ? "+"+modifier : ""+modifier);
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
		return new DIYLabel(text,DIYToolkit.Align.LEFT);
	}
	
	/*-------------------------------------------------------------------------*/
	private DIYLabel getLabel(String text, Color colour)
	{
		DIYLabel result = new DIYLabel(text, DIYToolkit.Align.LEFT);
		result.setForegroundColour(colour);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}

	/*-------------------------------------------------------------------------*/
	public static DIYLabel getBlank()
	{
		return new DIYLabel();
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
					"Enter New Name"));
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
}
