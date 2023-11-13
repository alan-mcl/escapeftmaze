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

package mclachlan.diygui.test;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.util.*;
import mclachlan.diygui.*;
import mclachlan.diygui.render.dflt.DefaultRendererFactory;
import mclachlan.diygui.toolkit.DIYBorderLayout;
import mclachlan.diygui.toolkit.DIYButtonGroup;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.util.HashMapMutableTree;

/**
 * A simple client for testing the engine.
 */
public class TestClient extends Frame 
{
	private static boolean[] key  = new boolean[256];

	private static final int MAZE_WIN_WIDTH = 400;
	private static final int MAZE_WIN_HEIGHT = 400;
	private static final int SCREEN_WIDTH = 1024;
	private static final int SCREEN_HEIGHT = 768;
	private static final int MAZE_WIN_LEFT = (SCREEN_WIDTH/2) - (MAZE_WIN_WIDTH/2);
	private static final int MAZE_WIN_TOP = (SCREEN_HEIGHT/2) - (MAZE_WIN_HEIGHT/2);
	
	private DIYToolkit gui;

	/*-------------------------------------------------------------------------*/
	public TestClient(String[] args) throws AWTException
	{
		this.setTitle("Test Client");
		
		gui = new DIYToolkit(SCREEN_WIDTH, SCREEN_HEIGHT, this,
//			MazeRendererFactory.class.getName());
			DefaultRendererFactory.class.getName());

		GraphicsDevice device =
			GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		this.enableEvents(KeyEvent.KEY_EVENT_MASK);
		this.setUndecorated(true);

		device.setFullScreenWindow(this);
		this.enableInputMethods(false);
		device.setDisplayMode(new DisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT, 32, 0));

		this.createBufferStrategy(2);

		buildGUI();

