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
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class FormationDialog extends GeneralDialog implements ActionListener
{
	static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH / 3;
	static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT / 2;

	private final FormationWidget formationWidget;
	private final DIYButton close, moveUp, moveDown;
	private final FormationCallback formationCallback;

	/*-------------------------------------------------------------------------*/
	public FormationDialog(FormationCallback formationCallback)
	{
		this.formationCallback = formationCallback;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH / 2 - DIALOG_WIDTH / 2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT / 2 - DIALOG_HEIGHT / 2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		this.setBounds(dialogBounds);

		int buttonPaneHeight = getButtonPaneHeight() * 2;

		Rectangle isBounds = new Rectangle(
			startX + getBorder() + getInset(),
			startY + getBorder() + getInset() + getTitlePaneHeight(),
			width - getInset() * 2 - getBorder() * 2,
			height - getTitlePaneHeight() - getBorder() * 2 - buttonPaneHeight - getInset() * 3);

		formationWidget = new FormationWidget(isBounds, Maze.getInstance().getParty());

		String titleText = StringUtil.getUiLabel("fd.title");
		DIYPane titlePane = getTitlePane(titleText);

		DIYPane buttonGrid = new DIYPane();
		buttonGrid.setBounds(
			x + getBorder() + getInset(),
			y + height - buttonPaneHeight - getBorder() - getInset(),
			width - getInset() * 2 - getBorder() * 2,
			buttonPaneHeight);
		buttonGrid.setLayoutManager(new DIYGridLayout(1, 2, getInset(), getInset()));

		close = getCloseButton();
		close.addActionListener(this);

		moveUp = new DIYButton(StringUtil.getUiLabel("fd.move.up"));
		moveUp.addActionListener(this);

		moveDown = new DIYButton(StringUtil.getUiLabel("fd.move.down"));
		moveDown.addActionListener(this);

		buttonGrid.add(moveUp);
		buttonGrid.add(moveDown);

		this.add(titlePane);
		this.add(formationWidget);
		this.add(buttonGrid);
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
			case KeyEvent.VK_ESCAPE -> canceled();
			case KeyEvent.VK_ENTER -> finished();
			case KeyEvent.VK_U -> moveUp();
			case KeyEvent.VK_D -> moveDown();
			case KeyEvent.VK_UP -> formationWidget.moveSelectionUp();
			case KeyEvent.VK_DOWN -> formationWidget.moveSelectionDown();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void finished()
	{
		DIYToolkit.getInstance().clearDialog(this);
		formationChanged();
	}

	private void formationChanged()
	{
		formationCallback.formationChanged(
			formationWidget.getActors(),
			formationWidget.getFormation());
	}

	/*-------------------------------------------------------------------------*/
	private void canceled()
	{
		DIYToolkit.getInstance().clearDialog(this);
	}

	/*-------------------------------------------------------------------------*/
	private void moveDown()
	{
		formationWidget.moveDown();
		formationChanged();
	}

	/*-------------------------------------------------------------------------*/
	private void moveUp()
	{
		formationWidget.moveUp();
		formationChanged();
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		if (obj == moveUp)
		{
			moveUp();
			return true;
		}
		else if (obj == moveDown)
		{
			moveDown();
			return true;
		}
		else if (obj == close)
		{
			finished();
			return true;
		}

		return false;
	}
}
