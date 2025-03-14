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
import java.util.List;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.crusader.Texture;
import mclachlan.maze.data.Database;
import mclachlan.maze.editor.swing.EditorPanel;
import mclachlan.maze.editor.swing.SingleTileScriptComponent;
import mclachlan.maze.editor.swing.TileScriptComponentCallback;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.crusader.MouseClickScriptAdapter;

/**
 *
 */
public class WallDetailsPanel extends JPanel
	implements ActionListener, TileScriptComponentCallback, ChangeListener
{
	// crusader wall properties
	private final JLabel index;
	private final JCheckBox isVisible, isSolid;
	private final TexturesPanel texturesPanel;
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

		internalScript = new SingleTileScriptComponent(null, -1, this, zone);
		dodgyGridBagShite(this, new JLabel("Internal Script:"), internalScript, gbc);

		mouseClickScript = new SingleTileScriptComponent(null, -1, this, zone);
		dodgyGridBagShite(this, new JLabel("Click Script:"), mouseClickScript, gbc);

		maskTextureMouseClickScript = new SingleTileScriptComponent(null, -1, this, zone);
		dodgyGridBagShite(this, new JLabel("Mask Script:"), maskTextureMouseClickScript, gbc);

		texturesPanel = new TexturesPanel();

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.gridy++;
		add(texturesPanel, gbc);

		initForeignKeys(multiSelect);
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys(boolean multiSelect)
	{
		texturesPanel.initForeignKeys(multiSelect);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(WallProxy wall, int index, boolean horiz)
	{
		this.wall = wall;

		isVisible.removeActionListener(this);
		isSolid.removeActionListener(this);
		height.removeChangeListener(this);
		texturesPanel.removeListeners();

		if (index >= 0)
		{
			this.index.setText((horiz ? "Horiz " : "Vert ") + index);
		}
		isVisible.setSelected(wall.isVisible());
		isSolid.setSelected(wall.isSolid());
		height.setValue(wall.getHeight());

		texturesPanel.refresh(wall);

		if (wall.isVisible())
		{
			setVisibleState(true);

			MouseClickScriptAdapter m1 = ((MouseClickScriptAdapter)wall.getMouseClickScript());
			mouseClickScript.refresh(m1 == null ? null : m1.getScript(), zone);

			MouseClickScriptAdapter m2 = ((MouseClickScriptAdapter)wall.getMaskTextureMouseClickScript());
			maskTextureMouseClickScript.refresh(m2 == null ? null : m2.getScript(), zone);

			MouseClickScriptAdapter m3 = ((MouseClickScriptAdapter)wall.getInternalScript());
			internalScript.refresh(m3 == null ? null : m3.getScript(), zone);

		}
		else
		{
			setVisibleState(false);

			internalScript.refresh(null, zone);
			mouseClickScript.refresh(null, zone);
			maskTextureMouseClickScript.refresh(null, zone);
		}

		isVisible.addActionListener(this);
		isSolid.addActionListener(this);
		height.addChangeListener(this);
		texturesPanel.addListeners();
	}

	/*-------------------------------------------------------------------------*/
	private void setVisibleState(boolean b)
	{
		internalScript.setEnabled(b);
		mouseClickScript.setEnabled(b);
		maskTextureMouseClickScript.setEnabled(b);

		texturesPanel.setVisibleState(b);
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

	/*-------------------------------------------------------------------------*/
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

	/*-------------------------------------------------------------------------*/
	private static class TexturesPanel extends JPanel implements ActionListener
	{
		private static final int MAX = 9;
		private final java.util.List<JComboBox<String>> textures, maskTextures;
		private WallProxy wall;

		/*-------------------------------------------------------------------------*/
		public TexturesPanel()
		{
			super(new GridLayout(1, 2, 5, 5));
			textures = new ArrayList<>();
			maskTextures = new ArrayList<>();

			JPanel left = new JPanel(new GridLayout(MAX + 1, 1, 5, 5));
			left.add(new JLabel("Textures"));
			for (int i = 0; i < MAX; i++)
			{
				JComboBox<String> cb = new JComboBox<>();
				cb.addActionListener(this);
				textures.add(cb);
				left.add(cb);
			}

			JPanel right = new JPanel(new GridLayout(MAX + 1, 1, 5, 5));
			right.add(new JLabel("Mask Textures"));
			for (int i = 0; i < MAX; i++)
			{
				JComboBox<String> cb = new JComboBox<>();
				cb.addActionListener(this);
				maskTextures.add(cb);
				right.add(cb);
			}

			this.add(left);
			this.add(right);
		}

		/*-------------------------------------------------------------------------*/
		public void initForeignKeys(boolean multiSelect)
		{
			Vector<String> vec = new Vector<>(Database.getInstance().getMazeTextures().keySet());
			Collections.sort(vec);
			vec.insertElementAt(EditorPanel.NONE, 0);

			for (JComboBox<String> cb : textures)
			{
				cb.setModel(new DefaultComboBoxModel<>(vec));
			}
			for (JComboBox<String> cb : maskTextures)
			{
				cb.setModel(new DefaultComboBoxModel<>(vec));
			}
		}

		/*-------------------------------------------------------------------------*/
		@Override
		public void actionPerformed(ActionEvent e)
		{
			this.commit();
		}

		/*-------------------------------------------------------------------------*/
		public void commit()
		{
			if (this.wall != null)
			{
				List<Texture> texturesResult, maskTexturesResult;

				texturesResult = new ArrayList<>();
				maskTexturesResult = new ArrayList<>();

				// wall textures
				for (JComboBox<String> cb : textures)
				{
					if (cb.getSelectedItem() != EditorPanel.NONE)
					{
						texturesResult.add(Database.getInstance().getMazeTexture((String)cb.getSelectedItem()).getTexture());
					}
				}
				this.wall.setTextures(texturesResult.toArray(new Texture[0]));

				// mask textures
				for (JComboBox<String> cb : maskTextures)
				{
					if (cb.getSelectedItem() != EditorPanel.NONE)
					{
						maskTexturesResult.add(Database.getInstance().getMazeTexture((String)cb.getSelectedItem()).getTexture());
					}
				}
				if (maskTexturesResult.size() > 0)
				{
					this.wall.setMaskTextures(maskTexturesResult.toArray(new Texture[0]));
				}
				else
				{
					this.wall.setMaskTextures(null);
				}
			}
		}

		/*-------------------------------------------------------------------------*/
		void removeListeners()
		{
			for (int i = 0; i < MAX; i++)
			{
				textures.get(i).removeActionListener(this);
				maskTextures.get(i).removeActionListener(this);
			}
		}

		/*-------------------------------------------------------------------------*/
		void addListeners()
		{
			for (int i = 0; i < MAX; i++)
			{
				textures.get(i).addActionListener(this);
				maskTextures.get(i).addActionListener(this);
			}
		}

		/*-------------------------------------------------------------------------*/
		public void refresh(WallProxy wall)
		{
			this.wall = wall;

			if (wall != null)
			{
				for (int i = 0; i < MAX; i++)
				{
					if (wall.getTextures().length > i)
					{
						textures.get(i).setSelectedItem(wall.getTexture(i).getName());
					}
					else
					{
						textures.get(i).setSelectedItem(EditorPanel.NONE);
					}

					if (wall.getMaskTextures() != null && wall.getMaskTextures().length > i)
					{
						maskTextures.get(i).setSelectedItem(wall.getMaskTexture(i).getName());
					}
					else
					{
						maskTextures.get(i).setSelectedItem(EditorPanel.NONE);
					}
				}
			}
		}

		/*-------------------------------------------------------------------------*/
		public void setVisibleState(boolean b)
		{
			for (JComboBox<String> cb : textures)
			{
				if (!b)
				{
					cb.setSelectedItem(EditorPanel.NONE);
				}
				cb.setEnabled(b);
			}
			for (JComboBox<String> cb : maskTextures)
			{
				if (!b)
				{
					cb.setSelectedItem(EditorPanel.NONE);
				}
				cb.setEnabled(b);
			}
		}
	}
}