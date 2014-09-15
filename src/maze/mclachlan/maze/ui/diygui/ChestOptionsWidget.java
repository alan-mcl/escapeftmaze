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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.CheckPartyStatusEvent;
import mclachlan.maze.game.event.MazeScriptEvent;
import mclachlan.maze.map.Trap;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.map.script.RemoveObjectEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ChestOptionsWidget extends DIYPane
	implements ActionListener, ChooseCharacterCallback, UseItemCallback, CastSpellCallback
{
	private int inset=4;
	private int buttonHeight=18;

	private DIYButton disarm, spell, leave, open, use;

	private Chest chest;
	private Object lastObj;

	/*-------------------------------------------------------------------------*/
	public ChestOptionsWidget(Rectangle bounds)
	{
		super(bounds);
		int buttonCols = 3;
		int buttonRows = height/buttonHeight;

		setLayoutManager(new DIYGridLayout(buttonCols, buttonRows, inset, inset));

		disarm = new DIYButton("(D)isarm");
		disarm.addActionListener(this);

		spell = new DIYButton("(C)ast Spell");
		spell.addActionListener(this);

		leave = new DIYButton("(L)eave");
		leave.addActionListener(this);

		open = new DIYButton("(O)pen");
		open.addActionListener(this);

		use = new DIYButton("(U)se Item");
		use.addActionListener(this);

		add(disarm);
		add(spell);
		add(leave);
		add(open);
		add(use);
	}

	/*-------------------------------------------------------------------------*/
	public void setChest(Chest chest)
	{
		this.chest = chest;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		if (obj == disarm)
		{
			disarm();
		}
		else if (obj == open)
		{
			open();
		}
		else if (obj == spell)
		{
			spell();
		}
		else if (obj == use)
		{
			use();
		}
		else if (obj == leave)
		{
			leave();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void leave()
	{
		Maze maze = Maze.getInstance();
		maze.appendEvents(maze.new SetStateEvent(Maze.State.MOVEMENT));
	}

	public void use()
	{
		if (use.isEnabled())
		{
			lastObj = use;
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	public void spell()
	{
		if (spell.isEnabled())
		{
			lastObj = spell;
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	public void open()
	{
		if (open.isEnabled())
		{
			lastObj = open;
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	public void disarm()
	{
		if (disarm.isEnabled())
		{
			lastObj = disarm;
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void resolveLockOrTrapSpell(
		Spell spell, PlayerCharacter caster, int castingLevel)
	{
		SpellEffect spellEffect = null;
		List<SpellEffect> effects = spell.getEffects().getPossibilities();

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

		Trap trap = chest.getCurrentTrap();
		if (trap == null)
		{
			spellEvents(0, caster, spell, castingLevel);
			executeChestContents();
			return;
		}

		Value modifier = ((UnlockSpellResult)spellEffect.getUnsavedResult()).getValue();

		BitSet disarmed = new BitSet(8);
		for (int tool=0; tool<8; tool++)
		{
			if (!trap.getRequired().get(tool))
			{
				continue;
			}

			int result = GameSys.getInstance().disarmWithSpell(
				caster, castingLevel, modifier, trap, tool);

			if (result == Trap.DisarmResult.SPRING_TRAP)
			{
				spellEvents(result, caster, spell, castingLevel);
				springTrap();
				return;
			}
			else if (result == Trap.DisarmResult.DISARMED)
			{
				disarmed.set(tool);
			}
		}

		if (disarmed.equals(trap.getRequired()))
		{
			spellEvents(Trap.DisarmResult.DISARMED, caster, spell, castingLevel);
			executeChestContents();
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

		Trap trap = chest.getCurrentTrap();
		if (trap == null)
		{
			itemEvents(0, caster, item, item.getInvokedSpellLevel());
			executeChestContents();
			return;
		}

		// todo: multi-effect items?
		Value modifier = ((UnlockSpellResult)spell.getEffects().getPossibilities().get(0).getUnsavedResult()).getValue();

		BitSet disarmed = new BitSet(8);
		for (int tool=0; tool<8; tool++)
		{
			if (!trap.getRequired().get(tool))
			{
				continue;
			}

			int result = GameSys.getInstance().disarmWithSpell(
				caster, item.getInvokedSpellLevel(), modifier, trap, tool);

			if (result == Trap.DisarmResult.SPRING_TRAP)
			{
				itemEvents(result, caster, item, item.getInvokedSpellLevel());
				springTrap();
				return;
			}
			else if (result == Trap.DisarmResult.DISARMED)
			{
				disarmed.set(tool);
			}
		}

		if (disarmed.equals(trap.getRequired()))
		{
			itemEvents(Trap.DisarmResult.DISARMED, caster, item, item.getInvokedSpellLevel());
			executeChestContents();
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
		maze.appendEvents(maze.new PushStateEvent(Maze.State.COMBAT));
		maze.appendEvents(maze.new ShowCombatDisplayEvent());
		maze.appendEvents(events);
		maze.appendEvents(maze.new PopStateEvent());
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
		maze.appendEvents(maze.new PushStateEvent(Maze.State.COMBAT));
		maze.appendEvents(maze.new ShowCombatDisplayEvent());
		maze.appendEvents(events);
		maze.appendEvents(maze.new PopStateEvent());
	}

	/*-------------------------------------------------------------------------*/
	void springTrap()
	{
		Maze maze = Maze.getInstance();
		Point tile = maze.getTile();
		int facing = maze.getFacing();

		if (chest.getTraps() != null &&
			chest.getTraps().size()>0 &&
			chest.getCurrentTrap() != null)
		{
			maze.appendEvents(
				chest.getCurrentTrap().getPayload().execute(maze, tile, tile, facing));
		}

		if (maze.getCurrentCombat() != null)
		{
			// something has started a combat
			//leave the chest basically unopened
			return;
		}

		// check if trap has killed the party
		maze.appendEvents(new CheckPartyStatusEvent());

		if (Maze.getInstance().getParty() != null && Maze.getInstance().getParty().numAlive() > 0)
		{
			executeChestContents();
		}
	}

	/*-------------------------------------------------------------------------*/
	void executeChestContents()
	{
		Maze maze = Maze.getInstance();
		Point tile = maze.getTile();
		int facing = maze.getFacing();
		
		// chest opens
		maze.appendEvents(new MazeScriptEvent("_OPEN_CHEST_"));
		
		// chest contents
		maze.appendEvents(
			chest.getChestContents().execute(maze, tile, tile, facing));
		maze.appendEvents(new SetChestStateEvent(Chest.State.EMPTY));
		maze.appendEvents(new RemoveObjectEvent(chest.getEngineObject()));

		leave();
	}

	/*-------------------------------------------------------------------------*/
	public boolean characterChosen(PlayerCharacter pc, int pcIndex)
	{
		if (lastObj == disarm)
		{
			int x = DiyGuiUserInterface.SCREEN_WIDTH/4;
			int y = DiyGuiUserInterface.SCREEN_HEIGHT/5*3;
			Rectangle rectangle = new Rectangle(x, y,
				DiyGuiUserInterface.SCREEN_WIDTH/2,
				DiyGuiUserInterface.SCREEN_HEIGHT/3);

			DisarmTrapWidget dialog = new DisarmTrapWidget(
				chest.getCurrentTrap(), rectangle, this, pc);
			Maze.getInstance().getUi().showDialog(dialog);
		}
		else if (lastObj == open)
		{
			// open the chest and damn the consequences
			Maze.getInstance().appendEvents(new MazeScriptEvent("_OPEN_CHEST_"));
			
			springTrap();
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
	public boolean useItem(
		Item item, PlayerCharacter user, int userIndex, SpellTarget target)
	{
		if (item == null)
		{
			return false;
		}

		Spell invokedSpell = item.getInvokedSpell();

		if (invokedSpell == null)
		{
			return false;
		}

		if (invokedSpell.getTargetType() == MagicSys.SpellTargetType.LOCK_OR_TRAP)
		{
			// what we're really interested in
			resolveLockOrTrapItem(item, user);
			return true;
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

	/*-------------------------------------------------------------------------*/
	public class SetChestStateEvent extends MazeEvent
	{
		private String state;

		public SetChestStateEvent(String state)
		{
			this.state = state;
		}

		public List<MazeEvent> resolve()
		{
			chest.setState(state);
			return null;
		}
	}
}
