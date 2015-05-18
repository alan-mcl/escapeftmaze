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
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.stat.magic.SpellBook;

/**
 *
 */
public class FoeTemplate
{
	/** The singular name of the foe, eg "Zombie" */
	String name; 
	
	/** The plural name of the foe, eg "Zombies" */
	String pluralName;
	
	/** The unidentified name of the foe, eg "?Undead" */
	String unidentifiedName;

	/** The unidentified plural name of the foe, eg "?Plants" */
	String unidentifiedPluralName;

	/** The type of this foe, eg "Undead", "Plant", "Legendary" */
	String type;

	/** The potential hit points of this foe, expressed in dice */
	Dice hitPointsRange;
	
	/** The potential action points of this foe, expressed in dice */
	Dice actionPointsRange;
	
	/** The potential magic points of this foe, expressed in dice */
	Dice magicPointsRange;
	
	/** The potential level range of this foe, expressed in dice */
	Dice levelRange;

	/** How much experience this foe is worth */
	int experience;

	/** The stats bundle of this foe */
	StatModifier stats;
	
	/** The body parts of this foe */
	PercentageTable<BodyPart> bodyParts;
	
	/** Which parts of a player character this foe is likely to attack */
	PercentageTable<String> playerBodyParts;
	
	/** The base texture of this foe, what it does when it's just standing around */
	MazeTexture baseTexture;
	
	/** The texture to use when this foe executes a melee attack */
	MazeTexture meleeAttackTexture;
	
	/** The texture to use when this foe executes a ranged attack */
	MazeTexture rangedAttackTexture;
	
	/** The texture to use when this foe casts a spell */
	MazeTexture castSpellTexture;
	
	/** The texture to use when this foe uses a special ability */
	MazeTexture specialAbilityTexture;

	/** What this foe drops when it dies */
	LootTable loot;

	/** This foes behaviour when it gets a chance to evade the player.
	 * A constant from {@link Foe.EvasionBehaviour} */
	int evasionBehaviour;

	/** true if this foe cannot be evaded */
	boolean cannotBeEvaded;

	/** the difficulty in identifying this foe */
	int identificationDifficulty;

	/** Modifiers that this foe applies to all foes in it's group */
	StatModifier foeGroupBannerModifiers;

	/** Modifiers that this foe applies to all foes present */
	StatModifier allFoesBannerModifiers;

	/** true if this foe is immune to critical hits */
	boolean immuneToCriticals;

	/** chance of this foe to flee, each turn */
	int fleeChance;

	/** behaviour of the foe wrt stealth actions */
	int stealthBehaviour;

	/** the faction (if any) that this foe belongs to */
	String faction;

	/** is this Foe an NPC */
	boolean isNpc;
	
	/** script to run when a group of this foe type appears */
	MazeScript appearanceScript;

	/** script to run each time one of this foe dies */
	MazeScript deathScript;

	/** natural weapons of this foe (claw, bite, etc) */
	private List<String> naturalWeapons;

	/** spell book of this foe */
	private SpellBook spellBook;

	/** SLAs */
	private List<SpellLikeAbility> spellLikeAbilities;

	/*-------------------------------------------------------------------------*/
	public FoeTemplate(
		String name,
		String pluralName,
		String unidentifiedName,
		String unidentifiedPluralName,
		String type,
		Dice hitPointsRange,
		Dice actionPointsRange,
		Dice magicPointsRange,
		Dice levelRange,
		int experience,
		StatModifier stats,
		PercentageTable<BodyPart> bodyParts,
		PercentageTable<String> playerBodyParts,
		MazeTexture baseTexture,
		MazeTexture meleeAttackTexture,
		MazeTexture rangedAttackTexture,
		MazeTexture castSpellTexture,
		MazeTexture specialAbilityTexture,
		LootTable loot,
		int evasionBehaviour,
		boolean cannotBeEvaded,
		int identificationDifficulty,
		StatModifier foeGroupBannerModifiers,
		StatModifier allFoesBannerModifiers,
		boolean immuneToCriticals,
		int fleeChance,
		int stealthBehaviour,
		String faction,
		boolean isNpc,
		MazeScript appearanceScript,
		MazeScript deathScript,
		List<String> naturalWeapons,
		SpellBook spellBook,
		List<SpellLikeAbility> spellLikeAbilities)
	{
		this.unidentifiedPluralName = unidentifiedPluralName;
		this.type = type;
		this.experience = experience;
		this.levelRange = levelRange;
		this.bodyParts = bodyParts;
		this.hitPointsRange = hitPointsRange;
		this.actionPointsRange = actionPointsRange;
		this.name = name;
		this.playerBodyParts = playerBodyParts;
		this.pluralName = pluralName;
		this.magicPointsRange = magicPointsRange;
		this.stats = stats;
		this.unidentifiedName = unidentifiedName;
		this.baseTexture = baseTexture;
		this.meleeAttackTexture = meleeAttackTexture;
		this.rangedAttackTexture = rangedAttackTexture;
		this.castSpellTexture = castSpellTexture;
		this.specialAbilityTexture = specialAbilityTexture;
		this.loot = loot;
		this.evasionBehaviour = evasionBehaviour;
		this.cannotBeEvaded = cannotBeEvaded;
		this.identificationDifficulty = identificationDifficulty;
		this.foeGroupBannerModifiers = foeGroupBannerModifiers;
		this.allFoesBannerModifiers = allFoesBannerModifiers;
		this.immuneToCriticals = immuneToCriticals;
		this.fleeChance = fleeChance;
		this.stealthBehaviour = stealthBehaviour;
		this.faction = faction;
		this.isNpc = isNpc;
		this.appearanceScript = appearanceScript;
		this.deathScript = deathScript;
		this.naturalWeapons = naturalWeapons;
		this.spellBook = spellBook;
		this.spellLikeAbilities = spellLikeAbilities;
	}

