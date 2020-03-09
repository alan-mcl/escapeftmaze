/*
 * Copyright (c) 2012 Alan McLachlan
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
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.Personality;
import mclachlan.maze.ui.diygui.Constants;

public class PersonalitiesPanel extends EditorPanel
{
	private JButton colourButton;
	private JTextArea desc;
	private PersonalitySpeechPanel speechPanel;

	/*-------------------------------------------------------------------------*/
	public PersonalitiesPanel()
	{
		super(SwingEditor.Tab.PERSONALITIES);
	}

	/*-------------------------------------------------------------------------*/
	protected Container getEditControls()
	{
		JPanel result = new JPanel(new BorderLayout());

		desc = new JTextArea(5, 30);
		desc.addKeyListener(this);
		desc.setWrapStyleWord(true);
		desc.setLineWrap(true);

		JPanel top = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		dodgyGridBagShite(top, new JLabel("Description:"), new JScrollPane(desc), gbc);

		colourButton = new JButton(" ... ");
		colourButton.addActionListener(this);
		dodgyGridBagShite(top, new JLabel("Colour:"), colourButton, gbc);

		speechPanel = new PersonalitySpeechPanel(this.dirtyFlag);

		result.add(top, BorderLayout.NORTH);
		result.add(speechPanel, BorderLayout.CENTER);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getPersonalities().values());
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		Personality p = Database.getInstance().getPersonalities().get(name);

		desc.removeKeyListener(this);

		desc.setText(p.getDescription());
		speechPanel.refresh(p);
		colourButton.setBackground(p.getColour());

		desc.addKeyListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		Personality p = new Personality(name, "", new HashMap<String, String>(), Constants.Colour.LIGHT_BLUE);
		Database.getInstance().getPersonalities().put(name, p);
		return p;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		Personality p = Database.getInstance().getPersonalities().remove(currentName);
		p.setName(newName);
		Database.getInstance().getPersonalities().put(newName, p);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		Personality current = Database.getInstance().getPersonalities().get(currentName);

		Personality p = new Personality(
			newName,
			current.getDescription(),
			new HashMap<String, String>(current.getSpeech()),
			current.getColour());

		Database.getInstance().getPersonalities().put(newName, p);

		return p;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getPersonalities().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		Personality p = Database.getInstance().getPersonalities().get(currentName);

		p.setDescription(desc.getText());
		p.setSpeech(speechPanel.getSpeech());
		p.setColour(colourButton.getBackground());

		return p;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == colourButton)
		{
			Personality current = Database.getInstance().getPersonalities().get(currentName);

			Color c = JColorChooser.showDialog(
				SwingEditor.instance,
				"Choose Colour",
				current.getColour());

			if (c != null)
			{
				colourButton.setBackground(c);
			}
			
			SwingEditor.instance.setDirty(dirtyFlag);
		}
	}
}
