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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.npc.FoeInteraction;
import mclachlan.maze.stat.npc.NpcSpeech;
import mclachlan.maze.util.NpcSpeechCsv;

/**
 *
 */
public class FoeInteractionPanel extends EditorPanel
{
	private MazeEventsComponent neutralGreeting, friendlyGreeting, neutralFarewell, friendlyFarewell,
		attacksParty, givenItemScript;
	private JComboBox givenItemName;
	private NpcSpeechPanel dialog;

	/*-------------------------------------------------------------------------*/
	public FoeInteractionPanel()
	{
		super(SwingEditor.Tab.FOE_INTERACTION);
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
		dialog = new NpcSpeechPanel(
			dirtyFlag,
			NpcSpeechCsv.OWNER_TYPE_FOE_INTERACTION,
			this::getCurrentName);
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

		neutralGreeting = new MazeEventsComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Neutral Greeting:"), neutralGreeting, gbc);

		friendlyGreeting = new MazeEventsComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Friendly Greeting:"), friendlyGreeting, gbc);

		neutralFarewell = new MazeEventsComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Neutral Farewell:"), neutralFarewell, gbc);

		friendlyFarewell = new MazeEventsComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Friendly Farewell:"), friendlyFarewell, gbc);

		attacksParty = new MazeEventsComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Attacks Party:"), attacksParty, gbc);

		givenItemName = new JComboBox();
		givenItemName.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Given Item Name:"), givenItemName, gbc);

		givenItemScript = new MazeEventsComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Given Item Script:"), givenItemScript, gbc);

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
		return new Vector<>(Database.getInstance().getFoeInteractions().values());
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> items = new Vector<>(Database.getInstance().getItemTemplates().keySet());
		Collections.sort(items);
		items.add(0, NONE);
		givenItemName.setModel(new DefaultComboBoxModel<>(items));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		FoeInteraction ft = Database.getInstance().getFoeInteraction(name);

		givenItemName.removeActionListener(this);
		givenItemName.setSelectedItem(ft.getGivenItemName() == null ? NONE : ft.getGivenItemName());
		givenItemName.addActionListener(this);

		refreshScript(neutralGreeting, ft.getNeutralGreeting());
		refreshScript(friendlyGreeting, ft.getFriendlyGreeting());
		refreshScript(neutralFarewell, ft.getNeutralFarewell());
		refreshScript(friendlyFarewell, ft.getFriendlyFarewell());
		refreshScript(attacksParty, ft.getAttacksParty());
		refreshScript(givenItemScript, ft.getGivenItemScript());
		dialog.refresh(ft.getDialog());
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		FoeInteraction foeInteraction = new FoeInteraction();
		foeInteraction.setName(name);

		Database.getInstance().getFoeInteractions().put(name, foeInteraction);

		return foeInteraction;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		FoeInteraction ft = Database.getInstance().getFoeInteractions().remove(currentName);
		ft.setName(newName);
		Database.getInstance().getFoeInteractions().put(newName, ft);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		FoeInteraction current = Database.getInstance().getFoeInteraction(currentName);

		FoeInteraction ft = new FoeInteraction(
			newName,
			copyScript(current.getFriendlyGreeting(), newName + ".friendlyGreeting"),
			copyScript(current.getNeutralGreeting(), newName + ".neutralGreeting"),
			copyScript(current.getFriendlyFarewell(), newName + ".friendlyFarewell"),
			copyScript(current.getNeutralFarewell(), newName + ".neutralFarewell"),
			copyScript(current.getAttacksParty(), newName + ".attacksParty"),
			current.getGivenItemName(),
			copyScript(current.getGivenItemScript(), newName + ".givenItemScript"),
			current.getDialog() == null ? null : new NpcSpeech(current.getDialog()));

		Database.getInstance().getFoeInteractions().put(newName, ft);

		return ft;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getFoeInteractions().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		FoeInteraction ft = Database.getInstance().getFoeInteraction(name);

		ft.setNeutralGreeting(makeScript(name, "neutralGreeting", neutralGreeting.getEvents()));
		ft.setFriendlyGreeting(makeScript(name, "friendlyGreeting", friendlyGreeting.getEvents()));
		ft.setNeutralFarewell(makeScript(name, "neutralFarewell", neutralFarewell.getEvents()));
		ft.setFriendlyFarewell(makeScript(name, "friendlyFarewell", friendlyFarewell.getEvents()));
		ft.setAttacksParty(makeScript(name, "attacksParty", attacksParty.getEvents()));
		ft.setGivenItemName(NONE.equals(givenItemName.getSelectedItem())
			? null
			: (String)givenItemName.getSelectedItem());
		ft.setGivenItemScript(makeScript(name, "givenItemScript", givenItemScript.getEvents()));
		ft.setDialog(dialog.getDialogue());

		return ft;
	}

	/*-------------------------------------------------------------------------*/
	private void refreshScript(MazeEventsComponent component, MazeScript script)
	{
		component.refresh(script == null ? null : script.getEvents());
	}

	private MazeScript makeScript(String packName, String hook, java.util.List<MazeEvent> events)
	{
		if (events == null || events.isEmpty())
		{
			return null;
		}
		return new MazeScript(packName + "." + hook, events);
	}

	private MazeScript copyScript(MazeScript script, String name)
	{
		if (script == null || script.getEvents() == null || script.getEvents().isEmpty())
		{
			return null;
		}
		return new MazeScript(name, new ArrayList<>(script.getEvents()));
	}
}
