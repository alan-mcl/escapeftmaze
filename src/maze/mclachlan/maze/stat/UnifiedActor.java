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

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.UiMessageEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.combat.*;
import mclachlan.maze.stat.combat.event.ConditionEvent;
import mclachlan.maze.stat.combat.event.HealingEvent;
import mclachlan.maze.stat.combat.event.StrikeEvent;
import mclachlan.maze.stat.condition.*;
import mclachlan.maze.stat.condition.impl.FatigueKO;
import mclachlan.maze.stat.condition.impl.RestingSleep;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.stat.EquipableSlot.Type.*;

/**
 * The class to unify the PC and Foe data models.
 * This class is intended as a working data class, not a template class.
 */
public abstract class UnifiedActor implements ConditionBearer, SpellTarget
{
	public static final int MAX_PACK_ITEMS = 20;
	
	/** The name of this actor */
	private String name;

	/**
	 * Key  : String(character class name) <br>
	 * Value: character level in that class.
	 */
	private Map<String, Integer> levels = new HashMap<String, Integer>();

	/** The race of this actor. May be null, in which case the actor is race-less. */
	private Race race;

	/** The gender of this actor. May be null, in which case the actor is gender-less. */
	private Gender gender;

	/** Current character class */
	private CharacterClass characterClass;

	/** The modifiers for this actor */
	private Stats stats;

	/** Items in this actor's inventory */
	private Inventory inventory;

	/** Spells this actor knows */
	private SpellBook spellBook;

	//~~~~~~~~~~~~~~~~~~
	// volatile data
	/** available equipable slots*/
	private Map<EquipableSlot.Type, List<EquipableSlot>> equipableSlots;

	/** Body parts of this actor, along with percentages of how often they are hit in combat */
	private PercentageTable<BodyPart> bodyParts;

	/** Temp data for each combat round */
	private CombatantData combatantData;

	/** The group that this actor belongs to */
	private ActorGroup group;
	private WieldingCombo currentWieldingCombo;

	private PlayerCharacter.Stance stance;

	/*-------------------------------------------------------------------------*/

