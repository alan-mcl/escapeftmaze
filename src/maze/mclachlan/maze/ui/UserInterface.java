/*
 * Copyright (c) 2012 Alan McLachlan
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

package mclachlan.maze.ui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import mclachlan.crusader.EngineObject;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.maze.audio.Music;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.ChooseCharacterCallback;

public interface UserInterface
{
	public enum CombatOption
	{
		BACKUP,
		CANCEL,
		REPEAT,
		START_ROUND,
		EVADE_FOES,
		SURPRISE_FOES,
		TERMINATE_GAME,
	}

	/*-------------------------------------------------------------------------*/
	void addAnimation(Animation a);

	/*-------------------------------------------------------------------------*/
	void stopAllAnimations();

	/*-------------------------------------------------------------------------*/
	void mouseEventToAnimations(MouseEvent event);

	/*-------------------------------------------------------------------------*/
	void keyEventToAnimations(KeyEvent event);

	/*-------------------------------------------------------------------------*/
	void changeState(Maze.State state);

	/*-------------------------------------------------------------------------*/
	void draw();

	/*-------------------------------------------------------------------------*/
	Component getComponent();

	/*-------------------------------------------------------------------------*/
	void showCombatDisplay();

	/*-------------------------------------------------------------------------*/
	ActorActionIntention getCombatIntention(PlayerCharacter pc);

	/*-------------------------------------------------------------------------*/
	CombatOption getFinalCombatOption();

	/*-------------------------------------------------------------------------*/
	CombatOption getEvasionOption();

	/*-------------------------------------------------------------------------*/
	int getFacing();

	/*-------------------------------------------------------------------------*/
	void chooseACharacter(ChooseCharacterCallback callback);

	/*-------------------------------------------------------------------------*/
	void setDebug(boolean debug);

	/*-------------------------------------------------------------------------*/
	Font getDefaultFont();

	/*-------------------------------------------------------------------------*/
	void refreshCharacterData();

	/*-------------------------------------------------------------------------*/
	void addObject(EngineObject obj);

	/*-------------------------------------------------------------------------*/
	void removeObject(EngineObject obj);

	/*-------------------------------------------------------------------------*/
	void removeObject(String objectName);

	/*-------------------------------------------------------------------------*/
	void addObjectInFrontOfPlayer(
		EngineObject obj, double distance, double arcOffset,
		boolean randomStartingFrame);

	/*-------------------------------------------------------------------------*/
	void signBoard(String message, MazeEvent event);

	/*-------------------------------------------------------------------------*/
	void levelUp(PlayerCharacter playerCharacter);

	/*-------------------------------------------------------------------------*/
	void grantItems(List<Item> items);

	/*-------------------------------------------------------------------------*/
	void showBlockingScreen(String imageResource, int delay, Object mutex);

	/*-------------------------------------------------------------------------*/
	void showBlockingScreen(ContainerWidget dialog, int waitOnClick, Object mutex);

	/*-------------------------------------------------------------------------*/
	void clearBlockingScreen();

	void showCreateCharacterScreen();

	void showCombatScreen();

	void showMagicScreen();

	void showInventoryScreen();

	void showStatsScreen();

	void showModifiersScreen();

	void showPropertiesScreen();

	void showMovementScreen();

	void showRestingScreen();

	void showMainMenu();

	void resetMainMenuState();

	void showSaveLoadScreen();

	void showChestScreen(Chest chest);

	void showNpcScreen(Npc npc);

	void clearCombatEventDisplay();

	/*-------------------------------------------------------------------------*/
	void showDialog(ContainerWidget dialog);

	/*-------------------------------------------------------------------------*/
	void clearDialog();

	void clearMessages();

	/*-------------------------------------------------------------------------*/
	void setParty(PlayerParty party);

	/*-------------------------------------------------------------------------*/
	void setZone(Zone zone, Point pos, int facing);

	/*-------------------------------------------------------------------------*/
	void setPlayerPos(Point pos, int facing);

	/*-------------------------------------------------------------------------*/
	void setTile(Zone zone, Tile t, Point tile);

	/*-------------------------------------------------------------------------*/
	void actorAttacks(UnifiedActor attacker);

	/*-------------------------------------------------------------------------*/
	void actorDies(UnifiedActor victim);

	/*-------------------------------------------------------------------------*/
	void foeFlees(UnifiedActor coward);

	/*-------------------------------------------------------------------------*/
	void backPartyUp(int maxKeys);

	/*-------------------------------------------------------------------------*/
	void setFoes(List<FoeGroup> others);

	/*-------------------------------------------------------------------------*/
	void setAllies(List<FoeGroup> others);

	/*-------------------------------------------------------------------------*/
	void showCombatOptions();

	/*-------------------------------------------------------------------------*/
	void showEvasionOptions();

	/*-------------------------------------------------------------------------*/
	void showFinalCombatOptions();

	/*-------------------------------------------------------------------------*/
	void displayMazeEvent(MazeEvent event, boolean displayEventText);

	/*-------------------------------------------------------------------------*/
	void setPlayerCharacter(int index, PlayerCharacter pc);

	/*-------------------------------------------------------------------------*/
	void characterSelected(int index);

	/*-------------------------------------------------------------------------*/
	void characterSelected(PlayerCharacter pc);

	/*-------------------------------------------------------------------------*/

	/**
	 * @return Any text entered by the player as speech.
	 */
	String getPlayerSpeech();

	/*-------------------------------------------------------------------------*/
	boolean combatDisplayIsVisible();

	/*-------------------------------------------------------------------------*/
	void addMessage(String msg);

	/*-------------------------------------------------------------------------*/
	void refreshResting();

	/*-------------------------------------------------------------------------*/
	Rectangle getPlayerCharacterWidgetBounds(PlayerCharacter playerCharacter);

	/*-------------------------------------------------------------------------*/
	Rectangle getPortraitWidgetBounds(PlayerCharacter pc);

	/*-------------------------------------------------------------------------*/
	void errorDialog(String s);

	/*-------------------------------------------------------------------------*/
	public void waitingDialog(String s);

	/*-------------------------------------------------------------------------*/
	Music getMusic();

	/*-------------------------------------------------------------------------*/
	void setSelectedFoeGroup(int i);

	/*-------------------------------------------------------------------------*/
	boolean supportsAnimation();

	/*-------------------------------------------------------------------------*/
	ActorGroup getSelectedFoeGroup();
}
