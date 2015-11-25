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

import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.game.MazeScript;

/**
 *
 */
public class SpellPanel extends EditorPanel
{
	private JTextField displayName;
	private JComboBox targetType, school, book, usabilityType, primaryModifier, secondaryModifier;
	private SpellEffectGroupOfPossibilitiesPanel effects;
	private JTextArea description;
	private JSpinner level;
	private ValueComponent hitPointCost, actionPointCost, magicPointCost;
	private StatModifierComponent requirementsToLearn;
	private ManaRequirementPanel manaRequirements;
	private JComboBox castByPlayerScript, castByFoeScript;
	private JCheckBox wildMagic;
	private ValueComponent wildMagicValue;
	private JComboBox[] wildMagicTable;

	private static Vector<String> schools = new Vector<String>();
	private static Vector<String> books = new Vector<String>();

	/*-------------------------------------------------------------------------*/
	static
	{
		schools.addElement(MagicSys.SpellSchool.BEGUILMENT);
		schools.addElement(MagicSys.SpellSchool.BLESSING);
		schools.addElement(MagicSys.SpellSchool.CONJURATION);
		schools.addElement(MagicSys.SpellSchool.CURSE);
		schools.addElement(MagicSys.SpellSchool.EVOCATION);
		schools.addElement(MagicSys.SpellSchool.ILLUSION);
		schools.addElement(MagicSys.SpellSchool.TRANSMUTATION);

		books.addElement(MagicSys.SpellBook.BLACK_MAGIC.getName());
		books.addElement(MagicSys.SpellBook.DRUIDISM.getName());
		books.addElement(MagicSys.SpellBook.ELEMENTALISM.getName());
		books.addElement(MagicSys.SpellBook.ENCHANTMENT.getName());
		books.addElement(MagicSys.SpellBook.SORCERY.getName());
		books.addElement(MagicSys.SpellBook.WHITE_MAGIC.getName());
		books.addElement(MagicSys.SpellBook.WITCHCRAFT.getName());
	}

