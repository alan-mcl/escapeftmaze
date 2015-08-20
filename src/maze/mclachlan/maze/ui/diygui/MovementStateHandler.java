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

import java.awt.event.KeyEvent;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.UserInterface;

/**
 *
 */
public class MovementStateHandler implements ActionListener, ConfirmCallback, FormationCallback
{
	private Maze maze;
	private final int buttonRows;
	private final int inset;

	private DIYButton mainMenu,
		search, locks, rest, formation, hide, saveload,
		settings, map, journal;

	private UserInterface ui;

	/*-------------------------------------------------------------------------*/
	public MovementStateHandler(Maze maze, int buttonRows, int inset)
	{
		this.maze = maze;
		this.buttonRows = buttonRows;
		this.inset = inset;
		ui = DiyGuiUserInterface.instance;
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getLeftPane()
	{
		DIYPane result = new DIYPane(new DIYGridLayout(1, buttonRows, inset, inset));

		rest = new DIYButton(StringUtil.getUiLabel("poatw.rest"));
		rest.addActionListener(this);

		formation = new DIYButton(StringUtil.getUiLabel("poatw.formation"));
		formation.addActionListener(this);

		hide = new DIYButton(StringUtil.getUiLabel("poatw.hide"));
		hide.addActionListener(this);

		locks = new DIYButton(StringUtil.getUiLabel("poatw.open"));
		locks.addActionListener(this);

		search = new DIYButton(StringUtil.getUiLabel("poatw.search"));
		search.addActionListener(this);

		result.add(rest);
		result.add(search);
		result.add(locks);
		result.add(hide);
		result.add(formation);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getRightPane()
	{
		DIYPane result = new DIYPane(new DIYGridLayout(1, buttonRows, inset, inset));

		map = new DIYButton(StringUtil.getUiLabel("poatw.map"));
		map.addActionListener(this);

		journal = new DIYButton(StringUtil.getUiLabel("poatw.journal"));
		journal.addActionListener(this);

		saveload = new DIYButton(StringUtil.getUiLabel("poatw.save.load"));
		saveload.addActionListener(this);

		mainMenu = new DIYButton(StringUtil.getUiLabel("poatw.quit"));
		mainMenu.addActionListener(this);

		settings = new DIYButton(StringUtil.getUiLabel("poatw.settings"));
		settings.addActionListener(this);

		result.add(map);
		result.add(journal);
		result.add(saveload);
		result.add(settings);
		result.add(mainMenu);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		// movement options
		if (obj == formation)
		{
			formation();
		}
		else if (obj == hide)
		{
			hide();
		}
		else if (obj == saveload)
		{
			saveOrLoad();
		}
		else if (obj == mainMenu)
		{
			mainMenu();
		}
		else if (obj == search)
		{
			search();
		}
		else if (obj == locks)
		{
			open();
		}
		else if (obj == rest)
		{
			rest();
		}
		else if (obj == settings)
		{
			showSettingsDialog();
		}
		else if (obj == map)
		{
			showMap();
		}
		else if (obj == journal)
		{
			showJournal();
		}

	}

	/*-------------------------------------------------------------------------*/
	public void formation()
	{
		if (formation.isVisible())
		{
			FormationDialog formationDialog = new FormationDialog(this);
			maze.getUi().showDialog(formationDialog);
		}
	}

	public void showJournal()
	{
		if (journal.isVisible())
		{
			maze.getUi().showDialog(new JournalDialog());
		}
	}

	public void showSettingsDialog()
	{
		if (settings.isVisible())
		{
			maze.getUi().showDialog(new SettingsDialog());
		}
	}

	public void stats()
	{
		maze.setState(Maze.State.STATSDISPLAY);
	}

	public void rest()
	{
		if (maze.processPlayerAction(
			TileScript.PlayerAction.REST,
			DiyGuiUserInterface.instance.raycaster.getPlayerFacing()))
		{
			maze.setState(Maze.State.RESTING);
		}
	}

	public void open()
	{
		maze.processPlayerAction(
			TileScript.PlayerAction.LOCKS,
			DiyGuiUserInterface.instance.raycaster.getPlayerFacing());
	}

	public void search()
	{
		maze.processPlayerAction(
			TileScript.PlayerAction.SEARCH,
			DiyGuiUserInterface.instance.raycaster.getPlayerFacing());
	}

	public void mainMenu()
	{
		maze.getUi().showDialog(
			new ConfirmationDialog(
				StringUtil.getUiLabel("poatw.confirm.exit"),
				this));
	}

	public void saveOrLoad()
	{
		maze.setState(Maze.State.SAVE_LOAD);
	}

	public void hide()
	{
		maze.partyHides();
	}

	public void showMap()
	{
		maze.getUi().showDialog(
			new MapDisplayDialog());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void confirm()
	{
		// confirming the exit to main
		maze.backToMain();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void formationChanged(List<PlayerCharacter> actors, int formation)
	{
		maze.reorderParty(actors, formation);
	}

	/*-------------------------------------------------------------------------*/
	public void handleKey(int keyCode)
	{
		switch (keyCode)
		{
			case KeyEvent.VK_1: ui.characterSelected(0); Maze.getInstance().setState(Maze.State.INVENTORY); break;
			case KeyEvent.VK_2: ui.characterSelected(1); Maze.getInstance().setState(Maze.State.INVENTORY); break;
			case KeyEvent.VK_3: ui.characterSelected(2); Maze.getInstance().setState(Maze.State.INVENTORY); break;
			case KeyEvent.VK_4: ui.characterSelected(3); Maze.getInstance().setState(Maze.State.INVENTORY); break;
			case KeyEvent.VK_5: ui.characterSelected(4); Maze.getInstance().setState(Maze.State.INVENTORY); break;
			case KeyEvent.VK_6: ui.characterSelected(5); Maze.getInstance().setState(Maze.State.INVENTORY); break;
			case KeyEvent.VK_Q: mainMenu(); break;
			case KeyEvent.VK_S: search(); break;
			case KeyEvent.VK_O: open(); break;
			case KeyEvent.VK_R: rest(); break;
			case KeyEvent.VK_F: formation(); break;
			case KeyEvent.VK_H: hide(); break;
			case KeyEvent.VK_D: saveOrLoad(); break;
			case KeyEvent.VK_G: showSettingsDialog(); break;
			case KeyEvent.VK_J: showJournal(); break;
			case KeyEvent.VK_M:
			case KeyEvent.VK_TAB: showMap(); break;
		}

	}
}
