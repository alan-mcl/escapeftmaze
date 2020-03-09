/*
 * Copyright (c) 2013 Alan McLachlan
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
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.data.v1.V1Utils;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.SpellEffect;

/**
 *
 */
public class NaturalWeaponsPanel extends EditorPanel
{
	private JComboBox attackScript, minRange, maxRange;
	private JTextField description;
	private StatModifierComponent modifiers;
	private JTextField damage, attacks;
	private JComboBox damageType, slaysFoeType;
	private JSpinner spellEffectLevel;
	private SpellEffectGroupOfPossibilitiesPanel spellEffects;
	private JButton makeBackstab;
	private JCheckBox isRanged;

	private static String[] ranges =
		{
			ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.MELEE),
			ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.EXTENDED),
			ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.THROWN),
			ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.LONG),
		};

	/*-------------------------------------------------------------------------*/
	public NaturalWeaponsPanel()
	{
		super(SwingEditor.Tab.NATURAL_WEAPONS);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		return getPhysicalAttackPanel();
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getPhysicalAttackPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = createGridBagConstraints();

		description = new JTextField(20);
		description.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Description:"), description, gbc);

		Vector<MagicSys.SpellEffectType> damageTypes = new Vector<MagicSys.SpellEffectType>();
		Collections.addAll(damageTypes, MagicSys.SpellEffectType.values());
		damageType = new JComboBox(damageTypes);
		damageType.addActionListener(this);

		damage = new JTextField(20);
		damage.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Damage:"), damage, gbc);

		dodgyGridBagShite(result, new JLabel("Damage Type:"), damageType, gbc);

		attacks = new JTextField(20);
		attacks.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Attacks:"), attacks, gbc);

		JPanel temp = new JPanel();
		modifiers = new StatModifierComponent(dirtyFlag);
		makeBackstab = new JButton("Make Backstab/Snipe");
		makeBackstab.addActionListener(this);
		temp.add(modifiers);
		temp.add(makeBackstab);
		dodgyGridBagShite(result, new JLabel("Modifiers:"), temp, gbc);

		isRanged = new JCheckBox("Ranged?");
		isRanged.addActionListener(this);
		dodgyGridBagShite(result, isRanged, new JLabel(), gbc);

		minRange = new JComboBox(ranges);
		minRange.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Min Range:"), minRange, gbc);

		maxRange = new JComboBox(ranges);
		maxRange.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Max Range:"), maxRange, gbc);

		Vector<String> types = new Vector<String>();
		types.addAll(new ArrayList<>(Database.getInstance().getCharacterClasses().keySet()));
		types.addAll(new ArrayList<>(Database.getInstance().getRaces().keySet()));
		types.addAll(Database.getInstance().getFoeTypes().keySet());
		Collections.sort(types);
		types.add(0, EditorPanel.NONE);

		slaysFoeType = new JComboBox<String>(types);
		slaysFoeType.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Slays Foe Type:"), slaysFoeType, gbc);

		attackScript = new JComboBox();
		attackScript.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Attack Script:"), attackScript, gbc);

		spellEffectLevel = new JSpinner(new SpinnerNumberModel(1, 1, 256, 1));
		spellEffectLevel.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Spell Effect Level:"), spellEffectLevel, gbc);

		spellEffects = new SpellEffectGroupOfPossibilitiesPanel(dirtyFlag, 0.66);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		result.add(spellEffects, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getNaturalWeapons().values());
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector scripts = new Vector(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(scripts);
		attackScript.setModel(new DefaultComboBoxModel(scripts));

		this.spellEffects.initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		NaturalWeapon nw = Database.getInstance().getNaturalWeapons().get(name);

		description.setText(nw.getDescription());

		damageType.removeActionListener(this);
		minRange.removeActionListener(this);
		maxRange.removeActionListener(this);
		attackScript.removeActionListener(this);
		spellEffectLevel.removeChangeListener(this);
		isRanged.removeActionListener(this);
		slaysFoeType.removeActionListener(this);

		damage.setText(V1Dice.toString(nw.getDamage()));
		damageType.setSelectedItem(nw.getDefaultDamageType());
		attacks.setText(V1Utils.toStringInts(nw.getAttacks(), ","));
		modifiers.setModifier(nw.getModifiers());
		isRanged.setSelected(nw.isRanged());
		minRange.setSelectedItem(ItemTemplate.WeaponRange.describe(nw.getMinRange()));
		maxRange.setSelectedItem(ItemTemplate.WeaponRange.describe(nw.getMaxRange()));
		slaysFoeType.setSelectedItem(nw.getSlaysFoeType() == null ? NONE : nw.getSlaysFoeType().getName());
		attackScript.setSelectedItem(nw.getAttackScript().getName());
		spellEffectLevel.setValue(nw.getSpellEffectLevel());
		spellEffects.refresh(nw.getSpellEffects());

		damageType.addActionListener(this);
		minRange.addActionListener(this);
		maxRange.addActionListener(this);
		attackScript.addActionListener(this);
		spellEffectLevel.addChangeListener(this);
		isRanged.addActionListener(this);
		slaysFoeType.removeActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		// hackish way to set a default script, because I keep forgetting this 
		MazeScript script =
			Database.getInstance().getMazeScripts().get("generic weapon swish");

		if (script == null)
		{
			script =
				Database.getInstance().getMazeScript((String)attackScript.getItemAt(0));
		}

		NaturalWeapon nw = new NaturalWeapon(
			name,
			"",
			false,
			Dice.d1,
			MagicSys.SpellEffectType.NONE,
			new StatModifier(),
			ItemTemplate.WeaponRange.MELEE,
			ItemTemplate.WeaponRange.MELEE,
			new GroupOfPossibilities<SpellEffect>(),
			1,
			new int[]{1},
			null,
			script);

		Database.getInstance().getNaturalWeapons().put(name, nw);

		return nw;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		NaturalWeapon nw = Database.getInstance().getNaturalWeapons().remove(currentName);
		nw.setName(newName);
		Database.getInstance().getNaturalWeapons().put(newName, nw);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		NaturalWeapon current = Database.getInstance().getNaturalWeapons().get(currentName);

		NaturalWeapon nw;

		nw = new NaturalWeapon(
			newName,
			current.getDescription(),
			current.isRanged(),
			current.getDamage(),
			current.getDefaultDamageType(),
			new StatModifier(current.getModifiers()),
			current.getMinRange(),
			current.getMaxRange(),
			new GroupOfPossibilities<SpellEffect>(current.getSpellEffects()),
			current.getSpellEffectLevel(),
			current.getAttacks(),
			current.getSlaysFoeType(),
			current.getAttackScript());

		Database.getInstance().getNaturalWeapons().put(newName, nw);

		return nw;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getNaturalWeapons().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		NaturalWeapon nw = Database.getInstance().getNaturalWeapons().get(name);

		nw.setDescription(description.getText());

		nw.setDamage(V1Dice.fromString(damage.getText()));
		nw.setDamageType((MagicSys.SpellEffectType)damageType.getSelectedItem());
		nw.setAttacks(V1Utils.fromStringInts(attacks.getText(), ","));
		nw.setModifiers(modifiers.getModifier());
		nw.setRanged(isRanged.isSelected());
		nw.setMinRange(ItemTemplate.WeaponRange.valueOf((String)minRange.getSelectedItem()));
		nw.setMaxRange(ItemTemplate.WeaponRange.valueOf((String)maxRange.getSelectedItem()));
		nw.setSlaysFoeType(slaysFoeType.getSelectedItem()==NONE?
			null:new TypeDescriptorImpl(slaysFoeType.getName()));
		nw.setAttackScript(Database.getInstance().getMazeScript((String)attackScript.getSelectedItem()));
		nw.setSpellEffectLevel((Integer)spellEffectLevel.getValue());
		nw.setSpellEffects(spellEffects.getGroupOfPossibilties());

		return nw;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == makeBackstab)
		{
			StatModifier sm = modifiers.getModifier();
			sm.setModifier(Stats.Modifier.DAMAGE_MULTIPLIER, 1);
			modifiers.setModifier(sm);
		}

		super.actionPerformed(e);
	}
}
