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

import java.awt.*;
import java.util.List;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.RendererProperties;
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
	private TextPlacement textPlacement;


	public enum TextPlacement
	{
		CENTER, TOP_LEFT, TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT
	}

	public StoryboardEvent()
	{
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

		if (textResource != null && textResource.length() > 0)
		{
			String text = StringUtil.getCampaignText(textResource);

			DIYTextArea textArea = new DIYTextArea(text);
			textArea.setTransparent(false);
			textArea.setBackgroundColour(Color.BLACK);
			textArea.setForegroundColour(Constants.Colour.GOLD);
			Font defaultFont = Maze.getInstance().getUi().getDefaultFont();
			Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize() + 2f);
			textArea.setFont(f);

			Graphics g = DIYToolkit.getInstance().getComponent().getGraphics();
			Font oldFont = g.getFont();
			g.setFont(f);

			int textX, textY;
			int textWidth = DiyGuiUserInterface.SCREEN_WIDTH / 4;
			List<String> strings = DIYToolkit.wrapText(
				text, textWidth, g);

			FontMetrics fm = g.getFontMetrics(f);

			int textHeight = 5 + fm.getHeight() * strings.size();
			int inset = 40;

			g.setFont(oldFont);

			switch (textPlacement)
			{
				case CENTER ->
				{
					textX = DiyGuiUserInterface.SCREEN_WIDTH / 2 - textWidth / 2;
					textY = DiyGuiUserInterface.SCREEN_HEIGHT / 2 - textHeight / 2;
				}
				case TOP_LEFT ->
				{
					textX = inset;
					textY = inset;
				}
				case TOP ->
				{
					textX = DiyGuiUserInterface.SCREEN_WIDTH / 2 - textWidth / 2;
					textY = inset;
				}
				case TOP_RIGHT ->
				{
					textX = DiyGuiUserInterface.SCREEN_WIDTH - textWidth - inset;
					textY = inset;
				}
				case RIGHT ->
				{
					textX = DiyGuiUserInterface.SCREEN_WIDTH - textWidth - inset;
					textY = DiyGuiUserInterface.SCREEN_HEIGHT / 2 - textHeight / 2;
				}
				case BOTTOM_RIGHT ->
				{
					textX = DiyGuiUserInterface.SCREEN_WIDTH - textWidth - inset;
					textY = DiyGuiUserInterface.SCREEN_HEIGHT - textHeight - inset;
				}
				case BOTTOM ->
				{
					textX = DiyGuiUserInterface.SCREEN_WIDTH / 2 - textWidth / 2;
					textY = DiyGuiUserInterface.SCREEN_HEIGHT - textHeight - inset;
				}
				case BOTTOM_LEFT ->
				{
					textX = inset;
					textY = DiyGuiUserInterface.SCREEN_HEIGHT - textHeight - inset;
				}
				case LEFT ->
				{
					textX = inset;
					textY = DiyGuiUserInterface.SCREEN_HEIGHT / 2 - textHeight / 2;
				}
				default -> throw new MazeException(textPlacement.toString());
			}

			int border = DIYToolkit.getInstance().getRendererProperties().getProperty(RendererProperties.Property.PANEL_MED_BORDER);
			DIYPanel textPanel = new DIYPanel(
				textX-border,
				textY-border,
				textWidth+border*2,
				textHeight+border*2);
			textPanel.setStyle(DIYPanel.Style.PANEL_MED);

			textPanel.add(textArea);
			textArea.setBounds(textX, textY, textWidth, textHeight);

			dialog.add(textPanel);
		}

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

	public void setImageResource(String imageResource)
	{
		this.imageResource = imageResource;
	}

	public void setTextResource(String textResource)
	{
		this.textResource = textResource;
	}

	public void setTextPlacement(
		TextPlacement textPlacement)
	{
		this.textPlacement = textPlacement;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		StoryboardEvent that = (StoryboardEvent)o;

		if (getImageResource() != null ? !getImageResource().equals(that.getImageResource()) : that.getImageResource() != null)
		{
			return false;
		}
		if (getTextResource() != null ? !getTextResource().equals(that.getTextResource()) : that.getTextResource() != null)
		{
			return false;
		}
		return getTextPlacement() == that.getTextPlacement();
	}

	@Override
	public int hashCode()
	{
		int result = getImageResource() != null ? getImageResource().hashCode() : 0;
		result = 31 * result + (getTextResource() != null ? getTextResource().hashCode() : 0);
		result = 31 * result + (getTextPlacement() != null ? getTextPlacement().hashCode() : 0);
		return result;
	}
}
