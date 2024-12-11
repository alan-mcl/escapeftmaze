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
import java.util.Collections;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.maze.data.Database;
import mclachlan.maze.editor.swing.EditorPanel;
import mclachlan.maze.editor.swing.SingleTileScriptComponent;
import mclachlan.maze.editor.swing.TileScriptComponentCallback;
import mclachlan.maze.map.crusader.MouseClickScriptAdapter;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class WallDetailsPanel extends JPanel
	implements ActionListener, TileScriptComponentCallback, ChangeListener
{
	// crusader wall properties
	private final JLabel index;
	private final JCheckBox isVisible, isSolid;
	private final JComboBox<String> texture, maskTexture;
	private final SingleTileScriptComponent mouseClickScript, maskTextureMouseClickScript, internalScript;
	private final JSpinner height;
	
	// the wall being edited
	private WallProxy wall;
	private final Zone zone;

	/*-------------------------------------------------------------------------*/
	public WallDetailsPanel(boolean multiSelect, Zone zone)
	{
		setLayout(new GridBagLayout());
		
		this.zone = zone;

		GridBagConstraints gbc = createGridBagConstraints();
		
		index = new JLabel();
		dodgyGridBagShite(this, new JLabel("Index:"), index, gbc);
		
		isVisible = new JCheckBox("Visible?");
		isVisible.addActionListener(this);
		dodgyGridBagShite(this, isVisible, new JLabel(), gbc);

		isSolid = new JCheckBox("Solid?");
		isSolid.addActionListener(this);
		dodgyGridBagShite(this, isSolid, new JLabel(), gbc);

		height = new JSpinner(new SpinnerNumberModel(1, 1, 32, 1));
		height.addChangeListener(this);
		dodgyGridBagShite(this, new JLabel("Height:"), height, gbc);

		texture = new JComboBox<>();
		texture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Texture:"), texture, gbc);
		
		maskTexture = new JComboBox<>();
		maskTexture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Mask Texture:"), maskTexture, gbc);
		
		internalScript = new SingleTileScriptComponent(null, -1, this, zone);
		dodgyGridBagShite(this, new JLabel("Internal Script:"), internalScript, gbc);

		mouseClickScript = new SingleTileScriptComponent(null, -1, this, zone);
		dodgyGridBagShite(this, new JLabel("Click Script:"), mouseClickScript, gbc);
		
		maskTextureMouseClickScript = new SingleTileScriptComponent(null, -1, this, zone);
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		add(new JLabel("Mask Script:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		add(maskTextureMouseClickScript, gbc);

		initForeignKeys(multiSelect);
	}
	
	/*-------------------------------------------------------------------------*/
	public void initForeignKeys(boolean multiSelect)
	{
		Vector<String> vec = new Vector<>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(vec);
		if (multiSelect)
		{
			vec.insertElementAt(EditorPanel.NONE, 0);
		}
		texture.setModel(new DefaultComboBoxModel<>(vec));
		
		Vector<String> vec2 = new Vector<>(Database.getInstance().getMazeTextures().keySet());
		vec2.insertElementAt(EditorPanel.NONE, 0);
		Collections.sort(vec2);
		maskTexture.setModel(new DefaultComboBoxModel<>(vec2));
	}
	
	/*-------------------------------------------------------------------------*/
	public void refresh(WallProxy wall, int index, boolean horiz)
	{
		this.wall = wall;
		
		isVisible.removeActionListener(this);
		isSolid.removeActionListener(this);
		height.removeChangeListener(this);
		texture.removeActionListener(this);
		maskTexture.removeActionListener(this);
		
		if (index >= 0)
		{
			this.index.setText((horiz?"Horiz ":"Vert ")+index);
		}
		isVisible.setSelected(wall.isVisible());
		isSolid.setSelected(wall.isSolid());
		height.setValue(wall.getHeight());
		
		if (wall.isVisible())
		{
			setVisibleState(true);

			texture.setSelectedItem(wall.getTexture()==null?EditorPanel.NONE:wall.getTexture().getName());
			maskTexture.setSelectedItem(wall.getMaskTexture()==null?EditorPanel.NONE:wall.getMaskTexture().getName());

			MouseClickScriptAdapter m1 = ((MouseClickScriptAdapter)wall.getMouseClickScript());
			mouseClickScript.refresh(m1==null?null:m1.getScript(), zone);

			MouseClickScriptAdapter m2 = ((MouseClickScriptAdapter)wall.getMaskTextureMouseClickScript());
			maskTextureMouseClickScript.refresh(m2==null?null:m2.getScript(), zone);

			MouseClickScriptAdapter m3 = ((MouseClickScriptAdapter)wall.getInternalScript());
			internalScript.refresh(m3==null?null:m3.getScript(), zone);

		}
		else
		{
			setVisibleState(false);
			
			texture.setSelectedIndex(0);
			maskTexture.setSelectedIndex(0);
			internalScript.refresh(null, zone);
			mouseClickScript.refresh(null, zone);
			maskTextureMouseClickScript.refresh(null, zone);
		}
		
		isVisible.addActionListener(this);
		isSolid.addActionListener(this);
		height.addChangeListener(this);
		texture.addActionListener(this);
		maskTexture.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	private void setVisibleState(boolean b)
	{
		texture.setEnabled(b);
		maskTexture.setEnabled(b);
		internalScript.setEnabled(b);
		mouseClickScript.setEnabled(b);
		maskTextureMouseClickScript.setEnabled(b);
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
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == isVisible)
		{
			if (wall != null)
			{
				wall.setVisible(isVisible.isSelected());
			}	
			setVisibleState(isVisible.isSelected());
		}
		else if (e.getSource() == isSolid)
		{
			if (wall != null)
			{
				wall.setSolid(isSolid.isSelected());
			}
		}
		else if (e.getSource() == texture)
		{
			if (wall != null)
			{
				String s = (String)texture.getSelectedItem();
				wall.setTexture(Database.getInstance().getMazeTexture(s).getTexture());
			}
		}
		else if (e.getSource() == maskTexture)
		{
			String s = (String)maskTexture.getSelectedItem();
			
			if (wall != null)
			{
				if (EditorPanel.NONE.equals(s))
				{
					wall.setMaskTexture(null);
				}
				else
				{
					wall.setMaskTexture(Database.getInstance().getMazeTexture(s).getTexture());
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void tileScriptChanged(Component component)
	{
		if (component == mouseClickScript)
		{
			wall.setMouseClickScript(
				new MouseClickScriptAdapter(mouseClickScript.getScript()));
		}
		else if (component == maskTextureMouseClickScript)
		{
			wall.setMaskTextureMouseClickScript(
				new MouseClickScriptAdapter(maskTextureMouseClickScript.getScript()));
		}
		else if (component == internalScript)
		{
			wall.setInternalScript(new MouseClickScriptAdapter(internalScript.getScript()));
		}
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == height)
		{
			if (wall != null)
			{
				wall.setHeight((Integer)height.getValue());
			}
		}
	}
}
