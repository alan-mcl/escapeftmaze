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

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Item implements AttackWith, SpellTarget
{
	private int cursedState = CursedState.UNDISCOVERED;
	private int identificationState = IdentificationState.UNIDENTIFIED;
	private ItemTemplate template;
	private CurMax stack, charges;
	private ItemEnchantment enchantment;

	/*-------------------------------------------------------------------------*/
	public Item(
		ItemTemplate template,
		int cursedState,
		int identificationState,
		CurMax stack,
		CurMax charges,
		ItemEnchantment enchantment)
	{
		this.charges = charges;
		this.cursedState = cursedState;
		this.identificationState = identificationState;
		this.stack = stack;
		this.template = template;
		this.enchantment = enchantment;
	}

	/*-------------------------------------------------------------------------*/
	public Item(ItemTemplate template)
	{
		this(template, template.maxItemsPerStack);
	}

	/*-------------------------------------------------------------------------*/
	public Item(ItemTemplate template, int quantity)
	{
		this.template = template;
		stack = new CurMax(quantity, template.maxItemsPerStack);
		if (template.charges != null)
		{
			charges = new CurMax(template.charges.roll("item ["+getName()+"] charges"), template.charges.getMaxPossible());
		}

		if (Maze.getInstance() != null && GameSys.getInstance().applyEnchantmentToNewItem(template))
		{
			// item pick up an enchantment
			ItemEnchantments itemEnchantments =
				Database.getInstance().getItemEnchantments().get(template.enchantmentScheme);
			this.enchantment = itemEnchantments.getEnchantments().getRandomItem();
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public Item(Item i)
	{
		this.cursedState = i.cursedState;
		this.identificationState = i.identificationState;
		this.template = i.template;
		if (i.stack != null)
		{
			this.stack = new CurMax(i.stack);
		}
		if (i.charges != null)
		{
			this.charges = new CurMax(i.charges);
		}
		this.enchantment = i.enchantment;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A display name for this item that varies with it's identification state.
	 */
	public String getDisplayName()
	{
		String result;
		switch(getIdentificationState())
		{
			case Item.IdentificationState.IDENTIFIED:

				if (isStackable() && getStack().getCurrent() > 1)
				{
					result = template.getPluralName();
				}
				else
				{
					result = template.getName();
				}

				if (enchantment != null)
				{
					if (enchantment.getPrefix() != null)
					{
						result = enchantment.getPrefix() + " "+  result;
					}
					if (enchantment.getSuffix() != null)
					{
						result = result + " " + enchantment.getSuffix();
					}
				}
				break;
			case Item.IdentificationState.UNIDENTIFIED:
				result = getUnidentifiedName();
				break;
			default: throw new MazeException("Invalid item identification state: "+
				getIdentificationState());
		}
		
		return result;
	}

	@Override
	public int getModifier(Stats.Modifier modifier)
	{
		return 0;
	}

	public int getToHit()
	{
		return template.toHit;
	}

	public int getToPenetrate()
	{
		return template.toPenetrate;
	}

	public int getToCritical()
	{
		return template.toCritical;
	}
	
	public int getToInitiative()
	{
		return template.toInitiative;
	}

	@Override
	public int getWeaponType()
	{
		return template.subtype;
	}

	@Override
	public int getActionPointCost(UnifiedActor defender)
	{
		return 0;
	}

	public Dice getDamage()
	{
		return template.damage;
	}

	public MagicSys.SpellEffectType getDefaultDamageType()
	{
		return template.getDefaultDamageType();
	}

	public String describe(AttackEvent e)
	{
		return ItemTemplate.describeWeapon(e);
	}

	public String[] getAttackTypes()
	{
		return template.attackTypes;
	}

	public int getMaxRange()
	{
		return template.maxRange;
	}

	public int getMinRange()
	{
		return template.minRange;
	}

	public String getName()
	{
		return template.name;
	}

	public String getUnidentifiedName()
	{
		return template.unidentifiedName;
	}

	public String getImage()
	{
		return template.image;
	}

	public String getDescription()
	{
		return template.description;
	}

	public BitSet getEquipableSlots()
	{
		return template.equipableSlots;
	}

	public Set<EquipableSlot.Type> getEquipableSlotTypes()
	{
		Set<EquipableSlot.Type> result = new HashSet<EquipableSlot.Type>();

		BitSet slots = getEquipableSlots();

		if (slots != null && !slots.isEmpty())
		{
			if (slots.get(PlayerCharacter.EquipableSlots.PRIMARY_WEAPON)) {result.add(EquipableSlot.Type.PRIMARY_WEAPON);}
			if (slots.get(PlayerCharacter.EquipableSlots.SECONDARY_WEAPON)) {result.add(EquipableSlot.Type.SECONDARY_WEAPON);}
			if (slots.get(PlayerCharacter.EquipableSlots.HELM)) {result.add(EquipableSlot.Type.HELM);}
			if (slots.get(PlayerCharacter.EquipableSlots.TORSO_ARMOUR)) {result.add(EquipableSlot.Type.TORSO_ARMOUR);}
			if (slots.get(PlayerCharacter.EquipableSlots.LEG_ARMOUR)) {result.add(EquipableSlot.Type.LEG_ARMOUR);}
			if (slots.get(PlayerCharacter.EquipableSlots.GLOVES)) {result.add(EquipableSlot.Type.GLOVES);}
			if (slots.get(PlayerCharacter.EquipableSlots.BOOTS)) {result.add(EquipableSlot.Type.BOOTS);}
			if (slots.get(PlayerCharacter.EquipableSlots.MISC_ITEM_1)) {result.add(EquipableSlot.Type.MISC_ITEM);}
			if (slots.get(PlayerCharacter.EquipableSlots.BANNER_ITEM)) {result.add(EquipableSlot.Type.BANNER_ITEM);}
		}

		return result;
	}

	public Set<String> getUsableByCharacterClass()
	{
		return template.usableByCharacterClass;
	}

	public Set<String> getUsableByGender()
	{
		return template.usableByGender;
	}

	public Set<String> getUsableByRace()
	{
		return template.usableByRace;
	}

	public int getWeight()
	{
		return template.weight * stack.getCurrent();
	}

	public CurMax getStack()
	{
		return stack;
	}

	public int getBaseCost()
	{
		return template.baseCost;
	}

	public boolean isStackable()
	{
		return stack != null && stack.getMaximum() > 1;
	}

	public Spell getInvokedSpell()
	{
		return template.invokedSpell;
	}

	public int getInvokedSpellLevel()
	{
		return template.invokedSpellLevel;
	}

	public ItemTemplate.ChargesType getChargesType()
	{
		return template.chargesType;
	}

	public CurMax getCharges()
	{
		return charges;
	}

	public int getType()
	{
		return template.type;
	}

	public int getSubType()
	{
		return template.subtype;
	}

	public boolean isQuestItem()
	{
		return template.questItem;
	}

	public boolean isCursed()
	{
		return template.curseStrength > 0;
	}

	public int getCurseStrengh()
	{
		return template.curseStrength;
	}

	public int getIdentificationDifficulty()
	{
		return template.identificationDifficulty;
	}

	public int getRechargeDifficulty()
	{
		return template.rechargeDifficulty;
	}

	public StatModifier getEquipRequirements()
	{
		return template.equipRequirements;
	}

	public StatModifier getUseRequirements()
	{
		return template.getUseRequirements();
	}

	public MazeScript getAttackScript()
	{
		return template.attackScript;
	}

	public ItemTemplate getTemplate()
	{
		return template;
	}

	public void setCharges(CurMax charges)
	{
		this.charges = charges;
	}

	public void setStack(CurMax stack)
	{
		this.stack = stack;
	}

	public void setTemplate(ItemTemplate template)
	{
		this.template = template;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A constant from {@link Item.CursedState}.
	 */
	public int getCursedState()
	{
		return this.cursedState;
	}

	/**
	 * @param state
	 * 	A constant from {@link Item.CursedState}.
	 */
	public void setCursedState(int state)
	{
		this.cursedState = state;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A constant from {@link Item.IdentificationState}.
	 */
	public int getIdentificationState()
	{
		return identificationState;
	}

	/**
	 * @param state
	 * 	A constant from {@link Item.IdentificationState}.
	 */
	public void setIdentificationState(int state)
	{
		this.identificationState = state;
	}

	public StatModifier getModifiers()
	{
		StatModifier unifiedModifiers = new StatModifier(template.modifiers);
		if (enchantment != null)
		{
			unifiedModifiers.addModifiers(enchantment.getModifiers());
		}
		return unifiedModifiers;
	}

	public boolean isWeapon()
	{
		return getType() == ItemTemplate.Type.SHORT_WEAPON ||
			getType() == ItemTemplate.Type.EXTENDED_WEAPON ||
			getType() == ItemTemplate.Type.RANGED_WEAPON ||
			getType() == ItemTemplate.Type.THROWN_WEAPON;
	}

	public int getDamagePreventionChance()
	{
		return template.damagePreventionChance;
	}

	public int getDamagePrevention()
	{
		return template.damagePrevention;
	}

	public int getBonusAttacks()
	{
		return template.bonusAttacks;
	}

	public int getBonusStrikes()
	{
		return template.bonusStrikes;
	}

	public List<ItemTemplate.AmmoType> getAmmoRequired()
	{
		return template.ammo;
	}

	public ItemTemplate.AmmoType isAmmoType()
	{
		return template.ammoType;
	}

	public boolean isRanged()
	{
		return template.isRanged;
	}

	public GroupOfPossibilities<SpellEffect> getSpellEffects()
	{
		return template.spellEffects;
	}

	public int getSpellEffectLevel()
	{
		return template.invokedSpellLevel;
	}

	public TypeDescriptor slaysFoeType()
	{
		return template.slaysFoeType;
	}

	public boolean isAmmo()
	{
		return template.type == ItemTemplate.Type.AMMUNITION;
	}

	public boolean isTwoHanded()
	{
		return template.twoHanded;
	}
	
	public boolean isReturning()
	{
		return template.isReturning;
	}

	public boolean isBackstabCapable()
	{
		return template.isBackstabCapable;
	}

	public boolean isSnipeCapable()
	{
		return template.isSnipeCapable;
	}
	
	public Stats.Modifier getDiscipline()
	{
		return template.discipline;
	}

	public float getConversionRate()
	{
		return template.getConversionRate();
	}

	public int applyConversionRate()
	{
		return (int)(getStack().getCurrent() * getConversionRate());
	}
	
	public boolean isArmour()
	{
		return template.type == ItemTemplate.Type.HEAD_ARMOUR ||
			template.type == ItemTemplate.Type.TORSO_ARMOUR ||
			template.type == ItemTemplate.Type.LEG_ARMOUR ||
			template.type == ItemTemplate.Type.BOOTS ||
			template.type == ItemTemplate.Type.GLOVES;
	}

	public boolean isShield()
	{
		return template.type == ItemTemplate.Type.SHIELD;
	}

	public ItemEnchantment getEnchantment()
	{
		return enchantment;
	}

	public String getDisassemblyLootTable()
	{
		return template.getDisassemblyLootTable();
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("Item");
		sb.append("{name=").append(getName());
		sb.append('}');
		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static class CursedState
	{
		public static final int UNDISCOVERED = 0;
		public static final int DISCOVERED = 1;
		public static final int TEMPORARILY_REMOVED = 2;
		
		public String toString(int cursedState)
		{
			switch (cursedState)
			{
				case UNDISCOVERED: return "undiscovered";
				case DISCOVERED: return "discovered";
				case TEMPORARILY_REMOVED: return "removed";
				default: throw new MazeException("Invalid cursedState: "+cursedState);
			}
		}
		
		public int valueOf(String s)
		{
			if (s.equalsIgnoreCase("undiscovered"))
			{
				return UNDISCOVERED;
			}
			else if (s.equalsIgnoreCase("discovered"))
			{
				return DISCOVERED;
			}
			else if (s.equalsIgnoreCase("removed"))
			{
				return TEMPORARILY_REMOVED;
			}
			else
			{
				throw new MazeException("Invalid ["+s+"]"); 
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class IdentificationState
	{
		public static final int UNIDENTIFIED = 1;
		public static final int IDENTIFIED = 2;
	}
}

