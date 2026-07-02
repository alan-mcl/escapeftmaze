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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.game.GameState;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.condition.ConditionManager;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SaveGamePanel extends JPanel
{
	private String saveGameName;
	private GameStatePanel gameStatePanel;
	private MazeVariablesPanel mazeVariablesPanel;
	private SaveGamePlayerCharactersPanel saveGamePlayerCharacterPanel;
	private SaveGameItemCachesPanel itemCachesPanel;
	private NpcFactionPanel npcFactionPanel;
	private SaveGameNpcPanel npcPanel;
	private SaveGameJournalsPanel journalsPanel;
	private SaveGameTilesVisitedPanel tilesVisitedPanel;

	/*-------------------------------------------------------------------------*/
	public SaveGamePanel(String saveGameName)
	{
		this.saveGameName = saveGameName;
		this.setLayout(new BorderLayout(5,5));

		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		
		tabs.add("Game State", getGameStatePanel());
		tabs.add("Maze Variables", getMazeVariablesPanel());
		tabs.add("Player Characters", getPlayerCharactersPanel());

		// conditions currently configured at the various condition bearer
		// locations (not all of which are done yet).
		// Is it worth having a central tab?
//		tabs.add("Conditions", getConditionsPanel());

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
		tilesVisitedPanel = new SaveGameTilesVisitedPanel();
		return tilesVisitedPanel;
	}

	private Component getMazeVariablesPanel()
	{
		mazeVariablesPanel = new MazeVariablesPanel();
		return mazeVariablesPanel;
	}

	private Component getNpcPanel()
	{
		npcPanel = new SaveGameNpcPanel();
		return npcPanel;
	}

	private Component getNpcFactionsPanel()
	{
		npcFactionPanel = new NpcFactionPanel(saveGameName);
		return npcFactionPanel;
	}

	private Component getItemCachesPanel()
	{
		itemCachesPanel = new SaveGameItemCachesPanel();
		return itemCachesPanel;
	}

	private Component getPlayerCharactersPanel()
	{
		saveGamePlayerCharacterPanel = new SaveGamePlayerCharactersPanel(saveGameName);
		return saveGamePlayerCharacterPanel;
	}

	private Component getGameStatePanel()
	{
		gameStatePanel = new GameStatePanel(saveGameName);
		return gameStatePanel;
	}

//	private Component getConditionsPanel()
//	{
//		return new JLabel("todo");
//	}

	private Component getJournalsPanel()
	{
		journalsPanel = new SaveGameJournalsPanel();
		return journalsPanel;
	}
	
	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		SwingEditor.instance.runWithoutSaveGameDirty(() ->
		{
			gameStatePanel.initForeignKeys();
			itemCachesPanel.initForeignKeys();
			npcPanel.initForeignKeys();
			tilesVisitedPanel.initForeignKeys();
		});
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		SwingEditor.instance.runWithoutSaveGameDirty(() ->
		{
			try
			{
				refreshImpl();
			}
			catch (Exception e)
			{
				throw new MazeException(e);
			}
		});
	}

	/*-------------------------------------------------------------------------*/
	private void refreshImpl() throws Exception
	{
		// sync with mclachlan.maze.game.Maze.loadGame()

		Loader loader = Database.getInstance().getLoader();

		GameState gs = loader.loadGameState(saveGameName);
		Map<String, PlayerCharacter> playerCharacterCache =
			loader.loadPlayerCharacters(saveGameName);

		ConditionManager.getInstance().loadGame(
			saveGameName, loader, playerCharacterCache);

		gameStatePanel.refresh(gs);
		mazeVariablesPanel.refresh(loader.loadMazeVariablesMap(saveGameName));
		saveGamePlayerCharacterPanel.refresh(playerCharacterCache);
		tilesVisitedPanel.refresh(loader.loadPlayerTilesVisited(saveGameName));
		itemCachesPanel.refresh(loader.loadItemCaches(saveGameName));
		npcFactionPanel.refresh(loader.loadNpcFactions(saveGameName));
		npcPanel.refresh(loader.loadNpcs(saveGameName), playerCharacterCache.keySet());
		journalsPanel.refresh(loader, saveGameName);
	}

	/*-------------------------------------------------------------------------*/
	public void save()
	{
		Saver saver = Database.getInstance().getSaver();

		try
		{
			saveGamePlayerCharacterPanel.commit(null);
			tilesVisitedPanel.commit(null);
			mazeVariablesPanel.commit(null);
			itemCachesPanel.commit(null);
			npcFactionPanel.commit(null);
			npcPanel.commit(null);
			journalsPanel.commit(null);

			saver.saveGameState(saveGameName, gameStatePanel.getGameState());
			saver.savePlayerCharacters(
				saveGameName, saveGamePlayerCharacterPanel.getPlayerCharacters());
			saver.savePlayerTilesVisited(
				saveGameName, tilesVisitedPanel.getPlayerTilesVisited());
			saver.saveMazeVariables(saveGameName, mazeVariablesPanel.getMazeVariables());
			saver.saveItemCaches(saveGameName, itemCachesPanel.getCaches());
			saver.saveNpcFactions(saveGameName, npcFactionPanel.getNpcFactions());
			saver.saveNpcs(saveGameName, npcPanel.getNpcs());
			journalsPanel.save(saveGameName, saver);
			ConditionManager.getInstance().saveGame(saveGameName, saver);
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	public SaveGamePlayerCharactersPanel getSaveGamePlayerCharacterPanel()
	{
		return saveGamePlayerCharacterPanel;
	}

	public NpcFactionPanel getNpcFactionPanel()
	{
		return npcFactionPanel;
	}
}
