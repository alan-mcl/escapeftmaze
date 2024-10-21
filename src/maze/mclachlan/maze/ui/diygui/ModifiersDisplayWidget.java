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
import java.util.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Stats;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class ModifiersDisplayWidget extends ContainerWidget
{
	private PlayerCharacter character;

	private final Map<Stats.Modifier, DIYLabel> labelMap = new HashMap<>();
	private final Map<Stats.Modifier, DIYLabel> valueLabelMap = new HashMap<>();
	private final DIYLabel nameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
	private final DIYLabel hitPointsValue = new DIYLabel("", DIYToolkit.Align.CENTER);
	private final DIYLabel actionPointsValue = new DIYLabel("", DIYToolkit.Align.CENTER);
	private final DIYLabel magicPointsValue = new DIYLabel("", DIYToolkit.Align.CENTER);
	private final ActionListener listener;

	/*-------------------------------------------------------------------------*/
	public ModifiersDisplayWidget(Rectangle bounds)
	{
		super(bounds);
		this.listener = new ModifiersDisplayActionListener();
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
		int contentHeight = height - contentTop - buttonPaneHeight -inset -DiyGuiUserInterface.SCREEN_EDGE_INSET;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int frameBorderInset = rp.getProperty(RendererProperties.Property.PANEL_LIGHT_BORDER);

		int column1x = bounds.x + inset;
		int columnWidth = (width -5*inset) / 3;

		// screen title
		DIYLabel title = getSubTitle(StringUtil.getUiLabel("mdw.title"));
		title.setBounds(
			200, DiyGuiUserInterface.SCREEN_EDGE_INSET,
			DiyGuiUserInterface.SCREEN_WIDTH - 400, titleHeight);


		// attributes
		DIYLabel attributeHeader = getLabel(StringUtil.getUiLabel("mdw.attributes"), Constants.Colour.ATTRIBUTES_CYAN);

		DIYPanel attributesPanel = new DIYPanel();
		attributesPanel.setStyle(DIYPanel.Style.PANEL_LIGHT);
		attributesPanel.setLayoutManager(new DIYGridLayout(6, 3, inset/2, inset/2));
		attributesPanel.setInsets(new Insets(panelBorderInset+inset, panelBorderInset+inset, frameBorderInset, panelBorderInset+inset));
		attributesPanel.setBounds(
			column1x,
			contentTop,
			columnWidth*3 + inset*2,
			frameBorderInset*2 + 80);

		nameLabel.setBounds(
			attributesPanel.x +frameBorderInset +inset/2,
			attributesPanel.y +frameBorderInset,
			attributesPanel.width/2,
			20);

		attributesPanel.add(getBlank());
		attributesPanel.add(getBlank());
		addDescLabel(attributesPanel, null, attributeHeader);
		attributesPanel.add(getBlank());
		attributesPanel.add(getBlank());
		attributesPanel.add(getBlank());

		Stats.Modifier[][] layout = new Stats.Modifier[][]
			{
				{Stats.Modifier.BRAWN, Stats.Modifier.THIEVING, Stats.Modifier.BRAINS},
				{Stats.Modifier.SKILL, Stats.Modifier.SNEAKING, Stats.Modifier.POWER},
			};

		for (Stats.Modifier[] row : layout)
		{
			for (Stats.Modifier mod : row)
			{
				String modName = StringUtil.getModifierName(mod);
				addDescLabel(attributesPanel, mod, getLabel(modName));
				addStatLabel(attributesPanel, mod, getModifierLabel());
			}
		}

		// modifiers
		DIYLabel combatHeader = getLabel(StringUtil.getUiLabel("mdw.combat"), Constants.Colour.COMBAT_RED);
		DIYLabel stealthHeader = getLabel(StringUtil.getUiLabel("mdw.stealth"), Constants.Colour.STEALTH_GREEN);
		DIYLabel magicHeader = getLabel(StringUtil.getUiLabel("mdw.magic"), Constants.Colour.MAGIC_BLUE);

		DIYPanel modifiersPanel = new DIYPanel();
		modifiersPanel.setStyle(DIYPanel.Style.PANEL_MED);
		modifiersPanel.setLayoutManager(new DIYGridLayout(6, 15, inset/2, inset/2));
		modifiersPanel.setInsets(new Insets(panelBorderInset+inset, panelBorderInset+inset, panelBorderInset+inset, panelBorderInset+inset));
		modifiersPanel.setBounds(
			column1x,
			attributesPanel.y +attributesPanel.height +inset,
			columnWidth*3 + inset*2,
			contentHeight -attributesPanel.height -inset);

		addDescLabel(modifiersPanel, null, combatHeader);
		modifiersPanel.add(getBlank());
		addDescLabel(modifiersPanel, null, stealthHeader);
		modifiersPanel.add(getBlank());
		addDescLabel(modifiersPanel, null, magicHeader);
		modifiersPanel.add(getBlank());


		layout = new Stats.Modifier[][]
		{
			{ Stats.Modifier.SWING,			Stats.Modifier.STREETWISE,		Stats.Modifier.CHANT},
			{ Stats.Modifier.THRUST,			Stats.Modifier.DUNGEONEER,		Stats.Modifier.RHYME},
			{ Stats.Modifier.BASH,				Stats.Modifier.WILDERNESS_LORE,	Stats.Modifier.GESTURE},
			{ Stats.Modifier.CUT,				Stats.Modifier.SURVIVAL,			Stats.Modifier.POSTURE},
			{ Stats.Modifier.LUNGE,			Stats.Modifier.BACKSTAB,			Stats.Modifier.THOUGHT},
			{ Stats.Modifier.PUNCH,			Stats.Modifier.SNIPE,				Stats.Modifier.HERBAL},
			{ Stats.Modifier.KICK,				Stats.Modifier.LOCK_AND_TRAP,	Stats.Modifier.ALCHEMIC},
			{ Stats.Modifier.SHOOT,			Stats.Modifier.STEAL,				null},
			{ Stats.Modifier.THROW,			null,										Stats.Modifier.ARTIFACTS},
			{ null,									Stats.Modifier.SCOUTING,			Stats.Modifier.MYTHOLOGY},
			{ Stats.Modifier.FIRE,				Stats.Modifier.MARTIAL_ARTS,		Stats.Modifier.CRAFT},
			{ Stats.Modifier.DUAL_WEAPONS,	Stats.Modifier.MELEE_CRITICALS,	Stats.Modifier.POWER_CAST},
			{ Stats.Modifier.CHIVALRY,		Stats.Modifier.THROWN_CRITICALS, Stats.Modifier.ENGINEERING},
			{ Stats.Modifier.KENDO,			Stats.Modifier.RANGED_CRITICALS, Stats.Modifier.MUSIC},
		};

		for (Stats.Modifier[] row : layout)
		{
			for (Stats.Modifier mod : row)
			{
				if (mod == null)
				{
					modifiersPanel.add(getBlank());
					modifiersPanel.add(getBlank());
				}
				else
				{
					String modName = StringUtil.getModifierName(mod);
					addDescLabel(modifiersPanel, mod, getLabel(modName));
					addStatLabel(modifiersPanel, mod, getModifierLabel());
				}
			}
		}

		this.add(title);
		this.add(attributesPanel);
		this.add(modifiersPanel);
		this.add(nameLabel);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Adds a static text desc label.
	 */ 
	private void addDescLabel(ContainerWidget parent, Stats.Modifier modifier, DIYLabel label)
	{
		parent.add(label);
		label.setActionMessage(modifier == null? null : modifier.toString());
		label.addActionListener(this.listener);
		label.setActionPayload(this.character);
		this.labelMap.put(modifier, label);
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Adds a volatile stats label.
	 */ 
	private void addStatLabel(ContainerWidget parent, Stats.Modifier name, DIYLabel label)
	{
		parent.add(label);
		label.setActionMessage(name.toString());
		label.addActionListener(this.listener);
		label.setActionPayload(this.character);
		this.valueLabelMap.put(name, label);
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

		for (Stats.Modifier modifier : this.valueLabelMap.keySet())
		{
			DIYLabel label = this.valueLabelMap.get(modifier);
			if (label != null)
			{
				label.setText(toString(modifier, label));
				label.setEnabled(character.isActiveModifier(modifier));
				label.setActionPayload(character);
			}

			label = this.labelMap.get(modifier);
			if (label != null)
			{
				label.setActionPayload(character);
				if (character.isActiveModifier(modifier))
				{
					label.setForegroundColour(null);
				}
				else
				{
					label.setForegroundColour(Color.GRAY);
				}
			}
		}
		
		nameLabel.setForegroundColour(Color.WHITE);
		nameLabel.setText(StringUtil.getUiLabel(
			"idw.character.details",
			this.character.getName(),
			String.valueOf(this.character.getLevel()),
			character.getGender().getName(),
			character.getRace().getName(),
			character.getCharacterClass().getName()));

		hitPointsValue.setText(toString(character.getHitPoints()));
		actionPointsValue.setText(toString(character.getActionPoints()));
		magicPointsValue.setText(toString(character.getMagicPoints()));
	}

	/*-------------------------------------------------------------------------*/
	private String toString(Stats.Modifier mod, DIYLabel label)
	{
		int modifier = character.getModifier(mod);
		int intrinsicModifier = character.getIntrinsicModifier(mod);
		
		Color colour = null;
		if (modifier < intrinsicModifier)
		{
			colour = Constants.Colour.COMBAT_RED;
		}
		else if (modifier == intrinsicModifier)
		{
			colour = Color.LIGHT_GRAY;
		}
		else if (modifier > intrinsicModifier)
		{
			colour = Constants.Colour.STEALTH_GREEN;
		}
		
		if (!character.isActiveModifier(mod))
		{
			colour = colour.darker();
		}
		
		label.setForegroundColour(colour);

		return Stats.descModifier(mod, modifier);
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
	private String toString(CurMax c)
	{
		return c.getCurrent()+" / "+c.getMaximum();
	}
	
	/*-------------------------------------------------------------------------*/
	public static DIYLabel getBlank()
	{
		return new DIYLabel();
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
