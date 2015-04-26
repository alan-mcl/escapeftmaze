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

package mclachlan.maze.stat.condition.impl;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.*;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.ui.diygui.Constants;

/**
 * A custom condition impl for when an actor passes out from fatigue.
 */
public class Berserk extends Condition
{
	private static ConditionEffect effect = new BerserkEffect();

	private static final StatModifier berserk = new StatModifier();

	/*-------------------------------------------------------------------------*/
	static
	{
		berserk.setModifier(Stats.Modifiers.BRAWN, +3);
		berserk.setModifier(Stats.Modifiers.SKILL, -2);
		berserk.setModifier(Stats.Modifiers.BRAINS, -5);
		berserk.setModifier(Stats.Modifiers.SNEAKING, -10);
		berserk.setModifier(Stats.Modifiers.BONUS_ATTACKS, 1);
		berserk.setModifier(Stats.Modifiers.DAMAGE, +1);
		berserk.setModifier(Stats.Modifiers.DEFENCE, -4);
		berserk.setModifier(Stats.Modifiers.INITIATIVE, +2);
		berserk.setModifier(Stats.Modifiers.RESIST_BLUDGEONING, +15);
		berserk.setModifier(Stats.Modifiers.RESIST_SLASHING, +15);
		berserk.setModifier(Stats.Modifiers.RESIST_PIERCING, +15);
		berserk.setModifier(Stats.Modifiers.RESIST_MENTAL, +10);
		berserk.setModifier(Stats.Modifiers.STAMINA_REGEN, +20);
		berserk.setModifier(Stats.Modifiers.ACTION_POINT_REGEN, -20);
		berserk.setModifier(Stats.Modifiers.IMMUNE_TO_FEAR, 1);
		berserk.setModifier(Stats.Modifiers.IMMUNE_TO_IRRITATE, 1);
		berserk.setModifier(Stats.Modifiers.DAMAGE_MULTIPLIER, 1);
		berserk.setModifier(Stats.Modifiers.LIGHTNING_STRIKE_UNARMED, 1);
	}

	/*-------------------------------------------------------------------------*/
	public Berserk()
	{
		setDuration(Integer.MAX_VALUE);
		setStrength(Integer.MAX_VALUE);
		setIdentified(true);
		setStrengthIdentified(false);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getName()
	{
		return Constants.Conditions.BERSERK;
	}

	@Override
	public String getDisplayName()
	{
		return "Berserk";
	}

	@Override
	public String getIcon()
	{
		return "condition/berserk";
	}

	@Override
	public String getAdjective()
	{
		return "berserk";
	}

	@Override
	public int getModifier(String modifier, ConditionBearer bearer)
	{
		return berserk.getModifier(modifier);
	}

	@Override
	public Map<String, Integer> getModifiers()
	{
		return berserk.getModifiers();
	}

	@Override
	public ConditionEffect getEffect()
	{
		return effect;
	}

	@Override
	public boolean isStrengthWanes()
	{
		return false;
	}

	@Override
	public void setCastingLevel(int castingLevel)
	{
		super.setCastingLevel(castingLevel);
	}

	@Override
	public MagicSys.SpellEffectType getType()
	{
		return MagicSys.SpellEffectType.NONE;
	}

	@Override
	public MagicSys.SpellEffectSubType getSubtype()
	{
		return MagicSys.SpellEffectSubType.NONE;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		// only end berserk once combat is over or the bearer passes out

		if (Maze.getInstance().getState() != Maze.State.COMBAT)
		{
			// todo: stick around a few turns?
			setDuration(-1);
			return null;
		}

		ConditionBearer target = getTarget();
		if (target instanceof UnifiedActor)
		{
			if (!GameSys.getInstance().isActorAware((UnifiedActor)target))
			{
				setDuration(-1);
				return null;
			}
		}

		setDuration(Integer.MAX_VALUE);
		return null;
	}

	/*-------------------------------------------------------------------------*/
	static class BerserkEffect extends ConditionEffect
	{
		/*----------------------------------------------------------------------*/
		public ActorActionIntention checkIntention(
			UnifiedActor actor,
			Combat combat,
			ActorActionIntention intention,
			Condition condition)
		{
			//
			// Berserk characters always attack if possible
			// todo: with melee weapons, drop anything else.
			//

			if (actor instanceof Foe)
			{
				Foe foe = (Foe)actor;

				// try to find a melee attack
				for (int i=0; i<100; i++)
				{
					ActorActionIntention actorActionOption = foe.getCombatIntention();

					if (actorActionOption instanceof FoeAttackIntention)
					{
						FoeAttackIntention fai = (FoeAttackIntention)actorActionOption;

						if (fai.getFoeAttack().getType() == FoeAttack.Type.MELEE_ATTACK)
						{
							return actorActionOption;
						}
					}
				}

				// otherwise just default to the original
				return intention;
			}
			else
			{
				List<ActorGroup> foeGroups = combat.getFoesOf(actor);
				ActorGroup actorGroup = foeGroups.get(0);
				return new AttackIntention(actorGroup, combat, actor.getAttackWithOptions().get(0));
			}
		}
	}
}