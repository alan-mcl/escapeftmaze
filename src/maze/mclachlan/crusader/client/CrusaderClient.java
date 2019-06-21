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
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.CrusaderEngine32;
import mclachlan.crusader.CrusaderException;
import mclachlan.crusader.Map;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Zone;

/**
 * A simple client for testing the engine.
 */
public class CrusaderClient extends Frame 
{
	private static boolean[] key  = new boolean[256];

	private static final int MAZE_WIN_WIDTH = 600;
	private static final int MAZE_WIN_HEIGHT = 600;
	private static final int SCREEN_WIDTH = 1024;
	private static final int SCREEN_HEIGHT = 768;
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
	
	private CrusaderEngine engine;
	
	private MemoryImageSource pictureArray;
	private Image displayImage;
	
	private static final int PALETE_MAGNIFICATION = 8;
	private static final int PALETTE_IMAGE_SIZE = 16*PALETE_MAGNIFICATION;
	private byte[] paletteImage = new byte[PALETTE_IMAGE_SIZE*PALETTE_IMAGE_SIZE];
	private byte[] renderBuffer = new byte[SCREEN_WIDTH * SCREEN_HEIGHT];
	private int engineMode;
	private boolean doShading = true;
	private boolean doLighting = true;
	private double shadingDistance = 4.0;
	private double shadingMultiplier = 4.0;
	private CrusaderEngine.AntiAliasing antiAliasing = CrusaderEngine.AntiAliasing.DEFAULT;
	//	private String mapFile = "../maze/data/test/arena/testMap.txt";
	private String mapFile = "test/crusader/testMap.txt";
	private boolean eight_bit_mode = false;
	private String mazeMap;

	/*-------------------------------------------------------------------------*/
	public CrusaderClient(String[] args) throws Exception
	{
		this.setTitle("Crusader Client");
		this.setIconImage(Toolkit.getDefaultToolkit().createImage(
			"test/crusader/img/texture1.gif"));
		this.engineMode = CrusaderEngine.MovementMode.CONTINUOUS;
//		this.engineMode = CrusaderEngine.MovementMode.DISCRETE;
		this.evaluateArgs(args);

		if (eight_bit_mode)
		{
//			engine = new CrusaderEngine8(
//				getMap(),
//				MAZE_WIN_WIDTH,
//				MAZE_WIN_HEIGHT,
//				engineMode,
//				new Color(0,64,0),
//				doShading,
//				doLighting,
//				shadingDistance,
//				shadingMultiplier,
//				this);
			throw new CrusaderException("8-bit engine no longer supported");
		}
		else
		{
			if (mazeMap == null)
			{
				engine = new CrusaderEngine32(
					getMap(),
					MAZE_WIN_WIDTH,
					MAZE_WIN_HEIGHT,
					engineMode,
					Color.BLACK,
					doShading,
					doLighting,
					shadingDistance,
					shadingMultiplier,
					antiAliasing,
					0,
					CrusaderEngine.FieldOfView.FOV_60_DEGREES,
					1.0,
					this);
			}
			else
			{
				V1Loader loader = new V1Loader();
				V1Saver saver = new V1Saver();
				Database db = new Database(loader, saver);
				Campaign campaign = Maze.getStubCampaign();
				loader.init(campaign);
				saver.init(campaign);

				Zone zone = db.getZone(mazeMap);

				engine = new CrusaderEngine32(
					zone.getMap(),
					MAZE_WIN_WIDTH,
					MAZE_WIN_HEIGHT,
					engineMode,
					zone.getShadeTargetColor(),
					zone.doShading(),
					zone.doLighting(),
					zone.getShadingDistance(),
					zone.getShadingMultiplier(),
					antiAliasing,
					zone.getProjectionPlaneOffset(),
					zone.getPlayerFieldOfView(),
					zone.getScaleDistFromProjPlane(),
					this);
			}
		}
		engine.setPlayerPos(1, 2, CrusaderEngine.Facing.EAST);

		initPaletteImage();

		GraphicsDevice device = 
			GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		System.out.println("Supported display modes:");
		for (DisplayMode dm : device.getDisplayModes())
		{
			System.out.println(dm.toString());
		}
		
		this.enableEvents(KeyEvent.KEY_EVENT_MASK);
		this.setUndecorated(true);
        
		device.setFullScreenWindow(this);
		this.enableInputMethods(false);
		DisplayMode dm = new DisplayMode(
				SCREEN_WIDTH,
				SCREEN_HEIGHT,
				32,
				DisplayMode.REFRESH_RATE_UNKNOWN);
		System.out.println("Using display mode: " + dm);
		device.setDisplayMode(dm);
        
		this.createBufferStrategy(2);
		
		// get rid of the cursor
		new Robot().mouseMove(SCREEN_WIDTH*2, SCREEN_HEIGHT*2);
	}

