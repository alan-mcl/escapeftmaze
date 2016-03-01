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
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.TypeDescriptorImpl;
import mclachlan.maze.stat.magic.MagicSys;

import static mclachlan.maze.stat.ItemTemplate.Type.MAX_ITEM_TYPES;
import static mclachlan.maze.stat.ItemTemplate.WeaponSubType.MAX_SUBTYPES;

/**
 *
 */
public class ItemTemplatePanel extends EditorPanel
{
	private JTextField pluralName, unidentifiedName, image;
	private JComboBox sortBy, type, subType, invokedSpell, chargesType, attackScript,
		minRange, maxRange, discipline, ammoType, slaysFoeType;
	private JTextArea description;
	private StatModifierComponent modifiers, equipRequirements, useRequirements;
	private EquipableSlotsComponent equipableSlots;
	private JSpinner weight, curseStrength, maxItemsPerStack, baseCost, invokedSpellLevel,
		identificationDifficulty, rechargeDifficulty, toHit, toPenetrate, toCritical, 
		toInitiative, bonusAttacks, bonusStrikes, damagePrevention, damagePreventionChance;
	private CharacterClassSelection usableByCharacterClass;
	private RaceSelection usableByRace;
	private GenderSelection usableByGender;
	private JCheckBox isQuestItem, isTwoHanded, isRanged, isReturning, isBackstabCapable, isSnipeCapable;
	private DiceField charges, damage;
	private JComboBox defaultDamageType;
	private AttackTypeSelection attackTypes;
	private AmmoTypeComponent ammoTypes;
	private SpellEffectGroupOfPossibilitiesPanel spellEffects;
	private JSpinner enchantmentChance;
	private JComboBox enchantmentCalculation, enchantmentScheme, disassemblyLootTable;
	private JSpinner conversionRate;

	private static String[] validItemTypes, validItemSubTypes;

