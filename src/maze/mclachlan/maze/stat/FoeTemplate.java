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

import java.awt.Color;
import java.util.*;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.stat.magic.SpellBook;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.ui.diygui.Constants;

/**
 *
 */
public class FoeTemplate extends DataObject
{
	/** The singular name of the foe, eg "Zombie" */
	private String name;
	/** The plural name of the foe, eg "Zombies" */
	private String pluralName;
	/** The unidentified name of the foe, eg "?Undead" */
	private String unidentifiedName;
	/** The unidentified plural name of the foe, eg "?Plants" */
	private String unidentifiedPluralName;
	/** The type of this foe, eg "Undead", "Plant", "Legendary" */
	private List<FoeType> types;
	/** Race of this foe. May be null. */
	private Race race;
	/** Class of this foe. May be null. */
	private CharacterClass characterClass;
	/** The potential hit points of this foe, expressed in dice */
	private Dice hitPointsRange;
	/** The potential action points of this foe, expressed in dice */
	private Dice actionPointsRange;
	/** The potential magic points of this foe, expressed in dice */
	private Dice magicPointsRange;
	/** The potential level range of this foe, expressed in dice */
	private Dice levelRange;
	/** How much experience this foe is worth */
	private int experience;
	/** The stats bundle of this foe */
	private StatModifier stats;
	/** The body parts of this foe */
	private PercentageTable<BodyPart> bodyParts;
	/** Which parts of a player character this foe is likely to attack */
	private PercentageTable<String> playerBodyParts;
	/** The base texture of this foe, what it does when it's just standing around */
	private MazeTexture baseTexture;
	/** The texture to use when this foe executes a melee attack */
	private MazeTexture meleeAttackTexture;
	/** The texture to use when this foe executes a ranged attack */
	private MazeTexture rangedAttackTexture;
	/** The texture to use when this foe casts a spell */
	private MazeTexture castSpellTexture;
	/** The texture to use when this foe uses a special ability */
	private MazeTexture specialAbilityTexture;
	/** Texture alignment for this foe */
	private EngineObject.Alignment verticalAlignment;
	/** Any texture tint for this foe */
	private Color textureTint;
	/** What this foe drops when it dies */
	private LootTable loot;
	/** true if this foe cannot be evaded */
	private boolean cannotBeEvaded;
	/** the difficulty in identifying this foe */
	private int identificationDifficulty;
	/** Modifiers that this foe applies to all foes in it's group */
	private StatModifier foeGroupBannerModifiers;
	/** Modifiers that this foe applies to all foes present */
	private StatModifier allFoesBannerModifiers;
	/** the faction (if any) that this foe belongs to */
	private String faction;
	/** is this Foe an NPC */
	private boolean npc;
	/** how to scroll the sprite onto the screen*/
	private AppearanceDirection appearanceDirection;
	/** script to run when a group of this foe type appears */
	private MazeScript appearanceScript;
	/** any scripts to animate this object */
	private List<ObjectScript> animationScripts;
	/** script to run each time one of this foe dies */
	private MazeScript deathScript;
	/** natural weapons of this foe (claw, bite, etc) */
	private List<String> naturalWeapons;
	/** spell book of this foe */
	private SpellBook spellBook;
	/** SLAs */
	private List<SpellLikeAbility> spellLikeAbilities;
	/** chance of this foe to flee, each turn */
	private int fleeChance;

	//--- AI parameters
	/** behaviour of the foe wrt stealth actions */
	private int stealthBehaviour;
	/** focus of this foe */
	private CharacterClass.Focus focus;
	/** This foes behaviour when it gets a chance to evade the player.
	 * A constant from {@link Foe.EvasionBehaviour} */
	private int evasionBehaviour;
	/** default encounter attitude of this foe, if not overridden */
	private NpcFaction.Attitude defaultAttitude;
	/** any allies that this foe can summon at the start of combat */
	private String alliesOnCall;

	public FoeTemplate()
	{
	}

