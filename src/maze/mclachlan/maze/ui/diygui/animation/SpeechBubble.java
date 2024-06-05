package mclachlan.maze.ui.diygui.animation;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.DiyGuiUserInterface;

/**
 *
 */
public class SpeechBubble
{
	private static final List<SpeechBubble> bubbles = new ArrayList<>();

	private String text;
	private Rectangle origination;

	private Color colour;
	private PlayerCharacter pc;
	private Orientation orientation;
	private int bHeight, bWidth;
	private int bX, bY;
	private Color col1;
	private Color col2;
	private static final int INSET = 40;
	private static final int PADDING = 5;
	private static final int POLY_INTRUSION = 30;
	private static final int POLY_FATNESS = 5;
	private RoundRectangle2D rect;
	private Polygon poly;
	private int textHeight, textWidth;

	private Rectangle bounds;
	private int index;
	private ArrayList<String> strings;

	/**
	 * Orientation of the speech bubble relative to its origination bounds
	 */
	public enum Orientation
	{
		LEFT, RIGHT, ABOVE, BELOW, ABOVE_LEFT, ABOVE_RIGHT, BELOW_LEFT, BELOW_RIGHT, CENTERED
	}


	/*-------------------------------------------------------------------------*/
	public SpeechBubble(
		Color colour,
		String text,
		Rectangle origination,
		Orientation orientation)
	{
		this.colour = colour;
		this.text = text;
		this.origination = origination;
		this.orientation = orientation;

		synchronized (SpeechBubble.bubbles)
		{
			bubbles.add(this);
		}
	}

	public static void remove(SpeechBubble speechBubble)
	{
		synchronized (bubbles)
		{
			bubbles.remove(speechBubble);
		}
	}

	public void draw(Graphics2D g, boolean modal)
	{
		g.setPaint(new GradientPaint(bX, bY, col1, bX, bY + bHeight, col2, true));
		if (poly != null)
		{
			g.fill(poly);
		}
		g.fill(rect);

		if (modal)
		{
			g.setPaint(colour);
			if (poly != null)
			{
				g.draw(poly);
			}
			g.draw(rect);
		}

//		 debug
//			if (debug)
//			{
//				g.drawRect(origination.x, origination.y, origination.width, origination.height);
//			}

		g.setColor(Color.DARK_GRAY);
		int line = 1;
		for (String s : strings)
		{
			g.drawString(s, bX + PADDING, bY + PADDING + line * textHeight);
			line++;
		}
	}

	/*-------------------------------------------------------------------------*/
	void computeBounds(Graphics2D g, PlayerCharacter pc, int index)
	{
		this.pc = pc;
		this.index = index;

		FontMetrics fm = g.getFontMetrics();

		int maxWidth;
		if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			maxWidth = DiyGuiUserInterface.SCREEN_WIDTH / 3;
		}
		else
		{
			maxWidth = DiyGuiUserInterface.SCREEN_WIDTH / 4;
		}
		textWidth = Math.min(fm.stringWidth(text), maxWidth);

		strings = new ArrayList<>();
		String[] paragraphs = text.split("\\n");

		for (int i = 0; i < paragraphs.length; i++)
		{
			String paragraph = paragraphs[i];
			strings.addAll(DIYToolkit.wrapText(
				paragraph, Maze.getInstance().getComponent().getGraphics(), maxWidth));
		}

		textHeight = fm.getAscent();

		bHeight = textHeight * strings.size() + PADDING * 2;
		bWidth = textWidth + PADDING * 3;
		switch (orientation)
		{
			case RIGHT ->
			{
				bX = origination.x + origination.width + INSET;
				bY = origination.y + origination.height / 2 - bHeight / 2;
			}
			case LEFT ->
			{
				bX = origination.x - INSET - bWidth;
				bY = origination.y + origination.height / 2 - bHeight / 2;
			}
			case ABOVE ->
			{
				bX = origination.x + origination.width / 2 - bWidth / 2;
				bY = origination.y - INSET - bHeight;
			}
			case BELOW ->
			{
				bX = origination.x + origination.width / 2 - bWidth / 2;
				bY = origination.y + origination.height + INSET;
			}
			case ABOVE_LEFT ->
			{
				bX = origination.x - INSET / 2 - bWidth;
				bY = origination.y - INSET / 2 - bHeight;
			}
			case ABOVE_RIGHT ->
			{
				bX = origination.x + origination.width + INSET / 2;
				bY = origination.y - INSET / 2 - bHeight;
			}
			case BELOW_LEFT ->
			{
				bX = origination.x - INSET / 2 - bWidth;
				bY = origination.y + origination.height + INSET / 2;
			}
			case BELOW_RIGHT ->
			{
				bX = origination.x + origination.width + INSET / 2;
				bY = origination.y + origination.height + INSET / 2;
			}
			case CENTERED ->
			{
				bX = origination.x + origination.width / 2 - bWidth / 2;
				bY = origination.y + origination.height / 2 - bHeight / 2;
			}
		}


