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
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import mclachlan.maze.stat.StartingSpellBook;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class AvailableSpellBookPanel extends JPanel implements ActionListener, ChangeListener
{
	java.util.List<StartingSpellBook> list;

	MagicSys.SpellBook[] books = new MagicSys.SpellBook[]
	{
		MagicSys.SpellBook.RED_MAGIC,
		MagicSys.SpellBook.BLACK_MAGIC,
		MagicSys.SpellBook.PURPLE_MAGIC,
		MagicSys.SpellBook.GOLD_MAGIC,
		MagicSys.SpellBook.WHITE_MAGIC,
		MagicSys.SpellBook.GREEN_MAGIC,
		MagicSys.SpellBook.BLUE_MAGIC
	};

	JCheckBox[] checkboxes = new JCheckBox[7];
	JSpinner[] maxes = new JSpinner[7];
	JSpinner[] offsets = new JSpinner[7];
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public AvailableSpellBookPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		list = null;

		for (int i = 0; i < checkboxes.length; i++)
		{
			checkboxes[i] = new JCheckBox(books[i].getName());
			checkboxes[i].addActionListener(this);
		}

		for (int i = 0; i < maxes.length; i++)
		{
			maxes[i] = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
			maxes[i].addChangeListener(this);
		}

		for (int i = 0; i < offsets.length; i++)
		{
			offsets[i] = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
			offsets[i].addChangeListener(this);
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

		this.add(new JLabel("Spell Book"), gbc);
		for (int i = 0; i < checkboxes.length; i++)
		{
			gbc.gridy++;
			this.add(checkboxes[i], gbc);
		}

		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		this.add(new JLabel("max"), gbc);
		for (int i = 0; i < maxes.length; i++)
		{
			gbc.gridy++;
			this.add(maxes[i], gbc);
		}

		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		this.add(new JLabel("offset"), gbc);
		for (int i = 0; i < offsets.length; i++)
		{
			gbc.gridy++;
			this.add(offsets[i], gbc);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(java.util.List<StartingSpellBook> list)
	{
		this.list = null;

		// disable all
		for (int i = 0; i < checkboxes.length; i++)
		{
			maxes[i].removeChangeListener(this);
			offsets[i].removeChangeListener(this);

			checkboxes[i].setSelected(false);
			maxes[i].setValue(0);
			maxes[i].setEnabled(false);
			offsets[i].setValue(0);
			offsets[i].setEnabled(false);

			maxes[i].addChangeListener(this);
			offsets[i].addChangeListener(this);
		}

		if (list == null)
		{
			this.list = new ArrayList<StartingSpellBook>();
		}
		else
		{
			this.list = list;
		}

		for (StartingSpellBook ssb : this.list)
		{
			int i = indexOf(ssb.getSpellBook());
			checkboxes[i].setSelected(true);

			maxes[i].removeChangeListener(this);
			offsets[i].removeChangeListener(this);

			maxes[i].setValue(ssb.getMaxLevel());
			maxes[i].setEnabled(true);
			offsets[i].setValue(ssb.getLevelOffset());
			offsets[i].setEnabled(true);

			maxes[i].addChangeListener(this);
			offsets[i].addChangeListener(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	private int indexOf(MagicSys.SpellBook book)
	{
		for (int i = 0; i < books.length; i++)
		{
			if (books[i].equals(book))
			{
				return i;
			}
		}
		throw new MazeException("Invalid spellbook: "+book);
	}

	/*-------------------------------------------------------------------------*/
	private int indexOfMax(JSpinner spinner)
	{
		for (int i = 0; i < maxes.length; i++)
		{
			if (maxes[i].equals(spinner))
			{
				return i;
			}
		}
		return -1;
	}

	/*-------------------------------------------------------------------------*/
	private int indexOfOffset(JSpinner spinner)
	{
		for (int i = 0; i < offsets.length; i++)
		{
			if (offsets[i].equals(spinner))
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
			MagicSys.SpellBook book = MagicSys.SpellBook.valueOf(cb.getText());
			int i = indexOf(book);

			if (cb.isSelected())
			{
				// spell book is just selected now
				this.list.add(new StartingSpellBook(book, 0, 0));
				maxes[i].setValue(0);
				maxes[i].setEnabled(true);
				offsets[i].setValue(0);
				offsets[i].setEnabled(true);
			}
			else
			{
				// spell book is removed
				for (StartingSpellBook ssb : list)
				{
					if (ssb.getSpellBook().getName().equals(cb.getText()))
					{
						list.remove(ssb);
						break;
					}
				}
				maxes[i].setValue(0);
				maxes[i].setEnabled(false);
				offsets[i].setValue(0);
				offsets[i].setEnabled(false);
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
			boolean isMax = false;
			int i = indexOfMax(spinner);
			if (i > -1)
			{
				isMax = true;
			}
			else
			{
				i = indexOfOffset(spinner);
				isMax = false;
			}

			for (StartingSpellBook ssb : list)
			{
				if (ssb.getSpellBook().getName().equals(books[i].getName()))
				{
					if (isMax)
					{
						ssb.setMaxLevel((Integer)(spinner.getValue()));
					}
					else
					{
						ssb.setLevelOffset((Integer)(spinner.getValue()));
					}
					break;
				}
			}
		}
	}
}
