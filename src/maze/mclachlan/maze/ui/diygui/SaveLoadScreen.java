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
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.diygui.*;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;

/**
 *
 */
public class SaveLoadScreen extends DIYPanel implements ActionListener
{
	DIYListBox list;
	DIYTextField saveGameName;
	DIYButton save, load, exit;
	private List<String> saveGames;

	/*-------------------------------------------------------------------------*/
	public SaveLoadScreen(Rectangle bounds)
	{
		super(bounds);

		setBackgroundImage(Database.getInstance().getImage("screen/main_menu"));

		Rectangle scrollerBounds = new Rectangle(width/4, 90, width/2, height-260);
		Rectangle listBounds = new Rectangle(0, 0, width/2-40, height-200);
		list = new DIYListBox(new ArrayList());
		list.setBounds(listBounds);
		saveGames = new ArrayList<String>(Database.getInstance().getLoader().getSaveGames());
		list.setItems(saveGames);
		DIYPane pane = new DIYPane(listBounds);
		pane.add(list);
		DIYScrollPane scroller = new DIYScrollPane(
			scrollerBounds.x,
			scrollerBounds.y,
			scrollerBounds.width,
			scrollerBounds.height,
			pane);

		list.addActionListener(this);

		Rectangle textFieldLabelBounds = new Rectangle(width/4, height-140, width/2/4, 20);
		Rectangle textFieldBounds = new Rectangle(width/4+100, height-140, width/2/4*3, 20);

		DIYLabel label = new DIYLabel("Save Game Name:", DIYToolkit.Align.LEFT);
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
		if (saveGames.size() > 0)
		{
			list.setSelected(saveGames.get(0));
			saveGameName.setText(saveGames.get(0));
		}

		Rectangle buttonBounds = new Rectangle(0, bounds.height-100, width, 100);
		DIYPane buttonPanel = new DIYPane(buttonBounds);
		buttonPanel.setLayoutManager(new DIYFlowLayout(10, 10, DIYToolkit.Align.CENTER));

		save = new DIYButton("(S)ave");
		save.addActionListener(this);
		load = new DIYButton("Loa(d)");
		load.addActionListener(this);
		exit = new DIYButton("Exit");
		exit.addActionListener(this);

		buttonPanel.add(save);
		buttonPanel.add(load);
		buttonPanel.add(exit);

		this.add(scroller);
		this.add(label);
		this.add(saveGameName);
		this.add(buttonPanel);

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
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == exit)
		{
			exit();
		}
		else if (e.getSource() == save)
		{
			saveGame();
		}
		else if (e.getSource() == load)
		{
			loadGame();
		}
		else if (e.getSource() == list)
		{
			refresh();
		}
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
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
				this.list.processKeyPressed(e);
				this.refresh();
				break;
			case KeyEvent.VK_ESCAPE:
				exit();
				break;
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
