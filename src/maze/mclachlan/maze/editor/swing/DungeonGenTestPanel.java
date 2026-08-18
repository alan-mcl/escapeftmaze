/*
 * Copyright (c) 2026 Alan McLachlan
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
import java.util.Collections;
import java.util.Vector;
import javax.swing.*;
import mclachlan.dungeongen.*;
import mclachlan.dungeongen.fragment.FragmentDungeonGen;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.DataObject;
import mclachlan.maze.editor.swing.map.MapEditorDialog;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.MapGenZoneScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;

/**
 * Editor tool: generate cloned dungeon layouts into the map editor.
 */
public class DungeonGenTestPanel extends JPanel implements ActionListener, IEditorPanel
{
	private JComboBox<String> shellZone;
	private JComboBox<String> generator;
	private JComboBox<String> layoutUsage;
	private JSpinner seed;
	private JSpinner depth;
	private JSpinner mapSize;
	private JSpinner noiseAttempts;
	private JSpinner noiseMinRoom;
	private JSpinner noiseMaxRoom;
	private JSpinner noiseTolerance;
	private JSpinner fragmentMinRooms;
	private JSpinner fragmentTargetRooms;
	private JSpinner fragmentAttempts;
	private JCheckBox fullPipeline;
	private JPanel fragmentPanel;
	private JPanel noisePanel;
	private Campaign campaign;

	public DungeonGenTestPanel()
	{
		setLayout(new BorderLayout(8, 8));

		JPanel form = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;

		shellZone = new JComboBox<>();
		addRow(form, gbc, "Shell zone:", shellZone);

		generator = new JComboBox<>();
		generator.addActionListener(this);
		addRow(form, gbc, "Generator:", generator);

		layoutUsage = new JComboBox<>();
		addRow(form, gbc, "Fragment usage:", layoutUsage);

		seed = new JSpinner(new SpinnerNumberModel(42, 0, Integer.MAX_VALUE, 1));
		addRow(form, gbc, "Seed:", seed);

		depth = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
		addRow(form, gbc, "Depth:", depth);

		mapSize = new JSpinner(new SpinnerNumberModel(15, 7, 63, 2));
		addRow(form, gbc, "Map size (odd):", mapSize);

		noisePanel = new JPanel(new GridLayout(0, 2, 4, 4));
		noiseAttempts = spinner(500, 1, 5000);
		noiseMinRoom = spinner(3, 1, 20);
		noiseMaxRoom = spinner(7, 1, 20);
		noiseTolerance = spinner(3, 0, 10);
		noisePanel.add(new JLabel("Room attempts:"));
		noisePanel.add(noiseAttempts);
		noisePanel.add(new JLabel("Min room size:"));
		noisePanel.add(noiseMinRoom);
		noisePanel.add(new JLabel("Max room size:"));
		noisePanel.add(noiseMaxRoom);
		noisePanel.add(new JLabel("Aspect tolerance:"));
		noisePanel.add(noiseTolerance);
		addRow(form, gbc, "Noise4j:", noisePanel);

		fragmentPanel = new JPanel(new GridLayout(0, 2, 4, 4));
		fragmentMinRooms = spinner(3, 1, 20);
		fragmentTargetRooms = spinner(3, 1, 20);
		fragmentAttempts = spinner(8, 1, 64);
		fragmentPanel.add(new JLabel("Min rooms:"));
		fragmentPanel.add(fragmentMinRooms);
		fragmentPanel.add(new JLabel("Target rooms:"));
		fragmentPanel.add(fragmentTargetRooms);
		fragmentPanel.add(new JLabel("Max attempts:"));
		fragmentPanel.add(fragmentAttempts);
		addRow(form, gbc, "Fragment:", fragmentPanel);

		fullPipeline = new JCheckBox("Run zone script init (full campaign pipeline)");
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		form.add(fullPipeline, gbc);

		JButton generate = new JButton("Generate preview");
		generate.addActionListener(this);
		gbc.gridy++;
		form.add(generate, gbc);

		add(form, BorderLayout.NORTH);
	}

	@Override
	public void initForeignKeys()
	{
	}

