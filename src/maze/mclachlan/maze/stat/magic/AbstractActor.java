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

package mclachlan.maze.stat.magic;

import java.util.List;
import java.util.ArrayList;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.ModifierValue;
import mclachlan.maze.stat.combat.*;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.npc.NpcScript;

/**
 *
 */
public abstract class AbstractActor extends UnifiedActor
{
	private CombatantData data;

	/*-------------------------------------------------------------------------*/
	public CombatantData getCombatantData()
	{
		return this.data;
	}

	public void setCombatantData(CombatantData data)
	{
		this.data = data;
	}

	/*-------------------------------------------------------------------------*/
	public void addCondition(Condition c) {}
	public void deductAmmo(AttackEvent event) {}
	public ActorGroup getActorGroup() { return null; }
	public Item getArmour(BodyPart bodyPart) { return null; }
	public PercentageTable<BodyPart> getBodyParts() { return null; }
	public List<CombatAction> getCombatActions(
		ActorActionIntention actionIntention) { return null; }
	public List<Condition> getConditions() { return new ArrayList<Condition>(); }
	public int getLevel() { return 0; }
	public String getType() { return Foe.Type.NONE; }
	public int getModifier(String modifier) { return 0; }
	public int getModifier(String modifier, boolean checkCarryingCapacity) {return 0;}
	public int getBaseModifier(String modifier) { return 0; };
	public String getName() { return "Abstract Actor"; }
	public String getDisplayName() { return getName(); }

	@Override
	public String getDisplayNamePlural() { return getName(); }

	public CurMax getActionPoints() { return new CurMax(); }
	public CurMaxSub getHitPoints() { return new CurMaxSub(); }
	public CurMax getMagicPoints() { return new CurMax(); }
	public void removeCondition(Condition c) {}
	public void setModifier(String modifier, int value) {}
	public void removeItem(Item item, boolean removeWholeStack) {}

	public void removeItem(String itemName, boolean removeWholeStack) {}

	public void regenerateResources(long turnNr, boolean resting, boolean combat, Tile currentTile) {}
	public void removeCurse(int strength) {}
	public void addAllies(List<FoeGroup> foeGroups) {}

	public boolean meetsRequirements(StatModifier req) { return false; }
	public boolean isActiveModifier(String modifier) {return true;}

	@Override
	public List<AttackWith> getAttackWithOptions() { return new ArrayList<AttackWith>(); }

	public Item getEquippedItem(EquipableSlot.Type type, int i) {return null;};

	public int getCarrying() {return 0;};

	public Gender getGender(){return null;};
	public Race getRace(){return null;};
	public CharacterClass getCharacterClass(){return null;};
	public ModifierValue collectConditionBanners(String modifier) {return null;};
	public int getAmountMagicPresent(int colour) {return 0;};

	public void inventoryItemAdded(Item item) {}

	@Override
	public NpcScript getActionScript()
	{
		return null;
	}

	;

	@Override
	public List<SpellLikeAbility> getSpellLikeAbilities()
	{
		return new ArrayList<SpellLikeAbility>();
	}

	@Override
	public CharacterClass.Focus getFocus()
	{
		return CharacterClass.Focus.COMBAT;
	}

	@Override
	public String getFaction()
	{
		return null;
	}
}
