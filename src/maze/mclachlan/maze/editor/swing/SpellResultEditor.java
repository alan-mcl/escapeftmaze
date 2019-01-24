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
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1SpellResult;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.TypeDescriptorImpl;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.data.v1.V1SpellResult.*;

/**
 *
 */
public class SpellResultEditor extends JDialog implements ActionListener
{
	private SpellResult result;

	private JButton ok, cancel;
	private JComboBox type;
	private JComboBox<String> foeType;
	private JTextField impl;
	private int dirtyFlag;
	private ValueComponent casterFatigueValue;
	private ValueComponent charmValue;
	private CardLayout cards;
	private JPanel controls;
	private ValueComponent hitPointDamageValue, fatigueDamageValue,
		actionDamageValue, magicDamageValue;
	private JCheckBox damageTransferToCaster;
	private JComboBox conditionTemplate;
	private ValueComponent hpHealing, stamHealing, apHealing, mpHealing;
	private ValueComponent mindReadValue;
	private ValueComponent mindReadFailedValue;
	private ValueComponent rechargeValue;
	private ValueComponent removeCurseValue;
	private ValueComponent theftValue;
	private ValueComponent theftFailedValue;
	private ValueComponent unlockValue;
	private JSpinner damageMultiplier;
	private ValueComponent identifyValue;
	private JCheckBox revealCurses;
	private ValueComponent summoningStrengthValue;
	private JComboBox[] encounterTables;
	private ValueComponent drainValue;
	private JComboBox drainModifier;
	private ValueComponent conditionRemovalStrength;
	private JCheckBox[] conditionEffects;
	private JCheckBox isDeliverCondition;
	private JCheckBox[] conditionTransferConditionEffects;
	private Map<String, JCheckBox> conditionEffectsBoxes = new HashMap<String, JCheckBox>();
	private Map<String, JCheckBox> conditionTransferConditionEffectsBoxes = new HashMap<String, JCheckBox>();
	private ValueComponent cloudSpellDuration, cloudSpellStrength;
	private JComboBox cloudSpellSpell;
	private JTextField cloudSpellIcon;
	private ValueComponent purifyAirStrength;
	private ValueComponent forgetStrength;
	private ValueComponent conditionIdentificationStrength;
	private JCheckBox canIdenfityConditionStrength;
	private ValueComponent weaponNrStrikes;
	private StatModifierComponent weaponModifiers;
	private JComboBox<MagicSys.SpellEffectType> weaponDamageType;
	private JComboBox<String> weaponAttackScript;
	private JComboBox<String> weaponAttackType;
	private JComboBox<String> requiredWeaponType;
	private JCheckBox weaponRequiresBackstab, weaponRequiresSnipe, consumesWeapon;
	private GroupOfPossibilitiesPanel spellEffects;
	private JComboBox<String> createItemLootTables;
	private JCheckBox equipItems;
	private ValueComponent locatePersonValue;
	private JComboBox<String> removeItemName;

	/*-------------------------------------------------------------------------*/
	public SpellResultEditor(
		Frame owner, 
		SpellResult spellResult, 
		int dirtyFlag) 
		throws HeadlessException
	{
		super(owner, "Edit Spell Result", true);
		this.dirtyFlag = dirtyFlag;

		JPanel top = new JPanel(new GridLayout(2,1));

		JPanel top1 = new JPanel();
		Vector<String> spellResultTypes = new Vector<String>();
		for (int i=0; i<MAX; i++)
		{
			spellResultTypes.addElement(describeType(i));
		}
		type = new JComboBox(spellResultTypes);
		type.addActionListener(this);
		top1.add(new JLabel("Type:"));
		top1.add(type);

		JPanel top2 = new JPanel();
		Vector<String> types = new Vector<String>();
		types.addAll(Database.getInstance().getCharacterClassList());
		types.addAll(Database.getInstance().getRaceList());
		types.addAll(Database.getInstance().getFoeTypes().keySet());
		Collections.sort(types);
		types.add(0, EditorPanel.NONE);

		foeType = new JComboBox<String>(types);
		top2.add(new JLabel("Foe Type:"));
		top2.add(foeType);

		top.add(top1);
		top.add(top2);

		cards = new CardLayout(3,3);
		controls = new JPanel(cards);
		for (int i=0; i<MAX; i++)
		{
			JPanel c = getControls(i);
			controls.add(c, String.valueOf(i));
		}

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);

