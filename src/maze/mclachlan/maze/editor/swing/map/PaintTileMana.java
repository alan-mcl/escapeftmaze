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

import mclachlan.crusader.Tile;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.editor.swing.SwingEditor;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class PaintTileMana extends Tool implements ActionListener
{
	private JButton ok, cancel;
	private JDialog dialog;
	private OptionsPanel optionsPanel;
	private MapEditor editor;

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return "Paint Tile Mana";
	}

	/*-------------------------------------------------------------------------*/
	public void execute(MapEditor editor, Zone zone)
	{
		this.editor = editor;
		List<Object> selection = this.editor.getSelection();
		
		if (selection == null || selection.size() == 0)
		{
			JOptionPane.showMessageDialog(editor, "No tiles selected");
			return;
		}
		
		boolean wallFound = false;
		for (Object obj : selection)
		{
			if (obj instanceof Tile)
			{
				wallFound = true;
				break;
			}
		}
		
		if (!wallFound)
		{
			JOptionPane.showMessageDialog(editor, "No tiles selected");
			return;
		}
		
		ok = new JButton("OK");
		ok.addActionListener(this);
		
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		
		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);

		dialog = new JDialog(SwingEditor.instance, "Paint Tile Mana", true);
		dialog.setLayout(new BorderLayout());
		optionsPanel = new OptionsPanel();
		dialog.add(optionsPanel, BorderLayout.CENTER);
		dialog.add(buttons, BorderLayout.SOUTH);
		dialog.setLocationRelativeTo(SwingEditor.instance);
		dialog.pack();
		dialog.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
		{
			applyChangesToSelection();			
		}
		else if (e.getSource() == cancel)
		{
			dialog.setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void applyChangesToSelection()
	{
		try
		{
			applyToTiles(Stats.Modifier.RED_MAGIC_GEN, getDice(optionsPanel.red));
			applyToTiles(Stats.Modifier.BLACK_MAGIC_GEN, getDice(optionsPanel.black));
			applyToTiles(Stats.Modifier.PURPLE_MAGIC_GEN, getDice(optionsPanel.purple));
			applyToTiles(Stats.Modifier.GOLD_MAGIC_GEN, getDice(optionsPanel.gold));
			applyToTiles(Stats.Modifier.WHITE_MAGIC_GEN, getDice(optionsPanel.white));
			applyToTiles(Stats.Modifier.GREEN_MAGIC_GEN, getDice(optionsPanel.green));
			applyToTiles(Stats.Modifier.BLUE_MAGIC_GEN, getDice(optionsPanel.blue));
			dialog.setVisible(false);
		}
		catch (Exception x)
		{
			x.printStackTrace();
			JOptionPane.showMessageDialog(editor, x.getMessage());
		}
		editor.display.repaint();
	}
	
	/*-------------------------------------------------------------------------*/
	private void applyToTiles(Stats.Modifier modifier, Dice d)
	{
		if (d == null)
		{
			return;
		}
		
		List<Object> selection = editor.getSelection();
		
		for (Object obj : selection)
		{
			if (obj instanceof Tile)
			{
				Tile t = (Tile)obj;
				mclachlan.maze.map.Tile tile = editor.getMazeTile(t);
				
				tile.getStatModifier().setModifier(modifier, d.roll("paint tile mana"));
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private Dice getDice(JTextField field)
	{
		String s = field.getText();
		if (s == null || s.equals(""))
		{
			return null;
		}
		
		try
		{
			return V1Dice.fromString(s);
		}
		catch (Exception x)
		{
			throw new MazeException("Invalid dice ["+ s +"]", x);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Class to offer a dice range for each colour of magic.
	 */
	static class OptionsPanel extends JPanel
	{
		JTextField red, black, purple, gold, white, green, blue;

		/*----------------------------------------------------------------------*/
		public OptionsPanel()
		{
			setLayout(new GridLayout(9, 2));
			add(new JLabel("Colour               "));
			add(new JLabel("Dice                 "));
			
			red = new JTextField();
			black = new JTextField();
			purple = new JTextField();
			gold = new JTextField();
			white = new JTextField();
			green = new JTextField();
			blue = new JTextField();
			
			add(new JLabel("Red")); add(red);
			add(new JLabel("Black")); add(black);
			add(new JLabel("Purple")); add(purple);
			add(new JLabel("Gold")); add(gold);
			add(new JLabel("White")); add(white);
			add(new JLabel("Green")); add(green);
			add(new JLabel("Blue")); add(blue);
		}
	}
}
