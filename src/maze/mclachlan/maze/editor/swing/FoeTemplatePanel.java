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
import java.util.List;
import java.util.*;
import javax.swing.*;
import mclachlan.crusader.EngineObject;
import mclachlan.maze.balance.CharacterBuilder;
import mclachlan.maze.balance.MockCombat;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.CombatStatistics;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.util.FoeXpCalculator;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class FoeTemplatePanel extends EditorPanel
{
	private JTextField pluralName, unidentifiedName, unidentifiedPluralName, faction;
	private DiceField hitPointsRange, actionPointsRange, magicPointsRange, levelRange;
	private JSpinner experience, identificationDifficulty, fleeChance;
	private StatModifierComponent stats, foeGroupBannerModifiers, allFoesBannerModifiers;
	private JComboBox baseTexture, meleeTexture, rangedTexture, castSpellTexture,
		specialAbilityTexture, evasionBehaviour, stealthBehaviour,
		appearanceScript, appearanceDirection, deathScript, focus, attitude,
		verticalAlignment, alliesOnCall;
	private JButton textureTint;
	private JComboBox spriteAnimations;
	private JCheckBox cannotBeEvaded, isNpc;
	private JButton quickAssignAllTextures, quickApplyStatPack, quickAssignXp;
	private FoeTypeSelection foeTypes;
	private JComboBox race, characterClass;

	private BodyPartPercentageTablePanel bodyParts;
	private PlayerBodyPartAttackedPanel playerBodyParts;

	private NaturalWeaponsWidget naturalWeapons;
	private SpellListPanel spellBook;
	private SpellLikeAbilitiesWidget spellLikeAbilitiesWidget;
	private LootTableDisplayWidget lootTable;

	private JComboBox foeSpeech;

	static String[] evasionBehaviours =
		{
			Foe.EvasionBehaviour.toString(Foe.EvasionBehaviour.NEVER_EVADE),
			Foe.EvasionBehaviour.toString(Foe.EvasionBehaviour.RANDOM_EVADE),
			Foe.EvasionBehaviour.toString(Foe.EvasionBehaviour.ALWAYS_EVADE),
			Foe.EvasionBehaviour.toString(Foe.EvasionBehaviour.CLEVER_EVADE),
		};

	static String[] stealthBehaviours =
		{
			Foe.StealthBehaviour.toString(Foe.StealthBehaviour.NOT_STEALTHY),
			Foe.StealthBehaviour.toString(Foe.StealthBehaviour.OPPORTUNISTIC),
			Foe.StealthBehaviour.toString(Foe.StealthBehaviour.STEALTH_RELIANT),
		};
	private JTextArea analysisOutput;

	/*-------------------------------------------------------------------------*/
	public FoeTemplatePanel()
	{
		super(SwingEditor.Tab.FOE_TEMPLATES);
	}

	/*-------------------------------------------------------------------------*/
	public Container getEditControls()
	{
		JTabbedPane tabs = new JTabbedPane();

		tabs.add("Stats", getStatsPanel());
		tabs.add("Attacks", getAttacksPanel());
		tabs.add("Art & Scripts", getArtAndScriptsPanel());
		tabs.add("AI Params", getAiPanel());
		tabs.add("Analysis", getAnalysisPanel());

		return tabs;
	}

	private Component getAnalysisPanel()
	{
		JPanel result = new JPanel(new BorderLayout());

		analysisOutput = new JTextArea();
		analysisOutput.setLineWrap(true);
		analysisOutput.setWrapStyleWord(true);
		analysisOutput.setEditable(false);

		JPanel buttons = new JPanel();

		JButton mockCombat1v1 = new JButton("Mock Combat 1v1");
		mockCombat1v1.addActionListener(e -> mockCombat(1));
		buttons.add(mockCombat1v1);

		JButton mockCombat6v6 = new JButton("Mock Combat 6v6");
		mockCombat6v6.addActionListener(e -> mockCombat(6));
		buttons.add(mockCombat6v6);

		JButton equip = new JButton("Equip");
		equip.addActionListener(e-> equip());
		buttons.add(equip);

		result.add(buttons, BorderLayout.NORTH);
		result.add(analysisOutput, BorderLayout.CENTER);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void equip()
	{
		try
		{
			MockCombat mc = new MockCombat();
			Database db = Database.getInstance();
			Maze maze = MockCombat.getMockMaze(db);

			FoeTemplate ft = (FoeTemplate)commit(getCurrentName());
			Foe foe = new Foe(ft);

			StringBuilder sb = new StringBuilder();

			sb.append("Inventory:\n");

			for (Item item : foe.getAllItems())
			{
				sb.append(item.getName()).append(", ");
			}

			sb.append("\n\nEquipped:\n");

			List<EquipableSlot> allEquipableSlots = foe.getAllEquipableSlots();
			allEquipableSlots.sort(Comparator.comparing(EquipableSlot::getType));
			for (EquipableSlot slot : allEquipableSlots)
			{
				sb.append(slot.getType()).append("(").append(slot.getName()).append(")").append(" - ").append(slot.getItem()).append("\n");
			}

			analysisOutput.setText(sb.toString());
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
		finally
		{
			Maze.destroy();
		}
	}

	private void mockCombat(int size)
	{
		analysisOutput.setText("");

		try
		{
			MockCombat mc = new MockCombat();
			Database db = Database.getInstance();
			Maze maze = MockCombat.getMockMaze(db);
			CharacterBuilder cb = new CharacterBuilder(db);

			FoeTemplate ft = (FoeTemplate)commit(getCurrentName());

			List<UnifiedActor> pcs = new ArrayList<>();
			List<Foe> foes = new ArrayList<>();

			for (int i=0; i<size; i++)
			{
				Foe foe = new Foe(ft);
				foes.add(foe);
				pcs.add(MockCombat.getRefCombatPC(cb, foe.getLevel()));
			}

			CombatStatistics s = mc.groupTest(db, maze, pcs, foes);

			analysisOutput.setText(MockCombat.formatCombatStats(s));
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			Maze.destroy();
		}
	}

	/*-------------------------------------------------------------------------*/
	private Component getArtAndScriptsPanel()
	{
		JPanel result = new JPanel(new BorderLayout());

		JPanel leftPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		baseTexture = new JComboBox();
		baseTexture.addActionListener(this);
		quickAssignAllTextures = new JButton("Base Texture:");
		quickAssignAllTextures.addActionListener(this);
		quickAssignAllTextures.setToolTipText("Quick assign all textures");
		dodgyGridBagShite(leftPanel, quickAssignAllTextures, baseTexture, gbc);

		meleeTexture = new JComboBox();
		meleeTexture.addActionListener(this);
		dodgyGridBagShite(leftPanel, new JLabel("Melee Texture:"), meleeTexture, gbc);

		rangedTexture = new JComboBox();
		rangedTexture.addActionListener(this);
		dodgyGridBagShite(leftPanel, new JLabel("Ranged Texture:"), rangedTexture, gbc);

		castSpellTexture = new JComboBox();
		castSpellTexture.addActionListener(this);
		dodgyGridBagShite(leftPanel, new JLabel("Cast Spell Texture:"), castSpellTexture, gbc);

		specialAbilityTexture = new JComboBox();
		specialAbilityTexture.addActionListener(this);
		dodgyGridBagShite(leftPanel, new JLabel("Special Ability Texture:"), specialAbilityTexture, gbc);

		textureTint = new JButton("...");
		textureTint.addActionListener(this);
		dodgyGridBagShite(leftPanel, new JLabel("Texture Tint:"), textureTint, gbc);

		verticalAlignment = new JComboBox();
		verticalAlignment.addActionListener(this);
		dodgyGridBagShite(leftPanel, new JLabel("Vertical Alignment:"), verticalAlignment, gbc);

		appearanceScript = new JComboBox();
		appearanceScript.addActionListener(this);
		dodgyGridBagShite(leftPanel, new JLabel("Appearance Script:"), appearanceScript, gbc);

		appearanceDirection = new JComboBox();
		appearanceDirection.addActionListener(this);
		dodgyGridBagShite(leftPanel, new JLabel("Appearance Direction:"), appearanceDirection, gbc);

		deathScript = new JComboBox();
		deathScript.addActionListener(this);
		dodgyGridBagShite(leftPanel, new JLabel("Death Script:"), deathScript, gbc);

		spriteAnimations = new JComboBox();
		spriteAnimations.addActionListener(this);
		dodgyGridBagShite(leftPanel, new JLabel("Sprite Animations:"), spriteAnimations, gbc);

		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		leftPanel.add(new JLabel(), gbc);

		result.add(leftPanel, BorderLayout.CENTER);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Component getAttacksPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.gridwidth = 2;

		lootTable = new LootTableDisplayWidget("Inventory", this.dirtyFlag);
		result.add(lootTable);

		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth = 1;

		naturalWeapons = new NaturalWeaponsWidget(this.dirtyFlag);
		result.add(naturalWeapons, gbc);

		gbc.gridx+=2;
		gbc.weightx = 1.0;
		gbc.gridheight = 2;

		spellBook = new SpellListPanel(this.dirtyFlag);
		result.add(spellBook, gbc);

		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.gridy++;
		gbc.gridheight = 1;

		spellLikeAbilitiesWidget = new SpellLikeAbilitiesWidget(this.dirtyFlag);
		result.add(spellLikeAbilitiesWidget, gbc);

		gbc.gridx=0;
		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;

		playerBodyParts = new PlayerBodyPartAttackedPanel("Player Body Parts Attacked", dirtyFlag);
		result.add(playerBodyParts, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getStatsPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		JPanel leftPanel = getLeftPanel();
		JPanel rightPanel = getRightPanel();

		result.add(leftPanel, gbc);

		gbc.gridx++;
		gbc.gridy=0;
		gbc.weightx = 1.0;

		result.add(rightPanel, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getRightPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		foeTypes = new FoeTypeSelection(dirtyFlag);
		result.add(foeTypes, gbc);

		gbc.gridy++;
		gbc.weighty = 1.0;

		bodyParts = new BodyPartPercentageTablePanel("Foe Body Parts", dirtyFlag, 0.75, 0.3);
		result.add(bodyParts, gbc);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getLeftPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		pluralName = new JTextField(20);
		pluralName.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Plural Name:"), pluralName, gbc);

		unidentifiedName = new JTextField(20);
		unidentifiedName.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Unidentified Name:"), unidentifiedName, gbc);

		unidentifiedPluralName = new JTextField(20);
		unidentifiedPluralName.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Unidentified Plural:"), unidentifiedPluralName, gbc);

		Vector races = new Vector();
		races.addAll(new ArrayList<>(Database.getInstance().getRaces().keySet()));
		Collections.sort(races);
		races.add(0, NONE);

		race = new JComboBox(races);
		race.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Race:"), race, gbc);

		Vector classes = new Vector();
		classes.addAll(new ArrayList<>(Database.getInstance().getCharacterClasses().keySet()));
		Collections.sort(classes);
		classes.add(0, NONE);

		characterClass = new JComboBox(classes);
		characterClass.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Class:"), characterClass, gbc);

		levelRange = new DiceField();
		levelRange.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Level:"), levelRange, gbc);

		hitPointsRange = new DiceField();
		hitPointsRange.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Hit Points:"), hitPointsRange, gbc);

		actionPointsRange = new DiceField();
		actionPointsRange.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Action Points:"), actionPointsRange, gbc);

		magicPointsRange = new DiceField();
		magicPointsRange.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Magic Points:"), magicPointsRange, gbc);

		experience = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		experience.addChangeListener(this);
		quickAssignXp = new JButton("Experience:");
		quickAssignXp.addActionListener(this);
		dodgyGridBagShite(result, quickAssignXp, experience, gbc);

		stats = new StatModifierComponent(dirtyFlag);
		quickApplyStatPack = new JButton("Stats:");
		quickApplyStatPack.addActionListener(this);
		quickApplyStatPack.setToolTipText("Apply Stat Pack...");
		dodgyGridBagShite(result, quickApplyStatPack, stats, gbc);

		foeGroupBannerModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Foe Group Banner:"), foeGroupBannerModifiers, gbc);

		allFoesBannerModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("All Foes Banner:"), allFoesBannerModifiers, gbc);

		identificationDifficulty = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		identificationDifficulty.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Identification Difficulty:"), identificationDifficulty, gbc);

		faction = new JTextField(20);
		faction.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Faction"), faction, gbc);

		isNpc = new JCheckBox("Is NPC?");
		isNpc.addActionListener(this);
		dodgyGridBagShite(result, isNpc, new JLabel(), gbc);

		cannotBeEvaded = new JCheckBox("Cannot Be Evaded?");
		cannotBeEvaded.addActionListener(this);
		dodgyGridBagShite(result, cannotBeEvaded, new JLabel(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getAiPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		focus = new JComboBox(CharacterClass.Focus.values());
		focus.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Focus:"), focus, gbc);

		evasionBehaviour = new JComboBox(evasionBehaviours);
		evasionBehaviour.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Evasion Behaviour:"), evasionBehaviour, gbc);

		stealthBehaviour = new JComboBox(stealthBehaviours);
		stealthBehaviour.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Stealth Behaviour:"), stealthBehaviour, gbc);

		fleeChance = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		fleeChance.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Flee Chance:"), fleeChance, gbc);

		attitude = new JComboBox(NpcFaction.Attitude.values());
		attitude.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Default Attitude:"), attitude, gbc);

		alliesOnCall = new JComboBox();
		alliesOnCall.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Allies On Call:"), alliesOnCall, gbc);

		foeSpeech = new JComboBox();
		foeSpeech.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Speech:"), foeSpeech, gbc);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		result.add(new JLabel(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getFoeTemplates().values());
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> textures = new Vector<>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(textures);
		baseTexture.setModel(new DefaultComboBoxModel<>(textures));
		meleeTexture.setModel(new DefaultComboBoxModel<>(textures));
		rangedTexture.setModel(new DefaultComboBoxModel<>(textures));
		castSpellTexture.setModel(new DefaultComboBoxModel<>(textures));
		specialAbilityTexture.setModel(new DefaultComboBoxModel<>(textures));
		verticalAlignment.setModel(new DefaultComboBoxModel<>(EngineObject.Alignment.values()));

		lootTable.initForeignKeys();
		
		Vector<String> scripts = new Vector<>(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(scripts);
		scripts.add(0, NONE);
		appearanceScript.setModel(new DefaultComboBoxModel<>(scripts));
		appearanceDirection.setModel(new DefaultComboBoxModel<>(FoeTemplate.AppearanceDirection.values()));
		deathScript.setModel(new DefaultComboBoxModel<>(scripts));

		bodyParts.initForeignKeys();
		playerBodyParts.initForeignKeys();
		spellBook.initForeignKeys();

		Vector<String> encounterTables = new Vector<>(Database.getInstance().getEncounterTables().keySet());
		Collections.sort(encounterTables);
		encounterTables.add(0, NONE);
		alliesOnCall.setModel(new DefaultComboBoxModel<>(encounterTables));

		Vector<String> animations = new Vector<>(Database.getInstance().getObjectAnimations().keySet());
		Collections.sort(animations);
		animations.add(0, NONE);
		spriteAnimations.setModel(new DefaultComboBoxModel<>(animations));

		Vector<String> foeSpeeches = new Vector<>(Database.getInstance().getFoeSpeeches().keySet());
		Collections.sort(foeSpeeches);
		foeSpeeches.add(0, NONE);
		foeSpeech.setModel(new DefaultComboBoxModel<>(foeSpeeches));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		FoeTemplate ft = Database.getInstance().getFoeTemplate(name);

		experience.removeChangeListener(this);
		baseTexture.removeActionListener(this);
		meleeTexture.removeActionListener(this);
		rangedTexture.removeActionListener(this);
		castSpellTexture.removeActionListener(this);
		specialAbilityTexture.removeActionListener(this);
		verticalAlignment.removeActionListener(this);
		evasionBehaviour.removeActionListener(this);
		stealthBehaviour.removeActionListener(this);
		identificationDifficulty.removeChangeListener(this);
		fleeChance.removeChangeListener(this);
		appearanceScript.removeActionListener(this);
		appearanceDirection.removeActionListener(this);
		deathScript.removeActionListener(this);
		faction.removeKeyListener(this);
		focus.removeActionListener(this);
		attitude.removeActionListener(this);
		race.removeActionListener(this);
		characterClass.removeActionListener(this);
		alliesOnCall.removeActionListener(this);
		spriteAnimations.removeActionListener(this);
		foeSpeech.removeActionListener(this);

		pluralName.setText(ft.getPluralName());
		unidentifiedName.setText(ft.getUnidentifiedName());
		unidentifiedPluralName.setText(ft.getUnidentifiedPluralName());
		race.setSelectedItem(ft.getRace()==null?NONE:ft.getRace().getName());
		characterClass.setSelectedItem(ft.getCharacterClass()==null?NONE:ft.getCharacterClass().getName());
		foeTypes.refresh(ft.getTypes());
		levelRange.setDice(ft.getLevelRange());
		hitPointsRange.setDice(ft.getHitPointsRange());
		actionPointsRange.setDice(ft.getActionPointsRange());
		magicPointsRange.setDice(ft.getMagicPointsRange());
		experience.setValue(ft.getExperience());
		stats.setModifier(ft.getStats());
		foeGroupBannerModifiers.setModifier(ft.getFoeGroupBannerModifiers());
		allFoesBannerModifiers.setModifier(ft.getAllFoesBannerModifiers());
		baseTexture.setSelectedItem(ft.getBaseTexture().getName());
		meleeTexture.setSelectedItem(ft.getMeleeAttackTexture().getName());
		rangedTexture.setSelectedItem(ft.getRangedAttackTexture().getName());
		castSpellTexture.setSelectedItem(ft.getCastSpellTexture().getName());
		specialAbilityTexture.setSelectedItem(ft.getSpecialAbilityTexture().getName());
		textureTint.setBackground(ft.getTextureTint()==null?null:ft.getTextureTint());
		verticalAlignment.setSelectedItem(ft.getVerticalAlignment());
		lootTable.refresh(ft.getLoot());
		evasionBehaviour.setSelectedItem(Foe.EvasionBehaviour.toString(ft.getEvasionBehaviour()));
		stealthBehaviour.setSelectedItem(Foe.StealthBehaviour.toString(ft.getStealthBehaviour()));
		identificationDifficulty.setValue(ft.getIdentificationDifficulty());
		fleeChance.setValue(ft.getFleeChance());
		isNpc.setSelected(ft.isNpc());
		cannotBeEvaded.setSelected(ft.cannotBeEvaded());
		naturalWeapons.refreshStrings(ft.getNaturalWeapons());
		spellBook.refresh(ft.getSpellBook());
		spellLikeAbilitiesWidget.refresh(ft.getSpellLikeAbilities());
		bodyParts.refresh(ft.getBodyParts());
		playerBodyParts.refresh(ft.getPlayerBodyParts());
		MazeScript mazeScript = ft.getAppearanceScript();
		if (mazeScript != null)
		{
			appearanceScript.setSelectedItem(mazeScript.getName());
		}
		else
		{
			appearanceScript.setSelectedItem(NONE);
		}
		appearanceDirection.setSelectedItem(ft.getAppearanceDirection());
		spriteAnimations.setSelectedItem(ft.getSpriteAnimations() == null ? EditorPanel.NONE : ft.getSpriteAnimations().getName());
		mazeScript = ft.getDeathScript();
		if (mazeScript != null)
		{
			deathScript.setSelectedItem(mazeScript.getName());
		}
		else
		{
			deathScript.setSelectedItem(NONE);
		}
		faction.setText(ft.getFaction());
		focus.setSelectedItem(ft.getFocus());
		attitude.setSelectedItem(ft.getDefaultAttitude());
		alliesOnCall.setSelectedItem(ft.getAlliesOnCall()==null?NONE:ft.getAlliesOnCall());
		foeSpeech.setSelectedItem(ft.getFoeSpeech() == null ? EditorPanel.NONE : ft.getFoeSpeech().getName());

		analysisOutput.setText("");

		experience.addChangeListener(this);
		baseTexture.addActionListener(this);
		meleeTexture.addActionListener(this);
		rangedTexture.addActionListener(this);
		castSpellTexture.addActionListener(this);
		specialAbilityTexture.addActionListener(this);
		verticalAlignment.addActionListener(this);
		evasionBehaviour.addActionListener(this);
		stealthBehaviour.addActionListener(this);
		appearanceScript.addActionListener(this);
		appearanceDirection.addActionListener(this);
		deathScript.addActionListener(this);
		identificationDifficulty.addChangeListener(this);
		fleeChance.addChangeListener(this);
		faction.addKeyListener(this);
		focus.addActionListener(this);
		attitude.addActionListener(this);
		race.addActionListener(this);
		characterClass.addActionListener(this);
		alliesOnCall.addActionListener(this);
		spriteAnimations.addActionListener(this);
		foeSpeech.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		String raceS = (String)race.getSelectedItem();
		String classS = (String)characterClass.getSelectedItem();

		if (NONE.equals(raceS))
		{
			raceS = null;
		}

		if (NONE.equals(classS))
		{
			classS = null;
		}

		FoeTemplate ft = new FoeTemplate(
			name,
			"",
			"",
			"",
			foeTypes.getFoeTypes(),
			raceS==null?null:Database.getInstance().getRace(raceS),
			classS==null?null:Database.getInstance().getCharacterClass(classS),
			Dice.d1,
			Dice.d1,
			Dice.d1,
			Dice.d1,
			0,
			new StatModifier(),
			new PercentageTable<BodyPart>(),
			getDefaultPlayerBodyPartsAttacked(),
			Database.getInstance().getMazeTexture((String)baseTexture.getItemAt(0)),
			Database.getInstance().getMazeTexture((String)baseTexture.getItemAt(0)),
			Database.getInstance().getMazeTexture((String)baseTexture.getItemAt(0)),
			Database.getInstance().getMazeTexture((String)baseTexture.getItemAt(0)),
			Database.getInstance().getMazeTexture((String)baseTexture.getItemAt(0)),
			EngineObject.Alignment.BOTTOM,
			null,
			Database.getInstance().getLootTable((String)lootTable.getDefault()),
			Foe.EvasionBehaviour.NEVER_EVADE,
			false,
			0,
			new StatModifier(),
			new StatModifier(),
			0,
			Foe.StealthBehaviour.NOT_STEALTHY,
			"",
			false,
			null,
			new ObjectAnimations(),
			FoeTemplate.AppearanceDirection.FROM_LEFT_OR_RIGHT,
			null,
			null,
			null,
			null,
			CharacterClass.Focus.COMBAT,
			NpcFaction.Attitude.ATTACKING,
			null,
			null);
		Database.getInstance().getFoeTemplates().put(name, ft);

		return ft;
	}

	/*-------------------------------------------------------------------------*/
	private PercentageTable<String> getDefaultPlayerBodyPartsAttacked()
	{
		PercentageTable<String> result = new PercentageTable<String>(true);

		result.add(PlayerCharacter.BodyParts.HEAD, 18);
		result.add(PlayerCharacter.BodyParts.TORSO, 33);
		result.add(PlayerCharacter.BodyParts.LEG, 31);
		result.add(PlayerCharacter.BodyParts.HAND, 8);
		result.add(PlayerCharacter.BodyParts.FOOT, 10);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		FoeTemplate ft = Database.getInstance().getFoeTemplates().remove(currentName);
		ft.setName(newName);
		Database.getInstance().getFoeTemplates().put(newName, ft);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		FoeTemplate current = Database.getInstance().getFoeTemplate(currentName);

		FoeTemplate ft = new FoeTemplate(
			newName,
			current.getPluralName(),
			current.getUnidentifiedName(),
			current.getUnidentifiedPluralName(),
			current.getTypes(),
			current.getRace(),
			current.getCharacterClass(),
			current.getHitPointsRange(),
			current.getActionPointsRange(),
			current.getMagicPointsRange(),
			current.getLevelRange(),
			current.getExperience(),
			new StatModifier(current.getStats()),
			new PercentageTable<>(current.getBodyParts()),
			new PercentageTable<>(current.getPlayerBodyParts()),
			current.getBaseTexture(),
			current.getMeleeAttackTexture(),
			current.getRangedAttackTexture(),
			current.getCastSpellTexture(),
			current.getSpecialAbilityTexture(),
			current.getVerticalAlignment(),
			current.getTextureTint(),
			current.getLoot(),
			current.getEvasionBehaviour(),
			current.cannotBeEvaded(),
			current.getIdentificationDifficulty(),
			new StatModifier(current.getFoeGroupBannerModifiers()),
			new StatModifier(current.getAllFoesBannerModifiers()),
			current.getFleeChance(),
			current.getStealthBehaviour(),
			current.getFaction(),
			current.isNpc(),
			current.getAppearanceScript(),
			current.getSpriteAnimations(),
			current.getAppearanceDirection(),
			current.getDeathScript(),
			current.getNaturalWeapons(),
			current.getSpellBook(),
			current.getSpellLikeAbilities(),
			current.getFocus(),
			current.getDefaultAttitude(),
			current.getAlliesOnCall(),
			current.getFoeSpeech());

		Database.getInstance().getFoeTemplates().put(newName, ft);

		return ft;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getFoeTemplates().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		FoeTemplate ft = Database.getInstance().getFoeTemplate(name);

		MazeScript appScript;
		if (NONE.equals(appearanceScript.getSelectedItem()))
		{
			appScript = null;
		}
		else
		{
			appScript = Database.getInstance().getMazeScript((String)appearanceScript.getSelectedItem());
		}

		MazeScript dScript;
		if (NONE.equals(deathScript.getSelectedItem()))
		{
			dScript = null;
		}
		else
		{
			dScript = Database.getInstance().getMazeScript((String)deathScript.getSelectedItem());
		}

		ft.setPluralName(pluralName.getText());
		ft.setUnidentifiedName(unidentifiedName.getText());
		ft.setUnidentifiedPluralName(unidentifiedPluralName.getText());
		ft.setTypes(foeTypes.getFoeTypes());

		String raceS = (String)race.getSelectedItem();
		String classS = (String)characterClass.getSelectedItem();

		ft.setRace(NONE.equals(raceS)?null:Database.getInstance().getRace(raceS));
		ft.setCharacterClass(NONE.equals(classS)?null:Database.getInstance().getCharacterClass(classS));

		ft.setLevelRange(levelRange.getDice());
		ft.setHitPointsRange(hitPointsRange.getDice());
		ft.setActionPointsRange(actionPointsRange.getDice());
		ft.setMagicPointsRange(magicPointsRange.getDice());
		ft.setExperience((Integer)experience.getValue());
		ft.setStats(stats.getModifier());
		ft.setFoeGroupBannerModifiers(foeGroupBannerModifiers.getModifier());
		ft.setAllFoesBannerModifiers(allFoesBannerModifiers.getModifier());
		ft.setNaturalWeapons(naturalWeapons.getNaturalWeaponsStrings());
		ft.setSpellBook(spellBook.getSpellBook());
		ft.setSpellLikeAbilities(spellLikeAbilitiesWidget.getSpellLikeAbilities());
		ft.setBodyParts(bodyParts.getPercentageTable());
		ft.setPlayerBodyParts(playerBodyParts.getPlayerBodyParts());
		ft.setBaseTexture(Database.getInstance().getMazeTexture((String)baseTexture.getSelectedItem()));
		ft.setMeleeAttackTexture(Database.getInstance().getMazeTexture((String)meleeTexture.getSelectedItem()));
		ft.setRangedAttackTexture(Database.getInstance().getMazeTexture((String)rangedTexture.getSelectedItem()));
		ft.setCastSpellTexture(Database.getInstance().getMazeTexture((String)castSpellTexture.getSelectedItem()));
		ft.setSpecialAbilityTexture(Database.getInstance().getMazeTexture((String)specialAbilityTexture.getSelectedItem()));

		if (textureTint.isBackgroundSet())
		{
			ft.setTextureTint(textureTint.getBackground());
		}
		else
		{
			ft.setTextureTint(null);
		}

		ft.setVerticalAlignment((EngineObject.Alignment)verticalAlignment.getSelectedItem());
		ft.setLoot(Database.getInstance().getLootTable(lootTable.getSelectedLootTable()));
		ft.setEvasionBehaviour(Foe.EvasionBehaviour.valueOf((String)evasionBehaviour.getSelectedItem()));
		ft.setStealthBehaviour(Foe.StealthBehaviour.valueOf((String)stealthBehaviour.getSelectedItem()));
		ft.setIdentificationDifficulty((Integer)identificationDifficulty.getValue());
		ft.setFleeChance((Integer)fleeChance.getValue());
		ft.setCannotBeEvaded(cannotBeEvaded.isSelected());
		ft.setFaction(faction.getText().equals("") ? null : faction.getText());
		ft.setNpc(isNpc.isSelected());
		ft.setAppearanceScript(appScript);
		ft.setSpriteAnimations(spriteAnimations.getSelectedItem()==EditorPanel.NONE ? null : Database.getInstance().getObjectAnimation((String)spriteAnimations.getSelectedItem()));
		ft.setAppearanceDirection((FoeTemplate.AppearanceDirection)appearanceDirection.getSelectedItem());
		ft.setDeathScript(dScript);
		ft.setFocus((CharacterClass.Focus)focus.getSelectedItem());
		ft.setDefaultAttitude((NpcFaction.Attitude)attitude.getSelectedItem());
		ft.setAlliesOnCall(NONE==alliesOnCall.getSelectedItem()?null: (String)alliesOnCall.getSelectedItem());

		ft.setFoeSpeech(EditorPanel.NONE.equals(foeSpeech.getSelectedItem()) ? null : Database.getInstance().getFoeSpeech((String)foeSpeech.getSelectedItem()));

		return ft;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == quickAssignAllTextures)
		{
			Object txt = baseTexture.getSelectedItem();
			meleeTexture.setSelectedItem(txt);
			rangedTexture.setSelectedItem(txt);
			castSpellTexture.setSelectedItem(txt);
			specialAbilityTexture.setSelectedItem(txt);
		}
		else if (e.getSource() == quickApplyStatPack)
		{
			showStatPackDialog();
		}
		else if (e.getSource() == quickAssignXp)
		{
			commit(currentName);
			FoeTemplate ft = Database.getInstance().getFoeTemplate(currentName);
			experience.setValue(FoeXpCalculator.calcXp(ft));
		}
		else if (e.getSource() == textureTint)
		{
			Color c = JColorChooser.showDialog(
				SwingEditor.instance,
				"Shade Target Colour",
				Color.BLACK);

			textureTint.setBackground(c);

			SwingEditor.instance.setDirty(dirtyFlag);
		}
		else
		{
			super.actionPerformed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void showStatPackDialog()
	{
		final JDialog dialog = new JDialog(SwingEditor.instance, "Apply Stat Pack", true);
		dialog.setLayout(new BorderLayout());

		final StatPackPanel statPackPanel = new StatPackPanel();
		dialog.add(statPackPanel);

		JPanel buttons = new JPanel();
		JButton ok = new JButton("OK");
		ok.addActionListener(e -> {
			List<StatModifier> modifiers = statPackPanel.getSelectedStatPack();
			BitSet mode = statPackPanel.getSelectedMode();

			applyStatPack(modifiers, mode);
			dialog.setVisible(false);
		});
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(e -> dialog.setVisible(false));
		buttons.add(ok);
		buttons.add(cancel);

		dialog.add(statPackPanel, BorderLayout.CENTER);
		dialog.add(buttons, BorderLayout.SOUTH);

		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private void applyStatPack(List<StatModifier> modifiers, BitSet mode)
	{
		StatModifier current = this.stats.getModifier();

		int minLvl = levelRange.getDice().getMinPossible();
		int avgLvl = (int)Math.ceil(levelRange.getDice().getAverage());
		int maxLvl = levelRange.getDice().getMaxPossible();

		for (StatModifier sm : modifiers)
		{
			for (Stats.Modifier modifier : Stats.allModifiers)
			{
				int currentValue = current.getModifier(modifier);
				int packValue = sm.getModifier(modifier);

				if (mode.get(StatPackPanel.Mode.MULT_BY_MIN_LVL))
				{
					packValue *= minLvl;
				}
				else if (mode.get(StatPackPanel.Mode.MULT_BY_AVG_LVL))
				{
					packValue *= avgLvl;
				}
				else if (mode.get(StatPackPanel.Mode.MULT_BY_MAX_LVL))
				{
					packValue *= maxLvl;
				}

				if (mode.get(StatPackPanel.Mode.ADD))
				{
					currentValue += packValue;
				}
				else if (mode.get(StatPackPanel.Mode.SET))
				{
					currentValue = packValue;
				}

				current.setModifier(modifier, currentValue);
			}
		}

		this.stats.refresh(current);
	}
}
