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

package mclachlan.diygui.toolkit;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.*;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.render.dflt.DefaultRendererFactory;
import mclachlan.maze.game.Maze;
import mclachlan.maze.ui.diygui.render.MazeRendererFactory;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class DIYToolkit
{
	public static final String NONE = "NoRenderer";
	public static final String LABEL = "Label";
	public static final String PANE = "Pane";
	public static final String PANEL = "Panel";
	public static final String BUTTON = "Button";
	public static final String SCROLL_PANE = "ScrollPane";
	public static final String TEXT_AREA = "TextArea";
	public static final String TEXT_FIELD = "TextField";
	public static final String CHECKBOX = "Checkbox";
	public static final String RADIO_BUTTON = "RadioButton";
	public static final String LIST_BOX_ITEM = "ListBoxItem";
	public static final String COMBO_BOX = "ComboBox";
	public static final String COMBO_ITEM = "ComboItem";

	/*-------------------------------------------------------------------------*/
	public enum Align
	{
		LEFT, CENTER, RIGHT
	}

	/*-------------------------------------------------------------------------*/
	/** the singleton instance */
	private static DIYToolkit instance;

	/** render factory for the widgets */
	private RendererFactory rendererFactory;

	/** for debugging components */
	public static boolean debug = false;

	/** The AWT frame */
	private final Component comp;
	
	/** The main content pane. Everything else is added to this. */
	private final ContainerWidget contentPane;

	/** and overlay pane, typically used for modal dialogs */
	private ContainerWidget overlayPane;
	/** The widget currently under the mouse */
	private Widget hoverWidget;
	/** The widget that currently has focus */
	private Widget focusWidget;
	/** The current cursor, null if the default */
	private Cursor cursor;
	/** The contents of the drag and drop cursor */
	private Object cursorContents;
	/** The modal dialog stack */
	private final Stack<ContainerWidget> dialogs = new Stack<>();
	/** mutex protecting the modal dialog stack */
	private final Object dialogMutex = new Object();

	/**
	 * True if this DIYToolkit must use an internal queue for event processing.
	 * This queue then shares the draw thread. DIYToolkit does not start any of
	 * its own threads.
	 */
	private boolean internalQueue;
	/** The queue used to process input events. */
	private final Queue<InputEvent> queue;

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a GUI that manages its own internal event queue.  Events are
	 * processed during calls to the draw method.
	 */
	public DIYToolkit(
		int width,
		int height,
		Component comp)
	{
		this(width, height, comp, new ConcurrentLinkedQueue<>(), null);
		this.internalQueue = true;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a GUI that manages its own internal event queue.  Events are
	 * processed during calls to the draw method.
	 */
	public DIYToolkit(
		int width,
		int height,
		Component comp,
		String renderFactoryImpl)
	{
		this(width, height, comp, new ConcurrentLinkedQueue<>(), renderFactoryImpl);
		this.internalQueue = true;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a GUI that places events on the specified queue.  Event processing
	 * is the responsibility of the caller
	 */
	public DIYToolkit(
		int width, 
		int height, 
		Component comp, 
		Queue<InputEvent> queue, 
		String rendererFactoryImpl)
	{
		instance = this;

		this.comp = comp;

		// the listeners job is to intercept the AWT events and put them on the
		// queue for processing
		Listener tap = new Listener();
		comp.addKeyListener(tap);
		comp.addMouseListener(tap);
		comp.addMouseMotionListener(tap);

		this.initRendererFactory(rendererFactoryImpl);

		this.contentPane = new DIYPane(0, 0, width, height);
		this.contentPane.setLayoutManager(new NullLayout());

		this.queue = queue;
		this.internalQueue = false;
	}

	/*-------------------------------------------------------------------------*/
	public static DIYToolkit getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/
	private void initRendererFactory(String rendererFactoryImpl)
	{
		String impl = rendererFactoryImpl==null?
			System.getProperty("mclachlan.diygui.renderer_factory"):
			rendererFactoryImpl;
		
		if (impl == null)
		{
			this.rendererFactory = new DefaultRendererFactory();
		}
		else
		{
			try
			{
				Class<?> clazz = Class.forName(impl);
				this.rendererFactory = (RendererFactory)clazz.newInstance();
			}
			catch (Exception e)
			{
				throw new DIYException(e);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void add(Widget w)
	{
		this.contentPane.add(w);
		this.contentPane.doLayout();
	}
	
	/*-------------------------------------------------------------------------*/
	public void remove(Widget w)
	{
		this.contentPane.remove(w);
		this.contentPane.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public int getQueueLength()
	{
		return this.queue.size();
	}
	
	/*-------------------------------------------------------------------------*/
	public ContainerWidget getContentPane()
	{
		return contentPane;
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getOverlayPane()
	{
		return overlayPane;
	}

	/*-------------------------------------------------------------------------*/
	public void setOverlayPane(ContainerWidget overlayPane)
	{
		this.overlayPane = overlayPane;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Draw the GUI
	 */
	public void draw(Graphics2D g)
	{
		this.contentPane.draw(g);
		if (this.overlayPane != null)
		{
			this.overlayPane.draw(g);
		}

		if (hoverWidget != null && debug)
		{
			g.setColor(Color.WHITE);
			g.drawRect(hoverWidget.x, hoverWidget.y, hoverWidget.width, hoverWidget.height);
		}

		// instead of syncing over the whole draw process create a copy
		java.util.List<ContainerWidget> dd;

		synchronized(dialogMutex)
		{
			dd = new ArrayList<>(dialogs);
		}

		for (ContainerWidget d : dd)
		{
			d.draw(g);
		}

		if (internalQueue)
		{
			// not sure if we should be throttling here?
			while (queue.peek() != null)
			{
				processEvent(queue.poll());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	RendererFactory getRendererFactory()
	{
		return rendererFactory;
	}

	/*-------------------------------------------------------------------------*/

	public static Dimension getDimension(String s)
	{
		return getDimension(s, null);
	}
	
	/*-------------------------------------------------------------------------*/

	/**
	 * @return the dimensions of the given string when rendered with the current
	 * font
	 */
	public static Dimension getDimension(String s, Font f)
	{
		Graphics g = instance.comp.getGraphics();

		if (g == null)
		{
			return null;
		}

		if (f == null)
		{
			f = g.getFont();
		}

		FontMetrics fm = g.getFontMetrics(f);
	
		int textHeight = fm.getHeight();
		int textWidth = fm.stringWidth(s);
		
		return new Dimension(textWidth, textHeight);
	}

	/*-------------------------------------------------------------------------*/
	public static Dimension getDimension(Image image)
	{
		return new Dimension(
			image.getWidth(getInstance().getComponent()),
			image.getHeight(getInstance().getComponent()));
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return the estimated dimensions of a string of the given length rendered
	 * with the current font
	 */
	public static Dimension getDimension(int stringLength)
	{
		Graphics g = instance.comp.getGraphics();

		if (g == null)
		{
			return null;
		}

		FontMetrics fm = g.getFontMetrics();

		int textHeight = fm.getAscent();
		int textWidth = fm.charWidth(' ') *stringLength;

		return new Dimension(textWidth, textHeight);
	}

	/*-------------------------------------------------------------------------*/
	public Component getComponent()
	{
		return comp;
	}

	/*-------------------------------------------------------------------------*/
	public Graphics2D getGraphics()
	{
		return (Graphics2D)comp.getGraphics();
	}

	/*-------------------------------------------------------------------------*/
	/**
    * Method to overlay Images
    *
    * @param bgImage --> The background Image
    * @param fgImage --> The foreground Image
    * @return --> overlayed image (fgImage over bgImage)
    */
	public static BufferedImage overlayImages(
		BufferedImage bgImage,
		BufferedImage fgImage)
	{
		//
		// Doing some preliminary validations.
		// Foreground image height cannot be greater than background image height.
		// Foreground image width cannot be greater than background image width.
		//
		if (fgImage.getHeight() > bgImage.getHeight() || fgImage.getWidth() > fgImage.getWidth())
		{
			throw new DIYException(
				"Foreground Image Is Bigger In One or Both Dimensions"
					+ "\nCannot proceed with overlay."
					+ "\n\n Please use smaller Image for foreground");
		}

		//Create a Graphics  from the background image
		Graphics2D g = bgImage.createGraphics();
		//Set Antialias Rendering
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		//
		// Draw background image at location (0,0)
		// You can change the (x,y) value as required
		//
		g.drawImage(bgImage, 0, 0, null);

		//
		// Draw foreground image at location (0,0)
		// Change (x,y) value as required.
		//
		g.drawImage(fgImage, 0, 0, null);

		g.dispose();
		return bgImage;
	}

	/*-------------------------------------------------------------------------*/
	public static BufferedImage cloneImage(BufferedImage original)
	{
		ColorModel cm = original.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = original.copyData(null);

		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}


	/*-------------------------------------------------------------------------*/
	public void setCursor(Cursor cursor, Object cursorContents)
	{
		this.cursor = cursor;
		this.cursorContents = cursorContents;
		comp.setCursor(this.cursor);
	}
	
	/*-------------------------------------------------------------------------*/	
	public Object getCursorContents()
	{
		return cursorContents;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Clears the cursor of any contents and sets the cursor icon back to the
	 * default.
	 */
	public void clearCursor()
	{
		this.cursor = null;
		this.cursorContents = null;
		comp.setCursor(Cursor.getDefaultCursor());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Sets the modal dialog.
	 */ 
	public void setDialog(ContainerWidget d)
	{
		synchronized (dialogMutex)
		{
			this.dialogs.push(d);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Clears the topmost modal dialog.
	 */ 
	public void clearDialog()
	{
		synchronized (dialogMutex)
		{
			if (!this.dialogs.isEmpty())
			{
				this.dialogs.pop();
				resetFocusAndHoverWidgets();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Clears the topmost modal dialog.
	 */
	public void clearAllDialogs()
	{
		synchronized (dialogMutex)
		{
			while(!this.dialogs.isEmpty())
			{
				this.dialogs.pop();
			}
			resetFocusAndHoverWidgets();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void resetFocusAndHoverWidgets()
	{
		Point p = comp.getMousePosition();
		hoverWidget = getHoverComponent(p);
		setFocus(hoverWidget);
	}

	/*-------------------------------------------------------------------------*/
	private Widget getHoverComponent(Point p)
	{
		if (p == null)
		{
			return null;
		}

		synchronized (dialogMutex)
		{
			int x = p.x;
			int y = p.y;

			if (overlayPane != null)
			{
				return overlayPane.getHoverComponent(x, y);
			}
			else if (contentPane != null)
			{
				return contentPane.getHoverComponent(x, y);
			}
			else
			{
				// hiding possible race conditions here, but wth
				return null;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Clears the given dialog from the stack, whether or not it's on top
	 */
	public void clearDialog(ContainerWidget dialog)
	{
		synchronized(dialogMutex)
		{
			this.dialogs.remove(dialog);
			resetFocusAndHoverWidgets();
		}
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getDialog()
	{
		synchronized (dialogMutex)
		{
			if (this.dialogs.isEmpty())
			{
				return null;
			}
			else
			{
				return this.dialogs.peek();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setFocus(Widget widget)
	{
		if (focusWidget != null)
		{
			focusWidget.focus = false;
		}

		focusWidget = widget;

		if (focusWidget != null)
		{
			focusWidget.focus = true;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void processEvent(Object e)
	{
		if (e instanceof KeyEvent)
		{
			KeyEvent event = (KeyEvent)e;
			switch (event.getID())
			{
				case KeyEvent.KEY_PRESSED -> this.keyPressed(event);
				case KeyEvent.KEY_RELEASED -> this.keyReleased(event);
				case KeyEvent.KEY_TYPED -> this.keyTyped(event);
				default ->
					throw new DIYException("Unrecognised KeyEvent ID: " + event.getID());
			}
		}
		else if (e instanceof MouseEvent)
		{
			MouseEvent event = (MouseEvent)e;
			switch (event.getID())
			{
				case MouseEvent.MOUSE_CLICKED -> this.mouseClicked(event);
				case MouseEvent.MOUSE_DRAGGED -> this.mouseDragged(event);
				case MouseEvent.MOUSE_ENTERED -> this.mouseEntered(event);
				case MouseEvent.MOUSE_EXITED -> this.mouseExited(event);
				case MouseEvent.MOUSE_MOVED -> this.mouseMoved(event);
				case MouseEvent.MOUSE_PRESSED -> this.mousePressed(event);
				case MouseEvent.MOUSE_RELEASED -> this.mouseReleased(event);
				default ->
					throw new DIYException("Unrecognised MouseEvent ID: " + event.getID());
			}
		}
		else if (e == null)
		{
		}
		else
		{
			throw new DIYException("Unrecognised event: "+e);
		}
	}

	/*----------------------------------------------------------------------*/
	public void keyPressed(KeyEvent e)
	{
		if (!dialogs.isEmpty())
		{
			if (focusWidget != null && focusWidget.parent == getDialog())
			{
				focusWidget.processKeyPressed(e);
			}
			else
			{
				getDialog().processKeyPressed(e);
			}
		}
		else if (focusWidget != null && focusWidget.isEnabled())
		{
			focusWidget.processKeyPressed(e);
		}
		else if (hoverWidget != null && hoverWidget.isEnabled())
		{
			hoverWidget.processKeyPressed(e);
		}
	}

	/*----------------------------------------------------------------------*/
	public void keyReleased(KeyEvent e)
	{
		if (!dialogs.isEmpty())
		{
			getDialog().processKeyReleased(e);
		}
		else if (focusWidget != null && focusWidget.isEnabled())
		{
			focusWidget.processKeyReleased(e);
		}
		else if (hoverWidget != null && hoverWidget.isEnabled())
		{
			hoverWidget.processKeyReleased(e);
		}
	}

	/*----------------------------------------------------------------------*/
	public void keyTyped(KeyEvent e)
	{
		if (!dialogs.isEmpty())
		{
			getDialog().processKeyTyped(e);
		}
		else if (focusWidget != null && focusWidget.isEnabled())
		{
			focusWidget.processKeyTyped(e);
		}
		else if (hoverWidget != null && hoverWidget.isEnabled())
		{
			hoverWidget.processKeyTyped(e);
		}
	}

	/*----------------------------------------------------------------------*/
	public void mouseClicked(MouseEvent e)
	{
		if (e.getButton() != MouseEvent.NOBUTTON)
		{
			hoverWidget = getHoverComponent(e.getPoint());
			if (hoverWidget != null)
			{
				// change the focus component:
				if (focusWidget != null)
				{
					focusWidget.setFocus(false);
					focusWidget = hoverWidget;
					focusWidget.setFocus(true);
				}

				hoverWidget.processMouseClicked(e);
			}
			else if (getDialog() != null)
			{
				// special case: there is a modal dialog visible but the user
				// has clicked outside it.  In this case, notify the dialog
				// anyway.
				Widget child = contentPane.getChild(e.getX(), e.getY());
				e.setSource(child);
				getDialog().processMouseClicked(e);
			}
			else
			{
				// lose focus
				if (focusWidget != null)
				{
					focusWidget.setFocus(false);
					focusWidget = null;
				}
			}
			
			// if not consumed, notify the content pane.  this is a hack to allows
			// applications to write a global mouse click handler
//			if (!consumed)
//			{
//				this.contentPane.processMouseClicked(e);
//			}
		}
	}

	/*----------------------------------------------------------------------*/
	public void mouseEntered(MouseEvent e)
	{
		hoverWidget = getHoverComponent(e.getPoint());
		if (hoverWidget != null && hoverWidget.isEnabled())
		{
			hoverWidget.processMouseEntered(e);
		}
	}

	/*----------------------------------------------------------------------*/
	public void mouseExited(MouseEvent e)
	{
		hoverWidget = getHoverComponent(e.getPoint());
		if (hoverWidget != null && hoverWidget.isEnabled())
		{
			hoverWidget.processMouseExited(e);
		}
	}

	/*----------------------------------------------------------------------*/
	public void mousePressed(MouseEvent e)
	{
		if (hoverWidget != null && hoverWidget.isEnabled())
		{
			hoverWidget.processMousePressed(e);
		}
	}

	/*----------------------------------------------------------------------*/
	public void mouseReleased(MouseEvent e)
	{
		if (hoverWidget != null && hoverWidget.isEnabled())
		{
			hoverWidget.processMouseReleased(e);
		}
	}

	/*----------------------------------------------------------------------*/
	public void mouseDragged(MouseEvent e)
	{
		// not yet supported
	}

	/*----------------------------------------------------------------------*/
	public void mouseMoved(MouseEvent e)
	{
		Widget newHoverWidget = getHoverComponent(e.getPoint());
		if (newHoverWidget != null && newHoverWidget != hoverWidget)
		{
			if (hoverWidget != null  && hoverWidget.isEnabled())
			{
				hoverWidget.processMouseExited(e);
			}
			newHoverWidget.processMouseEntered(e);
			hoverWidget = newHoverWidget;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static ArrayList<String> wrapText(String s, Graphics g, int width)
	{
		FontMetrics fm = g.getFontMetrics();

		Scanner scanner = new Scanner(s);

		ArrayList<String> result = new ArrayList<>();
		StringBuilder temp = new StringBuilder();

		// dunno why this is necessary, probably hides a bug
		int inset = 10;

		while (scanner.hasNext())
		{
			String cur = scanner.next();

			Rectangle2D bounds = fm.getStringBounds(temp + cur, g);

			if (bounds.getWidth() > width- inset)
			{
				result.add(temp.toString().trim());
				temp = new StringBuilder(cur);
			}
			else
			{
				temp.append(" ").append(cur);
			}
		}
		result.add(temp.toString().trim());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void drawImageCentered(
		Graphics2D g,
		Image img,
		Rectangle bounds,
		Align alignment)
	{
		Dimension imgD = getDimension(img);

		// center on the Y axis
		int imgOffsetY = (bounds.height - imgD.height)/2;

		// cater for various X axis alignment
		int imgOffsetX;
		if (alignment == Align.CENTER)
		{
			imgOffsetX = (bounds.width - imgD.width)/2;
		}
		else if (alignment == Align.LEFT)
		{
			imgOffsetX = 0;
		}
		else if (alignment == Align.RIGHT)
		{
			imgOffsetX = bounds.width - imgD.width;
		}
		else
		{
			throw new MazeException(alignment.toString());
		}

		int imgX = bounds.x + imgOffsetX;
		int imgY = bounds.y + imgOffsetY;

		g.drawImage(img,
			imgX,
			imgY,
			Maze.getInstance().getComponent());
	}

	/*-------------------------------------------------------------------------*/
	public static void drawStringCentered(
		Graphics2D g,
		String s,
		Rectangle bounds,
		Align alignment,
		Color foreground,
		Color background)
	{
		FontMetrics fm = g.getFontMetrics();

		int textHeight = fm.getAscent();
		int textWidth = fm.stringWidth(s);

		// center the text on the Y axis
		int textY = bounds.y + bounds.height/2 + textHeight/2;

		int textX = bounds.x;
		if (alignment == Align.CENTER)
		{
			// center the text on the X axis
			textX = bounds.x + bounds.width/2 - textWidth/2;
		}
		else if (alignment == Align.RIGHT)
		{
			// align right
			textX = bounds.x + bounds.width - textWidth;
		}

		if (foreground == null)
		{
			foreground = MazeRendererFactory.LABEL_FOREGROUND;
		}

		if (background != null)
		{
			g.setColor(background);
			g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		g.setColor(foreground);
		g.drawString(s, textX, textY);
	}

	/*-------------------------------------------------------------------------*/
	class Listener implements KeyListener, MouseListener, MouseMotionListener
	{
		/*----------------------------------------------------------------------*/
		public void keyPressed(KeyEvent e)
		{
			queue.offer(e);
		}

		/*----------------------------------------------------------------------*/
		public void keyReleased(KeyEvent e)
		{
			queue.offer(e);
		}

		/*----------------------------------------------------------------------*/
		public void keyTyped(KeyEvent e)
		{
			queue.offer(e);
		}

		/*----------------------------------------------------------------------*/
		public void mouseClicked(MouseEvent e)
		{
			queue.offer(e);
		}

		/*----------------------------------------------------------------------*/
		public void mouseEntered(MouseEvent e)
		{
			queue.offer(e);
		}

		/*----------------------------------------------------------------------*/
		public void mouseExited(MouseEvent e)
		{
			queue.offer(e);
		}

		/*----------------------------------------------------------------------*/
		public void mousePressed(MouseEvent e)
		{
			queue.offer(e);
		}

		/*----------------------------------------------------------------------*/
		public void mouseReleased(MouseEvent e)
		{
			queue.offer(e);
		}

		/*----------------------------------------------------------------------*/
		public void mouseDragged(MouseEvent e)
		{
			queue.offer(e);
		}

		/*----------------------------------------------------------------------*/
		public void mouseMoved(MouseEvent e)
		{
			queue.offer(e);
		}
	}
}
