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
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.magic.MagicSys;

/**
 *
 */
public class AttackTypePanel extends EditorPanel
{
	private JTextField verb;
	private JComboBox damageType, attackModifier;
	private StatModifierComponent modifiers;

	/*-------------------------------------------------------------------------*/
	public AttackTypePanel()
	{
		super(SwingEditor.Tab.ATTACK_TYPES);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getEditControls()
	{
		verb = new JTextField(30);
		verb.addActionListener(this);
		verb.addKeyListener(this);

		Vector<MagicSys.SpellEffectType> damageTypes = new Vector<MagicSys.SpellEffectType>();
		Collections.addAll(damageTypes, MagicSys.SpellEffectType.values());
		damageType = new JComboBox(damageTypes);
		damageType.addActionListener(this);

		Vector vec = new Vector();
		vec.addAll(Arrays.asList(Stats.Modifier.values()));
		Collections.sort(vec);
		attackModifier = new JComboBox(vec);
		attackModifier.addActionListener(this);

		modifiers = new StatModifierComponent(SwingEditor.Tab.ATTACK_TYPES);

		JPanel editControls = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = createGridBagConstraints();

		dodgyGridBagShite(editControls, new JLabel("Verb:"), verb, gbc);

		dodgyGridBagShite(editControls, new JLabel("Damage Type:"), damageType, gbc);

		dodgyGridBagShite(editControls, new JLabel("Attack Modifier:"), attackModifier, gbc);

		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		editControls.add(new JLabel("Modifiers:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(modifiers, gbc);

		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>((Database.getInstance().getAttackTypes().values()));
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.ATTACK_TYPES);
		AttackType attackType = new AttackType(
			name,
			"",
			Stats.Modifier.NONE,
			MagicSys.SpellEffectType.NONE,
			new StatModifier());
		Database.getInstance().getAttackTypes().put(name, attackType);

		return attackType;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.ATTACK_TYPES);
		AttackType current = Database.getInstance().getAttackType((String)names.getSelectedValue());
		Database.getInstance().getAttackTypes().remove(current.getName());
		current.setName(newName);
		Database.getInstance().getAttackTypes().put(current.getName(), current);
		refreshNames(newName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.ATTACK_TYPES);

		AttackType current = Database.getInstance().getAttackType((String)names.getSelectedValue());

		AttackType attackType = new AttackType(
			newName,
			current.getVerb(),
			current.getAttackModifier(),
			current.getDamageType(),
			new StatModifier(current.getModifiers()));
		Database.getInstance().getAttackTypes().put(newName, attackType);

		return attackType;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		SwingEditor.instance.setDirty(SwingEditor.Tab.ATTACK_TYPES);
		String name = (String)names.getSelectedValue();
		Database.getInstance().getAttackTypes().remove(name);
		refreshNames(null);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		if (name == null)
		{
			return;
		}

		AttackType at = Database.getInstance().getAttackType(name);
		
		verb.removeActionListener(this);
		verb.removeKeyListener(this);
		damageType.removeActionListener(this);
		attackModifier.removeActionListener(this);

		verb.setText(at.getVerb());
		damageType.setSelectedItem(at.getDamageType());
		modifiers.setModifier(at.getModifiers());
		attackModifier.setSelectedItem(at.getAttackModifier());
		
		verb.addActionListener(this);
		verb.addKeyListener(this);
		damageType.addActionListener(this);
		attackModifier.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		AttackType attackType = Database.getInstance().getAttackType(name);
		attackType.setVerb(verb.getText());
		attackType.setModifiers(modifiers.getModifier());
		attackType.setDamageType((MagicSys.SpellEffectType)damageType.getSelectedItem());

		return attackType;
	}
}
