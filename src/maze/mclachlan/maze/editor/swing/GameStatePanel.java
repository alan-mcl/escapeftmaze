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
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.DifficultyLevel;
import mclachlan.maze.game.GameState;
import mclachlan.maze.game.PlayerTilesVisited;
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class GameStatePanel extends JPanel implements KeyListener, ActionListener, ChangeListener
{
	JComboBox zone, facing;
	JSpinner playerX, playerY, partyGold, partySupplies, formation, turnNr;
	JComboBox p1, p2, p3 , p4 , p5, p6;
	JComboBox difficultyLevel;
	private String saveGameName;
	private PlayerTilesVisited playerTilesVisited;

	/*-------------------------------------------------------------------------*/
	public GameStatePanel(String saveGameName)
	{
		this.saveGameName = saveGameName;
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		
		zone = new JComboBox();
		zone.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Zone:"), zone, gbc);

		difficultyLevel = new JComboBox();
		difficultyLevel.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Difficulty Level:"), difficultyLevel, gbc);
		
		playerX = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
		playerX.addChangeListener(this);
		dodgyGridBagShite(this, new JLabel("Player X:"), playerX, gbc);
		
		playerY = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
		playerY.addChangeListener(this);
		dodgyGridBagShite(this, new JLabel("Player Y:"), playerY, gbc);
		
		String[] facingOptions = 
			{
				"NORTH",
				"SOUTH",
				"EAST",
				"WEST",
			};
		facing = new JComboBox(facingOptions);
		facing.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Facing:"), facing, gbc);
		
		partyGold = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		partyGold.addChangeListener(this);
		dodgyGridBagShite(this, new JLabel("Party Gold:"), partyGold, gbc);

		partySupplies = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		partySupplies.addChangeListener(this);
		dodgyGridBagShite(this, new JLabel("Party Supplies:"), partySupplies, gbc);
		
		turnNr = new JSpinner(new SpinnerNumberModel(0, 0, Long.MAX_VALUE, 1));
		turnNr.addChangeListener(this);
		dodgyGridBagShite(this, new JLabel("Turn Number:"), turnNr, gbc);
		
		p1 = new JComboBox();
		p1.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("PC #1:"), p1, gbc);
		
		p2 = new JComboBox();
		p2.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("PC #2:"), p2, gbc);
		
		p3 = new JComboBox();
		p3.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("PC #3:"), p3, gbc);
		
		p4 = new JComboBox();
		p4.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("PC #4:"), p4, gbc);
		
		p5 = new JComboBox();
		p5.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("PC #5:"), p5, gbc);
		
		p6 = new JComboBox();
		p6.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("PC #6:"), p6, gbc);
		
		formation = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
		formation.addChangeListener(this);
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		add(new JLabel("Formation:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		add(formation, gbc);
	}
	
	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> v1 = new Vector<String>(Database.getInstance().getZoneNames());
		Collections.sort(v1);
		zone.setModel(new DefaultComboBoxModel(v1));

		try
		{
			Vector<String> pcs = new Vector<String>(
				Database.getInstance().getLoader().loadPlayerCharacters(saveGameName).keySet());
			Collections.sort(pcs);
			pcs.add(0, EditorPanel.NONE);
			
			p1.setModel(new DefaultComboBoxModel(pcs));
			p2.setModel(new DefaultComboBoxModel(pcs));
			p3.setModel(new DefaultComboBoxModel(pcs));
			p4.setModel(new DefaultComboBoxModel(pcs));
			p5.setModel(new DefaultComboBoxModel(pcs));
			p6.setModel(new DefaultComboBoxModel(pcs));
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}

		Vector<String> difficultyLevels = new Vector<String>(
			Database.getInstance().getDifficultyLevels().keySet());
		Collections.sort(difficultyLevels);
		difficultyLevel.setModel(new DefaultComboBoxModel(difficultyLevels));
	}
	
	/*-------------------------------------------------------------------------*/
	public void refresh(GameState gs)
	{
		zone.removeActionListener(this);
		facing.removeActionListener(this);
		playerX.removeChangeListener(this);
		playerY.removeChangeListener(this);
		formation.removeChangeListener(this);
		partyGold.removeChangeListener(this);
		partySupplies.removeChangeListener(this);
		turnNr.removeChangeListener(this);
		p1.removeActionListener(this);
		p2.removeActionListener(this);
		p3.removeActionListener(this);
		p4.removeActionListener(this);
		p5.removeActionListener(this);
		p6.removeActionListener(this);
		difficultyLevel.removeActionListener(this);

		zone.setSelectedItem(gs.getCurrentZone().getName());
		facing.setSelectedIndex(gs.getFacing()-1);
		playerX.setValue(gs.getPlayerPos().x);
		playerY.setValue(gs.getPlayerPos().y);
		formation.setValue(gs.getFormation());
		partyGold.setValue(gs.getPartyGold());
		partySupplies.setValue(gs.getPartySupplies());
		turnNr.setValue(gs.getTurnNr());
		
		List<String> list = gs.getPartyNames();
		if (list != null)
		{
			p1.setSelectedItem(list.size()>=1?list.get(0):EditorPanel.NONE);
			p2.setSelectedItem(list.size()>=2?list.get(1):EditorPanel.NONE);
			p3.setSelectedItem(list.size()>=3?list.get(2):EditorPanel.NONE);
			p4.setSelectedItem(list.size()>=4?list.get(3):EditorPanel.NONE);
			p5.setSelectedItem(list.size()>=5?list.get(4):EditorPanel.NONE);
			p6.setSelectedItem(list.size()>=6?list.get(5):EditorPanel.NONE);
		}

		difficultyLevel.setSelectedItem(gs.getDifficultyLevel().getName());

		zone.addActionListener(this);
		facing.addActionListener(this);
		playerX.addChangeListener(this);
		playerY.addChangeListener(this);
		formation.addChangeListener(this);
		partyGold.addChangeListener(this);
		partySupplies.addChangeListener(this);
		turnNr.addChangeListener(this);
		p1.addActionListener(this);
		p2.addActionListener(this);
		p3.addActionListener(this);
		p4.addActionListener(this);
		p5.addActionListener(this);
		p6.addActionListener(this);
		difficultyLevel.addActionListener(this);
	}
	
	/*-------------------------------------------------------------------------*/
	public GameState getGameState()
	{
		Zone zone = Database.getInstance().getZone((String)this.zone.getSelectedItem());
		int x = (Integer)playerX.getValue();
		int y = (Integer)playerY.getValue();
		int gold = (Integer)partyGold.getValue();
		int supplies = (Integer)partySupplies.getValue();
		int form = (Integer)formation.getValue();
		int face = facing.getSelectedIndex()+1;
		long tN = (Long)turnNr.getValue();
		List<String> characters = new ArrayList<String>();
		if (p1.getSelectedIndex() != 0) characters.add((String)p1.getSelectedItem());
		if (p2.getSelectedIndex() != 0) characters.add((String)p2.getSelectedItem());
		if (p3.getSelectedIndex() != 0) characters.add((String)p3.getSelectedItem());
		if (p4.getSelectedIndex() != 0) characters.add((String)p4.getSelectedItem());
		if (p5.getSelectedIndex() != 0) characters.add((String)p5.getSelectedItem());
		if (p6.getSelectedIndex() != 0) characters.add((String)p6.getSelectedItem());
		DifficultyLevel dl = Database.getInstance().getDifficultyLevels().get(
			(String)this.difficultyLevel.getSelectedItem());

		return new GameState(
			zone,
			dl,
			new Point(x, y),
			face, 
			gold,
			supplies,
			characters,
			form, 
			tN);
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

	public void keyTyped(KeyEvent e)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.SAVE_GAMES);
	}

	public void keyPressed(KeyEvent e)
	{

	}

	public void keyReleased(KeyEvent e)
	{

	}

	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.SAVE_GAMES);
	}

	public void stateChanged(ChangeEvent e)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.SAVE_GAMES);
	}
}
