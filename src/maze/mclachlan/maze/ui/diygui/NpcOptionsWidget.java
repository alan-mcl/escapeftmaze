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
import java.awt.event.KeyEvent;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.stat.npc.CastSpellOnNpcEvent;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.UseItemOnNpcEvent;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class NpcOptionsWidget extends DIYPane
	implements ActionListener, ChooseCharacterCallback,
	UseItemCallback, GiveItemCallback, CastSpellCallback,
	GetAmountCallback, TheftCallback, GuildCallback
{
	private int inset=4;
	private int buttonHeight=18;

	private CardLayoutWidget cards;

	// friendly
	private DIYButton talk, trade, steal, spell, leave, attack, use, give, guild;
	// neutral
	private DIYButton stealN, spellN, leaveN, attackN, useN, threatenN, bribeN, giveN;

	private Npc npc;
	private Object lastObj;
	private DIYLabel friendlyNameLabel, neutralNameLabel;
	private DIYPane friendlyPane;
	private DIYPane neutralPane;

	/*-------------------------------------------------------------------------*/
	public NpcOptionsWidget(Rectangle bounds)
	{
		super(bounds);
		int buttonCols = 3;
		int buttonRows = height/buttonHeight;

		talk = new DIYButton("T(a)lk");
		talk.addActionListener(this);

		trade = new DIYButton("(T)rade");
		trade.addActionListener(this);

		steal = new DIYButton("(S)teal");
		steal.addActionListener(this);

		spell = new DIYButton("(C)ast Spell");
		spell.addActionListener(this);

		leave = new DIYButton("(L)eave");
		leave.addActionListener(this);

		attack = new DIYButton("Attac(k)");
		attack.addActionListener(this);

		use = new DIYButton("(U)se Item");
		use.addActionListener(this);

		give = new DIYButton("(G)ive");
		give.addActionListener(this);
		
		guild = new DIYButton("Guil(d)");
		guild.addActionListener(this);

		friendlyNameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
		neutralNameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);

		friendlyPane = new DIYPane(new DIYGridLayout(buttonCols, buttonRows, inset, inset));
		friendlyPane.setBounds(bounds);

		friendlyPane.add(friendlyNameLabel);
		friendlyPane.add(new DIYLabel());
		friendlyPane.add(new DIYLabel());
		friendlyPane.add(talk);
		friendlyPane.add(trade);
		friendlyPane.add(guild);
		friendlyPane.add(give);
		friendlyPane.add(steal);
		friendlyPane.add(spell);
		friendlyPane.add(use);
		friendlyPane.add(attack);
		friendlyPane.add(leave);
		
		stealN = new DIYButton("(S)teal");
		stealN.addActionListener(this);

		spellN = new DIYButton("(C)ast Spell");
		spellN.addActionListener(this);

		leaveN = new DIYButton("(L)eave");
		leaveN.addActionListener(this);

		attackN = new DIYButton("Attac(k)");
		attackN.addActionListener(this);

		useN = new DIYButton("(U)se Item");
		useN.addActionListener(this);

		threatenN = new DIYButton("(T)hreaten");
		threatenN.addActionListener(this);

		bribeN = new DIYButton("(B)ribe");
		bribeN.addActionListener(this);

		giveN = new DIYButton("(G)ive");
		giveN.addActionListener(this);
		
		neutralPane = new DIYPane(new DIYGridLayout(buttonCols, buttonRows, inset, inset));
		neutralPane.setBounds(bounds);

		neutralPane.add(neutralNameLabel);
		neutralPane.add(new DIYLabel());
		neutralPane.add(new DIYLabel());
		neutralPane.add(threatenN);
		neutralPane.add(bribeN);
		neutralPane.add(giveN);
		neutralPane.add(stealN);
		neutralPane.add(spellN);
		neutralPane.add(useN);
		neutralPane.add(attackN);
		neutralPane.add(leaveN);

		ArrayList<ContainerWidget> list = new ArrayList<ContainerWidget>();
		list.add(friendlyPane);
		list.add(neutralPane);

		cards = new CardLayoutWidget(bounds, list);
		this.add(cards);

		doLayout();
	}
	
	/*-------------------------------------------------------------------------*/
	public void handleKeyCode(int keyCode)
	{
		if (npc.getAttitude() >= 100)
		{
			switch (keyCode)
			{
				case KeyEvent.VK_A: talk(); break;
				case KeyEvent.VK_T: trade(); break;
				case KeyEvent.VK_S: steal(); break;
				case KeyEvent.VK_C: castSpell(); break;
				case KeyEvent.VK_ESCAPE:
				case KeyEvent.VK_L: leave(); break;
				case KeyEvent.VK_K: attack(); break;
				case KeyEvent.VK_U: use(); break;
				case KeyEvent.VK_G: give(); break;
				case KeyEvent.VK_D: guild(); break;
			}
		}
		else
		{
			switch (keyCode)
			{
				case KeyEvent.VK_S: steal(); break;
				case KeyEvent.VK_C: castSpell(); break;
				case KeyEvent.VK_ESCAPE:
				case KeyEvent.VK_L: leave(); break;
				case KeyEvent.VK_K: attack(); break;
				case KeyEvent.VK_U: use(); break;
				case KeyEvent.VK_T: threaten(); break;
				case KeyEvent.VK_B: bribe(); break;
				case KeyEvent.VK_G: give(); break;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setNpc(Npc npc)
	{
		this.npc = npc;
		String foeName = npc.getFoeName();
		String npcDisplayName = Database.getInstance().getFoeTemplate(foeName).getName();
		friendlyNameLabel.setText(npcDisplayName+": Friendly ("+npc.getAttitude()+")");
		neutralNameLabel.setText(npcDisplayName+": Neutral("+npc.getAttitude()+")");
		
		guild.setEnabled(npc.isGuildMaster());

		if (npc.getAttitude() >= 100)
		{
			this.cards.show(friendlyPane);
		}
		else
		{
			this.cards.show(neutralPane);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		if (obj == talk)
		{
			talk();
		}
		else if (obj == spell || obj == spellN)
		{
			castSpell();
		}
		else if (obj == give || obj == giveN)
		{
			give();
		}
		else if (obj == steal || obj == stealN)
		{
			steal();
		}
		else if (obj == trade)
		{
			trade();
		}
		else if (obj == use || obj == useN)
		{
			use();
		}
		else if (obj == leave || obj == leaveN)
		{
			leave();
		}
		else if (obj == attack || obj == attackN)
		{
			attack();
		}
		else if (obj == threatenN)
		{
			threaten();
		}
		else if (obj == guild)
		{
			guild();
		}
		else if (obj == bribeN)
		{
			bribe();
		}
		setNpc(npc);
	}

	public void createCharacter(int createPrice)
	{
		Maze.getInstance().getUi().clearDialog();
		Maze.getInstance().deductPartyGold(createPrice);
		Maze.getInstance().pushState(Maze.State.CREATE_CHARACTER);
	}

	public void transferPlayerCharacterToParty(PlayerCharacter pc, int recruitPrice)
	{
		Maze.getInstance().transferPlayerCharacterToParty(pc, npc);
		Maze.getInstance().deductPartyGold(recruitPrice);
	}

	public void grabAndAttack(Item item, PlayerCharacter pc)
	{
		// regardless of the result, this dialog is done.
		Maze.getInstance().getUi().clearDialog();

		if (item instanceof GoldPieces)
		{
			// generate the amount stolen now
			int amount = GameSys.getInstance().getAmountOfGoldStolen(npc, pc);
			item = new GoldPieces(amount);
		}

		Maze.getInstance().processNpcScriptEvents(
				npc.getScript().grabAndAttack(pc, item));
	}

	public void stealItem(Item item, PlayerCharacter pc)
	{
		// regardless of the result, this dialog is done.
		Maze.getInstance().getUi().clearDialog();
		steal(item, pc);
	}

	public void guild()
	{
		if (guild.isEnabled())
		{
			Maze.getInstance().getUi().showDialog(new GuildDisplayDialogForNpc(npc, this));
		}
	}

	public void threaten()
	{
		if (threatenN.isEnabled())
		{
			int total = GameSys.getInstance().threatenNpc(npc, Maze.getInstance().getParty());
			if (total > 0)
			{
				Maze.getInstance().processNpcScriptEvents(npc.getScript().successfulThreat(total));
			}
			else
			{
				Maze.getInstance().processNpcScriptEvents(npc.getScript().failedThreat(total));
			}
		}
	}
	
	public void bribe()
	{
		if (bribeN.isEnabled())
		{
			lastObj = bribeN;
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	public void attack()
	{
		if (attack.isEnabled())
			Maze.getInstance().processNpcScriptEvents(npc.getScript().attackedByParty());
	}

	public void leave()
	{
		if (npc.getAttitude() >= 100)
		{
			Maze.getInstance().processNpcScriptEvents(npc.getScript().partyLeavesFriendly());
		}
		else
		{
			Maze.getInstance().processNpcScriptEvents(npc.getScript().partyLeavesNeutral());
		}
	}

	public void use()
	{
		if (use.isEnabled())
		{
			lastObj = use;
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	public void trade()
	{
		if (trade.isEnabled())
		{
			lastObj = trade;
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	public void steal()
	{
		if (steal.isEnabled())
		{
			lastObj = steal;
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	public void give()
	{
		if (give.isEnabled())
		{
			lastObj = give;
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	public void castSpell()
	{
		if (spell.isEnabled())
		{
			lastObj = spell;
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	public void talk()
	{
		if (talk.isEnabled())
		{
			lastObj = talk;
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void steal(Item item, PlayerCharacter pc)
	{
		// treat "nothing to steal" as an undetected failure
		if (item == null)
		{
			Maze.getInstance().processNpcScriptEvents(
				npc.getScript().failedUndetectedTheft(pc, item));
			return;
		}

		if (item instanceof GoldPieces)
		{
			// generate the amount stolen now
			int amount = GameSys.getInstance().getAmountOfGoldStolen(npc, pc);
			item = new GoldPieces(amount);
		}

		int result = GameSys.getInstance().stealItem(npc, pc, item);

		if (result == Npc.TheftResult.SUCCESS)
		{
			Maze.getInstance().processNpcScriptEvents(
				npc.getScript().successfulTheft(pc, item));
		}
		else if (result == Npc.TheftResult.FAILED_UNDETECTED)
		{
			Maze.getInstance().processNpcScriptEvents(
				npc.getScript().failedUndetectedTheft(pc, item));
		}
		else if (result == Npc.TheftResult.FAILED_DETECTED)
		{
			Maze.getInstance().processNpcScriptEvents(
				npc.getScript().failedDetectedTheft(pc, item));
		}
		else
		{
			throw new MazeException("invalid theft result: "+result);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void resolveNpcSpell(
		Spell spell, PlayerCharacter caster, int castingLevel)
	{
		SpellEffect spellEffect = null;
		List<SpellEffect> effects = spell.getEffects().getRandom();

		for (SpellEffect s : effects)
		{
			if (s.getUnsavedResult() instanceof CharmSpellResult ||
				s.getUnsavedResult() instanceof MindReadSpellResult ||
				s.getUnsavedResult() instanceof TheftSpellResult)
			{
				spellEffect = s;
				break;
			}
		}

		if (spellEffect == null)
		{
			throw new MazeException("No legal spell effect found for ["+spell.getName()+"]");
		}

//		GameSys.getInstance().castSpellOnNpc(spell, caster, castingLevel, npc);
		Maze.getInstance().appendEvents(new CastSpellOnNpcEvent(spell, caster, castingLevel, npc));
	}

	/*-------------------------------------------------------------------------*/
	private void resolveNpcItem(
		Item item, PlayerCharacter caster)
	{
		Spell spell = item.getInvokedSpell();

		if (spell == null || spell.getTargetType() != MagicSys.SpellTargetType.NPC)
		{
			throw new MazeException("No legal spell effect found for ["+item.getName()+"]");
		}

//		GameSys.getInstance().useItemOnNpc(item, caster, npc);
		Maze.getInstance().appendEvents(new UseItemOnNpcEvent(item, caster, npc));
	}

	/*-------------------------------------------------------------------------*/
	public boolean characterChosen(PlayerCharacter pc, int pcIndex)
	{
		if (lastObj == spell || lastObj == spellN)
		{
			new CastSpell(this, pc);
		}
		else if (lastObj == talk)
		{
			Maze.getInstance().processNpcScriptEvents(npc.getScript().partyWantsToTalk(pc));
		}
		else if (lastObj == use || lastObj == useN)
		{
			new UseItem(pc.getName(), this, pc);
		}
		else if (lastObj == give || lastObj == giveN)
		{
			new GiveItem(this, pc);
		}
		else if (lastObj == trade)
		{
			TradingDialog tradingDialog = new TradingDialog(pc, npc);
			Maze.getInstance().getUi().showDialog(tradingDialog);
		}
		else if (lastObj == bribeN)
		{
			new GetAmount(this, pc, Maze.getInstance().getParty().getGold());
		}
		else if (lastObj == steal || lastObj == stealN)
		{
			if (npc.getAttitude() >= 100)
			{
				// a friendly NPC gives the party a chance to pick an item to steal
				Maze.getInstance().getUi().showDialog(new TheftDialog(pc, npc, this));
			}
			else
			{
				// a neutral NPC means a random item is the target
				Item item = GameSys.getInstance().getRandomItemToSteal(npc);
				steal(item, pc);
			}
		}

		setNpc(npc);

		return true;
	}

	/*-------------------------------------------------------------------------*/
	public void removeFromParty(PlayerCharacter pc, int recruitPrice)
	{
		Maze.getInstance().transferPlayerCharacterToGuild(pc, npc);
		Maze.getInstance().deductPartyGold(recruitPrice);
	}

	/*-------------------------------------------------------------------------*/
	public boolean useItem(
		Item item, PlayerCharacter user, int userIndex, SpellTarget target)
	{
		if (item.getInvokedSpell() != null &&
			item.getInvokedSpell().getTargetType() == MagicSys.SpellTargetType.NPC)
		{
			// what we're really interested in
			resolveNpcItem(item, user);
			return true;
		}

		setNpc(npc);

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
		if (spell.getTargetType() == MagicSys.SpellTargetType.NPC)
		{
			// what we're really interested in
			resolveNpcSpell(spell, caster, castingLevel);
			return true;
		}

		setNpc(npc);

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean giveItem(Item item, PlayerCharacter user, int userIndex)
	{
		Maze.getInstance().processNpcScriptEvents(
			npc.getScript().givenItemByParty(user, item));
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public boolean amountChosen(int amount, PlayerCharacter user, int userIndex)
	{
		// bribery happens
		Maze.getInstance().getParty().incGold(-amount);
		int total = GameSys.getInstance().bribeNpc(npc, user, amount);

		if (total > 0)
		{
			Maze.getInstance().processNpcScriptEvents(npc.getScript().successfulBribe(total));
		}
		else
		{
			Maze.getInstance().processNpcScriptEvents(npc.getScript().failedBribe(total));
		}

		return true;
	}
}
