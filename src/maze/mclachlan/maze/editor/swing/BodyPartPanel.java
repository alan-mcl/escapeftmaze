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
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.BodyPart;
import mclachlan.maze.stat.EquipableSlot;
import mclachlan.maze.stat.StatModifier;

/**
 *
 */
public class BodyPartPanel extends EditorPanel
{
	private JTextField displayName;
	private JSpinner damagePrevention;
	private JSpinner damagePreventionChance;
	private StatModifierComponent modifiers;
	private JSpinner nrWeaponHardpoints;
	private JComboBox equipableSlotType;

	/*-------------------------------------------------------------------------*/
	public BodyPartPanel()
	{
		super(SwingEditor.Tab.BODY_PART);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getEditControls()
	{
		displayName = new JTextField(30);
		displayName.addActionListener(this);
		displayName.addKeyListener(this);
		damagePrevention = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		damagePrevention.addChangeListener(this);
		damagePreventionChance = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		damagePreventionChance.addChangeListener(this);
		modifiers = new StatModifierComponent(SwingEditor.Tab.BODY_PART);
		nrWeaponHardpoints = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		nrWeaponHardpoints.addChangeListener(this);
		equipableSlotType = new JComboBox(EquipableSlot.Type.values());
		equipableSlotType.addActionListener(this);

		JPanel editControls = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5,5,5,5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx++;
		editControls.add(new JLabel("Display Name:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(displayName, gbc);

		gbc.gridy++;
		gbc.gridx=1;
		gbc.weightx = 0.0;
		editControls.add(new JLabel("Damage Prevention:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(damagePrevention, gbc);

		gbc.gridy++;
		gbc.gridx=1;
		gbc.weightx = 0.0;
		editControls.add(new JLabel("Damage Prevention Chance:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(damagePreventionChance, gbc);

		gbc.gridy++;
		gbc.gridx=1;
		gbc.weightx = 0.0;
		editControls.add(new JLabel("Nr Weapon Hardpoints:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(nrWeaponHardpoints, gbc);

		gbc.gridy++;
		gbc.gridx=1;
		gbc.weightx = 0.0;
		editControls.add(new JLabel("Equipable Slot Type:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(equipableSlotType, gbc);

		gbc.gridy++;
		gbc.gridx=1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		editControls.add(new JLabel("Modifiers:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(modifiers, gbc);
		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getBodyParts().values());
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.BODY_PART);
		BodyPart bodyPart = new BodyPart(
			name,
			"",
			new StatModifier(),
			0,
			0,
			0,
			EquipableSlot.Type.NONE);
		Database.getInstance().getBodyParts().put(name, bodyPart);

		return bodyPart;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.BODY_PART);
		BodyPart current = Database.getInstance().getBodyPart((String)names.getSelectedValue());
		Database.getInstance().getBodyParts().remove(current.getName());
		current.setName(newName);
		Database.getInstance().getBodyParts().put(current.getName(), current);
		refreshNames(newName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.BODY_PART);

		BodyPart current = Database.getInstance().getBodyPart((String)names.getSelectedValue());

		BodyPart bodyPart = new BodyPart(
			newName,
			current.getDisplayName(),
			new StatModifier(current.getModifiers()),
			current.getDamagePrevention(),
			current.getDamagePreventionChance(),
			current.getNrWeaponHardpoints(),
			current.getEquipableSlotType());
		Database.getInstance().getBodyParts().put(newName, bodyPart);

		return bodyPart;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.BODY_PART);
		String name = (String)names.getSelectedValue();
		Database.getInstance().getBodyParts().remove(name);
		refreshNames(null);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		BodyPart bp = Database.getInstance().getBodyPart(name);

		bp.setDisplayName(displayName.getText());
		bp.setDamagePrevention((Integer)(damagePrevention.getValue()));
		bp.setDamagePreventionChance((Integer)(damagePreventionChance.getValue()));
		bp.setNrWeaponHardpoints((Integer)(nrWeaponHardpoints.getValue()));
		bp.setEquipableSlotType((EquipableSlot.Type)equipableSlotType.getSelectedItem());

		return bp;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		if (name == null)
		{
			return;
		}

		BodyPart bp = Database.getInstance().getBodyPart(name);

		damagePrevention.removeChangeListener(this);
		damagePreventionChance.removeChangeListener(this);
		nrWeaponHardpoints.removeChangeListener(this);
		equipableSlotType.removeActionListener(this);

		displayName.setText(bp.getDisplayName());
		damagePrevention.setValue(bp.getDamagePrevention());
		damagePreventionChance.setValue(bp.getDamagePreventionChance());
		modifiers.setModifier(bp.getModifiers());
		nrWeaponHardpoints.setValue(bp.getNrWeaponHardpoints());
		equipableSlotType.setSelectedItem(bp.getEquipableSlotType());

		damagePrevention.addChangeListener(this);
		damagePreventionChance.addChangeListener(this);
		nrWeaponHardpoints.addChangeListener(this);
		equipableSlotType.addActionListener(this);
	}
}