		bounds = new Rectangle(bX, bY, bWidth, bHeight);

		//
		// check and see if these bounds overlap an existing speech bubble
		//
		synchronized (bubbles)
		{
			int moves = 0;

			ListIterator<SpeechBubble> li = bubbles.listIterator();
			while (li.hasNext())
			{
				if (moves > 2)
				{
					// prevent infinite looping
					break;
				}

				SpeechBubble a = li.next();

				if (a == this)
				{
					continue;
				}

				if (a.pc == this.pc)
				{
					// same origin. kill the other one.
					// todo
//						a.duration = 0;
//						li.remove();
					break;
				}

				if (a != this && this.pc != a.pc && this.index != a.index &&
					a.bounds != null &&
					this.bounds.intersects(a.bounds))
				{
					if (this.index < a.index)
					{
						// place this bubble above the other one
						this.bY = a.bY - this.bHeight - PADDING;
					}
					else
					{
						// place this bubble below the other one
						this.bY = a.bY + a.bHeight + PADDING;
					}

					// reset to the start again
					li = bubbles.listIterator();
					moves++;
				}
			}
		}

		col1 = colour.brighter();
		col2 = colour.darker();

		rect = new RoundRectangle2D.Double(bX, bY, bWidth, bHeight, 10, 10);

		switch (orientation)
		{
			case LEFT -> poly = new Polygon(
				new int[]
					{
						origination.x + POLY_INTRUSION,
						bX + bWidth,
						bX + bWidth,
					},
				new int[]
					{
						origination.y + origination.height / 2,
						bY + bHeight / 2 - POLY_FATNESS,
						bY + bHeight / 2 + POLY_FATNESS,
					},
				3);
			case RIGHT -> poly = new Polygon(
				new int[]
					{
						origination.x + origination.width - POLY_INTRUSION,
						bX,
						bX,
					},
				new int[]
					{
						origination.y + origination.height / 2,
						bY + bHeight / 2 - POLY_FATNESS,
						bY + bHeight / 2 + POLY_FATNESS,
					},
				3);
			case ABOVE -> poly = new Polygon(
				new int[]
					{
						origination.x + origination.width / 2,
						bX + bWidth / 2 - POLY_FATNESS,
						bX + bWidth / 2 + POLY_FATNESS,
					},
				new int[]
					{
						origination.y + POLY_INTRUSION,
						bY + bHeight,
						bY + bHeight,
					},
				3);
			case BELOW -> poly = new Polygon(
				new int[]
					{
						origination.x + origination.width / 2,
						bX + bWidth / 2 - POLY_FATNESS,
						bX + bWidth / 2 + POLY_FATNESS,
					},
				new int[]
					{
						origination.y + origination.height - POLY_INTRUSION,
						bY,
						bY,
					},
				3);
			case ABOVE_LEFT -> poly = new Polygon(
				new int[]
					{
						origination.x + POLY_INTRUSION,
						bX + bWidth,
						bX + bWidth - POLY_FATNESS,
					},
				new int[]
					{
						origination.y + POLY_INTRUSION,
						bY + bHeight - POLY_FATNESS / 2,
						bY + bHeight,
					},
				3);
			case ABOVE_RIGHT -> poly = new Polygon(
				new int[]
					{
						origination.x + origination.width - POLY_INTRUSION,
						bX,
						bX + POLY_FATNESS,
					},
				new int[]
					{
						origination.y + POLY_INTRUSION,
						bY + bHeight - POLY_FATNESS / 2,
						bY + bHeight,
					},
				3);
			case BELOW_LEFT -> poly = new Polygon(
				new int[]
					{
						origination.x + POLY_INTRUSION,
						bX + bWidth - POLY_FATNESS,
						bX + bWidth,
					},
				new int[]
					{
						origination.y + origination.height - POLY_INTRUSION,
						bY,
						bY + POLY_FATNESS,
					},
				3);
			case BELOW_RIGHT -> poly = new Polygon(
				new int[]
					{
						origination.x + origination.width - POLY_INTRUSION,
						bX + POLY_FATNESS,
						bX,
					},
				new int[]
					{
						origination.y + origination.height - POLY_INTRUSION,
						bY,
						bY + POLY_FATNESS,
					},
				3);
			case CENTERED ->
			{
				// no prong
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public Rectangle getOrigination()
	{
		return origination;
	}

	public void setOrigination(Rectangle origination)
	{
		this.origination = origination;
	}

	public Color getColour()
	{
		return colour;
	}

	public void setColour(Color colour)
	{
		this.colour = colour;
	}

	public Orientation getOrientation()
	{
		return orientation;
	}

	public void setOrientation(
		Orientation orientation)
	{
		this.orientation = orientation;
	}
}