		this.setLayout(new BorderLayout(3,3));
		this.add(top, BorderLayout.NORTH);
		this.add(controls, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);

		if (spellResult != null)
		{
			setState(spellResult);
		}

		this.pack();
		setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private void setState(SpellResult sr)
	{
		int srType;
		if (V1SpellResult.types.containsKey(sr.getClass()))
		{
			srType = V1SpellResult.types.get(sr.getClass());
		}
		else
		{
			srType = CUSTOM;
		}
		type.setSelectedIndex(srType);

		foeType.setSelectedItem(sr.getFoeType() == null ? EditorPanel.NONE : sr.getFoeType().getName());

		switch (srType)
		{
			case CUSTOM:
				impl.setText(sr.getClass().getName());
				break;
			case ATTACK_WITH_WEAPON:
				AttackWithWeaponSpellResult awwsr = (AttackWithWeaponSpellResult)sr;

				weaponAttackScript.setSelectedItem(
					awwsr.getAttackScript()==null? EditorPanel.NONE:awwsr.getAttackScript());
				weaponDamageType.setSelectedItem(awwsr.getDamageType());
				weaponModifiers.setModifier(awwsr.getModifiers());
				weaponNrStrikes.setValue(awwsr.getNrStrikes());
				weaponAttackType.setSelectedItem(
					awwsr.getAttackType() == null ? EditorPanel.NONE : awwsr.getAttackType().getName());
				weaponRequiresBackstab.setSelected(awwsr.isRequiresBackstabWeapon());
				weaponRequiresSnipe.setSelected(awwsr.isRequiresSnipeWeapon());
				requiredWeaponType.setSelectedItem(
					ItemTemplate.WeaponSubType.describe(awwsr.getRequiredWeaponType()));
				consumesWeapon.setSelected(awwsr.isConsumesWeapon());
				spellEffects.refresh(awwsr.getSpellEffects());

				break;
			case CHARM:
				CharmSpellResult csr = (CharmSpellResult)sr;
				charmValue.setValue(csr.getValue());
				break;
			case CONDITION:
				ConditionSpellResult condsr = (ConditionSpellResult)sr;
				conditionTemplate.setSelectedItem(condsr.getConditionTemplate().getName());
				break;
			case DAMAGE:
				DamageSpellResult dsr = (DamageSpellResult)sr;
				hitPointDamageValue.setValue(dsr.getHitPointDamage());
				fatigueDamageValue.setValue(dsr.getFatigueDamage());
				actionDamageValue.setValue(dsr.getActionPointDamage());
				magicDamageValue.setValue(dsr.getMagicPointDamage());
				damageMultiplier.setValue(dsr.getMultiplier());
				damageTransferToCaster.setSelected(dsr.transferToCaster());
				break;
			case HEALING:
				HealingSpellResult hsr = (HealingSpellResult)sr;
				hpHealing.setValue(hsr.getHitPointHealing());
				stamHealing.setValue(hsr.getStaminaHealing());
				apHealing.setValue(hsr.getActionPointHealing());
				mpHealing.setValue(hsr.getMagicPointHealing());
				break;
			case IDENTIFY:
				IdentifySpellResult isr = (IdentifySpellResult)sr;
				identifyValue.setValue(isr.getValue());
				revealCurses.setSelected(isr.revealCurses());
				break;
			case MIND_READ:
				MindReadSpellResult mrsr = (MindReadSpellResult)sr;
				mindReadValue.setValue(mrsr.getValue());
				break;
			case MIND_READ_FAILED:
				MindReadFailedSpellResult mrfsr = (MindReadFailedSpellResult)sr;
				mindReadFailedValue.setValue(mrfsr.getValue());
				break;
			case RECHARGE:
				RechargeSpellResult rsr = (RechargeSpellResult)sr;
				rechargeValue.setValue(rsr.getValue());
				break;
			case REMOVE_CURSE:
				RemoveCurseSpellResult rcsr = (RemoveCurseSpellResult)sr;
				removeCurseValue.setValue(rcsr.getValue());
				break;
			case SUMMONING:
				SummoningSpellResult ssr = (SummoningSpellResult)sr;
				summoningStrengthValue.setValue(ssr.getStrength());
				for (int i = 0; i < encounterTables.length; i++)
				{
					encounterTables[i].setSelectedItem(ssr.getEncounterTable()[i]);
				}
				break;
			case THEFT:
				TheftSpellResult tsr = (TheftSpellResult)sr;
				theftValue.setValue(tsr.getValue());
				break;
			case THEFT_FAILED:
				TheftFailedSpellResult tfsr = (TheftFailedSpellResult)sr;
				theftFailedValue.setValue(tfsr.getValue());
				break;
			case UNLOCK:
				UnlockSpellResult usr = (UnlockSpellResult)sr;
				unlockValue.setValue(usr.getValue());
				break;
			case DRAIN:
				DrainSpellResult drainSR = (DrainSpellResult)sr;
				drainValue.setValue(drainSR.getDrain());
				drainModifier.setSelectedItem(drainSR.getModifier());
				break;
			case CONDITION_REMOVAL:
				ConditionRemovalSpellResult crsr = (ConditionRemovalSpellResult)sr;
				conditionRemovalStrength.setValue(crsr.getStrength());
				
				for (ConditionEffect ce : crsr.getEffects())
				{
					conditionEffectsBoxes.get(ce.getName()).setSelected(true);
				}
				break;
			case CONDITION_TRANSFER:
				ConditionTransferSpellResult ctsr = (ConditionTransferSpellResult)sr;
				isDeliverCondition.setSelected(ctsr.isDeliver());
				for (ConditionEffect ce : ctsr.getEffects())
				{
					conditionTransferConditionEffectsBoxes.get(ce.getName()).setSelected(true);
				}
				break;
			case DEATH:
				DeathSpellResult d = (DeathSpellResult)sr;
				break;
			case CLOUD_SPELL:
				CloudSpellResult clsr = (CloudSpellResult)sr;
				cloudSpellDuration.setValue(clsr.getDuration());
				cloudSpellStrength.setValue(clsr.getStrength());
				cloudSpellIcon.setText(clsr.getIcon());
				cloudSpellSpell.setSelectedItem(clsr.getSpell());
				break;
			case PURIFY_AIR:
				PurifyAirSpellResult pasr = (PurifyAirSpellResult)sr;
				purifyAirStrength.setValue(pasr.getStrength());
				break;
			case RESURRECTION:
				ResurrectionSpellResult resr = (ResurrectionSpellResult)sr;
				break;
			case BOOZE:
				BoozeSpellResult bsr = new BoozeSpellResult();
				break;
			case FORGET :
				ForgetSpellResult fsr = (ForgetSpellResult)sr;
				forgetStrength.setValue(fsr.getStrength());
				break;
			case CONDITION_IDENTIFICATION:
				ConditionIdentificationSpellResult cisr = (ConditionIdentificationSpellResult)sr;
				conditionIdentificationStrength.setValue(cisr.getStrength());
				canIdenfityConditionStrength.setSelected(cisr.isCanIdentifyConditionStrength());
				break;
			case CREATE_ITEM:
				CreateItemSpellResult createItemSpellResult = (CreateItemSpellResult)sr;
				createItemLootTables.setSelectedItem(createItemSpellResult.getLootTable());
				equipItems.setSelected(createItemSpellResult.isEquipItems());
				break;
			case LOCATE_PERSON:
				LocatePersonSpellResult locatePersonSpellResult = (LocatePersonSpellResult)sr;
				locatePersonValue.refresh(locatePersonSpellResult.getValue());
				break;
			case REMOVE_ITEM:
				RemoveItemSpellResult removeItemSpellResult = (RemoveItemSpellResult)sr;
				removeItemName.setSelectedItem(removeItemSpellResult.getItemName());
				break;
			case SINGLE_USE_SPELL:
				SingleUseSpellSpellResult singleUseSpellSpellResult = (SingleUseSpellSpellResult)sr;
				break;

			default: throw new MazeException("Invalid type "+srType);
		}
	}

