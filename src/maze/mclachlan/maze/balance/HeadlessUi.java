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
import mclachlan.maze.ui.UserInterface;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.ChooseCharacterCallback;
import mclachlan.maze.ui.diygui.MessageDestination;
import mclachlan.maze.ui.diygui.TextDialogWidget;

/**
 *
 */
public class HeadlessUi implements UserInterface
{
	@Override
	public void initConfig()
	{

	}

	public void addAnimation(Animation a)
	{
		
	}

	public void stopAllAnimations()
	{
		
	}

	public boolean mouseEventToAnimations(MouseEvent event)
	{
		return false;
	}

	public boolean keyEventToAnimations(KeyEvent event)
	{
		return false;
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

	@Override
	public Rectangle getObjectBounds(EngineObject obj)
	{
		return null;
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

	@Override
	public Font getSignboardFont()
	{
		return null;
	}

	public void refreshCharacterData()
	{
		
	}

	@Override
	public void refreshCharacterWidget(PlayerCharacter pc)
	{

	}

	@Override
	public void disableInput()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void enableInput()
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}

	@Override
	public void refreshPcActionOptions()
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

	public void showBlockingScreen(Image image, int delay, Object mutex)
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

	public void actorAttacks(UnifiedActor attacker, UnifiedActor defender, AttackWith attackWith)
	{
		
	}

	public void actorDies(UnifiedActor victim)
	{
		
	}

	public void foeLeaves(UnifiedActor foe)
	{
		
	}

	public List<MazeEvent> backPartyUp(int maxTiles, int facing)
	{
		return null;
	}

	public void setFoes(List<FoeGroup> others, boolean runAppearanceAnimations)
	{
		
	}

	@Override
	public void rebalanceFoeSprites(Combat combat)
	{
	}

	@Override
	public void addFoes(List<FoeGroup> others, boolean runAppearanceAnimations)
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

	@Override
	public void addTexture(Texture t)
	{

	}

	public void characterSelected(PlayerCharacter pc)
	{
		
	}

	public void addMessage(String msg, boolean shouldJournal)
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

	@Override
	public Rectangle getPlayerCharacterPortraitBounds(PlayerCharacter defender)
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
	public TextDialogWidget waitingDialog(String s)
	{
		return null;
	}

	public Music getMusic()
	{
		return new Music(null, false)
		{
			@Override
			public void stop()
			{

			}

			@Override
			public void close()
			{

			}

			@Override
			public String getState()
			{
				return null;
			}

			@Override
			public void setState(String state)
			{

			}

			@Override
			public void playLooped(int volume, String... fileNames)
			{

			}

			@Override
			public void setVolume(int volume)
			{

			}

			@Override
			public boolean isRunning()
			{
				return false;
			}

			@Override
			public void setEnabled(boolean enabled)
			{

			}
		};
	}

	public void setSelectedFoeGroup(int i)
	{
		
	}

	@Override
	public void setPlayerCharacterActionOption(PlayerCharacter pc,
		Class<? extends ActorActionOption> option)
	{

	}

	public boolean supportsAnimation()
	{
		return false;
	}

	@Override
	public void tempChangeTexture(EngineObject obj, Texture texture)
	{

	}

	public ActorGroup getSelectedFoeGroup()
	{
		return null;
	}

	@Override
	public void buildGui()
	{

	}
}
