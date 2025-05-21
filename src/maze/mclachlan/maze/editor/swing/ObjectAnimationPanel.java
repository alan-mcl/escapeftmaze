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
import mclachlan.maze.stat.ObjectAnimations;

/**
 *
 */
public class ObjectAnimationPanel extends EditorPanel
{
	private ObjectScriptListPanel scriptListPanel;

	/*-------------------------------------------------------------------------*/
	public ObjectAnimationPanel()
	{
		super(SwingEditor.Tab.OBJECT_ANIMATIONS);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel editControls = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		editControls.add(new JLabel("Engine Object Scripts:"), gbc);
		gbc.gridy++;
//		gbc.gridwidth = 3;
//		editControls.add(new JLabel("Events are executed in order"), gbc);

		scriptListPanel = new ObjectScriptListPanel(this.dirtyFlag);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridy++;
		gbc.gridx=0;
		gbc.gridwidth = 3;
		editControls.add(scriptListPanel, gbc);

		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getObjectAnimations().values());
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		ObjectAnimations objectAnimation = Database.getInstance().getObjectAnimation(name);
		scriptListPanel.refresh(objectAnimation.getAnimationScripts());
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		ObjectAnimations result = new ObjectAnimations(name, new ArrayList<>());

		Database.getInstance().getObjectAnimations().put(name, result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		ObjectAnimations oa = Database.getInstance().getObjectAnimations().remove(currentName);
		oa.setName(newName);
		Database.getInstance().getObjectAnimations().put(newName, oa);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		ObjectAnimations current = Database.getInstance().getObjectAnimations().get(currentName);

		ObjectAnimations newObj = new ObjectAnimations(newName, new ArrayList<>(current.getAnimationScripts()));

		Database.getInstance().getObjectAnimations().put(newName, newObj);

		return newObj;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getObjectAnimations().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		ObjectAnimations oa = Database.getInstance().getObjectAnimation(name);

		oa.setAnimationScripts(scriptListPanel.getObjectScripts());

		return oa;
	}
}
