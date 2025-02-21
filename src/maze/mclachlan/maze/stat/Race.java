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
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Race extends DataObject implements TypeDescriptor
{
	private String name;
	private String description;

	/** The percentage of the class starting hit points this character gains */
	private int startingHitPointPercent;
	/** The percentage of the class starting action points this character gains */
	private int startingActionPointPercent;
	/** The percentage of the class starting magic points this character gains */
	private int startingMagicPointPercent;

	/** once off bonus applied to a new character of this race */
	private StatModifier startingModifiers;
	/** constantly applied to characters of this race */
	private StatModifier constantModifiers;
	/** constantly applied to the whole party */
	private StatModifier bannerModifiers;
	/** ceiling above which raising the attribute costs double */
	private StatModifier attributeCeilings;

	/** body parts likely to be attacked */
	private BodyPart head, torso, leg, hand, foot;

	/** GUI icons */
	private String rightHandIcon;
	private String leftHandIcon;

	/** Genders allowed to this race */
	private List<Gender> allowedGenders;

	/** True if this race cannot use magic */
	private boolean magicDead;

	/** any special ability for this race */
	private Spell specialAbility;
	
	/** Starting kits for this race.  If present, they will override an starting
	 * kits provided by the character class */
	private List<StartingKit> startingItems;

	/** Race body parts, and their % chance of being hit */
	private PercentageTable<BodyPart> bodyParts;

	/** Natural weapons available to this race */
	private List<NaturalWeapon> naturalWeapons;

	/** Suggested names for this race, by gender */
	private Map<String, List<String>> suggestedNames;

	/** User config variable needing to be set to unlock this race */
	private String unlockVariable;

	/** Description displayed when a race is locked */
	private String unlockDescription;

	/** Modifier on attackers that denotes that this race is a favoured enemy */
	private Stats.Modifier favouredEnemyModifier;

	/** Image displayed on the Create Character wizard */
	private String characterCreationImage;

	/*-------------------------------------------------------------------------*/

	public Race()
	{
	}

	/*-------------------------------------------------------------------------*/
	public Race(
		String name,
		String description,
		int startingHitPointPercent,
		int startingActionPointPercent,
		int startingMagicPointPercent,
		StatModifier startingModifiers,
		StatModifier constantModifiers,
		StatModifier bannerModifiers,
		StatModifier attributeCeilings,
		BodyPart head,
		BodyPart torso,
		BodyPart leg,
		BodyPart hand,
		BodyPart foot,
		String leftHandIcon,
		String rightHandIcon,
		List<Gender> allowedGenders,
		boolean isMagicDead,
		Spell specialAbility,
		List<StartingKit> startingItems,
		List<NaturalWeapon> naturalWeapons,
		Map<String, List<String>> suggestedNames,
		String unlockVariable,
		String unlockDescription,
		Stats.Modifier favouredEnemyModifier,
		String characterCreationImage)
	{
		this.name = name;
		this.description = description;
		this.startingHitPointPercent = startingHitPointPercent;
		this.startingActionPointPercent = startingActionPointPercent;
		this.startingMagicPointPercent = startingMagicPointPercent;
		this.startingModifiers = startingModifiers;
		this.constantModifiers = constantModifiers;
		this.bannerModifiers = bannerModifiers;
		this.attributeCeilings = attributeCeilings;
		this.head = head;
		this.torso = torso;
		this.leg = leg;
		this.hand = hand;
		this.foot = foot;
		this.leftHandIcon = leftHandIcon;
		this.rightHandIcon = rightHandIcon;
		this.allowedGenders = allowedGenders;
		this.magicDead = isMagicDead;
		this.specialAbility = specialAbility;
		this.startingItems = startingItems;
		this.naturalWeapons = naturalWeapons;
		this.suggestedNames = suggestedNames;
		this.unlockVariable = unlockVariable;
		this.unlockDescription = unlockDescription;
		this.favouredEnemyModifier = favouredEnemyModifier;
		this.characterCreationImage = characterCreationImage;

		initBodyParts();
	}

	/*-------------------------------------------------------------------------*/
	public Race(FoeType ft)
	{
		this(ft.getName(),
			ft.getDescription(),
			0,0,0,
			ft.getStartingModifiers(),
			ft.getConstantModifiers(),
			ft.getBannerModifiers(),
			null,
			ft.getHead(),
			ft.getTorso(),
			ft.getLeg(),
			ft.getHand(),
			ft.getFoot(),
			null, null, null,
			ft.isMagicDead(),
			ft.getSpecialAbility(),
			null,
			ft.getNaturalWeapons(),
			null,
			null,
			null,
			ft.getFavouredEnemyModifier(),
			null);
	}

	/*-------------------------------------------------------------------------*/
	public void initBodyParts()
	{
		// todo: update the actual data model
		bodyParts = new PercentageTable<>();
		bodyParts.add(head, 18);
		bodyParts.add(torso, 33);
		bodyParts.add(leg, 31);
		bodyParts.add(hand, 8);
		bodyParts.add(foot, 10);
	}


	/*-------------------------------------------------------------------------*/
	public boolean isLocked()
	{
		return unlockVariable != null &&
			!Maze.getInstance().getUserConfig().getBoolean(this.unlockVariable);
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	@Override
	public Stats.Modifier getFavouredEnemyModifier()
	{
		return favouredEnemyModifier;
	}

	/*-------------------------------------------------------------------------*/
	public String getDescription()
	{
		return description;
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getConstantModifiers()
	{
		return constantModifiers;
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getStartingModifiers()
	{
		return startingModifiers;
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getBannerModifiers()
	{
		return bannerModifiers;
	}

	/*-------------------------------------------------------------------------*/
	public List<Gender> getAllowedGenders()
	{
		return allowedGenders;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isMagicDead()
	{
		return magicDead;
	}
	
	/*-------------------------------------------------------------------------*/
	public StartingKit getStartingItems(String name)
	{
		for (StartingKit i : startingItems)
		{
			if (i.getName().equals(name))
			{
				return i;
			}
		}

		throw new MazeException("Starting items not found: ["+name+"]");
	}

	/*-------------------------------------------------------------------------*/
	public Spell getSpecialAbility()
	{
		return specialAbility;
	}

	public BodyPart getFoot()
	{
		return foot;
	}

	public BodyPart getHand()
	{
		return hand;
	}

	public BodyPart getHead()
	{
		return head;
	}

	public BodyPart getLeg()
	{
		return leg;
	}

	public BodyPart getTorso()
	{
		return torso;
	}

	public String getLeftHandIcon()
	{
		return leftHandIcon;
	}

	public String getRightHandIcon()
	{
		return rightHandIcon;
	}

	public int getStartingHitPointPercent()
	{
		return startingHitPointPercent;
	}

	public int getStartingMagicPointPercent()
	{
		return startingMagicPointPercent;
	}

	public int getStartingActionPointPercent()
	{
		return startingActionPointPercent;
	}

	public List<StartingKit> getStartingItems()
	{
		return startingItems;
	}

	public StatModifier getAttributeCeilings()
	{
		return attributeCeilings;
	}

	public PercentageTable<BodyPart> getBodyParts()
	{
		return bodyParts;
	}

	public List<NaturalWeapon> getNaturalWeapons()
	{
		return naturalWeapons;
	}

	public Map<String, List<String>> getSuggestedNames()
	{
		return suggestedNames;
	}

	public String getUnlockVariable()
	{
		return unlockVariable;
	}

	public String getUnlockDescription()
	{
		return unlockDescription;
	}

	/*-------------------------------------------------------------------------*/
	public void setAllowedGenders(List<Gender> allowedGenders)
	{
		this.allowedGenders = allowedGenders;
	}

	public void setBannerModifiers(StatModifier bannerModifiers)
	{
		this.bannerModifiers = bannerModifiers;
	}

	public void setFoot(BodyPart foot)
	{
		this.foot = foot;
	}

	public void setHand(BodyPart hand)
	{
		this.hand = hand;
	}

	public void setHead(BodyPart head)
	{
		this.head = head;
	}

	public void setLeg(BodyPart leg)
	{
		this.leg = leg;
	}

	public void setTorso(BodyPart torso)
	{
		this.torso = torso;
	}

	public void setConstantModifiers(StatModifier constantModifiers)
	{
		this.constantModifiers = constantModifiers;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setLeftHandIcon(String leftHandIcon)
	{
		this.leftHandIcon = leftHandIcon;
	}

	public void setMagicDead(boolean magicDead)
	{
		this.magicDead = magicDead;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setRightHandIcon(String rightHandIcon)
	{
		this.rightHandIcon = rightHandIcon;
	}

	public void setSpecialAbility(Spell specialAbility)
	{
		this.specialAbility = specialAbility;
	}

	public void setStartingHitPointPercent(int startingHitPointPercent)
	{
		this.startingHitPointPercent = startingHitPointPercent;
	}

	public void setStartingMagicPointPercent(int startingMagicPointPercent)
	{
		this.startingMagicPointPercent = startingMagicPointPercent;
	}

	public void setStartingModifiers(StatModifier startingModifiers)
	{
		this.startingModifiers = startingModifiers;
	}

	public void setStartingActionPointPercent(int startingActionPointPercent)
	{
		this.startingActionPointPercent = startingActionPointPercent;
	}

	public void setStartingItems(List<StartingKit> startingItems)
	{
		this.startingItems = startingItems;
	}

	public void setAttributeCeilings(StatModifier attributeCeilings)
	{
		this.attributeCeilings = attributeCeilings;
	}

	public void setBodyParts(PercentageTable<BodyPart> bodyParts)
	{
		this.bodyParts = bodyParts;
	}

	public void setNaturalWeapons (List<NaturalWeapon> weapons)
	{
		naturalWeapons = weapons;
	}

	public void setSuggestedNames(Map<String, List<String>> suggestedNames)
	{
		this.suggestedNames = suggestedNames;
	}

	public void setUnlockVariable(String unlockVariable)
	{
		this.unlockVariable = unlockVariable;
	}

	public void setUnlockDescription(String unlockDescription)
	{
		this.unlockDescription = unlockDescription;
	}

	public void setFavouredEnemyModifier(Stats.Modifier favouredEnemyModifier)
	{
		this.favouredEnemyModifier = favouredEnemyModifier;
	}

	/*-------------------------------------------------------------------------*/

	public Item getLeftHandItem()
	{
		return new Item(new ItemTemplate())
		{
			public String getImage()
			{
				return getLeftHandIcon();
			}
		};
	}

	public Item getRightHandItem()
	{
		return new Item(new ItemTemplate())
		{
			public String getImage()
			{
				return getRightHandIcon();
			}
		};
	}

	/*-------------------------------------------------------------------------*/

	public String getCharacterCreationImage()
	{
		return this.characterCreationImage;
	}

	public void setCharacterCreationImage(String characterCreationImage)
	{
		this.characterCreationImage = characterCreationImage;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		return "Race{" +
			"name='" + name + '\'' +
			", description='" + description + '\'' +
			", startingHitPointPercent=" + startingHitPointPercent +
			", startingActionPointPercent=" + startingActionPointPercent +
			", startingMagicPointPercent=" + startingMagicPointPercent +
			", startingModifiers=" + startingModifiers +
			", constantModifiers=" + constantModifiers +
			", bannerModifiers=" + bannerModifiers +
			", attributeCeilings=" + attributeCeilings +
			", head=" + head +
			", torso=" + torso +
			", leg=" + leg +
			", hand=" + hand +
			", foot=" + foot +
			", rightHandIcon='" + rightHandIcon + '\'' +
			", leftHandIcon='" + leftHandIcon + '\'' +
			", allowedGenders=" + allowedGenders +
			", magicDead=" + magicDead +
			", specialAbility=" + specialAbility +
			", startingItems=" + startingItems +
			", bodyParts=" + bodyParts +
			", naturalWeapons=" + naturalWeapons +
			", suggestedNames=" + suggestedNames +
			", unlockVariable='" + unlockVariable + '\'' +
			", unlockDescription='" + unlockDescription + '\'' +
			", favouredEnemyModifier=" + favouredEnemyModifier +
			", characterCreationImage='" + characterCreationImage + '\'' +
			"} " + super.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Race))
		{
			return false;
		}

		Race race = (Race)o;

		if (getStartingHitPointPercent() != race.getStartingHitPointPercent())
		{
			return false;
		}
		if (getStartingActionPointPercent() != race.getStartingActionPointPercent())
		{
			return false;
		}
		if (getStartingMagicPointPercent() != race.getStartingMagicPointPercent())
		{
			return false;
		}
		if (isMagicDead() != race.isMagicDead())
		{
			return false;
		}
		if (getName() != null ? !getName().equals(race.getName()) : race.getName() != null)
		{
			return false;
		}
		if (getDescription() != null ? !getDescription().equals(race.getDescription()) : race.getDescription() != null)
		{
			return false;
		}
		if (getStartingModifiers() != null ? !getStartingModifiers().equals(race.getStartingModifiers()) : race.getStartingModifiers() != null)
		{
			return false;
		}
		if (getConstantModifiers() != null ? !getConstantModifiers().equals(race.getConstantModifiers()) : race.getConstantModifiers() != null)
		{
			return false;
		}
		if (getBannerModifiers() != null ? !getBannerModifiers().equals(race.getBannerModifiers()) : race.getBannerModifiers() != null)
		{
			return false;
		}
		if (getAttributeCeilings() != null ? !getAttributeCeilings().equals(race.getAttributeCeilings()) : race.getAttributeCeilings() != null)
		{
			return false;
		}
		if (getHead() != null ? !getHead().equals(race.getHead()) : race.getHead() != null)
		{
			return false;
		}
		if (getTorso() != null ? !getTorso().equals(race.getTorso()) : race.getTorso() != null)
		{
			return false;
		}
		if (getLeg() != null ? !getLeg().equals(race.getLeg()) : race.getLeg() != null)
		{
			return false;
		}
		if (getHand() != null ? !getHand().equals(race.getHand()) : race.getHand() != null)
		{
			return false;
		}
		if (getFoot() != null ? !getFoot().equals(race.getFoot()) : race.getFoot() != null)
		{
			return false;
		}
		if (getRightHandIcon() != null ? !getRightHandIcon().equals(race.getRightHandIcon()) : race.getRightHandIcon() != null)
		{
			return false;
		}
		if (getLeftHandIcon() != null ? !getLeftHandIcon().equals(race.getLeftHandIcon()) : race.getLeftHandIcon() != null)
		{
			return false;
		}
		if (getAllowedGenders() != null ? !getAllowedGenders().equals(race.getAllowedGenders()) : race.getAllowedGenders() != null)
		{
			return false;
		}
		if (getSpecialAbility() != null ? !getSpecialAbility().equals(race.getSpecialAbility()) : race.getSpecialAbility() != null)
		{
			return false;
		}
		if (getStartingItems() != null ? !getStartingItems().equals(race.getStartingItems()) : race.getStartingItems() != null)
		{
			return false;
		}
		if (getBodyParts() != null ? !getBodyParts().equals(race.getBodyParts()) : race.getBodyParts() != null)
		{
			return false;
		}
		if (getNaturalWeapons() != null ? !getNaturalWeapons().equals(race.getNaturalWeapons()) : race.getNaturalWeapons() != null)
		{
			return false;
		}
		if (getSuggestedNames() != null ? !getSuggestedNames().equals(race.getSuggestedNames()) : race.getSuggestedNames() != null)
		{
			return false;
		}
		if (getUnlockVariable() != null ? !getUnlockVariable().equals(race.getUnlockVariable()) : race.getUnlockVariable() != null)
		{
			return false;
		}
		if (getUnlockDescription() != null ? !getUnlockDescription().equals(race.getUnlockDescription()) : race.getUnlockDescription() != null)
		{
			return false;
		}
		if (getFavouredEnemyModifier() != race.getFavouredEnemyModifier())
		{
			return false;
		}
		return getCharacterCreationImage() != null ? getCharacterCreationImage().equals(race.getCharacterCreationImage()) : race.getCharacterCreationImage() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName().hashCode();
		result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
		result = 31 * result + getStartingHitPointPercent();
		result = 31 * result + getStartingActionPointPercent();
		result = 31 * result + getStartingMagicPointPercent();
		result = 31 * result + (getStartingModifiers() != null ? getStartingModifiers().hashCode() : 0);
		result = 31 * result + (getConstantModifiers() != null ? getConstantModifiers().hashCode() : 0);
		result = 31 * result + (getBannerModifiers() != null ? getBannerModifiers().hashCode() : 0);
		result = 31 * result + (getAttributeCeilings() != null ? getAttributeCeilings().hashCode() : 0);
		result = 31 * result + (getHead() != null ? getHead().hashCode() : 0);
		result = 31 * result + (getTorso() != null ? getTorso().hashCode() : 0);
		result = 31 * result + (getLeg() != null ? getLeg().hashCode() : 0);
		result = 31 * result + (getHand() != null ? getHand().hashCode() : 0);
		result = 31 * result + (getFoot() != null ? getFoot().hashCode() : 0);
		result = 31 * result + (getRightHandIcon() != null ? getRightHandIcon().hashCode() : 0);
		result = 31 * result + (getLeftHandIcon() != null ? getLeftHandIcon().hashCode() : 0);
		result = 31 * result + (getAllowedGenders() != null ? getAllowedGenders().hashCode() : 0);
		result = 31 * result + (isMagicDead() ? 1 : 0);
		result = 31 * result + (getSpecialAbility() != null ? getSpecialAbility().hashCode() : 0);
		result = 31 * result + (getStartingItems() != null ? getStartingItems().hashCode() : 0);
		result = 31 * result + (getBodyParts() != null ? getBodyParts().hashCode() : 0);
		result = 31 * result + (getNaturalWeapons() != null ? getNaturalWeapons().hashCode() : 0);
		result = 31 * result + (getSuggestedNames() != null ? getSuggestedNames().hashCode() : 0);
		result = 31 * result + (getUnlockVariable() != null ? getUnlockVariable().hashCode() : 0);
		result = 31 * result + (getUnlockDescription() != null ? getUnlockDescription().hashCode() : 0);
		result = 31 * result + (getFavouredEnemyModifier() != null ? getFavouredEnemyModifier().hashCode() : 0);
		result = 31 * result + (getCharacterCreationImage() != null ? getCharacterCreationImage().hashCode() : 0);
		return result;
	}
}