		// get rid of the cursor
		new Robot().mouseMove(SCREEN_WIDTH*2, SCREEN_HEIGHT*2);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGUI()
	{
		DIYRadioButton radio1 = new DIYRadioButton("radio1", true);
		DIYRadioButton radio2 = new DIYRadioButton("radio2");
		DIYRadioButton radio3 = new DIYRadioButton("radio3");
		DIYButtonGroup bg = new DIYButtonGroup();
		bg.addButton(radio1);
		bg.addButton(radio2);
		bg.addButton(radio3);
		
		DIYPanel gridPanel = new DIYPanel(new DIYGridLayout(2,5,10,10));
		gridPanel.setInsets(new Insets(10,10,10,10));
		gridPanel.setBounds(80, 80, 200, 200);
		gridPanel.add(new DIYLabel("Grid Layout Panel"));
		gridPanel.add(new DIYLabel("Test Label 2 (left align)", DIYToolkit.Align.LEFT));
		gridPanel.add(new DIYLabel("Test Label 3"));
		gridPanel.add(new DIYButton("Test Button"));
		gridPanel.add(new DIYButton("Test Button (left align)", DIYToolkit.Align.LEFT));
		gridPanel.add(new DIYLabel("x"));
		gridPanel.add(new DIYCheckbox("check"));
		gridPanel.add(radio1);
		gridPanel.add(radio2);
		gridPanel.add(radio3);
		gui.add(gridPanel);
		
		DIYPanel borderPanel = new DIYPanel(new DIYBorderLayout(10, 10));
		borderPanel.setInsets(new Insets(10,10,10,10));
		borderPanel.setBounds(400, 80, 300, 300);
		borderPanel.add(new DIYButton("North"), DIYBorderLayout.Constraint.NORTH);
//		borderPanel.add(new DIYButton("South"), DIYBorderLayout.Constraint.SOUTH);
//		borderPanel.add(new DIYButton("East"), DIYBorderLayout.Constraint.EAST);
//		borderPanel.add(new DIYButton("West"), DIYBorderLayout.Constraint.WEST);
		borderPanel.add(new DIYTextArea("Border Layout Panel"), DIYBorderLayout.Constraint.CENTER);
		gui.add(borderPanel);
		
		DIYPanel gridPanel2 = new DIYPanel(new DIYGridLayout(1,1,10,10));
		gridPanel2.setInsets(new Insets(10,10,10,10));
		gridPanel2.setBounds(80, 80, 200, 200);
		gridPanel2.add(new DIYTextArea("The quick brown fox jumped over the " +
			"lazy dog.\n\nAble Was I Ere I Saw Elba\n\n" +
			"A Man A Plan A Canal Panama"));
//		gridPanel2.add(new DIYLabel("Test Label 2 (left align)", DIYToolkit.Align.LEFT));
//		gridPanel2.add(new DIYLabel("Test Label 3"));
//		gridPanel2.add(new DIYButton("Test Button"));
//		gridPanel2.add(new DIYButton("Test Button (left align)", DIYToolkit.Align.LEFT));
//		gridPanel2.add(new DIYLabel("x"));
//		gridPanel2.add(new DIYLabel("x"));
//		gridPanel2.add(new DIYLabel("x"));
//		gridPanel2.add(new DIYLabel("x"));
//		gridPanel2.add(new DIYLabel("x"));
//		Widget gridPanel2 = new DIYButton("FOOOOOOOOO", DIYToolkit.Align.LEFT);
		DIYScrollPane scrollPane = new DIYScrollPane(100, 300, 250, 150, gridPanel2);
		gui.add(scrollPane);
		
		java.util.List items = new ArrayList();
		for (int i=0; i<39; i++)
		{
			items.add("item "+i);
		}
		DIYListBox listBox = new DIYListBox(items);
		DIYPane pane = new DIYPane();
		pane.setLayoutManager(new DIYBorderLayout());
		pane.add(listBox);
		DIYScrollPane scrollPane2 = new DIYScrollPane(400,400,150,150,pane);
		gui.add(scrollPane2);

		HashMapMutableTree<String> model = new HashMapMutableTree<String>();
		model.add("ONE", null);
		model.add("LALA", "ONE");
		model.add("BABA", "ONE");
		model.add("TWO", null);
		model.add("FIVE", null);
		model.add("SIX", null);
		model.add("SIXTEEN", "SIX");
		model.add("NINE", null);
		model.add("NINETEEN", "NINE");
		model.add("NINETY", "NINE");
		model.add("NINETEEN FIFTY", "NINETY");
		model.add("TWENTY ONE", null);

		Rectangle bounds = new Rectangle(250, 25, 100, 20);
		DIYComboBox combo = new DIYComboBox(model, bounds);
		combo.setPopupExpansionDirection(DIYComboBox.PopupExpansionDirection.LEFT);
		gui.add(combo);
		
		DIYToolkit.debug = true;
	}

	/*-------------------------------------------------------------------------*/
	void run()
	{
		// FPS calculations
		int frameCount, frameCountRecord;
		frameCount = frameCountRecord = 0;
		
		long counter, sumRenderTime, avgRenderTime;
		counter = sumRenderTime = avgRenderTime = 0;
		
		BufferStrategy strategy = this.getBufferStrategy();
		long curTime = System.currentTimeMillis();
		while (true)
		{
			Graphics2D g = (Graphics2D)strategy.getDrawGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			try
			{
				long now = System.currentTimeMillis();
				
				gui.draw(g);
				
				long renderTime = System.currentTimeMillis() - now;
				counter++;
				sumRenderTime += renderTime; 
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			g.setColor(Color.BLUE);
			g.drawString("fps: "+frameCountRecord,20,20);
			g.setColor(Color.YELLOW);
			g.drawString("fps: "+frameCountRecord,21,21);
			g.setColor(Color.BLUE);
			g.drawString("render ms: "+avgRenderTime,20,30);
			g.setColor(Color.YELLOW);
			g.drawString("render ms: "+avgRenderTime,21,31);
			
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
		new TestClient(args).run();
	}
}