	/*-------------------------------------------------------------------------*/
	JPanel getControls(int type)
	{
		switch (type)
		{
			case CUSTOM:
				return getCustomPanel();
			case ATTACK_WITH_WEAPON:
				return getAttackWithWeaponPanel();
			case CHARM:
				return getCharmPanel();
			case CONDITION:
				return getConditionPanel();
			case DAMAGE:
				return getDamagePanel();
			case HEALING:
				return getHealingPanel();
			case IDENTIFY:
				return getIdentifyPanel();
			case MIND_READ:
				return getMindReadPanel();
			case MIND_READ_FAILED:
				return getMindReadFailedPanel();
			case RECHARGE:
				return getRechargePanel();
			case REMOVE_CURSE:
				return getRemoveCursePanel();
			case SUMMONING:
				return getSummoningPanel();
			case THEFT:
				return getTheftPanel();
			case THEFT_FAILED:
				return getTheftFailedPanel();
			case UNLOCK:
				return getUnlockPanel();
			case DRAIN:
				return getDrainPanel();
			case SINGLE_USE_SPELL:
				return new JPanel();
			case CONDITION_REMOVAL:
				return getConditionRemovalPanel();
			case CONDITION_TRANSFER:
				return getConditionTransferPanel();
			case DEATH:
				return new JPanel();
			case CLOUD_SPELL:
				return getCloudSpellPanel();
			case PURIFY_AIR:
				return getPurifyAirPanel();
			case RESURRECTION:
				return new JPanel();
			case BOOZE:
				return new JPanel();
			case FORGET:
				return getForgetPanel();
			case CONDITION_IDENTIFICATION:
				return getConditionIdentificationPanel();
			case CREATE_ITEM:
				return getCreateItemPanel();
			case LOCATE_PERSON:
				return getLocatePersonPanel();
			case REMOVE_ITEM:
				return getRemoveItemPanel();
			default:
				throw new MazeException("Invalid type " + type);
		}
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getRemoveItemPanel()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getItemTemplates().keySet());
		Collections.sort(vec);
		removeItemName = new JComboBox<String>(vec);

