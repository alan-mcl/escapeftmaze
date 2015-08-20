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
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.util.FoeXpCalculator;

/**
 *
 */
public class FoeTemplatePanel extends EditorPanel
{
	private JTextField pluralName, unidentifiedName, unidentifiedPluralName, type, faction;
	private DiceField hitPointsRange, actionPointsRange, magicPointsRange, levelRange;
	private JSpinner experience, identificationDifficulty, fleeChance;
	private StatModifierComponent stats, foeGroupBannerModifiers, allFoesBannerModifiers;
	private JComboBox baseTexture, meleeTexture, rangedTexture, castSpellTexture,
		specialAbilityTexture, evasionBehaviour, stealthBehaviour,
		appearanceScript, deathScript, focus, attitude;
	private JCheckBox cannotBeEvaded, immuneToCriticals, isNpc;
	private JButton quickAssignAllTextures, quickApplyStatPack, quickAssignXp;

	private BodyPartPercentageTablePanel bodyParts;
	private PlayerBodyPartAttackedPanel playerBodyParts;

	private NaturalWeaponsWidget naturalWeapons;
	private SpellListPanel spellBook;
	private SpellLikeAbilitiesWidget spellLikeAbilitiesWidget;
	private LootTableDisplayWidget lootTable;

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
		tabs.add("AI Params", getAiPanel());
		tabs.add("Attacks", getAttacksPanel());
		tabs.add("Art & Scripts", getArtAndScriptsPanel());

