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

package mclachlan.crusader.client;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.io.File;

/**
 * A simple client for testing the engine.
 */
public class TestGraphics extends Frame 
{
	private static boolean[] key  = new boolean[256];

	private static final int MAZE_WIN_WIDTH = 400;
	private static final int MAZE_WIN_HEIGHT = 400;
	private static final int SCREEN_WIDTH = 800;
	private static final int SCREEN_HEIGHT = 600;
	private static final int MAZE_WIN_LEFT = (SCREEN_WIDTH/2) - (MAZE_WIN_WIDTH/2);
	private static final int MAZE_WIN_TOP = (SCREEN_HEIGHT/2) - (MAZE_WIN_HEIGHT/2);
	
	private static final int[] keys = 
		{
			KeyEvent.VK_LEFT,
			KeyEvent.VK_RIGHT,
			KeyEvent.VK_UP,
			KeyEvent.VK_DOWN,
			KeyEvent.VK_A,
			KeyEvent.VK_S,
		};
	
	int xcounter = 0;
	int ycounter = 0;
	Image image;

	/*-------------------------------------------------------------------------*/
	public TestGraphics(String[] args) throws AWTException
	{
		this.setTitle("Graphics Test");

		GraphicsDevice device = 
			GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		this.enableEvents(KeyEvent.KEY_EVENT_MASK);
		this.setUndecorated(true);
        
		device.setFullScreenWindow(this);
		this.enableInputMethods(false);
		device.setDisplayMode(new DisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT, 32, 0));
        
		this.createBufferStrategy(2);
		
		// get rid of the cursor
		new Robot().mouseMove(SCREEN_WIDTH*2, SCREEN_HEIGHT*2);

		image = getImage("raw/fire", this);
	}

	/*-------------------------------------------------------------------------*/
	Image getImage(String resourceName, Component guiComponent)
	{
		String fileName = "img/"+resourceName+".png";
		// just get it from the file system
		MediaTracker watch = new MediaTracker(guiComponent);
		if (!new File(fileName).exists())
		{
			throw new RuntimeException(fileName+" does not exist");
		}

		Image result = Toolkit.getDefaultToolkit().createImage(fileName);
		watch.addImage(result, 0);

		try
		{
			watch.waitForAll();
		}
		catch (InterruptedException x)
		{
			throw new RuntimeException(x);
		}

		return result;
	}



	/*-------------------------------------------------------------------------*/
	void run()
	{
		// used so we can continue if the engine throws an exception
		// useful to sometimes get a partial scene and see where the engine died.
		boolean error = false;
		
		// FPS calculations
		int frameCount, frameCountRecord;
		frameCount = frameCountRecord = 0;
		
		long counter, sumRenderTime, avgRenderTime;
		counter = sumRenderTime = avgRenderTime = 0;
		
		BufferStrategy strategy = this.getBufferStrategy();
		long curTime = System.currentTimeMillis();
		while (true)
		{
			Graphics g = strategy.getDrawGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

			if (!error)
			{
				long now = System.currentTimeMillis();

				render((Graphics2D)g);

				long renderTime = System.currentTimeMillis() - now;
				counter++;
				sumRenderTime += renderTime; 
			}

			g.setColor(Color.BLUE);
			g.drawString("fps: "+frameCountRecord,20,20);
			g.setColor(Color.YELLOW);
			g.drawString("fps: "+frameCountRecord,21,21);
			
			g.setColor(Color.BLUE);
			g.drawString("render ms: "+avgRenderTime,20,30);
			g.setColor(Color.YELLOW);
			g.drawString("render ms: "+avgRenderTime,21,31);
			
			//-------- keys
			
			for (int i = 0; i < keys.length; i++)
			{
				if(key[keys[i]])
				{
					switch (keys[i])
					{
						// todo
					}
				}
			}
			
			if (key[KeyEvent.VK_ESCAPE])
			{
				System.exit(0);
			}
			
			g.dispose();
			strategy.show();
			
			frameCount++;
			
			long now = System.currentTimeMillis();
			if (now-curTime > 1000)
			{
				frameCountRecord = frameCount;
				frameCount = 0;
				curTime = now;
				avgRenderTime = sumRenderTime / counter;
				sumRenderTime = 0;
				counter = 0;
			}
		}
	}

	int interval = 50;
	long last = System.currentTimeMillis();

	/*-------------------------------------------------------------------------*/
	private void render(Graphics2D g)
	{
		if (System.currentTimeMillis() - last > interval)
		{
			xcounter += 2;
			ycounter += 1;
			last = System.currentTimeMillis();
		}

		int x = MAZE_WIN_LEFT+MAZE_WIN_WIDTH-xcounter;
		int y = MAZE_WIN_TOP+MAZE_WIN_HEIGHT/2-ycounter;

		Graphics2D g2d = (Graphics2D)g.create(x, y, 400, 400);

//		g2d.shear(.5, .5);

//		g2d.rotate(Math.PI,20,20);

		g2d.drawImage(image, 0, 0, this);
	}

	/*-------------------------------------------------------------------------*/
	protected void processKeyEvent(KeyEvent evt)
	{
		int code = evt.getKeyCode();
		
		if(evt.getID() == KeyEvent.KEY_PRESSED)
		{
			key[code] = true;
		}
		else if(code != KeyEvent.VK_SPACE)
		{
			key[code] = false;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		new TestGraphics(args).run();
	}
}
