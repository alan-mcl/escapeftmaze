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

package mclachlan.maze.balance;

import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import mclachlan.crusader.EngineObject;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.maze.audio.Music;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.stat.*;
import mclachlan.maze.ui.UserInterface;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.ChooseCharacterCallback;
import mclachlan.maze.ui.diygui.MessageDestination;

/**
 *
 */
public class HeadlessUi implements UserInterface
{
	public void addAnimation(Animation a)
	{
		
	}

	public void stopAllAnimations()
	{
		
	}

	public void mouseEventToAnimations(MouseEvent event)
	{
		
	}

	public void keyEventToAnimations(KeyEvent event)
	{
		
	}

	public void changeState(Maze.State state)
	{
		
	}

	public void draw()
	{
		
	}

	public Component getComponent()
	{
		return null;
	}

	public ActorActionIntention getCombatIntention(PlayerCharacter pc)
	{
		return null;
	}

	public int getFacing()
	{
		// todo
		return 0;
	}

	public void chooseACharacter(ChooseCharacterCallback callback)
	{
		
	}

	public void setDebug(boolean debug)
	{
		
	}

	public Font getDefaultFont()
	{
		return null;
	}

	public void refreshCharacterData()
	{
		
	}

	public void addObject(EngineObject obj)
	{
		
	}

	public void removeObject(EngineObject obj)
	{
		
	}

	public void removeObject(String objectName)
	{
		
	}

	public void addObjectInFrontOfPlayer(EngineObject obj, double distance,
		double arcOffset, boolean randomStartingFrame)
	{
		
	}

	public void signBoard(String message, MazeEvent event)
	{
		
	}

	public void levelUp(PlayerCharacter playerCharacter)
	{
		
	}

	public void grantItems(List<Item> items)
	{
		
	}

	public void showBlockingScreen(String imageResource, int delay, Object mutex)
	{
		
	}

	public void showBlockingScreen(ContainerWidget dialog, int waitOnClick,
		Object mutex)
	{

	}

	public void clearBlockingScreen()
	{
		
	}

	public void showCreateCharacterScreen()
	{
		
	}

	public void showCombatScreen()
	{
		
	}

	public void showMagicScreen()
	{
		
	}

	public void showInventoryScreen()
	{
		
	}

	public void showStatsScreen()
	{
		
	}

	public void showModifiersScreen()
	{
		
	}

	public void showPropertiesScreen()
	{
		
	}

	public void showMovementScreen()
	{
		
	}

	public void showRestingScreen()
	{
		
	}

	public void showMainMenu()
	{
		
	}

	public void resetMainMenuState()
	{
		
	}

	public void showSaveLoadScreen()
	{
		
	}

	public void showChestScreen(Chest chest)
	{
		
	}

	public void showDialog(ContainerWidget dialog)
	{
		
	}

	public void clearDialog()
	{
		
	}

	@Override
	public void clearMessages()
	{

	}

	public void setParty(PlayerParty party)
	{
		
	}

	public void setZone(Zone zone, Point pos, int facing)
	{
		
	}

	public void setPlayerPos(Point pos, int facing)
	{
		
	}

	public void setTile(Zone zone, Tile t, Point tile)
	{
		
	}

	public void actorAttacks(UnifiedActor attacker)
	{
		
	}

	public void actorDies(UnifiedActor victim)
	{
		
	}

	public void foeFlees(UnifiedActor coward)
	{
		
	}

	public void backPartyUp(int maxKeys)
	{
		
	}

	public void setFoes(List<FoeGroup> others)
	{
		
	}

	public void setAllies(List<FoeGroup> others)
	{
		
	}

	public void displayMazeEvent(MazeEvent event, boolean displayEventText)
	{
		
	}

	public void setPlayerCharacter(int index, PlayerCharacter pc)
	{
		
	}

	public void characterSelected(int index)
	{
		
	}

	public void characterSelected(PlayerCharacter pc)
	{
		
	}

	public void addMessage(String msg)
	{
		
	}

	@Override
	public MessageDestination getMessageDestination()
	{
		return new MessageDestination()
		{
			@Override
			public void addMessage(String message)
			{

			}

			@Override
			public void setHeader(String text)
			{

			}

			@Override
			public void clearMessages()
			{

			}
		};
	}

	public void refreshResting()
	{
		
	}

	public Rectangle getPlayerCharacterWidgetBounds(
		PlayerCharacter playerCharacter)
	{
		return null;
	}

	public Rectangle getPortraitWidgetBounds(PlayerCharacter pc)
	{
		return null;
	}

	public void errorDialog(String s)
	{
		
	}

	@Override
	public void waitingDialog(String s)
	{

	}

	public Music getMusic()
	{
		return null;
	}

	public void setSelectedFoeGroup(int i)
	{
		
	}

	public boolean supportsAnimation()
	{
		return false;
	}

	public ActorGroup getSelectedFoeGroup()
	{
		return null;
	}
}
