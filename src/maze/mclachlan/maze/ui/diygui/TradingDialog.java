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

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYScrollPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class TradingDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/8*7;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/8*7;

	private TradingWidget pcWidget, npcWidget;
	private DIYButton buy, sell, exit;
	private Foe npc;
	private PlayerCharacter pc;
	private DIYLabel goldLabel;

	/*-------------------------------------------------------------------------*/
	public TradingDialog(
		PlayerCharacter pc,
		Foe npc)
	{
		super();

		this.npc = npc;
		this.pc = pc;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		int buttonPaneHeight = 20;
		int inset = 10;
		Rectangle isBounds = new Rectangle(
			startX+ inset,
			startY+ inset + buttonPaneHeight,
			DIALOG_WIDTH/2- inset*2,
			DIALOG_HEIGHT- buttonPaneHeight *2- inset *4);

		this.setBounds(dialogBounds);
		pcWidget = new TradingWidget(
			pc, isBounds, pc.getInventory(), getNpcBuysAt(npc, pc), 0, pc.getInventory().size(), this);
		npcWidget = new TradingWidget(
			pc, isBounds, new Inventory(npc.getTradingInventory()), getNpcSellsAt(npc, pc), 0, -1, this);

		DIYPane titlePane = new DIYPane(new DIYGridLayout(1, 2, 0, 0));
		DIYLabel title = new DIYLabel(StringUtil.getUiLabel("trd.title",pc.getName(), npc.getDisplayName()));
		titlePane.setBounds(x, y + getBorder(), width, getTitlePaneHeight() +20);
		title.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize()+3);
		title.setFont(f);
		titlePane.add(title);
		goldLabel = new DIYLabel();
		goldLabel.setForegroundColour(Color.WHITE);
		titlePane.add(goldLabel);

		refresh(pc, npc);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height- buttonPaneHeight - inset, width, buttonPaneHeight);
		exit = new DIYButton(StringUtil.getUiLabel("common.exit"));
		exit.addActionListener(this);
		buttonPane.add(exit);

		DIYPane pcButtonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		pcButtonPane.setBounds(x, y+height- buttonPaneHeight *2- inset *2, width/2, buttonPaneHeight);

		DIYPane npcButtonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		npcButtonPane.setBounds(x+width/2, y+height- buttonPaneHeight *2- inset *2, width/2, buttonPaneHeight);

		sell = new DIYButton(StringUtil.getUiLabel("trd.sell"));
		sell.addActionListener(this);

		buy = new DIYButton(StringUtil.getUiLabel("trd.buy"));
		buy.addActionListener(this);

		pcButtonPane.add(sell);
		npcButtonPane.add(buy);

		int tradingPaneY = y+ buttonPaneHeight *2+ inset;
		int tradingPaneWidth = (DIALOG_WIDTH- inset *3)/2;
		int tradingPaneHeight = DIALOG_HEIGHT- buttonPaneHeight *5- inset *2;
		DIYScrollPane pcPane = new DIYScrollPane(
			x+ inset, tradingPaneY, tradingPaneWidth, tradingPaneHeight,
			pcWidget);
		DIYScrollPane npcPane = new DIYScrollPane(
			x+ inset *2+tradingPaneWidth, tradingPaneY, tradingPaneWidth, tradingPaneHeight,
			npcWidget);

		// initial state: selling
		npcWidget.setSelected(null);
		buy.setEnabled(false);

		this.add(titlePane);
		this.add(pcPane);
		this.add(pcButtonPane);
		this.add(npcPane);
		this.add(npcButtonPane);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public int getNpcSellsAt(Foe npc, PlayerCharacter pc)
	{
		int barterExpert = pc.getActorGroup().getBestModifier(Stats.Modifier.BARTER_EXPERT);
		int markup = npc.getSellsAt() - 100;

		if (markup > 10 && barterExpert > 0)
		{
			markup -= (barterExpert*10);
		}

		return 100+markup;
	}

	/*-------------------------------------------------------------------------*/
	public int getNpcBuysAt(Foe npc, PlayerCharacter pc)
	{
		int barterExpert = pc.getActorGroup().getBestModifier(Stats.Modifier.BARTER_EXPERT);
		int discount = 100 - npc.getBuysAt();

		if (discount > 10 && barterExpert > 0)
		{
			discount -= barterExpert*10;
		}

		return 100-discount;
	}

	/*-------------------------------------------------------------------------*/
	private void refresh(PlayerCharacter pc, Foe npc)
	{
		goldLabel.setText(StringUtil.getUiLabel(
			"trd.party.gold",Maze.getInstance().getParty().getGold()));
		npc.sortInventory();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getID() != KeyEvent.KEY_PRESSED)
		{
			return;
		}

		switch(e.getKeyCode())
		{
			case KeyEvent.VK_B:
				buy();
				break;
			case KeyEvent.VK_S:
				sell();
				break;
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_ENTER:
				exit();
				break;
			case KeyEvent.VK_LEFT:
				switchToSell();
				break;
			case KeyEvent.VK_RIGHT:
				switchToBuy();
				break;
			default:
			{
				if (pcWidget.getSelected() != null)
				{
					pcWidget.processKeyPressed(e);
				}
				else
				{
					npcWidget.processKeyPressed(e);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();
		if (obj instanceof TradingWidget.TradingItemWidget)
		{
			// ensure that only one grid is selected at a time.

			Widget w = (Widget)obj;
			if (w.getParent() == pcWidget)
			{
				if (canSell())
				{
					switchToSell();
				}
				else
				{
					pcWidget.setSelected(null);
				}
			}
			else if (w.getParent() == npcWidget)
			{
				if (canBuy())
				{
					switchToBuy();
				}
				else
				{
					npcWidget.setSelected(null);
				}
			}

			return true;
		}
		else if (obj == sell)
		{
			sell();

			return true;
		}
		else if (obj == buy)
		{
			buy();

			return true;
		}
		else if (obj == exit)
		{
			exit();

			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void buy()
	{
		if (npcWidget.getSelected() == null)
		{
			return;
		}

		Item item = npcWidget.getSelected().getItem();

		PlayerParty party = Maze.getInstance().getParty();
		int price = GameSys.getInstance().getItemCost(item, getNpcSellsAt(npc, pc), pc);

		// some assertions
		if (item == null)
		{
			throw new MazeException("PC can't buy item "+item);
		}

		if (price > party.getGold())
		{
			Maze.getInstance().appendEvents(npc.getActionScript().partyCantAffordItem(party, item));
			return;
		}

		if (pcWidget.isFull())
		{
			Maze.getInstance().appendEvents(npc.getActionScript().characterInventoryFull(party, item));
			return;
		}

		// transfer the thing
		npc.removeItem(item, true);
		pc.addInventoryItem(item);
		Maze.getInstance().getParty().incGold(
			- GameSys.getInstance().getItemCost(item, getNpcSellsAt(npc, pc), pc));
		pcWidget.refresh(pc.getInventory().getItems());
		npcWidget.refresh(npc.getTradingInventory());
		resetSelection();
		this.refresh(pc, npc);
	}

	/*-------------------------------------------------------------------------*/
	private void sell()
	{
		if (pcWidget.getSelected() == null)
		{
			return;
		}

		Item item = pcWidget.getSelected().getItem();

		// some assertions
		if (item == null)
		{
			throw new MazeException("NPC can't buy item "+item);
		}

		if (!npc.isInterestedInBuyingItem(item, pc))
		{
			Maze.getInstance().appendEvents(npc.getActionScript().notInterestedInBuyingItem(item));
			return;
		}

		if (!npc.isAbleToAffordItem(item, pc))
		{
			Maze.getInstance().appendEvents(npc.getActionScript().cantAffordToBuyItem(item));
			return;
		}

		if (npcWidget.isFull())
		{
			Maze.getInstance().appendEvents(npc.getActionScript().npcInventoryFull(item));
			return;
		}

		// transfer the thing
		pc.removeItem(item, true);
		npc.addInventoryItem(item);
		Maze.getInstance().getParty().incGold(
			GameSys.getInstance().getItemCost(item, getNpcBuysAt(npc, pc), pc));
		pcWidget.refresh(pc.getInventory().getItems());
		npcWidget.refresh(npc.getTradingInventory());
		resetSelection();
		this.refresh(pc, npc);
	}

	/*-------------------------------------------------------------------------*/
	private void resetSelection()
	{
		if (sell.isEnabled())
		{
			if (!pcWidget.resetSelected())
			{
				npcWidget.resetSelected();

				switchToBuy();
			}
		}
		else if (buy.isEnabled())
		{
			if (!npcWidget.resetSelected())
			{
				pcWidget.resetSelected();

				switchToSell();
			}
		}
		else
		{
			throw new MazeException("Something is rather fucked.");
		}
	}

	/*-------------------------------------------------------------------------*/
	private void switchToBuy()
	{
		pcWidget.setSelected(null);
		npcWidget.resetSelected();
		if (canBuy())
		{
			buy.setEnabled(true);
		}
		sell.setEnabled(false);
	}

	/*-------------------------------------------------------------------------*/
	private boolean canBuy()
	{
		return pc.getInventorySize() < PlayerCharacter.MAX_PACK_ITEMS;
	}

	/*-------------------------------------------------------------------------*/
	private void switchToSell()
	{
		npcWidget.setSelected(null);
		pcWidget.resetSelected();
		if (canSell())
		{
			sell.setEnabled(true);
		}
		buy.setEnabled(false);
	}

	/*-------------------------------------------------------------------------*/
	private boolean canSell()
	{
		return pc.getInventorySize() > 0;
	}
}
