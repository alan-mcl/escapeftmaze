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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.maze.data.Database;
import mclachlan.maze.editor.swing.*;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class TileDetailsPanel extends JPanel 
	implements KeyListener, ChangeListener, ActionListener, 
		StatModifierComponentCallback, TileScriptComponentCallback
{
	private final JLabel indexLabel;
	private final Zone zone;
	private final MapEditor editor;
	private final JButton placeObjectButton;
	private final Tool placeObject;
	private int index;
	private TileProxy tile;
	
	// maze tile properties
	private final JComboBox terrainType;
	private final JTextField terrainSubType;
	private final StatModifierComponent statModifier;
	private final JSpinner randomEncounterChance;
	private final JComboBox randomEncounterTable;
	private final JComboBox restingDanger;
	private final JComboBox restingEfficiency;
	private final JTextField sector;
	private final MultipleTileScriptComponent tileScript;
	
	// crusader tile properties
	private final JComboBox ceilingTexture, floorTexture;                                                                
	private final JComboBox ceilingMaskTexture, floorMaskTexture;
	private final JSpinner lightLevel, ceilingHeight;
	
	/*-------------------------------------------------------------------------*/
	public TileDetailsPanel(Zone zone, MapEditor editor, boolean multiSelect)
	{
		this.zone = zone;
		this.editor = editor;

		this.setLayout(new BorderLayout());

		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());

		GridBagConstraints gbc = createGridBagConstraints();
		
		indexLabel = new JLabel();
		dodgyGridBagShite(content, new JLabel("Index:"), indexLabel, gbc);
		
		dodgyGridBagShite(content, new JLabel("Maze Properties"), new JLabel(), gbc);
		
		terrainType = new JComboBox(Tile.TerrainType.values());
		terrainType.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Terrain Type:"), terrainType, gbc);
		
		terrainSubType = new JTextField(15);
		terrainSubType.addKeyListener(this);
		dodgyGridBagShite(content, new JLabel("Terrain Sub Type:"), terrainSubType, gbc);
		
		statModifier = new StatModifierComponent(null, -1, this);
		dodgyGridBagShite(content, new JLabel("Stat Modifier:"), statModifier, gbc);

		restingDanger = new JComboBox(Tile.RestingDanger.values());
		restingDanger.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Resting Danger:"), restingDanger, gbc);

		restingEfficiency = new JComboBox(Tile.RestingEfficiency.values());
		restingEfficiency.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Resting Efficiency:"), restingEfficiency, gbc);

		sector = new JTextField(15);
		sector.addKeyListener(this);
		dodgyGridBagShite(content, new JLabel("Map Sector:"), sector, gbc);
		
		randomEncounterChance = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
		randomEncounterChance.addChangeListener(this);
		dodgyGridBagShite(content, new JLabel("Encounters Chance:"), randomEncounterChance, gbc);

		Vector<String> vec = new Vector<String>(Database.getInstance().getEncounterTables().keySet());
		if (multiSelect)
		{
			vec.insertElementAt(EditorPanel.NONE, 0);
		}
		Collections.sort(vec);
		randomEncounterTable = new JComboBox(vec);
		randomEncounterTable.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Encounter Table:"), randomEncounterTable, gbc);
		
		tileScript = new MultipleTileScriptComponent(null, -1, this, zone);
		dodgyGridBagShite(content, new JLabel("Tile Script:"), tileScript, gbc);
		
		dodgyGridBagShite(content, new JLabel("Crusader Properties"), new JLabel(), gbc);

		Vector<String> vec1 = new Vector<>(Database.getInstance().getMazeTextures().keySet());
		if (multiSelect)
		{
			vec1.insertElementAt(EditorPanel.NONE, 0);
		}
		Collections.sort(vec1);
		floorTexture = new JComboBox(vec1);
		floorTexture.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Floor Texture:"), floorTexture, gbc);

		Vector<String> vec2 = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		vec2.insertElementAt(EditorPanel.NONE, 0);
		Collections.sort(vec2);
		floorMaskTexture = new JComboBox(vec2);
		floorMaskTexture.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Floor Mask Texture:"), floorMaskTexture, gbc);

		Vector<String> vec3 = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		if (multiSelect)
		{
			vec3.insertElementAt(EditorPanel.NONE, 0);
		}
		Collections.sort(vec3);
		ceilingTexture = new JComboBox(vec3);
		ceilingTexture.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Ceiling Texture:"), ceilingTexture, gbc);

		Vector<String> vec4 = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		vec4.insertElementAt(EditorPanel.NONE, 0);
		Collections.sort(vec4);
		ceilingMaskTexture = new JComboBox(vec4);
		ceilingMaskTexture.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Ceiling Mask Texture:"), ceilingMaskTexture, gbc);

		lightLevel = new JSpinner(new SpinnerNumberModel(0, 0, 64, 1));
		lightLevel.addChangeListener(this);
		dodgyGridBagShite(content, new JLabel("Light Level:"), lightLevel, gbc);

		ceilingHeight = new JSpinner(new SpinnerNumberModel(1, 1, 32, 1));
		ceilingHeight.addChangeListener(this);
		dodgyGridBagShite(content, new JLabel("Ceiling Height:"), ceilingHeight, gbc);

		placeObject = new PlaceObjects();
		String toolName = placeObject.getName();
		placeObjectButton = new JButton(toolName);
		placeObjectButton.addActionListener(this);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		content.add(placeObjectButton, gbc);

		JScrollPane scroller = new JScrollPane(
			content,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(scroller, BorderLayout.CENTER);
	}
	
	/*-------------------------------------------------------------------------*/
	public void refresh(TileProxy tile, int index, int x, int y)
	{
		this.tile = tile;
		this.index = index;
		
		terrainType.removeKeyListener(this);
		terrainSubType.removeKeyListener(this);
		restingDanger.removeActionListener(this);
		restingEfficiency.removeActionListener(this);
		sector.removeKeyListener(this);
		randomEncounterChance.removeChangeListener(this);
		randomEncounterTable.removeActionListener(this);
		floorTexture.removeActionListener(this);
		floorMaskTexture.removeActionListener(this);
		ceilingTexture.removeActionListener(this);
		ceilingMaskTexture.removeActionListener(this);
		lightLevel.removeChangeListener(this);
		ceilingHeight.removeChangeListener(this);

		terrainType.setSelectedItem(tile.getTerrainType()==null?"":tile.getTerrainType());
		terrainSubType.setText(tile.getTerrainSubType() == null ? "" : tile.getTerrainSubType());
		statModifier.refresh(tile.getStatModifier());
		restingDanger.setSelectedItem(tile.getRestingDanger() == null ? "" : tile.getRestingDanger());
		restingEfficiency.setSelectedItem(tile.getRestingEfficiency()==null?"":tile.getRestingEfficiency());
		randomEncounterChance.setValue(tile.getRandomEncounterChance());
		randomEncounterTable.setSelectedItem(tile.getRandomEncounters()==null?EditorPanel.NONE:tile.getRandomEncounters().getName());
		tileScript.refresh(tile.getScripts(), zone);
		sector.setText(tile.getSector() == null ? "" : tile.getSector());
		
		floorTexture.setSelectedItem(tile.getFloorTexture()==null?EditorPanel.NONE:tile.getFloorTexture().getName());
		floorMaskTexture.setSelectedItem(tile.getFloorMaskTexture()==null?EditorPanel.NONE:tile.getFloorMaskTexture().getName());
		ceilingTexture.setSelectedItem(tile.getCeilingTexture()==null?EditorPanel.NONE:tile.getCeilingTexture().getName());
		ceilingMaskTexture.setSelectedItem(tile.getCeilingMaskTexture()==null?EditorPanel.NONE:tile.getCeilingMaskTexture().getName());
		lightLevel.setValue(tile.getLightLevel());
		ceilingHeight.setValue(tile.getCeilingHeight());

		if (index >= 0)
		{
			this.indexLabel.setText(index + " = (" + x + "," + y + ")");
		}

		terrainType.addKeyListener(this);
		terrainSubType.addKeyListener(this);
		restingDanger.addActionListener(this);
		restingEfficiency.addActionListener(this);
		sector.addKeyListener(this);
		randomEncounterChance.addChangeListener(this);
		randomEncounterTable.addActionListener(this);
		floorTexture.addActionListener(this);
		floorMaskTexture.addActionListener(this);
		ceilingTexture.addActionListener(this);
		ceilingMaskTexture.addActionListener(this);
		lightLevel.addChangeListener(this);
		ceilingHeight.addChangeListener(this);
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

	/*-------------------------------------------------------------------------*/
	public void keyTyped(KeyEvent e)
	{
		if (e.getSource() == terrainSubType)
		{
			if (tile != null)
			{
				tile.setTerrainSubType(terrainSubType.getText());
			}
		}
		else if (e.getSource() == sector)
		{
			if (tile != null)
			{
				tile.setSector("".equals(sector.getText()) ? null : sector.getText());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void keyPressed(KeyEvent e)
	{
		
	}

	/*-------------------------------------------------------------------------*/
	public void keyReleased(KeyEvent e)
	{
		
	}

	/*-------------------------------------------------------------------------*/
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == randomEncounterChance)
		{
			if (tile != null)
			{
				tile.setRandomEncounterChance((Integer)randomEncounterChance.getValue());
			}
		}
		else if (e.getSource() == lightLevel)
		{
			if (tile != null)
			{
				tile.setLightLevel((Integer)lightLevel.getValue());
			}
		}
		else if (e.getSource() == ceilingHeight)
		{
			if (tile != null)
			{
				tile.setCeilingHeight((Integer)ceilingHeight.getValue());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == randomEncounterTable)
		{
			if (tile != null)
			{
				String name = (String)randomEncounterTable.getSelectedItem();
				tile.setRandomEncounters(Database.getInstance().getEncounterTable(name));
			}
		}
		else if (e.getSource() == terrainType)
		{
			if (tile != null)
			{
				tile.setTerrainType((Tile.TerrainType)terrainType.getSelectedItem());
			}
		}
		else if (e.getSource() == restingDanger)
		{
			if (tile != null)
			{
				tile.setRestingDanger((Tile.RestingDanger)restingDanger.getSelectedItem());
			}
		}
		else if (e.getSource() == restingEfficiency)
		{
			if (tile != null)
			{
				tile.setRestingEfficiency((Tile.RestingEfficiency)restingEfficiency.getSelectedItem());
			}
		}
		else if (e.getSource() == floorTexture)
		{
			if (tile != null)
			{
				String name = (String)floorTexture.getSelectedItem();
				if (!EditorPanel.NONE.equals(name))
				{
					tile.setFloorTexture(Database.getInstance().getMazeTexture(name).getTexture());
				}
				else
				{
					tile.setFloorTexture(null);
				}
			}
		}
		else if (e.getSource() == floorMaskTexture)
		{
			if (tile != null)
			{
				String name = (String)floorMaskTexture.getSelectedItem();
				if (!EditorPanel.NONE.equals(name))
				{
					tile.setFloorMaskTexture(Database.getInstance().getMazeTexture(name).getTexture());
				}
				else
				{
					tile.setFloorMaskTexture(null);
				}
			}
		}
		else if (e.getSource() == ceilingTexture)
		{
			if (tile != null)
			{
				String name = (String)ceilingTexture.getSelectedItem();
				if (!EditorPanel.NONE.equals(name))
				{
					tile.setCeilingTexture(Database.getInstance().getMazeTexture(name).getTexture());
				}
				else
				{
					tile.setCeilingTexture(null);
				}
			}
		}
		else if (e.getSource() == ceilingMaskTexture)
		{
			if (tile != null)
			{
				String name = (String)ceilingMaskTexture.getSelectedItem();
				if (!EditorPanel.NONE.equals(name))
				{
					tile.setCeilingMaskTexture(Database.getInstance().getMazeTexture(name).getTexture());
				}
				else
				{
					tile.setCeilingMaskTexture(null);
				}
			}
		}
		else if (e.getSource() == placeObjectButton)
		{
			placeObject.execute(editor, zone);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void statModifierChanged(StatModifierComponent component)
	{
		if (component == statModifier)
		{
			tile.setStatModifier(statModifier.getModifier());
		}
	}

	/*-------------------------------------------------------------------------*/
	public void tileScriptChanged(Component component)
	{
		if (component == tileScript)
		{
			tile.setScripts(tileScript.getScripts());
		}
	}
}