	/*-------------------------------------------------------------------------*/
	public SpellPanel()
	{
		super(SwingEditor.Tab.SPELLS);
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
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		description = new JTextArea(10, 30);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.addKeyListener(this);
		result.add(new JScrollPane(description), gbc);

		gbc.gridy++;
		effects = new SpellEffectGroupOfPossibilitiesPanel(dirtyFlag, 0.5);
		result.add(effects, gbc);

		gbc.gridy++;
		wildMagic = new JCheckBox("Is Wild Magic?");
		wildMagic.addActionListener(this);
		result.add(wildMagic, gbc);

		gbc.gridy++;
		wildMagicValue = new ValueComponent(dirtyFlag);
		wildMagicValue.setEnabled(false);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = 1;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel("Wild Magic Value"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(wildMagicValue, gbc);

		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		JPanel wildMagicPanel = new JPanel(new GridLayout(10, 1));
		wildMagicTable = new JComboBox[10];
		for (int i = 0; i < wildMagicTable.length; i++)
		{
			wildMagicTable[i] = new JComboBox();
			wildMagicTable[i].setEnabled(false);
			wildMagicTable[i].addActionListener(this);
			wildMagicPanel.add(wildMagicTable[i]);
		}
		gbc.gridy++;
		result.add(wildMagicPanel, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getLeftPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());

		Vector<String> validTargetTypes = new Vector<String>();
		for (int i=0; i<MagicSys.SpellTargetType.MAX; i++)
		{
			validTargetTypes.addElement(MagicSys.SpellTargetType.describe(i));
		}

		Vector<String> usabilityTypes = new Vector<String>();
		for (int i=0; i<MagicSys.SpellUsabilityType.MAX; i++)
		{
			usabilityTypes.add(MagicSys.SpellUsabilityType.describe(i));
		}

		Vector<String> modifiers = new Vector<String>(Stats.allModifiers);
		Collections.sort(modifiers);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		displayName = new JTextField(15);
		displayName.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Display Name: "), displayName, gbc);

		level = new JSpinner(new SpinnerNumberModel(1, 1, 256, 1));
		level.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Level: "), level, gbc);

		hitPointCost = new ValueComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Hit point Cost: "), hitPointCost, gbc);

		actionPointCost = new ValueComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Action point Cost: "), actionPointCost, gbc);

		magicPointCost = new ValueComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Magic point Cost: "), magicPointCost, gbc);

		book = new JComboBox(books);
		book.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Book: "), book, gbc);

		school = new JComboBox(schools);
		school.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("School: "), school, gbc);

		targetType = new JComboBox(validTargetTypes);
		targetType.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Target Type: "), targetType, gbc);

		usabilityType = new JComboBox(usabilityTypes);
		usabilityType.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Usable: "), usabilityType, gbc);

		primaryModifier = new JComboBox(modifiers);
		primaryModifier.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Primary Modifier: "), primaryModifier, gbc);

		secondaryModifier = new JComboBox(modifiers);
		secondaryModifier.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Secondary Modifier: "), secondaryModifier, gbc);

		requirementsToLearn = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Required To Learn: "), requirementsToLearn, gbc);

		castByPlayerScript = new JComboBox();
		castByPlayerScript.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Cast By Player Script: "), castByPlayerScript, gbc);

		castByFoeScript = new JComboBox();
		castByFoeScript.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Cast By Foe Script: "), castByFoeScript, gbc);
		
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		manaRequirements = new ManaRequirementPanel(dirtyFlag);
		result.add(manaRequirements, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getSpells().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(vec);
		vec.add(0, NONE);
		castByFoeScript.setModel(new DefaultComboBoxModel(vec));
		castByPlayerScript.setModel(new DefaultComboBoxModel(vec));
		effects.initForeignKeys();

		Vector<String> vec2 = new Vector<String>(Database.getInstance().getSpellList());
		Collections.sort(vec2);
		for (int i = 0; i < wildMagicTable.length; i++)
		{
			wildMagicTable[i].setModel(new DefaultComboBoxModel(vec2));
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		Spell s = Database.getInstance().getSpell(name);

		level.removeChangeListener(this);
		book.removeActionListener(this);
		school.removeActionListener(this);
		targetType.removeActionListener(this);
		usabilityType.removeActionListener(this);
		primaryModifier.removeActionListener(this);
		secondaryModifier.removeActionListener(this);
		castByPlayerScript.removeActionListener(this);
		castByFoeScript.removeActionListener(this);
		wildMagic.removeActionListener(this);
		for (int i = 0; i < wildMagicTable.length; i++)
		{
			wildMagicTable[i].removeActionListener(this);
		}

		level.setValue(s.getLevel());
		displayName.setText(s.getDisplayName());
		hitPointCost.refresh(s.getHitPointCost());
		actionPointCost.refresh(s.getActionPointCost());
		magicPointCost.refresh(s.getMagicPointCost());
		book.setSelectedItem(s.getBook().getName());
		school.setSelectedItem(s.getSchool());
		targetType.setSelectedIndex(s.getTargetType());
		usabilityType.setSelectedIndex(s.getUsabilityType());
		primaryModifier.setSelectedItem(s.getPrimaryModifier());
		secondaryModifier.setSelectedItem(s.getSecondaryModifier());
		requirementsToLearn.setModifier(s.getRequirementsToLearn());
		MazeScript cbps = s.getCastByPlayerScript();
		castByPlayerScript.setSelectedItem(cbps==null?NONE:cbps.getName());
		MazeScript cbfs = s.getCastByFoeScript();
		castByFoeScript.setSelectedItem(cbfs==null?NONE:cbfs.getName());
		description.setText(s.getDescription());
		description.setCaretPosition(0);
		effects.refresh(s.getEffects());
		manaRequirements.refresh(s.getRequirementsToCast());

		if (s.getWildMagicValue() != null)
		{
			wildMagic.setSelected(true);
			setWildMagicEnabled(true);
			wildMagicValue.refresh(s.getWildMagicValue());
			for (int i = 0; i < wildMagicTable.length; i++)
			{
				wildMagicTable[i].setSelectedItem(s.getWildMagicTable()[i]);
			}
		}
		else
		{
			wildMagic.setSelected(false);
			setWildMagicEnabled(false);
		}

		level.addChangeListener(this);
		book.addActionListener(this);
		school.addActionListener(this);
		targetType.addActionListener(this);
		usabilityType.addActionListener(this);
		primaryModifier.addActionListener(this);
		secondaryModifier.addActionListener(this);
		castByPlayerScript.addActionListener(this);
		castByFoeScript.addActionListener(this);
		wildMagic.addActionListener(this);
		for (int i = 0; i < wildMagicTable.length; i++)
		{
			wildMagicTable[i].addActionListener(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		Spell spell = new Spell(
			name,
			"",
			new Value(0, Value.SCALE.NONE),
			new Value(0, Value.SCALE.NONE),
			new Value(0, Value.SCALE.NONE),
			"",
			1,
			MagicSys.SpellTargetType.ALL_FOES,
			MagicSys.SpellUsabilityType.ANY_TIME,
			MagicSys.SpellSchool.BEGUILMENT,
			MagicSys.SpellBook.SORCERY,
			new GroupOfPossibilities<SpellEffect>(),
			new ArrayList<ManaRequirement>(),
			new StatModifier(),
			null,
			null,
			Stats.Modifiers.ALCHEMIC,
			Stats.Modifiers.ALCHEMIC,
			null,
			null);
		Database.getInstance().getSpells().put(name, spell);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		Spell current = Database.getInstance().getSpells().remove(currentName);
		current.setName(newName);
		Database.getInstance().getSpells().put(newName, current);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		Spell current = Database.getInstance().getSpells().get(currentName);
		Spell spell = new Spell(
			newName,
			current.getDisplayName(),
			current.getHitPointCost(),
			current.getActionPointCost(),
			current.getMagicPointCost(),
			current.getDescription(),
			current.getLevel(),
			current.getTargetType(),
			current.getUsabilityType(),
			current.getSchool(),
			current.getBook(),
			new GroupOfPossibilities<SpellEffect>(current.getEffects()),
			new ArrayList<ManaRequirement>(current.getRequirementsToCast()),
			new StatModifier(current.getRequirementsToLearn()),
			current.getCastByPlayerScript(),
			current.getCastByFoeScript(),
			current.getPrimaryModifier(),
			current.getSecondaryModifier(),
			current.getWildMagicValue()==null?null:new Value(current.getWildMagicValue()),
			current.getWildMagicTable()==null?null:Arrays.copyOf(current.getWildMagicTable(), current.getWildMagicTable().length));
		Database.getInstance().getSpells().put(newName, spell);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getSpells().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		Spell s = Database.getInstance().getSpell(name);

		s.setLevel((Integer)level.getValue());
		s.setHitPointCost(hitPointCost.getValue());
		s.setActionPointCost(actionPointCost.getValue());
		s.setMagicPointCost(magicPointCost.getValue());
		s.setDisplayName(displayName.getText());
		s.setBook(MagicSys.SpellBook.valueOf((String)book.getSelectedItem()));
		s.setSchool((String)school.getSelectedItem());
		s.setTargetType(targetType.getSelectedIndex());
		s.setUsabilityType(usabilityType.getSelectedIndex());
		s.setPrimaryModifier((String)primaryModifier.getSelectedItem());
		s.setSecondaryModifier((String)secondaryModifier.getSelectedItem());
		s.setRequirementsToLearn(requirementsToLearn.getModifier());
		String cbps = (String)castByPlayerScript.getSelectedItem();
		s.setCastByPlayerScript(cbps.equals(NONE)?null:Database.getInstance().getScript(cbps));
		String cbfs = (String)castByFoeScript.getSelectedItem();
		s.setCastByFoeScript(cbfs.equals(NONE)?null:Database.getInstance().getScript(cbfs));
		s.setDescription(description.getText());
		s.setEffects(effects.getGroupOfPossibilties());
		s.setRequirementsToCast(manaRequirements.getManaRequirements());

		if (wildMagic.isSelected())
		{
			s.setWildMagicValue(wildMagicValue.getValue());

			String[] table = new String[10];
			for (int i = 0; i < wildMagicTable.length; i++)
			{
				table[i] = (String)wildMagicTable[i].getSelectedItem();
			}
			s.setWildMagicTable(table);
		}
		else
		{
			s.setWildMagicValue(null);
			s.setWildMagicTable(null);
		}
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == wildMagic)
		{
			setWildMagicEnabled(wildMagic.isSelected());
		}

		super.actionPerformed(e);
	}

	/*-------------------------------------------------------------------------*/
	private void setWildMagicEnabled(boolean selected)
	{
		wildMagicValue.setEnabled(selected);
		for (int i = 0; i < wildMagicTable.length; i++)
		{
			wildMagicTable[i].setEnabled(selected);
		}
	}
}
