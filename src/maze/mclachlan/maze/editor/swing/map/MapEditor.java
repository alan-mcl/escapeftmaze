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

package mclachlan.maze.editor.swing.map;

import mclachlan.crusader.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.Portal;
import mclachlan.maze.util.MazeException;
import mclachlan.maze.editor.swing.ZonePanel;

/**
 *
 */
public class MapEditor extends JPanel implements ActionListener, MouseListener, MouseMotionListener
{
	MapDisplay display;
	JButton save, cancel, zoomIn, zoomOut;
	Zone zone;
	Map<JCheckBox, Integer> displayFeatureBoxes = new HashMap<JCheckBox, Integer>();
	Map<JCheckBox, Integer> selectionFeatureBoxes = new HashMap<JCheckBox, Integer>();
	private Point mousePressed;
	private TileDisplayPanel tileDisplayPanel;
	private WallDisplayPanel wallDisplayPanel;
	private MultipleTileEditingPanel multipleTileEditingPanel;
	private MultipleWallEditingPanel multipleWallEditingPanel;
	private PortalDisplayPanel portalDisplayPanel;
	private SelectionSummaryPanel selectionSummaryPanel;
	private JPanel selectionCards;
	private CardLayout selectionCardLayout;
	private static final String NO_SELECTION = "NO SELECTION";
	private static final String TILE_SELECTED = "TILE SELECTED";
	private static final String WALL_SELECTED = "WALL SELECTED";
	private static final String SELECTION_SUMMARY = "SELECTION SUMMARY";
	private static final String PORTAL_SELECTED = "PORTAL SELECTED";
	private static final String EDIT_MULTIPLE_TILES = "EDIT MULTIPLE TILES";
	private static final String EDIT_MULTIPLE_WALLS = "EDIT MULTIPLE WALLS";
	private JDialog dialog;
	private ZonePanel panel;
	private Map<String, Tool> selectionTools = new HashMap<String, Tool>();

