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
import java.awt.event.MouseEvent;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;

import static mclachlan.diygui.toolkit.RendererProperties.Property.ITEM_WIDGET_SIZE;

/**
 *
 */
public class GrantItemsWidget extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/2;
	private static final int MAX_DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT -DiyGuiUserInterface.SCREEN_EDGE_INSET*2;

	private final LootWidget lootWidget;
	private final DIYButton close;

	/*-------------------------------------------------------------------------*/
	public GrantItemsWidget(
		List<Item> items,
		ActionListener exteriorListener)
	{
		super();

		// todo: scroll pane?

		int itemWidgetHeight = DIYToolkit.getInstance().getRendererProperties().getProperty(ITEM_WIDGET_SIZE);
		int buttonPaneHeight = getButtonPaneHeight();
		int inset = getInset();
		int border = getBorder();
		int titlePaneHeight = getTitlePaneHeight();

		int dialogHeight = Math.min(
			titlePaneHeight +items.size()*(itemWidgetHeight+inset/2) + inset*3 +border*2,
			MAX_DIALOG_HEIGHT);
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - dialogHeight/2;

		Rectangle dialogBounds = new Rectangle(
			startX,
			startY,
			DIALOG_WIDTH,
			dialogHeight);

		Rectangle lwBounds = new Rectangle(
			startX +border +inset,
			startY +border +inset +titlePaneHeight,
			DIALOG_WIDTH -border*2 -inset*2,
			dialogHeight -border*2 -inset*3 -titlePaneHeight);

		this.setBounds(dialogBounds);
		lootWidget = new LootWidget(lwBounds, items);
		this.add(lootWidget);

		close = getCloseButton();
		close.addActionListener(this);
		if (exteriorListener != null)
		{
			close.addActionListener(exteriorListener);
		}

		this.add(getTitlePane(StringUtil.getUiLabel("giw.title")));
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
			case KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER -> exit();
		}
		
		Item item = (Item)DIYToolkit.getInstance().getCursorContents();

		if (item == null)
		{
			return;
		}

		// on a press on 1-6, add the item to that character
		PlayerCharacter playerCharacter = switch (e.getKeyCode())
			{
				case KeyEvent.VK_1, KeyEvent.VK_NUMPAD1 ->
					Maze.getInstance().getPlayerCharacter(0);
				case KeyEvent.VK_2, KeyEvent.VK_NUMPAD2 ->
					Maze.getInstance().getPlayerCharacter(1);
				case KeyEvent.VK_3, KeyEvent.VK_NUMPAD3 ->
					Maze.getInstance().getPlayerCharacter(2);
				case KeyEvent.VK_4, KeyEvent.VK_NUMPAD4 ->
					Maze.getInstance().getPlayerCharacter(3);
				case KeyEvent.VK_5, KeyEvent.VK_NUMPAD5 ->
					Maze.getInstance().getPlayerCharacter(4);
				case KeyEvent.VK_6, KeyEvent.VK_NUMPAD6 ->
					Maze.getInstance().getPlayerCharacter(5);
				default -> null;
			};

		if (playerCharacter != null)
		{
			if (playerCharacter.addInventoryItem(item))
			{
				DIYToolkit.getInstance().clearCursor();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void processMouseClicked(MouseEvent e)
	{
		if (DIYToolkit.getInstance().getCursorContents() != null)
		{
			// drop an item onto a player

			for (PlayerCharacter pc : Maze.getInstance().getParty().getPlayerCharacters())
			{
				if (Maze.getInstance().getUi().getPlayerCharacterWidgetBounds(pc).contains(e.getPoint()))
				{
					Item item = (Item)DIYToolkit.getInstance().getCursorContents();

					if (pc.addInventoryItem(item))
					{
						DIYToolkit.getInstance().clearCursor();
					}

					break;
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == close)
		{
			exit();
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		List<Item> remainingItems = lootWidget.getRemainingItems();
		if (remainingItems != null && remainingItems.size() > 0)
		{
			// drop these items on the ground
			Maze.getInstance().dropItemsOnCurrentTile(remainingItems);
		}
		Maze.getInstance().getUi().refreshCharacterData();
		Maze.getInstance().getUi().clearDialog();
	}
}