	protected UnifiedActor()
	{
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor(
		String name,
		Gender gender,
		Race race,
		CharacterClass characterClass,
		PercentageTable<BodyPart> bodyParts,
		Map<String, Integer> levels,
		Stats stats, SpellBook spellBook,
		Inventory inventory)
	{
		this.characterClass = characterClass;
		this.gender = gender;
		this.inventory = inventory;
		this.levels = levels;
		this.name = name;
		this.race = race;
		this.spellBook = spellBook;
		this.stats = stats;
		this.bodyParts = bodyParts;

		if (getInventory() != null)
		{
			for (int i = 0; i < MAX_PACK_ITEMS; i++)
			{
				getInventory().add(null);
			}
		}

		equipableSlots = buildEquipableSlots();
	}

	/*-------------------------------------------------------------------------*/
	/** Clone, should perform a deep copy */
	protected UnifiedActor(UnifiedActor other)
	{
		this.characterClass = other.characterClass;
		this.gender = other.gender;
		this.inventory = new Inventory(other.inventory);
		this.levels = other.levels;
		this.name = other.name;
		this.race = other.race;
		this.spellBook = other.spellBook;
		this.stats = other.stats;
		this.bodyParts = other.getBodyParts();

		equipableSlots = buildEquipableSlots();
	}

	/*-------------------------------------------------------------------------*/
	public Item getEquippedItem(EquipableSlot.Type type, int ordinal)
	{
		if (!equipableSlots.containsKey(type))
		{
			throw new MazeException("No slot of type "+type);
		}

		EquipableSlot es = equipableSlots.get(type).get(ordinal);

		if (es != null)
		{
			return es.getItem();
		}
		else
		{
			throw new MazeException("No slot of type "+type);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setEquippedItem(EquipableSlot.Type type, Item item, int ordinal)
	{
		if (!equipableSlots.containsKey(type))
		{
			throw new MazeException("No slot of type "+type);
		}

		EquipableSlot es = equipableSlots.get(type).get(ordinal);

		if (es != null)
		{
			es.setItem(item);
		}
		else
		{
			throw new MazeException("No slot of type "+type);
		}

		if (type.equals(PRIMARY_WEAPON) || type.equals(SECONDARY_WEAPON))
		{
			this.currentWieldingCombo = getCurrentWieldingCombo();
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Equips the given item in the first available slot
	 */
	public void setEquippedItem(EquipableSlot.Type type, Item item)
	{
		if (!equipableSlots.containsKey(type))
		{
			throw new MazeException("No slot of type "+type);
		}

		List<EquipableSlot> slots = equipableSlots.get(type);

		for(int i=0; i<slots.size(); i++)
		{
			if (!slots.get(i).hasItemEquipped())
			{
				setEquippedItem(type, item, i);
				return;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<EquipableSlot.Type, List<EquipableSlot>> getEquipableSlots()
	{
		return equipableSlots;
	}

	/*-------------------------------------------------------------------------*/
	public List<EquipableSlot> getAllEquipableSlots()
	{
		List<EquipableSlot> result = new ArrayList<EquipableSlot>();

		for (List<EquipableSlot> i : this.getEquipableSlots().values())
		{
			result.addAll(i);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Map<EquipableSlot.Type, List<EquipableSlot>> buildEquipableSlots()
	{
		Map<EquipableSlot.Type, List<EquipableSlot>> result =
			new HashMap<EquipableSlot.Type, List<EquipableSlot>>();

		// count the weapon-wielding parts
		int weaponWieldingParts = 0;
		for (BodyPart bp : bodyParts.getItems())
		{
			weaponWieldingParts += bp.getNrWeaponHardpoints();
		}

		//
		// add weapon-wielding parts first
		//

		// half of a being's weapon wielding slots are primary, rounded down.
		// todo: ambidexterity modifier
		int primarySlots = weaponWieldingParts/2;
		for (int i=0; i<primarySlots; i++)
		{
			String slotName = "Primary Weapon";
			if (primarySlots > 1)
			{
				slotName += " "+(i+1);
			}
			addToListInMap(result, new EquipableSlot(slotName, slotName, PRIMARY_WEAPON));
			addToListInMap(result, new EquipableSlot("Alt "+slotName, slotName, PRIMARY_WEAPON));
		}
		for (int i=0; i<weaponWieldingParts-primarySlots; i++)
		{
			String slotName = "Secondary Weapon";
			if (primarySlots > 1)
			{
				slotName += " "+(i+1);
			}
			addToListInMap(result, new EquipableSlot(slotName, slotName, SECONDARY_WEAPON));
			addToListInMap(result, new EquipableSlot("Alt "+slotName, slotName, SECONDARY_WEAPON));
		}

		//
		// add the other types of slots
		//

		for (BodyPart bp : bodyParts.getItems())
		{
			EquipableSlot.Type est = bp.getEquipableSlotType();
			if (est != NONE)
			{
				// todo: naming
				addToListInMap(result, new EquipableSlot(est.name(), est.name(), est));
			}
		}

		//
		// every actor gets two misc item slots by default
		//

		addToListInMap(result, new EquipableSlot("Misc Item 1", "Misc Item 1", MISC_ITEM));
		addToListInMap(result, new EquipableSlot("Misc Item 2", "Misc Item 2", MISC_ITEM));

		//
		// every actor gets one banner item slot by default
		//

		addToListInMap(result, new EquipableSlot("Banner Item", "Banner Item", BANNER_ITEM));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void addToListInMap(Map<EquipableSlot.Type, List<EquipableSlot>> map, EquipableSlot es)
	{
		List<EquipableSlot> list = map.get(es.getType());

		if (list == null)
		{
			list = new ArrayList<EquipableSlot>();
			map.put(es.getType(), list);
		}

		list.add(es);
	}

	/*-------------------------------------------------------------------------*/

	//
	// Utility shortcuts for common equipment slots in bipedal humanoids
	//

	public Item getPrimaryWeapon()
	{
		if (this.hasEquipableSlot(PRIMARY_WEAPON))
		{
			return getEquippedItem(PRIMARY_WEAPON, 0);
		}
		else
		{
			return null;
		}
	}

	public void setPrimaryWeapon(Item primaryWeapon)
	{
		if (this.hasEquipableSlot(PRIMARY_WEAPON))
		{
			setEquippedItem(PRIMARY_WEAPON, primaryWeapon, 0);
		}
	}

	public Item getSecondaryWeapon()
	{
		if (this.hasEquipableSlot(SECONDARY_WEAPON))
		{
			return getEquippedItem(SECONDARY_WEAPON, 0);
		}
		else
		{
			return null;
		}
	}

	public void setSecondaryWeapon(Item secondaryWeapon)
	{
		if (this.hasEquipableSlot(SECONDARY_WEAPON))
		{
			setEquippedItem(SECONDARY_WEAPON, secondaryWeapon, 0);
		}
	}

	public Item getAltPrimaryWeapon()
	{
		if (this.hasEquipableSlot(PRIMARY_WEAPON, 1))
		{
			return getEquippedItem(PRIMARY_WEAPON, 1);
		}
		else
		{
			return null;
		}
	}

	public void setAltPrimaryWeapon(Item altPrimaryWeapon)
	{
		if (this.hasEquipableSlot(PRIMARY_WEAPON, 1))
		{
			// doesn't set the curse state
			setEquippedItem(PRIMARY_WEAPON, altPrimaryWeapon, 1);
		}
	}

	public Item getAltSecondaryWeapon()
	{
		if (this.hasEquipableSlot(SECONDARY_WEAPON, 1))
		{
			return getEquippedItem(SECONDARY_WEAPON, 1);
		}
		else
		{
			return null;
		}
	}

	public void setAltSecondaryWeapon(Item altSecondaryWeapon)
	{
		if (this.hasEquipableSlot(SECONDARY_WEAPON, 1))
		{
			// doesn't set the curse state
			setEquippedItem(SECONDARY_WEAPON, altSecondaryWeapon, 1);
		}
	}

	public Item getBannerItem()
	{
		if (this.hasEquipableSlot(BANNER_ITEM))
		{
			return getEquippedItem(BANNER_ITEM, 0);
		}
		else
		{
			return null;
		}
	}

	public void setBannerItem(Item bannerItem)
	{
		if (this.hasEquipableSlot(BANNER_ITEM))
		{
			setEquippedItem(BANNER_ITEM, bannerItem, 0);
		}
	}

	public Item getMiscItem1()
	{
		if (this.hasEquipableSlot(MISC_ITEM, 0))
		{
			return getEquippedItem(MISC_ITEM, 0);
		}
		else
		{
			return null;
		}
	}

	public Item getMiscItem2()
	{
		if (this.hasEquipableSlot(MISC_ITEM, 1))
		{
			return getEquippedItem(MISC_ITEM, 1);
		}
		else
		{
			return null;
		}
	}

	public void setMiscItem1(Item miscItem1)
	{
		if (this.hasEquipableSlot(MISC_ITEM))
		{
			setEquippedItem(MISC_ITEM, miscItem1, 0);
		}
	}

	public void setMiscItem2(Item miscItem2)
	{
		if (this.hasEquipableSlot(MISC_ITEM, 1))
		{
			setEquippedItem(MISC_ITEM, miscItem2, 1);
		}
	}

	public Item getBoots()
	{
		if (this.hasEquipableSlot(BOOTS))
		{
			return getEquippedItem(BOOTS, 0);
		}
		else
		{
			return null;
		}
	}

	public Item getGloves()
	{
		if (hasEquipableSlot(GLOVES))
		{
			return getEquippedItem(GLOVES, 0);
		}
		else
		{
			return null;
		}
	}

	public Item getHelm()
	{
		if (hasEquipableSlot(HELM))
		{
			return getEquippedItem(HELM, 0);
		}
		else
		{
			return null;
		}
	}

	public Item getLegArmour()
	{
		if (hasEquipableSlot(LEG_ARMOUR))
		{
			return getEquippedItem(LEG_ARMOUR, 0);
		}
		else
		{
			return null;
		}
	}

	public Item getTorsoArmour()
	{
		if (hasEquipableSlot(TORSO_ARMOUR))
		{
			return getEquippedItem(TORSO_ARMOUR, 0);
		}
		else
		{
			return null;
		}
	}

	public void setBoots(Item boots)
	{
		if (this.hasEquipableSlot(BOOTS))
		{
			setEquippedItem(BOOTS, boots, 0);
		}
	}

	public void setGloves(Item gloves)
	{
		if (this.hasEquipableSlot(GLOVES))
		{
			setEquippedItem(GLOVES, gloves, 0);
		}
	}

	public void setHelm(Item helm)
	{
		if (this.hasEquipableSlot(HELM))
		{
			setEquippedItem(HELM, helm, 0);
		}
	}

	public void setLegArmour(Item legArmour)
	{
		if (this.hasEquipableSlot(LEG_ARMOUR))
		{
			setEquippedItem(LEG_ARMOUR, legArmour, 0);
		}
	}

	public void setTorsoArmour(Item torsoArmour)
	{
		if (this.hasEquipableSlot(TORSO_ARMOUR))
		{
			setEquippedItem(TORSO_ARMOUR, torsoArmour, 0);
		}
	}

	/*-------------------------------------------------------------------------*/
	public PercentageTable<BodyPart> getBodyParts()
	{
		return bodyParts;
	}

	public void setBodyParts(PercentageTable<BodyPart> bodyParts)
	{
		this.bodyParts = bodyParts;
	}

	public CharacterClass getCharacterClass()
	{
		return characterClass;
	}

	public void setCharacterClass(CharacterClass characterClass)
	{
		this.characterClass = characterClass;
	}

	public Gender getGender()
	{
		return gender;
	}

	public void setGender(Gender gender)
	{
		this.gender = gender;
	}

	public Inventory getInventory()
	{
		return inventory;
	}

	public void setInventory(Inventory inventory)
	{
		this.inventory = inventory;
	}

	public Map<String, Integer> getLevels()
	{
		return levels;
	}

	public void setLevels(Map<String, Integer> levels)
	{
		this.levels = levels;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Race getRace()
	{
		return race;
	}

	public void setRace(Race race)
	{
		this.race = race;
	}

	public SpellBook getSpellBook()
	{
		return spellBook;
	}

	public void setSpellBook(SpellBook spellBook)
	{
		this.spellBook = spellBook;
	}

	public Stats getStats()
	{
		return stats;
	}

	public void setStats(Stats stats)
	{
		this.stats = stats;
	}

	public List<NaturalWeapon> getNaturalWeapons()
	{
		List<NaturalWeapon> result = new ArrayList<NaturalWeapon>();

		// race natural weapons
		if (getRace() != null && getRace().getNaturalWeapons() != null)
		{
			result.addAll(getRace().getNaturalWeapons());
		}

		// class natural weapons
		List<LevelAbility> abilities = getLevelAbilities();

		for (LevelAbility la : abilities)
		{
			if (la.getNaturalWeapon() != null)
			{
				result.add(la.getNaturalWeapon());
			}
		}

		return result;
	}

	public List<SpellLikeAbility> getSpellLikeAbilities()
	{
		List<SpellLikeAbility> result = new ArrayList<SpellLikeAbility>();

		// race abilities
		if (getRace() != null && getRace().getSpecialAbility() != null)
		{
			result.add(new SpellLikeAbility(
				getRace().getSpecialAbility(),
				new ValueList(new Value(getLevel(), Value.SCALE.NONE))));
		}

		// class abilities
		List<LevelAbility> abilities = getLevelAbilities();

		for (LevelAbility la : abilities)
		{
			if (la.getAbility() != null)
			{
				result.add(la.getAbility());
			}
		}

		return result;
	}


	/*-------------------------------------------------------------------------*/
	/**
	 * @return the number of items in the inventory
	 */
	public int getInventorySize()
	{
		int result = 0;

		for (Item i : getInventory())
		{
			if (i != null)
			{
				result++;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return This PCs total character level
	 */
	public int getLevel()
	{
		int result = 0;
		for (Integer i : getLevels().values())
		{
			result += i;
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return All items equipped by this character. Will return an empty list
	 * if no items are equipped, never null.
	 */
	public List<Item> getEquippedItems()
	{
		List<Item> result = new ArrayList<Item>();

		for (List<EquipableSlot> slots : equipableSlots.values())
		{
			for (EquipableSlot slot : slots)
			{
				if (slot.hasItemEquipped())
				{
					result.add(slot.getItem());
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return All items equipped by this character, EXCEPT any items equipped
	 * in BANNER slots. Will return an empty list if no items are equipped, never null.
	 */
	public List<Item> getEquippedNonBannerItems()
	{
		List<Item> result = new ArrayList<Item>();

		for (List<EquipableSlot> slots : equipableSlots.values())
		{
			for (EquipableSlot slot : slots)
			{
				if (slot.hasItemEquipped() && slot.getType() != EquipableSlot.Type.BANNER_ITEM)
				{
					result.add(slot.getItem());
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasItemEquipped(String itemName)
	{
		for (Item item : this.getEquippedItems())
		{
			if (item.getName().equals(itemName))
			{
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public CurMaxSub getHitPoints()
	{
		return getStats().getHitPoints();
	}

	/*-------------------------------------------------------------------------*/
	public CurMax getActionPoints()
	{
		return getStats().getActionPoints();
	}

	/*-------------------------------------------------------------------------*/
	public CurMax getMagicPoints()
	{
		return getStats().getMagicPoints();
	}

	/*-------------------------------------------------------------------------*/
	public Stance getStance()
	{
		return stance;
	}

	/*-------------------------------------------------------------------------*/
	public void setStance(Stance stance)
	{
		this.stance = stance;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return This PCs level in the given class
	 */
	public int getLevel(String className)
	{
		if (this.getLevels().containsKey(className))
		{
			return this.getLevels().get(className);
		}
		else
		{
			return 0;
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return This PCs level in his current class
	 */
	public int getCurrentClassLevel()
	{
		if (this.getCharacterClass() != null)
		{
			return this.getLevels().get(this.getCharacterClass().getName());
		}
		else
		{
			return this.getLevel();
		}
	}

	/*-------------------------------------------------------------------------*/
	public ActorGroup getActorGroup()
	{
		return group;
	}

	public void setActorGroup(ActorGroup group)
	{
		this.group = group;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Called when an item is added to this character's inventory. Subclasses
	 * must take appropriate action. At this point the item is already in the
	 * inventory.
	 */
	public abstract void inventoryItemAdded(Item item);

	/*-------------------------------------------------------------------------*/

	/**
	 * @return Action script for this actor.
	 */
	public abstract NpcScript getActionScript();

	/*-------------------------------------------------------------------------*/

	/**
	 * Smartly attempts to add the item to this actor. It is added to a slot
	 * if possible in a prioritised way.
	 * <ol>
	 *    <li>Primary weapon
	 *    <li>Secondary weapon
	 *    <li>any other equipable slot, starting with misc
	 *    <li>inventory
	 * </ol>
	 *
	 * @return
	 * 	true if the item was added, false otherwise
	 */
	public boolean addItemSmartly(Item item)
	{
		BitSet slots = item.getEquipableSlots();

		if (!this.meetsRequirements(item.getEquipRequirements()))
		{
			return false;
		}
		else if (slots == null)
		{
			return addInventoryItem(item);
		}
		else if (slots.get(PlayerCharacter.EquipableSlots.PRIMARY_WEAPON) &&
			this.hasEquipableSlot(PRIMARY_WEAPON) &&
			this.getPrimaryWeapon() == null)
		{
			System.out.println("primary weapon");
			this.setPrimaryWeapon(item);
			return true;
		}
		else if (slots.get(PlayerCharacter.EquipableSlots.SECONDARY_WEAPON) &&
			this.hasEquipableSlot(SECONDARY_WEAPON) &&
			this.getSecondaryWeapon() == null)
		{
			this.setSecondaryWeapon(item);
			return true;
		}
		else if (slots.get(PlayerCharacter.EquipableSlots.BANNER_ITEM) &&
			this.hasEquipableSlot(BANNER_ITEM) &&
			this.getBannerItem() == null)
		{
			this.setBannerItem(item);
			return true;
		}
		else if (slots.get(PlayerCharacter.EquipableSlots.MISC_ITEM_1) &&
			this.hasEquipableSlot(MISC_ITEM))
		{
			if (this.getMiscItem1() == null)
			{
				this.setMiscItem1(item);
				return true;
			}
			else if (this.getMiscItem2() == null)
			{
				this.setMiscItem2(item);
				return true;
			}
			else
			{
				return false;
			}
		}
		else if (slots.get(PlayerCharacter.EquipableSlots.HELM) &&
			this.hasEquipableSlot(HELM) &&
			this.getHelm() == null)
		{
			this.setHelm(item);
			return true;
		}
		else if (slots.get(PlayerCharacter.EquipableSlots.TORSO_ARMOUR) &&
			this.hasEquipableSlot(TORSO_ARMOUR) &&
			this.getTorsoArmour() == null)
		{
			this.setTorsoArmour(item);
			return true;
		}
		else if (slots.get(PlayerCharacter.EquipableSlots.LEG_ARMOUR) &&
			this.hasEquipableSlot(LEG_ARMOUR) &&
			this.getLegArmour() == null)
		{
			this.setLegArmour(item);
			return true;
		}
		else if (slots.get(PlayerCharacter.EquipableSlots.GLOVES) &&
			this.hasEquipableSlot(GLOVES) &&
			this.getGloves() == null)
		{
			this.setGloves(item);
			return true;
		}
		else if (slots.get(PlayerCharacter.EquipableSlots.BOOTS) &&
			this.hasEquipableSlot(BOOTS) &&
			this.getBoots() == null)
		{
			this.setBoots(item);
			return true;
		}
		else
		{
			return addInventoryItem(item);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Attempts to add an item to this characters inventory.
	 *
	 * @return false if the item could not be added; true if it could
	 */
	public boolean addInventoryItem(Item item)
	{
		boolean result = inventory.add(item);

		if (result)
		{
			inventoryItemAdded(item);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Item getArmour(BodyPart bodyPart)
	{
		EquipableSlot.Type type = bodyPart.getEquipableSlotType();

		if (type == NONE)
		{
			return null;
		}

		List<EquipableSlot> slots = getEquipableSlots().get(type);

		if (slots == null)
		{
			throw new MazeException(type.toString());
		}

		return slots.get(0).getItem();
	}

	/*-------------------------------------------------------------------------*/
	public void deductAmmo(StrikeEvent event)
	{
		Item ammoItem;

		if (event.getAttackWith().getAmmoRequired().contains(ItemTemplate.AmmoType.SELF))
		{
			ammoItem = (Item)event.getAttackWith();
		}
		else
		{
			ammoItem = getEquippedItem(SECONDARY_WEAPON, 0);
		}

		// returning items do no deduct ammo
		if (ammoItem != null && !ammoItem.isReturning())
		{
			ammoItem.getStack().decCurrent(1);

			if (ammoItem.getStack().getCurrent() == 0)
			{
				// Ammo expended, destroy item.
				removeItem(ammoItem, true);
			}
		}
		else
		{
			throw new MazeException("Attempt to deduct ammo when there is no " +
				"ammo.  Something is fuxored.");
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setModifier(Stats.Modifier modifier, int value)
	{
		this.getStats().setModifier(modifier, value);

		if (modifier.equals(Stats.Modifier.HIT_POINTS))
		{
			setCurMax(getHitPoints(), value);
		}
		else if (modifier.equals(Stats.Modifier.ACTION_POINTS))
		{
			setCurMax(getActionPoints(), value);
		}
		else if (modifier.equals(Stats.Modifier.MAGIC_POINTS))
		{
			setCurMax(getMagicPoints(), value);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void setCurMax(CurMax cm, int value)
	{
		cm.incMaximum(value);
		cm.incCurrent(value);
	}

	/*-------------------------------------------------------------------------*/
	public CombatantData getCombatantData()
	{
		return combatantData;
	}

	/*-------------------------------------------------------------------------*/
	public void setCombatantData(CombatantData combatantData)
	{
		this.combatantData = combatantData;
	}

	/*-------------------------------------------------------------------------*/
	public ModifierValue getModifierValue(Stats.Modifier modifier, boolean checkCC)
	{
		ModifierValue result = new ModifierValue();

		result.add(StringUtil.getUiLabel("mdw.influence.base"), this.getBaseModifier(modifier));

		//
		// Add this character's race, class and gender
		//
		if (this.getGender() != null)
		{
			result.add(StringUtil.getUiLabel("mdw.influence.gender", this.getGender().getName()),
				addModifier(modifier, this.getGender().getConstantModifiers()));
		}
		if (this.getRace() != null)
		{
			result.add(StringUtil.getUiLabel("mdw.influence.race", this.getRace().getName()),
				addModifier(modifier, this.getRace().getConstantModifiers()));
		}
		if (this.getCharacterClass() != null)
		{
			collectCharacterClassModifiers(modifier, result);
		}

		//
		// Add this character's equipment
		//
		for (Item item : getEquippedNonBannerItems())
		{
			result.add(item.getName(), addModifier(modifier, item));
		}

		//
		// Add the banner items of the whole party
		//
		result.add(collectBanners(modifier));

		//
		// Add this characters combat action and intention
		//
		CombatantData combatantData = this.getCombatantData();
		if (combatantData != null)
		{
			if (combatantData.getCurrentIntention() != null)
			{
				result.add(StringUtil.getUiLabel("mdw.influence.combat.intention"),
					addModifier(modifier, combatantData.getCurrentIntention().getStatModifier()));
			}
			CombatAction currentAction = combatantData.getCurrentAction();
			if (currentAction != null)
			{
				result.add(StringUtil.getUiLabel("mdw.influence.combat.action"),
					addModifier(modifier, currentAction.getModifiers()));
			}
			result.add(StringUtil.getUiLabel("mdw.influence.combatant.data"),
				addModifier(modifier, combatantData.getMiscModifiers()));
		}

		//
		// Add the modifiers of all conditions on this character
		//
		for (Condition c : ConditionManager.getInstance().getConditions(this))
		{
			result.add(c.getDisplayName(), c.getModifier(modifier, this));
		}

		//
		// Add the modifiers for the current tile
		//
		if (Maze.getInstance() != null && Maze.getInstance().getCurrentTile() != null)
		{
			// Tile modifiers
			result.add(StringUtil.getUiLabel("mdw.influence.current.tile"),
				addModifier(modifier, Maze.getInstance().getCurrentTile().getStatModifier()));

			// Conditions on the tile
			for (Condition c : Maze.getInstance().getCurrentTile().getConditions())
			{
				result.add(c.getDisplayName(), c.getModifier(modifier, this));
			}
		}

		//
		// Add any wielding combo
		//
		if (this.currentWieldingCombo != null)
		{
			result.add(StringUtil.getUiLabel("mdw.influence.wielding.combo"),
				currentWieldingCombo.getModifier(modifier));
		}

		//
		// Modifiers from Carrying Capacity
		//
		if (checkCC)
		{
			result.add(StringUtil.getUiLabel("mdw.influence.encumbrance"),
				addModifier(modifier, GameSys.getInstance().getModifierForCarryingCapacity(this)));
		}

		//
		// Modifiers for any special abilities
		//
		result.addAll(GameSys.getInstance().modifyModifierForSpecialAbility(this, modifier));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void collectCharacterClassModifiers(
		Stats.Modifier modifier,
		ModifierValue result)
	{
		List<LevelAbility> abilities = getLevelAbilities();

		for (LevelAbility ability : abilities)
		{
			// only stat modifier level abilities influence modifiers
			if (ability.getModifier().getModifier(modifier) != 0)
			{
				result.add(
					StringUtil.getUiLabel(
						"mdw.influence.class",
						this.getCharacterClass().getName(),
						StringUtil.getGamesysString(ability.getDisplayName(), false, ability.getDisplayArgs())),
					addModifier(modifier, ability.getModifier()));
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	all modifiers this character has as a result of their character class
	 * 	ability progression
	 */
	private StatModifier getCharacterClassModifiers()
	{
		StatModifier result = new StatModifier();

		List<LevelAbility> abilities = getLevelAbilities();

		for (LevelAbility ability : abilities)
		{
			// only stat modifier level abilities influence modifiers
			result.addModifiers(ability.getModifier());
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	Level abilities available to this actor at its current level
	 */
	public List<LevelAbility> getLevelAbilities()
	{
		List<LevelAbility> result = new ArrayList<LevelAbility>();

		for (String ccName : this.levels.keySet())
		{
			if (Database.getInstance().getCharacterClasses().containsKey(ccName))
			{
				CharacterClass cc = Database.getInstance().getCharacterClass(ccName);
				int ccLevel = this.getLevel(ccName);

				LevelAbilityProgression progression = cc.getProgression();

				result.addAll(progression.getForLevelCumulative(ccLevel));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * The level ability that represents the given spell.
	 */
	public abstract void removeLevelAbility(Spell spell);

	/*-------------------------------------------------------------------------*/
	private void collectCharacterClassBannerModifiers(
		UnifiedActor actor,
		Stats.Modifier modifier,
		ModifierValue result)
	{
		List<LevelAbility> abilities = actor.getLevelAbilities();

		for (LevelAbility ability : abilities)
		{
			// only banner modifier level abilities influence modifiers
			if (ability.getBannerModifier().getModifier(modifier) != 0)
			{
				result.add(
					actor.getName()+" ("+StringUtil.getGamesysString(ability.getDisplayName())+")",
					addModifier(modifier, ability.getBannerModifier()));
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The final value of the modifier for this actor, including all influences
	 * @param modifier
	 */
	public int getModifier(Stats.Modifier modifier)
	{
		return getModifier(modifier, true);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param modifier
	 * 	The modifier to return
	 * @param checkCC
	 * 	True if Carrying Capacity needs to be calculated
	 * @return
	 * 	The final value of the modifier for this actor, including all influences
	 */
	public int getModifier(Stats.Modifier modifier, boolean checkCC)
	{
		return getModifierValue(modifier, checkCC).getValue();
	}

	/*-------------------------------------------------------------------------*/
	protected ModifierValue collectBanners(Stats.Modifier modifier)
	{
		ModifierValue result = new ModifierValue();

		if (getActorGroup() == null)
		{
			if (Maze.getInstance() != null)
			{
				setActorGroup(Maze.getInstance().getParty());
			}

			if (getActorGroup() == null)
			{
				// no party, perhaps character creation?
				return result;
			}
		}

		for (UnifiedActor a : getActorGroup().getActors())
		{
			Item bannerItem = a.getEquippedItem(BANNER_ITEM, 0);
			if (bannerItem != null)
			{
				result.add(a.getName()+" ("+bannerItem.getDisplayName()+")",
					addModifier(modifier, bannerItem));
			}
			if (a.getGender() != null)
			{
				result.add(a.getName()+" ("+a.getGender().getName()+")",
					addModifier(modifier, a.getGender().getBannerModifiers()));
			}
			if (a.getRace() != null)
			{
				result.add(a.getName()+" ("+a.getRace().getName()+")",
					addModifier(modifier, a.getRace().getBannerModifiers()));
			}
			if (a.getCharacterClass() != null)
			{
				collectCharacterClassBannerModifiers(a, modifier, result);
			}

			result.add(a.collectConditionBanners(modifier));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected int addModifier(Stats.Modifier modifier, StatModifier stat)
	{
		return stat == null ? 0 : stat.getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/
	protected int addModifier(Stats.Modifier modifier, Item item)
	{
		return item == null ? 0 : addModifier(modifier, item.getModifiers());
	}

	/*-------------------------------------------------------------------------*/
	public ModifierValue collectConditionBanners(Stats.Modifier modifier)
	{
		ModifierValue result = new ModifierValue();
		for (Condition c : getConditions())
		{
			ConditionTemplate template = c.getTemplate();
			if (template != null)
			{
				result.add(c.getDisplayName(), addModifier(modifier, template.getBannerModifier()));
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return Base modifier + racial modifier + class modifier + gender modifier
	 */
	public int getIntrinsicModifier(Stats.Modifier modifier)
	{
		int result = this.getBaseModifier(modifier);

		if (this.getGender() != null)
		{
			result += addModifier(modifier, this.getGender().getConstantModifiers());
		}
		if (this.getRace() != null)
		{
			result += addModifier(modifier, this.getRace().getConstantModifiers());
		}
		if (this.getCharacterClass() != null)
		{
			result += addModifier(modifier, this.getCharacterClassModifiers());
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> addCondition(Condition c)
	{
		//
		// Check for condition-triggering modifiers
		//

		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();

		if (this.getModifier(Stats.Modifier.FURIOUS_PURPOSE) > 0 &&
			c.getEffect() instanceof KOEffect && !(c instanceof FatigueKO) &&
			this.isAlive() && this.isConscious())
		{
			// apply healing instead of KO
			int healing = this.getModifier(Stats.Modifier.FURIOUS_PURPOSE) * this.getLevel();
			result.add(new HealingEvent(this, healing));

			return result;
 		}
		else if (this.getModifier(Stats.Modifier.AMAZON_COURAGE) > 0 &&
			c.getEffect() instanceof FearEffect &&
			this.isAlive() && this.isConscious())
		{
			// apply an Amazon Courage condition instead of the fear condition
			ConditionTemplate ct = Database.getInstance().getConditionTemplate("amazon courage");
			Condition newC = ct.create(
				this,
				this,
				this.getLevel(),
				MagicSys.SpellEffectType.NONE,
				MagicSys.SpellEffectSubType.NONE);

			result.add(new UiMessageEvent(
				StringUtil.getEventText("msg.amazon.courage", this.getDisplayName())));
			result.add(new ConditionEvent(this, newC));

			return result;
		}
		else if (this.getModifier(Stats.Modifier.AMAZON_WILLPOWER) > 0 &&
			(c.getEffect() instanceof InsaneEffect ||
				c.getEffect() instanceof PossessionEffect) &&
			this.isAlive() && this.isConscious())
		{
			// apply an Amazon Willpower condition instead of the fear condition
			ConditionTemplate ct = Database.getInstance().getConditionTemplate("amazon willpower");
			Condition newC = ct.create(
				this,
				this,
				this.getLevel(),
				MagicSys.SpellEffectType.NONE,
				MagicSys.SpellEffectSubType.NONE);

			result.add(new UiMessageEvent(
				StringUtil.getEventText("msg.amazon.willpower", this.getDisplayName())));
			result.add(new ConditionEvent(this, newC));

			return result;
		}
		else if (this.getModifier(Stats.Modifier.AMAZON_FURY) > 0 &&
			(c.getEffect() instanceof WebEffect ||
				(c.getEffect() instanceof SleepEffect && !(c instanceof RestingSleep)) ||
				c.getEffect() instanceof SlowEffect ||
				c.getEffect() instanceof ParalyseEffect)
			&&
			this.isAlive() && this.isConscious())
		{
			// apply an Amazon Fury condition instead of the fear condition
			ConditionTemplate ct = Database.getInstance().getConditionTemplate("amazon fury");
			Condition newC = ct.create(
				this,
				this,
				this.getLevel(),
				MagicSys.SpellEffectType.NONE,
				MagicSys.SpellEffectSubType.NONE);

			result.add(new UiMessageEvent(
				StringUtil.getEventText("msg.amazon.fury", this.getDisplayName())));
			result.add(new ConditionEvent(this, newC));

			return result;
		}


		//
		// Prevent duplicates.  The rules are:
		// 1. Only one of each specific type of condition
		//     1.1) Conditions are typed by Condition Effect.
		//     1.2) Except if multiples are allowed.
		// 2. For untyped conditions, only one of each name.
		// 3. In both cases the new condition must have strength > the old one
		//    to replace it.
		//

		for (Condition condition : ConditionManager.getInstance().getConditions(this))
		{
			if (condition.getEffect() == c.getEffect() &&
				c.getEffect() != ConditionEffect.NONE &&
				!c.getEffect().isMultiplesAllowed())
			{
				// conditions of the same type, just check strengths
				if (condition.getStrength() < c.getStrength())
				{
					// The new condition will replace this one
					removeCondition(condition);
					break;
				}
				else
				{
					// The old condition will remain
					return result;
				}
			}
			else if (!c.getEffect().isMultiplesAllowed())
			{
				// untyped condition, check the name
				if (condition.getName().equals(c.getName()))
				{
					if (condition.getStrength() < c.getStrength())
					{
						// The new condition will replace this one
						removeCondition(condition);
						break;
					}
					else
					{
						// The old condition will remain
						return result;
					}
				}
			}
		}

		ConditionManager.getInstance().addCondition(this, c);

		// should this be in the subclass?
		if (this instanceof PlayerCharacter)
		{
			result.addAll(SpeechUtil.getInstance().conditionSpeech(c, (PlayerCharacter)this));
		}

		return result;
	}

	public void removeCondition(Condition c)
	{
		ConditionManager.getInstance().removeCondition(this, c);
	}

	public List<Condition> getConditions()
	{
		return ConditionManager.getInstance().getConditions(this);
	}

	/*-------------------------------------------------------------------------*/
	public int getCarrying()
	{
		int result = 0;

		for (Item item : getEquippedItems())
		{
			result += getWeight(item);
		}

		for (Item i : getInventory())
		{
			result += getWeight(i);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private int getWeight(Item item)
	{
		return (item == null) ? 0 : item.getWeight();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return True if this character can cast the given spell, based on stuff like
	 *         the spells mana requirements and this characters current
	 *         conditions.
	 */
	public boolean canCast(Spell s)
	{
		//
		// Check requirements.
		//
		if (!s.meetsRequirements(this))
		{
			return false;
		}

		//
		// Check available magic points
		//
		if (!GameSys.getInstance().canPaySpellCost(s, 1, this))
		{
			// cannot cast this spell even on first level
			return false;
		}

		//
		// Check spell usability type
		//
		if (s.getUsabilityType() == MagicSys.SpellUsabilityType.COMBAT_ONLY &&
			Maze.getInstance().getState() != Maze.State.COMBAT)
		{
			Maze.log(Log.DEBUG, s.getName()+": cannot cast [" +
				MagicSys.SpellUsabilityType.describe(s.getUsabilityType()) +
				"] spell in state " + Maze.getInstance().getState());
			return false;
		}
		else if (s.getUsabilityType() == MagicSys.SpellUsabilityType.NPC_ONLY &&
			Maze.getInstance().getState() != Maze.State.ENCOUNTER_ACTORS)
		{
			Maze.log(Log.DEBUG, s.getName()+": cannot cast [" +
				MagicSys.SpellUsabilityType.describe(s.getUsabilityType()) +
				"] spell in state " + Maze.getInstance().getState());
			return false;
		}
		else if (s.getUsabilityType() == MagicSys.SpellUsabilityType.LOCKS_TRAPS_ONLY &&
			!(Maze.getInstance().getState() == Maze.State.ENCOUNTER_CHEST ||
				Maze.getInstance().getState() == Maze.State.ENCOUNTER_PORTAL))
		{
			Maze.log(Log.DEBUG, s.getName()+": cannot cast [" +
				MagicSys.SpellUsabilityType.describe(s.getUsabilityType()) +
				"] spell in state " + Maze.getInstance().getState());
			return false;
		}
		else if (Maze.getInstance().getState() == Maze.State.INVENTORY &&
			s.getUsabilityType() != MagicSys.SpellUsabilityType.INVENTORY_SCREEN_ONLY
			||
			Maze.getInstance().getState() != Maze.State.INVENTORY &&
				s.getUsabilityType() == MagicSys.SpellUsabilityType.INVENTORY_SCREEN_ONLY)
		{
			Maze.log(Log.DEBUG, s.getName()+": cannot cast [" +
				MagicSys.SpellUsabilityType.describe(s.getUsabilityType()) +
				"] spell in state " + Maze.getInstance().getState());
			// from inventory screen, allow only these spells to be cast
			return false;
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	public boolean canUseSpellLikeAbility(SpellLikeAbility sla)
	{
		// todo:
		return true;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return true if this actor meets the given requirements based on its
	 * 	current modifiers.
	 */
	public boolean meetsRequirements(StatModifier req)
	{
		return meetsRequirements(req, true, true);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param nonAttributeModifiers
	 * 	pass false if non-attribute modifiers should be considered
	 * @param nonBaseModifiers
	 * 	pass false if only base modifiers should be considered
	 * @return true if this actor meets the given requirements based on either its
	 * 	current or base modifiers.
	 */
	public boolean meetsRequirements(StatModifier req,
		boolean nonBaseModifiers,
		boolean nonAttributeModifiers)
	{
		if (req == null)
		{
			return true;
		}

		for (Stats.Modifier modifier : req.getModifiers().keySet())
		{
			if (!Stats.attributeModifiers.contains(modifier) && !nonAttributeModifiers)
			{
				// we only care about attribute modifiers
				continue;
			}

			int cur;
			if (nonBaseModifiers)
			{
				cur = getModifier(modifier);
			}
			else
			{
				cur = getBaseModifier(modifier);
			}

			if (cur < req.getModifier(modifier))
			{
				return false;
			}
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	public int getAmountBlueMagic()
	{
		return getModifier(Stats.Modifier.BLUE_MAGIC_GEN);
	}

	public int getAmountGoldMagic()
	{
		return getModifier(Stats.Modifier.GOLD_MAGIC_GEN);
	}

	public int getAmountGreenMagic()
	{
		return getModifier(Stats.Modifier.GREEN_MAGIC_GEN);
	}

	public int getAmountPurpleMagic()
	{
		return getModifier(Stats.Modifier.PURPLE_MAGIC_GEN);
	}

	public int getAmountRedMagic()
	{
		return getModifier(Stats.Modifier.RED_MAGIC_GEN);
	}

	public int getAmountWhiteMagic()
	{
		return getModifier(Stats.Modifier.WHITE_MAGIC_GEN);
	}

	public int getAmountBlackMagic()
	{
		return getModifier(Stats.Modifier.BLACK_MAGIC_GEN);
	}

	public int getAmountMagicPresent(int type)
	{
		switch (type)
		{
			case MagicSys.ManaType.RED:
				return getAmountRedMagic();
			case MagicSys.ManaType.BLACK:
				return getAmountBlackMagic();
			case MagicSys.ManaType.PURPLE:
				return getAmountPurpleMagic();
			case MagicSys.ManaType.GOLD:
				return getAmountGoldMagic();
			case MagicSys.ManaType.WHITE:
				return getAmountWhiteMagic();
			case MagicSys.ManaType.GREEN:
				return getAmountGreenMagic();
			case MagicSys.ManaType.BLUE:
				return getAmountBlueMagic();
			default:
				throw new MazeException("Invalid mana colour " + type);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<Item> getAllItems()
	{
		List<Item> result = new ArrayList<Item>();

		for (Item i : getEquippedItems())
		{
			result.add(i);
		}

		result.addAll(getInventory().getItems());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void regenerateResources(long turnNr,
		boolean resting,
		boolean combat,
		Tile tile)
	{
		// check if alive
		if (getHitPoints().getCurrent() <= 0)
		{
			return;
		}

		int hpRegen = GameSys.getInstance().getHitPointsToRegeneratePerTurn(
			this, turnNr, resting, getActorGroup());
		getHitPoints().incCurrent(hpRegen);

		int actionRegen = GameSys.getInstance().getActionPointsToRegeneratePerTurn(
			this, turnNr, resting, getActorGroup(), tile);
		getActionPoints().incCurrent(actionRegen);

		int magicRegen = GameSys.getInstance().getMagicPointsToRegeneratePerTurn(
			this, turnNr, resting, getActorGroup());
		getMagicPoints().incCurrent(magicRegen);

		int fatigueRegen = 0;
		if (Maze.getInstance().getState() == Maze.State.RESTING ||
			Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			fatigueRegen = GameSys.getInstance().getFatigueToRegenWhileMoving(this, resting);
			getHitPoints().incSub(-fatigueRegen);
		}

		Maze.log(Log.DEBUG, getName() + " regen " + hpRegen + " hit points (fatigue -" + fatigueRegen + ")");
		Maze.log(Log.DEBUG, getName() + " regen " + actionRegen + " action points");
		Maze.log(Log.DEBUG, getName() + " regen " + magicRegen + " magic points");
	}

	/*-------------------------------------------------------------------------*/
	private WieldingCombo getCurrentWieldingCombo()
	{
		Map<String, WieldingCombo> combos = Database.getInstance().getWieldingCombos();

		for (WieldingCombo c : combos.values())
		{
			if (c.matches(getEquippedItem(PRIMARY_WEAPON, 0), getEquippedItem(SECONDARY_WEAPON, 0)))
			{
				return c;
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return true if this actor is alive
	 */
	public boolean isAlive()
	{
		return getHitPoints().getCurrent()>0;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return true if this actor is conscious
	 */
	public boolean isConscious()
	{
		return getHitPoints().getCurrent()> getHitPoints().getSub();
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasEquipableSlot(EquipableSlot.Type slot)
	{
		return equipableSlots.containsKey(slot);
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasEquipableSlot(EquipableSlot.Type slot, int index)
	{
		return equipableSlots.containsKey(slot) &&
			equipableSlots.get(slot).size() > index;
	}

	/*-------------------------------------------------------------------------*/
	public List<CombatAction> getCombatActions(ActorActionIntention intention)
	{
		return ActorIntentionResolver.getCombatActions(this, intention);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	All pre-combat actions that character special abilities allow this
	 * 	character to take. Only one is supported, so if there are multiple
	 * 	choose one at random.
	 */
	public ActorActionIntention getPreCombatIntentions(Combat combat)
	{
		List<ActorActionIntention> possibilities = new ArrayList<ActorActionIntention>();

		if (getModifier(Stats.Modifier.IAJUTSU) > 0)
		{
			// check it's a sword with the KENDO focus
			Item primaryWeapon = getPrimaryWeapon();
			if (primaryWeapon != null &&
				primaryWeapon.getSubType() == ItemTemplate.WeaponSubType.SWORD &&
				Stats.Modifier.KENDO.equals(primaryWeapon.getDiscipline()))
			{
				possibilities.add(
					new SpecialAbilityIntention(
						combat.getRandomFoeOf(this),
						Database.getInstance().getSpell("_IAJUTSU_"),
						this.getLevel()));
			}
		}

		if (!possibilities.isEmpty())
		{
			return possibilities.get(new Dice(1,possibilities.size(),-1).roll("UnifiedActor.getPreCombatIntentions"));
		}
		else
		{
			return ActorActionIntention.INTEND_NOTHING;
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The maximum amount of gold that can be stolen from this character in
	 * 	one theft event
	 */
	public int getMaxStealableGold()
	{
		return 0;
	}

	/*-------------------------------------------------------------------------*/
	public abstract List<Item> getStealableItems();
	public abstract List<AttackWith> getAttackWithOptions();
	public abstract List<TypeDescriptor> getTypes();
	public abstract int getBaseModifier(Stats.Modifier modifier);
	public abstract String getDisplayName();
	public abstract String getDisplayNamePlural();
	public abstract void removeItem(Item item, boolean removeWholeStack);
	public abstract void removeItem(String itemName, boolean removeWholeStack);
	public abstract void removeCurse(int strength);
	public abstract void addAllies(List<FoeGroup> foeGroups);
	public abstract boolean isActiveModifier(Stats.Modifier modifier);

	public abstract CharacterClass.Focus getFocus();

	public abstract String getFaction();


	/*-------------------------------------------------------------------------*/
	public static enum Stance
	{
		DEAD("aao.dead", 0),
		UNAWARE("aao.unaware", 1),
		SNAKESPEED("aso.snakespeed", 12),
		ACT_EARLY("aso.act.early", 8),
		ACT_LATE("aso.act.late", 4),
		PATIENCE("aso.patience", 2);

		private String key;
		private int priority; // higher is faster

		Stance(String key, int priority)
		{
			this.key = key;
			this.priority = priority;
		}

		public int getPriority()
		{
			return priority;
		}

		public String getKey()
		{
			return key;
		}

		@Override
		public String toString()
		{
			return StringUtil.getUiLabel(key);
		}
	}
}
