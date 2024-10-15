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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYPanel;
import mclachlan.maze.stat.Leveler;

/**
 *
 */
public class PortraitSelectionWidget extends ContainerWidget
	implements ActionListener
{
	DIYButton previous, next;
	List<String> portraits;
	int currentImage;
	DIYPanel imagePanel, prevImage, nextImage;
	private DIYLabel portraitFrame;

	/*-------------------------------------------------------------------------*/
	public PortraitSelectionWidget(Rectangle r)
	{
		this(r.x, r.y, r.width, r.height);
	}
	
	/*-------------------------------------------------------------------------*/
	public PortraitSelectionWidget(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		portraits = Database.getInstance().getPortraitNames();
		this.buildGui();
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui()
	{
		int inset = 10;
		int buttonWidth;
		int buttonHeight;
		buttonWidth = width/6;
		buttonHeight = width/5;
		int buttonY = y+height/2-buttonHeight/2;

		previous = new DIYButton("<");
		next = new DIYButton(">");
		previous.addActionListener(this);
		next.addActionListener(this);

		imagePanel = new DIYPanel();
		prevImage = new DIYPanel();
		nextImage = new DIYPanel();

		portraitFrame = new DIYLabel();
		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();
		BufferedImage portraitFrameImage = rp.getImageResource("pcw/portrait_frame");
		this.portraitFrame.setIcon(portraitFrameImage);
		int frameWidth = portraitFrameImage.getWidth();
		int frameHeight = portraitFrameImage.getHeight();
		this.portraitFrame.setBounds(
			x +width/2 -frameWidth/2,
			y +height/2 -frameHeight/2,
			frameWidth,
			frameHeight);


		String defaultPortrait = Maze.getInstance().getCampaign().getDefaultPortrait();
		currentImage = portraits.indexOf(defaultPortrait);
		if (currentImage == -1)
		{
			currentImage = 0;
		}
		setImage(currentImage);

		previous.setBounds(x+inset, buttonY, buttonWidth, buttonHeight);
		next.setBounds(x+width-buttonWidth-inset*2, buttonY, buttonWidth, buttonHeight);

		this.add(portraitFrame);
//		this.add(prevImage);
//		this.add(nextImage);
		this.add(imagePanel);
		this.add(next);
		this.add(previous);
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Attempts to set the portrait to the given race.
	 */
	public void setToRaceAndGender(String raceName, String genderName,
		List<String> portraits)
	{
		String portraitName = Leveler.getRandomPortraitName(raceName, genderName);
		setImage(portraits.indexOf(portraitName));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Attempts to set the portrait to the given image
	 */
	public void setToPortrait(String image)
	{
		int i = portraits.indexOf(image);
		if (i >= 0)
		{
			setImage(i);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void setImage(int imageNr)
	{
		int inset = 10;
		currentImage = imageNr;
		Image image = Database.getInstance().getImage(portraits.get(imageNr));

		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();

		int imageHeight = image.getHeight(DiyGuiUserInterface.gui.getComponent());
		int imageWidth = image.getWidth(DiyGuiUserInterface.gui.getComponent());
		int frameBorder = rp.getProperty(RendererProperties.Property.PCW_PORTRAIT_FRAME_BORDER);

		// center the image
		imagePanel.setBounds(
			portraitFrame.x +portraitFrame.width/2 -imageWidth/2,
			portraitFrame.y +portraitFrame.height -frameBorder -imageHeight,
			imageWidth,
			imageHeight);
		imagePanel.setBackgroundImage(image);

		// set the prev image
		image = Database.getInstance().getImage(portraits.get(getPrevImageNr(imageNr)));

		imageHeight = image.getHeight(DiyGuiUserInterface.gui.getComponent());
		imageWidth = image.getWidth(DiyGuiUserInterface.gui.getComponent());

		prevImage.setBounds(
			x+inset,
			y+height/2-imageHeight/2,
			imageWidth,
			imageHeight);
		prevImage.setBackgroundImage(image);

		// set the next image
		image = Database.getInstance().getImage(portraits.get(getNextImageNr(imageNr)));

		imageHeight = image.getHeight(DiyGuiUserInterface.gui.getComponent());
		imageWidth = image.getWidth(DiyGuiUserInterface.gui.getComponent());

		nextImage.setBounds(
			x+width-inset*2-imageWidth,
			y+height/2-imageHeight/2,
			imageWidth,
			imageHeight);
		nextImage.setBackgroundImage(image);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.NONE;
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		if (obj == next)
		{
			return next();
		}
		else if (obj == previous)
		{
			return previous();
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	boolean previous()
	{
		currentImage = getPrevImageNr(currentImage);
		setImage(currentImage);
		return true;
	}

	boolean next()
	{
		currentImage = getNextImageNr(currentImage);
		setImage(currentImage);
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_LEFT -> previous();
			case KeyEvent.VK_RIGHT -> next();
		}
	}

	/*-------------------------------------------------------------------------*/
	private int getPrevImageNr(int current)
	{
		current = current - 1;

		if (current < 0)
		{
			current = portraits.size()-1;
		}
		return current;
	}

	/*-------------------------------------------------------------------------*/
	private int getNextImageNr(int current)
	{
		return (current +1) % portraits.size();
	}
}
