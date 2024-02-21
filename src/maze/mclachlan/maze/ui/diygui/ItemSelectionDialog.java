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
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class ItemSelectionDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/2;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/3*2;

	private ItemSelectionWidget isWidget;
	private DIYButton okButton, cancel;
	private ItemSelectionCallback itemSelectionCallback;

	/*-------------------------------------------------------------------------*/
	public ItemSelectionDialog(
		String title,
		PlayerCharacter pc,
		ItemSelectionCallback itemSelectionCallback,
		boolean showEquippedItems,
		boolean showPackItems)
	{
		this.itemSelectionCallback = itemSelectionCallback;
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		int buttonPaneHeight = 20;
		int inset = 10;
		Rectangle isBounds = new Rectangle(startX+ inset, startY+ inset + buttonPaneHeight,
			DIALOG_WIDTH- inset *2, DIALOG_HEIGHT- buttonPaneHeight *2- inset *4);

		this.setBounds(dialogBounds);
		isWidget = new ItemSelectionWidget(isBounds, pc, showEquippedItems, showPackItems);

		DIYPane titlePane = getTitle(title);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height- buttonPaneHeight - inset, width, buttonPaneHeight);
		okButton = new DIYButton("OK");
		okButton.addActionListener(this);
		cancel = new DIYButton("Cancel");
		cancel.addActionListener(this);
		buttonPane.add(okButton);
		buttonPane.add(cancel);

		setBackground();

		this.add(titlePane);
		this.add(isWidget);
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
				canceled();
				break;
			case KeyEvent.VK_ENTER:
				itemSelected();
				break;
			case KeyEvent.VK_UP:
				isWidget.moveSelectionUp();
				break;
			case KeyEvent.VK_DOWN:
				isWidget.moveSelectionDown();
				break;
			case KeyEvent.VK_RIGHT:
				isWidget.moveSelectionRight();
				break;
			case KeyEvent.VK_LEFT:
				isWidget.moveSelectionLeft();
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	private void itemSelected()
	{
		Item item = isWidget.getSelectedItem();
		DIYToolkit.getInstance().clearDialog(this);
		itemSelectionCallback.itemSelected(item);
	}

	/*-------------------------------------------------------------------------*/
	private void canceled()
	{
		DIYToolkit.getInstance().clearDialog(this);
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
		{
			itemSelected();
			return true;
		}
		else if (event.getSource() == cancel)
		{
			canceled();
			return true;
		}

		return false;
	}
}
