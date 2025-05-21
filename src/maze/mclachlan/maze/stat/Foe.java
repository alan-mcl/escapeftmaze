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
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.game.*;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.CombatantData;
import mclachlan.maze.stat.combat.DefaultFoeAiScript;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.stat.magic.ValueList;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Foe extends UnifiedActor
{
	protected FoeTemplate template;

	/** The ActorGroup that this foe belongs to */
	private FoeGroup foeGroup;

	/** Temp data for each combat round */
	private CombatantData combatantData;
	
	/** A handle to this foe's CrusaderEngine object */
	private EngineObject sprite;

	/** Whether the PCs have figured out what the heck this is. A constant from
	 * {@link Item.IdentificationState} */
	private int identificationState = Item.IdentificationState.UNIDENTIFIED;

	/** whether this foes is around because of a summon spell */
	private boolean isSummoned;

	/*-------------------------------------------------------------------------*/

	public Foe()
	{
	}


	/*-------------------------------------------------------------------------*/

	/**
	 * Creates a new foe from the given foe template
	 */
	public Foe(FoeTemplate template)
	{
		// todo: gender, race and class for foes
		super(
			template.getName(),
			null,
			null,
			null,
			template.getBodyParts(),
			null,
			new Stats(template.getStats()),
			template.getSpellBook(),
			new Inventory(MAX_PACK_ITEMS));

		this.template = template;

		// set level
		HashMap<String, Integer> levels = new HashMap<>();
		levels.put(template.getName(), template.getLevelRange().roll("Foe: level"));
		this.setLevels(levels);

		// roll up this foes vitals
		int maxHP = template.getHitPointsRange().roll("Foe: hp");
		int maxStealth = template.getActionPointsRange().roll("Foe: ap");
		int maxMagic = template.getMagicPointsRange().roll("Foe: mp");

		getStats().setHitPoints(new CurMaxSub(maxHP));
		getStats().setActionPoints(new CurMax(maxStealth));
		getStats().setMagicPoints(new CurMax(maxMagic));

		if (template.getIdentificationDifficulty() == 0)
		{
			identificationState = Item.IdentificationState.IDENTIFIED;
		}

		Maze instance = Maze.getInstance();
		if (instance != null)
		{
			// apply difficulty levels
			DifficultyLevel dl = instance.getDifficultyLevel();
			if (dl != null)
			{
				dl.foeIsSpawned(this);
			}

			// generate and equip inventory
			generateInventory();
			initialEquip();

			Maze.log(Log.DEBUG, "Spawned [" + template.getName() + "] " +
				"hp=[" + getStats().getHitPoints().getCurrent() + "] " +
				"sp=[" + getStats().getActionPoints().getCurrent() + "] " +
				"mp=[" + getStats().getMagicPoints().getCurrent() + "]");
		}
	}

	/*-------------------------------------------------------------------------*/

	public void setTemplate(FoeTemplate template)
	{
		this.template = template;

		super.setName(template.getName());
		super.setBodyParts(template.getBodyParts());
		super.setStats(new Stats(template.getStats()));
		super.setSpellBook(template.getSpellBook());
		super.setInventory(new Inventory(MAX_PACK_ITEMS));
	}

	/*-------------------------------------------------------------------------*/
	public void generateInventory()
	{
		Maze.log(Log.DEBUG, "generating inventory for "+template.getName());

		LootTable lootTable = getLootTable();
		if (lootTable == null)
		{
			return;
		}

		// clear existing inventory
		getInventory().clear();
		for (EquipableSlot slot : getAllEquipableSlots())
		{
			slot.setItem(null);
		}

		GroupOfPossibilities<ILootEntry> lootEntries = lootTable.getLootEntries();
		if (lootEntries != null)
		{
			List<ILootEntry> entries = lootEntries.getRandom();
			List<Item> items = LootEntry.generate(entries);

			for (Item i : items)
			{
				Maze.log(Log.DEBUG, template.getName()+" carries "+i.getName());

				addInventoryItem(i);
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * This foe gets a chance to look through what is in its inventory and
	 * equip items in any available slots
	 */
	public void initialEquip()
	{
		Maze.log(Log.DEBUG, template.getName()+" organises inventory");
		Maze.log(Log.DEBUG, template.getName()+" slots: ["+this.getAllEquipableSlots()+"]");
		Maze.log(Log.DEBUG, template.getName()+" inventory: ["+this.getInventory()+"]");

		for (EquipableSlot slot : this.getAllEquipableSlots())
		{
			List<Item> inventory = this.getInventory().getItems();
			Item item = getBestItemForSlot(slot.getType(), inventory);
			if (item != null)
			{
				Maze.log(Log.MEDIUM, template.getName()+" equips "+item.getName()+" in "+slot.getType()+" ("+slot.getName()+")");
				this.setEquippedItem(slot.getType(), item);
				this.getInventory().remove(item);
			}
			else
			{
				slot.setItem(null);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private Item getBestItemForSlot(EquipableSlot.Type slotType, List<Item> items)
	{
		Item bestItem = null;

		for (Item item : items)
		{
			boolean meetsRequirements = this.meetsRequirements(item.getEquipRequirements());
			boolean contains = item.getEquipableSlotTypes().contains(slotType);

			if (meetsRequirements &&
				contains)
			{
				if (bestItem == null)
				{
					bestItem = item;
				}
				else if (item.getBaseCost() > bestItem.getBaseCost())
				{
					// todo: better item scoring that just base cost
					bestItem = item;
				}
			}
		}

		return bestItem;
	}

	/*-------------------------------------------------------------------------*/
	public ActorActionIntention getCombatIntention()
	{
		Combat combat = this.combatantData.getCombat();

		// party gets a free round (foes with QUICK WITS can act)
		Combat.AmbushStatus ambushStatus = combat.getAmbushStatus();

		boolean foeIsSurprised =
			ambushStatus == Combat.AmbushStatus.PARTY_MAY_AMBUSH_FOES ||
			ambushStatus == Combat.AmbushStatus.PARTY_MAY_AMBUSH_OR_EVADE_FOES;

		if (foeIsSurprised && getModifier(Stats.Modifier.QUICK_WITS)<=0)
		{
			return ActorActionIntention.INTEND_NOTHING;
		}

		// dead or immobilised foes do nothing
		if (this.getHitPoints().getCurrent() <= 0 ||
			!GameSys.getInstance().askActorForCombatIntentions(this))
		{
			return ActorActionIntention.INTEND_NOTHING;
		}

		FoeCombatAi ai = Maze.getInstance().getDifficultyLevel().getFoeCombatAi();
		return ai.getCombatIntention(this, combat);
	}

	/*-------------------------------------------------------------------------*/
	public int getStealthBehaviour()
	{
		return template.getStealthBehaviour();
	}

	/*-------------------------------------------------------------------------*/
	public int getFleeChance()
	{
		return template.getFleeChance();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Asks this foe (who has been nominated leader of the bunch of foes), if
	 * they should evade the party.
	 */
	public boolean shouldEvade(List<FoeGroup> groups, PlayerParty party)
	{
		FoeCombatAi ai = Maze.getInstance().getDifficultyLevel().getFoeCombatAi();
		return ai.shouldEvade(this, groups, party);
	}

	/*-------------------------------------------------------------------------*/
	public ActorGroup getActorGroup()
	{
		return foeGroup;
	}

	@Override
	public void inventoryItemAdded(Item item)
	{
	}

	public boolean isFound()
	{
		return false;
	}

	public void setFound(boolean found)
	{
		// no op
	}

	@Override
	public NpcScript getActionScript()
	{
		return new DefaultFoeAiScript(Maze.getInstance().getCurrentActorEncounter());
	}

	/*-------------------------------------------------------------------------*/
	public void removeCurse(int strength)
	{
		// no effect on foes
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void addAllies(List<FoeGroup> foeGroups)
	{
		Maze.getInstance().addFoeAllies(foeGroups);
		if (this.combatantData != null)
		{
			this.combatantData.setSummonedGroup(foeGroups);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isActiveModifier(Stats.Modifier modifier)
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<SpellLikeAbility> getSpellLikeAbilities()
	{
		List<SpellLikeAbility> result = new ArrayList<>(super.getSpellLikeAbilities());

		if (template.getSpellLikeAbilities() != null)
		{
			result.addAll(template.getSpellLikeAbilities());
		}

		if (template.getTypes() != null)
		{
			for (FoeType ft : template.getTypes())
			{
				if (ft.getSpecialAbility() != null)
				{
					result.add(new SpellLikeAbility(
						ft.getSpecialAbility(),
						new ValueList(new Value(getLevel(), Value.SCALE.NONE))));
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public List<NaturalWeapon> getNaturalWeapons()
	{
		List<NaturalWeapon> result = new ArrayList<>(super.getNaturalWeapons());

		// template natural weapons
		if (template.getNaturalWeapons() != null)
		{
			for (String nw : template.getNaturalWeapons())
			{
				result.add(Database.getInstance().getNaturalWeapons().get(nw));
			}
		}

		// foe types
		if (template.getTypes() != null)
		{
			for (FoeType ft : template.getTypes())
			{
				if (ft.getNaturalWeapons() != null)
				{
					result.addAll(ft.getNaturalWeapons());
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public CharacterClass.Focus getFocus()
	{
		return template.getFocus();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public PercentageTable<BodyPart> getBodyParts()
	{
		return this.template.getBodyParts();
	}
	
	/*-------------------------------------------------------------------------*/
	public PercentageTable<String> getPlayerBodyParts()
	{
		return this.template.getPlayerBodyParts();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Item getArmour(BodyPart bodyPart)
	{
		// foes are only treated as having natural armour, on their BodyParts
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return template.getName();
	}

	/*-------------------------------------------------------------------------*/
	public boolean isNpc()
	{
		return template.isNpc();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A display name for this foe that depends on whether or not it has
	 * 	been identified.
	 */
	public String getDisplayName()
	{
		return switch (getIdentificationState())
			{
				case Item.IdentificationState.IDENTIFIED -> template.getName();
				case Item.IdentificationState.UNIDENTIFIED -> template.getUnidentifiedName();
				default ->
					throw new MazeException("Invalid item identification state: " +
						getIdentificationState());
			};
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A plural display name for this foe that depends on whether or not
	 * 	it has been identified.
	 */
	public String getDisplayNamePlural()
	{
		return switch (getIdentificationState())
			{
				case Item.IdentificationState.IDENTIFIED -> getPluralName();
				case Item.IdentificationState.UNIDENTIFIED ->
					getUnidentifiedPluralName();
				default ->
					throw new MazeException("Invalid item identification state: " +
						getIdentificationState());
			};
	}

	/*-------------------------------------------------------------------------*/
	public int getBaseModifier(Stats.Modifier modifier)
	{
		return this.template.getStats().getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public ModifierValue getModifierValue(Stats.Modifier modifier, boolean checkCC)
	{
		ModifierValue result = super.getModifierValue(modifier, checkCC);

		//
		// Add foe types
		//
		if (template.getTypes() != null)
		{
			for (FoeType ft : template.getTypes())
			{
				result.add(ft.getName(), addModifier(modifier, ft.getConstantModifiers()));
			}
		}

		return result;
	}

	@Override
	public void removeLevelAbility(Spell spell)
	{
		// no op
	}

	/*-------------------------------------------------------------------------*/
	public void incModifier(Stats.Modifier modifier, int amount)
	{
		this.getStats().incModifier(modifier, amount);
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getBaseTexture()
	{
		return this.template.getBaseTexture();
	}
	
	/*-------------------------------------------------------------------------*/
	public MazeTexture getMeleeAttackTexture()
	{
		return this.template.getMeleeAttackTexture();
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getRangedAttackTexture()
	{
		return this.template.getRangedAttackTexture();
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getCastSpellTexture()
	{
		return this.template.getCastSpellTexture();
	}

	/*-------------------------------------------------------------------------*/
	public MazeTexture getSpecialAbilityTexture()
	{
		return this.template.getSpecialAbilityTexture();
	}
	
	/*-------------------------------------------------------------------------*/
	//
	// Data used by the engine in combat
	//
	public CombatantData getCombatantData()
	{
		return combatantData;
	}

	public void setCombatantData(CombatantData combatantData)
	{
		this.combatantData = combatantData;
	}

	@Override
	public List<AttackWith> getAttackWithOptions()
	{
		ArrayList<AttackWith> result = new ArrayList<>();
		if (getNaturalWeapons() != null)
		{
			result.addAll(getNaturalWeapons());
		}

		if (getPrimaryWeapon() != null)
		{
			result.add(getPrimaryWeapon());
		}

		if (result.isEmpty())
		{
			result.add(GameSys.getInstance().getUnarmedWeapon(this, true));
		}

		return result;
	}

	public EngineObject getSprite()
	{
		return sprite;
	}

	public void setSprite(EngineObject sprite)
	{
		this.sprite = sprite;
	}

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

	/*-------------------------------------------------------------------------*/
	public String getPluralName()
	{
		return template.getPluralName();
	}

	public String getUnidentifiedName()
	{
		return template.getUnidentifiedName();
	}

	public String getUnidentifiedPluralName()
	{
		return template.getUnidentifiedPluralName();
	}

	public LootTable getLootTable()
	{
		return template.getLoot();
	}

	public int getExperience()
	{
		return template.getExperience();
	}

	public boolean cannotBeEvaded()
	{
		return template.cannotBeEvaded();
	}

	public int getIdentificationDifficulty()
	{
		return template.getIdentificationDifficulty();
	}

	public List<TypeDescriptor> getTypes()
	{
		List<TypeDescriptor> result = new ArrayList<>();
		if (template.getTypes() != null)
		{
			result.addAll(template.getTypes());
		}
		if (getCharacterClass() != null)
		{
			result.add(getCharacterClass());
		}
		if (getRace() != null)
		{
			result.add(getRace());
		}
		List<LevelAbility> levelAbilities = getLevelAbilities();
		if (levelAbilities != null)
		{
			for (LevelAbility la : levelAbilities)
			{
				if (la.getTypeDescriptors() != null)
				{
					result.addAll(la.getTypeDescriptors());
				}
			}
		}
		return result;
	}

	public StatModifier getAllFoesBannerModifiers()
	{
		return template.getAllFoesBannerModifiers();
	}

	public StatModifier getFoeGroupBannerModifiers()
	{
		return template.getFoeGroupBannerModifiers();
	}

	public boolean isImmuneToCriticals()
	{
		return getModifier(Stats.Modifier.IMMUNE_TO_CRITICALS) > 0;
	}

	public boolean isSummoned()
	{
		return isSummoned;
	}

	public void setSummoned(boolean summoned)
	{
		isSummoned = summoned;
	}

	public String getFaction()
	{
		return template.getFaction();
	}
	
	public MazeScript getAppearanceScript()
	{
		return template.getAppearanceScript();
	}

	public FoeGroup getFoeGroup()
	{
		return this.foeGroup;
	}

	public void setFoeGroup(FoeGroup foeGroup)
	{
		this.foeGroup = foeGroup;
	}

	public MazeScript getDeathScript()
	{
		return this.template.getDeathScript();
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		return "Foe{name='" + template.getName() + '\'' + ", level=" + getLevel() + ", hp=" + getHitPoints() + '}';
	}

	/*-------------------------------------------------------------------------*/
	public boolean canAttack(int engagementRange)
	{
		List<AttackWith> items = getAttackWithOptions();

		for (AttackWith aw : items)
		{
			if (isLegalAttack(aw, engagementRange))
			{
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isLegalAttack(AttackWith aw, int engagementRange)
	{
		return aw.getMinRange() <= engagementRange &&
			aw.getMaxRange() >= engagementRange;
	}

	/*-------------------------------------------------------------------------*/
	public int getEvasionBehaviour()
	{
		return template.getEvasionBehaviour();
	}

	/*-------------------------------------------------------------------------*/
	public NpcFaction.Attitude getDefaultAttitude()
	{
		return template.getDefaultAttitude();
	}

	/*-------------------------------------------------------------------------*/
	public String getAlliesOnCall()
	{
		return template.getAlliesOnCall();
	}

	/*-------------------------------------------------------------------------*/
	public void changeAttitude(NpcFaction.AttitudeChange change)
	{
		ActorEncounter actorEncounter = Maze.getInstance().getCurrentActorEncounter();
		if (actorEncounter != null)
		{
			actorEncounter.setEncounterAttitude(
				GameSys.getInstance().calcAttitudeChange(
					actorEncounter.getEncounterAttitude(), change));
		}
	}

	/*-------------------------------------------------------------------------*/
	public NpcFaction.Attitude getAttitude()
	{
		if (Maze.getInstance().getCurrentActorEncounter() != null)
		{
			return Maze.getInstance().getCurrentActorEncounter().getEncounterAttitude();
		}
		else
		{
			return NpcFaction.Attitude.ATTACKING;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void incTheftCounter(int value)
	{
		// no op
	}

	/*-------------------------------------------------------------------------*/
	public int getResistBribes()
	{
		return 0;
	}

	@Override
	public int getMaxStealableGold()
	{
		return template.getLoot().getMaxDroppableGold();
	}

	public int getMaxPurchasePrice()
	{
		return 100*getLevel();
	}

	public int getResistSteal()
	{
		return 0;
	}

	public int getTheftCounter()
	{
		return 0;
	}

	public int getSellsAt()
	{
		return 250;
	}

	public List<Item> getStealableItems()
	{
		if (this.getInventory() != null)
		{
			List<Item> result = new ArrayList<>(this.getInventory().getItems());

			// remove stuff that is equipped
			result.removeAll(getEquippedItems());

			return result;
		}
		else
		{
			return null;
		}
	}

	public int getBuysAt()
	{
		return 30;
	}

	public List<Item> getTradingInventory()
	{
		return getInventory().getItems();
	}

	public void sortInventory()
	{

	}

	public boolean isInterestedInBuyingItem(Item item, PlayerCharacter pc)
	{
		return !item.isQuestItem() && item.getBaseCost()<1000;
	}

	public boolean isAbleToAffordItem(Item item, PlayerCharacter pc)
	{
		// todo: money?
		return true;
	}

	public boolean isGuildMaster()
	{
		return false;
	}

	public List<String> getGuild()
	{
		return new ArrayList<>();
	}

	public FoeTemplate.AppearanceDirection getAppearanceDirection()
	{
		return this.template.getAppearanceDirection();
	}

	public List<ObjectScript> getAnimationScripts()
	{
		ObjectAnimations spriteAnimations = this.template.getSpriteAnimations();
		return spriteAnimations == null ? new ArrayList<>() : spriteAnimations.getAnimationScripts();
	}

	public EngineObject.Alignment getVerticalAlignment()
	{
		return this.template.getVerticalAlignment();
	}

	/*-------------------------------------------------------------------------*/
	public static class EvasionBehaviour
	{
		/** the foe will always ambush the party if it can */
		public static final int NEVER_EVADE = 1;
		/** the foe will evade the party 50% of the time */
		public static final int RANDOM_EVADE = 2;
		/** the foe will always evade the party if it can */
		public static final int ALWAYS_EVADE = 3;
		/** the foe will make a judgement call based on party strength */
		public static final int CLEVER_EVADE = 4;

		public static String toString(int i)
		{
			return switch (i)
				{
					case NEVER_EVADE -> "never evade";
					case RANDOM_EVADE -> "random evade";
					case ALWAYS_EVADE -> "always evade";
					case CLEVER_EVADE -> "clever evade";
					default ->
						throw new MazeException("Invalid evasion behaviour: " + i);
				};
		}

		public static int valueOf(String s)
		{
			return switch (s)
				{
					case "never evade" -> NEVER_EVADE;
					case "random evade" -> RANDOM_EVADE;
					case "always evade" -> ALWAYS_EVADE;
					case "clever evade" -> CLEVER_EVADE;
					default ->
						throw new MazeException("Invalid evasion behaviour: [" + s + "]");
				};
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class StealthBehaviour
	{
		/** foe will never take a hide or ambush action */
		public static final int NOT_STEALTHY = 1;
		/** foe will not rely exclusively on stealth actions, but will ambush if
		 * possible and hide sometimes when out of action points */
		public static final int OPPORTUNISTIC = 2;
		/** foe relies on stealth and will always try to hide and ambush */
		public static final int STEALTH_RELIANT = 3;

		public static String toString(int i)
		{
			return switch (i)
				{
					case NOT_STEALTHY -> "not stealthy";
					case OPPORTUNISTIC -> "opportunistic";
					case STEALTH_RELIANT -> "stealth reliant";
					default ->
						throw new MazeException("Invalid stealth behaviour: " + i);
				};
		}

		public static int valueOf(String s)
		{
			return switch (s)
				{
					case "not stealthy" -> NOT_STEALTHY;
					case "opportunistic" -> OPPORTUNISTIC;
					case "stealth reliant" -> STEALTH_RELIANT;
					default ->
						throw new MazeException("Invalid stealth behaviour: [" + s + "]");
				};
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Contains some common foe types, but not an exclusive set.
	 */
	public static class Type
	{
		public static final TypeDescriptor LEGENDARY = new TypeDescriptor()
		{
			@Override
			public String getName()
			{
				return "Legendary";
			}

			@Override
			public Stats.Modifier getFavouredEnemyModifier()
			{
				return null;
			}
		};
	}

	/*-------------------------------------------------------------------------*/
	public static class Animation
	{
		public static final int BASE_TEXTURE = 0;
		public static final int MELEE_ATTACK = 1;
		public static final int RANGED_ATTACK = 2;
		public static final int CAST_SPELL = 3;
		public static final int SPECIAL_ABILITY = 4;
	}
}
