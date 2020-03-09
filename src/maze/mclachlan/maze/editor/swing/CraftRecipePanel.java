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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.CraftRecipe;
import mclachlan.maze.stat.StatModifier;

/**
 *
 */
public class CraftRecipePanel extends EditorPanel
{
	private JComboBox item1, item2, resultingItem;
	private StatModifierComponent requirements;

	/*-------------------------------------------------------------------------*/
	public CraftRecipePanel()
	{
		super(SwingEditor.Tab.CRAFT_RECIPES);
	}

	/*-------------------------------------------------------------------------*/
	protected Container getEditControls()
	{
		JPanel panel = new JPanel();

		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		item1 = new JComboBox();
		item1.addActionListener(this);
		dodgyGridBagShite(panel, new JLabel("Item 1:"), item1, gbc);

		item2 = new JComboBox();
		item2.addActionListener(this);
		dodgyGridBagShite(panel, new JLabel("Item 2:"), item2, gbc);

		resultingItem = new JComboBox();
		resultingItem.addActionListener(this);
		dodgyGridBagShite(panel, new JLabel("Resulting Item:"), resultingItem, gbc);

		requirements = new StatModifierComponent(dirtyFlag);
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		panel.add(new JLabel("Requirements:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		panel.add(requirements, gbc);

		return panel;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getCraftRecipes().values());
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		CraftRecipe cr = Database.getInstance().getCraftRecipes().get(name);

		item1.removeActionListener(this);
		item2.removeActionListener(this);
		resultingItem.removeActionListener(this);

		requirements.refresh(cr.getRequirements());
		item1.setSelectedItem(cr.getItem1());
		item2.setSelectedItem(cr.getItem2());
		resultingItem.setSelectedItem(cr.getResultingItem());

		item1.addActionListener(this);
		item2.addActionListener(this);
		resultingItem.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		CraftRecipe cr = new CraftRecipe(
			name,
			new StatModifier(),
			(String)item1.getItemAt(0),
			(String)item1.getItemAt(0),
			(String)item1.getItemAt(0));

		Database.getInstance().getCraftRecipes().put(name, cr);

		return cr;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		CraftRecipe cr = Database.getInstance().getCraftRecipes().remove(currentName);
		cr.setName(newName);
		Database.getInstance().getCraftRecipes().put(newName, cr);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		CraftRecipe current = Database.getInstance().getCraftRecipes().get(currentName);

		CraftRecipe cr = new CraftRecipe(
			newName,
			new StatModifier(current.getRequirements()),
			current.getItem1(),
			current.getItem2(),
			current.getResultingItem());

		Database.getInstance().getCraftRecipes().put(newName, cr);

		return cr;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getCraftRecipes().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		CraftRecipe cr = Database.getInstance().getCraftRecipes().get(currentName);

		cr.setRequirements(requirements.getModifier());
		cr.setItem1((String)item1.getSelectedItem());
		cr.setItem2((String)item2.getSelectedItem());
		cr.setResultingItem((String)resultingItem.getSelectedItem());

		return cr;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector items = new Vector(Database.getInstance().getItemTemplates().keySet());
		Collections.sort(items);
		item1.setModel(new DefaultComboBoxModel(items));
		item2.setModel(new DefaultComboBoxModel(items));
		resultingItem.setModel(new DefaultComboBoxModel(items));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(CraftRecipe cr)
	{
		item1.removeActionListener(this);
		item2.removeActionListener(this);
		resultingItem.removeActionListener(this);

		item1.setSelectedItem(cr.getItem1());
		item2.setSelectedItem(cr.getItem2());
		resultingItem.setSelectedItem(cr.getResultingItem());
		requirements.refresh(cr.getRequirements());

		item1.addActionListener(this);
		item2.addActionListener(this);
		resultingItem.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	protected void dodgyGridBagShite(JPanel panel, Component a, Component b, GridBagConstraints gbc)
	{
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx=0;
		gbc.gridy++;
		panel.add(a, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		panel.add(b, gbc);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	/*-------------------------------------------------------------------------*/
	public void stateChanged(ChangeEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}
}
