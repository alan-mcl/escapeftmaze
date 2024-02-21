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
import mclachlan.maze.stat.PlayerCharacter;

/**
 * A dialog for selecting spells.
 */
public class SpellSelectionDialog extends GeneralDialog implements ActionListener
{
	private static int XX = DiyGuiUserInterface.SCREEN_WIDTH/5;
	private static int YY = DiyGuiUserInterface.SCREEN_HEIGHT/5;
	private static Rectangle bounds = new Rectangle(XX, YY,
		DiyGuiUserInterface.SCREEN_WIDTH/5*3, DiyGuiUserInterface.SCREEN_HEIGHT/5*3);

	private static int buttonPaneHeight = 20;
	private static int inset = 10;

	private static Rectangle sdwBounds = new Rectangle(XX+inset, YY+inset,
		DiyGuiUserInterface.SCREEN_WIDTH/5*3-inset*2,
		DiyGuiUserInterface.SCREEN_HEIGHT/5*3-buttonPaneHeight*2-inset*2);

	private SpellDisplayWidget sdw;
	private DIYButton okButton, cancelButton;
	private SpellSelectionCallback spellSelectionCallback;

	/*-------------------------------------------------------------------------*/
	public SpellSelectionDialog(
		PlayerCharacter c,
		SpellSelectionCallback spellSelectionCallback)
	{
		super(bounds);
		this.spellSelectionCallback = spellSelectionCallback;
		sdw = new SpellDisplayWidget(c, sdwBounds);
		this.add(sdw);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height-buttonPaneHeight-inset, width, buttonPaneHeight);
		okButton = new DIYButton("OK");
		okButton.addActionListener(this);
		
		cancelButton = new DIYButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);

		setBackground();

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
				spellSelected();
				break;
			case KeyEvent.VK_1:
				sdw.setSpellLevel(1);
				break;
			case KeyEvent.VK_2:
				sdw.setSpellLevel(2);
				break;
			case KeyEvent.VK_3:
				sdw.setSpellLevel(3);
				break;
			case KeyEvent.VK_4:
				sdw.setSpellLevel(4);
				break;
			case KeyEvent.VK_5:
				sdw.setSpellLevel(5);
				break;
			case KeyEvent.VK_6:
				sdw.setSpellLevel(6);
				break;
			case KeyEvent.VK_7:
				sdw.setSpellLevel(7);
				break;
			case KeyEvent.VK_EQUALS:
			case KeyEvent.VK_ADD:
			case KeyEvent.VK_PLUS:
				// The '+' key
				sdw.incrementPowerLevel();
				break;
			case KeyEvent.VK_MINUS:
			case KeyEvent.VK_SUBTRACT:
				sdw.decrementPowerLevel();
				break;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
				sdw.getSpellList().processKeyPressed(e);
				break;
			default:
			{
				sdw.getQuickName().processKeyPressed(e);
				sdw.filterSpells();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
		{
			spellSelected();
			return true;
		}
		else if (event.getSource() == cancelButton)
		{
			canceled();
			return true;
		}

		return false;
	}

	private void canceled()
	{
		DIYToolkit.getInstance().clearDialog(this);
	}

	private void spellSelected()
	{
		DIYToolkit.getInstance().clearDialog(this);
		spellSelectionCallback.spellSelected(sdw.getSpellSelected());
	}

	public int getCastingLevel()
	{
		return sdw.getCastingLevel();
	}
}
