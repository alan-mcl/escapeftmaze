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
import java.util.Vector;
import java.util.Collections;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.combat.WieldingCombo;
import mclachlan.maze.stat.StatModifier;
import javax.swing.*;

/**
 *
 */
public class WieldingComboPanel extends EditorPanel
{
	private JComboBox primaryHand, secondaryHand;
	private StatModifierComponent modifiers;

	/*-------------------------------------------------------------------------*/
	public WieldingComboPanel()
	{
		super(SwingEditor.Tab.WIELDING_COMBOS);
	}

	/*-------------------------------------------------------------------------*/
	public Container getEditControls()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		primaryHand = new JComboBox();
		primaryHand.addActionListener(this);
		secondaryHand = new JComboBox();
		secondaryHand.addActionListener(this);
		modifiers = new StatModifierComponent(dirtyFlag);

		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel("Primary Hand: "), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(primaryHand, gbc);

		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel("Secondary Hand: "), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(secondaryHand, gbc);

		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel("Modifiers: "), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(modifiers, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> items = new Vector<String>(Database.getInstance().getItemList());
		Collections.sort(items);

		Vector<String> vec = new Vector<String>();
		vec.add(WieldingCombo.Key.NONE);
		vec.add(WieldingCombo.Key.ANYTHING);
		vec.add(WieldingCombo.Key.SHORT_WEAPON);
		vec.add(WieldingCombo.Key.EXTENDED_WEAPON);
		vec.add(WieldingCombo.Key.THROWN_WEAPON);
		vec.add(WieldingCombo.Key.RANGED_WEAPON);
		vec.add(WieldingCombo.Key.AMMUNITION);
		vec.add(WieldingCombo.Key.SHIELD);
		vec.add(WieldingCombo.Key.SWORD);
		vec.add(WieldingCombo.Key.AXE);
		vec.add(WieldingCombo.Key.POLEARM);
		vec.add(WieldingCombo.Key.MACE);
		vec.add(WieldingCombo.Key.DAGGER);
		vec.add(WieldingCombo.Key.STAFF);
		vec.add(WieldingCombo.Key.WAND);
		vec.add(WieldingCombo.Key.MODERN);
		vec.add(WieldingCombo.Key.BOW);

		vec.addAll(items);

		primaryHand.setModel(new DefaultComboBoxModel(vec));
		secondaryHand.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getWieldingCombos().values());
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		WieldingCombo wc = Database.getInstance().getWieldingCombo(name);

		primaryHand.removeActionListener(this);
		secondaryHand.removeActionListener(this);

		primaryHand.setSelectedItem(wc.getPrimaryHand());
		secondaryHand.setSelectedItem(wc.getSecondaryHand());
		modifiers.setModifier(wc.getModifiers());

		primaryHand.addActionListener(this);
		secondaryHand.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		WieldingCombo wc = new WieldingCombo(
			name,
			WieldingCombo.Key.NONE,
			WieldingCombo.Key.NONE,
			new StatModifier());
		Database.getInstance().getWieldingCombos().put(name, wc);

		return wc;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		WieldingCombo wc = Database.getInstance().getWieldingCombos().remove(currentName);
		wc.setName(newName);
		Database.getInstance().getWieldingCombos().put(newName, wc);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		WieldingCombo current = Database.getInstance().getWieldingCombo(currentName);
		WieldingCombo wc = new WieldingCombo(
			newName,
			current.getPrimaryHand(),
			current.getSecondaryHand(), 
			new StatModifier(current.getModifiers()));
		Database.getInstance().getWieldingCombos().put(newName, wc);

		return wc;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getWieldingCombos().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		WieldingCombo wc = Database.getInstance().getWieldingCombo(name);
		wc.setPrimaryHand((String)primaryHand.getSelectedItem());
		wc.setSecondaryHand((String)secondaryHand.getSelectedItem());
		wc.setModifiers(modifiers.getModifier());

		return wc;
	}
}
