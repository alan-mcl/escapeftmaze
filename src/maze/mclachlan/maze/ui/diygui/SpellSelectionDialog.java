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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.PlayerCharacter;

/**
 * A dialog for selecting spells.
 */
public class SpellSelectionDialog extends GeneralDialog implements ActionListener
{
	private static final int XX = 75;
	private static final int YY = 75;

	private static final Rectangle bounds = new Rectangle(XX, YY,
		DiyGuiUserInterface.SCREEN_WIDTH -XX*2,
		DiyGuiUserInterface.SCREEN_HEIGHT -YY*2);

	private final SpellDisplayWidget sdw;
	private final DIYButton okButton, close;
	private final SpellSelectionCallback spellSelectionCallback;

	/*-------------------------------------------------------------------------*/
	public SpellSelectionDialog(
		PlayerCharacter pc,
		SpellSelectionCallback spellSelectionCallback)
	{
		super(bounds);

		int titlePaneHeight = getTitlePaneHeight();
		int border = getBorder();
		int buttonPaneHeight = getButtonPaneHeight();
		int inset = getInset();

		this.spellSelectionCallback = spellSelectionCallback;

		Rectangle sdwBounds = new Rectangle(
			XX +border +inset,
			YY +border +inset +titlePaneHeight,
			width -border*2 -inset*2,
			height -border*2 -inset*2 -titlePaneHeight -buttonPaneHeight);

		DIYPane title = getTitlePane(StringUtil.getUiLabel("ssd.title", pc.getDisplayName()));

		sdw = new SpellDisplayWidget(pc, sdwBounds);
		this.add(sdw);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(
			x +border,
			y +height -buttonPaneHeight -inset -border,
			width -border*2,
			buttonPaneHeight);
		okButton = new DIYButton(StringUtil.getUiLabel("ssd.cast"));
		okButton.addActionListener(this);
		
		close = getCloseButton();
		close.addActionListener(this);

		buttonPane.add(okButton);

		this.add(title);
		this.add(close);
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
			case KeyEvent.VK_ESCAPE -> canceled();
			case KeyEvent.VK_ENTER -> spellSelected();
			case KeyEvent.VK_1 -> sdw.setSpellLevel(1);
			case KeyEvent.VK_2 -> sdw.setSpellLevel(2);
			case KeyEvent.VK_3 -> sdw.setSpellLevel(3);
			case KeyEvent.VK_4 -> sdw.setSpellLevel(4);
			case KeyEvent.VK_5 -> sdw.setSpellLevel(5);
			case KeyEvent.VK_6 -> sdw.setSpellLevel(6);
			case KeyEvent.VK_7 -> sdw.setSpellLevel(7);
			case KeyEvent.VK_EQUALS, KeyEvent.VK_ADD, KeyEvent.VK_PLUS ->
				// The '+' key
				sdw.incrementPowerLevel();
			case KeyEvent.VK_MINUS, KeyEvent.VK_SUBTRACT ->
				sdw.decrementPowerLevel();
			case KeyEvent.VK_UP, KeyEvent.VK_DOWN ->
				sdw.getSpellList().processKeyPressed(e);
			default ->
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
		else if (event.getSource() == close)
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