	/*-------------------------------------------------------------------------*/
	private void evaluateArgs(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			
			if (arg.equalsIgnoreCase("-c"))
			{
				this.engineMode = CrusaderEngine.MovementMode.CONTINUOUS;
			}
			else if (arg.equalsIgnoreCase("-d"))
			{
				this.engineMode = CrusaderEngine.MovementMode.DISCRETE;				
			}
			else if (arg.equalsIgnoreCase("-noshade"))
			{
				this.doShading = false;
			}
			else if (arg.equalsIgnoreCase("-nolight"))
			{
				this.doLighting = false;
			}
			else if (arg.startsWith("-sd"))
			{
				shadingDistance = Double.parseDouble(arg.substring(3));
			}
			else if (arg.startsWith("-sm"))
			{
				shadingMultiplier = Double.parseDouble(arg.substring(3));
			}
			else if (arg.equalsIgnoreCase("-map"))
			{
				this.mapFile = args[++i];
			}
			else if (arg.equalsIgnoreCase("-8"))
			{
				this.eight_bit_mode = true;
			}
			else if (arg.equalsIgnoreCase("-mazeMap"))
			{
				this.mazeMap = args[++i];
			}
			else if (arg.startsWith("-aa:"))
			{
				String aa = arg.substring(arg.indexOf(':')+1);
				if (aa.equalsIgnoreCase("smooth"))
				{
					antiAliasing = CrusaderEngine.AntiAliasing.BOX_SMOOTH;
				}
				else if (aa.equalsIgnoreCase("sharpen"))
				{
					antiAliasing = CrusaderEngine.AntiAliasing.BOX_SHARPEN;
				}
				else if (aa.equalsIgnoreCase("raised"))
				{
					antiAliasing = CrusaderEngine.AntiAliasing.BOX_RAISED;
				}
				else if (aa.equalsIgnoreCase("motion"))
				{
					antiAliasing = CrusaderEngine.AntiAliasing.BOX_MOTION_BLUR;
				}
				else if (aa.equalsIgnoreCase("edge"))
				{
					antiAliasing = CrusaderEngine.AntiAliasing.BOX_EDGE_DETECT;
				}
				else
				{
					throw new CrusaderException("invalid aa arg: ["+aa+"]");
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void initPaletteImage()
	{
		for (int x=0; x<PALETTE_IMAGE_SIZE; x++)
		{
			for (int y=0; y<PALETTE_IMAGE_SIZE; y++)
			{
				paletteImage[x+y*PALETTE_IMAGE_SIZE] = 
					(byte)(x/PALETE_MAGNIFICATION + (y/PALETE_MAGNIFICATION)*
										PALETTE_IMAGE_SIZE/PALETE_MAGNIFICATION);
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	void run()
	{
		pictureArray = new MemoryImageSource(
			SCREEN_WIDTH,
			SCREEN_HEIGHT,
			this.engine.getColourModel(),
			this.renderBuffer,
			0,
			SCREEN_WIDTH);
		
		pictureArray.setAnimated(true);
		pictureArray.setFullBufferUpdates(true);
		displayImage = this.createImage(pictureArray);

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

			Image mazeImage = null;
			if (!error)
			{
				long now = System.currentTimeMillis();
				mazeImage = engine.render();
				long renderTime = System.currentTimeMillis() - now;
				counter++;
				sumRenderTime += renderTime; 
			}

			blit(
				MAZE_WIN_LEFT+MAZE_WIN_WIDTH+20, 
				PALETTE_IMAGE_SIZE, 
				MAZE_WIN_TOP, 
				PALETTE_IMAGE_SIZE, 
				paletteImage);

			// Note the importance of using framenotify=false.  Without this flag
			// the screen flickers on this method call.
			pictureArray.newPixels(0,0,SCREEN_WIDTH,SCREEN_HEIGHT,false);
			
			g.drawImage(displayImage, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, this);
			
			g.drawImage(mazeImage, MAZE_WIN_LEFT, MAZE_WIN_TOP, MAZE_WIN_WIDTH, MAZE_WIN_HEIGHT, this);

			// marker around the maze image
			g.setColor(Color.BLUE);
			g.drawRect(MAZE_WIN_LEFT-2, MAZE_WIN_TOP-2, MAZE_WIN_WIDTH+4, MAZE_WIN_HEIGHT+4);

			// marker around the palette image
			g.setColor(Color.BLUE);
			g.drawRect(
				MAZE_WIN_LEFT+MAZE_WIN_WIDTH+18,
				MAZE_WIN_TOP-2,
				PALETTE_IMAGE_SIZE+4,
				PALETTE_IMAGE_SIZE+4);
			

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
						case KeyEvent.VK_UP:
							this.engine.handleKey(CrusaderEngine.KeyStroke.FORWARD); 
							break;
						case KeyEvent.VK_DOWN:
							this.engine.handleKey(CrusaderEngine.KeyStroke.BACKWARD); 
							break;
						case KeyEvent.VK_LEFT:
							this.engine.handleKey(CrusaderEngine.KeyStroke.TURN_LEFT); 
							break;
						case KeyEvent.VK_RIGHT:
							this.engine.handleKey(CrusaderEngine.KeyStroke.TURN_RIGHT); 
							break;
						case KeyEvent.VK_A:
							this.engine.handleKey(CrusaderEngine.KeyStroke.STRAFE_LEFT); 
							break;
						case KeyEvent.VK_S:
							this.engine.handleKey(CrusaderEngine.KeyStroke.STRAFE_RIGHT); 
							break;
					}
					
					if (engineMode == CrusaderEngine.MovementMode.DISCRETE)
					{
						key[keys[i]]  = false;
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

	/*-------------------------------------------------------------------------*/
	private void blit(
		int left, 
		int width, 
		int top, 
		int height, 
		byte[] sourceImage)
	{
		int screenX = left;
		for (int imageX=0; imageX<width; imageX++)
		{
			int screenY = top;
			for (int imageY=0; imageY<height; imageY++)
			{
				int screenIndex = screenX + screenY*SCREEN_WIDTH;
				int imageIndex = imageX + imageY*width;
				this.renderBuffer[screenIndex] = sourceImage[imageIndex];
				screenY++;					
			}
			screenX++;
		}
	}

	/*-------------------------------------------------------------------------*/
	Map getMap()
	{
		ClientMapLoader loader = new ClientMapLoader();
		try
		{
			return loader.loadMap(new File(mapFile));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
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
	protected void processMouseEvent(MouseEvent event)
	{
		// todo: for some weird fucking reason this does not work
		int id = event.getID();
		if (id == MouseEvent.MOUSE_CLICKED)
		{
			int x = event.getX()-MAZE_WIN_LEFT;
			int y = event.getY()-MAZE_WIN_TOP;

			if (x >= 0 && x < MAZE_WIN_WIDTH && y >= 0 && y < MAZE_WIN_HEIGHT)
			{
				engine.handleMouseClick(x, y);
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		new CrusaderClient(args).run();
	}
}
