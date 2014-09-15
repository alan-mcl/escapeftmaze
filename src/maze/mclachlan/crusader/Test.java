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

package mclachlan.crusader;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;

/**
 *
 */
public class Test extends Frame
{
	private MemoryImageSource pictureArray;
	private static final int SCREEN_WIDTH = 400;
	private int[] renderBuffer;
	private Image displayImage;
	private static final int SCREEN_HEIGHT = 400;

	/*-------------------------------------------------------------------------*/
	public Test()
	{
		this.setTitle("Test");

		this.pack();
		this.setVisible(true);
		
//		this.enableEvents(KeyEvent.KEY_EVENT_MASK);
//		this.setUndecorated(true);
        
//		device.setFullScreenWindow(this);
//		this.enableInputMethods(false);
//		device.setDisplayMode(new DisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT, 32, 0));
        
//		this.createBufferStrategy(2);
	}

	/*-------------------------------------------------------------------------*/
	/*-------------------------------------------------------------------------*/
	private void run()
	{
		this.renderBuffer = new int[SCREEN_HEIGHT*SCREEN_WIDTH];
		for (int i = 0; i < renderBuffer.length; i++)
		{
			renderBuffer[i] = 0;			
		}
		
		pictureArray = new MemoryImageSource(
			400,
			400,
			this.getColourModel(),
			this.renderBuffer,
			0,
			SCREEN_WIDTH);
		
		pictureArray.setAnimated(true);
		pictureArray.setFullBufferUpdates(true);
		displayImage = this.createImage(pictureArray);

		BufferStrategy strategy = this.getBufferStrategy();
//		while (true)
		{
			Graphics g = strategy.getDrawGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

			// Note the importance of using framenotify=false.  Without this flag
			// the screen flickers on this method call.
			pictureArray.newPixels(0,0,SCREEN_WIDTH,SCREEN_HEIGHT,false);
			
			g.drawImage(displayImage, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, this);
		}
	}

	/*-------------------------------------------------------------------------*/
	private ColorModel getColourModel()
	{
		return new CrusaderColourModel();		
	}


	/*-------------------------------------------------------------------------*/
	public static void main(String[] args)
	{
		new Test().run();
	}
}
