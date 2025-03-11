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
import java.awt.Insets;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.game.DifficultyLevel;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class DifficultyLevelPanel extends EditorPanel
{
	private JTextField impl;
	private JSpinner sortOrder;

	/*-------------------------------------------------------------------------*/
	public DifficultyLevelPanel()
	{
		super(SwingEditor.Tab.DIFFICULTY_LEVELS);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getEditControls()
	{
		impl = new JTextField(30);
		impl.addActionListener(this);
		impl.addKeyListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5,5,5,5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		JPanel result = new JPanel(new GridBagLayout());

		result.add(new JLabel("Impl:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(impl, gbc);

		sortOrder = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy++;
		result.add(new JLabel("Sort Order:"), gbc);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx++;
		result.add(sortOrder, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>((Database.getInstance().getDifficultyLevels().values()));
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.DIFFICULTY_LEVELS);
		DifficultyLevel dl = new DifficultyLevel();
		dl.setName(name);
		Database.getInstance().getDifficultyLevels().put(name, dl);

		return dl;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.DIFFICULTY_LEVELS);
		DifficultyLevel current = Database.getInstance().getDifficultyLevels().get(
			(String)names.getSelectedValue());
		Database.getInstance().getDifficultyLevels().remove(current.getName());
		current.setName(newName);
		Database.getInstance().getDifficultyLevels().put(current.getName(), current);
		refreshNames(newName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.DIFFICULTY_LEVELS);
		DifficultyLevel current = Database.getInstance().getDifficultyLevels().get(
			(String)names.getSelectedValue());

		try
		{
			DifficultyLevel dl = current.getClass().newInstance();
			dl.setName(newName);
			Database.getInstance().getDifficultyLevels().put(dl.getName(), dl);

			return dl;
		}
		catch (Exception x)
		{
			throw new MazeException(x);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.DIFFICULTY_LEVELS);
		String name = (String)names.getSelectedValue();
		Database.getInstance().getDifficultyLevels().remove(name);
		refreshNames(null);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		if (name == null)
		{
			return;
		}

		DifficultyLevel dl = Database.getInstance().getDifficultyLevels().get(name);

		impl.setText(dl.getClass().getName());
		sortOrder.setValue(dl.getSortOrder());
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		// custom impls only supported
		if (name == null)
		{
			return null;
		}

		Map<String, DifficultyLevel> difficultyLevels = Database.getInstance().getDifficultyLevels();

		try
		{
			Class clazz = Class.forName(impl.getText());
			DifficultyLevel dl = (DifficultyLevel)clazz.newInstance();

			if (difficultyLevels.containsKey(name))
			{
				// carry the campaign over
				dl.setCampaign(difficultyLevels.get(name).getCampaign());
			}

			dl.setName(name);
			dl.setSortOrder((Integer)sortOrder.getValue());
			difficultyLevels.put(name, dl);

			return dl;
		}
		catch (Exception x)
		{
			throw new MazeException(x);
		}
	}
}