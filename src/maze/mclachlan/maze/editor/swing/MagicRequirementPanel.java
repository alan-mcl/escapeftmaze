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
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.ColourMagicRequirement;

/**
 *
 */
public class MagicRequirementPanel extends JPanel implements ActionListener, ChangeListener
{
	List<ColourMagicRequirement> list = new ArrayList<>();

	String[] magicColours = new String[]
	{
		MagicSys.MagicColour.describe(MagicSys.MagicColour.RED),
		MagicSys.MagicColour.describe(MagicSys.MagicColour.BLACK),
		MagicSys.MagicColour.describe(MagicSys.MagicColour.PURPLE),
		MagicSys.MagicColour.describe(MagicSys.MagicColour.GOLD),
		MagicSys.MagicColour.describe(MagicSys.MagicColour.WHITE),
		MagicSys.MagicColour.describe(MagicSys.MagicColour.GREEN),
		MagicSys.MagicColour.describe(MagicSys.MagicColour.BLUE),
	};

	JCheckBox[] checkboxes = new JCheckBox[7];
	JSpinner[] amounts = new JSpinner[7];
	private final int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public MagicRequirementPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;

		for (int i = 0; i < checkboxes.length; i++)
		{
			checkboxes[i] = new JCheckBox(magicColours[i]);
			checkboxes[i].addActionListener(this);
		}

		for (int i = 0; i < amounts.length; i++)
		{
			amounts[i] = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
			amounts[i].addChangeListener(this);
		}

		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;

		this.add(new JLabel("Magic Colour"), gbc);
		for (int i = 0; i < checkboxes.length; i++)
		{
			gbc.gridy++;
			this.add(checkboxes[i], gbc);
		}

		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		this.add(new JLabel("amount"), gbc);
		for (int i = 0; i < amounts.length; i++)
		{
			gbc.gridy++;
			this.add(amounts[i], gbc);
		}

		this.setBorder(BorderFactory.createTitledBorder("Magic Colour Requirements"));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(java.util.List<ColourMagicRequirement> list)
	{
		this.list = null;

		// disable all
		for (int i = 0; i < checkboxes.length; i++)
		{
			amounts[i].removeChangeListener(this);

			checkboxes[i].setSelected(false);
			amounts[i].setValue(0);
			amounts[i].setEnabled(false);

			amounts[i].addChangeListener(this);
		}

		if (list == null)
		{
			this.list = new ArrayList<>();
		}
		else
		{
			this.list = list;
		}

		for (ColourMagicRequirement mr : this.list)
		{
			int i = mr.getColour();
			checkboxes[i].setSelected(true);

			amounts[i].removeChangeListener(this);

			amounts[i].setValue(mr.getAmount());
			amounts[i].setEnabled(true);

			amounts[i].addChangeListener(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<ColourMagicRequirement> getColourMagicRequirements()
	{
		return new ArrayList<>(this.list);
	}

	/*-------------------------------------------------------------------------*/
	private int indexOfAmount(JSpinner spinner)
	{
		for (int i = 0; i < amounts.length; i++)
		{
			if (amounts[i].equals(spinner))
			{
				return i;
			}
		}
		return -1;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);

		Object obj = e.getSource();
		if (obj instanceof JCheckBox)
		{
			JCheckBox cb = (JCheckBox)obj;
			int i = MagicSys.MagicColour.valueOf(cb.getText());

			if (cb.isSelected())
			{
				// spell book is just selected now
				this.list.add(new ColourMagicRequirement(i, 0));
				amounts[i].setValue(0);
				amounts[i].setEnabled(true);
			}
			else
			{
				// spell book is removed
				for (ColourMagicRequirement mr : list)
				{
					if (mr.getColour() == MagicSys.MagicColour.valueOf(cb.getText()))
					{
						list.remove(mr);
						break;
					}
				}
				amounts[i].setValue(0);
				amounts[i].setEnabled(false);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void stateChanged(ChangeEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);

		if (this.list == null)
		{
			return;
		}

		Object obj = e.getSource();
		if (obj instanceof JSpinner)
		{
			// stupid code!
			JSpinner spinner = (JSpinner)obj;
			int i = indexOfAmount(spinner);

			for (ColourMagicRequirement mr : list)
			{
				if (mr.getColour() == i)
				{
					mr.setAmount((Integer)(spinner.getValue()));
					break;
				}
			}
		}
	}
}
