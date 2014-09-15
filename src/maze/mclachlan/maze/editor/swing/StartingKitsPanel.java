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

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.*;

/**
 *
 */
public class StartingKitsPanel extends EditorPanel
{
	private JTextField displayName;
	private JComboBox primaryWeapon;
	private JComboBox secondaryWeapon;
	private JComboBox helm;
	private JComboBox torsoArmour;
	private JComboBox legArmour;
	private JComboBox gloves;
	private JComboBox boots;
	private JComboBox miscItem1;
	private JComboBox miscItem2;
	private JComboBox bannerItem;
	private JComboBox[] packItems;
	private JTextArea description;
	private StatModifierComponent combatModifiers;
	private StatModifierComponent stealthModifiers;
	private StatModifierComponent magicModifiers;
	private CharacterClassSelection classes;

	/*-------------------------------------------------------------------------*/
	public StartingKitsPanel()
	{
		super(SwingEditor.Tab.STARTING_KITS);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel result = new JPanel();

		displayName = new JTextField(20);
		displayName.addKeyListener(this);
		description = new JTextArea(7,40);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.addKeyListener(this);
		combatModifiers = new StatModifierComponent(dirtyFlag);
		stealthModifiers = new StatModifierComponent(dirtyFlag);
		magicModifiers = new StatModifierComponent(dirtyFlag);

		List<String> itemList = Database.getInstance().getItemList();
		Collections.sort(itemList);
		itemList.add(0, EditorPanel.NONE);

		primaryWeapon = getComboBox();
		secondaryWeapon = getComboBox();
		helm = getComboBox();
		torsoArmour = getComboBox();
		legArmour = getComboBox();
		gloves = getComboBox();
		boots = getComboBox();
		miscItem1 = getComboBox();
		miscItem2 = getComboBox();
		bannerItem = getComboBox();
		packItems = new JComboBox[PlayerCharacter.MAX_PACK_ITEMS];
		for (int i = 0; i < packItems.length; i++)
		{
			packItems[i] = getComboBox();
		}

		JPanel topPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		dodgyGridBagShite(topPanel, new JLabel("Display Name:"), displayName, gbc);
		dodgyGridBagShite(topPanel, new JLabel("Combat Modifiers:"), combatModifiers, gbc);
		dodgyGridBagShite(topPanel, new JLabel("Stealth Modifiers:"), stealthModifiers, gbc);
		dodgyGridBagShite(topPanel, new JLabel("Magic Modifiers:"), magicModifiers, gbc);
		dodgyGridBagShite(topPanel, new JLabel("Description:"), new JScrollPane(description), gbc);

		JPanel panel = new JPanel(new GridBagLayout());
		gbc = createGridBagConstraints();

		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		panel.add(new JLabel("Primary Weapon:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Secondary Weapon:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Helm:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Torso Armour:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Leg Armour:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Gloves:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Boots:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Misc Item #1:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Misc Item #2:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Banner Item:", JLabel.RIGHT), gbc);

		gbc.gridy++;
		panel.add(new JLabel("Pack 0:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Pack 1:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Pack 2:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Pack 3:", JLabel.RIGHT), gbc);
		gbc.gridy++;
		panel.add(new JLabel("Pack 4:", JLabel.RIGHT), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		panel.add(primaryWeapon, gbc);
		gbc.gridy++;
		panel.add(secondaryWeapon, gbc);
		gbc.gridy++;
		panel.add(helm, gbc);
		gbc.gridy++;
		panel.add(torsoArmour, gbc);
		gbc.gridy++;
		panel.add(legArmour, gbc);
		gbc.gridy++;
		panel.add(gloves, gbc);
		gbc.gridy++;
		panel.add(boots, gbc);
		gbc.gridy++;
		panel.add(miscItem1, gbc);
		gbc.gridy++;
		panel.add(miscItem2, gbc);
		gbc.gridy++;
		panel.add(bannerItem, gbc);
		int firstColPackItems = 5;
		for (int i=0; i< firstColPackItems; i++)
		{
			gbc.gridy++;
			panel.add(packItems[i], gbc);
		}

		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		for (int i= firstColPackItems; i<PlayerCharacter.MAX_PACK_ITEMS; i++)
		{
			panel.add(new JLabel("Pack "+i+":", JLabel.RIGHT), gbc);
			gbc.gridy++;
		}

		gbc.gridx = 4;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		for (int i= firstColPackItems; i<PlayerCharacter.MAX_PACK_ITEMS; i++)
		{
			panel.add(packItems[i], gbc);
			gbc.gridy++;
		}

		classes = new CharacterClassSelection(dirtyFlag);

		result.setLayout(new BorderLayout(3, 3));
		JPanel temp = new JPanel(new FlowLayout(FlowLayout.LEFT));
		temp.add(topPanel);
		result.add(temp, BorderLayout.NORTH);
		result.add(classes, BorderLayout.CENTER);
		result.add(panel, BorderLayout.WEST);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JComboBox getComboBox()
	{
		JComboBox box = new JComboBox();
		box.addActionListener(this);
		return box;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector vec = new Vector(Database.getInstance().getStartingKits().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector items = new Vector(Database.getInstance().getItemList());
		Collections.sort(items);
		items.add(0, NONE);
		
		primaryWeapon.setModel(new DefaultComboBoxModel(items));
		secondaryWeapon.setModel(new DefaultComboBoxModel(items));
		helm.setModel(new DefaultComboBoxModel(items));
		gloves.setModel(new DefaultComboBoxModel(items));
		boots.setModel(new DefaultComboBoxModel(items));
		torsoArmour.setModel(new DefaultComboBoxModel(items));
		legArmour.setModel(new DefaultComboBoxModel(items));
		miscItem1.setModel(new DefaultComboBoxModel(items));
		miscItem2.setModel(new DefaultComboBoxModel(items));
		bannerItem.setModel(new DefaultComboBoxModel(items));
		for (int i = 0; i < packItems.length; i++)
		{
			packItems[i].setModel(new DefaultComboBoxModel(items));
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		StartingKit si = Database.getInstance().getStartingKits().get(name);

		displayName.removeKeyListener(this);
		description.removeKeyListener(this);
		primaryWeapon.removeActionListener(this);
		secondaryWeapon.removeActionListener(this);
		helm.removeActionListener(this);
		gloves.removeActionListener(this);
		boots.removeActionListener(this);
		torsoArmour.removeActionListener(this);
		legArmour.removeActionListener(this);
		miscItem1.removeActionListener(this);
		miscItem2.removeActionListener(this);
		bannerItem.removeActionListener(this);
		for (int i = 0; i < packItems.length; i++)
		{
			packItems[i].removeActionListener(this);
		}

		displayName.setText(si.getDisplayName());
		description.setText(si.getDescription());
		combatModifiers.setModifier(si.getCombatModifiers());
		stealthModifiers.setModifier(si.getStealthModifiers());
		magicModifiers.setModifier(si.getMagicModifiers());

		primaryWeapon.setSelectedItem(getSelectItem(si.getPrimaryWeapon()));
		secondaryWeapon.setSelectedItem(getSelectItem(si.getSecondaryWeapon()));
		helm.setSelectedItem(getSelectItem(si.getHelm()));
		String selectItem = getSelectItem(si.getGloves());
		gloves.setSelectedItem(selectItem);
		boots.setSelectedItem(getSelectItem(si.getBoots()));
		torsoArmour.setSelectedItem(getSelectItem(si.getTorsoArmour()));
		legArmour.setSelectedItem(getSelectItem(si.getLegArmour()));
		miscItem1.setSelectedItem(getSelectItem(si.getMiscItem1()));
		miscItem2.setSelectedItem(getSelectItem(si.getMiscItem2()));
		bannerItem.setSelectedItem(getSelectItem(si.getBannerItem()));
		for (int i = 0; i < packItems.length; i++)
		{
			packItems[i].setSelectedItem(NONE);
		}
		List<String> pi = si.getPackItems();
		for (int i = 0; i < pi.size(); i++)
		{
			packItems[i].setSelectedItem(getSelectItem(pi.get(i)));
		}
		classes.refresh(si.getUsableByCharacterClass());
		
		displayName.addKeyListener(this);
		description.addKeyListener(this);
		primaryWeapon.addActionListener(this);
		secondaryWeapon.addActionListener(this);
		helm.addActionListener(this);
		gloves.addActionListener(this);
		boots.addActionListener(this);
		torsoArmour.addActionListener(this);
		legArmour.addActionListener(this);
		miscItem1.addActionListener(this);
		miscItem2.addActionListener(this);
		bannerItem.addActionListener(this);
		for (int i = 0; i < packItems.length; i++)
		{
			packItems[i].addActionListener(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	private String getSelectItem(String selectItem)
	{
		return (selectItem == null || "".equals(selectItem)) ? NONE : selectItem;
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		StartingKit si = new StartingKit(
			name,
			name,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			new ArrayList<String>(),
			null,
			new StatModifier(),
			new StatModifier(),
			new StatModifier(),
			new HashSet<String>());

		Database.getInstance().getStartingKits().put(name, si);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		StartingKit si = Database.getInstance().getStartingKits().remove(currentName);
		si.setName(newName);
		Database.getInstance().getStartingKits().put(newName, si);

		// FK updates
		for (Race r : Database.getInstance().getRaces().values())
		{
			if (r.getStartingItems() != null)
			{
				for (StartingKit s : r.getStartingItems())
				{
					if (s.getName().equals(currentName))
					{
						s.setName(newName);
						SwingEditor.instance.setDirty(SwingEditor.Tab.RACES);
					}
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		StartingKit current = Database.getInstance().getStartingKits().get(currentName);

		StartingKit sk;

		sk = new StartingKit(
			newName,
			current.getDisplayName(),
			current.getPrimaryWeapon(),
			current.getSecondaryWeapon(),
			current.getHelm(),
			current.getTorsoArmour(),
			current.getLegArmour(),
			current.getGloves(),
			current.getBoots(),
			current.getMiscItem1(),
			current.getMiscItem2(),
			current.getBannerItem(),
			current.getPackItems(),
			current.getDescription(),
			current.getCombatModifiers(),
			current.getStealthModifiers(),
			current.getMagicModifiers(),
			current.getUsableByCharacterClass());

		Database.getInstance().getStartingKits().put(newName, sk);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getStartingKits().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		StartingKit si = Database.getInstance().getStartingKits().get(name);

		si.setDisplayName(displayName.getText());
		si.setDescription(description.getText());
		si.setCombatModifiers(combatModifiers.getModifier());
		si.setStealthModifiers(stealthModifiers.getModifier());
		si.setMagicModifiers(magicModifiers.getModifier());

		si.setPrimaryWeapon(getSelected(primaryWeapon));
		si.setSecondaryWeapon(getSelected(secondaryWeapon));
		si.setHelm(getSelected(helm));
		si.setGloves(getSelected(gloves));
		si.setBoots(getSelected(boots));
		si.setTorsoArmour(getSelected(torsoArmour));
		si.setLegArmour(getSelected(legArmour));
		si.setMiscItem1(getSelected(miscItem1));
		si.setMiscItem2(getSelected(miscItem2));
		si.setBannerItem(getSelected(bannerItem));
		si.setUsableByCharacterClass(classes.getAllowedCharacterClasses());

		List<String> packItemsList = new ArrayList<String>();
		for (int i = 0; i < packItems.length; i++)
		{
			if (packItems[i].getSelectedItem() != NONE)
			{
				packItemsList.add(getSelected(packItems[i]));
			}
		}
		si.setPackItems(packItemsList);
	}

	/*-------------------------------------------------------------------------*/
	private String getSelected(JComboBox x)
	{
		return (String)(x.getSelectedItem()==NONE?null:x.getSelectedItem());
	}
}
