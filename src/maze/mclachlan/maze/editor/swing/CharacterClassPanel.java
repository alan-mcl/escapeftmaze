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
import java.util.List;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.stat.*;

/**
 *
 */
public class CharacterClassPanel extends EditorPanel
{
	private JTextArea description;
	private JComboBox focus;
	private JSpinner startingHitPoints;
	private JSpinner startingActionPoints;
	private JSpinner startingMagicPoints;
	private StatModifierComponent startingModifiers;
	private StatModifierComponent unlockModifiers;
	private StatModifierComponent startingActiveModifiers;
	private GenderSelection allowedGenders;
	private RaceSelection allowedRaces;
	private JComboBox experienceTable;
	private JTextField levelUpHitPoints;
	private JTextField levelUpActionPoints;
	private JTextField levelUpMagicPoints;
	private JSpinner levelUpAssignableModifiers;
	private StatModifierComponent levelUpModifiers;
	private List<LevelAbilityComponent>[] levelAbilityComponents;
	private static final int MAX_ABILITIES = 10;

	/*-------------------------------------------------------------------------*/
	protected CharacterClassPanel()
	{
		super(SwingEditor.Tab.CHARACTER_CLASSES);
	}

	/*-------------------------------------------------------------------------*/
	protected Container getEditControls()
	{
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

		tabs.addTab("Stats & Desc", getOne());
		tabs.addTab("Availability", getTwo());
		tabs.addTab("Progression", getProgression());

		return tabs;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getProgression()
	{
		int maxLevels = LevelAbilityProgression.MAX_LEVELS;

		levelAbilityComponents = new List[maxLevels];

		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		for (int i=0; i<maxLevels; i++)
		{

			levelAbilityComponents[i] = new ArrayList();
			JPanel temp = new JPanel(new FlowLayout());

			for (int j=0; j< MAX_ABILITIES; j++)
			{
				LevelAbilityComponent lac = new LevelAbilityComponent(this.dirtyFlag);
				temp.add(lac);
				levelAbilityComponents[i].add(lac);
			}

			dodgyGridBagShite(result, new JLabel("Level " + (i + 1) + ":"), temp, gbc);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getTwo()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel left = getTwoLeftPanel();

		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		result.add(left, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getTwoLeftPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		allowedGenders = new GenderSelection(dirtyFlag, false);
		result.add(allowedGenders, gbc);
		
		allowedRaces = new RaceSelection(dirtyFlag);
		gbc.gridy++;
		gbc.weighty = 1.0;
		result.add(allowedRaces, gbc);
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getOne()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel left = getOneLeftPanel();
		JPanel right = getOneRightPanel();

		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		result.add(left, gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		result.add(right, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getOneRightPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		description = new JTextArea(35, 30);
		description.setWrapStyleWord(true);
		description.setLineWrap(true);
		description.addKeyListener(this);
		result.add(new JScrollPane(description), gbc);
				
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getOneLeftPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		
		focus = new JComboBox(
			new Object[]{
				CharacterClass.Focus.COMBAT,
				CharacterClass.Focus.STEALTH,
				CharacterClass.Focus.MAGIC,
			});
		focus.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Focus:"), focus, gbc);

		experienceTable = new JComboBox();
		experienceTable.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Experience Table:"), experienceTable, gbc);
		
		levelUpHitPoints = new JTextField(5);
		levelUpHitPoints.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Level Up Hit Points:"), levelUpHitPoints, gbc);
		
		levelUpActionPoints = new JTextField(5);
		levelUpActionPoints.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Level Up Action Points"), levelUpActionPoints, gbc);
		
		levelUpMagicPoints = new JTextField(5);
		levelUpMagicPoints.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Level Up Magic Points:"), levelUpMagicPoints, gbc);
		
		levelUpAssignableModifiers = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		dodgyGridBagShite(result, new JLabel("Level Up Assignable Mdfrs:"), levelUpAssignableModifiers, gbc);
		
		levelUpModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Level Up Modifiers:"), levelUpModifiers, gbc);
		
		unlockModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Unlock Modifiers:"), unlockModifiers, gbc);
		
		startingHitPoints = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		startingHitPoints.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Starting Hit Points:"), startingHitPoints, gbc);
		
		startingActionPoints = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		startingActionPoints.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Starting Action Points:"), startingActionPoints, gbc);
		
		startingMagicPoints = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		startingMagicPoints.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Starting Magic Points:"), startingMagicPoints, gbc);
		
		startingModifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(result, new JLabel("Starting Modifiers:"), startingModifiers, gbc);
		
		startingActiveModifiers = new StatModifierComponent(dirtyFlag);
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel("Starting Active Modifiers:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(startingActiveModifiers, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
		CharacterClass characterClass = new CharacterClass(
			name,
			CharacterClass.Focus.COMBAT,
			"",
			0,
			0,
			0,
			new StatModifier(),
			new StatModifier(),
			new StatModifier(),
			null,
			null,
			Database.getInstance().getExperienceTable((String)(experienceTable.getSelectedObjects()[0])),
			Dice.d1,
			Dice.d1,
			Dice.d1,
			0,
			new StatModifier(),
			new LevelAbilityProgression());
		getMap().put(name, characterClass);

		return characterClass;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
		CharacterClass current = Database.getInstance().getCharacterClass((String)names.getSelectedValue());
		getMap().remove(current.getName());
		current.setName(newName);
		getMap().put(current.getName(), current);
		refreshNames(newName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		SwingEditor.instance.setDirty(dirtyFlag);

		CharacterClass current = Database.getInstance().getCharacterClass((String)names.getSelectedValue());

		CharacterClass characterClass = new CharacterClass(
			newName,
			current.getFocus(),
			current.getDescription(),
			current.getStartingHitPoints(),
			current.getStartingActionPoints(),
			current.getStartingMagicPoints(),
			new StatModifier(current.getStartingActiveModifiers()),
			new StatModifier(current.getStartingModifiers()),
			new StatModifier(current.getUnlockModifiers()),
			new HashSet<>(current.getAllowedGenders()),
			new HashSet<>(current.getAllowedRaces()),
			current.getExperienceTable(),
			current.getLevelUpHitPoints(),
			current.getLevelUpActionPoints(),
			current.getLevelUpMagicPoints(),
			current.getLevelUpAssignableModifiers(),
			new StatModifier(current.getLevelUpModifiers()),
			current.getProgression());

		getMap().put(newName, characterClass);

		return characterClass;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		SwingEditor.instance.setDirty(dirtyFlag);
		String name = (String)names.getSelectedValue();
		getMap().remove(name);
		refreshNames(null);
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>((Database.getInstance().getCharacterClasses().values()));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		if (name == null)
		{
			return;
		}

		CharacterClass cc = Database.getInstance().getCharacterClass(name);
		
		focus.removeActionListener(this);
		startingHitPoints.removeChangeListener(this);
		startingActionPoints.removeChangeListener(this);
		startingMagicPoints.removeChangeListener(this);
		experienceTable.removeActionListener(this);
		levelUpAssignableModifiers.removeChangeListener(this);

		description.setText(cc.getDescription());
		description.setCaretPosition(0);
		focus.setSelectedItem(cc.getFocus());
		startingHitPoints.setValue(cc.getStartingHitPoints());
		startingActionPoints.setValue(cc.getStartingActionPoints());
		startingMagicPoints.setValue(cc.getStartingMagicPoints());
		startingModifiers.setModifier(cc.getStartingModifiers());
		unlockModifiers.setModifier(cc.getUnlockModifiers());
		startingActiveModifiers.setModifier(cc.getStartingActiveModifiers());
		allowedGenders.refresh(cc.getAllowedGenders(), null);
		allowedRaces.refresh(cc.getAllowedRaces());
		experienceTable.setSelectedItem(cc.getExperienceTable().getName());
		levelUpHitPoints.setText(cc.getLevelUpHitPoints().toString());
		levelUpActionPoints.setText(cc.getLevelUpActionPoints().toString());
		levelUpMagicPoints.setText(cc.getLevelUpMagicPoints().toString());
		levelUpAssignableModifiers.setValue(cc.getLevelUpAssignableModifiers());
		levelUpModifiers.setModifier(cc.getLevelUpModifiers());

		updateProgression(cc.getProgression());
		
		focus.addActionListener(this);
		startingHitPoints.addChangeListener(this);
		startingActionPoints.addChangeListener(this);
		startingMagicPoints.addChangeListener(this);
		experienceTable.addActionListener(this);
		levelUpAssignableModifiers.addChangeListener(this);
	}

	/*-------------------------------------------------------------------------*/
	private void updateProgression(LevelAbilityProgression progression)
	{
		for (int i=1; i<=LevelAbilityProgression.MAX_LEVELS; i++)
		{
			List<LevelAbility> abilities = progression.getForLevel(i);

			for (int j=0; j<MAX_ABILITIES; j++)
			{
				LevelAbilityComponent lac = levelAbilityComponents[i-1].get(j);
				if (abilities.size() > j)
				{
					lac.refresh(abilities.get(j));
				}
				else
				{
					lac.refresh(null);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> xpTables = new Vector<String>(Database.getInstance().getExperienceTables().keySet());
		Collections.sort(xpTables);
		experienceTable.setModel(new DefaultComboBoxModel(xpTables));
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		if (!getMap().containsKey(name))
		{
			// class is gone for some reason, ignore
			return null;
		}

		CharacterClass c = Database.getInstance().getCharacterClass(name);

		c.setFocus((CharacterClass.Focus)this.focus.getSelectedItem());
		c.setDescription(this.description.getText());
		c.setStartingActiveModifiers(startingActiveModifiers.getModifier());
		c.setStartingModifiers(startingModifiers.getModifier());
		c.setStartingHitPoints((Integer)startingHitPoints.getValue());
		c.setStartingActionPoints((Integer)startingActionPoints.getValue());
		c.setStartingMagicPoints((Integer)startingMagicPoints.getValue());
		c.setLevelUpAssignableModifiers((Integer)levelUpAssignableModifiers.getValue());
		c.setLevelUpModifiers(levelUpModifiers.getModifier());
		c.setUnlockModifiers(unlockModifiers.getModifier());
		c.setLevelUpHitPoints(V1Dice.fromString(levelUpHitPoints.getText()));
		c.setLevelUpActionPoints(V1Dice.fromString(levelUpActionPoints.getText()));
		c.setLevelUpMagicPoints(V1Dice.fromString(levelUpMagicPoints.getText()));

		c.setAllowedGenders(allowedGenders.getAllowedGenders());
		c.setAllowedRaces(allowedRaces.getAllowedRaces());
		c.setExperienceTable(Database.getInstance().getExperienceTable((String)experienceTable.getSelectedItem()));

		c.setProgression(getProgressionData());

		return c;
	}

	/*-------------------------------------------------------------------------*/
	private LevelAbilityProgression getProgressionData()
	{
		LevelAbilityProgression result = new LevelAbilityProgression();

		for (int i=1; i<=LevelAbilityProgression.MAX_LEVELS; i++)
		{
			List<LevelAbility> abilities = new ArrayList<LevelAbility>();

			for (int j=0; j<MAX_ABILITIES; j++)
			{
				LevelAbilityComponent lac = levelAbilityComponents[i-1].get(j);
				if (lac.getResult() != null)
				{
					abilities.add(lac.getResult());
				}
			}

			result.setForLevel(i, abilities);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected Map<String, CharacterClass> getMap()
	{
		return Database.getInstance().getCharacterClasses();
	}
}
