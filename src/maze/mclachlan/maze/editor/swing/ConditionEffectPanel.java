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
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ConditionEffectPanel extends EditorPanel
{
	JTextField impl;

	/*-------------------------------------------------------------------------*/
	public ConditionEffectPanel()
	{
		super(SwingEditor.Tab.CONDITION_EFFECTS);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getEditControls()
	{
		impl = new JTextField(30);
		impl.addActionListener(this);
		impl.addKeyListener(this);

		JPanel editControls = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5,5,5,5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		editControls.add(new JLabel("Impl:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(impl, gbc);
		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getConditionEffects().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		// todo
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		// todo
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		// todo
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		// todo
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		if (name == null)
		{
			return;
		}

		ConditionEffect ce = Database.getInstance().getConditionEffect(name);

		impl.setText(ce.getClass().getName());
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		// custom impls only supported
		if (name == null)
		{
			return;
		}

		Map<String, ConditionEffect> conditionEffects = Database.getInstance().getConditionEffects();

		try
		{
			Class clazz = Class.forName(impl.getText());
			ConditionEffect ce = (ConditionEffect)clazz.newInstance();
			ce.setName(name);
			conditionEffects.put(name, ce);
		}
		catch (Exception x)
		{
			throw new MazeException(x);
		}
	}
}
