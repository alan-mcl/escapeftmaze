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
import mclachlan.crusader.Texture;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.maze.audio.Music;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.ChooseCharacterCallback;
import mclachlan.maze.ui.diygui.MessageDestination;
import mclachlan.maze.ui.diygui.TextDialogWidget;

public interface UserInterface
{
	void initConfig();

	/*-------------------------------------------------------------------------*/
	void addAnimation(Animation a);

	/*-------------------------------------------------------------------------*/
	void stopAllAnimations();

	/*-------------------------------------------------------------------------*/
	boolean mouseEventToAnimations(MouseEvent event);

	/*-------------------------------------------------------------------------*/
	boolean keyEventToAnimations(KeyEvent event);

	/*-------------------------------------------------------------------------*/
	void changeState(Maze.State state);

	/*-------------------------------------------------------------------------*/
	void draw();

	/*-------------------------------------------------------------------------*/
	Component getComponent();

	/*-------------------------------------------------------------------------*/
	ActorActionIntention getCombatIntention(PlayerCharacter pc);

	/*-------------------------------------------------------------------------*/
	int getFacing();

	/*-------------------------------------------------------------------------*/
	Rectangle getObjectBounds(EngineObject obj);

	/*-------------------------------------------------------------------------*/
	void chooseACharacter(ChooseCharacterCallback callback);

	/*-------------------------------------------------------------------------*/
	void setDebug(boolean debug);

	/*-------------------------------------------------------------------------*/
	Font getDefaultFont();

	/*-------------------------------------------------------------------------*/
	Font getSignboardFont();

	/*-------------------------------------------------------------------------*/
	void refreshCharacterData();

	/*-------------------------------------------------------------------------*/
	void refreshCharacterWidget(PlayerCharacter pc);

	/*-------------------------------------------------------------------------*/
	void disableInput();

	/*-------------------------------------------------------------------------*/
	void enableInput();

	void refreshPcActionOptions();

	/*-------------------------------------------------------------------------*/
	void addObject(EngineObject obj);

	/*-------------------------------------------------------------------------*/
	void removeObject(EngineObject obj);

	/*-------------------------------------------------------------------------*/
	void removeObject(String objectName);

	/*-------------------------------------------------------------------------*/
	void signBoard(String message, MazeEvent event);

	/*-------------------------------------------------------------------------*/
	void levelUp(PlayerCharacter playerCharacter);

	/*-------------------------------------------------------------------------*/
	void grantItems(List<Item> items);

	/*-------------------------------------------------------------------------*/
	void showBlockingScreen(String imageResource, int delay, Object mutex);

	/*-------------------------------------------------------------------------*/
	void showBlockingScreen(Image image, int delay, Object mutex);

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
	void actorAttacks(UnifiedActor attacker, UnifiedActor defender, AttackWith attackWith);

	/*-------------------------------------------------------------------------*/
	void actorDies(UnifiedActor victim);

	/*-------------------------------------------------------------------------*/
	void foeLeaves(UnifiedActor foe);

	/*-------------------------------------------------------------------------*/
	List<MazeEvent> backPartyUp(int maxTiles, int facing);

	/*-------------------------------------------------------------------------*/
	void setFoes(List<FoeGroup> others, boolean runAppearanceAnimations);

	/*-------------------------------------------------------------------------*/
	void rebalanceFoeSprites(Combat combat);

	/*-------------------------------------------------------------------------*/
	void addFoes(List<FoeGroup> others, boolean runAppearanceAnimations);

	/*-------------------------------------------------------------------------*/
	void setAllies(List<FoeGroup> others);

	/*-------------------------------------------------------------------------*/
	void displayMazeEvent(MazeEvent event, boolean displayEventText);

	/*-------------------------------------------------------------------------*/
	void setPlayerCharacter(int index, PlayerCharacter pc);

	/*-------------------------------------------------------------------------*/
	void characterSelected(int index);

	/*-------------------------------------------------------------------------*/
	public void addTexture(Texture t);

	/*-------------------------------------------------------------------------*/
	void characterSelected(PlayerCharacter pc);

	/*-------------------------------------------------------------------------*/
	void addMessage(String msg, boolean shouldJournal);

	/*-------------------------------------------------------------------------*/
	MessageDestination getMessageDestination();

	/*-------------------------------------------------------------------------*/
	void refreshResting();

	/*-------------------------------------------------------------------------*/
	Rectangle getPlayerCharacterWidgetBounds(PlayerCharacter playerCharacter);

	/*-------------------------------------------------------------------------*/
	Rectangle getPlayerCharacterPortraitBounds(PlayerCharacter defender);

	/*-------------------------------------------------------------------------*/
	Rectangle getPortraitWidgetBounds(PlayerCharacter pc);

	/*-------------------------------------------------------------------------*/
	void errorDialog(String s);

	/*-------------------------------------------------------------------------*/
	TextDialogWidget waitingDialog(String s);

	/*-------------------------------------------------------------------------*/
	Music getMusic();

	/*-------------------------------------------------------------------------*/
	void setSelectedFoeGroup(int i);

	/*-------------------------------------------------------------------------*/
	void setPlayerCharacterActionOption(PlayerCharacter pc, Class<? extends ActorActionOption> option);

	/*-------------------------------------------------------------------------*/
	boolean supportsAnimation();

	/*-------------------------------------------------------------------------*/
	void tempChangeTexture(EngineObject obj, Texture texture);

	/*-------------------------------------------------------------------------*/

	ActorGroup getSelectedFoeGroup();

	void buildGui();
}
