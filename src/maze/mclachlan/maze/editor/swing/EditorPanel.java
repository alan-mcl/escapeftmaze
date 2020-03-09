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
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import mclachlan.maze.data.v1.DataObject;

/**
 *
 */
public abstract class EditorPanel
	extends JPanel
	implements ListSelectionListener, KeyListener, ActionListener,
	ChangeListener, IEditorPanel, TableModelListener, ItemListener
{
	public static final String NONE = " - ";

	protected JList names;
	protected Container editControls;
	protected String currentName;

	protected int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public EditorPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		names = new JList();

		refreshNames(null);
		names.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		names.addListSelectionListener(this);
		names.setFixedCellWidth(100);
		JScrollPane nameScroller = new JScrollPane(names);

		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		editControls = getEditControls();
		JScrollPane editControlsScroller = new JScrollPane(editControls);

		JSplitPane splitPane = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT,
			true,
			nameScroller,
			editControlsScroller);

		add(splitPane, gbc);

		initForeignKeys();
		if (currentName != null)
		{
			refresh(currentName);
		}

		splitPane.setDividerLocation(-1);
	}

	/*-------------------------------------------------------------------------*/
	public void refreshNames(String toBeSelected)
	{
		currentName = null;

		Vector<DataObject> loadedData = loadData();

		Vector<String> keys = new Vector<>();

		for (DataObject dataObject : loadedData)
		{
			if (dataObject.getCampaign() == null ||
				dataObject.getCampaign().equals(SwingEditor.instance.getCurrentCampaign()))
			{
				keys.add(dataObject.getName());
			}
		}

		Collections.sort(keys);

		names.setListData(keys);
		if (toBeSelected == null)
		{
			names.setSelectedIndex(0);
		}
		else
		{
			names.setSelectedValue(toBeSelected, true);
		}
		currentName = (String)names.getSelectedValue();
	}

	/*-------------------------------------------------------------------------*/
	protected void setEnabledAllEditControls(boolean enabled)
	{
		setEnabledAllEditControls(editControls, enabled);
	}

	/*-------------------------------------------------------------------------*/
	protected static void setEnabledAllEditControls(Container parent, boolean enabled)
	{
		Component[] components = parent.getComponents();
		for (Component comp : components)
		{
			comp.setEnabled(enabled);
			if (comp instanceof Container)
			{
				setEnabledAllEditControls((Container)comp, enabled);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void reload()
	{
		refreshNames(null);
	}

	/*-------------------------------------------------------------------------*/
	public final void valueChanged(ListSelectionEvent e)
	{
		if (currentName != null)
		{
			DataObject dataObject = commit(currentName);
			dataObject.setCampaign(SwingEditor.instance.getCurrentCampaign());;
		}

		currentName = (String)names.getSelectedValue();
		if (currentName == null)
		{
			return;
		}
		if (currentName != null)
		{
			refresh(currentName);
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getDirtyFlag()
	{
		return dirtyFlag;
	}

	/*-------------------------------------------------------------------------*/
	protected abstract Container getEditControls();

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The name of the currently selected item
	 */
	public String getCurrentName()
	{
		return (String)names.getSelectedValue();
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
	}

	/*-------------------------------------------------------------------------*/
	public void keyPressed(KeyEvent e)
	{
	}

	public void keyReleased(KeyEvent e)
	{
	}

	public void keyTyped(KeyEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
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

	/*-------------------------------------------------------------------------*/
	@Override
	public void itemStateChanged(ItemEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	/*-------------------------------------------------------------------------*/
	protected boolean isDirty()
	{
		return SwingEditor.instance.isDirty(dirtyFlag);
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
	protected GridBagConstraints createGridBagConstraints()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		return gbc;
	}

	/*-------------------------------------------------------------------------*/
	public void tableChanged(TableModelEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}
}
