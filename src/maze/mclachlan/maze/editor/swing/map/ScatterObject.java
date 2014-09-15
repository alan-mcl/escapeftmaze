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
import java.util.List;
import java.util.Vector;
import java.util.BitSet;
import javax.swing.*;
import mclachlan.crusader.Tile;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Texture;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.editor.swing.SwingEditor;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ScatterObject extends Tool implements ActionListener
{
	private JButton ok, cancel;
	private JDialog dialog;
	private OptionsPanel optionsPanel;
	private MapEditor editor;

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return "Scatter Object";
	}

	/*-------------------------------------------------------------------------*/
	public void execute(MapEditor editor)
	{
		this.editor = editor;
		List<Object> selection = this.editor.getSelection();
		
		if (selection == null || selection.size() == 0)
		{
			JOptionPane.showMessageDialog(editor, "No tiles selected");
			return;
		}
		
		boolean tileFound = false;
		for (Object obj : selection)
		{
			if (obj instanceof Tile)
			{
				tileFound = true;
				break;
			}
		}
		
		if (!tileFound)
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

		dialog = new JDialog(SwingEditor.instance, getName(), true);
		dialog.setLayout(new BorderLayout());
		optionsPanel = new ScatterObject.OptionsPanel();
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
			applyToTiles(
				(String)optionsPanel.objectTexture.getSelectedItem(), 
				getDice(optionsPanel.amountPerTile),
				(Integer)optionsPanel.probabilityPerTile.getValue());
			dialog.setVisible(false);
		}
		catch (Exception x)
		{
			x.printStackTrace();
			JOptionPane.showMessageDialog(editor, x.getMessage());
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private void applyToTiles(String name, Dice nrPerTile, int prob)
	{
		if (nrPerTile == null)
		{
			return;
		}

		MazeTexture mazeTexture = Database.getInstance().getMazeTextures().get(name);
		Texture tx = mazeTexture.getTexture();
		Dice posD = new Dice(1,9,-1);

		List<Object> selection = editor.getSelection();
		
		for (Object obj : selection)
		{
			if (obj instanceof Tile)
			{
				Tile t = (Tile)obj;
				
				if (Dice.d100.roll() <= prob)
				{
					int index = editor.getCrusaderIndexOfTile(t);
					
					int nr = nrPerTile.roll();
					BitSet mask = new BitSet();
					
					for (int i=0; i<nr; i++)
					{
						int pos;
						do
						{
							pos = posD.roll();
						}
						while (mask.get(pos));
												
						mask.set(pos);
					}
					
					// remove any duplicate
					editor.getMap().removeObject(index);
					
					editor.getMap().addObject(
						new EngineObject(
							null, tx, tx, tx, tx, index, false, null, mask));
				}
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
		JComboBox objectTexture;
		JTextField amountPerTile;
		JSpinner probabilityPerTile;

		/*----------------------------------------------------------------------*/
		public OptionsPanel()
		{
			setLayout(new GridLayout(3, 2));
			
			Vector<String> vec = new Vector<String>(
				Database.getInstance().getMazeTextures().keySet());
			Collections.sort(vec);
			objectTexture = new JComboBox(vec);
			amountPerTile = new JTextField(20);
			probabilityPerTile = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
			
			add(new JLabel("Object texture:"));
			add(objectTexture);
			add(new JLabel("Nr per tile:"));
			add(amountPerTile);
			add(new JLabel("Probability per tile (%):"));
			add(probabilityPerTile);
		}
	}
}
