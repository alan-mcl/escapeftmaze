/*
 * Copyright (c) 2014 Alan McLachlan
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

package mclachlan.maze.game.event;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.*;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.ui.diygui.BlockingScreen;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.DiyGuiUserInterface;
import mclachlan.maze.util.MazeException;

/**
 * Displays an image and text, with various options for placement
 */
public class StoryboardEvent extends MazeEvent
{
	/**
	 * The image to display.
	 */
	private String imageResource;

	/**
	 * The key of the text to display
	 */
	private String textResource;

	/**
	 * Placement of the text
	 */
	private final TextPlacement textPlacement;


	public static enum TextPlacement
	{
		CENTER, TOP_LEFT, TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT
	}

	/*-------------------------------------------------------------------------*/
	public StoryboardEvent(String imageResource, String textResource, TextPlacement textPlacement)
	{
		this.imageResource = imageResource;
		this.textResource = textResource;
		this.textPlacement = textPlacement;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		DIYPanel dialog = new DIYPanel();

		Image back = Database.getInstance().getImage(imageResource);
		dialog.setBackgroundImage(back);

		String text = StringUtil.getCampaignText(textResource);

		DIYTextArea textArea = new DIYTextArea(text);
		textArea.setTransparent(false);
		textArea.setBackgroundColour(Color.BLACK);
		textArea.setForegroundColour(Constants.Colour.GOLD);
		Font defaultFont = Maze.getInstance().getUi().getDefaultFont();
		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize() + 2f);
		textArea.setFont(f);

		int textX, textY;
		int textWidth = DiyGuiUserInterface.SCREEN_WIDTH /4;
		List<String> strings = DIYToolkit.wrapText(
			text, DIYToolkit.getInstance().getComponent().getGraphics(), textWidth);
		int textHeight = 25 + 25 * strings.size();
		int inset = 30;

		switch (textPlacement)
		{
			case CENTER:
				textX = DiyGuiUserInterface.SCREEN_WIDTH/2 - textWidth/2;
				textY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - textHeight/2;
				break;
			case TOP_LEFT:
				textX = inset;
				textY = inset;
				break;
			case TOP:
				textX = DiyGuiUserInterface.SCREEN_WIDTH/2 - textWidth/2;
				textY = inset;
				break;
			case TOP_RIGHT:
				textX = DiyGuiUserInterface.SCREEN_WIDTH -textWidth -inset;
				textY = inset;
				break;
			case RIGHT:
				textX = DiyGuiUserInterface.SCREEN_WIDTH -textWidth -inset;
				textY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - textHeight/2;
				break;
			case BOTTOM_RIGHT:
				textX = DiyGuiUserInterface.SCREEN_WIDTH -textWidth -inset;
				textY = DiyGuiUserInterface.SCREEN_HEIGHT -textHeight -inset;
				break;
			case BOTTOM:
				textX = DiyGuiUserInterface.SCREEN_WIDTH/2 - textWidth/2;
				textY = DiyGuiUserInterface.SCREEN_HEIGHT -textHeight -inset;
				break;
			case BOTTOM_LEFT:
				textX = inset;
				textY = DiyGuiUserInterface.SCREEN_HEIGHT -textHeight -inset;
				break;
			case LEFT:
				textX = inset;
				textY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - textHeight/2;
				break;
			default:
				throw new MazeException(textPlacement.toString());
		}

		textArea.setBounds(textX, textY, textWidth, textHeight);
		dialog.add(textArea);

		Maze.getInstance().getUi().showBlockingScreen(
			dialog, BlockingScreen.Mode.INTERRUPTABLE, Maze.getInstance().getEventMutex());
		
		synchronized(Maze.getInstance().getEventMutex())
		{
			try
			{
				Maze.getInstance().getEventMutex().wait();
			}
			catch (InterruptedException e)
			{
				throw new MazeException(e);
			}
		}
		
		return null;
	}
	
	/*-------------------------------------------------------------------------*/

	public String getImageResource()
	{
		return imageResource;
	}

	public String getTextResource()
	{
		return textResource;
	}

	public TextPlacement getTextPlacement()
	{
		return textPlacement;
	}
}
