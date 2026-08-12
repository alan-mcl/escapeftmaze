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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import mclachlan.crusader.Texture;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.DataObject;

/**
 *
 */
public class MazeTexturePanel extends EditorPanel
{
	private static final int IMAGE_RESOURCE_WIDTH = 320;
	private static final int IMAGE_RESOURCE_HEIGHT = 200;

	private JSpinner animationDelay, scrollSpeed;
	private JComboBox scrollBehaviour;
	private JTable imageResources;
	private JButton add, remove;
	private DefaultTableModel dataModel;
	private TexturePreviewPanel texturePreview;

	/*-------------------------------------------------------------------------*/
	public MazeTexturePanel()
	{
		super(SwingEditor.Tab.TEXTURES);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel content = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = createGridBagConstraints();
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		animationDelay = new JSpinner(new SpinnerNumberModel(-1, -1, 9999999, 1));
		animationDelay.addChangeListener(this);
		dodgyGridBagShite(content, new JLabel("Animation Delay:"), animationDelay, gbc);

		scrollBehaviour = new JComboBox(Texture.ScrollBehaviour.values());
		scrollBehaviour.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Scroll Behaviour:"), scrollBehaviour, gbc);

		scrollSpeed = new JSpinner(new SpinnerNumberModel(-1, -1, 999999, 1));
		scrollSpeed.addChangeListener(this);
		dodgyGridBagShite(content, new JLabel("Scroll Speed:"), scrollSpeed, gbc);

		dataModel = new DefaultTableModel(new String[]{"image resource"}, 0);
		imageResources = new JTable(dataModel);
		dataModel.addTableModelListener(this);
		imageResources.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add = new JButton("Add");
		add.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);

		JScrollPane imageScroller = new JScrollPane(imageResources);
		Dimension imageListSize = new Dimension(IMAGE_RESOURCE_WIDTH, IMAGE_RESOURCE_HEIGHT);
		imageScroller.setPreferredSize(imageListSize);
		imageScroller.setMinimumSize(imageListSize);
		imageScroller.setMaximumSize(imageListSize);

		JPanel imageButtons = new JPanel(new GridBagLayout());
		GridBagConstraints buttonGbc = createGridBagConstraints();
		buttonGbc.weightx = 0.0;
		buttonGbc.weighty = 0.0;
		buttonGbc.gridx = 0;
		buttonGbc.gridy = 0;
		imageButtons.add(add, buttonGbc);
		buttonGbc.gridx = 1;
		buttonGbc.weightx = 1.0;
		imageButtons.add(remove, buttonGbc);
		Dimension imageButtonsSize = new Dimension(IMAGE_RESOURCE_WIDTH, imageButtons.getPreferredSize().height);
		imageButtons.setPreferredSize(imageButtonsSize);
		imageButtons.setMaximumSize(imageButtonsSize);

		texturePreview = new TexturePreviewPanel();

		JPanel tableAndPreview = new JPanel(new GridBagLayout());
		GridBagConstraints tpGbc = new GridBagConstraints();
		tpGbc.anchor = GridBagConstraints.NORTHWEST;
		tpGbc.fill = GridBagConstraints.NONE;
		tpGbc.weightx = 0.0;
		tpGbc.weighty = 0.0;
		tpGbc.gridx = 0;
		tpGbc.gridy = 0;
		tableAndPreview.add(imageScroller, tpGbc);
		tpGbc.gridx = 1;
		tpGbc.insets = new Insets(0, 8, 0, 0);
		tableAndPreview.add(texturePreview, tpGbc);
		tpGbc.gridx = 0;
		tpGbc.gridy = 1;
		tpGbc.insets = new Insets(4, 0, 0, 0);
		tableAndPreview.add(imageButtons, tpGbc);

		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		content.add(tableAndPreview, gbc);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(content, BorderLayout.NORTH);
		return wrapper;
	}

	/*-------------------------------------------------------------------------*/
	private void refreshPreview()
	{
		if (texturePreview == null)
		{
			return;
		}

		List<String> images = new ArrayList<>();
		for (Object obj : dataModel.getDataVector())
		{
			Vector v = (Vector)obj;
			images.add((String)v.get(0));
		}

		Texture.ScrollBehaviour sb = (Texture.ScrollBehaviour)scrollBehaviour.getSelectedItem();
		texturePreview.setPreview(
			images,
			(Integer)animationDelay.getValue(),
			sb == Texture.ScrollBehaviour.NONE ? null : sb,
			(Integer)scrollSpeed.getValue());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void stateChanged(ChangeEvent e)
	{
		refreshPreview();
		super.stateChanged(e);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void tableChanged(TableModelEvent e)
	{
		refreshPreview();
		super.tableChanged(e);
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getMazeTextures().values());
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		MazeTexture mt = Database.getInstance().getMazeTexture(name);

		animationDelay.removeChangeListener(this);
		dataModel.removeTableModelListener(this);
		scrollBehaviour.removeActionListener(this);
		scrollSpeed.removeChangeListener(this);

		animationDelay.setValue(mt.getAnimationDelay());
		scrollBehaviour.setSelectedItem(
			mt.getScrollBehaviour() == null ? Texture.ScrollBehaviour.NONE : mt.getScrollBehaviour());
		scrollSpeed.setValue(mt.getScrollSpeed());
		dataModel.setRowCount(0);
		for (String s : mt.getImageResources())
		{
			dataModel.addRow(new String[]{s});
		}

		animationDelay.addChangeListener(this);
		dataModel.addTableModelListener(this);
		scrollBehaviour.addActionListener(this);
		scrollSpeed.addChangeListener(this);

		refreshPreview();
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		MazeTexture mt = new MazeTexture(
			name,
			new ArrayList<>(),
			-1,
			null,
			-1);
		Database.getInstance().getMazeTextures().put(name, mt);
		return mt;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		MazeTexture mt = Database.getInstance().getMazeTextures().remove(currentName);
		mt.setName(newName);
		Database.getInstance().getMazeTextures().put(newName, mt);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		MazeTexture current = Database.getInstance().getMazeTextures().get(currentName);
		MazeTexture mt = new MazeTexture(
			newName,
			new ArrayList<String>(current.getImageResources()),
			current.getAnimationDelay(),
			current.getScrollBehaviour(),
			current.getScrollSpeed());
		Database.getInstance().getMazeTextures().put(newName, mt);
		return mt;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getMazeTextures().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		MazeTexture mt = Database.getInstance().getMazeTexture(name);

		mt.setAnimationDelay((Integer)animationDelay.getValue());
		Texture.ScrollBehaviour sb = (Texture.ScrollBehaviour)scrollBehaviour.getSelectedItem();
		mt.setScrollBehaviour(sb == Texture.ScrollBehaviour.NONE ? null : sb);
		mt.setScrollSpeed((Integer)scrollSpeed.getValue());

		List<String> images = new ArrayList<String>();
		for (Object obj : dataModel.getDataVector())
		{
			Vector v = (Vector)obj;
			images.add((String)v.get(0));
		}
		mt.setImageResources(images);

		Database.getInstance().getMazeTextures().put(name, mt);

		return mt;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			dataModel.addRow(new String[]{""});
		}
		else if (e.getSource() == remove)
		{
			int index = imageResources.getSelectedRow();
			if (index > -1)
			{
				dataModel.removeRow(index);
			}
		}

		refreshPreview();
		super.actionPerformed(e);
	}
}
