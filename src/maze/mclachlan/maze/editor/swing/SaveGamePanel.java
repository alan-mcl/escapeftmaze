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

package mclachlan.maze.editor.swing;

import java.awt.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Saver;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SaveGamePanel extends JPanel
{
	private String saveGameName;
	private GameStatePanel gameStatePanel;
	private SaveGamePlayerCharacterPanel saveGamePlayerCharacterPanel;
	private NpcFactionPanel npcFactionPanel;

	/*-------------------------------------------------------------------------*/
	public SaveGamePanel(String saveGameName)
	{
		this.saveGameName = saveGameName;
		this.setLayout(new BorderLayout(5,5));

		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		
		tabs.add("Game State", getGameStatePanel());
		tabs.add("Player Characters", getPlayerCharactersPanel());
		tabs.add("Conditions", getConditionsPanel());
		tabs.add("Item Caches", getItemCachesPanel());
		tabs.add("NPC Factions", getNpcFactionsPanel());
		tabs.add("NPCs", getNpcPanel());
		tabs.add("Tiles Visited", getPlayerTilesVisitedPanel());
		tabs.add("Journals", getJournalsPanel());

		this.add(tabs);
	}

	/*-------------------------------------------------------------------------*/
	public Component getPlayerTilesVisitedPanel()
	{
		// todo (and remember to add to SwingEditor.addDynamicDataTab)
		return new JLabel("todo");
	}

	private Component getNpcPanel()
	{
		// todo (and remember to add to SwingEditor.addDynamicDataTab)
		return new JLabel("todo");
	}

	private Component getNpcFactionsPanel()
	{
		npcFactionPanel = new NpcFactionPanel(saveGameName);
		return npcFactionPanel;
	}

	private Component getItemCachesPanel()
	{
		// todo (and remember to add to SwingEditor.addDynamicDataTab)
		return new JLabel("todo");
	}

	private Component getPlayerCharactersPanel()
	{
		saveGamePlayerCharacterPanel = new SaveGamePlayerCharacterPanel(saveGameName);
		return saveGamePlayerCharacterPanel;
	}

	private Component getGameStatePanel()
	{
		gameStatePanel = new GameStatePanel(saveGameName);
		return gameStatePanel;
	}

	private Component getConditionsPanel()
	{
		// todo (and remember to add to SwingEditor.addDynamicDataTab)
		return new JLabel("todo");
	}

	private Component getJournalsPanel()
	{
		// todo (and remember to add to SwingEditor.addDynamicDataTab)
		return new JLabel("todo");
	}
	
	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		gameStatePanel.initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		try
		{
			gameStatePanel.refresh(Database.getInstance().getLoader().loadGameState(saveGameName));
			saveGamePlayerCharacterPanel.reload();
			npcFactionPanel.reload();
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void save()
	{
		Saver saver = Database.getInstance().getSaver();

		try
		{
			saver.saveGameState(saveGameName, gameStatePanel.getGameState());
			saver.savePlayerCharacters(saveGameName, saveGamePlayerCharacterPanel.getPlayerCharacters());
			saver.saveNpcFactions(saveGameName, npcFactionPanel.getNpcFactions());
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	public SaveGamePlayerCharacterPanel getSaveGamePlayerCharacterPanel()
	{
		return saveGamePlayerCharacterPanel;
	}

	public NpcFactionPanel getNpcFactionPanel()
	{
		return npcFactionPanel;
	}
}
