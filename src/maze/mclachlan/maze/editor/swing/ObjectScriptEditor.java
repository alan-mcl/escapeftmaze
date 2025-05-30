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

package mclachlan.maze.editor.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.crusader.ObjectScript;
import mclachlan.crusader.script.JagObjectVertically;
import mclachlan.crusader.script.JagObjectWithinRadius;
import mclachlan.crusader.script.SinusoidalStretch;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ObjectScriptEditor extends JDialog implements ActionListener
{
//	protected static final String SEP = ",";

	private static final int CUSTOM = 0;
	private static final int JAG_VERTICALLY = 1;
	private static final int JAG_WITHIN_RADIUS = 2;
	private static final int SINUSOIDAL_STRETCH = 3;

	private static final int MAX = 4;

	static Map<Class, Integer> types;

	static
	{
		types = new HashMap<>();

		types.put(JagObjectVertically.class, JAG_VERTICALLY);
		types.put(JagObjectWithinRadius.class, JAG_WITHIN_RADIUS);
		types.put(SinusoidalStretch.class, SINUSOIDAL_STRETCH);
	}

	private ObjectScript result;

	private JButton ok, cancel;
	private JComboBox<String> type;
	private CardLayout cards;
	private JPanel controls;
	private JTextField impl;
	private int dirtyFlag;

	private JSpinner jvMinSpeed, jvMaxSpeed, minOffset, maxOffset, jvMinPause, jvMaxPause;
	private JCheckBox jvPauseTop, jvPauseBottom, jvHomeTop, jvHomeBottom;
	private JSpinner maxRadius, jwrMinSpeed, jwrMaxSpeed, jwrMinPause, jwrMaxPause;
	private JSpinner ssMinStretch, ssMaxStretch, ssSpeed;
	private JCheckBox ssVertical, ssHorizontal;

	/*-------------------------------------------------------------------------*/
	public ObjectScriptEditor(
		Frame owner,
		ObjectScript objectScript,
		int dirtyFlag)
		throws HeadlessException
	{
		super(owner, "Edit Object Script", true);
		this.dirtyFlag = dirtyFlag;

		JPanel top = new JPanel();

		JPanel top1 = new JPanel();
		Vector<String> vec = new Vector<>();
		for (int i=0; i<MAX; i++)
		{
			vec.addElement(describeType(i));
		}
		type = new JComboBox<>(vec);
		type.addActionListener(this);
		top1.add(new JLabel("Type:"));
		top1.add(type);

		top.add(top1);

		cards = new CardLayout(3,3);
		controls = new JPanel(cards);
		for (int i=0; i<MAX; i++)
		{
			JPanel c = getControls(i);
			controls.add(c, String.valueOf(i));
		}

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);

		this.setLayout(new BorderLayout(3,3));
		this.add(top, BorderLayout.NORTH);
		this.add(controls, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);

		if (objectScript != null)
		{
			setState(objectScript);
		}

		this.pack();
		setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	protected void setState(ObjectScript ms)
	{
		int srType;
		if (types.containsKey(ms.getClass()))
		{
			srType = types.get(ms.getClass());
		}
		else
		{
			srType = CUSTOM;
		}
		type.setSelectedIndex(srType);

		switch (srType)
		{
			case CUSTOM:
				impl.setText(ms.getClass().getName()); break;
			case JAG_VERTICALLY:
				JagObjectVertically jv = (JagObjectVertically)ms;
				minOffset.setValue(jv.getMinOffset());
				maxOffset.setValue(jv.getMaxOffset());
				jvMinSpeed.setValue(jv.getMinSpeed());
				jvMaxSpeed.setValue(jv.getMaxSpeed());
				jvMinPause.setValue(jv.getMinPause());
				jvMaxPause.setValue(jv.getMaxPause());
				jvPauseTop.setSelected(jv.isPauseTop());
				jvPauseBottom.setSelected(jv.isPauseBottom());
				jvHomeTop.setSelected(jv.isHomeTop());
				jvHomeBottom.setSelected(jv.isHomeBottom());
				break;
			case JAG_WITHIN_RADIUS:
				JagObjectWithinRadius jwr = (JagObjectWithinRadius)ms;
				maxRadius.setValue(jwr.getMaxRadius());
				jwrMinSpeed.setValue(jwr.getMinSpeed());
				jwrMaxSpeed.setValue(jwr.getMaxSpeed());
				jwrMinPause.setValue(jwr.getMinPause());
				jwrMaxPause.setValue(jwr.getMaxPause());
				break;
			case SINUSOIDAL_STRETCH:
				SinusoidalStretch ss = (SinusoidalStretch)ms;
				ssMinStretch.setValue(ss.getMinStretch());
				ssMaxStretch.setValue(ss.getMaxStretch());
				ssSpeed.setValue(ss.getSpeed());
				ssVertical.setSelected(ss.isVertical());
				ssHorizontal.setSelected(ss.isHorizontal());
				break;

			default: throw new MazeException("Invalid type "+srType);
		}
	}

	/*-------------------------------------------------------------------------*/
	JPanel getControls(int type)
	{
		switch (type)
		{
			case CUSTOM: return getCustomPanel();
			case JAG_VERTICALLY: return getJagVerticallyPanel();
			case JAG_WITHIN_RADIUS: return getJagWithinRadiusPanel();
			case SINUSOIDAL_STRETCH: return getSinusoidalStretchPanel();
			default: throw new MazeException("Invalid type "+type);
		}
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getSinusoidalStretchPanel()
	{
		ssMinStretch = new JSpinner(new SpinnerNumberModel(0, 0, 10, .01));
		ssMaxStretch = new JSpinner(new SpinnerNumberModel(0, 0, 10, .01));
		ssSpeed = new JSpinner(new SpinnerNumberModel(0, 0, 10, .1));
		ssVertical = new JCheckBox("Vertical?");
		ssHorizontal = new JCheckBox("Horizontal?");

		return dirtyGridLayoutCrap(
			new JLabel("Speed:"), ssSpeed,
			new JLabel("Min Stretch:"), ssMinStretch,
			new JLabel("Max Stretch:"), ssMaxStretch,
			ssVertical, new JLabel(),
			ssHorizontal, new JLabel());
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getJagVerticallyPanel()
	{
		minOffset = new JSpinner(new SpinnerNumberModel(0, -500, 500, 1));
		maxOffset = new JSpinner(new SpinnerNumberModel(0, -500, 500, 1));
		jvMinSpeed = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
		jvMaxSpeed = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
		jvMinPause = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
		jvMaxPause = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
		jvPauseTop = new JCheckBox("Pause Top?");
		jvPauseBottom = new JCheckBox("Pause Bottom?");
		jvHomeTop = new JCheckBox("Home Top?");
		jvHomeBottom = new JCheckBox("Home Bottom?");

		return dirtyGridLayoutCrap(
			new JLabel("Min Offset:"), minOffset,
			new JLabel("Max Offset:"), maxOffset,
			new JLabel("Min Speed:"), jvMinSpeed,
			new JLabel("Max Speed:"), jvMaxSpeed,
			new JLabel("Min Pause:"), jvMinPause,
			new JLabel("Max Pause:"), jvMaxPause,
			jvPauseTop, new JLabel(),
			jvPauseBottom, new JLabel(),
			jvHomeTop, new JLabel(),
			jvHomeBottom, new JLabel());
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getJagWithinRadiusPanel()
	{
		maxRadius = new JSpinner(new SpinnerNumberModel(0, 0, 500, 1));
		jwrMinSpeed = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
		jwrMaxSpeed = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
		jwrMinPause = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
		jwrMaxPause = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));

		return dirtyGridLayoutCrap(
			new JLabel("Max Radius:"), maxRadius,
			new JLabel("Min Speed:"), jwrMinSpeed,
			new JLabel("Max Speed:"), jwrMaxSpeed,
			new JLabel("Min Pause:"), jwrMinPause,
			new JLabel("Max Pause:"), jwrMaxPause);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getCustomPanel()
	{
		impl = new JTextField(20);
		return dirtyGridLayoutCrap(new JLabel("Custom Impl: "), impl);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel dirtyGridLayoutCrap(JLabel label, Component field)
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		result.add(label, gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		result.add(field, gbc);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel dirtyGridLayoutCrap(Component... comps)
	{
		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		for (int i=0; i<comps.length; i+=2)
		{
			if (i == comps.length-2)
			{
				gbc.weighty = 1;
			}
			gbc.gridx = 0;
			gbc.weightx = 0;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 2;
				gbc.weightx = 1;
			}
			panel.add(comps[i], gbc);
			gbc.gridx = 1;
			gbc.weightx = 1;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 1;
			}
			else
			{
				panel.add(comps[i+1], gbc);
			}
			gbc.gridy++;
		}

		return panel;
	}

	/*-------------------------------------------------------------------------*/
	static String describeType(int type)
	{
		switch (type)
		{
			case CUSTOM: return "Custom"; 
			case JAG_VERTICALLY: return "Jag Vertically";
			case JAG_WITHIN_RADIUS: return "Jag Within Radius";
			case SINUSOIDAL_STRETCH: return "Sinusoidal Stretch";
			default: throw new MazeException("Invalid type "+type);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == type)
		{
			cards.show(controls, String.valueOf(type.getSelectedIndex()));
		}
		else if (e.getSource() == ok)
		{
			// save changes
			setResult();
			if (SwingEditor.instance != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
			}
			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			// discard changes
			setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	public ObjectScript getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected void setResult()
	{
		int srType = type.getSelectedIndex();
		switch (srType)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(impl.getText());
					this.result = (ObjectScript)clazz.newInstance();
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
				break;
			case JAG_VERTICALLY:
				result = new JagObjectVertically(
					(Integer)minOffset.getValue(),
					(Integer)maxOffset.getValue(),
					(Integer)jvMinSpeed.getValue(),
					(Integer)jvMaxSpeed.getValue(),
					(Integer)jvMinPause.getValue(),
					(Integer)jvMaxPause.getValue(),
					jvPauseTop.isSelected(),
					jvPauseBottom.isSelected(),
					jvHomeTop.isSelected(),
					jvHomeBottom.isSelected());
				break;
			case JAG_WITHIN_RADIUS:
				result = new JagObjectWithinRadius(
					(Integer)maxRadius.getValue(),
					(Integer)jwrMinSpeed.getValue(),
					(Integer)jwrMaxSpeed.getValue(),
					(Integer)jwrMinPause.getValue(),
					(Integer)jwrMaxPause.getValue());
				break;
			case SINUSOIDAL_STRETCH:
				result = new SinusoidalStretch(
					(Double)ssSpeed.getValue(),
					(Double)ssMinStretch.getValue(),
					(Double)ssMaxStretch.getValue(),
					ssVertical.isSelected(),
					ssHorizontal.isSelected());
				break;
			default: throw new MazeException("Invalid type "+srType);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V2Loader();
		Saver saver = new V2Saver();
		new Database(loader, saver, Maze.getStubCampaign());

		JFrame owner = new JFrame("test");
		owner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		while (1==1)
		{
			ObjectScriptEditor test = new ObjectScriptEditor(owner, new JagObjectWithinRadius(99,1,2,3,4), -1);
			System.out.println("test.result = [" + test.result + "]");
		}
	}
}