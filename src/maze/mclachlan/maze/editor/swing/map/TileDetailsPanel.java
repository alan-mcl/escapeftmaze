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

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.maze.data.Database;
import mclachlan.maze.editor.swing.*;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.crusader.MouseClickScriptAdapter;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Texture;

/**
 *
 */
public class TileDetailsPanel extends JPanel 
	implements KeyListener, ChangeListener, ActionListener, 
		StatModifierComponentCallback, TileScriptComponentCallback
{
	JLabel indexLabel;
	Zone zone;
	int index;
	TileProxy tile;
	
	// maze tile properties
	JComboBox terrainType;
	JTextField terrainSubType;
	StatModifierComponent statModifier;
	JSpinner randomEncounterChance;
	JComboBox randomEncounterTable;
	MultipleTileScriptComponent tileScript;
	
	// crusader tile properties
	JComboBox ceilingTexture, floorTexture;
	JComboBox ceilingMaskTexture, floorMaskTexture;
	JSpinner lightLevel;
	
	// object properties
	JCheckBox hasObject;
	JTextField objectName;
	JComboBox northTexture, southTexture, eastTexture, westTexture;
	JCheckBox isLightSource;
	SingleTileScriptComponent mouseClickScript;
	List<JCheckBox> placementMask;
	JButton quickObjectTexture;

	/*-------------------------------------------------------------------------*/
	public TileDetailsPanel(Zone zone, boolean multiSelect)
	{
		this.zone = zone;
		setLayout(new GridBagLayout());

		GridBagConstraints gbc = createGridBagConstraints();
		
		indexLabel = new JLabel();
		dodgyGridBagShite(this, new JLabel("Index:"), indexLabel, gbc);
		
		dodgyGridBagShite(this, new JLabel("Maze Properties"), new JLabel(), gbc);
		
		terrainType = new JComboBox(ZonePanel.TERRAIN_TYPES);
		terrainType.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Terrain Type:"), terrainType, gbc);
		
		terrainSubType = new JTextField(15);
		terrainSubType.addKeyListener(this);
		dodgyGridBagShite(this, new JLabel("Terrain Sub Type:"), terrainSubType, gbc);
		
		statModifier = new StatModifierComponent(null, -1, this);
		dodgyGridBagShite(this, new JLabel("Stat Modifier:"), statModifier, gbc);
		
		randomEncounterChance = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
		randomEncounterChance.addChangeListener(this);
		dodgyGridBagShite(this, new JLabel("Encounters Chance:"), randomEncounterChance, gbc);

		Vector<String> vec = new Vector<String>(Database.getInstance().getEncounterTables().keySet());
		if (multiSelect)
		{
			vec.insertElementAt(EditorPanel.NONE, 0);
		}
		Collections.sort(vec);
		randomEncounterTable = new JComboBox(vec);
		randomEncounterTable.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Encounter Table:"), randomEncounterTable, gbc);
		
		tileScript = new MultipleTileScriptComponent(null, -1, this, zone);
		dodgyGridBagShite(this, new JLabel("Tile Script:"), tileScript, gbc);
		
		dodgyGridBagShite(this, new JLabel("Crusader Properties"), new JLabel(), gbc);

		Vector<String> vec1 = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		if (multiSelect)
		{
			vec1.insertElementAt(EditorPanel.NONE, 0);
		}
		Collections.sort(vec1);
		floorTexture = new JComboBox(vec1);
		floorTexture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Floor Texture:"), floorTexture, gbc);

		Vector<String> vec2 = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		vec2.insertElementAt(EditorPanel.NONE, 0);
		Collections.sort(vec2);
		floorMaskTexture = new JComboBox(vec2);
		floorMaskTexture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Floor Mask Texture:"), floorMaskTexture, gbc);

		Vector<String> vec3 = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		if (multiSelect)
		{
			vec3.insertElementAt(EditorPanel.NONE, 0);
		}
		Collections.sort(vec3);
		ceilingTexture = new JComboBox(vec3);
		ceilingTexture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Ceiling Texture:"), ceilingTexture, gbc);

		Vector<String> vec4 = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		vec4.insertElementAt(EditorPanel.NONE, 0);
		Collections.sort(vec4);
		ceilingMaskTexture = new JComboBox(vec4);
		ceilingMaskTexture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Ceiling Mask Texture:"), ceilingMaskTexture, gbc);

		
		lightLevel = new JSpinner(new SpinnerNumberModel(0, 0, 64, 1));
		lightLevel.addChangeListener(this);
		dodgyGridBagShite(this, new JLabel("Light Level:"), lightLevel, gbc);
		
		hasObject = new JCheckBox("?");
		hasObject.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Object"), hasObject, gbc);

		objectName = new JTextField(20);
		objectName.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("Object Name:"), objectName, gbc);
		
		Vector<String> textures = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(textures);
		
		quickObjectTexture = new JButton("North Texture:");
		quickObjectTexture.addActionListener(this);
		quickObjectTexture.setToolTipText("Assign other textures to this.");

		northTexture = new JComboBox(textures);
		northTexture.addActionListener(this);
		dodgyGridBagShite(this, quickObjectTexture, northTexture, gbc);
		
		southTexture = new JComboBox(textures);
		southTexture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("South Texture:"), southTexture, gbc);
		
		eastTexture = new JComboBox(textures);
		eastTexture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("East Texture:"), eastTexture, gbc);
		
		westTexture = new JComboBox(textures);
		westTexture.addActionListener(this);
		dodgyGridBagShite(this, new JLabel("West Texture:"), westTexture, gbc);
		
		isLightSource = new JCheckBox("Light Source?");
		isLightSource.addActionListener(this);
		dodgyGridBagShite(this, isLightSource, new JLabel(), gbc);
		
		mouseClickScript = new SingleTileScriptComponent(null, -1, this, zone);
		dodgyGridBagShite(this, new JLabel("Click Script:"), mouseClickScript, gbc);
		
		JPanel placementPanel = new JPanel(new GridLayout(3,3));
		placementMask = new ArrayList<JCheckBox>(9);
		for (int i = 0; i < 9; i++)
		{
			placementMask.add(new JCheckBox());
			placementMask.get(i).addActionListener(this);
			placementPanel.add(placementMask.get(i));
		}

		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		add(new JLabel("Placement:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		add(placementPanel, gbc);
		
		if (multiSelect)
		{
			hasObject.setEnabled(false);
			disableObjectWidgets();
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void refresh(TileProxy tile, int index, int x, int y)
	{
		this.tile = tile;
		this.index = index;
		
		terrainType.removeKeyListener(this);
		terrainSubType.removeKeyListener(this);
		randomEncounterChance.removeChangeListener(this);
		randomEncounterTable.removeActionListener(this);
		floorTexture.removeActionListener(this);
		floorMaskTexture.removeActionListener(this);
		ceilingTexture.removeActionListener(this);
		ceilingMaskTexture.removeActionListener(this);
		lightLevel.removeChangeListener(this);
		hasObject.removeActionListener(this);
		objectName.addActionListener(this);
		northTexture.removeActionListener(this);
		southTexture.removeActionListener(this);
		eastTexture.removeActionListener(this);
		westTexture.removeActionListener(this);
		isLightSource.removeActionListener(this);
		for (JCheckBox b : placementMask)
		{
			b.removeActionListener(this);
		}
		
		terrainType.setSelectedItem(tile.getTerrainType()==null?"":tile.getTerrainType());
		terrainSubType.setText(tile.getTerrainSubType()==null?"":tile.getTerrainSubType());
		statModifier.refresh(tile.getStatModifier());
		randomEncounterChance.setValue(tile.getRandomEncounterChance());
		randomEncounterTable.setSelectedItem(tile.getRandomEncounters()==null?EditorPanel.NONE:tile.getRandomEncounters().getName());
		tileScript.refresh(tile.getScripts(), zone);
		
		floorTexture.setSelectedItem(tile.getFloorTexture()==null?EditorPanel.NONE:tile.getFloorTexture().getName());
		floorMaskTexture.setSelectedItem(tile.getFloorMaskTexture()==null?EditorPanel.NONE:tile.getFloorMaskTexture().getName());
		ceilingTexture.setSelectedItem(tile.getCeilingTexture()==null?EditorPanel.NONE:tile.getCeilingTexture().getName());
		ceilingMaskTexture.setSelectedItem(tile.getCeilingMaskTexture()==null?EditorPanel.NONE:tile.getCeilingMaskTexture().getName());
		lightLevel.setValue(tile.getLightLevel());

		if (index >= 0)
		{
			this.indexLabel.setText(index+" = ("+x+","+y+")");

			//
			// todo
			// yes, i know that this doesn't allow more than one object per tile.
			// go hack the txt file.
			//
			EngineObject obj = null;
			for (EngineObject o : zone.getMap().getOriginalObjects())
			{
				if (o.getTileIndex() == index)
				{
					obj = o;
					break;
				}
			}
			if (obj != null)
			{
				enableObjectWidgets();

				if (obj.getName() != null)
				{
					objectName.setText(obj.getName());
				}
				northTexture.setSelectedItem(obj.getNorthTexture().getName());
				southTexture.setSelectedItem(obj.getSouthTexture().getName());
				eastTexture.setSelectedItem(obj.getEastTexture().getName());
				westTexture.setSelectedItem(obj.getWestTexture().getName());
				isLightSource.setSelected(obj.isLightSource());
				BitSet mask = obj.getPlacementMask();
				MouseClickScriptAdapter m = ((MouseClickScriptAdapter)obj.getMouseClickScript());
				mouseClickScript.refresh(m==null?null:m.getScript(), zone);
				if (mask == null)
				{
					mask = new BitSet();
					mask.set(EngineObject.Placement.CENTER);
				}
				for (int i= EngineObject.Placement.NORTH_WEST; i<= EngineObject.Placement.SOUTH_EAST; i++)
				{
					placementMask.get(i).setSelected(mask.get(i));
				}
			}
			else
			{
				disableObjectWidgets();

				objectName.setText("");
				northTexture.setSelectedIndex(0);
				southTexture.setSelectedIndex(0);
				eastTexture.setSelectedIndex(0);
				westTexture.setSelectedIndex(0);
				isLightSource.setSelected(false);
				mouseClickScript.refresh(null, zone);
				for (JCheckBox b : placementMask)
				{
					b.setSelected(false);
				}
			}
		}
		terrainType.addKeyListener(this);
		terrainSubType.addKeyListener(this);
		randomEncounterChance.addChangeListener(this);
		randomEncounterTable.addActionListener(this);
		floorTexture.addActionListener(this);
		floorMaskTexture.addActionListener(this);
		ceilingTexture.addActionListener(this);
		ceilingMaskTexture.addActionListener(this);
		lightLevel.addChangeListener(this);
		hasObject.addActionListener(this);
		objectName.addActionListener(this);
		northTexture.addActionListener(this);
		southTexture.addActionListener(this);
		eastTexture.addActionListener(this);
		westTexture.addActionListener(this);
		isLightSource.addActionListener(this);
		for (JCheckBox b : placementMask)
		{
			b.addActionListener(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void enableObjectWidgets()
	{
		hasObject.setSelected(true);
		objectName.setEnabled(true);
		northTexture.setEnabled(true);
		southTexture.setEnabled(true);
		eastTexture.setEnabled(true);
		westTexture.setEnabled(true);
		mouseClickScript.setEnabled(true);
		isLightSource.setEnabled(true);
		for (JCheckBox b : placementMask)
		{
			b.setEnabled(true);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void disableObjectWidgets()
	{
		hasObject.setSelected(false);
		objectName.setEnabled(false);
		northTexture.setEnabled(false);
		southTexture.setEnabled(false);
		eastTexture.setEnabled(false);
		westTexture.setEnabled(false);
		isLightSource.setEnabled(false);
		mouseClickScript.setEnabled(false);
		for (JCheckBox b : placementMask)
		{
			b.setEnabled(false);
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
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		mclachlan.crusader.Map map = zone.getMap();
		
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
				tile.setTerrainType((String)terrainType.getSelectedItem());
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
		else if (e.getSource() == hasObject)
		{
			if (hasObject.isSelected())
			{
				enableObjectWidgets();
				Texture tx = Database.getInstance().getMazeTexture(
					(String)northTexture.getSelectedItem()).getTexture();
				String name = objectName.getText();
				if (name.length() == 0)
				{
					name = null;
				}
				map.addObject(new EngineObject(
					name,tx,tx,tx,tx, index, false, null, null));
			}
			else
			{
				disableObjectWidgets();
				map.removeObject(index);
			}
		}
		else if (e.getSource() == northTexture)
		{
			map.getObject(index).setNorthTexture(
				Database.getInstance().getMazeTexture((String)northTexture.getSelectedItem()).getTexture());
		}
		else if (e.getSource() == southTexture)
		{
			map.getObject(index).setSouthTexture(
				Database.getInstance().getMazeTexture((String)southTexture.getSelectedItem()).getTexture());
		}
		else if (e.getSource() == eastTexture)
		{
			map.getObject(index).setEastTexture(
				Database.getInstance().getMazeTexture((String)eastTexture.getSelectedItem()).getTexture());
		}
		else if (e.getSource() == westTexture)
		{
			map.getObject(index).setWestTexture(
				Database.getInstance().getMazeTexture((String)westTexture.getSelectedItem()).getTexture());
		}
		else if (e.getSource() == isLightSource)
		{
			map.getObject(index).setLightSource(isLightSource.isSelected());
		}
		else if (placementMask.contains(e.getSource()))
		{
			int i = placementMask.indexOf(e.getSource());
			EngineObject obj = map.getObject(this.index);
			BitSet mask = obj.getPlacementMask();
			if (mask == null)
			{
				mask = new BitSet();
			}
			mask.set(i, placementMask.get(i).isSelected());
			obj.setPlacementMask(mask);
		}
		else if (e.getSource() == quickObjectTexture)
		{
			southTexture.setSelectedItem(northTexture.getSelectedItem());
			eastTexture.setSelectedItem(northTexture.getSelectedItem());
			westTexture.setSelectedItem(northTexture.getSelectedItem());

			map.getObject(index).setSouthTexture(
				Database.getInstance().getMazeTexture((String)southTexture.getSelectedItem()).getTexture());
			map.getObject(index).setEastTexture(
				Database.getInstance().getMazeTexture((String)eastTexture.getSelectedItem()).getTexture());
			map.getObject(index).setWestTexture(
				Database.getInstance().getMazeTexture((String)westTexture.getSelectedItem()).getTexture());
		}
		else if (e.getSource() == objectName)
		{
			if (objectName.getText().length() > 0)
			{
				map.getObject(index).setName(objectName.getText());
			}
			else
			{
				map.getObject(index).setName(null);
			}
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
		else if (component == mouseClickScript)
		{
			EngineObject obj = zone.getMap().getObject(this.index);
			obj.setMouseClickScript(
				new MouseClickScriptAdapter(mouseClickScript.getScript()));
		}
	}
}
