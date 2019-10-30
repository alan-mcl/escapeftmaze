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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.crusader.Tile;
import mclachlan.maze.data.Database;
import mclachlan.maze.editor.swing.SwingEditor;
import mclachlan.maze.editor.swing.TileScriptEditor;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Encounter;

/**
 *
 */
public class PaintEncounters extends Tool implements ActionListener
{
	private JButton ok, cancel;
	private JDialog dialog;
	private OptionsPanel optionsPanel;
	private MapEditor editor;

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return "Paint Encounters";
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

		dialog = new JDialog(SwingEditor.instance, "Paint Encounters", true);
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
			dialog.setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			dialog.setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void applyChangesToSelection()
	{
		List<Object> selection = editor.getSelection();

		EncounterTable table = Database.getInstance().getEncounterTable(
			(String)optionsPanel.encounterTable.getSelectedItem());

		for (Object obj : selection)
		{
			if (obj instanceof Tile)
			{
				Tile t = (Tile)obj;
				mclachlan.maze.map.Tile tile = editor.getMazeTile(t);

				String mazeVar = TileScriptEditor.getEncounterMazeVariable(editor.zone);
				Encounter enc = new Encounter(table, mazeVar, null, null);

				tile.getScripts().add(enc);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Class to offer a dice range for each colour of magic.
	 */
	static class OptionsPanel extends JPanel
	{
		private JComboBox encounterTable;

		/*----------------------------------------------------------------------*/
		public OptionsPanel()
		{
			Vector<String> vec = new Vector<String>(Database.getInstance().getEncounterTables().keySet());
			Collections.sort(vec);
			encounterTable = new JComboBox(vec);

			add(new JLabel("Encounter table:"));
			add(encounterTable);
		}
	}

}