	/*-------------------------------------------------------------------------*/
	public MapEditor(Zone zone, JDialog dialog, ZonePanel panel)
	{
		this.zone = zone;
		this.dialog = dialog;
		this.panel = panel;

		this.setLayout(new BorderLayout());
		
		display = new MapDisplay(zone);
		display.addMouseListener(this);
		display.addMouseMotionListener(this);
		this.add(new JScrollPane(
			display, 
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS),
			BorderLayout.CENTER);
		
		JPanel left = getLeftPanel();
		
		this.add(left, BorderLayout.WEST);

		save = new JButton("Save To DB");
		save.addActionListener(this);
		
		cancel = new JButton("Exit");
		cancel.addActionListener(this);
		
		JPanel bottom = new JPanel();
		bottom.add(save);
		bottom.add(cancel);
		this.add(bottom, BorderLayout.SOUTH);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getLeftPanel()
	{
		JPanel left = new JPanel(new BorderLayout());
		JPanel zoomers = new JPanel();

		zoomIn = new JButton("+");
		zoomIn.addActionListener(this);
		
		zoomOut = new JButton("-");
		zoomOut.addActionListener(this);

		zoomers.add(new JLabel("Zoom:"));
		zoomers.add(zoomIn);
		zoomers.add(zoomOut);
		left.add(zoomers, BorderLayout.NORTH);
		
		JTabbedPane tabs = new JTabbedPane();
		
		JPanel selected = new JPanel(new BorderLayout());

		selectionCardLayout = new CardLayout();
		selectionCards = new JPanel(this.selectionCardLayout);

		JComponent noSelection = new JPanel(new BorderLayout());
		noSelection.add(new JLabel("no selection"), BorderLayout.NORTH);
		tileDisplayPanel = new TileDisplayPanel(zone);
		wallDisplayPanel = new WallDisplayPanel(zone);
		portalDisplayPanel = new PortalDisplayPanel(zone, this);
		selectionSummaryPanel = new SelectionSummaryPanel(display, this, zone);
		multipleTileEditingPanel = new MultipleTileEditingPanel(zone, this);
		multipleWallEditingPanel = new MultipleWallEditingPanel(zone);
		
		selectionCards.add(noSelection, NO_SELECTION);
		selectionCards.add(tileDisplayPanel, TILE_SELECTED);
		selectionCards.add(wallDisplayPanel, WALL_SELECTED);
		selectionCards.add(portalDisplayPanel, PORTAL_SELECTED);
		selectionCards.add(multipleTileEditingPanel, EDIT_MULTIPLE_TILES);
		selectionCards.add(multipleWallEditingPanel, EDIT_MULTIPLE_WALLS);
		
		selected.add(selectionCards, BorderLayout.CENTER);
		
		JPanel displayFeatures = new JPanel(new GridLayout(15,1));
		addDisplayFeatureCheckbox("Grid", MapDisplay.Display.GRID, displayFeatures);
		addDisplayFeatureCheckbox("Tiles", MapDisplay.Display.TILES, displayFeatures);
		addDisplayFeatureCheckbox("Horiz Walls", MapDisplay.Display.HORIZ_WALLS, displayFeatures);
		addDisplayFeatureCheckbox("Vert Walls", MapDisplay.Display.VERT_WALLS, displayFeatures);
		addDisplayFeatureCheckbox("Encounters", MapDisplay.Display.ENCOUNTERS, displayFeatures);
		addDisplayFeatureCheckbox("Chests", MapDisplay.Display.CHESTS, displayFeatures);
		addDisplayFeatureCheckbox("Cast Spell Scripts", MapDisplay.Display.CAST_SPELL_SCRIPTS, displayFeatures);
		addDisplayFeatureCheckbox("Loot Scripts", MapDisplay.Display.LOOT_SCRIPTS, displayFeatures);
		addDisplayFeatureCheckbox("Flavour Text", MapDisplay.Display.FLAVOUR_TEXT_SCRIPTS, displayFeatures);
		addDisplayFeatureCheckbox("Remove Wall Scripts", MapDisplay.Display.REMOVE_WALL_SCRIPTS, displayFeatures);
		addDisplayFeatureCheckbox("Execute Script Scripts", MapDisplay.Display.EXECUTE_MAZE_SCRIPT, displayFeatures);
		addDisplayFeatureCheckbox("Custom Scripts", MapDisplay.Display.CUSTOM_SCRIPTS, displayFeatures);
		addDisplayFeatureCheckbox("Scripts On Walls", MapDisplay.Display.SCRIPTS_ON_WALLS, displayFeatures);
		addDisplayFeatureCheckbox("Objects", MapDisplay.Display.OBJECTS, displayFeatures);
		addDisplayFeatureCheckbox("Portals", MapDisplay.Display.PORTALS, displayFeatures);
		
		JPanel selectionFeatures = new JPanel(new GridLayout(20,10));
		addSelectionFeatureCheckbox("Tiles", MapDisplay.Selection.TILES, selectionFeatures);
		addSelectionFeatureCheckbox("Horiz Walls", MapDisplay.Selection.HORIZ_WALLS, selectionFeatures);
		addSelectionFeatureCheckbox("Vert Walls", MapDisplay.Selection.VERT_WALLS, selectionFeatures);

		for (Tool t : this.getSelectionTools())
		{
			JButton b = new JButton(t.getName());
			b.setActionCommand(t.getName());
			this.selectionTools.put(t.getName(), t);
			b.addActionListener(this);
			selectionFeatures.add(b);
		}
		
		ToolsPanel toolsPanel = new ToolsPanel(this, zone);
		
		tabs.add("Selected", selected);
		tabs.add("Summary", selectionSummaryPanel);
		tabs.add("Display", displayFeatures);
		tabs.add("Select", selectionFeatures);
		tabs.add("Tools", toolsPanel);
		
		left.add(tabs, BorderLayout.CENTER);
		
		return left;
	}

	/*-------------------------------------------------------------------------*/
	private void addDisplayFeatureCheckbox(String title, int feature, JPanel panel)
	{
		JCheckBox box = new JCheckBox(title);
		box.addActionListener(this);
		box.setSelected(true);
		displayFeatureBoxes.put(box, feature);
		panel.add(box);
	}
	
	/*-------------------------------------------------------------------------*/
	private void addSelectionFeatureCheckbox(String title, int feature, JPanel panel)
	{
		JCheckBox box = new JCheckBox(title);
		box.addActionListener(this);
		box.setSelected(true);
		selectionFeatureBoxes.put(box, feature);
		panel.add(box);
	}
	
	/*-------------------------------------------------------------------------*/
	private int calcIndex(Point p)
	{
		int width = zone.getWidth();
		return p.y*width + p.x%width;
	}
	
	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == save)
		{
			try
			{
				Database.getInstance().getSaver().saveZone(zone);
				
				if (panel != null)
				{
					panel.zoneWasSaved(zone);
				}
			}
			catch (Exception x)
			{
				throw new MazeException(x);
			}
		}
		else if (e.getSource() == cancel)
		{
			if (dialog != null)
			{
				dialog.setVisible(false);
			}
			else
			{
				// assume we are running stand alone
				System.exit(0);
			}
		}
		else if (e.getSource() == zoomIn)
		{
			display.incrementZoomLevel(1);
		}
		else if (e.getSource() == zoomOut)
		{
			display.incrementZoomLevel(-1);
		}
		else if (displayFeatureBoxes.containsKey(e.getSource()))
		{
			JCheckBox box = (JCheckBox)e.getSource();
			int feature = displayFeatureBoxes.get(box);
			display.setDisplayFeature(feature, box.isSelected());
		}
		else if (selectionFeatureBoxes.containsKey(e.getSource()))
		{
			JCheckBox box = (JCheckBox)e.getSource();
			int feature = selectionFeatureBoxes.get(box);
			display.setSelectionFeature(feature, box.isSelected());
		}
		else
		{
			Tool tool = this.selectionTools.get(e.getActionCommand());

			if (tool != null)
			{
				tool.execute(this, zone);
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void mouseClicked(MouseEvent e)
	{
		if (!((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK ||
			(e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK))
		{
			display.clearSelection();
		}
		
		Object obj = display.selectObjectAt(e);
		
		if (obj == null)
		{
			selectionCardLayout.show(selectionCards, NO_SELECTION);
		}
		else if (display.selectionLayer.selected.size() > 1)
		{
			refreshSelectionSummary();
		}
		else if (obj instanceof Tile)
		{
			tileDisplayPanel.setTile((Tile)obj);
			selectionCardLayout.show(selectionCards, TILE_SELECTED);
		}
		else if (obj instanceof Wall)
		{
			int index = -1;
			boolean isHoriz = false;
			for (int i=0; i<zone.getMap().getHorizontalWalls().length; i++)
			{
				if (zone.getMap().getHorizontalWalls()[i] == obj)
				{
					index = i;
					isHoriz = true;
				}
			}
			if (index == -1)
			{
				isHoriz = false;
				for (int i=0; i<zone.getMap().getVerticalWalls().length; i++)
				{
					if (zone.getMap().getVerticalWalls()[i] == obj)
					{
						index = i;
					}
				}
			}
			wallDisplayPanel.setWall((Wall)obj, index, isHoriz);
			selectionCardLayout.show(selectionCards, WALL_SELECTED);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refreshSelectionSummary()
	{
		selectionSummaryPanel.refresh();
		display.repaint();

		if (display.selectionLayer.selected.size() == 2)
		{
			// check if a Portal is selected
			
			Object t1 = display.selectionLayer.selected.get(0);
			Object t2 = display.selectionLayer.selected.get(1);
			
			if (t1 instanceof Tile && t2 instanceof Tile)
			{
				for (Portal p : zone.getPortals())
				{
					Tile from = zone.getMap().getTiles()[calcIndex(p.getFrom())];
					Tile to = zone.getMap().getTiles()[calcIndex(p.getTo())];

					if (t1 == to && t2 == from || t1 == from && t2 == to)
					{
						// user has selected two ends of a portal
						portalDisplayPanel.setPortal(p);
						selectionCardLayout.show(selectionCards, PORTAL_SELECTED);
						return;
					}
				}
			}
		}

		if (display.selectionLayer.selected.size() >= 2)
		{
			boolean foundWall = false;
			boolean foundTile = false;
			
			// check if all tiles or all walls are selected
			for (Object obj : display.selectionLayer.selected)
			{
				if (obj instanceof Tile)
				{
					foundTile = true;
					if (foundWall)
					{
						break;
					}
				}
				else if (obj instanceof Wall)
				{
					foundWall = true;
					if (foundTile)
					{
						break;
					}
				}
				else
				{
					throw new MazeException("Unexepected Object in selection ["+obj+"]");
				}
			}
			
			if (foundWall && !foundTile)
			{
				multipleWallEditingPanel.setWalls(display.selectionLayer.selected);
				selectionCardLayout.show(selectionCards, EDIT_MULTIPLE_WALLS);
				return;
			}
			else if (foundTile && !foundWall)
			{
				multipleTileEditingPanel.setTiles(display.selectionLayer.selected);
				selectionCardLayout.show(selectionCards, EDIT_MULTIPLE_TILES);
				return;
			}
		}

		display.repaint();
	}
	
	/*-------------------------------------------------------------------------*/
	public List<Object> getSelection()
	{
		return display.selectionLayer.selected;
	}

	/*-------------------------------------------------------------------------*/
	public void setSelection(List<Object> list)
	{
		display.selectionLayer.selected = list;
	}
	
	/*-------------------------------------------------------------------------*/
	public mclachlan.maze.map.Tile getMazeTile(Tile t)
	{
		for (int i=0; i<zone.getMap().getTiles().length; i++)
		{
			if (t == zone.getMap().getTiles()[i])
			{
				int x = i % zone.getWidth();
				int y = i / zone.getWidth();
				return zone.getTiles()[x][y];
			}
		}
		
		throw new MazeException("Cannot match crusader tile ["+t+"] to a maze tile");
	}

	/*-------------------------------------------------------------------------*/
	public Tile getCrusaderTile(mclachlan.maze.map.Tile t)
	{
		int index = zone.getMap().getIndex(t.getCoords());
		return zone.getMap().getTiles()[index];
	}
	
	/*-------------------------------------------------------------------------*/
	public mclachlan.crusader.Map getMap()
	{
		return zone.getMap();
	}
	
	/*-------------------------------------------------------------------------*/
	public int getCrusaderIndexOfTile(Tile t)
	{
		return zone.getMap().getIndex(t);
	}

	/*-------------------------------------------------------------------------*/
	public void mousePressed(MouseEvent e)
	{
		if (!((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK ||
			(e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK))
		{
			display.clearSelection();
		}
		mousePressed = e.getPoint();
	}

	/*-------------------------------------------------------------------------*/
	public void mouseReleased(MouseEvent e)
	{
		mousePressed = null;
		display.clearActiveSelection();
	}

	/*-------------------------------------------------------------------------*/
	public void mouseEntered(MouseEvent e)
	{
		
	}

	/*-------------------------------------------------------------------------*/
	public void mouseExited(MouseEvent e)
	{
		
	}

	/*-------------------------------------------------------------------------*/
	public void mouseDragged(MouseEvent e)
	{
		int x = Math.min(mousePressed.x, e.getX());
		int y = Math.min(mousePressed.y, e.getY());
		int width = Math.abs(mousePressed.x - e.getX());
		int height = Math.abs(mousePressed.y - e.getY());

		if (width + height < 10)
		{
			// ignore small dragsc
			return;
		}
		
		Rectangle r = new Rectangle(x, y, width, height);
		display.setActiveSelection(r);
		refreshSelectionSummary();
	}

	/*-------------------------------------------------------------------------*/
	public void mouseMoved(MouseEvent e)
	{
		
	}

	/*-------------------------------------------------------------------------*/
	public void clearSelection()
	{
		display.clearSelection();
		selectionCardLayout.show(selectionCards, NO_SELECTION);
	}

	/*-------------------------------------------------------------------------*/
	public List<Tool> getTools()
	{
		List<Tool> result = new ArrayList<Tool>();
		
		result.add(new PaintTileMana());
		result.add(new ScatterObject());
		result.add(new DeleteObjects());
		result.add(new PaintEncounters());
		result.add(new PaintWater());
		result.add(new AddMapScripts());
		result.add(new RunZoneScript());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<Tool> getSelectionTools()
	{
		List<Tool> result = new ArrayList<Tool>();

		result.add(new RouteFinder());
		result.add(new InvertSelection());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		new Database(loader, saver, Maze.getStubCampaign());

		JFrame frame = new JFrame("Map Editor");
		frame.add(new MapEditor(Database.getInstance().getZone("The Arena"), null, null));
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)(d.getWidth()/2);
		int centerY = (int)(d.getHeight()/2);
		int width = (int)(d.getWidth()-200);
		int height = (int)(d.getHeight()-200);

		frame.setBounds(centerX-width/2, centerY-height/2, width, height);
		frame.setVisible(true);
		
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
}
