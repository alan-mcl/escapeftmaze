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
import java.util.*;
import java.util.List;
import javax.swing.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.HiddenStuff;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class TileScriptEditor extends JDialog implements ActionListener
{
	private static final int CUSTOM = 0;
	private static final int CAST_SPELL = 1;
	private static final int CHEST = 2;
	private static final int ENCOUNTER = 3;
	private static final int FLAVOUR_TEXT = 4;
	private static final int LOOT = 5;
	private static final int REMOVE_WALL = 6;
	private static final int EXECUTE_MAZE_EVENTS = 7;
	private static final int SIGNBOARD = 8;
	private static final int SET_MAZE_VARIABLE = 9;
	private static final int HIDDEN_STUFF = 10;
	private static final int WATER = 11;

	private static final int MAX = 12;

	static Map<Class, Integer> types;

	static
	{
		types = new HashMap<Class, Integer>();

		types.put(CastSpell.class, CAST_SPELL);
		types.put(Chest.class, CHEST);
		types.put(Encounter.class, ENCOUNTER);
		types.put(FlavourText.class, FLAVOUR_TEXT);
		types.put(Loot.class, LOOT);
		types.put(RemoveWall.class, REMOVE_WALL);
		types.put(ExecuteMazeScript.class, EXECUTE_MAZE_EVENTS);
		types.put(SignBoard.class, SIGNBOARD);
		types.put(SetMazeVariable.class, SET_MAZE_VARIABLE);
		types.put(HiddenStuff.class, HIDDEN_STUFF);
		types.put(Water.class, WATER);
	}

	private TileScript result;

	private JButton ok, cancel;
	private JComboBox type;
	private JTextField impl;
	private int dirtyFlag;
	private Zone zone;
	private CardLayout cards;
	private JPanel controls;

	private JCheckBox executeOnce;
	private JButton executeOnceQuick;
	private JTextField executeOnceMazeVariable;
	private JCheckBox north, south, east, west;
	private JCheckBox reexecuteOnSameTile;

	private JComboBox spell;
	private JSpinner castingLevel;
	private JSpinner casterLevel;
	private SingleTileScriptComponent chestContents;
	private TrapPercentageTablePanel trap;
	private JTextField chestMazeVariable;
	private JComboBox chestNorthTexture;
	private JComboBox chestSouthTexture;
	private JComboBox chestEastTexture;
	private JComboBox chestWestTexture;
	private JComboBox encounterTable;
	private JComboBox chestPreScript;
	private JTextField encounterVariable;
	private JButton encounterQuickAssignMazeVar;
	private JTextArea flavourText;
	private JComboBox lootTable;
	private JTextField removeWallMazeVariable;
	private JSpinner removeWallWallIndex;
	private JButton removeWallQuick;
	private JCheckBox removeWallIsHoriz;
	private JComboBox mazeScript;
	private JTextArea signBoard;
	private JTextField setMazeVariableVariable;
	private JTextField setMazeVariableValue;
	private JTextField hiddenStuffVariable;
	private JComboBox hiddenStuffContents;
	private JComboBox hiddenStuffPreScript;

	/*-------------------------------------------------------------------------*/
	public TileScriptEditor(Frame owner, TileScript tileScript, int dirtyFlag, Zone zone)
	{
		super(owner, "Edit Tile Script", true);
		this.dirtyFlag = dirtyFlag;
		this.zone = zone;

		JPanel top = new JPanel();
		Vector<String> vec = new Vector<String>();
		for (int i=0; i<MAX; i++)
		{
			vec.addElement(describeType(i));
		}
		type = new JComboBox(vec);
		type.addActionListener(this);
		top.add(new JLabel("Type:"));
		top.add(type);

		cards = new CardLayout(3,3);
		controls = new JPanel(cards);
		for (int i=0; i<MAX; i++)
		{
			JPanel c = getControls(i);
			controls.add(c, String.valueOf(i));
		}

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);

		controls.setBorder(BorderFactory.createEtchedBorder());
		
		JPanel commonOptions = getCommonOptionsPanel();

		this.setLayout(new BorderLayout(3,3));
		this.add(top, BorderLayout.NORTH);
		this.add(controls, BorderLayout.CENTER);
		this.add(commonOptions, BorderLayout.EAST);
		this.add(buttons, BorderLayout.SOUTH);

		if (tileScript != null)
		{
			setState(tileScript);
		}

		this.pack();
		setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCommonOptionsPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		
//		dodgyGridBagShite(result, new JLabel("Common Options"), new JLabel(), gbc);
		
		executeOnce = new JCheckBox("Execute Only Once (maze var)?");
		executeOnce.addActionListener(this);
		gbc.weightx = 0.0;
		gbc.gridx=0;
		result.add(executeOnce, gbc);
		gbc.weightx = 1.0;
		gbc.gridy++;

		JPanel panel = new JPanel();
		executeOnceQuick = new JButton("*");
		executeOnceQuick.addActionListener(this);
		executeOnceMazeVariable = new JTextField(15);
		panel.add(executeOnceMazeVariable);
		panel.add(executeOnceQuick);
		gbc.weightx = 1.0;
		result.add(panel, gbc);
		gbc.gridy++;

		JPanel facing = new JPanel(new GridLayout(3,3));
		north = new JCheckBox("N");
		south = new JCheckBox("S");
		east = new JCheckBox("E");
		west = new JCheckBox("W");
		facing.add(new JLabel());
		facing.add(north);
		facing.add(new JLabel());
		facing.add(west);
		facing.add(new JLabel());
		facing.add(east);
		facing.add(new JLabel());
		facing.add(south);
		facing.add(new JLabel());

		gbc.weightx = 1.0;
		result.add(new JLabel("Execute When Party Is Facing:"), gbc);
		gbc.weighty = 0.0;
		gbc.gridy++;
		result.add(facing, gbc);

		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		reexecuteOnSameTile = new JCheckBox("Re-execute On Same Tile?");
		reexecuteOnSameTile.addActionListener(this);
		result.add(reexecuteOnSameTile, gbc);
		
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridy++;
		result.add(new JLabel(), gbc);
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void setState(TileScript ts)
	{
		int tsType;
		if (types.containsKey(ts.getClass()))
		{
			tsType = types.get(ts.getClass());
		}
		else
		{
			tsType = CUSTOM;
		}
		type.setSelectedIndex(tsType);
		
		if (ts.getExecuteOnceMazeVariable() != null)
		{
			executeOnce.setSelected(true);
			executeOnceQuick.setEnabled(true);
			executeOnceMazeVariable.setEnabled(true);
			executeOnceMazeVariable.setText(ts.getExecuteOnceMazeVariable());
		}
		else
		{
			executeOnce.setSelected(false);
			executeOnceQuick.setEnabled(false);
			executeOnceMazeVariable.setEnabled(false);
			executeOnceMazeVariable.setText("");
		}
		
		reexecuteOnSameTile.setSelected(ts.isReexecuteOnSameTile());
		
		if (ts.getFacings() != null)
		{
			north.setSelected(ts.getFacings().get(CrusaderEngine.Facing.NORTH));
			south.setSelected(ts.getFacings().get(CrusaderEngine.Facing.SOUTH));
			east.setSelected(ts.getFacings().get(CrusaderEngine.Facing.EAST));
			west.setSelected(ts.getFacings().get(CrusaderEngine.Facing.WEST));
		}
		else
		{
			north.setSelected(false);
			south.setSelected(false);
			east.setSelected(false);
			west.setSelected(false);
		}

		switch (tsType)
		{
			case CUSTOM: impl.setText(ts.getClass().getName()); break;
			case CAST_SPELL:
				CastSpell cs = (CastSpell)ts;
				spell.setSelectedItem(cs.getSpellName());
				casterLevel.setValue(cs.getCasterLevel());
				castingLevel.setValue(cs.getCastingLevel());
				break;
			case CHEST:
				Chest c = (Chest)ts;
				chestContents.refresh(c.getChestContents(), zone);
				chestMazeVariable.setText(c.getMazeVariable());
				trap.refresh(c.getTraps());
				chestNorthTexture.setSelectedItem(c.getNorthTexture());
				chestSouthTexture.setSelectedItem(c.getSouthTexture());
				chestEastTexture.setSelectedItem(c.getEastTexture());
				chestWestTexture.setSelectedItem(c.getWestTexture());
				chestPreScript.setSelectedItem(c.getPreScript()==null?EditorPanel.NONE:c.getPreScript().getName());
				break;
			case ENCOUNTER:
				Encounter e = (Encounter)ts;
				encounterTable.setSelectedItem(e.getEncounterTable().getName());
				encounterVariable.setText(e.getMazeVariable());
				break;
			case FLAVOUR_TEXT:
				FlavourText ft = (FlavourText)ts;
				flavourText.setText(ft.getText());
				flavourText.setCaretPosition(0);
				break;
			case LOOT:
				Loot l = (Loot)ts;
				lootTable.setSelectedItem(l.getLootTable());
				break;
			case REMOVE_WALL:
				RemoveWall r = (RemoveWall)ts;
				removeWallMazeVariable.setText(r.getMazeVariable());
				removeWallWallIndex.setValue(r.getWallIndex());
				removeWallIsHoriz.setSelected(r.isHorizontalWall());
				break;
			case EXECUTE_MAZE_EVENTS:
				ExecuteMazeScript eme = (ExecuteMazeScript)ts;
				mazeScript.setSelectedItem(eme.getScript());
				break;
			case SIGNBOARD:
				SignBoard sb = (SignBoard)ts;
				signBoard.setText(sb.getText());
				break;
			case SET_MAZE_VARIABLE:
				SetMazeVariable smv = (SetMazeVariable)ts;
				setMazeVariableVariable.setText(smv.getMazeVariable());
				setMazeVariableValue.setText(smv.getValue());
				break;
			case HIDDEN_STUFF:
				HiddenStuff hs = (HiddenStuff)ts;
				hiddenStuffContents.setSelectedItem(hs.getContent().getName());
				hiddenStuffPreScript.setSelectedItem(hs.getPreScript()==null?EditorPanel.NONE:hs.getPreScript().getName());
				hiddenStuffVariable.setText(hs.getMazeVariable());
				break;
			case WATER:
				break;
			default: throw new MazeException("Invalid type "+tsType);
		}
	}

	/*-------------------------------------------------------------------------*/
	JPanel getControls(int type)
	{
		switch (type)
		{
			case CUSTOM: return getCustomPanel();
			case CAST_SPELL: return getCastSpellPanel();
			case CHEST: return getChestPanel();
			case ENCOUNTER: return getEncounterPanel();
			case FLAVOUR_TEXT: return getFlavourTextPanel();
			case LOOT: return getLootPanel();
			case REMOVE_WALL: return getRemoveWallPanel();
			case EXECUTE_MAZE_EVENTS: return getExecuteMazeEventsPanel();
			case SIGNBOARD: return getSignBoardPanel();
			case SET_MAZE_VARIABLE: return getSetMazeVariablePanel();
			case HIDDEN_STUFF: return getHiddenStuffPanel();
			case WATER: return new JPanel();
			default: throw new MazeException("Invalid type "+type);
		}
	}

	private JPanel getHiddenStuffPanel()
	{
		Vector<String> scripts = new Vector<String>(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(scripts);
		scripts.add(0, EditorPanel.NONE);
		
		hiddenStuffContents = new JComboBox(scripts);
		hiddenStuffPreScript = new JComboBox(scripts);
		hiddenStuffVariable = new JTextField(20);

		JButton edit = getMazeScriptEditButton();
		
		return dirtyGridBagCrap(
			new JLabel("Maze Variable:"), hiddenStuffVariable,
			new JLabel("Contents:"), hiddenStuffContents,
			new JLabel("Pre Script:"), hiddenStuffPreScript,
			new JLabel(), edit);			
	}

	private JPanel getSetMazeVariablePanel()
	{
		setMazeVariableVariable = new JTextField(20);
		setMazeVariableValue = new JTextField(20);
		
		return dirtyGridBagCrap(
			new JLabel("Maze Variable:"), setMazeVariableVariable,
			new JLabel("Value:"), setMazeVariableValue);
	}

	private JPanel getSignBoardPanel()
	{
		signBoard = new JTextArea(20,30);
		signBoard.setLineWrap(true);
		signBoard.setWrapStyleWord(true);

		return dirtyGridBagCrap(new JScrollPane(signBoard), new JLabel());
	}

	private JPanel getExecuteMazeEventsPanel()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(vec);
		mazeScript = new JComboBox(vec);

		JButton edit = getMazeScriptEditButton();

		return dirtyGridBagCrap(
			new JLabel("Maze Script:"), mazeScript,
			new JLabel(), edit);
	}

	private JButton getMazeScriptEditButton()
	{
		JButton edit = new JButton("Edit Maze Scripts...");
		edit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new EditorPanelDialog("Edit Maze Scripts", new MazeScriptPanel());

				Vector<String> scripts =
					new Vector<String>(Database.getInstance().getMazeScripts().keySet());
				Collections.sort(scripts);

				Vector<String> scripts2 = new Vector<String>(scripts);
				scripts2.add(0, EditorPanel.NONE);

				// all the foreign keys...
				mazeScript.setModel(new DefaultComboBoxModel(scripts));
				chestPreScript.setModel(new DefaultComboBoxModel(scripts2));
				hiddenStuffPreScript.setModel(new DefaultComboBoxModel(scripts2));
			}
		});
		return edit;
	}

	private JButton getLootTableEditButton()
	{
		JButton edit = new JButton("Edit Loot Tables...");
		edit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new EditorPanelDialog("Edit Loot Tables", new LootTablePanel());

				Vector<String> tables =
					new Vector<String>(Database.getInstance().getLootTables().keySet());
				Collections.sort(tables);

				// all the foreign keys...
				lootTable.setModel(new DefaultComboBoxModel(tables));
			}
		});
		return edit;
	}

	private JPanel getRemoveWallPanel()
	{
		removeWallMazeVariable = new JTextField(20);
		removeWallQuick = new JButton("Maze Variable:");
		removeWallQuick.addActionListener(this);
		removeWallQuick.setToolTipText("Auto assign maz var");
		removeWallWallIndex = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
		removeWallIsHoriz = new JCheckBox("Horizontal?");
		
		return dirtyGridBagCrap(
			removeWallQuick, removeWallMazeVariable,
			new JLabel("Wall Index:"), removeWallWallIndex,
			removeWallIsHoriz, new JLabel());
	}

	private JPanel getLootPanel()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getLootTables().keySet());
		Collections.sort(vec);
		lootTable = new JComboBox(vec);

		return dirtyGridBagCrap(
			new JLabel("Loot Table:"), lootTable,
			new JLabel(), getLootTableEditButton());
	}

	private JPanel getFlavourTextPanel()
	{
		flavourText = new JTextArea(20,30);
		flavourText.setLineWrap(true);
		flavourText.setWrapStyleWord(true);

		return dirtyGridBagCrap(new JScrollPane(flavourText), new JLabel());
	}

	private JPanel getEncounterPanel()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getEncounterTables().keySet());
		Collections.sort(vec);
		encounterTable = new JComboBox(vec);
		encounterVariable = new JTextField(20);
		encounterQuickAssignMazeVar = new JButton("Maze Variable:");
		encounterQuickAssignMazeVar.setToolTipText("Auto assign maz var");
		encounterQuickAssignMazeVar.addActionListener(this);

		return dirtyGridBagCrap(
			new JLabel("Encounter Table:"), encounterTable,
			encounterQuickAssignMazeVar, encounterVariable);
	}

	private JPanel getChestPanel()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getTraps().keySet());
		Collections.sort(vec);
		trap = new TrapPercentageTablePanel("Trap", SwingEditor.Tab.SCRIPTS, 0.5, 0.5);
		trap.initForeignKeys();
		chestContents = new SingleTileScriptComponent(dirtyFlag, zone);
		
		Vector<String> textures = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(textures);
		
		chestMazeVariable = new JTextField(20);
		chestNorthTexture = new JComboBox(textures);
		chestSouthTexture = new JComboBox(textures);
		chestEastTexture = new JComboBox(textures);
		chestWestTexture = new JComboBox(textures);
		
		Vector<String> scripts = new Vector<String>(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(scripts);
		scripts.add(0, EditorPanel.NONE);
		
		chestPreScript = new JComboBox(scripts);

		JButton edit = getMazeScriptEditButton();

		Component[] comps = new Component[]
		{
			new JLabel("Chest Contents:"), chestContents, 
			new JLabel("Maze Variable:"), chestMazeVariable,
			new JLabel("North Texture:"), chestNorthTexture,
			new JLabel("South Texture:"), chestSouthTexture,
			new JLabel("East Texture:"), chestEastTexture,
			new JLabel("West Texture:"), chestWestTexture,
			new JLabel("Pre Script:"), chestPreScript,
			new JLabel(), edit
		};
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		for (int i=0; i< comps.length; i+=2)
		{
			dodgyGridBagShite(result, comps[i], comps[i+1], gbc);
		}

		gbc.gridx=0;
		gbc.weighty=1.0;
		gbc.gridwidth=2;
		result.add(trap, gbc);
		
		return result;
	}

	private JPanel getCastSpellPanel()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getSpellList());
		Collections.sort(vec);
		spell = new JComboBox(vec);
		castingLevel = new JSpinner(new SpinnerNumberModel(1, 1, 256, 1));
		casterLevel = new JSpinner(new SpinnerNumberModel(1, 1, 256, 1));

		return dirtyGridBagCrap(
			new JLabel("Spell:"), spell,
			new JLabel("Casting Level:"), castingLevel,
			new JLabel("Caster Level:"), casterLevel);
	}

	private JPanel getCustomPanel()
	{
		impl = new JTextField(20);
		return dirtyGridBagCrap(new JLabel("Custom Impl: "), impl);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel dirtyGridBagCrap(Component... comps)
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		for (int i=0; i<comps.length; i+=2)
		{
			dodgyGridBagShite(result, comps[i], comps[i+1], gbc);
		}

		gbc.weighty = 1.0;
		dodgyGridBagShite(result, new JLabel(), new JLabel(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected void dodgyGridBagShite(JPanel panel, Component a, Component b, GridBagConstraints gbc)
	{
		gbc.weightx = 0.0;
		gbc.gridx=0;
		panel.add(a, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		panel.add(b, gbc);
		gbc.gridy++;
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
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		return gbc;
	}

	/*-------------------------------------------------------------------------*/
	static String describeType(int type)
	{
		switch (type)
		{
			case CUSTOM: return "Custom";
			case CAST_SPELL: return "Cast Spell At Party";
			case CHEST: return "Chest";
			case ENCOUNTER: return "Encounter";
			case FLAVOUR_TEXT: return "Flavour Text";
			case LOOT: return "Loot";
			case REMOVE_WALL: return "Remove Wall";
			case EXECUTE_MAZE_EVENTS: return "Execute Maze Script";
			case SIGNBOARD: return "Sign Board";
			case SET_MAZE_VARIABLE: return "Set Maze Variable";
			case HIDDEN_STUFF: return "Hidden Stuff";
			case WATER: return "Water";
			default: throw new MazeException("Invalid type "+type);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == type)
		{
			cards.show(controls, String.valueOf(type.getSelectedIndex()));
		}
		else if (e.getSource() == ok)
		{
			// save changes
			setResult();
			if (SwingEditor.instance != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
			}
			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			// discard changes
			setVisible(false);
		}
		else if (e.getSource() == executeOnce)
		{
			if (executeOnce.isSelected())
			{
				executeOnceQuick.setEnabled(true);
				executeOnceMazeVariable.setEnabled(true);
			}
			else
			{
				executeOnceQuick.setEnabled(false);
				executeOnceMazeVariable.setText("");
				executeOnceMazeVariable.setEnabled(false);
			}
		}
		else if (e.getSource() == executeOnceQuick)
		{
			if (zone == null)
			{
				return;
			}

			executeOnceMazeVariable.setText(getExecuteOnceMazeVariable(zone));			
		}
		else if (e.getSource() == encounterQuickAssignMazeVar)
		{
			if (zone == null)
			{
				return;
			}
			
			encounterVariable.setText(getEncounterMazeVariable(zone));
		}
		else if (e.getSource() == removeWallQuick)
		{
			if (zone == null)
			{
				return;
			}

			removeWallMazeVariable.setText(getRemoveWallVariable(zone));
		}
	}

	/*-------------------------------------------------------------------------*/
	public static String getExecuteOnceMazeVariable(Zone zone)
	{
		Set<String> existingEncMazeVars = new HashSet<String>();

		// collect all existing encounter maze vars
		Tile[][] tiles = zone.getTiles();
		for (Tile[] tt : tiles)
		{
			for (Tile t : tt)
			{
				List<TileScript> scripts = t.getScripts();

				for (TileScript script : scripts)
				{
					if (script.getExecuteOnceMazeVariable() != null)
					{
						existingEncMazeVars.add(script.getExecuteOnceMazeVariable());
					}
				}
			}
		}

		// iterate over our template string and take the first available one
		return constructMazeVar(zone, existingEncMazeVars, ".execute.once.");
	}

	/*-------------------------------------------------------------------------*/
	private static String constructMazeVar(Zone zone,
		Set<String> existingEncMazeVars, String suffix)
	{
		String zoneName = zone.getName().toLowerCase();
		zoneName = zoneName.replaceAll("\\s", ".");
		zoneName = zoneName.replaceAll("'", "");
		int count = 0;
		while (true)
		{
			String s = zoneName + suffix + count++;
			if (!existingEncMazeVars.contains(s))
			{
				return s;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public static String getEncounterMazeVariable(Zone zone)
	{
		Set<String> existingEncMazeVars = new HashSet<String>();

		// collect all existing encounter maze vars
		Tile[][] tiles = zone.getTiles();
		for (Tile[] tt : tiles)
		{
			for (Tile t : tt)
			{
				List<TileScript> scripts = t.getScripts();

				for (TileScript script : scripts)
				{
					if (script instanceof Encounter)
					{
						Encounter enc = (Encounter)script;
						if (enc.getMazeVariable() != null)
						{
							existingEncMazeVars.add(enc.getMazeVariable());
						}
					}
				}
			}
		}

		// iterate over our template string and take the first available one
		return constructMazeVar(zone, existingEncMazeVars, ".encounter.");
	}

	/*-------------------------------------------------------------------------*/
	public static String getRemoveWallVariable(Zone zone)
	{
		Set<String> existingMazeVars = new HashSet<String>();

		// collect all existing encounter maze vars
		Tile[][] tiles = zone.getTiles();
		for (Tile[] tt : tiles)
		{
			for (Tile t : tt)
			{
				List<TileScript> scripts = t.getScripts();

				for (TileScript script : scripts)
				{
					if (script instanceof RemoveWall)
					{
						RemoveWall s = (RemoveWall)script;
						if (s.getMazeVariable() != null)
						{
							existingMazeVars.add(s.getMazeVariable());
						}
					}
				}
			}
		}

		// iterate over our template string and take the first available one
		return constructMazeVar(zone, existingMazeVars, ".remove.wall.");
	}

	/*-------------------------------------------------------------------------*/
	public TileScript getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void setResult()
	{
		int srType = type.getSelectedIndex();
		switch (srType)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(impl.getText());
					this.result = (TileScript)clazz.newInstance();
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
				break;
			case CAST_SPELL:
				result = new CastSpell(
					(String)spell.getSelectedItem(),
					(Integer)castingLevel.getValue(),
					(Integer)casterLevel.getValue());
				break;
			case CHEST:
				MazeScript script = (chestPreScript.getSelectedItem() == EditorPanel.NONE) ?
					null:Database.getInstance().getScript((String)chestPreScript.getSelectedItem());
				result = new Chest(
					chestContents.getScript(),
					trap.getPercentageTable(), 
					chestMazeVariable.getText(),
					(String)chestNorthTexture.getSelectedItem(),
					(String)chestSouthTexture.getSelectedItem(),
					(String)chestEastTexture.getSelectedItem(),
					(String)chestWestTexture.getSelectedItem(), 
					script);
				break;
			case ENCOUNTER:
				result = new Encounter(
					Database.getInstance().getEncounterTable((String)encounterTable.getSelectedItem()),
					encounterVariable.getText());
				break;
			case FLAVOUR_TEXT:
				result = new FlavourText(flavourText.getText());
				break;
			case LOOT:
				result = new Loot((String)lootTable.getSelectedItem());
				break;
			case REMOVE_WALL:
				result = new RemoveWall(
					removeWallMazeVariable.getText(), 
					(Integer)removeWallWallIndex.getValue(),
					removeWallIsHoriz.isSelected());
				break;
			case EXECUTE_MAZE_EVENTS:
				result = new ExecuteMazeScript((String)mazeScript.getSelectedItem());
				break;
			case SIGNBOARD:
				result = new SignBoard(signBoard.getText());
				break;
			case SET_MAZE_VARIABLE:
				result = new SetMazeVariable(
					setMazeVariableVariable.getText(),
					setMazeVariableValue.getText());
				break;
			case HIDDEN_STUFF:
				String mazeVar = hiddenStuffVariable.getText();
				String contentStr = (String)hiddenStuffContents.getSelectedItem();
				String preStr = (String)hiddenStuffPreScript.getSelectedItem();
				MazeScript content = (contentStr.equals(EditorPanel.NONE)) ? null : Database.getInstance().getScript(contentStr);
				MazeScript preScript = (preStr.equals(EditorPanel.NONE)) ? null : Database.getInstance().getScript(preStr);
				result = new HiddenStuff(content, preScript, mazeVar);
				break;
			case WATER:
				result = new Water();
				break;
			default: throw new MazeException("Invalid type "+srType);
		}
		
		if (executeOnceMazeVariable.getText() != null && executeOnceMazeVariable.getText().length() > 0)
		{
			result.setExecuteOnceMazeVariable(executeOnceMazeVariable.getText());
		}
		else
		{
			result.setExecuteOnceMazeVariable(null);
		}
		
		if (north.isSelected() || south.isSelected() || east.isSelected() || west.isSelected())
		{
			BitSet bs = new BitSet();
			if (north.isSelected()) bs.set(CrusaderEngine.Facing.NORTH);
			if (south.isSelected()) bs.set(CrusaderEngine.Facing.SOUTH);
			if (east.isSelected()) bs.set(CrusaderEngine.Facing.EAST);
			if (west.isSelected()) bs.set(CrusaderEngine.Facing.WEST);
			result.setFacings(bs);
		}
		
		result.setReexecuteOnSameTile(reexecuteOnSameTile.isSelected());
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		new Database(loader, saver);
		loader.init(Maze.getStubCampaign());

		JFrame owner = new JFrame("test");
		owner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		while (1==1)
		{
			TileScriptEditor test = new TileScriptEditor(
				owner, 
				new CastSpell("Force Bolt", 2, 3), 
				-1,
				null);
			System.out.println("test.result = [" + test.result + "]");
		}
	}
}