	/*-------------------------------------------------------------------------*/
	public Foe create()
	{
		return new Foe(this);
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getStats()
	{
		return stats;		
	}
	
	/*-------------------------------------------------------------------------*/
	public PercentageTable<BodyPart> getBodyParts()
	{
		return bodyParts;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getAllFoesBannerModifiers()
	{
		return allFoesBannerModifiers;
	}

	public MazeTexture getBaseTexture()
	{
		return baseTexture;
	}

	public boolean cannotBeEvaded()
	{
		return cannotBeEvaded;
	}

	public MazeTexture getCastSpellTexture()
	{
		return castSpellTexture;
	}

	public int getEvasionBehaviour()
	{
		return evasionBehaviour;
	}

	public int getExperience()
	{
		return experience;
	}

	public String getFaction()
	{
		return faction;
	}

	public int getFleeChance()
	{
		return fleeChance;
	}

	public StatModifier getFoeGroupBannerModifiers()
	{
		return foeGroupBannerModifiers;
	}

	public Dice getHitPointsRange()
	{
		return hitPointsRange;
	}

	public int getIdentificationDifficulty()
	{
		return identificationDifficulty;
	}

	public boolean isImmuneToCriticals()
	{
		return immuneToCriticals;
	}

	public boolean isNpc()
	{
		return isNpc;
	}

	public Dice getLevelRange()
	{
		return levelRange;
	}

	public LootTable getLoot()
	{
		return loot;
	}

	public Dice getMagicPointsRange()
	{
		return magicPointsRange;
	}

	public MazeTexture getMeleeAttackTexture()
	{
		return meleeAttackTexture;
	}

	public PercentageTable<String> getPlayerBodyParts()
	{
		return playerBodyParts;
	}

	public String getPluralName()
	{
		return pluralName;
	}

	public MazeTexture getRangedAttackTexture()
	{
		return rangedAttackTexture;
	}

	public MazeTexture getSpecialAbilityTexture()
	{
		return specialAbilityTexture;
	}

	public int getStealthBehaviour()
	{
		return stealthBehaviour;
	}

	public Dice getActionPointsRange()
	{
		return actionPointsRange;
	}

	public String getType()
	{
		return type;
	}

	public String getUnidentifiedName()
	{
		return unidentifiedName;
	}

	public String getUnidentifiedPluralName()
	{
		return unidentifiedPluralName;
	}

	public MazeScript getAppearanceScript()
	{
		return appearanceScript;
	}

	public MazeScript getDeathScript()
	{
		return deathScript;
	}

	/*-------------------------------------------------------------------------*/
	public void setAllFoesBannerModifiers(StatModifier allFoesBannerModifiers)
	{
		this.allFoesBannerModifiers = allFoesBannerModifiers;
	}

	public void setBaseTexture(MazeTexture baseTexture)
	{
		this.baseTexture = baseTexture;
	}

	public void setBodyParts(PercentageTable<BodyPart> bodyParts)
	{
		this.bodyParts = bodyParts;
	}

	public void setCannotBeEvaded(boolean cannotBeEvaded)
	{
		this.cannotBeEvaded = cannotBeEvaded;
	}

	public void setCastSpellTexture(MazeTexture castSpellTexture)
	{
		this.castSpellTexture = castSpellTexture;
	}

	public void setEvasionBehaviour(int evasionBehaviour)
	{
		this.evasionBehaviour = evasionBehaviour;
	}

	public void setExperience(int experience)
	{
		this.experience = experience;
	}

	public void setFaction(String faction)
	{
		this.faction = faction;
	}

	public void setFleeChance(int fleeChance)
	{
		this.fleeChance = fleeChance;
	}

	public void setFoeGroupBannerModifiers(StatModifier foeGroupBannerModifiers)
	{
		this.foeGroupBannerModifiers = foeGroupBannerModifiers;
	}

	public void setHitPointsRange(Dice hitPointsRange)
	{
		this.hitPointsRange = hitPointsRange;
	}

	public void setIdentificationDifficulty(int identificationDifficulty)
	{
		this.identificationDifficulty = identificationDifficulty;
	}

	public void setImmuneToCriticals(boolean immuneToCriticals)
	{
		this.immuneToCriticals = immuneToCriticals;
	}

	public void setNpc(boolean npc)
	{
		isNpc = npc;
	}

	public void setLevelRange(Dice levelRange)
	{
		this.levelRange = levelRange;
	}

	public void setLoot(LootTable loot)
	{
		this.loot = loot;
	}

	public void setMagicPointsRange(Dice magicPointsRange)
	{
		this.magicPointsRange = magicPointsRange;
	}

	public void setMeleeAttackTexture(MazeTexture meleeAttackTexture)
	{
		this.meleeAttackTexture = meleeAttackTexture;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPlayerBodyParts(PercentageTable<String> playerBodyParts)
	{
		this.playerBodyParts = playerBodyParts;
	}

	public void setPluralName(String pluralName)
	{
		this.pluralName = pluralName;
	}

	public void setRangedAttackTexture(MazeTexture rangedAttackTexture)
	{
		this.rangedAttackTexture = rangedAttackTexture;
	}

	public void setSpecialAbilityTexture(MazeTexture specialAbilityTexture)
	{
		this.specialAbilityTexture = specialAbilityTexture;
	}

	public void setStats(StatModifier stats)
	{
		this.stats = stats;
	}

	public void setStealthBehaviour(int stealthBehaviour)
	{
		this.stealthBehaviour = stealthBehaviour;
	}

	public void setActionPointsRange(Dice actionPointsRange)
	{
		this.actionPointsRange = actionPointsRange;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setUnidentifiedName(String unidentifiedName)
	{
		this.unidentifiedName = unidentifiedName;
	}

	public void setUnidentifiedPluralName(String unidentifiedPluralName)
	{
		this.unidentifiedPluralName = unidentifiedPluralName;
	}

	public void setAppearanceScript(MazeScript appearanceScript)
	{
		this.appearanceScript = appearanceScript;
	}

	public void setDeathScript(MazeScript deathScript)
	{
		this.deathScript = deathScript;
	}

	public List<String> getNaturalWeapons()
	{
		return naturalWeapons;

//		for (FoeAttack fa : attacks.getItems())
//		{
//			if (fa.getType() == FoeAttack.Type.MELEE_ATTACK ||
//				fa.getType() == FoeAttack.Type.RANGED_ATTACK)
//			{
//				naturalWeapons.add(new NaturalWeapon(
//					fa.getName(),
//					fa.getDescription(),
//					fa.isRanged(),
//					fa.getDamage(),
//					fa.getDefaultDamageType(),
//					fa.getModifiers(),
//					fa.getMinRange(),
//					fa.getMaxRange(),
//					fa.getSpellEffects(),
//					fa.getSpellEffectLevel(),
//					fa.getAttacks(),
//					fa.getSlaysFoeType(),
//					fa.getAttackScript()));
//			}
//		}
//
//		return naturalWeapons;
	}

	public List<SpellLikeAbility> getSpellLikeAbilities()
	{
		return spellLikeAbilities;
//		List<SpellLikeAbility> result = new ArrayList<SpellLikeAbility>();
//
//		for (FoeAttack fa : attacks.getItems())
//		{
//			if (fa.getType() == FoeAttack.Type.SPECIAL_ABILITY)
//			{
//				result.add(new SpellLikeAbility(
//					fa.getSpecialAbility().getSpell(),
//					new DiceValue(fa.getSpecialAbility().getCastingLevel())));
//			}
//		}
//
//		return result;
	}

	public SpellBook getSpellBook()
	{
		return spellBook;
	}

	public void setNaturalWeapons(List<String> naturalWeapons)
	{
		this.naturalWeapons = naturalWeapons;
	}

	public void setSpellBook(SpellBook spellBook)
	{
		this.spellBook = spellBook;
	}

	public void setSpellLikeAbilities(List<SpellLikeAbility> spellLikeAbilities)
	{
		this.spellLikeAbilities = spellLikeAbilities;
	}
}