	static String[] ranges =
	{
		ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.MELEE),
		ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.EXTENDED),
		ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.THROWN),
		ItemTemplate.WeaponRange.describe(ItemTemplate.WeaponRange.LONG),
	};

	/*-------------------------------------------------------------------------*/
	private enum SortBy
	{
		NAME,
		TYPE
	}

	private SortBy currentSortBy;

	/*-------------------------------------------------------------------------*/
	static
	{
		validItemTypes = new String[MAX_ITEM_TYPES];
		for (int i=0; i<MAX_ITEM_TYPES; i++)
		{
			validItemTypes[i] = ItemTemplate.Type.describe(i);
		}

		validItemSubTypes = new String[MAX_SUBTYPES];
		for (int i = 0; i < MAX_SUBTYPES; i++)
		{
			validItemSubTypes[i] = ItemTemplate.WeaponSubType.describe(i);
		}
	}

	/*-------------------------------------------------------------------------*/
	public ItemTemplatePanel()
	{
		super(SwingEditor.Tab.ITEM_TEMPLATES);

		currentSortBy = SortBy.NAME;
	}

	/*-------------------------------------------------------------------------*/
	public Container getEditControls()
	{
		JTabbedPane tabs = new JTabbedPane();

		tabs.add("Base", getBaseTab());
		tabs.add("Specific", getSpecificTab());
		tabs.add("Usability", getUsabilityTab());
		tabs.add("Enchantments", getEnchantmentsTab());

		return tabs;
	}

	/*-------------------------------------------------------------------------*/
	private Component getEnchantmentsTab()
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

		enchantmentChance = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		dodgyGridBagShite(result, new JLabel("Enchantment Chance:"), enchantmentChance, gbc);

		enchantmentCalculation = new JComboBox(ItemTemplate.EnchantmentCalculation.values());
		dodgyGridBagShite(result, new JLabel("Enchantment Calculation:"), enchantmentCalculation, gbc);

		enchantmentScheme = new JComboBox();
		enchantmentScheme.addActionListener(this);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(new JLabel("Item Enchantment Scheme:"), gbc);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx++;
		result.add(enchantmentScheme, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Component getUsabilityTab()
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

		equipableSlots = new EquipableSlotsComponent("Equipable Slots", dirtyFlag);
		result.add(equipableSlots, gbc);

		gbc.gridx++;
		usableByCharacterClass = new CharacterClassSelection(dirtyFlag);
		result.add(usableByCharacterClass, gbc);

		gbc.gridx++;
		usableByRace = new RaceSelection(dirtyFlag);
		result.add(usableByRace, gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		usableByGender = new GenderSelection(dirtyFlag, false);
		result.add(usableByGender, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Component getSpecificTab()
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

		result.add(getSpecificLeft(), gbc);

		gbc.gridx++;
		attackTypes = new AttackTypeSelection(dirtyFlag);
		result.add(attackTypes, gbc);

		gbc.gridx++;
		gbc.weightx=1.0;
		ammoTypes = new AmmoTypeComponent("Uses Ammo", dirtyFlag);
		result.add(ammoTypes, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getSpecificLeft()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		damage = new DiceField();
		damage.addActionListener(this);
		damage.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("Damage:"), damage, gbc);

		Vector<MagicSys.SpellEffectType> damageTypes = new Vector<MagicSys.SpellEffectType>();
		Collections.addAll(damageTypes, MagicSys.SpellEffectType.values());
		defaultDamageType = new JComboBox(damageTypes);
		defaultDamageType.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Default Damage Type:"), defaultDamageType, gbc);

		isTwoHanded = new JCheckBox("Two Handed?");
		isRanged = new JCheckBox("Ranged?");
		isReturning = new JCheckBox("Returning?");
		isBackstabCapable = new JCheckBox("Backstab?");
		isSnipeCapable = new JCheckBox("Snipe?");

		isTwoHanded.addActionListener(this);
		isRanged.addActionListener(this);
		isReturning.addActionListener(this);
		isBackstabCapable.addActionListener(this);
		isSnipeCapable.addActionListener(this);

		dodgyGridBagShite(result, isTwoHanded, isRanged, gbc);
		dodgyGridBagShite(result, isBackstabCapable, isSnipeCapable, gbc);
		dodgyGridBagShite(result, isReturning, new JLabel(), gbc);

		toHit = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		toHit.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("To Hit:"), toHit, gbc);

		toPenetrate = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		toPenetrate.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("To Penetrate:"), toPenetrate, gbc);
		
		toCritical = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		toCritical.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("To Critical:"), toCritical, gbc);
		
		toInitiative = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		toInitiative.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("To Initiative:"), toInitiative, gbc);

		minRange = new JComboBox(ranges);
		minRange.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Min Range:"), minRange, gbc);

		maxRange = new JComboBox(ranges);
		maxRange.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Max Range:"), maxRange, gbc);

		bonusAttacks = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		bonusAttacks.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Bonus Attacks:"), bonusAttacks, gbc);

		bonusStrikes = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		bonusStrikes.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Bonus Strikes:"), bonusStrikes, gbc);

		Vector<Object> vec = new Vector<Object>(Stats.allModifiers);
		vec.add(0, NONE);
		discipline = new JComboBox(vec);
		discipline.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Discipline:"), discipline, gbc);

		Vector<String> types = new Vector<String>();
		types.addAll(Database.getInstance().getCharacterClassList());
		types.addAll(Database.getInstance().getRaceList());
		types.addAll(Database.getInstance().getFoeTypes().keySet());
		Collections.sort(types);
		types.add(0, NONE);

		slaysFoeType = new JComboBox(types);
		slaysFoeType.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Slays Foe Type:"), slaysFoeType, gbc);

		Vector<String> ammoTypeVec = new Vector<String>();
		for (ItemTemplate.AmmoType at : ItemTemplate.AmmoType.values())
		{
			ammoTypeVec.add(at.toString());
		}
		Collections.sort(ammoTypeVec);
		ammoTypeVec.add(0, NONE);
		
		ammoType = new JComboBox(ammoTypeVec);
		ammoType.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Is Ammo Type:"), ammoType, gbc);

		damagePrevention = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		damagePrevention.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Damage Prevention:"), damagePrevention, gbc);

		damagePreventionChance = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		damagePreventionChance.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Damage Prevention Chance:"), damagePreventionChance, gbc);

		spellEffects = new SpellEffectGroupOfPossibilitiesPanel(dirtyFlag, 0.5);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridwidth = 2;
		gbc.gridy++;
		result.add(spellEffects, gbc);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Component getBaseTab()
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

		result.add(getBaseLeft(), gbc);

		gbc.gridx++;
		gbc.weightx=1.0;

		result.add(getBaseRight(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Component getBaseRight()
	{
		JPanel right = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		description = new JTextArea(20, 30);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.addKeyListener(this);
		gbc.weighty = 1.0;
		right.add(new JScrollPane(description), gbc);

		return right;
	}

	/*-------------------------------------------------------------------------*/
	private Component getBaseLeft()
	{
		JPanel left = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		sortBy = new JComboBox(SortBy.values());
		sortBy.addActionListener(this);
		dodgyGridBagShite(left, new JLabel("Sort By:"), sortBy, gbc);

		pluralName = new JTextField(20);
		pluralName.addKeyListener(this);
		dodgyGridBagShite(left, new JLabel("Plural Name:"), pluralName, gbc);

		unidentifiedName = new JTextField(20);
		unidentifiedName.addKeyListener(this);
		dodgyGridBagShite(left, new JLabel("Unidentified Name:"), unidentifiedName, gbc);

		type = new JComboBox(validItemTypes);
		type.addActionListener(this);
		dodgyGridBagShite(left, new JLabel("Type:"), type, gbc);

		subType = new JComboBox(validItemSubTypes);
		subType.addActionListener(this);
		dodgyGridBagShite(left, new JLabel("Sub Type:"), subType, gbc);

		image = new JTextField(20);
		image.addKeyListener(this);
		dodgyGridBagShite(left, new JLabel("Image:"), image, gbc);

		modifiers = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(left, new JLabel("Modifiers:"), modifiers, gbc);

		weight = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		weight.addChangeListener(this);
		dodgyGridBagShite(left, new JLabel("Weight (g):"), weight, gbc);

		isQuestItem = new JCheckBox("Quest Item?");
		isQuestItem.addActionListener(this);
		dodgyGridBagShite(left, isQuestItem, new JLabel(), gbc);

		curseStrength = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		curseStrength.addChangeListener(this);
		dodgyGridBagShite(left, new JLabel("Curse Strength:"), curseStrength, gbc);

		maxItemsPerStack = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
		maxItemsPerStack.addChangeListener(this);
		dodgyGridBagShite(left, new JLabel("Max per Stack:"), maxItemsPerStack, gbc);

		conversionRate = new JSpinner(new SpinnerNumberModel(0D, 0D, 99D, 0.1D));
		conversionRate.addChangeListener(this);
		dodgyGridBagShite(left, new JLabel("Conversion Rate:"), conversionRate, gbc);

		baseCost = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		baseCost.addChangeListener(this);
		dodgyGridBagShite(left, new JLabel("Base Cost (gp):"), baseCost, gbc);

		invokedSpell = new JComboBox();
		invokedSpell.addActionListener(this);
		dodgyGridBagShite(left, new JLabel("Invoked Spell:"), invokedSpell, gbc);

		invokedSpellLevel = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		invokedSpellLevel.addChangeListener(this);
		dodgyGridBagShite(left, new JLabel("Invoked Spell Level:"), invokedSpellLevel, gbc);

		charges = new DiceField();
		charges.addActionListener(this);
		charges.addKeyListener(this);
		dodgyGridBagShite(left, new JLabel("Charges:"), charges, gbc);

		chargesType = new JComboBox(ItemTemplate.ChargesType.values());
		chargesType.addActionListener(this);
		dodgyGridBagShite(left, new JLabel("Charges Type:"), chargesType, gbc);

		identificationDifficulty = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		identificationDifficulty.addChangeListener(this);
		dodgyGridBagShite(left, new JLabel("Identification Difficulty:"), identificationDifficulty, gbc);

		rechargeDifficulty = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		rechargeDifficulty.addChangeListener(this);
		dodgyGridBagShite(left, new JLabel("Recharge Difficulty:"), rechargeDifficulty, gbc);

		equipRequirements = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(left, new JLabel("Equip Requirements:"), equipRequirements, gbc);

		useRequirements = new StatModifierComponent(dirtyFlag);
		dodgyGridBagShite(left, new JLabel("Use Requirements:"), useRequirements, gbc);

		disassemblyLootTable = new JComboBox();
		disassemblyLootTable.addActionListener(this);
		dodgyGridBagShite(left, new JLabel("Disassembly Loot:"), disassemblyLootTable, gbc);

		attackScript = new JComboBox();
		attackScript.addActionListener(this);
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		left.add(new JLabel("Attack Script:"), gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		left.add(attackScript, gbc);

		return left;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getItemTemplates().keySet());

		if (currentSortBy == null)
		{
			currentSortBy = SortBy.NAME;
		}

		switch (currentSortBy)
		{
			case NAME: Collections.sort(vec); break;
			case TYPE: Collections.sort(vec, new ItemTypeComparator()); break;
		}

		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> spells = new Vector<String>(Database.getInstance().getSpellList());
		Collections.sort(spells);
		spells.add(0, NONE);
		invokedSpell.setModel(new DefaultComboBoxModel(spells));

		Vector<String> scripts = new Vector<String>(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(scripts);
		scripts.add(0, NONE);
		attackScript.setModel(new DefaultComboBoxModel(scripts));

		Vector<String> ies = new Vector<String>(Database.getInstance().getItemEnchantments().keySet());
		Collections.sort(ies);
		ies.add(0, NONE);
		enchantmentScheme.setModel(new DefaultComboBoxModel(ies));

		Vector<String> lts = new Vector<String>(Database.getInstance().getLootTables().keySet());
		Collections.sort(lts);
		lts.add(0, NONE);
		disassemblyLootTable.setModel(new DefaultComboBoxModel(lts));

		spellEffects.initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		ItemTemplate it = Database.getInstance().getItemTemplate(name);

		type.removeActionListener(this);
		subType.removeActionListener(this);
		weight.removeChangeListener(this);
		curseStrength.removeChangeListener(this);
		maxItemsPerStack.removeChangeListener(this);
		conversionRate.removeChangeListener(this);
		baseCost.removeChangeListener(this);
		invokedSpell.removeActionListener(this);
		invokedSpellLevel.removeChangeListener(this);
		chargesType.removeActionListener(this);
		identificationDifficulty.removeChangeListener(this);
		rechargeDifficulty.removeChangeListener(this);
		attackScript.removeActionListener(this);
		toHit.removeChangeListener(this);
		toPenetrate.removeChangeListener(this);
		toCritical.removeChangeListener(this);
		toInitiative.removeChangeListener(this);
		minRange.removeActionListener(this);
		maxRange.removeActionListener(this);
		bonusAttacks.removeChangeListener(this);
		bonusStrikes.removeChangeListener(this);
		discipline.removeActionListener(this);
		ammoType.removeActionListener(this);
		defaultDamageType.removeActionListener(this);
		damagePrevention.removeChangeListener(this);
		damagePreventionChance.removeChangeListener(this);
		enchantmentChance.removeChangeListener(this);
		enchantmentCalculation.removeActionListener(this);
		enchantmentScheme.removeActionListener(this);
		isTwoHanded.removeActionListener(this);
		isRanged.removeActionListener(this);
		isReturning.removeActionListener(this);
		isBackstabCapable.removeActionListener(this);
		isSnipeCapable.removeActionListener(this);
		disassemblyLootTable.removeActionListener(this);
		slaysFoeType.removeActionListener(this);

		pluralName.setText(it.getPluralName());
		unidentifiedName.setText(it.getUnidentifiedName());
		type.setSelectedIndex(it.getType());
		subType.setSelectedIndex(it.getSubtype());
		description.setText(it.getDescription());
		description.setCaretPosition(0);
		image.setText(it.getImage());
		modifiers.setModifier(it.getModifiers());
		equipableSlots.refresh(it.getEquipableSlots());
		weight.setValue(it.getWeight());
		usableByCharacterClass.refresh(it.getUsableByCharacterClass());
		usableByGender.refresh(it.getUsableByGender(), null);
		usableByRace.refresh(it.getUsableByRace());
		isQuestItem.setSelected(it.isQuestItem());
		curseStrength.setValue(it.getCurseStrength());
		maxItemsPerStack.setValue(it.getMaxItemsPerStack());
		conversionRate.setValue((double)it.getConversionRate());
		baseCost.setValue(it.getBaseCost());
		if (it.getInvokedSpell() == null)
		{
			invokedSpell.setSelectedItem(NONE);
		}
		else
		{
			invokedSpell.setSelectedItem(it.getInvokedSpell().getName());
		}
		invokedSpellLevel.setValue(it.getInvokedSpellLevel());
		charges.setDice(it.getCharges());
		chargesType.setSelectedItem(it.getChargesType());
		identificationDifficulty.setValue(it.getIdentificationDifficulty());
		rechargeDifficulty.setValue(it.getRechargeDifficulty());
		equipRequirements.setModifier(it.getEquipRequirements());
		useRequirements.setModifier(it.getUseRequirements());
		if (it.getAttackScript() == null)
		{
			attackScript.setSelectedItem(NONE);
		}
		else
		{
			attackScript.setSelectedItem(it.getAttackScript().getName());
		}
		damage.setDice(it.getDamage());
		defaultDamageType.setSelectedItem(it.getDefaultDamageType());
		attackTypes.refresh(it.getAttackTypes());
		isTwoHanded.setSelected(it.isTwoHanded());
		isRanged.setSelected(it.isRanged());
		isReturning.setSelected(it.isReturning());
		isBackstabCapable.setSelected(it.isBackstabCapable());
		isSnipeCapable.setSelected(it.isSnipeCapable());
		toHit.setValue(it.getToHit());
		toPenetrate.setValue(it.getToPenetrate());
		toCritical.setValue(it.getToCritical());
		toInitiative.setValue(it.getToInitiative());
		minRange.setSelectedItem(ItemTemplate.WeaponRange.describe(it.getMinRange()));
		maxRange.setSelectedItem(ItemTemplate.WeaponRange.describe(it.getMaxRange()));
		ammoTypes.refresh(it.getAmmo());
		spellEffects.refresh(it.getSpellEffects());
		bonusAttacks.setValue(it.getBonusAttacks());
		bonusStrikes.setValue(it.getBonusStrikes());
		if (it.getDiscipline() == null)
		{
			discipline.setSelectedItem(NONE);
		}
		else
		{
			discipline.setSelectedItem(it.getDiscipline());
		}
		slaysFoeType.setSelectedItem(it.getSlaysFoeType()==null?NONE:it.getSlaysFoeType().getName());
		if (it.getAmmoType() == null)
		{
			ammoType.setSelectedItem(NONE);
		}
		else
		{
			ammoType.setSelectedItem(it.getAmmoType().toString());
		}
		damagePrevention.setValue(it.getDamagePrevention());
		damagePreventionChance.setValue(it.getDamagePreventionChance());
		enchantmentChance.setValue(it.getEnchantmentChance());
		enchantmentCalculation.setSelectedItem(it.getEnchantmentCalculation());
		if (it.getEnchantmentScheme() == null)
		{
			enchantmentScheme.setSelectedItem(NONE);
		}
		else
		{
			enchantmentScheme.setSelectedItem(it.getEnchantmentScheme());
		}
		if (it.getDisassemblyLootTable() == null)
		{
			disassemblyLootTable.setSelectedItem(NONE);
		}
		else
		{
			disassemblyLootTable.setSelectedItem(it.getDisassemblyLootTable());
		}

		type.addActionListener(this);
		subType.addActionListener(this);
		weight.addChangeListener(this);
		curseStrength.addChangeListener(this);
		maxItemsPerStack.addChangeListener(this);
		conversionRate.addChangeListener(this);
		baseCost.addChangeListener(this);
		invokedSpell.addActionListener(this);
		invokedSpellLevel.addChangeListener(this);
		chargesType.addActionListener(this);
		identificationDifficulty.addChangeListener(this);
		rechargeDifficulty.addChangeListener(this);
		attackScript.addActionListener(this);
		toHit.addChangeListener(this);
		toPenetrate.addChangeListener(this);
		toCritical.addChangeListener(this);
		toInitiative.addChangeListener(this);
		minRange.addActionListener(this);
		maxRange.addActionListener(this);
		bonusAttacks.addChangeListener(this);
		bonusStrikes.addChangeListener(this);
		discipline.addActionListener(this);
		ammoType.addActionListener(this);
		defaultDamageType.addActionListener(this);
		damagePrevention.addChangeListener(this);
		damagePreventionChance.addChangeListener(this);
		enchantmentChance.addChangeListener(this);
		enchantmentCalculation.addActionListener(this);
		enchantmentScheme.addActionListener(this);
		isTwoHanded.addActionListener(this);
		isRanged.addActionListener(this);
		isReturning.addActionListener(this);
		isBackstabCapable.addActionListener(this);
		isSnipeCapable.addActionListener(this);
		disassemblyLootTable.addActionListener(this);
		slaysFoeType.removeActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void newItem(String name)
	{
		ItemTemplate it = new ItemTemplate(
			name,
			"",
			"",
			ItemTemplate.Type.OTHER,
			ItemTemplate.WeaponSubType.NONE,
			"",
			new StatModifier(),
			"",
			new BitSet(),
			0,
			1,
			0,
			null,
			0,
			null,
			ItemTemplate.ChargesType.CHARGES_INFINITE,
			new HashSet<String>(),
			new HashSet<String>(),
			new HashSet<String>(),
			false,
			0,
			1,
			1,
			new StatModifier(),
			new StatModifier(),
			null,
			null,
			MagicSys.SpellEffectType.NONE,
			null,
			false,
			false,
			false,
			false,
			false,
			0,
			0,
			0,
			0,
			ItemTemplate.WeaponRange.MELEE,
			ItemTemplate.WeaponRange.MELEE,
			null,
			null,
			0,
			0,
			null,
			null,
			null,
			0,
			0,
			0,
			ItemTemplate.EnchantmentCalculation.STRAIGHT,
			null,
			null,
			0F);

		Database.getInstance().getItemTemplates().put(name, it);
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		ItemTemplate it = Database.getInstance().getItemTemplates().remove(currentName);
		it.setName(newName);
		Database.getInstance().getItemTemplates().put(newName, it);
	}

	/*-------------------------------------------------------------------------*/
	public void copyItem(String newName)
	{
		ItemTemplate current = Database.getInstance().getItemTemplates().get(currentName);

		ItemTemplate it = new ItemTemplate(
			newName,
			current.getPluralName(),
			current.getUnidentifiedName(),
			current.getType(),
			current.getSubtype(),
			current.getDescription(),
			new StatModifier(current.getModifiers()),
			current.getImage(),
			current.getEquipableSlots(),
			current.getWeight(),
			current.getMaxItemsPerStack(),
			current.getBaseCost(),
			current.getInvokedSpell(),
			current.getInvokedSpellLevel(),
			current.getCharges(),
			current.getChargesType(),
			current.getUsableByCharacterClass(),
			current.getUsableByRace(),
			current.getUsableByGender(),
			current.isQuestItem(),
			current.getCurseStrength(),
			current.getIdentificationDifficulty(),
			current.getRechargeDifficulty(),
			new StatModifier(current.getEquipRequirements()),
			new StatModifier(current.getUseRequirements()),
			current.getAttackScript(),
			current.getDamage(),
			current.getDefaultDamageType(),
			current.getAttackTypes(),
			current.isTwoHanded(),
			current.isRanged(),
			current.isReturning(),
			current.isBackstabCapable(),
			current.isSnipeCapable(), 
			current.getToHit(),
			current.getToPenetrate(),
			current.getToCritical(),
			current.getToInitiative(),
			current.getMinRange(),
			current.getMaxRange(),
			current.getAmmo(),
			current.getSpellEffects(),
			current.getBonusAttacks(),
			current.getBonusStrikes(),
			current.getDiscipline(),
			current.getSlaysFoeType(),
			current.getAmmoType(),
			current.getDamagePrevention(),
			current.getDamagePreventionChance(),
			current.getEnchantmentChance(),
			current.getEnchantmentCalculation(),
			current.getEnchantmentScheme(),
			current.getDisassemblyLootTable(),
			current.getConversionRate());

		Database.getInstance().getItemTemplates().put(newName, it);
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getItemTemplates().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public void commit(String name)
	{
		ItemTemplate it = Database.getInstance().getItemTemplate(name);

		it.setPluralName(pluralName.getText());
		it.setUnidentifiedName(unidentifiedName.getText());
		it.setType(type.getSelectedIndex());
		it.setSubtype(subType.getSelectedIndex());
		it.setDescription(description.getText());
		it.setImage(image.getText());
		it.setModifiers(modifiers.getModifier());
		it.setEquipableSlots(equipableSlots.getEquipableSlots());
		it.setWeight((Integer)weight.getValue());
		it.setUsableByCharacterClass(usableByCharacterClass.getAllowedCharacterClasses());
		it.setUsableByGender(usableByGender.getAllowedGenders());
		it.setUsableByRace(usableByRace.getAllowedRaces());
		it.setQuestItem(isQuestItem.isSelected());
		it.setCurseStrength((Integer)curseStrength.getValue());
		it.setMaxItemsPerStack((Integer)maxItemsPerStack.getValue());
		Double cr = (Double)conversionRate.getValue();
		float crf = cr.floatValue();
		it.setConversionRate(crf);
		it.setBaseCost((Integer)baseCost.getValue());
		if (invokedSpell.getSelectedItem().equals(NONE))
		{
			it.setInvokedSpell(null);
		}
		else
		{
			it.setInvokedSpell(Database.getInstance().getSpell((String)invokedSpell.getSelectedItem()));
		}
		it.setInvokedSpellLevel((Integer)invokedSpellLevel.getValue());
		it.setCharges(charges.getDice());
		it.setChargesType((ItemTemplate.ChargesType)chargesType.getSelectedItem());
		it.setIdentificationDifficulty((Integer)identificationDifficulty.getValue());
		it.setRechargeDifficulty((Integer)rechargeDifficulty.getValue());
		it.setEquipRequirements(equipRequirements.getModifier());
		it.setUseRequirements(useRequirements.getModifier());
		if (attackScript.getSelectedItem().equals(NONE))
		{
			it.setAttackScript(null);
		}
		else
		{
			it.setAttackScript(Database.getInstance().getScript((String)attackScript.getSelectedItem()));
		}
		it.setDamage(damage.getDice());
		it.setDefaultDamageType((MagicSys.SpellEffectType)defaultDamageType.getSelectedItem());
		it.setAttackTypes(attackTypes.getAttackTypes());
		it.setTwoHanded(isTwoHanded.isSelected());
		it.setRanged(isRanged.isSelected());
		it.setReturning(isReturning.isSelected());
		it.setBackstabCapable(isBackstabCapable.isSelected());
		it.setSnipeCapable(isSnipeCapable.isSelected());
		it.setToHit((Integer)toHit.getValue());
		it.setToPenetrate((Integer)toPenetrate.getValue());
		it.setToCritical((Integer)toCritical.getValue());
		it.setToInitiative((Integer)toInitiative.getValue());
		it.setMinRange(ItemTemplate.WeaponRange.valueOf((String)minRange.getSelectedItem()));
		it.setMaxRange(ItemTemplate.WeaponRange.valueOf((String)maxRange.getSelectedItem()));
		it.setAmmo(ammoTypes.getAmmoTypes());
		it.setSpellEffects(spellEffects.getGroupOfPossibilties());
		it.setBonusAttacks((Integer)bonusAttacks.getValue());
		it.setBonusStrikes((Integer)bonusStrikes.getValue());
		if (discipline.getSelectedItem().equals(NONE))
		{
			it.setDiscipline(null);
		}
		else
		{
			it.setDiscipline((String)discipline.getSelectedItem());
		}
		it.setSlaysFoeType(slaysFoeType.getSelectedItem().equals(NONE)?
			null:new TypeDescriptorImpl((String)slaysFoeType.getSelectedItem()));
		if (ammoType.getSelectedItem().equals(NONE))
		{
			it.setAmmoType(null);
		}
		else
		{
			it.setAmmoType(ItemTemplate.AmmoType.valueOf((String)ammoType.getSelectedItem()));
		}
		it.setDamagePrevention((Integer)damagePrevention.getValue());
		it.setDamagePreventionChance((Integer)damagePreventionChance.getValue());
		Object ies = enchantmentScheme.getSelectedItem();
		if (ies == NONE)
		{
			it.setEnchantmentScheme(null);
		}
		else
		{
			it.setEnchantmentScheme((String)ies);
		}
		it.setEnchantmentChance((Integer)enchantmentChance.getValue());
		it.setEnchantmentCalculation((ItemTemplate.EnchantmentCalculation)enchantmentCalculation.getSelectedItem());
		Object dlt = disassemblyLootTable.getSelectedItem();
		if (dlt == NONE)
		{
			it.setDisassemblyLootTable(null);
		}
		else
		{
			it.setDisassemblyLootTable((String)dlt);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == sortBy)
		{
			currentSortBy = (SortBy)sortBy.getSelectedItem();
			loadData();
			refreshNames(currentName);
		}
		else
		{
			super.actionPerformed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	private class ItemTypeComparator implements Comparator<String>
	{
		public int compare(String o1, String o2)
		{
			ItemTemplate item1 = Database.getInstance().getItemTemplate(o1);
			ItemTemplate item2 = Database.getInstance().getItemTemplate(o2);

			int i = item1.getType() - item2.getType();
			if (i != 0)
			{
				return i;
			}
			else
			{
				return item1.getName().compareTo(item2.getName());
			}
		}
	}
}
