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
import java.awt.Rectangle;
import java.util.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ModifiersDisplayWidget extends ContainerWidget
{
	private PlayerCharacter character;

	private Map<String, DIYLabel> labelMap = new HashMap<String, DIYLabel>();
	private Map<String, DIYLabel> valueLabelMap = new HashMap<String, DIYLabel>();
	private DIYLabel header = getLabel("");
	private DIYLabel nameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
	private DIYLabel attributeHeader = getLabel("Attribute Modifiers", Constants.Colour.ATTRIBUTES_CYAN);
	private DIYLabel combatHeader = getLabel("Combat Modifiers", Constants.Colour.COMBAT_RED);
	private DIYLabel stealthHeader = getLabel("Stealth Modifiers", Constants.Colour.STEALTH_GREEN);
	private DIYLabel hitPointsValue = new DIYLabel("", DIYToolkit.Align.CENTER);
	private DIYLabel magicHeader = getLabel("Magic Skill Modifiers", Constants.Colour.MAGIC_BLUE);
	private DIYLabel actionPointsValue = new DIYLabel("", DIYToolkit.Align.CENTER);
	private DIYLabel magicPointsValue = new DIYLabel("", DIYToolkit.Align.CENTER);
	private ActionListener listener;

	Object[][] layout = new Object[][]
	{
		{ header,								null,										null},
		{ nameLabel,							null,										null},
		{ null,									attributeHeader,						null},
		{ Stats.Modifiers.BRAWN,			Stats.Modifiers.THIEVING,			Stats.Modifiers.BRAINS},
		{ Stats.Modifiers.SKILL,			Stats.Modifiers.SNEAKING,			Stats.Modifiers.POWER},
		{ null,									null,										null},
		{ combatHeader,						stealthHeader,							magicHeader},
		{ Stats.Modifiers.SWING,			Stats.Modifiers.STREETWISE,		Stats.Modifiers.CHANT},
		{ Stats.Modifiers.THRUST,			Stats.Modifiers.DUNGEONEER,		Stats.Modifiers.RHYME},
		{ Stats.Modifiers.BASH,				Stats.Modifiers.WILDERNESS_LORE,	Stats.Modifiers.GESTURE},
		{ Stats.Modifiers.CUT,				Stats.Modifiers.SURVIVAL,			Stats.Modifiers.POSTURE},
		{ Stats.Modifiers.LUNGE,			Stats.Modifiers.BACKSTAB,			Stats.Modifiers.THOUGHT},
		{ Stats.Modifiers.PUNCH,			Stats.Modifiers.SNIPE,				Stats.Modifiers.HERBAL},
		{ Stats.Modifiers.KICK,				Stats.Modifiers.LOCK_AND_TRAP,	Stats.Modifiers.ALCHEMIC},
		{ Stats.Modifiers.SHOOT,			Stats.Modifiers.STEAL,				null},
		{ Stats.Modifiers.THROW,			null,										Stats.Modifiers.ARTIFACTS},
		{ null,									Stats.Modifiers.SCOUTING,			Stats.Modifiers.MYTHOLOGY},
		{ Stats.Modifiers.FIRE,				Stats.Modifiers.MARTIAL_ARTS,		Stats.Modifiers.CRAFT},
		{ Stats.Modifiers.DUAL_WEAPONS,	Stats.Modifiers.MELEE_CRITICALS,	Stats.Modifiers.POWER_CAST},
		{ Stats.Modifiers.CHIVALRY,		Stats.Modifiers.THROWN_CRITICALS,Stats.Modifiers.ENGINEERING},
		{ Stats.Modifiers.KENDO,			Stats.Modifiers.RANGED_CRITICALS,Stats.Modifiers.MUSIC},
	};

	/*-------------------------------------------------------------------------*/
	public ModifiersDisplayWidget(Rectangle bounds)
	{
		super(bounds);
		this.listener = new ModifiersDisplayActionListener();
		this.buildGUI();
	}
	
	/*-------------------------------------------------------------------------*/
	private void buildGUI()
	{
		DIYLabel top = new DIYLabel("Modifiers", DIYToolkit.Align.CENTER);
		top.setBounds(162, 0, DiyGuiUserInterface.SCREEN_WIDTH-162, 30);
		top.setForegroundColour(Constants.Colour.GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize()+5);
		top.setFont(f);
		this.add(top);

		int inset = 3;
		int rows = 25;//bounds.height/(textHeight+inset) -2;
		int columns = 6;

		DIYPane pane = new DIYPane(this.x, this.y, this.width, this.height);

		pane.setLayoutManager(new DIYGridLayout(columns, rows, inset, inset));
		this.add(pane);

		for (Object[] row : layout)
		{
			for (Object cell : row)
			{
				if (cell == null)
				{
					pane.add(getBlank());
					pane.add(getBlank());
				}
				else if (cell instanceof DIYLabel[])
				{
					DIYLabel[] r = (DIYLabel[])cell;
					pane.add(r[0]);
					pane.add(r[1]);
				}
				else if (cell instanceof DIYLabel)
				{
					this.addDescLabel(pane, null, (DIYLabel)cell);
					pane.add(getBlank());
				}
				else if (cell instanceof String)
				{
					String modifier = (String)cell;
					String modName = StringUtil.getModifierName(modifier);
					this.addDescLabel(pane, modifier, getLabel(modName));
					this.addStatLabel(pane, modifier, getModifierLabel());
				}
				else
				{
					throw new MazeException("Invalid cell: " + cell);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Adds a static text desc label.
	 */ 
	private void addDescLabel(ContainerWidget parent, String name, DIYLabel label)
	{
		parent.add(label);
		label.setActionMessage(name);
		label.addActionListener(this.listener);
		label.setActionPayload(this.character);
		this.labelMap.put(name, label);
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Adds a volatile stats label.
	 */ 
	private void addStatLabel(ContainerWidget parent, String name, DIYLabel label)
	{
		parent.add(label);
		label.setActionMessage(name);
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

		for (String modifier : this.valueLabelMap.keySet())
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
		nameLabel.setText(this.character.getName()+", "+
			"level " + this.character.getLevel() + " " +
			character.getGender().getName() + " " +
			character.getRace().getName() + " " +
			character.getCharacterClass().getName());

		hitPointsValue.setText(toString(character.getHitPoints()));
		actionPointsValue.setText(toString(character.getActionPoints()));
		magicPointsValue.setText(toString(character.getMagicPoints()));
	}

	/*-------------------------------------------------------------------------*/
	private String toString(String mod, DIYLabel label)
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
}
