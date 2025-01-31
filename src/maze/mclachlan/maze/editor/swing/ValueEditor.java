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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ValueEditor extends JDialog implements ActionListener
{
	private final JSpinner base;
	private final JCheckBox negate;
	private final JComboBox scaling;
	private final JComboBox reference;
	private final JRadioButton custom, constant, dice, modifier, colourMagic;
	private final JTextField impl, diceValue;
	private final JComboBox modifierValue, colourMagicValue;
	private final JButton ok, cancel;

	private Value value;

	/*-------------------------------------------------------------------------*/
	public ValueEditor(Frame owner, Value v) throws HeadlessException
	{
		super(owner, "Edit Value", true);

		base = new JSpinner(new SpinnerNumberModel(v.getValue(), -127, 127, 1));

		scaling = new JComboBox(Value.SCALE.values());
		scaling.setSelectedItem(v.getScaling());
		scaling.addActionListener(this);

		reference = new JComboBox();
		negate = new JCheckBox("Negate?", v.shouldNegate());
		custom = new JRadioButton("Custom");
		custom.addActionListener(this);
		constant = new JRadioButton("Constant");
		constant.addActionListener(this);
		dice = new JRadioButton("Dice");
		dice.addActionListener(this);
		modifier = new JRadioButton("Modifier");
		modifier.addActionListener(this);
		colourMagic = new JRadioButton("Colour Magic");
		colourMagic.addActionListener(this);

		impl = new JTextField();
		diceValue = new JTextField();
		Vector<Stats.Modifier> vec = new Vector<Stats.Modifier>(Stats.allModifiers);
		Collections.sort(vec);
		modifierValue = new JComboBox(vec);
		Vector<String> magicColours = new Vector<String>();
		magicColours.add(MagicSys.MagicColour.describe(MagicSys.MagicColour.BLACK));
		magicColours.add(MagicSys.MagicColour.describe(MagicSys.MagicColour.BLUE));
		magicColours.add(MagicSys.MagicColour.describe(MagicSys.MagicColour.GOLD));
		magicColours.add(MagicSys.MagicColour.describe(MagicSys.MagicColour.GREEN));
		magicColours.add(MagicSys.MagicColour.describe(MagicSys.MagicColour.PURPLE));
		magicColours.add(MagicSys.MagicColour.describe(MagicSys.MagicColour.RED));
		magicColours.add(MagicSys.MagicColour.describe(MagicSys.MagicColour.WHITE));
		colourMagicValue = new JComboBox(magicColours);

		ButtonGroup bg = new ButtonGroup();
		bg.add(custom);
		bg.add(constant);
		bg.add(dice);
		bg.add(modifier);
		bg.add(colourMagic);

		initState(v);
		resetReferenceOptions(
			v.getScaling(),
			v.getReference());

		JPanel controls = new JPanel(new GridLayout(8, 2, 3, 3));
		controls.add(new JLabel("Scaling:"));
		controls.add(scaling);
		controls.add(new JLabel("Reference:"));
		controls.add(reference);
		controls.add(negate);
		controls.add(new JLabel());
		controls.add(constant);
		controls.add(base);
		controls.add(custom);
		controls.add(impl);
		controls.add(dice);
		controls.add(diceValue);
		controls.add(modifier);
		controls.add(modifierValue);
		controls.add(colourMagic);
		controls.add(colourMagicValue);

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);

		this.setLayout(new BorderLayout(3,3));
		this.add(controls, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);
		
		this.pack();
		setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private void initState(Value v)
	{
		if (v.getClass() == Value.class)
		{
			// just a base value
			constant.setSelected(true);
			base.setEnabled(true);
			impl.setEnabled(false);
			diceValue.setEnabled(false);
			modifierValue.setEnabled(false);
			colourMagicValue.setEnabled(false);
		}
		else if (v instanceof DiceValue)
		{
			dice.setSelected(true);
			base.setEnabled(false);
			impl.setEnabled(false);
			diceValue.setEnabled(true);
			modifierValue.setEnabled(false);
			colourMagicValue.setEnabled(false);

			diceValue.setText(V1Dice.toString(((DiceValue)v).getDice()));
		}
		else if (v instanceof ModifierValue)
		{
			modifier.setSelected(true);
			base.setEnabled(false);
			impl.setEnabled(false);
			diceValue.setEnabled(false);
			modifierValue.setEnabled(true);
			colourMagicValue.setEnabled(false);

			modifierValue.setSelectedItem(((ModifierValue)v).getModifier());
		}
		else if (v instanceof MagicPresentValue)
		{
			colourMagic.setSelected(true);
			base.setEnabled(false);
			impl.setEnabled(false);
			diceValue.setEnabled(false);
			modifierValue.setEnabled(false);
			colourMagicValue.setEnabled(true);

			colourMagicValue.setSelectedItem(MagicSys.MagicColour.describe(((MagicPresentValue)v).getColour()));
		}
		else
		{
			// must be a custom impl
			custom.setSelected(true);
			base.setEnabled(false);
			impl.setEnabled(true);
			diceValue.setEnabled(false);
			modifierValue.setEnabled(false);
			colourMagicValue.setEnabled(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
		{
			// construct the result
			int baseValue = (Integer)(base.getValue());
			Value.SCALE scale = (Value.SCALE)scaling.getSelectedItem();
			Object ref = reference.getSelectedItem();
			boolean negate = this.negate.isSelected();
			if (constant.isSelected())
			{
				this.value = new Value(baseValue, scale);
			}
			else if (custom.isSelected())
			{
				try
				{
					Class clazz = Class.forName(impl.getText());
					this.value = (Value)clazz.newInstance();
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
			}
			else if (dice.isSelected())
			{
				this.value = new DiceValue(V1Dice.fromString(diceValue.getText()));
			}
			else if (modifier.isSelected())
			{
				this.value = new ModifierValue((Stats.Modifier)modifierValue.getSelectedItem());
			}
			else if (colourMagic.isSelected())
			{
				int magicColour = MagicSys.MagicColour.valueOf((String)colourMagicValue.getSelectedItem());
				this.value = new MagicPresentValue(magicColour);
			}
			
			this.value.setScaling(scale);
			this.value.setNegate(negate);

			if (ref != null && ref.toString().length() > 0)
			{
				String s = ref.toString();
				this.value.setReference(s);
			}

			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			setVisible(false);
		}
		else if (e.getSource() == scaling)
		{
			resetReferenceOptions(
				(Value.SCALE)scaling.getSelectedItem(),
				reference.getSelectedItem());
		}
		else if (e.getSource() == constant)
		{
			base.setEnabled(true);
			impl.setEnabled(false);
			diceValue.setEnabled(false);
			modifierValue.setEnabled(false);
			colourMagicValue.setEnabled(false);

			base.setValue(0);
			impl.setText("");
			diceValue.setText("");
			modifierValue.setSelectedIndex(0);
			colourMagicValue.setSelectedIndex(0);
		}
		else if (e.getSource() == custom)
		{
			base.setEnabled(false);
			impl.setEnabled(true);
			diceValue.setEnabled(false);
			modifierValue.setEnabled(false);
			colourMagicValue.setEnabled(false);

			base.setValue(0);
			impl.setText("");
			diceValue.setText("");
			modifierValue.setSelectedIndex(0);
			colourMagicValue.setSelectedIndex(0);
		}
		else if (e.getSource() == dice)
		{
			base.setEnabled(false);
			impl.setEnabled(false);
			diceValue.setEnabled(true);
			modifierValue.setEnabled(false);
			colourMagicValue.setEnabled(false);

			base.setValue(0);
			impl.setText("");
			diceValue.setText("");
			modifierValue.setSelectedIndex(0);
			colourMagicValue.setSelectedIndex(0);
		}
		else if (e.getSource() == modifier)
		{
			base.setEnabled(false);
			impl.setEnabled(false);
			diceValue.setEnabled(false);
			modifierValue.setEnabled(true);
			colourMagicValue.setEnabled(false);

			base.setValue(0);
			impl.setText("");
			diceValue.setText("");
			modifierValue.setSelectedIndex(0);
			colourMagicValue.setSelectedIndex(0);
		}
		else if (e.getSource() == colourMagic)
		{
			base.setValue(0);
			base.setEnabled(false);
			impl.setEnabled(false);
			diceValue.setEnabled(false);
			modifierValue.setEnabled(false);
			colourMagicValue.setEnabled(true);

			impl.setText("");
			diceValue.setText("");
			modifierValue.setSelectedIndex(0);
			colourMagicValue.setSelectedIndex(0);
		}
	}

	/*-------------------------------------------------------------------------*/
	protected void resetReferenceOptions(Value.SCALE scale, Object ref)
	{
		switch (scale)
		{
			case NONE:
			case SCALE_WITH_CASTING_LEVEL:
			case SCALE_WITH_CHARACTER_LEVEL:
			case SCALE_WITH_PARTY_SIZE:
				reference.setModel(new DefaultComboBoxModel());
				reference.setSelectedItem(null);
				reference.setEnabled(false);
				break;
			case SCALE_WITH_CLASS_LEVEL:
				Vector<String> classes = new Vector<String>(Database.getInstance().getCharacterClasses().keySet());
				Collections.sort(classes);
				reference.setModel(new DefaultComboBoxModel(classes));
				if (ref != null)
				{
					reference.setSelectedItem(ref);
				}
				else
				{
					reference.setSelectedIndex(0);
				}
				reference.setEnabled(true);
				break;
			case SCALE_WITH_MODIFIER:
				Vector<Stats.Modifier> vec = new Vector<Stats.Modifier>(Stats.allModifiers);
				Collections.sort(vec);
				reference.setModel(new DefaultComboBoxModel(vec));
				if (ref != null)
				{
					reference.setSelectedItem(ref);
				}
				else
				{
					reference.setSelectedIndex(0);
				}
				reference.setEnabled(true);
				break;
			default: throw new MazeException("invalid "+ scale);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Value getValue()
	{
		return value;
	}

}
