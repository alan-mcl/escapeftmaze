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

package mclachlan.maze.editor.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.stat.*;

/**
 * Editor for one {@link CharacterSelectionMethod}.
 */
public class CharacterSelectionMethodEditor extends JDialog implements ActionListener
{
	private static final int CUSTOM = 0;
	private static final int PLAYER = 1;
	private static final int LOWEST = 2;
	private static final int HIGHEST = 3;
	private static final int RANDOM = 4;
	private static final int MODIFIER_COMP = 5;
	private static final int MAX = 6;

	private static Map<Class<?>, Integer> types;

	static
	{
		types = new HashMap<>();
		types.put(PlayerCharacterSelection.class, PLAYER);
		types.put(LowestModifierSelection.class, LOWEST);
		types.put(HighestModifierSelection.class, HIGHEST);
		types.put(RandomCharacterSelection.class, RANDOM);
		types.put(ModifierComparisonSelection.class, MODIFIER_COMP);
	}

	private CharacterSelectionMethod result;
	private final boolean allowPlayer;
	private final int dirtyFlag;

	private JComboBox<String> type;
	private JTextField impl;
	private CardLayout cards;
	private JPanel controls;
	private JComboBox<Stats.Modifier> modifier;
	private JComboBox<ComparisonOperator> op;
	private JSpinner value;
	private JButton ok, cancel;

	/*-------------------------------------------------------------------------*/
	public CharacterSelectionMethodEditor(
		Frame owner,
		CharacterSelectionMethod method,
		boolean allowPlayer,
		int dirtyFlag)
	{
		super(owner, "Edit Character Selection Method", true);
		this.allowPlayer = allowPlayer;
		this.dirtyFlag = dirtyFlag;

		Vector<String> vec = new Vector<>();
		vec.add("Custom (Java class)");
		if (allowPlayer)
		{
			vec.add("Player pick");
		}
		vec.add("Lowest modifier");
		vec.add("Highest modifier");
		vec.add("Random");
		vec.add("Modifier comparison");

		type = new JComboBox<>(vec);
		type.addActionListener(this);

		impl = new JTextField(30);

		modifier = new JComboBox<>(Stats.Modifier.values());
		op = new JComboBox<>(ComparisonOperator.values());
		value = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));

		cards = new CardLayout(3, 3);
		controls = new JPanel(cards);
		controls.add(getCustomPanel(), String.valueOf(CUSTOM));
		controls.add(new JPanel(), String.valueOf(PLAYER));
		controls.add(getModifierPanel(), String.valueOf(LOWEST));
		controls.add(getModifierPanel(), String.valueOf(HIGHEST));
		controls.add(new JPanel(), String.valueOf(RANDOM));
		controls.add(getModifierComparisonPanel(), String.valueOf(MODIFIER_COMP));

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);

		JPanel top = new JPanel();
		top.add(new JLabel("Type"));
		top.add(type);

		setLayout(new BorderLayout(3, 3));
		add(top, BorderLayout.NORTH);
		add(controls, BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);

		if (method != null)
		{
			setState(method);
		}
		else
		{
			if (allowPlayer)
			{
				type.setSelectedIndex(1);
				cards.show(controls, String.valueOf(PLAYER));
			}
			else
			{
				type.setSelectedIndex(1);
				cards.show(controls, String.valueOf(LOWEST));
			}
		}

		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCustomPanel()
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel("Implementation class"));
		panel.add(impl);
		return panel;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getModifierPanel()
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel("Modifier"));
		panel.add(modifier);
		return panel;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getModifierComparisonPanel()
	{
		JPanel panel = new JPanel(new GridLayout(3, 2, 3, 3));
		panel.add(new JLabel("Modifier"));
		panel.add(modifier);
		panel.add(new JLabel("Operator"));
		panel.add(op);
		panel.add(new JLabel("Value"));
		panel.add(value);
		return panel;
	}

	/*-------------------------------------------------------------------------*/
	private void setState(CharacterSelectionMethod method)
	{
		if (types.containsKey(method.getClass()))
		{
			int meType = types.get(method.getClass());
			type.setSelectedIndex(dialogIndexForType(meType));
			cards.show(controls, String.valueOf(meType));

			if (method instanceof LowestModifierSelection lowest)
			{
				modifier.setSelectedItem(lowest.getTargetModifier());
			}
			else if (method instanceof HighestModifierSelection highest)
			{
				modifier.setSelectedItem(highest.getTargetModifier());
			}
			else if (method instanceof ModifierComparisonSelection comp)
			{
				modifier.setSelectedItem(comp.getTargetModifier());
				op.setSelectedItem(comp.getOp());
				value.setValue(comp.getValue());
			}
		}
		else
		{
			type.setSelectedIndex(0);
			cards.show(controls, String.valueOf(CUSTOM));
			impl.setText(method.getClass().getName());
		}
	}

	/*-------------------------------------------------------------------------*/
	private int dialogIndexForType(int meType)
	{
		if (allowPlayer)
		{
			return meType;
		}
		return meType - 1;
	}

	/*-------------------------------------------------------------------------*/
	private int typeForDialogIndex(int dialogIndex)
	{
		if (allowPlayer)
		{
			return dialogIndex;
		}
		return dialogIndex + 1;
	}

	/*-------------------------------------------------------------------------*/
	public CharacterSelectionMethod getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == type)
		{
			cards.show(controls, String.valueOf(typeForDialogIndex(type.getSelectedIndex())));
		}
		else if (e.getSource() == ok)
		{
			saveResult();
			if (SwingEditor.instance != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
			}
			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			result = null;
			setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void saveResult()
	{
		int meType = typeForDialogIndex(type.getSelectedIndex());
		switch (meType)
		{
			case CUSTOM:
				try
				{
					result = (CharacterSelectionMethod)Class.forName(impl.getText()).getDeclaredConstructor().newInstance();
				}
				catch (Exception ex)
				{
					throw new RuntimeException(ex);
				}
				break;
			case PLAYER:
				result = new PlayerCharacterSelection();
				break;
			case LOWEST:
				result = new LowestModifierSelection((Stats.Modifier)modifier.getSelectedItem());
				break;
			case HIGHEST:
				result = new HighestModifierSelection((Stats.Modifier)modifier.getSelectedItem());
				break;
			case RANDOM:
				result = new RandomCharacterSelection();
				break;
			case MODIFIER_COMP:
				result = new ModifierComparisonSelection(
					(Stats.Modifier)modifier.getSelectedItem(),
					(ComparisonOperator)op.getSelectedItem(),
					((Number)value.getValue()).intValue());
				break;
			default:
				result = null;
		}
	}
}
