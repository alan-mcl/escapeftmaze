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
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class PlaceObjects extends Tool implements ActionListener
{
	private JButton ok, cancel;
	private JDialog dialog;
	private OptionsPanel optionsPanel;
	private MapEditor editor;

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return "Place Object(s)";
	}

	/*-------------------------------------------------------------------------*/
	public void execute(MapEditor editor, Zone zone)
	{
		this.editor = editor;
		List<Object> selection = this.editor.getSelection();
		
		if (selection == null || selection.isEmpty())
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
		optionsPanel = new PlaceObjects.OptionsPanel();
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
			editor.display.repaint();
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
		Dice posD = new Dice(1, 9, -1);

		List<Object> selection = editor.getSelection();

		for (Object obj : selection)
		{
			if (obj instanceof Tile)
			{
				Tile t = (Tile)obj;

				if (Dice.d100.roll("scatter object 1") <= prob)
				{
					int tileIndex = editor.getCrusaderIndexOfTile(t);

					int nrObjsToPlace = nrPerTile.roll("scatter object 2");

					boolean anySelected = false;
					for (JCheckBox cb : optionsPanel.placementMask)
					{
						if (cb.isSelected())
						{
							anySelected = true;
							break;
						}
					}

					if (anySelected)
					{
						// place according to the mask

						BitSet maskAllowed = new BitSet();
						for (int i = 0; i < optionsPanel.placementMask.length; i++)
						{
							if (optionsPanel.placementMask[i].isSelected())
							{
								maskAllowed.set(i);
							}
						}

						nrObjsToPlace = Math.min(nrObjsToPlace, maskAllowed.cardinality());

						BitSet mask = new BitSet();

						for (int i = 0; i < nrObjsToPlace; i++)
						{
							int pos;
							do
							{
								pos = posD.roll("scatter object 3");
							}
							while (mask.get(pos) || !maskAllowed.get(pos));

							mask.set(pos);
						}

						for (int i = 0; i < 9; i++)
						{
							if (mask.get(i))
							{
								EngineObject eo = new EngineObject(
									null, 0, 0, tx, tx, tx, tx, tileIndex, false, null, EngineObject.Alignment.BOTTOM);
								editor.getMap().initObjectFromTileIndex(eo, i);

								editor.getMap().addObject(eo);
							}
						}
					}
					else
					{
						// just place in random positions
						for (int i = 0; i < nrObjsToPlace; i++)
						{
							// get the actual x and y coords from the tile index
							Point tileXYPos = editor.getMap().getTileXYPos(tileIndex);

							Dice xD = new Dice(1, editor.getMap().getBaseImageSize(), tileXYPos.x - 1);
							Dice yD = new Dice(1, editor.getMap().getBaseImageSize(), tileXYPos.y - 1);

							EngineObject eo = new EngineObject(
								null, xD.roll("scatter object 4"), yD.roll("scatter object 5"),
								tx, tx, tx, tx, tileIndex, false, null, EngineObject.Alignment.BOTTOM);
							editor.getMap().initObjectFromXY(eo);

							editor.getMap().addObject(eo);
						}
					}
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
		private JCheckBox[] placementMask;

		/*----------------------------------------------------------------------*/
		public OptionsPanel()
		{
			setLayout(new BorderLayout(5,5));

			JPanel top = new JPanel(new GridLayout(3, 2, 5, 5));
			
			Vector<String> vec = new Vector<>(
				Database.getInstance().getMazeTextures().keySet());
			Collections.sort(vec);
			objectTexture = new JComboBox(vec);
			amountPerTile = new JTextField(20);
			amountPerTile.setText("1d1");
			probabilityPerTile = new JSpinner(new SpinnerNumberModel(100, 1, 100, 1));


			JPanel placementPanel = new JPanel(new BorderLayout(5,5));

			JPanel maskPanel = new JPanel(new GridLayout(3, 3));
			placementMask = new JCheckBox[9];

			for (int i=0; i<placementMask.length; i++)
			{
				placementMask[i] = new JCheckBox();
				maskPanel.add(placementMask[i]);
			}

			placementMask[EngineObject.Placement.CENTER].setSelected(true);

			placementPanel.add(new JLabel("Placement mask:"), BorderLayout.NORTH);
			placementPanel.add(maskPanel, BorderLayout.CENTER);
			
			top.add(new JLabel("Object texture:"));
			top.add(objectTexture);
			top.add(new JLabel("Nr per tile:"));
			top.add(amountPerTile);
			top.add(new JLabel("Probability per tile (%):"));
			top.add(probabilityPerTile);

			this.add(top, BorderLayout.NORTH);
			this.add(placementPanel, BorderLayout.CENTER);
		}
	}
}
