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

package mclachlan.maze.game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.audio.WavAudioPlayer;
import mclachlan.maze.data.Database;
import mclachlan.maze.editor.swing.CampaignPanel;
import mclachlan.maze.ui.UserInterface;
import mclachlan.maze.ui.diygui.DiyGuiUserInterface;
import mclachlan.maze.util.PerfLog;

/**
 *
 */
public class Launcher implements ActionListener
{
	private JButton ok, cancel;
	private List<Campaign> campaignList;
	private JFrame frame;
	private Map<String, String> config;
	private CampaignPanel campaignPanel;

	/*-------------------------------------------------------------------------*/
	public Launcher() throws Exception
	{
		frame = new JFrame("Escape From The Maze");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		ok = new JButton("OK");
		ok.addActionListener(this);

		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(ok);
		buttonPanel.add(cancel);

		JPanel panel = new JPanel(new BorderLayout(5,5));
		panel.setBorder(BorderFactory.createRaisedBevelBorder());

		try
		{
			config = getConfig();
			campaignList =  new ArrayList<>(Database.getCampaigns().values());

			campaignPanel = new CampaignPanel(campaignList);
			panel.add(campaignPanel, BorderLayout.CENTER);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			frame.add(panel);
		}
		catch (Exception x)
		{
			JOptionPane.showMessageDialog(frame, "ERROR");
			x.printStackTrace();
			System.exit(1);
		}

		//
		// Here's how it works:
		// If there is only one campaign, or if the maze.campaign config property
		// is set, launch straight into that one.  Else show the launcher dialog.
		// The launcher dialog can be explicitly shown using the
		// maze.show_launcher=true config property
		//

		boolean showLauncher = Boolean.valueOf(config.get(Maze.AppConfig.SHOW_LAUNCHER));

		if (campaignList.size() == 1 && !showLauncher)
		{
			frame.dispose();
			startGame(campaignList.get(0));
		}
		else if (config.get(Maze.AppConfig.CAMPAIGN) != null && !showLauncher)
		{
			frame.dispose();
			String campaign = config.get(Maze.AppConfig.CAMPAIGN);
			for (Campaign c : campaignList)
			{
				if (c.getName().equals(campaign))
				{
					startGame(c);
					break;
				}
			}
		}
		else
		{
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			int centerX = (int)(d.getWidth()/2);
			int centerY = (int)(d.getHeight()/2);
			int width = 500;
			int height = 500;

			frame.setBounds(centerX-width/2, centerY-height/2, width, height);
			frame.setVisible(true);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static Map<String, String> getConfig() throws IOException
	{
		Properties p = new Properties();
		FileInputStream inStream = new FileInputStream("maze.cfg");
		p.load(inStream);
		inStream.close();

		HashMap<String, String> config = new HashMap<String, String>();

		Iterator i = p.keySet().iterator();
		while (i.hasNext())
		{
			String key = (String)i.next();
			String value = (String)p.get(key);
			config.put(key, value);
		}

		return config;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == cancel)
		{
			frame.setVisible(false);
			System.exit(0);
		}
		else if (e.getSource() == ok)
		{
			Campaign c = campaignPanel.getCampaign();
			try
			{
				startGame(c);
			}
			catch (Exception x)
			{
				// todo: cleanup
				JOptionPane.showMessageDialog(frame, "ERROR");
				x.printStackTrace();
				frame.setVisible(false);
				System.exit(1);
			}
			frame.setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void startGame(Campaign c)
		throws Exception
	{
		Maze maze = new Maze(config, c);

		maze.initAudio(new WavAudioPlayer());
		maze.initLog(getLog(config));
		maze.initPerfLog(getPerfLog(config));
		maze.initState();
		// Beware the dependencies between components here.
		maze.initDb();
		maze.initSystems();
		maze.startThreads();
		maze.initUi(getUi(config));
	}

	/*-------------------------------------------------------------------------*/
	private Log getLog(Map<String, String> config)
		throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		String log_impl = this.config.get(Maze.AppConfig.LOG_IMPL);
		Class log_class = Class.forName(log_impl);
		Log log = (Log)log_class.newInstance();
		int logLevel = Integer.parseInt(config.get(Maze.AppConfig.LOG_LEVEL));
		log.setLevel(logLevel);
		int bufferSize = Integer.parseInt(config.get(Maze.AppConfig.LOG_BUFFER_SIZE));
		log.setBufferSize(bufferSize);

		return log;
	}

	/*-------------------------------------------------------------------------*/
	private PerfLog getPerfLog(Map<String, String> config)
		throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		String log_impl = this.config.get(Maze.AppConfig.PERF_LOG_IMPL);
		Class log_class = Class.forName(log_impl);
		PerfLog log = (PerfLog)log_class.newInstance();
		int logLevel = Integer.parseInt(config.get(Maze.AppConfig.PERF_LOG_LEVEL));
		log.setLevel(logLevel);
		int bufferSize = Integer.parseInt(config.get(Maze.AppConfig.LOG_BUFFER_SIZE));
		log.setBufferSize(bufferSize);

		return log;
	}

	/*-------------------------------------------------------------------------*/
	private UserInterface getUi(Map<String, String> config)
		throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		String ui_impl = this.config.get(Maze.AppConfig.UI_IMPL);
		Class ui_class = Class.forName(ui_impl);
		return (DiyGuiUserInterface)ui_class.newInstance();
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		new Launcher();
	}
}
