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

package mclachlan.maze.editor.swing.map;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.crusader.EngineObject;
import mclachlan.maze.data.Database;
import mclachlan.maze.editor.swing.EditorPanel;
import mclachlan.maze.editor.swing.SingleTileScriptComponent;
import mclachlan.maze.editor.swing.TileScriptComponentCallback;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.crusader.MouseClickScriptAdapter;

/**
 *
 */
public class ObjectDetailsPanel extends JPanel
	implements ActionListener, TileScriptComponentCallback, ChangeListener
{
	// crusader wall properties
	private final JTextField objectName;
	private final JComboBox northTexture, southTexture, eastTexture, westTexture;
	private final JCheckBox isLightSource;
	private final SingleTileScriptComponent mouseClickScript;
	private final JButton quickObjectTexture;
	private final JComboBox<EngineObject.Alignment> verticalAlignment;

	// the object(s) being edited
	private ObjectProxy object;
	private final Zone zone;

	/*-------------------------------------------------------------------------*/
	public ObjectDetailsPanel(boolean multiSelect, Zone zone)
	{
		setLayout(new GridBagLayout());

		this.zone = zone;

		GridBagConstraints gbc = createGridBagConstraints();

		objectName = new JTextField(20);
		objectName.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Object Name:"), objectName, gbc);

		Vector<String> textures = new Vector<>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(textures);

		quickObjectTexture = new JButton("North Texture:");
		quickObjectTexture.addActionListener(this);
		quickObjectTexture.setToolTipText("Assign other textures to this.");

		northTexture = new JComboBox(textures);
		northTexture.addActionListener(this);
		dodgyGridBagShite(this, quickObjectTexture, northTexture, gbc);

		southTexture = new JComboBox(textures);
		southTexture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("South Texture:"), southTexture, gbc);

		eastTexture = new JComboBox(textures);
		eastTexture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("East Texture:"), eastTexture, gbc);

		westTexture = new JComboBox(textures);
		westTexture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("West Texture:"), westTexture, gbc);

		verticalAlignment = new JComboBox<>(EngineObject.Alignment.values());
		verticalAlignment.setSelectedItem(EngineObject.Alignment.BOTTOM);
		verticalAlignment.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Vertical Alignment:"), verticalAlignment, gbc);

		isLightSource = new JCheckBox("Light Source?");
		isLightSource.addActionListener(this);
		dodgyGridBagShite(this, isLightSource, new JLabel(), gbc);

		mouseClickScript = new SingleTileScriptComponent(null, -1, this, zone);
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx = 0;
		gbc.gridy++;
		add(new JLabel("Click Script:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		add(mouseClickScript, gbc);

		initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(vec);
		vec.insertElementAt(EditorPanel.NONE, 0);

		northTexture.setModel(new DefaultComboBoxModel<>(vec));
		southTexture.setModel(new DefaultComboBoxModel<>(vec));
		eastTexture.setModel(new DefaultComboBoxModel<>(vec));
		westTexture.setModel(new DefaultComboBoxModel<>(vec));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(ObjectProxy obj)
	{
		this.object = obj;

		objectName.removeActionListener(this);
		northTexture.removeActionListener(this);
		southTexture.removeActionListener(this);
		eastTexture.removeActionListener(this);
		westTexture.removeActionListener(this);
		verticalAlignment.removeActionListener(this);
		isLightSource.removeActionListener(this);

		if (obj.getName() != null)
		{
			objectName.setText(obj.getName());
		}
		else
		{
			objectName.setText("");
		}
		northTexture.setSelectedItem(obj.getNorthTexture() == null ? EditorPanel.NONE : obj.getNorthTexture().getName());
		southTexture.setSelectedItem(obj.getSouthTexture() == null ? EditorPanel.NONE : obj.getSouthTexture().getName());
		eastTexture.setSelectedItem(obj.getEastTexture() == null ? EditorPanel.NONE : obj.getEastTexture().getName());
		westTexture.setSelectedItem(obj.getWestTexture() == null ? EditorPanel.NONE : obj.getWestTexture().getName());
		verticalAlignment.setSelectedItem(obj.getVerticalAlignment());
		isLightSource.setSelected(obj.isLightSource());
		BitSet mask = new BitSet(); // todo remove
		MouseClickScriptAdapter m = ((MouseClickScriptAdapter)obj.getMouseClickScript());
		mouseClickScript.refresh(m==null?null:m.getScript(), zone);
		if (mask == null)
		{
			mask = new BitSet();
			mask.set(EngineObject.Placement.CENTER);
		}

		objectName.addActionListener(this);
		northTexture.addActionListener(this);
		southTexture.addActionListener(this);
		eastTexture.addActionListener(this);
		westTexture.addActionListener(this);
		verticalAlignment.addActionListener(this);
		isLightSource.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	protected void dodgyGridBagShite(JPanel panel, Component a, Component b,
		GridBagConstraints gbc)
	{
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
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
		gbc.insets = new Insets(2, 2, 2, 2);
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
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == northTexture)
		{
			object.setNorthTexture(
				Database.getInstance().getMazeTexture((String)northTexture.getSelectedItem()).getTexture());
		}
		else if (e.getSource() == southTexture)
		{
			object.setSouthTexture(
				Database.getInstance().getMazeTexture((String)southTexture.getSelectedItem()).getTexture());
		}
		else if (e.getSource() == eastTexture)
		{
			object.setEastTexture(
				Database.getInstance().getMazeTexture((String)eastTexture.getSelectedItem()).getTexture());
		}
		else if (e.getSource() == westTexture)
		{
			object.setWestTexture(
				Database.getInstance().getMazeTexture((String)westTexture.getSelectedItem()).getTexture());
		}
		else if (e.getSource() == verticalAlignment)
		{
			object.setVerticalAlignment((EngineObject.Alignment)verticalAlignment.getSelectedItem());
		}
		else if (e.getSource() == isLightSource)
		{
			object.setLightSource(isLightSource.isSelected());
		}
		else if (e.getSource() == quickObjectTexture)
		{
			southTexture.setSelectedItem(northTexture.getSelectedItem());
			eastTexture.setSelectedItem(northTexture.getSelectedItem());
			westTexture.setSelectedItem(northTexture.getSelectedItem());

			object.setSouthTexture(
				Database.getInstance().getMazeTexture((String)southTexture.getSelectedItem()).getTexture());
			object.setEastTexture(
				Database.getInstance().getMazeTexture((String)eastTexture.getSelectedItem()).getTexture());
			object.setWestTexture(
				Database.getInstance().getMazeTexture((String)westTexture.getSelectedItem()).getTexture());
		}
		else if (e.getSource() == objectName)
		{
			if (objectName.getText().length() > 0)
			{
				object.setName(objectName.getText());
			}
			else
			{
				object.setName(null);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void tileScriptChanged(Component component)
	{
		if (component == mouseClickScript)
		{
			object.setMouseClickScript(
				new MouseClickScriptAdapter(mouseClickScript.getScript()));
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void stateChanged(ChangeEvent e)
	{
	}
}