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
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;

/**
 *
 */
public class MazeTexturePanel extends EditorPanel
{
	JSpinner imageWidth, imageHeight, animationDelay;
	JTable imageResources;
	JButton add, remove;
	private DefaultTableModel dataModel;

	/*-------------------------------------------------------------------------*/
	public MazeTexturePanel()
	{
		super(SwingEditor.Tab.TEXTURES);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel result = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = createGridBagConstraints();

		imageWidth = new JSpinner(new SpinnerNumberModel(-1, -1, 9999999, 1));
		imageWidth.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Image Width:"), imageWidth, gbc);

		imageHeight = new JSpinner(new SpinnerNumberModel(-1, -1, 9999999, 1));
		imageHeight.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Image Height:"), imageHeight, gbc);

		animationDelay = new JSpinner(new SpinnerNumberModel(-1, -1, 9999999, 1));
		animationDelay.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Animation Delay:"), animationDelay, gbc);

		dataModel = new DefaultTableModel(new String[]{"image resource"}, 0);
		imageResources = new JTable(dataModel);
		dataModel.addTableModelListener(this);
		imageResources.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth=2;
		result.add(new JScrollPane(imageResources), gbc);

		add = new JButton("Add");
		add.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);

		gbc.gridwidth=1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(add, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(remove, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector vec = new Vector(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		MazeTexture mt = Database.getInstance().getMazeTexture(name);

		imageHeight.removeChangeListener(this);
		imageWidth.removeChangeListener(this);
		animationDelay.removeChangeListener(this);
		dataModel.removeTableModelListener(this);

		imageHeight.setValue(mt.getImageHeight());
		imageWidth.setValue(mt.getImageWidth());
		animationDelay.setValue(mt.getAnimationDelay());
		dataModel.setRowCount(0);
		for (String s : mt.getImageResources())
		{
			dataModel.addRow(new String[]{s});
		}

		imageHeight.addChangeListener(this);
		imageWidth.addChangeListener(this);
		animationDelay.addChangeListener(this);
		dataModel.addTableModelListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		MazeTexture mt = new MazeTexture(name, new ArrayList<String>(), 0, 0, -1);
		Database.getInstance().getMazeTextures().put(name, mt);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		MazeTexture mt = Database.getInstance().getMazeTextures().remove(currentName);
		mt.setName(newName);
		Database.getInstance().getMazeTextures().put(newName, mt);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		MazeTexture current = Database.getInstance().getMazeTextures().get(currentName);
		MazeTexture mt = new MazeTexture(
			newName,
			new ArrayList<String>(current.getImageResources()),
			current.getImageWidth(),
			current.getImageHeight(),
			current.getAnimationDelay());
		Database.getInstance().getMazeTextures().put(newName, mt);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getMazeTextures().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		MazeTexture mt = Database.getInstance().getMazeTexture(name);

		mt.setAnimationDelay((Integer)animationDelay.getValue());
		mt.setImageHeight((Integer)imageHeight.getValue());
		mt.setImageWidth((Integer)imageWidth.getValue());
		List<String> images = new ArrayList<String>();
		for (Object obj : dataModel.getDataVector())
		{
			Vector v = (Vector)obj;
			images.add((String)v.get(0));
		}
		mt.setImageResources(images);

		Database.getInstance().getMazeTextures().put(name, mt);
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

		super.actionPerformed(e);
	}
}
