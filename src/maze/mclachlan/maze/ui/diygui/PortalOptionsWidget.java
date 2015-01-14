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

package mclachlan.maze.ui.diygui;

import java.awt.Rectangle;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Trap;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class PortalOptionsWidget extends DIYPane
	implements ActionListener, ChooseCharacterCallback, UseItemCallback, CastSpellCallback
{
	private int inset=4;
	private int buttonHeight=18;

	private DIYButton pickLock, spell, leave, force, use;

	private Portal portal;
	private Object lastObj;

	/*-------------------------------------------------------------------------*/
	public PortalOptionsWidget(Rectangle bounds)
	{
		super(bounds);
		int buttonCols = 3;
		int buttonRows = height/buttonHeight;

		setLayoutManager(new DIYGridLayout(buttonCols, buttonRows, inset, inset));

		pickLock = new DIYButton("(P)ick Lock");
		pickLock.addActionListener(this);

		spell = new DIYButton("(C)ast Spell");
		spell.addActionListener(this);

		leave = new DIYButton("(L)eave");
		leave.addActionListener(this);

		force = new DIYButton("(F)orce");
		force.addActionListener(this);

		use = new DIYButton("(U)se Item");
		use.addActionListener(this);

		add(pickLock);
		add(force);
		add(spell);
		add(use);
		add(leave);
	}

	/*-------------------------------------------------------------------------*/
	public void setPortal(Portal portal)
	{
		this.portal = portal;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		if (obj == force)
		{
			force();
		}
		else if (obj == pickLock)
		{
			pickLock();
		}
		else if (obj == spell)
		{
			castSpell();
		}
		else if (obj == use)
		{
			useItem();
		}
		else if (obj == leave)
		{
			leave();
		}
	}

	public void leave()
	{
		Maze.getInstance().setState(Maze.State.MOVEMENT);
	}

	public void useItem()
	{
		lastObj = use;
		DiyGuiUserInterface.instance.chooseACharacter(this);
	}

	public void castSpell()
	{
		lastObj = spell;
		DiyGuiUserInterface.instance.chooseACharacter(this);
	}

	public void pickLock()
	{
		lastObj = pickLock;
		DiyGuiUserInterface.instance.chooseACharacter(this);
	}

	public void force()
	{
		lastObj = force;
		DiyGuiUserInterface.instance.chooseACharacter(this);
	}

	/*-------------------------------------------------------------------------*/
	private void resolveLockOrTrapSpell(
		Spell spell, PlayerCharacter caster, int castingLevel)
	{
		SpellEffect spellEffect = null;
		List<SpellEffect> effects = spell.getEffects().getRandom();

		for (SpellEffect s : effects)
		{
			if (s.getUnsavedResult() instanceof UnlockSpellResult)
			{
				spellEffect = s;
				break;
			}
		}

		if (spellEffect == null)
		{
			throw new MazeException("No UnlockSpellResult found for ["+spell.getName()+"]");
		}

		Value modifier = ((UnlockSpellResult)spellEffect.getUnsavedResult()).getValue();

		BitSet disarmed = new BitSet(8);
		for (int tool=0; tool<8; tool++)
		{
			if (!portal.getRequired().get(tool))
			{
				continue;
			}

			int result = GameSys.getInstance().pickLockWithSpell(
				caster, castingLevel, modifier, portal, tool);

			if (result == Trap.DisarmResult.SPRING_TRAP)
			{
				spellEvents(result, caster, spell, castingLevel);
				return;
			}
			else if (result == Trap.DisarmResult.DISARMED)
			{
				disarmed.set(tool);
			}
		}

		if (disarmed.equals(portal.getRequired()))
		{
			spellEvents(Trap.DisarmResult.DISARMED, caster, spell, castingLevel);
			unlock();
		}
		else
		{
			spellEvents(Trap.DisarmResult.NOTHING, caster, spell, castingLevel);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void resolveLockOrTrapItem(
		Item item, PlayerCharacter caster)
	{
		Spell spell = item.getInvokedSpell();

		if (spell == null || spell.getTargetType() != MagicSys.SpellTargetType.LOCK_OR_TRAP)
		{
			throw new MazeException("No UnlockSpellResult found for "+item.getName());
		}

		// todo: multi-effect items?
		Value modifier = ((UnlockSpellResult)spell.getEffects().getPossibilities().get(0).getUnsavedResult()).getValue();

		BitSet disarmed = new BitSet(8);
		for (int tool=0; tool<8; tool++)
		{
			if (!portal.getRequired().get(tool))
			{
				continue;
			}

			int result = GameSys.getInstance().pickLockWithSpell(
				caster, item.getInvokedSpellLevel(), modifier, portal, tool);

			if (result == Trap.DisarmResult.SPRING_TRAP)
			{
				itemEvents(result, caster, item, item.getInvokedSpellLevel());
				return;
			}
			else if (result == Trap.DisarmResult.DISARMED)
			{
				disarmed.set(tool);
			}
		}

		if (disarmed.equals(portal.getRequired()))
		{
			itemEvents(Trap.DisarmResult.DISARMED, caster, item, item.getInvokedSpellLevel());
			unlock();
		}
		else
		{
			itemEvents(Trap.DisarmResult.NOTHING, caster, item, item.getInvokedSpellLevel());
		}
	}

	/*-------------------------------------------------------------------------*/
	void spellEvents(int disarmResult, PlayerCharacter caster, Spell spell, int castingLevel)
	{
		Maze maze = Maze.getInstance();

		ArrayList<MazeEvent> events = new ArrayList<MazeEvent>();

		events.add(new SpellCastEvent(caster, spell, castingLevel));

		int spellFailure = GameSys.getInstance().getSpellFailureChance(caster, spell, castingLevel);

		if (Dice.d100.roll() <= spellFailure)
		{
			// spell fizzles
			events.add(new SpellFizzlesEvent(caster, spell, castingLevel));
		}
		else
		{
			switch (disarmResult)
			{
				case Trap.DisarmResult.NOTHING:
					events.add(new NoEffectEvent());
					break;
				case Trap.DisarmResult.DISARMED:
					events.add(new SuccessEvent());
					break;
				case Trap.DisarmResult.SPRING_TRAP:
					events.add(new FailureEvent());
					break;
				default:
					throw new MazeException("Invalid result: "+disarmResult);
			}
		}

		// Then, show the combat listener
		maze.setState(Maze.State.COMBAT);
		maze.getUi().showCombatDisplay();

		maze.resolveEvents(events);

//		combat.endRound();
//		combat.endCombat();
		maze.setState(Maze.State.ENCOUNTER_PORTAL);

	}

	/*-------------------------------------------------------------------------*/
	void itemEvents(int disarmResult, PlayerCharacter caster, Item item, int castingLevel)
	{
		Maze maze = Maze.getInstance();

		ArrayList<MazeEvent> events = new ArrayList<MazeEvent>();

		events.add(new ItemUseEvent(caster, item));

		switch (disarmResult)
		{
			case 0: break;
			case Trap.DisarmResult.NOTHING:
				events.add(new NoEffectEvent());
				break;
			case Trap.DisarmResult.DISARMED:
				events.add(new SuccessEvent());
				break;
			case Trap.DisarmResult.SPRING_TRAP:
				events.add(new FailureEvent());
				break;
			default:
				throw new MazeException("Invalid result: "+disarmResult);
		}

		// Then, show the combat listener
		maze.setState(Maze.State.COMBAT);
		maze.getUi().showCombatDisplay();

		maze.resolveEvents(events);

//		combat.endRound();
//		combat.endCombat();
		maze.setState(Maze.State.ENCOUNTER_PORTAL);
	}

	/*-------------------------------------------------------------------------*/
	public boolean characterChosen(PlayerCharacter pc, int pcIndex)
	{
		if (lastObj == pickLock)
		{
			int x = DiyGuiUserInterface.SCREEN_WIDTH/4;
			int y = DiyGuiUserInterface.SCREEN_HEIGHT/5*3;
			Rectangle rectangle = new Rectangle(x, y,
				DiyGuiUserInterface.SCREEN_WIDTH/2,
				DiyGuiUserInterface.SCREEN_HEIGHT/3);

			PickLockWidget dialog = new PickLockWidget(portal, rectangle, pc);
			Maze.getInstance().getUi().showDialog(dialog);
		}
		else if (lastObj == force)
		{
			int result = GameSys.getInstance().forcePortal(pc, portal);

			pc.getActionPoints().setCurrent(0);
			CurMaxSub hp = pc.getHitPoints();

			switch (result)
			{
				case Portal.ForceResult.FAILED_NO_DAMAGE:
					Maze.getInstance().walkIntoWall();
					DiyGuiUserInterface.instance.addMessage("OUCH!");
					break;
				case Portal.ForceResult.FAILED_DAMAGE:
					Maze.getInstance().walkIntoWall();
					DiyGuiUserInterface.instance.addMessage("OUCH!");
					hp.incSub(portal.getHitPointCostToForce());
					break;
				case Portal.ForceResult.SUCCESS:
					MazeScript script = Database.getInstance().getScript("_FORCE_PORTAL_");
					Maze.getInstance().resolveEvents(script.getEvents());
					DiyGuiUserInterface.instance.addMessage("SUCCESSFULLY FORCED!");
					hp.incSub(portal.getHitPointCostToForce());
					unlock();
					break;
				default:
					throw new MazeException("Invalid result: "+result);
			}

			if (hp.getSub() >= hp.getCurrent() && hp.getCurrent() > 0)
			{
				ConditionTemplate kot = Database.getInstance().getConditionTemplate(
					Constants.Conditions.FATIGUE_KO);
				Condition ko = kot.create(
					pc, pc, 1, MagicSys.SpellEffectType.NONE, MagicSys.SpellEffectSubType.NONE);
				pc.addCondition(ko);
			}

		}
		else if (lastObj == spell)
		{
			new CastSpell(this, pc);
		}
		else if (lastObj == leave)
		{
			leave();
		}
		else if (lastObj == use)
		{
			new UseItem(pc.getName(), this, pc);
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	private void unlock()
	{
		portal.setState(Portal.State.UNLOCKED);
		leave();
	}

	/*-------------------------------------------------------------------------*/
	public boolean useItem(
		Item item, PlayerCharacter user, int userIndex, SpellTarget target)
	{
		if (item.getInvokedSpell() != null &&
			item.getInvokedSpell().getTargetType() == MagicSys.SpellTargetType.LOCK_OR_TRAP)
		{
			// what we're really interested in
			resolveLockOrTrapItem(item, user);
			return true;
		}
		else if (item.getName().equals(portal.getKeyItem()))
		{
			unlock();
			MazeScript script = Database.getInstance().getScript("_UNLOCK_");
			Maze.getInstance().resolveEvents(script.getEvents());
			DiyGuiUserInterface.instance.addMessage("UNLOCKED!");
			if (portal.consumeKeyItem())
			{
				user.removeItem(item, false);
			}
		}

		// back to default behaviour
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean castSpell(
		Spell spell,
		PlayerCharacter caster, int casterIndex,
		int castingLevel,
		int target)
	{
		if (spell.getTargetType() == MagicSys.SpellTargetType.LOCK_OR_TRAP)
		{
			// what we're really interested in
			resolveLockOrTrapSpell(spell, caster, castingLevel);
			return true;
		}

		return false;
	}
}
