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
import mclachlan.maze.game.Maze;

/**
 *
 */
public class FormationDialog extends GeneralDialog implements ActionListener
{
	static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/3;
	static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT/2;

	private FormationWidget formationWidget;
	private DIYButton ok, cancel, moveUp, moveDown;
	private FormationCallback formationCallback;

	/*-------------------------------------------------------------------------*/
	public FormationDialog(FormationCallback formationCallback)
	{
		this.formationCallback = formationCallback;
		String titleText = "Formation";
		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		Rectangle isBounds = new Rectangle(startX+inset, startY+inset+titlePaneHeight,
			DIALOG_WIDTH-inset*2, DIALOG_HEIGHT-titlePaneHeight-buttonPaneHeight-inset*3);

		this.setBounds(dialogBounds);

		formationWidget = new FormationWidget(isBounds, Maze.getInstance().getParty());

		DIYPane titlePane = getTitle(titleText);

		DIYPane buttonGrid = new DIYPane();
		buttonGrid.setBounds(x+inset, y+height-buttonPaneHeight-inset, width-inset*2, buttonPaneHeight);
		buttonGrid.setLayoutManager(new DIYGridLayout(1, 3, 4, 4));

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		ok = new DIYButton("OK");
		ok.addActionListener(this);

		cancel = new DIYButton("Cancel");
		cancel.addActionListener(this);

		buttonPane.add(ok);
		buttonPane.add(cancel);

		moveUp = new DIYButton("Move (U)p");
		moveUp.addActionListener(this);

		moveDown = new DIYButton("Move (D)own");
		moveDown.addActionListener(this);

		buttonGrid.add(moveUp);
		buttonGrid.add(moveDown);
		buttonGrid.add(buttonPane);

		setBackground();

		this.add(titlePane);
		this.add(formationWidget);
		this.add(buttonGrid);
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
				finished();
				break;
			case KeyEvent.VK_U:
				moveUp();
				break;
			case KeyEvent.VK_D:
				moveDown();
				break;
			case KeyEvent.VK_UP:
				formationWidget.moveSelectionUp();
				break;
			case KeyEvent.VK_DOWN:
				formationWidget.moveSelectionDown();
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	private void finished()
	{
		DIYToolkit.getInstance().clearDialog(this);
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
	}

	/*-------------------------------------------------------------------------*/
	private void moveUp()
	{
		formationWidget.moveUp();
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
		else if (obj == ok)
		{
			finished();
			return true;
		}
		else if (obj == cancel)
		{
			canceled();
			return true;
		}

		return false;
	}
}