	/*-------------------------------------------------------------------------*/
	public FoeTemplate(
		String name,
		String pluralName,
		String unidentifiedName,
		String unidentifiedPluralName,
		List<FoeType> types,
		Race race,
		CharacterClass characterClass,
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
		EngineObject.Alignment verticalAlignment,
		Color textureTint,
		LootTable loot,
		int evasionBehaviour,
		boolean cannotBeEvaded,
		int identificationDifficulty,
		StatModifier foeGroupBannerModifiers,
		StatModifier allFoesBannerModifiers,
		int fleeChance,
		int stealthBehaviour,
		String faction,
		boolean npc,
		MazeScript appearanceScript,
		List<ObjectScript> animationScripts,
		AppearanceDirection appearanceDirection,
		MazeScript deathScript,
		List<String> naturalWeapons,
		SpellBook spellBook,
		List<SpellLikeAbility> spellLikeAbilities,
		CharacterClass.Focus focus,
		NpcFaction.Attitude defaultAttitude,
		String alliesOnCall)
	{
		this.unidentifiedPluralName = unidentifiedPluralName;
		this.types = types;
		this.race = race;
		this.characterClass = characterClass;
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
		this.verticalAlignment = verticalAlignment;
		this.textureTint = textureTint;
		this.loot = loot;
		this.evasionBehaviour = evasionBehaviour;
		this.cannotBeEvaded = cannotBeEvaded;
		this.identificationDifficulty = identificationDifficulty;
		this.foeGroupBannerModifiers = foeGroupBannerModifiers;
		this.allFoesBannerModifiers = allFoesBannerModifiers;
		this.fleeChance = fleeChance;
		this.stealthBehaviour = stealthBehaviour;
		this.faction = faction;
		this.npc = npc;
		this.appearanceScript = appearanceScript;
		this.animationScripts = animationScripts;
		this.appearanceDirection = appearanceDirection;
		this.deathScript = deathScript;
		this.naturalWeapons = naturalWeapons;
		this.spellBook = spellBook;
		this.spellLikeAbilities = spellLikeAbilities;
		this.focus = focus;
		this.defaultAttitude = defaultAttitude;
		this.alliesOnCall = alliesOnCall;

		init();
	}

