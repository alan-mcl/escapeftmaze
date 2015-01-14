
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
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYScrollPane;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.SpellTarget;
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class MovementOptionsWidget extends DIYPane
	implements ActionListener, CastSpellCallback,
	UseItemCallback, FormationCallback,
	ConfirmCallback
{
	public static final int BUTTON_HEIGHT = 20;
	public static final int INSET = 4;

	private DIYButton mainMenu,
		search, locks, rest, formation, hide, saveload,
		settings, map, journal;

	private DIYTextArea textArea;
	private int bufferSize = 30;
	private List<String> messages = new ArrayList<String>();

	/*-------------------------------------------------------------------------*/
	public MovementOptionsWidget(Rectangle bounds)
	{
		super(bounds.x, bounds.y, bounds.width, bounds.height);

		int buttonRows = height/ BUTTON_HEIGHT;

		this.setLayoutManager(new DIYBorderLayout(INSET,INSET));

		DIYPane left = new DIYPane(new DIYGridLayout(1, buttonRows, INSET, INSET));
		DIYPane right = new DIYPane(new DIYGridLayout(1, buttonRows, INSET, INSET));

		mainMenu = new DIYButton(StringUtil.getUiLabel("mow.quit"));
		mainMenu.addActionListener(this);

		search = new DIYButton(StringUtil.getUiLabel("mow.search"));
		search.addActionListener(this);

		locks = new DIYButton(StringUtil.getUiLabel("mow.open"));
		locks.addActionListener(this);

		rest = new DIYButton(StringUtil.getUiLabel("mow.rest"));
		rest.addActionListener(this);

		formation = new DIYButton(StringUtil.getUiLabel("mow.formation"));
		formation.addActionListener(this);

		hide = new DIYButton(StringUtil.getUiLabel("mow.hide"));
		hide.addActionListener(this);

		saveload = new DIYButton(StringUtil.getUiLabel("mow.save.load"));
		saveload.addActionListener(this);

		settings = new DIYButton(StringUtil.getUiLabel("mow.settings"));
		settings.addActionListener(this);

		map = new DIYButton(StringUtil.getUiLabel("mow.map"));
		map.addActionListener(this);

		journal = new DIYButton(StringUtil.getUiLabel("mow.journal"));
		journal.addActionListener(this);

		textArea = new DIYTextArea("");
		textArea.setTransparent(true);
		textArea.setAlignment(DIYToolkit.Align.CENTER);

		left.add(rest);
		left.add(search);
		left.add(locks);
		left.add(hide);
		left.add(formation);

		right.add(map);
		right.add(journal);
		right.add(saveload);
		right.add(settings);
		right.add(mainMenu);

		DIYScrollPane scrollPane = new DIYScrollPane(textArea);

		this.add(left, DIYBorderLayout.Constraint.WEST);
		this.add(right, DIYBorderLayout.Constraint.EAST);
		this.add(scrollPane, DIYBorderLayout.Constraint.CENTER);

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void formationChanged(List<PlayerCharacter> actors, int formation)
	{
		Maze.getInstance().reorderParty(actors, formation);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

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
	}

	/*-------------------------------------------------------------------------*/
	public void addMessage(String message)
	{
		messages.add(0, message);
		if (messages.size() > bufferSize)
		{
			messages.remove(messages.size()-1);
		}

		StringBuilder sb = new StringBuilder();
		for (String s : messages)
		{
			sb.append(s).append("\n");
		}

		textArea.setText(sb.toString());
	}

	/*-------------------------------------------------------------------------*/
	public void formation()
	{
		FormationDialog formationDialog = new FormationDialog(this);
		Maze.getInstance().getUi().showDialog(formationDialog);
	}

	public void showSettingsDialog()
	{
		Maze.getInstance().getUi().showDialog(new SettingsDialog());
	}

	public void stats()
	{
		Maze.getInstance().setState(Maze.State.STATSDISPLAY);
	}

	public void rest()
	{
		if (Maze.getInstance().processPlayerAction(
			TileScript.PlayerAction.REST,
			DiyGuiUserInterface.instance.raycaster.getPlayerFacing()))
		{
			Maze.getInstance().setState(Maze.State.RESTING);
		}
	}

	public void open()
	{
		Maze.getInstance().processPlayerAction(
			TileScript.PlayerAction.LOCKS,
			DiyGuiUserInterface.instance.raycaster.getPlayerFacing());
	}

	public void search()
	{
		Maze.getInstance().processPlayerAction(
			TileScript.PlayerAction.SEARCH,
			DiyGuiUserInterface.instance.raycaster.getPlayerFacing());
	}

	public void mainMenu()
	{
		Maze.getInstance().getUi().showDialog(
			new ConfirmationDialog(
				StringUtil.getUiLabel("mow.confirm.exit"),
				this));
	}

	public void saveOrLoad()
	{
		Maze.getInstance().setState(Maze.State.SAVE_LOAD);
	}

	public void hide()
	{
		Maze.getInstance().partyHides();
	}

	public void showMap()
	{
		Maze.getInstance().getUi().showDialog(
			new MapDisplayDialog());
	}

	/*-------------------------------------------------------------------------*/
	public boolean castSpell(
		Spell spell,
		PlayerCharacter caster,
		int casterIndex,
		int castingLevel,
		int target)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean useItem(
		Item item, PlayerCharacter user, int userIndex, SpellTarget target)
	{
		return !Maze.getInstance().processUseItem(
			item,
			user,
			DiyGuiUserInterface.instance.raycaster.getPlayerFacing());
	}

	/*-------------------------------------------------------------------------*/
	public void confirm()
	{
		// confirming the exit to main
		Maze.getInstance().backToMain();
	}
}
