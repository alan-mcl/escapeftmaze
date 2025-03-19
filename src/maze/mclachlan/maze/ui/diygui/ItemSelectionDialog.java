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

import java.awt.Dimension;
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
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/5*4;

	private final ItemSelectionWidget itemSelectionWidget;
	private final DIYButton okButton, close;
	private final ItemSelectionCallback itemSelectionCallback;

	/*-------------------------------------------------------------------------*/
	public ItemSelectionDialog(
		String title,
		String okButtonText,
		PlayerCharacter pc,
		ItemSelectionCallback itemSelectionCallback,
		boolean showEquippedItems,
		boolean showPackItems)
	{
		int border = getBorder();
		int titlePaneHeight = getTitlePaneHeight();
		int inset = getInset();
		int buttonPaneHeight = getButtonPaneHeight();

		itemSelectionWidget = new ItemSelectionWidget(pc, showEquippedItems, showPackItems);

		Dimension isDim = itemSelectionWidget.getPreferredSize();

		int DIALOG_HEIGHT = border*2 +titlePaneHeight +buttonPaneHeight
			+inset*3 +isDim.height;

		this.itemSelectionCallback = itemSelectionCallback;
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT /2;


		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);

		itemSelectionWidget.setBounds(
			startX +border +inset,
			startY +border +inset +titlePaneHeight,
			DIALOG_WIDTH -inset*2 -border*2,
			isDim.height);

		this.setBounds(dialogBounds);

		DIYPane titlePane = getTitlePane(title);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(
			x,
			y +height -buttonPaneHeight -border,
			width,
			buttonPaneHeight);

		okButton = new DIYButton(okButtonText);
		okButton.addActionListener(this);

		close = getCloseButton();
		close.addActionListener(this);
		buttonPane.add(okButton);
		this.add(close);

		this.add(titlePane);
		this.add(itemSelectionWidget);
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

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE -> { e.consume(); canceled(); }
			case KeyEvent.VK_ENTER -> { e.consume(); itemSelected(); }
			case KeyEvent.VK_UP -> { e.consume(); itemSelectionWidget.moveSelectionUp(); }
			case KeyEvent.VK_DOWN -> { e.consume(); itemSelectionWidget.moveSelectionDown(); }
			case KeyEvent.VK_RIGHT -> { e.consume(); itemSelectionWidget.moveSelectionRight(); }
			case KeyEvent.VK_LEFT -> { e.consume(); itemSelectionWidget.moveSelectionLeft(); }
		}
	}

	/*-------------------------------------------------------------------------*/
	private void itemSelected()
	{
		Item item = itemSelectionWidget.getSelectedItem();
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
		else if (event.getSource() == close)
		{
			canceled();
			return true;
		}

		return false;
	}
}
