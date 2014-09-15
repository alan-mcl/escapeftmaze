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
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Value;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SpellEffectPanel extends EditorPanel
{
	private JCheckBox custom;
	private JTextField customImpl, displayName;
	private JComboBox type, subType, targetType;
	private ValueComponent saveAdjustment;
	private SpellResultComponent unsavedResult, savedResult;
	private JComboBox application;
	private JButton halfDamage;

	/*-------------------------------------------------------------------------*/
	public SpellEffectPanel()
	{
		super(SwingEditor.Tab.SPELL_EFFECTS);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		Vector<String> validTypes = new Vector<String>();
		for (int i=0; i<MagicSys.SpellEffectType.MAX; i++)
		{
			validTypes.addElement(MagicSys.SpellEffectType.describe(i));
		}

		Vector<String> validTargetTypes = new Vector<String>();
		for (int i=0; i<MagicSys.SpellTargetType.MAX; i++)
		{
			validTargetTypes.addElement(MagicSys.SpellTargetType.describe(i));
		}

		Vector<String> validSubTypes = new Vector<String>();
		validSubTypes.add(MagicSys.SpellEffectSubType.NONE.toString());
		validSubTypes.add(MagicSys.SpellEffectSubType.NORMAL_DAMAGE.toString());
		validSubTypes.add(MagicSys.SpellEffectSubType.HEAT.toString());
		validSubTypes.add(MagicSys.SpellEffectSubType.COLD.toString());
		validSubTypes.add(MagicSys.SpellEffectSubType.POISON.toString());
		validSubTypes.add(MagicSys.SpellEffectSubType.ACID.toString());
		validSubTypes.add(MagicSys.SpellEffectSubType.LIGHTNING.toString());
		validSubTypes.add(MagicSys.SpellEffectSubType.PSYCHIC.toString());

		custom = new JCheckBox("Custom?");
		custom.addActionListener(this);
		customImpl = new JTextField(20);
		customImpl.addActionListener(this);

		displayName = new JTextField(25);
		displayName.addKeyListener(this);
		type = new JComboBox(validTypes);
		type.addActionListener(this);
		subType = new JComboBox(validSubTypes);
		subType.addActionListener(this);
		targetType = new JComboBox(validTargetTypes);
		targetType.addActionListener(this);

		Vector<SpellEffect.Application> applications = new Vector<SpellEffect.Application>();
		Collections.addAll(applications, SpellEffect.Application.values());
		application = new JComboBox(applications);
		application.addActionListener(this);

		saveAdjustment = new ValueComponent(dirtyFlag);
		savedResult = new SpellResultComponent(dirtyFlag);
		unsavedResult = new SpellResultComponent(dirtyFlag);
		
		halfDamage = new JButton("(set to half damage)");
		halfDamage.addActionListener(this);

		JPanel editControls = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		editControls.add(custom, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(customImpl, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		editControls.add(new JLabel("Display Name:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(displayName, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		editControls.add(new JLabel("Type:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(type, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		editControls.add(new JLabel("SubType:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(subType, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		editControls.add(new JLabel("Application:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(application, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		editControls.add(new JLabel("Save Adjustment:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(saveAdjustment, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		editControls.add(new JLabel("Unsaved Spell Result:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(unsavedResult, gbc);

		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		editControls.add(new JLabel("Saved Spell Result:"), gbc);
		gbc.weightx = 0.0;
		gbc.gridx++;
		editControls.add(savedResult, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(halfDamage, gbc);
		
		gbc.gridy++;
		gbc.gridx=0;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		editControls.add(new JLabel("Target Type:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		editControls.add(targetType, gbc);

		return editControls;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getSpellEffects().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		if (name == null)
		{
			return;
		}
		
		SpellEffect se = Database.getInstance().getSpellEffect(name);

		displayName.removeKeyListener(this);
		type.removeActionListener(this);
		subType.removeActionListener(this);
		targetType.removeActionListener(this);
		application.removeActionListener(this);

		if (se.getClass() != SpellEffect.class)
		{
			custom.setSelected(true);
			checkCustom();
			customImpl.setText(se.getClass().getName());
		}
		else
		{
			custom.setSelected(false);
			checkCustom();
			customImpl.setText("");

			if (se.getDisplayName() != null)
			{
				displayName.setText(se.getDisplayName());
			}
			else
			{
				displayName.setText("");
			}
			type.setSelectedIndex(se.getType());
			subType.setSelectedItem(se.getSubType().toString());
			targetType.setSelectedIndex(se.getTargetType());
			saveAdjustment.setValue(se.getSaveAdjustment());
			this.unsavedResult.setResult(se.getUnsavedResult());
			this.savedResult.setResult(se.getSavedResult());
			application.setSelectedItem(se.getApplication());
		}

		application.addActionListener(this);
		displayName.addKeyListener(this);
		type.addActionListener(this);
		subType.addActionListener(this);
		targetType.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		SpellEffect se = new SpellEffect(
			name,
			name,
			0,
			MagicSys.SpellEffectSubType.NONE, SpellEffect.Application.AS_PER_SPELL,
			null,
			null,
			null,
			0);

		Database.getInstance().getSpellEffects().put(name, se);
		refreshNames(name);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		SpellEffect se = Database.getInstance().getSpellEffect(currentName);
		Database.getInstance().getSpellEffects().remove(currentName);
		se.setName(newName);
		Database.getInstance().getSpellEffects().put(newName, se);
		refreshNames(newName);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		SpellEffect current = Database.getInstance().getSpellEffect(currentName);

		SpellEffect se = new SpellEffect(
			newName,
			current.getDisplayName(),
			current.getType(),
			current.getSubType(), current.getApplication(),
			current.getSaveAdjustment(),
			current.getUnsavedResult(),
			current.getSavedResult(),
			current.getTargetType());

		Database.getInstance().getSpellEffects().put(newName, se);
		refreshNames(newName);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getSpellEffects().remove(currentName);
		refreshNames(null);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		SpellEffect se = Database.getInstance().getSpellEffect(name);

		if (custom.isSelected())
		{
			try
			{
				Class c = Class.forName(customImpl.getText());
				se = (SpellEffect)c.newInstance();
				Database.getInstance().getSpellEffects().put(name, se);
			}
			catch (Exception e)
			{
				throw new MazeException(e);
			}
		}
		else
		{
			String dn = displayName.getText();
			se.setDisplayName("".equals(dn)?null:dn);
			se.setSaveAdjustment(saveAdjustment.getValue());
			se.setTargetType(targetType.getSelectedIndex());
			se.setType(type.getSelectedIndex());
			se.setSubType(MagicSys.SpellEffectSubType.valueOf(subType.getSelectedItem().toString()));
			se.setSavedResult(savedResult.getSpellResult());
			se.setUnsavedResult(unsavedResult.getSpellResult());
			se.setApplication((SpellEffect.Application)application.getSelectedItem());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void checkCustom()
	{
		setEnabledAllEditControls(!custom.isSelected());
		custom.setEnabled(true);
		customImpl.setEnabled(custom.isSelected());
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == halfDamage)
		{
			SpellResult sr = unsavedResult.getSpellResult();
			if (sr instanceof DamageSpellResult)
			{
				DamageSpellResult d = (DamageSpellResult)sr;
				Value hpDamage = d.getHitPointDamage();
				Value fatigueDamage = d.getFatigueDamage();
				Value actionDamage = d.getActionPointDamage();
				Value magicDamage = d.getMagicPointDamage();

				// cheap shitty way of cloning
				double multiplier = d.getMultiplier();

				DamageSpellResult result = new DamageSpellResult(
					V1Value.fromString(V1Value.toString(hpDamage)), 
					V1Value.fromString(V1Value.toString(fatigueDamage)),
					V1Value.fromString(V1Value.toString(actionDamage)),
					V1Value.fromString(V1Value.toString(magicDamage)),
					multiplier==0?0.5:multiplier/2,
					d.transferToCaster());

				result.setFoeType(d.getFoeType());

				this.savedResult.refresh(result);
			}
			else if (sr instanceof DamageFoeTypeSpellResult)
			{
				DamageFoeTypeSpellResult d = (DamageFoeTypeSpellResult)sr;
				Value damage = d.getDamage();

				// cheap shitty way of cloning
				String s = V1Value.toString(damage);
				Value cloneValue = V1Value.fromString(s);
				double multiplier = d.getMultiplier();

				DamageFoeTypeSpellResult result = new DamageFoeTypeSpellResult(
					cloneValue,
					multiplier==0?0.5:multiplier/2,
					d.getType());
				this.savedResult.refresh(result);
			}
			else
			{
				JOptionPane.showMessageDialog(this, "Unsaved spell result is not " +
					"one of [Damage, DamageFoeType]");
			}
		}
		else if (e.getSource() == custom)
		{
			checkCustom();
		}
		
		super.actionPerformed(e);
	}
}
