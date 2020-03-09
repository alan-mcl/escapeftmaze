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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.FoeType;
import mclachlan.maze.stat.Gender;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class FoeTypePanel extends EditorPanel
{
	private StatModifierComponent startingModifiers,
		constantModifiers,
		bannerModifiers,
		attributeCeilings;
	private JCheckBox magicDead;
	private JComboBox specialAbility;
	private PlayerBodyPartTablePanel bodyParts;
	private JTextArea description;
	private NaturalWeaponsWidget naturalWeapons;
	private JComboBox favouredEnemyModifier;

	/*-------------------------------------------------------------------------*/
	public FoeTypePanel()
	{
		super(SwingEditor.Tab.FOE_TYPES);
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

		startingModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Starting Modifiers:"), startingModifiers, gbc);

		constantModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Constant Modifiers:"), constantModifiers, gbc);

		bannerModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Banner Modifiers:"), bannerModifiers, gbc);

		attributeCeilings = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Attribute Ceilings:"), attributeCeilings, gbc);

		magicDead = new JCheckBox("Magic Dead?");
		magicDead.addActionListener(this);
		dodgyGridBagShite(result, magicDead, new JLabel(), gbc);

		specialAbility = new JComboBox();
		specialAbility.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Special Ability:"), specialAbility, gbc);

		Vector<Stats.Modifier> vec = new Vector<Stats.Modifier>();
		vec.addAll(Stats.allModifiers);
		Collections.sort(vec);
		vec.add(0, Stats.Modifier.NONE);
		favouredEnemyModifier = new JComboBox(vec);
		favouredEnemyModifier.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Favoured Enemy Modifier:"), favouredEnemyModifier, gbc);

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
		return new Vector<>(Database.getInstance().getFoeTypes().values());
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
		FoeType ft = Database.getInstance().getFoeTypes().get(name);

		specialAbility.removeActionListener(this);
		favouredEnemyModifier.removeActionListener(this);

		startingModifiers.setModifier(ft.getStartingModifiers());
		constantModifiers.setModifier(ft.getConstantModifiers());
		bannerModifiers.setModifier(ft.getBannerModifiers());
		attributeCeilings.setModifier(ft.getAttributeCeilings());
		magicDead.setSelected(ft.isMagicDead());
		Spell sa = ft.getSpecialAbility();
		this.specialAbility.setSelectedItem(sa == null ? NONE : sa.getName());
		description.setText(ft.getDescription());
		description.setCaretPosition(0);
		Stats.Modifier fem = ft.getFavouredEnemyModifier();
		favouredEnemyModifier.setSelectedItem(fem == null ? NONE : fem);

		bodyParts.refresh(
			ft.getHead(),
			ft.getTorso(),
			ft.getLeg(),
			ft.getHand(),
			ft.getFoot());

		naturalWeapons.refresh(ft.getNaturalWeapons());

		specialAbility.addActionListener(this);
		favouredEnemyModifier.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		FoeType ft = new FoeType(
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
			null);

		Database.getInstance().getFoeTypes().put(name, ft);

		return ft;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		FoeType ft = Database.getInstance().getFoeTypes().remove(currentName);
		ft.setName(newName);
		Database.getInstance().getFoeTypes().put(newName, ft);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		FoeType current = Database.getInstance().getFoeTypes().get(currentName);

		FoeType ft = new FoeType(
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
			new ArrayList<>(),
			current.isMagicDead(),
			current.getSpecialAbility(),
			null, //todo: duplicate starting items
			current.getNaturalWeapons(),
			current.getSuggestedNames(),
			current.getUnlockVariable(),
			current.getUnlockDescription(),
			current.getFavouredEnemyModifier());

		Database.getInstance().getFoeTypes().put(newName, ft);

		return ft;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getFoeTypes().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		FoeType ft = Database.getInstance().getFoeTypes().get(name);

		ft.setDescription(description.getText());
		ft.setStartingModifiers(startingModifiers.getModifier());
		ft.setConstantModifiers(constantModifiers.getModifier());
		ft.setBannerModifiers(bannerModifiers.getModifier());
		ft.setAttributeCeilings(attributeCeilings.getModifier());
		ft.setMagicDead(magicDead.isSelected());

		ft.setHead(bodyParts.getHead());
		ft.setTorso(bodyParts.getTorso());
		ft.setLeg(bodyParts.getLeg());
		ft.setHand(bodyParts.getHand());
		ft.setFoot(bodyParts.getFoot());

		ft.setNaturalWeapons(naturalWeapons.getNaturalWeapons());

		String spellName = (String)specialAbility.getSelectedItem();
		ft.setSpecialAbility(spellName.equals(NONE) ? null : Database.getInstance().getSpell(spellName));

		Stats.Modifier fem = (Stats.Modifier)favouredEnemyModifier.getSelectedItem();
		ft.setFavouredEnemyModifier(Stats.Modifier.NONE.equals(fem) ? null : fem);

		return ft;
	}
}