	/*-------------------------------------------------------------------------*/
	public void init()
	{
		// if there is a tint, clone any textures so that the originals are not tinted
		if (textureTint != null)
		{
			baseTexture = baseTexture.cloneWithTint(textureTint);
			meleeAttackTexture = baseTexture.cloneWithTint(textureTint);
			rangedAttackTexture = baseTexture.cloneWithTint(textureTint);
			castSpellTexture = baseTexture.cloneWithTint(textureTint);
			specialAbilityTexture = baseTexture.cloneWithTint(textureTint);
		}
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

	public void setStats(StatModifier stats)
	{
		this.stats = stats;
	}
	
	public PercentageTable<BodyPart> getBodyParts()
	{
		return bodyParts;
	}

	public void setBodyParts(PercentageTable<BodyPart> bodyParts)
	{
		this.bodyParts = bodyParts;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public StatModifier getAllFoesBannerModifiers()
	{
		return allFoesBannerModifiers;
	}

	public void setAllFoesBannerModifiers(StatModifier allFoesBannerModifiers)
	{
		this.allFoesBannerModifiers = allFoesBannerModifiers;
	}

	public MazeTexture getBaseTexture()
	{
		return baseTexture;
	}

	public void setBaseTexture(MazeTexture baseTexture)
	{
		this.baseTexture = baseTexture;
	}

	public boolean cannotBeEvaded()
	{
		return cannotBeEvaded;
	}

	public MazeTexture getCastSpellTexture()
	{
		return castSpellTexture;
	}

	public void setCastSpellTexture(MazeTexture castSpellTexture)
	{
		this.castSpellTexture = castSpellTexture;
	}

	public int getEvasionBehaviour()
	{
		return evasionBehaviour;
	}

	public void setEvasionBehaviour(int evasionBehaviour)
	{
		this.evasionBehaviour = evasionBehaviour;
	}

	public int getExperience()
	{
		return experience;
	}

	public void setExperience(int experience)
	{
		this.experience = experience;
	}

	public String getFaction()
	{
		return faction;
	}

	public void setFaction(String faction)
	{
		this.faction = faction;
	}

	public int getFleeChance()
	{
		return fleeChance;
	}

	public void setFleeChance(int fleeChance)
	{
		this.fleeChance = fleeChance;
	}

	public StatModifier getFoeGroupBannerModifiers()
	{
		return foeGroupBannerModifiers;
	}

	public void setFoeGroupBannerModifiers(StatModifier foeGroupBannerModifiers)
	{
		this.foeGroupBannerModifiers = foeGroupBannerModifiers;
	}

	public Dice getHitPointsRange()
	{
		return hitPointsRange;
	}

	public void setHitPointsRange(Dice hitPointsRange)
	{
		this.hitPointsRange = hitPointsRange;
	}

	public int getIdentificationDifficulty()
	{
		return identificationDifficulty;
	}

	public void setIdentificationDifficulty(int identificationDifficulty)
	{
		this.identificationDifficulty = identificationDifficulty;
	}

	public boolean isNpc()
	{
		return npc;
	}

	public void setNpc(boolean npc)
	{
		this.npc = npc;
	}

	public Dice getLevelRange()
	{
		return levelRange;
	}

	public void setLevelRange(Dice levelRange)
	{
		this.levelRange = levelRange;
	}

	public LootTable getLoot()
	{
		return loot;
	}

	public void setLoot(LootTable loot)
	{
		this.loot = loot;
	}

	public Dice getMagicPointsRange()
	{
		return magicPointsRange;
	}

	public void setMagicPointsRange(Dice magicPointsRange)
	{
		this.magicPointsRange = magicPointsRange;
	}

	public MazeTexture getMeleeAttackTexture()
	{
		return meleeAttackTexture;
	}

	public void setMeleeAttackTexture(MazeTexture meleeAttackTexture)
	{
		this.meleeAttackTexture = meleeAttackTexture;
	}

	public PercentageTable<String> getPlayerBodyParts()
	{
		return playerBodyParts;
	}

	public void setPlayerBodyParts(PercentageTable<String> playerBodyParts)
	{
		this.playerBodyParts = playerBodyParts;
	}

	public String getPluralName()
	{
		return pluralName;
	}

	public void setPluralName(String pluralName)
	{
		this.pluralName = pluralName;
	}

	public MazeTexture getRangedAttackTexture()
	{
		return rangedAttackTexture;
	}

	public void setRangedAttackTexture(MazeTexture rangedAttackTexture)
	{
		this.rangedAttackTexture = rangedAttackTexture;
	}

	public MazeTexture getSpecialAbilityTexture()
	{
		return specialAbilityTexture;
	}

	public void setSpecialAbilityTexture(MazeTexture specialAbilityTexture)
	{
		this.specialAbilityTexture = specialAbilityTexture;
	}

	public EngineObject.Alignment getVerticalAlignment()
	{
		return verticalAlignment;
	}

	public void setVerticalAlignment(
		EngineObject.Alignment verticalAlignment)
	{
		this.verticalAlignment = verticalAlignment;
	}

	public Color getTextureTint()
	{
		return textureTint;
	}

	public void setTextureTint(Color textureTint)
	{
		this.textureTint = textureTint;
	}

	public int getStealthBehaviour()
	{
		return stealthBehaviour;
	}

	public void setStealthBehaviour(int stealthBehaviour)
	{
		this.stealthBehaviour = stealthBehaviour;
	}

	public Dice getActionPointsRange()
	{
		return actionPointsRange;
	}

	public void setActionPointsRange(Dice actionPointsRange)
	{
		this.actionPointsRange = actionPointsRange;
	}

	public List<FoeType> getTypes()
	{
		return types;
	}

	public void setTypes(List<FoeType> types)
	{
		this.types = types;
	}

	public Race getRace()
	{
		return race;
	}

	public void setRace(Race race)
	{
		this.race = race;
	}

	public CharacterClass getCharacterClass()
	{
		return characterClass;
	}

	public void setCharacterClass(CharacterClass characterClass)
	{
		this.characterClass = characterClass;
	}

	public String getUnidentifiedName()
	{
		return unidentifiedName;
	}

	public void setUnidentifiedName(String unidentifiedName)
	{
		this.unidentifiedName = unidentifiedName;
	}

	public String getUnidentifiedPluralName()
	{
		return unidentifiedPluralName;
	}

	public void setUnidentifiedPluralName(String unidentifiedPluralName)
	{
		this.unidentifiedPluralName = unidentifiedPluralName;
	}

	public MazeScript getAppearanceScript()
	{
		return appearanceScript;
	}

	public void setAppearanceScript(MazeScript appearanceScript)
	{
		this.appearanceScript = appearanceScript;
	}

	public AppearanceDirection getAppearanceDirection()
	{
		return appearanceDirection;
	}

	public void setAppearanceDirection(
		AppearanceDirection appearanceDirection)
	{
		this.appearanceDirection = appearanceDirection;
	}

	public List<ObjectScript> getAnimationScripts()
	{
		return animationScripts;
	}

	public void setAnimationScripts(
		List<ObjectScript> animationScripts)
	{
		this.animationScripts = animationScripts;
	}

	public MazeScript getDeathScript()
	{
		return deathScript;
	}

	public void setDeathScript(MazeScript deathScript)
	{
		this.deathScript = deathScript;
	}

	public CharacterClass.Focus getFocus()
	{
		return focus;
	}

	public void setFocus(CharacterClass.Focus focus)
	{
		this.focus = focus;
	}

	public NpcFaction.Attitude getDefaultAttitude()
	{
		return defaultAttitude;
	}

	public void setDefaultAttitude(NpcFaction.Attitude defaultAttitude)
	{
		this.defaultAttitude = defaultAttitude;
	}

	public void setCannotBeEvaded(boolean cannotBeEvaded)
	{
		this.cannotBeEvaded = cannotBeEvaded;
	}

	public List<String> getNaturalWeapons()
	{
		return naturalWeapons;
	}

	public void setNaturalWeapons(List<String> naturalWeapons)
	{
		this.naturalWeapons = naturalWeapons;
	}

	public List<SpellLikeAbility> getSpellLikeAbilities()
	{
		return spellLikeAbilities;
	}

	public void setSpellLikeAbilities(List<SpellLikeAbility> spellLikeAbilities)
	{
		this.spellLikeAbilities = spellLikeAbilities;
	}

	public SpellBook getSpellBook()
	{
		return spellBook;
	}

	public void setSpellBook(SpellBook spellBook)
	{
		this.spellBook = spellBook;
	}

	public String getAlliesOnCall()
	{
		return alliesOnCall;
	}

	public void setAlliesOnCall(String alliesOnCall)
	{
		this.alliesOnCall = alliesOnCall;
	}

	public boolean isCannotBeEvaded()
	{
		return cannotBeEvaded;
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

		FoeTemplate that = (FoeTemplate)o;

		if (getExperience() != that.getExperience())
		{
			return false;
		}
		if (isCannotBeEvaded() != that.isCannotBeEvaded())
		{
			return false;
		}
		if (getIdentificationDifficulty() != that.getIdentificationDifficulty())
		{
			return false;
		}
		if (isNpc() != that.isNpc())
		{
			return false;
		}
		if (getFleeChance() != that.getFleeChance())
		{
			return false;
		}
		if (getStealthBehaviour() != that.getStealthBehaviour())
		{
			return false;
		}
		if (getEvasionBehaviour() != that.getEvasionBehaviour())
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
		if (getUnidentifiedPluralName() != null ? !getUnidentifiedPluralName().equals(that.getUnidentifiedPluralName()) : that.getUnidentifiedPluralName() != null)
		{
			return false;
		}
		if (getTypes() != null ? !getTypes().equals(that.getTypes()) : that.getTypes() != null)
		{
			return false;
		}
		if (getRace() != null ? !getRace().equals(that.getRace()) : that.getRace() != null)
		{
			return false;
		}
		if (getCharacterClass() != null ? !getCharacterClass().equals(that.getCharacterClass()) : that.getCharacterClass() != null)
		{
			return false;
		}
		if (getHitPointsRange() != null ? !getHitPointsRange().equals(that.getHitPointsRange()) : that.getHitPointsRange() != null)
		{
			return false;
		}
		if (getActionPointsRange() != null ? !getActionPointsRange().equals(that.getActionPointsRange()) : that.getActionPointsRange() != null)
		{
			return false;
		}
		if (getMagicPointsRange() != null ? !getMagicPointsRange().equals(that.getMagicPointsRange()) : that.getMagicPointsRange() != null)
		{
			return false;
		}
		if (getLevelRange() != null ? !getLevelRange().equals(that.getLevelRange()) : that.getLevelRange() != null)
		{
			return false;
		}
		if (getStats() != null ? !getStats().equals(that.getStats()) : that.getStats() != null)
		{
			return false;
		}
		if (getBodyParts() != null ? !getBodyParts().equals(that.getBodyParts()) : that.getBodyParts() != null)
		{
			return false;
		}
		if (getPlayerBodyParts() != null ? !getPlayerBodyParts().equals(that.getPlayerBodyParts()) : that.getPlayerBodyParts() != null)
		{
			return false;
		}
		if (getBaseTexture() != null ? !getBaseTexture().equals(that.getBaseTexture()) : that.getBaseTexture() != null)
		{
			return false;
		}
		if (getMeleeAttackTexture() != null ? !getMeleeAttackTexture().equals(that.getMeleeAttackTexture()) : that.getMeleeAttackTexture() != null)
		{
			return false;
		}
		if (getRangedAttackTexture() != null ? !getRangedAttackTexture().equals(that.getRangedAttackTexture()) : that.getRangedAttackTexture() != null)
		{
			return false;
		}
		if (getCastSpellTexture() != null ? !getCastSpellTexture().equals(that.getCastSpellTexture()) : that.getCastSpellTexture() != null)
		{
			return false;
		}
		if (getSpecialAbilityTexture() != null ? !getSpecialAbilityTexture().equals(that.getSpecialAbilityTexture()) : that.getSpecialAbilityTexture() != null)
		{
			return false;
		}
		if (getVerticalAlignment() != that.getVerticalAlignment())
		{
			return false;
		}
		if (getTextureTint() != null ? !getTextureTint().equals(that.getTextureTint()) : that.getTextureTint() != null)
		{
			return false;
		}
		if (getLoot() != null ? !getLoot().equals(that.getLoot()) : that.getLoot() != null)
		{
			return false;
		}
		if (getFoeGroupBannerModifiers() != null ? !getFoeGroupBannerModifiers().equals(that.getFoeGroupBannerModifiers()) : that.getFoeGroupBannerModifiers() != null)
		{
			return false;
		}
		if (getAllFoesBannerModifiers() != null ? !getAllFoesBannerModifiers().equals(that.getAllFoesBannerModifiers()) : that.getAllFoesBannerModifiers() != null)
		{
			return false;
		}
		if (getFaction() != null ? !getFaction().equals(that.getFaction()) : that.getFaction() != null)
		{
			return false;
		}
		if (getAppearanceDirection() != that.getAppearanceDirection())
		{
			return false;
		}
		if (getAppearanceScript() != null ? !getAppearanceScript().equals(that.getAppearanceScript()) : that.getAppearanceScript() != null)
		{
			return false;
		}
		if (getAnimationScripts() != null ? !getAnimationScripts().equals(that.getAnimationScripts()) : that.getAnimationScripts() != null)
		{
			return false;
		}
		if (getDeathScript() != null ? !getDeathScript().equals(that.getDeathScript()) : that.getDeathScript() != null)
		{
			return false;
		}
		if (getNaturalWeapons() != null ? !getNaturalWeapons().equals(that.getNaturalWeapons()) : that.getNaturalWeapons() != null)
		{
			return false;
		}
		if (getSpellBook() != null ? !getSpellBook().equals(that.getSpellBook()) : that.getSpellBook() != null)
		{
			return false;
		}
		if (getSpellLikeAbilities() != null ? !getSpellLikeAbilities().equals(that.getSpellLikeAbilities()) : that.getSpellLikeAbilities() != null)
		{
			return false;
		}
		if (getFocus() != that.getFocus())
		{
			return false;
		}
		if (getDefaultAttitude() != that.getDefaultAttitude())
		{
			return false;
		}
		return getAlliesOnCall() != null ? getAlliesOnCall().equals(that.getAlliesOnCall()) : that.getAlliesOnCall() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getPluralName() != null ? getPluralName().hashCode() : 0);
		result = 31 * result + (getUnidentifiedName() != null ? getUnidentifiedName().hashCode() : 0);
		result = 31 * result + (getUnidentifiedPluralName() != null ? getUnidentifiedPluralName().hashCode() : 0);
		result = 31 * result + (getTypes() != null ? getTypes().hashCode() : 0);
		result = 31 * result + (getRace() != null ? getRace().hashCode() : 0);
		result = 31 * result + (getCharacterClass() != null ? getCharacterClass().hashCode() : 0);
		result = 31 * result + (getHitPointsRange() != null ? getHitPointsRange().hashCode() : 0);
		result = 31 * result + (getActionPointsRange() != null ? getActionPointsRange().hashCode() : 0);
		result = 31 * result + (getMagicPointsRange() != null ? getMagicPointsRange().hashCode() : 0);
		result = 31 * result + (getLevelRange() != null ? getLevelRange().hashCode() : 0);
		result = 31 * result + getExperience();
		result = 31 * result + (getStats() != null ? getStats().hashCode() : 0);
		result = 31 * result + (getBodyParts() != null ? getBodyParts().hashCode() : 0);
		result = 31 * result + (getPlayerBodyParts() != null ? getPlayerBodyParts().hashCode() : 0);
		result = 31 * result + (getBaseTexture() != null ? getBaseTexture().hashCode() : 0);
		result = 31 * result + (getMeleeAttackTexture() != null ? getMeleeAttackTexture().hashCode() : 0);
		result = 31 * result + (getRangedAttackTexture() != null ? getRangedAttackTexture().hashCode() : 0);
		result = 31 * result + (getCastSpellTexture() != null ? getCastSpellTexture().hashCode() : 0);
		result = 31 * result + (getSpecialAbilityTexture() != null ? getSpecialAbilityTexture().hashCode() : 0);
		result = 31 * result + (getVerticalAlignment() != null ? getVerticalAlignment().hashCode() : 0);
		result = 31 * result + (getTextureTint() != null ? getTextureTint().hashCode() : 0);
		result = 31 * result + (getLoot() != null ? getLoot().hashCode() : 0);
		result = 31 * result + (isCannotBeEvaded() ? 1 : 0);
		result = 31 * result + getIdentificationDifficulty();
		result = 31 * result + (getFoeGroupBannerModifiers() != null ? getFoeGroupBannerModifiers().hashCode() : 0);
		result = 31 * result + (getAllFoesBannerModifiers() != null ? getAllFoesBannerModifiers().hashCode() : 0);
		result = 31 * result + (getFaction() != null ? getFaction().hashCode() : 0);
		result = 31 * result + (isNpc() ? 1 : 0);
		result = 31 * result + (getAppearanceDirection() != null ? getAppearanceDirection().hashCode() : 0);
		result = 31 * result + (getAppearanceScript() != null ? getAppearanceScript().hashCode() : 0);
		result = 31 * result + (getAnimationScripts() != null ? getAnimationScripts().hashCode() : 0);
		result = 31 * result + (getDeathScript() != null ? getDeathScript().hashCode() : 0);
		result = 31 * result + (getNaturalWeapons() != null ? getNaturalWeapons().hashCode() : 0);
		result = 31 * result + (getSpellBook() != null ? getSpellBook().hashCode() : 0);
		result = 31 * result + (getSpellLikeAbilities() != null ? getSpellLikeAbilities().hashCode() : 0);
		result = 31 * result + getFleeChance();
		result = 31 * result + getStealthBehaviour();
		result = 31 * result + (getFocus() != null ? getFocus().hashCode() : 0);
		result = 31 * result + getEvasionBehaviour();
		result = 31 * result + (getDefaultAttitude() != null ? getDefaultAttitude().hashCode() : 0);
		result = 31 * result + (getAlliesOnCall() != null ? getAlliesOnCall().hashCode() : 0);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public enum AppearanceDirection
	{
		FROM_LEFT,
		FROM_RIGHT,
		FROM_LEFT_OR_RIGHT,
		FROM_TOP,
		FROM_FAR
	}
}