		return tabs;
	}

	/*-------------------------------------------------------------------------*/
	private Component getArtAndScriptsPanel()
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

		baseTexture = new JComboBox();
		baseTexture.addActionListener(this);
		quickAssignAllTextures = new JButton("Base Texture:");
		quickAssignAllTextures.addActionListener(this);
		quickAssignAllTextures.setToolTipText("Quick assign all textures");
		dodgyGridBagShite(result, quickAssignAllTextures, baseTexture, gbc);

		meleeTexture = new JComboBox();
		meleeTexture.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Melee Texture:"), meleeTexture, gbc);

		rangedTexture = new JComboBox();
		rangedTexture.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Ranged Texture:"), rangedTexture, gbc);

		castSpellTexture = new JComboBox();
		castSpellTexture.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Cast Spell Texture:"), castSpellTexture, gbc);

		specialAbilityTexture = new JComboBox();
		specialAbilityTexture.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Special Ability Texture:"), specialAbilityTexture, gbc);

		appearanceScript = new JComboBox();
		appearanceScript.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Appearance Script:"), appearanceScript, gbc);

		deathScript = new JComboBox();
		deathScript.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Death Script:"), deathScript, gbc);

		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel(), gbc);

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

		pluralName = new JTextField(20);
		pluralName.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Plural Name:"), pluralName, gbc);

		unidentifiedName = new JTextField(20);
		unidentifiedName.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Unidentified Name:"), unidentifiedName, gbc);

		unidentifiedPluralName = new JTextField(20);
		unidentifiedPluralName.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Unidentified Plural:"), unidentifiedPluralName, gbc);

		type = new JTextField(20);
		type.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Type:"), type, gbc);

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

		immuneToCriticals = new JCheckBox("Immune To Criticals?");
		immuneToCriticals.addActionListener(this);
		dodgyGridBagShite(result, immuneToCriticals, new JLabel(), gbc);

		cannotBeEvaded = new JCheckBox("Cannot Be Evaded?");
		cannotBeEvaded.addActionListener(this);
		dodgyGridBagShite(result, cannotBeEvaded, new JLabel(), gbc);

		gbc.gridy++;
		gbc.weighty = 1.0;

		bodyParts = new BodyPartPercentageTablePanel("Foe Body Parts", dirtyFlag, 0.75, 0.3);
		result.add(bodyParts, gbc);

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

		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getFoeTemplates().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> textures = new Vector<String>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(textures);
		baseTexture.setModel(new DefaultComboBoxModel(textures));
		meleeTexture.setModel(new DefaultComboBoxModel(textures));
		rangedTexture.setModel(new DefaultComboBoxModel(textures));
		castSpellTexture.setModel(new DefaultComboBoxModel(textures));
		specialAbilityTexture.setModel(new DefaultComboBoxModel(textures));

		lootTable.initForeignKeys();
		
		Vector<String> scripts = new Vector<String>(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(scripts);
		scripts.add(0, NONE);
		appearanceScript.setModel(new DefaultComboBoxModel(scripts));
		deathScript.setModel(new DefaultComboBoxModel(scripts));

		bodyParts.initForeignKeys();
		playerBodyParts.initForeignKeys();
		spellBook.initForeignKeys();
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
		evasionBehaviour.removeActionListener(this);
		stealthBehaviour.removeActionListener(this);
		identificationDifficulty.removeChangeListener(this);
		fleeChance.removeChangeListener(this);
		appearanceScript.removeActionListener(this);
		deathScript.removeActionListener(this);
		faction.removeKeyListener(this);
		focus.removeActionListener(this);
		attitude.removeActionListener(this);

		pluralName.setText(ft.getPluralName());
		unidentifiedName.setText(ft.getUnidentifiedName());
		unidentifiedPluralName.setText(ft.getUnidentifiedPluralName());
		type.setText(ft.getType());
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
		lootTable.refresh(ft.getLoot());
		evasionBehaviour.setSelectedItem(Foe.EvasionBehaviour.toString(ft.getEvasionBehaviour()));
		stealthBehaviour.setSelectedItem(Foe.StealthBehaviour.toString(ft.getStealthBehaviour()));
		identificationDifficulty.setValue(ft.getIdentificationDifficulty());
		fleeChance.setValue(ft.getFleeChance());
		isNpc.setSelected(ft.isNpc());
		immuneToCriticals.setSelected(ft.isImmuneToCriticals());
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

		experience.addChangeListener(this);
		baseTexture.addActionListener(this);
		meleeTexture.addActionListener(this);
		rangedTexture.addActionListener(this);
		castSpellTexture.addActionListener(this);
		specialAbilityTexture.addActionListener(this);
		evasionBehaviour.addActionListener(this);
		stealthBehaviour.addActionListener(this);
		appearanceScript.addActionListener(this);
		deathScript.addActionListener(this);
		identificationDifficulty.addChangeListener(this);
		fleeChance.addChangeListener(this);
		faction.addKeyListener(this);
		focus.addActionListener(this);
		attitude.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		FoeTemplate ft = new FoeTemplate(
			name,
			"",
			"",
			"",
			"",
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
			Database.getInstance().getLootTable((String)lootTable.getDefault()),
			Foe.EvasionBehaviour.NEVER_EVADE,
			false,
			0,
			new StatModifier(),
			new StatModifier(),
			false,
			0,
			Foe.StealthBehaviour.NOT_STEALTHY,
			"",
			false,
			null,
			null,
			null,
			null,
			null,
			CharacterClass.Focus.COMBAT,
			NpcFaction.Attitude.ATTACKING);
		Database.getInstance().getFoeTemplates().put(name, ft);
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
	public void copyItem(String newName)
	{
		FoeTemplate current = Database.getInstance().getFoeTemplate(currentName);

		FoeTemplate ft = new FoeTemplate(
			newName,
			current.getPluralName(),
			current.getUnidentifiedName(),
			current.getUnidentifiedPluralName(),
			current.getType(),
			current.getHitPointsRange(),
			current.getActionPointsRange(),
			current.getMagicPointsRange(),
			current.getLevelRange(),
			current.getExperience(),
			new StatModifier(current.getStats()),
			new PercentageTable<BodyPart>(current.getBodyParts()),
			new PercentageTable<String>(current.getPlayerBodyParts()),
			current.getBaseTexture(),
			current.getMeleeAttackTexture(),
			current.getRangedAttackTexture(),
			current.getCastSpellTexture(),
			current.getSpecialAbilityTexture(),
			current.getLoot(),
			current.getEvasionBehaviour(),
			current.cannotBeEvaded(),
			current.getIdentificationDifficulty(),
			new StatModifier(current.getFoeGroupBannerModifiers()),
			new StatModifier(current.getAllFoesBannerModifiers()),
			current.isImmuneToCriticals(),
			current.getFleeChance(),
			current.getStealthBehaviour(),
			current.getFaction(),
			current.isNpc(),
			current.getAppearanceScript(),
			current.getDeathScript(),
			current.getNaturalWeapons(),
			current.getSpellBook(),
			current.getSpellLikeAbilities(),
			current.getFocus(),
			current.getDefaultAttitude());

		Database.getInstance().getFoeTemplates().put(newName, ft);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getFoeTemplates().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		FoeTemplate ft = Database.getInstance().getFoeTemplate(name);

		MazeScript appScript;
		if (NONE.equals(appearanceScript.getSelectedItem()))
		{
			appScript = null;
		}
		else
		{
			appScript = Database.getInstance().getScript((String)appearanceScript.getSelectedItem());
		}

		MazeScript dScript;
		if (NONE.equals(deathScript.getSelectedItem()))
		{
			dScript = null;
		}
		else
		{
			dScript = Database.getInstance().getScript((String)deathScript.getSelectedItem());
		}

		ft.setPluralName(pluralName.getText());
		ft.setUnidentifiedName(unidentifiedName.getText());
		ft.setUnidentifiedPluralName(unidentifiedPluralName.getText());
		ft.setType(type.getText());
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
		ft.setLoot(Database.getInstance().getLootTable(lootTable.getSelectedLootTable()));
		ft.setEvasionBehaviour(Foe.EvasionBehaviour.valueOf((String)evasionBehaviour.getSelectedItem()));
		ft.setStealthBehaviour(Foe.StealthBehaviour.valueOf((String)stealthBehaviour.getSelectedItem()));
		ft.setIdentificationDifficulty((Integer)identificationDifficulty.getValue());
		ft.setFleeChance((Integer)fleeChance.getValue());
		ft.setCannotBeEvaded(cannotBeEvaded.isSelected());
		ft.setImmuneToCriticals(immuneToCriticals.isSelected());
		ft.setFaction(faction.getText().equals("") ? null : faction.getText());
		ft.setNpc(isNpc.isSelected());
		ft.setAppearanceScript(appScript);
		ft.setDeathScript(dScript);
		ft.setFocus((CharacterClass.Focus)focus.getSelectedItem());
		ft.setDefaultAttitude((NpcFaction.Attitude)attitude.getSelectedItem());
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
		ok.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				List<StatModifier> modifiers = statPackPanel.getSelectedStatPack();
				BitSet mode = statPackPanel.getSelectedMode();

				applyStatPack(modifiers, mode);
				dialog.setVisible(false);
			}
		});
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dialog.setVisible(false);
			}
		});
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
			for (String modifier : Stats.allModifiers)
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
