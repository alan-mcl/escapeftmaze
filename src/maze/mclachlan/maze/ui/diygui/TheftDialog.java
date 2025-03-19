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

	private final TradingWidget itemWidget;
	private final DIYButton steal, grabAndAttack, close;
	private final PlayerCharacter pc;
	private final TheftCallback theftCallback;

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

		int border = getBorder();
		int buttonPaneHeight = getButtonPaneHeight();
		int inset = getInset();
		int titlePaneHeight = getTitlePaneHeight();

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		this.setBounds(dialogBounds);

		List<Item> stealableItems = npc.getStealableItems();
		int maxRows = stealableItems == null ? 1 : stealableItems.size() + 1;
		itemWidget = new TradingWidget(
			pc,
			new Inventory(stealableItems),
			npc.getSellsAt(),
			npc.getMaxPurchasePrice(),
			maxRows,
			this,
			true);

		DIYPane titlePane = getTitlePane(
			StringUtil.getUiLabel(
				"td.title",pc.getName(),npc.getDisplayName()));

		DIYPane buttonPane = getButtonPane();
		close = getCloseButton();
		close.addActionListener(this);

		steal = new DIYButton(StringUtil.getUiLabel("td.steal"));
		steal.addActionListener(this);

		grabAndAttack = new DIYButton(StringUtil.getUiLabel("td.grab.and.attack"));
		grabAndAttack.addActionListener(this);

		buttonPane.add(steal);
		buttonPane.add(grabAndAttack);

//		int tradingPaneY = y+ buttonPaneHeight *2+ inset;
//		int tradingPaneWidth = DIALOG_WIDTH- inset *2;
//		int tradingPaneHeight = DIALOG_HEIGHT- buttonPaneHeight *5- inset *2;
		DIYScrollPane npcPane = new DIYScrollPane(
			x +border +inset,
			y +border +inset +titlePaneHeight,
			width -border*2 -inset*2,
			height -border*2 -inset*2 -buttonPaneHeight -titlePaneHeight,
			itemWidget);

		this.add(titlePane);
		this.add(npcPane);
		this.add(buttonPane);
		this.add(close);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getID() != KeyEvent.KEY_PRESSED)
		{
			return;
		}

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE -> { e.consume(); exit(); }
			case KeyEvent.VK_ENTER, KeyEvent.VK_S -> { e.consume(); steal(); }
			case KeyEvent.VK_G -> { e.consume(); grabAndAttack(); }
			default -> itemWidget.processKeyPressed(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();
		if (obj == steal)
		{
			steal();
			return true;
		}
		else if (obj == grabAndAttack)
		{
			grabAndAttack();
			return true;
		}
		else if (obj == close)
		{
			exit();
			return true;
		}

		return false;
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
