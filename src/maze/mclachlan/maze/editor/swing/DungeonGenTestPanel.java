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
import java.util.Random;
import java.util.Vector;
import javax.swing.*;
import mclachlan.dungeongen.*;
import mclachlan.dungeongen.fragment.FragmentCatalog;
import mclachlan.dungeongen.fragment.FragmentDungeonGen;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.DataObject;
import mclachlan.maze.editor.swing.map.MapEditorDialog;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.map.MapGenZoneScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;

/**
 * Editor tool: generate cloned dungeon layouts into the map editor.
 */
public class DungeonGenTestPanel extends JPanel implements ActionListener, IEditorPanel
{
	private static final int FRAGMENT_DEFAULT_MAP_SIZE = 31;
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
	private CardLayout generatorCards;
	private JPanel generatorCardPanel;
	private Campaign campaign;

	public DungeonGenTestPanel()
	{
		setLayout(new GridBagLayout());
		GridBagConstraints outer = createGridBagConstraints();
		outer.weighty = 1.0;
		add(buildFormPanel(), outer);

		outer.gridx = 1;
		outer.weightx = 1.0;
		add(new JLabel(), outer);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel buildFormPanel()
	{
		JPanel form = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		shellZone = sizedCombo();
		dodgyGridBagShite(form, new JLabel("Shell zone:"), shellZone, gbc);

		generator = sizedCombo();
		generator.addActionListener(this);
		dodgyGridBagShite(form, new JLabel("Generator:"), generator, gbc);

		JPanel seedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		seed = new JSpinner(new SpinnerNumberModel(42, 0, Integer.MAX_VALUE, 1));
		seed.setPreferredSize(new Dimension(120, seed.getPreferredSize().height));
		JButton randomiseSeed = new JButton("Randomise");
		randomiseSeed.addActionListener(this);
		seedPanel.add(seed);
		seedPanel.add(randomiseSeed);
		dodgyGridBagShite(form, new JLabel("Seed:"), seedPanel, gbc);

		depth = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
		depth.setPreferredSize(new Dimension(80, depth.getPreferredSize().height));
		dodgyGridBagShite(form, new JLabel("Depth:"), depth, gbc);

		mapSize = new JSpinner(new SpinnerNumberModel(15, 7, 63, 2));
		mapSize.setPreferredSize(new Dimension(80, mapSize.getPreferredSize().height));
		dodgyGridBagShite(form, new JLabel("Map size (odd):"), mapSize, gbc);

		generatorCards = new CardLayout();
		generatorCardPanel = new JPanel(generatorCards);
		generatorCardPanel.setBorder(BorderFactory.createTitledBorder("Generator settings"));
		generatorCardPanel.add(buildNoisePanel(), DungeonGens.NOISE4J);
		generatorCardPanel.add(buildFragmentPanel(), DungeonGens.FRAGMENT);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_START;
		form.add(generatorCardPanel, gbc);

		fullPipeline = new JCheckBox("Run zone script init (full campaign pipeline)");
		gbc.gridy++;
		gbc.gridwidth = 2;
		form.add(fullPipeline, gbc);

		JButton generate = new JButton("Generate preview");
		generate.addActionListener(this);
		gbc.gridy++;
		form.add(generate, gbc);

		gbc.gridy++;
		gbc.weighty = 1.0;
		form.add(new JLabel(), gbc);
		return form;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel buildNoisePanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		noiseAttempts = spinner(500, 1, 5000);
		noiseMinRoom = spinner(3, 1, 20);
		noiseMaxRoom = spinner(7, 1, 20);
		noiseTolerance = spinner(3, 0, 10);

		dodgyGridBagShite(panel, new JLabel("Room attempts:"), noiseAttempts, gbc);
		dodgyGridBagShite(panel, new JLabel("Min room size:"), noiseMinRoom, gbc);
		dodgyGridBagShite(panel, new JLabel("Max room size:"), noiseMaxRoom, gbc);
		dodgyGridBagShite(panel, new JLabel("Aspect tolerance:"), noiseTolerance, gbc);

		gbc.gridy++;
		gbc.weighty = 1.0;
		panel.add(new JLabel(), gbc);
		return panel;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel buildFragmentPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		layoutUsage = sizedCombo();
		fragmentMinRooms = spinner(3, 1, 20);
		fragmentTargetRooms = spinner(3, 1, 20);
		fragmentAttempts = spinner(32, 1, 64);

		dodgyGridBagShite(panel, new JLabel("Fragment usage:"), layoutUsage, gbc);
		dodgyGridBagShite(panel, new JLabel("Min rooms:"), fragmentMinRooms, gbc);
		dodgyGridBagShite(panel, new JLabel("Target rooms:"), fragmentTargetRooms, gbc);
		dodgyGridBagShite(panel, new JLabel("Max attempts:"), fragmentAttempts, gbc);

		gbc.gridy++;
		gbc.weighty = 1.0;
		panel.add(new JLabel(), gbc);
		return panel;
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

		java.util.List<String> usages = FragmentCatalog.usageIds();
		if (usages.isEmpty())
		{
			usages = java.util.List.of("barracks");
		}
		layoutUsage.setModel(new DefaultComboBoxModel<>(new Vector<>(usages)));

		updateGeneratorPanels();
	}

	/*-------------------------------------------------------------------------*/
	private void updateGeneratorPanels()
	{
		String id = (String)generator.getSelectedItem();
		if (id == null)
		{
			id = DungeonGens.NOISE4J;
		}
		generatorCards.show(generatorCardPanel, id);
		maybeBumpFragmentMapSize(id);
	}

	/*-------------------------------------------------------------------------*/
	private void maybeBumpFragmentMapSize(String generatorId)
	{
		if (DungeonGens.FRAGMENT.equals(generatorId)
			&& ((Number)mapSize.getValue()).intValue() == 15)
		{
			mapSize.setValue(FRAGMENT_DEFAULT_MAP_SIZE);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() instanceof JButton button
			&& "Randomise".equals(button.getText()))
		{
			seed.setValue(new Random().nextInt(Integer.MAX_VALUE));
			return;
		}

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

		String genId = (String)generator.getSelectedItem();
		if (genId == null || genId.isEmpty())
		{
			genId = DungeonGens.NOISE4J;
		}
		maybeBumpFragmentMapSize(genId);

		int size = normaliseOddSize(((Number)mapSize.getValue()).intValue());
		mapSize.setValue(size);

		String previewName = shellName + "_preview.dungeongen";
		Zone zone = MapEditorDialog.cloneShell(shellName, previewName);
		ZoneShell.ensureSize(zone, size);

		int dungeonLevel = ((Number)depth.getValue()).intValue();
		long runSeed = ((Number)seed.getValue()).longValue();

		try
		{
			if (fullPipeline.isSelected() && zone.getScript() instanceof MapGenZoneScript)
			{
				DungeonGenPreview.apply(
					size,
					genId,
					runSeed,
					(String)layoutUsage.getSelectedItem(),
					((Number)fragmentMinRooms.getValue()).intValue(),
					((Number)fragmentTargetRooms.getValue()).intValue(),
					((Number)fragmentAttempts.getValue()).intValue());
				zone.getScript().init(zone, 0);
			}
			else
			{
				DungeonGen gen = buildGenerator(genId);
				PreviewDecorator decorator = new PreviewDecorator(zone);
				DungeonGenContext ctx = DungeonGenContext.builder()
					.stairwellPlanner(new Noise4jStairwellPlanner())
					.build();
				gen.generate(zone, runSeed, dungeonLevel, decorator, ctx);
			}
		}
		catch (IllegalStateException ex)
		{
			JOptionPane.showMessageDialog(
				SwingEditor.instance,
				ex.getMessage()
					+ "\n\nTry increasing map size (31+) or max attempts.",
				"Fragment assembly failed",
				JOptionPane.WARNING_MESSAGE);
			return;
		}
		finally
		{
			DungeonGenPreview.clearAll();
		}

		MapEditorDialog.open(SwingEditor.instance, zone);
	}

	private DungeonGen buildGenerator(String genId)
	{
		if (DungeonGens.FRAGMENT.equals(genId))
		{
			String usage = (String)layoutUsage.getSelectedItem();
			if (usage == null || usage.isEmpty())
			{
				throw new MazeException("Select a fragment usage");
			}
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

	private static int normaliseOddSize(int size)
	{
		if (size < 7)
		{
			return 7;
		}
		if (size > 63)
		{
			return 63;
		}
		if (size % 2 == 0)
		{
			return size + 1;
		}
		return size;
	}

	private static JComboBox<String> sizedCombo()
	{
		JComboBox<String> combo = new JComboBox<>();
		combo.setPrototypeDisplayValue("fragment.barracks.room.entry");
		return combo;
	}

	private static JSpinner spinner(int value, int min, int max)
	{
		JSpinner result = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
		result.setPreferredSize(new Dimension(80, result.getPreferredSize().height));
		return result;
	}

	private static GridBagConstraints createGridBagConstraints()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		return gbc;
	}

	private static void dodgyGridBagShite(
		JPanel panel,
		Component label,
		Component field,
		GridBagConstraints gbc)
	{
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(field, gbc);
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