	/*-------------------------------------------------------------------------*/
	public void refreshCampaign(Campaign campaign)
	{
		this.campaign = campaign;
		Vector<String> zones = new Vector<>(Database.getInstance().getZoneNames());
		Collections.sort(zones);
		shellZone.setModel(new DefaultComboBoxModel<>(zones));
		if (zones.contains("temple.1"))
		{
			shellZone.setSelectedItem("temple.1");
		}

		java.util.List<String> ids = DungeonGens.idsFor(campaign);
		generator.setModel(new DefaultComboBoxModel<>(new Vector<>(ids)));
		String defaultId = campaign.getDefaultDungeonGenerator();
		if (defaultId != null && !defaultId.isEmpty())
		{
			generator.setSelectedItem(defaultId);
		}

		java.util.List<String> themes = campaign.getFragmentLayoutThemes();
		if (themes.isEmpty())
		{
			themes = java.util.List.of("barracks");
		}
		layoutUsage.setModel(new DefaultComboBoxModel<>(new Vector<>(themes)));

		updateGeneratorPanels();
	}

	/*-------------------------------------------------------------------------*/
	private void updateGeneratorPanels()
	{
		String id = (String)generator.getSelectedItem();
		boolean fragment = DungeonGens.FRAGMENT.equals(id);
		fragmentPanel.setVisible(fragment);
		layoutUsage.setEnabled(fragment);
		noisePanel.setVisible(!fragment);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == generator)
		{
			updateGeneratorPanels();
			return;
		}

		try
		{
			generatePreview();
		}
		catch (Exception ex)
		{
			throw new MazeException(ex);
		}
	}

	private void generatePreview() throws Exception
	{
		String shellName = (String)shellZone.getSelectedItem();
		if (shellName == null || shellName.isEmpty())
		{
			throw new MazeException("Select a shell zone");
		}

		String previewName = shellName + "_preview.dungeongen";
		Zone zone = MapEditorDialog.cloneShell(shellName, previewName);
		int size = ((Number)mapSize.getValue()).intValue();
		if (size % 2 == 0)
		{
			size++;
			mapSize.setValue(size);
		}
		ZoneShell.ensureSize(zone, size);

		int dungeonLevel = ((Number)depth.getValue()).intValue();
		long runSeed = ((Number)seed.getValue()).longValue();

		if (fullPipeline.isSelected() && zone.getScript() instanceof MapGenZoneScript)
		{
			MazeVariables.set("map.seed." + previewName, Long.toString(runSeed));
			zone.getScript().init(zone, 0);
		}
		else
		{
			String genId = (String)generator.getSelectedItem();
			DungeonGen gen = buildGenerator(genId);
			PreviewDecorator decorator = new PreviewDecorator(zone);
			DungeonGenContext ctx = DungeonGenContext.builder()
				.stairwellPlanner(new Noise4jStairwellPlanner())
				.build();
			gen.generate(zone, runSeed, dungeonLevel, decorator, ctx);
		}

		MapEditorDialog.open(SwingEditor.instance, zone);
	}

	private DungeonGen buildGenerator(String genId)
	{
		if (DungeonGens.FRAGMENT.equals(genId))
		{
			String usage = (String)layoutUsage.getSelectedItem();
			FragmentDungeonGen.Options options = new FragmentDungeonGen.Options(
				usage,
				((Number)fragmentMinRooms.getValue()).intValue(),
				((Number)fragmentTargetRooms.getValue()).intValue(),
				((Number)fragmentAttempts.getValue()).intValue(),
				1);
			return new FragmentDungeonGen(options);
		}
		if (DungeonGens.NOISE4J.equals(genId))
		{
			return new Noise4jDungeonGen(new Noise4jDungeonGen.Options(
				((Number)noiseAttempts.getValue()).intValue(),
				((Number)noiseMinRoom.getValue()).intValue(),
				((Number)noiseMaxRoom.getValue()).intValue(),
				((Number)noiseTolerance.getValue()).intValue()));
		}
		return DungeonGens.create(genId);
	}

	private static JSpinner spinner(int value, int min, int max)
	{
		return new JSpinner(new SpinnerNumberModel(value, min, max, 1));
	}

	private static void addRow(JPanel panel, GridBagConstraints gbc, String label, Component field)
	{
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.gridx = 0;
		panel.add(new JLabel(label), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(field, gbc);
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy++;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Vector<DataObject> loadData()
	{
		return null;
	}

	@Override
	public void refresh(String name)
	{
	}

	@Override
	public DataObject newItem(String name)
	{
		return null;
	}

	@Override
	public void renameItem(String newName)
	{
	}

	@Override
	public DataObject copyItem(String newName)
	{
		return null;
	}

	@Override
	public void deleteItem()
	{
	}

	@Override
	public DataObject commit(String name)
	{
		return null;
	}

	@Override
	public String getCurrentName()
	{
		return null;
	}

	@Override
	public void refreshNames(String name)
	{
	}

	@Override
	public int getDirtyFlag()
	{
		return -1;
	}

	@Override
	public void reload()
	{
	}
}
