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
import java.util.List;
import java.util.*;
import javax.swing.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.*;
import mclachlan.maze.map.crusader.MouseClickScriptAdapter;
import mclachlan.maze.map.script.*;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.magic.ValueList;
import mclachlan.maze.stat.npc.NpcFaction;
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
	private static final int LEVER = 12;
	private static final int TOGGLE_WALL = 13;
	private static final int PERSONALITY_SPEECH = 14;
	private static final int DISPLAY_OPTIONS = 15;
	private static final int SKILL_TEST = 16;

	private static final int MAX = 17;

	static Map<Class<?>, Integer> types;

	static
	{
		types = new HashMap<>();

		types.put(CastSpell.class, CAST_SPELL);
		types.put(Chest.class, CHEST);
		types.put(Lever.class, LEVER);
		types.put(Encounter.class, ENCOUNTER);
		types.put(FlavourText.class, FLAVOUR_TEXT);
		types.put(PersonalitySpeech.class, PERSONALITY_SPEECH);
		types.put(DisplayOptions.class, DISPLAY_OPTIONS);
		types.put(Loot.class, LOOT);
		types.put(RemoveWall.class, REMOVE_WALL);
		types.put(ToggleWall.class, TOGGLE_WALL);
		types.put(ExecuteMazeScript.class, EXECUTE_MAZE_EVENTS);
		types.put(SignBoard.class, SIGNBOARD);
		types.put(SetMazeVariable.class, SET_MAZE_VARIABLE);
		types.put(HiddenStuff.class, HIDDEN_STUFF);
		types.put(Water.class, WATER);
		types.put(SkillTest.class, SKILL_TEST);
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
	private JSpinner scoutSecretDifficulty, clickMaxDistance;

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
	private MazeEventsComponent chestPreScript;

	private JTextField leverMazeVariable;
	private JComboBox leverNorthTexture;
	private JComboBox leverSouthTexture;
	private JComboBox leverEastTexture;
	private JComboBox leverWestTexture;
	private MazeEventsComponent leverPreTransScript, leverPostTransScript;

	private JTextField encounterVariable;
	private JComboBox encounterAttitude;
	private JComboBox encounterAmbushStatus;
	private MazeEventsComponent encounterPreScript, encounterPostAppearanceScript,
		partyLeavesNeutralScript, partyLeavesFriendlyScript;
	private JButton encounterQuickAssignMazeVar;

	private JTextArea flavourText;
	private JComboBox flavourTextAlignment;
	private JComboBox lootTable;

	private JTextField removeWallMazeVariable;
	private JSpinner removeWallWallIndex;
	private JButton removeWallQuick;
	private JCheckBox removeWallIsHoriz;

	private JTextField toggleWallMazeVariable;
	private JSpinner toggleWallWallIndex;
	private JButton toggleWallQuick;
	private JCheckBox toggleWallIsHoriz;
	private JComboBox state1Texture, state1MaskTexture, state2Texture, state2MaskTexture;
	private JCheckBox state1Visible, state1Solid, state1Secret, state2Visible, state2Solid, state2Secret;
	private JSpinner state1Height, state2Height;
	private JComboBox preToggleScript, postToggleScript;

	private MazeEventsComponent mazeScript;
	private JTextArea signBoard;

	private JTextField setMazeVariableVariable;
	private JTextField setMazeVariableValue;

	private JTextField hiddenStuffVariable;
	private MazeEventsComponent hiddenStuffContents;
	private MazeEventsComponent hiddenStuffPreScript;
	private JSpinner hiddenStuffFindDifficulty;

	private JTextField psSpeechKey;
	private JCheckBox psModal;

	private int maxOptions = 5;
	private JCheckBox displayOptionsForceSelection;
	private JTextField displayOptionsTitle;
	private List<JTextField> displayOptionsOptions;
	private List<MazeEventsComponent> displayOptionsScripts;

	private JComboBox steKeyModifier;
	private ValueComponent steSkillValue;
	private ValueComponent steSuccessValue;
	private JComboBox steSuccessScript;
	private JComboBox steFailureScript;

	/*-------------------------------------------------------------------------*/
	public TileScriptEditor(Frame owner, TileScript tileScript, int dirtyFlag,
		Zone zone)
	{
		super(owner, "Edit Tile Script", true);
		this.dirtyFlag = dirtyFlag;
		this.zone = zone;

		JPanel top = new JPanel();
		Vector<String> vec = new Vector<String>();
		for (int i = 0; i < MAX; i++)
		{
			vec.addElement(describeType(i));
		}
		type = new JComboBox(vec);
		type.addActionListener(this);
		top.add(new JLabel("Type:"));
		top.add(type);

		cards = new CardLayout(3, 3);
		controls = new JPanel(cards);
		for (int i = 0; i < MAX; i++)
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

		this.setLayout(new BorderLayout(3, 3));
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
		gbc.gridx = 0;
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

		JPanel facing = new JPanel(new GridLayout(3, 3));
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

		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		JPanel panel2 = new JPanel();
		scoutSecretDifficulty = new JSpinner(new SpinnerNumberModel(-1, -1, 256, 1));
		panel2.add(new JLabel("Scout Secret Difficulty:"));
		panel2.add(scoutSecretDifficulty);
		result.add(panel2, gbc);

		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		JPanel panel3 = new JPanel();
		clickMaxDistance = new JSpinner(new SpinnerNumberModel(1, 1, 256, 1));
		panel3.add(new JLabel("Click Max Distance:"));
		panel3.add(clickMaxDistance);
		result.add(panel3, gbc);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx = 0;
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

		scoutSecretDifficulty.setValue(ts.getScoutSecretDifficulty());
		clickMaxDistance.setValue(ts.getClickMaxDistance());

		switch (tsType)
		{
			case CUSTOM:
				impl.setText(ts.getClass().getName());
				break;
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
				chestPreScript.refresh(c.getPreScript() == null ? null : c.getPreScript().getEvents());
				break;
			case LEVER:
				Lever lever = (Lever)ts;
				leverMazeVariable.setText(lever.getMazeVariable());
				leverNorthTexture.setSelectedItem(lever.getNorthTexture());
				leverSouthTexture.setSelectedItem(lever.getSouthTexture());
				leverEastTexture.setSelectedItem(lever.getEastTexture());
				leverWestTexture.setSelectedItem(lever.getWestTexture());
				leverPreTransScript.refresh(lever.getPreTransitionScript() == null ? null : lever.getPreTransitionScript().getEvents());
				leverPostTransScript.refresh(lever.getPostTransitionScript() == null ? null : lever.getPostTransitionScript().getEvents());
				break;
			case ENCOUNTER:
				Encounter e = (Encounter)ts;
				encounterTable.setSelectedItem(e.getEncounterTable().getName());
				encounterVariable.setText(e.getMazeVariable());
				encounterAttitude.setSelectedItem(e.getAttitude() == null ? EditorPanel.NONE : e.getAttitude());
				encounterAmbushStatus.setSelectedItem(e.getAmbushStatus() == null ? EditorPanel.NONE : e.getAmbushStatus());
				encounterPreScript.refresh(e.getPreScriptEvents() == null ? null : e.getPreScriptEvents().getEvents());
				encounterPostAppearanceScript.refresh(e.getPostAppearanceScriptEvents() == null ? null : e.getPostAppearanceScriptEvents().getEvents());
				partyLeavesNeutralScript.refresh(e.getPartyLeavesNeutralScript() == null ? null : e.getPartyLeavesNeutralScript().getEvents());
				partyLeavesFriendlyScript.refresh(e.getPartyLeavesFriendlyScript() == null ? null : e.getPartyLeavesFriendlyScript().getEvents());
				break;
			case FLAVOUR_TEXT:
				FlavourText ft = (FlavourText)ts;
				flavourText.setText(ft.getText());
				flavourText.setCaretPosition(0);
				flavourTextAlignment.setSelectedItem(ft.getAlignment());
				break;
			case PERSONALITY_SPEECH:
				PersonalitySpeech ps = (PersonalitySpeech)ts;
				psSpeechKey.setText(ps.getSpeechKey());
				psModal.setSelected(ps.isModal());
				break;
			case DISPLAY_OPTIONS:
				DisplayOptions dop = (DisplayOptions)ts;

				displayOptionsForceSelection.setSelected(dop.isForceSelection());

				displayOptionsTitle.setText(dop.getTitle());

				List<String> options = dop.getOptions();
				List<MazeScript> scripts = dop.getMazeScripts();
				for (int i = 0; i < maxOptions; i++)
				{
					if (i < options.size())
					{
						displayOptionsOptions.get(i).setText(options.get(i));
					}
					else
					{
						displayOptionsOptions.get(i).setText("");
					}
					if (i < scripts.size())
					{
						displayOptionsScripts.get(i).refresh(scripts.get(i) == null ? null : scripts.get(i).getEvents());
					}
					else
					{
						displayOptionsScripts.get(i).refresh(null);
					}
				}


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
			case TOGGLE_WALL:
				ToggleWall tw = (ToggleWall)ts;

				toggleWallMazeVariable.setText(tw.getMazeVariable());
				toggleWallWallIndex.setValue(tw.getWallIndex());
				toggleWallIsHoriz.setSelected(tw.isHorizontalWall());

				state1Texture.setSelectedItem(tw.getState1Texture() == null ? EditorPanel.NONE : tw.getState1Texture().getName());
				state1MaskTexture.setSelectedItem(tw.getState1MaskTexture() == null ? EditorPanel.NONE : tw.getState1MaskTexture().getName());
				state1Solid.setSelected(tw.isState1Solid());
				state1Visible.setSelected(tw.isState1Visible());
				state1Secret.setSelected(tw.isState1Secret());
				state1Height.setValue(tw.getState1Height());

				state2Texture.setSelectedItem(tw.getState2Texture() == null ? EditorPanel.NONE : tw.getState2Texture().getName());
				state2MaskTexture.setSelectedItem(tw.getState2MaskTexture() == null ? EditorPanel.NONE : tw.getState2MaskTexture().getName());
				state2Solid.setSelected(tw.isState2Solid());
				state2Visible.setSelected(tw.isState2Visible());
				state2Secret.setSelected(tw.isState2Secret());
				state2Height.setValue(tw.getState2Height());

				preToggleScript.setSelectedItem(tw.getPreToggleScript() == null ? EditorPanel.NONE : tw.getPreToggleScript());
				postToggleScript.setSelectedItem(tw.getPostToggleScript() == null ? EditorPanel.NONE : tw.getPostToggleScript());

				break;
			case EXECUTE_MAZE_EVENTS:
				ExecuteMazeScript eme = (ExecuteMazeScript)ts;
				mazeScript.refresh(eme.getScript().getEvents());
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
				hiddenStuffContents.refresh(hs.getContent() == null ? null : hs.getContent().getEvents());
				hiddenStuffPreScript.refresh(hs.getPreScript() == null ? null : hs.getPreScript().getEvents());
				hiddenStuffVariable.setText(hs.getMazeVariable());
				hiddenStuffFindDifficulty.setValue(hs.getFindDifficulty());
				break;
			case WATER:
				break;
			case SKILL_TEST:
				SkillTest ste = (SkillTest)ts;
				steKeyModifier.setSelectedItem(ste.getKeyModifier() == null ? EditorPanel.NONE : ste.getKeyModifier());
				steSkillValue.setValue(ste.getSkill());
				steSuccessValue.setValue(ste.getSuccessValue());
				steSuccessScript.setSelectedItem(ste.getSuccessScript() == null ? EditorPanel.NONE : ste.getSuccessScript());
				steFailureScript.setSelectedItem(ste.getFailureScript() == null ? EditorPanel.NONE : ste.getFailureScript());
				break;
			default:
				throw new MazeException("Invalid type " + tsType);
		}
	}

	/*-------------------------------------------------------------------------*/
	JPanel getControls(int type)
	{
		return switch (type)
			{
				case CUSTOM -> getCustomPanel();
				case CAST_SPELL -> getCastSpellPanel();
				case CHEST -> getChestPanel();
				case LEVER -> getLeverPanel();
				case ENCOUNTER -> getEncounterPanel();
				case FLAVOUR_TEXT -> getFlavourTextPanel();
				case PERSONALITY_SPEECH -> getPersonalitySpeechPanel();
				case DISPLAY_OPTIONS -> getDisplayOptionsPanel();
				case LOOT -> getLootPanel();
				case REMOVE_WALL -> getRemoveWallPanel();
				case TOGGLE_WALL -> getToggleWallPanel();
				case EXECUTE_MAZE_EVENTS -> getExecuteMazeEventsPanel();
				case SIGNBOARD -> getSignBoardPanel();
				case SET_MAZE_VARIABLE -> getSetMazeVariablePanel();
				case HIDDEN_STUFF -> getHiddenStuffPanel();
				case WATER -> new JPanel();
				case SKILL_TEST -> getSkillTestPanel();
				default -> throw new MazeException("Invalid type " + type);
			};
	}

	private JPanel getSkillTestPanel()
	{
		Vector mods = new Vector(Stats.allModifiers);
		mods.sort(Comparator.comparing(Object::toString));
		mods.add(0, EditorPanel.NONE);
		steKeyModifier = new JComboBox(mods);

		steSkillValue = new ValueComponent(this.dirtyFlag);
		steSuccessValue = new ValueComponent(this.dirtyFlag);

		Vector scripts = new Vector(Database.getInstance().getMazeScripts().keySet());
		scripts.sort(Comparator.comparing(Object::toString));
		scripts.add(0, EditorPanel.NONE);

		steSuccessScript = new JComboBox(scripts);
		steFailureScript = new JComboBox(new Vector(scripts));

		return dirtyGridBagCrap(
			new JLabel("Key Modifier:"), steKeyModifier,
			new JLabel("Skill:"), steSkillValue,
			new JLabel("Success Value:"), steSuccessValue,
			new JLabel("Success Script:"), steSuccessScript,
			new JLabel("Failure Script:"), steFailureScript);

	}

	private JPanel getDisplayOptionsPanel()
	{
		displayOptionsForceSelection = new JCheckBox("Force selection?");

		displayOptionsTitle = new JTextField(20);

		displayOptionsOptions = new ArrayList<>(maxOptions);
		displayOptionsScripts = new ArrayList<>(maxOptions);
		for (int i = 0; i < maxOptions; i++)
		{
			displayOptionsOptions.add(new JTextField(20));
			displayOptionsScripts.add(new MazeEventsComponent(dirtyFlag));
		}

		JPanel result = new JPanel(new BorderLayout());
		JPanel header = new JPanel();
		header.add(new JLabel("Title:"));
		header.add(displayOptionsTitle);

		result.add(header, BorderLayout.NORTH);
		result.add(
			dirtyGridBagCrap(
				new JLabel("Options:"),new JLabel(),
				displayOptionsForceSelection, new JLabel(),
				displayOptionsOptions.get(0), displayOptionsScripts.get(0),
				displayOptionsOptions.get(1), displayOptionsScripts.get(1),
				displayOptionsOptions.get(2), displayOptionsScripts.get(2),
				displayOptionsOptions.get(3), displayOptionsScripts.get(3),
				displayOptionsOptions.get(4), displayOptionsScripts.get(4)),
			BorderLayout.CENTER);

		return result;
	}

	private JPanel getHiddenStuffPanel()
	{
		hiddenStuffContents = new MazeEventsComponent(dirtyFlag);
		hiddenStuffPreScript = new MazeEventsComponent(dirtyFlag);
		hiddenStuffVariable = new JTextField(20);
		hiddenStuffFindDifficulty = new JSpinner(new SpinnerNumberModel(1, 0, 127, 1));

		return dirtyGridBagCrap(
			new JLabel("Maze Variable:"), hiddenStuffVariable,
			new JLabel("Contents:"), hiddenStuffContents,
			new JLabel("Pre Script:"), hiddenStuffPreScript,
			new JLabel("Find Difficulty:"), hiddenStuffFindDifficulty);
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
		signBoard = new JTextArea(20, 30);
		signBoard.setLineWrap(true);
		signBoard.setWrapStyleWord(true);

		return dirtyGridBagCrap(new JScrollPane(signBoard), new JLabel());
	}

	private JPanel getExecuteMazeEventsPanel()
	{
		mazeScript = new MazeEventsComponent(dirtyFlag);

		return dirtyGridBagCrap(
			new JLabel("Maze Script:"), mazeScript
		);
	}

	private JButton getMazeScriptEditButton()
	{
		JButton edit = new JButton("Edit Maze Scripts...");
		edit.addActionListener(e -> {
			new EditorPanelDialog("Edit Maze Scripts", new MazeScriptPanel());
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

	private JPanel getToggleWallPanel()
	{
		toggleWallMazeVariable = new JTextField(20);
		toggleWallQuick = new JButton("Maze Variable:");
		toggleWallQuick.addActionListener(this);
		toggleWallQuick.setToolTipText("Auto assign maz var");
		toggleWallWallIndex = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
		toggleWallIsHoriz = new JCheckBox("Horizontal?");

		state1Texture = new JComboBox();
		state1MaskTexture = new JComboBox();
		state1Solid = new JCheckBox();
		state1Visible = new JCheckBox();
		state1Secret = new JCheckBox();
		state1Height = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

		state2Texture = new JComboBox();
		state2MaskTexture = new JComboBox();
		state2Solid = new JCheckBox();
		state2Visible = new JCheckBox();
		state2Secret = new JCheckBox();
		state2Height = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

		preToggleScript = new JComboBox();
		postToggleScript = new JComboBox();

		Vector<String> vec2 = new Vector<>(Database.getInstance().getMazeTextures().keySet());
		vec2.insertElementAt(EditorPanel.NONE, 0);
		Collections.sort(vec2);

		Vector<String> vec3 = new Vector<>(Database.getInstance().getMazeScripts().keySet());
		vec3.insertElementAt(EditorPanel.NONE, 0);
		Collections.sort(vec3);

		state1Texture.setModel(new DefaultComboBoxModel(vec2));
		state1MaskTexture.setModel(new DefaultComboBoxModel(vec2));
		state2Texture.setModel(new DefaultComboBoxModel(vec2));
		state2MaskTexture.setModel(new DefaultComboBoxModel(vec2));
		preToggleScript.setModel(new DefaultComboBoxModel(vec3));
		postToggleScript.setModel(new DefaultComboBoxModel(vec3));

		Component[] comps = {
			toggleWallQuick, toggleWallMazeVariable,
			new JLabel("Wall Index:"), toggleWallWallIndex,
			toggleWallIsHoriz, new JLabel(),
			new JLabel(), new JLabel(),
			new JLabel("_____ State 1 _____:"), new JLabel(),
			new JLabel("Texture:"), state1Texture,
			new JLabel("Mask Texture:"), state1MaskTexture,
			new JLabel("Solid?"), state1Solid,
			new JLabel("Visible?"), state1Visible,
			new JLabel("Secret?"), state1Secret,
			new JLabel("Height:"), state1Height,
			new JLabel("_____ State 2 _____:"), new JLabel(),
			new JLabel("Texture:"), state2Texture,
			new JLabel("Mask Texture:"), state2MaskTexture,
			new JLabel("Solid?"), state2Solid,
			new JLabel("Visible?"), state2Visible,
			new JLabel("Secret?"), state2Secret,
			new JLabel("Height:"), state2Height,
			new JLabel("Pre toggle script:"), preToggleScript,
			new JLabel("Post toggle script:"), postToggleScript,
		};

		return dirtyGridBagCrap(comps);
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

	private JPanel getPersonalitySpeechPanel()
	{
		psSpeechKey = new JTextField(30);
		psModal = new JCheckBox("Modal?");

		return dirtyGridBagCrap(
			new JLabel("Speech Key:"), psSpeechKey,
			psModal, new JLabel());
	}

	private JPanel getFlavourTextPanel()
	{
		flavourTextAlignment = new JComboBox(FlavourTextEvent.Alignment.values());

		flavourText = new JTextArea(20, 30);
		flavourText.setLineWrap(true);
		flavourText.setWrapStyleWord(true);

		return dirtyGridBagCrap(
			new JLabel("Alignment:"), flavourTextAlignment,
			new JScrollPane(flavourText), new JLabel());
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

		NpcFaction.Attitude[] values = NpcFaction.Attitude.values();
		Vector attitudes = new Vector();
		Collections.addAll(attitudes, values);
		attitudes.add(0, EditorPanel.NONE);
		encounterAttitude = new JComboBox(attitudes);

		Combat.AmbushStatus[] statuses = Combat.AmbushStatus.values();
		Vector ambushStatuses = new Vector();
		Collections.addAll(ambushStatuses, statuses);
		ambushStatuses.add(0, EditorPanel.NONE);
		encounterAmbushStatus = new JComboBox(ambushStatuses);

		encounterPreScript = new MazeEventsComponent(dirtyFlag);
		encounterPostAppearanceScript = new MazeEventsComponent(dirtyFlag);
		partyLeavesNeutralScript = new MazeEventsComponent(dirtyFlag);
		partyLeavesFriendlyScript = new MazeEventsComponent(dirtyFlag);


		return dirtyGridBagCrap(
			new JLabel("Encounter Table:"), encounterTable,
			encounterQuickAssignMazeVar, encounterVariable,
			new JLabel("Attitude:"), encounterAttitude,
			new JLabel("Ambush Status:"), encounterAmbushStatus,
			new JLabel("Pre Script:"), encounterPreScript,
			new JLabel("Post Appearance Script:"), encounterPostAppearanceScript,
			new JLabel("Party Leaves Neutral Script: "), partyLeavesNeutralScript,
			new JLabel("Party Leaves Friendly Script: "), partyLeavesFriendlyScript
		);
	}

	private JPanel getChestPanel()
	{
		Vector<String> vec = new Vector<>(Database.getInstance().getTraps().keySet());
		Collections.sort(vec);
		trap = new TrapPercentageTablePanel("Trap", SwingEditor.Tab.SCRIPTS, 0.5, 0.5);
		trap.initForeignKeys();
		chestContents = new SingleTileScriptComponent(dirtyFlag, zone);

		Vector<String> textures = new Vector<>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(textures);

		chestMazeVariable = new JTextField(20);
		chestNorthTexture = new JComboBox(textures);
		chestSouthTexture = new JComboBox(textures);
		chestEastTexture = new JComboBox(textures);
		chestWestTexture = new JComboBox(textures);

		chestPreScript = new MazeEventsComponent(dirtyFlag);

		Component[] comps = new Component[]
			{
				new JLabel("Chest Contents:"), chestContents,
				new JLabel("Maze Variable:"), chestMazeVariable,
				new JLabel("North Texture:"), chestNorthTexture,
				new JLabel("South Texture:"), chestSouthTexture,
				new JLabel("East Texture:"), chestEastTexture,
				new JLabel("West Texture:"), chestWestTexture,
				new JLabel("Pre Script:"), chestPreScript,
			};
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		for (int i = 0; i < comps.length; i += 2)
		{
			dodgyGridBagShite(result, comps[i], comps[i + 1], gbc);
		}

		gbc.gridx = 0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 2;
		result.add(trap, gbc);

		return result;
	}

	private JPanel getLeverPanel()
	{
		Vector<String> textures = new Vector<>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(textures);

		leverMazeVariable = new JTextField(20);
		leverNorthTexture = new JComboBox(textures);
		leverSouthTexture = new JComboBox(textures);
		leverEastTexture = new JComboBox(textures);
		leverWestTexture = new JComboBox(textures);

		Vector<String> scripts = new Vector<>(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(scripts);
		scripts.add(0, EditorPanel.NONE);

		leverPreTransScript = new MazeEventsComponent(dirtyFlag);
		leverPostTransScript = new MazeEventsComponent(dirtyFlag);

		JButton northTextureButton = new JButton("North Texture:");

		northTextureButton.addActionListener(actionEvent -> {
			leverSouthTexture.setSelectedItem(leverNorthTexture.getSelectedItem());
			leverEastTexture.setSelectedItem(leverNorthTexture.getSelectedItem());
			leverWestTexture.setSelectedItem(leverNorthTexture.getSelectedItem());
		});

		JButton quickMazeVar = new JButton("*");
		quickMazeVar.addActionListener(actionEvent -> leverMazeVariable.setText(getLeverMazeVar(zone)));

		JPanel mazeVarPanel = new JPanel();
		mazeVarPanel.add(leverMazeVariable);
		mazeVarPanel.add(quickMazeVar);

		Component[] comps = new Component[]
			{
				new JLabel("Maze Variable:"), mazeVarPanel,
				northTextureButton, leverNorthTexture,
				new JLabel("South Texture:"), leverSouthTexture,
				new JLabel("East Texture:"), leverEastTexture,
				new JLabel("West Texture:"), leverWestTexture,
				new JLabel("Pre-transition Script:"), leverPreTransScript,
				new JLabel("Post-transition Script:"), leverPostTransScript,
			};
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		for (int i = 0; i < comps.length; i += 2)
		{
			dodgyGridBagShite(result, comps[i], comps[i + 1], gbc);
		}

		gbc.gridx = 0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 2;
		result.add(new JPanel(), gbc);

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
		for (int i = 0; i < comps.length; i += 2)
		{
			dodgyGridBagShite(result, comps[i], comps[i + 1], gbc);
		}

		gbc.weighty = 1.0;
		dodgyGridBagShite(result, new JLabel(), new JLabel(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected void dodgyGridBagShite(JPanel panel, Component a, Component b,
		GridBagConstraints gbc)
	{
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		panel.add(a, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		panel.add(b, gbc);
		gbc.gridy++;
	}

	protected GridBagConstraints createGridBagConstraints()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
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
		return switch (type)
			{
				case CUSTOM -> "Custom";
				case CAST_SPELL -> "Cast Spell At Party";
				case CHEST -> "Chest";
				case LEVER -> "Lever";
				case ENCOUNTER -> "Encounter";
				case FLAVOUR_TEXT -> "Flavour Text";
				case PERSONALITY_SPEECH -> "Personality Speech";
				case DISPLAY_OPTIONS -> "Display Options";
				case LOOT -> "Loot";
				case REMOVE_WALL -> "Remove Wall";
				case TOGGLE_WALL -> "Toggle Wall";
				case EXECUTE_MAZE_EVENTS -> "Execute Maze Script";
				case SIGNBOARD -> "Sign Board";
				case SET_MAZE_VARIABLE -> "Set Maze Variable";
				case HIDDEN_STUFF -> "Hidden Stuff";
				case WATER -> "Water";
				case SKILL_TEST -> "Skill Test";
				default -> throw new MazeException("Invalid type " + type);
			};
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
		else if (e.getSource() == toggleWallQuick)
		{
			if (zone == null)
			{
				return;
			}

			toggleWallMazeVariable.setText(getToggleWallVariable(zone));
		}
	}

	/*-------------------------------------------------------------------------*/
	public static String getExecuteOnceMazeVariable(Zone zone)
	{
		Set<String> existingEncMazeVars = new HashSet<String>();

		// collect all existing execute once maze vars
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

				Point point = zone.getPoint(t);
				for (Wall w : zone.getMap().getWalls(point))
				{
					MouseClickScriptAdapter mouseClickScript = (MouseClickScriptAdapter)w.getMouseClickScript();
					MouseClickScriptAdapter maskTextureMouseClickScript = (MouseClickScriptAdapter)w.getMaskTextureMouseClickScript();
					MouseClickScriptAdapter internalScript = (MouseClickScriptAdapter)w.getInternalScript();

					if (mouseClickScript != null && mouseClickScript.getScript().getExecuteOnceMazeVariable() != null)
					{
						existingEncMazeVars.add(mouseClickScript.getScript().getExecuteOnceMazeVariable());
					}
					if (maskTextureMouseClickScript != null && maskTextureMouseClickScript.getScript().getExecuteOnceMazeVariable() != null)
					{
						existingEncMazeVars.add(maskTextureMouseClickScript.getScript().getExecuteOnceMazeVariable());
					}
					if (internalScript != null && internalScript.getScript().getExecuteOnceMazeVariable() != null)
					{
						existingEncMazeVars.add(internalScript.getScript().getExecuteOnceMazeVariable());
					}
				}

				List<EngineObject> objects = zone.getMap().getObjects(zone.getTileIndex(point));
				for (EngineObject object : objects)
				{
					MouseClickScriptAdapter objectClickScript = (MouseClickScriptAdapter)object.getMouseClickScript();
					if (objectClickScript != null && objectClickScript.getScript().getExecuteOnceMazeVariable() != null)
					{
						existingEncMazeVars.add(objectClickScript.getScript().getExecuteOnceMazeVariable());
					}
				}
			}
		}

		// iterate over our template string and take the first available one
		return constructMazeVar(zone, existingEncMazeVars, ".execute.once.");
	}

	/*-------------------------------------------------------------------------*/
	public static String getLeverMazeVar(Zone zone)
	{
		Set<String> existingEncMazeVars = new HashSet<>();

		// collect all existing encounter maze vars
		Tile[][] tiles = zone.getTiles();
		for (Tile[] tt : tiles)
		{
			for (Tile t : tt)
			{
				List<TileScript> scripts = t.getScripts();

				for (TileScript script : scripts)
				{
					if (script instanceof Lever)
					{
						existingEncMazeVars.add(((Lever)script).getMazeVariable());
					}
				}
			}
		}

		// iterate over our template string and take the first available one
		return constructMazeVar(zone, existingEncMazeVars, ".lever-state.");
	}

	/*-------------------------------------------------------------------------*/
	public static String getChestMazeVar(Zone zone)
	{
		Set<String> existingEncMazeVars = new HashSet<>();

		// collect all existing encounter maze vars
		Tile[][] tiles = zone.getTiles();
		for (Tile[] tt : tiles)
		{
			for (Tile t : tt)
			{
				List<TileScript> scripts = t.getScripts();

				for (TileScript script : scripts)
				{
					if (script instanceof Chest)
					{
						existingEncMazeVars.add(((Lever)script).getMazeVariable());
					}
				}
			}
		}

		// iterate over our template string and take the first available one
		return constructMazeVar(zone, existingEncMazeVars, ".chest.");
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
		Set<String> existingMazeVars = new HashSet<>();

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
	public static String getToggleWallVariable(Zone zone)
	{
		Set<String> existingMazeVars = new HashSet<>();

		// collect all existing encounter maze vars
		Tile[][] tiles = zone.getTiles();
		for (Tile[] tt : tiles)
		{
			for (Tile t : tt)
			{
				List<TileScript> scripts = t.getScripts();

				for (TileScript script : scripts)
				{
					if (script instanceof ToggleWall)
					{
						ToggleWall s = (ToggleWall)script;
						if (s.getMazeVariable() != null)
						{
							existingMazeVars.add(s.getMazeVariable());
						}
					}
				}
			}
		}

		List<Wall> walls = new Vector<>(Arrays.asList(zone.getMap().getHorizontalWalls()));
		walls.addAll(Arrays.asList(zone.getMap().getVerticalWalls()));
		for (Wall w : walls)
		{
			if (w.getMouseClickScript() != null)
			{
				TileScript s = ((MouseClickScriptAdapter)w.getMouseClickScript()).getScript();
				if (s instanceof ToggleWall)
				{
					if (((ToggleWall)s).getMazeVariable() != null)
					{
						existingMazeVars.add(((ToggleWall)s).getMazeVariable());
					}
				}
			}

			if (w.getMaskTextureMouseClickScript() != null)
			{
				TileScript s = ((MouseClickScriptAdapter)w.getMaskTextureMouseClickScript()).getScript();
				if (s instanceof ToggleWall)
				{
					if (((ToggleWall)s).getMazeVariable() != null)
					{
						existingMazeVars.add(((ToggleWall)s).getMazeVariable());
					}
				}
			}

			if (w.getInternalScript() != null)
			{
				TileScript s = ((MouseClickScriptAdapter)w.getInternalScript()).getScript();
				if (s instanceof ToggleWall)
				{
					if (((ToggleWall)s).getMazeVariable() != null)
					{
						existingMazeVars.add(((ToggleWall)s).getMazeVariable());
					}
				}
			}
		}

		// todo: search portals

		// iterate over our template string and take the first available one
		return constructMazeVar(zone, existingMazeVars, ".toggle.wall.");
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
				MazeScript script = null;
				if (chestPreScript.getEvents() != null && !chestPreScript.getEvents().isEmpty())
				{
					script = new MazeScript("Chest.preScript", chestPreScript.getEvents());
				}

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
			case LEVER:
				MazeScript preTransScript = null;
				if (leverPreTransScript.getEvents() != null && leverPreTransScript.getEvents().size() > 0)
				{
					preTransScript = new MazeScript("Lever.preTransScript", leverPreTransScript.getEvents());
				}

				MazeScript postTransScript = null;
				if (leverPostTransScript.getEvents() != null && leverPostTransScript.getEvents().size() > 0)
				{
					postTransScript = new MazeScript("Lever.postTransScript", leverPostTransScript.getEvents());
				}

				result = new Lever(
					(String)leverNorthTexture.getSelectedItem(),
					(String)leverSouthTexture.getSelectedItem(),
					(String)leverEastTexture.getSelectedItem(),
					(String)leverWestTexture.getSelectedItem(),
					leverMazeVariable.getText(),
					preTransScript,
					postTransScript);
				break;
			case ENCOUNTER:
				NpcFaction.Attitude attitude = encounterAttitude.getSelectedItem() == EditorPanel.NONE ? null : (NpcFaction.Attitude)encounterAttitude.getSelectedItem();
				Combat.AmbushStatus ambushStatus = encounterAmbushStatus.getSelectedItem() == EditorPanel.NONE ? null : (Combat.AmbushStatus)encounterAmbushStatus.getSelectedItem();
				MazeScript encPreScript = null;
				if (encounterPreScript.getEvents() != null && encounterPreScript.getEvents().size() > 0)
				{
					encPreScript = new MazeScript("Encounter.preScript", encounterPreScript.getEvents());
				}
				MazeScript encPostAppearanceScript = null;
				if (encounterPostAppearanceScript.getEvents() != null && encounterPostAppearanceScript.getEvents().size() > 0)
				{
					encPostAppearanceScript = new MazeScript("Encounter.postAppearanceScript", encounterPostAppearanceScript.getEvents());
				}
				MazeScript partyLeavesFriendly = null;
				if (partyLeavesFriendlyScript.getEvents() != null && partyLeavesFriendlyScript.getEvents().size()>0)
				{
					partyLeavesFriendly = new MazeScript("EncounterActorsEvent.partyLeavesFriendly", partyLeavesFriendlyScript.getEvents());
				}
				MazeScript partyLeavesNeutral = null;
				if (partyLeavesNeutralScript.getEvents() != null && partyLeavesNeutralScript.getEvents().size()>0)
				{
					partyLeavesNeutral = new MazeScript("EncounterActorsEvent.partyLeavesNeutral", partyLeavesNeutralScript.getEvents());
				}

				result = new Encounter(
					Database.getInstance().getEncounterTable((String)encounterTable.getSelectedItem()),
					encounterVariable.getText(),
					attitude,
					ambushStatus,
					encPreScript,
					encPostAppearanceScript,
					partyLeavesNeutral,
					partyLeavesFriendly);
				break;
			case FLAVOUR_TEXT:
				result = new FlavourText(flavourText.getText(), (FlavourTextEvent.Alignment)flavourTextAlignment.getSelectedItem());
				break;
			case PERSONALITY_SPEECH:
				result = new PersonalitySpeech(psSpeechKey.getText(), psModal.isSelected());
				break;
			case DISPLAY_OPTIONS:
				ArrayList<String> options = new ArrayList<>();
				ArrayList<MazeScript> scripts = new ArrayList<>();

				for (int i = 0; i < maxOptions; i++)
				{
					String option = displayOptionsOptions.get(i).getText();

					if (option != null && option.length() > 0)
					{
						options.add(option);
						MazeScript scriptopt = null;
//						if (displayOptionsScripts.get(i).getEvents() != null && displayOptionsScripts.get(i).getEvents().size()>0)
						{
							scriptopt = new MazeScript("DisplayOptions.mazeScripts", displayOptionsScripts.get(i).getEvents());
						}
						scripts.add(scriptopt);
					}
				}
				result = new DisplayOptions(
					displayOptionsForceSelection.isSelected(),
					displayOptionsTitle.getText(),
					options,
					scripts);
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
			case TOGGLE_WALL:
				result = new ToggleWall(
					toggleWallMazeVariable.getText(), (Integer)toggleWallWallIndex.getValue(), toggleWallIsHoriz.isSelected(),

					EditorPanel.NONE == state1Texture.getSelectedItem() ? null : Database.getInstance().getMazeTexture((String)state1Texture.getSelectedItem()),
					EditorPanel.NONE == state1MaskTexture.getSelectedItem() ? null : Database.getInstance().getMazeTexture((String)state1MaskTexture.getSelectedItem()),
					state1Visible.isSelected(),
					state1Solid.isSelected(),
					state1Secret.isSelected(),
					(Integer)state1Height.getValue(),

					EditorPanel.NONE == state2Texture.getSelectedItem() ? null : Database.getInstance().getMazeTexture((String)state2Texture.getSelectedItem()),
					EditorPanel.NONE == state2MaskTexture.getSelectedItem() ? null : Database.getInstance().getMazeTexture((String)state2MaskTexture.getSelectedItem()),
					state2Visible.isSelected(),
					state2Solid.isSelected(),
					state2Secret.isSelected(),
					(Integer)state2Height.getValue(),

					EditorPanel.NONE == preToggleScript.getSelectedItem() ? null : (String)preToggleScript.getSelectedItem(),
					EditorPanel.NONE == postToggleScript.getSelectedItem() ? null : (String)postToggleScript.getSelectedItem());

				break;
			case EXECUTE_MAZE_EVENTS:
				result = new ExecuteMazeScript(mazeScript.getEvents() == null ? null : new MazeScript("ExecuteMazeScript.script", mazeScript.getEvents()));
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
				int findDifficulty = (Integer)hiddenStuffFindDifficulty.getValue();
				MazeScript content = null;
				if (hiddenStuffContents.getEvents() != null && !hiddenStuffContents.getEvents().isEmpty())
				{
					content = new MazeScript("HiddenStuff.contents", hiddenStuffContents.getEvents());
				}
				MazeScript preScript = null;
				if (hiddenStuffPreScript.getEvents() != null && !hiddenStuffPreScript.getEvents().isEmpty())
				{
					preScript = new MazeScript("HiddenStuff.preScript", hiddenStuffPreScript.getEvents());
				}

				result = new HiddenStuff(content, preScript, mazeVar, findDifficulty);
				break;
			case WATER:
				result = new Water();
				break;
			case SKILL_TEST:
				Stats.Modifier keyMod = steKeyModifier.getSelectedItem() == EditorPanel.NONE ? null : (Stats.Modifier)steKeyModifier.getSelectedItem();
				ValueList skill = steSkillValue.getValue();
				ValueList successVal = steSuccessValue.getValue();
				String successScript = steSuccessScript.getSelectedItem() == EditorPanel.NONE ? null : (String)steSuccessScript.getSelectedItem();
				String failureScript = steFailureScript.getSelectedItem() == EditorPanel.NONE ? null : (String)steFailureScript.getSelectedItem();
				result = new SkillTest(keyMod, skill, successVal, successScript, failureScript);
				break;
			default:
				throw new MazeException("Invalid type " + srType);
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
			if (north.isSelected())
			{
				bs.set(CrusaderEngine.Facing.NORTH);
			}
			if (south.isSelected())
			{
				bs.set(CrusaderEngine.Facing.SOUTH);
			}
			if (east.isSelected())
			{
				bs.set(CrusaderEngine.Facing.EAST);
			}
			if (west.isSelected())
			{
				bs.set(CrusaderEngine.Facing.WEST);
			}
			result.setFacings(bs);
		}

		result.setScoutSecretDifficulty((Integer)scoutSecretDifficulty.getValue());
		result.setClickMaxDistance((Integer)clickMaxDistance.getValue());

		result.setReexecuteOnSameTile(reexecuteOnSameTile.isSelected());
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V2Loader();
		Saver saver = new V2Saver();
		new Database(loader, saver, Maze.getStubCampaign());

		JFrame owner = new JFrame("test");
		owner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		while (1 == 1)
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
