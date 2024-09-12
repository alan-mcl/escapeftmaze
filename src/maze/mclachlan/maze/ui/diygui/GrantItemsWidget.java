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
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class GrantItemsWidget extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;
	private static final int MAX_DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/5*4;

	private LootWidget lootWidget;
	private DIYButton okButton;

	/*-------------------------------------------------------------------------*/
	public GrantItemsWidget(
		List<Item> items,
		ActionListener exteriorListener)
	{
		super();

		// todo: scroll pane?
		int itemWidgetHeight = 30;
		int buttonPaneHeight = 20;
		int inset = 10;
		
		int dialogHeight = Math.min(
			items.size()* itemWidgetHeight + inset *3 + buttonPaneHeight,
			MAX_DIALOG_HEIGHT);
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - dialogHeight/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, dialogHeight);
		Rectangle lwBounds = new Rectangle(startX+ inset, startY+ inset,
			DIALOG_WIDTH- inset *2, dialogHeight- buttonPaneHeight - inset *3);

		this.setBounds(dialogBounds);
		lootWidget = new LootWidget(lwBounds, items);
		this.add(lootWidget);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height- buttonPaneHeight - inset, width, buttonPaneHeight);
		okButton = new DIYButton("OK");
		okButton.addActionListener(this);
		if (exteriorListener != null)
		{
			okButton.addActionListener(exteriorListener);
		}
		buttonPane.add(okButton);

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
			case KeyEvent.VK_ENTER:
				exit();
				break;
		}
		
		Item item = (Item)DIYToolkit.getInstance().getCursorContents();

		if (item == null)
		{
			return;
		}

		// on a press on 1-6, add the item to that character
		PlayerCharacter playerCharacter = null;
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_1:
			case KeyEvent.VK_NUMPAD1:
				playerCharacter = Maze.getInstance().getPlayerCharacter(0);
				break;
			case KeyEvent.VK_2:
			case KeyEvent.VK_NUMPAD2:
				playerCharacter = Maze.getInstance().getPlayerCharacter(1);
				break;
			case KeyEvent.VK_3:
			case KeyEvent.VK_NUMPAD3:
				playerCharacter = Maze.getInstance().getPlayerCharacter(2);
				break;
			case KeyEvent.VK_4:
			case KeyEvent.VK_NUMPAD4:
				playerCharacter = Maze.getInstance().getPlayerCharacter(3);
				break;
			case KeyEvent.VK_5:
			case KeyEvent.VK_NUMPAD5:
				playerCharacter = Maze.getInstance().getPlayerCharacter(4);
				break;
			case KeyEvent.VK_6:
			case KeyEvent.VK_NUMPAD6:
				playerCharacter = Maze.getInstance().getPlayerCharacter(5);
				break;
		}

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
		if (e.getSource() instanceof PlayerCharacterWidget &&
			DIYToolkit.getInstance().getCursorContents() != null)
		{
			// drop an item onto a player

			Item item = (Item)DIYToolkit.getInstance().getCursorContents();
			PlayerCharacter pc = ((PlayerCharacterWidget)e.getSource()).getPlayerCharacter();

			if (pc.addInventoryItem(item))
			{
				DIYToolkit.getInstance().clearCursor();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
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
		Maze.getInstance().getUi().clearDialog();
	}
}
