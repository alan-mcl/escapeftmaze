
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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.*;
import mclachlan.diygui.*;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.stat.combat.Combat;

import static mclachlan.maze.game.Maze.State.*;

/**
 * Widget for the bottom center that:
 * <ul>
 *    <li>displays a the text of events as things happen</li>
 *    <li>displays buttons for party options (i.e. options not tied to a specific
 *    character) to the left and right of it.</li>
 *    <li>changes those displayed buttons based on game state</li>
 *    <li>works for all maze-window centric game states (i.e. movement, combat,
 *    NPC, chest, door, etc)</li>
 * </ul>
 */
public class PartyOptionsAndTextWidget extends DIYPanel
	implements MessageDestination, ActionListener, ConfirmCallback
{
	private static final int TOTAL_LOG_MESSAGES = 200;
	private static final int MAIN_SCREEN_MESSAGES = 5;
	private final Maze maze;

	// card layouts for the left and right
	private final CardLayoutWidget leftCards;
	private final CardLayoutWidget rightCards;

	// central header text, common to all game states
	private final DIYLabel header;

	// the central text area, common to all game states
	private final DIYTextArea textArea;
	private final List<String> messages = new ArrayList<>();

	// movement options
	private final MovementStateHandler movementHandler;

	// encounter actors options
	private final EncounterActorsStateHandler encounterActorsStateHandler;

	// combat party options
	private final CombatStateHandler combatStateHandler;

	// encounter chest options
	private final EncounterChestStateHandler encounterChestStateHandler;

	// encounter portal options
	private final EncounterPortalStateHandler encounterPortalStateHandler;

	// bottom button pane
	private final DIYButton viewLog, quit, saveload, settings, journal, map;

	/*-------------------------------------------------------------------------*/
	public PartyOptionsAndTextWidget(Rectangle bounds)
	{
		super(bounds);
		this.setStyle(Style.PANEL_HEAVY);

		maze = Maze.getInstance();

		int panelBorder = 33;
		int internalInset = 5;

		int buttonHeight = 40;

		DIYPane mainPane = new DIYPane(/*new DIYBorderLayout(internalInset, internalInset)*/);
		mainPane.setBounds(
			x + panelBorder,
			y + panelBorder,
			width - panelBorder * 2,
			height - panelBorder); // extend over the border so that the admin buttons are clickable

		this.add(mainPane);

		int headerHeight = 15;
		int columnWidth = (mainPane.width - internalInset * 5) / 4;
		int contentTop = mainPane.y + headerHeight + internalInset;
		int columnHeight = mainPane.height - panelBorder - internalInset * 2 - headerHeight;
		int column1x = mainPane.x + internalInset;
		int column2x = column1x + columnWidth + internalInset;
		int column3x = column2x + columnWidth + internalInset;
		int column4x = column3x + columnWidth + internalInset;

		int buttonRows = columnHeight / buttonHeight;

		// init the state handlers
		movementHandler = new MovementStateHandler(maze, buttonRows, internalInset);
		encounterActorsStateHandler = new EncounterActorsStateHandler(maze, buttonRows, internalInset, this);
		combatStateHandler = new CombatStateHandler(maze, buttonRows, internalInset, this);
		encounterChestStateHandler = new EncounterChestStateHandler(maze, buttonRows, internalInset, this);
		encounterPortalStateHandler = new EncounterPortalStateHandler(maze, buttonRows, internalInset, this);

		// init the header
		header = new DIYLabel("", DIYToolkit.Align.CENTER);
		header.setForegroundColour(Color.WHITE);
		header.setBounds(
			column1x,
			mainPane.y,
			columnWidth * 4 + internalInset * 3,
			headerHeight);

		// pack card layout widgets for the right and left panes
		Map<Object, ContainerWidget> leftCardsWidgets = getLeftCardsWidgets();
		leftCards = new CardLayoutWidget(new Rectangle(), leftCardsWidgets);
		leftCards.setBounds(
			column1x,
			contentTop,
			columnWidth,
			columnHeight);

		Map<Object, ContainerWidget> rightCardsWidgets = getRightCardsWidgets();
		rightCards = new CardLayoutWidget(new Rectangle(), rightCardsWidgets);
		rightCards.setBounds(
			column4x,
			contentTop,
			columnWidth,
			columnHeight);

		// init text area
		textArea = new DIYTextArea("");
		textArea.setTransparent(true);
		textArea.setAlignment(DIYToolkit.Align.CENTER);
		textArea.setBounds(
			column2x,
			contentTop,
			columnWidth * 2 + internalInset,
			columnHeight - 20);

		// admin buttons
		int nrAdminButtons = 6;
		int buttonSize = 45;
		int startX = bounds.x + panelBorder + 30;
		int buttonY = contentTop + columnHeight;
		int columnInc = (bounds.width - panelBorder * 2 - 60) / nrAdminButtons;
		int buttonX = startX + columnInc / 2 - buttonSize / 2;

		journal = new DIYButton(null);
		journal.setTooltip(StringUtil.getUiLabel("poatw.journal"));
		journal.setImage("ui/mf/icons/journal");
		journal.addActionListener(this);
		journal.setBounds(buttonX,
			buttonY, buttonSize, buttonSize);

		map = new DIYButton(null);
		map.setTooltip(StringUtil.getUiLabel("poatw.map"));
		map.setImage("ui/mf/icons/map");
		map.addActionListener(this);
		map.setBounds(buttonX + columnInc,
			buttonY, buttonSize, buttonSize);

		viewLog = new DIYButton(null);
		viewLog.setTooltip(StringUtil.getUiLabel("poatw.view.log"));
		viewLog.setImage("ui/mf/icons/log");
		viewLog.addActionListener(this);
		viewLog.setBounds(buttonX + columnInc * 2,
			buttonY, buttonSize, buttonSize);

		saveload = new DIYButton(null);
		saveload.setTooltip(StringUtil.getUiLabel("poatw.save.load"));
		saveload.setImage("ui/mf/icons/save");
		saveload.addActionListener(this);
		saveload.setBounds(buttonX + columnInc * 3,
			buttonY, buttonSize, buttonSize);

		settings = new DIYButton(null);
		settings.setTooltip(StringUtil.getUiLabel("poatw.settings"));
		settings.setImage("ui/mf/icons/settings");
		settings.addActionListener(this);
		settings.setBounds(buttonX + columnInc * 4,
			buttonY, buttonSize, buttonSize);

		quit = new DIYButton(null);
		quit.setTooltip(StringUtil.getUiLabel("poatw.quit"));
		quit.setImage("ui/mf/icons/close");
		quit.addActionListener(this);
		quit.setBounds(buttonX + columnInc * 5,
			buttonY, buttonSize, buttonSize);

		// add widgets
		mainPane.add(header);
		mainPane.add(leftCards);
		mainPane.add(rightCards);
		mainPane.add(textArea);

		mainPane.add(journal);
		mainPane.add(map);
		mainPane.add(viewLog);
		mainPane.add(saveload);
		mainPane.add(settings);
		mainPane.add(quit);

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	private Map<Object, ContainerWidget> getLeftCardsWidgets()
	{
		Map<Object, ContainerWidget> result = new HashMap<>();

		result.put(MOVEMENT, movementHandler.getLeftPane());
		result.put(ENCOUNTER_ACTORS, encounterActorsStateHandler.getLeftPane());
		result.put(COMBAT, combatStateHandler.getLeftPane());
		result.put(ENCOUNTER_CHEST, encounterChestStateHandler.getLeftPane());
		result.put(ENCOUNTER_PORTAL, encounterPortalStateHandler.getLeftPane());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Map<Object, ContainerWidget> getRightCardsWidgets()
	{
		Map<Object, ContainerWidget> result = new HashMap<>();

		result.put(MOVEMENT, movementHandler.getRightPane());
		result.put(ENCOUNTER_ACTORS, encounterActorsStateHandler.getRightPane());
		result.put(COMBAT, combatStateHandler.getRightPane());
		result.put(ENCOUNTER_CHEST, encounterChestStateHandler.getRightPane());
		result.put(ENCOUNTER_PORTAL, encounterPortalStateHandler.getRightPane());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void addMessage(String message)
	{
		messages.add(0, message);
		if (messages.size() > TOTAL_LOG_MESSAGES)
		{
			messages.remove(messages.size() - 1);
		}

		textArea.setText(String.join("\n", messages.subList(0, Math.min(messages.size(), MAIN_SCREEN_MESSAGES))));
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setHeader(String text)
	{
		header.setText(text);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Clears both the message history and the displayed text
	 */
	@Override
	public void clearMessages()
	{
		messages.clear();
		header.setText("");
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Clears only the displayed text.
	 */
	public void clearDisplayedMessages()
	{
		textArea.setText("");
		addMessage(""); // add a blank line to the log
	}

	/*-------------------------------------------------------------------------*/
	public void disableInput()
	{
		combatStateHandler.setEnabled(false);
		encounterActorsStateHandler.setEnabled(false);

		journal.setEnabled(false);
		map.setEnabled(false);
		viewLog.setEnabled(false);
		saveload.setEnabled(false);
		settings.setEnabled(false);
		quit.setEnabled(false);
	}

	/*-------------------------------------------------------------------------*/
	public void enableInput()
	{
		combatStateHandler.setEnabled(true);
		encounterActorsStateHandler.setEnabled(true);

		// can't just set everything enabled
		setGameState(Maze.getInstance().getState(), Maze.getInstance());
	}

	/*-------------------------------------------------------------------------*/
	public void setGameState(Maze.State state, Maze maze)
	{
		switch (state)
		{
			case MOVEMENT ->
			{
				String zoneName = maze.getCurrentZone().getName();
				header.setText(StringUtil.getUiLabel("poatw.exploring", zoneName));
				leftCards.show(MOVEMENT);
				rightCards.show(MOVEMENT);

				journal.setEnabled(true);
				map.setEnabled(true);
				viewLog.setEnabled(true);
				saveload.setEnabled(true);
				settings.setEnabled(true);
				quit.setEnabled(true);
			}
			case ENCOUNTER_ACTORS ->
			{
				leftCards.show(ENCOUNTER_ACTORS);
				rightCards.show(ENCOUNTER_ACTORS);

				journal.setEnabled(true);
				map.setEnabled(true);
				viewLog.setEnabled(true);
				saveload.setEnabled(false);
				settings.setEnabled(true);
				quit.setEnabled(true);
			}
			case COMBAT ->
			{
				leftCards.show(COMBAT);
				rightCards.show(COMBAT);

				journal.setEnabled(true);
				map.setEnabled(true);
				viewLog.setEnabled(true);
				saveload.setEnabled(false);
				settings.setEnabled(true);
				quit.setEnabled(true);
			}
			case ENCOUNTER_CHEST ->
			{
				leftCards.show(ENCOUNTER_CHEST);
				rightCards.show(ENCOUNTER_CHEST);

				journal.setEnabled(true);
				map.setEnabled(true);
				viewLog.setEnabled(true);
				saveload.setEnabled(false);
				settings.setEnabled(true);
				quit.setEnabled(true);
			}
			case ENCOUNTER_PORTAL ->
			{
				leftCards.show(ENCOUNTER_PORTAL);
				rightCards.show(ENCOUNTER_PORTAL);

				journal.setEnabled(true);
				map.setEnabled(true);
				viewLog.setEnabled(true);
				saveload.setEnabled(false);
				settings.setEnabled(true);
				quit.setEnabled(true);
			}
			default ->
			{
				journal.setEnabled(true);
				map.setEnabled(true);
				viewLog.setEnabled(true);
				saveload.setEnabled(true);
				settings.setEnabled(true);
				quit.setEnabled(true);
			}
			// do nothing
		}
	}

	/*-------------------------------------------------------------------------*/
	public void handleKey(int keyCode)
	{
		switch (keyCode)
		{
			case KeyEvent.VK_Q ->
			{
				quit();
				return;
			}
			case KeyEvent.VK_D ->
			{
				saveOrLoad();
				return;
			}
			case KeyEvent.VK_G ->
			{
				showSettingsDialog();
				return;
			}
			case KeyEvent.VK_J ->
			{
				showJournal();
				return;
			}
			case KeyEvent.VK_M, KeyEvent.VK_TAB ->
			{
				showMap();
				return;
			}
			case KeyEvent.VK_V ->
			{
				viewLog();
				return;
			}
		}

		switch (maze.getState())
		{
			case MOVEMENT:
				movementHandler.handleKey(keyCode);
				break;
			case ENCOUNTER_ACTORS:
				encounterActorsStateHandler.handleKey(keyCode);
				break;
			case COMBAT:
				combatStateHandler.handleKey(keyCode);
				break;
			case ENCOUNTER_CHEST:
				encounterChestStateHandler.handleKey(keyCode);
				break;
			case ENCOUNTER_PORTAL:
				encounterPortalStateHandler.handleKey(keyCode);
				break;

			// no impact on these states
			case MAINMENU:
			case CREATE_CHARACTER:
			case SAVE_LOAD:
			case MODIFIERSDISPLAY:
			case STATSDISPLAY:
			case PROPERTIESDISPLAY:
			case INVENTORY:
			case LEVELLING_UP:
			case MAGIC:
			case RESTING:
			case FINISHED:
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		Maze maze = Maze.getInstance();
		switch (maze.getState())
		{
			case MOVEMENT:
				break;
			case ENCOUNTER_ACTORS:
				setActorEncounter(maze.getCurrentActorEncounter());
				break;
			case COMBAT:
				setCurrentCombat(maze.getCurrentCombat());
				break;
			case ENCOUNTER_CHEST:
				setChest(maze.getCurrentChest());
				break;
			default:
				// do nothing
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setChest(Chest chest)
	{
		encounterChestStateHandler.setChest(chest);
	}

	/*-------------------------------------------------------------------------*/
	public void setActorEncounter(ActorEncounter actorEncounter)
	{
		encounterActorsStateHandler.setActorEncounter(actorEncounter);
	}

	/*-------------------------------------------------------------------------*/
	public void setCurrentCombat(Combat currentCombat)
	{
		combatStateHandler.setCurrentCombat(currentCombat);
	}

	/*-------------------------------------------------------------------------*/
	public void setPortal(Portal portal)
	{
		encounterPortalStateHandler.setPortal(portal);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();
		if (obj == viewLog)
		{
			viewLog();
			return true;
		}
		else if (obj == saveload)
		{
			saveOrLoad();
			return true;
		}
		else if (obj == quit)
		{
			quit();
			return true;
		}
		else if (obj == settings)
		{
			showSettingsDialog();
			return true;
		}
		else if (obj == journal)
		{
			showJournal();
			return true;
		}
		else if (obj == map)
		{
			showMap();
			return true;
		}

		return false;
	}

	private void viewLog()
	{
		ArrayList<String> msgs = new ArrayList<>(messages);
		Collections.reverse(msgs);
		String text = String.join("\n", msgs);

		int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH / 2;
		int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_WIDTH / 2;
		int startX = DiyGuiUserInterface.SCREEN_WIDTH / 2 - DIALOG_WIDTH / 2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT / 2 - DIALOG_HEIGHT / 2;

		Rectangle bounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		OkDialogWidget dialog = new OkDialogWidget(bounds, null, text);

		DiyGuiUserInterface.instance.showDialog(dialog);
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

	public void quit()
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

	public void showMap()
	{
		maze.getUi().showDialog(new MapDisplayDialog());
	}

	@Override
	public void confirm()
	{
		// confirming the exit to main
		maze.backToMain();
	}
}
