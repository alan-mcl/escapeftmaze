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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.diygui.*;

/**
 *
 */
public class SaveLoadScreen extends DIYPanel implements ActionListener
{
	private final DIYListBox list;
	private final DIYTextField saveGameName;
	private final DIYButton save, load, exit;
	private final List<String> saveGames;

	/*-------------------------------------------------------------------------*/
	public SaveLoadScreen(Rectangle bounds)
	{
		super(bounds);

		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();

		setBackgroundImage(rp.getImageResource("screen/load_game_back"));

		int border = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int inset = rp.getProperty(RendererProperties.Property.INSET);
		int buttonPaneHeight = rp.getProperty(RendererProperties.Property.BUTTON_PANE_HEIGHT);

		DIYPanel panel = new DIYPanel();
		panel.setStyle(Style.PANEL_MED);
		panel.setBounds(
			new Rectangle(
				width / 4,
				90,
				width / 2,
				height - 150));

		saveGames = new ArrayList<>(Database.getInstance().getLoader().getSaveGames());
		list = new DIYListBox(saveGames);
		list.addActionListener(this);

		DIYScrollPane scroller = new DIYScrollPane(
			panel.x +border +inset,
			panel.y +border +inset,
			panel.width -border*2 -inset*2,
			panel.height -border*2 -inset*3 -80 -buttonPaneHeight,
			list);

		panel.add(scroller);

		Rectangle textFieldLabelBounds = new Rectangle(
			panel.x +border +inset,
			scroller.y +scroller.height +inset,
			panel.width -border*2 -inset*2,
			20);
		Rectangle textFieldBounds = new Rectangle(
			panel.x +border +inset,
			textFieldLabelBounds.y +textFieldLabelBounds.height+inset/2,
			panel.width -border*2 -inset*2,
			40);

		DIYLabel label = new DIYLabel(StringUtil.getUiLabel("sls.save.game.name"), DIYToolkit.Align.LEFT);
		label.setBounds(textFieldLabelBounds);
		saveGameName = new DIYTextField()
		{
			public void processKeyPressed(KeyEvent e)
			{
				super.processKeyPressed(e);
				updateButtonState();
			}
		};
		saveGameName.setBounds(textFieldBounds);
		if (this.saveGames.size() > 0)
		{
			list.setSelected(this.saveGames.get(0));
			saveGameName.setText(this.saveGames.get(0));
		}

		panel.add(label);
		panel.add(saveGameName);

		Rectangle buttonBounds = new Rectangle(
			panel.x +border +inset,
			saveGameName.y +saveGameName.height +inset,
			panel.width -border*2 -inset*2,
			buttonPaneHeight);
		DIYPane buttonPanel = new DIYPane(buttonBounds);
		buttonPanel.setLayoutManager(new DIYFlowLayout(inset, inset, DIYToolkit.Align.CENTER));

		save = new DIYButton(StringUtil.getUiLabel("sls.save"));
		save.addActionListener(this);
		load = new DIYButton(StringUtil.getUiLabel("sls.load"));
		load.addActionListener(this);
		exit = new DIYButton(StringUtil.getUiLabel("common.exit"));
		exit.addActionListener(this);

		buttonPanel.add(save);
		buttonPanel.add(load);
		buttonPanel.add(exit);

		panel.add(buttonPanel);

		this.add(panel);
		doLayout();

		updateButtonState();
	}

	/*-------------------------------------------------------------------------*/
	private void updateButtonState()
	{
		// can't save if there's no current game
		save.setEnabled(Maze.getInstance().isInGame());
		load.setEnabled(saveGames.contains(saveGameName.getText()));
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent e)
	{
		if (e.getSource() == exit)
		{
			exit();
			return true;
		}
		else if (e.getSource() == save)
		{
			saveGame();
			return true;
		}
		else if (e.getSource() == load)
		{
			loadGame();
			return true;
		}
		else if (e.getSource() == list)
		{
			refresh();
			return true;
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void refresh()
	{
		saveGameName.setText((String)list.getSelected());
		updateButtonState();
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		if (Maze.getInstance().isInGame())
		{
			Maze.getInstance().setState(Maze.State.MOVEMENT);
		}
		else
		{
			Maze.getInstance().setState(Maze.State.MAINMENU);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_UP, KeyEvent.VK_DOWN ->
			{
				this.list.processKeyPressed(e);
				this.refresh();
			}
			case KeyEvent.VK_ESCAPE -> exit();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void loadGame()
	{
		String s = saveGameName.getText();
		exit();
		Maze.getInstance().loadGame(s);
	}

	/*-------------------------------------------------------------------------*/
	public void saveGame()
	{
		if (Maze.getInstance().getParty() != null)
		{
			String s = saveGameName.getText();

			if (!saveGames.contains(s))
			{
				saveGames.add(s);
				list.setItems(saveGames);
				list.setSelected(s);
				updateButtonState();
			}

			Maze.getInstance().saveGame(s);
			exit();
		}
	}
}
