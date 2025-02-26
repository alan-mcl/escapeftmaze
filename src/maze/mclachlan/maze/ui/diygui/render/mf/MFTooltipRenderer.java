package mclachlan.maze.ui.diygui.render.mf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import mclachlan.diygui.DIYTooltip;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.data.Database;

/**
 *
 */
public class MFTooltipRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height,
		Widget widget)
	{
		DIYTooltip tooltip = (DIYTooltip)widget;
		String text = tooltip.getText();

		if (text == null)
		{
			// error blob
			g.setColor(Color.RED);
			g.drawOval(x-20, y-20, 40, 40);
			return;
		}

		int border = 5;
		Dimension d = DIYToolkit.getDimension(tooltip.getText());

		Rectangle ttBounds = new Rectangle(
			x +2,
			y -2 -d.height -border*2,
			d.width +border*2,
			d.height +border*2);

		int overshoot = ttBounds.x + ttBounds.width +10 - DIYToolkit.getInstance().getContentPane().width;
		if (overshoot > 0)
		{
			// shift left so as not to run off the screen
			ttBounds.x -= overshoot;
		}

		DIYToolkit.drawImageTiled(g,
			Database.getInstance().getImage("ui/mf/tooltip/back"),
			ttBounds.x, ttBounds.y, ttBounds.width, ttBounds.height);

		DIYToolkit.drawStringCentered(g,
			text,
			ttBounds,
			DIYToolkit.Align.CENTER,
			Colours.LABEL_TEXT,
			null);

		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
			g.drawRect(ttBounds.x, ttBounds.y, ttBounds.width, ttBounds.height);
		}
	}
}
