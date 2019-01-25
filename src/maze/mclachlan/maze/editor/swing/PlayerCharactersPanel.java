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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.condition.ConditionManager;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellBook;

/**
 *
 */
public abstract class PlayerCharactersPanel extends JPanel implements ListSelectionListener, ChangeListener, ActionListener
{
	public static final int MAX_ITEMS = 32;

	// names
	private JList names;
	private String currentName;
	private Container editControls;
	private int dirtyFlag;
	
	// general tab
	private CharacterLevelsTablePanel levels;
	private JSpinner experience, kills, spellPicks;
	private JComboBox gender, race, characterClass, personality;
	private JTextField portrait;
	private CurMaxComponent hitPoints, actionPoints, magicPoints;
	private StatModifierComponent stats, practice, activeModifiers;
	
	// items tab
	private ItemComponent[] items;
	
	// spells tab
	private SpellListPanel spellList;

	// conditions tab
	private ConditionListWidget conditionList;

	/*-------------------------------------------------------------------------*/
	public PlayerCharactersPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		names = new JList();

		refreshNames(null);
		names.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		names.addListSelectionListener(this);
		names.setFixedCellWidth(100);
		JScrollPane nameScroller = new JScrollPane(names);

		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		editControls = getEditControls();
		JScrollPane editControlsScroller = new JScrollPane(editControls);

