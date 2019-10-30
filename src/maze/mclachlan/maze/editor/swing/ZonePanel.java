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

import java.util.*;
import mclachlan.crusader.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.Tile;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.editor.swing.map.MapEditor;
import mclachlan.maze.map.*;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ZonePanel extends EditorPanel
{
	private JLabel width, length;
	private JCheckBox isCustomZoneScript;
	private JSpinner defaultZoneScriptTurns;
	private JSpinner defaultZoneScriptLightLevelDiff;
	private JTextField customZoneScript;
	private JComboBox skyTexture;
	private JButton editMap;
	private JButton shadeTargetColour, transparentColour;
	private JCheckBox doLighting, doShading;
	private JSpinner shadingDistance, shadingMultiplier, projectionPlaneOffset,
		scaleDistFromProjPlane;
	private JComboBox playerFieldOfView;
	private MazeScriptPercentageTablePanel ambientScripts;
	private MapScriptListPanel mapScripts;
	private JSpinner order, playerOriginX, playerOriginY;
	private Zone zone;

	/*-------------------------------------------------------------------------*/
	public ZonePanel()
	{
		super(SwingEditor.Tab.ZONES);
	}

	/*-------------------------------------------------------------------------*/
	public Container getEditControls()
	{
		JTabbedPane result = new JTabbedPane();

		result.add("Base", getBasePanel());
		result.add("Zone Script", getZoneScriptPanel());
		result.add("Crusader Map Scripts", getMapScriptPanel());
		result.add("Balancing Data", getBalancePanel());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Component getBalancePanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		order = new JSpinner(new SpinnerNumberModel(0,-1,99999,1));
		order.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Zone Order:"), order, gbc);

		playerOriginX = new JSpinner(new SpinnerNumberModel(0,0,9999,1));
		playerOriginX.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Player Origin X:"), playerOriginX, gbc);

		playerOriginY = new JSpinner(new SpinnerNumberModel(0,0,9999,1));
		playerOriginY.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Player Origin Y:"), playerOriginY, gbc);

		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel(), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(new JLabel(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Component getMapScriptPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		mapScripts = new MapScriptListPanel(dirtyFlag);
		result.add(mapScripts, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Component getZoneScriptPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		isCustomZoneScript = new JCheckBox("Custom?");
		isCustomZoneScript.addActionListener(this);
		result.add(isCustomZoneScript, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Custom Script:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		customZoneScript = new JTextField(30);
		customZoneScript.addKeyListener(this);
		result.add(customZoneScript, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Turns Before Sky Changes:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		defaultZoneScriptTurns = new JSpinner(new SpinnerNumberModel(-1, -1, 256, 1));
		defaultZoneScriptTurns.addChangeListener(this);
		result.add(defaultZoneScriptTurns, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Light Level Diff:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		defaultZoneScriptLightLevelDiff = new JSpinner(new SpinnerNumberModel(-1, -1, 32, 1));
		defaultZoneScriptLightLevelDiff.addChangeListener(this);
		result.add(defaultZoneScriptLightLevelDiff, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 2;
		ambientScripts = new MazeScriptPercentageTablePanel(
			"Ambient Scripts", this.dirtyFlag, .75, .3);
		result.add(ambientScripts, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getBasePanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		result.add(new JLabel("Width (E-W):"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		width = new JLabel();
		result.add(width, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Length (N-S):"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		length = new JLabel();
		result.add(length, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Sky Texture:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		Vector<String> textures = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(textures);
		skyTexture = new JComboBox(textures);
		skyTexture.addActionListener(this);
		result.add(skyTexture, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Shade Target Colour:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		shadeTargetColour = new JButton("...");
		shadeTargetColour.addActionListener(this);
		result.add(shadeTargetColour, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Transparent Colour:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		transparentColour = new JButton("...");
		transparentColour.addActionListener(this);
		result.add(transparentColour, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		doShading = new JCheckBox("Do Shading?");
		doShading.addActionListener(this);
		result.add(doShading, gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		result.add(new JLabel(), gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		doLighting = new JCheckBox("Do Lighting?");
		doLighting.addActionListener(this);
		result.add(doLighting, gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		result.add(new JLabel(), gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Shading Distance:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		shadingDistance = new JSpinner(new SpinnerNumberModel(1, 0, 99.00, 0.01));
		shadingDistance.addChangeListener(this);
		result.add(shadingDistance, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Shading Multiplier:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		shadingMultiplier = new JSpinner(new SpinnerNumberModel(1, 0, 99.00, 0.01));
		shadingMultiplier.addChangeListener(this);
		result.add(shadingMultiplier, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Projection Plane Offset:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		projectionPlaneOffset = new JSpinner(new SpinnerNumberModel(0, -999, 999, 1));
		projectionPlaneOffset.addChangeListener(this);
		result.add(projectionPlaneOffset, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Player Field Of View:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		playerFieldOfView = new JComboBox(new Object[]
			{
				"30 degrees",
				"60 degrees",
				"90 degrees",
				"180 degrees"
			});
		playerFieldOfView.addActionListener(this);
		result.add(playerFieldOfView, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0.0;
		result.add(new JLabel("Scale Dist From Proj Plane:"), gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		scaleDistFromProjPlane = new JSpinner(new SpinnerNumberModel(0, -1, 1, 0.01));
		scaleDistFromProjPlane.addChangeListener(this);
		result.add(scaleDistFromProjPlane, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		editMap = new JButton("Edit Map");
		editMap.addActionListener(this);
		result.add(editMap, gbc);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getZoneNames());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		zone = Database.getInstance().getZone(name);

		isCustomZoneScript.removeActionListener(this);
		customZoneScript.removeKeyListener(this);
		defaultZoneScriptTurns.removeChangeListener(this);
		defaultZoneScriptLightLevelDiff.removeChangeListener(this);
		skyTexture.removeActionListener(this);
		doShading.removeActionListener(this);
		doLighting.removeActionListener(this);
		shadingDistance.removeChangeListener(this);
		shadingMultiplier.removeChangeListener(this);
		projectionPlaneOffset.removeChangeListener(this);
		playerFieldOfView.removeActionListener(this);
		scaleDistFromProjPlane.removeChangeListener(this);
		order.removeChangeListener(this);
		playerOriginX.removeChangeListener(this);
		playerOriginY.removeChangeListener(this);
		
		width.setText(""+zone.getWidth());
		length.setText(""+zone.getLength());
		
		if (zone.getScript() instanceof DefaultZoneScript)
		{
			DefaultZoneScript dzs = (DefaultZoneScript)zone.getScript();

			isCustomZoneScript.setSelected(false);
			customZoneScript.setText("");
			customZoneScript.setEnabled(false);
			customZoneScript.setEditable(false);
			defaultZoneScriptTurns.setEnabled(true);
			defaultZoneScriptLightLevelDiff.setEnabled(true);
			ambientScripts.setEnabled(true);

			defaultZoneScriptTurns.setValue(dzs.getTurnsBetweenChange());
			defaultZoneScriptLightLevelDiff.setValue(dzs.getLightLevelDiff());
			PercentageTable<String> s = dzs.getAmbientScripts();
			ambientScripts.refresh(s);
		}
		else
		{
			isCustomZoneScript.setSelected(true);
			customZoneScript.setText(zone.getScript().getClass().getName());
			customZoneScript.setEnabled(true);
			customZoneScript.setEditable(true);
			defaultZoneScriptTurns.setEnabled(false);
			defaultZoneScriptLightLevelDiff.setEnabled(false);
			defaultZoneScriptTurns.setValue(0);
			defaultZoneScriptLightLevelDiff.setValue(0);
			ambientScripts.setEnabled(false);
			ambientScripts.refresh(null);
		}

		mapScripts.refresh(zone.getMap().getScripts());		
		doShading.setSelected(zone.doShading());
		doLighting.setSelected(zone.doLighting());
		shadingDistance.setValue(zone.getShadingDistance());
		shadingMultiplier.setValue(zone.getShadingMultiplier());
		projectionPlaneOffset.setValue(zone.getProjectionPlaneOffset());
		playerFieldOfView.setSelectedIndex(zone.getPlayerFieldOfView()-1); // yes, another one of those unfortunate offsets
		scaleDistFromProjPlane.setValue(zone.getScaleDistFromProjPlane());
		shadeTargetColour.setBackground(zone.getShadeTargetColor());
		transparentColour.setBackground(zone.getTransparentColor());
		skyTexture.setSelectedItem(zone.getMap().getSkyTexture().getName());

		order.setValue(zone.getOrder());
		playerOriginX.setValue(zone.getPlayerOrigin().x);
		playerOriginY.setValue(zone.getPlayerOrigin().y);

		isCustomZoneScript.addActionListener(this);
		customZoneScript.addKeyListener(this);
		skyTexture.addActionListener(this);
		defaultZoneScriptTurns.addChangeListener(this);
		defaultZoneScriptLightLevelDiff.addChangeListener(this);
		doShading.addActionListener(this);
		doLighting.addActionListener(this);
		shadingDistance.addChangeListener(this);
		shadingMultiplier.addChangeListener(this);
		projectionPlaneOffset.addChangeListener(this);
		playerFieldOfView.addActionListener(this);
		scaleDistFromProjPlane.addChangeListener(this);
		order.addChangeListener(this);
		playerOriginX.addChangeListener(this);
		playerOriginY.addChangeListener(this);
	}
	
	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
		
		if (e.getSource() == isCustomZoneScript)
		{
			if (isCustomZoneScript.isSelected())
			{
				customZoneScript.setEnabled(true);
				customZoneScript.setEditable(true);
				defaultZoneScriptTurns.setEnabled(false);
				defaultZoneScriptLightLevelDiff.setEnabled(false);
			}
			else
			{
				customZoneScript.setEnabled(false);
				customZoneScript.setEditable(false);
				defaultZoneScriptTurns.setEnabled(true);
				defaultZoneScriptLightLevelDiff.setEnabled(true);
			}
		}
		else if (e.getSource() == editMap)
		{
			JDialog dialog = new JDialog(SwingEditor.instance, "Map Editor", true);
			dialog.add(new MapEditor(Database.getInstance().getZone(currentName), dialog, this));
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			int centerX = (int)(d.getWidth()/2);
			int centerY = (int)(d.getHeight()/2);
			int width = (int)(d.getWidth()-150);
			int height = (int)(d.getHeight()-150);

			dialog.setBounds(centerX-width/2, centerY-height/2, width, height);
			dialog.setVisible(true);
		}
		else if (e.getSource() == shadeTargetColour)
		{
			Color c = JColorChooser.showDialog(
				SwingEditor.instance,
				"Shade Target Colour",
				Color.BLACK);
			
			shadeTargetColour.setBackground(c);
		}
		else if (e.getSource() == transparentColour)
		{
			Color c = JColorChooser.showDialog(
				SwingEditor.instance,
				"Transparent Colour",
				Color.BLACK);
			
			transparentColour.setBackground(c);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		ambientScripts.initForeignKeys();

//		Vector<String> textures = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
//		Collections.sort(textures);
//		skyTexture = new JComboBox(textures);
//		if (zone != null)
//		{
		// todo: this line is wrong
//			skyTexture.setSelectedItem(zone.getMap().getSkyImage().getName());
//		}
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		final JDialog dialog = new JDialog(SwingEditor.instance, "New Zone", true);
		dialog.setLayout(new BorderLayout());
		
		JSpinner lengthSpinner = new JSpinner(new SpinnerNumberModel(16, 1, 1024, 1));
		JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(16, 1, 1024, 1));
		JSpinner textureSizeSpinner = new JSpinner(new SpinnerNumberModel(512, 1, 1024, 1));
		
		Vector<String> textures = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(textures);
		
		JComboBox initialWallTexture = new JComboBox(textures);
		JComboBox initialFloorTexture = new JComboBox(textures);
		JComboBox initialCeilingTexture = new JComboBox(textures);
		JComboBox initialSkyTexture = new JComboBox(textures);
		
		Vector<String> encounters = new Vector<String>(
			Database.getInstance().getEncounterTables().keySet());
		Collections.sort(encounters);
		
		JComboBox initialEncounters = new JComboBox(encounters);
		
		JComboBox initialTerrainType = new JComboBox(mclachlan.maze.map.Tile.TerrainType.values());
		JTextField initialTerrainSubType = new JTextField(20);
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		
		dodgyGridBagShite(panel, new JLabel("Length:"), lengthSpinner, gbc);
		dodgyGridBagShite(panel, new JLabel("Width:"), widthSpinner, gbc);
		dodgyGridBagShite(panel, new JLabel("Initial Floor:"), initialFloorTexture, gbc);
		dodgyGridBagShite(panel, new JLabel("Initial Ceiling:"), initialCeilingTexture, gbc);
		dodgyGridBagShite(panel, new JLabel("Initial Walls:"), initialWallTexture, gbc);
		dodgyGridBagShite(panel, new JLabel("Initial Sky:"), initialSkyTexture, gbc);
		dodgyGridBagShite(panel, new JLabel("Texture Size:"), textureSizeSpinner, gbc);
		dodgyGridBagShite(panel, new JLabel("Encounter Table:"), initialEncounters, gbc);
		dodgyGridBagShite(panel, new JLabel("Terrain Type:"), initialTerrainType, gbc);
		dodgyGridBagShite(panel, new JLabel("Terrain Sub Type:"), initialTerrainSubType, gbc);

		JButton dialogOk = new JButton("OK");
		dialogOk.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dialog.setVisible(false);
			}
		});
		
		JPanel buttons = new JPanel();
		buttons.add(dialogOk);
		
		dialog.add(panel, BorderLayout.CENTER);
		dialog.add(buttons, BorderLayout.SOUTH);
		
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		
		int length = (Integer)lengthSpinner.getValue();
		int width = (Integer)widthSpinner.getValue();
		int textureSize = (Integer)textureSizeSpinner.getValue();
		Texture floor = Database.getInstance().getMazeTexture((String)initialFloorTexture.getSelectedItem()).getTexture();
		Texture ceiling = Database.getInstance().getMazeTexture((String)initialCeilingTexture.getSelectedItem()).getTexture();
		Texture walls = Database.getInstance().getMazeTexture((String)initialWallTexture.getSelectedItem()).getTexture();
		MazeTexture skyTexture = Database.getInstance().getMazeTexture(
			(String)initialSkyTexture.getSelectedItem());


		Tile[] tiles = new Tile[length*width];
		for (int i = 0; i < tiles.length; i++)
		{
			tiles[i] = new Tile(
				ceiling,
				null,
				floor,
				null,
				walls,
				walls,
				walls,
				walls,
				false,
				32,
				1);
		}
		Texture[] textureArray = 
			{
				floor, ceiling, walls, skyTexture.getTexture()
			};
		Arrays.sort(textureArray);
		Wall[] horiz = new Wall[length*width + width];
		for (int i = 0; i < horiz.length; i++)
		{
			horiz[i] = new Wall(Map.NO_WALL, null, false, false, 1, null, null);
		}
		Wall[] vert = new Wall[length*width + length];
		for (int i = 0; i < vert.length; i++)
		{
			vert[i] = new Wall(Map.NO_WALL, null, false, false, 1, null, null);
		}
		mclachlan.maze.map.Tile[][] mazeTiles = new mclachlan.maze.map.Tile[length][width];
		for (int x = 0; x < mazeTiles.length; x++)
		{
			for (int y = 0; y < mazeTiles[x].length; y++)
			{
				mazeTiles[x][y] = new mclachlan.maze.map.Tile(
					null,
					Database.getInstance().getEncounterTable((String)initialEncounters.getSelectedItem()),
					new StatModifier(),
					(mclachlan.maze.map.Tile.TerrainType)initialTerrainType.getSelectedItem(),
					initialTerrainSubType.getText(),
					5,
					mclachlan.maze.map.Tile.RestingDanger.MEDIUM,
					mclachlan.maze.map.Tile.RestingEfficiency.AVERAGE);
			}
		}

		Map map = new Map(
			length, 
			width, 
			textureSize,
			Arrays.binarySearch(textureArray, skyTexture.getTexture()),
			Map.SkyTextureType.CYLINDER, // todo
			tiles, 
			textureArray, 
			horiz,
			vert,
			new EngineObject[0],
			new MapScript[0]);
		
		Zone z = new Zone(
			name, 
			map, 
			mazeTiles, 
			new Portal[0], 
			new DefaultZoneScript(-1, 0, null),
			Color.BLACK,
			Color.BLACK,
			true,
			true,
			2.5,
			2.5,
			-40,
			CrusaderEngine.FieldOfView.FOV_60_DEGREES,
			0.65,
			0,
			new Point(0,0));
		
		try
		{
			Database.getInstance().getSaver().saveZone(z);
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		JOptionPane.showMessageDialog(SwingEditor.instance, "Not supported");
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		JOptionPane.showMessageDialog(SwingEditor.instance, "Not supported");
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		try
		{
			Database.getInstance().getSaver().deleteZone(getCurrentName());
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		if (isDirty())
		{
			// only save the zone if changes were made
			zone.setDoLighting(doLighting.isSelected());
			zone.setDoShading(doShading.isSelected());
			zone.setPlayerFieldOfView(playerFieldOfView.getSelectedIndex()+1); //sigh
			zone.setProjectionPlaneOffset((Integer)projectionPlaneOffset.getValue());
			zone.setScaleDistFromProjPlane((Double)scaleDistFromProjPlane.getValue());
			zone.setShadeTargetColor(shadeTargetColour.getBackground());
			zone.setShadingDistance((Double)shadingDistance.getValue());
			zone.setShadingMultiplier((Double)shadingMultiplier.getValue());
			zone.setTransparentColor(transparentColour.getBackground());
			zone.setOrder((Integer)order.getValue());
			zone.setPlayerOrigin(new Point(
				(Integer)playerOriginX.getValue(), (Integer)playerOriginY.getValue()));

			// set the sky texture
			MazeTexture txt = Database.getInstance().getMazeTexture(
				(String)skyTexture.getSelectedItem());

			zone.getMap().setSkyTexture(txt.getTexture());
			
			if (isCustomZoneScript.isSelected())
			{
				try
				{
					Class clazz = Class.forName(customZoneScript.getText());
					zone.setScript((ZoneScript)clazz.newInstance());
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
			}
			else
			{
				zone.setScript(
					new DefaultZoneScript(
						(Integer)defaultZoneScriptTurns.getValue(),
						(Integer)defaultZoneScriptLightLevelDiff.getValue(),
						ambientScripts.getPercentageTable(false)));
			}

			zone.getMap().setScripts(
				mapScripts.getMapScripts().toArray(
					new MapScript[mapScripts.getMapScripts().size()]));
			
			try
			{
				Database.getInstance().getSaver().saveZone(zone);
			}
			catch (Exception e)
			{
				throw new MazeException(e);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void zoneWasSaved(Zone zone)
	{
		this.zone = zone;
		SwingEditor.instance.clearDirty(dirtyFlag);
		this.mapScripts.refresh(zone.getMap().getScripts());
	}
}
