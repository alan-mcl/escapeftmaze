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
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.data.v1.V1Utils;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.util.MazeException;
import mclachlan.maze.game.MazeScript;

/**
 *
 */
public class FoeAttackPanel extends EditorPanel
{
	private JComboBox type, attackScript, minRange, maxRange;
	private JTextField description, slaysFoeType;
	private StatModifierComponent modifiers;

	// physical
	private JTextField damage, attacks;
	private JComboBox damageType;
	private JSpinner spellEffectLevel;
	private SpellEffectGroupOfPossibilitiesPanel spellEffects;
	private JButton makeBackstab;

	// spells
	private CastSpellPercentageTablePanel spells;

	// special ability
	private JComboBox specialAbility;
	private JTextField specialAbilityCastingLevel;
	
	static FoeAttack.Type[] types = 
		{
			FoeAttack.Type.MELEE_ATTACK,
			FoeAttack.Type.RANGED_ATTACK,
			FoeAttack.Type.CAST_SPELL,
			FoeAttack.Type.SPECIAL_ABILITY,
		};
	
	static String[] ranges = 
		{
			ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.MELEE),
			ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.EXTENDED),
			ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.THROWN),
			ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.LONG),
		};
	private JPanel physicalAttackPanel;
	private JPanel specialAbilityPanel;
	private JPanel castSpellPanel;

	/*-------------------------------------------------------------------------*/
	public FoeAttackPanel()
	{
		super(SwingEditor.Tab.FOE_ATTACKS);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel result = new JPanel(new BorderLayout(3,3));

		result.add(getTopPanel(), BorderLayout.NORTH);

		JPanel center = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		physicalAttackPanel = getPhysicalAttackPanel();
		center.add(physicalAttackPanel, gbc);

		JPanel right = new JPanel(new GridBagLayout());
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.insets = new Insets(3,3,3,3);
		gbc2.gridx = 0;
		gbc2.gridy = 0;
		gbc2.gridwidth = 1;
		gbc2.gridheight = 1;
		gbc2.weightx = 1.0;
		gbc2.weighty = 0.0;
		gbc2.anchor = GridBagConstraints.NORTHWEST;
		specialAbilityPanel = getSpecialAbilityPanel();
		right.add(specialAbilityPanel, gbc2);

		gbc2.gridy++;
		gbc2.weighty = 1.0;
		castSpellPanel = getCastSpellPanel();
		right.add(castSpellPanel, gbc2);

		gbc.gridx++;
		gbc.weightx = 1.0;
		center.add(right, gbc);

		result.add(center, BorderLayout.CENTER);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getTopPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = createGridBagConstraints();

		type = new JComboBox(types);
		type.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Type:"), type, gbc);

		description = new JTextField(20);
		description.addKeyListener(this);
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel("Description:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(description, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getPhysicalAttackPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());

		result.setBorder(BorderFactory.createTitledBorder("Melee/Ranged"));

		GridBagConstraints gbc = createGridBagConstraints();

		Vector<MagicSys.SpellEffectType> damageTypes = new Vector<MagicSys.SpellEffectType>();
		for (MagicSys.SpellEffectType type : MagicSys.SpellEffectType.values())
		{
			damageTypes.add(type);
		}
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

		minRange = new JComboBox(ranges);
		minRange.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Min Range:"), minRange, gbc);

		maxRange = new JComboBox(ranges);
		maxRange.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Max Range:"), maxRange, gbc);

		slaysFoeType = new JTextField(20);
		slaysFoeType.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Slays Foe Type:"), slaysFoeType, gbc);

		attackScript = new JComboBox();
		attackScript.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Attack Script:"), attackScript, gbc);

		spellEffectLevel = new JSpinner(new SpinnerNumberModel(1, 1, 256, 1));
		spellEffectLevel.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Spell Effect Level:"), spellEffectLevel, gbc);

		spellEffects = new SpellEffectGroupOfPossibilitiesPanel(dirtyFlag, 0.66);
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		result.add(spellEffects, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCastSpellPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());

		result.setBorder(BorderFactory.createTitledBorder("Cast Spell"));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		spells = new CastSpellPercentageTablePanel(dirtyFlag, 0.66);
		result.add(new JScrollPane(spells), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getSpecialAbilityPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());

		result.setBorder(BorderFactory.createTitledBorder("Special Abilty"));

		GridBagConstraints gbc = createGridBagConstraints();

		specialAbility = new JComboBox();
		specialAbility.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Special Ability:"), specialAbility, gbc);

		specialAbilityCastingLevel = new JTextField(20);
		specialAbilityCastingLevel.addKeyListener(this);
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel("Casting Level:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(specialAbilityCastingLevel, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector vec = new Vector(Database.getInstance().getFoeAttacks().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector scripts = new Vector(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(scripts);
		attackScript.setModel(new DefaultComboBoxModel(scripts));

		Vector spells = new Vector(Database.getInstance().getSpellList());
		Collections.sort(spells);
		specialAbility.setModel(new DefaultComboBoxModel(spells));

		this.spellEffects.initForeignKeys();
		this.spells.initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	private void clearPhysicalAttackWidgets()
	{
		maxRange.removeActionListener(this);
		minRange.removeActionListener(this);
		attackScript.removeActionListener(this);

		damage.setText("");
		attacks.setText("");
		modifiers.setModifier(null);
		minRange.setSelectedIndex(0);
		maxRange.setSelectedIndex(0);
		slaysFoeType.setText("");
		attackScript.setSelectedIndex(0);
		spellEffectLevel.setValue(1);
		spellEffects.refresh(null);

		minRange.addActionListener(this);
		maxRange.addActionListener(this);
		attackScript.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	private void clearCastSpellsWidgets()
	{
		spells.refresh(null);
	}

	/*-------------------------------------------------------------------------*/
	private void clearSpecialAbilityWidgets()
	{
		specialAbility.removeActionListener(this);

		specialAbility.setSelectedIndex(0);
		specialAbilityCastingLevel.setText("");

		specialAbility.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	private void setType(FoeAttack.Type faType)
	{
		type.removeActionListener(this);
		type.setSelectedItem(faType);

		switch (faType)
		{
			case MELEE_ATTACK:
			case RANGED_ATTACK:
				setEnabledAllEditControls(physicalAttackPanel, true);
				setEnabledAllEditControls(castSpellPanel, false);
				setEnabledAllEditControls(specialAbilityPanel, false);
				clearCastSpellsWidgets();
				clearSpecialAbilityWidgets();
				break;
			case CAST_SPELL:
				setEnabledAllEditControls(physicalAttackPanel, false);
				setEnabledAllEditControls(castSpellPanel, true);
				setEnabledAllEditControls(specialAbilityPanel, false);
				clearPhysicalAttackWidgets();
				clearSpecialAbilityWidgets();
				break;
			case SPECIAL_ABILITY:
				setEnabledAllEditControls(physicalAttackPanel, false);
				setEnabledAllEditControls(castSpellPanel, false);
				setEnabledAllEditControls(specialAbilityPanel, true);
				clearCastSpellsWidgets();
				clearPhysicalAttackWidgets();
				break;
		}

		type.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		FoeAttack fa = Database.getInstance().getFoeAttack(name);

		setType(fa.getType());
		description.setText(fa.getDescription());

		damageType.removeActionListener(this);
		minRange.removeActionListener(this);
		maxRange.removeActionListener(this);
		attackScript.removeActionListener(this);
		spellEffectLevel.removeChangeListener(this);
		specialAbility.removeActionListener(this);

		if (fa.getType() == FoeAttack.Type.MELEE_ATTACK || fa.getType() == FoeAttack.Type.RANGED_ATTACK)
		{
			damage.setText(V1Dice.toString(fa.getDamage()));
			damageType.setSelectedItem(fa.getDefaultDamageType());
			attacks.setText(V1Utils.toStringInts(fa.getAttacks(), ","));
			modifiers.setModifier(fa.getModifiers());
			minRange.setSelectedItem(ItemTemplate.WeaponRange.describe(fa.getMinRange()));
			maxRange.setSelectedItem(ItemTemplate.WeaponRange.describe(fa.getMaxRange()));
			slaysFoeType.setText(fa.getSlaysFoeType());
			attackScript.setSelectedItem(fa.getAttackScript().getName());
			spellEffectLevel.setValue(fa.getSpellEffectLevel());
			spellEffects.refresh(fa.getSpellEffects());
		}
		else if (fa.getType() == FoeAttack.Type.CAST_SPELL)
		{
			spells.refresh(fa.getSpells());
		}
		else if (fa.getType() == FoeAttack.Type.SPECIAL_ABILITY)
		{
			specialAbility.setSelectedItem(fa.getSpecialAbility().getName());
			specialAbilityCastingLevel.setText(V1Dice.toString(fa.getSpecialAbility().getCastingLevel()));
		}

		damageType.addActionListener(this);
		minRange.addActionListener(this);
		maxRange.addActionListener(this);
		attackScript.addActionListener(this);
		spellEffectLevel.addChangeListener(this);
		specialAbility.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		// hackish way to set a default script, because I keep forgetting this 
		MazeScript script =
			Database.getInstance().getMazeScripts().get("generic weapon swish");

		if (script == null)
		{
			script =
				Database.getInstance().getScript((String)attackScript.getItemAt(0));
		}

		FoeAttack fa = new FoeAttack(
			name,
			"",
			FoeAttack.Type.MELEE_ATTACK,
			Dice.d1,
			MagicSys.SpellEffectType.NONE,
			new StatModifier(),
			ItemTemplate.WeaponRange.MELEE,
			ItemTemplate.WeaponRange.MELEE,
			new GroupOfPossibilities<SpellEffect>(),
			1,
			new int[]{1},
			"",
			script);

		Database.getInstance().getFoeAttacks().put(name, fa);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		FoeAttack fa = Database.getInstance().getFoeAttacks().remove(currentName);
		fa.setName(newName);
		Database.getInstance().getFoeAttacks().put(newName, fa);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		FoeAttack current = Database.getInstance().getFoeAttacks().get(currentName);

		FoeAttack fa;

		if (current.getType() == FoeAttack.Type.MELEE_ATTACK ||
			current.getType() == FoeAttack.Type.RANGED_ATTACK)
		{
			fa = new FoeAttack(
				newName,
				current.getDescription(),
				current.getType(),
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
		}
		else if (current.getType() == FoeAttack.Type.CAST_SPELL)
		{
			fa = new FoeAttack(
				newName,
				current.getDescription(),
				current.getType(),
				current.getSpells());
		}
		else if (current.getType() == FoeAttack.Type.SPECIAL_ABILITY)
		{
			fa = new FoeAttack(
				newName,
				current.getDescription(),
				current.getType(),
				current.getSpecialAbility());
		}
		else
		{
			throw new MazeException("Invalid type "+current.getType());
		}

		Database.getInstance().getFoeAttacks().put(newName, fa);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getFoeAttacks().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		FoeAttack fa = Database.getInstance().getFoeAttack(name);

		FoeAttack.Type faType = (FoeAttack.Type)type.getSelectedItem();

		fa.setType(faType);
		fa.setDescription(description.getText());

		if (faType == FoeAttack.Type.MELEE_ATTACK ||
			faType == FoeAttack.Type.RANGED_ATTACK)
		{
			fa.setDamage(V1Dice.fromString(damage.getText()));
			fa.setDamageType((MagicSys.SpellEffectType)damageType.getSelectedItem());
			fa.setAttacks(V1Utils.fromStringInts(attacks.getText(), ","));
			fa.setModifiers(modifiers.getModifier());
			fa.setMinRange(ItemTemplate.WeaponRange.valueOf((String)minRange.getSelectedItem()));
			fa.setMaxRange(ItemTemplate.WeaponRange.valueOf((String)maxRange.getSelectedItem()));
			fa.setSlaysFoeType(slaysFoeType.getText());
			fa.setAttackScript(Database.getInstance().getScript((String)attackScript.getSelectedItem()));
			fa.setSpellEffectLevel((Integer)spellEffectLevel.getValue());
			fa.setSpellEffects(spellEffects.getGroupOfPossibilties());
		}
		else if (faType == FoeAttack.Type.CAST_SPELL)
		{
			fa.setSpells(spells.getPercentageTable());
		}
		else if (faType == FoeAttack.Type.SPECIAL_ABILITY)
		{
			FoeAttack.FoeAttackSpell sa = new FoeAttack.FoeAttackSpell(
				Database.getInstance().getSpell((String)specialAbility.getSelectedItem()),
				V1Dice.fromString(specialAbilityCastingLevel.getText()));
			fa.setSpecialAbility(sa);
		}
		else
		{
			throw new MazeException("Invalid type "+faType);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == type)
		{
			setType((FoeAttack.Type)type.getSelectedItem());
			return;
		}
		else if (e.getSource() == makeBackstab)
		{
			StatModifier sm = modifiers.getModifier();
			sm.setModifier(Stats.Modifiers.DAMAGE_MULTIPLIER, 1);
			modifiers.setModifier(sm);
		}

		super.actionPerformed(e);
	}
}
