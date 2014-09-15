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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileOutputStream;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class CampaignEditorPanel extends JPanel
	implements KeyListener, ActionListener, ChangeListener, IEditorPanel
{
	JTextField displayName, defaultRace, defaultPortrait;
	JTextArea description;
	JComboBox startingScript, introScript;
	Campaign currentCampaign;

	/*-------------------------------------------------------------------------*/
	public CampaignEditorPanel()
	{
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		add(getLeft(), gbc);

		gbc.gridx++;
		gbc.weightx=1.0;
		description = new JTextArea();
		description.setWrapStyleWord(true);
		description.setLineWrap(true);
		description.setRows(25);
		description.setColumns(30);
		description.addKeyListener(this);
		add(new JScrollPane(description), gbc);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getLeft()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		displayName = new JTextField(20);
		displayName.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Display Name:"), displayName, gbc);

		introScript = new JComboBox();
		introScript.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Intro Script:"), introScript, gbc);

		startingScript = new JComboBox();
		startingScript.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Starting Script:"), startingScript, gbc);
		
		defaultRace = new JTextField(20);
		defaultRace.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Default Race:"), defaultRace, gbc);
		
		defaultPortrait = new JTextField(20);
		defaultPortrait.addActionListener(this);
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel("Default Portrait:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(defaultPortrait, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(vec);
		
		startingScript.setModel(new DefaultComboBoxModel(vec));
		introScript.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(Campaign c)
	{
		currentCampaign = c;

		startingScript.removeActionListener(this);
		introScript.removeActionListener(this);
		defaultPortrait.removeActionListener(this);
		defaultRace.removeActionListener(this);

		displayName.setText(c.getDisplayName());
		startingScript.setSelectedItem(c.getStartingScript());
		introScript.setSelectedItem(c.getIntroScript());
		description.setText(c.getDescription());
		description.setCaretPosition(0);
		defaultPortrait.setText(c.getDefaultPortrait());
		defaultRace.setText(c.getDefaultRace());

		startingScript.addActionListener(this);
		introScript.addActionListener(this);
		defaultPortrait.addActionListener(this);
		defaultRace.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void commit()
	{
		Properties p = new Properties();
		p.setProperty("displayName", displayName.getText());
		p.setProperty("description", description.getText());
		p.setProperty("startingScript", startingScript.getSelectedItem().toString());
		p.setProperty("defaultRace", defaultRace.getText());
		p.setProperty("defaultPortrait", defaultPortrait.getText());
		p.setProperty("introScript", introScript.getSelectedItem().toString());

		currentCampaign.setDisplayName(displayName.getText());
		currentCampaign.setDescription(description.getText());
		currentCampaign.setStartingScript((String)startingScript.getSelectedItem());
		currentCampaign.setDefaultRace(defaultRace.getText());
		currentCampaign.setDefaultPortrait(defaultPortrait.getText());
		currentCampaign.setIntroScript((String)introScript.getSelectedItem());

		try
		{
			FileOutputStream fos = new FileOutputStream("data/"+currentCampaign.getName()+"/campaign.cfg");
			p.store(fos, "");
			fos.close();
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
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
	public void keyTyped(KeyEvent e)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.CAMPAIGN);
	}

	public void keyPressed(KeyEvent e)
	{

	}

	public void keyReleased(KeyEvent e)
	{

	}

	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.CAMPAIGN);
	}

	public void stateChanged(ChangeEvent e)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.CAMPAIGN);
	}

	/*-------------------------------------------------------------------------*/
	protected Container getEditControls()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		// not supported
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		// not supported
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		// not supported
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		// not supported
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		// not supported
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		commit();
	}

	/*-------------------------------------------------------------------------*/
	public String getCurrentName()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public void refreshNames(String name)
	{
	}

	/*-------------------------------------------------------------------------*/
	public int getDirtyFlag()
	{
		return SwingEditor.Tab.CAMPAIGN;
	}

	/*-------------------------------------------------------------------------*/
	public void reload()
	{
		// todo?
	}
}
