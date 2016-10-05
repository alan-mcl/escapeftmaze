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
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYScrollPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.Inventory;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class TheftDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/2;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/4*3;

	private TradingWidget itemWidget;
	private DIYButton steal, grabAndAttack, exit;
	private PlayerCharacter pc;
	private TheftCallback theftCallback;

	/*-------------------------------------------------------------------------*/
	public TheftDialog(
		PlayerCharacter pc,
		Foe npc,
		TheftCallback theftCallback)
	{
		super();

		this.pc = pc;
		this.theftCallback = theftCallback;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		int buttonPaneHeight = 20;
		int inset = 10;
		Rectangle isBounds = new Rectangle(startX+ inset, startY+ inset + buttonPaneHeight,
			DIALOG_WIDTH- inset *2, DIALOG_HEIGHT- buttonPaneHeight *2- inset *4);

		this.setBounds(dialogBounds);
		List<Item> stealableItems = npc.getStealableItems();
		int maxRows = stealableItems == null ? 1 : stealableItems.size() + 1;
		itemWidget = new TradingWidget(
			pc,
			isBounds,
			new Inventory(stealableItems),
			npc.getSellsAt(),
			npc.getMaxPurchasePrice(),
			maxRows,
			this);

		DIYPane titlePane = getTitle(
			StringUtil.getUiLabel(
				"td.title",pc.getName(),npc.getDisplayName()));
//		npc.sortInventory();

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height- buttonPaneHeight - inset, width, buttonPaneHeight);
		exit = new DIYButton(StringUtil.getUiLabel("common.exit"));
		exit.addActionListener(this);

		steal = new DIYButton(StringUtil.getUiLabel("td.steal"));
		steal.addActionListener(this);

		grabAndAttack = new DIYButton(StringUtil.getUiLabel("td.grab.and.attack"));
		grabAndAttack.addActionListener(this);

		buttonPane.add(steal);
		buttonPane.add(grabAndAttack);
		buttonPane.add(exit);

		int tradingPaneY = y+ buttonPaneHeight *2+ inset;
		int tradingPaneWidth = DIALOG_WIDTH- inset *2;
		int tradingPaneHeight = DIALOG_HEIGHT- buttonPaneHeight *5- inset *2;
		DIYScrollPane npcPane = new DIYScrollPane(
			x+ inset, tradingPaneY, tradingPaneWidth, tradingPaneHeight,
			itemWidget);

		setBackground();

		this.add(titlePane);
		this.add(npcPane);
		this.add(buttonPane);
		this.doLayout();
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
			case KeyEvent.VK_ESCAPE:
				exit();
				break;
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_S:
				steal();
				break;
			case KeyEvent.VK_G:
				grabAndAttack();
				break;
			default:
				itemWidget.processKeyPressed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();
		if (obj == steal)
		{
			steal();
		}
		else if (obj == grabAndAttack)
		{
			grabAndAttack();
		}
		else if (obj == exit)
		{
			exit();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void grabAndAttack()
	{
		if (itemWidget.getSelected() != null && itemWidget.getSelected().getItem() != null)
		{
			theftCallback.grabAndAttack(itemWidget.getSelected().getItem(), pc);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void steal()
	{
		if (itemWidget.getSelected() != null && itemWidget.getSelected().getItem() != null)
		{
			theftCallback.stealItem(itemWidget.getSelected().getItem(), pc);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}
}
