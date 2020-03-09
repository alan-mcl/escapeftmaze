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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class RacePanel extends EditorPanel
{
	private JSpinner startingHitPointPercent,
		startingActionPointPercent,
		startingMagicPointPercent;
	private StatModifierComponent startingModifiers,
		constantModifiers,
		bannerModifiers,
		attributeCeilings;
	private JTextField leftHandIcon, rightHandIcon, unlockVariable, characterCreationImage;
	private GenderSelection allowedGenders;
	private JCheckBox magicDead;
	private JComboBox specialAbility, favouredEnemyModifier;
	private PlayerBodyPartTablePanel bodyParts;
	private JTextArea description, unlockDescription;
	private StartingItemsPanel startingItems;
	private NaturalWeaponsWidget naturalWeapons;

	/*-------------------------------------------------------------------------*/
	public RacePanel()
	{
		super(SwingEditor.Tab.RACES);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel result = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		JPanel left = getLeftPanel();
		result.add(left, gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;

		JPanel right = getRightPanel();
		result.add(right, gbc);

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
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		description = new JTextArea(20, 30);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.addKeyListener(this);
		result.add(new JScrollPane(description), gbc);
		
		gbc.gridy++;
		startingItems = new StartingItemsPanel(SwingEditor.Tab.RACES);
		result.add(startingItems, gbc);

		gbc.gridy++;
		allowedGenders = new GenderSelection(dirtyFlag, true);
		JPanel temp = new JPanel();
		temp.setBorder(BorderFactory.createTitledBorder("Genders & Suggested Names"));
		temp.add(allowedGenders);
		result.add(temp, gbc);

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
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		startingHitPointPercent = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		startingHitPointPercent.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Starting Hit Point %"), startingHitPointPercent, gbc);

		startingActionPointPercent = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		startingActionPointPercent.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Starting Action Point %"), startingActionPointPercent, gbc);

		startingMagicPointPercent = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		startingMagicPointPercent.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Starting Magic Point %"), startingMagicPointPercent, gbc);

		startingModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Starting Modifiers:"), startingModifiers, gbc);

		constantModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Constant Modifiers:"), constantModifiers, gbc);

		bannerModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Banner Modifiers:"), bannerModifiers, gbc);

		attributeCeilings = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Attribute Ceilings:"), attributeCeilings, gbc);

		leftHandIcon = new JTextField(20);
		leftHandIcon.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Left Hand Icon:"), leftHandIcon, gbc);

		rightHandIcon = new JTextField(20);
		rightHandIcon.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Right Hand Icon:"), rightHandIcon, gbc);

		magicDead = new JCheckBox("Magic Dead?");
		magicDead.addActionListener(this);
		dodgyGridBagShite(result, magicDead, new JLabel(), gbc);

		specialAbility = new JComboBox();
		specialAbility.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Special Ability:"), specialAbility, gbc);

		unlockVariable = new JTextField(20);
		unlockVariable.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Unlock Variable:"), unlockVariable, gbc);

		unlockDescription = new JTextArea(3, 20);
		unlockDescription.setLineWrap(true);
		unlockDescription.setWrapStyleWord(true);
		unlockDescription.addKeyListener(this);
		result.add(new JScrollPane(unlockDescription), gbc);
		dodgyGridBagShite(result, new JLabel("Unlock Desc:"), unlockDescription, gbc);

		Vector<Stats.Modifier> vec = new Vector<Stats.Modifier>();
		vec.addAll(Stats.allModifiers);
		Collections.sort(vec);
		vec.add(0, Stats.Modifier.NONE);
		favouredEnemyModifier = new JComboBox(vec);
		favouredEnemyModifier.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Favoured Enemy Modifier:"), favouredEnemyModifier, gbc);

		characterCreationImage = new JTextField(20);
		characterCreationImage.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Character Creation Image:"), characterCreationImage, gbc);

		bodyParts = new PlayerBodyPartTablePanel("Body Parts", dirtyFlag);
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth=2;
		result.add(bodyParts, gbc);

		naturalWeapons = new NaturalWeaponsWidget(dirtyFlag);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth=2;
		result.add(naturalWeapons, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getRaces().values());
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> spells = new Vector<String>(Database.getInstance().getSpells().keySet());
		Collections.sort(spells);
		spells.add(0, NONE);
		specialAbility.setModel(new DefaultComboBoxModel(spells));

		bodyParts.initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		Race race = Database.getInstance().getRace(name);

		startingHitPointPercent.removeChangeListener(this);
		startingActionPointPercent.removeChangeListener(this);
		startingMagicPointPercent.removeChangeListener(this);
		specialAbility.removeActionListener(this);
		favouredEnemyModifier.removeActionListener(this);

		startingHitPointPercent.setValue(race.getStartingHitPointPercent());
		startingActionPointPercent.setValue(race.getStartingActionPointPercent());
		startingMagicPointPercent.setValue(race.getStartingMagicPointPercent());
		startingModifiers.setModifier(race.getStartingModifiers());
		constantModifiers.setModifier(race.getConstantModifiers());
		bannerModifiers.setModifier(race.getBannerModifiers());
		attributeCeilings.setModifier(race.getAttributeCeilings());
		leftHandIcon.setText(race.getLeftHandIcon());
		rightHandIcon.setText(race.getRightHandIcon());
		unlockVariable.setText(race.getUnlockVariable());
		magicDead.setSelected(race.isMagicDead());
		Spell sa = race.getSpecialAbility();
		specialAbility.setSelectedItem(sa==null?NONE:sa.getName());
		description.setText(race.getDescription());
		description.setCaretPosition(0);
		unlockDescription.setText(race.getUnlockDescription());
		unlockDescription.setCaretPosition(0);
		allowedGenders.refreshGenders(race.getAllowedGenders(), race.getSuggestedNames());
		startingItems.refresh(race.getStartingItems());
		Stats.Modifier fem = race.getFavouredEnemyModifier();
		favouredEnemyModifier.setSelectedItem(fem == null ? NONE : fem);
		characterCreationImage.setText(race.getCharacterCreationImage());

		bodyParts.refresh(
			race.getHead(),
			race.getTorso(),
			race.getLeg(),
			race.getHand(),
			race.getFoot());

		naturalWeapons.refresh(race.getNaturalWeapons());

		startingHitPointPercent.addChangeListener(this);
		startingActionPointPercent.addChangeListener(this);
		startingMagicPointPercent.addChangeListener(this);
		specialAbility.addActionListener(this);
		favouredEnemyModifier.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		Race race = new Race(
			name,
			"",
			0,
			0,
			0,
			new StatModifier(),
			new StatModifier(),
			new StatModifier(),
			new StatModifier(),
			null,
			null,
			null,
			null,
			null,
			"",
			"",
			new ArrayList<Gender>(),
			false,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null);

		Database.getInstance().getRaces().put(name, race);

		return race;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		Race race = Database.getInstance().getRaces().remove(currentName);
		race.setName(newName);
		Database.getInstance().getRaces().put(newName, race);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		Race current = Database.getInstance().getRaces().get(currentName);

		Race race = new Race(
			newName,
			current.getDescription(),
			current.getStartingHitPointPercent(),
			current.getStartingActionPointPercent(),
			current.getStartingMagicPointPercent(),
			new StatModifier(current.getStartingModifiers()),
			new StatModifier(current.getConstantModifiers()),
			new StatModifier(current.getBannerModifiers()),
			new StatModifier(current.getAttributeCeilings()), 
			current.getHead(),
			current.getTorso(),
			current.getLeg(),
			current.getHand(),
			current.getFoot(),
			current.getLeftHandIcon(),
			current.getRightHandIcon(),
			new ArrayList<Gender>(current.getAllowedGenders()),
			current.isMagicDead(),
			current.getSpecialAbility(),
			null, //todo: duplicate starting items
			current.getNaturalWeapons(),
			current.getSuggestedNames(),
			current.getUnlockVariable(),
			current.getUnlockDescription(),
			current.getFavouredEnemyModifier(),
			current.getCharacterCreationImage());

		Database.getInstance().getRaces().put(newName, race);

		return race;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getRaces().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		Race r = Database.getInstance().getRaces().get(name);

		r.setDescription(description.getText());
		r.setStartingHitPointPercent((Integer)startingHitPointPercent.getValue());
		r.setStartingActionPointPercent((Integer)startingActionPointPercent.getValue());
		r.setStartingMagicPointPercent((Integer)startingMagicPointPercent.getValue());
		r.setStartingModifiers(startingModifiers.getModifier());
		r.setConstantModifiers(constantModifiers.getModifier());
		r.setBannerModifiers(bannerModifiers.getModifier());
		r.setAttributeCeilings(attributeCeilings.getModifier());
		r.setLeftHandIcon(leftHandIcon.getText());
		r.setRightHandIcon(rightHandIcon.getText());
		r.setUnlockVariable(unlockVariable.getText());
		r.setUnlockDescription(unlockDescription.getText());
		r.setMagicDead(magicDead.isSelected());
		r.setAllowedGenders(allowedGenders.getAllowedGendersList());
		r.setSuggestedNames(allowedGenders.getSuggestedNamesMap());
		r.setStartingItems(startingItems.getStartingItems());
		Stats.Modifier fem = (Stats.Modifier)favouredEnemyModifier.getSelectedItem();
		r.setFavouredEnemyModifier(Stats.Modifier.NONE.equals(fem) ? null : fem);

		r.setHead(bodyParts.getHead());
		r.setTorso(bodyParts.getTorso());
		r.setLeg(bodyParts.getLeg());
		r.setHand(bodyParts.getHand());
		r.setFoot(bodyParts.getFoot());

		r.setNaturalWeapons(naturalWeapons.getNaturalWeapons());

		String spellName = (String)specialAbility.getSelectedItem();
		r.setSpecialAbility(spellName.equals(NONE)?null:Database.getInstance().getSpell(spellName));

		r.setCharacterCreationImage(characterCreationImage.getText());

		return r;
	}
}
