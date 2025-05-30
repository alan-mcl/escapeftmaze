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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.data.v2.V2DataObject;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ItemTemplate extends DataObject
{
	protected static final BitSet SECONDARY_WEAPON =
		new BitSet(PlayerCharacter.EquipableSlots.NUMBER_OF_SLOTS);

	/**
	 * Identified name of this item, eg "Longsword"
	 */
	String name;

	/**
	 * Plural name of this item, eg "Longswords"
	 */
	String pluralName;

	/**
	 * Unidentified name of this item, eg "?Sword"
	 */
	String unidentifiedName;

	/**
	 * The type of the item, from {@link mclachlan.maze.stat.ItemTemplate.Type}
	 */
	int type;

	/**
	 * The subtype of the item, from each item types {@link WeaponSubType} class.
	 * -1 indicates no sub type
	 */
	int subtype;

	/**
	 * Wordy description of this item
	 */
	String description;

	/**
	 * icon for this item
	 */
	String image;

	/**
	 * modifiers of this item
	 */
	StatModifier modifiers;

	/**
	 * In which slots a PC can equip this item.  See
	 * {@link PlayerCharacter.EquipableSlots}
	 */
	BitSet equipableSlots;

	/**
	 * Weight of item, in grammes
	 */
	int weight;

	/**
	 * Which heroic classes can use this item. See
	 * {@link mclachlan.maze.data.Database#getCharacterClasses()}
	 */
	Set<String> usableByCharacterClass;

	/**
	 * Which races can use this item. See 
	 * {@link mclachlan.maze.data.Database#getRaces()}
	 */
	Set<String> usableByRace;

	/**
	 * Which genders can user this item. See 
	 * {@link Database#getGenders()}
	 */
	Set<String> usableByGender;

	/**
	 * True if this is a quest item.
	 */
	boolean questItem;

	/**
	 * Non-zero if this item is cursed.
	 */
	int curseStrength;

	/**
	 * The max of this item you can stack together.  1 or less implies not
	 * stackable.
	 */
	int maxItemsPerStack;

	/**
	 * The base cost of the item.  In GP ;-)
	 */
	int baseCost;

	/**
	 * What happens when this item is invoked.
	 */
	Spell invokedSpell;

	/**
	 * What level the invoked spell is cast at.
	 */
	int invokedSpellLevel;

	/**
	 * How many charges each item is created with.
	 */
	Dice charges;

	/**
	 * What type of charges this item has;
	 */
	ChargesType chargesType;

	/**
	 * Character level required to identify this item.
	 */
	int identificationDifficulty;

	/**
	 * Difficulty in recharging this item.
	 */
	int rechargeDifficulty;

	/**
	 * Stats required to equip this item.
	 */
	StatModifier equipRequirements;

	/**
	 * Stats required to use/invoke or disassemble this item.
	 */
	StatModifier useRequirements;

	/**
	 * Script to execute when the player attacks with this item (may be null)
	 */
	MazeScript attackScript;

	/**
	 * Base chance for this item to pick up an enchantment
	 */
	int enchantmentChance;

	/**
	 * Type of enchantment chance calculation.
	 */
	EnchantmentCalculation enchantmentCalculation;

	/**
	 * Potential enchantments on this item.
	 */
	String enchantmentScheme;

	/**
	 * If this item can be disassembled, which items it produces.
	 */
	String disassemblyLootTable;

	/**
	 * If this item is a consumable (money/supplies), what rate does it convert
	 * to gold pieces/supply units.
	 */
	float conversionRate;

	//--- weapons only
	Dice damage;
	MagicSys.SpellEffectType defaultDamageType;
	String[] attackTypes;
	boolean twoHanded;
	boolean isRanged;
	boolean isReturning;
	boolean isBackstabCapable;
	boolean isSnipeCapable;
	int toHit;
	int toPenetrate;
	int toCritical;
	int toInitiative;
	int minRange;
	int maxRange;
	List<AmmoType> ammo;
	GroupOfPossibilities<SpellEffect> spellEffects;
	int bonusAttacks;
	int bonusStrikes;
	Stats.Modifier discipline;
	TypeDescriptor slaysFoeType;

	//--- ammo only
	AmmoType ammoType;

	//--- armour only
	int damagePrevention;
	int damagePreventionChance;

	public enum ChargesType
	{
		/**
		 * charges never run out for this item
		 */
		CHARGES_INFINITE,
		/**
		 * when charges run out this item vanishes
		 */
		CHARGES_FATAL,
		/**
		 * when charges run out this item doesn't vanish
		 */
		CHARGES_NON_FATAL
	}

	/*-------------------------------------------------------------------------*/
	public enum AmmoType implements V2DataObject
	{
		ARROW, BOLT, STONE, SELF, SHOT, STAR, DART, JAVELIN, HAMMER, AXE, KNIFE;

		@Override
		public String getName()
		{
			return this.name();
		}

		@Override
		public void setName(String newName)
		{
			throw new MazeException("not supported");
		}
	}

	/*-------------------------------------------------------------------------*/
	static
	{
		SECONDARY_WEAPON.set(PlayerCharacter.EquipableSlots.SECONDARY_WEAPON);
	}

	/*-------------------------------------------------------------------------*/
	public ItemTemplate()
	{
	}

	/*-------------------------------------------------------------------------*/
	public ItemTemplate(
		String name,
		String pluralName,
		String unidentifiedName,
		int type,
		int subtype,
		String description,
		StatModifier modifiers,
		String image,
		BitSet equipableSlots,
		int weight,
		int maxItemsPerStack,
		int baseCost,
		Spell invokedSpell,
		int invokedSpellLevel,
		Dice charges,
		ChargesType chargesType,
		Set<String> usableByCharacterClass,
		Set<String> usableByRace,
		Set<String> usableByGender,
		boolean questItem,
		int curseStrength,
		int identificationDifficulty,
		int rechargeDifficulty,
		StatModifier equipRequirements,
		StatModifier useRequirements,
		MazeScript attackScript,
		Dice damage,
		MagicSys.SpellEffectType defaultDamageType,
		String[] attackTypes,
		boolean twoHanded,
		boolean isRanged,
		boolean isReturning,
		boolean isBackstabCapable,
		boolean isSnipeCapable,
		int toHit,
		int toPenetrate,
		int toCritical,
		int toInitiative,
		int minRange,
		int maxRange,
		List<AmmoType> ammo,
		GroupOfPossibilities<SpellEffect> spellEffects,
		int bonusAttacks,
		int bonusStrikes,
		Stats.Modifier discipline,
		TypeDescriptor slaysFoeType,
		AmmoType ammoType,
		int damagePrevention,
		int damagePreventionChance,
		int enchantmentChance,
		EnchantmentCalculation enchantmentCalculation,
		String enchantmentScheme,
		String disassemblyLootTable,
		float conversionRate)
	{
		this.name = name;
		this.pluralName = pluralName;
		this.unidentifiedName = unidentifiedName;
		this.type = type;
		this.subtype = subtype;
		this.modifiers = modifiers;
		this.image = image;
		this.description = description;
		this.equipableSlots = equipableSlots;
		this.weight = weight;
		this.maxItemsPerStack = maxItemsPerStack;
		this.baseCost = baseCost;
		this.invokedSpell = invokedSpell;
		this.invokedSpellLevel = invokedSpellLevel;
		this.charges = charges;
		this.chargesType = chargesType;
		this.usableByCharacterClass = usableByCharacterClass;
		this.usableByRace = usableByRace;
		this.usableByGender = usableByGender;
		this.questItem = questItem;
		this.curseStrength = curseStrength;
		this.identificationDifficulty = identificationDifficulty;
		this.rechargeDifficulty = rechargeDifficulty;
		this.equipRequirements = equipRequirements;
		this.useRequirements = useRequirements;
		this.attackScript = attackScript;
		this.damage = damage;
		this.defaultDamageType = defaultDamageType;
		this.attackTypes = attackTypes;
		this.twoHanded = twoHanded;
		this.isRanged = isRanged;
		this.isReturning = isReturning;
		this.isBackstabCapable = isBackstabCapable;
		this.isSnipeCapable = isSnipeCapable;
		this.toHit = toHit;
		this.toPenetrate = toPenetrate;
		this.toCritical = toCritical;
		this.toInitiative = toInitiative;
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.ammo = ammo;
		this.spellEffects = spellEffects;
		this.bonusAttacks = bonusAttacks;
		this.bonusStrikes = bonusStrikes;
		this.discipline = discipline;
		this.slaysFoeType = slaysFoeType;
		this.ammoType = ammoType;
		this.damagePrevention = damagePrevention;
		this.damagePreventionChance = damagePreventionChance;
		this.enchantmentChance = enchantmentChance;
		this.enchantmentCalculation = enchantmentCalculation;
		this.enchantmentScheme = enchantmentScheme;
		this.disassemblyLootTable = disassemblyLootTable;
		this.conversionRate = conversionRate;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return A newly created item, unstacked.
	 */
	public Item create()
	{
		Item result = new Item(this);
		result.setIdentificationState(Item.IdentificationState.UNIDENTIFIED);
		result.setCursedState(Item.CursedState.UNDISCOVERED);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return A newly created stack of the given number of items.
	 */
	public Item create(int stack)
	{
		Item result = new Item(this, stack);
		result.setIdentificationState(Item.IdentificationState.UNIDENTIFIED);
		result.setCursedState(Item.CursedState.UNDISCOVERED);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getMaxItemsPerStack()
	{
		return maxItemsPerStack;
	}

	/*-------------------------------------------------------------------------*/
	public void setBaseCost(int baseCost)
	{
		this.baseCost = baseCost;
	}

	public StatModifier getModifiers()
	{
		return modifiers;
	}

	public List<AmmoType> getAmmo()
	{
		return ammo;
	}

	public AmmoType getAmmoType()
	{
		return ammoType;
	}

	public MazeScript getAttackScript()
	{
		return attackScript;
	}

	public String[] getAttackTypes()
	{
		return attackTypes;
	}

	public int getBaseCost()
	{
		return baseCost;
	}

	public int getBonusAttacks()
	{
		return bonusAttacks;
	}

	public int getBonusStrikes()
	{
		return bonusStrikes;
	}

	public Dice getCharges()
	{
		return charges;
	}

	public ChargesType getChargesType()
	{
		return chargesType;
	}

	public int getCurseStrength()
	{
		return curseStrength;
	}

	public Dice getDamage()
	{
		return damage;
	}

	public MagicSys.SpellEffectType getDefaultDamageType()
	{
		return defaultDamageType;
	}

	public int getDamagePrevention()
	{
		return damagePrevention;
	}

	public int getDamagePreventionChance()
	{
		return damagePreventionChance;
	}

	public String getDescription()
	{
		return description;
	}

	public Stats.Modifier getDiscipline()
	{
		return discipline;
	}

	public BitSet getEquipableSlots()
	{
		return equipableSlots;
	}

	public int getIdentificationDifficulty()
	{
		return identificationDifficulty;
	}

	public String getImage()
	{
		return image;
	}

	public Spell getInvokedSpell()
	{
		return invokedSpell;
	}

	public int getInvokedSpellLevel()
	{
		return invokedSpellLevel;
	}

	public boolean isRanged()
	{
		return isRanged;
	}

	public int getMaxRange()
	{
		return maxRange;
	}

	public int getMinRange()
	{
		return minRange;
	}

	public String getName()
	{
		return name;
	}

	public String getPluralName()
	{
		return pluralName;
	}

	public boolean isQuestItem()
	{
		return questItem;
	}

	public int getRechargeDifficulty()
	{
		return rechargeDifficulty;
	}

	public StatModifier getEquipRequirements()
	{
		return equipRequirements;
	}

	public StatModifier getUseRequirements()
	{
		return useRequirements;
	}

	public TypeDescriptor getSlaysFoeType()
	{
		return slaysFoeType;
	}

	public GroupOfPossibilities<SpellEffect> getSpellEffects()
	{
		return spellEffects;
	}

	public int getSubtype()
	{
		return subtype;
	}

	public int getToHit()
	{
		return toHit;
	}

	public int getToPenetrate()
	{
		return toPenetrate;
	}
	
	public int getToInitiative()
	{
		return toInitiative;
	}

	public boolean isTwoHanded()
	{
		return twoHanded;
	}

	public boolean isBackstabCapable()
	{
		return isBackstabCapable;
	}

	public boolean isSnipeCapable()
	{
		return isSnipeCapable;
	}

	public int getType()
	{
		return type;
	}

	public String getUnidentifiedName()
	{
		return unidentifiedName;
	}

	public Set<String> getUsableByGender()
	{
		return usableByGender;
	}

	public Set<String> getUsableByCharacterClass()
	{
		return usableByCharacterClass;
	}

	public Set<String> getUsableByRace()
	{
		return usableByRace;
	}

	public int getWeight()
	{
		return weight;
	}

	public int getToCritical()
	{
		return toCritical;
	}

	public EnchantmentCalculation getEnchantmentCalculation()
	{
		return enchantmentCalculation;
	}

	public int getEnchantmentChance()
	{
		return enchantmentChance;
	}

	public String getEnchantmentScheme()
	{
		return enchantmentScheme;
	}

	public String getDisassemblyLootTable()
	{
		return disassemblyLootTable;
	}

	public float getConversionRate()
	{
		return conversionRate;
	}

	/*-------------------------------------------------------------------------*/

	public void setToCritical(int toCritical)
	{
		this.toCritical = toCritical;
	}

	public void setToInitiative(int toInitiative)
	{
		this.toInitiative = toInitiative;
	}

	public void setAmmo(List<AmmoType> ammo)
	{
		this.ammo = ammo;
	}

	public void setAmmoType(AmmoType ammoType)
	{
		this.ammoType = ammoType;
	}

	public void setAttackScript(MazeScript attackScript)
	{
		this.attackScript = attackScript;
	}

	public void setAttackTypes(String[] attackTypes)
	{
		this.attackTypes = attackTypes;
	}

	public void setBonusAttacks(int bonusAttacks)
	{
		this.bonusAttacks = bonusAttacks;
	}

	public void setBonusStrikes(int bonusStrikes)
	{
		this.bonusStrikes = bonusStrikes;
	}

	public void setCharges(Dice charges)
	{
		this.charges = charges;
	}

	public void setChargesType(ChargesType chargesType)
	{
		this.chargesType = chargesType;
	}

	public void setCurseStrength(int curseStrength)
	{
		this.curseStrength = curseStrength;
	}

	public void setDamage(Dice damage)
	{
		this.damage = damage;
	}

	public void setDefaultDamageType(MagicSys.SpellEffectType defaultDamageType)
	{
		this.defaultDamageType = defaultDamageType;
	}

	public void setDamagePrevention(int damagePrevention)
	{
		this.damagePrevention = damagePrevention;
	}

	public void setDamagePreventionChance(int damagePreventionChance)
	{
		this.damagePreventionChance = damagePreventionChance;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setDiscipline(Stats.Modifier discipline)
	{
		this.discipline = discipline;
	}

	public void setEquipableSlots(BitSet equipableSlots)
	{
		this.equipableSlots = equipableSlots;
	}

	public void setIdentificationDifficulty(int identificationDifficulty)
	{
		this.identificationDifficulty = identificationDifficulty;
	}

	public void setImage(String image)
	{
		this.image = image;
	}

	public void setInvokedSpell(Spell invokedSpell)
	{
		this.invokedSpell = invokedSpell;
	}

	public void setInvokedSpellLevel(int invokedSpellLevel)
	{
		this.invokedSpellLevel = invokedSpellLevel;
	}

	public void setRanged(boolean ranged)
	{
		isRanged = ranged;
	}

	public void setMaxItemsPerStack(int maxItemsPerStack)
	{
		this.maxItemsPerStack = maxItemsPerStack;
	}

	public void setMaxRange(int maxRange)
	{
		this.maxRange = maxRange;
	}

	public void setMinRange(int minRange)
	{
		this.minRange = minRange;
	}

	public void setModifiers(StatModifier modifiers)
	{
		this.modifiers = modifiers;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPluralName(String pluralName)
	{
		this.pluralName = pluralName;
	}

	public void setQuestItem(boolean questItem)
	{
		this.questItem = questItem;
	}

	public void setRechargeDifficulty(int rechargeDifficulty)
	{
		this.rechargeDifficulty = rechargeDifficulty;
	}

	public void setEquipRequirements(StatModifier equipRequirements)
	{
		this.equipRequirements = equipRequirements;
	}

	public void setUseRequirements(StatModifier useRequirements)
	{
		this.useRequirements = useRequirements;
	}

	public void setSlaysFoeType(TypeDescriptor slaysFoeType)
	{
		this.slaysFoeType = slaysFoeType;
	}

	public void setSpellEffects(GroupOfPossibilities<SpellEffect> spellEffects)
	{
		this.spellEffects = spellEffects;
	}

	public void setSubtype(int subtype)
	{
		this.subtype = subtype;
	}

	public void setToHit(int toHit)
	{
		this.toHit = toHit;
	}

	public void setToPenetrate(int toPenetrate)
	{
		this.toPenetrate = toPenetrate;
	}

	public void setTwoHanded(boolean twoHanded)
	{
		this.twoHanded = twoHanded;
	}

	public void setBackstabCapable(boolean backstabCapable)
	{
		isBackstabCapable = backstabCapable;
	}

	public void setSnipeCapable(boolean snipeCapable)
	{
		isSnipeCapable = snipeCapable;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public void setUnidentifiedName(String unidentifiedName)
	{
		this.unidentifiedName = unidentifiedName;
	}

	public void setUsableByGender(Set<String> usableByGender)
	{
		this.usableByGender = usableByGender;
	}

	public void setUsableByCharacterClass(Set<String> list)
	{
		this.usableByCharacterClass = list;
	}

	public void setUsableByRace(Set<String> usableByRace)
	{
		this.usableByRace = usableByRace;
	}

	public void setWeight(int weight)
	{
		this.weight = weight;
	}

	public boolean isReturning()
	{
		return isReturning;
	}

	public void setReturning(boolean returning)
	{
		isReturning = returning;
	}

	public void setEnchantmentCalculation(
		EnchantmentCalculation enchantmentCalculation)
	{
		this.enchantmentCalculation = enchantmentCalculation;
	}

	public void setEnchantmentChance(int enchantmentChance)
	{
		this.enchantmentChance = enchantmentChance;
	}

	public void setEnchantmentScheme(String enchantmentScheme)
	{
		this.enchantmentScheme = enchantmentScheme;
	}

	public void setDisassemblyLootTable(String disassemblyLootTable)
	{
		this.disassemblyLootTable = disassemblyLootTable;
	}

	public void setConversionRate(float conversionRate)
	{
		this.conversionRate = conversionRate;
	}

	/*-------------------------------------------------------------------------*/
	public ItemEnchantment getEnchantment(String enchantment)
	{
		ItemEnchantments scheme = Database.getInstance().getItemEnchantments().get(
			this.enchantmentScheme);

		if (scheme != null)
		{
			return scheme.getEnchantment(enchantment);
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean isWeapon()
	{
		return type == Type.SHORT_WEAPON ||
			type == Type.EXTENDED_WEAPON ||
			type == Type.THROWN_WEAPON ||
			type == Type.RANGED_WEAPON;
	}

	/*-------------------------------------------------------------------------*/
	public static String describeWeapon(AttackEvent e)
	{
		String s = e.getAttacker().getName()
			+ " " + e.getAttackType().getVerb()
			+ " " + e.getAttackWith().getDisplayName();

		if (e.getNrStrikes() > 1)
		{
			s += " x" + e.getNrStrikes();
		}

		return s;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		ItemTemplate that = (ItemTemplate)o;

		if (getType() != that.getType())
		{
			return false;
		}
		if (getSubtype() != that.getSubtype())
		{
			return false;
		}
		if (getWeight() != that.getWeight())
		{
			return false;
		}
		if (isQuestItem() != that.isQuestItem())
		{
			return false;
		}
		if (getCurseStrength() != that.getCurseStrength())
		{
			return false;
		}
		if (getMaxItemsPerStack() != that.getMaxItemsPerStack())
		{
			return false;
		}
		if (getBaseCost() != that.getBaseCost())
		{
			return false;
		}
		if (getInvokedSpellLevel() != that.getInvokedSpellLevel())
		{
			return false;
		}
		if (getIdentificationDifficulty() != that.getIdentificationDifficulty())
		{
			return false;
		}
		if (getRechargeDifficulty() != that.getRechargeDifficulty())
		{
			return false;
		}
		if (getEnchantmentChance() != that.getEnchantmentChance())
		{
			return false;
		}
		if (Float.compare(that.getConversionRate(), getConversionRate()) != 0)
		{
			return false;
		}
		if (isTwoHanded() != that.isTwoHanded())
		{
			return false;
		}
		if (isRanged() != that.isRanged())
		{
			return false;
		}
		if (isReturning() != that.isReturning())
		{
			return false;
		}
		if (isBackstabCapable() != that.isBackstabCapable())
		{
			return false;
		}
		if (isSnipeCapable() != that.isSnipeCapable())
		{
			return false;
		}
		if (getToHit() != that.getToHit())
		{
			return false;
		}
		if (getToPenetrate() != that.getToPenetrate())
		{
			return false;
		}
		if (getToCritical() != that.getToCritical())
		{
			return false;
		}
		if (getToInitiative() != that.getToInitiative())
		{
			return false;
		}
		if (getMinRange() != that.getMinRange())
		{
			return false;
		}
		if (getMaxRange() != that.getMaxRange())
		{
			return false;
		}
		if (getBonusAttacks() != that.getBonusAttacks())
		{
			return false;
		}
		if (getBonusStrikes() != that.getBonusStrikes())
		{
			return false;
		}
		if (getDamagePrevention() != that.getDamagePrevention())
		{
			return false;
		}
		if (getDamagePreventionChance() != that.getDamagePreventionChance())
		{
			return false;
		}
		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
		{
			return false;
		}
		if (getPluralName() != null ? !getPluralName().equals(that.getPluralName()) : that.getPluralName() != null)
		{
			return false;
		}
		if (getUnidentifiedName() != null ? !getUnidentifiedName().equals(that.getUnidentifiedName()) : that.getUnidentifiedName() != null)
		{
			return false;
		}
		if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
		{
			return false;
		}
		if (getImage() != null ? !getImage().equals(that.getImage()) : that.getImage() != null)
		{
			return false;
		}
		if (getModifiers() != null ? !getModifiers().equals(that.getModifiers()) : that.getModifiers() != null)
		{
			return false;
		}
		if (getEquipableSlots() != null ? !getEquipableSlots().equals(that.getEquipableSlots()) : that.getEquipableSlots() != null)
		{
			return false;
		}
		if (getUsableByCharacterClass() != null ? !getUsableByCharacterClass().equals(that.getUsableByCharacterClass()) : that.getUsableByCharacterClass() != null)
		{
			return false;
		}
		if (getUsableByRace() != null ? !getUsableByRace().equals(that.getUsableByRace()) : that.getUsableByRace() != null)
		{
			return false;
		}
		if (getUsableByGender() != null ? !getUsableByGender().equals(that.getUsableByGender()) : that.getUsableByGender() != null)
		{
			return false;
		}
		if (getInvokedSpell() != null ? !getInvokedSpell().equals(that.getInvokedSpell()) : that.getInvokedSpell() != null)
		{
			return false;
		}
		if (getCharges() != null ? !getCharges().equals(that.getCharges()) : that.getCharges() != null)
		{
			return false;
		}
		if (getChargesType() != that.getChargesType())
		{
			return false;
		}
		if (getEquipRequirements() != null ? !getEquipRequirements().equals(that.getEquipRequirements()) : that.getEquipRequirements() != null)
		{
			return false;
		}
		if (getUseRequirements() != null ? !getUseRequirements().equals(that.getUseRequirements()) : that.getUseRequirements() != null)
		{
			return false;
		}
		if (getAttackScript() != null ? !getAttackScript().equals(that.getAttackScript()) : that.getAttackScript() != null)
		{
			return false;
		}
		if (getEnchantmentCalculation() != that.getEnchantmentCalculation())
		{
			return false;
		}
		if (getEnchantmentScheme() != null ? !getEnchantmentScheme().equals(that.getEnchantmentScheme()) : that.getEnchantmentScheme() != null)
		{
			return false;
		}
		if (getDisassemblyLootTable() != null ? !getDisassemblyLootTable().equals(that.getDisassemblyLootTable()) : that.getDisassemblyLootTable() != null)
		{
			return false;
		}
		if (getDamage() != null ? !getDamage().equals(that.getDamage()) : that.getDamage() != null)
		{
			return false;
		}
		if (getDefaultDamageType() != that.getDefaultDamageType())
		{
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(getAttackTypes(), that.getAttackTypes()))
		{
			return false;
		}
		if (getAmmo() != null ? !getAmmo().equals(that.getAmmo()) : that.getAmmo() != null)
		{
			return false;
		}
		if (getSpellEffects() != null ? !getSpellEffects().equals(that.getSpellEffects()) : that.getSpellEffects() != null)
		{
			return false;
		}
		if (getDiscipline() != that.getDiscipline())
		{
			return false;
		}
		if (getSlaysFoeType() != null ? !getSlaysFoeType().equals(that.getSlaysFoeType()) : that.getSlaysFoeType() != null)
		{
			return false;
		}
		return getAmmoType() == that.getAmmoType();
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getPluralName() != null ? getPluralName().hashCode() : 0);
		result = 31 * result + (getUnidentifiedName() != null ? getUnidentifiedName().hashCode() : 0);
		result = 31 * result + getType();
		result = 31 * result + getSubtype();
		result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
		result = 31 * result + (getImage() != null ? getImage().hashCode() : 0);
		result = 31 * result + (getModifiers() != null ? getModifiers().hashCode() : 0);
		result = 31 * result + (getEquipableSlots() != null ? getEquipableSlots().hashCode() : 0);
		result = 31 * result + getWeight();
		result = 31 * result + (getUsableByCharacterClass() != null ? getUsableByCharacterClass().hashCode() : 0);
		result = 31 * result + (getUsableByRace() != null ? getUsableByRace().hashCode() : 0);
		result = 31 * result + (getUsableByGender() != null ? getUsableByGender().hashCode() : 0);
		result = 31 * result + (isQuestItem() ? 1 : 0);
		result = 31 * result + getCurseStrength();
		result = 31 * result + getMaxItemsPerStack();
		result = 31 * result + getBaseCost();
		result = 31 * result + (getInvokedSpell() != null ? getInvokedSpell().hashCode() : 0);
		result = 31 * result + getInvokedSpellLevel();
		result = 31 * result + (getCharges() != null ? getCharges().hashCode() : 0);
		result = 31 * result + (getChargesType() != null ? getChargesType().hashCode() : 0);
		result = 31 * result + getIdentificationDifficulty();
		result = 31 * result + getRechargeDifficulty();
		result = 31 * result + (getEquipRequirements() != null ? getEquipRequirements().hashCode() : 0);
		result = 31 * result + (getUseRequirements() != null ? getUseRequirements().hashCode() : 0);
		result = 31 * result + (getAttackScript() != null ? getAttackScript().hashCode() : 0);
		result = 31 * result + getEnchantmentChance();
		result = 31 * result + (getEnchantmentCalculation() != null ? getEnchantmentCalculation().hashCode() : 0);
		result = 31 * result + (getEnchantmentScheme() != null ? getEnchantmentScheme().hashCode() : 0);
		result = 31 * result + (getDisassemblyLootTable() != null ? getDisassemblyLootTable().hashCode() : 0);
		result = 31 * result + (getConversionRate() != +0.0f ? Float.floatToIntBits(getConversionRate()) : 0);
		result = 31 * result + (getDamage() != null ? getDamage().hashCode() : 0);
		result = 31 * result + (getDefaultDamageType() != null ? getDefaultDamageType().hashCode() : 0);
		result = 31 * result + Arrays.hashCode(getAttackTypes());
		result = 31 * result + (isTwoHanded() ? 1 : 0);
		result = 31 * result + (isRanged() ? 1 : 0);
		result = 31 * result + (isReturning() ? 1 : 0);
		result = 31 * result + (isBackstabCapable() ? 1 : 0);
		result = 31 * result + (isSnipeCapable() ? 1 : 0);
		result = 31 * result + getToHit();
		result = 31 * result + getToPenetrate();
		result = 31 * result + getToCritical();
		result = 31 * result + getToInitiative();
		result = 31 * result + getMinRange();
		result = 31 * result + getMaxRange();
		result = 31 * result + (getAmmo() != null ? getAmmo().hashCode() : 0);
		result = 31 * result + (getSpellEffects() != null ? getSpellEffects().hashCode() : 0);
		result = 31 * result + getBonusAttacks();
		result = 31 * result + getBonusStrikes();
		result = 31 * result + (getDiscipline() != null ? getDiscipline().hashCode() : 0);
		result = 31 * result + (getSlaysFoeType() != null ? getSlaysFoeType().hashCode() : 0);
		result = 31 * result + (getAmmoType() != null ? getAmmoType().hashCode() : 0);
		result = 31 * result + getDamagePrevention();
		result = 31 * result + getDamagePreventionChance();
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static class WeaponRange
	{
		public static final int MELEE = 1, EXTENDED = 2, THROWN = 3, LONG = 4;

		public static String describe(int value)
		{
			switch (value)
			{
				case 0:
				case MELEE:
					return "melee";
				case EXTENDED:
					return "extended";
				case THROWN:
					return "thrown";
				case LONG:
					return "long";
				default:
					throw new MazeException("Invalid range: " + value);
			}
		}

		public static int valueOf(String s)
		{
			if (s.equals("melee"))
			{
				return MELEE;
			}
			else if (s.equals("extended"))
			{
				return EXTENDED;
			}
			else if (s.equals("thrown"))
			{
				return THROWN;
			}
			else if (s.equals("long"))
			{
				return LONG;
			}
			else
			{
				throw new MazeException("Invalid range ["+s+"]");
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class WeaponSubType
	{
		public static final int NONE = 0;
		public static final int SWORD = 1;
		public static final int AXE = 2;
		public static final int POLEARM = 3;
		public static final int MACE = 4;
		public static final int DAGGER = 5;
		public static final int STAFF = 6;
		public static final int WAND = 7;
		public static final int MODERN = 8;
		public static final int BOW = 9;
		public static final int THROWN = 10;
		public static final int MARTIAL_ARTS = 11;

		public static final int MAX_SUBTYPES = 12;

		static Map<String, Integer> types = new HashMap<String, Integer>();

		static
		{
			for (int i=0; i<MAX_SUBTYPES; i++)
			{
				types.put(describe(i), i);
			}
		}

		public static int valueOf(String s)
		{
			if (types.containsKey(s))
			{
				return types.get(s);
			}
			else
			{
				throw new MazeException("Invalid type ["+s+"]");
			}
		}

		public static String describe(int subType)
		{
			switch (subType)
			{
				case NONE: return "none";
				case SWORD: return "sword";
				case AXE: return "axe";
				case POLEARM: return "polearm";
				case MACE: return "mace";
				case DAGGER: return "dagger";
				case STAFF: return "staff";
				case WAND: return "wand";
				case MODERN: return "modern";
				case BOW: return "bow";
				case THROWN: return "thrown";
				case MARTIAL_ARTS: return "martial arts";
				default:
					throw new MazeException("Invalid type [" + subType + "]");
			}
		}

		public static Collection<String> values()
		{
			return types.keySet();
		}
	}

	/*-------------------------------------------------------------------------*/
	public static enum EnchantmentCalculation
	{
		STRAIGHT,
		PARTY_LEVEL,
	}

	/*-------------------------------------------------------------------------*/
	public static class Type
	{
		public static final int MAX_ITEM_TYPES = 28;

		public static final int SHORT_WEAPON = 0;
		public static final int EXTENDED_WEAPON = 1;
		public static final int THROWN_WEAPON = 2;
		public static final int RANGED_WEAPON = 3;
		public static final int AMMUNITION = 4;
		public static final int SHIELD = 5;
		public static final int TORSO_ARMOUR = 6;
		public static final int LEG_ARMOUR = 7;
		public static final int HEAD_ARMOUR = 8;
		public static final int GLOVES = 9;
		public static final int BOOTS = 10;
		public static final int MISC_EQUIPMENT = 11;
		public static final int BANNER_EQUIPMENT = 12;
		public static final int MISC_MAGIC = 13;
		public static final int POTION = 14;
		public static final int BOMB = 15;
		public static final int POWDER = 16;
		public static final int SPELLBOOK = 17;
		public static final int SCROLL = 18;
		public static final int FOOD = 19;
		public static final int DRINK = 20;
		public static final int KEY = 21;
		public static final int WRITING = 22;
		public static final int OTHER = 23;
		public static final int GADGET = 24;
		public static final int MUSICAL_INSTRUMENT = 25;
		public static final int MONEY = 26;
		public static final int SUPPLIES = 27;

		static Map<String, Integer> types = new HashMap<String, Integer>();

		static
		{
			for (int i=0; i<MAX_ITEM_TYPES; i++)
			{
				types.put(describe(i), i);
			}
		}

		public static int valueOf(String s)
		{
			if (types.containsKey(s))
			{
				return types.get(s);
			}
			else
			{
				throw new MazeException("Invalid type ["+s+"]");
			}
		}

		public static String describe(int type)
		{
			switch(type)
			{
				case SHORT_WEAPON: return StringUtil.getGamesysString("item.type.short_weapon");
				case EXTENDED_WEAPON: return StringUtil.getGamesysString("item.type.extended_weapon");
				case THROWN_WEAPON: return StringUtil.getGamesysString("item.type.thrown_weapon");
				case RANGED_WEAPON: return StringUtil.getGamesysString("item.type.ranged_weapon");
				case AMMUNITION: return StringUtil.getGamesysString("item.type.ammunition");
				case SHIELD: return StringUtil.getGamesysString("item.type.shield");
				case TORSO_ARMOUR: return StringUtil.getGamesysString("item.type.torso_armour");
				case LEG_ARMOUR: return StringUtil.getGamesysString("item.type.leg_armour");
				case HEAD_ARMOUR: return StringUtil.getGamesysString("item.type.head_armour");
				case GLOVES: return StringUtil.getGamesysString("item.type.gloves");
				case BOOTS: return StringUtil.getGamesysString("item.type.boots");
				case MISC_EQUIPMENT: return StringUtil.getGamesysString("item.type.misc_equipment");
				case BANNER_EQUIPMENT: return StringUtil.getGamesysString("item.type.banner_equipment");
				case MISC_MAGIC: return StringUtil.getGamesysString("item.type.misc_magic");
				case POTION: return StringUtil.getGamesysString("item.type.potion");
				case BOMB: return StringUtil.getGamesysString("item.type.bomb");
				case POWDER: return StringUtil.getGamesysString("item.type.powder");
				case SPELLBOOK: return StringUtil.getGamesysString("item.type.spellbook");
				case SCROLL: return StringUtil.getGamesysString("item.type.scroll");
				case FOOD: return StringUtil.getGamesysString("item.type.food");
				case DRINK: return StringUtil.getGamesysString("item.type.drink");
				case KEY: return StringUtil.getGamesysString("item.type.key");
				case WRITING: return StringUtil.getGamesysString("item.type.writing");
				case GADGET: return StringUtil.getGamesysString("item.type.gadget");
				case MUSICAL_INSTRUMENT: return StringUtil.getGamesysString("item.type.musical_instrument");
				case MONEY: return StringUtil.getGamesysString("item.type.money");
				case SUPPLIES: return StringUtil.getGamesysString("item.type.supplies");
				case OTHER: return StringUtil.getGamesysString("item.type.other");
				default: throw new MazeException("Invalid type ["+type+"]");
			}
		}
	}
}
