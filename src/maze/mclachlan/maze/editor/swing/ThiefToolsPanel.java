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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.maze.map.Trap;
import mclachlan.maze.stat.Dice;

/**
 *
 */
public class ThiefToolsPanel extends JPanel implements ActionListener, ChangeListener
{
	int[] list = new int[Trap.Tool.MAX_TOOLS];
	BitSet required = new BitSet(Trap.Tool.MAX_TOOLS);

	String[] tools = new String[]
	{
		Trap.Tool.toString(Trap.Tool.CHISEL),
		Trap.Tool.toString(Trap.Tool.CROWBAR),
		Trap.Tool.toString(Trap.Tool.DRILL),
		Trap.Tool.toString(Trap.Tool.HAMMER),
		Trap.Tool.toString(Trap.Tool.JACKKNIFE),
		Trap.Tool.toString(Trap.Tool.LOCKPICK),
		Trap.Tool.toString(Trap.Tool.SKELETON_KEY),
		Trap.Tool.toString(Trap.Tool.TENSION_WRENCH),
	};

	JCheckBox[] checkboxes = new JCheckBox[Trap.Tool.MAX_TOOLS];
	JSpinner[] difficulties = new JSpinner[Trap.Tool.MAX_TOOLS];
	JButton[] quick = new JButton[5];
	private int dirtyFlag;
	private ThiefToolsCallback callback;

	/*-------------------------------------------------------------------------*/
	public ThiefToolsPanel(String title, int dirtyFlag, ThiefToolsCallback callback)
	{
		this.dirtyFlag = dirtyFlag;
		this.callback = callback;

		for (int i = 0; i < checkboxes.length; i++)
		{
			checkboxes[i] = new JCheckBox(tools[i]);
			checkboxes[i].addActionListener(this);
		}

		for (int i = 0; i < difficulties.length; i++)
		{
			difficulties[i] = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
			difficulties[i].addChangeListener(this);
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

		this.add(new JLabel("Tool"), gbc);
		for (JCheckBox checkbox : checkboxes)
		{
			gbc.gridy++;
			this.add(checkbox, gbc);
		}

		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		this.add(new JLabel("difficulty"), gbc);
		for (JSpinner difficulty : difficulties)
		{
			gbc.gridy++;
			this.add(difficulty, gbc);
		}

		JPanel quickButtons = new JPanel();

		for (int i=0; i<5; i++)
		{
			quick[i] = new JButton(""+i);
			quick[i].addActionListener(this);
			quick[i].setActionCommand(""+i);
			quickButtons.add(quick[i]);
		}

		gbc.gridx=0;
		gbc.gridwidth=3;
		gbc.gridy++;

		this.add(quickButtons, gbc);

		this.setBorder(BorderFactory.createTitledBorder(title));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(int[] list, BitSet required)
	{
		this.list = null;
		this.required = new BitSet();

		// disable all
		for (int i = 0; i < checkboxes.length; i++)
		{
			difficulties[i].removeChangeListener(this);

			checkboxes[i].setSelected(false);
			difficulties[i].setValue(0);
			difficulties[i].setEnabled(false);

			difficulties[i].addChangeListener(this);
		}

		if (list == null)
		{
			this.list = new int[Trap.Tool.MAX_TOOLS];
		}
		else
		{
			this.list = list;
		}
		
		if (required == null)
		{
			this.required = new BitSet(Trap.Tool.MAX_TOOLS);
		}
		else
		{
			this.required = required;
		}

		if (required != null)
		{
			for (int i=0; i<this.list.length; i++)
			{
				if (required.get(i))
				{
					checkboxes[i].setSelected(true);
	
					difficulties[i].removeChangeListener(this);
	
					difficulties[i].setValue(list[i]);
					difficulties[i].setEnabled(true);
	
					difficulties[i].addChangeListener(this);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public int[] getDifficulties()
	{
		int[] result = new int[Trap.Tool.MAX_TOOLS];
		System.arraycopy(list, 0, result, 0, list.length);
		return result;
	}

	public BitSet getRequired()
	{
		return required.get(0, Trap.Tool.MAX_TOOLS);
	}

	/*-------------------------------------------------------------------------*/
	private int indexOfAmount(JSpinner spinner)
	{
		for (int i = 0; i < difficulties.length; i++)
		{
			if (difficulties[i].equals(spinner))
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
			int i = Trap.Tool.valueOf(cb.getText());

			if (cb.isSelected())
			{
				// tool is just selected now
				this.required.set(i);
				difficulties[i].setValue(0);
				difficulties[i].setEnabled(true);
			}
			else
			{
				// tool is removed
				this.required.clear(i);
				difficulties[i].setValue(0);
				difficulties[i].setEnabled(false);
			}
		}
		else if (obj instanceof JButton)
		{
			int level = Integer.parseInt(e.getActionCommand());
			quickAssignTools(level);
		}
		
		if (callback != null)
		{
			callback.thiefToolsChanged(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void quickAssignTools(int level)
	{
		refresh(null, null);

		int nrTools = Dice.d3.roll() + level;
		for (int i=0; i<nrTools; i++)
		{
			int tool;
			do
			{
				tool = Dice.d8.roll()-1;
			}
			while (required.get(tool));
			int diff = Dice.d3.roll() + level*2;

			required.set(tool);
			list[tool] = diff;

			refresh(list, required);
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
			JSpinner spinner = (JSpinner)obj;
			int i = indexOfAmount(spinner);

			list[i] = (Integer)spinner.getValue();
		}
		
		if (callback != null)
		{
			callback.thiefToolsChanged(this);
		}
	}
}