		return dirtyGridLayoutCrap(new JLabel("Item:"), removeItemName);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getLocatePersonPanel()
	{
		locatePersonValue = new ValueComponent(dirtyFlag);

		return dirtyGridLayoutCrap(new JLabel("Value:"), locatePersonValue);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCreateItemPanel()
	{
		Vector<String> vec = new Vector<String>();
		vec.addAll(Database.getInstance().getLootTables().keySet());
		Collections.sort(vec);
		createItemLootTables = new JComboBox<String>(vec);
		equipItems = new JCheckBox("Equip/Add to Inventory?");

		return dirtyGridLayoutCrap(
			new JLabel("Loot Table:"), createItemLootTables,
			new JLabel(), equipItems);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getAttackWithWeaponPanel()
	{
		weaponModifiers = new StatModifierComponent(dirtyFlag);
		weaponNrStrikes = new ValueComponent(dirtyFlag);

		Vector<MagicSys.SpellEffectType> vec = new Vector<MagicSys.SpellEffectType>();
		vec.addAll(Arrays.asList(MagicSys.SpellEffectType.values()));
		weaponDamageType = new JComboBox<MagicSys.SpellEffectType>(vec);

		Vector<String> scripts = new Vector<String>();
		scripts.addAll(new ArrayList<String>(
			Database.getInstance().getMazeScripts().keySet()));
		Collections.sort(scripts);
		scripts.add(0, EditorPanel.NONE);
		weaponAttackScript = new JComboBox(scripts);

		Vector<String> attackTypes = new Vector<String>();
		attackTypes.addAll(new ArrayList<String>(
			Database.getInstance().getAttackTypes().keySet()));
		Collections.sort(attackTypes);
		attackTypes.add(0, EditorPanel.NONE);
		weaponAttackType = new JComboBox<String>(attackTypes);

		weaponRequiresBackstab = new JCheckBox("Requires backstab weapon?");
		weaponRequiresSnipe = new JCheckBox("Requires snipe weapon?");

		Vector<String> weaponTypes = new Vector<String>();
		weaponTypes.addAll(ItemTemplate.WeaponSubType.values());

		requiredWeaponType = new JComboBox<String>(weaponTypes);

		consumesWeapon = new JCheckBox("Consumes weapon?");

		spellEffects = new GroupOfPossibilitiesPanel(
			SwingEditor.Tab.SPELL_EFFECTS, 0.5);
		List<String> spellEffectNames = new ArrayList<String>(
			Database.getInstance().getSpellEffects().keySet());
		spellEffects.initForeignKeys(spellEffectNames);

		return dirtyGridLayoutCrap(
			new JLabel("Modifiers:"), weaponModifiers,
			new JLabel("Nr Strikes:"), weaponNrStrikes,
			new JLabel("Attack Type:"), weaponAttackType,
			new JLabel("Damage Type:"), weaponDamageType,
			new JLabel("Attack Script:"), weaponAttackScript,
			new JLabel("Required Weapon Type"), requiredWeaponType,
			new JLabel(), weaponRequiresBackstab,
			new JLabel(), weaponRequiresSnipe,
			new JLabel(), consumesWeapon,
			new JLabel("Spell Effects:"), spellEffects
			);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getConditionIdentificationPanel()
	{
		conditionIdentificationStrength = new ValueComponent(this.dirtyFlag);
		canIdenfityConditionStrength = new JCheckBox("Can Identify Condition Strength?");
		return dirtyGridLayoutCrap(
			new JLabel("Strength:"), conditionIdentificationStrength,
			canIdenfityConditionStrength, new JLabel());
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getForgetPanel()
	{
		forgetStrength = new ValueComponent(this.dirtyFlag);
		return dirtyGridLayoutCrap(new JLabel("Strength:"), forgetStrength);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getPurifyAirPanel()
	{
		purifyAirStrength = new ValueComponent(this.dirtyFlag);
		return dirtyGridLayoutCrap(new JLabel("Strength:"), purifyAirStrength);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCloudSpellPanel()
	{
		cloudSpellDuration = new ValueComponent(this.dirtyFlag);
		cloudSpellStrength = new ValueComponent(this.dirtyFlag);
		cloudSpellIcon = new JTextField(30);

		Vector<String> vec = new Vector<String>(Database.getInstance().getSpellList());
		Collections.sort(vec);
		cloudSpellSpell = new JComboBox(vec);

		return dirtyGridLayoutCrap(
			new JLabel("Duration:"), cloudSpellDuration,
			new JLabel("Strength:"), cloudSpellStrength,
			new JLabel("Icon:"), cloudSpellIcon,
			new JLabel("Spell:"), cloudSpellSpell);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getConditionRemovalPanel()
	{
		List<String> ce = new ArrayList<String>(Database.getInstance().getConditionEffects().keySet());
		Collections.sort(ce);
		
		conditionRemovalStrength = new ValueComponent(dirtyFlag);
		conditionEffects = new JCheckBox[ce.size()];
		
		JPanel top = new JPanel();
		top.add(new JLabel("Strength:"));
		top.add(conditionRemovalStrength);
		
		JPanel boxes = new JPanel(new GridLayout(ce.size()/3 +4, 3));
		for (int i=0; i<conditionEffects.length; i++)
		{
			String name = ce.get(i);
			conditionEffects[i] = new JCheckBox(name);
			boxes.add(conditionEffects[i]);
			conditionEffectsBoxes.put(name, conditionEffects[i]);
		}
		
		JPanel result = new JPanel(new BorderLayout());
		
		result.add(top, BorderLayout.NORTH);
		result.add(boxes, BorderLayout.CENTER);
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getConditionTransferPanel()
	{
		List<String> ce = new ArrayList<String>(Database.getInstance().getConditionEffects().keySet());
		Collections.sort(ce);

		isDeliverCondition = new JCheckBox("Deliver?");
		conditionTransferConditionEffects = new JCheckBox[ce.size()];

		JPanel top = new JPanel();
		top.add(isDeliverCondition);

		JPanel boxes = new JPanel(new GridLayout(ce.size()/3 +4, 3));
		for (int i=0; i<conditionTransferConditionEffects.length; i++)
		{
			String name = ce.get(i);
			conditionTransferConditionEffects[i] = new JCheckBox(name);
			boxes.add(conditionTransferConditionEffects[i]);
			conditionTransferConditionEffectsBoxes.put(name, conditionTransferConditionEffects[i]);
		}

		JPanel result = new JPanel(new BorderLayout());

		result.add(top, BorderLayout.NORTH);
		result.add(boxes, BorderLayout.CENTER);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getDrainPanel()
	{
		drainValue = new ValueComponent(dirtyFlag);
		Vector<Stats.Modifier> vec = new Vector<Stats.Modifier>(Stats.allModifiers);
		drainModifier = new JComboBox(vec);
		
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		result.add(new JLabel("Drain Amount: "), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		result.add(drainValue, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		result.add(new JLabel("Modifier: "), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		result.add(drainModifier, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.weighty = 1;
		result.add(new JLabel(), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		result.add(new JLabel(), gbc);
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getUnlockPanel()
	{
		unlockValue = new ValueComponent(dirtyFlag);
		return dirtyGridLayoutCrap(new JLabel("Unlock Strength: "), unlockValue);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getTheftFailedPanel()
	{
		theftFailedValue = new ValueComponent(dirtyFlag);
		return dirtyGridLayoutCrap(new JLabel("Theft Failure: "), theftFailedValue);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getTheftPanel()
	{
		theftValue = new ValueComponent(dirtyFlag);
		return dirtyGridLayoutCrap(new JLabel("Theft Strength: "), theftValue);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getSummoningPanel()
	{
		JPanel result = new JPanel(new GridLayout(11, 2, 3, 3));

		result.add(new JLabel("Summoning Strength:"));
		summoningStrengthValue = new ValueComponent(dirtyFlag);
		result.add(summoningStrengthValue);

		Vector<String> vec = new Vector<String>(Database.getInstance().getEncounterTables().keySet());
		Collections.sort(vec);
		encounterTables = new JComboBox[10];
		for (int i = 0; i < encounterTables.length; i++)
		{
			encounterTables[i] = new JComboBox(vec);
			result.add(new JLabel("Encounter Table "+i));
			result.add(encounterTables[i]);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getRemoveCursePanel()
	{
		removeCurseValue = new ValueComponent(dirtyFlag);
		return dirtyGridLayoutCrap(new JLabel("Remove Curse Strength: "), removeCurseValue);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getRechargePanel()
	{
		rechargeValue = new ValueComponent(dirtyFlag);
		return dirtyGridLayoutCrap(new JLabel("Recharge Strength: "), rechargeValue);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getMindReadFailedPanel()
	{
		mindReadFailedValue = new ValueComponent(dirtyFlag);
		return dirtyGridLayoutCrap(new JLabel("Mind Read Failure: "), mindReadFailedValue);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getMindReadPanel()
	{
		mindReadValue = new ValueComponent(dirtyFlag);
		return dirtyGridLayoutCrap(new JLabel("Mind Read Strength: "), mindReadValue);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getIdentifyPanel()
	{
		identifyValue = new ValueComponent(dirtyFlag);
		revealCurses = new JCheckBox("Reveal Curses?");

		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		result.add(new JLabel("Identify Strength: "), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		result.add(identifyValue, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		result.add(revealCurses, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getHealingPanel()
	{
		hpHealing = new ValueComponent(dirtyFlag);
		stamHealing = new ValueComponent(dirtyFlag);
		apHealing = new ValueComponent(dirtyFlag);
		mpHealing = new ValueComponent(dirtyFlag);
		return dirtyGridLayoutCrap(
			new JLabel("Hit Point Healing: "), hpHealing,
			new JLabel("Stamina Restore: "), stamHealing,
			new JLabel("Action Point Restore: "), apHealing,
			new JLabel("Magic Point Restore: "), mpHealing);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getDamagePanel()
	{
		hitPointDamageValue = new ValueComponent(dirtyFlag);
		fatigueDamageValue = new ValueComponent(dirtyFlag);
		actionDamageValue = new ValueComponent(dirtyFlag);
		magicDamageValue = new ValueComponent(dirtyFlag);
		damageMultiplier = new JSpinner(new SpinnerNumberModel(0.0, -127.0, 127.0, 0.1));
		damageTransferToCaster = new JCheckBox("Transfer to Caster?");

		return dirtyGridLayoutCrap(
			new JLabel("Hit Point Damage: "), hitPointDamageValue,
			new JLabel("Stamina Damage: "), fatigueDamageValue,
			new JLabel("Action Point Damage: "), actionDamageValue,
			new JLabel("Magic Point Damage: "), magicDamageValue,
			new JLabel("Damage Multiplier: "), damageMultiplier,
			damageTransferToCaster, new JLabel(""));
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getConditionPanel()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getConditionTemplates().keySet());
		Collections.sort(vec);
		conditionTemplate = new JComboBox(vec);
		return dirtyGridLayoutCrap(new JLabel("Condition Template: "), conditionTemplate);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCharmPanel()
	{
		charmValue = new ValueComponent(dirtyFlag);
		return dirtyGridLayoutCrap(new JLabel("Charm Strength: "), charmValue);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCustomPanel()
	{
		impl = new JTextField(20);
		return dirtyGridLayoutCrap(new JLabel("Custom Impl: "), impl);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel dirtyGridLayoutCrap(JLabel label, Component field)
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		result.add(label, gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		result.add(field, gbc);
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	private JPanel dirtyGridLayoutCrap(Component... comps)
	{
		JPanel panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		for (int i=0; i<comps.length; i+=2)
		{
			if (i == comps.length-2)
			{
				gbc.weighty = 1;
			}
			gbc.gridx = 0;
			gbc.weightx = 0;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 2;
				gbc.weightx = 1;
			}
			panel.add(comps[i], gbc);
			gbc.gridx = 1;
			gbc.weightx = 1;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 1;
			}
			else
			{
				panel.add(comps[i+1], gbc);
			}
			gbc.gridy++;
		}
		
		return panel;
	}

	/*-------------------------------------------------------------------------*/
	static String describeType(int type)
	{
		switch (type)
		{
			case CUSTOM: return "Custom";
			case ATTACK_WITH_WEAPON: return "Attack With Weapon";
			case CHARM: return "Charm";
			case CONDITION: return "Condition";
			case DAMAGE: return "Damage";
			case HEALING: return "Healing";
			case IDENTIFY: return "Identify";
			case MIND_READ: return "Mind Read";
			case MIND_READ_FAILED: return "Mind Read Failed";
			case RECHARGE: return "Recharge";
			case REMOVE_CURSE: return "Remove Curse";
			case SUMMONING: return "Summoning";
			case THEFT: return "Theft";
			case THEFT_FAILED: return "Theft Failed";
			case UNLOCK: return "Unlock";
			case DRAIN: return "Drain";
			case SINGLE_USE_SPELL: return "Single Use Spell";
			case CONDITION_REMOVAL: return "Condition Removal/Curing";
			case CONDITION_TRANSFER: return "Condition Transfer";
			case DEATH: return "Death";
			case CLOUD_SPELL: return "Cloud Spell";
			case PURIFY_AIR: return "Purify Air";
			case RESURRECTION: return "Resurrection";
			case BOOZE: return "Booze";
			case FORGET: return "Forget";
			case CONDITION_IDENTIFICATION: return "Condition Identification";
			case CREATE_ITEM: return "Create Item";
			case LOCATE_PERSON: return "Locate Person";
			case REMOVE_ITEM: return "Remove Item";
			default: throw new MazeException("Invalid type "+type);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == type)
		{
			cards.show(controls, String.valueOf(type.getSelectedIndex()));
		}
		else if (e.getSource() == ok)
		{
			// save changes
			setResult();
			if (SwingEditor.instance != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
			}
			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			// discard changes
			setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	public SpellResult getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void setResult()
	{
		int srType = type.getSelectedIndex();
		switch (srType)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(impl.getText());
					this.result = (SpellResult)clazz.newInstance();
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
				break;
			case ATTACK_WITH_WEAPON:

				StatModifier modifier = weaponModifiers.getModifier();
				ValueList nrStrikes = weaponNrStrikes.getValue();

				MagicSys.SpellEffectType damageType = (MagicSys.SpellEffectType)weaponDamageType.getSelectedItem();
				if (damageType == MagicSys.SpellEffectType.NONE)
				{
					damageType = null;
				}

				String attackScript = (String)weaponAttackScript.getSelectedItem();
				if (EditorPanel.NONE.equals(attackScript))
				{
					attackScript = null;
				}

				String attackType = (String)weaponAttackType.getSelectedItem();
				if (EditorPanel.NONE.equals(attackType))
				{
					attackType = null;
				}

				AttackType at = attackType==null? null:Database.getInstance().getAttackType(attackType);
				result = new AttackWithWeaponSpellResult(
					nrStrikes,
					modifier,
					at,
					damageType,
					attackScript,
					weaponRequiresBackstab.isSelected(),
					weaponRequiresSnipe.isSelected(),
					consumesWeapon.isSelected(),
					ItemTemplate.WeaponSubType.valueOf((String)requiredWeaponType.getSelectedItem()),
					spellEffects.getGroupOfPossibilties());
				break;
			case CHARM:
				result = new CharmSpellResult(charmValue.getValue());
				break;
			case CONDITION:
				String ctName = (String)conditionTemplate.getSelectedItem();
				ConditionTemplate ct = Database.getInstance().getConditionTemplate(ctName);
				result = new ConditionSpellResult(ct);
				break;
			case DAMAGE:
				result = new DamageSpellResult(
					hitPointDamageValue.getValue(), 
					fatigueDamageValue.getValue(), 
					actionDamageValue.getValue(),
					magicDamageValue.getValue(), 
					(Double)damageMultiplier.getValue(),
					damageTransferToCaster.isSelected());
				break;
			case HEALING:
				result = new HealingSpellResult(
					hpHealing.getValue(), 
					stamHealing.getValue(), 
					apHealing.getValue(),
					mpHealing.getValue());
				break;
			case IDENTIFY:
				result = new IdentifySpellResult(identifyValue.getValue(), revealCurses.isSelected()); break;
			case MIND_READ:
				result = new MindReadSpellResult(mindReadValue.getValue()); break;
			case MIND_READ_FAILED:
				result = new MindReadFailedSpellResult(mindReadFailedValue.getValue()); break;
			case RECHARGE:
				result = new RechargeSpellResult(rechargeValue.getValue()); break;
			case REMOVE_CURSE:
				result = new RemoveCurseSpellResult(removeCurseValue.getValue()); break;
			case SUMMONING:
				String[] tableNames = new String[10];
				for (int i = 0; i < tableNames.length; i++)
				{
					tableNames[i] = (String)encounterTables[i].getSelectedItem();
				}
				result = new SummoningSpellResult(tableNames, summoningStrengthValue.getValue());
				break;
			case THEFT:
				result = new TheftSpellResult(theftValue.getValue()); break;
			case THEFT_FAILED:
				result = new TheftFailedSpellResult(theftFailedValue.getValue()); break;
			case UNLOCK:
				result = new UnlockSpellResult(unlockValue.getValue()); break;
			case DRAIN:
				result = new DrainSpellResult(drainValue.getValue(), (Stats.Modifier)drainModifier.getSelectedItem()); break;
			case CONDITION_REMOVAL:
				List<ConditionEffect> effects = new ArrayList<ConditionEffect>();
				for (JCheckBox cb : conditionEffects)
				{
					if (cb.isSelected())
					{
						effects.add(Database.getInstance().getConditionEffect(cb.getText()));
					}
				}
				result = new ConditionRemovalSpellResult(effects, conditionRemovalStrength.getValue());
				break;
			case CONDITION_TRANSFER:
				List<ConditionEffect> effectsList = new ArrayList<ConditionEffect>();
				for (JCheckBox cb : conditionTransferConditionEffects)
				{
					if (cb.isSelected())
					{
						effectsList.add(Database.getInstance().getConditionEffect(cb.getText()));
					}
				}
				result = new ConditionTransferSpellResult(effectsList, isDeliverCondition.isSelected());
				break;
			case DEATH:
				result = new DeathSpellResult();
				break;
			case CLOUD_SPELL:
				result = new CloudSpellResult(
					cloudSpellDuration.getValue(),
					cloudSpellStrength.getValue(),
					cloudSpellIcon.getText(),
					(String)cloudSpellSpell.getSelectedItem());
				break;
			case PURIFY_AIR:
				result = new PurifyAirSpellResult(purifyAirStrength.getValue());
				break;
			case RESURRECTION:
				result = new ResurrectionSpellResult();
				break;
			case BOOZE:
				result = new BoozeSpellResult();
				break;
			case FORGET:
				result = new ForgetSpellResult(forgetStrength.getValue());
				break;
			case CONDITION_IDENTIFICATION:
				result = new ConditionIdentificationSpellResult(
					conditionIdentificationStrength.getValue(),
					canIdenfityConditionStrength.isSelected());
				break;
			case CREATE_ITEM:
				result = new CreateItemSpellResult(
					(String)createItemLootTables.getSelectedItem(),
					equipItems.isSelected());
				break;
			case LOCATE_PERSON:
				result = new LocatePersonSpellResult(
					locatePersonValue.getValue());
				break;
			case REMOVE_ITEM:
				result = new RemoveItemSpellResult((String)removeItemName.getSelectedItem());
				break;
			case SINGLE_USE_SPELL:
				result = new SingleUseSpellSpellResult();
				break;
			default: throw new MazeException("Invalid type "+srType);
		}

		result.setFoeType(foeType.getSelectedItem() == EditorPanel.NONE ?
			null : new TypeDescriptorImpl((String)foeType.getSelectedItem()));
	}
}
