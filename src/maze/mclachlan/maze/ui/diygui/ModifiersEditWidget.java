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
import java.awt.event.KeyEvent;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.*;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class ModifiersEditWidget extends ContainerWidget
{
	private PlayerCharacter playerCharacter;
	private CurMax bonuses = new CurMax();
	private int maxAssignable;

	private DIYLabel bonusesLeft = new DIYLabel();
	private Map<Stats.Modifier, EditWidget> editLabelMap = new HashMap<Stats.Modifier, EditWidget>();
	private Map<Stats.Modifier, DIYLabel> labelMap = new HashMap<Stats.Modifier, DIYLabel>();

	/** this is the ultimate player choices */
	private StatModifier statModifier;

	private ActionListener listener = new ModifiersDisplayActionListener();

	private Stats.Modifier modifierPointer;
	private int pointerRow;
	private int pointerColumn;

	private Object[][] layout;
	private ActionListener parentListener;
	private Leveler leveler;

	/*-------------------------------------------------------------------------*/
	public ModifiersEditWidget(
		int x, 
		int y, 
		int width, 
		int height, 
		ActionListener parentListener,
		boolean allowEditingBaseModifiers,
		Leveler leveler)
	{
		super(x, y, width, height);
		this.parentListener = parentListener;
		this.leveler = leveler;

		DIYLabel header = getSubTitle(" Assign Modifiers");
		header.setForegroundColour(Constants.Colour.GOLD);

		DIYLabel combatHeader = getLabel("Combat Modifiers", Constants.Colour.COMBAT_RED);
		DIYLabel stealthHeader = getLabel("Stealth Modifiers", Constants.Colour.STEALTH_GREEN);
		DIYLabel magicHeader = getLabel("Magic Modifiers", Constants.Colour.MAGIC_BLUE);
		DIYLabel attributeHeader = getLabel("Attribute Modifiers", Constants.Colour.ATTRIBUTES_CYAN);
		
		if (!allowEditingBaseModifiers)
		{
			layout = new Object[][]
			{
				{ header, 								bonusesLeft,							null},
				{combatHeader,							stealthHeader,							magicHeader},
				{ Stats.Modifier.SWING,			Stats.Modifier.STREETWISE,	Stats.Modifier.CHANT},
				{ Stats.Modifier.THRUST,			Stats.Modifier.DUNGEONEER,	Stats.Modifier.RHYME},
				{ Stats.Modifier.BASH,				Stats.Modifier.WILDERNESS_LORE,	Stats.Modifier.GESTURE},
				{ Stats.Modifier.CUT,				Stats.Modifier.SURVIVAL,	Stats.Modifier.POSTURE},
				{ Stats.Modifier.LUNGE,			Stats.Modifier.BACKSTAB,			Stats.Modifier.THOUGHT},
				{ Stats.Modifier.PUNCH,			Stats.Modifier.SNIPE,				Stats.Modifier.HERBAL},
				{ Stats.Modifier.KICK,				Stats.Modifier.LOCK_AND_TRAP,	Stats.Modifier.ALCHEMIC},
				{ Stats.Modifier.SHOOT,			Stats.Modifier.STEAL,				null},
				{ Stats.Modifier.THROW,			null,										Stats.Modifier.ARTIFACTS},
				{ null,									Stats.Modifier.MARTIAL_ARTS,		Stats.Modifier.MYTHOLOGY},
				{ Stats.Modifier.FIRE,				Stats.Modifier.MELEE_CRITICALS,	Stats.Modifier.CRAFT},
				{ Stats.Modifier.DUAL_WEAPONS,	Stats.Modifier.THROWN_CRITICALS, Stats.Modifier.POWER_CAST},
				{ Stats.Modifier.CHIVALRY,		Stats.Modifier.RANGED_CRITICALS, Stats.Modifier.ENGINEERING},
				{ Stats.Modifier.KENDO,			null,										Stats.Modifier.MUSIC},			};
		}
		else
		{
			layout = new Object[][]
			{
				{ header, 								bonusesLeft,							null},
				{ null, 									attributeHeader,						null},
				{ Stats.Modifier.BRAWN,			Stats.Modifier.THIEVING,			Stats.Modifier.BRAINS},
				{ Stats.Modifier.SKILL,			Stats.Modifier.SNEAKING,			Stats.Modifier.POWER},
				{ null,									null,										null},
				{combatHeader,							stealthHeader,							magicHeader},
				{ Stats.Modifier.SWING,			Stats.Modifier.STREETWISE,	Stats.Modifier.CHANT},
				{ Stats.Modifier.THRUST,			Stats.Modifier.DUNGEONEER,	Stats.Modifier.RHYME},
				{ Stats.Modifier.BASH,				Stats.Modifier.WILDERNESS_LORE,	Stats.Modifier.GESTURE},
				{ Stats.Modifier.CUT,				Stats.Modifier.SURVIVAL,	Stats.Modifier.POSTURE},
				{ Stats.Modifier.LUNGE,			Stats.Modifier.BACKSTAB,			Stats.Modifier.THOUGHT},
				{ Stats.Modifier.PUNCH,			Stats.Modifier.SNIPE,				Stats.Modifier.HERBAL},
				{ Stats.Modifier.KICK,				Stats.Modifier.LOCK_AND_TRAP,	Stats.Modifier.ALCHEMIC},
				{ Stats.Modifier.SHOOT,			Stats.Modifier.STEAL,				null},
				{ Stats.Modifier.THROW,			null,										Stats.Modifier.ARTIFACTS},
				{ null,									Stats.Modifier.MARTIAL_ARTS,		Stats.Modifier.MYTHOLOGY},
				{ Stats.Modifier.FIRE,				Stats.Modifier.MELEE_CRITICALS,	Stats.Modifier.CRAFT},
				{ Stats.Modifier.DUAL_WEAPONS,	Stats.Modifier.THROWN_CRITICALS, Stats.Modifier.POWER_CAST},
				{ Stats.Modifier.CHIVALRY,		Stats.Modifier.RANGED_CRITICALS, Stats.Modifier.ENGINEERING},
				{ Stats.Modifier.KENDO,			null,										Stats.Modifier.MUSIC},
			};
		}

		buildGui();
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui()
	{
		int columns = 6;
		int rows = layout.length;

		this.setLayoutManager(new DIYGridLayout(columns, rows, 2, 2));

		bonusesLeft.setText(toString(this.bonuses));

		for (Object[] row : layout)
		{
			for (Object aRow : row)
			{
				if (aRow == null)
				{
					this.add(getBlank());
					this.add(getBlank());
				}
				else if (aRow instanceof DIYLabel)
				{
					this.addDescLabel(null, (DIYLabel)aRow);
					this.add(getBlank());
				}
				else if (aRow instanceof Stats.Modifier)
				{
					Stats.Modifier modifier = (Stats.Modifier)aRow;
					String modName = StringUtil.getModifierName(modifier);
					this.addDescLabel(modifier, getLabel(modName));
					this.addStatLabel(modifier, getModifierLabel());
				}
				else
				{
					throw new MazeException("Invalid cell: " + aRow);
				}
			}
		}

		setModifierPointer(2, 0);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Adds a static text desc label.
	 */
	private void addDescLabel(Stats.Modifier modifier, DIYLabel label)
	{
		this.add(label);
		label.setActionMessage(modifier==null?null:modifier.toString());
		label.addActionListener(listener);
		this.labelMap.put(modifier, label);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Adds a volatile stats label.
	 */
	private void addStatLabel(Stats.Modifier name, EditWidget label)
	{
		this.add(label);
		label.modifier = name;
		this.editLabelMap.put(name, label);
	}

	/*-------------------------------------------------------------------------*/
	private String toString(CurMax c)
	{
		return "Modifiers Remaining: "+c.getCurrent();
	}

	/*-------------------------------------------------------------------------*/
	private String toString(int modifier)
	{
		return ((modifier>=0) ? "+"+modifier : ""+modifier);
	}

	/*-------------------------------------------------------------------------*/
	private EditWidget getModifierLabel()
	{
		return new EditWidget();
	}

	/*-------------------------------------------------------------------------*/
	private static DIYLabel getLabel(String text)
	{
		return new DIYLabel(text, DIYToolkit.Align.LEFT);
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getSubTitle(String titleText)
	{
		DIYLabel title = new DIYLabel(titleText);
		title.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize()+3);
		title.setFont(f);
		return title;
	}

	/*-------------------------------------------------------------------------*/
	private static DIYLabel getLabel(String text, Color colour)
	{
		DIYLabel result = new DIYLabel(text, DIYToolkit.Align.LEFT);
		result.setForegroundColour(colour);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static DIYLabel getBlank()
	{
		return new DIYLabel();
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.NONE;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(PlayerCharacter pc, int bonuses, int maxAssignable)
	{
		this.playerCharacter = pc;
		this.bonuses = new CurMax(bonuses);
		this.maxAssignable = maxAssignable;

		this.statModifier = new StatModifier();
		updateBonusesLeft();

		setModifierPointer(2, 0);

		for (Stats.Modifier modifier : this.editLabelMap.keySet())
		{
			EditWidget editLabel = this.editLabelMap.get(modifier);
			if (editLabel != null)
			{
				editLabel.setEnabled(pc.isActiveModifier(modifier));
				editLabel.refresh(pc.getBaseModifier(modifier));
			}

			DIYLabel label = this.labelMap.get(modifier);
			if (label != null)
			{
				if (pc.isActiveModifier(modifier))
				{
					label.setForegroundColour(null);
				}
				else
				{
					label.setForegroundColour(Color.GRAY);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void setModifierPointer(int row, int column)
	{
		pointerRow = row;
		pointerColumn = column;

		modifierPointer = (Stats.Modifier)layout[row][column];
		for (Stats.Modifier s : this.editLabelMap.keySet())
		{
			EditWidget editWidget = editLabelMap.get(s);
			editWidget.setPointer(s.equals(modifierPointer));
		}
	}

	/*-------------------------------------------------------------------------*/
	void plus(PlayerCharacter playerCharacter, StatModifier statModifier, Stats.Modifier modifier, CurMax bonuses)
	{
		this.leveler.plus(playerCharacter, statModifier, modifier, bonuses);
		updateBonusesLeft();
	}

	/*-------------------------------------------------------------------------*/
	void minus(PlayerCharacter playerCharacter, StatModifier statModifier, Stats.Modifier modifier, CurMax bonuses)
	{
		this.leveler.minus(playerCharacter, statModifier, modifier, bonuses);
		updateBonusesLeft();
	}

	/*-------------------------------------------------------------------------*/
	private void updateBonusesLeft()
	{
		this.bonusesLeft.setText(toString(this.bonuses));

		// set the state of all the plus buttons
		for (Stats.Modifier modifier : this.editLabelMap.keySet())
		{
			int current = playerCharacter.getModifier(modifier);
			if (statModifier != null)
			{
				current += statModifier.getModifier(modifier);
			}

			int costToIncrease = GameSys.getInstance().getModifierIncreaseCost(
				modifier, playerCharacter, current);

			EditWidget label = this.editLabelMap.get(modifier);
			if (label != null)
			{
				label.plus.setEnabled(
					bonuses.getCurrent() >= costToIncrease &&
						label.assignedSoFar < maxAssignable &&
						playerCharacter.isActiveModifier(modifier));

				if (costToIncrease > 1)
				{
					label.valueLabel.setForegroundColour(Color.WHITE);
				}
				else
				{
					label.valueLabel.setForegroundColour(Color.LIGHT_GRAY);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		int pr = pointerRow;
		int pc = pointerColumn;
		EditWidget w;

		switch(e.getKeyCode())
		{
			case KeyEvent.VK_UP:
				if (pr > 2)
					pr--;
				break;
			case KeyEvent.VK_DOWN:
				if (pr < 10)
					pr++;
				break;
			case KeyEvent.VK_LEFT:
				if (pc > 0)
					pc--;
				break;
			case KeyEvent.VK_RIGHT:
				if (pc < 2)
					pc++;
				break;
			case KeyEvent.VK_PLUS:
			case KeyEvent.VK_ADD:
			case KeyEvent.VK_EQUALS:
				w = this.editLabelMap.get(modifierPointer);
				if (w.plus.isEnabled())
					w.plus();
				break;
			case KeyEvent.VK_SUBTRACT:
			case KeyEvent.VK_MINUS:
				w = this.editLabelMap.get(modifierPointer);
				if (w.minus.isEnabled())
					w.minus();
				break;
		}

		if (layout[pr][pc] instanceof String)
		{
			setModifierPointer(pr, pc);
		}

		// Hax0r alert! Dummy action event to update the parent state. 
		parentListener.actionPerformed(new ActionEvent(null, null, null, null));
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getStatModifier()
	{
		return statModifier;
	}

	/*-------------------------------------------------------------------------*/
	public CurMax getBonuses()
	{
		return bonuses;
	}

	/*-------------------------------------------------------------------------*/
	private class EditWidget extends ContainerWidget implements ActionListener
	{
		DIYButton plus, minus;
		DIYLabel valueLabel, pointerLabel;
		int value;

		int assignedSoFar = 0;
		Stats.Modifier modifier;

		/*----------------------------------------------------------------------*/
		public EditWidget()
		{
			super(0,0,1,1);
			plus = new DIYButton("+");
			minus = new DIYButton("-");
			valueLabel = new DIYLabel();
			pointerLabel = new DIYLabel();
			pointerLabel.setForegroundColour(Color.WHITE);

			plus.addActionListener(this);
			plus.addActionListener(parentListener);
			minus.addActionListener(this);
			minus.addActionListener(parentListener);

			this.add(plus);
			this.add(minus);
			this.add(valueLabel);
			this.add(pointerLabel);
		}

		/*----------------------------------------------------------------------*/
		public void setPointer(boolean pointer)
		{
			if (pointer)
			{
				pointerLabel.setText("<");
			}
			else
			{
				pointerLabel.setText("");
			}
		}

		/*----------------------------------------------------------------------*/
		public void refresh(int value)
		{
			this.value = value;
			this.assignedSoFar = 0;
			this.valueLabel.setText(ModifiersEditWidget.this.toString(value));

			if (isEnabled())
			{
				this.minus.setEnabled(false);
				this.plus.setEnabled(true);
			}
		}

		/*----------------------------------------------------------------------*/
		public String getWidgetName()
		{
			return DIYToolkit.NONE;
		}

		/*----------------------------------------------------------------------*/
		public void doLayout()
		{
			int inset = 1;
			int buttonSize = height-inset*2;
			int sx = x + width/2 - buttonSize*2;

			this.minus.setBounds(sx, y+inset, buttonSize, buttonSize);
			this.valueLabel.setBounds(sx+buttonSize+inset, y+inset, buttonSize*2, buttonSize);
			this.plus.setBounds(sx+buttonSize*3+inset*2, y+inset, buttonSize, buttonSize);
			this.pointerLabel.setBounds(sx+buttonSize*4+inset*3, y+inset, buttonSize, buttonSize);
		}

		/*----------------------------------------------------------------------*/
		public void setEnabled(boolean enabled)
		{
			super.setEnabled(enabled);

			this.plus.setEnabled(enabled);
			this.minus.setEnabled(enabled);
			this.valueLabel.setEnabled(enabled);

			super.setEnabled(enabled);
		}

		/*----------------------------------------------------------------------*/
		public void actionPerformed(ActionEvent event)
		{
			Object obj = event.getSource();

			if (obj == plus)
			{
				plus();
			}
			else if (obj == minus)
			{
				minus();
			}
		}

		/*-------------------------------------------------------------------------*/
		private void minus()
		{
			this.value--;
			this.assignedSoFar--;
			this.valueLabel.setText(ModifiersEditWidget.this.toString(value));

			if (this.assignedSoFar == 0)
			{
				this.minus.setEnabled(false);
			}

			if (this.assignedSoFar < maxAssignable)
			{
				this.plus.setEnabled(true);
			}

			ModifiersEditWidget.this.minus(playerCharacter, statModifier, modifier, bonuses);
		}

		/*-------------------------------------------------------------------------*/
		private void plus()
		{
			this.value++;
			this.assignedSoFar++;
			this.valueLabel.setText(ModifiersEditWidget.this.toString(value));

			if (this.assignedSoFar == maxAssignable)
			{
				this.plus.setEnabled(false);
			}

			if (this.assignedSoFar > 0)
			{
				this.minus.setEnabled(true);
			}

			ModifiersEditWidget.this.plus(playerCharacter, statModifier, modifier, bonuses);
		}
	}
}
