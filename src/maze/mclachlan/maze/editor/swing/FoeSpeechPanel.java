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
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.npc.FoeSpeech;
import mclachlan.maze.stat.npc.NpcSpeech;

/**
 *
 */
public class FoeSpeechPanel extends EditorPanel
{
	private JTextField friendlyGreeting, neutralGreeting, friendlyFarewell, neutralFarewell;
	private NpcSpeechPanel dialog;

	/*-------------------------------------------------------------------------*/
	public FoeSpeechPanel()
	{
		super(SwingEditor.Tab.FOE_SPEECH);
	}

	/*-------------------------------------------------------------------------*/
	public Container getEditControls()
	{
		JTabbedPane tabs = new JTabbedPane();

		tabs.add("Interactions", getInteractionsPanel());
		tabs.add("Dialog", getDialogPanel());

		return tabs;
	}

	/*-------------------------------------------------------------------------*/
	private Component getDialogPanel()
	{
		dialog = new NpcSpeechPanel(dirtyFlag);
		return dialog;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getInteractionsPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		neutralGreeting = new JTextField(50);
		neutralGreeting.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Neutral Greeting:"), neutralGreeting, gbc);

		friendlyGreeting = new JTextField(50);
		friendlyGreeting.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Friendly Greeting:"), friendlyGreeting, gbc);

		neutralFarewell = new JTextField(50);
		neutralFarewell.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Neutral Farewell:"), neutralFarewell, gbc);

		friendlyFarewell = new JTextField(50);
		friendlyFarewell.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Friendly Farewell:"), friendlyFarewell, gbc);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		result.add(new JLabel(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getFoeSpeeches().values());
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		FoeSpeech ft = Database.getInstance().getFoeSpeech(name);

		neutralGreeting.removeKeyListener(this);
		friendlyGreeting.removeKeyListener(this);
		neutralFarewell.removeKeyListener(this);
		friendlyFarewell.removeKeyListener(this);

		neutralGreeting.setText(ft.getNeutralGreeting() == null ? "" : ft.getNeutralGreeting());
		friendlyGreeting.setText(ft.getFriendlyGreeting() == null ? "" : ft.getFriendlyGreeting());
		neutralFarewell.setText(ft.getNeutralFarewell() == null ? "" : ft.getNeutralFarewell());
		friendlyFarewell.setText(ft.getFriendlyFarewell() == null ? "" : ft.getFriendlyFarewell());
		dialog.refresh(ft.getDialog());

		neutralGreeting.addKeyListener(this);
		friendlyGreeting.addKeyListener(this);
		neutralFarewell.addKeyListener(this);
		friendlyFarewell.addKeyListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		FoeSpeech foeSpeech = new FoeSpeech();
		foeSpeech.setName(name);

		Database.getInstance().getFoeSpeeches().put(name, foeSpeech);

		return foeSpeech;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		FoeSpeech ft = Database.getInstance().getFoeSpeeches().remove(currentName);
		ft.setName(newName);
		Database.getInstance().getFoeSpeeches().put(newName, ft);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		FoeSpeech current = Database.getInstance().getFoeSpeech(currentName);

		FoeSpeech ft = new FoeSpeech(
			newName,
			current.getFriendlyGreeting(),
			current.getNeutralGreeting(),
			current.getFriendlyFarewell(),
			current.getNeutralFarewell(),
			new NpcSpeech(current.getDialog()));

		Database.getInstance().getFoeSpeeches().put(newName, ft);

		return ft;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getFoeSpeeches().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		FoeSpeech ft = Database.getInstance().getFoeSpeech(name);

		ft.setNeutralGreeting("".equals(neutralGreeting.getText()) ? null : neutralGreeting.getText());
		ft.setFriendlyGreeting("".equals(friendlyGreeting.getText()) ? null : friendlyGreeting.getText());
		ft.setNeutralFarewell("".equals(neutralFarewell.getText()) ? null : neutralFarewell.getText());
		ft.setFriendlyFarewell("".equals(friendlyFarewell.getText()) ? null : friendlyFarewell.getText());

		ft.setDialog(dialog.getDialogue());

		return ft;
	}
}
