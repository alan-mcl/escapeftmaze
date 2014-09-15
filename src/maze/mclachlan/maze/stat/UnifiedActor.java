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
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.combat.CombatAction;
import mclachlan.maze.stat.combat.CombatantData;
import mclachlan.maze.stat.combat.WieldingCombo;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.condition.*;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellBook;
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

	/** Natural weapons of this actor */
	private List<NaturalWeapon> naturalWeapons;

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
		List<NaturalWeapon> naturalWeapons,
		Map<String, Integer> levels,
		Stats stats, SpellBook spellBook,
		Inventory inventory)
	{
		this.characterClass = characterClass;
		this.gender = gender;
		this.naturalWeapons = naturalWeapons;
		this.inventory = inventory;
		this.levels = levels;
		this.name = name;
		this.race = race;
		this.spellBook = spellBook;
		this.stats = stats;
		this.bodyParts = bodyParts;

		for (int i = 0; i < MAX_PACK_ITEMS; i++)
		{
			getInventory().add(null);
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
		this.naturalWeapons = other.naturalWeapons;

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
		return naturalWeapons;
	}

	public void setNaturalWeapons(List<NaturalWeapon> naturalWeapons)
	{
		this.naturalWeapons = naturalWeapons;
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
		return getStats().hitPoints;
	}

	/*-------------------------------------------------------------------------*/
	public CurMax getActionPoints()
	{
		return getStats().actionPoints;
	}

	/*-------------------------------------------------------------------------*/
	public CurMax getMagicPoints()
	{
		return getStats().magicPoints;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return This PCs level in the given class
	 */
	public int getLevel(String className)
	{
		return this.getLevels().get(className);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return This PCs level in his current class
	 */
	public int getCurrentClassLevel()
	{
		return this.getLevels().get(this.getCharacterClass().getName());
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
		List<EquipableSlot> slots = getEquipableSlots().get(type);

		if (slots == null)
		{
			throw new MazeException(type.toString());
		}

		return slots.get(0).getItem();
	}

	/*-------------------------------------------------------------------------*/
	public void deductAmmo(AttackEvent event)
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
	public void setModifier(String modifier, int value)
	{
		this.getStats().setModifier(modifier, value);

		if (modifier.equals(Stats.Modifiers.HIT_POINTS))
		{
			setCurMax(getHitPoints(), value);
		}
		else if (modifier.equals(Stats.Modifiers.ACTION_POINTS))
		{
			setCurMax(getActionPoints(), value);
		}
		else if (modifier.equals(Stats.Modifiers.MAGIC_POINTS))
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
	public ModifierValue getModifierValue(String modifier, boolean checkCC)
	{
		ModifierValue result = new ModifierValue();

		result.add(StringUtil.getUiLabel("mdw.influence.base"), this.getBaseModifier(modifier));

		// Add this character's race, class and gender
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
		// Add this character's equipment
		for (Item item : getEquippedNonBannerItems())
		{
			result.add(item.getName(), addModifier(modifier, item));
		}

		// Add the banner items of the whole party
		result.add(collectBanners(modifier));

		// Add this characters combat action and intention
		CombatantData combatantData = this.getCombatantData();
		if (combatantData != null)
		{
			if (combatantData.getCurrentIntention() != null)
			{
				result.add(StringUtil.getUiLabel("mdw.influence.combat.intention"),
					addModifier(modifier, combatantData.getCurrentIntention().getStatModifier()));
			}
			result.add(StringUtil.getUiLabel("mdw.influence.combat.action"),
				addModifier(modifier, combatantData.getCurrentAction()));
			result.add(StringUtil.getUiLabel("mdw.influence.combatant.data")
				, addModifier(modifier, combatantData.getMiscModifiers()));
		}

		// Add the modifiers of all conditions on this character
		for (Condition c : ConditionManager.getInstance().getConditions(this))
		{
			result.add(c.getName(), c.getModifier(modifier, this));
		}

		// Add the modifiers of all conditions on the current tile
		if (Maze.getInstance() != null && Maze.getInstance().getCurrentTile() != null)
		{
			// Add the modifiers of the current tile
			result.add(StringUtil.getUiLabel("mdw.influence.current.tile"), addModifier(modifier, Maze.getInstance().getCurrentTile().getStatModifier()));

			for (Condition c : Maze.getInstance().getCurrentTile().getConditions())
			{
				result.add(c.getName(), c.getModifier(modifier, this));
			}
		}

		// add any wielding combo
		if (this.currentWieldingCombo != null)
		{
			result.add(StringUtil.getUiLabel("mdw.influence.wielding.combo"), currentWieldingCombo.getModifier(modifier));
		}

		if (checkCC)
		{
			result.add(StringUtil.getUiLabel("mdw.influence.encumbrance"), addModifier(modifier, GameSys.getInstance().getModifierForCarryingCapacity(this)));
		}

		result.add(GameSys.getInstance().modifyModifierForSpecialAbility(this, modifier));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void collectCharacterClassModifiers(
		String modifier,
		ModifierValue result)
	{
		List<LevelAbility> abilities = getLevelAbilities();

		for (LevelAbility ability : abilities)
		{
			// only stat modifier level abilities influence modifiers
			if (ability instanceof StatModifierLevelAbility)
			{
				result.add(
					StringUtil.getUiLabel(
						"mdw.influence.class",
						this.getCharacterClass().getName(),
						StringUtil.getGamesysString(ability.getDisplayName(), false, ability.getDisplayArgs())),
					addModifier(modifier, ((StatModifierLevelAbility)ability).getModifier()));
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
			if (ability instanceof StatModifierLevelAbility)
			{
				result.addModifiers(((StatModifierLevelAbility)ability).getModifier());
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<LevelAbility> getLevelAbilities()
	{
		List<LevelAbility> result = new ArrayList<LevelAbility>();

		for (String ccName : this.levels.keySet())
		{
			CharacterClass cc = Database.getInstance().getCharacterClass(ccName);
			int ccLevel = this.getLevel(ccName);

			LevelAbilityProgression progression = cc.getProgression();

			result.addAll(progression.getForLevelCumulative(ccLevel));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void collectCharacterClassBannerModifiers(
		UnifiedActor actor,
		String modifier,
		ModifierValue result)
	{
		List<LevelAbility> abilities = actor.getLevelAbilities();

		for (LevelAbility ability : abilities)
		{
			// only banner modifier level abilities influence modifiers
			if (ability instanceof BannerModifierLevelAbility)
			{
				result.add(
					actor.getName()+" ("+StringUtil.getGamesysString(ability.getDisplayName())+")",
					addModifier(modifier, ((BannerModifierLevelAbility)ability).getModifier()));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getModifier(String modifier)
	{
		return getModifier(modifier, true);
	}

	/*-------------------------------------------------------------------------*/
	public int getModifier(String modifier, boolean checkCC)
	{
		return getModifierValue(modifier, checkCC).getValue();
	}

	/*-------------------------------------------------------------------------*/
	private ModifierValue collectBanners(String modifier)
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
	private int addModifier(String modifier, StatModifier stat)
	{
		return stat == null ? 0 : stat.getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/
	private int addModifier(String modifier, Item item)
	{
		return item == null ? 0 : addModifier(modifier, item.getModifiers());
	}

	/*-------------------------------------------------------------------------*/
	public ModifierValue collectConditionBanners(String modifier)
	{
		ModifierValue result = new ModifierValue();
		for (Condition c : getConditions())
		{
			ConditionTemplate template = c.getTemplate();
			if (template != null)
			{
				result.add(c.getName(), addModifier(modifier, template.getBannerModifier()));
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return Base modifier + racial modifier + class modifier + gender modifier
	 */
	public int getIntrinsicModifier(String modifier)
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
	public void addCondition(Condition c)
	{
		//
		// Prevent duplicates.  The rules are:
		// 1. Only one of each specific type of condition.
		// 2. For untyped conditions, only one of each name.
		// 3. In both cases the new condition must have strength > the old one
		//    to replace it.
		//

		for (Condition condition : ConditionManager.getInstance().getConditions(this))
		{
			if (condition.getEffect() == c.getEffect() && c.getEffect() != ConditionEffect.NONE)
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
					return;
				}
			}
			else
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
						return;
					}
				}
			}
		}

		ConditionManager.getInstance().addCondition(this, c);
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
		if (!(getMagicPoints().getCurrent() >= MagicSys.getInstance().getMagicPointCost(s, 1, this)))
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
			!Maze.getInstance().containsState(Maze.State.ENCOUNTER_NPC))
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

		for (String modifier : req.getModifiers().keySet())
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
		return getModifier(Stats.Modifiers.BLUE_MAGIC_GEN);
	}

	public int getAmountGoldMagic()
	{
		return getModifier(Stats.Modifiers.GOLD_MAGIC_GEN);
	}

	public int getAmountGreenMagic()
	{
		return getModifier(Stats.Modifiers.GREEN_MAGIC_GEN);
	}

	public int getAmountPurpleMagic()
	{
		return getModifier(Stats.Modifiers.PURPLE_MAGIC_GEN);
	}

	public int getAmountRedMagic()
	{
		return getModifier(Stats.Modifiers.RED_MAGIC_GEN);
	}

	public int getAmountWhiteMagic()
	{
		return getModifier(Stats.Modifiers.WHITE_MAGIC_GEN);
	}

	public int getAmountBlackMagic()
	{
		String modifier = Stats.Modifiers.BLACK_MAGIC_GEN;
		return getModifier(modifier);
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

		int hpRegen = GameSys.getInstance().getHitPointsToRegenerate(
			this, turnNr, resting, getActorGroup());
		getHitPoints().incCurrent(hpRegen);

		int magicRegen = GameSys.getInstance().getMagicPointsToRegenerate(
			this, turnNr, resting, getActorGroup());
		getMagicPoints().incCurrent(magicRegen);

		int actionRegen = 0;
		if (combat)
		{
			actionRegen = GameSys.getInstance().getActionPointsToRegenerateInCombat(
				this, turnNr);
		}
		else if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			actionRegen = GameSys.getInstance().getActionPointsToRegenerateWhileMoving(this, tile);
		}
		getActionPoints().incCurrent(actionRegen);

		int fatigueRegen = 0;
		if (Maze.getInstance().getState() == Maze.State.RESTING ||
			Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			fatigueRegen = GameSys.getInstance().getFatigueToRegenWhileMoving(this, resting);
			getHitPoints().incSub(-fatigueRegen);
		}

		Maze.log(Log.DEBUG, getName() + " regen " + hpRegen + " hit points (fatigue -" + fatigueRegen + ")");
		Maze.log(Log.DEBUG, getName() + " regen " + magicRegen + " magic points");
		Maze.log(Log.DEBUG, getName() + " regen " + actionRegen + " action points");
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
	public abstract List<CombatAction> getCombatActions(
		ActorActionIntention actionIntention);
	public abstract String getType();
	public abstract int getBaseModifier(String modifier);
	public abstract String getDisplayName();
	public abstract void removeItem(Item item, boolean removeWholeStack);
	public abstract void removeItem(String itemName, boolean removeWholeStack);
	public abstract void removeCurse(int strength);
	public abstract void addAllies(List<FoeGroup> foeGroups);
	public abstract boolean isActiveModifier(String modifier);
	public abstract List<AttackWith> getAttackWithOptions();
	public abstract List<SpellLikeAbility> getSpellLikeAbilities();
}