		JSplitPane splitPane = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT,
			true,
			nameScroller,
			editControlsScroller);

		add(splitPane, gbc);

		initForeignKeys();
		if (currentName != null)
		{
			refresh(currentName);
		}

		splitPane.setDividerLocation(-1);
	}

	/*-------------------------------------------------------------------------*/
	public void refreshNames(String toBeSelected)
	{
		currentName = null;
		Vector vec = getCharacterNames();
		names.setListData(vec);
		if (toBeSelected == null)
		{
			names.setSelectedIndex(0);
		}
		else
		{
			names.setSelectedValue(toBeSelected, true);
		}
		currentName = (String)names.getSelectedValue();
	}

	/*-------------------------------------------------------------------------*/
	public abstract Vector getCharacterNames();

	/*-------------------------------------------------------------------------*/
	public Container getEditControls()
	{
		JTabbedPane result = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		
		result.addTab("General", getGeneralTab());
		result.addTab("Items", getItemsTab());
		result.addTab("Spells", getSpellsTab());
		result.addTab("Conditions", getConditionsTab());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getConditionsTab()
	{
		JPanel result = new JPanel();

		result.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		conditionList = new ConditionListWidget(dirtyFlag);
		result.add(conditionList, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getSpellsTab()
	{
		JPanel result = new JPanel();

		result.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		
		spellList = new SpellListPanel(dirtyFlag);
		result.add(spellList, gbc);		

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getItemsTab()
	{
		JPanel result = new JPanel();
		
		result.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.WEST;

		items = new ItemComponent[MAX_ITEMS];
		String[] labels = new String[MAX_ITEMS];
		
		labels[0] = "Primary Weapon:";
		labels[1] = "Secondary Weapon:";
		labels[2] = "Alt Primary Weapon:";
		labels[3] = "Alt Sec Weapon:";
		labels[4] = "Helm:";
		labels[5] = "Torso Armour:";
		labels[6] = "Leg Armour:";
		labels[7] = "Gloves:";
		labels[8] = "Boots:";
		labels[9] = "Banner Item:";
		labels[10] = "Misc Item #1:";
		labels[11] = "Misc Item #2:";

		for (int i=12; i<MAX_ITEMS; i++)
		{
			labels[i] = "Inventory "+(i-12)+":";
		}
		
		dodgyGridBagShite(
			result, 
			new JLabel("Slot"), 
			new JLabel("Item Template / Cursed State / Identified ? / Stack Current / Charges Current / Charges Max"),
			gbc);
		
		for (int i=0; i<MAX_ITEMS; i++)
		{
			items[i] = new ItemComponent(dirtyFlag);
			dodgyGridBagShite(result, new JLabel(labels[i]), items[i], gbc);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Container getGeneralTab()
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

		result.add(getLeftPanel(), gbc);

		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(getRightPanel(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	JPanel getLeftPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		
		experience = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		experience.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Experience:"), experience, gbc);
		
		kills = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		kills.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Kills:"), kills, gbc);
		
		gender = new JComboBox();
		gender.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Gender:"), gender, gbc);
		
		race = new JComboBox();
		race.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Race:"), race, gbc);
		
		characterClass = new JComboBox();
		characterClass.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Character Class:"), characterClass, gbc);

		personality = new JComboBox();
		personality.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Personality:"), personality, gbc);
		
		portrait = new JTextField(20);
		portrait.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Portrait:"), portrait, gbc);
		
		hitPoints = new CurMaxComponent(dirtyFlag, -9999, 9999);
		dodgyGridBagShite(result, new JLabel("Hit Points:"), hitPoints, gbc);
		
		actionPoints = new CurMaxComponent(dirtyFlag, -9999, 9999);
		dodgyGridBagShite(result, new JLabel("Action Points:"), actionPoints, gbc);
		
		magicPoints = new CurMaxComponent(dirtyFlag, -9999, 9999);
		dodgyGridBagShite(result, new JLabel("Magic Points:"), magicPoints, gbc);
		
		stats = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Stats:"), stats, gbc);
		
		practice = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Practice:"), practice, gbc);
		
		activeModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Active Modifiers:"), activeModifiers, gbc);
		
		spellPicks = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		spellPicks.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Spell Picks:"), spellPicks, gbc);
		
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	JPanel getRightPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		
		levels = new CharacterLevelsTablePanel("Class Levels", dirtyFlag, 0.5, 0.5);
		result.add(levels, gbc);
		
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> v1 = new Vector<String>(Database.getInstance().getGenderList());
		Collections.sort(v1);
		gender.setModel(new DefaultComboBoxModel(v1));
		
		Vector<String> v2 = new Vector<String>(Database.getInstance().getRaceList());
		Collections.sort(v2);
		race.setModel(new DefaultComboBoxModel(v2));
		
		Vector<String> v3 = new Vector<String>(Database.getInstance().getCharacterClassList());
		Collections.sort(v3);
		characterClass.setModel(new DefaultComboBoxModel(v3));
		
		Vector<String> v4 = new Vector<String>(Database.getInstance().getPersonalities().keySet());
		Collections.sort(v4);
		personality.setModel(new DefaultComboBoxModel(v4));

		for (ItemComponent item : items)
		{
			item.initForeignKeys();
		}

		levels.initForeignKeys();
		spellList.initForeignKeys();
	}
	
	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		PlayerCharacter pc = getPlayerCharacter(name);

		if (pc == null)
		{
			pc = getPlayerCharacter(currentName);
		}
		
		pc.setExperience((Integer)experience.getValue());
		pc.setKills((Integer)kills.getValue());
		pc.setSpellPicks((Integer)spellPicks.getValue());
		pc.setGender(Database.getInstance().getGender((String)gender.getSelectedItem()));
		pc.setRace(Database.getInstance().getRace((String)race.getSelectedItem()));
		pc.setCharacterClass(Database.getInstance().getCharacterClass((String)characterClass.getSelectedItem()));
		pc.setPersonality(Database.getInstance().getPersonalities().get((String)personality.getSelectedItem()));
		pc.setPortrait(portrait.getText());
		
		pc.setLevels(levels.getLevels());

		CurMax hp = hitPoints.getCurMax();
		CurMax sp = actionPoints.getCurMax();
		CurMax mp = magicPoints.getCurMax();
		Stats newStats = new Stats((CurMaxSub)hp, sp, mp, stats.getModifier());
		pc.setStats(newStats);
		
		pc.setPractice(new Practice(practice.getModifier()));
		pc.setActiveModifiers(activeModifiers.getModifier());
		
		pc.setPrimaryWeapon(items[0].getItem());
		pc.setSecondaryWeapon(items[1].getItem());
		pc.setAltPrimaryWeapon(items[2].getItem());
		pc.setAltSecondaryWeapon(items[3].getItem());
		pc.setHelm(items[4].getItem());
		pc.setTorsoArmour(items[5].getItem());
		pc.setLegArmour(items[6].getItem());
		pc.setGloves(items[7].getItem());
		pc.setBoots(items[8].getItem());
		pc.setBannerItem(items[9].getItem());
		pc.setMiscItem1(items[10].getItem());
		pc.setMiscItem2(items[11].getItem());

		for (int i=12; i<MAX_ITEMS; i++)
		{
			pc.getInventory().add(items[i].getItem(), i-12);
		}

		List<Spell> spells = spellList.getSpells();
		pc.setSpellBook(new SpellBook(spells));

		ConditionManager.getInstance().setConditions(pc, conditionList.getConditions());
		
		commitPlayerCharacter(pc);
	}

	public void copyItem(String newName)
	{
		// not supported
	}

	public void deleteItem()
	{
		// not supported
	}
	
	public void newItem(String name)
	{
		// not supported
	}

	public void refresh(String name)
	{
		PlayerCharacter pc = getPlayerCharacter(name);

		if (pc == null)
		{
			return;
		}
		
		experience.removeChangeListener(this);
		kills.removeChangeListener(this);
		spellPicks.removeChangeListener(this);
		gender.removeActionListener(this);
		race.removeActionListener(this);
		characterClass.removeActionListener(this);
		personality.removeActionListener(this);
		
		levels.refresh(pc.getLevels());
		experience.setValue(pc.getExperience());
		kills.setValue(pc.getKills());
		spellPicks.setValue(pc.getSpellPicks());
		gender.setSelectedItem(pc.getGender().getName());
		race.setSelectedItem(pc.getRace().getName());
		characterClass.setSelectedItem(pc.getCharacterClass().getName());
		personality.setSelectedItem(pc.getPersonality().getName());
		portrait.setText(pc.getPortrait());
		hitPoints.refresh(pc.getHitPoints());
		actionPoints.refresh(pc.getActionPoints());
		magicPoints.refresh(pc.getMagicPoints());
		stats.refresh(pc.getStats().getModifiers());
		practice.refresh(pc.getPractice().getModifiers());
		activeModifiers.refresh(pc.getActiveModifiers());
		
		items[0].refresh(pc.getPrimaryWeapon());
		items[1].refresh(pc.getSecondaryWeapon());
		items[2].refresh(pc.getAltPrimaryWeapon());
		items[3].refresh(pc.getAltSecondaryWeapon());
		items[4].refresh(pc.getHelm());
		items[5].refresh(pc.getTorsoArmour());
		items[6].refresh(pc.getLegArmour());
		items[7].refresh(pc.getGloves());
		items[8].refresh(pc.getBoots());
		items[9].refresh(pc.getBannerItem());
		items[10].refresh(pc.getMiscItem1());
		items[11].refresh(pc.getMiscItem2());

		for (int i=12; i<MAX_ITEMS; i++)
		{
			items[i].refresh(pc.getInventory().get(i-12));
		}
		
		spellList.refresh(pc.getSpellBook().getSpells());
		conditionList.refresh(pc);
		
		experience.addChangeListener(this);
		kills.addChangeListener(this);
		spellPicks.addChangeListener(this);
		gender.addActionListener(this);
		race.addActionListener(this);
		characterClass.addActionListener(this);
		personality.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		// not supported
	}

	/*-------------------------------------------------------------------------*/
	public abstract PlayerCharacter getPlayerCharacter(String name);
	
	/*-------------------------------------------------------------------------*/
	public abstract void commitPlayerCharacter(PlayerCharacter pc);

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if (currentName != null)
		{
			commit(currentName);
		}

		currentName = (String)names.getSelectedValue();
		if (currentName == null)
		{
			return;
		}
		if (currentName != null)
		{
			refresh(currentName);
		}

		SwingEditor.instance.setDirty(this.dirtyFlag);
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		// todo
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// todo
	}

	/*-------------------------------------------------------------------------*/
	protected void dodgyGridBagShite(JPanel panel, Component a, Component b, GridBagConstraints gbc)
	{
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx=0;
		gbc.gridy++;
		panel.add(a, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		panel.add(b, gbc);
	}

	/*-------------------------------------------------------------------------*/
	protected GridBagConstraints createGridBagConstraints()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		return gbc;
	}
}
